package financial;

import agents.EconomicAgent;
import financial.utilities.Quote;
import goods.GoodType;

import java.util.Iterator;

/**
 * <h4>Description</h4>
 * <p/> A very simple extension to the order book market: in this class the best prices are not visible. Quotes are still cleared through order book dynamics though
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-14
 * @see
 */
public class OrderBookBlindMarket extends OrderBookMarket {
    public OrderBookBlindMarket(GoodType t) {
        super(t);
    }


    /**
     * Best bid and asks are NOT visible.
     */
    @Override
    public boolean isBestBuyPriceVisible() {
        return false;
    }

    /**
     * Best bid and asks are NOT visible.
     */
    @Override
    public boolean isBestSalePriceVisible() {
        return false;
    }

    /**
     * Throws illegal access exception
     */
    @Override
    public long getBestSellPrice() throws IllegalAccessException {
        throw new IllegalAccessException("not visible");
    }

    /**
     * Asks the market to return the owner of the best ask price in the market
     *
     * @return the best seller or NULL if there is none
     * @throws IllegalAccessException thrown by markets that do not allow such information.
     */
    @Override
    public EconomicAgent getBestSeller() throws IllegalAccessException {
        throw new IllegalAccessException("not visible");
    }


    /**
     * Asks the market to return the best (highest) offer for buying a good at the market
     *
     * @return the best price or -1 if there are none
     * @throws IllegalAccessException thrown by markets that do not allow such information.
     */
    @Override
    public long getBestBuyPrice() throws IllegalAccessException {
        throw new IllegalAccessException("not visible");
    }

    /**
     * Asks the market to return the owner of the best offer in the market
     *
     * @return the best buyer or NULL if there is none
     * @throws IllegalAccessException thrown by markets that do not allow such information.
     */
    @Override
    public EconomicAgent getBestBuyer() throws IllegalAccessException {
        throw new IllegalAccessException("not visible");
    }

    /**
     * Can I get an iterator to cycle through all the quotes?
     *
     * @return true if it's possible
     */
    @Override
    public boolean areAllQuotesVisibile() {
        return false;
    }

    /**
     * Get an iterator to cycle through all the bids
     *
     * @return the iterator
     */
    @Override
    public Iterator<Quote> getIteratorForBids() throws IllegalAccessException{
        throw new IllegalAccessException("A blind order book can't be traversed");
    }

    /**
     * Get an iterator to cycle through all the bids
     *
     * @return the iterator
     */
    @Override
    public Iterator<Quote> getIteratorForAsks() throws IllegalAccessException {
        throw new IllegalAccessException("A blind order book can't be traversed");
    }
}
