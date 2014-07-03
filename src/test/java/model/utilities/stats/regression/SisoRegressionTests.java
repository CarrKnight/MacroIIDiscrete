/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import ec.util.MersenneTwisterFast;
import model.utilities.pid.PIDController;
import model.utilities.stats.processes.DynamicProcess;
import model.utilities.stats.processes.FirstOrderIntegratingPlusDeadTime;
import model.utilities.stats.processes.FirstOrderPlusDeadTime;
import org.junit.Assert;
import org.junit.Test;

import java.util.function.Supplier;

/**
 * Create a random PID playing with a random FOPDT, see if the regression finds it
 */
public class SisoRegressionTests
{

    private final float minimumP =.1f;

    private final float maximumP =.3f;


    private final float minimumI =.1f;

    private final float maximumI =.3f;

    private final float minimumGain =.1f;

    private final float maximumGain =2f;

    private final float minimumTimeConstant =.1f;

    private final float maximumTimeConstant =.2f;

    private final int minimumDelay =0;

    private final int maximumDelay =10;

    @Test
    public void knownDelayNoNoiseFOPDTTest() throws Exception
    {
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        for(int experiments =0; experiments < 10; experiments++)
        {

            float proportionalParameter = random.nextFloat()*maximumP-minimumP + minimumP;
            float integrativeParameter = random.nextFloat()*maximumI-minimumI + minimumI;



            float gain = random.nextFloat()*maximumGain-minimumGain + minimumGain;
            float timeConstant = random.nextFloat()*maximumTimeConstant-minimumTimeConstant + minimumTimeConstant;


            final int delay = random.nextInt(maximumDelay);
            final SISORegression result = runLearningExperiment(random, new FirstOrderPlusDeadTime(0, gain, timeConstant, delay), proportionalParameter, integrativeParameter,
                    null,()->new KalmanFOPDTRegressionWithKnownTimeDelay(delay));


            System.out.println("actual gain: " + gain + ", actual timeConstant: " + timeConstant + ", and delay: " + delay);
            System.out.println("learned gain: " +result.getGain() + ", learned timeConstant: " + result.getTimeConstant() + ", learned delay: " + result.getDelay());

            Assert.assertEquals(gain, result.getGain(), .001);
            Assert.assertEquals(timeConstant,result.getTimeConstant(),.001);
            System.out.println("===================================================================== ");


        }



    }

    @Test
    public void unknownDelayNoNoiseFOPDT() throws Exception
    {
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        for(int experiments =0; experiments < 10; experiments++)
        {

            float proportionalParameter = random.nextFloat()*maximumP-minimumP + minimumP;
            float integrativeParameter = random.nextFloat()*maximumI-minimumI + minimumI;



            float gain = random.nextFloat()*maximumGain-minimumGain + minimumGain;
            float timeConstant = random.nextFloat()*maximumTimeConstant-minimumTimeConstant + minimumTimeConstant;


            final int deadTime = random.nextInt(maximumDelay);
            final SISORegression result =
                    runLearningExperiment(random, new FirstOrderPlusDeadTime(0, gain, timeConstant, deadTime), proportionalParameter, integrativeParameter,
                            null, () -> new SISOGuessingRegression(0,1,2,3,4,5,6,7,8,9,10)
                    );
            System.out.println("actual gain: " + gain + ", actual timeConstant: " + timeConstant + ", and delay: " + deadTime);
            System.out.println("learned gain: " +result.getGain() + ", learned timeConstant: " + result.getTimeConstant() + ", learned delay: " + result.getDelay());


            Assert.assertEquals(deadTime,result.getDelay());
            Assert.assertEquals(gain, result.getGain(), .001);
            Assert.assertEquals(timeConstant,result.getTimeConstant(),.001);
            System.out.println("===================================================================== ");


        }



    }


