/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.processes;

import com.google.common.base.Preconditions;

import java.util.function.Supplier;

/**
 * <h4>Description</h4>
 * <p> Can "step" forward, producing a new y
 * <p> It's usually single input single output but there may be other variables of interest, those are covariants
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-06-25
 * @see
 */
public interface DynamicProcess {


    public double newStep(double todayInput, double... covariants);

    public Supplier<Double> getRandomNoise();

    public void setRandomNoise(Supplier<Double> randomNoise);

    /**
     * utility method to simulate a series of steps where the input is fixed
     * @param toSimulate process to step
     * @param steps how many steps
     * @param fixedInput the input throughout these new steps
     * @param covariants the  fixed covariants, if any
     * @return
     */
    public static double simulateManyStepsWithFixedInput(DynamicProcess toSimulate, int steps, double fixedInput,
                                                         double... covariants){
        Preconditions.checkArgument(steps>1);
        Preconditions.checkArgument(toSimulate != null);             assert toSimulate != null;
        double output = Double.NaN;
        for(int i=0; i<steps; i++) {
            output = toSimulate.newStep(fixedInput,covariants);
        }
        assert !Double.isNaN(output);
        return output;

    }



}
