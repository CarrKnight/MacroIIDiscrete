/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import model.utilities.filters.MovingVariance;

/**
 * <h4>Description</h4>
 * <p> The idea is to penalize or even censor observarions when the covariance of Y and X is very small.
 * This is because when we get to equilibrium we tend to get the same observations all the time and those are kind of useless.
 * <p>
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-07-06
 * @see
 */
public class AutovarianceReweighterDecorator extends RecursiveLinearRegressionDecorator {


    private final MovingVariance<Double> dependentVariableVariance;

    private final MovingVariance<Double>[] independentVariableVariances;

    private final int[] independentVariablesToObserve;

    /**
     * the first elements have to be added without knowing the variance. We will add them with weight 100. Then once we know their variance, we will use this factor to scale all regressions by this
     */
    private float scaleFactor = Float.NaN;

    public AutovarianceReweighterDecorator(RecursiveLinearRegression decorated, int observationWindow, int... independentVariablesToObserve) {
        super(decorated);
        dependentVariableVariance = new MovingVariance(observationWindow);
        this.independentVariablesToObserve = independentVariablesToObserve;
        independentVariableVariances = new MovingVariance[independentVariablesToObserve.length];
        for(int i= 0; i< independentVariableVariances.length; i++)
            independentVariableVariances[i] = new MovingVariance<>(observationWindow);

    }


    @Override
    public void addObservation(double observationWeight, double y, double... observation) {


        observe(y, observation);
        double newObservationWeight = buildWeight() * observationWeight;
        if(newObservationWeight > .01) //don't bother downstream with very low weights.
            super.addObservation(newObservationWeight, y, observation);
    }

    private void observe(double y, double[] observation) {
        dependentVariableVariance.addObservation(y);
        for(int i=0; i< independentVariableVariances.length; i++ )
            independentVariableVariances[i].addObservation(observation[independentVariablesToObserve[i]]);
        //one being not nan means the others are not nan as well
        assert independentVariableVariances.length ==0 || (!dependentVariableVariance.isReady() || independentVariableVariances[0].isReady() );
    }

    private double buildWeight() {
        double newObservationWeight = 100; //by default, 100
        if(dependentVariableVariance.isReady())
        {
            //scale factor is initialized only once so we know if this is the first time or not
            if(Float.isNaN(scaleFactor))
                scaleFactor = 100/sumOfCoefficientsOfDetermination();
            newObservationWeight = scaleFactor * sumOfCoefficientsOfDetermination();
        }
        return newObservationWeight;
    }


    private float sumOfCoefficientsOfDetermination(){
        float sum = dependentVariableVariance.getRelativeStandardDeviation();
        for(MovingVariance v : independentVariableVariances)
            sum+=v.getRelativeStandardDeviation();
        assert  Float.isFinite(sum);
        assert sum >= 0;
        return sum;
    }



}
