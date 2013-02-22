package agents.firm.sales;

import agents.EconomicAgent;
import agents.firm.Department;
import agents.firm.Firm;
import agents.firm.sales.exploration.BuyerSearchAlgorithm;
import agents.firm.sales.exploration.SellerSearchAlgorithm;
import agents.firm.sales.prediction.MemorySalesPredictor;
import agents.firm.sales.prediction.SalesPredictor;
import agents.firm.sales.pricing.AskPricingStrategy;
import agents.firm.sales.pricing.decorators.AskReservationPriceDecorator;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import financial.Market;
import financial.MarketEvents;
import financial.utilities.ActionsAllowed;
import financial.utilities.PurchaseResult;
import financial.utilities.Quote;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import sim.engine.SimState;
import sim.engine.Steppable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * <h4>Description</h4>
 * <p/> This is the class representing the sales department
 * <p/> Chiefly it takes care of three tasks:
 * <ul>
 *     <li> Responds to a customer approaching the firm willing to buy a good</li>
 *     <li> Be tasked by the firm to sell something</li>
 *     <li> Be asked to search the market for the best price to sell to</li>
 * </ul>
 * <p/>
 * The subclasses are different in the way they respond to HQ when asked what is next price going to be.
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version %I%, %G%
 * @see
 */
public class SalesDepartment implements Department {

    /**
     * the list of goods that the firm has decided to let the sales department sell
     */
    private final Set<Good> toSell;

    /**
     * a map associating to each good to sell the quote submitted for it at a centralized market
     */
    private final Map<Good,Quote> goodsQuotedOnTheMarket;


    private LinkedList<SalesDepartmentListener> salesDepartmentListeners;

    /**
     * This is the memory associated with the weekly results of selling. It records whether any good was sold, quoted or failed to sell.
     * A good that remained quoted at the end of the week without being sold is unsold
     */
    private final Map<Good,SaleResult> salesResults;

    /**
     * The firm where the sales department belongs
     */
    private final Firm firm;

    /**
     * The market the sales department deals in
     */
    private Market market;

    private final MacroII model;




    /**
     * The procedure used by the sales department to search the market.
     * It is
     */
    private BuyerSearchAlgorithm buyerSearchAlgorithm;


    private SellerSearchAlgorithm sellerSearchAlgorithm;

    /**
     * This is the strategy used by the sales department to choose its price
     */
    private AskPricingStrategy askPricingStrategy;

    /**
     * This is the strategy to predict future sale prices when the order book is not visible.
     */
    private SalesPredictor predictorStrategy;



    /*
    * ***************************
     * Statistics
     ****************************
     */
    /**
     * Here we keep memorized the last n totalSales of this firm.
     */
    final private Deque<Long> totalSales;

    /**
     * Here we keep memorized the value of unsold merchandise of this department
     */
    final private Deque<Long> totalUnsold;

    /**
     * Here we keep memorized the sum of revenue-costs for the goods sold
     */
    final private Deque<Long> grossMargin;

    /**
     * Here we keep memorized the cost of goods sold
     */
    final private Deque<Long> cogs;

    /**
     * This counts how many goods the department managed to sell last week
     */
    private int goodsSoldLastWeek;

    /**
     * This counts how many goods the department was tasked to sell last week
     */
    private int goodsToSellLastWeek;

    /**
     * goods sold today. Reset every day at PREPARE_TO_TRADE step
     */
    private int todayOutflow;

    /**
     * goods that were given to us to sell today
     */
    private int todayInflow;

    /**
     * How many days will inventories last at the current flow? It is 0 if there is no inventory, MAX_VALUE if there are more inflows than outflows!
     */
    private float daysOfInventory = 0;

    /**
     * this flag is set to true whenever the first sellThis is called. It is never set to false
     */
    private boolean started = false;

    /**
     * This is the ratio goodsToSell/GoodsSold
     */
    private float soldPercentage = 1;

    /**
     * This is the price of the last good the sales department managed to sell
     */
    private long lastClosingPrice = -1;

    /**
     * This is the cost of the last good the sales department managed to sell
     */
    private long lastClosingCost = -1;

    /**
     * When this is true, the sales department peddles its goods around when it can't make a quote.
     * If this is false and the sales department can't quote, it just passively wait for buyers
     */
    private boolean canPeddle = true;


    /*
    ******************************
    * METHODS
    ******************************
     */

    /**
     * This is the constructor for the template sales department.  It also registers the firm as seller
     * @param firm The firm where the sales department belongs
     * @param market The market the sales department deals in
     */
    private SalesDepartment(@Nonnull Firm firm,@Nonnull Market market ) {
        this(firm,market,null,null,firm.getModel());



    }

    /**
     * This is the constructor for the template sales department.  It also registers the firm as seller
     * @param firm The firm where the sales department belongs
     * @param market The market the sales department deals in
     */
    private SalesDepartment(@Nonnull Firm firm,@Nonnull Market market,@Nonnull MacroII model ) {
        this(firm,market,null,null,model);



    }


