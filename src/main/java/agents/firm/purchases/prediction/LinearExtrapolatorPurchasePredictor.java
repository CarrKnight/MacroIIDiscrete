/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.prediction;

import agents.firm.purchases.PurchasesDepartment;
import com.google.common.base.Preconditions;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.filters.ExponentialFilter;
import model.utilities.stats.collectors.enums.PurchasesDataType;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * <h4>Description</h4>
 * <p/> A simple (and hopefully faster) alternative to prediction by regression. Whenever the firm increases or decreases
 * # of workers in plants that need the input the purchase department provides, this extrapolator remember the date.
 * Then, whenever asked for a prediction, it checks what happened to price and quantity changed
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-08-19
 * @see
 */
public class LinearExtrapolatorPurchasePredictor implements PurchasesPredictor, Steppable
{

    /**
     * which day we last observed one of our plants change workers?
     */
    private int lastWorkerShockDay=-1;

    /**
     * which day before the last we observed one of our plants change workers?
     */
    private int olderWorkerShockDay=-1;

    /**
     * when we last estimated the slope, what was the oldest day we looked at?
     */
    private int lowerBoundObservation = -1;

    /**
     * when we last estimated the slope, what was the less remote day we looked at?
     */
    private int higherBoundObservation = -1;

    /**
     * The number of workers we saw last time
     */
    private int newestNumberOfWorkersObserved=0;

    /**
     * The number of workers we saw the time before
     */
    private int olderNumberOfWorkersObserved=0;

    /**
     * the purchase department we predict for
     */
    private final PurchasesDepartment department;



    /**
     * set to off by turnOff() prevents from rescheduling!
     */
    private boolean active = true;

    /**
     * we feed the slope in here.
     */
    private final FixedIncreasePurchasesPredictor predictor;


    /**
     * how many days before the shock can you look into?
     */
    private int howManyDaysBackShallILook  = 15;

    /**
     * how many days after the shock can you look into?
     */
    private int maximumNumberOfDaysToLookAhead = 30;

    /**
     * the exponential weight used
     */
    private float weight = .9f ;

    /**
     * the new slope we compute is usually averaged over the previous ones through anotherEMA
     */
    private float slopeAverageWeight = 1f;

    /**
     * builds and schedule the extrapolator to step every day
     */
    public LinearExtrapolatorPurchasePredictor(PurchasesDepartment department) {
        this(department,department.getModel());
    }

    /**
     * builds and schedule the extrapolator to step every day
     * @param department
     * @param model
     */
    public LinearExtrapolatorPurchasePredictor(PurchasesDepartment department, MacroII model) {
        this.department = department;

        //schedule soon, at THINK
        model.scheduleSoon(ActionOrder.THINK,this);

        predictor = new FixedIncreasePurchasesPredictor(0);
    }



    @Override
    public void step(SimState state)
    {
        if(!active)
            return;

        assert state instanceof MacroII;
        MacroII macroII = (MacroII)state;


        int newWorkers = department.getNumberOfWorkersWhoConsumeWhatWePurchase();
        if(newWorkers != newestNumberOfWorkersObserved)
        {
            //remember the new workers
            olderNumberOfWorkersObserved = newestNumberOfWorkersObserved; //go off by one
            newestNumberOfWorkersObserved = newWorkers;
            assert olderNumberOfWorkersObserved != newestNumberOfWorkersObserved;

            //remember the day
            olderWorkerShockDay = lastWorkerShockDay;
            lastWorkerShockDay = (int) macroII.getMainScheduleTime();
            assert olderWorkerShockDay != lastWorkerShockDay;

        }

        macroII.scheduleTomorrow(ActionOrder.THINK,this);




    }

    /**
     * Predicts the future price of the next good to buy
     *
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    @Override
    public int predictPurchasePriceWhenIncreasingProduction(PurchasesDepartment dept) {
        updateModelIfNeeded();
        return predictor.predictPurchasePriceWhenIncreasingProduction(dept);

    }

    private void updateModelIfNeeded() {
        if(lastWorkerShockDay != -1) //if it's -1 we have no observations
        {
            int newLowBound = computeLowerBound();
            int newUpperBound = computeUpperBound();

            //if the bounds are all the same, no point in re-evaluating since the result would be the same!
            if ((newLowBound != lowerBoundObservation || newUpperBound != higherBoundObservation)
                    &&
                    newUpperBound>lastWorkerShockDay+1 && newLowBound < lastWorkerShockDay
                    )
            {
                //at least one bound is different!
                lowerBoundObservation = newLowBound;
                higherBoundObservation = newUpperBound;

                float deltaWorkers =  newestNumberOfWorkersObserved - olderNumberOfWorkersObserved;
                assert deltaWorkers!=0;
                float deltaPrice = computeAfterShockPrice() - computeBeforeShockPrice();
                if(!Float.isNaN(deltaPrice/deltaWorkers)) //NaN implies that there weren't enough good observations (probably because the few days before changing workers there was no production)x
                {

                    predictor.setIncrementDelta(deltaPrice/deltaWorkers);
                }
   //             System.out.println(predictor.getIncrementDelta());

            }


        }
    }

    /**
     * the slope is going to be:
     * (p2-p1)/(w2-w1)
     * This method computes p2
     * @return
     */
    protected float computeAfterShockPrice()
    {
        Preconditions.checkState(higherBoundObservation > lastWorkerShockDay, "not ready!");
        Preconditions.checkState(higherBoundObservation != -1, "not ready!");
        Preconditions.checkState(lastWorkerShockDay != -1,"not ready!");

        //make sure the dates are right!
        assert checkTheObservationsAreCorrect();

        //take the EMA
        ExponentialFilter<Double> afterShockPrice = new ExponentialFilter<>(weight);
        double[] observations = department.getObservationsRecordedTheseDays(PurchasesDataType.AVERAGE_CLOSING_PRICES,lastWorkerShockDay,higherBoundObservation);
        assert observations.length > 0;
        float sum = 0;
        for(double observation : observations)
        {
            if(observation >= 0)
                afterShockPrice.addObservation(observation);
            sum += observation;
        }
        //return it!
        // return sum/observations.length;
        return afterShockPrice.getSmoothedObservation();

    }


