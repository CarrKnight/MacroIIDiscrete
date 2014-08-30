/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.prediction;

import agents.firm.personell.HumanResources;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.sales.prediction.RegressionDataCollector;
import agents.firm.sales.prediction.SISOPredictorBase;
import model.MacroII;
import model.utilities.stats.collectors.enums.PurchasesDataType;
import model.utilities.stats.regression.ErrorCorrectingRegressionOneStep;

import java.io.IOException;
import java.nio.file.Path;

public class ErrorCorrectingPurchasePredictor implements PurchasesPredictor {



    private final RegressionDataCollector<PurchasesDataType> collector;

    private final SISOPredictorBase<PurchasesDataType,ErrorCorrectingRegressionOneStep> base;

    public ErrorCorrectingPurchasePredictor(MacroII model, PurchasesDepartment department) {

        final PurchasesDataType xVariable = department.getGoodType().isLabor() ?  PurchasesDataType.WORKERS_TARGETED :
                PurchasesDataType.WORKERS_CONSUMING_THIS_GOOD;
        this.collector = new RegressionDataCollector<>(department, xVariable,
                PurchasesDataType.CLOSING_PRICES,PurchasesDataType.DEMAND_GAP);
        collector.setxValidator(collector.getxValidator().and(y -> y > 0));
        collector.setyValidator(collector.getyValidator().and(x -> x >0));
        base = new SISOPredictorBase<>(model,collector,new ErrorCorrectingRegressionOneStep(.9999f),null);
        base.setBurnOut(100);


    }



    /**
     * Predicts the future price of the next good to buy
     *
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    @Override
    public float predictPurchasePriceWhenIncreasingProduction(PurchasesDepartment dept) {
        double slope = base.getRegression().getGain();
        float toAdd = base.readyForPrediction() && Double.isFinite(slope) ? (float)slope : 0;
        if(dept instanceof HumanResources)
            System.out.println("slope: " + toAdd);
        return  dept.getLastClosingPrice() + toAdd;
    }

    /**
     * Predicts the future price of the next good to buy
     *
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    @Override
    public float predictPurchasePriceWhenDecreasingProduction(PurchasesDepartment dept) {
        double slope = base.getRegression().getGain();
        float toAdd = base.readyForPrediction() && Double.isFinite(slope) ? (float)slope : 0;
        return  dept.getLastClosingPrice() - toAdd;
    }

    /**
     * Predicts the future price of the next good to buy
     *
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    @Override
    public float predictPurchasePriceWhenNoChangeInProduction(PurchasesDepartment dept) {
        return  dept.getLastClosingPrice();
    }

    /**
     * Call this to kill the predictor
     */
    @Override
    public void turnOff() {
        base.turnOff();

    }

    public void setDebugWriter(Path pathToDebugFileToWrite) throws IOException {
        base.setDebugWriter(pathToDebugFileToWrite);
    }
}