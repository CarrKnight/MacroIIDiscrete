/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.prediction;

import agents.firm.purchases.PurchasesDepartment;
import model.MacroII;

/**
 * <h4>Description</h4>
 * <p/> This is basically the open-loop version of the recursive purchase predictor. Instead of using the beta of the regression as soon as new information comes in, this predictor
 * uses the beta it learned (closes the loop) only every 100 days.
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-11-15
 * @see
 */
public class OpenLoopRecursivePurchasesPredictor extends AbstractOpenLoopRecursivePredictor implements PurchasesPredictor {


    private final FixedIncreasePurchasesPredictor predictor;



    public OpenLoopRecursivePurchasesPredictor(MacroII model, PurchasesDepartment department) {
        super(new RecursivePurchasesPredictor(model,department),model);


        predictor = new FixedIncreasePurchasesPredictor(0);
    }

    /**
     * Predicts the future price of the next good to buy
     *
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    @Override
    public long predictPurchasePriceWhenIncreasingProduction(PurchasesDepartment dept) {

        float slope = predictPrice(1)-predictPrice(0);
        predictor.setIncrementDelta(slope);
        return predictor.predictPurchasePriceWhenIncreasingProduction(dept);

    }

    /**
     * Predicts the future price of the next good to buy
     *
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    @Override
    public long predictPurchasePriceWhenDecreasingProduction(PurchasesDepartment dept) {
        float slope = predictPrice(0)-predictPrice(-1);
        predictor.setIncrementDelta(slope);
        return predictor.predictPurchasePriceWhenDecreasingProduction(dept);
    }

    /**
     * Predicts the future price of the next good to buy
     *
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    @Override
    public long predictPurchasePriceWhenNoChangeInProduction(PurchasesDepartment dept) {
        return predictor.predictPurchasePriceWhenNoChangeInProduction(dept);

    }


}
