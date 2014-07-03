/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.pid.decorator;

import agents.firm.Department;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.pid.ControllerInput;
import model.utilities.pid.PIDController;
import model.utilities.stats.regression.KalmanFOPDTRegressionWithUnknownTimeDelay;
import model.utilities.stats.regression.SISORegression;
import sim.engine.Steppable;

/**
 * <h4>Description</h4>
 * <p> This should really only decorate PID controllers.
 * <p> Fits a FOTPD by scatter-shot regressions, then uses:
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-07-01
 * @see
 */
public class FOPDTAutotuner extends ControllerDecorator {

    /**
     * optional: if given the autotuner will not record until the department has at least one trade
     */
    private final Department linkedDepartment;

    private final SISORegression regression;

    private final PIDController decoratedCasted;

    private int afterHowManyDaysShouldTune = 100;

    private int observations = 0;

    public FOPDTAutotuner(PIDController toDecorate) {

        this(toDecorate,null);
    }


    /**
     *
     * @param toDecorate the PID controller to deal with
     * @param department nullable: if given the autotuner will not record until the department has at least one trade
     */
    public FOPDTAutotuner(PIDController toDecorate, Department department) {
        super(toDecorate);
        decoratedCasted =toDecorate;
        this.linkedDepartment = department;
        regression = new KalmanFOPDTRegressionWithUnknownTimeDelay(0,1,2,5,10,50,100);


    }




    /**
     * The adjust is the main part of the a controller. It checks the new error and set the MV (which is the price, really)
     *
     * @param input    the controller input object holding the state variables (set point, current value and so on)
     * @param isActive are we active?
     * @param simState a link to the model (to adjust yourself)
     * @param user     the user who calls the PID (it needs to be steppable since the PID doesn't adjust itself)
     * @param phase    at which phase should this controller be rescheduled
     */
    @Override
    public void adjust(ControllerInput input, boolean isActive, MacroII simState, Steppable user, ActionOrder phase) {

        if(linkedDepartment == null || linkedDepartment.hasTradedAtLeastOnce())
            learn(input);
        if(observations > afterHowManyDaysShouldTune) {
            final float kc = (regression.getTimeConstant() / (regression.getTimeConstant() + regression.getDelay())) / regression.getGain();
            final float ti = regression.getTimeConstant();
            setGains(kc, kc/ti,0);
        }

        super.adjust(input, isActive, simState, user, phase);
    }

    private void learn(ControllerInput input) {
        if (isControllingFlows())
            regression.addObservation(input.getFlowInput(), getCurrentMV());
        else
            regression.addObservation(input.getFlowTarget(), getCurrentMV());
        observations++;
    }

    public boolean isControllingFlows() {
        return decoratedCasted.isControllingFlows();
    }

    public void setControllingFlows(boolean controllingFlows) {
        decoratedCasted.setControllingFlows(controllingFlows);
    }

    public int getAfterHowManyDaysShouldTune() {
        return afterHowManyDaysShouldTune;
    }

    public void setAfterHowManyDaysShouldTune(int afterHowManyDaysShouldTune) {
        this.afterHowManyDaysShouldTune = afterHowManyDaysShouldTune;
    }

    public int getDelay() {
        return regression.getDelay();
    }

    public float getGain() {
        return regression.getGain();
    }

    public float getTimeConstant() {
        return regression.getTimeConstant();
    }
}
