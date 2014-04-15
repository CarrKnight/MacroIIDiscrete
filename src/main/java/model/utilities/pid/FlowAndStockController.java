/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.pid;

import ec.util.MersenneTwisterFast;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.filters.ExponentialFilter;
import sim.engine.Steppable;


/**
 * <h4>Description</h4>
 * <p/> This is a weird cascade PID, sort of the reverse. It targets the flow first and then, when it's correct, an auxiliary
 * pid kicks in trying to get the right inventory too
 * <p/> It needs 2 inputs and 4 targets:
 * <ul>
 *     <li>Input 0 : stock</li>
 *     <li>Input 1 : inflow</li>
 * </ul>
 * <ul>
 *     <li>Target 0 : target stock</li>
 *     <li>Target 1 : outflow</li>
 *     <li>Target 2:  0 if the current stock is acceptable, any other number otherwise
 * </ul>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2012-12-30
 * @see
 */
public class FlowAndStockController implements Controller{

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


    long priceQuoted = 0;

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
     * Creates a flow and stock controller, chooses to filter exponentially with weight 0.5
     * @param proportional1 the proportional gain for flow
     * @param integrative1 the integrative gain for flow
     * @param derivative1 the derivative gain for flow
     * @param proportional2 the proportional gain for stock
     * @param integrative2 the integrative gain for stock
     * @param derivative2 the derivative gain for stock
     * @param random a randomizer, used by the individual pid controllers
     */
    public FlowAndStockController(float proportional1, float integrative1, float derivative1,
                                  float proportional2, float integrative2, float derivative2,
                                  MersenneTwisterFast random)
    {
        this(proportional1, integrative1, derivative1, proportional2, integrative2, derivative2, random,
                true,.5f);

    }

    /**
     * Creates a flow and stock controller with given gains for the flow PID and the stock PID
     * @param proportional1 the proportional gain for flow
     * @param integrative1 the integrative gain for flow
     * @param derivative1 the derivative gain for flow
     * @param proportional2 the proportional gain for stock
     * @param integrative2 the integrative gain for stock
     * @param derivative2 the derivative gain for stock
     * @param filtered true if the controller should filter its inputs
     * @param random a randomizer, used by the individual pid controllers
     */
    public FlowAndStockController(float proportional1, float integrative1, float derivative1,
                                  float proportional2, float integrative2, float derivative2,
                                  MersenneTwisterFast random,
                                  boolean filtered,float filterWeight) {
        this.flowPIDRoot =  new PIDController(proportional1,integrative1,derivative1,random);
        flowPID = flowPIDRoot;
        this.stockPID = new PIDController(proportional2,integrative2,derivative2,random);

        if(filtered)
        {
            inflowFilterer = new ExponentialFilter<>(filterWeight);
            outflowFilterer = new ExponentialFilter<>(filterWeight);
        }

        stockPID.setCanGoNegative(true); stockPID.setWindupStop(false);

    }


    /**
     * Set the sampling speed of the controller (how often it updates)
     *
     * @param samplingSpeed the sampling speed
     */
    @Override
    public void setSpeed(int samplingSpeed) {
        flowPID.setSpeed(samplingSpeed);
        stockPID.setSpeed(samplingSpeed);

    }

    /**
     * Get the sampling speed of the controller (how often it updates)
     *
     * @return the sampling speed
     */
    @Override
    public int getSpeed() {
        return flowPID.getSpeed();
    }

