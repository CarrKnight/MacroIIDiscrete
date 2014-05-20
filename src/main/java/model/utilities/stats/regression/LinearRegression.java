/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import Jama.Matrix;
import com.google.common.base.Preconditions;
import com.sun.istack.internal.Nullable;

import java.util.Arrays;

/**
 * <h4>Description</h4>
 * <p/> The simplest regression, looking for y= a+ b*x
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-07-11
 * @see
 */
public class LinearRegression implements UnivariateRegression
{

    /**
     * the intercept of the estimated model
     */
    private double intercept = Double.NaN;

    /**
     * the slope of the estimated model
     */
    private double slope = Double.NaN;

    public static boolean shortcut = false;


    /**
     * Do the regression/estimation. The two arrays must be of the same size
     *
     * @param x an array of observations of the independent variable
     * @param y an array of observations of the dependent variable
     */
    @Override
    public void estimateModel(double[] x, double[] y, double[] weights)
    {
        Preconditions.checkArgument(x.length == y.length, "number of observations are not the same!");
        Preconditions.checkArgument(x.length > 0, "no observations in x!");
        Preconditions.checkArgument(weights == null || weights.length == x.length, "the length of weights is wrong");

        //the try is to check for non-invertibility (this is faster than building all the matrices)
        try{
            double[] clonedWeights = weights == null ? null : weights.clone();
            Matrix result = regress(y.clone(), clonedWeights, x.clone());

            slope = result.get(1,0);
            intercept = result.get(0,0);
        }
        catch (CollinearityException ex)
        {
            double average = takeYAverage(y);
            slope=0;
            intercept = average;
        }






         /*


        //if there are no weights, multiply everything by 1
        if(weights == null){
            weights = new double[x.length];
            for(int i=0; i < weights.length; i++)
                weights[i]=1;
        }
        else {
            Preconditions.checkArgument(weights.length == y.length, "number of weights is different than observations");
            for(int i=0; i < x.length; i++)
            {
                x[i] = x[i] ;
                y[i] = y[i] ;

            }
        }



        //this is adapted (stolen) from here: http://introcs.cs.princeton.edu/java/97data/LinearRegression.java.html
        //of course, it's just a linear regression formula

        //sum up x and y
        double sumx=0; double sumy=0;
        for(int i=0; i < x.length; i++)
        {
            sumx += x[i] ;
            sumy += y[i] ;
        }
        double n = x.length;

        //compute averages
        double xbar = sumx / n;
        double ybar = sumy / n;

        // second pass: compute summary statistics
        double xxbar = 0.0, xybar = 0.0;
        for (int i = 0; i < n; i++)
        {
            xxbar += (x[i] - xbar) * (x[i] - xbar);
            xybar += (x[i] - xbar) * (y[i] - ybar);
        }

        //xxbar is 0 when every x is the same
        if(xxbar != 0)
            slope = xybar / xxbar;
        else
        {
            slope=0;
            System.err.println("estimated a model with no slope");
        }

*/





    }

    public static double takeYAverage(double[] y) {
        double sum = 0;
        for(double yObs : y)
        {
            sum += yObs;
        }
        sum = sum/(double)y.length;
        return sum;
    }

    public static Matrix regressOld(double[] y, double[] weights, double[]... regressors) throws CollinearityException {
        //build weights

        Preconditions.checkArgument(regressors.length > 0);

        //build the X matrix
        Matrix xMatrix= new Matrix(regressors[0].length,regressors.length+1);

        double[] intercept = new double[regressors[0].length];
        //if there are weights, do this:
        if(weights!=null)
        {
            //get the square root of the weight and multiply everything by it!
            for(int i=0; i< weights.length; i++)
            {
                double w = Math.sqrt(weights[i]);
                intercept[i] = w;
                //reweight x
                for(double[] x : regressors)
                    x[i] = x[i] * w;
                //reweight y
                y[i] = y[i] * w;

            }
        }
        //otherwise all intercept is one
        else
        {
            for(int i=0; i<intercept.length; i++)
                intercept[i]=1;
        }
        //build the Y matrix
        Matrix yMatrix= new Matrix(intercept.length,1);

        //set them in the matrix
        for(int row=0; row< xMatrix.getRowDimension(); row++)
        {
            xMatrix.set(row,0,intercept[row]);
            for(int i=0; i < regressors.length; i++)
                xMatrix.set(row,i+1,regressors[i][row]);
            yMatrix.set(row,0,y[row]);
        }







        //the usual formula is (X'X)^-1 X'y
        //(X'X)^-1:
        Matrix xMatrixTranspose = xMatrix.transpose();
        Matrix xtx = xMatrixTranspose.times(xMatrix);

        if(Math.abs(xtx.det())<.01)
            throw new CollinearityException();

        xtx = xtx.inverse();
        //(X'Y):
        Matrix xty = xMatrixTranspose.times(yMatrix);
        //result:
        Matrix result = xtx.times(xty);

        assert result.getRowDimension()==regressors.length+1;
        assert result.getColumnDimension()==1;
        return result;
    }

