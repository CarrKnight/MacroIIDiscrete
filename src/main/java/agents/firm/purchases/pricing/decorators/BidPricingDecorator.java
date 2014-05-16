/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.pricing.decorators;

import agents.firm.purchases.pricing.BidPricingStrategy;
import goods.Good;
import goods.GoodType;
import model.utilities.NonDrawable;

/**
 * <h4>Description</h4>
 * <p/> A simple abstract class to use as the basis for decorating bid prices.
 * <p/> It is nondrawable
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2012-11-13
 * @see
 */
@NonDrawable
public abstract class BidPricingDecorator implements BidPricingStrategy {



    final protected BidPricingStrategy toDecorate;


    protected BidPricingDecorator(BidPricingStrategy toDecorate) {
        this.toDecorate = toDecorate;
    }


    /**
     * Answer the purchase strategy question: how much am I willing to pay for a good of this type?
     * @param type the type of good you want to buy
     * @return the maximum price I am willing to pay for this good
     */
    @Override
    public int maxPrice(GoodType type) {
        return toDecorate.maxPrice(type);
    }

    /**
     * Answer the purchase strategy question: how much am I willing to pay for this specific good?
     * @param good the specific good being offered to you
     * @return the maximum price I am willing to pay for this good
     */
    @Override
    public int maxPrice(Good good) {
        return toDecorate.maxPrice(good);
    }

    /**
     * Useful to stop the bid pricing strategy from giving orders
     */
    @Override
    public void turnOff() {
        toDecorate.turnOff();
    }
}