    /**
     * This is the constructor for the template sales department. It also registers the firm as seller
     * @param firm The firm where the sales department belongs
     * @param market The market the sales department deals in
     * @param buyerSearchAlgorithm the buyer search department
     * @param sellerSearchAlgorithm the seller search department
     */
    private SalesDepartment(Firm firm, Market market, BuyerSearchAlgorithm buyerSearchAlgorithm, SellerSearchAlgorithm sellerSearchAlgorithm ) {
        this(firm, market, buyerSearchAlgorithm, sellerSearchAlgorithm,firm.getModel());


    }


    /**
     * This is the constructor for the template sales department. It also registers the firm as seller
     * @param firm The firm where the sales department belongs
     * @param market The market the sales department deals in
     * @param buyerSearchAlgorithm the buyer search department
     * @param sellerSearchAlgorithm the seller search department
     */
    private SalesDepartment(Firm firm, Market market, BuyerSearchAlgorithm buyerSearchAlgorithm, SellerSearchAlgorithm sellerSearchAlgorithm,
                            @Nonnull MacroII model ) {
        this.firm = firm;
        this.market = market;
        market.registerSeller(firm); //register!


        //instantiate the lists
        toSell = new LinkedHashSet<>();
        goodsQuotedOnTheMarket = new HashMap<>();
        salesResults = new HashMap<>();
        salesDepartmentListeners = new LinkedList<>();
        //statistics
        totalSales = new ArrayDeque<>(firm.getModel().getSalesMemoryLength());
        totalUnsold = new ArrayDeque<>(firm.getModel().getSalesMemoryLength());
        grossMargin = new ArrayDeque<>(firm.getModel().getSalesMemoryLength());
        cogs = new ArrayDeque<>(firm.getModel().getSalesMemoryLength());
        this.buyerSearchAlgorithm = buyerSearchAlgorithm;
        this.sellerSearchAlgorithm = sellerSearchAlgorithm;
        predictorStrategy = new MemorySalesPredictor();

        this.model = model;


    }

    //TODO make it register itself with the firm too

    /**
     * This is a factory method for sales department that retuns a NON-READY sales department. That is, one that is missing ask-pricing strategy and predictor pricing and the search algorithms.
     * Use only if you know what you are doing
     * @param firm the firm the sales department belongs to
     * @param market the market the sales department markets
     * @return a new sales department
     */
    static public SalesDepartment incompleteSalesDepartment(@Nonnull Firm firm, @Nonnull Market market)
    {
        return new SalesDepartment(firm,market);
    }

    /**
     * This is a factory method for sales department that retuns a NON-READY sales department. That is, one that is missing ask-pricing strategy and predictor pricing.
     * Use only if you know what you are doing
     * @param firm the firm the sales department belongs to
     * @param market the market the sales department markets
     * @return a new sales department
     */
    static public SalesDepartment incompleteSalesDepartment(@Nonnull Firm firm,@Nonnull  Market market,@Nonnull BuyerSearchAlgorithm buyerSearchAlgorithm,@Nonnull  SellerSearchAlgorithm sellerSearchAlgorithm )
    {
        return new SalesDepartment(firm,market,buyerSearchAlgorithm,sellerSearchAlgorithm);
    }

    /**
     * This is a standard factory method to create a new sales department.
     * Any "null" type argument is randomized
     * @param firm the firm the sales department belongs to
     * @param market the market the sales department markets
     * @return a new sales department
     */
    static public SalesDepartment newSalesDepartment(@Nonnull Firm firm,@Nonnull  Market market,
                                                     @Nullable Class<? extends BuyerSearchAlgorithm> buyerSearch, @Nullable Class<? extends SellerSearchAlgorithm > sellerSearch,
                                                     @Nullable Class<? extends AskPricingStrategy> priceStrategy, @Nullable Class<? extends SalesPredictor > predictionStrategy
    )
    {
        //create the search algorithms
        BuyerSearchAlgorithm buyerSearchAlgorithm;
        if(buyerSearch== null)
            buyerSearchAlgorithm = BuyerSearchAlgorithm.Factory.randomBuyerSearchAlgorithm(market,firm);
        else
            buyerSearchAlgorithm = BuyerSearchAlgorithm.Factory.newBuyerSearchAlgorithm(buyerSearch,market,firm);

        SellerSearchAlgorithm sellerSearchAlgorithm;
        if(sellerSearch== null)
            sellerSearchAlgorithm = SellerSearchAlgorithm.Factory.randomSellerSearchAlgorithm(market,firm);
        else
            sellerSearchAlgorithm = SellerSearchAlgorithm.Factory.newSellerSearchAlgorithm(sellerSearch,market,firm);




        SalesDepartment dept = new SalesDepartment(firm,market,buyerSearchAlgorithm,sellerSearchAlgorithm);
        firm.registerSaleDepartment(dept, GoodType.GENERIC);

        //now create the two pricing strategies
        AskPricingStrategy askPricingStrategy;
        if(priceStrategy== null)
            askPricingStrategy = AskPricingStrategy.Factory.randomAskPricingStrategy(dept);
        else
            askPricingStrategy = AskPricingStrategy.Factory.newAskPricingStrategy(priceStrategy,dept);

        dept.setAskPricingStrategy(askPricingStrategy);

        SalesPredictor salesPredictor;
        if(predictionStrategy== null)
            salesPredictor = SalesPredictor.Factory.randomSalesPredictor(firm.getRandom());
        else
            salesPredictor = SalesPredictor.Factory.newSalesPredictor(predictionStrategy);

        dept.setPredictorStrategy(salesPredictor);
        //register and retun
        //finally return!
        return dept;
    }



