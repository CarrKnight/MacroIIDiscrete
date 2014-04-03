/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.experiments.tuningRuns;

import agents.firm.Firm;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.prediction.LearningDecreaseWithTimeSeriesSalesPredictor;
import agents.firm.sales.pricing.pid.SmoothedDailyInventoryPricingStrategy;
import au.com.bytecode.opencsv.CSVReader;
import com.google.common.base.Preconditions;
import financial.market.Market;
import goods.GoodType;
import model.MacroII;
import model.scenario.MonopolistScenario;
import model.scenario.TripolistScenario;
import model.utilities.stats.collectors.SalesData;
import model.utilities.stats.collectors.enums.SalesDataType;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.watchmaker.framework.*;
import org.uncommons.watchmaker.framework.factories.AbstractCandidateFactory;
import org.uncommons.watchmaker.framework.operators.AbstractCrossover;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.operators.IntArrayCrossover;
import org.uncommons.watchmaker.framework.operators.ObjectArrayCrossover;
import org.uncommons.watchmaker.framework.selection.RouletteWheelSelection;
import org.uncommons.watchmaker.framework.termination.GenerationCount;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static org.mockito.Mockito.*;

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
 * @version 2013-10-04
 * @see
 */
public class LearningTimeSeriesGeneticAlgorithm
{


    public static long commonSeed = 0;

    public static class LearningCandidate
    {

        /**
         * whether it is going to be a WLS regression
         */
        final private boolean usingWeights;

        /**
         * adds DeltaP as a regressor
         */
        final private boolean correctingWithDeltaPrice;

        /**
         * the quantity is usually outflow, unless this is set to true
         */
        final private boolean regressingOnWorkers;

        final private int howManyDaysForwardToLook;

        final private int howManyDaysBackToLook;

        final private int oneObservationEveryHowManyDays;

        final private int howManyShockDaysBackToLookFor;

        final private int deadTime;


        public LearningCandidate(boolean usingWeights, boolean correctingWithDeltaPrice, boolean regressingOnWorkers,
                                 int howManyDaysForwardToLook, int howManyDaysBackToLook,
                                 int oneObservationEveryHowManyDays, int howManyShockDaysBackToLookFor, int deadTime) {
            this.usingWeights = usingWeights;
            this.correctingWithDeltaPrice = correctingWithDeltaPrice;
            this.regressingOnWorkers = regressingOnWorkers;
            this.howManyDaysForwardToLook = howManyDaysForwardToLook;
            this.howManyDaysBackToLook = howManyDaysBackToLook;
            this.oneObservationEveryHowManyDays = oneObservationEveryHowManyDays;
            this.howManyShockDaysBackToLookFor = howManyShockDaysBackToLookFor;
            this.deadTime = deadTime;
        }

        public LearningCandidate(Boolean[] booleanParameters,int[] integerParameters) {
            Preconditions.checkArgument(booleanParameters.length == 3);
            Preconditions.checkArgument(integerParameters.length == 5);
            this.usingWeights = booleanParameters[0];
            this.correctingWithDeltaPrice = booleanParameters[1];
            this.regressingOnWorkers = booleanParameters[2];
            this.howManyDaysForwardToLook = integerParameters[0];
            this.howManyDaysBackToLook = integerParameters[1];
            this.oneObservationEveryHowManyDays = integerParameters[2];
            this.howManyShockDaysBackToLookFor = integerParameters[3];
            this.deadTime = integerParameters[4];
        }


        public boolean isUsingWeights() {
            return usingWeights;
        }

        public boolean isCorrectingWithDeltaPrice() {
            return correctingWithDeltaPrice;
        }

        public boolean isRegressingOnWorkers() {
            return regressingOnWorkers;
        }

        public int getHowManyDaysForwardToLook() {
            return howManyDaysForwardToLook;
        }

        public int getHowManyDaysBackToLook() {
            return howManyDaysBackToLook;
        }

        public int getOneObservationEveryHowManyDays() {
            return oneObservationEveryHowManyDays;
        }

        public int getHowManyShockDaysBackToLookFor() {
            return howManyShockDaysBackToLookFor;
        }

        public int getDeadTime() {
            return deadTime;
        }

