/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases;

import agents.EconomicAgent;
import goods.InventoryListener;
import agents.firm.Department;
import agents.firm.Firm;
import agents.firm.purchases.inventoryControl.InventoryControl;
import agents.firm.purchases.inventoryControl.Level;
import agents.firm.purchases.prediction.PurchasesPredictor;
import agents.firm.purchases.prediction.RecursivePurchasesPredictor;
import agents.firm.purchases.pricing.BidPricingStrategy;
import agents.firm.purchases.pricing.decorators.MaximumBidPriceDecorator;
import agents.firm.sales.exploration.BuyerSearchAlgorithm;
import agents.firm.sales.exploration.SellerSearchAlgorithm;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import financial.market.Market;
import financial.utilities.ActionsAllowed;
import financial.utilities.PurchaseResult;
import financial.utilities.Quote;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.Control;
import model.utilities.Deactivatable;
import model.utilities.logs.*;
import model.utilities.scheduler.Priority;
import model.utilities.stats.collectors.PurchasesDepartmentData;
import model.utilities.stats.collectors.enums.PurchasesDataType;
import sim.engine.SimState;
import sim.engine.Steppable;



/**
 * The  purchases department's job is to keep supplying ONE GOOD-TYPE <p>
 *     Purchase department has 3 main components within it:
 *     <ul>
 <li>Inventory control:
 Inventory control is the routine that tells the department when to place a buy order. How this happens doesn't matter for the purchase department. All it has to do is to answer the order.
 I have to be careful how I call buy: I shouldn't call buy() directly or I end up screwing the expected matched quotes chain of events          </li>
 <li>Pricing:
 This is called everytime the purchases department need to price its goods. The pricing strategy may call updateOfferPrices to force the quotes' prices to call maxPrice.  </li>
 <li>Buy routine:
 This is the part implemented in the purchases department class. It is called by inventory control and either place quote or shop or both or whatever.  </li>
 </ul>
 * </p>
 */
public class PurchasesDepartment implements Deactivatable, Department, LogNode {


    public static Class<? extends PurchasesPredictor> defaultPurchasePredictor =
            RecursivePurchasesPredictor.class;
    /**
     * The weekly budget given by the firm to this purchase department to carry out its tasks
     */
    private int budgetGiven;

    /**
     * the amount of weekly budget already spent
     */
    private int budgetSpent;

    /**
     * The firm that owns this department
     */
    final private Firm firm;

    /**
     * What good-type are you looking to buy?
     */
    final private GoodType goodType;

    /**
     * The market you are going to use to buy stuff from
     */
    private Market market;

    /**
     * What price to choose?
     */
    private BidPricingStrategy pricingStrategy;

    /**
     * The strategy we are using to choose how to control the inventory
     */
    private Control control;

    /**
     * At what priority does the purchase department act when trading. It can be changed by its strategies.
     */
    private Priority tradePriority = Priority.STANDARD;


    /**
     * what is the department doing RIGHT NOW?
     */
    private PurchasesDepartmentStatus status = PurchasesDepartmentStatus.IDLE;

    /**
     * Since the purchases department is single market-single goodtype we only need to remember AT MOST one quote. This is where we remember it
     */
    private Quote quotePlaced = null;

    /**
     * Sometimes inventory control tells us to buy a good while we are in the middle of buying another.
     * If so set this flag to true so that we can buy such good when we are done with the previous one.
     */
    private boolean queuedBuy = false;

    /**
     * this is set to true when we scheduled a buy but haven't yet placed it
     */
    private boolean aboutToBuy = false;


    /**
     * algorithm to search the registry for opponents
     */
    private BuyerSearchAlgorithm opponentSearch;

    /**
     * Algorithm to search the registry for suppliers
     */
    private SellerSearchAlgorithm supplierSearch;

    /**
     * What price was paid for the last good bought?
     */
    private int lastClosingPrice = -1;

    /**
     * when you placed your last bid, what was your offer price. I think it is more informative than querying maxPrice() because
     * it takes some time between maxPrice being updated and then update moving in the quotes
     */
    private int lastOfferedPrice = -1;


    private final PurchasesDepartmentData purchasesData;

    /**
     * The predictor for next purchases. It is set in the constructor but that can be changed
     */
    protected PurchasesPredictor predictor;

    /**
     * an explicit link to the model, to reschedule yourself
     */
    private final MacroII model;

    /**
     * a counter to check daily inflows and outflows. Created at constructor, started and turned off. Steps itself independetly
     */
    protected InflowOutflowCounter counter;

    /**
     * a counter to compute the daily average closing price from filled quotes. Resets itself at DAWN every day
     */
    private AveragePurchasePriceCounter averagePriceCounter;


    protected boolean startWasCalled=false;


    protected PurchasesDepartment(int budgetGiven, Firm firm, Market market,
                                  MacroII model) {
        //initialize objects
        this.budgetGiven = budgetGiven;
        this.budgetSpent = 0;
        this.firm = firm;
        this.market = market;
        this.goodType = market.getGoodType();
        this.model = model;

        //register as a buyer
        market.registerBuyer(firm);

        predictor = PurchasesPredictor.Factory.newPurchasesPredictor(defaultPurchasePredictor,this);


        counter = new InflowOutflowCounter(model,firm,goodType);
        averagePriceCounter = new AveragePurchasePriceCounter(this);
        purchasesData = new PurchasesDepartmentData();






    }

