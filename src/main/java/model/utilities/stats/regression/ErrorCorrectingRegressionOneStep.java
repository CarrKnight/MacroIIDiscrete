/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import com.google.common.base.Preconditions;
import model.utilities.stats.processes.DynamicProcess;

/**
 * A simple 1-step error correcting model. See here: http://springschool.politics.ox.ac.uk/spring_school/OxfordECM.pdf
 * Created by carrknight on 8/29/14.
 */
public class ErrorCorrectingRegressionOneStep implements SISORegression {

    /**
     * y_{t-1}
     */
    private double previousOutput = Double.NaN;

    /**
     * x_{t-1}
     */
    private double previousInput = Double.NaN;


    private int numberOfObservations = 0;


    private final KalmanBasedRecursiveRegression regression;

    public ErrorCorrectingRegressionOneStep() {
        regression = new ExponentialForgettingRegressionDecorator(
                new KalmanRecursiveRegression(4),.99,1);
    }

    public ErrorCorrectingRegressionOneStep(float forgettingFactor) {
        if(forgettingFactor >= 1)
            regression = new KalmanRecursiveRegression(4);
        else
            regression = new ExponentialForgettingRegressionDecorator(
                    new KalmanRecursiveRegression(4),forgettingFactor,1);
    }

    /**
     * add a new observation
     *
     * @param output     the y of the sistem
     * @param input      the input of the system
     * @param intercepts any other variable that affects y but it is not controlled like u is.
     */
    @Override
    public void addObservation(double output, double input, double... ignored) {
        Preconditions.checkArgument(Double.isFinite(output) && Double.isFinite(input), "arguments must be finite");

        double deltaInput = input - previousInput;
        double deltaOutput = output - previousOutput;

        if(Double.isFinite(deltaInput) &&  Double.isFinite(deltaOutput))
        {
            assert Double.isFinite(previousInput);
            assert Double.isFinite(previousOutput);
            numberOfObservations++;
            regression.addObservation(1,deltaOutput,1,deltaInput,previousOutput,previousInput);
        }


        previousInput = input;
        previousOutput = output;

    }

    /**
     * get notified that an observation is skipped. This is usually to avoid having fake/wrong y_t - y_{t-1} from not considering the skipped observation
     *
     * @param skippedOutput
     * @param skippedInput
     * @param skippedIntercepts
     */
    @Override
    public void skipObservation(double skippedOutput, double skippedInput, double... skippedIntercepts) {

        previousInput = skippedInput;
        previousOutput = skippedOutput;
    }

    @Override
    public double predictNextOutput(double input, double... intercepts) {
        final double[] coefficients = regression.getBeta();
        double deltaInput = input - previousInput;

        return previousOutput + coefficients[0] + coefficients[1]*deltaInput +
                coefficients[2]* previousOutput + coefficients[3]*previousInput;
    }

    /**
     * returns the approximate linear attractor between the two equations
     * @return
     */
    @Override
    public double getTimeConstant() {
        return  -regression.getBeta()[2];
    }

    @Override
    public double getGain() {
        return -regression.getBeta()[3]/regression.getBeta()[2];
    }

    @Override
    public int getNumberOfObservations() {
        return numberOfObservations;
    }

    @Override
    public double getIntercept() {
        return -regression.getBeta()[0]/regression.getBeta()[2];
    }

    @Override
    public int getDelay() {
        return 0;
    }

    @Override
    public DynamicProcess generateDynamicProcessImpliedByRegression() {
        return null;
    }


}
