/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.pid.decorator;

import agents.firm.Department;
import com.google.common.annotations.VisibleForTesting;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.logs.LogEvent;
import model.utilities.logs.LogLevel;
import model.utilities.pid.ControllerInput;
import model.utilities.pid.PIDController;
import model.utilities.stats.processes.PIGradientDescent;
import model.utilities.stats.regression.AutoRegressiveWithInputRegression;
import model.utilities.stats.regression.MultipleModelRegressionWithSwitching;
import model.utilities.stats.regression.SISORegression;
import sim.engine.Steppable;

import java.util.function.Function;
import java.util.function.Predicate;

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

    private final MultipleModelRegressionWithSwitching regression;

    private final PIDController decoratedCasted;

    private int afterHowManyDaysShouldTune = 200;

    private int observations = 0;

    /**
     * returns true whenever the new observation is "valid" as in, we should learn from this.
     * By default accepts everyhing
     */
    private Predicate<ControllerInput> validateInput = controllerInput -> true;


    float fallbackPolicy = Float.NaN;

    /**
     * a generic way to stop the tuner from learning
     */
    private boolean paused = false;



    /**
     * optional, if there are additional intercepts to feed to the regression, fill in this function
     */
    protected Function<ControllerInput,double[]> additionalInterceptsExtractor;



    public PIDAutotuner(PIDController toDecorate) {

        this(toDecorate,null);
    }


    /**
     *
     * @param toDecorate the PID controller to deal with
     * @param department nullable: if given the autotuner will not record until the department has at least one trade
     */
    public PIDAutotuner(PIDController toDecorate, Department department) {
        this(toDecorate,integer -> new AutoRegressiveWithInputRegression(integer,integer),department);



    }


    /**
     *  @param toDecorate the PID controller to deal with
     * @param department nullable: if given the autotuner will not record until the department has at least one trade
     */
    public PIDAutotuner(PIDController toDecorate, Function<Integer, SISORegression> regressionBuilder,
                        Department department) {
        super(toDecorate);
        decoratedCasted =toDecorate;
        this.linkedDepartment = department;
        regression = new MultipleModelRegressionWithSwitching(regressionBuilder,0,2,4,6,10);

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

        final boolean validInput = !paused &&
                (linkedDepartment == null || linkedDepartment.hasTradedAtLeastOnce())
                && validateInput.test(input);

        learn(input,!validInput );

        if(observations > afterHowManyDaysShouldTune) {

            //if it's time to tune, run a gradient descent and try to minimize ITAE given your best guess of what the real model is.

            tune(input);
        }
        //log
        log();

        super.adjust(input, isActive, simState, user, phase);
    }

    public void log() {
        if(linkedDepartment != null)
            linkedDepartment.handleNewEvent(new LogEvent(this, LogLevel.TRACE,"tuned with following gains and speed {},{},{},{}",
                    decoratedCasted.getProportionalGain(),decoratedCasted.getIntegralGain(),decoratedCasted.getDerivativeGain(),
                    decoratedCasted.getSpeed()));
    }

    protected void tune(ControllerInput input) {
        PIGradientDescent descent = new PIGradientDescent(regression,decoratedCasted,isControllingFlows() ? input.getFlowTarget() : input.getStockTarget(),
                additionalInterceptsExtractor == null ? null : additionalInterceptsExtractor.apply(input));

        final PIGradientDescent.PIDGains newGains = descent.getNewGains();
        System.out.println(newGains);
        decoratedCasted.setGains(newGains.getProportional(),newGains.getIntegral(),newGains.getDerivative());
    }

    private boolean notStuckAtEquilibrium() {
        return Math.abs(decoratedCasted.getNewError())>.001 || Math.abs(decoratedCasted.getOldError())>.001;
    }


    /**
     * Get the current u_t
     */
    @Override
    public float getCurrentMV() {
        //if we think there is no dynamics, hijack it
        if(Float.isFinite(fallbackPolicy))
            return Math.max(fallbackPolicy,0);
        else
            return super.getCurrentMV();
    }

    private void learn(ControllerInput input, boolean ignoreThisObservation) {

        final double[] additionalVariables = additionalInterceptsExtractor == null ? null : additionalInterceptsExtractor.apply(input);
        double output =   decoratedCasted.isControllingFlows()? input.getFlowInput() : input.getStockInput();
        double policy =   decoratedCasted.getCurrentMV();

        if(ignoreThisObservation) {
            regression.skipObservation(output, policy, additionalVariables);
        }
        else
        {
            regression.addObservation(output, policy, additionalVariables);
            observations++;

        }

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

    public double getGain() {
        return regression.getGain();
    }

    public double getTimeConstant() {
        return regression.getTimeConstant();
    }


    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public Function<ControllerInput, double[]> getAdditionalInterceptsExtractor() {
        return additionalInterceptsExtractor;
    }

    public void setAdditionalInterceptsExtractor(Function<ControllerInput, double[]> additionalInterceptsExtractor) {
        this.additionalInterceptsExtractor = additionalInterceptsExtractor;
    }

    public void setValidateInput(Predicate<ControllerInput> validateInput) {
        this.validateInput = validateInput;
    }

    public void setExcludeLinearFallback(boolean excludeLinearFallback) {
        regression.setExcludeLinearFallback(excludeLinearFallback);
    }

    public String describeRegression() {
        return regression.toString();
    }

    @VisibleForTesting
    public MultipleModelRegressionWithSwitching getRegression() {
        return regression;
    }
}