    /**
     * A buyer asks the sales department for the price they are willing to sell one of their good.
     * @param buyer the economic agent that asks you that
     * @return a price quoted or -1 if there are no quotes
     */
    public Quote askedForASalePrice(EconomicAgent buyer){
        long priceQuoted = Long.MAX_VALUE;
        if(toSell.isEmpty()){ //if you have nothing to sell
            //was the seller rich enough to buy something if we had any?
            if(lastClosingCost >=0) //if we ever sold something before
                if(buyer.maximumOffer(new Good(market.getGoodType(),getFirm(),lastClosingCost)) >= lastClosingPrice)
                    //then tell the listeners this is a stockout
                    for(SalesDepartmentListener listener : salesDepartmentListeners)
                        listener.stockOutEvent(getFirm(),this,buyer);

            return Quote.emptySellQuote(null);
        }


        if(market.getSellerRole() == ActionsAllowed.QUOTE) //if you are in an environment for quoting
        {
            //find your own lowest quote

            return Collections.min(goodsQuotedOnTheMarket.values(),new Comparator<Quote>() {
                @Override
                public int compare(Quote o1, Quote o2) {
                    return Long.compare(o1.getPriceQuoted(),o2.getPriceQuoted());
                }
            }); //return lowest quoted price
        }
        else
        {
            Good goodQuoted = null;
            //you don't have a list of quotes; go through each toSell and price them
            for(Good g : toSell){
                long price = price(g); //the price of this good
                assert price >=0; //can't be -1!
                if(price < priceQuoted) //if that's the lowest, quote that
                {
                    priceQuoted = price;
                    goodQuoted = g;
                }
            }

            assert goodQuoted != null; //can't be null because the list toSell is not empty!
            assert priceQuoted < Long.MAX_VALUE;
            //return it!
            Quote q = Quote.newSellerQuote(getFirm(),priceQuoted,goodQuoted);
            q.setOriginator(this);
            return q;

        }
    }


    /**
     * This method may be called by the firm to ask the sales department to predict what the sell price for a new good may be (usually to guide production). <br>
     * It works in 2 steps: <ul>
     *     <li> If quotes are visible in the market, just return the best quote</li>
     *     <li> Otherwise use the department pricePredictor</li>
     * </ul>
     *
     * @param expectedProductionCost the HQ estimate of costs in producing whatever it wants to sell. It isn't necesarilly used.
     * @return the best offer available or -1 if there are no quotes
     */
    public long predictSalePrice(long expectedProductionCost)
    {

        if(market.isBestBuyPriceVisible())
            try {
                long bestVisiblePrice =  market.getBestBuyPrice();  //ask the market for best price
                if(bestVisiblePrice == -1) //no price visible
                    return predictorStrategy.predictSalePrice(this,expectedProductionCost);
                else
                    return bestVisiblePrice;

            } catch (IllegalAccessException e) {
                //this should really never happen;
                assert false;
                return predictorStrategy.predictSalePrice(this,expectedProductionCost);
            }
        else
            return predictorStrategy.predictSalePrice(this,expectedProductionCost);
    }


    /**
     * The sales department is asked at what should be the sale price for a specific good; this, I guess, is the fundamental
     * part of the sales department
     * <p>
     *     This method doesn't check if the department actually owns this good.
     * </p>
     *
     * @param g the good to price
     * @return
     */
    public long price(Good g){
        return askPricingStrategy.price(g);
    }




    /**
     * This is the method called by the firm when it tasks the department to sell a specific good!  <p>
     * The way I see it, most likely you just want to implement shouldIPeddle, peddle and price. Those are the really big ones. <br>
     * This method calls them, especially price(); placing a quote is straightforward and so implemented here.
     * @param g the good the department needs to sell!
     */
    public void sellThis(final Good g){

        //preconditions
        assert firm.has(g); //we should be selling something we have!
        assert market.getGoodType().equals(g.getType());
        toSell.add(g); //addSalesDepartmentListener it to the list of stuff to sell


        //log it (this also fires listeners)
        logInflow(g);


        //now act
        if(market.getSellerRole() == ActionsAllowed.QUOTE) //if we are supposed to quote
        {
            placeQuote(g);

        }
        else  if(canPeddle)
        {
            peddle(g);

        }

    }

