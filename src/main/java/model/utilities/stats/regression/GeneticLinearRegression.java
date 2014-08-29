/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.watchmaker.framework.*;
import org.uncommons.watchmaker.framework.factories.AbstractCandidateFactory;
import org.uncommons.watchmaker.framework.operators.AbstractCrossover;
import org.uncommons.watchmaker.framework.operators.DoubleArrayCrossover;
import org.uncommons.watchmaker.framework.selection.RouletteWheelSelection;
import org.uncommons.watchmaker.framework.termination.GenerationCount;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * <h4>Description</h4>
 * <p/> A total joke of a linear regression. Uses a GA to run the regression, mostly because
 * I want to learn how to use the watchmaker library
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-10-07
 * @see
 */
public class GeneticLinearRegression implements MultivariateRegression
{

    private double minimumParameter = -15;

    private double maximumParameter = 15;

    private double[] results;


    /**
     * Do the regression/estimation. The two arrays must be of the same size
     *
     * @param y       an array of observations of the dependent variable
     * @param weights an array of weights to apply to each observation, can be null.
     * @param x       each array is a column containing all the observations of one regressor.
     */
    @Override
    public void estimateModel(double[] y,  double[] weights, double[]... x) throws LinearRegression.CollinearityException
    {
        GenerationalEvolutionEngine<RegressionCandidate> evolutionEngine = new GenerationalEvolutionEngine<>(
                new RegressionCandidateFactory(x.length+1),new AverageAndMutateOperator(),
                new CachingFitnessEvaluator<RegressionCandidate>(new RegressionFitness(y,weights,x)),
                new RouletteWheelSelection(),new MersenneTwisterRNG()
        );



        results = evolutionEngine.evolve(1000,5,new GenerationCount(200)).getParameters();



    }

    public double[] getResults() {
        return results;
    }

    /**
     * What is the model prediction for the y associated to this specific x
     *
     * @param x where we want to estimate y
     * @return the y estimated or NAN if it can't be predicted (because the model wasn't estimated or there aren't enough data points or whatever)
     */
    @Override
    public double predict(double... x) {
        throw new RuntimeException("not implemented yet!");
    }

    //the class representing a candidate solution
    public static class RegressionCandidate
    {
        //every parameter plus intercept
        private final double[] parameters;

        public RegressionCandidate(double... parameters) {
            this.parameters = parameters;
        }

        private double[] getParameters() {
            return parameters;
        }

        @Override
        public String toString() {
            return Arrays.toString(parameters);
        }
    }

    private class RegressionCandidateFactory extends AbstractCandidateFactory<RegressionCandidate>
    {

        final private int howManyParameters;

        private RegressionCandidateFactory(int howManyParameters) {
            this.howManyParameters = howManyParameters;
        }

        /**
         * everything uniform random
         */
        @Override
        public RegressionCandidate generateRandomCandidate(Random rng)
        {
            double[] parameters = new double[howManyParameters];

            parameters[0] = rng.nextDouble() * (400) - 200;
            for(int i=1; i< howManyParameters; i++)
                parameters[i] = rng.nextDouble() * (maximumParameter-minimumParameter) + minimumParameter;

            return new RegressionCandidate(parameters);

        }
    }

    //create 2 offsprings that just two midpoints of the parents. Also shocks by 1% (I assume there is some elitism)
    private class AverageAndMutateOperator extends AbstractCrossover<RegressionCandidate>
    {
        DoubleArrayCrossover crossover = new DoubleArrayCrossover(1);

        private AverageAndMutateOperator() {
            super(1);
        }

        /**
         *create 2 offsprings that just two midpoints of the parents. Also shocks by 1% (I assume there is some elitism)

         */
        @Override
        protected List<RegressionCandidate> mate(RegressionCandidate parent1, RegressionCandidate parent2, int numberOfCrossoverPoints, Random rng) {
            if(rng.nextBoolean()){
                double alpha = rng.nextDouble();

                double[] parametersOffspring1 = new double[parent1.parameters.length];
                double[] parametersOffspring2 = new double[parametersOffspring1.length];

                for(int i=0; i< parametersOffspring1.length; i++)
                {
                    parametersOffspring1[i] = parent1.parameters[i] * alpha + parent2.parameters[i] * (1d-alpha);
                    parametersOffspring2[i] = parent2.parameters[i] * (1+alpha) + parent1.parameters[i] * -alpha;
              /*      if(rng.nextFloat() < .02){
                        parametersOffspring1[i] = parametersOffspring1[i] +  Math.sqrt(parametersOffspring1[i]) * rng.nextGaussian();
                        parametersOffspring2[i] = parametersOffspring2[i] +  Math.sqrt(parametersOffspring2[i]) * rng.nextGaussian();

                    }
                    */
                }




                List<RegressionCandidate> offsprings = new LinkedList<>();
                offsprings.add(new RegressionCandidate(parametersOffspring1));
                offsprings.add(new RegressionCandidate(parametersOffspring2));




                return offsprings;
            }
            else
            {
                List<double[]> parents = new LinkedList<>(); parents.add(parent1.parameters); parents.add(parent2.parameters);
                List<double[]> genes = crossover.apply(parents, rng);
                List<RegressionCandidate> offsprings = new LinkedList<>();
                for(int i=0; i< parent1.parameters.length; i++)
                {
                    if(rng.nextFloat() < .02){
                        genes.get(0)[i] = rng.nextDouble() * (maximumParameter-minimumParameter) + minimumParameter;
                        genes.get(1)[i] = rng.nextDouble() * (maximumParameter-minimumParameter) + minimumParameter;

                    }

                }

                offsprings.add(new RegressionCandidate(genes.get(0)));
                offsprings.add(new RegressionCandidate(genes.get(1)));
                return offsprings;
            }


        }
    }

    //the class that computes R^2
    public static class RegressionFitness implements FitnessEvaluator<RegressionCandidate>
    {

        private final double[] ys;


        private final double[] weights;

        private final double[][] xs;


        public RegressionFitness(double[] y, double[] weights, double[]... x) {
            this.ys = y;
            this.weights = weights;
            this.xs = x;
        }

        /**
         * R^2 of the regression.
         */
        @Override
        public double getFitness(RegressionCandidate candidate,
                                 List<? extends RegressionCandidate> population)
        {
            double[] parameters = candidate.getParameters();

            double r2 = 0;
            boolean weighted = weights != null;
            //unweighted is easy:
            for(int row =0; row<ys.length; row++ )
            {
                float sumX = 0;
                //add intercept
                sumX += parameters[0];
                //add the others
                for(int column =0; column<xs.length; column++)
                {
                    sumX += xs[column][row] * parameters[column+1];
                }
                double residual = ys[row] - sumX;
                if(weighted)
                    residual = residual*weights[row];
                r2 += Math.pow(residual,2);
            }

            return r2;

        }

        /**
         *  false, it's a minimization!
         */
        @Override
        public boolean isNatural() {
            return false;
        }
    }




}
