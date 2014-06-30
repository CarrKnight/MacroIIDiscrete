/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import com.google.common.base.Preconditions;
import model.utilities.filters.ExponentialFilter;

/**
 * <h4>Description</h4>
 * <p> This works basically as a scattershot. We have multiple linear regressions, each with its own guessed delay and we pick the one with the lowest prediction error
 * <p>
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-06-30
 * @see
 */
public class KalmanFOPDTRegressionWithUnknownTimeDelay {

    private final ExponentialFilter<Double> errors[];


    private final KalmanFOPDTRegressionWithKnownTimeDelay[] regressions;

    private int minimum = 0;

    private boolean minimumToBeRevalued = false;

    private float previousInput = Float.NaN;

    public KalmanFOPDTRegressionWithUnknownTimeDelay(int... guesses) {
        Preconditions.checkArgument(guesses.length > 1, "need more than one guess, man");
        errors = new ExponentialFilter[guesses.length];
        regressions = new KalmanFOPDTRegressionWithKnownTimeDelay[guesses.length];
        //create the various guessed regressions
        for(int i=0; i<guesses.length; i++)
        {
            errors[i] = new ExponentialFilter<>(.005f);
            regressions[i] = new KalmanFOPDTRegressionWithKnownTimeDelay(guesses[i]);
        }


    }


    public void addObservation(float output, float input){

        Preconditions.checkArgument(Float.isFinite(output));
        Preconditions.checkArgument(Float.isFinite(input));

        for(int i=0; i< regressions.length; i++)
        {
            if(Float.isFinite(previousInput))
                errors[i].addObservation(Math.pow(output - regressions[i].predictNextOutput(previousInput),2));
            regressions[i].addObservation(output,input);
        }





        previousInput = input;

        minimumToBeRevalued = true; //lazy evaluation

    }


    public double[] getBeta() {
        updateMinimumIfNeeded();
        return regressions[minimum].getBeta();
    }

    private void updateMinimumIfNeeded() {
        if(minimumToBeRevalued) {
            minimum = 0;
            for (int i = 1; i < regressions.length; i++)
                if (errors[i].getSmoothedObservation() < errors[minimum].getSmoothedObservation())
                    minimum = i;
            minimumToBeRevalued = false;
        }
    }

    public float getTimeConstant(){
        //find new minimum
        updateMinimumIfNeeded();

        return regressions[minimum].getTimeConstant();

    }


    public float getGain()
    {
        //find new minimum
        updateMinimumIfNeeded();

        return regressions[minimum].getGain();

    }


    public int getDelay(){
        //find new minimum
        updateMinimumIfNeeded();

        return regressions[minimum].getDelay();
    }
}
