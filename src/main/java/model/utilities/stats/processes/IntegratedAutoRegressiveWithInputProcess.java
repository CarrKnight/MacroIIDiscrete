/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.processes;

import java.util.function.Supplier;

/**
 * This is just like the normal autoregressive process except it is about deltas: <br>
 *Δy_t = a_1 Δy_{t-1} + a_2 Δy_{t-2} + ... + b_0 Δx_t + b_1 Δx_{t-1} + ... <br>
 *     So to generate a new Y we really need to do:
 *     y_{t+1} = y_t + Δy_{t+1}
 * Created by carrknight on 9/5/14.
 */
public class IntegratedAutoRegressiveWithInputProcess implements DynamicProcess {

    private double lastNonIntegratedOutput;

    private double lastNonIntegratedInput;

    private AutoRegressiveWithInputProcess integrated;


    public IntegratedAutoRegressiveWithInputProcess(double[] deltaInputCoefficients, double[] deltaOutputCoefficients, double intercept,
                                                    double[] deltaInputLags, double[] deltaOutputLags,
                                                    double lastNonIntegratedOutput,double lastNonIntegratedInput) {
        integrated =  new AutoRegressiveWithInputProcess(deltaInputCoefficients, deltaOutputCoefficients, intercept,
                deltaInputLags, deltaOutputLags);
        this.lastNonIntegratedOutput = lastNonIntegratedOutput;
        this.lastNonIntegratedInput = lastNonIntegratedInput;

    }

    public IntegratedAutoRegressiveWithInputProcess(AutoRegressiveWithInputProcess deltaProcess,
                                                    double lastNonIntegratedOutput,double lastNonIntegratedInput) {
        integrated =  deltaProcess;
        this.lastNonIntegratedOutput = lastNonIntegratedOutput;
        this.lastNonIntegratedInput = lastNonIntegratedInput;

    }


    @Override
    public double newStep(double todayInput, double... covariants) {

        double output = lastNonIntegratedOutput + integrated.newStep(todayInput-lastNonIntegratedInput);

        lastNonIntegratedOutput = output;
        lastNonIntegratedInput = todayInput;

        return output;

    }


    @Override
    public Supplier<Double> getRandomNoise() {
        return integrated.getRandomNoise();
    }

    @Override
    public void setRandomNoise(Supplier<Double> randomNoise) {
        integrated.setRandomNoise(randomNoise);
    }

    public double getLaggedInput(int lag) {
        return integrated.getLaggedInput(lag);
    }

    public double getLaggedOutput(int lag) {
        return integrated.getLaggedOutput(lag);
    }
}
