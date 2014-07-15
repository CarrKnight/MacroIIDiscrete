/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import com.google.common.base.Preconditions;
import model.utilities.DelayBin;
import model.utilities.stats.processes.DynamicProcess;
import model.utilities.stats.processes.LinearNonDynamicProcess;

/**
 * <h4>Description</h4>
 * <p> Something of a default. Here we just regress y~u; So if the model reacts immediately with no time constant, we pick that up here
 * <p> It's available with or without delay
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-07-04
 * @see
 */
public class NonDynamicRegression implements SISORegression {


    private final RecursiveLinearRegression regression;

    private final DelayBin<Double> delayedInput;

    public NonDynamicRegression() {
       this(new KalmanRecursiveRegression(2));
    }

    public NonDynamicRegression(RecursiveLinearRegression regression) {
        this(regression,0);
    }


    public NonDynamicRegression(RecursiveLinearRegression regression, int delay) {
        Preconditions.checkArgument(regression.getBeta().length == 2);
        this.regression = regression;
        delayedInput = new DelayBin<>(delay,0d);
    }

    @Override
    public void addObservation(double output, double input, double... intercepts) {
        Preconditions.checkArgument(Double.isFinite(output));
        Preconditions.checkArgument(Double.isFinite(input));

        double delayed = delayedInput.addAndRetrieve(input);
        //derivative
        regression.addObservation(1, output, 1, delayed);


    }

    /**
     * ignored
     */
    @Override
    public void skipObservation(double skippedOutput, double skippedInput, double... skippedIntercepts) {
    }

    @Override
    public double predictNextOutput(double input, double... intercepts) {


        return regression.getBeta()[0] + regression.getBeta()[1] * input;

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
    public int getDelay() {
        return delayedInput.getDelay();
    }


    @Override
    public double getIntercept() {
        return regression.getBeta()[0];
    }


    /**
     * if we are willing to believe the world is non-dynamic, what should be the policy for this target
     * @param target the target to achieve
     * @return the policy that supposedly is associated with it
     */
    public double impliedMV(double target){
          return (target-regression.getBeta()[0])/regression.getBeta()[1];
    }


    @Override
    public DynamicProcess generateDynamicProcessImpliedByRegression() {
        return  new LinearNonDynamicProcess(getDelay(),regression.getBeta()[0],regression.getBeta()[1],delayedInput.peekAll(Double.class));
    }

    public String toString() {
        double[] beta = regression.getBeta();
        return "nonlinear: y = " + beta[0] + " + " + beta[1] + " * x";
    }
}
