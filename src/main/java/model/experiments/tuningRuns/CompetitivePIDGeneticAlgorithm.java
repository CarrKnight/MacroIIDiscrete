/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.experiments.tuningRuns;

import agents.firm.Firm;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.pricing.pid.salesControlWithSmoothedinventoryAndPID;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.scenario.MonopolistScenario;
import model.scenario.TripolistScenario;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.watchmaker.framework.*;
import org.uncommons.watchmaker.framework.factories.AbstractCandidateFactory;
import org.uncommons.watchmaker.framework.operators.AbstractCrossover;
import org.uncommons.watchmaker.framework.operators.DoubleArrayCrossover;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.selection.RouletteWheelSelection;
import org.uncommons.watchmaker.framework.termination.GenerationCount;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

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
 * @version 2013-10-02
 * @see
 */
public class CompetitivePIDGeneticAlgorithm {

    public static long seed1 = 0;

    public static long seed2 = 100;


    public static class CompetitivePIDCandidate
    {

        final private float averageMasterP;

        final private float averageSlaveP;

        final private float averageSlaveI;

        final private float averageSlaveD;




        private double totalError = 0;


        public CompetitivePIDCandidate(float averageMasterP, float averageSlaveP, float averageSlaveI, float averageSlaveD) {
            this.averageMasterP = averageMasterP;
            this.averageSlaveP = averageSlaveP;
            this.averageSlaveI = averageSlaveI;
            this.averageSlaveD = averageSlaveD;
        }


        public void clear(){
            totalError = 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CompetitivePIDCandidate candidate = (CompetitivePIDCandidate) o;

            if (Float.compare(candidate.averageMasterP, averageMasterP) != 0) return false;
            if (Float.compare(candidate.averageSlaveD, averageSlaveD) != 0) return false;
            if (Float.compare(candidate.averageSlaveI, averageSlaveI) != 0) return false;
            return Float.compare(candidate.averageSlaveP, averageSlaveP) == 0;

        }

        @Override
        public int hashCode() {
            int result = (averageMasterP != +0.0f ? Float.floatToIntBits(averageMasterP) : 0);
            result = 31 * result + (averageSlaveP != +0.0f ? Float.floatToIntBits(averageSlaveP) : 0);
            result = 31 * result + (averageSlaveI != +0.0f ? Float.floatToIntBits(averageSlaveI) : 0);
            result = 31 * result + (averageSlaveD != +0.0f ? Float.floatToIntBits(averageSlaveD) : 0);
            return result;
        }

        /**
         * Computes a result, or throws an exception if unable to do so.
         *
         * @return computed result
         * @throws Exception if unable to compute a result
         */
        public double call(long seed){

            for(int competitors=2;competitors<=7;competitors+=2)
            {
                //    System.out.println("FORCED COMPETITIVE FIRMS: " + (competitors+1));
                float averageResultingPrice = 0;
                float averageResultingQuantity = 0;
                for(int i=0; i<2; i++)
                {

                    final MacroII macroII = new MacroII(seed);
                    final TripolistScenario scenario1 = new TripolistScenario(macroII);
                    scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
                    scenario1.setAskPricingStrategy(salesControlWithSmoothedinventoryAndPID.class);
                    scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
                    scenario1.setAdditionalCompetitors(competitors);



                    //assign scenario
                    macroII.setScenario(scenario1);

                    macroII.start();
                    macroII.schedule.step(macroII);
                    for(Firm f : scenario1.getCompetitors()){
                        SalesDepartment department = f.getSalesDepartment(UndifferentiatedGoodType.GENERIC);
                        salesControlWithSmoothedinventoryAndPID strategy =new salesControlWithSmoothedinventoryAndPID(department);
                        strategy.setMasterProportionalGain((float) (averageMasterP + macroII.random.nextGaussian()/100f));
                        strategy.setGainsSlavePID(averageSlaveP + ((float)macroII.random.nextGaussian()) / 100f
                                , averageSlaveI + ((float)macroII.random.nextGaussian()/100f),
                                averageSlaveD + ((float)macroII.random.nextGaussian()/10000f));

                        department.setAskPricingStrategy(strategy);
                    }



                    while(macroII.schedule.getTime()<8000)
                    {
                        macroII.schedule.step(macroII);


                    }

                    //error is the squared differences
                    for(int j=0; j<500; j++)
                    {
                        macroII.schedule.step(macroII);
                        float todayPrice = macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayAveragePrice();
                        if(Float.isNaN(todayPrice))
                            todayPrice = 0;
                        int todayVolume = macroII.getMarket(UndifferentiatedGoodType.GENERIC).getYesterdayVolume();
                        if(Float.isNaN(todayVolume))
                            todayVolume = 0;

                        totalError+=Math.pow(todayVolume - 43, 2) + Math.pow(todayPrice - 58, 2);


                    }

                    //     System.out.println(averagePrice/500 + " - " + totalError + "----" + macroII.seed() );



                }


            }
            return totalError;
        }

