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

    final private double previousSteps[] = new double[2];

    final private double gain;

    final private double timeConstant;

    final private int deadTime;

    final private DelayBin<Double> inputBin;


    private double previousY;

    public FirstOrderPlusDeadTime(double intercept, double gain, double timeConstant, int deadTime) {
        this.intercept = intercept;
        this.gain = gain;
        this.timeConstant = timeConstant;
        Preconditions.checkArgument(deadTime>=0);
        this.deadTime = deadTime;
        inputBin = new DelayBin<>(deadTime,intercept);
        previousSteps[0] = intercept;
        previousSteps[1] = intercept;

    }

    @Override
    public double newStep(double todayInput)
    {
        //put input in the bin
        double input = inputBin.addAndRetrieve(todayInput);

        double output = intercept + gain * input - timeConstant * (getCurrentDerivative());
        //shock it, if needed
        if(randomNoise != null)
            output = output + randomNoise.get();
        //remember your output for later
        previousSteps[1] = previousSteps[0];
        previousSteps[0] = output;
        return output;




    }

    public double getCurrentDerivative() {
        return previousSteps[0] - previousSteps[1];
    }

    private Supplier<Double> randomNoise = null;

    public Supplier<Double> getRandomNoise() {
        return randomNoise;
    }

    public void setRandomNoise(Supplier<Double> randomNoise) {
        this.randomNoise = randomNoise;
    }
}
