/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package financial.market;

import financial.utilities.Quote;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.scheduler.Priority;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.Queue;

/**
 * <h4>Description</h4>
 * <p/> This order handler schedules itself to clear trades at the end of each day's TRADE phase. Basically it waits for everybody to place trades before it starts clearing them
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
public class EndOfPhaseOrderHandler implements OrderHandler, Steppable {


    private boolean isActive = true;

    private Queue<Quote> asks;

    private Queue<Quote> bids;

    private OrderBookMarket market;


    /**
     * start the handler
     *
     * @param model the model needed to schedule to
     * @param asks the asks queue, needs to never change and be priority actually, it isn't now because i am lazy
     * @param bids needs to never change and be priority actually, it isn't now because i am lazy
     * @param market the order book market
     */
    @Override
    public void start(MacroII model, Queue<Quote> asks, Queue<Quote> bids, OrderBookMarket market)
    {
        this.asks = asks;
        this.bids = bids;
        this.market = market;

        model.scheduleSoon(ActionOrder.TRADE,this,Priority.FINAL);


    }

    /**
     * ignored
     */
    @Override
    public void reactToNewQuote(Queue<Quote> asks, Queue<Quote> bids, OrderBookMarket market) {
    }

    @Override
    public void turnOff() {
        isActive=false;
    }


    @Override
    public void step(SimState simState) {

        if(!isActive)
            return;

        MacroII model = (MacroII)simState;

        //try to clear the market!
        boolean anyTradeOccur = ImmediateOrderHandler.matchQuotes(asks,bids,market);
        //if any trade occurred, recursively step again!

        if(anyTradeOccur)
            model.scheduleSoon(ActionOrder.TRADE,this, Priority.FINAL);
        else
        //otherwise start over tomorrow!
            model.scheduleTomorrow(ActionOrder.TRADE,this,Priority.FINAL);


    }
}


