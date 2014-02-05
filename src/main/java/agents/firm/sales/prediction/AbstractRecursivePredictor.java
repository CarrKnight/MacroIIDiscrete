
package agents.firm.sales.prediction;

import agents.firm.personell.HumanResources;
import agents.firm.purchases.prediction.OpenLoopRecursivePurchasesPredictor;
import agents.firm.purchases.prediction.SamplingLearningIncreasePurchasePredictor;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentAllAtOnce;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.pricing.pid.SmoothedDailyInventoryPricingStrategy;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Doubles;
import ec.util.MersenneTwisterFast;
import goods.GoodType;
import model.MacroII;
import model.scenario.MonopolistScenario;
import model.utilities.ActionOrder;
import model.utilities.Deactivatable;
import model.utilities.filters.ExponentialFilter;
import model.utilities.filters.MovingAverage;
import model.utilities.stats.collectors.DataStorage;
import model.utilities.stats.regression.ExponentialForgettingRegressionDecorator;
import model.utilities.stats.regression.GunnarsonRegularizerDecorator;
import model.utilities.stats.regression.KalmanRecursiveRegression;
import model.utilities.stats.regression.RecursiveLinearRegression;
import sim.engine.SimState;
import sim.engine.Steppable;

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
public abstract class AbstractRecursivePredictor  implements Steppable, Deactivatable
{

    public static int defaultPriceLags = 0;

    public static int defaultIndependentLags = 1;

    public static int defaultMovingAverageSize = 1;


    /**
     * activated only if independent lag is 1
     */
    private MovingAverage<Float> x[];

    /**
     * activated only if independent lag is 1
     */
    private MovingAverage<Float> y;


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
        this.regression = new GunnarsonRegularizerDecorator(
                new ExponentialForgettingRegressionDecorator(
                        new KalmanRecursiveRegression(1+priceLags+ independentLags,initialCoefficients)
                     //   ,.9999d));
                        ,.9995d ));

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

    private int initialOpenLoopLearningTime=50;

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

                        //smooth x and y
                        y.addObservation(price);
                        price = y.getSmoothedObservation();
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