    /**
     * Does three things: Logs the event that we were tasked to sell a good, tell the listeners about this event and if this was teh very
     * first good we have to sell, schedule daily a beginningOfTheDayStatistics() call
     */
    private void logInflow(Good g) {


        //tell the listeners about it
        for(SalesDepartmentListener listener : salesDepartmentListeners)
            listener.sellThisEvent(firm,this,g);




        todayInflow++;
        getFirm().logEvent(SalesDepartment.this, MarketEvents.TASKED_TO_SELL, getFirm().getModel().getCurrentSimulationTimeInMillis());



        //if this is the first time we get to sell, start also resetting data
        if(!started)
        {
            started = true;
            model.scheduleSoon(ActionOrder.DAWN,new Steppable() {
                @Override
                public void step(SimState state) {
                    beginningOfTheDayStatistics();

                }
            });
        }


    }

    /**
     * this method resets the daily counters of inflow and outflow and if the firm is active, reschedule itself assuming
     * this was called when the phase was DAWN .
     * It also computes the "days of inventory"
     */
    private void beginningOfTheDayStatistics() {
        if(!firm.isActive())
            return;



        float netflow = todayOutflow - todayInflow;
        if(toSell.size() == 0)   //no inventory, days of inventory is 0
            daysOfInventory = 0;
        else if(netflow > 0) //positive netflow (we sell more than we produce, that's great)
            daysOfInventory = ((float)toSell.size()) / netflow;
        else //negative or 0 netflow, days of inventory is infinite
            daysOfInventory = Float.MAX_VALUE;


        //reset
        todayInflow = 0;
        todayOutflow = 0;

        model.scheduleTomorrow(ActionOrder.DAWN,new Steppable() {
            @Override
            public void step(SimState state) {

                beginningOfTheDayStatistics();
            }
        });


    }

    /**
     * Schedule yourself to peddle when you can
     * @param g the good to sell
     */
    private void peddle(final Good g) {
        model.scheduleSoon(ActionOrder.TRADE,new Steppable() {
            @Override
            public void step(SimState state) {
                //if we are here it means that the market didn't allow us to quote; this means that we should peddle
                boolean success = peddleNow(g);
                if(success){               //did we manage to sell?
                    assert !firm.has(g);
                    assert salesResults.get(g).getResult() == SaleResult.Result.SOLD;

                    //done!

                }
                else{
                    //we didn't manage to sell!
                    assert firm.has(g);
                    assert salesResults.get(g).getResult() == SaleResult.Result.UNSOLD;
                    //shall we try again?
                    double tryAgainIn = tryAgainNextTime(g);
                    if(tryAgainIn > 0)   //if we do want to try again
                        firm.getModel().schedule.scheduleOnceIn(tryAgainIn,new Steppable() {
                            @Override
                            public void step(SimState simState) {     //schedule to peddle again!
                                peddleNow(g);
                            }
                        });
                }
            }
        });

    }

    /**
     * Place an ask in the order book
     * @param g the good to quote
     */
    private void placeQuote(final Good g)
    {


        model.scheduleSoon(ActionOrder.TRADE,new Steppable() {
            @Override
            public void step(SimState state) {

                long price = price(g);
                getFirm().logEvent(SalesDepartment.this, MarketEvents.SUBMIT_SELL_QUOTE,getFirm().getModel().getCurrentSimulationTimeInMillis(),
                        "price:" + price);
                Quote q = market.submitSellQuote(firm,price,g,SalesDepartment.this); //put a quote into the market
                if(q.getPriceQuoted() != -1) //if the quote is not null
                {
                    //if the quote is not null, we quoted but not sold
                    assert q.getAgent() == firm; //make sure we got back the right quote
                    goodsQuotedOnTheMarket.put(g,q); //record the quote!
                    salesResults.put(g, SaleResult.quoted());

                    if(shouldIPeddle(q))    //do you want to try and peddle too?
                        peddleNow(q.getGood()); //then peddle!

                }
                else{
                    //if we are here, the quote returned was null which means that we already sold the good!
                    assert !firm.has(g); //shouldn't be ours anymore!
                    assert q.getAgent() == null; //should be null

                    //now the accounting should have been already taken care of by reactToFilledAskedQuote() method! Make sure:
                    assert salesResults.get(g).getResult() == SaleResult.Result.SOLD;
                    assert lastClosingPrice == salesResults.get(g).getPriceSold(); //check that the price recorded is correct
                }
            }
        });



    }

