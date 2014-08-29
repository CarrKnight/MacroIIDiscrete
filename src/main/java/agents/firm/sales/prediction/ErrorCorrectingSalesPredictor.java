/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.sales.SalesDepartment;
import model.MacroII;
import model.utilities.stats.collectors.enums.SalesDataType;
import model.utilities.stats.regression.ErrorCorrectingRegressionOneStep;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Error correcting model as a base for predicting marginals
 * Created by carrknight on 8/29/14.
 */
public class ErrorCorrectingSalesPredictor extends BaseSalesPredictor {

    private final RegressionDataCollector<SalesDataType> collector;

    private final SISOPredictorBase<SalesDataType,ErrorCorrectingRegressionOneStep> base;

    public ErrorCorrectingSalesPredictor(MacroII model, SalesDepartment department) {
        this.collector = new RegressionDataCollector<>(department,SalesDataType.WORKERS_PRODUCING_THIS_GOOD,
                SalesDataType.CLOSING_PRICES,SalesDataType.SUPPLY_GAP);
        collector.setxValidator(collector.getxValidator().and(y -> y > 0));
        collector.setyValidator(collector.getyValidator().and(x -> x >0));
        base = new SISOPredictorBase<>(model,collector,new ErrorCorrectingRegressionOneStep(.9999f),null);
        base.setBurnOut(100);

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
        float toAdd = base.readyForPrediction() && Double.isFinite(slope) ? (float)slope : 0;
        //System.out.println("slope: " + toAdd);
        return  dept.getLastClosingPrice() + toAdd;
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
        float toAdd = base.readyForPrediction() && Double.isFinite(slope) ? (float)slope : 0;
        return  dept.getLastClosingPrice() - toAdd;
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
        return  dept.getLastClosingPrice();
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
