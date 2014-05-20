/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package financial.market;

import financial.Bankruptcy;
import financial.utilities.PurchaseResult;
import financial.utilities.Quote;
import model.MacroII;

import java.util.Queue;

/**
 * <h4>Description</h4>
 * <p/>  As soon as a new quote comes in, recursively clears all the crossing bids/asks
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-01-16
 * @see
 */
public class ImmediateOrderHandler implements OrderHandler {


    /**
     * start the handler, if needed
     */
    @Override
    public void start(MacroII model, Queue<Quote> asks, Queue<Quote> bids, OrderBookMarket market) {

        //ignored
    }

    /**
     * tell the handler a new quote arrived.
     *
     */
    @Override
    public void reactToNewQuote(Queue<Quote> asks, Queue<Quote> bids, OrderBookMarket market) {

        //recursively match quotes!
        boolean anyMatch;
        do{
            anyMatch = matchQuotes(asks, bids, market);
        }while (anyMatch);

    }

    /**
     * check best buy and ask quotes, if they cross then make agents trade!
     * @param asks  the queue of asks
     * @param bids  the queue of bids
     * @return true if a trade occurred
     */
    public static boolean matchQuotes(Queue<Quote> asks, Queue<Quote> bids, OrderBookMarket market)
    {
        if(bids.isEmpty() || asks.isEmpty())
            return false;  //if any of the two is empty, nothing to match

        Quote bestBid = bids.peek(); //check best bid
        Quote bestAsk = asks.peek(); //check best ask

        //if best bid and best ask cross
        if(bestBid.getPriceQuoted() >= bestAsk.getPriceQuoted())
        {
            //price is somewhere in the middle
            int price = market.price(bestAsk.getPriceQuoted(), bestBid.getPriceQuoted());

            //sanity check
            assert price >= bestAsk.getPriceQuoted();
            assert price <= bestBid.getPriceQuoted();
            assert bestAsk.getGood().getType().equals(bestBid.getType()); //they should be buying the same stuff


            //TODO need to addSalesDepartmentListener a bankruptcy check that remove the bid, restarts the match quotes and notifies the bidder he's out of money!


            PurchaseResult result = market.trade(bestBid.getAgent(), bestAsk.getAgent(), bestAsk.getGood(), price, bestBid, bestAsk);  //TRADE!
            if(result == PurchaseResult.BUYER_HAS_NO_MONEY)
                throw new Bankruptcy(bestBid.getAgent());

            //remove the two crossing quotes
            final Quote bidQuote = bids.remove();
            final Quote askQuote = asks.remove();
            //reactions!
            bestBid.getAgent().reactToFilledBidQuote(bidQuote, bestAsk.getGood(), price, bestAsk.getAgent());
            bestAsk.getAgent().reactToFilledAskedQuote(askQuote, bestAsk.getGood(), price, bestBid.getAgent());

            //recursively make sure there are no more crossing quotes
            return true;
        }
        else
            return false;
    }

    @Override
    public void turnOff() {

    }
}