    /**
     * This is called by a buyer that is shopping at this department. This means that it is not going through quotes and I assume the buyer had a quote from this department to make a choice
     * @param buyerQuote the quote of the buyer, it must have at least a goodtype
     * @param sellerQuote our quote, we must have given it previously
     */
    public PurchaseResult shopHere(Quote buyerQuote, Quote sellerQuote)
    {
        assert buyerQuote.getType().equals(market.getGoodType()); //it's of the right type, correct?
        Preconditions.checkArgument(sellerQuote.getOriginator().equals(this), "We didn't make this quote");


        /***************************************************************************
         * Choose the good to trade
         ***************************************************************************/
        Good g;
        //did we quote a specific good?
        if(sellerQuote.getGood() != null)
        {
            //then trade it!
            g = sellerQuote.getGood();
            assert toSell.contains(g); assert getFirm().has(g); //make sure we own it and we can sell it

        }
        else
        {
            assert false : "This should never happen as long as I don't introduce displayPrice";
            //if we didn't quote a specific good

            if(toSell.isEmpty()) //if we have none, tell them it's a stockout
            {
                assert false : "This should never happen as long as I don't introduce displayPrice";

                //tell all your listeners
                for(SalesDepartmentListener listener : salesDepartmentListeners)
                    listener.stockOutEvent(getFirm(),this,buyerQuote.getAgent());
                //return a stockout
                return PurchaseResult.STOCKOUT;

            }
            else //otherwise it's the cheapest possible
                g = Collections.min(toSell,new Comparator<Good>() {
                    @Override
                    public int compare(Good o1, Good o2) {
                        return Long.compare(o1.getLastValidPrice(),o2.getLastValidPrice());

                    }
                });
        }
        //we are here, good is not null
        assert  g != null;

        /*********************************************************************
         * TRADE
         *********************************************************************/
        assert sellerQuote.getPriceQuoted() >= 0 ; //can't be negative!!!
        assert buyerQuote.getPriceQuoted() >= sellerQuote.getPriceQuoted(); //the price offers must cross

        long finalPrice = market.price(sellerQuote.getPriceQuoted(),buyerQuote.getPriceQuoted());

        //exchange hostages
        market.trade(buyerQuote.getAgent(),getFirm(),g,finalPrice,buyerQuote,sellerQuote);
        getFirm().logEvent(SalesDepartment.this, MarketEvents.SOLD, getFirm().getModel().getCurrentSimulationTimeInMillis(), "price: " + finalPrice + ", through buyFromHere()"); //sold a good

        /********************************************************************
         * Record information
         *******************************************************************/
        toSell.remove(g);
        logOutflow(g, finalPrice);
        PurchaseResult toReturn =  PurchaseResult.SUCCESS;
        toReturn.setPriceTrade(finalPrice);
        return toReturn;


    }

    /**
     * This is called automatically whenever a quote we made was filled
     * @param g the good sold
     * @param price the price for which it sold
     */
    public void reactToFilledQuote(Good g, long price, EconomicAgent buyer){

        assert !firm.has(g); //we should have sold
        boolean removedCorrectly = toSell.remove(g);  //remove it from the list of tosell
        assert removedCorrectly;


        if(goodsQuotedOnTheMarket.remove(g) != null)  //do you remember having a quote at all?
        {
            //removed the quote
            SaleResult oldResult = salesResults.remove(g);
            //it should have been registered as either quoted or unsold
            assert oldResult.getResult() == SaleResult.Result.UNSOLD || oldResult.getResult() == SaleResult.Result.QUOTED;

        }
        else
        {
            //you don't have a quote, but it was still in the ToSell, it means we got immediately filled!
            assert !salesResults.containsKey(g) || salesResults.get(g).getResult() == SaleResult.Result.BEING_UPDATED : //you shouldn't be registered as a result or you were repriced
                    "The sales result is: " + salesResults.get(g);

        }

        //register it!
        logOutflow(g, price);

    }

    /**
     * After having removed the good from the sales department toSell, this records it as a sale and tell the listeners
     */
    private void logOutflow(Good g, long price)
    {
        assert !firm.has(g); //you can't have it if you sold it!
        assert !toSell.contains(g); //you should have removed it!!!

        //finally, register the sale!!
        SaleResult newResult = SaleResult.sold(price,g.getSecondLastValidPrice()); //congratulation, you sold
        //       newResult.setPriceSold(price); //record the price of sale
        salesResults.put(g, newResult); //put it in!
        lastClosingPrice = price;
        lastClosingCost = newResult.getPreviousCost();

        //tell the listeners!
        fireGoodSoldEvent(newResult);

        //log it
        getFirm().logEvent(SalesDepartment.this, MarketEvents.SOLD
                , getFirm().getModel().getCurrentSimulationTimeInMillis(), "price " + newResult.getPriceSold());
        todayOutflow++;




    }


    /**
     * The market asks you to quote, and you did. Do you want to try and peddle as well?
     * @param q the quote made in the market
     * @return true if you want to peddle too
     */
    public boolean shouldIPeddle(Quote q){
        return false; //TODO fix this
    }


