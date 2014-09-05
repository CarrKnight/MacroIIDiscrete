/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.processes;

import com.google.common.base.Preconditions;

import java.util.function.Supplier;

public class AutoRegressiveWithInputProcess  implements DynamicProcess{


    private double[] inputLags;

    private double[] outputLags;

    /**
     * should be of one size more than the lags to account for the coefficient for today's input
     */
    final private double[] inputCoefficients;

    final private double[] outputCoefficients;

    final private double intercept;
    private Supplier<Double> randomNoise;


    public AutoRegressiveWithInputProcess(int outputLags, int inputLags,
                                          double[] inputCoefficients, double[] outputCoefficients, double intercept) {
        this.inputLags = new double[inputLags];
        this.outputLags = new double[outputLags];
        Preconditions.checkArgument(outputCoefficients.length == outputLags);
        this.inputCoefficients = inputCoefficients;
        Preconditions.checkArgument(inputCoefficients.length == inputLags + 1, "should be of one size more than the lags to account for the coefficient for today's input");
        this.outputCoefficients = outputCoefficients;
        this.intercept = intercept;
    }

    public AutoRegressiveWithInputProcess(double[] inputCoefficients, double[] outputCoefficients, double intercept,
                                          double[] inputLags, double[] outputLags) {
        this.inputCoefficients = inputCoefficients;
        this.outputCoefficients = outputCoefficients;
        Preconditions.checkArgument(inputCoefficients.length == inputLags.length + 1, "should be of one size more than the lags to account for the coefficient for today's input");
        Preconditions.checkArgument(outputCoefficients.length == outputLags.length);
        this.intercept = intercept;
        this.inputLags = inputLags;
        this.outputLags = outputLags;
    }


    @Override
    public double newStep(double todayInput, double... covariants) {

        double output = computeOutput(todayInput);


        pushLagsDown(todayInput, output);


        return output;




    }

    private void pushLagsDown(double todayInput, double output) {
        for(int i=inputLags.length-1; i>0;i--)
            inputLags[i]=inputLags[i-1];
        inputLags[0] = todayInput;

        for(int i=inputLags.length-1; i>0;i--)
            outputLags[i]=outputLags[i-1];
        outputLags[0] = output;
    }

    private double computeOutput(double todayInput) {
        double output = 0;
        for(int i=0; i<outputLags.length; i++)
        {
            output += outputCoefficients[i] * outputLags[i];
        }

        output += intercept;

        output += inputCoefficients[0] * todayInput;
        for(int i=0; i<inputLags.length; i++)
            output += inputCoefficients[i+1]*inputLags[i];

        if(randomNoise != null)
            output += randomNoise.get();
        return output;
    }


    public  double getLaggedOutput(int lag)
    {
        return outputLags[lag-1];
    }

    public  double getLaggedInput(int lag)
    {
        return inputLags[lag-1];
    }


    @Override
    public Supplier<Double> getRandomNoise() {
        return randomNoise;
    }

    @Override
    public void setRandomNoise(Supplier<Double> randomNoise) {
        this.randomNoise = randomNoise;
    }



}