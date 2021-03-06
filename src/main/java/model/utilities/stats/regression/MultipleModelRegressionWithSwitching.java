/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import com.google.common.base.Preconditions;
import javafx.util.Pair;
import model.utilities.filters.ExponentialFilter;
import model.utilities.stats.processes.DynamicProcess;

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
public class MultipleModelRegressionWithSwitching implements SISORegression {

    private final ExponentialFilter<Double> errors[];


    private final SISORegression[] regressions;


    private float exponentialAveragingWeight = 0.1f;

    private int minimum = 0;

    private boolean minimumToBeRevalued = false;

    private int howManyObservationsBeforeModelSelection = 500;

    private int observations = 0;

    //the linear fallback: a regression with no time dimension.
    private final NonDynamicRegression linearFallback = new NonDynamicRegression();
    //the error of the fallback
    private final ExponentialFilter<Double> fallbackError;

    /**
     * basically builds a FOPDT regression if I tell you this is my guessed delay
     */
    public final  static Function<Integer,SISORegression> DEFAULT_REGRESSION_FROM_GUESS_BUILDER = integer ->
            new ErrorCorrectingRegressionOneStep(ErrorCorrectingRegressionOneStep.DEFAULT_FORGETTING_FACTOR,integer);

    /**
     * if this is set to true, the linear fallback is always ignored
     */
    private boolean excludeLinearFallback = false;


    private boolean roundError = false;


    public MultipleModelRegressionWithSwitching(int... guesses) {
        this(DEFAULT_REGRESSION_FROM_GUESS_BUILDER,guesses);


    }

    public MultipleModelRegressionWithSwitching(Function<Integer, SISORegression> regressionFromGuessBuilder, int... guesses) {
        Preconditions.checkArgument(guesses.length >= 1, "need more than one guess, man");
        errors = new ExponentialFilter[guesses.length];
        regressions = new SISORegression[guesses.length];
        //create the various guessed regressions
        for(int i=0; i<guesses.length; i++)
        {
            errors[i] = new ExponentialFilter<>(exponentialAveragingWeight);
            errors[i].addObservation(0d); //reset it
            regressions[i] = regressionFromGuessBuilder.apply(guesses[i]);
        }
        fallbackError = new ExponentialFilter<>(exponentialAveragingWeight);


    }

    /**
     * the constructor to have an heterogeneous set of regressions to choose from!
     * @param regressionBuilders the regression builder
     */
    public MultipleModelRegressionWithSwitching(Pair<Function<Integer, SISORegression>,Integer[]>... regressionBuilders) {
        Preconditions.checkArgument(regressionBuilders.length >= 1, "need at least one regression builder");

        int totalSize = 0;
        for(Pair<Function<Integer, SISORegression>,Integer[]> p : regressionBuilders) {
            Preconditions.checkArgument(p.getValue().length > 0, "at least one regression has no guesses");
            totalSize += p.getValue().length;
        }
        errors = new ExponentialFilter[totalSize];
        regressions = new SISORegression[totalSize];


        int i=0;
        for(Pair<Function<Integer, SISORegression>,Integer[]> p : regressionBuilders)
        {
            for(Integer guess : p.getValue())
            {
                errors[i] = new ExponentialFilter<>();
                errors[i].addObservation(0d);
                regressions[i] = p.getKey().apply(guess);
                i++;
            }
        }
        fallbackError = new ExponentialFilter<>(exponentialAveragingWeight);

        assert  i==totalSize;


    }


    public void addObservation(double output, double input, double... intercepts){

        //   System.out.println(output + "," + input);
        Preconditions.checkArgument(Double.isFinite(output));
        Preconditions.checkArgument(Double.isFinite(input), output + "," + input);


        //dynamic regression
        for(int i=0; i< regressions.length; i++)
        {
            if(Double.isFinite(input) && hasEnoughObservations()) {
                double error = output - regressions[i].predictNextOutput(input, intercepts);
                if(roundError)
                    error = Math.round(error);
                errors[i].addObservation(Math.pow(error,2));
            }
            regressions[i].addObservation(output,input,intercepts);
        }
        //fallback regressions
        if(Double.isFinite(input) && hasEnoughObservations()) {
            double error = output - linearFallback.predictNextOutput(input, intercepts);
            if(roundError)
                error = Math.round(error);
            fallbackError.addObservation(Math.pow(error,2));
        }
        linearFallback.addObservation(output,input,intercepts);



        minimumToBeRevalued = true; //lazy evaluation
        observations++;

    }

