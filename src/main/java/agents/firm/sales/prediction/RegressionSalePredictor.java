/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.sales.SalesDepartment;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Doubles;
import financial.Market;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.scheduler.Priority;
import model.utilities.stats.regression.LinearRegression;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.LinkedList;

/**
 * <h4>Description</h4>
 * <p/> This sales predictor checks periodically the price-quantity relationship in the market and feeds it into a
 * linear regression model
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
public class RegressionSalePredictor implements SalesPredictor, Steppable {

    /**
     * The market we are going to observe!
     */
    private final Market market;

    /**
     * the regression object we use
     */
    private LinearRegression regression;

    private final LinkedList<Double> pricesObserved;

    private final LinkedList<Double> quantitiesObserved;

    private boolean isActive = true;


    public static float defaultDailyProbabilityOfObserving =  0.142f;

    /**
     * The probability each day the predictor will memorize today's price and quantity as an observation
     */
    private float dailyProbabilityOfObserving = defaultDailyProbabilityOfObserving; //this corresponds to weekly


    /**
     *
     * @param market the market to observe
     * @param macroII a link to the model to schedule yourself
     */


    public RegressionSalePredictor(Market market, MacroII macroII)
    {
        this.market = market;
        regression = new LinearRegression();

        pricesObserved= new LinkedList<>();
        quantitiesObserved = new LinkedList<>();

        macroII.scheduleAnotherDayWithFixedProbability(ActionOrder.DAWN, this, dailyProbabilityOfObserving,
                Priority.AFTER_STANDARD);
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
            return Math.round(regression.predict(Math.max(market.getYesterdayVolume(),dept.getTodayInflow()) + 1));
        }



    }

    /**
     * Force the predictor to run a regression, if possible
     */
    public void updateModel() {
        if(quantitiesObserved.size() >1)
            regression.estimateModel(Doubles.toArray(quantitiesObserved),Doubles.toArray(pricesObserved),null);
    }

    /**
     * Call this to kill the predictor
     */
    @Override
    public void turnOff() {
        isActive=false;
    }


    /**
     * reads and memorizes price quantity, and schedules itself again approximately in a week.
     *
     * @param state
     */
    @Override
    public void step(SimState state)
    {
        if(!isActive)
            return;

        Preconditions.checkState(state instanceof MacroII);

        double price = market.getYesterdayLastPrice();
        double quantity = market.getYesterdayVolume();
        //if some trade actually occurred:
        if( price!= -1 && quantity != 0)
        {
            pricesObserved.add(price);
            quantitiesObserved.add(quantity);
        }


        ((MacroII) state).scheduleAnotherDayWithFixedProbability(ActionOrder.DAWN,this,dailyProbabilityOfObserving,
                Priority.AFTER_STANDARD);
    }


    public float getDailyProbabilityOfObserving() {
        return dailyProbabilityOfObserving;
    }

    public void setDailyProbabilityOfObserving(float dailyProbabilityOfObserving) {
        this.dailyProbabilityOfObserving = dailyProbabilityOfObserving;
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

    protected LinkedList<Double> getPricesObserved() {
        return pricesObserved;
    }

    protected LinkedList<Double> getQuantitiesObserved() {
        return quantitiesObserved;
    }


}
