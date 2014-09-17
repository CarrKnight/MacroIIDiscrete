/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.pricing.pid;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentListener;
import agents.firm.sales.pricing.BaseAskPricingStrategy;
import com.google.common.base.Preconditions;
import financial.BidListener;
import financial.TradeListener;
import financial.market.Market;
import financial.utilities.Quote;
import goods.Good;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.logs.LogEvent;
import model.utilities.logs.LogLevel;
import model.utilities.pid.Controller;
import model.utilities.pid.ControllerInput;
import model.utilities.pid.PIDController;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.function.Function;


/**
 * <h4>Description</h4>
 * <p/> This PID strategy tries to target the sale flow, basically it wants: stuffToSell = numberOfCustomers and changes prices. <br>
 *     Unfortunately for things like OrderBook Market we can't really have numberOfCustomers so we will estimate it as: goodsSold + stockOuts where stockOuts is how many trades
 *     occur at a price ABOVE our last closing price when the sales department has NO goods to sell.
 * <p/> In order to learn stockouts events we make this strategy a TradeListener (if the quotes aren't visible) or BidListener.
 * <p/>  It's important that <b>PID can be overruled by production costs</b> , but that is set off by default. Still price is never negative!
 * <h4>Notes</h4>
 * It's important to notice that the sales department needs to invert the PID controller. I obviate to this by simpling supplying the inverse of the target.
 * That is the target will be coded as goodsSold + stockout - goodsToSell --> 0 This way negative error (more goods to sell than sold) will push prices down.
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-27
 * @see
 */
