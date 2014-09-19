/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.sales.SalesDepartment;
import javafx.util.Pair;
import model.MacroII;
import model.utilities.stats.collectors.enums.SalesDataType;
import model.utilities.stats.regression.ErrorCorrectingRegressionOneStep;
import model.utilities.stats.regression.MultipleModelRegressionWithSwitching;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Error correcting model as a base for predicting marginals
 * Created by carrknight on 8/29/14.
 */
public class ErrorCorrectingSalesPredictor extends BaseSalesPredictor {

    private final RegressionDataCollector<SalesDataType> collector;

    private final SISOPredictorBase<SalesDataType,MultipleModelRegressionWithSwitching> base;

    private final FixedDecreaseSalesPredictor predictor;

    public ErrorCorrectingSalesPredictor(MacroII model,
                                         SalesDepartment department) {
        this.collector = new RegressionDataCollector<>(department,SalesDataType.WORKERS_PRODUCING_THIS_GOOD,
                SalesDataType.LAST_ASKED_PRICE,SalesDataType.SUPPLY_GAP);
        collector.setDataValidator(collector.getDataValidator().and((dept)->dept.hasTradedAtLeastOnce()));
        collector.setxValidator(collector.getxValidator().and(x -> x >=0));
        collector.setyValidator(collector.getyValidator().and(y -> y >0));
        final MultipleModelRegressionWithSwitching switching = new MultipleModelRegressionWithSwitching(
                new Pair<>((integer) -> new ErrorCorrectingRegressionOneStep(.98f), new Integer[]{0}),
                new Pair<>((integer) -> new ErrorCorrectingRegressionOneStep(.99f), new Integer[]{0}),
                new Pair<>((integer) -> new ErrorCorrectingRegressionOneStep(1), new Integer[]{0})
        );
        switching.setHowManyObservationsBeforeModelSelection(300);
        switching.setExcludeLinearFallback(false);
        switching.setRoundError(true);
        base = new SISOPredictorBase<>(model,collector,switching,null);
        base.setBurnOut(300);

        predictor = new FixedDecreaseSalesPredictor();

    }



    /**
     * This is called by the firm when it wants to predict the price they can sell to if they increase production
     *
     * @param dept                   the sales department that has to answer this question
     * @param expectedProductionCost the HQ estimate of costs in producing whatever it wants to sell. It isn't necesarilly used.
     * @param increaseStep           by how much the daily production will increase (has to be a positive number)
     * @return the best offer available/predicted or -1 if there are no quotes/good predictions
     */
    @Override
    public float predictSalePriceAfterIncreasingProduction(SalesDepartment dept, int expectedProductionCost, int increaseStep) {
        double slope = base.getRegression().getGain();
        float toAdd = base.readyForPrediction() && Double.isFinite(slope) ? (float) slope : 0;
        predictor.setDecrementDelta(-toAdd);
        return predictor.predictSalePriceAfterIncreasingProduction(dept,expectedProductionCost,1);
    }

    /**
     * This is called by the firm when it wants to predict the price they can sell to if they increase production
     *
     * @param dept                   the sales department that has to answer this question
     * @param expectedProductionCost the HQ estimate of costs in producing whatever it wants to sell. It isn't necesarilly used.
     * @param decreaseStep           by how much the daily production will decrease (has to be a positive number)
     * @return the best offer available/predicted or -1 if there are no quotes/good predictions
     */
    @Override
    public float predictSalePriceAfterDecreasingProduction(SalesDepartment dept, int expectedProductionCost, int decreaseStep) {
        double slope = base.getRegression().getGain();
        float toAdd = base.readyForPrediction() && Double.isFinite(slope) ? (float) slope : 0;
        predictor.setDecrementDelta(-toAdd);
        return predictor.predictSalePriceAfterDecreasingProduction(dept,expectedProductionCost,1);
    }

    /**
     * This is a little bit weird to predict, but basically you want to know what will be "tomorrow" price if you don't change production.
     * Most predictors simply return today closing price, because maybe this will be useful in some cases. It's used by Marginal Maximizer Statics
     *
     * @param dept the sales department
     * @return predicted price
     */
    @Override
    public float predictSalePriceWhenNotChangingProduction(SalesDepartment dept) {
       return predictor.predictSalePriceWhenNotChangingProduction(dept);
    }

    @Override
    public void turnOff() {
        super.turnOff();
        base.turnOff();
    }

    public void setDebugWriter(Path pathToDebugFileToWrite) throws IOException {
        base.setDebugWriter(pathToDebugFileToWrite);
    }
}
