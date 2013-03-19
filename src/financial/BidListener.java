/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package financial;

import agents.EconomicAgent;
import financial.utilities.Quote;

import javax.annotation.Nonnull;

/**
 * <h4>Description</h4>
 * <p/> This a simple interface denoting agents that are to be notified whenever a new bid is put or removed from the market
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-27
 * @see
 */
public interface BidListener {


    /**
     * Tell the listener a new bid has been placed into the market
     * @param buyer the agent placing the bid
     * @param price the price of the good
     * @param bestAsk the best ask when the bid was made
     */
    public void newBidEvent(@Nonnull final EconomicAgent buyer, final long price, final Quote bestAsk);


    /**
     * Tell the listener a new bid has been placed into the market
     * @param buyer the agent placing the bid
     * @param quote the removed quote
     */
    public void removedBidEvent(@Nonnull final EconomicAgent buyer, @Nonnull final Quote quote);

}
