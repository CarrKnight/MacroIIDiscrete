/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import java.util.Arrays;

/**
 * <h4>Description</h4>
 * <p>
 * <p>
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-07-19
 * @see
 */
//todo turn this into a gradient descent

public class RecursiveLeastMeansFilter implements RecursiveLinearRegression {


    private double stepSize = 1;

    private boolean roundPredictedValues = true;

    private double betas[];

    private boolean normalizeByObservationSize = true;


    public RecursiveLeastMeansFilter(int dimension, double stepSize) {
        betas = new double[dimension];
        this.stepSize = stepSize;
    }

    public RecursiveLeastMeansFilter(double[] initialBetas, double stepSize) {
        betas = new double[initialBetas.length];
        this.stepSize = stepSize;
        betas = initialBetas.clone();
    }

    @Override
    public void addObservation(double weight, double y, double... observation) {

        //reweight
        //weight, if needed
        if(weight != 1) {
            assert weight > 0;
            double w = Math.sqrt(weight);
            for(int i=0; i< observation.length; i++)
                observation[i] *= w;

            //reweight y
            y = y* w;
        }


        //get error
        double prediction = 0;
        for(int i=0; i<betas.length; i++) {
            prediction += betas[i] * observation[i];
            assert Double.isFinite(betas[i]);
        }
        assert Double.isFinite(prediction);
        if(roundPredictedValues) {
            prediction = Math.round(prediction);
            y = Math.round(y);
        }
        double error = y - prediction;

        //get adjustment
        double deltaBeta = stepSize * error;
        //weight it, if needed
        if(normalizeByObservationSize)
        {
            double square = 0;
            for (double anObservation : observation)
                square += anObservation * anObservation;
            deltaBeta = deltaBeta/square;
        }


        for(int i=0; i< betas.length; i++) {
            betas[i] = betas[i] + deltaBeta * observation[i];
            assert Double.isFinite(betas[i]);
        }

        System.out.println(Arrays.toString(betas));



    }

    @Override
    public double[] getBeta() {
        return betas;
    }

    @Override
    public double[] setBeta(int index, double newValue) {
        betas[index] = newValue;
        return betas;
    }
}
