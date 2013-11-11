/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.sales.SalesDepartment;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import model.utilities.filters.MovingAverage;
import model.utilities.stats.collectors.enums.SalesDataType;
import model.utilities.stats.regression.LinearRegression;
import model.utilities.stats.regression.MultipleLinearRegression;

import java.util.ArrayList;
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
 * @version 2013-08-07
 * @see
 */
public class LearningDecreaseWithTimeSeriesSalesPredictor implements SalesPredictor {


    //{usingWeights=false, correctingWithDeltaPrice=false, regressingOnWorkers=true,
    // howManyDaysForwardToLook=223, howManyDaysBackToLook=1409, oneObservationEveryHowManyDays=1,
    // howManyShockDaysBackToLookFor=16, dead time=109}

    /**
     * The object running the regression
     */
    private final MultipleLinearRegression regression;


    /**
     * the object we are going to feed with "learned" slope to make a prediction
     */
    private final FixedDecreaseSalesPredictor predictor;
    /**
     * whether it is going to be a WLS regression
     */
    private boolean usingWeights = false;

    /**
     * adds DeltaP as a regressor
     */
    private boolean correctingWithDeltaPrice = false;

    /**
     * the quantity is usually outflow, unless this is set to true
     */
    private boolean regressingOnWorkersRatherThanSales = true;

    private int howManyDaysForwardToLook = 223;

    private int howManyDaysBackToLook = 1400;

    private int oneObservationEveryHowManyDays = 1;

    private int howManyShockDaysBackToLookFor = 16;

    /**
     * how far back should we look?
     */
    private int deadTime = 109;

    private int lastRegressionDay = -1;

    /**
     * flag that is set to true when the last linear regression was collinear and an exception was thrown
     */
    private boolean wasCollinear = false;

    /**
     * when this is set to true, the regression inertia was 1 so we needed to run a simple y~x
     */
    private boolean correctedRegression = false;



    public static int defaultInitialDecrementDelta = 0;



    /**
     * The constructor that gets the observer from "outside". Notice that the turnOff will close the periodicMarketObserver just the same
     */
    public LearningDecreaseWithTimeSeriesSalesPredictor()
    {


        regression = new MultipleLinearRegression();
        predictor = new FixedDecreaseSalesPredictor();
        predictor.setDecrementDelta(defaultInitialDecrementDelta); //initially stay flat

    }



    public LearningDecreaseWithTimeSeriesSalesPredictor(boolean usingWeights, boolean correctingWithDeltaPrice, boolean regressingOnWorkersRatherThanSales,
                                                        int howManyDaysForwardToLook, int howManyDaysBackToLook, int oneObservationEveryHowManyDays,
                                                        int howManyShockDaysBackToLookFor) {
        this();
        this.usingWeights = usingWeights;
        this.correctingWithDeltaPrice = correctingWithDeltaPrice;
        this.regressingOnWorkersRatherThanSales = regressingOnWorkersRatherThanSales;
        this.howManyDaysForwardToLook = howManyDaysForwardToLook;
        this.howManyDaysBackToLook = howManyDaysBackToLook;
        this.oneObservationEveryHowManyDays = oneObservationEveryHowManyDays;
        this.howManyShockDaysBackToLookFor = howManyShockDaysBackToLookFor;
    }

    /**
     * This is called by the firm when it wants to predict the price they can sell to
     * (usually in order to guide production). <br>
     *
     *
     * @param dept                   the sales department that has to answer this question
     * @param expectedProductionCost the HQ estimate of costs in producing whatever it wants to sell. It isn't necesarilly used.
     * @param increaseStep
     * @return the best offer available/predicted or -1 if there are no quotes/good predictions
     */
    @Override
    public long predictSalePriceAfterIncreasingProduction(SalesDepartment dept, long expectedProductionCost, int increaseStep) {
        Preconditions.checkArgument(increaseStep >= 0);

        if(dept.numberOfObservations() > 200 &&
                dept.getFirm().getLatestDayWithMeaningfulWorkforceChangeInProducingThisGood(dept.getGoodType()) > 0 )
            try {
                updateModel(dept);

                if(regression.getResultMatrix() != null)
                {
                    double slope =  extractSlopeOfDemandFromRegression();
                    //      System.out.println("q= " + extractInterceptOfDemandFromRegression() + " + p * " + extractSlopeOfDemandFromRegression());
                    //     System.out.println("p= " + intercept + " + q * " + slope);
                    predictor.setDecrementDelta((float) -slope);
                }


            } catch (LinearRegression.CollinearityException e) {
                wasCollinear = true;

            }

        if(regressingOnWorkersRatherThanSales)
            increaseStep = 1;
        return predictor.predictSalePriceAfterIncreasingProduction(dept, expectedProductionCost, increaseStep);
    }

