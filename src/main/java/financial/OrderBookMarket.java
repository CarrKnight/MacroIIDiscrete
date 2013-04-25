/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package financial;

import agents.EconomicAgent;
import agents.firm.Department;
import com.google.common.base.Preconditions;
import financial.utilities.ActionsAllowed;
import financial.utilities.HistogramDecoratedPriorityBook;
import financial.utilities.PurchaseResult;
import financial.utilities.Quote;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import sim.portrayal.Inspector;
import sim.portrayal.inspector.TabbedInspector;
import sim.util.media.chart.HistogramGenerator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * A simple order book market. Everybody can only quote.
 * Whenever a new quote arrives, the order book checks for crossing quotes and make them trade.
 * User: carrknight
 * Date: 7/15/12
 * Time: 5:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class OrderBookMarket extends Market {


    private final Queue<Quote> asks;

    private final Queue<Quote> bids;

    /**
     * This is the histogram that will be used by the inspector if the GUI is on
     */
    private final HistogramGenerator histogramGenerator;


    public OrderBookMarket(GoodType t) {
        super(t);

        //create the two books as priority queues, we might decorate them if there is GUI

        //asks put the first element of the queue as the one with lowest price (best ask)
        PriorityQueue<Quote> asks = new PriorityQueue<>(10,new Comparator<Quote>() {
            @Override
            public int compare(Quote o1, Quote o2) {
                return Long.compare(o1.getPriceQuoted(),o2.getPriceQuoted());


            }
        });

        //bids put the first element of the queue as the one with the highest offer (best bid)
        PriorityQueue<Quote> bids = new PriorityQueue<>(10,new Comparator<Quote>() {
            @Override
            public int compare(Quote o1, Quote o2) {
                return -Long.compare(o1.getPriceQuoted(),o2.getPriceQuoted());


            }
        });

        //if the gui is on
        if(MacroII.hasGUI())
        {
            histogramGenerator = new HistogramGenerator();
            //add the 2 series
            histogramGenerator.addSeries(null,50,"bids",null);
            histogramGenerator.addSeries(null,50,"asks",null);
            //decorate the asks and bids
            this.bids = new HistogramDecoratedPriorityBook(bids,histogramGenerator,0,"Bids");
            this.asks = new HistogramDecoratedPriorityBook(asks,histogramGenerator,1,"Asks");

            buildInspector();

        }
        else{
            histogramGenerator = null;
            //do not decorate the bids and asks
            this.asks = asks;
            this.bids = bids;
        }





    }

    /**
     * Order book with limit orders only!
     */
    @Override
    public ActionsAllowed getBuyerRole() {

        return ActionsAllowed.QUOTE;

    }

    /**
     * Order book with limit orders only!
     */
    @Nonnull
    @Override
    public ActionsAllowed getSellerRole() {

        return ActionsAllowed.QUOTE;
    }

    /**
     * Submit a sell quote on a specific good
     *
     * @param seller the agent making the sale
     * @param price  the price at/above which the agent is willing to sell
     * @param good   the good to sell
     * @return the quote made; returns a null quote if the quote is immediately filled.
     */
    @Nonnull
    @Override
    public Quote submitSellQuote(@Nonnull EconomicAgent seller, long price, @Nonnull Good good, @Nullable Department department) {

        assert getSellers().contains(seller);  //you should be registered if you are here
        Preconditions.checkArgument(price>=0);

        if(MacroII.SAFE_MODE) //double check the good isn't already on sale
            for(Quote x : asks){
                assert x.getGood() != good; //make sure it wasn't put in already
            }

        Quote q = Quote.newSellerQuote(seller,price,good);
        if(department != null)
            q.setOriginator(department);

        asks.add(q); //addSalesDepartmentListener it to the asks

        //tell the GUI!
        if(MacroII.hasGUI())
        {
          getRecords().event(seller,MarketEvents.SUBMIT_SELL_QUOTE, seller.getModel().getCurrentSimulationTimeInMillis()
                  ,"price: " + q.getPriceQuoted());
        }


        matchQuotes(); //check for trades

        if(asks.contains(q)) //if it's still in
            return q; //return the quote to the seller
        else //it crossed and was immediately removed!
            return Quote.emptySellQuote(good); //if it was crossed, return a null quote

    }

    /**
     * Submit a sell quote on a specific good
     *
     * @param seller the agent making the sale
     * @param price  the price at/above which the agent is willing to sell
     * @param good   the good to sell
     * @return the quote made
     */
    @Nonnull
    @Override
    public Quote submitSellQuote(@Nonnull EconomicAgent seller, long price, @Nonnull Good good) {
        return submitSellQuote(seller,price,good,null);
    }

    /**
     * If the seller changes its mind and wants to remove its sell quote, call this
     *
     * @param q quote to cancel
     */
    @Override
    public void removeSellQuote(Quote q) {

        boolean removedSuccessfully = asks.remove(q); //remove it from the asks!
        if(!removedSuccessfully)
            throw new IllegalArgumentException("Removed a quote we didn't have. Error");


        //tell the GUI
        if(MacroII.hasGUI()){
            getRecords().event(q.getAgent(),MarketEvents.REMOVE_SELL_QUOTE,
                    q.getAgent().getModel().getCurrentSimulationTimeInMillis()
                    ,"price: " + q.getPriceQuoted());
        }

    }



    /**
     * Submit a buy quote
     *
     * @param buyer the agent trying to buy
     * @param price the price at/below which the agent is willing to buy
     * @return quote made
     */
    @Nonnull
    @Override
    public Quote submitBuyQuote(@Nonnull EconomicAgent buyer, long price, @Nullable Department department) {
        assert getBuyers().contains(buyer) : buyer + " ---- " + getBuyers() + " ---- " + this.getGoodType();  //you should be registered if you are here

        Quote q = Quote.newBuyerQuote(buyer, price, goodType);
        if(department != null)
            q.setOriginator(department);

        bids.add(q); //addSalesDepartmentListener it to the asks


        //notify the listeners (if the order book is visible)
        Quote bestAsk = asks.peek();
        if(isBestBuyPriceVisible())
            for(BidListener listener : bidListeners)
                listener.newBidEvent(buyer,price,bestAsk);


        //tell the GUI
        if(MacroII.hasGUI())
        {
            getRecords().event(buyer,MarketEvents.SUBMIT_BUY_QUOTE,
                    buyer.getModel().getCurrentSimulationTimeInMillis()
                    ,"price: " + q.getPriceQuoted());
        }


        matchQuotes(); //check for trades


        if(bids.contains(q)) //if it's still in
            return q; //return the quote to the seller
        else //it crossed and was immediately removed!
            return Quote.emptyBidQuote(goodType); //if it was crossed, return a null quote
    }





    /**
     * Submit a buy quote
     *
     * @param buyer the agent trying to buy
     * @param price the price at/below which the agent is willing to buy
     * @return quote made
     */
    @Nonnull
    @Override
    public Quote submitBuyQuote(@Nonnull EconomicAgent buyer, long price) {
        return submitBuyQuote(buyer,price,null);
    }

    /**
     * If the buyer changes its mind and wants to remove its purchase quote, call this
     *
     * @param q quote to cancel
     */
    @Override
    public void removeBuyQuote(Quote q) {


        boolean removedSuccessfully = bids.remove(q); //remove it from the asks!
        if(!removedSuccessfully)
            throw new IllegalArgumentException("Removed a quote we didn't have. Error");

        //notify the listeners (if the order book is visible)
        if(isBestBuyPriceVisible())
            for(BidListener listener : bidListeners)
                listener.removedBidEvent(q.getAgent(),q);


        //tell the GUI
        if(MacroII.hasGUI()){
            getRecords().event(q.getAgent(),MarketEvents.REMOVE_BUY_QUOTE,
                    q.getAgent().getModel().getCurrentSimulationTimeInMillis()
                    ,"price: " + q.getPriceQuoted());
        }
    }


    /**
     * Best bid and asks are visible.
     */
    @Override
    public boolean isBestSalePriceVisible() {
        return true;
    }

    /**
     * Asks the market to return the best (lowest) price for a good on sale at the market
     *
     * @return the best price or -1 if there are none
     */
    @Override
    public long getBestSellPrice() throws IllegalAccessException {
        if(!isBestSalePriceVisible())
            throw new IllegalAccessException();

        if(asks.isEmpty())   //if the ask is empty returns -1
            return -1;
        else
            return asks.peek().getPriceQuoted(); //returns best ask
    }

    /**
     * Asks the market to return the owner of the best ask price in the market
     *
     * @return the best seller or NULL if there is none
     * @throws IllegalAccessException thrown by markets that do not allow such information.
     */
    @Override
    public EconomicAgent getBestSeller() throws IllegalAccessException {
        if(!isBestSalePriceVisible())          //don't show it if it's illegal
            throw new IllegalAccessException();

        if(asks.isEmpty())   //if the ask is empty returns -1
            return null;
        else
            return asks.peek().getAgent(); //returns best ask
    }

    /**
     * Best bid and asks are visible.
     */
    @Override
    public boolean isBestBuyPriceVisible() {
        return true;
    }

    /**
     * Asks the market to return the best (highest) offer for buying a good at the market
     *
     * @return the best price or -1 if there are none
     * @throws IllegalAccessException thrown by markets that do not allow such information.
     */
    @Override
    public long getBestBuyPrice() throws IllegalAccessException{
        if(bids.isEmpty())   //if the ask is empty returns -1
            return -1;
        else
            return bids.peek().getPriceQuoted(); //returns best ask
    }

    /**
     * Asks the market to return the owner of the best offer in the market
     *
     * @return the best buyer or NULL if there is none
     * @throws IllegalAccessException thrown by markets that do not allow such information.
     */
    @Override
    public EconomicAgent getBestBuyer() throws IllegalAccessException{

        if(!isBestBuyPriceVisible())          //don't show it if it's illegal
            throw new IllegalAccessException();

        if(bids.isEmpty())   //if the ask is empty returns -1
            return null;
        else
            return bids.peek().getAgent(); //returns best ask

    }

    /**
     * Checks if there are crossing quotes, if so make trade happens!
     */
    private void matchQuotes(){
        if(bids.isEmpty() || asks.isEmpty())
            return; //if any of the two is empty, nothing to match

        Quote bestBid = bids.peek(); //check best bid
        Quote bestAsk = asks.peek(); //check best ask

        //if best bid and best ask cross
        if(bestBid.getPriceQuoted() >= bestAsk.getPriceQuoted())
        {
            //price is somewhere in the middle
            long price = pricePolicy.price(bestAsk.getPriceQuoted(),bestBid.getPriceQuoted());

            //sanity check
            assert price >= bestAsk.getPriceQuoted();
            assert price <= bestBid.getPriceQuoted();
            assert bestAsk.getGood().getType().equals(bestBid.getType()); //they should be buying the same stuff


            //TODO need to addSalesDepartmentListener a bankruptcy check that remove the bid, restarts the match quotes and notifies the bidder he's out of money!


            PurchaseResult result = trade(bestBid.getAgent(),bestAsk.getAgent(),bestAsk.getGood(),price,bestBid,bestAsk);  //TRADE!
            if(result == PurchaseResult.BUYER_HAS_NO_MONEY)
                throw new Bankruptcy(bestBid.getAgent());

            //remove the two crossing quotes
            bids.remove();
            asks.remove();
            //reactions!
            bestBid.getAgent().reactToFilledBidQuote(bestAsk.getGood(),price,bestAsk.getAgent());
            bestAsk.getAgent().reactToFilledAskedQuote(bestAsk.getGood(), price,bestBid.getAgent());

            //recursively make sure there are no more crossing quotes
            matchQuotes();
        }


        //

    }


    /**
     * Can I get an iterator to cycle through all the quotes?
     *
     * @return true if it's possible
     */
    @Override
    public boolean areAllQuotesVisibile() {
        return true;
    }

    /**
     * Get an iterator to cycle through all the bids
     *
     * @return the iterator
     */
    @Override
    public Iterator<Quote> getIteratorForBids() throws  IllegalAccessException{
        return  bids.iterator();
    }

    /**
     * Get an iterator to cycle through all the bids
     *
     * @return the iterator
     */
    @Override
    public Iterator<Quote> getIteratorForAsks() throws IllegalAccessException{
        return  asks.iterator();
    }



    /**
     * The order book adds the histogram viewer to the market inspector.
     */
    private void buildInspector()
    {
        assert MacroII.hasGUI();

        //
        TabbedInspector inspector = getMarketInspector();

        Inspector orderBookViewer = new Inspector() {
            @Override
            public void updateInspector() {
                histogramGenerator.update();
          //      this.repaint();
            }
        };

        orderBookViewer.setLayout(new BorderLayout());
        orderBookViewer.add(histogramGenerator.getChartPanel());
        inspector.addInspector(orderBookViewer,"Order Book View");

    }

    public boolean containsQuotesFromThisBuyer(EconomicAgent buyer)
    {
        for(Quote q: bids)
            if(q.getAgent().equals(buyer))
                return true;
        return false;
    }


}