        public int[] getIntegerParameters(){
            int[] integerParameters = new int[5];
            integerParameters[0] = this.howManyDaysForwardToLook;
            integerParameters[1] = this.howManyDaysBackToLook;
            integerParameters[2] = this.oneObservationEveryHowManyDays ;
            integerParameters[3] = this.howManyShockDaysBackToLookFor;
            integerParameters[4] = this.deadTime;
            return integerParameters;


        }


        public Boolean[] getBooleanParameters(){
            Boolean[] booleanParameters = new Boolean[3];
            booleanParameters[0] = this.usingWeights ;
            booleanParameters[1] = this.correctingWithDeltaPrice;
            booleanParameters[2]=  this.regressingOnWorkers;
            return booleanParameters;


        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("{");
            sb.append("usingWeights=").append(usingWeights);
            sb.append(", correctingWithDeltaPrice=").append(correctingWithDeltaPrice);
            sb.append(", regressingOnWorkers=").append(regressingOnWorkers);
            sb.append(", howManyDaysForwardToLook=").append(howManyDaysForwardToLook);
            sb.append(", howManyDaysBackToLook=").append(howManyDaysBackToLook);
            sb.append(", oneObservationEveryHowManyDays=").append(oneObservationEveryHowManyDays);
            sb.append(", howManyShockDaysBackToLookFor=").append(howManyShockDaysBackToLookFor);
            sb.append(", dead time=").append(deadTime);
            sb.append('}');
            return sb.toString();
        }
    }


    public static class LearningCandidateFactory extends AbstractCandidateFactory<LearningCandidate>
    {

        private int minhowManyDaysForwardToLook = 10;
        private int maxhowManyDaysForwardToLook = 2000;

        private int minhowManyDaysBackToLook = 10;
        private int maxhowManyDaysBackToLook = 2000;

        private int minOneObservationEveryHowManyDays = 1;
        private int maxOneObservationEveryHowManyDays = 1;

        private int minHowManyShockdaysBackToLook = 1;
        private int maxHowManyShockdaysToLook = 15;

        private int minDeadTime = 1;
        private int maxDeadTime = 200;

        /**
         * Randomly create a single candidate solution.
         *
         * @param rng The random number generator to use when creating the random
         *            candidate.
         * @return A randomly-initialised candidate.
         */
        @Override
        public LearningCandidate generateRandomCandidate(Random rng) {
            return new LearningCandidate(rng.nextBoolean(),rng.nextBoolean(),rng.nextBoolean(),
                    rng.nextInt(maxhowManyDaysForwardToLook-minhowManyDaysForwardToLook) + minhowManyDaysForwardToLook,
                    rng.nextInt(maxhowManyDaysBackToLook-minhowManyDaysBackToLook) + minhowManyDaysBackToLook,
                    1,//rng.nextInt(maxOneObservationEveryHowManyDays - minOneObservationEveryHowManyDays) + minOneObservationEveryHowManyDays,
                    rng.nextInt(maxHowManyShockdaysToLook-minHowManyShockdaysBackToLook) + minHowManyShockdaysBackToLook,
                    rng.nextInt(maxDeadTime-minDeadTime) + minDeadTime
                    );


        }
    }


    public static class LearningCrossover extends AbstractCrossover<LearningCandidate>{


