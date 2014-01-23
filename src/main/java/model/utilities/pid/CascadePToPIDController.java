/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */
package model.utilities.pid;



import cern.jet.random.engine.MersenneTwister;
import ec.util.MersenneTwisterFast;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.pid.Controller;
import model.utilities.pid.ControllerInput;
import model.utilities.pid.PIDController;
import model.utilities.pid.decorator.ExponentialFilterTargetDecorator;
import sim.engine.Steppable;

import javax.annotation.Nullable;

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
public class CascadePToPIDController implements Controller {
    private float secondTarget;

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


    public CascadePToPIDController(MacroII model) {
        this(1 / 30f, model.drawProportionalGain() / 5f, model.drawIntegrativeGain() / 5f, model.drawDerivativeGain(),model.getRandom());

    }

    public CascadePToPIDController(float proportional1,
                                   float proportional2, float integrative2, float derivative2,
                                   MersenneTwisterFast random) {

        pid1 = new PIDController(proportional1,0,0,random);

        //careful how you set up your controller!
        //somewhat counterintuitively we let the Master PID be a P only. Because our slave will have as input---> (Inflow-Outflow)
        //which should nicely be 0 at inventory.

        pid2Root = new PIDController(proportional2,integrative2,derivative2,random);
        pid2 = pid2Root;

        setMasterCanGoNegative(true); setMasterWindupStop(false);
        setSlaveCanGoNegative(false); setSlaveWindupStop(true);

        pid2 = new ExponentialFilterTargetDecorator(pid2,.8f);

    }



    /**
     * This is the MV of the first PID and the set point of the second
     * @return y^* of the second PID
     */
    public float getSecondTarget() {
        return secondTarget;
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
    public void adjust(ControllerInput input, boolean isActive, @Nullable MacroII simState, @Nullable Steppable user,
                       ActionOrder phase){

            float targetInventory = input.getTarget(0);
            float currentInventory = input.getInput(0);



            this.adjust(targetInventory,currentInventory,input.getTarget(1)-input.getInput(1),isActive,simState,user, phase);


    }


    /**
     /**
     * The adjust is the main part of the a controller. It checks the new error and set the MV (which is the price, really)
     *
     * @param firstTarget the stock target
     * @param firstInput the stock input
     * @param secondInput the flow input
     * @param isActive true if the pid is not turned off
     * @param state   the simstate link to schedule the user
     * @param user     the user who calls the PID (it needs to be steppable since the PID doesn't adjust itself)     * @param firstTarget
     */
    public void adjust(float firstTarget,float firstInput, float secondInput, boolean isActive,
                       @Nullable MacroII state, @Nullable Steppable user,  ActionOrder phase)
    {
        //master
        pid1.adjust(firstTarget,Math.min(firstInput,firstTarget*2),isActive,state,user,phase);
        //to avoid exxaggerating in disinvesting, the recorded inventory is never more than twice the target
        //slave
        secondTarget = pid1.getCurrentMV();
        //

        ControllerInput secondPIDInput = ControllerInput.simplePIDTarget(secondTarget,secondInput);
        pid2.adjust(secondPIDInput, isActive, null, null, null);
        //  System.out.println("targ additional inflow " + secondTarget + ", price: " + pid2.getCurrentMV() + ", old price: " + oldMV);

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
        final StringBuilder sb = new StringBuilder("CascadePIDController{");
        sb.append("secondTarget=").append(secondTarget);
        sb.append("\n pid1=").append(pid1);
        sb.append("\n pid2Root=").append(pid2Root);
        sb.append("\n pid2=").append(pid2);
        sb.append('}');
        return sb.toString();
    }


    public float getProportionalGain() {
        return pid2Root.getProportionalGain();
    }

    public float getIntegralGain() {
        return pid2Root.getIntegralGain();
    }


    public float getDerivativeGain() {
        return pid2Root.getDerivativeGain();
    }

    public void setProportionalGain(float v) {
        pid2Root.setProportionalGain(v);
    }

    public void setMasterProportionalGain(float proportionalGain) {
        pid1.setProportionalGain(proportionalGain);
    }
}