    /**
     * the slope is going to be:
     * (p2-p1)/(w2-w1)
     * This method computes p1
     * @return
     */
    protected float computeBeforeShockPrice()
    {
        Preconditions.checkState(lowerBoundObservation <= lastWorkerShockDay, "not ready!");
        Preconditions.checkState(lowerBoundObservation != -1, "not ready!");
        Preconditions.checkState(lastWorkerShockDay > 1,"not ready!");

        //make sure the dates are right!
        assert checkTheObservationsAreCorrect();

        //take the EMA
        ExponentialFilter<Double> beforeShockPrice = new ExponentialFilter<>(weight);
        double[] observations = department.getObservationsRecordedTheseDays(PurchasesDataType.AVERAGE_CLOSING_PRICES,
                lowerBoundObservation,lastWorkerShockDay-1);
        assert observations.length > 0;
        float sum = 0;
        for(double observation : observations)
        {
            if(observation >= 0)

                beforeShockPrice.addObservation(observation);
            sum += observation;
        }
        //return it!
        return beforeShockPrice.getSmoothedObservation();
        //return sum/observations.length;


    }

    /**
     * just a bunch of asserts grouped together
     * @return
     */
    private boolean checkTheObservationsAreCorrect() {
        assert department.getObservationRecordedThisDay(PurchasesDataType.WORKERS_CONSUMING_THIS_GOOD,lastWorkerShockDay)
                == newestNumberOfWorkersObserved; // shouldn't have changed!
        assert department.getObservationRecordedThisDay(PurchasesDataType.WORKERS_CONSUMING_THIS_GOOD,lastWorkerShockDay-1)
                != newestNumberOfWorkersObserved; // the day before it must have been different!
        assert department.getObservationRecordedThisDay(PurchasesDataType.WORKERS_CONSUMING_THIS_GOOD,lastWorkerShockDay-1)
                == olderNumberOfWorkersObserved; // the day before it must have been different!
        return true;
    }


    /**
     * which is the oldest day we need to look for?
     * @return the oldest day we need to look for
     */
    public int computeLowerBound()
    {
        int oldestDay = lastWorkerShockDay - Math.min(howManyDaysBackShallILook,lastWorkerShockDay-olderWorkerShockDay-1);
        return Math.max(0,oldestDay);
    }

    /**
     * what is the most recent day we need to look into?
     * @return
     */
    public int computeUpperBound()
    {
        int newestDay = lastWorkerShockDay + Math.max(Math.min(maximumNumberOfDaysToLookAhead,
                department.getLastObservedDay() - lastWorkerShockDay -1),0);
        assert newestDay >= lastWorkerShockDay;
        return newestDay;

    }


    /**
     * Predicts the last closing price
     *
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    @Override
    public int predictPurchasePriceWhenNoChangeInProduction(PurchasesDepartment dept) {
        return predictor.predictPurchasePriceWhenNoChangeInProduction(dept);
    }

    /**
     * Predicts the future price of the next good to buy
     *
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    @Override
    public int predictPurchasePriceWhenDecreasingProduction(PurchasesDepartment dept) {
        updateModelIfNeeded();
        return predictor.predictPurchasePriceWhenDecreasingProduction(dept);
    }

    /**
     * Call this to kill the predictor
     */
    @Override
    public void turnOff() {
        predictor.turnOff();
        active= false;
    }

    public float getCurrentSlope() {
        return predictor.getIncrementDelta();
    }


    /**
     * Sets new the exponential weight used.
     *
     * @param weight New value of the exponential weight used.
     */
    public void setWeight(float weight) {
        this.weight = weight;
    }

    /**
     * Gets the exponential weight used.
     *
     * @return Value of the exponential weight used.
     */
    public float getWeight() {
        return weight;
    }


    /**
     * Sets new how many days after the shock can you look into?.
     *
     * @param maximumNumberOfDaysToLookAhead New value of how many days after the shock can you look into?.
     */
    public void setMaximumNumberOfDaysToLookAhead(int maximumNumberOfDaysToLookAhead) {
        this.maximumNumberOfDaysToLookAhead = maximumNumberOfDaysToLookAhead;
    }

    /**
     * Gets how many days before the shock can you look into?.
     *
     * @return Value of how many days before the shock can you look into?.
     */
    public int getHowManyDaysBackShallILook() {
        return howManyDaysBackShallILook;
    }

    /**
     * Gets how many days after the shock can you look into?.
     *
     * @return Value of how many days after the shock can you look into?.
     */
    public int getMaximumNumberOfDaysToLookAhead() {
        return maximumNumberOfDaysToLookAhead;
    }

    /**
     * Sets new how many days before the shock can you look into?.
     *
     * @param howManyDaysBackShallILook New value of how many days before the shock can you look into?.
     */
    public void setHowManyDaysBackShallILook(int howManyDaysBackShallILook) {
        this.howManyDaysBackShallILook = howManyDaysBackShallILook;
    }
}
