/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package financial.utilities.priceLooker;

import financial.market.Market;

import javax.annotation.Nonnull;

/**
 * <h4>Description</h4>
 * <p/> This simply reports the price of a market.
 * <p/> If best bids and asks are visibile, it reports the midprice, otherwise it reports the closing price
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-02-25
 * @see
 */
public class PriceLookupOnMarket implements PriceLookup {

    /**
     * The market to look at
     */
    final private Market market;

    /**
     * does the market allow us to view best offers?
     */
    final private boolean isVisible;

    public PriceLookupOnMarket(@Nonnull Market market) {
        this.market = market;
        isVisible = market.isBestBuyPriceVisible() && market.isBestSalePriceVisible(); //both have to be visible
    }

    /**
     * Get the price you are supposed to look up to.
     *
     * @return The price or -1 if there is no price
     */
    @Override
    public long getPrice() {

        if(!isVisible)
            return market.getLastPrice();
        else
        {
            try {
                long bestBuyPrice = market.getBestBuyPrice();
                long bestAskPrice = market.getBestSellPrice();

                if(bestBuyPrice > -1 && bestAskPrice > -1)   //both exist
                    return Math.round((bestBuyPrice+bestAskPrice)/2f); //rounded average
                else
                if(bestAskPrice > -1){
                    assert bestBuyPrice ==-1;
                    return bestAskPrice; //only best ask is available

                }
                else
                if(bestBuyPrice > -1)
                {
                    assert bestAskPrice ==-1;
                    return bestBuyPrice;
                }
                else
                {
                    assert bestAskPrice ==-1; assert bestBuyPrice ==-1;
                    return market.getLastPrice(); //if there is nothing ,return last closing price
                }



            } catch (IllegalAccessException e) {
                throw new RuntimeException("I expected bestBuy/bestAsk to be visible. They weren't");
            }

        }

    }
}
