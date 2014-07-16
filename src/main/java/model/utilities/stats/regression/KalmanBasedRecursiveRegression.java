/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

/**
 * <h4>Description</h4>
 * <p/> A recursive regression based on Kalman Filter, and as such based on a P-covariance that forgetting factors can modify easily.
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
public interface KalmanBasedRecursiveRegression extends RecursiveLinearRegression {

    double getNoiseVariance();

    void setNoiseVariance(double noiseVariance);

    double[][] getpCovariance();

    void setPCovariance(double[][] pCovariance);

    double getTrace();
}
