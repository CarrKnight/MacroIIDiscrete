/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.Firm;
import agents.firm.purchases.prediction.AbstractWorkerLearningPredictor;
import agents.firm.sales.SalesDepartment;
import goods.GoodType;
import model.utilities.stats.collectors.enums.SalesDataType;

import java.util.Collections;
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

    public SamplingLearningDecreaseSalesPredictor() {

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
    public int predictSalePriceAfterIncreasingProduction(SalesDepartment dept, int expectedProductionCost, int increaseStep) {
        this.department=dept;


        boolean newSlope = buildModelAndUpdateSlope(dept);
        if(newSlope)
            predictor.setDecrementDelta((float) -regression.getSlope());


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
    public int predictSalePriceAfterDecreasingProduction(SalesDepartment dept, int expectedProductionCost, int decreaseStep) {
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
    public int predictSalePriceWhenNotChangingProduction(SalesDepartment dept) {
        return predictor.predictSalePriceWhenNotChangingProduction(dept);

    }

    /**
     * Call this to kill the predictor
     */
    @Override
    public void turnOff() {

        super.turnOff();
        predictor.turnOff();
    }

    /**
     * when was the last time a meaningful change in workers occurred
     *
     * @return the day or -1 if there are none
     */
    @Override
    protected int findLatestShockDay() {

        return department.getFirm().getLatestDayWithMeaningfulWorkforceChangeInProducingThisGood(department.getGoodType());
    }



    //look for the oldest shock day to examine
    @Override
    protected int findOldestShockDay(){
        //get all shock days from firm
        Firm owner = department.getFirm();
        GoodType goodType = department.getGoodType();
        //get all the shock days
        List<Integer> shockDays =
                owner.getAllDayWithMeaningfulWorkforceChangeInProducingThisGood(goodType);
        Collections.sort(shockDays);
        assert shockDays.size() <= 1 || shockDays.get(0) < shockDays.get(shockDays.size() - 1);

        if(shockDays.size() == 0)
            return -1;
        if(shockDays.size() <= getHowManyShockDaysBackToLookFor())
            return shockDays.get(0);


        //choose it so that the range of workers (x) is at least 5
        int todayWorkers = owner.getNumberOfWorkersWhoProduceThisGood(goodType);
        int i;  int minX = todayWorkers; int maxX = todayWorkers;
        for(i=shockDays.size()-1; i>0;i--)
        {
            //if there are at least 5 workers differnce between then and now
            int thatDayWorkers = owner.getNumberOfWorkersWhoProducedThisGoodThatDay(goodType, shockDays.get(i));
            if(thatDayWorkers < minX)
                minX = thatDayWorkers;
            if(thatDayWorkers>maxX)
                maxX = thatDayWorkers;
            assert maxX>= minX;
            if(
                    maxX - minX >= 3 &&
                    shockDays.size()-i >= getHowManyShockDaysBackToLookFor() )
                break;
        }


        return shockDays.get(i);
    }


    /**
     * Gets by how much we decrease the price predicted in respect to current departmental price.
     *
     * @return Value of by how much we increase/decrease the price predicted in respect to current departmental price.
     */
    public float getDecrementDelta() {
        return predictor.getDecrementDelta();
    }
}
