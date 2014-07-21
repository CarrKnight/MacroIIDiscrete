/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

/**
 * <h4>Description</h4>
 * <p/> multiplies the P-covariance of the regression by a constant lambda = or smaller than 1
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-12-06
 * @see
 */
public class ExponentialForgettingRegressionDecorator extends RecursiveLinearRegressionDecorator
{

    private double lambda = .99d;


    private double maxTrace = Double.MAX_VALUE;

    public ExponentialForgettingRegressionDecorator(KalmanBasedRecursiveRegression decorated) {
        super(decorated);
    }

    public ExponentialForgettingRegressionDecorator(KalmanBasedRecursiveRegression decorated, double lambda) {
        super(decorated);
        this.lambda = lambda;
    }

    public ExponentialForgettingRegressionDecorator(KalmanBasedRecursiveRegression decorated, double lambda, double maxTrace) {
        super(decorated);
        this.lambda = lambda;
        this.maxTrace = maxTrace;
    }

    @Override
    public void addObservation(double observationWeight, double y, double... observation) {
        super.addObservation(observationWeight, y, observation);

        int dimensions = observation.length;
        double[][] pCovariance = getpCovariance();

        double trace = 0;
        for(int i=0; i< dimensions; i++)
            trace += pCovariance[i][i];
        if(trace > maxTrace)
            return;
        //reweight by forgetting factor
        for(int i=0;i<dimensions; i++)
            for(int j=0; j<dimensions; j++)
                pCovariance[i][j] *= 1d/lambda;

        setPCovariance(pCovariance);

    }


    public double getLambda() {
        return lambda;
    }

    public void setLambda(double lambda) {
        this.lambda = lambda;
    }
}
