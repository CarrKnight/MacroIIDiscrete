/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package financial.utilities;

/**
 * This is an ENUM that explains the result of purchaseGoods() call for retailers and consumers; useful for debug
 */
public enum PurchaseResult {

    /**
     * this is the result when we just have no money
     */
    BUYER_HAS_NO_MONEY(),

    /**
     * We found something to buy, but it's more than the cash we have!
     */
    PRICE_REJECTED,

    /**
     * We couldn't find anybody that would sell to us!
     */
    NO_MATCH_AVAILABLE,

    /**
     * We bought something!
     */
    SUCCESS,

    /**
     * Stockout, the seller has no goods
     */
    STOCKOUT,

    /**
     * this is how a new variable should be initialized
     */
    NEVER_USED;

    /**
     * Just a little variable attached to each purchase result telling what's the price of trade. I expect this to be used only if the state is success
     */
    private long priceTrade = -1;

    /**
     * Just a little variable attached to each purchase result telling what's the price of trade. I expect this to be used only if the state is success
     */
    public long getPriceTrade() {
        return priceTrade;
    }

    /**
     * Just a little variable attached to each purchase result telling what's the price of trade. I expect this to be used only if the state is success
     */
    public void setPriceTrade(long priceTrade) {
        this.priceTrade = priceTrade;
    }
}
