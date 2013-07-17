/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.sales.SalesDepartment;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Doubles;
import com.sun.istack.internal.Nullable;
import financial.Market;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.scheduler.Priority;
import model.utilities.stats.regression.LinearRegression;
import org.apache.commons.collections15.Transformer;
import sim.engine.SimState;
import sim.engine.Steppable;

import javax.annotation.Nonnull;
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


    private final LinkedList<Double> dayOfObservation;

    private boolean isActive = true;


    public static float defaultDailyProbabilityOfObserving =  0.142f;

    /**
     * A function we can put in to transform the observed price before reading it in
     */
    @Nullable
    private Transformer<Double, Double> priceTransformer;


    /**
     *  The inverse function of price transform, required for prediction
     */
    @Nullable
    private Transformer<Double, Double> priceInverseTransformer;


    /**
     * A function we can put in to transform the observed price before reading it in
     */
    @Nullable
    private Transformer<Double, Double> quantityTransformer;


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
        dayOfObservation = new LinkedList<>();

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
            double x = Math.max(market.getYesterdayVolume(),dept.getTodayInflow()) + 1;
            if(quantityTransformer != null)
                x = quantityTransformer.transform(x);
            double y =  regression.predict(x);
            if(priceTransformer != null)
            {
                assert priceInverseTransformer != null ;
                y = priceInverseTransformer.transform(y);
            }

            return Math.round(y);
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
        MacroII model = (MacroII) state;

        double price = market.getYesterdayLastPrice();
        if(priceTransformer!=null)
            price = priceTransformer.transform(price);
        double quantity = market.getYesterdayVolume();
        if(quantityTransformer!=null)
            quantity = quantityTransformer.transform(quantity);


        //if some trade actually occurred:
        if( price!= -1 && quantity != 0)
        {
            pricesObserved.add(price);
            quantitiesObserved.add(quantity);
            dayOfObservation.add(model.getMainScheduleTime());
        }


        model.scheduleAnotherDayWithFixedProbability(ActionOrder.DAWN,this,dailyProbabilityOfObserving,
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


    protected LinkedList<Double> getDayOfObservation() {
        return dayOfObservation;
    }


    /**
     * Sets new A function we can put in to transform the observed price before reading it in.
     *
     * @param quantityTransformer New value of A function we can put in to transform the observed price before reading it in.
     */
    public void setQuantityTransformer(Transformer<Double, Double> quantityTransformer) {
        this.quantityTransformer = quantityTransformer;
    }

    /**
     * Gets A function we can put in to transform the observed price before reading it in.
     *
     * @return Value of A function we can put in to transform the observed price before reading it in.
     */
    public Transformer<Double, Double> getQuantityTransformer() {
        return quantityTransformer;
    }

    /**
     * Sets new A function we can put in to transform the observed price before reading it in.
     *
     * @param priceTransformer New value of A function we can put in to transform the observed price before reading it in.
     */
    public void setPriceTransformer(@Nonnull Transformer<Double, Double> priceTransformer,
                                    @Nonnull Transformer<Double, Double> priceInverseTransformer) {
        this.priceTransformer = priceTransformer;
        this.priceInverseTransformer  = priceInverseTransformer;
    }

    /**
     * Gets A function we can put in to transform the observed price before reading it in.
     *
     * @return Value of A function we can put in to transform the observed price before reading it in.
     */
    public Transformer<Double, Double> getPriceTransformer() {
        return priceTransformer;
    }

    public Transformer<Double, Double> getPriceInverseTransformer() {
        return priceInverseTransformer;
    }
}
