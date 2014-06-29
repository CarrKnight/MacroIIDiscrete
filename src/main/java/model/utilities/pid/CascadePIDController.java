/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.pid;

import ec.util.MersenneTwisterFast;
import model.MacroII;
import model.utilities.ActionOrder;
import sim.engine.Steppable;


/**
 * <h4>Description</h4>
 * <p/> This is a cascade control: two PIDs so that the Mv of the first is the setpoint of the second.
 * Basically like the human centipede but less functional and more hideous.
 * <p/> Notice that, at least by default, the first pid controller can go negative and doesn't stop windup.
 * <p/>  This is a bit of mess: the controller interface plays very poorly with this design
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2012-12-16
 * @see
 */
public class CascadePIDController implements Controller {
    private float targetForSlavePID;

    /**
     * The master PID
     */
    private PIDController pid1;

    /**
     * The slave PID
     */
    private PIDController pid2Root;


    /**
     * The slave controller
     */
    private Controller pid2;




    /**
     * when this is set to true, the second PID input is the second target - the original second input
     */

    public CascadePIDController(float proportional1, float integrative1, float derivative1,
                                float proportional2, float integrative2, float derivative2,
                                MersenneTwisterFast random) {
        pid1 = new PIDController(proportional1,integrative1,derivative1,random);
        //    pid1.setCanGoNegative(true); pid1.setWindupStop(false);
        pid1.setCanGoNegative(false); pid1.setWindupStop(true);
        pid2Root = new PIDController(proportional2,integrative2,derivative2,pid1.getSpeed(),random);
        pid2Root.setCanGoNegative(true); pid2Root.setWindupStop(false);
        pid2=pid2Root;


    }



    /**
     * This is the MV of the first PID and the set point of the second
     * @return y^* of the second PID
     */
    public float getTargetForSlavePID() {
        return targetForSlavePID;
    }

    /**
     * The adjust is the main part of the a controller. It checks the new error and set the MV (which is the price, really)
     *
     * @param input    the controller input must have 2 inputs and 1 target
     * @param isActive are we active?
     * @param simState a link to the model (to adjust yourself)
     * @param user     the user who calls the PID (it needs to be steppable since the PID doesn't adjust itself)
     */
    @Override
    public void adjust(ControllerInput input, boolean isActive,  MacroII simState,  Steppable user,
                       ActionOrder phase){

        this.adjust(input.getStockTarget(),input.getStockInput(),input.getFlowInput(),isActive,simState,user, phase);

    }


    /**
     /**
     * The adjust is the main part of the a controller. It checks the new error and set the MV (which is the price, really)
     *
     * @param stockTarget the stock target
     * @param stockInput the stock input
     * @param flowInput the flow input
     * @param isActive true if the pid is not turned off
     * @param state   the simstate link to schedule the user
     * @param user     the user who calls the PID (it needs to be steppable since the PID doesn't adjust itself)
     */
    public void adjust(float stockTarget,float stockInput, float flowInput, boolean isActive,
                        MacroII state,  Steppable user,  ActionOrder phase)
    {
        //master
        pid1.adjust(stockTarget,stockInput,isActive,state,user,phase); //to avoid exxaggerating in disinvesting, the recorded inventory is never more than twice the target
        //slave

        targetForSlavePID = pid1.getCurrentMV();
        // targetForSlavePID = masterOutput > 1 ? (float)Math.log(masterOutput) : masterOutput < -1 ? -(float)Math.log(-masterOutput) : 0;
        //

        assert !Float.isNaN(targetForSlavePID) && !Float.isInfinite(targetForSlavePID);
        ControllerInput secondPIDInput = new ControllerInput(targetForSlavePID,flowInput);
        pid2.adjust(secondPIDInput, isActive, null, null, null);


    }

    /**
     * Get the current u_t
     */
    public float getCurrentMV() {
        return pid2.getCurrentMV();
    }

    /**
     * Set initial offset of the second PID
     * @param initialPrice the offset
     */
    @Override
    public void setOffset(float initialPrice) {
        pid2.setOffset(initialPrice);
    }


    /**
     * Get the "zero" of the second controller
     *
     * @return the "zero" of the second controller
     */
    @Override
    public float getOffset() {
        return pid2.getOffset();
    }


