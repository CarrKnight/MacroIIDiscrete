/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.pricing.pid;

import agents.EconomicAgent;
import agents.firm.Firm;
import com.google.common.base.Preconditions;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.pid.PIDController;
import agents.firm.sales.SaleResult;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentListener;
import agents.firm.sales.pricing.AskPricingStrategy;
import financial.BidListener;
import financial.market.Market;
import financial.MarketEvents;
import financial.TradeListener;
import financial.utilities.Quote;
import goods.Good;
import sim.engine.SimState;
import sim.engine.Steppable;

import javax.annotation.Nonnull;

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
public class SimpleFlowSellerPID implements TradeListener, BidListener, SalesDepartmentListener, AskPricingStrategy,
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
    private PIDController controller;

    /**
     * The price we are currently charging.
     */
    long price = 0;

    /**
     * if flow targeting is true, then rather than trying to keep inventory at level 0 at any check you just try to keep inflow and outflow constant
     */
    boolean flowTargeting = true;

    /**
     * This is the object we use to guess our stockouts
     */
    private StockoutEstimator stockOuts;

    /**
     * How many goods are left unsold at the beginning of the run
     */
    private int initialInventory = 0;




    /**
     * Constructor that generates at random the seller PID from the model randomizer
     * @param sales
     */
    public SimpleFlowSellerPID(@Nonnull SalesDepartment sales) {
        this(sales, sales.getFirm().getModel().drawProportionalGain()/20,  sales.getFirm().getModel().drawIntegrativeGain()/20,
                sales.getFirm().getModel().drawDerivativeGain(),sales.getFirm().getModel().drawPIDSpeed()  );
    }


    public SimpleFlowSellerPID(final SalesDepartment sales, float proportionalGain, float integralGain, float derivativeGain,
                               int speed) {
        this.sales = sales;
        market =sales.getMarket();
        //addSalesDepartmentListener yourself as a listener!
        sales.addSalesDepartmentListener(this);
        market.addTradeListener(this);

        if(market.areAllQuotesVisibile()) //if the order book is visible
        {
            //listen to bids
            market.addBidListener(this);
            //use the order book stockouts
            stockOuts = new OrderBookStockout(this);
        }
        else
        {
            //use the decentralized stockout then.
            stockOuts = new DecentralizedStockout(this);
            //automatically set up as flow targeter
            flowTargeting = true;

        }

        controller = new PIDController(proportionalGain,integralGain,derivativeGain,speed,sales.getRandom());
        //keep speed fixed if we are targeting flows rather than stock (otherwise you compare different periods and that's silly)
        if(flowTargeting)
            controller.setRandomSpeed(false);
        //start with a random price!
        price = sales.getFirm().getRandom().nextInt(100);
        controller.setOffset(price);
        //schedule yourself to change prices
        sales.getFirm().getModel().scheduleSoon(ActionOrder.ADJUST_PRICES, this);
        //schedule yourself also to annoy reset stockflow
        sales.getFirm().getModel().scheduleSoon(ActionOrder.PREPARE_TO_TRADE,new Steppable() {
            @Override
            public void step(SimState state) {
                if(!active)
                    return;

                //reset counters
                goodsSold =0;
                goodsToSell = sales.getHowManyToSell();
                initialInventory = goodsToSell;

                //reschedule automatically
                sales.getFirm().getModel().scheduleTomorrow(ActionOrder.PREPARE_TO_TRADE,this);

            }
        });






    }

    /**
     * When the pricing strategy is changed or the firm is shutdown this is called. It's useful to kill off steppables and so on
     */
    @Override
    public void turnOff() {
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
     *
     * @param owner the owner of the sales department
     * @param dept  the sales department asked
     * @param good  the good being sold
     */
    @Override
    public void sellThisEvent(@Nonnull Firm owner, @Nonnull SalesDepartment dept, @Nonnull Good good) {
        assert dept == sales;
        assert sales.getFirm() == owner;
        assert owner.has(good);
        //goodsToSell++;
        //percolate
        stockOuts.sellThisEvent(owner,dept,good);
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
     *
     * @param dept   The department
     * @param result The saleResult object describing the trade!
     */
    @Override
    public void goodSoldEvent(@Nonnull SalesDepartment dept, @Nonnull SaleResult result) {
        goodsSold++;
        stockOuts.goodSoldEvent(dept,result);

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

        Preconditions.checkState(((MacroII)simState).getCurrentPhase().equals(ActionOrder.ADJUST_PRICES));
        goodsToSell = sales.getHowManyToSell();
        //make the PID adjust, please


        if(flowTargeting) {
            gap = (goodsToSell - ((float) goodsSold + (float) stockOuts.getStockouts()));
            controller.adjust(0, gap,    //notice how I write: flowOut-flowIn, this is because price should go DOWN if flowIn>flowOut
                    active, (MacroII) simState, this, ActionOrder.ADJUST_PRICES);
        }
        else
        {
            //gap =  (float) (goodsToSell - ((float) goodsSold + (float) stockOuts.getStockouts()));
            gap = goodsToSell - stockOuts.getStockouts();
            controller.adjust(0, (float) (goodsToSell - ((float) goodsSold + (float) stockOuts.getStockouts())),    //notice how I write: flowOut-flowIn, this is because price should go DOWN if flowIn>flowOut
                    active, (MacroII)simState, this,ActionOrder.ADJUST_PRICES);
        }



        //get the new price
        price = Math.round(controller.getCurrentMV());
        //log the change in policy
        getSales().getFirm().logEvent(getSales(), MarketEvents.CHANGE_IN_POLICY, getSales().getFirm().getModel().getCurrentSimulationTimeInMillis(), "toSell: " + goodsToSell + ", customers:"
                + (goodsSold + stockOuts.getStockouts()) + "of which, stockouts: " + stockOuts.getStockouts() + "\n oldprice:" + oldPrice + ", newprice:" + price + " || MV: " + controller.getCurrentMV() );



        //we changed the prices, need to update the stockout counter
        stockOuts.newPIDStep(market);


        //we are being restepped by the controller so just wait.
        sales.updateQuotes();










    }

    /**
     * Resets the PID at this new price
     * @param price the new price
     */
    public void setInitialPrice(long price) {
        controller.setOffset(price);
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
    public long price(Good g) {
        if(productionCostOverride)
        {
            if(g.getCostOfProduction() > price) //force to increase MV
            {
                price = g.getCostOfProduction();
                controller.setOffset(g.getCostOfProduction());
            }
            return price;
        }
        else
            return Math.max(price,0l);

    }


    /**
     * If we could have supplied the new bid but we don't have goods, this is a stockout!
     *
     * @param buyer the agent placing the bid
     * @param price the price of the good
     */
    @Override
    public void newBidEvent(@Nonnull EconomicAgent buyer, long price,Quote bestAsk) {

        stockOuts.newBidEvent(buyer,price, bestAsk);


    }

    /**
     * If this bid made us think of stockout, lower stockout count
     *
     * @param buyer the agent placing the bid
     * @param quote the removed quote
     */
    @Override
    public void removedBidEvent(@Nonnull EconomicAgent buyer, @Nonnull Quote quote) {
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
    public void stockOutEvent(@Nonnull Firm owner, @Nonnull SalesDepartment dept, @Nonnull EconomicAgent buyer) {
        stockOuts.stockOutEvent(owner,dept,buyer);
    }

    /**
     * The price suggested by the PID
     * @return
     */
    public long getTargetPrice() {
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
        controller.setIntegralGain(integralGain);
    }

    public float getIntegralGain() {
        return controller.getIntegralGain();
    }

    public void setProportionalGain(float proportionalGain) {
        controller.setProportionalGain(proportionalGain);
    }

    public float getProportionalGain() {
        return controller.getProportionalGain();
    }

    public float getDerivativeGain() {
        return controller.getDerivativeGain();
    }

    public void setDerivativeGain(float derivativeGain) {
        controller.setDerivativeGain(derivativeGain);
    }

    public int getSpeed() {
        return controller.getSpeed();
    }
}