        /**
         *
         */
        public LearningCrossover(int crossoverPoints) {
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
        protected List<LearningCandidate> mate(LearningCandidate parent1, LearningCandidate parent2, int numberOfCrossoverPoints, Random rng)
        {

            //50% chance of averaging, 50% of crossover

            if(rng.nextBoolean()){
                //get the genes
                List<int[]> parentsIntegerGenes = new LinkedList<>();
                parentsIntegerGenes.add( parent1.getIntegerParameters());
                parentsIntegerGenes.add( parent2.getIntegerParameters());
                List<Boolean[]> parentsBooleanGenes = new LinkedList<>();
                parentsBooleanGenes.add( parent1.getBooleanParameters());
                parentsBooleanGenes.add( parent2.getBooleanParameters());



                //prepare crossovers
                IntArrayCrossover intsCrossovers = new IntArrayCrossover(rng.nextInt(3)+1);
                List<int[]> offspringIntegerGenes = intsCrossovers.apply(parentsIntegerGenes, rng);




                ObjectArrayCrossover<Boolean> booleanCrossover = new ObjectArrayCrossover<>(rng.nextInt(2)+1);


                List<Boolean[]> offspringBooleanGenes = booleanCrossover.apply(parentsBooleanGenes, rng);

                List<LearningCandidate> offsprings = new LinkedList<>();

                offsprings.add(new LearningCandidate(offspringBooleanGenes.get(0),offspringIntegerGenes.get(0)));
                offsprings.add(new LearningCandidate(offspringBooleanGenes.get(1),offspringIntegerGenes.get(1)));

                return offsprings;
            }
            else
            {

                //get the genes
                int[] offspring1IntGenes = new int[5];
                int[] offspring2IntGenes = new int[5];
                float alpha = rng.nextFloat();
                for(int i=0; i < offspring1IntGenes.length; i++)
                {
                    offspring1IntGenes[i] = Math.round(alpha * parent1.getIntegerParameters()[i] +   (1f-alpha) * parent2.getIntegerParameters()[i]);
                    offspring2IntGenes[i] = Math.round(alpha * parent2.getIntegerParameters()[i] +   (1f-alpha) * parent1.getIntegerParameters()[i]);


                }

                List<Boolean[]> parentsBooleanGenes = new LinkedList<>();
                parentsBooleanGenes.add( parent1.getBooleanParameters());
                parentsBooleanGenes.add( parent2.getBooleanParameters());
                ObjectArrayCrossover<Boolean> booleanCrossover = new ObjectArrayCrossover<>(rng.nextInt(2)+1);


                List<Boolean[]> offspringBooleanGenes = booleanCrossover.apply(parentsBooleanGenes, rng);

                List<LearningCandidate> offsprings = new LinkedList<>();

                offsprings.add(new LearningCandidate(offspringBooleanGenes.get(0),offspring1IntGenes));
                offsprings.add(new LearningCandidate(offspringBooleanGenes.get(1),offspring2IntGenes));

                return offsprings;




            }

        }
    }


    public static class LearningMutator implements EvolutionaryOperator<LearningCandidate> {

        float probability = .15f;




        /**
         * Every parameter has the same fixed chance of mutating (mutation is a normal shock)
         */
        @Override
        public List<LearningCandidate> apply(List<LearningCandidate> selectedCandidates, Random rng) {
            List<LearningCandidate> newCandidates = new LinkedList<>();
            for(LearningCandidate candidate : selectedCandidates)
            {
                boolean anyMutation = false;


                int lookBack = candidate.getHowManyDaysBackToLook();
                if(rng.nextFloat() < probability)
                {
                    lookBack += Math.max(rng.nextInt(30)-15,10);
                    anyMutation = true;
                }

                int lookForward = candidate.getHowManyDaysForwardToLook();
                if(rng.nextFloat() < probability)
                {
                    lookForward += rng.nextInt(30)-15;
                    lookForward = Math.max(lookForward,10);

                    anyMutation = true;
                }

                int oneObservationEveryHowManyDays = candidate.getOneObservationEveryHowManyDays();
         /*       if(rng.nextFloat() < probability)
                {
                    oneObservationEveryHowManyDays += rng.nextInt(6)-3;
                    oneObservationEveryHowManyDays = Math.max(oneObservationEveryHowManyDays,1);

                    anyMutation = true;
                }
                */

                int howManyShockDaysBackToLookFor = candidate.getHowManyShockDaysBackToLookFor();
                if(rng.nextFloat() < probability)
                {
                    howManyShockDaysBackToLookFor +=rng.nextInt(6)-3;
                    howManyShockDaysBackToLookFor = Math.max(howManyShockDaysBackToLookFor,1);
                    anyMutation = true;
                }

                int deadTime = candidate.getDeadTime();
                if(rng.nextFloat() < probability)
                {
                    deadTime +=rng.nextInt(16)-8;
                    deadTime = Math.max(deadTime,1);
                    anyMutation = true;
                }

                boolean usingInflow = candidate.isRegressingOnWorkers();
                if(rng.nextFloat() < probability)
                {
                    usingInflow = !usingInflow;
                    anyMutation = true;
                }

                boolean weights = candidate.isUsingWeights();
                if(rng.nextFloat() < probability)
                {
                    weights = !weights;
                    anyMutation = true;
                }

                boolean correcting = candidate.isCorrectingWithDeltaPrice();
                if(rng.nextFloat() < probability)
                {
                    correcting = !correcting;
                    anyMutation = true;
                }


                if(anyMutation)
                    newCandidates.add(new LearningCandidate(weights,correcting,usingInflow,lookForward,lookBack,
                            oneObservationEveryHowManyDays,howManyShockDaysBackToLookFor,deadTime));
                else
                    newCandidates.add(candidate);

            }

            return newCandidates;


        }


