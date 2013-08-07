/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.prediction;

import agents.firm.purchases.PurchasesDepartment;
import com.google.common.base.Preconditions;
import financial.Market;
import model.MacroII;
import model.utilities.stats.PeriodicMarketObserver;
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
public class LearningIncreaseWithTimeSeriesPurchasePredictor implements PurchasesPredictor {



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
    private final FixedIncreasePurchasesPredictor predictor;





    /**
     * The constructor that creates the linear regression, market observer and predictor
     * @param market the market we want to observe to learn from and eventually predict
     * @param macroII the model object, needed to schedule the days we are going to make observations
     */
    public LearningIncreaseWithTimeSeriesPurchasePredictor(Market market, MacroII macroII)
    {
        this(new PeriodicMarketObserver(market,macroII));
    }

    /**
     * The constructor that gets the observer from "outside". Notice that the turnOff will close the periodicMarketObserver just the same
     * @param observer the observer object
     */
    public LearningIncreaseWithTimeSeriesPurchasePredictor(PeriodicMarketObserver observer)
    {
        this.observer = observer;
        regression = new MultipleLinearRegression(2);
        predictor = new FixedIncreasePurchasesPredictor();
        predictor.setIncrementDelta(0); //initially stay flat

    }



    /**
     * Predicts the future price of the next good to buy
     *
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    @Override
    public long predictPurchasePrice(PurchasesDepartment dept) {
        if(observer.getNumberOfObservations() > 25)
        {
            updateModel();
            //     System.out.print(regression);
            predictor.setIncrementDelta((float) (1d/extractSlopeOfDemandFromRegression()));

            double intercept = - extractInterceptOfDemandFromRegression() / extractSlopeOfDemandFromRegression();
            double slope = 1d/ extractSlopeOfDemandFromRegression();
            System.out.println("supply: " + (intercept + slope * (dept.getMarket().countYesterdayProductionByRegisteredSellers()+1)));
            return Math.round(intercept + slope * (dept.getMarket().getYesterdayVolume()+1));
        }

        return predictor.predictPurchasePrice(dept);

    }

    private void updateModel() {

        //grab the production
        double[] production = observer.getQuantitiesProducedObservedAsArray();
        Preconditions.checkArgument(production.length >=3); //there needs to be enough observations to do this!

        //create delta production and lagged production
        double[] laggedProduction = new double[production.length-1];
        double[] deltaProduction = new double[production.length-1];
        for(int i=1; i < production.length; i++)
        {
            laggedProduction[i-1] = production[i-1];
            deltaProduction[i-1] = production[i] - production[i-1];

        }
        //reality check
        assert laggedProduction[0] == production[0]; //same starting point

        //now create price by lopping off the first element
        double[] todayPrice = new double[production.length-1];
        double[] price = observer.getPricesObservedAsArray();
        for(int i=1; i<todayPrice.length; i++)
            todayPrice[i-1]=price[i];

        assert todayPrice.length == laggedProduction.length;

        //done with that torture, just regress!
        regression.estimateModel(deltaProduction,null,laggedProduction,todayPrice);


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
     * get the demand slope estimated by the time series. Need to call updateModel or predictPurchasePrice first
     * @return
     */
    public double extractSlopeOfDemandFromRegression()
    {
        double[] coefficients = regression.getResultMatrix();
        assert coefficients.length == 3;
        double alpha = coefficients[1];
        double gamma = coefficients[2];

        if(alpha != 0 && gamma != 0)
            return - gamma/alpha;
        else
            return 0;
    }

    /**
     * get the demand slope estimated by the time series. Need to call updateModel or predictPurchasePrice first

     * @return
     */
    public double extractInterceptOfDemandFromRegression()
    {
        double[] coefficients = regression.getResultMatrix();
        assert coefficients.length == 3;
        double alpha = coefficients[1];
        double beta = coefficients[0];

        if(alpha != 0 && beta != 0)
            return - beta/alpha;
        else
            return 0;
    }
}
