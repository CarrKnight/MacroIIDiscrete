/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.sales.SalesDepartment;
import financial.Market;
import model.MacroII;
import model.utilities.stats.PeriodicMarketObserver;
import model.utilities.stats.regression.LinearRegression;
import org.apache.commons.collections15.Transformer;

import javax.annotation.Nonnull;

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
public class RegressionSalePredictor implements SalesPredictor{



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
     * @param dept                   the sales department that has to answer this question
     * @param expectedProductionCost the HQ estimate of costs in producing whatever it wants to sell. It isn't necesarilly used.
     * @return the best offer available/predicted or -1 if there are no quotes/good predictions
     */
    @Override
    public long predictSalePrice(SalesDepartment dept, long expectedProductionCost)
    {
        //regress and return
        updateModel();

        if(Double.isNaN(regression.getIntercept())) //if we couldn't do a regression, just return today's pricing
            return dept.hypotheticalSalePrice();
        else
        {
            //if you are producing more than what's sold, use production to predict tomorrow's quantity
            double x = getFutureXForSale(dept);
            if(observer.getQuantityTransformer() != null)
                x = observer.getQuantityTransformer().transform(x);
            double y =  regression.predict(x);
            if(observer.getPriceTransformer() != null)
            {
                assert observer.getPriceInverseTransformer() != null ;
                y = observer.getPriceInverseTransformer().transform(y);
            }

            return Math.round(y);
        }



    }

    /**
     * Usually we just predict lastX+1, we need to make sure we transform it correctly, though
     * @param dept
     * @return
     */
    private double getFutureXForSale(SalesDepartment dept) {
        return Math.max(observer.getLastUntrasformedQuantity(),dept.getTodayInflow()) + 1;
    }

    /**
     * Force the predictor to run a regression, if possible
     */
    public void updateModel() {
        if(observer.getNumberOfObservations() >1)
            regression.estimateModel(observer.getQuantitiesObservedAsArray(),
                    observer.getPricesObservedAsArray(),null);
    }

    /**
     * Call this to kill the predictor
     */
    @Override
    public void turnOff() {
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
    public void setPriceTransformer(@Nonnull Transformer<Double, Double> priceTransformer, @Nonnull Transformer<Double, Double> priceInverseTransformer) {
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
        return observer.getLastQuantityObserved();
    }


    public void setDailyProbabilityOfObserving(float dailyProbabilityOfObserving) {
        observer.setDailyProbabilityOfObserving(dailyProbabilityOfObserving);
    }

    public float getDailyProbabilityOfObserving() {
        return observer.getDailyProbabilityOfObserving();
    }
}
