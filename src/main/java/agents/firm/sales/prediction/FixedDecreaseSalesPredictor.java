/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.sales.SalesDepartment;
import com.google.common.base.Preconditions;

/**
 * <h4>Description</h4>
 * <p/> This is a simple, unrealistic sale price predictor that always predicts the current sales department price - a fixed delta (1 by default)
 * <p/> This is supposed to be a class to use when testing what happens with perfect information (the demand is linear and we know the slope or something)
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-07-01
 * @see
 */
public class FixedDecreaseSalesPredictor extends BaseSalesPredictor {

    public static float defaultDecrementDelta = 1;
    /**
     * by how much we increase/decrease the price predicted in respect to current departmental price
     */
    private float decrementDelta = defaultDecrementDelta;

    /**
     * the delegate object we use to get the department price
     */
    private final SalesPredictor delegate;


    public FixedDecreaseSalesPredictor() {
        this(defaultDecrementDelta);
    }


    public FixedDecreaseSalesPredictor(float decrementDelta) {
        this.decrementDelta = decrementDelta;
        delegate = new MemorySalesPredictor();

    }

    /**
     * This  returns the sales department current price for a new good - the fixed decrement delta.
     * Never returns less than 0
     */
    @Override
    public long predictSalePriceAfterIncreasingProduction(SalesDepartment dept, long expectedProductionCost, int increaseStep) {
        Preconditions.checkArgument(increaseStep >= 0);

        long beforeImpactPrice = delegate.predictSalePriceAfterIncreasingProduction(dept, expectedProductionCost, increaseStep);

        if(beforeImpactPrice == -1)
            return -1;

        return Math.max(0,Math.round(beforeImpactPrice -decrementDelta* (float)increaseStep));
    }

    /**
     * This  returns the sales department current price for a new good - the fixed decrement delta.
     * Never returns less than 0
     */
    @Override
    public long predictSalePriceAfterDecreasingProduction(SalesDepartment dept, long expectedProductionCost, int decreaseStep) {
        Preconditions.checkArgument(decreaseStep >= 0);

        long beforeImpactPrice = delegate.predictSalePriceAfterDecreasingProduction(dept, expectedProductionCost, decreaseStep);
        if(beforeImpactPrice == -1)
            return -1;

        return Math.max(0,Math.round(beforeImpactPrice + decrementDelta * (float) decreaseStep));
    }

    /**
     * Nothing happens
     */
    @Override
    public void turnOff() {
        super.turnOff();
        delegate.turnOff();
    }


    /**
     * Gets by how much we decrease the price predicted in respect to current departmental price.
     *
     * @return Value of by how much we increase/decrease the price predicted in respect to current departmental price.
     */
    public float getDecrementDelta() {
        return decrementDelta;
    }

    /**
     * Sets by how much we increase/decrease the price predicted in respect to current departmental price.
     *
     * @param decrementDelta New value of by how much we increase/decrease the price predicted in respect to current departmental price.
     */
    public void setDecrementDelta(float decrementDelta) {
        this.decrementDelta = decrementDelta;
    }

    /**
     * This is a little bit weird to predict, but basically you want to know what will be "tomorrow" price if you don't change production.
     * Most predictors simply return today closing price, because maybe this will be useful in some cases. It's used by Marginal Maximizer Statics
     *
     * @param dept the sales department
     * @return predicted price
     */
    @Override
    public long predictSalePriceWhenNotChangingProduction(SalesDepartment dept) {
        return delegate.predictSalePriceWhenNotChangingProduction(dept);
    }
}