        @Override
        public String toString() {
            return "{" +
                    "totalError=" + totalError +
                    ", averageSlaveD=" + averageSlaveD +
                    ", averageSlaveI=" + averageSlaveI +
                    ", averageSlaveP=" + averageSlaveP +
                    ", averageMasterP=" + averageMasterP +
                    '}';
        }

        public float getAverageMasterP() {
            return averageMasterP;
        }

        public float getAverageSlaveP() {
            return averageSlaveP;
        }

        public float getAverageSlaveI() {
            return averageSlaveI;
        }

        public float getAverageSlaveD() {
            return averageSlaveD;
        }


    }


    public static class CompetitivePIDCandidateFactory extends AbstractCandidateFactory<CompetitivePIDCandidate>{
        public static final float minimumMasterP = 1/300f;

        public static final float maximumMasterP = 1f;

        public static final float minimumSlaveP = .1f;

        public static final float maximumSlaveP = 2f;

        public static final float minimumSlaveI = .1f;

        public static final float maximumSlaveI = 2f;

        public static final float minimumSlaveD = 0;

        public static final float maximumSlaveD = .001f;


        /**
         * Randomly create a single candidate solution.
         *
         * @param rng The random number generator to use when creating the random
         *            candidate.
         * @return A randomly-initialised candidate.
         */
        @Override
        public CompetitivePIDCandidate generateRandomCandidate(Random rng)
        {
            float masterP = generateMasterP(rng);
            assert masterP >= minimumMasterP && masterP <= maximumMasterP;
            float slaveP = generateSlaveP(rng);
            assert slaveP >= minimumSlaveP && slaveP <= maximumSlaveP;
            float slaveI = generateSlaveI(rng);
            assert slaveI >= minimumSlaveI && slaveI <= maximumSlaveI;
            float slaveD = generateSlaveD(rng);
            assert slaveD >= minimumSlaveD && slaveI <= maximumSlaveD;

            return new CompetitivePIDCandidate(masterP,slaveP,slaveI,slaveD);

        }

        public static float generateSlaveD(Random rng) {
            return rng.nextFloat() *(maximumSlaveD-minimumSlaveD) + minimumSlaveD;
        }

        public static float generateSlaveI(Random rng) {
            return rng.nextFloat() *(maximumSlaveI-minimumSlaveI) + minimumSlaveI;
        }

        public static float generateSlaveP(Random rng) {
            return rng.nextFloat() *(maximumSlaveP-minimumSlaveP) + minimumSlaveP;
        }

        public static float generateMasterP(Random rng) {
            return rng.nextFloat() *(maximumMasterP-minimumMasterP) + minimumMasterP;
        }
    }


    public static class CompetitivePIDCrossover extends AbstractCrossover<CompetitivePIDCandidate>
    {

        public CompetitivePIDCrossover() {
            super(1);
        }

        private static final DoubleArrayCrossover CROSSOVER = new DoubleArrayCrossover();

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
        protected List<CompetitivePIDCandidate> mate(CompetitivePIDCandidate parent1, CompetitivePIDCandidate parent2, int numberOfCrossoverPoints, Random rng) {
            double[] parentFirst = new double[4];
            parentFirst[0] = parent1.averageMasterP;
            parentFirst[1] = parent1.averageSlaveP;
            parentFirst[2] = parent1.averageSlaveI;
            parentFirst[3] = parent1.averageSlaveD;
            double[] parentSecond = new double[4];
            parentSecond[0] = parent2.averageMasterP;
            parentSecond[1] = parent2.averageSlaveP;
            parentSecond[2] = parent2.averageSlaveI;
            parentSecond[3] = parent2.averageSlaveD;
            List<double[]> parents = new LinkedList<>(); parents.add(parentFirst); parents.add(parentSecond);

            List<double[]> offsprings = CROSSOVER.apply(parents,rng);

            CompetitivePIDCandidate offspring1 = new CompetitivePIDCandidate(
                    (float)offsprings.get(0)[0],(float)offsprings.get(0)[1],
                    (float)offsprings.get(0)[2],(float)offsprings.get(0)[3]);
            CompetitivePIDCandidate offspring2 = new CompetitivePIDCandidate(
                    (float)offsprings.get(1)[0],(float)offsprings.get(1)[1],
                    (float)offsprings.get(1)[2],(float)offsprings.get(1)[3]);
            List<CompetitivePIDCandidate> toReturn = new LinkedList<>();
            toReturn.add(offspring1);
            toReturn.add(offspring2);

            return toReturn;



        }
    }


    public static class CompetitivePIDMutator implements EvolutionaryOperator<CompetitivePIDCandidate>{

        float probability = .05f;




