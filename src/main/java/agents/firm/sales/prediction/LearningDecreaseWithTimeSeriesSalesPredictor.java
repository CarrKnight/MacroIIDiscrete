/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.sales.SalesDepartment;
import com.google.common.base.Preconditions;
import financial.Market;
import model.MacroII;
import model.utilities.filters.MovingAverage;
import model.utilities.stats.PeriodicMarketObserver;
import model.utilities.stats.regression.LinearRegression;
import model.utilities.stats.regression.MultipleLinearRegression;

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


    /**
     * The object running the regression
     */
    private final MultipleLinearRegression regression;

    /**
     * the object observing and memorizing
     */
    private final PeriodicMarketObserver observer;

    /**
     * the object we are going to feed with "learned" slope to make a prediction
     */
    private final FixedDecreaseSalesPredictor predictor;
    /**
     * whether it is going to be a WLS regression
     */
    private boolean usingWeights = true;

    /**
     * adds DeltaP as a regressor
     */
    private boolean correctingWithDeltaPrice = true;

    /**
     * The constructor that creates the linear regression, market observer and predictor
     * @param market the market we want to observe to learn from and eventually predict
     * @param macroII the model object, needed to schedule the days we are going to make observations
     */
    public LearningDecreaseWithTimeSeriesSalesPredictor(Market market, MacroII macroII)
    {
        this(new PeriodicMarketObserver(market,macroII));
    }

    /**
     * The constructor that gets the observer from "outside". Notice that the turnOff will close the periodicMarketObserver just the same
     * @param observer the observer object
     */
    public LearningDecreaseWithTimeSeriesSalesPredictor(PeriodicMarketObserver observer)
    {
        this.observer = observer;
        observer.setExact(true);


        regression = new MultipleLinearRegression();
        predictor = new FixedDecreaseSalesPredictor();
        predictor.setDecrementDelta(0); //initially stay flat

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

        if(observer.getNumberOfObservations() > 50)
        {
            updateModel();

            double intercept = - extractInterceptOfDemandFromRegression() / extractSlopeOfDemandFromRegression();
            double slope = 1d/ extractSlopeOfDemandFromRegression();
            System.out.println("q= " + extractInterceptOfDemandFromRegression() + " + p * " + extractSlopeOfDemandFromRegression());
            System.out.println("p= " + intercept + " + q * " + slope);
            predictor.setDecrementDelta((float) -slope);

            //return Math.round(intercept + slope * (dept.getMarket().getYesterdayVolume()+1));

        }


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

        if(observer.getNumberOfObservations() > 200)
        {
            updateModel();

            double intercept = - extractInterceptOfDemandFromRegression() / extractSlopeOfDemandFromRegression();
            double slope = 1d/ extractSlopeOfDemandFromRegression();
            System.out.println("q= " + extractInterceptOfDemandFromRegression() + " + p * " + extractSlopeOfDemandFromRegression());
            System.out.println("p= " + intercept + " + q * " + slope);
            predictor.setDecrementDelta((float) -slope);

            return Math.round(intercept + slope * (dept.getMarket().getYesterdayVolume()-1));

        }


        return predictor.predictSalePriceAfterDecreasingProduction(dept, expectedProductionCost, decreaseStep);
    }

    /**
     * Call this to kill the predictor
     */
    @Override
    public void turnOff() {
        predictor.turnOff();
        observer.turnOff();
    }

    /**
     * get the demand slope estimated by the time series. Need to call updateModel or predictPurchasePriceWhenIncreasingProduction first
     * @return
     */
    public double extractSlopeOfDemandFromRegression()
    {
        double[] coefficients = regression.getResultMatrix();
//        assert coefficients.length == 4;
        double alpha = coefficients[1];
        double gamma = coefficients[2];

        if(alpha != 0 && gamma != 0)
            return - gamma/alpha;
        else
            return 0;
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


    private void updateModel() {



        //grab the consumption
        double[] consumption = observer.getQuantitiesConsumedObservedAsArray();
        //grab production
        double[] production = observer.getQuantitiesProducedObservedAsArray();
        Preconditions.checkArgument(consumption != null && consumption.length >= 3); //there needs to be enough observations to do this!

        //create delta consumption and lagged consumption
        double[] laggedConsumption = new double[consumption.length-1];
        double[] deltaConsumption = new double[consumption.length-1];
        for(int i=1; i < consumption.length; i++)
        {
            laggedConsumption[i-1] = consumption[i-1];
            deltaConsumption[i-1] = consumption[i] - consumption[i-1];

        }
        //reality check
        assert laggedConsumption[0] == consumption[0]; //same starting point

        //now create price by lopping off the first element
        double[] todayPrice = new double[consumption.length-1];
        double[] price = observer.getPricesObservedAsArray();
        double[] deltaPrice = new double[consumption.length-1];
        for(int i=1; i<todayPrice.length+1; i++)
        {
            todayPrice[i-1]=price[i];
            deltaPrice[i-1] = price[i] - price[i-1];
        }
        assert todayPrice.length == laggedConsumption.length;

        //build weights
        double[] weights=null;
        if(usingWeights)
        {
            weights = new double[laggedConsumption.length];
            MovingAverage<Double> ma = new MovingAverage<>(5);
            for(int i=0; i < weights.length; i++)
            {
                ma.addObservation(Math.abs(deltaPrice[i]));
                weights[i] = 1/Math.exp(1 + ma.getSmoothedObservation());
            }
            weights[0]=weights[4];
            weights[1]=weights[4];
            weights[2]=weights[4];
            weights[3]=weights[4];
        }
        //done with that torture, just regress!

        //should I use deltaPrice?


        double[] clonedWeights = weights == null ? null : weights.clone();

        try{
        if(correctingWithDeltaPrice)
            regression.estimateModel(deltaConsumption.clone(),clonedWeights,laggedConsumption.clone(),todayPrice.clone(),deltaPrice.clone());
        else
            regression.estimateModel(deltaConsumption.clone(),clonedWeights,laggedConsumption.clone(),todayPrice.clone());
        }
        catch (LinearRegression.CollinearityException e)
        {
            //it must be that the deltaPrice were collinear, try again!
            try{
                regression.estimateModel(deltaConsumption.clone(),clonedWeights,laggedConsumption.clone(),todayPrice.clone());
            }
            catch (LinearRegression.CollinearityException ex)
            {
                //again collinear, that's impossiburu!
                throw new RuntimeException("Too many collinearities, I can't deal with this!");
            }
        }




        if(extractSlopeOfDemandFromRegression() == 0)
            System.err.println("nuuuu");



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
}
