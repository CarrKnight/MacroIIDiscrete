/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.sales.SalesDepartment;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Doubles;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.stats.collectors.SalesData;
import model.utilities.stats.collectors.enums.SalesDataType;
import model.utilities.stats.regression.RecursiveLinearRegression;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.Deque;
import java.util.LinkedList;

/**
 * <h4>Description</h4>
 * <p/> A simple recursive, every X days linear regression
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-11-10
 * @see
 */
public class RecursiveSalePredictor implements SalesPredictor, Steppable {


    /**
     * whether our x is workers or outflow
     */
    boolean regressingOnWorkers = false;

    /**
     * the linear regression object we are going to update
     */
    private final RecursiveLinearRegression regression;

    private boolean isActive = true;

    /**
     * the purchase department we are predicting for
     */
    final private SalesDepartment department;

    /**
     * how many prices in the past are we going to visit?
     */
    private final int priceLags;

    /**
     * how many dependent lags in the past are we going to visit?
     */
    private final int indepedentLags;

    private final MacroII model;

    /**
     * how much time it takes for the dependent variable to affect price?
     */
    private int timeDelay = 1;

    /**
     * we don't really have a demand slope anymore, so we just simulate through the regression what the real values are
     */
    private int howFarIntoTheFutureToPredict = 100;

    private int numberOfValidObservations = 0;

    public RecursiveSalePredictor(MacroII model, SalesDepartment department) {
        this(model, department,7,7);
    }

    public RecursiveSalePredictor(final MacroII model, final SalesDepartment department,
                                  int priceLags, int indepedentLags) {
        this(model, department,new double[indepedentLags+priceLags+1], priceLags, indepedentLags);

    }

    public RecursiveSalePredictor(SalesDepartment department, int priceLags,
                                  int indepedentLags, MacroII model, int timeDelay, int howFarIntoTheFutureToPredict) {
        this(model, department,new double[indepedentLags+priceLags+1], priceLags, indepedentLags);
        this.timeDelay = timeDelay;
        this.howFarIntoTheFutureToPredict = howFarIntoTheFutureToPredict;
    }

    public RecursiveSalePredictor(final MacroII model, final SalesDepartment department,double[] initialCoefficients,
                                  int priceLags, int indepedentLags) {
        Preconditions.checkState(priceLags+indepedentLags+1 == initialCoefficients.length);
        this.department = department;
        this.model = model;
        this.priceLags=priceLags;
        this.indepedentLags=indepedentLags;
        this.regression = new RecursiveLinearRegression(1+priceLags+indepedentLags,initialCoefficients);

        //keep scheduling yourself until you aren't active anymore
        model.scheduleSoon(ActionOrder.PREPARE_TO_TRADE,this);

    }

    /**
     * if possible, add observation to the regression.
     * @param state
     */
    @Override
    public void step(SimState state) {
        if (!isActive)
            return;

        int minimumLookBackTime = Math.max(priceLags, indepedentLags + timeDelay);

        //deltaPrice,clonedWeights,laggedPrice,laggedIndependentVariable
        //don't bother if there are not enough observations
        if (department.getData().numberOfObservations() >minimumLookBackTime) {
            int yesterday = (int) Math.round(model.getMainScheduleTime()) - 2;


            //check the y
            double price = department.getData().getLatestObservation(SalesDataType.AVERAGE_CLOSING_PRICES);
            assert price == department.getData().getObservationRecordedThisDay(SalesDataType.AVERAGE_CLOSING_PRICES, yesterday + 1);
            if (price > 0) {

                //gather x with all the lags
                double[] laggedIndependentVariable;
                if (regressingOnWorkers)
                    laggedIndependentVariable = department.getData().getObservationsRecordedTheseDays(SalesDataType.WORKERS_PRODUCING_THIS_GOOD,
                            yesterday - timeDelay - indepedentLags + 1, yesterday - timeDelay);
                else
                    laggedIndependentVariable = department.getData().getObservationsRecordedTheseDays(SalesDataType.WORKERS_PRODUCING_THIS_GOOD,
                            yesterday - timeDelay - indepedentLags + 1, yesterday - timeDelay);
                assert laggedIndependentVariable.length == indepedentLags;

                if (containsNoNegatives(laggedIndependentVariable)) {

                    //gather all the y lags
                    double[] laggedPrice = department.getData().getObservationsRecordedTheseDays(SalesDataType.AVERAGE_CLOSING_PRICES,
                            yesterday - priceLags + 1, yesterday);
                    assert laggedPrice.length == priceLags;

                    double weight = 1;//2d / (1d + Math.exp(Math.abs(department.getData().getLatestObservation(SalesDataType.SUPPLY_GAP))));

                    if (containsNoNegatives(laggedIndependentVariable)) {
                        //observation is: Intercept, oldest y,....,newest y,oldest x,....,newest Y
                        double[] observation = Doubles.concat(new double[]{1}, laggedPrice, laggedIndependentVariable);
                        //add it to the regression (DeltaP ~ 1 + laggedP + laggedX)
                        regression.addObservation(weight, price, observation);

                        numberOfValidObservations++;

                    }
                }
            }
            //    Preconditions.checkState(!Double.isNaN(regression.getBeta()[1]));

        }
        //reschedule!
        model.scheduleTomorrow(ActionOrder.PREPARE_TO_TRADE, this);
    }

    public double predictPrice(int step)
    {
       return predictPrice(step,howFarIntoTheFutureToPredict);

    }