    /**
     * The  purchases department's job is to keep ONE PLANT supplied so that
     * @param budgetGiven  budget given to the department by the purchases department
     * @param firm the firm owning the department
     * @param market the market to buy from
     */
    protected PurchasesDepartment(int budgetGiven, Firm firm, Market market) {
        this(budgetGiven,firm,market,firm.getModel());


    }

    /**
     * This factory method returns an INCOMPLETE and NOTFUNCTIONING purchase department objects as it lacks all the strategies it needs to act.
     * This is useful if the caller wants to assign a specific rule from its side; otherwise stick with the other factories
     * @param budgetGiven the amount of money given to the purchase department!
     * @param firm the firm owning the department
     * @param market the market the department beints to
     * @param  model the reference to the model object
     *
     */
    public static PurchasesDepartment getEmptyPurchasesDepartment(int budgetGiven, Firm firm, Market market,
                                                                  MacroII model){
        //create the simple purchases department
        //return it
        return new PurchasesDepartment(budgetGiven,firm,market,model);

    }

    /**
     * This factory method returns an INCOMPLETE and NOTFUNCTIONING purchase department objects as it lacks all the strategies it needs to act.
     * This is useful if the caller wants to assign a specific rule from its side; otherwise stick with the other factories
     * @param budgetGiven the amount of money given to the purchase department!
     * @param firm the firm owning the department
     * @param market the market the department beints to
     */
    public static PurchasesDepartment getEmptyPurchasesDepartment(int budgetGiven, Firm firm, Market market){
        //create the simple purchases department
        //return it
        return getEmptyPurchasesDepartment(budgetGiven, firm, market,firm.getModel());

    }

    /**
     *  This is the simplest factory method for the purchases department. It randomly chooses its
     *  search algorithms, inventory control and pricing strategy (these last two will not be integrated).
     *  The reason I am going through all this is to avoid having a reference to purchases department leak out from constructor
     * @param budgetGiven  budget given to the department by the purchases department
     * @param firm the firm owning the department
     * @param market the market to buy from
     */
    public static
    FactoryProducedPurchaseDepartment<InventoryControl,BidPricingStrategy,BuyerSearchAlgorithm,SellerSearchAlgorithm>
    getRandomPurchaseDepartment(int budgetGiven,  Firm firm,  Market market){
        //create the simple purchases department
        PurchasesDepartment instance = new PurchasesDepartment(budgetGiven,firm,market); //call the constructor
        //create random inventory control and assign it
        InventoryControl inventoryControl = PurchasesRuleFactory.randomInventoryControl(instance);
        instance.setControl(inventoryControl);
        // create a random bid pricing strategy and assign it
        BidPricingStrategy pricingStrategy = PurchasesRuleFactory.randomBidPricingStrategy(instance);
        instance.setPricingStrategy(pricingStrategy);
        //create a random buyer search algorithm and assign it
        BuyerSearchAlgorithm buyerSearchAlgorithm = BuyerSearchAlgorithm.Factory.randomBuyerSearchAlgorithm(market,firm);
        instance.setOpponentSearch(buyerSearchAlgorithm);
        //create a random seller search algorithm and assign it
        SellerSearchAlgorithm sellerSearchAlgorithm = SellerSearchAlgorithm.Factory.randomSellerSearchAlgorithm(market,firm);
        instance.setSupplierSearch(sellerSearchAlgorithm);

        //create the container
        FactoryProducedPurchaseDepartment<InventoryControl,BidPricingStrategy,BuyerSearchAlgorithm,SellerSearchAlgorithm>
                container =
                new FactoryProducedPurchaseDepartment<>(instance,inventoryControl,pricingStrategy,
                        buyerSearchAlgorithm,sellerSearchAlgorithm);
        //check it's correct
        assert container.getDepartment() == instance;
        assert container.getInventoryControl() == instance.control;
        assert container.getBidPricing() == instance.pricingStrategy;
        assert container.getBuyerSearch() == instance.opponentSearch;
        assert container.getSellerSearch() == instance.supplierSearch;

        //finally: return it!
        return container;
    }

