/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.pricing;

import agents.EconomicAgent;
import agents.firm.purchases.PurchasesDepartment;
import financial.utilities.PurchaseResult;
import goods.Good;
import goods.GoodType;

/**
 * <h4>Description</h4>
 * <p/> This strategy simply asks around for what the right price should be. Randomizes otherwise
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
public class SurveyMaxPricing implements BidPricingStrategy {

    private final PurchasesDepartment department;

    public SurveyMaxPricing(PurchasesDepartment dept)
    {
        this.department = dept;
    }




    /**
     * Answer the purchase strategy question: how much am I willing to pay for a good of this type?
     *
     * @param type the type of good you want to buy
     * @return the maximum price I am willing to pay for this good
     */
    @Override
    public long maxPrice(GoodType type) {

        EconomicAgent best = department.getBestSupplierFound();
        if(best == null)  //did you find anything?
        {
            //if not go at random
            department.supplierSearchFailure(best, PurchaseResult.NO_MATCH_AVAILABLE);
            if(department.getAvailableBudget() == 0)
                return 0;
            return department.getRandom().nextLong(department.getAvailableBudget());

        }
        else {
            long bestPrice = best.askedForASaleQuote(department.getFirm(),department.getGoodType()).getPriceQuoted();
            assert bestPrice >=0; //shouldn't be -1 or it would be null!

            return bestPrice;

        }

    }

    /**
     * Answer the purchase strategy question: how much am I willing to pay for this specific good?
     *
     * @param good the specific good being offered to you
     * @return the maximum price I am willing to pay for this good
     */
    @Override
    public long maxPrice(Good good) {
        return maxPrice(good.getType());
    }

    /**
     * Useful to stop the bid pricing strategy from giving orders
     */
    @Override
    public void turnOff() {
    }




}
