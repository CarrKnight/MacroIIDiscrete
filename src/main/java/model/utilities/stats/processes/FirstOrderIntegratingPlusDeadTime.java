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
 * <p> Similar to FOPDT but one differential more, representing an inventory/tank usually.
 * <p>
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-07-02
 * @see
 */
public class FirstOrderIntegratingPlusDeadTime implements DynamicProcess {


    final private double intercept;

    private double[] previousStep = new double[2];

    final private double gain;

    final private double timeConstant;


    final private DelayBin<Double> inputBin;


    public FirstOrderIntegratingPlusDeadTime(double intercept, double gain, double timeConstant, int deadTime) {
        this(intercept, gain, timeConstant, deadTime,0,0);

    }

    public FirstOrderIntegratingPlusDeadTime(double intercept, double gain, double timeConstant, int deadTime, double yesterdayOutput, double twoDaysAgoOutput, Double... previousInputs) {
        this.intercept = intercept;
        this.gain = gain;
        this.timeConstant = timeConstant;
        Preconditions.checkArgument(deadTime >= 0);

        Preconditions.checkArgument(previousInputs.length <= deadTime);
        inputBin = new DelayBin<>(deadTime,intercept);
        for(double oldInput: previousInputs)
            inputBin.addAndRetrieve(oldInput);

        previousStep[0] = yesterdayOutput;
        previousStep[1] = twoDaysAgoOutput;

    }

    @Override
    public double newStep(double todayInput, double... covariants) {

        double oldDifferential = previousStep[0]-previousStep[1];
        double commonDenominator = 1+timeConstant;
        todayInput = inputBin.addAndRetrieve(todayInput);

        double output =intercept/commonDenominator +
                oldDifferential * timeConstant/commonDenominator + previousStep[0] + todayInput * gain/commonDenominator;

        if(randomNoise != null)
            output = output + randomNoise.get()/commonDenominator;

        //update
        previousStep[1] = previousStep[0];
        previousStep[0] = output;

        return output;
    }

    private Supplier<Double> randomNoise = null;

    public Supplier<Double> getRandomNoise() {
        return randomNoise;
    }

    public void setRandomNoise(Supplier<Double> randomNoise) {
        this.randomNoise = randomNoise;
    }
}
