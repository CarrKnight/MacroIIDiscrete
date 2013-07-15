/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import com.sun.istack.internal.Nullable;

/**
 * <h4>Description</h4>
 * <p/> Any model that regresses x on y and then use that model to estimate y given x
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
public interface UnivariateRegression
{


    /**
     * Do the regression/estimation. The two arrays must be of the same size
     * @param x an array of observations of the independent variable
     * @param y an array of observations of the dependent variable
     * @param weights an array of weights to apply to each observation
     *
     */
    public void estimateModel(double[] x, double[] y, @Nullable double[] weights);


    /**
     * What is the model prediction for the y associated to this specific x
     * @param x where we want to estimate y
     * @return the y estimated or NAN if it can't be predicted (because the model wasn't estimated or there aren't enough data points or whatever)
     */
    public double predict(double x);

}
