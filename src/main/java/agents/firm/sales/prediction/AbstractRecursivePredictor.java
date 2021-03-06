
/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Doubles;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.Deactivatable;
import model.utilities.filters.ExponentialFilter;
import model.utilities.filters.MovingAverage;
import model.utilities.logs.*;
import model.utilities.stats.collectors.DataStorage;
import model.utilities.stats.regression.KalmanBasedRecursiveRegression;
import model.utilities.stats.regression.KalmanRecursiveRegression;
import model.utilities.stats.regression.RecursiveLinearRegression;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

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
 * @version 2013-11-12
 * @see
 */
public abstract class AbstractRecursivePredictor  implements Steppable, Deactivatable, LogNode
{

    public final static int defaultPriceLags = 0;

    public final static int defaultIndependentLags = 1;

    public final static int defaultMovingAverageSize = 1;

    /**
     * Where to write what we are actually regressing.
     */
    private BufferedWriter regressionInputsWriter;


    /**
     * activated only if independent lag is 1
     */
    private MovingAverage<Float> x[];



    private boolean usingWeights =true;

    public AbstractRecursivePredictor(MacroII model) {
        this(model,defaultPriceLags, defaultIndependentLags);
    }

    public AbstractRecursivePredictor(final MacroII model,
                                      int priceLags, int independentLags) {
        this(model,new double[independentLags +priceLags+1], priceLags, independentLags,defaultMovingAverageSize);

    }

    public AbstractRecursivePredictor(int priceLags,
                                      int independentLags, MacroII model, int timeDelay, int howFarIntoTheFutureToPredict) {
        this(model,new double[independentLags +priceLags+1], priceLags, independentLags,defaultMovingAverageSize);
        this.timeDelay = timeDelay;
        this.howFarIntoTheFutureToPredict = howFarIntoTheFutureToPredict;
    }

    public AbstractRecursivePredictor(final MacroII model,double[] initialCoefficients,
                                      int priceLags, int independentLags, int movingAverageSize) {
        Preconditions.checkState(priceLags + independentLags + 1 == initialCoefficients.length);
        this.model = model;
        this.priceLags=priceLags;
        this.independentLags = independentLags;
        this.regression =
    //                 new ExponentialForgettingRegressionDecorator(
                new KalmanRecursiveRegression(1+priceLags+ independentLags,initialCoefficients)
       //              ,.999d,.001) //very small scale forgetting. Good just to avoid getting stuck.
        ;
        //this.regression = new KalmanRecursiveRegression(1+priceLags+ independentLags,initialCoefficients);

        if(priceLags > 0) //if there a y lag in there
            this.regression.setBeta(1,1); // start with a simple prior y_t = y_{t-1}


        resetMovingAverages(movingAverageSize);



        //keep scheduling yourself until you aren't active anymore
        model.scheduleSoon(ActionOrder.PREPARE_TO_TRADE,this);




    }


    /**
     * the linear regression object we are going to update
     */
    private RecursiveLinearRegression regression;

    /**
     * how much time it takes for the dependent variable to affect price?
     */
    private int timeDelay = 0;

    /**
     * we don't really have a demand slope anymore, so we just simulate through the regression what the real values are
     */
    private int howFarIntoTheFutureToPredict = 100;

    private int numberOfValidObservations = 0;

    private int initialOpenLoopLearningTime=500;

    /**
     * the first burnoutPeriod observations are just ignored
     */
    private int burnoutPeriod = 0;

    /**
     * how many prices in the past are we going to visit?
     */
    private final int priceLags;

    /**
     * how many dependent lags in the past are we going to visit?
     */
    private final int independentLags;

    /**
     * the model to schedule yourself with
     */
    private final MacroII model;

    private boolean isActive = true;




