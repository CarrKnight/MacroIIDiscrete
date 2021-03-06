/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.pid;

import com.google.common.base.Preconditions;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.scheduler.Priority;
import sim.engine.Steppable;


/**
 * <h4>Description</h4>
 * <p/>
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-14
 * @see
 */
public class PIDController implements Controller {



    /**
     * The proportional gain of the PID controller
     */
    private float proportionalGain = 1;

    /**
     * The integral gain of the PID controller
     */
    private float integralGain = 1;

    /**
     * The derivative gain of the PID controller
     */
    private float derivativeGain = 1;

    /**
     * How much time passes between one PID check and the next?
     */
    private int speed = 0;

    /**
     * This will sum all the previous errors
     */
    private float integral = 0;

    /**
     * This is the previous adjust error term.
     */
    private float oldError = Float.NaN;

    /**
     * This is the latest adjust error term.
     */
    private float newError = Float.NaN;

    /**
     * This is actually current price, I call it MV because that's PID terminology
     */
    private float currentMV = 0;

    /**
     * if this is true, it stops increase the integral part as long as it is at saturation (MV <=0)
     */
    private boolean windupStop = true;

    /**
     * This is an initial offset if we don't want to start by pricing 0
     */
    private float initialPrice = 0;

    /**
     * if random speed is true then the PID speed is not constant but changes slightly over time
     */
    //todo make random speed pluggable. It's weird to just have it random walk
    private boolean randomSpeed = false;


    /**
     * MV can go below 0 if this is true
     */
    private boolean canGoNegative = false;

    private boolean controllingFlows = true;

    /**
     * when this is true, error is -(target-process variable)
     */
    private boolean invertSign = false;





    public PIDController(float proportionalGain, float integralGain, float derivativeGain) {
        this(proportionalGain,integralGain,derivativeGain,0);

    }


    public PIDController(float proportionalGain, float integralGain, float derivativeGain, int speed) {
        this.proportionalGain = proportionalGain;
        this.integralGain = integralGain;
        this.derivativeGain = derivativeGain;
        this.speed = speed;

    }

    public PIDController(PIDController toClone){
        this.proportionalGain = toClone.proportionalGain;
        this.integralGain = toClone.integralGain;
        this.derivativeGain=toClone.derivativeGain;
        this.canGoNegative = toClone.canGoNegative;
        this.controllingFlows = toClone.controllingFlows;
        this.randomSpeed = toClone.randomSpeed;
        this.initialPrice = toClone.initialPrice;
        this.windupStop = toClone.windupStop;
        this.currentMV = toClone.currentMV;
        this.newError = toClone.newError;
        this.oldError = toClone.oldError;
        this.speed = toClone.speed;
        this.integral = toClone.integral;
        this.invertSign = toClone.invertSign;

    }

    /**
     * Change the gains of the PID
     */
    public void setGains(float proportionalGain,float integralGain, float derivativeGain)
    {
        this.proportionalGain = proportionalGain;
        this.integralGain = integralGain;
        this.derivativeGain = derivativeGain;

        if(Float.isFinite(getCurrentMV()) && Float.isFinite(getOldError()) && Float.isFinite(getNewError()) && integralGain != 0 )
            integral = sumOfErrorsNecessaryForFormulaToBeX(getCurrentMV(),initialPrice,getOldError(),getNewError());
    }


    public boolean isCanGoNegative() {
        return canGoNegative;
    }

    public void setCanGoNegative(boolean canGoNegative) {
        this.canGoNegative = canGoNegative;
    }