    /**
     * This is called by the firm when it wants to predict the price they can sell to if they increase production
     *
     *
     * @param dept                   the sales department that has to answer this question
     * @param expectedProductionCost the HQ estimate of costs in producing whatever it wants to sell. It isn't necesarilly used.
     * @param decreaseStep
     * @return the best offer available/predicted or -1 if there are no quotes/good predictions
     */
    @Override
    public long predictSalePriceAfterDecreasingProduction(SalesDepartment dept, long expectedProductionCost, int decreaseStep)
    {
        Preconditions.checkArgument(decreaseStep >= 0);

        if(dept.numberOfObservations() > 200 &&
                dept.getFirm().getLatestDayWithMeaningfulWorkforceChangeInProducingThisGood(dept.getGoodType()) > 0)
            try{
                updateModel(dept);
                if(regression.getResultMatrix() != null)
                {
                    double slope = extractSlopeOfDemandFromRegression();
          //           System.out.println("p= " + extractInterceptOfDemandFromRegression() + " + q * " + slope);
                    //     System.out.println("p= " + intercept + " + q * " + slope);
                    predictor.setDecrementDelta((float) -slope);
                }

            } catch (LinearRegression.CollinearityException e)
            {
                wasCollinear = true;

            }


        if(regressingOnWorkersRatherThanSales)
            decreaseStep = 1;
        return predictor.predictSalePriceAfterDecreasingProduction(dept, expectedProductionCost, decreaseStep);
    }

    /**
     * Call this to kill the predictor
     */
    @Override
    public void turnOff() {
        predictor.turnOff();
    }

    /**
     * get the demand slope estimated by the time series. Need to call updateModel or predictPurchasePriceWhenIncreasingProduction first
     * @return
     */
    public double extractSlopeOfDemandFromRegression()
    {
//        regression.estimateModel(deltaPrice,clonedWeights,laggedPrice,laggedIndependentVariable);

        if(!correctedRegression)
        {
        double[] coefficients = regression.getResultMatrix();
//        assert coefficients.length == 4;
        double alpha = coefficients[1];
        double beta = coefficients[2];

        if(alpha != 0 && beta != 0)
            return - beta/alpha;
        else
            return 0;
        }
        else
        {
            return regression.getResultMatrix()[1];
        }
    }

    /**
     * get the demand slope estimated by the time series. Need to call updateModel or predictPurchasePriceWhenIncreasingProduction first

     * @return
     */
    public double extractInterceptOfDemandFromRegression()
    {
        double[] coefficients = regression.getResultMatrix();
        //       assert coefficients.length == 4;
        double alpha = coefficients[1];
        double beta = coefficients[0];

        if(alpha != 0 && beta != 0)
            return - beta/alpha;
        else
            return 0;
    }


    /**
     * which days to observe!
     * @param department  the department to observe
     * @return an array containing days to observe
     */
    private int[] daysToSample(SalesDepartment department)
    {

        int now = department.getLastObservedDay();


        int unsampledDays = 0;
        //       float probability = 1f / howManyDaysOnAverageToSample;
        ArrayList<Integer> daysToObserve = new ArrayList<>();

        int lowerLimit = 0;
        int shockDay = findTheRightShockDay(department);

        if(now-department.getStartingDay() > howManyDaysBackToLook) {
            lowerLimit = shockDay - howManyDaysBackToLook;
        }

        int upperLimit = Math.min(now,findLatestShockDay(department)+howManyDaysForwardToLook);

        for(int i=upperLimit; i>=Math.max(department.getStartingDay(), lowerLimit); i--) {
            unsampledDays++;
            if(oneObservationEveryHowManyDays <= unsampledDays)
            {
                daysToObserve.add(i);
                unsampledDays =0;
            }
        }
        Collections.reverse(daysToObserve);


        return Ints.toArray(daysToObserve);


    }

