/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package financial.utilities;

import agents.EconomicAgent;
import com.sun.istack.internal.Nullable;

import javax.annotation.Nonnull;

/**
 * This is a struct class that contains the information of the last trade. It returns:
 * <ul>
 *     <li>A PurchaseResult enum explaining what the trade result was</li>
 *     <li>The match with which the trade occurred</li>
 *     <li>The price</li>
 * </ul>
 * User: Ernesto
 * Date: 7/7/12
 * Time: 4:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class TradeResult {

    @Nonnull
    private final PurchaseResult result;

    @Nullable
    private final EconomicAgent match;

    @Nullable
    private final long price;


    /**
     * Return a trade result involving failure ( no need to specify the match)
     * @param result the result of trading.
     */
    public TradeResult(@Nonnull PurchaseResult result) {
        assert result!=PurchaseResult.SUCCESS;
        this.result = result;
        match = null;
        price = -1;
    }

    public TradeResult(PurchaseResult result,@Nonnull EconomicAgent match,@Nonnull long price) {
        if(price < 0)
            throw new IllegalArgumentException("Negative price can't be a result!");
        assert result != PurchaseResult.NO_MATCH_AVAILABLE; //if no match was available, what is the match you are passing to the constructor?

        this.result = result;
        this.match = match;
        this.price = price;
    }

    public PurchaseResult getResult() {
        return result;
    }

    public EconomicAgent getMatch() {
        return match;
    }

    public long getPrice() {
        return price;
    }
}
