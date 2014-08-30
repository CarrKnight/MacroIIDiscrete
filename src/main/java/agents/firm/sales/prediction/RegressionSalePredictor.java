/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.sales.SalesDepartment;
import com.google.common.base.Preconditions;
import financial.market.Market;
import model.MacroII;
import model.utilities.stats.collectors.PeriodicMarketObserver;
import model.utilities.stats.regression.LinearRegression;
import org.apache.commons.collections15.Transformer;


/**
 * <h4>Description</h4>
 * <p/> This object spawns a periodic market observer to look
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-07-11
 * @see
 */
public class RegressionSalePredictor extends BaseSalesPredictor{



    /**
     * the regression object we use
     */
    protected LinearRegression regression;


    /**
     * The object that periodically reads in stuff from the market
     */
    protected PeriodicMarketObserver observer;


    /**
     * The PeriodicMarketObserver is created straight into the constructor
     * @param market the market to observe
     * @param macroII a link to the model to schedule yourself
     */
    public RegressionSalePredictor(Market market, MacroII macroII)
    {

       this(new PeriodicMarketObserver(market,macroII));

    }

    /**
     * The PeriodicMarketObserver is created straight into the constructor
     * @param market the market to observe
     * @param macroII a link to the model to schedule yourself
     */
    public RegressionSalePredictor(Market market, MacroII macroII, float observationProbability)
    {

        this(new PeriodicMarketObserver(market,macroII,observationProbability));

    }


    /**
     * Give a premade observer to sale predictor. The observer will be turned Off when the predictor is turned off!
     * @param observer
     */
    public RegressionSalePredictor(PeriodicMarketObserver observer) {
        regression = new LinearRegression();

        this.observer = observer;
    }

    /**
     * Runs the regression and returns the regression prediction if it is possible or the last price otherwise.
     *
     *
     *
     * @param dept                   the sales department that has to answer this question
     * @param expectedProductionCost the HQ estimate of costs in producing whatever it wants to sell. It isn't necesarilly used.
     * @param increaseStep ignored
     * @return the best offer available/predicted or -1 if there are no quotes/good predictions
     */
    @Override
    public float predictSalePriceAfterIncreasingProduction(SalesDepartment dept, int expectedProductionCost, int increaseStep)
    {
        Preconditions.checkArgument(increaseStep >= 0);

        //regress and return
        updateModel();

        if(Double.isNaN(regression.getIntercept())) //if we couldn't do a regression, just return today's pricing
            return dept.hypotheticalSalePrice();
        else
        {
            //if you are producing more than what's sold, use production to predict tomorrow's quantity
            double x = Math.max(observer.getLastUntrasformedQuantityTraded(),dept.getTodayInflow()) + increaseStep;
            if(observer.getQuantityTransformer() != null)
                x = observer.getQuantityTransformer().transform(x);
            double y =  regression.predict(x);
            if(observer.getPriceTransformer() != null)
            {
                assert observer.getPriceInverseTransformer() != null ;
                y = observer.getPriceInverseTransformer().transform(y);
            }

            return (int) Math.round(y);
        }



    }

    /**
     * This is called by the firm when it wants to predict the price they can sell to if they increase production
     *
     *
     * @param dept                   the sales department that has to answer this question
     * @param expectedProductionCost the HQ estimate of costs in producing whatever it wants to sell. It isn't necesarilly used.
     * @param decreaseStep ignored
     * @return the best offer available/predicted or -1 if there are no quotes/good predictions
     */
    @Override
    public float predictSalePriceAfterDecreasingProduction(SalesDepartment dept, int expectedProductionCost, int decreaseStep) {
        Preconditions.checkArgument(decreaseStep >= 0);
        //regress and return
        updateModel();

        if(Double.isNaN(regression.getIntercept())) //if we couldn't do a regression, just return today's pricing
            return dept.hypotheticalSalePrice();
        else
        {
            //if you are producing more than what's sold, use production to predict tomorrow's quantity
            double x = Math.max(observer.getLastUntrasformedQuantityTraded(),dept.getTodayInflow()) - decreaseStep;
            if(observer.getQuantityTransformer() != null)
                x = observer.getQuantityTransformer().transform(x);
            double y =  regression.predict(x);
            if(observer.getPriceTransformer() != null)
            {
                assert observer.getPriceInverseTransformer() != null ;
                y = observer.getPriceInverseTransformer().transform(y);
            }

            return (int) Math.round(y);
        }

    }

    /**
     * Force the predictor to run a regression, if possible
     */
    public void updateModel() {
        if(observer.getNumberOfObservations() >2)
            regression.estimateModel(observer.getQuantitiesConsumedObservedAsArray(),
                    observer.getPricesObservedAsArray(),null);
    }

