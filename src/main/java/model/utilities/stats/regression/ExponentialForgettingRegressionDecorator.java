/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
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

    public ExponentialForgettingRegressionDecorator(RecursiveLinearRegression decorated) {
        super(decorated);
    }

    public ExponentialForgettingRegressionDecorator(RecursiveLinearRegression decorated, double lambda) {
        super(decorated);
        this.lambda = lambda;
    }

    @Override
    public void addObservation(double observationWeight, double y, double... observation) {
        super.addObservation(observationWeight, y, observation);

        int dimensions = observation.length;
        double[][] pCovariance = getpCovariance();

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
