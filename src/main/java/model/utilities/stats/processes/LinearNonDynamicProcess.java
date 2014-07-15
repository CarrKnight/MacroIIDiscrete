/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.processes;

import com.google.common.base.Preconditions;
import model.utilities.DelayBin;

import java.util.function.Supplier;

/**
 * <h4>Description</h4>
 * <p> Basically the regression y_t = a+b*u_{t-delay} + covariates
 * <p> Notice that it is basically a non-dynamic process (except for the lag/0 that we force into this interface
 *
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-07-09
 * @see
 */
public class LinearNonDynamicProcess implements DynamicProcess {

    final private DelayBin<Double> delayBin;

    private double intercept;

    private double slope;

    private  Supplier<Double> randomNoise;

    public LinearNonDynamicProcess(int delay, double intercept, double slope, Double... initialInputs) {
        this.intercept = intercept;
        this.slope = slope;
        delayBin = new DelayBin<>(delay,0d);
        Preconditions.checkState(initialInputs.length <= delay);
        for(double input: initialInputs)
            delayBin.addAndRetrieve(input);

    }

    @Override
    public double newStep(double todayInput, double... covariants) {

        double sum = 0;
        if(covariants != null)
            for(double covariant : covariants)
                sum+=covariant;
        double error = randomNoise == null ? 0 : randomNoise.get();

        return intercept + slope * delayBin.addAndRetrieve(todayInput) + sum + error;

    }

    @Override
    public Supplier<Double> getRandomNoise() {
        return randomNoise;
    }

    @Override
    public void setRandomNoise(Supplier<Double> randomNoise) {
        this.randomNoise = randomNoise;
    }
}
