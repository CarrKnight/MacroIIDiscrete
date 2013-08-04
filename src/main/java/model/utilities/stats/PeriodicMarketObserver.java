/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.stats;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Doubles;
import com.sun.istack.internal.Nullable;
import financial.Market;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.Deactivatable;
import model.utilities.scheduler.Priority;
import org.apache.commons.collections15.Transformer;
import sim.engine.SimState;
import sim.engine.Steppable;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * <h4>Description</h4>
 * <p/> This is a class that observes every now and then price and quantities of a market. This can then be used to run regressions on
 * or whatever.
 * <p/> Transformers can be used to modify observations when recording them.
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
public class PeriodicMarketObserver implements Steppable, Deactivatable {

    /**
     * The market we are going to observe!
     */
    private final Market market;

    /**
     * a list holding all the prices observed
     */
    private final LinkedList<Double> pricesObserved;

    /**
     * a list holding all the quantities observed
     */
    private final LinkedList<Double> quantitiesObserved;

    /**
     * This is the last quantity observed
     */
    private double lastUntrasformedQuantity;

    /**
     * a list holding all the quantities observed
     */
    private final LinkedList<Double> observationDays;




    private boolean isActive = true;


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
     * The probability of observing placed at constructor if no other is specified
     */
    public static float defaultDailyProbabilityOfObserving =  0.142f;

    /**
     * When created, it automatically schedules itself. It also register itself as deactivable so that it will be turned
     * off when MacroII is finished
     * @param market a link to the market to observe
     * @param macroII a link to the model (to reschedule oneself)
     */
    public PeriodicMarketObserver(Market market, MacroII macroII)
    {
        this.market = market;
        pricesObserved = new LinkedList<>();
        quantitiesObserved = new LinkedList<>();
        observationDays = new LinkedList<>();

        if(dailyProbabilityOfObserving < 1)
        {
            macroII.scheduleAnotherDayWithFixedProbability(ActionOrder.DAWN, this, dailyProbabilityOfObserving,
                    Priority.AFTER_STANDARD);
        }
        else
        {
            assert dailyProbabilityOfObserving == 1;
            macroII.scheduleSoon(ActionOrder.DAWN,this,Priority.AFTER_STANDARD);
        }
    }


    /**
     * get the last (newest) observation of price
     * @return
     */
    public Double getLastPriceObserved()
    {
        return pricesObserved.getLast();
    }

    /**
     * get the last (newest) observation of quantity
     * @return
     */
    public Double getLastQuantityObserved()
    {
        return quantitiesObserved.getLast();
    }

    /**
     * get the last (newest) observation day
     * @return
     */
    public Double getLastDayObserved()
    {
        return observationDays.getLast();
    }

    @Override
    public void turnOff() {
        isActive = false;

    }


    @Override
    public void step(SimState state) {
        //stop if you are activated
        if(!isActive)
            return;

        //make sure it's the right model
        Preconditions.checkState(state instanceof MacroII);
        MacroII model = (MacroII) state;

        //read (and if needed, transform) price and quantity
        double price = market.getYesterdayLastPrice();
        if(priceTransformer!=null)
            price = priceTransformer.transform(price);
        double quantity = market.getYesterdayVolume();
        lastUntrasformedQuantity = quantity; //remember it, you might need it for prediction
        if(quantityTransformer!=null)
            quantity = quantityTransformer.transform(quantity);


        //if some trade actually occurred:
        if( price!= -1 && quantity != 0)
        {
            pricesObserved.add(price);
            quantitiesObserved.add(quantity);
            observationDays.add(model.getMainScheduleTime());
        }

        //reschedule yourself
        model.scheduleAnotherDayWithFixedProbability(ActionOrder.DAWN,this,dailyProbabilityOfObserving,
                Priority.AFTER_STANDARD);

    }

    /**
     * Get unmodifiable view of the list
     * @return
     */
    protected List<Double> getPricesObserved() {
        return Collections.unmodifiableList(pricesObserved);
    }

    /**
     * Copies observed prices into a double[] and return it. Useful for regressions and other manipulations
     * @return
     */
    public double[] getPricesObservedAsArray(){
        return Doubles.toArray(pricesObserved);

    }

    /**
     * Get unmodifiable view of the list
     * @return
     */
    protected List<Double> getQuantitiesObserved() {
        return Collections.unmodifiableList(quantitiesObserved);
    }

    /**
     * Copies quantities observed into a double[] and return it. Useful for regressions and other manipulations
     * @return
     */
    public double[] getQuantitiesObservedAsArray(){
        return Doubles.toArray(quantitiesObserved);
    }


    /**
     * Get unmodifiable view of the list
     * @return
     */
    protected List<Double> getObservationDays() {

        return Collections.unmodifiableList(observationDays);
    }

    /**
     * Copies observation days into a double[] and return sit. Useful for regressions and other manipulations
     * @return
     */
    public double[] getObservationDaysAsArray(){
        return Doubles.toArray(observationDays);
    }


    /**
     * if this is called, the observer will forget the oldest set of observations (prices, quantities, day)
     */
    public void forgetOldestObservation()
    {

        int size = getNumberOfObservations();
        pricesObserved.removeFirst();
        quantitiesObserved.removeFirst();
        observationDays.removeFirst();
        assert  size == getNumberOfObservations() + 1;

    }





    public int getNumberOfObservations()
    {
        int size = pricesObserved.size();
        //all sizes should be the same
        assert  size == quantitiesObserved.size();
        assert size == observationDays.size();

        return size;

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


    public double getLastUntrasformedQuantity() {
        return lastUntrasformedQuantity;
    }

    public float getDailyProbabilityOfObserving() {
        return dailyProbabilityOfObserving;
    }

    public void setDailyProbabilityOfObserving(float dailyProbabilityOfObserving) {
        this.dailyProbabilityOfObserving = dailyProbabilityOfObserving;
    }
}
