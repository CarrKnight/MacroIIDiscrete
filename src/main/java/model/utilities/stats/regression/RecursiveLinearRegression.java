/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
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
