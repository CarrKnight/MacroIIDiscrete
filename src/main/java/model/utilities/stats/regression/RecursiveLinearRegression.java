/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

/**
 * <h4>Description</h4>
 * <p/>
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
public interface RecursiveLinearRegression {
    void addObservation(double observationWeight, double y, double... observation);

    double[] getBeta();

    double[] setBeta(int index, double newValue);

    double getNoiseVariance();

    void setNoiseVariance(double noiseVariance);

    double[][] getpCovariance();

    void setPCovariance(double[][] pCovariance);

    double getTrace();
}