    public double predictPrice(int step, int stepsInTheFuture)
    {
        //with no valid observations, what's the point?
        if(numberOfValidObservations < 1 || department.numberOfObservations() < 2 + Math.max(priceLags, indepedentLags + timeDelay) )
            return department.hypotheticalSalePrice();
        SalesDataType xType = regressingOnWorkers ? SalesDataType.WORKERS_PRODUCING_THIS_GOOD : SalesDataType.OUTFLOW;

        //the coefficients of the model
        double[] coefficients = regression.getBeta();

        //if we are regressing on workers, ignore the size of the step
        if(regressingOnWorkers)
            step = Integer.signum(step);

        return simulateFuturePrice(department.getData(),model,priceLags,indepedentLags,timeDelay,xType,stepsInTheFuture,coefficients,step);

    }

    /**
     * made static mostly for ease of testing. It just simulate a number of future steps of the time price time series
     * @param departmentData a link to the department data, to analyze properly
     * @param model a link to the model, to check the time only
     * @param priceLags how many lags of Y are we examining
     * @param indepedentLags how many lags of X are we examining
     * @param timeDelay how much time delay for X?
     * @param xType what kind of SalesData type is X?
     * @param howFarIntoTheFutureToPredict how far into the future to simulate?
     * @param coefficients what are the regression coefficients?
     * @param step how much to increase/decrease X?
     * @return the future price
     */
    public static double simulateFuturePrice(SalesData departmentData, MacroII model,int priceLags, int indepedentLags, int timeDelay,
                                             SalesDataType xType, int howFarIntoTheFutureToPredict, double[] coefficients, int step)
    {


        //set up the data for simulation
        int yesterday = (int) (Math.round(model.getMainScheduleTime()) - 1);
        double[] pricesArray = departmentData.getObservationsRecordedTheseDays(SalesDataType.AVERAGE_CLOSING_PRICES,yesterday-priceLags+1, yesterday);      //+1 because it's inclusive
        Deque <Double> prices = new LinkedList<>();
        for(Double price : pricesArray)
        {
            prices.addLast(price);
        }

        //x is a little bit different because, due to the lag, there are still some observations between the old and the simulated ones
        double lastX = departmentData.getLatestObservation(xType);
        double futureX = lastX + step;
        double[] oldXs = departmentData.getObservationsRecordedTheseDays(xType,
                yesterday-indepedentLags- timeDelay+1, yesterday- timeDelay);     //+1 because it's inclusive
        Deque <Double> simulatedX = new LinkedList<>();
        for(double oldX : oldXs)
        {
            simulatedX.addLast(oldX);
        }
        Deque<Double> comingX = new LinkedList<>();
        if(timeDelay>0)
        {
            double[] xToCome = departmentData.getObservationsRecordedTheseDays(xType,
                    yesterday - timeDelay+1, yesterday);
            assert xToCome.length == timeDelay;
            for(double coming : xToCome)
                comingX.addLast(coming);
        }



        //observation is: Intercept, oldest y,....,newest y,oldest x,....,newest Y
        //compute new price
        double newPrice=0;
        //simulate these many time steps
        for(int future=0; future<howFarIntoTheFutureToPredict; future++)
        {
            assert prices.size() + simulatedX.size() + 1 == coefficients.length;


            //reset at intercept
            newPrice = coefficients[0];

            int i=1;
            //price lags
            for(Double priceLag : prices)
            {
                newPrice+= coefficients[i] * priceLag;
                i++;
            }

            //now do the same for x lags
            for(Double xLag : simulatedX)
            {
                newPrice += coefficients[i] * xLag;
                i++;
            }
            //now update the lists
            prices.removeFirst(); prices.addLast(newPrice);
            if(!comingX.isEmpty())
                simulatedX.addLast(comingX.removeFirst());
            else
                simulatedX.addLast(futureX);
            simulatedX.removeFirst();

        }
        return newPrice;
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
    public long predictSalePriceAfterIncreasingProduction(SalesDepartment dept, long expectedProductionCost, int increaseStep) {
        return (long) Math.round(predictPrice(increaseStep));

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
        return (long) Math.round(predictPrice(-decreaseStep));

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
        return (long) Math.round(predictPrice(0));
    }

    /**
     * Call this to kill the predictor
     */
    @Override
    public void turnOff() {
        isActive = false;
    }



    public static boolean containsNoNegatives(double[] array)
    {

        for(Double d : array)
            if(d < 0)
                return false;
        return true;

    }

    public int getTimeDelay() {
        return timeDelay;
    }

    public void setTimeDelay(int timeDelay) {
        this.timeDelay = timeDelay;
    }

    public int getHowFarIntoTheFutureToPredict() {
        return howFarIntoTheFutureToPredict;
    }

    public void setHowFarIntoTheFutureToPredict(int howFarIntoTheFutureToPredict) {
        this.howFarIntoTheFutureToPredict = howFarIntoTheFutureToPredict;
    }

    public int getNumberOfValidObservations() {
        return numberOfValidObservations;
    }

    public boolean isRegressingOnWorkers() {
        return regressingOnWorkers;
    }

    public void setRegressingOnWorkers(boolean regressingOnWorkers) {
        this.regressingOnWorkers = regressingOnWorkers;
    }

    /**
     * simulates the decrement delta
     * @return
     */
    public double getDecrementDelta()
    {

        return -(predictPrice(1)-predictPrice(0));
    }

}
