/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.pricing.decorators;

import agents.firm.purchases.pricing.BidPricingStrategy;
import financial.market.Market;
import goods.Good;
import goods.GoodType;
import model.utilities.NonDrawable;

/**
 * <h4>Description</h4>
 * <p/> if there is best asks visible cheaper than the price about to offer, then return the best ask instead.
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-12-26
 * @see
 */
@NonDrawable
public class LookAtTheMarketBidPricingDecorator extends BidPricingDecorator {

    final Market market;

    public LookAtTheMarketBidPricingDecorator(BidPricingStrategy toDecorate, Market buyingMarket) {
        super(toDecorate);
        this.market = buyingMarket;
    }

    /**
     * Answer the purchase strategy question: how much am I willing to pay for a good of this type?
     *
     * @param type the type of good you want to buy
     * @return the maximum price I am willing to pay for this good
     */
    @Override
    public int maxPrice(GoodType type) {
        int askPrice = Integer.MAX_VALUE;
        try{
            if(market.isBestSalePriceVisible())
            {
                askPrice = market.getBestSellPrice() ;
                if(askPrice<0) //don't bother if there is none
                    askPrice = Integer.MAX_VALUE;
            }

        } catch (IllegalAccessException e) {
            assert false;

        }
        return Math.min(askPrice,super.maxPrice(type));
    }

    /**
     * Answer the purchase strategy question: how much am I willing to pay for this specific good?
     *
     * @param good the specific good being offered to you
     * @return the maximum price I am willing to pay for this good
     */
    @Override
    public int maxPrice(Good good) {
        int askPrice = Integer.MAX_VALUE;
        try{
            if(market.isBestSalePriceVisible())
            {
                askPrice = market.getBestSellPrice() ;
                if(askPrice<0) //don't bother if there is none
                    askPrice = Integer.MAX_VALUE;
            }

        } catch (IllegalAccessException e) {
            assert false;

        }
        return Math.min(askPrice, super.maxPrice(good));
    }
}