        public float getProbability() {
            return probability;
        }

        public void setProbability(float probability) {
            this.probability = probability;
        }
    }


    public static class LearningEvaluator implements FitnessEvaluator<LearningCandidate>{


        //6 monopolist and 2 competitive allScenarios!


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
        public double getFitness(LearningCandidate candidate, List<? extends LearningCandidate> population) {

            double monopolistError = 0;
            double competitiveError = 0;
            //  System.out.println(commonSeed);

            for(int i =0; i<6; i++)
            {
                double distance = Math.min(monopolistRun(candidate, i), 10000);

                monopolistError += distance;
            }
            int[] competitors = new int[2];
            competitors[0]=2;competitors[1]=4;
            for(int competitor  : competitors)
            {

                double totalDistance = Math.min(competitive(candidate, competitor), 10000); //avoid infinity
                competitiveError+=totalDistance;
            }

            System.out.println("competitive: " + competitiveError +" --- monopolist: " + monopolistError + ", candidate: " + candidate);
            return Math.sqrt(competitiveError+1)*Math.sqrt(monopolistError+1);
        }

        private double competitive(LearningCandidate candidate, int competitor) {
            final MacroII macroII = new MacroII(commonSeed+competitor);
            final TripolistScenario scenario1 = new TripolistScenario(macroII);
            scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
            scenario1.setAskPricingStrategy(SmoothedDailyInventoryPricingStrategy.class);
            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
            scenario1.setAdditionalCompetitors(competitor-1);


            //assign scenario
            macroII.setScenario(scenario1);

            macroII.start();
            macroII.schedule.step(macroII);
            for(Firm f : scenario1.getCompetitors()){
                SalesDepartment department = f.getSalesDepartment(GoodType.GENERIC);
                LearningDecreaseWithTimeSeriesSalesPredictor predictor = new LearningDecreaseWithTimeSeriesSalesPredictor(
                        candidate.isUsingWeights(),candidate.isCorrectingWithDeltaPrice(),candidate.isRegressingOnWorkers(),
                        candidate.getHowManyDaysForwardToLook(),candidate.getHowManyDaysBackToLook(),
                        candidate.getOneObservationEveryHowManyDays(),candidate.getHowManyShockDaysBackToLookFor()
                );
                department.setPredictorStrategy(predictor);
            }


            while(macroII.schedule.getTime()<7000)
            {
                macroII.schedule.step(macroII);


            }
            double quantity = 0;
            while(macroII.schedule.getTime()<8000)
            {
                macroII.schedule.step(macroII);
                quantity +=  scenario1.getMarkets().get(GoodType.GENERIC).getYesterdayVolume();
            }
            quantity= quantity/1000d;

            for(Firm f : scenario1.getCompetitors()){
                SalesDepartment department = f.getSalesDepartment(GoodType.GENERIC);

                LearningDecreaseWithTimeSeriesSalesPredictor predictor =
                        (LearningDecreaseWithTimeSeriesSalesPredictor) department.getPredictorStrategy();

                if(predictor.getLastRegressionDay() == -1) //if you have run no regressions: BAD!
                    return Double.MAX_VALUE;
            }


            return Math.pow(quantity-44d,2);
        }

        private double monopolistRun(LearningCandidate candidate, int i) {
            //monopolist:
            final MacroII macroII = new MacroII(commonSeed+i);

            MonopolistScenario scenario1 = new MonopolistScenario(macroII);

            //generate random parameters for labor supply and good demand
            int p0= macroII.random.nextInt(100)+100;
            int p1= macroII.random.nextInt(3)+1;
            scenario1.setDemandIntercept(p0);
            scenario1.setDemandSlope(p1);
            int w0=macroII.random.nextInt(10)+10;
            int w1=macroII.random.nextInt(3)+1;
            scenario1.setDailyWageIntercept(w0);
            scenario1.setDailyWageSlope(w1);
            int a=macroII.random.nextInt(3)+1;
            scenario1.setLaborProductivity(a);


            macroII.setScenario(scenario1);
            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
            //choose a sales control at random, but don't mix hill-climbing with inventory building since they aren't really compatible
            scenario1.setAskPricingStrategy(SmoothedDailyInventoryPricingStrategy.class);


            scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);


