/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package financial;

import agents.EconomicAgent;
import financial.market.Market;
import financial.utilities.PurchaseResult;
import financial.utilities.Quote;
import goods.Good;


/**
 * <h4>Description</h4>
 * <p/> This is a simple strategy pattern interface that dictates what happens in a market when ask-bid cross or two agents meet.
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-20
 * @see
 */
public interface TradePolicy {

    /**
     * The buyer and the seller can exchange gifts and hostages
     * @param buyer the buyer
     * @param seller the seller
     * @param good the good being exchanged
     * @param price the price established by the market
     * @param buyerQuote the original quote by the buyer (might be needed for some reason)
     * @param sellerQuote the original quote by the seller
     * @param market in which the trade is recorded/happening
     * @return a PurchaseResult describing the results
     */
    public PurchaseResult trade( EconomicAgent buyer, EconomicAgent seller, Good good, int price,
                                 Quote buyerQuote, Quote sellerQuote, Market market);
}
