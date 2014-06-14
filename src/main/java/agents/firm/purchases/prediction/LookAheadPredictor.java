/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.prediction;

import agents.firm.purchases.PurchasesDepartment;

/**
 * <h4>Description</h4>
 * <p/>  This predictor asks the market for the best price and returns it as a prediction. If the market doesn't allow that information then it just defaults to survey
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
public class LookAheadPredictor implements PurchasesPredictor {


    PricingPurchasesPredictor defaultTo;

    /**
     * Predicts the future price of the next good to buy
     *
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    @Override
    public float predictPurchasePriceWhenIncreasingProduction(PurchasesDepartment dept) {
        //can we see the best price?
        if(dept.getMarket().isBestSalePriceVisible())
            try {
                int bestPrice = dept.getMarket().getBestSellPrice(); //if so return it
                return  bestPrice == -1 ? dept.getMarket().getLastPrice() : bestPrice; //if there is none, return last closing price
            } catch (IllegalAccessException e) {
                assert false; //this should never happen!
                throw  new IllegalStateException("the market told us the best price was visible but then threw an exception when we asked about it");
            }
        else
        {
            //we can't see it, go to basics
            //if this is our first time we need to instantiate the default predictor
            if(defaultTo == null)
                defaultTo = new PricingPurchasesPredictor();
            return defaultTo.predictPurchasePriceWhenIncreasingProduction(dept);

        }

    }

    @Override
    public float predictPurchasePriceWhenDecreasingProduction(PurchasesDepartment dept) {
        if(defaultTo == null)
            defaultTo = new PricingPurchasesPredictor();
        return defaultTo.predictPurchasePriceWhenDecreasingProduction(dept);

    }

    /**
     * Predicts the last closing price
     *
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    @Override
    public float predictPurchasePriceWhenNoChangeInProduction(PurchasesDepartment dept) {
        return defaultTo.predictPurchasePriceWhenNoChangeInProduction(dept);
    }




    /**
     * Call this to kill the predictor
     */
    @Override
    public void turnOff() {
        defaultTo = null;
    }
}



