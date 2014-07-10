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
 * <p> easy IPD process: y_t = r*y_{t-1} + a +b*u_{t-d} + (intercepts if needed)
 *
 * <p>
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
public class IntegratorPlusDeadTime implements DynamicProcess {


    private double intercept = 0;

    private double policySlope = 0;

    private double depreciationRate = 0;

    private double previousOutput = 0;

    final private DelayBin<Double> delayBin;

    private Supplier<Double> randomNoiseMaker;

    public IntegratorPlusDeadTime(double intercept, double policySlope) {
        this(intercept, policySlope,1,0,0 );
    }


    public IntegratorPlusDeadTime(double intercept, double policySlope, double depreciationRate, double initialY, int delay, Double... olderInputs) {
        this.intercept = intercept;
        this.policySlope = policySlope;
        this.depreciationRate = depreciationRate;
        this.previousOutput = initialY;
        delayBin = new DelayBin<>(delay,0d);
        Preconditions.checkArgument(olderInputs.length <=delay);
        for(double initialInput : olderInputs)
            delayBin.addAndRetrieve(initialInput);
    }

    @Override
    public double newStep(double todayInput, double... covariants) {
        double covariantsSum = 0;
        if(covariants!= null && covariants.length > 0)
            for(double covariant : covariants)
                covariantsSum += covariant;

        todayInput = delayBin.addAndRetrieve(todayInput);

        double output = previousOutput*depreciationRate + intercept + policySlope*todayInput + covariantsSum;
        if(randomNoiseMaker != null)
            output += randomNoiseMaker.get();
        previousOutput = output;
        return output;
    }

    @Override
    public Supplier<Double> getRandomNoise() {

        return randomNoiseMaker;
    }

    @Override
    public void setRandomNoise(Supplier<Double> randomNoise) {
        this.randomNoiseMaker = randomNoise;
    }
}
