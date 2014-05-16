/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.experiments.tuningRuns;

import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.prediction.SamplingLearningDecreaseSalesPredictor;
import agents.firm.sales.pricing.pid.salesControlWithSmoothedinventoryAndPID;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.scenario.MonopolistScenario;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.watchmaker.framework.*;
import org.uncommons.watchmaker.framework.factories.AbstractCandidateFactory;
import org.uncommons.watchmaker.framework.operators.AbstractCrossover;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.selection.RouletteWheelSelection;
import org.uncommons.watchmaker.framework.termination.GenerationCount;

import java.util.*;

/**
 * <h4>Description</h4>
 * <p/>
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-09-30
 * @see
 */
public class SamplingSalesLearningGeneticAlgorithm {


    public static long commonSeed = 5;



    public static class SamplingSalesLearningIndividual
    {

        private int howManyDaysOnAverageToSample;

        /**
         * maximum number of days BEHIND THE LAST SHOCK DAY to examine
         */
        private int maximumDaysToLookBack;

        private int maximumDaysToLookForward;


        private Map<Long,Double> errors;

        private double totalError = 0;

        public SamplingSalesLearningIndividual(int howManyDaysOnAverageToSample, int maximumDaysToLookBack, int maximumDaysToLookForward) {
            this.howManyDaysOnAverageToSample = howManyDaysOnAverageToSample;
            this.maximumDaysToLookBack = maximumDaysToLookBack;
            this.maximumDaysToLookForward = maximumDaysToLookForward;
            errors = new HashMap<>();

        }

        public void clear(){
            errors.clear();
            totalError = 0;
        }

        /**
         * Computes a result, or throws an exception if unable to do so.
         *
         * @return computed result
         * @throws Exception if unable to compute a result
         */
        public double call(long seed){

            final MacroII macroII = new MacroII(seed);

            MonopolistScenario scenario1 = new MonopolistScenario(macroII);

            //generate random parameters for labor supply and good demand
            int p0= macroII.random.nextInt(100)+100; int p1= macroII.random.nextInt(3)+1;
            scenario1.setDemandIntercept(p0); scenario1.setDemandSlope(p1);
            int w0=macroII.random.nextInt(10)+10; int w1=macroII.random.nextInt(3)+1;
            scenario1.setDailyWageIntercept(w0); scenario1.setDailyWageSlope(w1);
            int a=macroII.random.nextInt(3)+1;
            scenario1.setLaborProductivity(a);


            macroII.setScenario(scenario1);
            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
            //choose a sales control at random, but don't mix hill-climbing with inventory building since they aren't really compatible
            scenario1.setAskPricingStrategy(salesControlWithSmoothedinventoryAndPID.class);


            scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);





            macroII.start();
            macroII.schedule.step(macroII);
            SamplingLearningDecreaseSalesPredictor predictor = new SamplingLearningDecreaseSalesPredictor();
            predictor.setHowManyDaysOnAverageToSample(howManyDaysOnAverageToSample);
            predictor.setMaximumDaysToLookBack(maximumDaysToLookBack);
            predictor.setMaximumDaysToLookForward(maximumDaysToLookForward);
            scenario1.getMonopolist().getSalesDepartment(UndifferentiatedGoodType.GENERIC).setPredictorStrategy(predictor);
            while(macroII.schedule.getTime()<5000)
                macroII.schedule.step(macroII);


            //    System.out.println(predictor.getDecrementDelta() + " - " + p1 * a);
            double distance = Math.abs(p1*a-predictor.getDecrementDelta());
            //double then square the distance whenever it's above .5 (which hopefully will never be)
            if(distance>.5f)
            {
                assert distance*2d > 1;
                distance = Math.exp(distance*2);
            }
            totalError+= distance;
            errors.put(macroII.seed(),distance);


            return distance;
        }

        @Override
        public String toString() {
            return  "{" +
                    "totalError=" + totalError +
                    ", LookForward=" + maximumDaysToLookForward +
                    ", LookBack=" + maximumDaysToLookBack +
                    ", frequency=" + howManyDaysOnAverageToSample + "}";
        }

