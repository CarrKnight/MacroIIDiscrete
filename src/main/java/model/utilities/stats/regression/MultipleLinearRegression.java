/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import Jama.Matrix;
import com.google.common.base.Preconditions;
import com.sun.istack.internal.Nullable;

/**
 * <h4>Description</h4>
 * <p/> A simpl JAMLA matrix-based solution to the linear regression problem. If it fails to invert the matrix, it just reverts to an intercept only solution
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-08-07
 * @see
 */
public class MultipleLinearRegression implements MultivariateRegression {

    final private int numberOfRegressorsBesidesIntercept;

    /**
     * the matrix where we store intercept, slope and so on.
     */
    private Matrix resultMatrix;


    public MultipleLinearRegression(int numberOfRegressorsBesidesIntercept) {
        this.numberOfRegressorsBesidesIntercept = numberOfRegressorsBesidesIntercept;
        Preconditions.checkArgument(numberOfRegressorsBesidesIntercept>=1);
    }

    /**
     * Do the regression/estimation. The arrays must be of the same size
     *
     * @param y       an array of observations of the dependent variable
     * @param weights an array of weights to apply to each observation, can be null.
     * @param x       each array is a column containing all the observations of one regressor.
     */
    @Override
    public void estimateModel(double[] y, @Nullable double[] weights, double[]... x) {

        Preconditions.checkArgument(x.length==numberOfRegressorsBesidesIntercept);
        if(y.length > numberOfRegressorsBesidesIntercept + 2)
        {
            try{
                resultMatrix = LinearRegression.regress(y,weights,x);

            }catch (RuntimeException matrixIsSingular)
            {
                //if here, the matrix is singular!
                resultMatrix = new Matrix(1,1+numberOfRegressorsBesidesIntercept);
                resultMatrix.set(0,0,LinearRegression.takeYAverage(y));
                for(int i=0;i<numberOfRegressorsBesidesIntercept; i++)
                    resultMatrix.set(0,i+1,0);

            }
        }
        else
        {
            //default to average, if needed
            resultMatrix = new Matrix(1,1+numberOfRegressorsBesidesIntercept);
            resultMatrix.set(0,0,LinearRegression.takeYAverage(y));
            for(int i=0;i<numberOfRegressorsBesidesIntercept; i++)
                resultMatrix.set(0,i+1,0);
        }

    }

    /**
     * What is the model prediction for the y associated to this specific x
     *
     * @param x where we want to estimate y
     * @return the y estimated or NAN if it can't be predicted (because the model wasn't estimated or there aren't enough data points or whatever)
     */
    @Override
    public double predict(double... x) {
        Preconditions.checkArgument(x.length==numberOfRegressorsBesidesIntercept);

        if(resultMatrix == null)
            return Double.NaN;

        double[] beta = getResultMatrix();
        assert beta.length == x.length + 1;
        double toReturn = beta[0];
        for(int i=1; i<beta.length; i++)
            toReturn += x[i-1] * beta[i];

        return toReturn;

    }

    /**
     * What is the dimension of this model?
     *
     * @return
     */
    @Override
    public int getNumberOfRegressors() {
        return numberOfRegressorsBesidesIntercept;
    }

    /**
     * get the coefficients in an array or null if there is no result
     * @return
     */
    public double[]  getResultMatrix() {
        if(resultMatrix != null)
            return resultMatrix.getRowPackedCopy();
        else
            return null;
    }
}
