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
import java.util.ArrayList;
import java.util.Collections;
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
    private final ArrayList<Double> pricesObserved;

    /**
     * a list holding all the quantities traded observed
     */
    private final ArrayList<Double> quantitiesTradedObserved;

    /**
     * a list holding all the quantities consumed observed
     */
    private final ArrayList<Double> quantitiesConsumedObserved;


    /**
     * a list holding all the quantity produced observed
     */
    private final ArrayList<Double> quantitiesProducedObserved;


    /**
     * a list holding all the demand gaps considered by the
     */
    private final ArrayList<Double> demandGaps;

    /**
     * a list holding all the quantity produced observed
     */
    private final ArrayList<Double> supplyGaps;

    /**
     * This is the last quantity traded observed
     */
    private double lastUntrasformedQuantityTraded;
    /**
     * This is the last quantity produced observed
     */
    private double lastUntrasformedQuantityProduced;
    /**
     * This is the last quantity consumed observed
     */
    private double lastUntrasformedQuantityConsumption;

    /**
     * a list holding all the quantities observed
     */
    private final ArrayList<Double> observationDays;




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
     * if "isExact" is set to true, the market observer doesn't check "at random" but at fixed intervals of size 1/probabilityOfObserving
     */
    public boolean isExact = true;

    /**
     * When created, it automatically schedules itself. It also register itself as deactivable so that it will be turned
     * off when MacroII is finished
     * @param market a link to the market to observe
     * @param macroII a link to the model (to reschedule oneself)
     */
    public PeriodicMarketObserver(Market market, MacroII macroII)
    {
        this.market = market;
        pricesObserved = new ArrayList<>(100);
        quantitiesTradedObserved = new ArrayList<>(100);
        observationDays = new ArrayList<>(100);
        quantitiesConsumedObserved = new ArrayList<>(100);
        quantitiesProducedObserved = new ArrayList<>(100);
        demandGaps = new ArrayList<>(100);
        supplyGaps = new ArrayList<>(100);

        if(dailyProbabilityOfObserving < 1)
        {
            reschedule(macroII);
        }
        else
        {
            assert dailyProbabilityOfObserving == 1;
            macroII.scheduleSoon(ActionOrder.DAWN,this,Priority.AFTER_STANDARD);
        }
    }

    private void reschedule(MacroII macroII) {

        if(!isExact)
            macroII.scheduleAnotherDayWithFixedProbability(ActionOrder.DAWN, this, dailyProbabilityOfObserving,
                    Priority.AFTER_STANDARD);
        else
            macroII.scheduleAnotherDay(ActionOrder.DAWN, this, Math.max(1,Math.round(1f/dailyProbabilityOfObserving)),
                    Priority.AFTER_STANDARD);

    }


    /**
     * get the last (newest) observation of price
     * @return
     */
    public Double getLastPriceObserved()
    {
        return pricesObserved.get(pricesObserved.size() - 1);
    }

    /**
     * get the last (newest) observation of quantity
     * @return
     */
    public Double getLastQuantityTradedObserved()
    {
        return quantitiesTradedObserved.get(quantitiesTradedObserved.size()-1);
    }

    /**
     * get the last (newest) observation of quantity
     * @return
     */
    public Double getLastQuantityConsumedObserved()
    {
        return quantitiesConsumedObserved.get(quantitiesConsumedObserved.size()-1);
    }

    /**
     * get the last (newest) observation of quantity
     * @return
     */
    public Double getLastQuantityProducedObserved()
    {
        return quantitiesProducedObserved.get(quantitiesProducedObserved.size()-1);
    }



    /**
     * get the last (newest) observation day
     * @return
     */
    public Double getLastDayObserved()
    {
        return observationDays.get(observationDays.size()-1);
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

        //read quantities
        double quantity = market.getYesterdayVolume();
        double produced = market.countYesterdayProductionByRegisteredSellers();
        double consumed = market.countYesterdayConsumptionByRegisteredBuyers();
        double demandgap = market.sumDemandGaps();
        double supplygap = market.sumSupplyGaps();
        //remember them before transforming them
        lastUntrasformedQuantityTraded = quantity;
        lastUntrasformedQuantityProduced = produced;
        lastUntrasformedQuantityConsumption = consumed;

        if(quantityTransformer!=null)
        {
            quantity = quantityTransformer.transform(quantity);
            produced = quantityTransformer.transform(produced);
            consumed = quantityTransformer.transform(consumed);
        }

        //if some trade actually occurred:
        if( price!= -1)
        {
            pricesObserved.add(price);
            quantitiesTradedObserved.add(quantity);
            quantitiesConsumedObserved.add(consumed);
            quantitiesProducedObserved.add(produced);
            observationDays.add(model.getMainScheduleTime());
            demandGaps.add(demandgap);
            supplyGaps.add(supplygap);
        }

        //reschedule yourself
        reschedule(model);


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
    protected List<Double> getQuantitiesTradedObserved() {
        return Collections.unmodifiableList(quantitiesTradedObserved);
    }

    /**
     * Copies quantities observed into a double[] and return it. Useful for regressions and other manipulations
     * @return
     */
    public double[] getQuantitiesTradedObservedAsArray(){
        return Doubles.toArray(quantitiesTradedObserved);
    }


    /**
     * Copies quantities observed into a double[] and return it. Useful for regressions and other manipulations
     * @return
     */
    public double[] getQuantitiesProducedObservedAsArray(){
        return Doubles.toArray(quantitiesProducedObserved);
    }


    /**
     * Copies quantities observed into a double[] and return it. Useful for regressions and other manipulations
     * @return
     */
    public double[] getQuantitiesConsumedObservedAsArray(){
        return Doubles.toArray(quantitiesConsumedObserved);
    }



    /**
     * Copies observation days into a double[] and return sit. Useful for regressions and other manipulations
     * @return
     */
    public double[] getObservationDaysAsArray(){
        return Doubles.toArray(observationDays);
    }



    /**
     * Copies quantities observed into a double[] and return it. Useful for regressions and other manipulations
     * @return
     */
    public double[] getDemandGapsAsArray(){
        return Doubles.toArray(demandGaps);
    }


    /**
     * Copies quantities observed into a double[] and return it. Useful for regressions and other manipulations
     * @return
     */
    public double[] getSupplyGapsAsArray(){
        return Doubles.toArray(supplyGaps);
    }



    /**
     * if this is called, the observer will forget the oldest set of observations (prices, quantities, day)
     */
    public void forgetOldestObservation()
    {

        int size = getNumberOfObservations();
        pricesObserved.remove(0);
        quantitiesTradedObserved.remove(0);
        observationDays.remove(0);
        quantitiesProducedObserved.remove(0);
        quantitiesConsumedObserved.remove(0);
        assert  size == getNumberOfObservations() + 1;

    }





    public int getNumberOfObservations()
    {
        int size = pricesObserved.size();
        //all sizes should be the same
        assert  size == quantitiesTradedObserved.size();
        assert size == observationDays.size();
        assert size == quantitiesProducedObserved.size();
        assert size == quantitiesConsumedObserved.size();

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


    public double getLastUntrasformedQuantityTraded() {
        return lastUntrasformedQuantityTraded;
    }

    public float getDailyProbabilityOfObserving() {
        return dailyProbabilityOfObserving;
    }

    public void setDailyProbabilityOfObserving(float dailyProbabilityOfObserving) {
        this.dailyProbabilityOfObserving = dailyProbabilityOfObserving;
    }

    public boolean isExact() {
        return isExact;
    }

    public void setExact(boolean exact) {
        isExact = exact;
    }



}
