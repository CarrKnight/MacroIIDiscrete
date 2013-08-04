/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.prediction;

import agents.firm.purchases.PurchasesDepartment;

/**
 * <h4>Description</h4>
 * <p/> The "inverse" of FixedDecreaseSalesPredictor. Predicts a purchase price by assuming it's always going to be the current price + a fixed delta
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-08-04
 * @see
 */
public class FixedIncreasePurchasesPredictor implements PurchasesPredictor {

    public static float defaultIncrementDelta = 1f;
    /**
     * by how much we increase/decrease the price predicted in respect to current departmental price
     */
    private float incrementDelta = defaultIncrementDelta;


    /**
     * the delegate object we use to get the department price
     */
    private final PricingPurchasesPredictor delegate;

    /**
     * creates the new
     */
    public FixedIncreasePurchasesPredictor() {
        this(defaultIncrementDelta);
    }


    public FixedIncreasePurchasesPredictor(float incrementDelta) {
        this.incrementDelta = incrementDelta;
        delegate = new PricingPurchasesPredictor();

    }

    /**
     * Predicts the future price of the next good to buy
     *
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    @Override
    public long predictPurchasePrice(PurchasesDepartment dept) {
        return Math.max(0,Math.round(delegate.predictPurchasePrice(dept)+incrementDelta));

    }

    /**
     * Call this to kill the predictor
     */
    @Override
    public void turnOff() {
        //nothing really happens!
        delegate.turnOff();
    }


    public float getIncrementDelta() {
        return incrementDelta;
    }

    public void setIncrementDelta(float incrementDelta) {
        this.incrementDelta = incrementDelta;
    }
}
