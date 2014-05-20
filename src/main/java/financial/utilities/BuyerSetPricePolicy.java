/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package financial.utilities;

/**
 * <h4>Description</h4>
 * <p/> With this policy the price of trade is always the buyer quoted price
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-10-09
 * @see
 */
public class BuyerSetPricePolicy implements PricePolicy {
    @Override
    public int price(int sellerPrice, int buyerPrice) {
        assert buyerPrice>=sellerPrice;
        return buyerPrice;
    }
}