    /**
     * The adjust is the main part of the PID controller. It checks the new error and set the MV (which is the price, really)     *
     * @param input    the controller input object holding the state variables (set point, current value and so on)
     * @param isActive are we active?
     * @param simState a link to the model (to reschedule the user)
     * @param user     the user who calls the PID (it needs to be steppable since the PID doesn't adjust itself)
     * @param phase at which phase should this controller be rescheduled
     */
    @Override
    public void adjust(ControllerInput input, boolean isActive, MacroII simState, Steppable user,ActionOrder phase) {
        if(controllingFlows)
            this.adjust(input.getFlowTarget(),input.getFlowInput(),isActive,simState,user,phase);
        else
            this.adjust(input.getStockTarget(),input.getStockInput(),isActive,simState,user,phase);

    }

    /**
     * The adjust is the main part of the PID Controller. This method doesn't compute but rather receive the new error
     * and just perform the PID magic on it
     * @param residual the residual/error: the difference between current value of y and its target
     * @param isActive is the agent calling this still alive and active?
     * @param simState a link to the model (to reschedule the user)
     * @param user     the user who calls the PID (it needs to be steppable since the PID doesn't adjust itself)
     * @param phase at which phase should this controller be rescheduled
     */
    public void adjust(float residual, boolean isActive,
                       MacroII simState,  Steppable user,ActionOrder phase, Priority priority)
    {
        //delegate the PID itself to adjustOnce, and worry about refactoring
        if (!adjustOnce(residual, isActive))
            return;


        /*************************
         * Reschedule
         *************************/


        if(simState != null && user != null){
            if(speed == 0)
                simState.scheduleTomorrow(phase, user,priority);
            else
            {
                assert speed > 0;
                simState.scheduleAnotherDay(phase,user,speed+1,priority);
            }
        }

    }

    /**
     * The adjust is the main part of the PID controller. It checks the new error and set the MV (which is the price, really)
     *
     * @param target the inventory we want to target this adjust
     * @param current the inventory we have
     * @param isActive are we active?
     * @param simState a link to the model (to adjust yourself) [can be null]
     * @param user the user who calls the PID (it needs to be steppable since the PID doesn't adjust itself) [can be null]
     * @param phase at which phase should this controller be rescheduled
     *
     */
    public void adjust(float target, float current, boolean isActive,
                       MacroII simState,  Steppable user,ActionOrder phase) {

        adjust(target, current, isActive, simState, user, phase,Priority.STANDARD);

    }


    /**
     * The adjust is the main part of the PID controller. It checks the new error and set the MV (which is the price, really)
     *
     * @param target the inventory we want to target this adjust
     * @param current the inventory we have
     * @param isActive are we active?
     * @param simState a link to the model (to adjust yourself) [can be null]
     * @param user the user who calls the PID (it needs to be steppable since the PID doesn't adjust itself) [can be null]
     * @param phase at which phase should this controller be rescheduled
     *
     */
    public void adjust(float target, float current, boolean isActive,
                       MacroII simState,  Steppable user,ActionOrder phase, Priority priority) {
        //get the residual and delegate to the residual method
        float residual = target - current;
        adjust(residual,isActive,simState,user,phase,priority);

    }

    /**
     * This adjusts the PID but doesn't reschedule itself. Useful for multiple PIDs at the same time where we want just one scheduling
     * @param target the set point
     * @param current the current value of y
     * @param isActive the user is active
     * @return just returns isActive
     */
    public boolean adjustOnce(float target, float current, boolean isActive) {
        if(!isActive)
            return false;

        //Compute residual and delegate to the other adjustOnce

        float residual = target - current; //this is your new error
        return adjustOnce(residual,isActive);



    }

    /**
     * This adjusts the PID but doesn't reschedule itself. Useful for multiple PIDs at the same time where we want just one scheduling
     * @param residual the difference between target and current value of y
     * @param isActive the user is active
     * @return just returns isActive
     */
    public boolean adjustOnce(float residual, boolean isActive) {
        if(!isActive)
            return false;

        if(invertSign)
            residual = -residual;


        /*************************
         *RECORDING
         ************************/
        oldError= newError; //shift errors down
        newError = residual; //this is your new error

        /*************************
         * PID FORMULA
         *************************/
        float derivative = 0;
        if(!Float.isNaN(oldError))    //if you can count the derivative too
            derivative = newError - oldError;



        integral += newError; //integral!
        if(windupStop && formula(derivative) < 0) {
            if (integralGain != 0)
                integral = sumOfErrorsNecessaryForFormulaToBe0(initialPrice, oldError, newError);
            else {
                currentMV = 0;
                return true;
            }
        }




        //initialPrice is the offset, the rest is your standard PID
        currentMV = formula(derivative);
        if(!canGoNegative && currentMV < 0)
            currentMV = 0; //bound to 0
        return true;
    }


