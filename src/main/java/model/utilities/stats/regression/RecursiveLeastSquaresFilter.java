/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import Jama.Matrix;
import com.google.common.base.Preconditions;

/**
 * <h4>Description</h4>
 * <p> This is supposed to be the RLS filter without the Kalman apparatus. I take the equations from the book "Kernel Adaptive Filters", but really they are everywhere.
 * <p>
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-07-15
 * @see
 */
public class RecursiveLeastSquaresFilter implements RecursiveLinearRegression {


    /**
     * the R matrix of the least squares
     */
    private Matrix inputCorrelationMatrix;

    private double[] betas;

    /**
     * the RLS filter, trying to regress these many dimensions.
     * @param dimensions
     */
    public RecursiveLeastSquaresFilter(int dimensions) {
        betas = new double[dimensions];
        inputCorrelationMatrix = Matrix.identity(dimensions,dimensions);

    }

    @Override
    public void addObservation(double weight, double y, double... observation) {
        Preconditions.checkArgument(observation.length == betas.length);


        //weight, if needed
        if(weight != 1) {
            assert weight > 0;
            double w = Math.sqrt(weight);
            for(int i=0; i< observation.length; i++)
                observation[i] *= w;

            //reweight y
            y = y* w;
        }
        //create a vector from the observations
        Matrix inputVector = new Matrix(observation,observation.length);
        //update R
        final Matrix todayInputCorrelation = inputVector.times(inputVector.transpose());
        inputCorrelationMatrix = inputCorrelationMatrix.plus(todayInputCorrelation);

        //get error
        double prediction = 0;
        for(int i=0; i<betas.length; i++)
            prediction += betas[i] * observation[i];
        double error = y - prediction;

        //get new update
        double[] update = inputCorrelationMatrix.inverse().times(inputVector).times(error).getColumnPackedCopy();
        assert update.length == betas.length;

        //sum to beta
        for(int i=0; i<betas.length; i++)
            betas[i] = betas[i] + update[i];

    }

    @Override
    public double[] getBeta() {

        return betas;
    }

    @Override
    public double[] setBeta(int index, double newValue) {

        betas[index] = newValue;
        return betas;
    }
}
