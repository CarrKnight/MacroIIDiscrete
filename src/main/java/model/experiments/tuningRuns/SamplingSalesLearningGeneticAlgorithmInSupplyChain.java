/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.experiments.tuningRuns;

import agents.firm.Firm;
import agents.firm.production.Blueprint;
import agents.firm.production.control.maximizer.algorithms.marginalMaximizers.RobustMarginalMaximizer;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.prediction.SamplingLearningIncreasePurchasePredictor;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.prediction.SamplingLearningDecreaseSalesPredictor;
import com.google.common.base.Preconditions;
import financial.market.Market;
import goods.GoodType;
import model.MacroII;
import model.scenario.OneLinkSupplyChainScenarioWithCheatingBuyingPrice;
import model.utilities.stats.collectors.enums.MarketDataType;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.watchmaker.framework.*;
import org.uncommons.watchmaker.framework.factories.AbstractCandidateFactory;
import org.uncommons.watchmaker.framework.operators.AbstractCrossover;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.selection.RouletteWheelSelection;
import org.uncommons.watchmaker.framework.termination.GenerationCount;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static model.experiments.tuningRuns.MarginalMaximizerPIDTuning.printProgressBar;

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
 * @version 2013-10-08
 * @see
 */
public class SamplingSalesLearningGeneticAlgorithmInSupplyChain
{



    public static class SamplingSalesLearningIndividual
    {

        private final int howManyDaysOnAverageToSample;

        /**
         * maximum number of days BEHIND THE LAST SHOCK DAY to examine
         */
        private final int maximumDaysToLookBack;

        private final int maximumDaysToLookForward;

        public SamplingSalesLearningIndividual(int howManyDaysOnAverageToSample,
                                               int maximumDaysToLookBack, int maximumDaysToLookForward) {
            this.howManyDaysOnAverageToSample = howManyDaysOnAverageToSample;
            this.maximumDaysToLookBack = maximumDaysToLookBack;
            this.maximumDaysToLookForward = maximumDaysToLookForward;
        }


        @Override
        public String toString() {
            return  "{" +
                    ", LookForward=" + maximumDaysToLookForward +
                    ", LookBack=" + maximumDaysToLookBack +
                    ", frequency=" + howManyDaysOnAverageToSample + "}";
        }

        private int getHowManyDaysOnAverageToSample() {
            return howManyDaysOnAverageToSample;
        }

        public int getMaximumDaysToLookBack() {
            return maximumDaysToLookBack;
        }

        public int getMaximumDaysToLookForward() {
            return maximumDaysToLookForward;
        }
    }


    public static class SamplingSalesLearningFactory extends AbstractCandidateFactory<SamplingSalesLearningIndividual>
    {

        private final static int minimumLookBack = 5;

        private final static int maximumLookBack = 3000;

        private final static int minFrequency = 1;

        private final static int maxFrequency = 40;

        private final static int minimumLookForward = 5;