            macroII.start();
            macroII.schedule.step(macroII);
            LearningDecreaseWithTimeSeriesSalesPredictor predictor = new LearningDecreaseWithTimeSeriesSalesPredictor(
                    candidate.isUsingWeights(),candidate.isCorrectingWithDeltaPrice(),candidate.isRegressingOnWorkers(),
                    candidate.getHowManyDaysForwardToLook(),candidate.getHowManyDaysBackToLook(),
                    candidate.getOneObservationEveryHowManyDays(),candidate.getHowManyShockDaysBackToLookFor()
            );
            scenario1.getMonopolist().getSalesDepartment(GoodType.GENERIC).setPredictorStrategy(predictor);
            while(macroII.schedule.getTime()<4000)
                macroII.schedule.step(macroII);
            double quantity =0;
            while(macroII.schedule.getTime()<5000)
            {
                macroII.schedule.step(macroII);
                quantity +=  scenario1.getMonopolist().getNumberOfWorkersWhoProduceThisGood(GoodType.GENERIC);
            }
            quantity= quantity/1000d;


            //    System.out.println(predictor.getDecrementDelta() + " - " + p1 * a);
            if(predictor.getLastRegressionDay() == -1) //if you have run no regressions: BAD!
                return Double.MAX_VALUE;

            return Math.pow(quantity-MonopolistScenario.findWorkerTargetThatMaximizesProfits(p0,p1,w0,w1,a)
                    ,2);
        }

        /**
         * false
         */
        @Override
        public boolean isNatural() {
            return false;
        }
    }


    public static class SupplyChainEvaluator implements FitnessEvaluator<LearningCandidate>{


