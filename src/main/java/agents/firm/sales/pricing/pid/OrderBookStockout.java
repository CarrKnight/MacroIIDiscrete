/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.pricing.pid;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.sales.SaleResult;
import agents.firm.sales.SalesDepartment;
import financial.market.Market;
import financial.utilities.Quote;
import goods.Good;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * <h4>Description</h4>
 * <p/>
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-28
 * @see
 */
public class OrderBookStockout implements StockoutEstimator {


    /**
     * These are possible stockouts, but there might still be a chance of getting them!
     */
    private List<EconomicAgent> opportunities = new LinkedList<>();

    /**
     * These are "confirmed" stockouts. We lost such opportunities "forever"
     */
    private List<EconomicAgent> foregoneOpportunities = new LinkedList<>();


    private SimpleFlowSellerPID strategy;

    public OrderBookStockout(SimpleFlowSellerPID strategy) {
        this.strategy = strategy;
    }

    /**
     * checks the market
     */
    @Override
    public void newPIDStep(Market market) {

        //clear old unsatistfied customers, we don't care
        opportunities.clear();
        foregoneOpportunities.clear();

        if(!market.areAllQuotesVisibile())
            throw new IllegalArgumentException("OrderBookStockout requires a market that has all quotes visible!");


        long ourPrice = strategy.getTargetPrice();

        try {
            Iterator<Quote> bids = market.getIteratorForBids();

            while(bids.hasNext())
            {
                //two possible options for each bids: we have something to sell but the bid is too low
                //or we got nothing to sell,
                Quote bid = bids.next();
                long price = bid.getPriceQuoted();

                if(price >= ourPrice)
                {
                    //if they are willing to shell good money for this good, how come we haven't provided them something? we must be running dry! (or the production costs are too high)
//                    assert !strategy.getSales().hasAnythingToSell() || strategy.getSales().getLastClosingCost() > ourPrice;
                    //this is a customer we should provide for!
                    opportunities.add(bid.getAgent());

                }


            }


        } catch (IllegalAccessException e) {

            throw new RuntimeException("Didn't I check if quotes were visible?");
        }

    }

    @Override
    public int getStockouts() {
        return opportunities.size() + foregoneOpportunities.size();
    }

    /**
     * Tell the listener a new bid has been placed into the market
     *
     * @param buyer the agent placing the bid
     * @param price the price of the good
     */
    @Override
    public void newBidEvent(@Nonnull final EconomicAgent buyer, final long price, final Quote bestAsk) {
        //if we are empty
        if(!strategy.getSales().hasAnythingToSell())
        {
            assert bestAsk == null || bestAsk.getAgent() != strategy.getSales().getFirm(); //come on, we can't be the one who placed the best ask AND we are empty!

            long ourPrice = strategy.getTargetPrice();


            //THERE IS A BEST ASK AND IT'S GOING TO CLEAR
            if(bestAsk != null && bestAsk.getPriceQuoted() < 0 && bestAsk.getPriceQuoted() <= price)
            {
                //could we have outcompeted the best ask?
                if(ourPrice <= bestAsk.getPriceQuoted())
                    foregoneOpportunities.add(buyer); //we can't fix this anymore!
            }
            else  //there is no best ask or the best ask is too high.
            {
                //it's not going to clear immediately: could we have serviced this person?
                if(price >= ourPrice)
                    opportunities.add(buyer); //we could have serviced this guy!
                //maybe we will soon though

            }

        }
    }

    /**
     * Tell the listener a new bid has been placed into the market
     *
     * @param buyer the agent placing the bid
     * @param quote the removed quote
     */
    @Override
    public void removedBidEvent(@Nonnull EconomicAgent buyer, @Nonnull Quote quote) {
        //if the quote was something we wanted something to do with the opportunity is not there anymore but not really foregone
        if(quote.getPriceQuoted() >= strategy.getTargetPrice())
        {
            boolean removedSuccesfully = opportunities.remove(buyer); //this should have been on our radar
//            assert removedSuccesfully : quote.getPriceQuoted();
        }
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
        //we don't care.
    }

    /**
     * This logEvent is fired whenever the sales department managed to sell a good!
     *
     * @param dept   The department
     * @param result The saleResult object describing the trade!
     */
    @Override
    public void goodSoldEvent(@Nonnull SalesDepartment dept, @Nonnull SaleResult result) {
        //if we sold to somebody we were eyeing (that is, he was listed among opportunities) then we are going to deal with it in tradeEvent

    }

    /**
     * Tell the listener a peddler just came by and we couldn't service him because we have no goods
     *
     * @param owner the owner of the sales department
     * @param dept  the sales department asked
     */
    @Override
    public void stockOutEvent(@Nonnull Firm owner, @Nonnull SalesDepartment dept, @Nonnull EconomicAgent buyer) {
        //i guess this is classic foregone opportunity
        foregoneOpportunities.add(buyer);
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


        if(seller == strategy.getSales().getFirm()){  //if we were the ones who sold!
            //if we were eyeing the buyer, this is no more a stockout!
            opportunities.remove(buyer);
        }
        else if(!strategy.getSales().hasAnythingToSell())  //if we are empty
        {
            //if this was something we could have provided for
            if( buyerQuote.getPriceQuoted() >= strategy.getTargetPrice())
            {
                //the only way we didn't get this is either the other guy was selling at  lower price or we aren't in the market
                long ourCompetitorAsk = sellerQuote.getPriceQuoted();
                //this is no more an opportunity
                boolean removed = opportunities.remove(buyer); //this is no more an opportunity
//                assert removed || (ourCompetitorAsk >=0 && ourCompetitorAsk <= strategy.getTargetPrice()) ;  //either we recorded this as an opportunity or we knew ahead of time we were outcompeted (immediate trade)




                if(strategy.getTargetPrice() <= ourCompetitorAsk) //if we sell at a better price
                {
                    assert !strategy.getSales().hasAnythingToSell(); //we must still have empty inventories
                    //this is really a stockout lost opportunity
                    foregoneOpportunities.add(buyer);
                }

            }

        }
    }
}
