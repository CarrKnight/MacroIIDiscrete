/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.experiments.tuningRuns;

import ec.util.MersenneTwisterFast;
import model.utilities.stats.regression.RecursiveLeastMeansFilter;
import model.utilities.stats.regression.RecursiveLinearRegression;
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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * <h4>Description</h4>
 * <p>
 * <p>
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-07-16
 * @see
 */
public class SelectiveForgettingGeneticAlgorithmTune {

    static long commonSeed = 0;

    private final static FitnessEvaluator<double[]> EVALUATOR = new FitnessEvaluator<double[]>() {
        @Override
        public double getFitness(double[] candidate, List<? extends double[]> population) {

            double errorsSquaredOverTheLast100Steps = 0;

            //run 10 regressions.

            //for each, 500 observations one way, 1000 observations of equilibrium and then 500 observations another way
            //test tracking ability
            for(int i=0; i<50; i++) {
                MersenneTwisterFast random = new MersenneTwisterFast(commonSeed+i);

                RecursiveLinearRegression regression = new RecursiveLeastMeansFilter(2,candidate[0]);
                int randomIntercept = random.nextInt(500);
                int randomSlope = random.nextInt(6) - 3;
                double xObservation = 0;
                double yObservation;
                int firstPhase = 250;
                for (int step = 0; step < firstPhase+100; step++) {
                    xObservation = random.nextInt(100);
                    yObservation = randomIntercept + randomSlope * xObservation + random.nextGaussian() * 3;
                    regression.addObservation(1, yObservation, 1, xObservation);

                    if(step>firstPhase)
                        errorsSquaredOverTheLast100Steps += Math.pow(regression.getBeta()[1]-randomSlope,2);

                }

                //stuck at equilibrium
                for (int step = 0; step < 10000; step++) {
                    yObservation = randomIntercept + randomSlope * xObservation + random.nextGaussian() * 3;
                    regression.addObservation(1, yObservation, 1, xObservation);

                    if(step>9900)
                        errorsSquaredOverTheLast100Steps += Math.pow(regression.getBeta()[1]-randomSlope,2);
                }


                //new slope!
                randomIntercept = random.nextInt(500);
                randomSlope = random.nextInt(6) - 3;
                int secondPhase =250;

                for (int step = 0; step < secondPhase+100; step++) {
                    xObservation = random.nextInt(100);
                    yObservation = randomIntercept + randomSlope * xObservation + random.nextGaussian() * 3;
                    regression.addObservation(1, yObservation, 1, xObservation);
                    if(step>secondPhase)
                        errorsSquaredOverTheLast100Steps += Math.pow(regression.getBeta()[1]-randomSlope,2);
                }

            }

            return errorsSquaredOverTheLast100Steps;

        }

        @Override
        public boolean isNatural() {
            return false;
        }
    };



    public static void main(String[] args) throws IOException {





        CandidateFactory<double[]> factory = new AbstractCandidateFactory<double[]>() {
            @Override
            public double[] generateRandomCandidate(Random rng) {
                return new double[]{rng.nextDouble()*2,rng.nextDouble()*100+1};
            }
        };


        EvolutionaryOperator<double[]> crossOver = new AbstractCrossover<double[]>(1) {
            @Override
            protected List<double[]> mate(double[] parent1, double[] parent2, int numberOfCrossoverPoints, Random rng) {
                final double alpha = rng.nextDouble();
                double[] offspring1 = new double[]{ alpha*parent1[0] + (1-alpha) *parent2[0],parent1[1]};
                double[] offspring2 = new double[]{ parent2[0],alpha*parent1[1] + (1-alpha) *parent2[1]};
                LinkedList<double[]> offsprings = new LinkedList<>();
                offsprings.add(offspring1);
                offsprings.add(offspring2);

                return offsprings;

            }
        };
        EvolutionaryOperator<double[]> mutation = (selectedCandidates, rng) -> {

            for(double[] candidate : selectedCandidates)
            {
                if(rng.nextDouble()>.85)
                    candidate[0] = Math.max(Math.min(candidate[0] + rng.nextGaussian()/100,3),0);
                if(rng.nextDouble()>.85)
                    candidate[1] = Math.max(Math.min(candidate[1] + rng.nextGaussian(),1000),1);
            }


            return selectedCandidates;
        };


        final LinkedList<EvolutionaryOperator<double[]>> evolutionaryOperators = new LinkedList<>();
        evolutionaryOperators.add(crossOver);
        evolutionaryOperators.add(mutation);
        EvolutionPipeline<double[]> pipeline = new EvolutionPipeline<>( evolutionaryOperators);

        SelectionStrategy selection = new RouletteWheelSelection();


        final MersenneTwisterRNG rng = new MersenneTwisterRNG();
        EvolutionEngine<double[]> engine = new GenerationalEvolutionEngine<>(factory,pipeline,
                EVALUATOR,selection, rng);

        Files.deleteIfExists(Paths.get("runs","gaSelectiveForgetting2")); //delete the file, if needed
        final Path file = Files.createFile(Paths.get("runs","gaSelectiveForgetting2")); //recreate.


        engine.addEvolutionObserver(data -> {
            System.out.println("Generation " +  data.getGenerationNumber() + ": " + Arrays.toString(data.getBestCandidate()) + "\n Mean fitness:" +
                    data.getMeanFitness()+ ", best fitness: " + data.getBestCandidateFitness());

            try(BufferedWriter writer = Files.newBufferedWriter(
                    file, Charset.defaultCharset(), StandardOpenOption.APPEND))
            {
                writer.append("Generation " +  data.getGenerationNumber() + ": " + Arrays.toString(data.getBestCandidate()) + "\t Mean fitness:" +
                        data.getMeanFitness() + ", best fitness: " + data.getBestCandidateFitness());
                writer.newLine();
                writer.flush();



            }catch(IOException exception){
                System.out.println("Error writing to file");
            }

            SelectiveForgettingGeneticAlgorithmTune.commonSeed = rng.nextLong();
        });

        List<EvaluatedCandidate<double[]>> evaluatedCandidates = engine.evolvePopulation(150, 5, new GenerationCount(200));
        try(BufferedWriter writer = Files.newBufferedWriter(
                file, Charset.defaultCharset(), StandardOpenOption.APPEND)){
            writer.newLine();
            for(EvaluatedCandidate<double[]> candidate : evaluatedCandidates) {
                writer.append(Arrays.toString(candidate.getCandidate()) + " ---> " + candidate.getFitness());
                writer.newLine();
            }

        }catch(IOException exception){
            System.out.println("Error writing to file");
        }
    }

}
