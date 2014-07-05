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
import model.utilities.pid.tuners.ControlGuruTableFOPDT;
import model.utilities.pid.tuners.PIDTuningTable;
import model.utilities.stats.regression.SISOGuessingRegression;
import model.utilities.stats.regression.SISORegression;
import sim.engine.Steppable;

import java.util.function.Function;

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
public class PIDAutotuner extends ControllerDecorator {

    /**
     * optional: if given the autotuner will not record until the department has at least one trade
     */
    private final Department linkedDepartment;

    private final SISOGuessingRegression regression;

    private final PIDController decoratedCasted;

    private int afterHowManyDaysShouldTune = 200;

    private int observations = 0;

    private PIDTuningTable tuningTable = new ControlGuruTableFOPDT();

    float fallbackPolicy = Float.NaN;



    public PIDAutotuner(PIDController toDecorate) {

        this(toDecorate,null);
    }


    /**
     *
     * @param toDecorate the PID controller to deal with
     * @param department nullable: if given the autotuner will not record until the department has at least one trade
     */
    public PIDAutotuner(PIDController toDecorate, Department department) {
        super(toDecorate);
        decoratedCasted =toDecorate;
        this.linkedDepartment = department;
        regression = new SISOGuessingRegression(0,1,2,5,10,20);


    }


    /**
     *
     * @param toDecorate the PID controller to deal with
     * @param department nullable: if given the autotuner will not record until the department has at least one trade
     */
    public PIDAutotuner(PIDController toDecorate, Function<Integer,SISORegression> regressionBuilder, PIDTuningTable tuningTable,
                        Department department) {
        super(toDecorate);
        decoratedCasted =toDecorate;
        this.linkedDepartment = department;
        regression = new SISOGuessingRegression(regressionBuilder,0,1,2,5,10);
        this.tuningTable = tuningTable;


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
        {
            learn(input);
        }
        if(observations > afterHowManyDaysShouldTune) {

            final float processGain = regression.getGain();
            final float timeConstant = regression.getTimeConstant();
            final float intercept = regression.getIntercept();
            final int delay = regression.getDelay();

            System.out.println("regression results: " +processGain + "," + timeConstant + "," + delay +  "," + intercept);

            if(regression.isFallbackBetter())
                fallbackPolicy = regression.fallbackPolicy(input.getFlowTarget());
            else {
                fallbackPolicy = Float.NaN;
                final float targetP = tuningTable.getProportionalParameter(processGain, timeConstant, intercept, delay);
                final float targetI = tuningTable.getIntegralParameter(processGain, timeConstant, intercept, delay);
                final float targetD = tuningTable.getDerivativeParameter(processGain, timeConstant, intercept, delay);
                setGains(getProportionalGain() * .99f + targetP * .01f,
                        getIntegralGain() * .99f + targetI * .01f,
                        getDerivativeGain() * .99f + targetD * .01f
                );

            }
        }

        super.adjust(input, isActive, simState, user, phase);
    }


    /**
     * Get the current u_t
     */
    @Override
    public float getCurrentMV() {
        //if we think there is no dynamics, hijack it
        if(Float.isFinite(fallbackPolicy))
            return fallbackPolicy;
        else
            return super.getCurrentMV();
    }

    private void learn(ControllerInput input) {

        if (isControllingFlows()) {
            System.out.println(input.getFlowInput() + "," + getCurrentMV());

            regression.addObservation(input.getFlowInput(), getCurrentMV());
        }
        else {
            regression.addObservation(input.getStockInput(), getCurrentMV());
        }
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

    public PIDTuningTable getTuningTable() {
        return tuningTable;
    }

    public void setTuningTable(PIDTuningTable tuningTable) {
        this.tuningTable = tuningTable;
    }


}