    /**
     * if possible, add observation to the regression.
     * @param state
     */
    @Override
    public void step(SimState state) {
        if (!isActive)
            return;

        int minimumLookBackTime = Math.max(priceLags, independentLags-1 + timeDelay);

        //deltaPrice,clonedWeights,laggedPrice,laggedIndependentVariable
        //don't bother if there are not enough observations
        DataStorage data = getData();
        if (hasDepartmentTradedAtLeastOnce() && data.numberOfObservations() >minimumLookBackTime) {
            int yesterday = (int) Math.round(model.getMainScheduleTime()) - 2;
            int today = yesterday + 1;


            //check the y
            Enum priceVariable = getYVariableType();
            double price = data.getLatestObservation(priceVariable);
            assert price == data.getObservationRecordedThisDay(priceVariable, today);
            if (price >= 0 &&
                    (burnoutPeriod == 0 ||data.getObservationRecordedThisDay(priceVariable,yesterday+1-burnoutPeriod) > 0)) { //y has been positive for at least burnoutPeriod days

                //gather x with all the lags
                double[] laggedIndependentVariable;

                laggedIndependentVariable = data.getObservationsRecordedTheseDays(getXVariableType(),
                        today - timeDelay - independentLags + 1, today - timeDelay);

                assert timeDelay > 0 || laggedIndependentVariable[laggedIndependentVariable.length-1] == data.getLatestObservation(getXVariableType());
                assert laggedIndependentVariable.length == independentLags;

                if (containsNoNegatives(laggedIndependentVariable)) {
                    double[] laggedPrice=null;

                    if(priceLags >0)
                    {
                        //gather all the y lags
                        laggedPrice = data.getObservationsRecordedTheseDays(priceVariable,
                                yesterday - priceLags + 1, yesterday);
                        assert laggedPrice.length == priceLags;
                    }

                    double[] gaps =  data.getObservationsRecordedTheseDays(getDisturbanceType(),
                            today - Math.max(priceLags, independentLags + timeDelay)+1, today);
                    ExponentialFilter<Double> ma = new ExponentialFilter< >(gaps.length);



                    if ((laggedPrice == null || containsNoNegatives(laggedPrice)) ) {
                        //observation is: Intercept, oldest y,....,newest y,oldest x,....,newest Y
                        for(double gap : gaps)
                            ma.addObservation(gap);
                        //   sumOfGaps = sumOfGaps/gaps.length;
                        double gap = ma.getSmoothedObservation();


                        double weight;
                        if(usingWeights)
                            weight = 2/(1+Math.exp(Math.abs(gap)));
                        else
                            weight=1;

                        double originalX = laggedIndependentVariable[0];

                        //smooth x
                        for(int i=0; i<x.length; i++)
                        {
                            x[i].addObservation(laggedIndependentVariable[i]);
                            laggedIndependentVariable[i] = x[i].getSmoothedObservation();
                        }


                        double[] observation;
                        if(laggedPrice != null)
                            observation = Doubles.concat(new double[]{1}, laggedPrice, laggedIndependentVariable);
                        else
                            observation = Doubles.concat(new double[]{1},laggedIndependentVariable);

                        if(regressionInputsWriter != null)
                            try {
                                regressionInputsWriter.write(price + "," +weight + "," +observation[1] +"," + originalX);
                                regressionInputsWriter.newLine();
                                regressionInputsWriter.flush();
                                System.out.println(Arrays.toString(regression.getBeta()));

                            } catch (IOException e) {
                                e.printStackTrace();
                            }



                        //add it to the regression (DeltaP ~ 1 + laggedP + laggedX)
                        if(!usingWeights || weight > .001)
                        {
                            handleNewEvent( new LogEvent(this,LogLevel.DEBUG,"{} predictor regressing {} over observation: {} and weight: {} \n {}",
                                    this.getClass(),price, Arrays.toString(observation),weight,regression));



                            regression.addObservation(weight, price, observation);
                            numberOfValidObservations++;





                        }










                    }
                }
            }
            //    Preconditions.checkState(!Double.isNaN(regression.getBetas()[1]));

        }


        //reschedule!
        model.scheduleTomorrow(ActionOrder.PREPARE_TO_TRADE, this);
    }

    public abstract Enum getDisturbanceType();

    public abstract Enum getXVariableType();

    public abstract Enum getYVariableType();

    public abstract DataStorage getData();

    public abstract boolean hasDepartmentTradedAtLeastOnce();

    public static boolean containsNoNegatives(double[] array)
    {

        return everyElementAboveThis(array, 0);


    }

