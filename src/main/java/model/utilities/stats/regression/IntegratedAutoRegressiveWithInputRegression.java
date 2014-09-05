/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import model.utilities.stats.processes.DynamicProcess;
import model.utilities.stats.processes.IntegratedAutoRegressiveWithInputProcess;

/**
 * Δy_t = a_1 Δy_{t-1} + a_2 Δy_{t-2} + ... + b_0 Δx_t + b_1 Δx_{t-1} + ...
 * Created by carrknight on 9/5/14.
 */
public class IntegratedAutoRegressiveWithInputRegression implements SISORegression {



    private double lastNonIntegratedOutput =0;

    private double lastNonIntegratedInput =0;

    private final AutoRegressiveWithInputRegression deltaRegression;




    public IntegratedAutoRegressiveWithInputRegression(int deltaXLags,int deltaYLags) {
        deltaRegression = new AutoRegressiveWithInputRegression(deltaXLags,deltaYLags);
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
        deltaRegression.addObservation(output-lastNonIntegratedOutput,input-lastNonIntegratedInput);

        lastNonIntegratedOutput = output;
        lastNonIntegratedInput = input;
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
        deltaRegression.skipObservation(skippedOutput - lastNonIntegratedOutput,
                skippedInput - lastNonIntegratedInput);

        lastNonIntegratedOutput = skippedOutput;
        lastNonIntegratedInput = skippedInput;
    }


    @Override
    public DynamicProcess generateDynamicProcessImpliedByRegression() {
        return new IntegratedAutoRegressiveWithInputProcess(deltaRegression.generateDynamicProcessImpliedByRegression(),
                lastNonIntegratedOutput,lastNonIntegratedInput);
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
        return deltaRegression.getNumberOfObservations();
    }

    @Override
    public double getIntercept() {
        return 0;
    }

    @Override
    public int getDelay() {
        return 0;
    }

    public double[] getBeta() {
        return deltaRegression.getBeta();
    }
}
