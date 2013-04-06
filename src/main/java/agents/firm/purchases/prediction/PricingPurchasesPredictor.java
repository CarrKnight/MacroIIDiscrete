/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.prediction;

import agents.firm.purchases.PurchasesDepartment;

/**
 * <h4>Description</h4>
 * <p/> This strategy simply predicts that the next price will be exactly what the purchases department wants to pay for it
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-10-04
 * @see
 */
public class PricingPurchasesPredictor implements PurchasesPredictor {
    /**
     * Predicts the future price of the next good to buy
     *
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    @Override
    public long predictPurchasePrice(PurchasesDepartment dept) {
        return dept.maxPrice(dept.getGoodType(),dept.getMarket());
    }

    /**
     * Call this to kill the predictor
     */
    @Override
    public void turnOff() {
    }
}
