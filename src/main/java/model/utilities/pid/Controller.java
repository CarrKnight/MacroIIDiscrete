/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.pid;

import model.MacroII;
import model.utilities.ActionOrder;
import sim.engine.Steppable;


/**
 * <h4>Description</h4>
 * <p/> Any object that can be used to adjust policies to hit targets given outputs.
 * <p/> Each inheritor MUST have a constructor that only takes a mersenne twister fast
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
public interface Controller {


    /**
     * The adjust is the main part of the a controller. It checks the new error and set the MV (which is the price, really)
     *
     * @param input the controller input object holding the state variables (set point, current value and so on)
     * @param isActive are we active?
     * @param simState a link to the model (to adjust yourself)
     * @param user the user who calls the PID (it needs to be steppable since the PID doesn't adjust itself)
     * @param phase at which phase should this controller be rescheduled
     *
     */
    public void adjust(ControllerInput input, boolean isActive,  MacroII simState,  Steppable user,
                       ActionOrder phase);


    /**
     * Get the current u_t
     */
    public float getCurrentMV();


    /**
     * Set the "zero" of the controller
     * @param initialPrice the "zero" of the controller
     * @param resetAfterSetting
     */
    public void setOffset(float initialPrice, boolean resetAfterSetting);

    /**
     * Get the "zero" of the controller
     * @return  the "zero" of the controller
     */
    public float getOffset();

    /**
     * Set the sampling speed of the controller (how often it updates, in days)
     * @param samplingSpeed the sampling speed
     */
    public void setSpeed(int samplingSpeed);

    /**
     * Get the sampling speed of the controller (how often it updates, in days)
     * @return the sampling speed
     */
    public int getSpeed();


    /**
     * setting 3 parameters. I am using here the PID terminology even though it doesn't have to be the case.
     * @param proportionalGain the first parameter
     * @param integralGain the second parameter
     * @param derivativeGain the third parameter
     */
    public void setGains(float proportionalGain,float integralGain, float derivativeGain);


    public float getProportionalGain();

    public float getIntegralGain();

    public float getDerivativeGain();



}