    @Test
    public void unknownDelayNoisyFOPDT() throws Exception
    {
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        int successes = 0;
        for(int experiments =0; experiments < 500; experiments++)
        {

            float proportionalParameter = random.nextFloat()*maximumP-minimumP + minimumP;
            float integrativeParameter = random.nextFloat()*maximumI-minimumI + minimumI;



            float gain = random.nextFloat()*maximumGain-minimumGain + minimumGain;
            float timeConstant = random.nextFloat()*maximumTimeConstant-minimumTimeConstant + minimumTimeConstant;


            final int deadTime = random.nextInt(maximumDelay);
            final SISORegression result = runLearningExperiment(random, new FirstOrderPlusDeadTime(0, gain, timeConstant, deadTime), proportionalParameter, integrativeParameter,
                    () -> random.nextGaussian() * .5, () -> new SISOGuessingRegression(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

            System.out.println("actual gain: " + gain + ", actual timeConstant: " + timeConstant + ", and delay: " + deadTime);
            System.out.println("learned gain: " +result.getGain() + ", learned timeConstant: " + result.getTimeConstant() + ", learned delay: " + result.getDelay());

            if ( Math.abs(gain-result.getGain())<.1 && Math.abs(timeConstant-result.getTimeConstant())<.1 ) {
                successes++;
                System.out.println("success");
            }
            System.out.println("===================================================================== ");


        }

        System.out.println(successes);
        Assert.assertTrue(String.valueOf(successes),successes>450);


    }

    @Test
    public void knownDelayNoisyFOPDT() throws Exception
    {
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        int successes = 0;
        for(int experiments =0; experiments < 500; experiments++)
        {

            float proportionalParameter = random.nextFloat()*maximumP-minimumP + minimumP;
            float integrativeParameter = random.nextFloat()*maximumI-minimumI + minimumI;



            float gain = random.nextFloat()*maximumGain-minimumGain + minimumGain;
            float timeConstant = random.nextFloat()*maximumTimeConstant-minimumTimeConstant + minimumTimeConstant;


            final int delay = random.nextInt(maximumDelay);
            final SISORegression result = runLearningExperiment(random, new FirstOrderPlusDeadTime(0, gain, timeConstant, delay), proportionalParameter, integrativeParameter,
                    () -> random.nextGaussian() * .5,()->new KalmanFOPDTRegressionWithKnownTimeDelay(delay));

            System.out.println("actual gain: " + gain + ", actual timeConstant: " + timeConstant + ", and delay: " + delay);
            System.out.println("learned gain: " +result.getGain() + ", learned timeConstant: " + result.getTimeConstant() + ", learned delay: " + result.getDelay());


            if ( Math.abs(gain-result.getGain())<.1 && Math.abs(timeConstant-result.getTimeConstant())<.1 ) {
                successes++;
                System.out.println("success");
            }
            System.out.println("===================================================================== ");


        }

        System.out.println(successes);
        Assert.assertTrue(successes>450);


    }


    @Test
    public void knownDelayNoNoiseFOIPDTTest() throws Exception
    {
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        for(int experiments =0; experiments < 10; experiments++)
        {

            float proportionalParameter = random.nextFloat()*maximumP-minimumP + minimumP;
            float integrativeParameter = random.nextFloat()*maximumI-minimumI + minimumI;



            float gain = random.nextFloat()*maximumGain-minimumGain + minimumGain;
            float timeConstant = random.nextFloat()*maximumTimeConstant-minimumTimeConstant + minimumTimeConstant;


            final int delay = random.nextInt(maximumDelay);
            final SISORegression result = runLearningExperiment(random, new FirstOrderIntegratingPlusDeadTime(0, gain, timeConstant, delay), proportionalParameter, integrativeParameter,
                    null,()->new KalmanFOPIDTRegressionWithKnownTimeDelay(delay));


            System.out.println("actual gain: " + gain + ", actual timeConstant: " + timeConstant + ", and delay: " + delay);
            System.out.println("learned gain: " +result.getGain() + ", learned timeConstant: " + result.getTimeConstant() + ", learned delay: " + result.getDelay());

            Assert.assertEquals(gain, result.getGain(), .001);
            Assert.assertEquals(timeConstant,result.getTimeConstant(),.001);
            System.out.println("===================================================================== ");


        }



    }

    @Test
    public void knownDelayNoisyFOIPDT() throws Exception
    {
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        int successes = 0;
        for(int experiments =0; experiments < 500; experiments++)
        {

            float proportionalParameter = random.nextFloat()*maximumP-minimumP + minimumP;
            float integrativeParameter = random.nextFloat()*maximumI-minimumI + minimumI;



            float gain = random.nextFloat()*maximumGain-minimumGain + minimumGain;
            float timeConstant = random.nextFloat()*maximumTimeConstant-minimumTimeConstant + minimumTimeConstant;


            final int delay = random.nextInt(maximumDelay);
            final SISORegression result = runLearningExperiment(random, new FirstOrderIntegratingPlusDeadTime(0, gain, timeConstant, delay),
                    proportionalParameter, integrativeParameter,
                    () -> random.nextGaussian() * .5,()->new KalmanFOPIDTRegressionWithKnownTimeDelay(delay));

            System.out.println("actual gain: " + gain + ", actual timeConstant: " + timeConstant + ", and delay: " + delay);
            System.out.println("learned gain: " +result.getGain() + ", learned timeConstant: " + result.getTimeConstant() + ", learned delay: " + result.getDelay());


            if ( Math.abs(gain-result.getGain())<.1 && Math.abs(timeConstant-result.getTimeConstant())<.1 ) {
                successes++;
                System.out.println("success");
            }
            System.out.println("===================================================================== ");


        }

        System.out.println(successes);
        Assert.assertTrue(successes>450);


    }

    @Test
    public void unknownDelayNoNoiseFOIPDTTest() throws Exception
    {
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        for(int experiments =0; experiments < 10; experiments++)
        {

            float proportionalParameter = random.nextFloat()*maximumP-minimumP + minimumP;
            float integrativeParameter = random.nextFloat()*maximumI-minimumI + minimumI;



            float gain = random.nextFloat()*maximumGain-minimumGain + minimumGain;
            float timeConstant = random.nextFloat()*maximumTimeConstant-minimumTimeConstant + minimumTimeConstant;


            final int delay = random.nextInt(maximumDelay);
            final SISORegression result = runLearningExperiment(random, new FirstOrderIntegratingPlusDeadTime(0, gain, timeConstant, delay), proportionalParameter, integrativeParameter,
                    null, () -> new SISOGuessingRegression(KalmanFOPIDTRegressionWithKnownTimeDelay::new,0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)) ;


            System.out.println("actual gain: " + gain + ", actual timeConstant: " + timeConstant + ", and delay: " + delay);
            System.out.println("learned gain: " +result.getGain() + ", learned timeConstant: " + result.getTimeConstant() + ", learned delay: " + result.getDelay());

            Assert.assertEquals(gain, result.getGain(), .001);
            Assert.assertEquals(timeConstant,result.getTimeConstant(),.001);
            System.out.println("===================================================================== ");


        }



    }

    @Test
    public void unknownDelayNoisyFOIPDT() throws Exception
    {
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        int successes = 0;
        for(int experiments =0; experiments < 500; experiments++)
        {

            float proportionalParameter = random.nextFloat()*maximumP-minimumP + minimumP;
            float integrativeParameter = random.nextFloat()*maximumI-minimumI + minimumI;



            float gain = random.nextFloat()*maximumGain-minimumGain + minimumGain;
            float timeConstant = random.nextFloat()*maximumTimeConstant-minimumTimeConstant + minimumTimeConstant;


            final int delay = random.nextInt(maximumDelay);
            final SISORegression result = runLearningExperiment(random, new FirstOrderIntegratingPlusDeadTime(0, gain, timeConstant, delay), proportionalParameter, integrativeParameter,
                    () -> random.nextGaussian() * .5, () -> new SISOGuessingRegression(KalmanFOPIDTRegressionWithKnownTimeDelay::new,0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)) ;



            System.out.println("actual gain: " + gain + ", actual timeConstant: " + timeConstant + ", and delay: " + delay);
            System.out.println("learned gain: " +result.getGain() + ", learned timeConstant: " + result.getTimeConstant() + ", learned delay: " + result.getDelay());


            if ( Math.abs(gain-result.getGain())<.1 && Math.abs(timeConstant-result.getTimeConstant())<.1 ) {
                successes++;
                System.out.println("success");
            }
            System.out.println("===================================================================== ");


        }

        System.out.println(successes);
        Assert.assertTrue(successes>450);


    }


    private SISORegression runLearningExperiment(MersenneTwisterFast random, DynamicProcess dynamicProcess, float proportionalParameter,
                                                 float integrativeParameter,
                                                 Supplier<Double> noiseMaker, Supplier<SISORegression> regressionSupplier) {
        PIDController controller = new PIDController(proportionalParameter,integrativeParameter,0,random);
        int target = 1;
        if(noiseMaker != null)
            dynamicProcess.setRandomNoise(noiseMaker);

        //create the regression too
        SISORegression regression = regressionSupplier.get(); //three dimension: intercept, input and output derivative

        //output starts at intercept
        float output = 0;
        //delayed input, useful for learning

        for(int step =0; step < 5000; step++)
        {


            //PID step
            controller.adjustOnce(target,output,true);

            //process reacts
            float input = controller.getCurrentMV();
            assert !Float.isNaN(input);
            assert !Float.isInfinite(input);
            output = (float) dynamicProcess.newStep(input);


            //regression learns
            regression.addObservation(output,input);





            //shock target with 10%
            if(random.nextBoolean(.10)) {
                if (random.nextBoolean())
                    target++;
                else
                    target--;
            }



        }

        return regression;
    }







}