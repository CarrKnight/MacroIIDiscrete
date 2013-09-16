/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.prediction;

import agents.firm.production.Plant;
import agents.firm.purchases.PurchasesDepartment;
import model.MacroII;
import model.utilities.stats.collectors.enums.PurchasesDataType;

import java.util.List;

/**
 * <h4>Description</h4>
 * <p/> Functionally this is exactly equal to LearningIncreasePurchasePredictor except that it uses the sales department
 * original data functionality rather than creating its own observer and so should be slightly easier to read
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-09-13
 * @see
 */
public class SamplingLearningIncreasePurchasePredictor extends  AbstractWorkerLearningPredictor implements PurchasesPredictor {



    /**
     * the object we are going to feed with "learned" slope to make a prediction
     */
    private final FixedIncreasePurchasesPredictor predictor;

    /**
     * the purchase department we are predicting for
     */
    private PurchasesDepartment department;

    public SamplingLearningIncreasePurchasePredictor(MacroII model) {

        super(model);

        predictor = new FixedIncreasePurchasesPredictor();


    }

    @Override
    protected double[] buildWeights(int[] days, double[] x, double[] y) {
        double[] weights = new double[days.length];
        double[] gaps =  department.getObservationsRecordedTheseDays(PurchasesDataType.DEMAND_GAP,days);
        for(int i=0; i<weights.length; i++)
        {
            double gap = gaps[i];
            if(department.getGoodType().isLabor())
                gap = gap;
            if(x[i]==0 || y[i]==-1) //if there was nothing traded that day, ignore the observation entirely
                weights[i]=0;
            else {
                double weight = 2d / (1d + Math.exp(Math.abs(gap)));
                weights[i] = weight;
            }
        }
        return weights;
    }


    @Override
    protected double[] getXArray(int[] days) {
        return department.getObservationsRecordedTheseDays(PurchasesDataType.WORKERS_CONSUMING_THIS_GOOD,days);
    }

    @Override
    protected double[] getYArray(int[] days) {
        return  department.getObservationsRecordedTheseDays(PurchasesDataType.CLOSING_PRICES,days);
    }



    /**
     * Predicts the future price of the next good to buy
     *
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    @Override
    public long predictPurchasePriceWhenIncreasingProduction(PurchasesDepartment dept) {
        this.department=dept;


        boolean newSlope = buildModelAndUpdateSlope(dept);
        if(newSlope)
            predictor.setIncrementDelta((float) regression.getSlope());

        System.out.println("purchases slope: " + predictor.getIncrementDelta());

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

        this.department=dept;


        boolean newSlope = buildModelAndUpdateSlope(dept);
        if(newSlope)
            predictor.setIncrementDelta((float) regression.getSlope());

        return predictor.predictPurchasePriceWhenDecreasingProduction(dept);    }

    /**
     * Call this to kill the predictor
     */
    @Override
    public void turnOff() {
        predictor.turnOff();

    }


    public int findLatestShockDay()
    {
        List<Plant> plants = department.getFirm().getListOfPlantsUsingSpecificInput(department.getGoodType());
        int latestShockDay = -1;
        for(Plant p : plants)
        {
            latestShockDay = Math.max(latestShockDay,p.getLastDayAMeaningfulChangeInWorkforceOccurred());
        }
        return latestShockDay;


    }

}