        /**
         * Every parameter has the same fixed chance of mutating (mutation is a normal shock)
         */
        @Override
        public List<CompetitivePIDCandidate> apply(List<CompetitivePIDCandidate> selectedCandidates, Random rng) {
            List<CompetitivePIDCandidate> newCandidates = new LinkedList<>();
            for(CompetitivePIDCandidate candidate : selectedCandidates)
            {
                boolean mutatedMasterP = rng.nextFloat() < probability; float masterP;
                boolean mutatedSlaveP = rng.nextFloat() < probability; float slaveP;
                boolean mutatedSlaveI = rng.nextFloat() < probability; float slaveI;
                boolean mutatedSlaveD = rng.nextFloat() < probability; float slaveD;

                masterP = candidate.getAverageMasterP();
                if(mutatedMasterP)
                    masterP += (float)(rng.nextGaussian() / 10f);
                slaveP = candidate.getAverageSlaveP();
                if(mutatedSlaveP)
                    slaveP += (float)(rng.nextGaussian() / 10f);
                slaveI = candidate.getAverageSlaveI();
                if(mutatedSlaveI)
                    slaveI += (float)(rng.nextGaussian() / 10f);
                slaveD = candidate.getAverageSlaveD();
                if(mutatedSlaveD)
                    slaveD += (float)(rng.nextGaussian() / 200f);

                if(mutatedMasterP || mutatedSlaveI || mutatedSlaveD || mutatedSlaveP)
                    newCandidates.add(new CompetitivePIDCandidate(masterP,slaveP,slaveI,slaveD));
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



    public static class CompetitiveFitness implements FitnessEvaluator<CompetitivePIDCandidate>{


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
        public double getFitness(CompetitivePIDCandidate candidate, List<? extends CompetitivePIDCandidate> population) {
            candidate.clear();
            double error = candidate.call(CompetitivePIDGeneticAlgorithm.seed1);
            error += candidate.call(CompetitivePIDGeneticAlgorithm.seed2);
            assert  error == candidate.totalError;

            return error;

        }

        /**
         * the lower the fitness, the better
         */
        @Override
        public boolean isNatural() {
            return false;
        }
    }


    public static void main(String[] args) throws IOException {

        List<EvolutionaryOperator<CompetitivePIDCandidate>> operators = new LinkedList<>();
        operators.add(new CompetitivePIDCrossover());
        operators.add(new CompetitivePIDMutator());

        EvolutionaryOperator<CompetitivePIDCandidate> pipeline = new EvolutionPipeline<>(operators);

        CachingFitnessEvaluator<CompetitivePIDCandidate> evaluator = new CachingFitnessEvaluator<>(new CompetitiveFitness());
        CompetitivePIDCandidateFactory factory = new CompetitivePIDCandidateFactory();

        RouletteWheelSelection selector = new RouletteWheelSelection();
        final MersenneTwisterRNG rng = new MersenneTwisterRNG();
        GenerationalEvolutionEngine<CompetitivePIDCandidate> solutions = new GenerationalEvolutionEngine<>(
                factory,pipeline, evaluator, selector, rng
        );

        //update the seeds
        CompetitivePIDGeneticAlgorithm.seed1 = rng.nextLong();
        CompetitivePIDGeneticAlgorithm.seed2 = rng.nextLong();

        Files.deleteIfExists(Paths.get("geneticCompetition")); //delete the file, if needed
        final Path file = Files.createFile(Paths.get("geneticCompetition")); //recreate.


        solutions.setSingleThreaded(false);

        solutions.addEvolutionObserver(new EvolutionObserver<CompetitivePIDCandidate>() {
            @Override
            public void populationUpdate(PopulationData<? extends CompetitivePIDCandidate> data) {
        //        System.out.println("Generation " +  data.getGenerationNumber() + ": " + data.getBestCandidate() + "\n Mean fitness:" +
         //               data.getMeanFitness());

                try(BufferedWriter writer = Files.newBufferedWriter(
                        file, Charset.defaultCharset(), StandardOpenOption.APPEND))
                {
                    writer.append("Generation " +  data.getGenerationNumber() + ": " + data.getBestCandidate() + "\t Mean fitness:" +
                            data.getMeanFitness());
                    writer.newLine();
                    writer.flush();

                }catch(IOException exception){
                 //   System.out.println("Error writing to file");
                }



            }
        });

        List<EvaluatedCandidate<CompetitivePIDCandidate>> evaluatedCandidates = solutions.evolvePopulation(50, 5, new GenerationCount(20));
        try(BufferedWriter writer = Files.newBufferedWriter(
                file, Charset.defaultCharset(), StandardOpenOption.APPEND)){
            writer.newLine();
            writer.append(evaluatedCandidates.toString());
            writer.newLine();

        }catch(IOException exception){
            System.err.println("Error writing to file");
        }

    }
}
