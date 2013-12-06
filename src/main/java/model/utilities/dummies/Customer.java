/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.dummies;

import agents.EconomicAgent;
import com.google.common.base.Preconditions;
import financial.market.Market;
import financial.utilities.PurchaseResult;
import financial.utilities.Quote;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.scheduler.Priority;
import sim.engine.SimState;
import sim.engine.Steppable;

import javax.annotation.Nonnull;

/**
 * <h4>Description</h4>
 * <p/> This is supposed to be a simple dummy buyer that repeatedly place the same quotes
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-11-30
 * @see
 */
public class Customer extends EconomicAgent{


    /**
     * how many units do you want to buy every day?
     */
    private int dailyDemand = 1;

    /**
     * the maximum price the customer is willing to pay for its stuff, that is
     * oilPrice + distanceCost * distance <= maxPrice to buy.
     */
    private long maxPrice;


    /**
     * every day, after consuming the customer gets back the total cash to this number
     */
    private long resetCashTo = 100000;

    private final Market market;


    public Customer(MacroII model, long maxPrice, Market market) {
        super(model);
        Preconditions.checkArgument(maxPrice > 0);
        this.maxPrice = maxPrice;
        this.market = market;
        market.registerBuyer(this);
        startSteppables(market);
    }



    private void startSteppables(final Market market)
    {
        //every day, at "production" eat all the oil and start over (also make sure your cash remains the same)
        model.scheduleSoon(ActionOrder.PRODUCTION, new Steppable() {
            @Override
            public void step(SimState state) {
                if(!isActive())
                {
                    return;
                }
                eatEverythingAndResetCash();
                removeAllQuotes(market);

                //reschedule yourself
                model.scheduleTomorrow(ActionOrder.PRODUCTION,this);
            }
        });

        //schedule yourself to try to buy every day
        model.scheduleSoon(ActionOrder.TRADE, new Steppable() {
            @Override
            public void step(SimState state) {
                if(!isActive())
                {
                    return;
                }

                buyIfNeeded(market);
                //reschedule yourself
                model.scheduleTomorrow(ActionOrder.TRADE,this, Priority.AFTER_STANDARD);

            }
        });

    }



    private void buyIfNeeded(Market market)
    {
        //cancel all previous quotes
        //place all the orders you need
        for(int i=0; i<dailyDemand-hasHowMany(market.getGoodType()); i++)
            market.submitBuyQuote(this,maxPrice);





    }

    private void removeAllQuotes(Market market) {
        market.removeAllBuyQuoteByBuyer(this);
    }


    private void eatEverythingAndResetCash() {
        //eat all!
        consumeAll();
        long cashDifference = resetCashTo-getCash();
        if(cashDifference > 0)
            earn(cashDifference);
        else if(cashDifference < 0)
            burnMoney(-cashDifference);

        assert getCash() == resetCashTo;
    }



    public int getDailyDemand() {
        return dailyDemand;
    }

    public void setDailyDemand(int dailyDemand) {
        this.dailyDemand = dailyDemand;
    }

    public long getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(long maxPrice) {
        this.maxPrice = maxPrice;
    }

    public long getResetCashTo() {
        return resetCashTo;
    }

    public void setResetCashTo(long resetCashTo) {
        this.resetCashTo = resetCashTo;
    }

    public Market getMarket() {
        return market;
    }




    @Override
    public void reactToFilledAskedQuote(Good g, long price, EconomicAgent buyer) {
        //nothing happens. We placed all our quotes already
    }

    @Override
    public void reactToFilledBidQuote(Good g, long price, EconomicAgent seller) {
        //nothing happens. We placed all our quotes already
    }

    /**
     * This is called by a peddler/survey to the agent and asks what would be the maximum he's willing to pay to buy a new good.
     *
     * @param t the type good the peddler/survey is pushing
     * @return the maximum price willing to pay or -1 if no price is good.
     */
    @Override
    public long askedForABuyOffer(GoodType t) {
        return -1;
    }

    /**
     * A buyer asks the sales department for the price they are willing to sell one of their good.
     *
     * @param buyer the economic agent that asks you that
     * @return a price quoted or -1 if there are no quotes
     */
    @Override
    public Quote askedForASaleQuote(EconomicAgent buyer, GoodType type) {
        return null;
    }

    /**
     * Called by a buyer that wants to buy directly from this agent, not going through the market.
     *
     * @param buyerQuote  the quote (including the offer price) of the buyer
     * @param sellerQuote the quote (including the offer price) of the seller; I expect the buyer to have achieved this through asked for an offer function
     * @return a purchaseResult including whether the trade was succesful and if so the final price
     */
    @Nonnull
    @Override
    public PurchaseResult shopHere(@Nonnull Quote buyerQuote, @Nonnull Quote sellerQuote) {
        return PurchaseResult.NO_MATCH_AVAILABLE;

    }

    /**
     * how "far" purchases inventory are from target.
     */
    @Override
    public int estimateDemandGap(GoodType type) {
        return hasHowMany(market.getGoodType()) - dailyDemand;
    }

    /**
     * how "far" sales inventory are from target.
     */
    @Override
    public int estimateSupplyGap(GoodType type) {
        return 0;
    }




    @Override
    public void turnOff() {
        super.turnOff();
        assert !isActive();
        //remove all quotes
        removeAllQuotes(market);
        //deregister yourself
        market.deregisterBuyer(this);
    }

    /**
     * This is called by a peddler/survey to the agent and asks what would be the maximum he's willing to pay to buy a new good.
     *
     * @param g the good the peddler/survey is pushing
     * @return the maximum price willing to pay or -1 if no price is good.
     */
    @Override
    public long maximumOffer(Good g) {
        //do not peddle
        return -1;
    }


}

