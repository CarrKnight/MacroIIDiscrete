/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.prediction;

import agents.firm.Department;
import com.google.common.primitives.Ints;
import model.utilities.Deactivatable;
import model.utilities.logs.*;
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
public abstract class AbstractWorkerLearningPredictor implements LogNode, Deactivatable{

    /**
     * The object running the regression
     */
    protected final LinearRegression regression;


    static public int defaultHowManyDaysOnAverageToSample = 17;

    private int howManyDaysOnAverageToSample = defaultHowManyDaysOnAverageToSample;

    static public int defaultMaximumDaysToLookBack = 623;
    /**
     * maximum number of days BEHIND THE LAST SHOCK DAY to examine
     */
    private int maximumDaysToLookBack = defaultMaximumDaysToLookBack;

    static public int  defaultMaximumDaysToLookForward = 330;

    private int maximumDaysToLookForward= defaultMaximumDaysToLookForward;



    private final int defaultHowManyShockDaysBackToLookFor = 10;

    private int howManyShockDaysBackToLookFor = defaultHowManyShockDaysBackToLookFor;



    /**
     * remember the last day you ran a regression so you don't run it multiple times the same day!
     *
     */
    private int lastRegressionDay = -1;


    public AbstractWorkerLearningPredictor() {
        regression = new LinearRegression();

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
        int unsampledDays = 0;
 //       float probability = 1f / howManyDaysOnAverageToSample;
        ArrayList<Integer> daysToObserve = new ArrayList<>();

        int lowerlimit = 0;
        int oldestShockDay = findOldestShockDay();

        if(now-department.getStartingDay() > maximumDaysToLookBack) {
            lowerlimit = oldestShockDay - maximumDaysToLookBack;
        }

        int upperLimit = Math.min(now,findLatestShockDay()+maximumDaysToLookForward);

        for(int i=upperLimit; i>=Math.max(department.getStartingDay(), lowerlimit); i--) {
            unsampledDays++;
            if(howManyDaysOnAverageToSample <= unsampledDays)
            {
                daysToObserve.add(i);
                unsampledDays =0;
            }
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
     * this checks finds not the latest shockday but the most remote we are to look for, so for example 5 shockdays ago
     * if HowManyShockDaysBackToLookFor is set to 5!
     * @return
     */
    abstract protected int findOldestShockDay();

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


    public int getMaximumDaysToLookForward() {
        return maximumDaysToLookForward;
    }

    public void setMaximumDaysToLookForward(int maximumDaysToLookForward) {
        this.maximumDaysToLookForward = maximumDaysToLookForward;
    }

    public int getHowManyShockDaysBackToLookFor() {
        return howManyShockDaysBackToLookFor;
    }

    public void setHowManyShockDaysBackToLookFor(int howManyShockDaysBackToLookFor) {
        this.howManyShockDaysBackToLookFor = howManyShockDaysBackToLookFor;
    }

    @Override
    public void turnOff() {
        logNode.turnOff();
    }

    /***
     *       __
     *      / /  ___   __ _ ___
     *     / /  / _ \ / _` / __|
     *    / /__| (_) | (_| \__ \
     *    \____/\___/ \__, |___/
     *                |___/
     */

    /**
     * simple lognode we delegate all loggings to.
     */
    private final LogNodeSimple logNode = new LogNodeSimple();

    @Override
    public boolean addLogEventListener(LogListener toAdd) {
        return logNode.addLogEventListener(toAdd);
    }

    @Override
    public boolean removeLogEventListener(LogListener toRemove) {
        return logNode.removeLogEventListener(toRemove);
    }

    @Override
    public void handleNewEvent(LogEvent logEvent)
    {
        logNode.handleNewEvent(logEvent);
    }

    @Override
    public boolean stopListeningTo(Loggable branch) {
        return logNode.stopListeningTo(branch);
    }

    @Override
    public boolean listenTo(Loggable branch) {
        return logNode.listenTo(branch);
    }
}
