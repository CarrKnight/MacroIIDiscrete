package agents.firm.sales.prediction;

import Jama.Matrix;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Doubles;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.Deactivatable;
import model.utilities.filters.ExponentialFilter;
import model.utilities.stats.collectors.DataStorage;
import model.utilities.stats.regression.RecursiveLinearRegression;
import sim.engine.SimState;
import sim.engine.Steppable;

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
public abstract class AbstractRecursivePredictor  implements Steppable, Deactivatable
{

    public static int defaultPriceLags = 3;

    public static int defaultIndepedentLags = 3;


    private boolean usingWeights =true;

    public AbstractRecursivePredictor(MacroII model) {
        this(model,defaultPriceLags,defaultIndepedentLags);
    }

    public AbstractRecursivePredictor(final MacroII model,
                                      int priceLags, int independentLags) {
        this(model,new double[independentLags +priceLags+1], priceLags, independentLags);

    }

    public AbstractRecursivePredictor(int priceLags,
                                      int independentLags, MacroII model, int timeDelay, int howFarIntoTheFutureToPredict) {
        this(model,new double[independentLags +priceLags+1], priceLags, independentLags);
        this.timeDelay = timeDelay;
        this.howFarIntoTheFutureToPredict = howFarIntoTheFutureToPredict;
    }

    public AbstractRecursivePredictor(final MacroII model,double[] initialCoefficients,
                                      int priceLags, int independentLags) {
        Preconditions.checkState(priceLags + independentLags + 1 == initialCoefficients.length);
        this.model = model;
        this.priceLags=priceLags;
        this.independentLags = independentLags;
        this.regression = new RecursiveLinearRegression(1+priceLags+ independentLags,initialCoefficients);
        this.regression.setBeta(1,1); // start with a simple prior y_t = y_{t-1}

        //keep scheduling yourself until you aren't active anymore
        model.scheduleSoon(ActionOrder.PREPARE_TO_TRADE,this);

    }


    /**
     * the linear regression object we are going to update
     */
    private final RecursiveLinearRegression regression;

    /**
     * how much time it takes for the dependent variable to affect price?
     */
    private int timeDelay = 0;

    /**
     * we don't really have a demand slope anymore, so we just simulate through the regression what the real values are
     */
    private int howFarIntoTheFutureToPredict = 100;

    private int numberOfValidObservations = 0;

    private int initialOpenLoopLearningTime=100;

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
        if (data.numberOfObservations() >minimumLookBackTime) {
            int yesterday = (int) Math.round(model.getMainScheduleTime()) - 2;
            int today = yesterday + 1;


            //check the y
            Enum priceVariable = getYVariableType();
            double price = data.getLatestObservation(priceVariable);
            assert price == data.getObservationRecordedThisDay(priceVariable, today);
            if (price > 0 &&
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
                    ExponentialFilter<Double> ma = new ExponentialFilter<>(gaps.length);



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


                        double[] observation;
                        if(laggedPrice != null)
                            observation = Doubles.concat(new double[]{1}, laggedPrice, laggedIndependentVariable);
                        else
                            observation = Doubles.concat(new double[]{1},laggedIndependentVariable);
                        //add it to the regression (DeltaP ~ 1 + laggedP + laggedX)
                        regression.addObservation(weight, price, observation);

                        double mu = 0.0000001d;
                        Matrix matrix = new Matrix(regression.getpCovariance());
                        //matrix = matrix.plus(Matrix.identity(observation.length,observation.length).times(1000));
                        matrix = matrix.times(Matrix.identity(observation.length,observation.length).plus(matrix.times(mu)).inverse());
                        getRegression().setPCovariance(matrix.getArray());



                        //            System.out.println(matrix.trace());
                        //  matrix = matrix.minus(matrix.times(matrix).times(mu));
//                        System.out.println(matrix.trace());
                        //                    regression.setPCovariance(matrix.getArray());
                        //  getRegression().multiplyPMatrixByThis(1d/(1d+mu));
                        // getRegression().addNoise(1d/(1d+mu));

                        //add noise

                        numberOfValidObservations++;


                        if(numberOfValidObservations % 100 == 0)
                        {
                            if(this instanceof  RecursiveSalePredictor)
                                System.out.println( "sales slope: " + ( predictPrice(1) - predictPrice(0)) + " , trace: " + getRegression().getTrace());
                            else
                                System.out.println( "purchases slope: " + ( predictPrice(1) - predictPrice(0)) + " , trace: " + getRegression().getTrace());

                            //         matrix = matrix.plus(Matrix.identity(observation.length,observation.length).times(1000));
                        }



                    }
                }
            }
            //    Preconditions.checkState(!Double.isNaN(regression.getBeta()[1]));

        }


        //reschedule!
        model.scheduleTomorrow(ActionOrder.PREPARE_TO_TRADE, this);
    }

    public abstract Enum getDisturbanceType();

    public abstract Enum getXVariableType();

    public abstract Enum getYVariableType();

    public abstract DataStorage getData();

    public static boolean containsNoNegatives(double[] array)
    {

        return everyElementAboveThis(array, 0);


    }

    public static boolean everyElementAboveThis(double[] array, float eachElementMustBeAboveThisNumber)
    {

        for(Double d : array)
            if(d <= eachElementMustBeAboveThisNumber)
                return false;
        return true;

    }

    /**
     * Call this to kill the predictor
     */
    @Override
    public void turnOff() {
        isActive = false;
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

    public double getTrace() {
        return regression.getTrace();
    }

    public boolean isUsingWeights() {
        return usingWeights;
    }

    public void setUsingWeights(boolean usingWeights) {
        this.usingWeights = usingWeights;
    }
}
