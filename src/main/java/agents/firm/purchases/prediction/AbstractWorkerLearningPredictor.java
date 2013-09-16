/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.prediction;

import agents.firm.Department;
import com.google.common.primitives.Ints;
import model.MacroII;
import model.utilities.stats.regression.LinearRegression;

import java.util.ArrayList;
import java.util.Collections;

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
public abstract class AbstractWorkerLearningPredictor {

    /**
     * The object running the regression
     */
    protected final LinearRegression regression;




    private final MacroII model;


    private int howManyDaysOnAverageToSample = 3;

    /**
     * maximum number of days BEHIND THE LAST SHOCK DAY to examine
     */
    private int maximumDaysToLookBack = 300;


    /**
     * remember the last day you ran a regression so you don't run it multiple times the same day!
     *
     */
    private int lastRegressionDay = -1;


    public AbstractWorkerLearningPredictor(MacroII model) {
        regression = new LinearRegression();

        this.model = model;
    }


    /**
     * tries to run a regression if there is enough data
     * @param department  the department whosem memory we sample
     * @return true if we ran a new regression (can grab the regression straight from the protected regression object)
     */
    protected boolean buildModelAndUpdateSlope(Department department)
    {

        if(lastRegressionDay ==department.getLastObservedDay() ) //we already ran a regression today!
            return false;

        //grab the days to regress
        int days[] = daysToSample(department);

        if (days.length < 3) //don't bother with too few observations
            return false;

        double[] x = getXArray(days);
        double[] y = getYArray(days);
        double[] weights = buildWeights(days,x,y);

        int validWeights = 0;
        for(double weight : weights)
            if(weight > 0)
                validWeights++;

        if(validWeights < 3) //if there are less than 3 real observations, ignore again!
            return false;

        regression.estimateModel(x,y,weights);

        return true;

    }

    protected abstract double[] getYArray(int[] days);

    protected abstract double[] getXArray(int[] days);

    /**
     * which days to observe!
     * @param department  the department to observe
     * @return an array containing days to observe
     */
    private int[] daysToSample(Department department)
    {

        int now = department.getLastObservedDay();
        lastRegressionDay = now;
        float probability = 1f / howManyDaysOnAverageToSample;
        ArrayList<Integer> daysToObserve = new ArrayList<>();

        int lowerlimit = 0;
        int latestShockDay = findLatestShockDay();

        if(now-department.getStartingDay() > maximumDaysToLookBack) {
            lowerlimit = latestShockDay - maximumDaysToLookBack;
        }

        for(int i=now; i>=Math.max(department.getStartingDay(), lowerlimit); i--) {
            if(model.random.nextBoolean(probability))
                daysToObserve.add(i);

        }
        Collections.reverse(daysToObserve);


        return Ints.toArray(daysToObserve);


    }

    abstract protected double[] buildWeights(int[] days, double[] x, double[] y);



    /**
     * when was the last time a meaningful change in workers occurred
     * @return the day or -1 if there are none
     */
    abstract protected int findLatestShockDay();


    /**
     * Gets maximum number of days BEHIND THE LAST SHOCK DAY to examine.
     *
     * @return Value of maximum number of days BEHIND THE LAST SHOCK DAY to examine.
     */
    public int getMaximumDaysToLookBack() {
        return maximumDaysToLookBack;
    }

    /**
     * Sets new howManyDaysOnAverageToSample.
     *
     * @param howManyDaysOnAverageToSample New value of howManyDaysOnAverageToSample.
     */
    public void setHowManyDaysOnAverageToSample(int howManyDaysOnAverageToSample) {
        this.howManyDaysOnAverageToSample = howManyDaysOnAverageToSample;
    }

    /**
     * Gets howManyDaysOnAverageToSample.
     *
     * @return Value of howManyDaysOnAverageToSample.
     */
    public int getHowManyDaysOnAverageToSample() {
        return howManyDaysOnAverageToSample;
    }

    /**
     * Sets new maximum number of days BEHIND THE LAST SHOCK DAY to examine.
     *
     * @param maximumDaysToLookBack New value of maximum number of days BEHIND THE LAST SHOCK DAY to examine.
     */
    public void setMaximumDaysToLookBack(int maximumDaysToLookBack) {
        this.maximumDaysToLookBack = maximumDaysToLookBack;
    }
}
