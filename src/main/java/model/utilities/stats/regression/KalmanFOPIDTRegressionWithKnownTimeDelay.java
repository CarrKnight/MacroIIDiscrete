/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import com.google.common.base.Preconditions;

/**
 * <h4>Description</h4>
 * <p> This is for first order integrating process with time delay.
 * <p> Because it is integrating it must have a unit root. Luckily taking and regressing on differences is guaranteed weakly stationary.
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
public class KalmanFOPIDTRegressionWithKnownTimeDelay implements SISORegression
{


    //todo see if you can just make this delegate to the FOPDT by feeding it differences.

    private final KalmanFOPDTRegressionWithKnownTimeDelay regression;

    /**
     * the previous ys observed.
     */
    private float previousOutput = 0;



    public KalmanFOPIDTRegressionWithKnownTimeDelay(int delay) {
        this(
                //  new GunnarsonRegularizerDecorator(
                //     new ExponentialForgettingRegressionDecorator(
                new KalmanRecursiveRegression(3)
                //       ,.995d)     )
                ,delay,0);
    }

    public KalmanFOPIDTRegressionWithKnownTimeDelay(RecursiveLinearRegression regression, int delay, float initialInput) {
        Preconditions.checkArgument(delay >= 0);
        Preconditions.checkArgument(regression.getBeta().length == 3); //should be of dimension 3!
        this.regression = new KalmanFOPDTRegressionWithKnownTimeDelay(regression,delay,initialInput);
    }


    @Override
    public void addObservation(float output, float input){
        Preconditions.checkArgument(Float.isFinite(output));
        Preconditions.checkArgument(Float.isFinite(input));

        float difference = output - previousOutput;
        //derivative
        regression.addObservation(difference,input);

        previousOutput = output;
    }

    @Override
    public float predictNextOutput(float input){

        return regression.predictNextOutput(input) + previousOutput;

    }


    @Override
    public float getTimeConstant() {

        return regression.getTimeConstant();

    }

    @Override
    public float getGain() {
        return regression.getGain();

    }


    @Override
    public int getDelay() {
        return regression.getDelay();
    }
}