public class SimpleFlowSellerPID extends BaseAskPricingStrategy implements TradeListener, BidListener, SalesDepartmentListener,
        Steppable{

    /**
     * The sales department associated with this rule
     */
    private final SalesDepartment sales;

    /**
     * The market the pid is listening to
     */
    private final Market market;

    /**
     * Is this model active?
     */
    private boolean active = true;

    /**
     * How many goods are we supposed to sell?
     */
    private int goodsToSell = 0;

    /**
     *How many goods were sold at a HIGHER price than our last closing price while we had nothing to sell? (For any given PID period)
     * */
    private int goodsSold = 0;

    /**
     * basically the error fed in the pid
     */
    private float gap = 0;


    /**
     * If the price we are quoting is LOWER than the production cost, what should we do? if true we quote the production cost if lower than the PID proposed price.
     */
    private boolean productionCostOverride = false;


    /**
     * The PID controller used by this firm.
     */
    protected Controller controller;

    /**
     * The price we are currently charging.
     */
    int price = 0;

    public static final boolean flowTargetingDefault = true;
    /**
     * if flow targeting is true, then rather than trying to keep inventory at level 0 at any check you just try to keep inflow and outflow constant
     */
    private boolean flowTargeting = flowTargetingDefault;

    /**
     * This is the object we use to guess our stockouts
     */
    private StockoutEstimator stockOuts;

    /**
     * How many goods are left unsold at the beginning of the run
     */
    private int initialInventory = 0;
    private PIDController rootController;


    /**
     * Constructor that generates at random the seller PID from the model randomizer
     * @param sales
     */
    public SimpleFlowSellerPID( SalesDepartment sales) {
        this(sales, sales.getFirm().getModel().drawProportionalGain()/5,  sales.getFirm().getModel().drawIntegrativeGain()/5,
                sales.getFirm().getModel().drawDerivativeGain(),sales.getFirm().getModel().drawPIDSpeed(), sales.getMarket(),
                sales.getRandom().nextInt(100), sales.getModel());
    }


    public SimpleFlowSellerPID(final SalesDepartment sales, float proportionalGain, float integralGain, float derivativeGain,
                               int speed, Market market, int initialPrice, final MacroII model) {
        this.sales = sales;
        this.market = market;
        //addSalesDepartmentListener yourself as a listener!
        sales.addSalesDepartmentListener(this);
        this.market.addTradeListener(this);

        if(this.market.areAllQuotesVisibile()) //if the order book is visible
        {
            //listen to bids
            this.market.addBidListener(this);
            //use the order book stockouts
            stockOuts = new AfterTradeCounter(this.market,sales);
        }
        else
        {
            //use the decentralized stockout then.
            stockOuts = new DecentralizedStockout(this);
            //automatically set up as flow targeter
            flowTargeting = true;

        }

        rootController = new PIDController(proportionalGain, integralGain, derivativeGain, speed);
        rootController.setInvertSign(true);
        controller = rootController;
        //keep speed fixed if we are targeting flows rather than stock (otherwise you compare different periods and that's silly)
        //start with a random price!
        price = initialPrice;
        controller.setOffset(price, true);

        //schedule yourself to change prices
        if(speed == 0)
            model.scheduleSoon(ActionOrder.ADJUST_PRICES, this);
        else
            model.scheduleAnotherDay(ActionOrder.ADJUST_PRICES, this, speed);
        //schedule yourself also to annoy reset stockflow
        model.scheduleSoon(ActionOrder.PREPARE_TO_TRADE, new Steppable() {
            @Override
            public void step(SimState state) {
                if (!active)
                    return;

                //reset counters
                goodsSold = 0;
                goodsToSell = sales.getHowManyToSell();
                initialInventory = goodsToSell;


                //reschedule automatically
                model.scheduleTomorrow(ActionOrder.PREPARE_TO_TRADE, this);

            }


        });
        model.scheduleSoon(ActionOrder.THINK, new Steppable() {
            @Override
            public void step(SimState simState) {
                if (!active)
                    return;
                //we changed the prices, need to update the stockout counter
                stockOuts.newPIDStep(SimpleFlowSellerPID.this.market);

            }
        });






    }

    /**
     * When the pricing strategy is changed or the firm is shutdown this is called. It's useful to kill off steppables and so on
     */
    @Override
    public void turnOff() {
        super.turnOff();
        //make sure we were active before!
        assert active;
        active = false;
        //stop listening
        boolean removed = market.removeTradeListener(this);
        assert removed;
        removed = sales.removeSalesDepartmentListener(this);
        assert removed;
        market.removeBidListener(this); //this might fail because not every time we are listening to bids
    }

    /**
     * Tell the listener the firm just tasked the salesdepartment to sell a new good
     *  @param owner the owner of the sales department
     * @param dept  the sales department asked
     * @param amount
     */
    @Override
    public void sellThisEvent(Firm owner, SalesDepartment dept, int amount) {
        assert dept == sales;
        assert sales.getFirm() == owner;
        //goodsToSell++;
        //percolate
        stockOuts.sellThisEvent(owner,dept, 1);
    }





    public void decorateController(Function<PIDController,Controller> decorationMaker)
    {
        controller = decorationMaker.apply(rootController);
    }


    /**
     * Tell the listener a trade has been carried out
     *
     * @param buyer         the buyer of this trade
     * @param seller        the seller of this trade
     * @param goodExchanged the good that has been traded
     * @param price         the price of the trade
     */
    @Override
    public void tradeEvent(EconomicAgent buyer, EconomicAgent seller, Good goodExchanged, long price, Quote sellerQuote, Quote buyerQuote) {
        stockOuts.tradeEvent(buyer,seller,goodExchanged,price,sellerQuote,buyerQuote);
    }

    /**
     * This logEvent is fired whenever the sales department managed to sell a good!
     * @param dept   The department
     * @param price
     */
    @Override
    public void goodSoldEvent(SalesDepartment dept, int price) {
        goodsSold++;
        stockOuts.goodSoldEvent(dept, price );

    }

    /**
     * Step refers to the PID adjust.
     * @param simState
     */
    @Override
    public void step(SimState simState) {

        if(!active) //if we are off, we are done
            return;
        //remember the old price
        long oldPrice = price;

        Preconditions.checkState(((MacroII)simState).getCurrentPhase().equals(ActionOrder.ADJUST_PRICES), ((MacroII)simState).getCurrentPhase());
    //
        //make the PID adjust, please
        goodsToSell = sales.getHowManyToSell();


        final int stockouts = stockOuts.getStockouts();
        if(flowTargeting) {

            float inflow = sales.getTodayInflow();
            float outflow = sales.getTodayOutflow() + stockouts;
            gap = -(outflow-inflow);



            controller.adjust(new ControllerInput(inflow,outflow),    //notice how I write: flowOut-flowIn, this is because price should go DOWN if flowIn>flowOut
                    active, (MacroII) simState, this, ActionOrder.ADJUST_PRICES);
        }
        else
        {
            //gap =  (float) (goodsToSell - ((float) goodsSold + (float) stockOuts.getStockouts()));
            gap = stockouts - goodsToSell ;
            controller.adjust(new ControllerInput(0,gap),    //notice how I write: flowOut-flowIn, this is because price should go DOWN if flowIn>flowOut
                    active, (MacroII)simState, this,ActionOrder.ADJUST_PRICES);
        }



        //get the new price
        price = Math.round(controller.getCurrentMV());
        //log the change in policy
        handleNewEvent(new LogEvent(this, LogLevel.DEBUG,
                "PID policy change, toSell: {}, customers:{}, of which, stockouts: {}\n oldprice:{} newprice:{} , MV:{}",
        goodsToSell,(goodsSold+ stockouts), stockouts,oldPrice,price,controller.getCurrentMV()));





        //we are being restepped by the controller so just wait.
        sales.updateQuotes();










    }

    /**
     * Resets the PID at this new price
     * @param price the new price
     */
    public void setInitialPrice(int price) {
        controller.setOffset(price, true);
        this.price = price;
    }



    /**
     * The sales department is asked at what should be the sale price for a specific good; this, I guess, is the fundamental
     * part of the sales department
     *
     * @param g the good to price
     * @return the price given to that good
     */
    @Override
    public int price(Good g) {
        if(productionCostOverride)
        {
            if(g.getCostOfProduction() > price) //force to increase MV
            {
                price = g.getCostOfProduction();
                controller.setOffset(g.getCostOfProduction(), true);
            }
            return price;
        }
        else
            return Math.max(price,0);

    }


    /**
     * If we could have supplied the new bid but we don't have goods, this is a stockout!
     *
     * @param buyer the agent placing the bid
     * @param price the price of the good
     */
    @Override
    public void newBidEvent( EconomicAgent buyer, long price,Quote bestAsk) {

        stockOuts.newBidEvent(buyer,price, bestAsk);


    }

    /**
     * If this bid made us think of stockout, lower stockout count
     *
     * @param buyer the agent placing the bid
     * @param quote the removed quote
     */
    @Override
    public void removedBidEvent( EconomicAgent buyer,  Quote quote) {
        stockOuts.removedBidEvent(buyer,quote);
    }

    /**
     * After computing all statistics the sales department calls the weekEnd method. This might come in handy
     */
    @Override
    public void weekEnd() {

        //the weekend does nothing but it might be subclassed.

    }

    /**
     * tries to sell everything
     *
     * @param inventorySize
     * @return
     */
    @Override
    public boolean isInventoryAcceptable(int inventorySize) {
        return inventorySize == 0; //tries to sell everything

    }

    public SalesDepartment getSales() {
        return sales;
    }

    /**
     * Tell the listener a peddler just came by and we couldn't service him because we have no goods
     *
     * @param owner the owner of the sales department
     * @param dept  the sales department asked
     */
    @Override
    public void stockOutEvent( Firm owner,  SalesDepartment dept,  EconomicAgent buyer) {
        stockOuts.stockOutEvent(owner,dept,buyer);
    }

    /**
     * The price suggested by the PID
     * @return
     */
    public int getTargetPrice() {
        return price;
    }

    public int getInitialInventory() {
        return initialInventory;
    }

    /**
     * All inventory is unwanted
     */
    @Override
    public float estimateSupplyGap() {

        return gap;
    }

    /**
     * Basically asks whether or not the salesDepartment has anything to sell currently.
     * @return
     */
    public boolean hasAnythingToSell() {
        return sales.hasAnythingToSell();
    }


    /**
     * How many goods are left to be sold by the department
     * @return goods to sell
     */
    public int getHowManyToSell() {
        return sales.getHowManyToSell();
    }


    /**
     * Change the gains of the PID
     */
    public void setGains(float proportionalGain, float integralGain, float derivativeGain) {
        controller.setGains(proportionalGain, integralGain, derivativeGain);
    }

    public void setSpeed(int speed) {
        controller.setSpeed(speed);
    }


    /**
     * Gets If the price we are quoting is LOWER than the production cost, what should we do? if true we quote the production cost if lower than the PID proposed price..
     *
     * @return Value of If the price we are quoting is LOWER than the production cost, what should we do? if true we quote the production cost if lower than the PID proposed price..
     */
    public boolean isProductionCostOverride() {
        return productionCostOverride;
    }

    /**
     * Sets new If the price we are quoting is LOWER than the production cost, what should we do? if true we quote the production cost if lower than the PID proposed price..
     *
     * @param productionCostOverride New value of If the price we are quoting is LOWER than the production cost, what should we do? if true we quote the production cost if lower than the PID proposed price..
     */
    public void setProductionCostOverride(boolean productionCostOverride) {
        this.productionCostOverride = productionCostOverride;
    }


    //--------------------------------------------------------------------------


    public void setIntegralGain(float integralGain) {
        rootController.setIntegralGain(integralGain);
    }

    public float getIntegralGain() {
        return controller.getIntegralGain();
    }

    public void setProportionalGain(float proportionalGain) {
        rootController.setProportionalGain(proportionalGain);
    }

    public float getProportionalGain() {
        return controller.getProportionalGain();
    }

    public float getDerivativeGain() {
        return controller.getDerivativeGain();
    }

    public void setDerivativeGain(float derivativeGain) {
        rootController.setDerivativeGain(derivativeGain);
    }

    public int getSpeed() {
        return controller.getSpeed();
    }

    public boolean isFlowTargeting() {
        return flowTargeting;
    }

    public void setFlowTargeting(boolean flowTargeting) {
        this.flowTargeting = flowTargeting;
    }
}
