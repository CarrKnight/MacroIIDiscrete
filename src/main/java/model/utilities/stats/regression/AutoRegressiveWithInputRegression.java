/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import com.google.common.base.Objects;
import com.google.common.primitives.Doubles;
import model.utilities.stats.processes.AutoRegressiveWithInputProcess;

import java.util.Arrays;

/**
 * Generate a simple y_t = a_1 y_{t-1} + a_2 y_{t-2} + ... + b_0 x_t + b_1 x_{t-1} + ...
 * Created by carrknight on 9/4/14.
 */
public class AutoRegressiveWithInputRegression implements SISORegression {



    private final double[] inputLags;

    private final double[] outputLags;

    private final RecursiveLinearRegression regression;

    private int numberOfObservations = 0;

    public AutoRegressiveWithInputRegression(int maxInputLag, int maxOutputLag) {

        inputLags = new double[maxInputLag];
        outputLags = new double[maxOutputLag];
        regression = new ExponentialForgettingRegressionDecorator(
                new KalmanRecursiveRegression(1+1+maxInputLag+maxOutputLag),.99d,10d); //the lags + the intercept + the input unlagged

    }

    /**
     * add a new observation
     *
     * @param output     the y of the sistem
     * @param input      the input of the system
     * @param intercepts any other variable that affects y but it is not controlled like u is.
     */
    @Override
    public void addObservation(double output, double input, double... intercepts) {
        numberOfObservations++;
        double regressionInput[] = Doubles.concat(new double[]{1,input},inputLags,outputLags);

        regression.addObservation(1,output,regressionInput);

        pushLagsDown(input,output);


    }


    private void pushLagsDown(double todayInput, double output) {
        for(int i=inputLags.length-1; i>0;i--)
            inputLags[i]=inputLags[i-1];
        inputLags[0] = todayInput;

        for(int i=inputLags.length-1; i>0;i--)
            outputLags[i]=outputLags[i-1];
        outputLags[0] = output;
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
        pushLagsDown(skippedInput,skippedOutput);

    }

    @Override
    public double predictNextOutput(double input, double... intercepts) {
        return generateDynamicProcessImpliedByRegression().newStep(input);
    }

    @Override
    public double getTimeConstant() {
        return 0;
    }

    @Override
    public double getGain() {
        return 0;
    }

    @Override
    public int getNumberOfObservations() {
        return numberOfObservations;
    }

    @Override
    public double getIntercept() {
        return 0;
    }

    @Override
    public int getDelay() {
        return 0;
    }

    @Override
    public AutoRegressiveWithInputProcess generateDynamicProcessImpliedByRegression() {
        double[] xCoefficients = new double[inputLags.length+1];
        double[] yCoefficients = new double[outputLags.length];
        final double[] beta = getBeta();

        for(int i=0; i<xCoefficients.length; i++) {
            xCoefficients[i]= beta[i+1];
        }

        for(int i=0; i<yCoefficients.length; i++) {
            yCoefficients[i]= beta[i+1 + xCoefficients.length];
        }
        return new AutoRegressiveWithInputProcess(xCoefficients,yCoefficients,beta[0],
                Arrays.copyOf(inputLags,inputLags.length),
                Arrays.copyOf(outputLags,inputLags.length));
    }

    public double[] getBeta() {
        return regression.getBeta();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("regression", Arrays.toString(getBeta()))
                .toString();
    }
}