    /**
     * This is called by sellThis if the market doesn't allow the seller to quote or the seller wants to peddle anyway. <br>
     *     Basically it's just a search. <p>
     *         It's this method responsibility to record the salesResults
     * @param g the good to sell
     * @return true if the peddling was successful
     */
    public boolean peddleNow(Good g)
    {
        assert firm.has(g); //should be owned by us, now
        assert toSell.contains(g); //should be owned by us, now

        long cost = g.getLastValidPrice(); //what did it cost us? Used for accounting only


        //look for a buyer
        EconomicAgent buyer = buyerSearchAlgorithm.getBestInSampleBuyer(); //call the search algorithm for that
        long priceAsked;
        if(goodsQuotedOnTheMarket.get(g) == null){   //if it's not quoted, compute its price
            priceAsked = price(g);
        }
        else{
            priceAsked =  goodsQuotedOnTheMarket.get(g).getPriceQuoted();  //otherwise your asking price will be whatever was in the quote
        }

        long priceBuyer = buyer.maximumOffer(g);
        if(priceAsked <= priceBuyer )
        {
            //price is somewhere in the middle
            long finalPrice = (long)Math.round(((double) (priceAsked + priceBuyer)) / 2d);
            assert finalPrice>=priceAsked;
            assert finalPrice<=priceBuyer;

            //create fake quote objects, these might be useful for record keeping and such
            Quote buyerQuote = Quote.newBuyerQuote(buyer,priceBuyer,market.getGoodType());
            Quote sellerQuote = Quote.newSellerQuote(getFirm(),priceAsked,g);
            sellerQuote.setOriginator(this);

            PurchaseResult result = market.trade(buyer,this.firm,g,finalPrice,buyerQuote,sellerQuote); //TRADE!
            if(result == PurchaseResult.SUCCESS)
            {
                //we made it!
                assert !firm.has(g);  //we sold it, right?
                toSell.remove(g);
                Quote q = goodsQuotedOnTheMarket.remove(g); //if you had a quote, remove it
                //remove it from the market too, if needed
                if(q!=null)
                    market.removeSellQuote(q);
                SaleResult saleResult = SaleResult.sold(finalPrice, cost);
                salesResults.put(g,saleResult ); //record the sale
                lastClosingPrice = finalPrice;
                buyerSearchAlgorithm.reactToSuccess(buyer,result); //tell the search algorithm
                //tell the listeners!
                fireGoodSoldEvent(saleResult);

                return true;

            }
            else
            {
                //we failed!
                assert firm.has(g);  //we still have it, uh?
                if(!salesResults.containsKey(g))
                    salesResults.put(g,SaleResult.unsold()); //record it as unsold!

                buyerSearchAlgorithm.reactToFailure(buyer,result);
                return false;

            }


        }
        //we failed
        assert firm.has(g);  //we still have it, uh?
        if(!salesResults.containsKey(g))
            salesResults.put(g,SaleResult.unsold()); //record it as unsold!
        //they rejected our price!
        buyerSearchAlgorithm.reactToFailure(buyer, PurchaseResult.PRICE_REJECTED);
        return false;



    }


    /**
     * Whenever we can ONLY peddle and we fail to do so, we call this method to know how much to wait before trying again
     * @param g the good to sell
     * @return the wait time, or -1 if we are NOT going to try again!
     */
    public final double tryAgainNextTime(Good g){
        return getFirm().getModel().getPeddlingSpeed();

    }


    /**
     * Weekend is a magical time when we record all quoted but unsold goods as unsold and then compute statistics and move on.
     */
    public void weekEnd(){
        //zero all the counters
        Collection<Good> recordsToRemove = new LinkedList<>();
        long thisWeekSales = 0;
        long thisWeekCOGS = 0;
        long thisWeekInventory = 0;
        long thisWeekMargin = 0;
        goodsSoldLastWeek= 0;  goodsToSellLastWeek =0;

        //compute the sales
        for(Map.Entry<Good, SaleResult> saleRecord : salesResults.entrySet()) //go through the entry set
        {
            goodsToSellLastWeek++; //count this among the goods I was supposed to sell

            if(saleRecord.getValue().getResult() == SaleResult.Result.SOLD) //was this good sold?
            {
                assert !firm.has(saleRecord.getKey()); //we shouldn't own it anymore!!!
                assert !toSell.contains(saleRecord.getKey());
                assert saleRecord.getValue().getPriceSold() >= 0 : saleRecord.getValue().getPriceSold(); //we should have recorded the price!!
                thisWeekSales = thisWeekSales + saleRecord.getValue().getPriceSold(); //sum up the sale
                thisWeekCOGS = thisWeekCOGS + saleRecord.getValue().getPreviousCost(); //sum up the costs
                thisWeekMargin = thisWeekMargin + saleRecord.getValue().getPriceSold() - saleRecord.getValue().getPreviousCost(); //addSalesDepartmentListener the margin
                recordsToRemove.add(saleRecord.getKey()); //remove it, so we don't count it twice
                goodsSoldLastWeek++; //count it
            }
            else
            {
                //it's unsold
//                assert saleRecord.getValue().getResult() != SaleResult.Result.BEING_UPDATED; //being updated should be a temporary state, shouldn't be still here by the end of the week
                assert firm.has(saleRecord.getKey()); //we still own it!!!
                assert toSell.contains(saleRecord.getKey()); //we still need to sell it


                if(saleRecord.getValue().getResult() == SaleResult.Result.QUOTED) //quoted are now counted as unsold
                    saleRecord.setValue(SaleResult.unsold());
                thisWeekInventory = thisWeekInventory + saleRecord.getKey().getLastValidPrice(); //count their value as part of unsold cost
            }
        }

        soldPercentage =  ((float)goodsSoldLastWeek)/(float)goodsToSellLastWeek;
        assert soldPercentage <= 1 || goodsToSellLastWeek == 0;
        assert soldPercentage >=0 || goodsToSellLastWeek == 0;

        //remove all keys
        for(Good g : recordsToRemove)
            salesResults.remove(g); //remove them all!

        if(MacroII.SAFE_MODE) //if safe mode is on
        {
            for(Map.Entry<Good, SaleResult> saleRecord : salesResults.entrySet())      //go back through all the salesResults
            {
                assert firm.has(saleRecord.getKey()); //unsold should still be in inventory
                assert toSell.contains(saleRecord.getKey()); //should be owned by us, now
                assert saleRecord.getValue().getResult() != SaleResult.Result.SOLD; //all successes should have been eliminated
            }

            for(Good g : toSell)
            {
                assert firm.has(g); //unsold should still be in inventory
            }
        }


        //RECORD results

        totalSales.addLast(thisWeekSales); //record total sales
        totalUnsold.addLast(thisWeekInventory);
        cogs.addLast(thisWeekCOGS);
        grossMargin.addLast(thisWeekMargin);
        assert thisWeekMargin == thisWeekSales - thisWeekCOGS;
        assert totalSales.size() == totalUnsold.size(); //they should be of the same size
        assert totalSales.size() == grossMargin.size(); //they should be of the same size


        if(totalSales.size() >= firm.getModel().getSalesMemoryLength()){
            //if the memory is too long
            totalSales.removeFirst();
            totalUnsold.removeFirst();
            grossMargin.removeFirst();
        }


        askPricingStrategy.weekEnd();



    }