                        //add it to the regression (DeltaP ~ 1 + laggedP + laggedX)
                        if(!usingWeights || weight > .001)
                        {
                            regression.addObservation(weight, price, observation);
                            numberOfValidObservations++;

                            /*
                            if(numberOfValidObservations % 1000 == 0){
                               if(this instanceof RecursiveSalePredictor)
                                    System.out.println("sales: " + Arrays.toString(regression.getBeta()));
                                else
                                if(this instanceof RecursivePurchasesPredictor && usingWeights)
                                    System.out.println("purchases: " + Arrays.toString(regression.getBeta()));


                            }
                            */

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

    public void setRegression(RecursiveLinearRegression regression) {
        this.regression = regression;
    }

    final
    public void resetMovingAverages(int movingAverageSize)
    {
        y = new MovingAverage<>(movingAverageSize);
        x = new MovingAverage[independentLags];
        for(int i=0; i<x.length; i++)
            x[i] = new MovingAverage<>(movingAverageSize);    }


    //test a few possible decorators to find the best one!
    // [4.0, 3.0, 9.0, 4.0, 36.0, 4.0, 58.0, 26.0]
    // [186.0, 535.0, 172.0, 150.0, 165.0, 288.0, 185.0, 264.0]

    public static void main(String[] args)
    {        //run the tests on failures first


        double[] firstErrors = new double[8];
        double[] secondErrors = new double[8];


        //run the test  times
        for(int i=50; i<155; i++)
        {

            for(int test=0;test<8; test++){

                MersenneTwisterFast random = new MersenneTwisterFast(i);


                final MacroII macroII = new MacroII(i);
                MonopolistScenario scenario1 = new MonopolistScenario(macroII);

                //generate random parameters for labor supply and good demand
                int p0= random.nextInt(100)+100; int p1= random.nextInt(3)+1;
                scenario1.setDemandIntercept(p0); scenario1.setDemandSlope(p1);
                int w0=random.nextInt(10)+10; int w1=random.nextInt(3)+1;
                scenario1.setDailyWageIntercept(w0); scenario1.setDailyWageSlope(w1);
                int a=random.nextInt(3)+1;
                scenario1.setLaborProductivity(a);



                macroII.setScenario(scenario1);
                scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
                scenario1.setWorkersToBeRehiredEveryDay(true);
                scenario1.setAskPricingStrategy(SmoothedDailyInventoryPricingStrategy.class);
                if(random.nextBoolean())
                    scenario1.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);
                else
                    scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);

                System.out.println(p0 + "," + p1 + "," + w0 + "," + w1 +"," + a);

                macroII.start();
                macroII.schedule.step(macroII);

                SalesDepartment department = scenario1.getMonopolist().getSalesDepartment(GoodType.GENERIC);
                SalesPredictor predictor = null;
                HumanResources hr = scenario1.getMonopolist().getHRs().iterator().next();


                switch (test)
                {
                    //Constant trace
                    default:
                    case 0:
                        System.out.println("OPENLOOP 99 regularized");
                        OpenLoopRecursiveSalesPredictor openLoop = new OpenLoopRecursiveSalesPredictor(macroII,department);
                        predictor = openLoop;
                        openLoop.setRegression(new GunnarsonRegularizerDecorator(new ExponentialForgettingRegressionDecorator(new KalmanRecursiveRegression(2),.99d)));

                        OpenLoopRecursivePurchasesPredictor openLoopHr = new OpenLoopRecursivePurchasesPredictor(macroII,hr);
                        openLoopHr.setRegression(new GunnarsonRegularizerDecorator(new ExponentialForgettingRegressionDecorator(new KalmanRecursiveRegression(2),.99d)));
                        hr.setPredictor(openLoopHr);


                        break;
                    //no forgetting, no regularization
                    case 1:
                        System.out.println("OPENLOOP 99.9");
                        openLoop = new OpenLoopRecursiveSalesPredictor(macroII,department);
                        predictor = openLoop;
                        openLoop.setRegression(new GunnarsonRegularizerDecorator(new ExponentialForgettingRegressionDecorator(new KalmanRecursiveRegression(2),.999d)));

                        openLoopHr = new OpenLoopRecursivePurchasesPredictor(macroII,hr);
                        openLoopHr.setRegression(new GunnarsonRegularizerDecorator(new ExponentialForgettingRegressionDecorator(new KalmanRecursiveRegression(2),.999d)));
                        hr.setPredictor(openLoopHr);

                        break;
                    //99% forgetting, no regularization
                    case 2:
                        System.out.println("OPENLOOP 98");
                        openLoop = new OpenLoopRecursiveSalesPredictor(macroII,department);
                        predictor = openLoop;
                        openLoop.setRegression(new GunnarsonRegularizerDecorator(new ExponentialForgettingRegressionDecorator(new KalmanRecursiveRegression(2),.98d)));

                        openLoopHr = new OpenLoopRecursivePurchasesPredictor(macroII,hr);
                        openLoopHr.setRegression(new GunnarsonRegularizerDecorator(new ExponentialForgettingRegressionDecorator(new KalmanRecursiveRegression(2),.98d)));
                        hr.setPredictor(openLoopHr);

                        break;
                    //99% forgetting AND regularization
                    case 3:
                        System.out.println("OPENLOOP 98.5");
                        openLoop = new OpenLoopRecursiveSalesPredictor(macroII,department);
                        predictor = openLoop;
                        openLoop.setRegression(new GunnarsonRegularizerDecorator(new ExponentialForgettingRegressionDecorator(new KalmanRecursiveRegression(2),.985d)));

                        openLoopHr = new OpenLoopRecursivePurchasesPredictor(macroII,hr);
                        openLoopHr.setRegression(new GunnarsonRegularizerDecorator(new ExponentialForgettingRegressionDecorator(new KalmanRecursiveRegression(2),.985d)));
                        hr.setPredictor(openLoopHr);

                        break;
                    //95% forgetting AND regularization
                    case 4:
                        System.out.println("OPENLOOP 97 regularized");
                        openLoop = new OpenLoopRecursiveSalesPredictor(macroII,department);
                        predictor = openLoop;
                        openLoop.setRegression(new GunnarsonRegularizerDecorator(new ExponentialForgettingRegressionDecorator(new KalmanRecursiveRegression(2),.97d)));

                        openLoopHr = new OpenLoopRecursivePurchasesPredictor(macroII,hr);
                        openLoopHr.setRegression(new GunnarsonRegularizerDecorator(new ExponentialForgettingRegressionDecorator(new KalmanRecursiveRegression(2),.97d)));
                        hr.setPredictor(openLoopHr);

                        break;
                    case 5:
                        System.out.println("OPENLOOP 99.5 regularized");
                        openLoop = new OpenLoopRecursiveSalesPredictor(macroII,department);
                        predictor = openLoop;
                        openLoop.setRegression(new GunnarsonRegularizerDecorator(new ExponentialForgettingRegressionDecorator(new KalmanRecursiveRegression(2),.995d)));

                        openLoopHr = new OpenLoopRecursivePurchasesPredictor(macroII,hr);
                        openLoopHr.setRegression(new GunnarsonRegularizerDecorator(new ExponentialForgettingRegressionDecorator(new KalmanRecursiveRegression(2),.995d)));
                        hr.setPredictor(openLoopHr);


                        break;
                    case 6:
                        System.out.println("OPENLOOP 96 regularized with time delay");
                        openLoop = new OpenLoopRecursiveSalesPredictor(macroII,department);
                        predictor = openLoop;
                        openLoop.setRegression(new GunnarsonRegularizerDecorator(new ExponentialForgettingRegressionDecorator(new KalmanRecursiveRegression(2),.96d)));

                        openLoopHr = new OpenLoopRecursivePurchasesPredictor(macroII,hr);
                        openLoopHr.setRegression(new GunnarsonRegularizerDecorator(new ExponentialForgettingRegressionDecorator(new KalmanRecursiveRegression(2),.96d)));
                        hr.setPredictor(openLoopHr);

                        break;
                    case 7:
                        System.out.println("Ye Olde Regression");
                        predictor = new SamplingLearningDecreaseSalesPredictor(macroII);

                        hr.setPredictor(new SamplingLearningIncreasePurchasePredictor(macroII));
                        break;




                }
                assert predictor != null;
                department.setPredictorStrategy(predictor);


                while(macroII.schedule.getTime()<5000)
                    macroII.schedule.step(macroII);

                //first checkpoint
                int profitMaximizingLaborForce = MonopolistScenario.findWorkerTargetThatMaximizesProfits(p0,p1,w0,w1,a);
                firstErrors[test] += Math.sqrt(Math.pow(scenario1.getMonopolist().getTotalWorkers()-profitMaximizingLaborForce,2));
                System.out.println(firstErrors[test]);


                //now reset
                //generate random parameters for labor supply and good demand
                p0= random.nextInt(100)+100; p1= random.nextInt(3)+1;
                scenario1.resetDemand(p0,p1);
                w0=random.nextInt(10)+10; w1=random.nextInt(3)+1;
                scenario1.resetLaborSupply(w0,w1);
                System.out.println(p0 + "," + p1 + "," + w0 + "," + w1 +"," + a);
                System.out.flush();


                //another 5000 observations
                while(macroII.schedule.getTime()<10000)
                    macroII.schedule.step(macroII);

                //the pi maximizing labor force employed is:




                profitMaximizingLaborForce = MonopolistScenario.findWorkerTargetThatMaximizesProfits(p0,p1,w0,w1,a);
                secondErrors[test] += Math.sqrt(Math.pow(scenario1.getMonopolist().getTotalWorkers()-profitMaximizingLaborForce,2));
                System.out.println(secondErrors[test]);



                System.out.println(i + "---------------------------------------------------------------------------------------------");

            }
        }

        System.out.println(Arrays.toString(firstErrors));
        System.out.println(Arrays.toString(secondErrors));







    }

}
