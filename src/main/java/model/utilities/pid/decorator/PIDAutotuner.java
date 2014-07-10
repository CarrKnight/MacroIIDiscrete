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
import model.utilities.stats.processes.PIGradientDescent;
import model.utilities.stats.regression.SISOGuessingRegression;
import model.utilities.stats.regression.SISORegression;
import sim.engine.Steppable;

import java.util.Arrays;
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


    float fallbackPolicy = Float.NaN;

    /**
     * a generic way to stop the tuner from learning
     */
    private boolean paused = false;


    /**
     * optional, if there are additional intercepts to feed to the regression, fill in this function
     */
    private Function<ControllerInput,double[]> additionalInterceptsExtractor;


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
     *  @param toDecorate the PID controller to deal with
     * @param department nullable: if given the autotuner will not record until the department has at least one trade
     */
    public PIDAutotuner(PIDController toDecorate, Function<Integer, SISORegression> regressionBuilder,
                        Department department) {
        super(toDecorate);
        decoratedCasted =toDecorate;
        this.linkedDepartment = department;
        regression = new SISOGuessingRegression(regressionBuilder,0);


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

        if(!paused && (linkedDepartment!=null && linkedDepartment.hasTradedAtLeastOnce()) )
        {
            learn(input);
        }
        if(observations > afterHowManyDaysShouldTune && notStuckAtEquilibrium()) {

            //if it's time to tune, run a gradient descent and try to minimize ITAE given your best guess of what the real model is.

     /*       final float processGain = (float) regression.getGain();
            final float timeConstant = (float) regression.getTimeConstant();
            final float intercept = (float) regression.getIntercept();
            final int delay = regression.getDelay();

            System.out.println("regression results: kp: " +processGain + ", Tm: " + timeConstant + ", delay: " + delay +  ",intercept: " + intercept);
            */

            PIGradientDescent descent = new PIGradientDescent(regression,decoratedCasted,isControllingFlows() ? input.getFlowTarget() : input.getStockTarget(),
                    additionalInterceptsExtractor == null ? null : additionalInterceptsExtractor.apply(input));

            final PIGradientDescent.PIDGains newGains = descent.getNewGains();
            System.out.println(newGains);
            decoratedCasted.setGains(newGains.getProportional(),newGains.getIntegral(),newGains.getDerivative());
        }

        super.adjust(input, isActive, simState, user, phase);
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

    private void learn(ControllerInput input) {

        final double[] additionalVariables = additionalInterceptsExtractor == null ? null : additionalInterceptsExtractor.apply(input);

        if (isControllingFlows()) {
            regression.addObservation(input.getFlowInput(), getCurrentMV(),additionalVariables);

        }
        else {
            System.out.println("learning: stock: " + input.getStockInput() + ", policy: " + getCurrentMV() + ", z: " + Arrays.toString(additionalVariables));
            regression.addObservation(input.getStockInput(), getCurrentMV(),additionalVariables);
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
}