        private final static int maximumLookForward = 3000;

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
            List<SamplingSalesLearningIndividual> mutatedPopulation = new LinkedList<>();
            for(SamplingSalesLearningIndividual individual : selectedCandidates)
            {

                boolean lookBackM = rng.nextFloat() < probability;
                boolean lookForwardM= rng.nextFloat() < probability;
                boolean frequencyM = rng.nextFloat() < probability;

                boolean mutation = lookBackM || lookForwardM || frequencyM;

                if(mutation)
                {

                    int lookBack = individual.getMaximumDaysToLookBack();
                    if(lookBackM)
                    {
                        lookBack += rng.nextInt(30)-15;
                        lookBack = Math.max(lookBack,1);
                    }

                    int lookForward = individual.getMaximumDaysToLookForward();
                    if(lookForwardM)
                    {
                        lookForward += rng.nextInt(30)-15;
                        lookForward = Math.max(lookForward,1);
                    }

                    int frequency = individual.getHowManyDaysOnAverageToSample();
                    if(lookForwardM)
                    {
                        frequency += rng.nextInt(6)-3;
                        frequency = Math.max(frequency,1);
                    }
                    SamplingSalesLearningIndividual newIndividual =
                            new SamplingSalesLearningIndividual(frequency,lookBack,lookForward);
                    mutatedPopulation.add(newIndividual);

                }
                {
                    mutatedPopulation.add(individual);
                }

            }
            return mutatedPopulation;

        }

    }


    public static class SupplyChainFitness implements FitnessEvaluator<SamplingSalesLearningIndividual>
    {

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
        public double getFitness(final SamplingSalesLearningIndividual candidate,
                                 List<? extends SamplingSalesLearningIndividual> population)
        {

            SamplingLearningDecreaseSalesPredictor.defaultHowManyDaysOnAverageToSample = candidate.getHowManyDaysOnAverageToSample();

            SamplingLearningDecreaseSalesPredictor.defaultMaximumDaysToLookBack = candidate.getMaximumDaysToLookBack();

            SamplingLearningDecreaseSalesPredictor.defaultMaximumDaysToLookForward = candidate.getMaximumDaysToLookForward();


            double totalError = 1;

            for(long i=0; i < 5; i ++)
            {
                final MacroII macroII = new MacroII(i);
                final OneLinkSupplyChainScenarioWithCheatingBuyingPrice scenario1 = new OneLinkSupplyChainScenarioWithCheatingBuyingPrice(macroII)
                {

                    @Override
                    protected SalesDepartment createSalesDepartment(Firm firm, Market goodmarket) {
                        SalesDepartment salesDepartment = super.createSalesDepartment(firm, goodmarket);
                        SamplingLearningDecreaseSalesPredictor predictorStrategy = new SamplingLearningDecreaseSalesPredictor(macroII);
                        predictorStrategy.setHowManyDaysOnAverageToSample(candidate.getHowManyDaysOnAverageToSample());
                        predictorStrategy.setMaximumDaysToLookBack(candidate.getMaximumDaysToLookBack());
                        predictorStrategy.setMaximumDaysToLookForward(candidate.getMaximumDaysToLookForward());
                        salesDepartment.setPredictorStrategy(predictorStrategy);

                        Preconditions.checkArgument(
                                ((SamplingLearningDecreaseSalesPredictor) salesDepartment.getPredictorStrategy()).getHowManyDaysOnAverageToSample() ==
                                        candidate.getHowManyDaysOnAverageToSample());
                        Preconditions.checkArgument(
                                ((SamplingLearningDecreaseSalesPredictor) salesDepartment.getPredictorStrategy()).getMaximumDaysToLookBack() ==
                                        candidate.getMaximumDaysToLookBack());
                        Preconditions.checkArgument(
                                ((SamplingLearningDecreaseSalesPredictor) salesDepartment.getPredictorStrategy()).getMaximumDaysToLookForward() ==
                                        candidate.getMaximumDaysToLookForward());


                        return salesDepartment;    //To change body of overridden methods use File | Settings | File Templates.
                    }

                    @Override
                    protected PurchasesDepartment createPurchaseDepartment(Blueprint blueprint, Firm firm) {
                        PurchasesDepartment purchaseDepartment = super.createPurchaseDepartment(blueprint, firm);
                        if(purchaseDepartment== null)
                            return purchaseDepartment;

                        SamplingLearningIncreasePurchasePredictor predictor = new SamplingLearningIncreasePurchasePredictor(macroII);
                        predictor.setHowManyDaysOnAverageToSample(candidate.getHowManyDaysOnAverageToSample());
                        predictor.setMaximumDaysToLookBack(candidate.getMaximumDaysToLookBack());
                        predictor.setMaximumDaysToLookForward(candidate.getMaximumDaysToLookForward());
                        purchaseDepartment.setPredictor(predictor);
                        return purchaseDepartment;
                    }
                };

                scenario1.setControlType(RobustMarginalMaximizer.class);
                scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
                scenario1.setBeefPriceFilterer(null);


                //competition!
                scenario1.setNumberOfBeefProducers(1);
                scenario1.setNumberOfFoodProducers(5);

                scenario1.setDivideProportionalGainByThis(50f);
                scenario1.setDivideIntegrativeGainByThis(50f);
                //no delay
                scenario1.setBeefPricingSpeed(0);


                macroII.setScenario(scenario1);
                macroII.start();


                while(macroII.schedule.getTime()<9000)
                {
                    macroII.schedule.step(macroII);
                    printProgressBar(9001,(int)macroII.schedule.getSteps(),100);
                }


                //I used to assert this:
                //Assert.assertEquals(macroII.getMarket(GoodType.FOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE),85l,6l );
                //but that's too hard because while on average the price hovers there, competition is noisy. Sometimes a lot.
                //so what I did was to attach a daily stat collector and then check the average of the last 10 prices
                float averageFoodPrice = 0;
                float averageBeefProduced = 0;
                float averageBeefPrice=0;
                for(int j=0; j< 1000; j++)
                {
                    //make the model run one more day:
                    macroII.schedule.step(macroII);
                    averageFoodPrice += macroII.getMarket(GoodType.FOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE);
                    averageBeefProduced+= macroII.getMarket(GoodType.BEEF).countTodayProductionByRegisteredSellers();
                    averageBeefPrice+= macroII.getMarket(GoodType.BEEF).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE);
                }

                totalError *= Math.sqrt(Math.pow(20.794-averageBeefProduced/1000f,2)+1);
            //    totalError *= Math.sqrt(Math.pow(65.353-averageBeefPrice/1000f,2)+1);
                totalError *= Math.sqrt(Math.pow(80.206-averageFoodPrice/1000f,2)+1);

            }

            System.out.println( totalError + " <---- " + candidate );
            return totalError;
        }

        /**
         * false
         */
        @Override
        public boolean isNatural() {
            return false;
        }
    }


    public static void main(String[] args) throws IOException {
        List<EvolutionaryOperator<SamplingSalesLearningIndividual>> operators = new LinkedList<>();
        operators.add(new CrossoverIndividual());
        operators.add(new RandomMutation(.25f));
        EvolutionPipeline<SamplingSalesLearningIndividual> pipeline =
                new EvolutionPipeline<SamplingSalesLearningIndividual>(operators);

        GenerationalEvolutionEngine<SamplingSalesLearningIndividual> engine =
                new GenerationalEvolutionEngine<SamplingSalesLearningIndividual>(new SamplingSalesLearningFactory(),
                        pipeline, new SupplyChainFitness(),new RouletteWheelSelection(),new MersenneTwisterRNG());

        Files.deleteIfExists(Paths.get("geneticLearning")); //delete the file, if needed
        final Path file = Files.createFile(Paths.get("geneticLearning")); //recreate.


        engine.setSingleThreaded(false);

        engine.addEvolutionObserver(new EvolutionObserver<SamplingSalesLearningIndividual>() {
            @Override
            public void populationUpdate(PopulationData<? extends SamplingSalesLearningIndividual> data) {

                System.out.println("Generation " +  data.getGenerationNumber() + ": " + data.getBestCandidate() + "\t Mean fitness:" +
                        data.getMeanFitness());

                try(BufferedWriter writer = Files.newBufferedWriter(
                        file, Charset.defaultCharset(), StandardOpenOption.APPEND))
                {
                    writer.append("Generation " +  data.getGenerationNumber() + ": " + data.getBestCandidate() + "\t Mean fitness:" +
                            data.getMeanFitness());
                    writer.newLine();
                    writer.flush();

                }catch(IOException exception){
                    System.out.println("Error writing to file");
                }



            }
        });

        engine.evolve(50,5,new GenerationCount(20));

    }



}
