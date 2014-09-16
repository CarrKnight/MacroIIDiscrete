/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.sales.SalesDepartment;
import model.MacroII;
import model.utilities.stats.collectors.enums.SalesDataType;
import model.utilities.stats.regression.AutoRegressiveWithInputRegression;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Sales predictor fitting Y on 2 lags of itself, X and 2 lags of X. Because of very likely stationarity, the regression is actually on deltas.
 * Created by carrknight on 9/5/14.
 */
public class AutoRegressiveSalesPredictor extends BaseSalesPredictor {


    private final RegressionDataCollector<SalesDataType> collector;

    private final SISOPredictorBase<SalesDataType,
            AutoRegressiveWithInputRegression> base;
    private final static SalesDataType INDEPENDENT_VARIABLE =
            SalesDataType.WORKERS_PRODUCING_THIS_GOOD;


    public AutoRegressiveSalesPredictor(MacroII model,
                                        SalesDepartment department) {
        this.collector = new RegressionDataCollector<>(department,
                INDEPENDENT_VARIABLE,
                SalesDataType.LAST_ASKED_PRICE,SalesDataType.SUPPLY_GAP);
        collector.setxValidator(collector.getxValidator().and(y -> y > 0));
        collector.setyValidator(collector.getyValidator().and(x -> x >0));
        base = new SISOPredictorBase<>(model,collector,new AutoRegressiveWithInputRegression(5,5),null);
        base.setBurnOut(300);
    }


    public AutoRegressiveSalesPredictor(SISOPredictorBase<SalesDataType, AutoRegressiveWithInputRegression> base)
    {
        this.base = base;
        this.collector = base.getCollector();
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
        final float prediction = base.predictYAfterChangingXBy(1);

        if(!base.readyForPrediction() || Float.isNaN(prediction))
            return dept.getLastClosingPrice();
        else
            assert (prediction>=0);
        return prediction;
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
        final float prediction = base.predictYAfterChangingXBy(-1);

        if(!base.readyForPrediction() || Float.isNaN(prediction))
            return dept.getLastClosingPrice();
        else
            assert (prediction>=0);
        return prediction;

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
        final float prediction = base.predictYAfterChangingXBy(0);

        if(!base.readyForPrediction() || Float.isNaN(prediction))
            return dept.getLastClosingPrice();
        else
            assert (prediction>=0);

        return prediction;

    }

    @Override
    public void turnOff() {
        super.turnOff();
        base.turnOff();
    }

    public void setDebugWriter(Path regressionLogToWrite) throws IOException {
        base.setDebugWriter(regressionLogToWrite);
    }
}
