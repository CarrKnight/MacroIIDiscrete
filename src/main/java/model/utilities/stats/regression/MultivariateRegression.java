/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

/**
 * <h4>Description</h4>
 * <p/> Any model that predicts y given multiple y
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
public interface MultivariateRegression {

    /**
     * Do the regression/estimation. The two arrays must be of the same size
     * @param y an array of observations of the dependent variable
     * @param weights an array of weights to apply to each observation, can be null.
     * @param x each array is a column containing all the observations of one regressor.
     *
     */
    public void estimateModel(double[] y,  double[] weights,double[]... x) throws LinearRegression.CollinearityException;


    /**
     * What is the model prediction for the y associated to this specific x
     * @param x where we want to estimate y
     * @return the y estimated or NAN if it can't be predicted (because the model wasn't estimated or there aren't enough data points or whatever)
     */
    public double predict(double... x);


}
