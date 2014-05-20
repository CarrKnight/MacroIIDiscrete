/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.pid;

import agents.HasInventory;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.inventoryControl.FixedInventoryControl;
import agents.firm.purchases.inventoryControl.Level;
import agents.firm.purchases.pricing.BidPricingStrategy;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.filters.ExponentialFilter;
import model.utilities.pid.Controller;
import model.utilities.pid.ControllerInput;
import model.utilities.pid.PIDController;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * <h4>Description</h4>
 * <p/>This PID is just a ugly cascade pid. There is an inner flow pid and a second
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2012-12-03
 * @see
 */
public class FlowAndStockFixedPID extends FixedInventoryControl implements BidPricingStrategy, Steppable
{


    /**
     * This PID will target inflow = outflow at all times
     */
    private Controller flowPID;

    /**
     * this is the flow original PID. so that if flowPID gets decorated we still are in contact with it!
     */
    final private PIDController flowPIDRoot;

    /**
     * This is the "auxiliary" PID that targets the stock
     */
    private PIDController stockPID;


    int priceQuoted = 0;

    /**
     * This flag is true whenever we modify the price offered by using stock adjustment. We need to keep track of it because we use it to turn off the flow pid to avoid it being hit by
     * weird disturbance from stock
     */
    private boolean stockAdjustmentUsed = false;

    /**
     * If our period is incorrect or our flows are not constant we'll need smoothing
     */
    private ExponentialFilter<Integer> inflowFilterer;

    /**
     * If our period is incorrect or our flows are not constant we'll need smoothing
     */
    private ExponentialFilter<Integer> outflowFilterer;


    /**
     * activates filtering for inflow and outflow
     * @param inputWeight the weight of the EMA used
     */
    public void smoothFlowPID(float inputWeight){

        inflowFilterer = new ExponentialFilter<>(inputWeight);

        outflowFilterer= new ExponentialFilter<>(inputWeight);


    }

    /**
     * Creates the flow and stock fixed PID by choosing random parameters.
     * @param dept the purchase department associated with it
     */
    public FlowAndStockFixedPID(PurchasesDepartment dept)
    {
        this(dept,(float) (.5f + dept.getRandom().nextGaussian()*.01f),
                (float) (.5f + dept.getRandom().nextGaussian()*.05f),
                (float) (0.01f + dept.getRandom().nextGaussian()*.005f),
                0
        );
        //create the 2 PIDs

    }

    public FlowAndStockFixedPID(PurchasesDepartment dept, float proportionalGain, float integralGain, float derivativeGain)
    {
        this(dept,proportionalGain,integralGain,derivativeGain,
                0);

    }

    public FlowAndStockFixedPID(PurchasesDepartment dept, float proportionalGain, float integralGain, float derivativeGain,
                                int speed)
    {
        super(dept);
        //create the 2 PIDs
        flowPIDRoot = new PIDController(proportionalGain,integralGain,derivativeGain, speed, dept.getRandom());
        flowPID = flowPIDRoot;
        stockPID = new PIDController(proportionalGain,integralGain,derivativeGain, speed, dept.getRandom());
        stockPID.setCanGoNegative(true); stockPID.setWindupStop(false);



    }

    /**
     * Set the parameters for the pid targeting the flow
     * @param proportionalGain proportional
     * @param integralGain integral
     * @param derivativeGain derivative
     */
    public void setGainsFlow( float proportionalGain, float integralGain, float derivativeGain){
        flowPIDRoot.setGains(proportionalGain,integralGain,derivativeGain);

    }

    /**
     * Set the parameters for the pid targeting the flow
     * @param proportionalGain proportional
     * @param integralGain integral
     * @param derivativeGain derivative
     */
    public void setGainsStock( float proportionalGain, float integralGain, float derivativeGain){
        stockPID.setGains(proportionalGain,integralGain,derivativeGain);

    }


