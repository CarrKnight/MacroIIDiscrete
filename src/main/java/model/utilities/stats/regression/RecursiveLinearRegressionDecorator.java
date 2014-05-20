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
public abstract class RecursiveLinearRegressionDecorator implements RecursiveLinearRegression {

    private final RecursiveLinearRegression decorated;

    public RecursiveLinearRegressionDecorator(RecursiveLinearRegression decorated) {
        this.decorated = decorated;
    }


    @Override
    public void addObservation(double observationWeight, double y, double... observation) {
        decorated.addObservation(observationWeight, y, observation);
    }

    @Override
    public double[] getBeta() {
        return decorated.getBeta();
    }

    @Override
    public double[] setBeta(int index, double newValue) {
        return decorated.setBeta(index, newValue);
    }

    @Override
    public double getNoiseVariance() {
        return decorated.getNoiseVariance();
    }

    @Override
    public void setNoiseVariance(double noiseVariance) {
        decorated.setNoiseVariance(noiseVariance);
    }

    @Override
    public double[][] getpCovariance() {
        return decorated.getpCovariance();
    }

    @Override
    public void setPCovariance(double[][] pCovariance) {
        decorated.setPCovariance(pCovariance);
    }

    @Override
    public double getTrace() {
        return decorated.getTrace();
    }

    public RecursiveLinearRegression getDecorated() {
        return decorated;
    }
}
