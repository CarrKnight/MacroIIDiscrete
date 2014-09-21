/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.prediction;

import agents.firm.purchases.PurchasesDepartment;
import com.google.common.base.Preconditions;

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

    public static final float defaultIncrementDelta = 1f;
    /**
     * by how much we increase/decrease the price predicted in respect to current departmental price
     */
    private float incrementDelta = defaultIncrementDelta;


    /**
     * the delegate object we use to get the department price
     */
    private final PurchasesPredictor delegate;

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
    public float predictPurchasePriceWhenIncreasingProduction(PurchasesDepartment dept) {
        return Math.max(0,(delegate.predictPurchasePriceWhenIncreasingProduction(dept)+incrementDelta));

    }

    @Override
    public float predictPurchasePriceWhenDecreasingProduction(PurchasesDepartment dept) {
        return Math.max(0,(delegate.predictPurchasePriceWhenDecreasingProduction(dept)-incrementDelta));


    }

    /**
     * Predicts the last closing price
     *
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    @Override
    public float predictPurchasePriceWhenNoChangeInProduction(PurchasesDepartment dept) {
        return delegate.predictPurchasePriceWhenNoChangeInProduction(dept);
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
        Preconditions.checkState(!Float.isNaN(incrementDelta) && !Float.isInfinite(incrementDelta));
        this.incrementDelta = incrementDelta;
    }
}