    /**
     * change the speed of the cascade controller
     * @param speed the new speed
     */
    public void setSpeed(int speed) {
        pid1.setSpeed(speed);
        pid2.setSpeed(speed); //this is actually useless, but I think it helps when I debug
    }

    /**
     * Get the sampling speed of the controller (how often it updates)
     *
     * @return the sampling speed
     */
    @Override
    public int getSpeed() {
        return pid1.getSpeed();

    }


    /**
     * sets the parameters for the SLAVE PID
     *
     * @param proportionalGain the first parameter
     * @param integralGain     the second parameter
     * @param derivativeGain   the third parameter
     */
    @Override
    public void setGains(float proportionalGain, float integralGain, float derivativeGain) {
        setGainsSlavePID(proportionalGain,integralGain,derivativeGain);
    }

    /**
     * Change the gains of the second PID
     */
    public void setGainsSlavePID(float proportionalGain, float integralGain, float derivativeGain) {
        pid2Root.setGains(proportionalGain, integralGain, derivativeGain);
    }

    /**
     * Get the proportional gain of the first PID
     * @return the proportional gain of the first PID
     */
    public float getMasterProportionalGain() {
        return pid1.getProportionalGain();
    }

    /**
     * Get the integral gain of the first PID
     * @return the integral gain of the first PID
     */
    public float getMasterIntegralGain() {
        return pid1.getIntegralGain();
    }

    /**
     * Get the derivative gain of the first PID
     * @return the derivative gain of the first PID
     */
    public float getMasterDerivativeGain() {
        return pid1.getDerivativeGain();
    }

    /**
     * returns the slave P
     * @return
     */
    @Override
    public float getProportionalGain() {
        return getSlaveProportionalGain();
    }
    /**
     * returns the slave I
     * @return
     */
    @Override
    public float getIntegralGain() {
        return getSlaveIntegralGain();
    }

    /**
     * returns the slave D
     * @return
     */
    @Override
    public float getDerivativeGain() {
        return getSlaveDerivativeGain();
    }

    /**
     * Get the proportional gain of the second PID
     * @return the proportional gain of the second PID
     */
    public float getSlaveProportionalGain() {
        return pid2Root.getProportionalGain();
    }

    /**
     * Get the integral gain of the second PID
     * @return the integral gain of the second PID
     */
    public float getSlaveIntegralGain() {
        return pid2Root.getIntegralGain();
    }

    /**
     * Get the derivative gain of the second PID
     * @return the derivative gain of the second PID
     */
    public float getSlaveDerivativeGain() {
        return pid2Root.getDerivativeGain();
    }

    public void setGainsMasterPID(float proportionalGain, float integralGain, float derivativeGain) {
        pid1.setGains(proportionalGain,integralGain,derivativeGain);
    }

    public void setMasterCanGoNegative(boolean canGoNegative) {
        pid1.setCanGoNegative(canGoNegative);
    }

    public boolean isMasterCanGoNegative() {
        return pid1.isCanGoNegative();
    }

    public void setMasterWindupStop(boolean windupStop) {
        pid1.setWindupStop(windupStop);
    }

    public boolean isMasterWindupStop() {
        return pid1.isWindupStop();
    }
    public void setSlaveCanGoNegative(boolean canGoNegative) {
        pid2Root.setCanGoNegative(canGoNegative);
    }

    public boolean isSlaveCanGoNegative() {
        return pid2Root.isCanGoNegative();
    }

    public void setSlaveWindupStop(boolean windupStop) {
        pid2Root.setWindupStop(windupStop);
    }

    public boolean isSlaveWindupStop() {
        return pid2Root.isWindupStop();
    }



    public float getMasterMV()
    {
        return pid1.getCurrentMV();
    }


    public float getMasterError() {
        return pid1.getNewError();
    }

    public float getSlaveError() {
        return pid2Root.getNewError();
    }






    @Override
    public String toString() {
        return "CascadePIDController{" + "targetForSlavePID=" + targetForSlavePID + "\n pid1=" + pid1 + "\n pid2Root=" + pid2Root + "\n pid2=" + pid2 + '}';
    }
}
