/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package financial.market;

import agents.EconomicAgent;
import agents.firm.Department;
import com.google.common.base.Preconditions;
import financial.BidListener;
import financial.MarketEvents;
import financial.utilities.ActionsAllowed;
import financial.utilities.HistogramDecoratedPriorityBook;
import financial.utilities.Quote;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import sim.portrayal.Inspector;
import sim.portrayal.inspector.TabbedInspector;
import sim.util.media.chart.HistogramGenerator;

import java.awt.*;
import java.util.*;
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


    private Queue<Quote> asks;

    private Queue<Quote> bids;

    /**
     * This is the histogram that will be used by the inspector if the GUI is on
     */
    private HistogramGenerator histogramGenerator = null;

    /**
     * this is the object that tries to match bids and asks and allows for trades.
     */
    private OrderHandler orderHandler;


    public OrderBookMarket(GoodType t) {
        super(t);

        //create the two books as priority queues, we might decorate them if there is GUI

        //asks put the first element of the queue as the one with lowest price (best ask)
        this.asks = new PriorityQueue<>(10,new Comparator<Quote>() {
            @Override
            public int compare(Quote o1, Quote o2) {
                return Long.compare(o1.getPriceQuoted(),o2.getPriceQuoted());


            }
        });

        //bids put the first element of the queue as the one with the highest offer (best bid)
        this.bids = new PriorityQueue<>(10,new Comparator<Quote>() {
            @Override
            public int compare(Quote o1, Quote o2) {
                return -Long.compare(o1.getPriceQuoted(),o2.getPriceQuoted());


            }
        });

        //if the gui is on
        if(MacroII.hasGUI())
        {


            buildInspector();

        }
        else{
            assert histogramGenerator == null;
            //do not decorate the bids and asks
        }



        orderHandler = new EndOfPhaseOrderHandler(); //let everybody place quotes before you start crossing them. With this priorities don't really matter


    }


    /**
     * tells the market that the model is starting up, and it would be a good idea to start the data collector
     *
     * @param model  the MacroII model running the show
     */
    @Override
    public void start(MacroII model) {
        super.start(model);

        orderHandler.start(model,asks,bids,this);
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

    @Override
    public Quote submitSellQuote( EconomicAgent seller, long price,  Good good,  Department department) {

        assert getSellers().contains(seller);  //you should be registered if you are here
        if(MacroII.SAFE_MODE) //double check the good isn't already on sale
            Preconditions.checkState(seller.getModel().getCurrentPhase().equals(ActionOrder.TRADE));

        Preconditions.checkArgument(price>=0);

        if(MacroII.SAFE_MODE) //double check the good isn't already on sale
            for(Quote x : asks){
                assert x.getGood() != good; //make sure it wasn't put in already
            }

        Quote q = Quote.newSellerQuote(seller,price,good);
        if(department != null)
            q.setOriginator(department);

        asks.add(q); //addSalesDepartmentListener it to the asks

        //tell the log
        //todo logtodo



        orderHandler.reactToNewQuote(asks,bids,this);

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

    @Override
    public Quote submitSellQuote( EconomicAgent seller, long price,  Good good) {
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


        //tell the logs
        //todo logtodo


    }



    /**
     * Submit a buy quote
     *
     * @param buyer the agent trying to buy
     * @param price the price at/below which the agent is willing to buy
     * @return quote made
     */

    @Override
    public Quote submitBuyQuote( EconomicAgent buyer, long price,  Department department) {
        assert getBuyers().contains(buyer) : buyer + " ---- " + getBuyers() + " ---- " + this.getGoodType();  //you should be registered if you are here
        if(MacroII.SAFE_MODE) //double check the good isn't already on sale
            Preconditions.checkState(buyer.getModel().getCurrentPhase().equals(ActionOrder.TRADE));

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

            //todo logtodo




        orderHandler.reactToNewQuote(asks, bids, this);


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

    @Override
    public Quote submitBuyQuote( EconomicAgent buyer, long price) {
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
        notifyListenersAndGUIQuoteHasBeenRemoved(q);


    }


    /**
     * Cancel a list of buy quotes
     *
     * @param quotes quotes to cancel
     */
    @Override
    public void removeBuyQuotes( Collection<Quote> quotes) {

        Preconditions.checkArgument(!quotes.isEmpty());
        bids.removeAll(quotes);

        for(Quote q : quotes)
            notifyListenersAndGUIQuoteHasBeenRemoved(q);


    }


    @Override
    public void removeSellQuotes(Collection<Quote> quotes) {

        Preconditions.checkArgument(!quotes.isEmpty());
        asks.removeAll(quotes);

        for(Quote q : quotes)
            notifyListenersAndGUIQuoteHasBeenRemoved(q);

    }

    private void notifyListenersAndGUIQuoteHasBeenRemoved(Quote q) {
        //notify the listeners (if the order book is visible)
        if(isBestBuyPriceVisible())
            for(BidListener listener : bidListeners)
                listener.removedBidEvent(q.getAgent(),q);


        //tell the GUI
        //todo logtodo

    }


    /**
     * Remove all these quotes by the buyer
     *
     *
     * @param buyer the buyer whose quotes we want to clear
     * @return the set of quotes removed
     */
    @Override
    public Collection<Quote> removeAllBuyQuoteByBuyer(EconomicAgent buyer) {
        //create the set of buy quotes  to remove
        Set<Quote> buyQuotesToRemove = new HashSet<>();
        for(Quote q : bids)
        {
            if(q.getAgent().equals(buyer))
                buyQuotesToRemove.add(q);
        }
        if(buyQuotesToRemove.isEmpty()) //nothing to remove!
            return buyQuotesToRemove;

        //non empty!
        boolean b = bids.removeAll(buyQuotesToRemove);
        assert b;

        //now tell the listeners
        for(Quote q : buyQuotesToRemove)
            notifyListenersAndGUIQuoteHasBeenRemoved(q);
        return buyQuotesToRemove;

    }


    /**
     * Remove all these quotes by the seller
     *
     * @param seller the buyer whose quotes we want to clear
     * @return the set of quotes removed
     */
    @Override
    public Collection<Quote> removeAllSellQuoteBySeller(EconomicAgent seller) {
        //create the set of buy quotes  to remove
        Set<Quote> askQuotes = new HashSet<>();
        for(Quote q : asks)
        {
            if(q.getAgent().equals(seller))
                askQuotes.add(q);
        }
        if(askQuotes.isEmpty()) //nothing to remove!
            return askQuotes;

        //non empty!
        boolean b = asks.removeAll(askQuotes);
        assert b;

        //now tell the listeners
        for(Quote q : askQuotes)
            notifyListenersAndGUIQuoteHasBeenRemoved(q);
        return askQuotes;

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
    protected TabbedInspector  buildInspector()
    {
        assert MacroII.hasGUI();
        TabbedInspector inspector = super.buildInspector();

        histogramGenerator = new HistogramGenerator();
        //add the 2 series
        histogramGenerator.addSeries(null,50,"bids",null);
        histogramGenerator.addSeries(null,50,"asks",null);
        //decorate the asks and bids
        this.bids = new HistogramDecoratedPriorityBook((PriorityQueue)bids,histogramGenerator,0,"Bids");
        this.asks = new HistogramDecoratedPriorityBook((PriorityQueue)asks,histogramGenerator,1,"Asks");


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

        return inspector;

    }

    public boolean containsQuotesFromThisBuyer(EconomicAgent buyer)
    {
        for(Quote q: bids)
            if(q.getAgent().equals(buyer))
                return true;
        return false;
    }

    /**
     * Returns how many asks are currently in the market
     */
    public int numberOfAsks() {
        return asks.size();
    }


    /**
     * Returns how many bids are currently in the market
     */
    public int numberOfBids() {
        return bids.size();
    }


    @Override
    public void turnOff() {
        super.turnOff();
        orderHandler.turnOff();
    }


    public OrderHandler getOrderHandler() {
        return orderHandler;
    }

    /**
     * this setter requires a link to the model to start the new handler
     * @param orderHandler
     * @param model
     */
    public void setOrderHandler(OrderHandler orderHandler, MacroII model) {

        assert this.orderHandler != null;
        this.orderHandler.turnOff(); //turn off the old one!

        //set the new order handler
        this.orderHandler = orderHandler;
        //start it!
        if(model.hasStarted())
            this.orderHandler.start(model,asks,bids,this);


    }
}
