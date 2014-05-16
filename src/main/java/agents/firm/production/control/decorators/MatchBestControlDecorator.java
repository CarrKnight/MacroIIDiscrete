/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.production.control.decorators;

import agents.EconomicAgent;
import com.google.common.base.Preconditions;
import financial.BidListener;
import financial.TradeListener;
import financial.utilities.Quote;
import goods.Good;
import agents.firm.production.control.PlantControl;


/**
 * <h4>Description</h4>
 * <p/> This decorator never let the current wage being below the current best offer in the market.
 * To do so it must be true that:
 * <ul>
 *     <li> Best bid offer must be visible in the labor market</li>
 *     <li> The human resources must have fixed pay structure </li>
 * </ul>
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-10-09
 * @see
 */
public class MatchBestControlDecorator extends PlantControlDecorator implements BidListener, TradeListener{
    /**
     * instantiate the decorator
     *
     * @param toDecorate the plant control to decorate
     */
    public MatchBestControlDecorator(PlantControl toDecorate) {
        super(toDecorate);
        Preconditions.checkArgument(toDecorate.getHr().isFixedPayStructure(), "I don't think matchBest will work with non fixed pay structure");
        Preconditions.checkArgument(toDecorate.getHr().getMarket().isBestBuyPriceVisible(),
                "matchBest decorator can't work ");
        //register yourself as listener
        toDecorate.getHr().getMarket().addBidListener(this);
        toDecorate.getHr().getMarket().addTradeListener(this);

    }

    /**
     * the best offer in the market
     */
    private int bestMarketOffer=-1;

    /**
     * the offer of the normal plant control
     */
    private int originalOffer =0;

    /**
     * keeps track of what can buy would be if it wasn't for this decorator
     */
    private boolean originalCanBuy = true;


    /**
     * Records the original wage but intercepts the call and only make it go through if the original offer is higher than the best market one
     *
     * @param newWage the new wage
     */
    @Override
    public void setCurrentWage(int newWage) {
        originalOffer = newWage;
        updateCanBuy();
        updateCurrentWage();

    }

    /**
     * intercepts the call and set can buy to false until it is the best offer
     *
     * @param canBuy true if the hr can hire more people at this wage.
     */
    @Override
    public void setCanBuy(boolean canBuy) {
        originalCanBuy = canBuy;
        updateCanBuy(canBuy);


    }

    /**
     * Sets the new wage to be the best offer or the original offer if it's higher
     */
    private void updateCurrentWage()
    {
        //if there is a better offer in the market
        if(originalOffer < bestMarketOffer)
            toDecorate.setCurrentWage(bestMarketOffer); //match it!
        else //otherwise
            toDecorate.setCurrentWage(originalOffer); //we are the only ones
    }



    /**
     * check what's the new bestMarketOffer
     */
    private void updateBestMarketOffer()
    {
        try {
            //if somebody has a bigger wage offer than us: copy it. Don't copy yourself though
            if(toDecorate.getHr().getMarket().getBestBuyer() != toDecorate.getHr().getFirm())
                bestMarketOffer = toDecorate.getHr().getMarket().getBestBuyPrice();
        } catch (IllegalAccessException e) {
            throw new RuntimeException("matchBest decorator can't work");
        }
    }

    /**
     * Can buy is automatically set to off if the marketOffer > originalOffer
     */
    private void updateCanBuy(boolean toDecorateCanBuy)
    {
        if(originalOffer < bestMarketOffer)
            toDecorate.setCanBuy(false);
        else
        {
            assert originalOffer >= bestMarketOffer;
            toDecorate.setCanBuy(originalCanBuy);
        }
    }

    /**
     * Can buy is automatically set to off if the marketOffer > originalOffer
     */
    private void updateCanBuy(){
        updateCanBuy(toDecorate.canBuy());
    }

    /**
     * Tell the listener a new bid has been placed into the market
     *
     * @param buyer   the agent placing the bid
     * @param price   the price of the good
     * @param bestAsk the best ask when the bid was made
     */
    @Override
    public void newBidEvent( EconomicAgent buyer, long price, Quote bestAsk) {
        //record the new market offer
        updateBestMarketOffer();
        //if you did have to match it then stop buying until you receive confirmation
        updateCanBuy();
        //see if you have to match it
        updateCurrentWage();


    }

    /**
     * Tell the listener a new bid has been placed into the market
     *
     * @param buyer the agent placing the bid
     * @param quote the removed quote
     */
    @Override
    public void removedBidEvent( EconomicAgent buyer,  Quote quote) {
        updateBestMarketOffer();
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
        updateBestMarketOffer();
    }
}
