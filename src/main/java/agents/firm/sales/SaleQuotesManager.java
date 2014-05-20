/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.sales;

import financial.utilities.Quote;
import goods.Good;
import model.utilities.Deactivatable;

import java.util.Collection;

/**
 * The part of the sales department that accounts for checking which/how many goods are to be sold, what quotes have been placed
 * and which haven
 * Created by carrknight on 5/12/14.
 */
public interface SaleQuotesManager extends Deactivatable {


    /**
     * Basically asks whether or not the salesDepartment has anything to sell currently.
     * @return
     */
    public boolean hasAnythingToSell();


    public Collection<Good> listOfGoodsToSell();

    public Quote getCheapestQuote();

    public void recordThisGoodAsSellable(Good g);

    public int getAnyPriceQuoted();

    public int numberOfGoodsToSell();

    public void recordQuoteAssociatedWithThisGood(Good g, Quote q);


    public boolean isThisGoodBeingSold(Good g);


    public Quote stopSellingThisGoodAndReturnItsAssociatedQuote(Good g);

    public Quote getQuoteAssociatedWithThisGood(Good g);


    public Collection<Quote> getAllQuotes();


    public void forgetTheQuoteAssociatedWithThisGood(Good g);

    public void removeAllQuotes();

    public int numberOfQuotesPlaced();

    public Good peekFirstGoodAvailable();

    public void recordTheseGoodsAsSellable(int amountNewGoods);


    public void thisQuoteHasBeenFilledSoRemoveItWithItsAssociatedGood(Quote q);
}
