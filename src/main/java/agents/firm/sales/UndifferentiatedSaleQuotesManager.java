/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.sales;

import com.google.common.base.Preconditions;
import financial.utilities.Quote;
import goods.Good;
import goods.UndifferentiatedGoodType;

import java.util.*;

/**
 * A sale manager that just has a counter for how many goods to sell and a list of quotes made.
 * Created by carrknight on 5/12/14.
 */
public class UndifferentiatedSaleQuotesManager implements SaleQuotesManager {

    private int goodsToSell = 0;

    final private Queue<Quote> quotes;

    final private UndifferentiatedGoodType type;

    public UndifferentiatedSaleQuotesManager(UndifferentiatedGoodType type) {
        quotes = new LinkedList<>();
        this.type = type;
    }

    @Override
    public boolean hasAnythingToSell() {
        return  goodsToSell >0;
    }

    /**
     * this actually will return a set made up of only the singleton
     * @return
     */
    @Override
    public Collection<Good> listOfGoodsToSell() {
        ArrayList<Good> toReturn = new ArrayList<>(1);
        toReturn.add(Good.getInstanceOfUndifferentiatedGood(type));
        return toReturn;
    }

    @Override
    public Quote getCheapestQuote() {
        return quotes.peek();
    }

    @Override
    public void recordThisGoodAsSellable(Good g) {
        goodsToSell++;
    }

    @Override
    public void recordTheseGoodsAsSellable(int amountNewGoods) {
        goodsToSell+=amountNewGoods;
    }

    @Override
    public int getAnyPriceQuoted() {
        return quotes.peek().getPriceQuoted();


    }

    @Override
    public int numberOfGoodsToSell() {
        return goodsToSell;
    }

    @Override
    public void recordQuoteAssociatedWithThisGood(Good g, Quote q) {
        quotes.add(q);
        Preconditions.checkState(goodsToSell >= 0);
        Preconditions.checkState(quotes.size() <= goodsToSell, "there are more quotes than goods to sell");
    }

    /**
     * doesn't check the specifics, just returns true if it has something to sell
     * @param g
     * @return
     */
    @Override
    public boolean isThisGoodBeingSold(Good g) {

        return hasAnythingToSell();
    }

    @Override
    public Quote stopSellingThisGoodAndReturnItsAssociatedQuote(Good g) {
        Quote q = quotes.poll();
        if(goodsToSell>0)
            goodsToSell--;
        Preconditions.checkState(goodsToSell >= 0);
        Preconditions.checkState(quotes.size() <= goodsToSell, "there are more quotes than goods to sell");
        return q;

    }

    @Override
    public Quote getQuoteAssociatedWithThisGood(Good g) {

        return quotes.peek();

    }

    @Override
    public Collection<Quote> getAllQuotes() {
        return quotes;
    }

    @Override
    public void forgetTheQuoteAssociatedWithThisGood(Good g) {
        quotes.poll(); //remove one at random
        Preconditions.checkState(goodsToSell >= 0);
        Preconditions.checkState(quotes.size() <= goodsToSell, "there are more quotes than goods to sell");
    }

    @Override
    public void removeAllQuotes() {
        quotes.clear();
        goodsToSell = 0;
    }

    @Override
    public int numberOfQuotesPlaced() {
        return quotes.size();
    }

    @Override
    public Good peekFirstGoodAvailable() {
        return goodsToSell > 0 ? Good.getInstanceOfUndifferentiatedGood(type) : null;


    }

    @Override
    public void thisQuoteHasBeenFilledSoRemoveItWithItsAssociatedGood(Quote q) {
        Preconditions.checkArgument(q != null);
        Preconditions.checkArgument(q.getGood() != null);
        final boolean removedCorrectly = quotes.remove(q);
        assert removedCorrectly;

        goodsToSell--;
        Preconditions.checkState(goodsToSell >= 0);
        Preconditions.checkState(quotes.size() <= goodsToSell, "there are more quotes than goods to sell");

    }


    @Override
    public void turnOff() {
        quotes.clear();
    }
}