    /**
     * If this is called, we add a reservation price decorators to the ask pricing object.
     * This is a ugly hack utility method until I finally give up and do injection dependency properly <br>
     * It can't be removed.
     */
    public void addReservationPrice(long reservationPrice){
        Preconditions.checkState(askPricingStrategy != null, "Can't add reservation prices until ");
        askPricingStrategy = new AskReservationPriceDecorator(askPricingStrategy,reservationPrice);
    }


    /**
     * @return the value for the field firm.
     */
    public Firm getFirm() {
        return firm;
    }

    /**
     * @return the value for the field market.
     */
    public Market getMarket() {
        return market;
    }

    /**
     * @return the value for the field buyerSearchAlgorithm.
     */
    public BuyerSearchAlgorithm getBuyerSearchAlgorithm() {
        return buyerSearchAlgorithm;
    }

    /**
     * @return the value for the field sellerSearchAlgorithm.
     */
    public SellerSearchAlgorithm getSellerSearchAlgorithm() {
        return sellerSearchAlgorithm;
    }



    /**
     * Setter for field askPricingStrategy.
     *
     * @param askPricingStrategy the value to set for the field.
     */
    public void setAskPricingStrategy(AskPricingStrategy askPricingStrategy) {

        if(this.askPricingStrategy != null)
            this.askPricingStrategy.turnOff();

        this.askPricingStrategy = askPricingStrategy;

    }

    /**
     * Looks for the price of the best competitor this department could find!
     * @return the price found or -1 if there is none
     */
    public long getLowestOpponentPrice()  {



        try
        {
            if(getMarket().isBestSalePriceVisible())         //can we just see it from the order book?
            {
                EconomicAgent bestSeller = getMarket().getBestSeller();

                if(bestSeller == this.getFirm()) //if our firm is the best, just search at random
                {
                    EconomicAgent opponent = getSellerSearchAlgorithm().getBestInSampleSeller();
                    if(opponent != null)
                        return opponent.askedForASaleQuote(getFirm(), getMarket().getGoodType()).getPriceQuoted();
                    else
                        return -1;
                }
                else //if we aren't the best, return the market best
                    return getMarket().getBestSellPrice(); //if so, return it
            }
            else{
                //otherwise search for it
                EconomicAgent opponent = getSellerSearchAlgorithm().getBestInSampleSeller();
                return opponent.askedForASaleQuote(getFirm(), getMarket().getGoodType()).getPriceQuoted();
            }


        }catch (IllegalAccessException e){assert false; System.exit(-1); return -1;}            //this won't happen4
    }


    /**
     * @return the value for the field goodsSoldLastWeek.
     */
    public int getGoodsSoldLastWeek() {
        return goodsSoldLastWeek;
    }

    /**
     * @return the value for the field goodsToSellLastWeek.
     */
    public int getGoodsToSellLastWeek() {
        return goodsToSellLastWeek;
    }

    /**
     * @return the value for the field soldPercentage. (100% if never usec)
     */
    public float getSoldPercentage() {
        return soldPercentage;
    }

    /**
     * last closing price
     * @return
     */
    public long getLastClosingPrice() {
        return lastClosingPrice;
    }


    public SalesPredictor getPredictorStrategy() {
        return predictorStrategy;
    }

    public void setPredictorStrategy(SalesPredictor predictorStrategy) {
        this.predictorStrategy = predictorStrategy;
    }

    public Deque<Long> getGrossMargin() {
        return grossMargin;
    }

    public Deque<Long> getTotalSales() {
        return totalSales;
    }

    public Deque<Long> getTotalUnsold() {
        return totalUnsold;
    }


    /**
     * Return last week gross margin (revenue - costs)
     */
    public long getLastWeekMargin(){
        return grossMargin.getLast();
    }