    /**
     * This factor for purchases department is used when we want the department to follow an integrated rule:
     * inventory control and pricing rule are the same object. <br>
     * Leaving any of the type arguments null will make the constructor generate a rule at random
     * @param budgetGiven the budget given to the department by the firm
     * @param firm the firm owning the department
     * @param market the market the department dabbles in
     * @param integratedControl the type of rule that'll be both BidPricing and InventoryControl
     * @param buyerSearchAlgorithmType the algorithm the buyer follows to search for competitors
     * @param sellerSearchAlgorithmType the algorithm the buyer follows to search for suppliers
     * @return a new instance of PurchasesDepartment
     */
    public static <IC extends InventoryControl & BidPricingStrategy,BS extends BuyerSearchAlgorithm, SS extends SellerSearchAlgorithm>
    FactoryProducedPurchaseDepartment<IC,IC,BS,SS> getPurchasesDepartmentIntegrated
    (int budgetGiven,  Firm firm,  Market market,
      Class<IC> integratedControl,  Class<BS> buyerSearchAlgorithmType,  Class<SS> sellerSearchAlgorithmType )
    {

        //create the simple purchases department
        PurchasesDepartment instance = new PurchasesDepartment(budgetGiven,firm,market); //call the constructor

        //create inventory control and assign it
        IC bidPricingStrategy;
        if(integratedControl == null) //if null randomize
            bidPricingStrategy =(IC) PurchasesRuleFactory.randomIntegratedRule(instance);
        else //otherwise instantiate the specified one
            bidPricingStrategy= PurchasesRuleFactory.newIntegratedRule(integratedControl,instance);
        instance.setPricingStrategy(bidPricingStrategy);
        instance.setControl(bidPricingStrategy);



        //create a buyer search algorithm and assign it
        BS buyerSearchAlgorithm;
        if(buyerSearchAlgorithmType == null)
            buyerSearchAlgorithm = (BS) BuyerSearchAlgorithm.Factory.randomBuyerSearchAlgorithm(market,firm);
        else
            buyerSearchAlgorithm = BuyerSearchAlgorithm.Factory.newBuyerSearchAlgorithm(buyerSearchAlgorithmType,market,firm);
        instance.setOpponentSearch(buyerSearchAlgorithm);


        //create a random seller search algorithm and assign it
        SS sellerSearchAlgorithm;
        if(sellerSearchAlgorithmType == null)
            sellerSearchAlgorithm = (SS) SellerSearchAlgorithm.Factory.randomSellerSearchAlgorithm(market,firm);
        else
            sellerSearchAlgorithm = SellerSearchAlgorithm.Factory.newSellerSearchAlgorithm(sellerSearchAlgorithmType,market,firm);
        instance.setSupplierSearch(sellerSearchAlgorithm);

        //finally: return it!
        FactoryProducedPurchaseDepartment<IC,IC,BS,SS> container =
                new FactoryProducedPurchaseDepartment<>(instance,bidPricingStrategy,bidPricingStrategy,
                        buyerSearchAlgorithm,sellerSearchAlgorithm);
        //check it's correct
        assert container.getDepartment() == instance;
        assert container.getInventoryControl() == instance.control;
        assert container.getBidPricing() == instance.pricingStrategy;
        assert container.getBuyerSearch() == instance.opponentSearch;
        assert container.getSellerSearch() == instance.supplierSearch;


        //finally: return it!
        return container;

    }


    /**
     *  This is the simplest factory method for the purchases department. It creates  controls and strategies as passed. Whenever
     *  a type is not passed (is null) then a random one is instantiated.
     *  The reason I am going through all this is to avoid having a reference to purchases department leak out from constructor
     * @param budgetGiven  budget given to the department by the purchases department
     * @param firm the firm owning the department
     * @param market the market to buy from
     * @param inventoryControlType the type of inventory control desired. If null it is randomized
     * @param bidPricingStrategyType the pricing strategy desired. If null it is randomized
     * @param buyerSearchAlgorithmType the buyer search algorithm. If null it is randomized
     * @param  sellerSearchAlgorithmType the seller search algorithm desired. If null it is randomized
     */
    public static
    <IC extends InventoryControl, BP extends BidPricingStrategy,BS extends BuyerSearchAlgorithm, SS extends SellerSearchAlgorithm>
    FactoryProducedPurchaseDepartment<IC,BP,BS,SS> getPurchasesDepartment(int budgetGiven,  Firm firm,  Market market,
                                                                           Class<IC> inventoryControlType,
                                                                           Class<BP> bidPricingStrategyType,
                                                                           Class<BS> buyerSearchAlgorithmType,
                                                                           Class<SS> sellerSearchAlgorithmType){
        //create the simple purchases department
        PurchasesDepartment instance = new PurchasesDepartment(budgetGiven,firm,market); //call the constructor

        //create inventory control and assign it
        IC inventoryControl;
        if(inventoryControlType == null) //if null randomize
            inventoryControl = (IC)PurchasesRuleFactory.randomInventoryControl(instance);
        else //otherwise instantiate the specified one
            inventoryControl= PurchasesRuleFactory.newInventoryControl(inventoryControlType,instance);
        instance.setControl(inventoryControl);

        //create BidPricingStrategy and assign it
        BP bidPricingStrategy;
        if(bidPricingStrategyType == null) //if null randomize
            bidPricingStrategy = (BP)PurchasesRuleFactory.randomBidPricingStrategy(instance);
        else //otherwise instantiate the specified one
            bidPricingStrategy= PurchasesRuleFactory.newBidPricingStrategy(bidPricingStrategyType,instance);
        instance.setPricingStrategy(bidPricingStrategy);

        //create a buyer search algorithm and assign it
        BS buyerSearchAlgorithm;
        if(buyerSearchAlgorithmType == null)
            buyerSearchAlgorithm = (BS)BuyerSearchAlgorithm.Factory.randomBuyerSearchAlgorithm(market,firm);
        else
            buyerSearchAlgorithm = BuyerSearchAlgorithm.Factory.newBuyerSearchAlgorithm(buyerSearchAlgorithmType,market,firm);
        instance.setOpponentSearch(buyerSearchAlgorithm);


        //create a random seller search algorithm and assign it
        SS sellerSearchAlgorithm;
        if(sellerSearchAlgorithmType == null)
            sellerSearchAlgorithm = (SS) SellerSearchAlgorithm.Factory.randomSellerSearchAlgorithm(market,firm);
        else
            sellerSearchAlgorithm = SellerSearchAlgorithm.Factory.newSellerSearchAlgorithm(sellerSearchAlgorithmType,market,firm);
        instance.setSupplierSearch(sellerSearchAlgorithm);

        //create the container
        FactoryProducedPurchaseDepartment<IC,BP,BS,SS> container =
                new FactoryProducedPurchaseDepartment<>(instance,inventoryControl,bidPricingStrategy,
                        buyerSearchAlgorithm,sellerSearchAlgorithm);
        //check it's correct
        assert container.getDepartment() == instance;
        assert container.getInventoryControl() == instance.control;
        assert container.getBidPricing() == instance.pricingStrategy;
        assert container.getBuyerSearch() == instance.opponentSearch;
        assert container.getSellerSearch() == instance.supplierSearch;


        //finally: return it!
        return container;
    }