    public static boolean everyElementAboveThis(double[] array, float eachElementMustBeAboveThisNumber)
    {

        for(Double d : array)
            if(d < eachElementMustBeAboveThisNumber)
                return false;
        return true;

    }

    /**
     * Call this to kill the predictor
     */
    @Override
    public void turnOff() {
        isActive = false;
        logNode.turnOff();
    }


    public double predictPrice(int step)
    {
        if(priceLags == 0) //if it's not a time series don't bother simulating, just fill the x with today observation and be done with it.
            return predictPrice(step,2);
        else
            return predictPrice(step,howFarIntoTheFutureToPredict);
    }

    /**
     * never returns a negative
     */
    public double predictPrice(int step, int stepsInTheFuture)
    {
        //with no valid observations, what's the point?
        DataStorage data = getData();
        if(numberOfValidObservations < initialOpenLoopLearningTime )
            return defaultPriceWithNoObservations();
        Enum xType = getXVariableType();

        //the coefficients of the model
        double[] coefficients = regression.getBeta();

        //if we are regressing on workers, ignore the size of the step
        step = modifyStepIfNeeded(step);

        return Math.max(simulateFuturePrice(data,model,priceLags, independentLags,timeDelay,xType,getYVariableType(),
                stepsInTheFuture,coefficients,step),0);

    }

    abstract public int modifyStepIfNeeded(int step);

    abstract public double defaultPriceWithNoObservations();

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
    public static double simulateFuturePrice(DataStorage departmentData, MacroII model,int priceLags, int indepedentLags, int timeDelay,
                                             Enum xType, Enum yType, int howFarIntoTheFutureToPredict, double[] coefficients, int step)
    {


        //set up the data for simulation
        int yesterday = (int) (Math.round(model.getMainScheduleTime()) - 1);
        // int today = yesterday+1;
        Deque<Double> prices = new LinkedList<>();
        if(priceLags >0)
        {
            double[] pricesArray = departmentData.getObservationsRecordedTheseDays(yType,yesterday-priceLags+1, yesterday);      //+1 because it's inclusive
            for(Double price : pricesArray)
            {
                prices.addLast(price);
            }
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
            if(!prices.isEmpty())
            {
                prices.removeFirst();
                prices.addLast(newPrice);
            }
            if(!comingX.isEmpty())
                simulatedX.addLast(comingX.removeFirst());
            else
                simulatedX.addLast(futureX);
            simulatedX.removeFirst();

        }
        return newPrice;
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

    public int getInitialOpenLoopLearningTime() {
        return initialOpenLoopLearningTime;
    }

    public void setInitialOpenLoopLearningTime(int initialOpenLoopLearningTime) {
        this.initialOpenLoopLearningTime = initialOpenLoopLearningTime;
    }

    public int getPriceLags() {
        return priceLags;
    }

    public int getIndependentLags() {
        return independentLags;
    }

    public int getNumberOfValidObservations() {
        return numberOfValidObservations;
    }


    public int getBurnoutPeriod() {
        return burnoutPeriod;
    }

    public void setBurnoutPeriod(int burnoutPeriod) {
        this.burnoutPeriod = burnoutPeriod;
    }

    public double[] getBeta() {
        return regression.getBeta().clone();
    }

    public RecursiveLinearRegression getRegression() {
        return regression;
    }


    public boolean isUsingWeights() {
        return usingWeights;
    }

    public void setUsingWeights(boolean usingWeights) {
        this.usingWeights = usingWeights;
    }

    public void setRegression(KalmanBasedRecursiveRegression regression) {
        this.regression = regression;
    }

    final
    public void resetMovingAverages(int movingAverageSize)
    {
        x = new MovingAverage[independentLags];
        for(int i=0; i<x.length; i++)
            x[i] = new MovingAverage<>(movingAverageSize);
    }


    public void logRegressionInput(Path pathToFile) throws IOException {
        Files.deleteIfExists(pathToFile);
        regressionInputsWriter = Files.newBufferedWriter(pathToFile, StandardOpenOption.CREATE_NEW);
        regressionInputsWriter.write("y,weight,x,originalX");
        regressionInputsWriter.newLine();
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