    public static Matrix regress(double[] y, double[] weights, double[]... regressors) throws CollinearityException
    {
        return regress(y,weights,true,regressors);
    }

    public static Matrix regress(double[] y, double[] weights,  boolean withIntercept,double[]... regressors) throws CollinearityException {
        Preconditions.checkArgument(regressors.length > 0);

        //build the X matrix
        Matrix xMatrix= new Matrix(regressors[0].length,regressors.length+1);

        double[] intercept = null;
        if(withIntercept)
            intercept = new double[regressors[0].length];
        //if there are weights, do this:
        if(weights!=null)
        {
            //get the square root of the weight and multiply everything by it!
            for(int i=0; i< weights.length; i++)
            {
                double w = Math.sqrt(weights[i]);
                if(withIntercept)
                    intercept[i] = w;
                //reweight x
                for(double[] x : regressors)
                    x[i] = x[i] * w;
                //reweight y
                y[i] = y[i] * w;

            }
        }
        //otherwise all intercept is one
        else
        {
            if(withIntercept)
                Arrays.fill(intercept,1);
        }

        if(withIntercept)
        {
            //add intercept to regressors
            double[][] newRegressors =new double[regressors.length+1][];
            newRegressors[0]=intercept;
            for(int i=0; i< regressors.length; i++)
                newRegressors[i+1]=regressors[i];
            regressors=newRegressors;
        }


        //build the XTX matrix and XTY matrix
        double[][] xtxArray = new double[regressors.length][regressors.length];
        double[] xtyArray = new double[regressors.length]; Arrays.fill(xtyArray,Double.NaN);
        for(int firstParameter = 0; firstParameter < regressors.length; firstParameter++)
            for(int secondParameter = firstParameter; secondParameter < regressors.length; secondParameter++)
            {
                double squaredSumX = 0; //this goes into X'X
                double sumXY = 0; //this goes into X'Y
                boolean xtyNeedsFilling = Double.isNaN(xtyArray[firstParameter]); //if this is true, we fill the X'Y vector too
                for( int i=0; i < y.length; i++) //get the sum squared
                {
                    squaredSumX += regressors[firstParameter][i] * regressors[secondParameter][i];
                    if(xtyNeedsFilling)
                        sumXY += regressors[firstParameter][i] * y[i];
                }
                //fill the matrices
                xtxArray[firstParameter][secondParameter] = squaredSumX;
                if(firstParameter != secondParameter) //the two triangles are symmetric in X'X
                    xtxArray[secondParameter][firstParameter] = squaredSumX;

                if(xtyNeedsFilling)
                    xtyArray[firstParameter] = sumXY;



            }


        //now we have a small matrix and a vector, should be much quicker to invert!

        //the usual formula is (X'X)^-1 X'y
        Matrix xtx = new Matrix(xtxArray);
        //(X'X)^-1:
        try{
            xtx = xtx.inverse();
        }catch (RuntimeException e)
        {
            //if we fail here, the xtx is singular, which means collinearity
            throw new CollinearityException();
        }
        //(X'Y):
        Matrix xty = new Matrix(xtyArray,xtyArray.length);
        //result:
        Matrix result = xtx.times(xty);

        assert result.getRowDimension()==regressors.length;
        assert result.getColumnDimension()==1;
        return result;
    }






    /**
     * What is the model prediction for the y associated to this specific x
     *
     * @param x where we want to estimate y
     * @return the y estimated or NAN if it can't be estimated!
     */
    @Override
    public double predict(double x) {
        //if we don't have a slope, we can't predict
        if(Double.isNaN(slope))
            return Double.NaN;

        //slope and intercept are always estimated together, so if one is not nan, the other also isn't!
        assert !Double.isNaN(intercept);

        return intercept + slope * x;


    }


    /**
     * Gets the slope of the estimated model.
     *
     * @return Value of the slope of the estimated model.
     */
    public double getSlope() {
        return slope;
    }

    /**
     * Gets the intercept of the estimated model.
     *
     * @return Value of the intercept of the estimated model.
     */
    public double getIntercept() {
        return intercept;
    }


    @Override
    public String toString() {
        if(slope ==0)
            return "y= " + getIntercept();
        else
        {
            String plus = slope > 0 ? " + " : " ";
            return "y= " + getIntercept() + plus + getSlope() +" * x";

        }
    }


    public static class CollinearityException extends Exception
    {

    }
}