    /**
     *  This factory reads in the String names of the types of class it has to instantiate. They can't be null.
     *  Because I don't know the type, the factory object returned assumes the most generic possibilities
     * @param budgetGiven  budget given to the department by the purchases department
     * @param firm the firm owning the department
     * @param market the market to buy from
     * @param inventoryControlType the name of inventory control desired.
     * @param bidPricingStrategyType the name of pricing strategy desired.
     * @param buyerSearchAlgorithmType the name of buyer search algorithm.
     * @param  sellerSearchAlgorithmType the name seller search algorithm desired.
     *<IC extends InventoryControl, BP extends BidPricingStrategy,BS extends BuyerSearchAlgorithm, SS extends SellerSearchAlgorithm>
     */
    public static
    FactoryProducedPurchaseDepartment<InventoryControl,BidPricingStrategy,BuyerSearchAlgorithm,SellerSearchAlgorithm>
    getPurchasesDepartment(int budgetGiven, Firm firm, Market market,
                            String inventoryControlType,  String bidPricingStrategyType,
                            String buyerSearchAlgorithmType, String sellerSearchAlgorithmType){
        //create the simple purchases department
        PurchasesDepartment instance = new PurchasesDepartment(budgetGiven,firm,market); //call the constructor
        //create random inventory control and assign it
        InventoryControl inventoryControl = PurchasesRuleFactory.newInventoryControl(inventoryControlType,instance);
        instance.setControl(inventoryControl);
        // create a random bid pricing strategy and assign it
        BidPricingStrategy pricingStrategy =PurchasesRuleFactory.newBidPricingStrategy(bidPricingStrategyType,instance);
        instance.setPricingStrategy(pricingStrategy);
        //create a random buyer search algorithm and assign it
        BuyerSearchAlgorithm buyerSearchAlgorithm = BuyerSearchAlgorithm.Factory.newBuyerSearchAlgorithm(buyerSearchAlgorithmType, market, firm);
        instance.setOpponentSearch(buyerSearchAlgorithm);
        //create a random seller search algorithm and assign it
        SellerSearchAlgorithm sellerSearchAlgorithm = SellerSearchAlgorithm.Factory.newSellerSearchAlgorithm(sellerSearchAlgorithmType,market,firm);
        instance.setSupplierSearch(sellerSearchAlgorithm);

        //create the container
        FactoryProducedPurchaseDepartment<InventoryControl,BidPricingStrategy,BuyerSearchAlgorithm,SellerSearchAlgorithm>
                container =
                new FactoryProducedPurchaseDepartment<>(instance,inventoryControl,pricingStrategy,
                        buyerSearchAlgorithm,sellerSearchAlgorithm);
        //check it's correct
        assert container.getDepartment() == instance;
        assert container.getInventoryControl() == instance.control;
        assert container.getBidPricing() == instance.pricingStrategy;
        assert container.getBuyerSearch() == instance.opponentSearch;
        assert container.getSellerSearch() == instance.supplierSearch;

        //finally: return it!
        return container;
    }

    /**
     * This method just calls the pricing strategy but if the market best ask is visible and lower than the pricing says it is then it just defaults to it. <br>
     * Also whatever is the result, it never returns more than the available budget
     * @param type the type of good we are going to buy
     * @param market the market we are buying it from
     * @return the max price we are willing to pay!
     */
    public int maxPrice(GoodType type, Market market)
    {


        return pricingStrategy.maxPrice(type);




    }


    /**
     * if approached by a peddler, how much is the purchase department willing to pay? It just calls maxPrice() if it's willing to buy
     * @return maxPrice or -1 if it doesn't want to buy
     */
    public int maximumOffer(Good g){
        if(!canBuy())
            return -1;
        else
            return maxPrice(g.getType(),getMarket());

    }

    /**
     * React to a bid filled quote
     * @param g good bought
     * @param price price of the good.
     */
    public void reactToFilledQuote(Good g, int price, EconomicAgent seller){

        budgetSpent += price; //budget recording
        lastClosingPrice = price;

        //two things: either I was waiting for it, or it was immediate
        if(status == PurchasesDepartmentStatus.WAITING){ //If I was waiting for it
            assert quotePlaced != null;
            assert getFirm().has(g) || g.getType().isLabor(); //it should be ours now! (unless we are buying labor in which case it makes no sense to "have"

            quotePlaced = null; //remove the memory!
            status = PurchasesDepartmentStatus.IDLE;

            if(queuedBuy){
                queuedBuy = false; //take down the flag
                buy(goodType,market);

            }
        }
        else{
            assert status == PurchasesDepartmentStatus.PLACING_QUOTE : "current status: " + status + ", quote placed: " + quotePlaced;
            assert quotePlaced == null;
            assert getFirm().has(g)  || g.getType().isLabor(); //it should be ours now!
            //we will return to the buy method so we don't need to deal with anything inside here.
        }

        averagePriceCounter.getNotifiedOfFilledQuote(price);
        handleNewEvent(new LogEvent(this, LogLevel.TRACE,"Bought at price:{}",price));
    }

