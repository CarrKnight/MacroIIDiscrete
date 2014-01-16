/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package financial.market;

import financial.utilities.Quote;
import model.MacroII;
import model.utilities.Deactivatable;

import java.util.Queue;

/**
 * <h4>Description</h4>
 * <p/> This is called to match orders and let agents trade in an order book market
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
public interface OrderHandler extends Deactivatable
{

    /**
     * start the handler, if needed
     */
    public void start(MacroII model,Queue<Quote> asks, Queue<Quote> bids, OrderBookMarket market);

    /**
     * tell the handler a new quote arrived.
     *
     */
    public void reactToNewQuote(Queue<Quote> asks, Queue<Quote> bids, OrderBookMarket market);


}
