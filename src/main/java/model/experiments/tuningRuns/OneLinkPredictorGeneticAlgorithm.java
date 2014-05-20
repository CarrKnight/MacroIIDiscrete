/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.experiments.tuningRuns;

import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.production.Blueprint;
import agents.firm.production.control.maximizer.algorithms.marginalMaximizers.MarginalMaximizer;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.prediction.FixedIncreasePurchasesPredictor;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.prediction.FixedDecreaseSalesPredictor;
import agents.firm.sales.prediction.SamplingLearningDecreaseSalesPredictor;
import com.google.common.base.Preconditions;
import financial.market.Market;
import model.MacroII;
import model.scenario.OneLinkSupplyChainScenario;
import model.scenario.OneLinkSupplyChainScenarioWithCheatingBuyingPrice;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.watchmaker.framework.*;
import org.uncommons.watchmaker.framework.factories.AbstractCandidateFactory;
import org.uncommons.watchmaker.framework.operators.AbstractCrossover;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.operators.IntArrayCrossover;
import org.uncommons.watchmaker.framework.selection.RouletteWheelSelection;
import org.uncommons.watchmaker.framework.termination.GenerationCount;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static model.experiments.tuningRuns.MarginalMaximizerPIDTuning.printProgressBar;

/**
 * <h4>Description</h4>
 * <p/> Basically a GA to check what are the parameters that predict best the input demand
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-10-03
 * @see
 */
public class OneLinkPredictorGeneticAlgorithm
{

    public static long commonSeed = 5;


    public static class OneLinkPredictorCandidate
    {


        final private int howManyDaysOnAverageToSample;

        /**
         * maximum number of days BEHIND THE LAST SHOCK DAY to examine
         */
        final private int maximumDaysToLookBack;

        final private int maximumDaysToLookForward;

        public OneLinkPredictorCandidate(int howManyDaysOnAverageToSample, int maximumDaysToLookBack, int maximumDaysToLookForward) {
            this.howManyDaysOnAverageToSample = howManyDaysOnAverageToSample;
            this.maximumDaysToLookBack = maximumDaysToLookBack;
            this.maximumDaysToLookForward = maximumDaysToLookForward;

        }


        public OneLinkPredictorCandidate(int[] parameters) {
            Preconditions.checkState(parameters.length == 3);
            this.howManyDaysOnAverageToSample = parameters[0];
            this.maximumDaysToLookBack = parameters[1];
            this.maximumDaysToLookForward = parameters[2];

        }


        public int[] getParameterArray()
        {
            int[] toReturn = new int[3];
            toReturn[0] = howManyDaysOnAverageToSample;
            toReturn[1] = maximumDaysToLookBack;
            toReturn[2] = maximumDaysToLookForward;

            return toReturn;
        }

        @Override
        public String toString() {
            return "OneLinkPredictorCandidate{" +
                    "howManyDaysOnAverageToSample=" + howManyDaysOnAverageToSample +
                    ", maximumDaysToLookBack=" + maximumDaysToLookBack +
                    ", maximumDaysToLookForward=" + maximumDaysToLookForward +
                    '}';
        }

        public int getHowManyDaysOnAverageToSample() {
            return howManyDaysOnAverageToSample;
        }

        public int getMaximumDaysToLookBack() {
            return maximumDaysToLookBack;
        }

        public int getMaximumDaysToLookForward() {
            return maximumDaysToLookForward;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            OneLinkPredictorCandidate that = (OneLinkPredictorCandidate) o;

            if (howManyDaysOnAverageToSample != that.howManyDaysOnAverageToSample) return false;
            if (maximumDaysToLookBack != that.maximumDaysToLookBack) return false;
            return maximumDaysToLookForward == that.maximumDaysToLookForward;

        }

        @Override
        public int hashCode() {
            int result = howManyDaysOnAverageToSample;
            result = 31 * result + maximumDaysToLookBack;
            result = 31 * result + maximumDaysToLookForward;
            return result;
        }
    }

    private static class OneLinkFactory extends AbstractCandidateFactory<OneLinkPredictorCandidate> {

        private final static int minimumLookBack = 5;

        private final static int maximumLookBack = 1000;

        private final static int minFrequency = 1;

        private final static int maxFrequency = 100;

        private final static int minimumLookForward = 5;