    /**
     * You are calling this because you can't, or won't, use the order book. Basically it manually searches around
     */
    public void shop(){


        model.scheduleSoon(ActionOrder.TRADE, new Steppable() {
            @Override
            public void step(SimState state) {

                assert control.canBuy(); //make sure it's okay
                status = PurchasesDepartmentStatus.SHOPPING; //put yourself in shopping mood
                int maxPrice = maxPrice(goodType,market); //maximum we are willing to pay

                EconomicAgent seller = supplierSearch.getBestInSampleSeller(); //get the best seller available
                //if we couldn't find any
                if(seller == null){
                    supplierSearch.reactToFailure(seller, PurchaseResult.NO_MATCH_AVAILABLE);
                    shop(); //call shop again soon!

                }
                else
                {
                    Quote sellerQuote = seller.askedForASaleQuote(getFirm(), goodType); //ask again for a price offer
                    assert sellerQuote.getPriceQuoted() >= 0 ; //can't be negative!!!
                    if(maxPrice >= sellerQuote.getPriceQuoted()) //if the match is good:
                    {
                        int finalPrice = market.price(sellerQuote.getPriceQuoted(),maxPrice);

                        //build a fake buyer quote for stat collection
                        Quote buyerQuote = Quote.newBuyerQuote(getFirm(),maxPrice,goodType);
                        buyerQuote.setOriginator(PurchasesDepartment.this);


                        //TRADE
                        PurchaseResult result = seller.shopHere(buyerQuote,sellerQuote);
                        assert result.equals(PurchaseResult.SUCCESS) : "haven't coded what happens otherwise";

                        //record info
                        budgetSpent += result.getPriceTrade(); lastClosingPrice = result.getPriceTrade(); //spent!
                        handleNewEvent(new LogEvent(this, LogLevel.TRACE,"Bought at price:{}",finalPrice));

                        status = PurchasesDepartmentStatus.IDLE; //you are done!
                        supplierSearch.reactToSuccess(seller,PurchaseResult.SUCCESS);


                        if(queuedBuy) //if we need to buy another good, do it!
                        {
                            queuedBuy = false; //reset the queue

                            buy(goodType,market);
                        }
                    }
                    else{   //we didn't make it!
                        supplierSearch.reactToFailure(seller,PurchaseResult.PRICE_REJECTED);
                        shop(); //call shop again soon!

                    }

                }

            }
        });



    }



    /**
     * This is the method used by whoever controls the purchase department to order it to go ahead and buy
     */
    public void buy(){

        if(status == PurchasesDepartmentStatus.IDLE)
        { //if we were waiting for the order to be given, just go ahead and buy
            assert !queuedBuy : quotePlaced + " ---- " + maxPrice(getGoodType(),getMarket()) + "  ///---\\\\" ; //we can't be here if there is a buy order in queue!
            buy(goodType,market);
        }
        else
        { //otherwise 2 possibilities: we are in the process of trading or we still haven't reacted to a filled quote. So wait
//            assert !queuedBuy;
            queuedBuy = true;

        }

        //log it
        handleNewEvent(new LogEvent(this, LogLevel.TRACE,"Tasked to buy"));



    }


    private void buy( GoodType type, Market market)
    {

        assert !queuedBuy; //as we call this there can't be any additional order to buy in queue. But this flag might be turned on later as we trade.
        assert quotePlaced == null; //there shouldn't be any quote already or this is weird.


        if(market.getBuyerRole() == ActionsAllowed.QUOTE)
        {
            placeQuote(type, market);



        }
        else{
            status = PurchasesDepartmentStatus.SHOPPING; //set your status to shopping!
            shop(); //shop around if you can't use the order book
        }


    }

    /**
     * Place quote through the phase scheduler at the nearest/current TRADE phase
     * @param type the type of good we are selling
     * @param market the market to trade into
     */
    private void placeQuote(final GoodType type, final Market market) {

        //this is a valid call only when you are idle!
        assert status == PurchasesDepartmentStatus.IDLE;

        //you are about to place a quote
        status = PurchasesDepartmentStatus.PLACING_QUOTE;

        model.scheduleSoon(ActionOrder.TRADE, new Steppable() {
            @Override
            public void step(SimState state) {



                if(status != PurchasesDepartmentStatus.PLACING_QUOTE) //if something came up while we were waiting, don't buy
                {
                    queuedBuy = true;
                    return;
                }

                lastOfferedPrice =maxPrice(type,market);
                Quote q = market.submitBuyQuote(firm,lastOfferedPrice,PurchasesDepartment.this); //submit the quote!




                if(q.getPriceQuoted() == -1) //if the quote is null
                {
                    assert q.getAgent() == null; //make sure we got back the right quote
                    //if we are here, it means that trade has been called, which has called react to filled quote and notified the inventory which probably called the inventory control.
                    //by now all that is past, we have a new good in inventory and we might have been ordered to buy again. If so, do it now.
                    if(queuedBuy)
                    { //if inventory control asked us to buy another good in the mean-time
                        queuedBuy = false;     //empty the queue
                        status = PurchasesDepartmentStatus.IDLE; //temporarily set yourself as idle
                        buy(goodType,market); //call another buy.
                    }
                    else{
                        status = PurchasesDepartmentStatus.IDLE; //if there is nothing to buy, stay idle
                    }

                }
                else
                {
                    assert q.getAgent() == firm; //make sure we got back the right quote
                    assert q.getPriceQuoted() >= 0; //make sure it's positive!
                    //we'll have to store it and wait
                    quotePlaced = q;
                    status = PurchasesDepartmentStatus.WAITING; //set your status to waiting

                }

            }
        },tradePriority);

    }


