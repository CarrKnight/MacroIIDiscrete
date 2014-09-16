/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.prediction;

import agents.firm.purchases.PurchasesDepartment;
import agents.firm.sales.prediction.RegressionDataCollector;
import agents.firm.sales.prediction.SISOPredictorBase;
import model.MacroII;
import model.utilities.stats.collectors.enums.PurchasesDataType;
import model.utilities.stats.regression.AutoRegressiveWithInputRegression;

/**
 * A predictor that uses a distributed lag time-series
 * Created by carrknight on 9/5/14.
 */
public class AutoRegressivePurchasesPredictor
        implements PurchasesPredictor {



    private final RegressionDataCollector<PurchasesDataType> collector;

    private final SISOPredictorBase<PurchasesDataType,
            AutoRegressiveWithInputRegression> base;

    final PurchasesDataType xVariable;


    public AutoRegressivePurchasesPredictor(MacroII model,
                                            PurchasesDepartment dept) {
        xVariable = dept.getGoodType().isLabor() ?  PurchasesDataType.WORKERS_TARGETED :
                PurchasesDataType.WORKERS_CONSUMING_THIS_GOOD;

        this.collector = new RegressionDataCollector<>(dept,xVariable,
              PurchasesDataType.LAST_OFFERED_PRICE,PurchasesDataType.DEMAND_GAP);

        collector.setxValidator(collector.getxValidator().and(y -> y > 0));
        collector.setyValidator(collector.getyValidator().and(x -> x >0));
        base = new SISOPredictorBase<>(
                model,collector,new AutoRegressiveWithInputRegression(5,5));
        base.setBurnOut(300);

    }


    public AutoRegressivePurchasesPredictor(SISOPredictorBase<PurchasesDataType,
            AutoRegressiveWithInputRegression> base, PurchasesDataType xVariable) {
        this.base = base;
        this.xVariable = xVariable;
        this.collector = base.getCollector();
    }

    /**
     * Predicts the future price of the next good to buy
     *
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    @Override
    public float predictPurchasePriceWhenIncreasingProduction(PurchasesDepartment dept) {
        final float prediction = base.predictYAfterChangingXBy(1);

        if(!base.readyForPrediction() || Float.isNaN(prediction))
            return dept.getLastClosingPrice();
        else
            assert (prediction>=0);

        return prediction;
    }

    /**
     * Predicts the future price of the next good to buy
     *
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    @Override
    public float predictPurchasePriceWhenDecreasingProduction(PurchasesDepartment dept) {
        final float prediction = base.predictYAfterChangingXBy(-1);

        if(!base.readyForPrediction() || Float.isNaN(prediction))
            return dept.getLastClosingPrice();
        else
            assert (prediction>=0);

        return prediction;    }

    /**
     * Predicts the future price of the next good to buy
     *
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    @Override
    public float predictPurchasePriceWhenNoChangeInProduction(PurchasesDepartment dept) {
        final float prediction = base.predictYAfterChangingXBy(0);

        if(!base.readyForPrediction() || Float.isNaN(prediction))
            return dept.getLastClosingPrice();
        else
            assert (prediction>=0);

        return prediction;
    }

    /**
     * Call this to kill the predictor
     */
    @Override
    public void turnOff() {
        base.turnOff();
    }
}
