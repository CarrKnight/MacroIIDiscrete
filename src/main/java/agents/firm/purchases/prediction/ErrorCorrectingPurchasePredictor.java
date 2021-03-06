/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.prediction;

import agents.firm.Department;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.sales.prediction.RegressionDataCollector;
import agents.firm.sales.prediction.SISOPredictorBase;
import javafx.util.Pair;
import model.MacroII;
import model.utilities.stats.collectors.enums.PurchasesDataType;
import model.utilities.stats.regression.ErrorCorrectingRegressionOneStep;
import model.utilities.stats.regression.MultipleModelRegressionWithSwitching;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ErrorCorrectingPurchasePredictor implements PurchasesPredictor {


    private final SISOPredictorBase<PurchasesDataType,MultipleModelRegressionWithSwitching> base;

    private final FixedIncreasePurchasesPredictor predictor;

    public ErrorCorrectingPurchasePredictor(MacroII model, PurchasesDepartment department) {

        final PurchasesDataType xVariable = PurchasesDataType.WORKERS_CONSUMING_THIS_GOOD;
        //focus on pid prices if you are hr, if you are buying other inputs, I expect you to be a price-taker
        final PurchasesDataType yVariable = department.getGoodType().isLabor() ?
                PurchasesDataType.LAST_OFFERED_PRICE : PurchasesDataType.EFFECTIVE_CLOSING_PRICE;
        RegressionDataCollector<PurchasesDataType> collector = new RegressionDataCollector<>(department, xVariable,
                yVariable, PurchasesDataType.DEMAND_GAP);
        collector.setDataValidator(collector.getDataValidator().and(Department::hasTradedAtLeastOnce));
        collector.setxValidator(collector.getxValidator().and(x -> x > 0));
        collector.setyValidator(collector.getyValidator().and(y -> y >= 0));
        final MultipleModelRegressionWithSwitching switching = new MultipleModelRegressionWithSwitching(
                new Pair<>((integer) -> new ErrorCorrectingRegressionOneStep(.96f), new Integer[]{0}),
                new Pair<>((integer) -> new ErrorCorrectingRegressionOneStep(.98f), new Integer[]{0}),
                new Pair<>((integer) -> new ErrorCorrectingRegressionOneStep(1), new Integer[]{0})
        );

        switching.setHowManyObservationsBeforeModelSelection(300);
        switching.setExcludeLinearFallback(false);
        switching.setRoundError(true);
        base = new SISOPredictorBase<>(model, collector, switching,null);


        try{
            if(!department.getGoodType().isLabor())
             base.setDebugWriter(Paths.get("tmp.csv"));
        }
        catch (Exception e){};

        predictor = new FixedIncreasePurchasesPredictor();

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
        float toAdd = base.readyForPrediction() && Double.isFinite(slope) ? (float) slope : 0;
        predictor.setIncrementDelta(Math.max(toAdd,0));
        return predictor.predictPurchasePriceWhenIncreasingProduction(dept);
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
        float toAdd = base.readyForPrediction() && Double.isFinite(slope) ? (float) slope : 0;
        predictor.setIncrementDelta(Math.max(toAdd,0));
        return predictor.predictPurchasePriceWhenDecreasingProduction(dept);
    }

    /**
     * Predicts the future price of the next good to buy
     *
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    @Override
    public float predictPurchasePriceWhenNoChangeInProduction(PurchasesDepartment dept) {
        return  predictor.predictPurchasePriceWhenNoChangeInProduction(dept);
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