    public GoodType getGoodType() {
        return goodType;
    }

    public Firm getFirm() {
        return firm;
    }

    public void setControl( Control control) {
        if(this.control != null) //if there was one before, turn it off
            this.control.turnOff();
        this.control = control;
    }




    public void setPricingStrategy( BidPricingStrategy pricingStrategy) {
        if(this.pricingStrategy != null)   //if you had one before, turn it off
            this.pricingStrategy.turnOff();
        this.pricingStrategy = pricingStrategy;
    }

    /**
     * Generate a new pricing strategy of this specific type!
     * @param pricingStrategyType  the class of which the new pricing strategy will be
     */
    public void setPricingStrategy( Class<? extends BidPricingStrategy> pricingStrategyType) {
        if(this.pricingStrategy != null)   //if you had one before, turn it off
            this.pricingStrategy.turnOff();
        this.pricingStrategy = PurchasesRuleFactory.newBidPricingStrategy(pricingStrategyType,this);
    }



    public void setOpponentSearch( BuyerSearchAlgorithm opponentSearch) {
        if(this.opponentSearch != null)
            opponentSearch.turnOff();
        this.opponentSearch = opponentSearch;
    }

    public void setSupplierSearch( SellerSearchAlgorithm supplierSearch) {
        if(this.supplierSearch != null)
            supplierSearch.turnOff();
        this.supplierSearch = supplierSearch;
    }


    /**
     * Tells the purchase department to never buy above a specific value. Under the hood we are decorating the pricing strategy.
     * Once set, don't change!
     * @param reservationPrice the new maximum
     */
    public void setReservationPrice(int reservationPrice)
    {
        Preconditions.checkState(pricingStrategy != null, "Can't add a reservation wage until a pricing strategy is in place!");
        pricingStrategy = new MaximumBidPriceDecorator(pricingStrategy,reservationPrice);
    }

    /**
     * Whenever we can ONLY shop and we fail to do so, we call this method to know how much to wait before trying again
     * @return the wait time, or -1 if we are NOT going to try again!
     */
    public final double tryAgainNextTime(){
        return getFirm().getModel().getPeddlingSpeed();

    }

    /**
     * At weekend remove budget spent from total budget; purely accounting trick
     */
    public void weekEnd(double time) {
        assert budgetGiven >= budgetSpent;
        budgetGiven -= budgetSpent;
        budgetSpent = 0;
    }

    /**
     * Add some money to the purchaser's budget
     * @param additionalBudget additional money to spend by the purchases department
     */
    public void addToBudget(int additionalBudget){
        budgetGiven +=additionalBudget;

    }

    /**
     * This is called by a pricing strategy that decided to change prices. It only affects the quote if there is any
     */
    public void updateOfferPrices(){
        if(quotePlaced != null) //if you already have a quote
        {
            //call straight the buy algorithm
            final boolean queued =  cancelQuote();
            //schedule ASAP: the quote itself will only be placed on TRADE though, so that's okay!


            buy(); //try again!
            if(!queuedBuy && queued){
                //there was something in queue we should buy
                if( status != PurchasesDepartmentStatus.IDLE)
                    queuedBuy = true;     //empty the queue (unlesss you are waiting in which case you already have it full)

            }




        }






    }

    /**
     * removes the current quote in the market, returns the old "queuedbuy" boolean
     */
    public boolean cancelQuote(){
        assert status == PurchasesDepartmentStatus.WAITING;
        market.removeBuyQuote(quotePlaced); //remove the buy order
        quotePlaced = null; //remove it from your memory
        status = PurchasesDepartmentStatus.IDLE; //you aren't waiting for the old buy order anymore
        boolean oldQueuedBuy = queuedBuy;
        queuedBuy = false;
        return oldQueuedBuy;
    }

    /**
     * Returns true if the department has already placed a quote
     */
    public boolean hasQuoted(){
        return quotePlaced != null;

    }

    /**
     * just return the amount of budget allocated that is still up for grabs
     * @return the budget left
     */
    public int getAvailableBudget(){
        int budget = budgetGiven - budgetSpent;
        assert budget>=0;
        return budget;

    }


    /**
     * This method returns the inventory control rating on the level of inventories. <br>
     * @return the rating on the inventory conditions or null if the department is not active.
     */

    public Level rateCurrentLevel() {
        assert control != null;
        return control.rateCurrentLevel();
    }

    public Market getMarket() {
        return market;
    }

    /**
     * Should we accept offer from peddlers?
     * @return true if the purchase department is looking to buy
     */
    public boolean canBuy() {
        return control.canBuy();
    }


    @Override
    public String toString() {
        return  market.getGoodType().getName() + "-Purchases" +
                "opponentSearch=" + opponentSearch +
                ", supplierSearch=" + supplierSearch +
                ", control=" + control +
                ", pricingStrategy=" + pricingStrategy +
                ", quotePlaced=" + quotePlaced +
                '}';
    }

