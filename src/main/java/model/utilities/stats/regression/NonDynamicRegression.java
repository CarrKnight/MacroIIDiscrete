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
 * <p> Something of a default. Here we just regress y~u; So if the model reacts immediately with no time constant, we pick that up here
 * <p> It's available with or without delay
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-07-04
 * @see
 */
public class NonDynamicRegression implements SISORegression {


    private final RecursiveLinearRegression regression;

    private final DelayBin<Float> delayedInput;

    public NonDynamicRegression() {
       this(new KalmanRecursiveRegression(2));
    }

    public NonDynamicRegression(RecursiveLinearRegression regression) {
        this(regression,0);
    }


    public NonDynamicRegression(RecursiveLinearRegression regression, int delay) {
        Preconditions.checkArgument(regression.getBeta().length == 2);
        this.regression = regression;
        delayedInput = new DelayBin<>(delay,0f);
    }

    @Override
    public void addObservation(float output, float input) {
        Preconditions.checkArgument(Float.isFinite(output));
        Preconditions.checkArgument(Float.isFinite(input));

        input = delayedInput.addAndRetrieve(input);
        //derivative
        regression.addObservation(1, output, 1, input);


    }

    @Override
    public float predictNextOutput(float input) {


        return (float) (regression.getBeta()[0] + regression.getBeta()[1] * input);

    }

    @Override
    public float getTimeConstant() {
        return 0;
    }

    @Override
    public float getGain() {
        return (float) regression.getBeta()[1];
    }

    @Override
    public int getDelay() {
        return delayedInput.getDelay();
    }


    @Override
    public float getIntercept() {
        return (float) regression.getBeta()[0];
    }


    /**
     * if we are willing to believe the world is non-dynamic, what should be the policy for this target
     * @param target the target to achieve
     * @return the policy that supposedly is associated with it
     */
    public float impliedMV(float target){
          return (float) ((target-regression.getBeta()[0])/regression.getBeta()[1]);
    }
}