        private final static int maximumLookForward = 1000;
        /**
         * Randomly create a single candidate solution.
         *
         * @param rng The random number generator to use when creating the random
         *            candidate.
         * @return A randomly-initialised candidate.
         */
        @Override
        public OneLinkPredictorCandidate generateRandomCandidate(Random rng) {

            int lookBack = rng.nextInt(maximumLookBack-minimumLookBack) + minimumLookBack;
            int lookForward = rng.nextInt(maximumLookForward-minimumLookForward) + minimumLookForward;
            int frequency = rng.nextInt(maxFrequency-minFrequency) + minFrequency;
            return new OneLinkPredictorCandidate(frequency,lookBack,lookForward);

        }
    }

    /**
     * 50% of the times it just reassign at random each variable amongs offsprings, the other half of the times it just
     * takes a random midpoint.
     */
    public static class CrossoverIndividualOneLink extends AbstractCrossover<OneLinkPredictorCandidate>
    {
        private final static IntArrayCrossover delegate = new IntArrayCrossover(1);

        protected CrossoverIndividualOneLink() {
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
        protected List<OneLinkPredictorCandidate> mate(OneLinkPredictorCandidate parent1,
                                                       OneLinkPredictorCandidate parent2,
                                                             int numberOfCrossoverPoints, Random rng)
        {
            List<int[]> parameterLists = new LinkedList<>();
            parameterLists.add(parent1.getParameterArray());
            parameterLists.add(parent2.getParameterArray());
            parameterLists = delegate.apply(parameterLists,rng);

            List<OneLinkPredictorCandidate> offsprings = new LinkedList<>();
            offsprings.add(new OneLinkPredictorCandidate(parameterLists.get(0)));
            offsprings.add(new OneLinkPredictorCandidate(parameterLists.get(1)));

            return offsprings;

        }

    }


    public static class MutationIndividualOneLink implements EvolutionaryOperator<OneLinkPredictorCandidate>
    {

        float probability = .15f;


        /**
         * 10% of shocking by a random +30-30 most parameters
         * @param selectedCandidates
         * @param rng
         * @return
         */
        @Override
        public List<OneLinkPredictorCandidate> apply(List<OneLinkPredictorCandidate> selectedCandidates, Random rng) {

            List<OneLinkPredictorCandidate> newCandidates = new LinkedList<>();

            for(OneLinkPredictorCandidate candidate : selectedCandidates)
            {
                boolean mutatedFrequency = rng.nextFloat() < probability; int frequency;
                boolean mutatedLookBack = rng.nextFloat() < probability; int lookback;
                boolean mutatedLookForward = rng.nextFloat() < probability; int lookForward;

                frequency = candidate.getHowManyDaysOnAverageToSample();
                if(mutatedFrequency)
                    frequency += (float)(rng.nextInt(10)-5);
                frequency = Math.max(1,frequency);

                lookback = candidate.getMaximumDaysToLookBack();
                if(mutatedLookBack)
                    lookback += (float)(rng.nextInt(60)-30);
                lookback = Math.max(10, lookback);

                lookForward = candidate.getMaximumDaysToLookBack();
                if(mutatedLookForward)
                    lookForward += (float)(rng.nextInt(60)-30);
                lookForward = Math.max(10,lookback);

                if(mutatedFrequency || mutatedLookBack || mutatedLookForward)
                    newCandidates.add(new OneLinkPredictorCandidate(frequency,lookback,lookForward));
                else
                    newCandidates.add(candidate);

            }

            return newCandidates;

        }
    }



    public static class OneLinkPredictorEvaluator implements FitnessEvaluator<OneLinkPredictorCandidate>
    {

