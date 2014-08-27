/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.sales.SalesDepartment;
import model.MacroII;
import model.utilities.Deactivatable;
import model.utilities.stats.collectors.enums.SalesDataType;

/**
 * A multiple model regression where one gets chosen to predict, possibly by simulating.
 * Created by carrknight on 8/26/14.
 */
public class SISOGuessingSalesPredictor extends BaseSalesPredictor implements Deactivatable {


    private final RegressionDataCollector<SalesDataType> collector;

    /**
     * the sales department to use
     */
    private final SalesDepartment toFollow;

    /**
     * the set of regressions to use
     */
    private final  SISOGuessingPredictorBase<SalesDataType> regression;


    public SISOGuessingSalesPredictor(MacroII model, SalesDepartment toFollow) {
        this.toFollow = toFollow;
        collector = new RegressionDataCollector<>(toFollow,SalesDataType.WORKERS_PRODUCING_THIS_GOOD,
                SalesDataType.CLOSING_PRICES,SalesDataType.SUPPLY_GAP);
        collector.setDataValidator(collector.getDataValidator().and(dep-> dep.hasTradedAtLeastOnce()));
        collector.setyValidator(price-> Double.isFinite(price) && price > 0); // we don't want -1 prices
        regression = new SISOGuessingPredictorBase<>(model,collector);

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

        final float predicted = predictYAfterChangingXBy(1);
        if(Float.isFinite(predicted))
            return predicted;
        else return (float) toFollow.getAveragedPrice();
    }

    /**
     * predict (by simulation) what will be Y after X is changed by "increaseStep"
     * @param increaseStep can be negative or 0
     * @return the prediction or NaN if no prediction is available
     */
    public float predictYAfterChangingXBy(int increaseStep) {
       return regression.predictYAfterChangingXBy(increaseStep);

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
        final float predicted = predictYAfterChangingXBy(-1);
        if(Float.isFinite(predicted))
            return predicted;
        else return (float) toFollow.getAveragedPrice();
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
        final float predicted = predictYAfterChangingXBy(0);
        if(Float.isFinite(predicted))
            return predicted;
        else return (float) toFollow.getAveragedPrice();
    }

    /**
     * Call this to kill the predictor
     */
    @Override
    public void turnOff() {
        regression.turnOff();
    }
}
