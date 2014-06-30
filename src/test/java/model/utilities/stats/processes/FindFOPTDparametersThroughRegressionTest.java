/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.processes;

public class FindFOPTDparametersThroughRegressionTest {


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

       /*
    @Test
    public void noDelayNoInterceptNoNoiseTest() throws Exception
    {
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        for(int experiments =0; experiments < 10; experiments++)
        {

            float proportionalParameter = random.nextFloat()*maximumP-minimumP + minimumP;
            float integrativeParameter = random.nextFloat()*maximumI-minimumI + minimumI;



            float gain = random.nextFloat()*maximumGain-minimumGain + minimumGain;
            float timeConstant = random.nextFloat()*maximumTimeConstant-minimumTimeConstant + minimumTimeConstant;


            RecursiveLinearRegression regression = runLearningExperimentWithKnownDeadTime(random, proportionalParameter, integrativeParameter, 0, gain, timeConstant, 0, null);

            Assert.assertEquals(gain,regression.getBeta()[1],.001);
            Assert.assertEquals(timeConstant, regression.getBeta()[2],.001);
            System.out.println("===================================================================== ");


        }



    }


    @Test
    public void knownDelayNoInterceptNoNoiseTest() throws Exception
    {
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        for(int experiments =0; experiments < 10; experiments++)
        {

            float proportionalParameter = random.nextFloat()*maximumP-minimumP + minimumP;
            float integrativeParameter = random.nextFloat()*maximumI-minimumI + minimumI;



            float gain = random.nextFloat()*maximumGain-minimumGain + minimumGain;
            float timeConstant = random.nextFloat()*maximumTimeConstant-minimumTimeConstant + minimumTimeConstant;

            int delay = random.nextInt(maximumDelay-minimumDelay) + minimumDelay;

            RecursiveLinearRegression regression = runLearningExperimentWithKnownDeadTime(random, proportionalParameter, integrativeParameter, 0, gain, timeConstant, delay, null);

            Assert.assertEquals(gain,regression.getBeta()[1],.001);
            Assert.assertEquals(timeConstant, regression.getBeta()[2],.001);
            System.out.println("===================================================================== ");


        }



    }

    @Test
    public void knownDelayNoInterceptWithNoiseTest() throws Exception
    {
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        int successes = 0;
        for(int experiments =0; experiments < 500; experiments++)
        {

            float proportionalParameter = random.nextFloat()*maximumP-minimumP + minimumP;
            float integrativeParameter = random.nextFloat()*maximumI-minimumI + minimumI;



            float gain = random.nextFloat()*maximumGain-minimumGain + minimumGain;
            float timeConstant = random.nextFloat()*maximumTimeConstant-minimumTimeConstant + minimumTimeConstant;

            int delay = random.nextInt(maximumDelay-minimumDelay) + minimumDelay;

            RecursiveLinearRegression regression = runLearningExperimentWithKnownDeadTime(random, proportionalParameter, integrativeParameter, 0, gain, timeConstant, delay,
                    ()->random.nextGaussian()*.5f);

            if ( Math.abs(gain-regression.getBeta()[1])<.1 && Math.abs(timeConstant-regression.getBeta()[2])<.1 )
                successes++;
            System.out.println("===================================================================== ");


        }
        System.out.println(successes);
        Assert.assertTrue(successes>400);



    }

    @Test
    public void unknownDelayNoInterceptNoNoiseTest() throws Exception
    {
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        for(int experiments =0; experiments < 10; experiments++)
        {

            float proportionalParameter = random.nextFloat()*maximumP-minimumP + minimumP;
            float integrativeParameter = random.nextFloat()*maximumI-minimumI + minimumI;



            float gain = random.nextFloat()*maximumGain-minimumGain + minimumGain;
            float timeConstant = random.nextFloat()*maximumTimeConstant-minimumTimeConstant + minimumTimeConstant;

            int delay = random.nextInt(maximumDelay-minimumDelay) + minimumDelay;

            RecursiveLinearRegression regression = runLearningExperimentWithUnknownDeadTime(random, proportionalParameter, integrativeParameter, 0, gain, timeConstant, delay, null);

            Assert.assertEquals(gain,regression.getBeta()[1],.001);
            Assert.assertEquals(timeConstant, regression.getBeta()[2],.001);
            System.out.println("===================================================================== ");


        }



    }

    @Test
    public void unknownDelayNoInterceptGaussianNoiseTest() throws Exception
    {
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        for(int experiments =0; experiments < 10; experiments++)
        {

            float proportionalParameter = random.nextFloat()*maximumP-minimumP + minimumP;
            float integrativeParameter = random.nextFloat()*maximumI-minimumI + minimumI;



            float gain = random.nextFloat()*maximumGain-minimumGain + minimumGain;
            float timeConstant = random.nextFloat()*maximumTimeConstant-minimumTimeConstant + minimumTimeConstant;

            int delay = random.nextInt(maximumDelay-minimumDelay) + minimumDelay;

            RecursiveLinearRegression regression = runLearningExperimentWithUnknownDeadTime(random, proportionalParameter, integrativeParameter, 0, gain, timeConstant, delay,
                    ()->random.nextGaussian()*.5f);

            //this is harder, get it right to the decimal
            Assert.assertEquals(gain,regression.getBeta()[1],.1);
            Assert.assertEquals(timeConstant, regression.getBeta()[2],.1);
            System.out.println("===================================================================== ");


        }



    }


    private RecursiveLinearRegression runLearningExperimentWithUnknownDeadTime(MersenneTwisterFast random, float proportionalParameter,
                                                                             float integrativeParameter, int intercept, float gain, float timeConstant, int deadTime,
                                                                             Supplier<Double> noiseMaker) {
        PIDController controller = new PIDController(proportionalParameter,integrativeParameter,0,random);
        int target = 1;
        FirstOrderPlusDeadTime process = new FirstOrderPlusDeadTime(intercept,gain,timeConstant, deadTime);
        if(noiseMaker != null)
            process.setRandomNoise(noiseMaker);

        //create the regressions with different delays too
        RecursiveLinearRegression regressions[] = new RecursiveLinearRegression[10];
        DelayBin<Float>[] delayedInputs = new DelayBin[10];
        double errors[] = new double[10];
        for(int i=0; i<10; i++)
        {
            regressions[i] =  new KalmanRecursiveRegression(3);
            delayedInputs[i] = new DelayBin<>(i,0f);
            errors[i] = 0;

        }

        //output starts at intercept
        float output = 0;
        //delayed input, useful for learning
        for(int step =0; step < 5000; step++)
        {


            //PID step
            controller.adjustOnce(target,output,true);
            float input = controller.getCurrentMV();
            assert !Float.isNaN(input);
            assert !Float.isInfinite(input);

            //process reacts

            float currentDerivative = (float) process.getCurrentDerivative();
            output = (float) process.newStep(input);


            //regress 10 times
            for(int regressionIndex=0; regressionIndex<10; regressionIndex++)
            {
                input = controller.getCurrentMV();
                input= delayedInputs[regressionIndex].addAndRetrieve(input);

                //predict
                final double[] betas = regressions[regressionIndex].getBeta();
                float predictedValue = (float) (betas[0] + betas[1] * input - betas[2] * currentDerivative);
                errors[regressionIndex] += Math.pow(output-predictedValue,2);

                //regression learns

                regressions[regressionIndex].addObservation(1, output, 1, input,- currentDerivative);
            }


            //shock target with 10%
            if(random.nextBoolean(.10)) {
                if (random.nextBoolean())
                    target++;
                else
                    target--;
            }



        }
        System.out.println("actual gain: " + gain + ", actual timeConstant: " + timeConstant + ", actual dead time: " + deadTime);
        System.out.println("errors: " + Arrays.toString(errors));
        int bestRegression =0;
        for(int i=1; i< errors.length; i++){
               if(errors[i] < errors[bestRegression])
                   bestRegression = i;
        }
        System.out.println("learned gain: " + regressions[bestRegression].getBeta()[1] + ", learned timeConstant: " + regressions[bestRegression].getBeta()[2] + ", learned dead time: " + bestRegression);
        return regressions[bestRegression];
    } */
}