    /**
     * this deregister the purchase department and looks in its components to see if any of them can be turned off as well.
     * It also deregisters the department from the firm.
     */
    public void turnOff(){
        //deregister the firm
        market.deregisterBuyer(firm);
        pricingStrategy.turnOff(); //turn off prices
        control.turnOff(); //turn off control
        averagePriceCounter.turnOff();
        supplierSearch.turnOff(); //turn off seller search
        opponentSearch.turnOff(); //turn off buyer search
        predictor.turnOff(); //turn off the predictor
        counter.turnOff();
        purchasesData.turnOff();
        logNode.turnOff();



    }


    /**
     * pass the randomizer from the firm object
     * @return
     */
    public MersenneTwisterFast getRandom() {
        return firm.getRandom();
    }

    /**
     * look into the seller registry and return what the search algorithm deems the best
     * @return the best seller available or null if you can't find any
     */

    public EconomicAgent getBestSupplierFound() {
        return supplierSearch.getBestInSampleSeller();
    }

    /**
     * look into the buyer registry and return what the search algorithm deems the best
     * @return the best buyer available or null if there were none
     */
    public EconomicAgent getBestOpponentFound() {
        return opponentSearch.getBestInSampleBuyer();
    }


    /**
     * Tell the supplier search algorithm that the last match was a bad one
     * @param seller match made
     * @param reason purchase result of the transaction
     */
    public void supplierSearchFailure(EconomicAgent seller, PurchaseResult reason) {
        supplierSearch.reactToFailure(seller, reason);
    }

    /**
     * Tell the search algorithm that the last match was a good one
     * @param seller match made
     * @param reason purchase result of the transaction
     */
    public void supplierSearchSuccess(EconomicAgent seller, PurchaseResult reason) {
        supplierSearch.reactToSuccess(seller, reason);
    }

    /**
     * Tell the search algorithm that the last match was a good one
     * @param buyer match made
     * @param reason purchase result of the transaction
     */
    public void opponentSearchFailure(EconomicAgent buyer, PurchaseResult reason) {
        opponentSearch.reactToSuccess(buyer, reason);
    }

    /**
     * Tell the search algorithm that the last match was a bad one
     * @param buyer match made
     * @param reason purchase result of the transaction
     */
    public void opponentSearchSuccess(EconomicAgent buyer, PurchaseResult reason) {
        opponentSearch.reactToFailure(buyer, reason);
    }

    /**
     * look into the buyer registry and return what the search algorithm deems the best
     * @return the best buyer available or null if there were none
     */
    public EconomicAgent getBestOpponent() {
        return opponentSearch.getBestInSampleBuyer();
    }

    /**
     * Start the inventory control and make the purchaseDepartment, if needed, buy stuff
     */
    public void start(MacroII model){
        Preconditions.checkArgument(!startWasCalled);
        startWasCalled = true;

        counter.start();
        averagePriceCounter.start(model);
        control.start();
        purchasesData.start(model,this);

    }

    /**
     * Returns a link to the pricing strategy. Only useful for subclasses
     * @return reference to the pricing strategy.
     */
    protected BidPricingStrategy getPricingStrategy() {
        return pricingStrategy;
    }


    /**
     * used by a subclass to tell you they are spending money!!
     */
    protected void spendFromBudget(int amountSpent){
        budgetSpent += amountSpent;
    }




    /**
     * The last price the purchases department got for its goods
     * @return
     */
    public int getLastClosingPrice() {
        return lastClosingPrice;
    }

    /**
     * changes the way the purchases department makes prediction about future prices
     * @param predictor the predictor
     */
    public void setPredictor( PurchasesPredictor predictor) {
        if(this.predictor != null)
            this.predictor.turnOff();
        this.predictor = predictor;
    }

    /**
     * Predicts the future price of the next good to buy
     * @return the predicted price or -1 if there are no predictions.
     */
    public int predictPurchasePriceWhenIncreasingProduction() {
        return predictor.predictPurchasePriceWhenIncreasingProduction(this);
    }

    /**
     * Predicts the future price of the next good to buy
     * @return the predicted price or -1 if there are no predictions.
     */
    public int predictPurchasePriceWhenDecreasingProduction()
    {
        return predictor.predictPurchasePriceWhenDecreasingProduction(this);
    }

    /**
     * Gets total outflow since dawn.
     *
     * @return Value of total outflow since dawn.
     */
    public int getTodayOutflow() {
        return counter.getTodayOutflow();
    }

    /**
     * Gets total inflow since dawn.
     *
     * @return Value of total inflow since dawn.
     */
    public int getTodayInflow() {
        return counter.getTodayInflow();
    }

    public int getTodayFailuresToConsume() {
        return counter.getTodayFailuresToConsume();
    }

    /**
     * utility method, asks the firm for the inventory in the type of good this purchase department deals in
     */
    public int getCurrentInventory()
    {
        return getFirm().hasHowMany(getGoodType());
    }
    /**
     * Answers how many days, at the current rate, will it take for all the inventories to be gone
     * @return If outflow > inflow it returns inventorySize/netOutflow, otherwise returns infinity
     */
    public float currentDaysOfInventory() {
        return counter.currentDaysOfInventory();
    }


