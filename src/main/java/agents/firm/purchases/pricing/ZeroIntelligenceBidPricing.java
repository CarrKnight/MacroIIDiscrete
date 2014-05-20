/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.pricing;

import agents.firm.purchases.PurchasesDepartment;
import ec.util.MersenneTwisterFast;
import goods.Good;
import goods.GoodType;

/**
 * <h4>Description</h4>
 * <p/> THis is the dumb pricing strategy: chooses a price a random within the available budget
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-06
 * @see
 */
public class ZeroIntelligenceBidPricing implements BidPricingStrategy{


    PurchasesDepartment dept;
    MersenneTwisterFast random;

    public ZeroIntelligenceBidPricing(PurchasesDepartment dept) {
        this.dept = dept;
        this.random = dept.getRandom();
    }

    /**
     * Answer the purchase strategy question: how much am I willing to pay for a good of this type?
     *
     * @param type the type of good you want to buy
     * @return the maximum price I am willing to pay for this good
     */
    @Override
    public int maxPrice(GoodType type) {

        assert dept.getGoodType() == type;
        if(dept.getAvailableBudget() == 0) //if we got no money, offer no money
            return 0;
        return random.nextInt(dept.getAvailableBudget());

    }

    /**
     * Answer the purchase strategy question: how much am I willing to pay for this specific good?
     *
     * @param good the specific good being offered to you
     * @return the maximum price I am willing to pay for this good
     */
    @Override
    public int maxPrice(Good good) {
        return random.nextInt(dept.getAvailableBudget());
    }

    /**
     * Useful to stop the bid pricing strategy from giving orders
     */
    @Override
    public void turnOff() {
    }



}