        private int getHowManyDaysOnAverageToSample() {
            return howManyDaysOnAverageToSample;
        }

        private void setHowManyDaysOnAverageToSample(int howManyDaysOnAverageToSample) {
            this.howManyDaysOnAverageToSample = howManyDaysOnAverageToSample;
        }

        private int getMaximumDaysToLookBack() {
            return maximumDaysToLookBack;
        }

        private void setMaximumDaysToLookBack(int maximumDaysToLookBack) {
            this.maximumDaysToLookBack = maximumDaysToLookBack;
        }

        private int getMaximumDaysToLookForward() {
            return maximumDaysToLookForward;
        }

        private void setMaximumDaysToLookForward(int maximumDaysToLookForward) {
            this.maximumDaysToLookForward = maximumDaysToLookForward;
        }

        public double getTotalError() {
            return totalError;
        }

        public double getErrorForThisSeed(Long seed)
        {
            return errors.get(seed);
        }
    }


    private static class SamplingSalesLearningFactory implements CandidateFactory<SamplingSalesLearningIndividual>{

        private final static int minimumLookBack = 5;

        private final static int maximumLookBack = 1000;

        private final static int minFrequency = 1;

        private final static int maxFrequency = 40;

        private final static int minimumLookForward = 5;

        private final static int maximumLookForward = 1000;


        /**
         * Creates an initial population of candidates.  If more control is required
         * over the composition of the initial population, consider the overloaded
         * {@link #generateInitialPopulation(int, java.util.Collection, java.util.Random)} method.
         *
         * @param populationSize The number of candidates to create.
         * @param rng            The random number generator to use when creating the initial
         *                       candidates.
         * @return An initial population of candidate solutions.
         */
        @Override
        public List<SamplingSalesLearningIndividual> generateInitialPopulation(int populationSize, Random rng) {

            ArrayList<SamplingSalesLearningIndividual> population = new ArrayList<>(populationSize);

            for(int i=0; i<populationSize; i++)
            {
                population.add(generateRandomCandidate(rng));
            }

            assert population.size() == populationSize;

            return population;

        }

        /**
         * Sometimes it is desirable to seed the initial population with some
         * known good candidates, or partial solutions, in order to provide some
         * hints for the evolution process.  This method generates an initial
         * population, seeded with some initial candidates.  If the number of seed
         * candidates is less than the required population size, the factory should
         * generate additional candidates to fill the remaining spaces in the
         * population.
         *
         * @param populationSize The size of the initial population.
         * @param seedCandidates Candidates to seed the population with.  Number
         *                       of candidates must be no bigger than the population size.
         * @param rng            The random number generator to use when creating additional
         *                       candidates to fill the population when the number of seed candidates is
         *                       insufficient.  This can be null if and only if the number of seed
         *                       candidates provided is sufficient to fully populate the initial population.
         * @return An initial population of candidate solutions, including the
         *         specified seed candidates.
         */
        @Override
        public List<SamplingSalesLearningIndividual> generateInitialPopulation(int populationSize, Collection<SamplingSalesLearningIndividual> seedCandidates, Random rng) {

            ArrayList<SamplingSalesLearningIndividual> population = new ArrayList<>(populationSize);

            for(SamplingSalesLearningIndividual individual : seedCandidates)
                population.add(individual);

            for(int i=seedCandidates.size(); i<populationSize; i++)
            {
                population.add(generateRandomCandidate(rng));
            }

            assert population.size() == populationSize;

            return population;

        }

        /**
         * Randomly create a single candidate solution.
         *
         * @param rng The random number generator to use when creating the random
         *            candidate.
         * @return A randomly-initialised candidate.
         */
        @Override
        public SamplingSalesLearningIndividual generateRandomCandidate(Random rng) {

            int lookBack = rng.nextInt(maximumLookBack-minimumLookBack) + minimumLookBack;
            int lookForward = rng.nextInt(maximumLookForward-minimumLookForward) + minimumLookForward;
            int frequency = rng.nextInt(maxFrequency-minFrequency) + minFrequency;
            return new SamplingSalesLearningIndividual(frequency,lookBack,lookForward);

        }
    }

    /**
     * 50% of the times it just reassign at random each variable amongs offsprings, the other half of the times it just
     * takes a random midpoint.
     */
    public static class CrossoverIndividual extends AbstractCrossover<SamplingSalesLearningIndividual>
    {


        protected CrossoverIndividual() {
            super(1);
        }

        /**
         * Perform cross-over on a pair of parents to generate a pair of offspring.
         *
         * @param parent1                 One of two individuals that provides the source material
         *                                for generating offspring.
         * @param parent2                 One of two individuals that provides the source material
         *                                for generating offspring.
         * @param numberOfCrossoverPoints The number of cross-overs performed on the
         *                                two parents.
         * @param rng                     A source of randomness used to determine the location of
         *                                cross-over points.
         * @return A list containing two evolved offspring.
         */
        @Override
        protected List<SamplingSalesLearningIndividual> mate(SamplingSalesLearningIndividual parent1,
                                                             SamplingSalesLearningIndividual parent2,
                                                             int numberOfCrossoverPoints, Random rng) {

            int lookDown1; int lookDown2;
            int lookUp1; int lookUp2;
            int frequency1; int frequency2;

            if(rng.nextBoolean()) //simple crossOver
            {
                //lookdown

                if(rng.nextBoolean())
                {
                    lookDown1 = parent1.getMaximumDaysToLookBack();
                    lookDown2 = parent2.getMaximumDaysToLookBack();
                }
                else
                {
                    lookDown1 = parent2.getMaximumDaysToLookBack();
                    lookDown2 = parent1.getMaximumDaysToLookBack();
                }

                //lookUp
                if(rng.nextBoolean())
                {
                    lookUp1 = parent1.getMaximumDaysToLookForward();
                    lookUp2 = parent2.getMaximumDaysToLookForward();
                }
                else
                {
                    lookUp1 = parent2.getMaximumDaysToLookForward();
                    lookUp2 = parent1.getMaximumDaysToLookForward();
                }

                //frequency
                if(rng.nextBoolean())
                {
                    frequency1 = parent1.getHowManyDaysOnAverageToSample();
                    frequency2 = parent2.getHowManyDaysOnAverageToSample();
                }
                else
                {
                    frequency1 = parent2.getHowManyDaysOnAverageToSample();
                    frequency2 = parent1.getHowManyDaysOnAverageToSample();
                }

            }
            else
            {
                //take some random midpoint
                float midpoint1= rng.nextFloat();
                float midpoint2= rng.nextFloat();

                lookUp1 = Math.round(midpoint1* parent1.getMaximumDaysToLookForward() + (1f-midpoint1) * parent2.getMaximumDaysToLookForward());
                lookUp2 = Math.round(midpoint2* parent1.getMaximumDaysToLookForward() + (1f-midpoint2) * parent2.getMaximumDaysToLookForward());
                lookDown1 = Math.round(midpoint1* parent1.getMaximumDaysToLookBack() + (1f-midpoint1) * parent2.getMaximumDaysToLookBack());
                lookDown2 = Math.round(midpoint2* parent1.getMaximumDaysToLookBack() + (1f-midpoint2) * parent2.getMaximumDaysToLookBack());
                frequency1 = Math.round(midpoint1* parent1.getHowManyDaysOnAverageToSample() + (1f-midpoint1) * parent2.getHowManyDaysOnAverageToSample());
                frequency2 = Math.round(midpoint2* parent1.getHowManyDaysOnAverageToSample() + (1f-midpoint2) * parent2.getHowManyDaysOnAverageToSample());


            }
            //create the offsprings
            SamplingSalesLearningIndividual offspring1 = new SamplingSalesLearningIndividual(frequency1,lookDown1,lookUp1);
            SamplingSalesLearningIndividual offspring2 = new SamplingSalesLearningIndividual(frequency2,lookDown2,lookUp2);
            //put them in a list and return them
            List<SamplingSalesLearningIndividual> offsprings = new ArrayList<>(2);
            offsprings.add(offspring1); offsprings.add(offspring2);
            return offsprings;


        }

    }



    public static class RandomMutation implements EvolutionaryOperator<SamplingSalesLearningIndividual>
    {

        final private float probability;

        private RandomMutation(float probability) {
            this.probability = probability;
        }

        /**
         *
         * @return The evolved individuals.
         */
        @Override
        public List<SamplingSalesLearningIndividual> apply(List<SamplingSalesLearningIndividual> selectedCandidates, Random rng) {
            for(SamplingSalesLearningIndividual individual : selectedCandidates)
            {

                int minimumLookBack = 5;

                int maximumLookBack = 1000;

                int minFrequency = 1;

                int maxFrequency = 25;

                int minimumLookForward = 5;

                int maximumLookForward = 1000;

                if(rng.nextFloat() < probability)
                {




                    int indexToChange = rng.nextInt(3);
                    if(indexToChange == 0)
                        individual.setMaximumDaysToLookBack(rng.nextInt(maximumLookBack - minimumLookBack) + minimumLookBack);
                    else if(indexToChange == 1)
                    {
                        individual.setMaximumDaysToLookBack(rng.nextInt(maximumLookForward-minimumLookForward) + minimumLookForward);
                    }
                    else
                        individual.setHowManyDaysOnAverageToSample(rng.nextInt(maxFrequency - minFrequency) + minFrequency);
                }
            }
            return selectedCandidates;
        }

    }



    public static class SamplingSalesEvaluator implements FitnessEvaluator<SamplingSalesLearningIndividual>{

        private List<Long> seedsToUse = new LinkedList<>();
        {
            seedsToUse.add(1l);
        }

        /**
         * goes through the list of seeds to use, and make the individual predict on all of them, then return the total error
         */
        @Override
        public double getFitness(SamplingSalesLearningIndividual candidate,
                                 List<? extends SamplingSalesLearningIndividual> population) {
            candidate.clear();
            assert seedsToUse.size()>0;
            for(Long seed : seedsToUse)
                candidate.call(seed);

            return candidate.getTotalError();
        }

        /**
         * false, because it's a minimization
         */
        @Override
        public boolean isNatural() {
            return  false;
        }

        public List<Long> getSeedsToUse() {
            return seedsToUse;
        }

        public void setSeedsToUse(List<Long> seedsToUse) {
            this.seedsToUse = seedsToUse;
        }
    }


    public static class TestEvaluator implements FitnessEvaluator<SamplingSalesLearningIndividual>{

        /**
         * Calculates a fitness score for the given candidate.  Whether
         * a higher score indicates a fitter candidate or not depends on
         * whether the fitness scores are natural (see {@link #isNatural}).
         * This method must always return a value greater than or equal to
         * zero.  Framework behaviour is undefined for negative fitness scores.
         *
         * @param candidate  The candidate solution to calculate fitness for.
         * @param population The entire population.  This will include the
         *                   specified candidate.  This is provided for fitness evaluators that
         *                   evaluate individuals in the context of the population that they are
         *                   part of (e.g. a program that evolves game-playing strategies may wish
         *                   to play each strategy against each of the others).  This parameter
         *                   can be ignored by simple fitness evaluators.  When iterating
         *                   over the population, a simple reference equality check (==) can be
         *                   used to identify which member of the population is the specified
         *                   candidate.
         * @return The fitness score for the specified candidate.  Must always be
         *         a non-negative value regardless of natural or non-natural evaluation is
         *         being used.
         */
        @Override
        public double getFitness(SamplingSalesLearningIndividual candidate, List<? extends SamplingSalesLearningIndividual> population) {
            return candidate.getMaximumDaysToLookBack();
        }

        /**
         * <p>Specifies whether this evaluator generates <i>natural</i> fitness
         * scores or not.</p>
         * <p>Natural fitness scores are those in which the fittest
         * individual in a population has the highest fitness value.  In this
         * case the algorithm is attempting to maximise fitness scores.
         * There need not be a specified maximum possible value.</p>
         * <p>In contrast, <i>non-natural</i> fitness evaluation results in fitter
         * individuals being assigned lower scores than weaker individuals.
         * In the case of non-natural fitness, the algorithm is attempting to
         * minimise fitness scores.</p>
         * <p>An example of a situation in which non-natural fitness scores are
         * preferable is when the fitness corresponds to a cost and the algorithm
         * is attempting to minimise that cost.</p>
         * <p>The terminology of <i>natural</i> and <i>non-natural</i> fitness scores
         * is introduced by the Watchmaker Framework to describe the two types of fitness
         * scoring that exist within the framework.  It does not correspond to either
         * <i>standardised fitness</i> or <i>normalised fitness</i> in the EA
         * literature.  Standardised fitness evaluation generates non-natural
         * scores with a score of zero corresponding to the best possible fitness.
         * Normalised fitness evaluation is similar to standardised fitness but
         * with the scores adjusted to fall within the range 0 - 1.</p>
         *
         * @return True if a high fitness score means a fitter candidate
         *         or false if a low fitness score means a fitter candidate.
         */
        @Override
        public boolean isNatural() {
            return  false;
        }
    }

    public static class SeedEvaluator implements FitnessEvaluator<Long>{

        private List<SamplingSalesLearningIndividual> solverPopulation;

        @Override
        public double getFitness(Long candidate, List<? extends Long > population) {
            double fitness=Double.MAX_VALUE;
            for(SamplingSalesLearningIndividual solver : solverPopulation)
                fitness += solver.getErrorForThisSeed(candidate);

            return fitness;
        }

        //the harder the better!
        @Override
        public boolean isNatural() {
            return true;
        }

        public void setSolverPopulation(List<SamplingSalesLearningIndividual> solverPopulation) {
            this.solverPopulation = solverPopulation;
        }

    }

    public static void main(String[] args)
    {

        MersenneTwisterRNG rng = new MersenneTwisterRNG();
        int elites=3;


        /*************************
         * setup the solution problem!
         *******************************/
        List<EvolutionaryOperator<SamplingSalesLearningIndividual>> operators = new LinkedList<>();
        operators.add(new CrossoverIndividual());
        operators.add(new RandomMutation(.15f));

        EvolutionaryOperator<SamplingSalesLearningIndividual> pipeline = new EvolutionPipeline<>(operators);

        SamplingSalesEvaluator evaluator = new SamplingSalesEvaluator();
        SamplingSalesLearningFactory factory = new SamplingSalesLearningFactory();

        RouletteWheelSelection selector = new RouletteWheelSelection();
        GenerationalEvolutionEngine <SamplingSalesLearningIndividual> solutions = new GenerationalEvolutionEngine<>(
                factory,pipeline, evaluator, selector,new MersenneTwisterRNG()
        );

        solutions.setSingleThreaded(false);

        solutions.addEvolutionObserver(new EvolutionObserver<SamplingSalesLearningIndividual>() {
            @Override
            public void populationUpdate(PopulationData<? extends SamplingSalesLearningIndividual> data) {
                System.out.printf("Generation %d: %s\n",
                        data.getGenerationNumber(),
                        data.getBestCandidate());

            }
        });




        /*********************************
         * setup the seeds problem
         *********************************/
        AbstractCandidateFactory<Long> problemCandidateFactory = new AbstractCandidateFactory<Long>() {
            @Override
            public Long generateRandomCandidate(Random rng) {
                return rng.nextLong();
            }
        };
        EvolutionaryOperator<Long> mutation = new EvolutionaryOperator<Long>() {
            @Override
            public List<Long> apply(List<Long> selectedCandidates, Random rng) {
                for(int i=0; i< selectedCandidates.size(); i++)
                    if(rng.nextDouble()<.85f)
                        selectedCandidates.set(i,rng.nextLong());
                return selectedCandidates;
            }
        };




        SeedEvaluator seedEvaluator = new SeedEvaluator();

        GenerationalEvolutionEngine<Long> problemSeeds = new GenerationalEvolutionEngine<>(problemCandidateFactory,
                mutation,
                seedEvaluator, selector,rng

        );

        problemSeeds.addEvolutionObserver(new EvolutionObserver<Long>() {
            @Override
            public void populationUpdate(PopulationData<? extends Long> data) {
                System.out.printf("Generation %d: %s\n",
                        data.getGenerationNumber(),
                        data.getBestCandidate());

            }
        });



        //    solutions.evolve(50, 5, new GenerationCount(10));

        /********************************************************
         * Run!
         ********************************************************/
        //create a new solver population
        List<SamplingSalesLearningIndividual> population = new LinkedList<>();
        population.add(new SamplingSalesLearningIndividual(20,734,329));
        population.add(new SamplingSalesLearningIndividual(28,636,443));
        population.add(new SamplingSalesLearningIndividual(25,622,523));
        population.add(new SamplingSalesLearningIndividual(21,639,329));
        population.add(new SamplingSalesLearningIndividual(26,741,367));
        population.add(new SamplingSalesLearningIndividual(14,746,341));
        while(population.size()<50)
            population.add(factory.generateRandomCandidate(rng));

        //create a new problem population
        List<Long> seedPopulation = new LinkedList<>();
        seedPopulation.add(-5856130985495245994l);
        seedPopulation.add(-510410077698640655l);
        seedPopulation.add(2184372359666435911l);
        seedPopulation.add(147785131115952052l);
        seedPopulation.add(4919926190013044917l);
        seedPopulation.add(-4950490371512117081l);
        seedPopulation.add(8420741333858763114l);
        seedPopulation.add(-3442500444884513992l);


        while(seedPopulation.size()<10)
            seedPopulation.add(problemCandidateFactory.generateRandomCandidate(rng));

        List<EvaluatedCandidate<SamplingSalesLearningIndividual>> populationEvaluated;
        List<EvaluatedCandidate<Long>> problemsEvaluated;
        for(int i=0; i<50; i++)
        {
            //exchange hostages
            seedEvaluator.setSolverPopulation(new LinkedList<>(population));
            evaluator.setSeedsToUse(new LinkedList<>(seedPopulation));

            //evolve solvers
            populationEvaluated = solutions.evolvePopulation(population.size(),elites,population,new GenerationCount(1));

            //create new solver population
            List<SamplingSalesLearningIndividual> tomorrowPopulation = new LinkedList<>();

            //selection
            tomorrowPopulation.addAll(selector.select(populationEvaluated,evaluator.isNatural(),
                    population.size()-elites,rng));
            //heredity
            tomorrowPopulation = pipeline.apply(tomorrowPopulation,rng);
            //elite
            for(int j=0; j< elites; j++)
                tomorrowPopulation.add(populationEvaluated.get(j).getCandidate());


            //create new problem population
            problemsEvaluated = problemSeeds.evolvePopulation(seedPopulation.size(),elites,seedPopulation,new GenerationCount(1));
            List<Long> tomorrowProblems = new LinkedList<>();
            //selection
            tomorrowProblems.addAll(selector.select(problemsEvaluated,seedEvaluator.isNatural(),
                    seedPopulation.size()-elites,rng));
            //heredity
            tomorrowProblems = mutation.apply(tomorrowProblems,rng);
            //elites
            for(int j=0; j< elites; j++)
                tomorrowProblems.add(problemsEvaluated.get(j).getCandidate());
            assert seedPopulation.size() == tomorrowPopulation.size();

            //set the population
            population=tomorrowPopulation;
            seedPopulation = tomorrowProblems;
            System.out.println(population);
            System.out.println(seedPopulation);
            System.out.println("------------------------------------------------------------------------");
            System.out.println();
        }

    }


    //problem population
    //solver population

    //(1)create a random problem population
    //(2)feed it into the solver, study the fitness of the solvers and make it evolve
    //(3)study the fitness of the problem and evolve it
    //loop 2-3

}