        //try to predict the right demand from a real run dataset
        static private final double[][] dataset = new double[SalesDataType.values().length+1][45000];
        static
        {
            try {
                CSVReader reader = new CSVReader(new FileReader("./runs/tunings/supplychaintuning/plantData.csv"));
                String[] header = reader.readNext(); //ignore the header

                for(int i=0; i<dataset[0].length; i++)
                {
                    String[] line = reader.readNext();
                    Preconditions.checkState(line.length == dataset.length);
                    for(int j=0; j<line.length; j++)
                        dataset[j][i] = Double.parseDouble(line[j]);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


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
        public double getFitness(LearningCandidate candidate, List<? extends LearningCandidate> population) {

            double predictionErrorSum = 0;

            LearningDecreaseWithTimeSeriesSalesPredictor predictor = new LearningDecreaseWithTimeSeriesSalesPredictor(
                    candidate.usingWeights,candidate.isCorrectingWithDeltaPrice(),candidate.isRegressingOnWorkers(),candidate.getHowManyDaysForwardToLook(),
                    candidate.getHowManyDaysBackToLook(),candidate.getOneObservationEveryHowManyDays(),candidate.getHowManyShockDaysBackToLookFor());

            SalesDepartment mockedDepartment = mock(SalesDepartment.class);
            MacroII model = mock(MacroII.class);
            SalesData data = new SalesData();
            data.start(model,mockedDepartment);

            //now we create a "real" sales department that we feed in with fake data, weirdly
            Firm fakeOwner = mock(Firm.class);
            Market market = mock(Market.class); when(market.getGoodType()).thenReturn(GoodType.GENERIC);
            SalesDepartment proxy = new SalesDepartmentOneAtATime(fakeOwner, market,null,null,model);
            proxy.setData(data);

            //finally we need to have a set/list of all the shock days
            //create a set that ignores negatives
            final Set<Integer> shockDays = new HashSet<Integer>(){

                @Override
                public boolean add(Integer integer) {
                    if(integer <= 0)
                        return false;
                    else
                        return super.add(integer);
                }
            };

            //now let the fakeowner redirect to the shockdays set
            doAnswer(new Answer<List<Integer>>() {
                @Override
                public List<Integer> answer(InvocationOnMock invocation) throws Throwable {
                    List<Integer> shockList = new ArrayList<>(shockDays);
                    Collections.sort(shockList);
                    return shockList;
                }
            }).when(fakeOwner).getAllDayWithMeaningfulWorkforceChangeInProducingThisGood(GoodType.GENERIC);


            MacroII macroII = new MacroII(1l);
            for(int day=0; day < 20000; day++)
            {
                when(model.getMainScheduleTime()).thenReturn((double) day);
                //feed the data object the dataset we read
                when(mockedDepartment.getTodayInflow()).thenReturn((int)dataset[SalesDataType.INFLOW.ordinal()][day]);
                when(mockedDepartment.getTodayOutflow()).thenReturn((int)dataset[SalesDataType.OUTFLOW.ordinal()][day]);
                when(mockedDepartment.getLastClosingPrice()).thenReturn((long)dataset[SalesDataType.CLOSING_PRICES.ordinal()][day]);
                when(mockedDepartment.getHowManyToSell()).thenReturn((int)dataset[SalesDataType.HOW_MANY_TO_SELL.ordinal()][day]);
                when(mockedDepartment.getTotalWorkersWhoProduceThisGood()).thenReturn((int)dataset[SalesDataType.WORKERS_PRODUCING_THIS_GOOD.ordinal()][day]);
                when(mockedDepartment.getAverageClosingPrice()).thenReturn((float) dataset[SalesDataType.AVERAGE_CLOSING_PRICES.ordinal()][day]);
                when(mockedDepartment.estimateSupplyGap()).thenReturn((float) dataset[SalesDataType.SUPPLY_GAP.ordinal()][day]);
                //now step the data, so that it memorizes it
                if(day>1)
                    data.step(macroII);
                else
                    data.step(model);


                //now add the latest shockday to the set, if needed
                shockDays.add((int)dataset[dataset.length-1][day]);
                when(fakeOwner.getLatestDayWithMeaningfulWorkforceChangeInProducingThisGood(GoodType.GENERIC))
                        .thenReturn((int)Math.round(dataset[dataset.length-1][day]));

                if(day%100==0 && day>=1000)
                {
                    //the real distance should be 12/7
                    //so between 1.5 and 2 is fine
                    predictor.predictSalePriceAfterIncreasingProduction(proxy, 100, 1);
                    float slope = predictor.getDecrementDelta();
                    if(slope < 1.5)
                        predictionErrorSum += Math.pow(slope-1.5f,2);
                    else if(slope > 2)
                    {
                        predictionErrorSum+= Math.pow(slope-2f,2);
                    }
                }


            }
            System.out.println(predictionErrorSum + " - " + candidate);
            return predictionErrorSum;







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

        List<EvolutionaryOperator<LearningCandidate>> operators = new LinkedList<>();
        operators.add(new LearningCrossover(1));
        operators.add(new LearningMutator());

        EvolutionaryOperator<LearningCandidate> pipeline = new EvolutionPipeline<>(operators);

        final MersenneTwisterRNG rng = new MersenneTwisterRNG();



        GenerationalEvolutionEngine<LearningCandidate> evolutionBaby =
                new GenerationalEvolutionEngine<>(new LearningCandidateFactory(),pipeline,
                        new CachingFitnessEvaluator<>(new SupplyChainEvaluator()),
                        new RouletteWheelSelection(), rng);

        Files.deleteIfExists(Paths.get("supplyChainLearning")); //delete the file, if needed
        final Path file = Files.createFile(Paths.get("supplyChainLearning")); //recreate.


        evolutionBaby.setSingleThreaded(false);

        evolutionBaby.addEvolutionObserver(new EvolutionObserver<LearningCandidate>() {
            @Override
            public void populationUpdate(PopulationData<? extends LearningCandidate> data) {

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


        List<EvaluatedCandidate<LearningCandidate>> evaluatedCandidates = evolutionBaby.evolvePopulation(500, 4, new GenerationCount(40));
        try(BufferedWriter writer = Files.newBufferedWriter(
                file, Charset.defaultCharset(), StandardOpenOption.APPEND)){
            writer.newLine();
            for(EvaluatedCandidate<LearningCandidate> candidate : evaluatedCandidates )
                writer.append(candidate.getFitness() + "-" + candidate.getCandidate() + "\n");
            writer.newLine();

        }catch(IOException exception){
            System.out.println("Error writing to file");
        }



    }


}
