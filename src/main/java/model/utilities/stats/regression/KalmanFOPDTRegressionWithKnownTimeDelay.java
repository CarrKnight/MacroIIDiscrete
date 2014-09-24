/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import com.google.common.base.Preconditions;
import model.utilities.DelayBin;
import model.utilities.stats.processes.DynamicProcess;
import model.utilities.stats.processes.FirstOrderPlusDeadTime;

/**
 * <h4>Description</h4>
 * <p> Using FOPDT(First order plus dead time) differential equation formulation.
 * With known delay this is just a linear regression. We do that iteratively through
 * <p>
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-06-29
 * @see
 */
public class KalmanFOPDTRegressionWithKnownTimeDelay implements SISORegression {



    private final RecursiveLinearRegression regression;

    /**
     * the previous ys observed.
     */
    private double previousOutput = Double.NaN;

    /**
     * a delay bin to "delay" the input variable so that it regresses correctly
     */
    final private DelayBin<Double> delayedInput;

    private int numberOfObservations = 0;



    public KalmanFOPDTRegressionWithKnownTimeDelay(int delay) {
        this(
                   //     new ExponentialForgettingRegressionDecorator(
                new ExponentialForgettingRegressionDecorator(
                        new KalmanRecursiveRegression(3),.98d,10) ,delay,0);

    }



    public KalmanFOPDTRegressionWithKnownTimeDelay(RecursiveLinearRegression regression, int delay, double initialInput) {
        Preconditions.checkArgument(delay>=0);
        Preconditions.checkArgument(regression.getBeta().length == 3); //should be of dimension 3!
        this.regression = regression;
        delayedInput = new DelayBin<>(delay,initialInput);
    }




    @Override
    public void addObservation(double output, double input, double... intercepts){
        Preconditions.checkArgument(Double.isFinite(output));
        Preconditions.checkArgument(Double.isFinite(input));
        Preconditions.checkArgument(intercepts == null || intercepts.length == 0, "This regression doesn't take additional intercepts");

  //      System.out.println("u: " + input + " , y: " + output +", previous: " + previousOutput);

        input = delayedInput.addAndRetrieve(input);
        //derivative
        if(Double.isFinite(previousOutput))
            regression.addObservation(1, output, 1, input, previousOutput);

        previousOutput = output;

        numberOfObservations++;

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
        delayedInput.addAndRetrieve(skippedInput);
        previousOutput = skippedOutput;
    }

    @Override
    public double predictNextOutput(double input, double... intercepts){

        final double[] betas = getBetas();
        input = getDelay() > 0 ? delayedInput.peek() : input;
        return (float) (betas[0] + betas[1] * input + betas[2] * previousOutput);
    }

    public double[] getBetas() {
        return regression.getBeta();
    }

    @Override
    public double getTimeConstant(){
        final double[] betas = getBetas();

        return (float) (betas[2]/(1-betas[2]));
    }

    @Override
    public double getGain()
    {
        final double[] betas = getBetas();

        return (float) (betas[1]*(1+getTimeConstant()));
    }


    @Override
    public double getIntercept() {
        return (float) getBetas()[0];
    }

    public int getDelay() {
        return delayedInput.getDelay();
    }

    @Override
    public String toString() {
        return "reg{" + "gain=" + getGain() + " TimeConstant=" + getTimeConstant() + " Delay=" + getDelay() + '}';
    }

    /**
     * Retrieves all that is currently in queue.
     */
    public Double[] peekInputQueue() {
        return delayedInput.peekAll(Double.class);
    }

    @Override
    public DynamicProcess generateDynamicProcessImpliedByRegression() {
        final double[] betas = getBetas();

        return new FirstOrderPlusDeadTime(betas[0],getGain(),getTimeConstant(),getDelay(),previousOutput,delayedInput.peekAll(Double.class));
    }

    public int getNumberOfObservations() {
        return numberOfObservations;
    }
}