    /**
     * Sets new At what priority does the purchase department act when trading. It can be changed by its strategies..
     *
     * @param tradePriority New value of At what priority does the purchase department act when trading. It can be changed by its strategies..
     */
    public void setTradePriority(Priority tradePriority) {
        this.tradePriority = tradePriority;
    }

    /**
     * Gets At what priority does the purchase department act when trading. It can be changed by its strategies..
     *
     * @return Value of At what priority does the purchase department act when trading. It can be changed by its strategies..
     */
    public Priority getTradePriority() {
        return tradePriority;
    }

    public MacroII getModel() {
        return model;
    }

    /**
     * This is somewhat similar to rate current level. It estimates the excess (or shortage)of goods purchased. It is basically
     * getCurrentInventory-AcceptableInventory
     * @return positive if there is an excess of goods bought, negative if there is a shortage, 0 if you are right on target.
     */
    public int estimateDemandGap() {
        return control.estimateDemandGap();
    }


    /**
     * Add a new inventory listener
     */
    public void addInventoryListener(InventoryListener listener) {
        firm.addInventoryListener(listener);
    }

    /**
     * Remove specific listener
     * @param listener the listener to remove
     * @return true if it was removed succesfully.
     */
    public boolean removeInventoryListener(InventoryListener listener) {
        return firm.removeInventoryListener(listener);
    }

    /**
     * check a specific datum of a given day
     */
    public double getObservationRecordedThisDay(PurchasesDataType type, int day) {
        return purchasesData.getObservationRecordedThisDay(type, day);
    }

    /**
     * check the time series of a specific datum between two days
     */
    public double[] getObservationsRecordedTheseDays(PurchasesDataType type, int beginningDay, int lastDay) {
        return purchasesData.getObservationsRecordedTheseDays(type, beginningDay, lastDay);
    }

    /**
     *  check the time series of a specific datum between on an array of days
     */
    public double[] getObservationsRecordedTheseDays(PurchasesDataType type,  int[] days) {
        return purchasesData.getObservationsRecordedTheseDays(type, days);
    }

    /**
     * return the latest (yesterday's) observation of a given datum
     */
    public Double getLatestObservation(PurchasesDataType type) {
        return purchasesData.getLatestObservation(type);
    }

    /**
     * return all the observations of a given datum
     */
    public double[] getAllRecordedObservations(PurchasesDataType type) {
        return purchasesData.getAllRecordedObservations(type);
    }

    /**
     * The last day recorded by the PurchasesDepartmentData
     * */
    public int getLastObservedDay() {
        return purchasesData.getLastObservedDay();
    }


    public PurchasesDepartmentData getPurchasesData() {
        return purchasesData;
    }

    /**
     * Count all the workers at plants that consume (as input) what this purchase department buys
     * @return the total number of workers
     */
    public int getNumberOfWorkersWhoConsumeWhatWePurchase() {
        return firm.getNumberOfWorkersWhoConsumeThisGood(getGoodType());
    }


    /**
     * the average closing price of today's trades, or -1 if there were no trades
     */
    public float getTodayAverageClosingPrice() {
        return averagePriceCounter.getTodayAverageClosingPrice();
    }

    /**
     * Predicts the future price of the next good to buy
     * @return the predicted price or -1 if there are no predictions.
     */
    public int predictPurchasePriceWhenNoChangeInProduction() {
        return predictor.predictPurchasePriceWhenNoChangeInProduction(this);
    }

    /**
     * Gets when you placed your last bid, what was your offer price. I think it is more informative than querying maxPrice because
     * it takes some time between maxPrice being updated and then update moving in the quotes.
     *
     * @return Value of when you placed your last bid, what was your offer price. I think it is more informative than querying maxPrice because
     *         it takes some time between maxPrice being updated and then update moving in the quotes.
     */
    public int getLastOfferedPrice() {
        return lastOfferedPrice;
    }

    public int getStartingDay() {
        return purchasesData.getStartingDay();
    }

    public float getAveragedClosingPrice() {
        return averagePriceCounter.getAveragedClosingPrice();
    }

    public int getYesterdayInflow() {
        return counter.getYesterdayInflow();
    }

    public int getYesterdayOutflow() {
        return counter.getYesterdayOutflow();
    }


    public Class<? extends BidPricingStrategy> getPricingStrategyClass()
    {
        return pricingStrategy.getClass();
    }

    @Override
    public boolean hasTradedAtLeastOnce() {
        return getLastClosingPrice() >=0;

    }


    /***
     *       __
     *      / /  ___   __ _ ___
     *     / /  / _ \ / _` / __|
     *    / /__| (_) | (_| \__ \
     *    \____/\___/ \__, |___/
     *                |___/
     */

    /**
     * simple lognode we delegate all loggings to.
     */
    private final LogNodeSimple logNode = new LogNodeSimple();

    @Override
    public boolean addLogEventListener(LogListener toAdd) {
        return logNode.addLogEventListener(toAdd);
    }

    @Override
    public boolean removeLogEventListener(LogListener toRemove) {
        return logNode.removeLogEventListener(toRemove);
    }

    @Override
    public void handleNewEvent(LogEvent logEvent)
    {
        logNode.handleNewEvent(logEvent);
    }

    @Override
    public boolean stopListeningTo(Loggable branch) {
        return logNode.stopListeningTo(branch);
    }

    @Override
    public boolean listenTo(Loggable branch) {
        return logNode.listenTo(branch);
    }
}
