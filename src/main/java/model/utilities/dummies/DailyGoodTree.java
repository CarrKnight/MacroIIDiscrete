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
import sim.engine.SimState;
import sim.engine.Steppable;

import javax.annotation.Nonnull;

/**
 * <h4>Description</h4>
 * <p/> This is basically the replacement for manual dummy sellers: agents that every day sell a fixed number of units of goods at a minimum price
 * <p/> Everything unsold is destroyed.
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-12-18
 * @see
 */
public class DailyGoodTree extends EconomicAgent
{

    /**
     * how many units of goods do you try to sell everyday
     */
    private int dailySupply;

    private long minPrice;

    private final Market market;

    private String name;

    public DailyGoodTree(MacroII model,int dailySupply, long minPrice, Market market)
    {
        super(model);
        Preconditions.checkArgument(minPrice >= 0);
        Preconditions.checkArgument(dailySupply > 0, "daily supply must be positive");
        this.minPrice = minPrice;
        this.market = market;
        this.dailySupply = dailySupply;
        market.registerSeller(this);

        startSteppables(model);
        name = market.getGoodType() + "Tree, price: " +minPrice;

    }

    public DailyGoodTree(MacroII model, long minPrice, Market market)
    {
        this(model,1,minPrice,market);

    }

    private void startSteppables(final MacroII model) {
        final DailyGoodTree goodTree = this;
        //two steppables:
        // FIRST: during production you consume everything you had, and create new goods to sell
        model.scheduleSoon(ActionOrder.PRODUCTION, new Steppable() {
            @Override
            public void step(SimState state) {
                if(!isActive())
                    return;
                eatEverythingAndRemoveQuotes();
                //create new goods
                createNewGoods(goodTree);

                assert hasAny(market.getGoodType());

                //reschedule this
                model.scheduleTomorrow(ActionOrder.PRODUCTION,this);

            }
        });
        //second, during trade, start by selling at least one good
        model.scheduleSoon(ActionOrder.TRADE,new Steppable() {
            @Override
            public void step(SimState state) {
                if(!isActive())
                {
                    return;
                }

                sellIfPossible(market);

                model.scheduleTomorrow(ActionOrder.TRADE,this);

            }
        });


    }

    /**
     * if it has anything to sell, the seller tries to sell one of its goods
     * @param market
     */
    private void sellIfPossible(Market market)
    {
        GoodType typeSold = market.getGoodType();
        if(hasAny(typeSold)){
            Good toSell = peekGood(typeSold);
            assert toSell != null;

            market.submitSellQuote(this,minPrice,toSell);
        }

    }

    private void createNewGoods(DailyGoodTree goodTree) {
        for(int i=0; i< dailySupply; i++)
        {
            receive(new Good(market.getGoodType(),goodTree,minPrice),null);
        }
    }

    private void eatEverythingAndRemoveQuotes() {
        consumeAll();
        market.removeAllSellQuoteBySeller(this);
    }

    @Override
    public void reactToFilledAskedQuote(Good g, long price, EconomicAgent buyer) {
        sellIfPossible(market);

    }

    @Override
    public void reactToFilledBidQuote(Good g, long price, EconomicAgent seller) {
        Preconditions.checkState(false, "good trees never buy!");
    }

    /**
     * This is called by a peddler/survey to the agent and asks what would be the maximum he's willing to pay to buy a new good.
     *
     * @param g the good the peddler/survey is pushing
     * @return the maximum price willing to pay or -1 if no price is good.
     */
    @Override
    public long maximumOffer(Good g) {
        return -1;
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

    @Override
    public void turnOff() {
        super.turnOff();
        assert !isActive();
        //remove all quotes
        eatEverythingAndRemoveQuotes();
        //deregister yourself
        market.deregisterBuyer(this);
    }

    /**
     * how "far" purchases inventory are from target.
     */
    @Override
    public int estimateDemandGap(GoodType type) {
        return 0;
    }

    /**
     * how "far" sales inventory are from target.
     */
    @Override
    public float estimateSupplyGap(GoodType type) {
        return hasHowMany(type);
    }

    public long getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(long minPrice) {
        this.minPrice = minPrice;
        name = market.getGoodType() + "Tree, price: " +minPrice;

    }

    @Override
    public String toString() {
        return name;
    }
}
