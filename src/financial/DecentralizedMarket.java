package financial;

import agents.EconomicAgent;
import agents.firm.Department;
import financial.utilities.ActionsAllowed;
import financial.utilities.Quote;
import goods.Good;
import goods.GoodType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;

/**
 * <h4>Description</h4>
 * <p/> A market with no structure, where only the registry is provided.
 * * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-09-20
 * @see
 */
public class DecentralizedMarket extends Market {
    /**
     * What is the seller role in this market?
     *
     * @return the actions allowed to the seller in this market
     */
    @Nonnull
    @Override
    public ActionsAllowed getSellerRole() {
        return ActionsAllowed.SEARCH;
    }

    /**
     * What is the buyer role in this market?
     *
     * @return the actions allowed to the buyer in this market
     */
    @Override
    public ActionsAllowed getBuyerRole() {
        return ActionsAllowed.SEARCH;
    }

    /**
     * Decentralized markets take no quotes
     */
    @Nonnull
    @Override
    public Quote submitSellQuote(@Nonnull EconomicAgent seller, long price, @Nonnull Good good) {
        throw new IllegalStateException("Decentralized markets take no quotes!");
    }

    /**
     * Decentralized markets take no quotes
     */
    @Nonnull
    @Override
    public Quote submitSellQuote(@Nonnull EconomicAgent seller, long price, @Nonnull Good good, @Nullable Department department) {
        throw new IllegalStateException("Decentralized markets take no quotes!");
    }

    /**
     * Decentralized markets take no quotes
     */
    @Override
    public void removeSellQuote(Quote q) {
        throw new IllegalStateException("Decentralized markets take no quotes!");
    }

    /**
     * Decentralized markets take no quotes
     */
    @Nonnull
    @Override
    public Quote submitBuyQuote(@Nonnull EconomicAgent buyer, long price, @Nullable Department department) {
        throw new IllegalStateException("Decentralized markets take no quotes!");
    }

    /**
     * Decentralized markets take no quotes
     */
    @Nonnull
    @Override
    public Quote submitBuyQuote(@Nonnull EconomicAgent buyer, long price) {
        throw new IllegalStateException("Decentralized markets take no quotes!");
    }

    /**
     * Decentralized markets take no quotes
     */
    @Override
    public void removeBuyQuote(Quote q) {
        throw new IllegalStateException("Decentralized markets take no quotes!");
    }

    /**
     * asks the market if users are allowed to see the best price for a good on sale
     */
    @Override
    public boolean isBestSalePriceVisible() {
        return false;
    }

    /**
     * Asks the market to return the best (lowest) price for a good on sale at the market
     *
     * @return the best price or -1 if there are none
     * @throws IllegalAccessException thrown by markets that do not allow such information.
     */
    @Override
    public long getBestSellPrice() throws IllegalAccessException {
        throw new IllegalAccessException("Not visible! Should have checked!");
    }

    /**
     * Asks the market to return the owner of the best ask price in the market
     *
     * @return the best price or -1 if there are none
     * @throws IllegalAccessException thrown by markets that do not allow such information.
     */
    @Override
    public EconomicAgent getBestSeller() throws IllegalAccessException {
        throw new IllegalAccessException("Not visible! Should have checked!");
    }

    /**
     * asks the market if users are allowed to see the best offer to buy a good
     */
    @Override
    public boolean isBestBuyPriceVisible() {
        return false;
    }

    /**
     * Asks the market to return the best (highest) offer for buying a good at the market
     *
     * @return the best price or -1 if there are none
     * @throws IllegalAccessException thrown by markets that do not allow such information.
     */
    @Override
    public long getBestBuyPrice() throws IllegalAccessException {
        throw new IllegalAccessException("Not visible! Should have checked!");
    }

    /**
     * Asks the market to return the owner of the best offer in the market
     *
     * @return the best buyer or NULL if there is none
     * @throws IllegalAccessException thrown by markets that do not allow such information.
     */
    @Override
    public EconomicAgent getBestBuyer() throws IllegalAccessException {
        throw new IllegalAccessException("Not visible! Should have checked!");
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
     * @throws IllegalAccessException if not all the quotes aren't visible
     */
    @Override
    public Iterator<Quote> getIteratorForBids() throws IllegalAccessException {
        throw new IllegalAccessException("Not visible! Should have checked!");
    }

    /**
     * Get an iterator to cycle through all the bids
     *
     * @return the iterator
     * @throws IllegalAccessException if not all the quotes aren't visible
     */
    @Override
    public Iterator<Quote> getIteratorForAsks() throws IllegalAccessException {
        throw new IllegalAccessException("Not visible! Should have checked!");
    }

    public DecentralizedMarket(GoodType goodType) {
        super(goodType);
    }
}
