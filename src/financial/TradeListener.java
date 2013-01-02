package financial;

import agents.EconomicAgent;
import financial.utilities.Quote;
import goods.Good;

/**
 * <h4>Description</h4>
 * <p/> Transaction listeners are object listening to the market and reacting when a trade occurs.
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
public interface TradeListener {

    /**
     * Tell the listener a trade has been carried out
     * @param buyer the buyer of this trade
     * @param seller the seller of this trade
     * @param goodExchanged the good that has been traded
     * @param price the price of the trade
     */
    public void tradeEvent(EconomicAgent buyer, EconomicAgent seller, Good goodExchanged, long price, Quote sellerQuote, Quote buyerQuote);

}
