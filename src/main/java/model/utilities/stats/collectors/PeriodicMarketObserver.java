/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.collectors;

import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import com.sun.istack.internal.Nullable;
import financial.market.Market;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.Deactivatable;
import model.utilities.scheduler.Priority;
import model.utilities.stats.collectors.enums.MarketDataType;
import org.apache.commons.collections15.Transformer;
import sim.engine.SimState;
import sim.engine.Steppable;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

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
     * an optional writer so we can output observations
     */
    private CSVWriter writer;

    /**
     * The market we are going to observe!
     */
    private final Market market;

    /**
     * the days were observations made sense
     */
    private final ArrayList<Integer> days;



    private MarketData.MarketDataAcceptor acceptor = new MarketData.MarketDataAcceptor() {
        @Override
        public boolean acceptDay(Double lastPrice, Double volumeTraded, Double volumeProduced, Double volumeConsumed, Double demandGap, Double supplyGap) {
            return lastPrice!= -1;

        }
    };




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
        days = new ArrayList<>();


        if(dailyProbabilityOfObserving < 1)
        {
            reschedule(macroII);
        }
        else
        {
            assert dailyProbabilityOfObserving == 1;
            macroII.scheduleSoon(ActionOrder.CLEANUP_DATA_GATHERING,this,Priority.AFTER_STANDARD);
        }
    }

    private void reschedule(MacroII macroII) {

        if(!isExact)
            macroII.scheduleAnotherDayWithFixedProbability(ActionOrder.CLEANUP_DATA_GATHERING, this, dailyProbabilityOfObserving,
                    Priority.AFTER_STANDARD);
        else
            macroII.scheduleAnotherDay(ActionOrder.CLEANUP_DATA_GATHERING, this, Math.max(1,Math.round(1f/dailyProbabilityOfObserving)),
                    Priority.AFTER_STANDARD);

    }


    /**
     * get the last (newest) observation of price
     * @return
     */
    public Double getLastPriceObserved()
    {
        double observation = market.getObservationRecordedThisDay(MarketDataType.CLOSING_PRICE, days.get(days.size() - 1));
        if(priceTransformer != null)
            observation = priceTransformer.transform(observation);
        return observation;
    }

    /**
     * get the last (newest) observation of quantity
     * @return
     */
    public Double getLastQuantityTradedObserved()
    {
        double observation = market.getObservationRecordedThisDay(MarketDataType.VOLUME_TRADED, days.get(days.size() - 1));
        if(quantityTransformer != null)
            observation = quantityTransformer.transform(observation);
        return observation;
    }

    /**
     * get the last (newest) observation of quantity
     * @return
     */
    public Double getLastQuantityConsumedObserved()
    {
        double observation = market.getObservationRecordedThisDay(MarketDataType.VOLUME_CONSUMED, days.get(days.size() - 1));
        if(quantityTransformer != null)
            observation = quantityTransformer.transform(observation);
        return observation;
    }

    /**
     * get the last (newest) observation of quantity
     * @return
     */
    public Double getLastQuantityProducedObserved()
    {

        double observation = market.getObservationRecordedThisDay(MarketDataType.VOLUME_PRODUCED, days.get(days.size() - 1));
        if(quantityTransformer != null)
            observation = quantityTransformer.transform(observation);
        return observation;

    }



    /**
     * get the last (newest) observation day
     * @return
     */
    public Double getLastDayObserved()
    {
        return Double.valueOf(days.get(days.size()-1));
    }


    public int[] getDaysObserved()
    {
             return Ints.toArray(days);

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




        //if some trade actually occurred:
        if( isLastDayAcceptable())
        {
            days.add(market.getLastObservedDay());
            if(writer != null)
                outputToFile();
        }

        //reschedule yourself
        reschedule(model);


    }

    private boolean isLastDayAcceptable() {
        return acceptor.acceptDay(
                market.getLatestObservation(MarketDataType.CLOSING_PRICE),
                market.getLatestObservation(MarketDataType.VOLUME_TRADED),
                market.getLatestObservation(MarketDataType.VOLUME_PRODUCED),
                market.getLatestObservation(MarketDataType.VOLUME_CONSUMED) ,
                market.getLatestObservation(MarketDataType.DEMAND_GAP),
                market.getLatestObservation(MarketDataType.SUPPLY_GAP)
                );


    }

    public void attachCSVWriter(CSVWriter writer) throws IOException {
        this.writer = writer;
        LinkedList<String> row = new LinkedList<>();

        row.add("price");
        row.add("traded");
        row.add("produced");
        row.add("consumed");
        row.add("day");
        row.add("demand gap");
        row.add("supply gap");

        writer.writeNext(row.toArray(new String[row.size()]));
        writer.flush();
    }


    private void outputToFile()
    {
        Preconditions.checkState(writer!=null);
        LinkedList<String> row = new LinkedList<>();

        row.add(Double.toString(getLastPriceObserved()));
        row.add(Double.toString(getLastQuantityTradedObserved()));
        row.add(Double.toString(getLastQuantityProducedObserved()));
        row.add(Double.toString(getLastQuantityConsumedObserved()));
        row.add(Double.toString(getLastDayObserved()));
        row.add(Double.toString(market.getObservationRecordedThisDay(MarketDataType.DEMAND_GAP, days.get(days.size() - 1))));
        row.add(Double.toString(market.getObservationRecordedThisDay(MarketDataType.SUPPLY_GAP, days.get(days.size() - 1))));

        writer.writeNext(row.toArray(new String[row.size()]));
        try {
            writer.flush();
        } catch (IOException e) {


        }


    }

    /**
     * Copies observed prices into a double[] and return it. Useful for regressions and other manipulations
     * @return
     */
    public double[] getPricesObservedAsArray(){
        double[] prices = market.getObservationsRecordedTheseDays(MarketDataType.CLOSING_PRICE, Ints.toArray(days));
        //if needed, transform
        if(priceTransformer != null)
            for(int i=0; i<prices.length; i++)
                prices[i] = priceTransformer.transform(prices[i]);

        return prices;


    }



    /**
     * Copies quantities observed into a double[] and return it. Useful for regressions and other manipulations
     * @return
     */
    public double[] getQuantitiesTradedObservedAsArray(){
        double[] quantities = market.getObservationsRecordedTheseDays(MarketDataType.VOLUME_TRADED, Ints.toArray(days));
        //if needed, transform
        if(quantityTransformer != null)
            for(int i=0; i<quantities.length; i++)
                quantities[i] = quantityTransformer.transform(quantities[i]);

        return quantities;
    }


    /**
     * Copies quantities observed into a double[] and return it. Useful for regressions and other manipulations
     * @return
     */
    public double[] getQuantitiesProducedObservedAsArray(){
        double[] quantities = market.getObservationsRecordedTheseDays(MarketDataType.VOLUME_PRODUCED, Ints.toArray(days));
        //if needed, transform
        if(quantityTransformer != null)
            for(int i=0; i<quantities.length; i++)
                quantities[i] = quantityTransformer.transform(quantities[i]);

        return quantities;
    }


    /**
     * Copies quantities observed into a double[] and return it. Useful for regressions and other manipulations
     * @return
     */
    public double[] getQuantitiesConsumedObservedAsArray(){
        double[] quantities = market.getObservationsRecordedTheseDays(MarketDataType.VOLUME_CONSUMED, Ints.toArray(days));
        //if needed, transform
        if(quantityTransformer != null)
            for(int i=0; i<quantities.length; i++)
                quantities[i] = quantityTransformer.transform(quantities[i]);

        return quantities;
    }



    /**
     * Copies observation days into a double[] and return sit. Useful for regressions and other manipulations
     * @return
     */
    public double[] getObservationDaysAsArray(){
        return Doubles.toArray(days);
    }



    /**
     * Copies quantities observed into a double[] and return it. Useful for regressions and other manipulations
     * @return
     */
    public double[] getDemandGapsAsArray(){
        return market.getObservationsRecordedTheseDays(MarketDataType.DEMAND_GAP, Ints.toArray(days));

    }


    /**
     * Copies quantities observed into a double[] and return it. Useful for regressions and other manipulations
     * @return
     */
    public double[] getSupplyGapsAsArray(){
        return market.getObservationsRecordedTheseDays(MarketDataType.SUPPLY_GAP, Ints.toArray(days));
    }



    /**
     * if this is called, the observer will forget the oldest set of observations (prices, quantities, day)
     */
    public void forgetOldestObservation()
    {

        int size = getNumberOfObservations();
        days.remove(0);

        assert  size == getNumberOfObservations() + 1;

    }





    public int getNumberOfObservations()
    {
        int size =days.size();
        //all sizes should be the same


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
        return market.getObservationRecordedThisDay(MarketDataType.VOLUME_TRADED, days.get(days.size() - 1));
    }
    public double getLastUntrasformedPrice() {
        return market.getObservationRecordedThisDay(MarketDataType.CLOSING_PRICE, days.get(days.size() - 1));
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
