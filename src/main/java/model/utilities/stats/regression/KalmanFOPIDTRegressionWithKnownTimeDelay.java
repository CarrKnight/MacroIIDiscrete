/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import model.utilities.stats.processes.DynamicProcess;
import model.utilities.stats.processes.FirstOrderIntegratingPlusDeadTime;

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



    private final KalmanFOPDTRegressionWithKnownTimeDelay regression;

    /**
     * the previous ys observed.
     */
    private double previousOutput = Double.NaN;

    /**
     * the previous ys observed.
     */
    private double twoStepsAgoOutput = Double.NaN;

    public KalmanFOPIDTRegressionWithKnownTimeDelay(int delay, float forgettingFactor) {
        this(
                new ExponentialForgettingRegressionDecorator(
                        new KalmanRecursiveRegression(3),forgettingFactor,10)
                //       ,.995d)     )
                ,delay,0);
    }

    public KalmanFOPIDTRegressionWithKnownTimeDelay(int delay) {
        this(delay,.995f);
    }

    public KalmanFOPIDTRegressionWithKnownTimeDelay(RecursiveLinearRegression regression, int delay, double initialInput) {
        Preconditions.checkArgument(delay >= 0);
        Preconditions.checkArgument(regression.getBeta().length == 3); //should be of dimension 3!
        this.regression = new KalmanFOPDTRegressionWithKnownTimeDelay(regression,delay,initialInput);
    }


    @Override
    public void addObservation(double output, double input, double... intercepts){
        Preconditions.checkArgument(Double.isFinite(output));
        Preconditions.checkArgument(Double.isFinite(input));

        if(Double.isFinite(previousOutput)) {
            double difference = output - previousOutput;
            //derivative
            regression.addObservation(difference, input,intercepts);
        }
        twoStepsAgoOutput = previousOutput;
        previousOutput = output;
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
        regression.skipObservation(skippedOutput - previousOutput,skippedInput,skippedIntercepts);


        twoStepsAgoOutput = previousOutput;
        previousOutput = skippedOutput;
    }

    @Override
    public double predictNextOutput(double input, double... intercepts){

        return  generateDynamicProcessImpliedByRegression().newStep(input,intercepts);

    }


    @Override
    public double getTimeConstant() {

        return regression.getTimeConstant();

    }

    @Override
    public double getGain() {
        return regression.getGain();

    }


    @Override
    public double getIntercept() {
        return regression.getIntercept();
    }

    @Override
    public int getDelay() {
        return regression.getDelay();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("regression", regression)
                .toString();
    }


    @Override
    public DynamicProcess generateDynamicProcessImpliedByRegression() {
         return new FirstOrderIntegratingPlusDeadTime(getIntercept(),getGain(),getTimeConstant(),getDelay(),
                 previousOutput,twoStepsAgoOutput,regression.peekInputQueue()
                 );
    }

    @Override
    public int getNumberOfObservations() {
        return regression.getNumberOfObservations();
    }
}