    /**
     * Return the last week cost of good solds
     */
    public long getLastWeekCostOfGoodSold()
    {
        return cogs.getLast();

    }


    /**
     * Return last week sales (that is, revenues)
     */
    public long getLastWeekSales()
    {
        return totalSales.getLast();
    }




    /**
     *
     *
     * This function is called by strategies that "adjust" when they changed their opinion of what the price should be.
     * The seller remove all its quotes and place them again on the market
     */
    public void updateQuotes()
    {
        //todo ASAP, is it a good idea?

        model.scheduleASAP(new Steppable() {
            @Override
            public void step(SimState state) {
                //get all the quotes to remove
                Iterable<Quote> goodsToRequote = new LinkedList<>(goodsQuotedOnTheMarket.values());
                //forget the old quotes
                goodsQuotedOnTheMarket.clear();
                for(Quote q: goodsToRequote)
                {
                    market.removeSellQuote(q); //remove the quote

                }

                //go through all the old quotes
                for(Quote q : goodsToRequote){


                    SaleResult oldResult = salesResults.put(q.getGood(), SaleResult.updating()); //signal to the results map that the good is being updated
                    assert oldResult.getResult() == SaleResult.Result.QUOTED || oldResult.getResult() == SaleResult.Result.UNSOLD; //previously you should have been classified as unsold or quoted
                    sellThis(q.getGood());//sell it again


                }

                //done!
            }
        });





    }


    public long getLastClosingCost() {
        return lastClosingCost;
    }

    /**
     * This method turns off all sub-components and clears all data structures
     */
    public void turnOff(){
        askPricingStrategy.turnOff(); askPricingStrategy = null;
        buyerSearchAlgorithm.turnOff(); buyerSearchAlgorithm = null;
        sellerSearchAlgorithm.turnOff(); sellerSearchAlgorithm = null;
        if(predictorStrategy!= null)
            predictorStrategy.turnOff(); predictorStrategy = null;
        toSell.clear();
        grossMargin.clear();
        goodsQuotedOnTheMarket.clear();
        salesResults.clear();
        assert salesDepartmentListeners.isEmpty(); //hopefully it is clear by now!
        salesDepartmentListeners.clear();
        salesDepartmentListeners = null;

    }

    /**
     * Get the randomizer of the owner
     */
    public MersenneTwisterFast getRandom() {
        return firm.getRandom();
    }


    /**
     * Appends the specified element to the end of this list.
     *
     *
     * @param salesDepartmentListener element to be appended to this list
     * @return {@code true} (as specified by {@link java.util.Collection#add})
     */
    public boolean addSalesDepartmentListener(SalesDepartmentListener salesDepartmentListener) {
        return salesDepartmentListeners.add(salesDepartmentListener);
    }

    /**
     * Removes the first occurrence of the specified element from this list,
     * if it is present.  If this list does not contain the element, it is
     * unchanged.  More formally, removes the element with the lowest index
     * {@code i} such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>
     * (if such an element exists).  Returns {@code true} if this list
     * contained the specified element (or equivalently, if this list
     * changed as a result of the call).
     *
     * @param o element to be removed from this list, if present
     * @return {@code true} if this list contained the specified element
     */
    public boolean removeSalesDepartmentListener(SalesDepartmentListener o) {
        return salesDepartmentListeners.remove(o);
    }

    /**
     * Get an unmodifiable view to the listeners
     * @return
     */
    public List<SalesDepartmentListener> getSalesDepartmentListeners() {
        return Collections.unmodifiableList(salesDepartmentListeners);
    }

    /**
     * Basically asks whether or not the salesDepartment has anything to sell currently.
     * @return
     */
    public boolean hasAnythingToSell(){
        return !toSell.isEmpty();
    }

    /**
     * How many goods are left to be sold by the department
     * @return goods to sell
     */
    public int getHowManyToSell(){
        return toSell.size();
    }


    /**
     * Notify the listeners and the logger/gui that a good was sold! Also count it among the daily goods sold
     */
    public void fireGoodSoldEvent(SaleResult saleResult){



        for(SalesDepartmentListener listener : salesDepartmentListeners)
            listener.goodSoldEvent(this,saleResult);



    }

    /**
     * When this is true, the sales department peddles its goods around when it can't make a quote.
     * If this is false and the sales department can't quote, it just passively wait for buyers
     */
    public boolean isCanPeddle() {
        return canPeddle;
    }

    /**
     * When this is true, the sales department peddles its goods around when it can't make a quote.
     * If this is false and the sales department can't quote, it just passively wait for buyers
     */
    public void setCanPeddle(boolean canPeddle) {
        this.canPeddle = canPeddle;
    }


    /**
     * Gets goods sold today. Reset every day at PREPARE_TO_TRADE step.
     *
     * @return Value of goods sold today. Reset every day at PREPARE_TO_TRADE step.
     */
    public int getTodayOutflow() {
        return todayOutflow;
    }

    /**
     * Gets goods that were given to us to sell today.   Reset every day at PREPARE_TO_TRADE step.
     *
     * @return Value of goods that were given to us to sell today.
     */
    public int getTodayInflow() {
        return todayInflow;
    }
}