    public boolean hasEnoughObservations() {
        return observations >= howManyObservationsBeforeModelSelection;
    }


    /**
     * get notified that an observation is skipped. This is usually to avoid having fake/wrong y_t - y_{t-1} from not considering the skipped observation
     *
     * @param skippedOutput
     * @param skippedInput
     * @param skippedIntercepts
     */
    @Override
    public void skipObservation(double skippedOutput, double skippedInput, double... skippedIntercepts) {

        for (SISORegression regression : regressions)
            regression.skipObservation(skippedOutput, skippedInput, skippedIntercepts);
        linearFallback.skipObservation(skippedOutput, skippedInput, skippedIntercepts);

    }

    private void updateMinimumIfNeeded() {
        if(minimumToBeRevalued) {
            minimum = 0;
            for (int i = 1; i < regressions.length; i++)
                if (errors[i].getSmoothedObservation() < errors[minimum].getSmoothedObservation() ||
                        (regressions[i].getTimeConstant() > 0 && regressions[minimum].getTimeConstant() < 0))
                    minimum = i;
            minimumToBeRevalued = false;
        }
    }

    public double getTimeConstant(){
        //find new minimum
        updateMinimumIfNeeded();
        final SISORegression regression = isFallbackBetter() ? linearFallback : regressions[minimum];
        return regression.getTimeConstant();

    }


    @Override
    public double getIntercept() {
        //find new minimum
        updateMinimumIfNeeded();
        final SISORegression regression = isFallbackBetter() ? linearFallback : regressions[minimum];
        return regression.getIntercept();

        }

    public double getGain()
    {
        //find new minimum
        updateMinimumIfNeeded();

        final SISORegression regression = isFallbackBetter() ? linearFallback : regressions[minimum];
        return regression.getGain();

    }

    @Override
    public double predictNextOutput(double input, double... intercepts) {
        //find new minimum
        updateMinimumIfNeeded();

        final SISORegression regression = isFallbackBetter() ? linearFallback : regressions[minimum];
        return regression.predictNextOutput(input);

    }



    public int getDelay(){
        //find new minimum
        updateMinimumIfNeeded();
        final SISORegression regression = isFallbackBetter() ? linearFallback : regressions[minimum];

        return regression.getDelay();
    }

    public String getErrors() {
        return Arrays.toString(errors);
    }


    @Override
    public String toString() {
        updateMinimumIfNeeded();
        final SISORegression regression = isFallbackBetter() ? linearFallback : regressions[minimum];

        return regression.toString();
    }


    /**
     * this is true if the linear fallback has better prediction or the model selected has very small/negative time constant
     * @return
     */
    public boolean isFallbackBetter(){
        if(excludeLinearFallback)
            return false;
        updateMinimumIfNeeded();
        return regressions[minimum].getTimeConstant() < 0 || //negative time constant is basically always bad news. Avoid avoid avoid
                fallbackError.getSmoothedObservation() <= errors[minimum].getSmoothedObservation();
    }

    @Override
    public DynamicProcess generateDynamicProcessImpliedByRegression() {
        //find new minimum
        updateMinimumIfNeeded();

        final SISORegression regression = isFallbackBetter() ? linearFallback : regressions[minimum];

        return regression.generateDynamicProcessImpliedByRegression();
    }


    public int getHowManyObservationsBeforeModelSelection() {
        return howManyObservationsBeforeModelSelection;
    }

    public void setHowManyObservationsBeforeModelSelection(int howManyObservationsBeforeModelSelection) {

        this.howManyObservationsBeforeModelSelection = howManyObservationsBeforeModelSelection;
    }

    public boolean isExcludeLinearFallback() {
        return excludeLinearFallback;
    }

    public void setExcludeLinearFallback(boolean excludeLinearFallback) {
        this.excludeLinearFallback = excludeLinearFallback;
    }

    public boolean isRoundError() {
        return roundError;
    }

    public void setRoundError(boolean roundError) {
        this.roundError = roundError;
    }

    public int getNumberOfObservations() {
        return observations;
    }

    public int getNumberOfModels(){
        return regressions.length;
    }




}