    protected int findLatestShockDay(SalesDepartment department) {

        return department.getFirm().
                getLatestDayWithMeaningfulWorkforceChangeInProducingThisGood(department.getGoodType());
    }

    private int findTheRightShockDay(SalesDepartment department) {

        //get all shock days from firm
        List<Integer> shockDays =
                department.getFirm().getAllDayWithMeaningfulWorkforceChangeInProducingThisGood(department.getGoodType());
        Collections.sort(shockDays);
        Preconditions.checkState(shockDays.size() <=1 || shockDays.get(0) < shockDays.get(shockDays.size()-1));
        Preconditions.checkState(shockDays.size() > 0);

        if(shockDays.size() <= howManyShockDaysBackToLookFor)
            return shockDays.get(0);
        else
            return shockDays.get(shockDays.size()-howManyShockDaysBackToLookFor);


    }


    private void updateModel(SalesDepartment department) throws LinearRegression.CollinearityException {

        //we have run this regression already!
        if(lastRegressionDay == department.getLastObservedDay())
        {
            if(wasCollinear)
                throw new LinearRegression.CollinearityException();
            else
                return;
        }

        int days[] = daysToSample(department);
        if(days.length <40+deadTime)
            if(wasCollinear)
                throw new LinearRegression.CollinearityException();
            else
                return;



        //grab the consumption
        double[] independentVariable;
        if(regressingOnWorkersRatherThanSales)
            independentVariable = department.getObservationsRecordedTheseDays(SalesDataType.WORKERS_PRODUCING_THIS_GOOD,days);
        else
            independentVariable = department.getObservationsRecordedTheseDays(SalesDataType.OUTFLOW,days);
        //grab production
        Preconditions.checkArgument(independentVariable != null && independentVariable.length >= 3); //there needs to be enough observations to do this!

        //create delta consumption and lagged consumption
        int lag = deadTime;
        double[] laggedIndependentVariable = new double[independentVariable.length- lag];
        double[] todayIndependentVariable = new double[independentVariable.length-1];
        double[] deltaIndepentendVariable = new double[independentVariable.length- lag];
        for(int i= lag; i < independentVariable.length; i++)
        {
            todayIndependentVariable[i-1]=independentVariable[i];
            laggedIndependentVariable[i- lag] = independentVariable[i- lag];
            deltaIndepentendVariable[i- lag] = independentVariable[i] - independentVariable[i- lag];

        }
        //reality check
  //      assert laggedIndependentVariable[0] == independentVariable[0]; //same starting point

        //now create price by lopping off the first element
        double[] todayPrice = new double[independentVariable.length-1];
        double[] price = department.getObservationsRecordedTheseDays(SalesDataType.AVERAGE_CLOSING_PRICES, days);
        double[] laggedPrice = new double[independentVariable.length- lag];
        double[] deltaPrice = new double[independentVariable.length- lag];
        for(int i= lag; i<laggedPrice.length+ lag; i++)
        {
            todayPrice[i-1]=price[i];
            deltaPrice[i- lag] = price[i] - price[i- lag];
            laggedPrice[i- lag] = price[i- lag];

        }
        laggedPrice[0] = price[0];

        assert laggedPrice.length == todayIndependentVariable.length;

        //build weights
        double[] weights=null;
        if(usingWeights)
        {
            double[] supplyGap = department.getObservationsRecordedTheseDays(SalesDataType.SUPPLY_GAP,days);

            weights = new double[todayIndependentVariable.length];
            MovingAverage<Double> ma = new MovingAverage<>(5);
            for(int i=0; i < weights.length; i++)
            {
                ma.addObservation(Math.abs(supplyGap[i]));
                if(price[i]<=0)
                    weights[i]=0;
                else
                    weights[i] = 1d/(1d + Math.exp(Math.abs(ma.getSmoothedObservation())));
            }
            weights[0]=weights[4];
            weights[1]=weights[4];
            weights[2]=weights[4];
            weights[3]=weights[4];

            //now go through all the weights, make sure at least some are non 0
            int nonZeroWeights = 0;
            for(double weight: weights)
                if(Math.abs(weight)>.01d)
                    nonZeroWeights++;
            if(nonZeroWeights<40+lag)
                throw new LinearRegression.CollinearityException();


        }
        //done with that torture, just regress!

        //should I use deltaPrice?

        lastRegressionDay = department.getLastObservedDay();



        double[] clonedWeights = weights == null ? null : weights.clone();

        try{
            if(correctingWithDeltaPrice)
            {
                regression.estimateModel(deltaPrice,clonedWeights,laggedPrice,laggedIndependentVariable,deltaIndepentendVariable);
                wasCollinear = false;
                correctedRegression=false;

            }
            else{
                regression.estimateModel(deltaPrice,clonedWeights,laggedPrice,laggedIndependentVariable);
                wasCollinear = false;
                correctedRegression=false;


            }
        }
        catch (LinearRegression.CollinearityException e)
        {
            try{
                //it might be collinear because inertia is 1, so try:
                regression.estimateModel(laggedPrice,null,laggedIndependentVariable);
                wasCollinear = false;
                correctedRegression=true;


            }
            catch (LinearRegression.CollinearityException ex)
            {
                throw  ex;
            }

        }








    }


