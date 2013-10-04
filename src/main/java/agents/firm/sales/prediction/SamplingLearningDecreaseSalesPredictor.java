/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.production.Plant;
import agents.firm.purchases.prediction.AbstractWorkerLearningPredictor;
import agents.firm.sales.SalesDepartment;
import model.MacroII;
import model.utilities.stats.collectors.enums.SalesDataType;

import java.util.List;

/**
 * <h4>Description</h4>
 * <p/>
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-09-15
 * @see
 */
public class SamplingLearningDecreaseSalesPredictor extends AbstractWorkerLearningPredictor implements SalesPredictor {

    /**
     * the object we are going to feed with "learned" slope to make a prediction
     */
    private final FixedDecreaseSalesPredictor predictor;

    /**
     * the purchase department we are predicting for
     */
    private SalesDepartment department;

    public SamplingLearningDecreaseSalesPredictor(MacroII model) {
        super(model);

        predictor = new FixedDecreaseSalesPredictor();
    }

    @Override
    protected double[] getYArray(int[] days) {
        return department.getObservationsRecordedTheseDays(SalesDataType.AVERAGE_CLOSING_PRICES,days);
    }

    @Override
    protected double[] getXArray(int[] days) {
        return department.getObservationsRecordedTheseDays(SalesDataType.WORKERS_PRODUCING_THIS_GOOD,days);
    }

    @Override
    protected double[] buildWeights(int[] days, double[] x, double[] y) {
        double[] weights = new double[days.length];
        double[] gaps =  department.getObservationsRecordedTheseDays(SalesDataType.SUPPLY_GAP,days);
        for(int i=0; i<weights.length; i++)
        {
            double gap = gaps[i];
            if(x[i]==0 || y[i]==-1) //if there was nothing traded that day, ignore the observation entirely
                weights[i]=0;
            else {
                double weight = 2d / (1d + Math.exp(Math.abs(gap)));
                weights[i] = weight;
            }
        }
        return weights;    }


    /**
     * This is called by the firm when it wants to predict the price they can sell to if they increase production
     *
     * @param dept                   the sales department that has to answer this question
     * @param expectedProductionCost the HQ estimate of costs in producing whatever it wants to sell. It isn't necesarilly used.
     * @param increaseStep           by how much the daily production will increase (has to be a positive number)
     * @return the best offer available/predicted or -1 if there are no quotes/good predictions
     */
    @Override
    public long predictSalePriceAfterIncreasingProduction(SalesDepartment dept, long expectedProductionCost, int increaseStep) {
        this.department=dept;


        boolean newSlope = buildModelAndUpdateSlope(dept);
        if(newSlope)
            predictor.setDecrementDelta((float) -regression.getSlope());

       // System.out.println("sales slope: " + predictor.getDecrementDelta());

        return predictor.predictSalePriceAfterIncreasingProduction(dept, expectedProductionCost, 1); //we increase step only by 1 because we are focusing on workers, not outflow
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
    public long predictSalePriceAfterDecreasingProduction(SalesDepartment dept, long expectedProductionCost, int decreaseStep) {
        this.department=dept;


        boolean newSlope = buildModelAndUpdateSlope(dept);
        if(newSlope)
            predictor.setDecrementDelta((float) -regression.getSlope());

        return predictor.predictSalePriceAfterDecreasingProduction(dept, expectedProductionCost, 1); //we increase step only by 1 because we are focusing on workers, not outflow

    }

    /**
     * This is a little bit weird to predict, but basically you want to know what will be "tomorrow" price if you don't change production.
     * Most predictors simply return today closing price, because maybe this will be useful in some cases. It's used by Marginal Maximizer Statics
     *
     * @param dept the sales department
     * @return predicted price
     */
    @Override
    public long predictSalePriceWhenNotChangingPoduction(SalesDepartment dept) {
        return predictor.predictSalePriceWhenNotChangingPoduction(dept);

    }

    /**
     * Call this to kill the predictor
     */
    @Override
    public void turnOff() {
        predictor.turnOff();
    }

    /**
     * when was the last time a meaningful change in workers occurred
     *
     * @return the day or -1 if there are none
     */
    @Override
    protected int findLatestShockDay() {
        List<Plant> plants = department.getFirm().getListOfPlantsProducingSpecificOutput(department.getGoodType());
        int latestShockDay = -1;
        for(Plant p : plants)
        {
            latestShockDay = Math.max(latestShockDay,p.getLastDayAMeaningfulChangeInWorkforceOccurred());
        }
        return latestShockDay;    }


    /**
     * Gets by how much we decrease the price predicted in respect to current departmental price.
     *
     * @return Value of by how much we increase/decrease the price predicted in respect to current departmental price.
     */
    public float getDecrementDelta() {
        return predictor.getDecrementDelta();
    }
}
