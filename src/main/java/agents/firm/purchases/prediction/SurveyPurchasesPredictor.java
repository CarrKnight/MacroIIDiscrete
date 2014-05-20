/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.prediction;

import agents.EconomicAgent;
import agents.firm.purchases.PurchasesDepartment;
import financial.utilities.Quote;

/**
 * <h4>Description</h4>
 * <p/>  This predictor samples its suppliers and return the cheapest
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
public class SurveyPurchasesPredictor implements PurchasesPredictor {
    /**
     * Predicts the future price of the next good to buy
     *
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    @Override
    public int predictPurchasePriceWhenIncreasingProduction(PurchasesDepartment dept) {

        return lookupPrice(dept);

    }

    private int lookupPrice(PurchasesDepartment dept) {
        EconomicAgent bestSupplier = dept.getBestSupplierFound();
        if(bestSupplier == null) //if we found nobody selling, give up
            return -1;
        else{
            Quote q = bestSupplier.askedForASaleQuote(dept.getFirm(),dept.getGoodType());
            return q.getPriceQuoted();
        }
    }

    @Override
    public int predictPurchasePriceWhenDecreasingProduction(PurchasesDepartment dept) {
        return lookupPrice(dept);
    }

    /**
     * Predicts the last closing price
     *
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    @Override
    public int predictPurchasePriceWhenNoChangeInProduction(PurchasesDepartment dept) {
        return lookupPrice(dept);
    }

    /**
     * Call this to kill the predictor
     */
    @Override
    public void turnOff() {
    }
}
