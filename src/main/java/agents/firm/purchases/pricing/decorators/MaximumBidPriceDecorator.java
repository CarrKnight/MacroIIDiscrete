/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.pricing.decorators;

import agents.firm.purchases.pricing.BidPricingStrategy;
import goods.Good;
import goods.GoodType;
import model.utilities.NonDrawable;

/**
 * <h4>Description</h4>
 * <p/> This decorator just implements a floor price (the reservation price) above which the purchase department just never goes.
 * <p/>
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
public class MaximumBidPriceDecorator extends BidPricingDecorator {


    private int reservationPrice;

    /**
     * This decorator just implements a floor price (the reservation price) above which the purchase department just never goes.
     * @param toDecorate the strategy to decorate
     * @param reservationPrice the minimum price above which the department never goes
     */
    public MaximumBidPriceDecorator(BidPricingStrategy toDecorate, int reservationPrice) {
        super(toDecorate);
        this.reservationPrice = reservationPrice;
    }


    /**
     * Answer the purchase strategy question: how much am I willing to pay for a good of this type?
     * @param type the type of good you want to buy
     * @return the maximum price I am willing to pay for this good
     */
    public int maxPrice(GoodType type) {
        return Math.min(toDecorate.maxPrice(type), reservationPrice);
    }

    /**
     * Answer the purchase strategy question: how much am I willing to pay for this specific good?
     * @param good the specific good being offered to you
     * @return the maximum price I am willing to pay for this good
     */
    public int maxPrice(Good good) {
        return Math.min(toDecorate.maxPrice(good), reservationPrice);
    }


    public int getReservationPrice() {
        return reservationPrice;
    }

    public void setReservationPrice(int reservationPrice) {
        this.reservationPrice = reservationPrice;
    }
}
