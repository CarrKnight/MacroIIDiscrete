/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import com.google.common.base.Preconditions;
import model.utilities.filters.ExponentialFilter;

import java.util.Arrays;
import java.util.function.Function;

/**
 * <h4>Description</h4>
 * <p> This works basically as a scattershot. We have multiple linear regressions, each with its own guessed delay and we pick the one with the lowest prediction error
 * <p> The regression at position 0 is always the non-dynamic default!
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
public class SISOGuessingRegression implements SISORegression {

    private final ExponentialFilter<Double> errors[];


    private final SISORegression[] regressions;


    private float exponentialAveragingWeight = 0.01f;

    private int minimum = 0;

    private boolean minimumToBeRevalued = false;

    private int howManyObservationsBeforeModelSelection = 100;

    private int observations = 0;

    //the linear fallback: a regression with no time dimension.
    private final NonDynamicRegression linearFallback = new NonDynamicRegression();
    //the error of the fallback
    private final ExponentialFilter<Float> fallbackError;

    /**
     * basically builds a FOPDT regression if I tell you this is my guessed delay
     */
    public final  static Function<Integer,SISORegression> DEFAULT_REGRESSION_FROM_GUESS_BUILDER = KalmanFOPDTRegressionWithKnownTimeDelay::new;


    public SISOGuessingRegression(int... guesses) {
        this(DEFAULT_REGRESSION_FROM_GUESS_BUILDER,guesses);


    }

    public SISOGuessingRegression(Function<Integer, SISORegression> regressionFromGuessBuilder, int... guesses) {
        Preconditions.checkArgument(guesses.length >= 1, "need more than one guess, man");
        errors = new ExponentialFilter[guesses.length];
        regressions = new SISORegression[guesses.length];
        //create the various guessed regressions
        for(int i=0; i<guesses.length; i++)
        {
            errors[i] = new ExponentialFilter<>(exponentialAveragingWeight);
            regressions[i] = regressionFromGuessBuilder.apply(guesses[i]);
        }
        fallbackError = new ExponentialFilter<>(exponentialAveragingWeight);


    }


    public void addObservation(float output, float input){

        //   System.out.println(output + "," + input);
        Preconditions.checkArgument(Float.isFinite(output));
        Preconditions.checkArgument(Float.isFinite(input));

        //dynamic regression
        for(int i=0; i< regressions.length; i++)
        {
            if(Float.isFinite(input) && observations > howManyObservationsBeforeModelSelection)
                errors[i].addObservation(Math.pow(output - regressions[i].predictNextOutput(input),2));
            regressions[i].addObservation(output,input);
        }
        //fallback regressions
        if(Float.isFinite(input) && observations > howManyObservationsBeforeModelSelection)
            fallbackError.addObservation((float) Math.pow(output - linearFallback.predictNextOutput(input),2));
        linearFallback.addObservation(output,input);




        minimumToBeRevalued = true; //lazy evaluation
        observations++;

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


    @Override
    public float getIntercept() {
        //find new minimum
        updateMinimumIfNeeded();

        return regressions[minimum].getIntercept();  }

    public float getGain()
    {
        //find new minimum
        updateMinimumIfNeeded();

        return regressions[minimum].getGain();

    }

    @Override
    public float predictNextOutput(float input) {
        //find new minimum
        updateMinimumIfNeeded();

        return regressions[minimum].predictNextOutput(input);

    }



    public int getDelay(){
        //find new minimum
        updateMinimumIfNeeded();

        return regressions[minimum].getDelay();
    }

    public String getErrors() {
        return Arrays.toString(errors);
    }


    @Override
    public String toString() {
        updateMinimumIfNeeded();
        return regressions[minimum].toString();
    }


    /**
     * this is true if the linear fallback has better prediction or the model selected has very small/negative time constant
     * @return
     */
    public boolean isFallbackBetter(){
        updateMinimumIfNeeded();
        return getTimeConstant() <= .001 || fallbackError.getSmoothedObservation() <= errors[minimum].getSmoothedObservation();
    }

    /**
     * use the fallback/linear regression for policy guidance.
     * @param target target (that's the y)
     * @return the policy associated with that target!
     */
    public float fallbackPolicy(float target){
        return linearFallback.impliedMV(target);
    }

}