    /**
     * The adjust is the main part of the a controller. It checks the new error and set the MV (which is the price, really)
     *
     * @param input
     * <p/> It needs 2 inputs and 4 targets:
     * <ul>
     *     <li>Input 0 : stock</li>
     *     <li>Input 1 : inflow</li>
     * </ul>
     * <ul>
     *     <li>Target 0 : target stock</li>
     *     <li>Target 1 : outflow</li>
     *     <li>Target 2:  0 if the current stock is acceptable, any other number otherwise
     * </ul>
     * <p/>
     * @param isActive are we active?
     * @param simState a link to the model (to adjust yourself)
     * @param user     the user who calls the PID (it needs to be steppable since the PID doesn't adjust itself)
     */
    @Override
    public void adjust(ControllerInput input,  boolean isActive, MacroII simState, Steppable user,ActionOrder phase) {

        adjust((int)input.getTarget(1),(int)input.getInput(1),input.getInput(0),input.getTarget(0),
                input.getTarget(2)==0,isActive,simState,user,phase);


    }

    public void adjust(int inflow, int outflow, float currentStock,
                       float targetStock, boolean stockAcceptable,
                       boolean isActive, MacroII state,  Steppable user, ActionOrder phase)
    {

        //record inflow and outflow
        long oldPrice = priceQuoted;


        //adjust the flow PID if stock wasn't used before
        if(!stockAdjustmentUsed){

            filterObservations(inflow,outflow);

            flowPID.adjust(ControllerInput.simplePIDTarget(getOutflowAsFloat(inflow),getInflowAsFloat(outflow))
                    ,isActive,state,user,phase);
        }
        else
        {
            //we need to schedule ourselves since the PID won't do it
            state.scheduleTomorrow(phase,user);
        }
        stockAdjustmentUsed = false;

        boolean isInflowEqualOutflow = isInflowEqualOutflow(getInflowAsFloat(inflow), getOutflowAsFloat(outflow));


        //if we get the flow right, think of the stock
        if(isInflowEqualOutflow)
        {
            //if the flow is right, let's deal with the stock too
            if(stockAcceptable)
            {
                //we are going to just use the flow to price it, then
                priceQuoted = Math.max(Math.round(flowPID.getCurrentMV()),0);
                //reset stockPID
                stockPID.setOffset(0);
            }
            else{
                stockPID.adjustOnce(targetStock, currentStock,
                        isActive);
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


    }




    /**
     * inflow is equal to outflow if the difference is less than 0.2.
     * If inflow and the outflow aren't smoothed, that just means they have to be the same!
     * @return true if they are the same
     */
    private boolean isInflowEqualOutflow(float inflow, float outflow) {

        //  return super.getInflow() == super.getOutflow();
        return Math.abs(inflow - outflow) < .2f;
    }

    private void filterObservations(int inflow, int outflow) {
        if(inflowFilterer != null)
            inflowFilterer.addObservation(inflow);
        if(outflowFilterer != null)
            outflowFilterer.addObservation(outflow);
    }


    /**
     * The number of goods bought since the last reset, as a float to accomodate smoothing, if necessary
     */
    protected float getInflowAsFloat(int currentInflow) {
        if(inflowFilterer == null || Float.isNaN(inflowFilterer.getSmoothedObservation()))
            return currentInflow;
        else
            return inflowFilterer.getSmoothedObservation();
    }

    /**
     * the number of goods consumed since the last reset, as a float to accomodate smoothing, if necessary.
     * @return
     */
    public float getOutflowAsFloat(int currentOutflow) {
        if(outflowFilterer == null || Float.isNaN(outflowFilterer.getSmoothedObservation()))
            return currentOutflow;
        else
            return outflowFilterer.getSmoothedObservation();
    }

    /**
     * Get the current u_t
     */
    @Override
    public float getCurrentMV() {
        return priceQuoted;

    }

    /**
     * Set the "zero" of the controller
     *
     * @param initialPrice the "zero" of the controller
     */
    @Override
    public void setOffset(float initialPrice) {
        flowPID.setOffset(initialPrice);

    }

    /**
     * Get the "zero" of the controller
     *
     * @return the "zero" of the controller
     */
    @Override
    public float getOffset() {
        return flowPID.getOffset();
    }


    public PIDController getFlowPIDRoot() {
        return flowPIDRoot;
    }
}