        /**
         * run the one link chain twice; check distance of predictor from the real demand
         */
        @Override
        public double getFitness(final OneLinkPredictorCandidate candidate, List<? extends OneLinkPredictorCandidate> population) {

            double totalError = 0;

            //run the test 5 times!
            for(int i=0; i <2; i++)
            {
                final MacroII macroII = new MacroII(commonSeed + i);
                final OneLinkSupplyChainScenarioWithCheatingBuyingPrice scenario1 = new OneLinkSupplyChainScenarioWithCheatingBuyingPrice(macroII)
                {


                    @Override
                    protected void buildBeefSalesPredictor(SalesDepartment dept) {
                        SamplingLearningDecreaseSalesPredictor predictorStrategy = new SamplingLearningDecreaseSalesPredictor();
                        predictorStrategy.setMaximumDaysToLookForward(candidate.getMaximumDaysToLookForward());
                        predictorStrategy.setMaximumDaysToLookBack(candidate.getMaximumDaysToLookBack());
                        predictorStrategy.setHowManyDaysOnAverageToSample(candidate.getHowManyDaysOnAverageToSample());
                        dept.setPredictorStrategy(predictorStrategy);

                    }

                    @Override
                    public void buildFoodPurchasesPredictor(PurchasesDepartment department) {
                        department.setPredictor(new FixedIncreasePurchasesPredictor(0));

                    }

                    @Override
                    protected SalesDepartment createSalesDepartment(Firm firm, Market goodmarket) {
                        SalesDepartment department = super.createSalesDepartment(firm, goodmarket);    //To change body of overridden methods use File | Settings | File Templates.
                        if(goodmarket.getGoodType().equals(OneLinkSupplyChainScenario.OUTPUT_GOOD))
                            department.setPredictorStrategy(new FixedDecreaseSalesPredictor(0));
                        return department;
                    }

                    @Override
                    protected HumanResources createPlant(Blueprint blueprint, Firm firm, Market laborMarket) {
                        HumanResources hr = super.createPlant(blueprint, firm, laborMarket);    //To change body of overridden methods use File | Settings | File Templates.
                        if(!blueprint.getOutputs().containsKey(OneLinkSupplyChainScenario.INPUT_GOOD))
                            hr.setPredictor(new FixedIncreasePurchasesPredictor(0));
                        return hr;
                    }
                };
                scenario1.setControlType(MarginalMaximizer.class);
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


                while(macroII.schedule.getTime()<14000)
                {
                    macroII.schedule.step(macroII);
                    printProgressBar(14001,(int)macroII.schedule.getSteps(),100);
                }


                Preconditions.checkState(scenario1.getMarkets().get(OneLinkSupplyChainScenario.INPUT_GOOD).getSellers().size() == 1);
                Firm beefMonopolist = (Firm) scenario1.getMarkets().get(OneLinkSupplyChainScenario.INPUT_GOOD).getSellers().iterator().next();
                SamplingLearningDecreaseSalesPredictor predictorStrategy =
                        (SamplingLearningDecreaseSalesPredictor) beefMonopolist.getSalesDepartment(OneLinkSupplyChainScenario.INPUT_GOOD).getPredictorStrategy();


                totalError += Math.pow(predictorStrategy.getDecrementDelta() - 1.714285714,2);


            }


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


        List<EvolutionaryOperator<OneLinkPredictorCandidate>> operators = new LinkedList<>();
        operators.add(new CrossoverIndividualOneLink());
        operators.add(new MutationIndividualOneLink());

        EvolutionaryOperator<OneLinkPredictorCandidate> pipeline = new EvolutionPipeline<>(operators);

        OneLinkPredictorEvaluator evaluator = new OneLinkPredictorEvaluator();
        OneLinkFactory factory = new OneLinkFactory();

        RouletteWheelSelection selector = new RouletteWheelSelection();
        final MersenneTwisterRNG rng = new MersenneTwisterRNG();
        GenerationalEvolutionEngine<OneLinkPredictorCandidate> solutions = new GenerationalEvolutionEngine<>(
                factory,pipeline, evaluator, selector, rng
        );

        //update the seeds
        OneLinkPredictorGeneticAlgorithm.commonSeed = rng.nextLong();


        Files.deleteIfExists(Paths.get("oneLinkCompetition")); //delete the file, if needed
        final Path file = Files.createFile(Paths.get("oneLinkCompetition")); //recreate.


        solutions.setSingleThreaded(false);

        solutions.addEvolutionObserver(new EvolutionObserver<OneLinkPredictorCandidate>() {
            @Override
            public void populationUpdate(PopulationData<? extends OneLinkPredictorCandidate> data) {
                System.out.println("Generation " +  data.getGenerationNumber() + ": " + data.getBestCandidate() + "\n Mean fitness:" +
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

                OneLinkPredictorGeneticAlgorithm.commonSeed = rng.nextLong();



            }
        });

        List<EvaluatedCandidate<OneLinkPredictorCandidate>> evaluatedCandidates = solutions.evolvePopulation(50, 5, new GenerationCount(20));
        try(BufferedWriter writer = Files.newBufferedWriter(
                file, Charset.defaultCharset(), StandardOpenOption.APPEND)){
            writer.newLine();
            writer.append(evaluatedCandidates.toString());
            writer.newLine();

        }catch(IOException exception){
            System.out.println("Error writing to file");
        }



    }





}