    private float sumOfErrorsNecessaryForFormulaToBe0(float baseline, float errorLastPeriod, float errorThisPeriod)
    {
        Preconditions.checkArgument(Float.isFinite(baseline));
        return sumOfErrorsNecessaryForFormulaToBeX(0,baseline,errorLastPeriod,errorThisPeriod);

    }

    private float sumOfErrorsNecessaryForFormulaToBeX(float x,float baseline, float errorLastPeriod, float errorThisPeriod)
    {
        //ignore nans. they mean the pid hasn't started which means they are 0
        errorLastPeriod = Float.isNaN(errorLastPeriod) ? 0 : errorLastPeriod;
        errorThisPeriod = Float.isNaN(errorThisPeriod) ? 0 : errorThisPeriod;

        return (
                (x - baseline
                        - (proportionalGain * errorThisPeriod)
                        - (derivativeGain * (errorThisPeriod-errorLastPeriod))
                )/integralGain
        );

    }


    /**
     * useful for resets
     * @param targetMV
     */
    public void setIntegralSoThatTheCurrentMVIsThis(float targetMV){
        integral = sumOfErrorsNecessaryForFormulaToBe0(initialPrice,oldError,newError);
    }


    private float formula(float derivative) {
        return initialPrice + proportionalGain * newError + integralGain * integral + derivativeGain * derivative;
    }


    public float getOffset() {
        return initialPrice;
    }

    /**
     * Whenever set the PID is reset
     */
    public void setOffset(float offset, boolean resetAfterSetting) {
        this.initialPrice = offset;
        if(resetAfterSetting) {
            integral = 0;
            oldError = Float.NaN;
            newError = Float.NaN;
            currentMV = offset;

        }
    }

    /**
     * Get the current u_t
     */
    public float getCurrentMV() {
        return currentMV;
    }

    public float getNewError() {
        return newError;
    }

    public float getOldError() {
        return oldError;
    }


    public float getProportionalGain() {
        return proportionalGain;
    }

    public void setProportionalGain(float proportionalGain) {
        this.proportionalGain = proportionalGain;
    }

    public float getIntegralGain() {
        return integralGain;
    }

    public void setIntegralGain(float integralGain) {
        this.integralGain = integralGain;
    }

    public float getDerivativeGain() {
        return derivativeGain;
    }

    public void setDerivativeGain(float derivativeGain) {
        this.derivativeGain = derivativeGain;
    }


    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public boolean isWindupStop() {
        return windupStop;
    }

    public void setWindupStop(boolean windupStop) {
        this.windupStop = windupStop;
    }

    public boolean isRandomSpeed() {
        return randomSpeed;
    }

    public void setRandomSpeed(boolean randomSpeed) {
        this.randomSpeed = randomSpeed;
    }

    public float getIntegral() {
        return integral;
    }

    public boolean isControllingFlows() {
        return controllingFlows;
    }

    public void setControllingFlows(boolean controllingFlows) {
        this.controllingFlows = controllingFlows;
    }

    @Override
    public String toString() {
        return "PIDController{" + "proportionalGain=" + proportionalGain + ", integralGain=" + integralGain + ", derivativeGain=" + derivativeGain + '}';
    }

    public boolean isInvertSign() {
        return invertSign;
    }

    public void setInvertSign(boolean invertSign) {
        this.invertSign = invertSign;
    }
}
