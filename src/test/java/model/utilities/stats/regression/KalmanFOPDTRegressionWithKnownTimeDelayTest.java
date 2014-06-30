/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import ec.util.MersenneTwisterFast;
import model.utilities.pid.PIDController;
import model.utilities.stats.processes.FirstOrderPlusDeadTime;
import org.junit.Assert;
import org.junit.Test;

import java.util.function.Supplier;

/**
 * Create a random PID playing with a random FOPDT, see if the regression finds it
 */
public class KalmanFOPDTRegressionWithKnownTimeDelayTest
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
    public void delayNoInterceptNoNoiseTest() throws Exception
    {
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        for(int experiments =0; experiments < 10; experiments++)
        {

            float proportionalParameter = random.nextFloat()*maximumP-minimumP + minimumP;
            float integrativeParameter = random.nextFloat()*maximumI-minimumI + minimumI;



            float gain = random.nextFloat()*maximumGain-minimumGain + minimumGain;
            float timeConstant = random.nextFloat()*maximumTimeConstant-minimumTimeConstant + minimumTimeConstant;


            double[] betas = runLearningExperimentWithKnownDeadTime(random, proportionalParameter, integrativeParameter, 0, gain, timeConstant, random.nextInt(maximumDelay), null);

            Assert.assertEquals(gain, betas[1], .001);
            Assert.assertEquals(timeConstant,betas[2],.001);
            System.out.println("===================================================================== ");


        }



    }

    @Test
    public void delayNoInterceptWithNoiseTest() throws Exception
    {
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        int successes = 0;
        for(int experiments =0; experiments < 500; experiments++)
        {

            float proportionalParameter = random.nextFloat()*maximumP-minimumP + minimumP;
            float integrativeParameter = random.nextFloat()*maximumI-minimumI + minimumI;



            float gain = random.nextFloat()*maximumGain-minimumGain + minimumGain;
            float timeConstant = random.nextFloat()*maximumTimeConstant-minimumTimeConstant + minimumTimeConstant;


            double[] betas = runLearningExperimentWithKnownDeadTime(random, proportionalParameter, integrativeParameter, 0, gain, timeConstant, random.nextInt(maximumDelay),
                    () -> random.nextGaussian()*.5);

            if ( Math.abs(gain-betas[1])<.1 && Math.abs(timeConstant-betas[2])<.1 )
                successes++;
            System.out.println("===================================================================== ");


        }

        System.out.println(successes);
        Assert.assertTrue(successes>450);


    }

    private double[] runLearningExperimentWithKnownDeadTime(MersenneTwisterFast random, float proportionalParameter,
                                                                             float integrativeParameter, int intercept, float gain, float timeConstant, int deadTime,
                                                                             Supplier<Double> noiseMaker) {
        PIDController controller = new PIDController(proportionalParameter,integrativeParameter,0,random);
        int target = 1;
        FirstOrderPlusDeadTime process = new FirstOrderPlusDeadTime(intercept,gain,timeConstant, deadTime);
        if(noiseMaker != null)
            process.setRandomNoise(noiseMaker);

        //create the regression too
        KalmanFOPDTRegressionWithKnownTimeDelay regression = new KalmanFOPDTRegressionWithKnownTimeDelay(new KalmanRecursiveRegression(3),deadTime,0); //three dimension: intercept, input and output derivative

        //output starts at intercept
        float output = 0;
        //delayed input, useful for learning

        for(int step =0; step < 1000; step++)
        {


            //PID step
            controller.adjustOnce(target,output,true);

            //process reacts
            float input = controller.getCurrentMV();
            assert !Float.isNaN(input);
            assert !Float.isInfinite(input);
            output = (float) process.newStep(input);


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
        System.out.println("actual gain: " + gain + ", actual timeConstant: " + timeConstant);
        System.out.println("learned gain: " + regression.getBeta()[1] + ", learned timeConstant: " + regression.getBeta()[2]);
        return regression.getBeta();
    }



}