/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import Jama.Matrix;

/**
 * <h4>Description</h4>
 * <p/> A simple decorator that normalizes the P covariance of the function, from:
 * Combining Tracking and Regularization in Recursive Least Squares Identification
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
public class GunnarsonRegularizerDecorator extends RecursiveLinearRegressionDecorator {
    private double mu =0.00001d;

    public GunnarsonRegularizerDecorator(RecursiveLinearRegression decorated) {
        super(decorated);
    }

    public GunnarsonRegularizerDecorator(RecursiveLinearRegression decorated, double mu) {
        super(decorated);
        this.mu = mu;
    }

    @Override
    public void addObservation(double observationWeight, double y, double... observation) {
        super.addObservation(observationWeight, y, observation);

        //regularization is just P * (I + mu P )
        Matrix matrix = new Matrix(getDecorated().getpCovariance());
        matrix = matrix.times(Matrix.identity(observation.length,observation.length).plus(matrix.times(mu)).inverse());
        getDecorated().setPCovariance(matrix.getArray());
    }

    public double getMu() {
        return mu;
    }

    public void setMu(double mu) {
        this.mu = mu;
    }
}