    /**
     * Gets whether it is going to be a WLS regression.
     *
     * @return Value of whether it is going to be a WLS regression.
     */
    public boolean isUsingWeights() {
        return usingWeights;
    }

    /**
     * Sets new whether it is going to be a WLS regression.
     *
     * @param usingWeights New value of whether it is going to be a WLS regression.
     */
    public void setUsingWeights(boolean usingWeights) {
        this.usingWeights = usingWeights;
    }


    /**
     * Gets adds DeltaP as a regressor.
     *
     * @return Value of adds DeltaP as a regressor.
     */
    public boolean isCorrectingWithDeltaPrice() {
        return correctingWithDeltaPrice;
    }

    /**
     * Sets new adds DeltaP as a regressor.
     *
     * @param correctingWithDeltaPrice New value of adds DeltaP as a regressor.
     */
    public void setCorrectingWithDeltaPrice(boolean correctingWithDeltaPrice) {
        this.correctingWithDeltaPrice = correctingWithDeltaPrice;
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

    public int getLastRegressionDay() {
        return lastRegressionDay;
    }

    public void setLastRegressionDay(int lastRegressionDay) {
        this.lastRegressionDay = lastRegressionDay;
    }

    public int getHowManyDaysForwardToLook() {
        return howManyDaysForwardToLook;
    }

    public void setHowManyDaysForwardToLook(int howManyDaysForwardToLook) {
        this.howManyDaysForwardToLook = howManyDaysForwardToLook;
    }

    public int getHowManyDaysBackToLook() {
        return howManyDaysBackToLook;
    }

    public void setHowManyDaysBackToLook(int howManyDaysBackToLook) {
        this.howManyDaysBackToLook = howManyDaysBackToLook;
    }

    public int getOneObservationEveryHowManyDays() {
        return oneObservationEveryHowManyDays;
    }

    public void setOneObservationEveryHowManyDays(int oneObservationEveryHowManyDays) {
        this.oneObservationEveryHowManyDays = oneObservationEveryHowManyDays;
    }

    public int getHowManyShockDaysBackToLookFor() {
        return howManyShockDaysBackToLookFor;
    }

    public void setHowManyShockDaysBackToLookFor(int howManyShockDaysBackToLookFor) {
        this.howManyShockDaysBackToLookFor = howManyShockDaysBackToLookFor;
    }

    /**
     * Gets by how much we decrease the price predicted in respect to current departmental price.
     *
     * @return Value of by how much we increase/decrease the price predicted in respect to current departmental price.
     */
    public float getDecrementDelta() {
        return predictor.getDecrementDelta();
    }

    public boolean isRegressingOnWorkersRatherThanSales() {
        return regressingOnWorkersRatherThanSales;
    }

    public int getDeadTime() {
        return deadTime;
    }

    public void setDeadTime(int deadTime) {
        this.deadTime = deadTime;
    }


}
