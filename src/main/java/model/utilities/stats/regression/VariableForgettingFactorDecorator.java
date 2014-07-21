/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import com.google.common.base.Objects;

import java.util.Arrays;

/**
 * <h4>Description</h4>
 * <p> This is variable forgetting with trace limit. Copied from "generalized predictive control with dual adaptation".
 * <p>
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-07-18
 * @see
 */
public class VariableForgettingFactorDecorator extends RecursiveLinearRegressionDecorator {


    private final double errorWeight;

    private final double maximumTrace;
    private double forgettingFactor;


    public VariableForgettingFactorDecorator(KalmanBasedRecursiveRegression decorated) {
        this(decorated,1000000000,1000000000);
    }

    public VariableForgettingFactorDecorator(KalmanBasedRecursiveRegression decorated,
                                             double errorWeight, double maximumTrace) {
        super(decorated);
        this.errorWeight = errorWeight;
        this.maximumTrace = maximumTrace;
    }


    @Override
    public void addObservation(double observationWeight, double y, double... observation) {


        //todo this is copy-pasted from Kalman, it's ugly to duplicate code like this
        //compute denominator
        int dimensions = observation.length;
        double[][] pCovariance = getpCovariance();
        double px[] = new double[dimensions];
        for(int i=0; i<dimensions; i++)
        {
            for(int j=0; j<dimensions; j++) {
                px[i] += pCovariance[i][j] * observation[j];
            }
        }

        double denominator = 0;
        for(int i=0; i<dimensions; i++)
            denominator += observation[i] * px[i];
        denominator += 1 / observationWeight;
        //compute error
        double predicted = 0;
        for(int i=0; i< dimensions; i++)
            predicted += getBeta()[i] * observation[i];
        double residual = y - predicted;

        //now compute lambda!
        forgettingFactor = 1 - Math.pow(residual,2)/(errorWeight*denominator);


        super.addObservation(observationWeight, y, observation);


        //get the new covariance
        double [][] candidateP = new double[getpCovariance().length][];
        for(int i = 0; i < getpCovariance().length; i++)
            candidateP[i] = getpCovariance()[i].clone();

        //reweight by forgetting factor
        float trace = 0;
        for(int i=0;i<dimensions; i++)
            for(int j=0; j<dimensions; j++) {
                candidateP[i][j] *= 1d / forgettingFactor;
                if(i==j)
                    trace += candidateP[i][j];
            }


        if(trace < maximumTrace)
            setPCovariance(candidateP);




    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("lambda", forgettingFactor)
                .add("beta", Arrays.toString(getBeta()))
                .toString();
    }
}
