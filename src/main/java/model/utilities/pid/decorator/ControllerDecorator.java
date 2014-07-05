/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.pid.decorator;

import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.NonDrawable;
import model.utilities.pid.Controller;
import model.utilities.pid.ControllerInput;
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
 * @author carrknight
 * @version 2012-11-14
 * @see
 */
@NonDrawable
public abstract class ControllerDecorator implements Controller
{


    protected Controller toDecorate;


    public ControllerDecorator(Controller toDecorate) {
        this.toDecorate = toDecorate;
    }





    public Controller getToDecorate() {
        return toDecorate;
    }

    @Override
    public void setOffset(float initialPrice, boolean resetAfterSetting) {
        toDecorate.setOffset(initialPrice, true);
    }


    /**
     * Get the sampling speed of the controller (how often it updates)
     * @return the sampling speed
     */
    @Override
    public int getSpeed() {
        return toDecorate.getSpeed();
    }

    /**
     * Set the sampling speed of the controller (how often it updates)
     * @param samplingSpeed the sampling speed
     */
    @Override
    public void setSpeed(int samplingSpeed) {
        toDecorate.setSpeed(samplingSpeed);
    }

    /**
     * Get the "zero" of the controller
     * @return  the "zero" of the controller
     */
    @Override
    public float getOffset() {
        return toDecorate.getOffset();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ControllerDecorator{");
        sb.append("decorated=").append(toDecorate);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public float getDerivativeGain() {
        return toDecorate.getDerivativeGain();
    }

    @Override
    public float getIntegralGain() {
        return toDecorate.getIntegralGain();
    }

    @Override
    public float getProportionalGain() {
        return toDecorate.getProportionalGain();
    }

    /**
     * setting 3 parameters. I am using here the PID terminology even though it doesn't have to be the case.
     * @param proportionalGain the first parameter
     * @param integralGain the second parameter
     * @param derivativeGain the third parameter
     */
    @Override
    public void setGains(float proportionalGain, float integralGain, float derivativeGain) {
        toDecorate.setGains(proportionalGain, integralGain, derivativeGain);
    }

    /**
     * The adjust is the main part of the a controller. It checks the new error and set the MV (which is the price, really)
     *  @param input the controller input object holding the state variables (set point, current value and so on)
     * @param isActive are we active?
     * @param simState a link to the model (to adjust yourself)
     * @param user the user who calls the PID (it needs to be steppable since the PID doesn't adjust itself)
     * @param phase at which phase should this controller be rescheduled
     *
     */
    @Override
    public void adjust(ControllerInput input, boolean isActive, MacroII simState, Steppable user, ActionOrder phase) {
        toDecorate.adjust(input, isActive, simState, user, phase);
    }

    /**
     * Get the current u_t
     */
    @Override
    public float getCurrentMV() {
        return toDecorate.getCurrentMV();
    }
}
