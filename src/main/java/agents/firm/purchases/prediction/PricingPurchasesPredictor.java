/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
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
    public float predictPurchasePriceWhenIncreasingProduction(PurchasesDepartment dept) {

        return price(dept);
    }

    private float price(PurchasesDepartment dept) {
        float averagedClosingPrice = dept.getAveragedClosingPrice();
        if(Float.isNaN(averagedClosingPrice))
            return -1;

        return averagedClosingPrice;
    }

    @Override
    public float predictPurchasePriceWhenDecreasingProduction(PurchasesDepartment dept) {
        return price(dept);
    }

    /**
     * Predicts the last closing price
     *
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    @Override
    public float predictPurchasePriceWhenNoChangeInProduction(PurchasesDepartment dept) {
        return price(dept);
    }

    /**
     * Call this to kill the predictor
     */
    @Override
    public void turnOff() {
    }
}