    /**
     * Call this to kill the predictor
     */
    @Override
    public void turnOff() {

        super.turnOff();
        observer.turnOff();
    }






    /**
     * Gets the intercept of the estimated model.
     *
     * @return Value of the intercept of the estimated model.
     */
    public double getIntercept() {
        return regression.getIntercept();
    }

    /**
     * Gets the slope of the estimated model.
     *
     * @return Value of the slope of the estimated model.
     */
    public double getSlope() {
        return regression.getSlope();
    }

    protected LinearRegression getRegression() {
        return regression;
    }


    public Transformer<Double, Double> getPriceInverseTransformer() {
        return observer.getPriceInverseTransformer();
    }

    /**
     * Gets A function we can put in to transform the observed price before reading it in.
     *
     * @return Value of A function we can put in to transform the observed price before reading it in.
     */
    public Transformer<Double, Double> getPriceTransformer() {
        return observer.getPriceTransformer();
    }

    /**
     * Sets new A function we can put in to transform the observed price before reading it in.
     *
     * @param priceTransformer New value of A function we can put in to transform the observed price before reading it in.
     */
    public void setPriceTransformer( Transformer<Double, Double> priceTransformer,  Transformer<Double, Double> priceInverseTransformer) {
        observer.setPriceTransformer(priceTransformer, priceInverseTransformer);
    }

    /**
     * Gets A function we can put in to transform the observed price before reading it in.
     *
     * @return Value of A function we can put in to transform the observed price before reading it in.
     */
    public Transformer<Double, Double> getQuantityTransformer() {
        return observer.getQuantityTransformer();
    }

    /**
     * Sets new A function we can put in to transform the observed price before reading it in.
     *
     * @param quantityTransformer New value of A function we can put in to transform the observed price before reading it in.
     */
    public void setQuantityTransformer(Transformer<Double, Double> quantityTransformer) {
        observer.setQuantityTransformer(quantityTransformer);
    }

    public int numberOfObservations() {
        return observer.getNumberOfObservations();
    }


    /**
     * get the last (newest) observation day
     * @return
     */
    public Double getLastDayObserved() {
        return observer.getLastDayObserved();
    }

    /**
     * get the last (newest) observation of price
     * @return
     */
    public Double getLastPriceObserved() {
        return observer.getLastPriceObserved();
    }

    /**
     * get the last (newest) observation of quantity
     * @return
     */
    public Double getLastQuantityObserved() {
        return observer.getLastQuantityTradedObserved();
    }


    public void setDailyProbabilityOfObserving(float dailyProbabilityOfObserving) {
        observer.setDailyProbabilityOfObserving(dailyProbabilityOfObserving);
    }

    public float getDailyProbabilityOfObserving() {
        return observer.getDailyProbabilityOfObserving();
    }

    /**
     * get the last (newest) observation of quantity
     * @return
     */
    public Double getLastQuantityProducedObserved() {
        return observer.getLastQuantityProducedObserved();
    }

    /**
     * get the last (newest) observation of quantity
     * @return
     */
    public Double getLastQuantityTradedObserved() {
        return observer.getLastQuantityTradedObserved();
    }

    /**
     * get the last (newest) observation of quantity
     * @return
     */
    public Double getLastQuantityConsumedObserved() {
        return observer.getLastQuantityConsumedObserved();
    }

    public double getLastUntrasformedQuantityTraded() {
        return observer.getLastUntrasformedQuantityTraded();
    }

    public double getLastUntrasformedPrice() {
        return observer.getLastUntrasformedPrice();
    }

    @Override
    public String toString() {
        return regression.toString();
    }

    /**
     * This is a little bit weird to predict, but basically you want to know what will be "tomorrow" price if you don't change production.
     * Most predictors simply return today closing price, because maybe this will be useful in some cases. It's used by Marginal Maximizer Statics
     *
     * @param dept the sales department
     * @return predicted price
     */
    @Override
    public float predictSalePriceWhenNotChangingProduction(SalesDepartment dept) {
        //regress and return
        updateModel();

        if(Double.isNaN(regression.getIntercept())) //if we couldn't do a regression, just return today's pricing
            return dept.hypotheticalSalePrice();
        else
        {
            //if you are producing more than what's sold, use production to predict tomorrow's quantity
            double x = Math.max(observer.getLastUntrasformedQuantityTraded(),dept.getTodayInflow());
            if(observer.getQuantityTransformer() != null)
                x = observer.getQuantityTransformer().transform(x);
            double y =  regression.predict(x);
            if(observer.getPriceTransformer() != null)
            {
                assert observer.getPriceInverseTransformer() != null ;
                y = observer.getPriceInverseTransformer().transform(y);
            }

            return (int) Math.round(y);
        }

    }
}
