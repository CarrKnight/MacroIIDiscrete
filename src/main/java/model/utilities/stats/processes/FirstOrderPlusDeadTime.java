/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.processes;

import com.google.common.base.Preconditions;
import model.utilities.DelayBin;

import java.util.function.Supplier;

/**
 * <h4>Description</h4>
 * <p> A simple FOPDT model, useful to generate Ys
 * <p>
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-06-25
 * @see
 */
public class FirstOrderPlusDeadTime implements DynamicProcess {


    final private double intercept;

    private double previousStep;

    final private double gain;

    final private double timeConstant;

    final private int deadTime;

    final private DelayBin<Double> inputBin;


    private double previousY;

    public FirstOrderPlusDeadTime(double intercept, double gain, double timeConstant, int deadTime) {
        this(intercept, gain, timeConstant, deadTime,0);
    }


    public FirstOrderPlusDeadTime(double intercept, double gain, double timeConstant, int deadTime, double initialOutput, Double... previousInputs) {
        this.intercept = intercept;
        this.gain = gain;
        this.timeConstant = timeConstant;
        Preconditions.checkArgument(deadTime>=0);
        this.deadTime = deadTime;
        inputBin = new DelayBin<>(deadTime,intercept);
        previousStep = initialOutput;

        Preconditions.checkArgument(previousInputs.length <= deadTime);
        for(double oldInput : previousInputs)
            inputBin.addAndRetrieve(oldInput);

    }

    @Override
    public double newStep(double todayInput, double... covariants)
    {
        //put input in the bin
        double input = inputBin.addAndRetrieve(todayInput);

        double commonDenominator = 1+timeConstant;

        double output = intercept + (gain * input)/commonDenominator + (timeConstant * (previousStep))/commonDenominator;
        //shock it, if needed
        if(randomNoise != null)
            output = output + randomNoise.get()/commonDenominator;
        //remember your output for later
        previousStep = output;
        return output;




    }


    private Supplier<Double> randomNoise = null;

    public Supplier<Double> getRandomNoise() {
        return randomNoise;
    }

    public void setRandomNoise(Supplier<Double> randomNoise) {
        this.randomNoise = randomNoise;
    }

    public int getDeadTime() {
        return deadTime;
    }
}