    @Override
    public void step(SimState state) {


        //record inflow and outflow
        boolean isInflowEqualOutflow = isInflowEqualOutflow();
        boolean isStockAcceptable = super.rateInventory().equals(Level.ACCEPTABLE);
        long oldPrice = priceQuoted;


        //adjust the flow PID if stock wasn't used before
        if(!stockAdjustmentUsed){

            filterObservations();

            flowPID.adjust(ControllerInput.simplePIDTarget(getOutflowAsFloat(),getInflowAsFloat())
                    ,isActive(),(MacroII)state,this, ActionOrder.PREPARE_TO_TRADE);
        }
        else
        {
            //we need to schedule ourselves since the PID won't do it
            assert state instanceof MacroII;
            if(flowPIDRoot.getSpeed()==0)
                ((MacroII)state).scheduleTomorrow(ActionOrder.PREPARE_TO_TRADE,this);
            else{
                assert flowPIDRoot.getSpeed()>=1;
                ((MacroII)state).scheduleAnotherDay(ActionOrder.PREPARE_TO_TRADE,this,flowPIDRoot.getSpeed());
            }
        }
        stockAdjustmentUsed = false;


        //if we get the flow right, think of the stock
        if(isInflowEqualOutflow)
        {
            //if the flow is right, let's deal with the stock too
            if(isStockAcceptable)
            {
                //we are going to just use the flow to price it, then
                priceQuoted = Math.max(Math.round(flowPID.getCurrentMV()),0);
                //reset stockPID
                stockPID.setOffset(0);
            }
            else{
                stockPID.adjustOnce(super.getInventoryTarget(),
                        getPurchasesDepartment().getFirm().hasHowMany(getGoodTypeToControl()), isActive());
                stockAdjustmentUsed = true;
                priceQuoted = Math.max(Math.round(flowPID.getCurrentMV() + stockPID.getCurrentMV()),0);

                //    priceQuoted = Math.max(Math.round(flowPID.getCurrentMV()),0);

            }
        }
        else
        {
            //get the flow right first!
            priceQuoted =  Math.max(Math.round(flowPID.getCurrentMV()),0);


        }

        //reset inflows and outflows


        if(oldPrice != priceQuoted )
            getPurchasesDepartment().updateOfferPrices();


    }

    private void filterObservations() {
        if(inflowFilterer != null)
            inflowFilterer.addObservation(getPurchasesDepartment().getTodayInflow());
        if(outflowFilterer != null)
            outflowFilterer.addObservation(getPurchasesDepartment().getTodayOutflow());
    }

    /**
     * inflow is equal to outflow if the difference is less than 0.2.
     * If inflow and the outflow aren't smoothed, that just means they have to be the same!
     * @return true if they are the same
     */
    private boolean isInflowEqualOutflow() {

        //  return super.getInflow() == super.getOutflow();
        return Math.abs(getInflowAsFloat() - getOutflowAsFloat()) < .2f;
    }

    /**
     * The number of goods bought since the last reset, as a float to accomodate smoothing, if necessary
     */
    protected float getInflowAsFloat() {
        if(inflowFilterer == null || Float.isNaN(inflowFilterer.getSmoothedObservation()))
            return getPurchasesDepartment().getTodayInflow();
        else
            return inflowFilterer.getSmoothedObservation();
    }

    /**
     * the number of goods consumed since the last reset, as a float to accomodate smoothing, if necessary.
     * @return
     */
    public float getOutflowAsFloat() {
        if(outflowFilterer == null || Float.isNaN(outflowFilterer.getSmoothedObservation()))
            return getPurchasesDepartment().getTodayOutflow() + getPurchasesDepartment().getTodayFailuresToConsume();
        else
            return outflowFilterer.getSmoothedObservation();
    }

    /**
     * Answer the purchase strategy question: how much am I willing to pay for a good of this type?
     *
     * @param type the type of good you want to buy
     * @return the maximum price I am willing to pay for this good
     */
    @Override
    public int maxPrice(GoodType type) {
        return priceQuoted;

    }

    /**
     * Answer the purchase strategy question: how much am I willing to pay for this specific good?
     *
     * @param good the specific good being offered to you
     * @return the maximum price I am willing to pay for this good
     */
    @Override
    public int maxPrice(Good good) {
        return priceQuoted;
    }

    /**
     * set the speed for BOTH pid controllers
     * @param pidPeriod the pid sample speed
     */
    public void setSpeed(int pidPeriod) {
        flowPIDRoot.setSpeed(pidPeriod);
        stockPID.setSpeed(pidPeriod);



    }


    /**
     * This is used by the purchases department for either debug or to ask the inventory control if
     * we can accept offer from peddlers.<br>
     * Asserts expect this to be consistent with the usual behavior of inventory control. But if asserts are off, then there is no other check
     *
     * @return
     */
    @Override
    public boolean canBuy() {
        return !super.rateInventory().equals(Level.TOOMUCH) || !getPurchasesDepartment().getFirm().hasAny(getGoodTypeToControl());

    }

    /**
     * When instantiated the inventory control doesn't move until it receives the first good. With this function you can start it immediately.
     * Notice: THIS DOESN'T ACTIVATE TURNEDOFF controls. Turn off is irreversible
     */
    @Override
    public void start() {
        //step once, in order to schedule yourself and all

        step(getPurchasesDepartment().getFirm().getModel());
        //     priceQuoted = 40;  flowPID.setOffset(40);
        super.start();

    }


    /**
     * This is the method overriden by the subclass of inventory control to decide whether the current inventory levels call for a new good being bought
     *
     * @param source   The firm
     * @param type     The goodtype associated with this inventory control
     * @param quantity the new inventory level
     * @return true if we need to buy one more good
     */
    @Override
    protected boolean shouldIBuy(HasInventory source, GoodType type, int quantity) {

        return !super.rateInventory().equals(Level.TOOMUCH) || !getPurchasesDepartment().getFirm().hasAny(getGoodTypeToControl());
    }


}
