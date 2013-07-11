/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import com.google.common.base.Preconditions;

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


    /**
     * Do the regression/estimation. The two arrays must be of the same size
     *
     * @param x an array of observations of the independent variable
     * @param y an array of observations of the dependent variable
     */
    @Override
    public void estimateModel(double[] x, double[] y)
    {
        Preconditions.checkArgument(x.length == y.length, "number of observations are not the same!");
        Preconditions.checkArgument(x.length > 0, "no observations in x!");


        //this is adapted (stolen) from here: http://introcs.cs.princeton.edu/java/97data/LinearRegression.java.html
        //of course, it's just a linear regression formula

        //sum up x and y
        double sumx=0; double sumy=0;
        for(int i=0; i < x.length; i++)
        {
            sumx += x[i];
            sumy += y[i];
        }
        double n = x.length;

        //compute averages
        double xbar = sumx / n;
        double ybar = sumy / n;

        // second pass: compute summary statistics
        double xxbar = 0.0, xybar = 0.0;
        for (int i = 0; i < n; i++) {
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

        intercept = ybar - slope * xbar;





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
}
