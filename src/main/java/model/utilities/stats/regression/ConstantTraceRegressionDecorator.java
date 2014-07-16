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
public class ConstantTraceRegressionDecorator extends RecursiveLinearRegressionDecorator {

    private double constant = 1000;


    public ConstantTraceRegressionDecorator(KalmanBasedRecursiveRegression decorated) {
        super(decorated);
    }

    public ConstantTraceRegressionDecorator(KalmanBasedRecursiveRegression decorated, double constant) {
        super(decorated);
        this.constant = constant;
    }

    @Override
    public void addObservation(double observationWeight, double y, double... observation) {
        super.addObservation(observationWeight, y, observation);

        double trace = getTrace();
        double toMultiply = constant/ trace;
        double pMatrix[][] = getpCovariance();
        for(int i=0; i < observation.length; i++)
            for(int j=0; j < observation.length; j++)
                pMatrix[i][j] *= toMultiply;

        setPCovariance(pMatrix);

    }

    public double getConstant() {
        return constant;
    }

    public void setConstant(double constant) {
        this.constant = constant;
    }
}
