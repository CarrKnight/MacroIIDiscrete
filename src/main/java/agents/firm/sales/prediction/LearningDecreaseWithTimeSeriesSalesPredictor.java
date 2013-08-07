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
        regression = new MultipleLinearRegression(2);
        predictor = new FixedDecreaseSalesPredictor();
        predictor.setDecrementDelta(0); //initially stay flat

    }


    /**
     * This is called by the firm when it wants to predict the price they can sell to
     * (usually in order to guide production). <br>
     *
     * @param dept                   the sales department that has to answer this question
     * @param expectedProductionCost the HQ estimate of costs in producing whatever it wants to sell. It isn't necesarilly used.
     * @return the best offer available/predicted or -1 if there are no quotes/good predictions
     */
    @Override
    public long predictSalePrice(SalesDepartment dept, long expectedProductionCost) {
        if(observer.getNumberOfObservations() > 25)
        {
            updateModel();
            //     System.out.print(regression);
        /*    predictor.setDecrementDelta((float) (-1d/extractSlopeOfDemandFromRegression()));
            if(dept.getGoodType() == GoodType.BEEF)
                System.out.println("demand: " + predictor.getDecrementDelta());
                */
            double intercept = - extractInterceptOfDemandFromRegression() / extractSlopeOfDemandFromRegression();
            double slope = 1d/ extractSlopeOfDemandFromRegression();
            System.out.println("demand: " + (intercept + slope * (dept.getMarket().countYesterdayConsumptionByRegisteredBuyers()+1)));
            return Math.round(intercept + slope * (dept.getMarket().getYesterdayVolume()+1));

        }


        return predictor.predictSalePrice(dept,expectedProductionCost);
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


    private void updateModel() {

        //grab the consumption
        double[] consumption = observer.getQuantitiesConsumedObservedAsArray();
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
        for(int i=1; i<todayPrice.length; i++)
            todayPrice[i-1]=price[i];

        assert todayPrice.length == laggedConsumption.length;

        //done with that torture, just regress!
        regression.estimateModel(deltaConsumption,null,laggedConsumption,todayPrice);


    }
}
