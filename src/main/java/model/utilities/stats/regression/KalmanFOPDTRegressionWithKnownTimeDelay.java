/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import com.google.common.base.Preconditions;
import model.utilities.DelayBin;

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
    private float previousOutput = 0;

    /**
     * a delay bin to "delay" the input variable so that it regresses correctly
     */
    final private DelayBin<Float> delayedInput;


    public KalmanFOPDTRegressionWithKnownTimeDelay(int delay) {
        this(
              //  new GunnarsonRegularizerDecorator(
                   //     new ExponentialForgettingRegressionDecorator(
                                new KalmanRecursiveRegression(3)
         //       ,.995d)     )
                ,delay,0);
    }

    public KalmanFOPDTRegressionWithKnownTimeDelay(RecursiveLinearRegression regression, int delay, float initialInput) {
        Preconditions.checkArgument(delay>=0);
        Preconditions.checkArgument(regression.getBeta().length == 3); //should be of dimension 3!
        this.regression = regression;
        delayedInput = new DelayBin<>(delay,initialInput);
    }

    @Override
    public void addObservation(float output, float input){
        Preconditions.checkArgument(Float.isFinite(output));
        Preconditions.checkArgument(Float.isFinite(input));

        input = delayedInput.addAndRetrieve(input);
        //derivative
        regression.addObservation(1, output, 1, input, previousOutput);

        previousOutput = output;

    }


    @Override
    public float predictNextOutput(float input){

        final double[] betas = getBeta();
        input = getDelay() > 0 ? delayedInput.peek() : input;
        return (float) (betas[0] + betas[1] * input + betas[2] * previousOutput);
    }

    public double[] getBeta() {
        return regression.getBeta();
    }

    @Override
    public float getTimeConstant(){
        final double[] betas = getBeta();

        return (float) (betas[2]/(1-betas[2]));
    }

    @Override
    public float getGain()
    {
        final double[] betas = getBeta();

        return (float) (betas[1]*(1+getTimeConstant()));
    }


    public int getDelay() {
        return delayedInput.getDelay();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("KalmanFOPDTRegressionWithKnownTimeDelay{");
        sb.append("gain=").append(getGain());
        sb.append("TimeConstant=").append(getTimeConstant());
        sb.append('}');
        return sb.toString();
    }
}
