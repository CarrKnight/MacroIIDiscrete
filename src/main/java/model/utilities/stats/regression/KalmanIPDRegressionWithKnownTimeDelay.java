/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import com.google.common.base.Preconditions;
import model.utilities.DelayBin;
import model.utilities.stats.processes.DynamicProcess;
import model.utilities.stats.processes.IntegratorPlusDeadTime;

/**
 * <h4>Description</h4>
 * <p> A simple Integrator Plus Delay. Expects an additional independent variable (usually representing production/consumption from outside the department)
 * <p> This is a simple regression: deltay on u
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-07-06
 * @see
 */
public class KalmanIPDRegressionWithKnownTimeDelay implements SISORegression {


    private final RecursiveLinearRegression regression;

    /**
     * the previous ys observed.
     */
    private double previousOutput = Double.NaN;

    /**
     * a delay bin to "delay" the input variable so that it regresses correctly
     */
    final private DelayBin<Double> delayedInput;

    public KalmanIPDRegressionWithKnownTimeDelay(int delay) {
        this(
                //     new ExponentialForgettingRegressionDecorator(
                new AutovarianceReweighterDecorator(new KalmanRecursiveRegression(2),5,1)
                ,delay,0);
    }



    public KalmanIPDRegressionWithKnownTimeDelay(RecursiveLinearRegression regression, int delay, double initialInput) {
        Preconditions.checkArgument(delay >= 0);
        Preconditions.checkArgument(regression.getBeta().length == 2); //should be of dimension 3!
        this.regression = regression;
        delayedInput = new DelayBin<>(delay,initialInput);
    }



    /**
     * add a new observation
     *  @param output     the y of the sistem
     * @param input      the input of the system
     * @param intercepts any other variable that affects y but it is not controlled like u is.
     */
    @Override
    public void addObservation(double output, double input, double... intercepts) {
        Preconditions.checkArgument(Double.isFinite(output));
        Preconditions.checkArgument(Double.isFinite(input));
        Preconditions.checkArgument(intercepts.length == 1); //there should be an additional item!

        input = delayedInput.addAndRetrieve(input);
        //derivative
        if(Double.isFinite(previousOutput))
            regression.addObservation(1, output-previousOutput-intercepts[0],1, input);
        //basically we regress deltay on u: changes on inventory ~ inflow + outflow
        //one of the flow is from "outside" the department. The other is controlled by its policy.
        previousOutput = output;


    }

    @Override
    public double predictNextOutput(double input, double... intercepts) {
        Preconditions.checkArgument(intercepts.length == 1); //there should be an additional item!
        final double[] beta = regression.getBeta();
        double delayed = delayedInput.getDelay() > 0 ? delayedInput.peek() : input; //feed it the delayed input.
        return beta[0] + beta[1]*delayed + intercepts[0] + previousOutput;
    }

    @Override
    public double getTimeConstant() {

        return 0;
    }

    @Override
    public double getGain() {
        return regression.getBeta()[1];
    }

    @Override
    public double getIntercept() {

        return regression.getBeta()[0];
    }

    @Override
    public int getDelay() {

        return delayedInput.getDelay();
    }

    @Override
    public DynamicProcess generateDynamicProcessImpliedByRegression() {
        final double[] beta = regression.getBeta();

        return new IntegratorPlusDeadTime(beta[0],beta[1],1,previousOutput,getDelay(),delayedInput.peekAll(Double.class));
    }
}
