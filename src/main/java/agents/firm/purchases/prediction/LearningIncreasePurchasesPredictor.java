/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.prediction;

import agents.firm.purchases.PurchasesDepartment;
import financial.Market;
import model.MacroII;
import model.utilities.stats.PeriodicMarketObserver;
import model.utilities.stats.regression.LinearRegression;

/**
 * <h4>Description</h4>
 * <p/> This is the "inverse" of LearningDecreaseSalesPredictor. Observes periodically prices and quantities,
 * when needed runs a regression and uses that regression's slope as the learned "markup" that is going to cost to
 * increase consumption
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-08-04
 * @see
 */
public class LearningIncreasePurchasesPredictor implements PurchasesPredictor {

    /**
     * The object running the regression
     */
    private final LinearRegression regression;

    /**
     * the object observing and memorizing
     */
    private final PeriodicMarketObserver observer;

    /**
     * the object we are going to feed with "learned" slope to make a prediction
     */
    private final FixedIncreasePurchasesPredictor predictor;

    /**
     *  whether the OLS is weighted
     */
    private boolean usingWeights = true;


    /**
     * The constructor that creates the linear regression, market observer and predictor
     * @param market the market we want to observe to learn from and eventually predict
     * @param macroII the model object, needed to schedule the days we are going to make observations
     */
    public LearningIncreasePurchasesPredictor(Market market, MacroII macroII)
    {
        this(new PeriodicMarketObserver(market,macroII));
    }

    /**
     * The constructor that gets the observer from "outside". Notice that the turnOff will close the periodicMarketObserver just the same
     * @param observer the observer object
     */
    public LearningIncreasePurchasesPredictor(PeriodicMarketObserver observer)
    {
        this.observer = observer;
        regression = new LinearRegression();
        predictor = new FixedIncreasePurchasesPredictor();
        predictor.setIncrementDelta(0); //initially stay flat

    }


    private void updateModel()
    {

        assert observer.getNumberOfObservations() > 0;

        //update the regression
        double[] weights = usingWeights ? buildWeights() : null;
        assert !usingWeights || weights.length ==  observer.getNumberOfObservations();

        regression.estimateModel(observer.getQuantitiesProducedObservedAsArray(),observer.getPricesObservedAsArray(),
                weights);



    }

    /**
     * Simple weight creation for OLS
     * @return the array of weights to use
     */
    private double[] buildWeights()
    {
        assert usingWeights;

        double[] weight = new double[observer.getNumberOfObservations()];
        for(int i=0; i<weight.length; i++)
        {
            weight[i]=i+1;
        }
        return weight;


    }

    /**
     * Predicts the future price of the next good to buy
     *
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    @Override
    public long predictPurchasePriceWhenIncreasingProduction(PurchasesDepartment dept) {
        if(observer.getNumberOfObservations() > 0)
        {
            updateModel();
       //     System.out.print(regression);
            predictor.setIncrementDelta((float) regression.getSlope());
        }

        return predictor.predictPurchasePriceWhenIncreasingProduction(dept);
    }

    @Override
    public long predictPurchasePriceWhenDecreasingProduction(PurchasesDepartment dept) {
        if(observer.getNumberOfObservations() > 0)
        {
            updateModel();
            //     System.out.print(regression);
            predictor.setIncrementDelta((float) regression.getSlope());
        }

        return predictor.predictPurchasePriceWhenIncreasingProduction(dept);    }

    /**
     * Call this to kill the predictor
     */
    @Override
    public void turnOff() {
        observer.turnOff();
        predictor.turnOff();

    }

    public boolean isUsingWeights() {
        return usingWeights;
    }

    public void setUsingWeights(boolean usingWeights) {
        this.usingWeights = usingWeights;
    }

    /**
     * Gets the slope of the estimated model.
     *
     * @return Value of the slope of the estimated model.
     */
    public double getSlope() {
        return regression.getSlope();
    }

    /**
     * Gets the intercept of the estimated model.
     *
     * @return Value of the intercept of the estimated model.
     */
    public double getIntercept() {
        return regression.getIntercept();
    }
}
