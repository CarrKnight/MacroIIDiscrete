/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.sales;

import com.google.common.base.Preconditions;
import financial.utilities.Quote;
import goods.Good;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The "original" sale quotes manager. It is simply a map goods to quote to which everything is delegated.
 * It really only works with differentiated goods since undifferentiated ones are backed by a singleton that would not work at all
 * with the underlying map structure
 * Created by carrknight on 5/12/14.
 */
public class DifferentiatedSaleQuotesManager implements SaleQuotesManager {

    /**
     * a map associating to each good to sell the quote submitted for it at a centralized market
     */
    private final Map<Good,Quote> goodsQuotedOnTheMarket;

    private int quotesCurrentlyPlaced = 0;

    public DifferentiatedSaleQuotesManager()
    {
        this.goodsQuotedOnTheMarket = new HashMap<>();
    }

    @Override
    public boolean hasAnythingToSell() {
        return !goodsQuotedOnTheMarket.isEmpty();

    }

    @Override
    public Collection<Good> listOfGoodsToSell() {
        return goodsQuotedOnTheMarket.keySet();
    }

    @Override
    public Quote getCheapestQuote() {
        final Optional<Quote> minimum = goodsQuotedOnTheMarket.values().stream().filter(quote -> quote != null).
                min((o1, o2) -> Integer.compare(o1.getPriceQuoted(), o2.getPriceQuoted()));
        return minimum.orElse(null);




    }

    @Override
    public void recordThisGoodAsSellable(Good g) {
        goodsQuotedOnTheMarket.put(g,null);
    }

    @Override
    public void recordTheseGoodsAsSellable(int amountNewGoods) {
        throw  new RuntimeException("Error in sale quotes manage, Differentiated Sale Quotes cannot deal with undifferentiated goods");

    }

    @Override
    public int getAnyPriceQuoted() {

        return goodsQuotedOnTheMarket.values().iterator().next().getPriceQuoted();

    }

    @Override
    public int numberOfGoodsToSell() {
        return goodsQuotedOnTheMarket.size();

    }

    @Override
    public void recordQuoteAssociatedWithThisGood(Good g, Quote q) {
        Preconditions.checkNotNull(g);
        Preconditions.checkNotNull(q);
        goodsQuotedOnTheMarket.put(g,q); //record the quote!
        quotesCurrentlyPlaced++;
    }

    @Override
    public boolean isThisGoodBeingSold(Good g) {
        return goodsQuotedOnTheMarket.containsKey(g);
    }

    @Override
    public Quote stopSellingThisGoodAndReturnItsAssociatedQuote(Good g) {


        Quote q = goodsQuotedOnTheMarket.remove(g);
        if(q != null)
            quotesCurrentlyPlaced--;
        assert quotesCurrentlyPlaced >=0;

        return q;
    }

    @Override
    public Quote getQuoteAssociatedWithThisGood(Good g) {
        return goodsQuotedOnTheMarket.get(g);
    }

    @Override
    public Collection<Quote> getAllQuotes() {
        return goodsQuotedOnTheMarket.values();
    }

    @Override
    public void forgetTheQuoteAssociatedWithThisGood(Good g) {
        final Quote oldQuote = goodsQuotedOnTheMarket.put(g, null);
        if(oldQuote != null)
            quotesCurrentlyPlaced--;
        assert quotesCurrentlyPlaced >=0;
    }

    @Override
    public void removeAllQuotes() {
        goodsQuotedOnTheMarket.clear();

    }

    @Override
    public int numberOfQuotesPlaced() {
        return quotesCurrentlyPlaced;
    }

    @Override
    public Good peekFirstGoodAvailable() {
        return goodsQuotedOnTheMarket.keySet().iterator().next();
    }

    @Override
    public void thisQuoteHasBeenFilledSoRemoveItWithItsAssociatedGood(Quote q) {
        Preconditions.checkArgument(q != null);
        Preconditions.checkArgument(q.getGood() != null);
//        assert q.getPriceQuoted() <0 || goodsQuotedOnTheMarket.get(q.getGood()).equals(q);
        stopSellingThisGoodAndReturnItsAssociatedQuote(q.getGood());
    }

    @Override
    public void turnOff() {
        goodsQuotedOnTheMarket.clear();
        quotesCurrentlyPlaced = 0;
    }
}
