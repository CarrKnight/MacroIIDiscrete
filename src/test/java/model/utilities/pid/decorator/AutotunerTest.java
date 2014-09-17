/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.pid.decorator;

import ec.util.MersenneTwisterFast;
import model.MacroII;
import model.scenario.RegressionStatics;
import model.utilities.ActionOrder;
import model.utilities.pid.ControllerInput;
import model.utilities.pid.PIDController;
import model.utilities.stats.processes.DynamicProcess;
import model.utilities.stats.processes.FirstOrderIntegratingPlusDeadTime;
import model.utilities.stats.processes.FirstOrderPlusDeadTime;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.function.Supplier;

import static org.mockito.Mockito.mock;

public class AutotunerTest {


    @Test
    public void tuneFOPDT() throws FileNotFoundException {
        MacroII model = new MacroII(0);
        final PIDAutotuner autotuner = runLearningExperimentWithUnknownDeadTime(
                model.getRandom(), model.drawProportionalGain(), model.drawIntegrativeGain(), null ,
                new FirstOrderPlusDeadTime(0, 1.5f, 0.3f, 5));
        System.out.println(autotuner.getDelay() + "-----" + autotuner.getGain() + " ----" + autotuner.getTimeConstant());
        System.out.println(autotuner.describeRegression());

    }

    @Test
    public void tuneFOIPDT() throws Exception {

        MacroII model = model = new MacroII(0);
        final PIDAutotuner autotuner = runLearningExperimentWithUnknownDeadTime(model.getRandom(),
                model.drawProportionalGain(), model.drawIntegrativeGain(), null,
                new FirstOrderIntegratingPlusDeadTime(-10, 1.5f, 0.3f, 5));
        System.out.println(autotuner.getDelay() + "-----" + autotuner.getGain() + " ----" + autotuner.getTimeConstant());
        System.out.println(autotuner.describeRegression());

    }


//new FirstOrderPlusDeadTime(intercept,gain,timeConstant, deadTime)
    private PIDAutotuner runLearningExperimentWithUnknownDeadTime(MersenneTwisterFast random, float proportionalParameter,
                                                                  float integrativeParameter,
                                                                  Supplier<Double> noiseMaker, DynamicProcess systemDynamic) throws FileNotFoundException {
        PIDAutotuner controller =
                new PIDAutotuner(new PIDController(proportionalParameter,integrativeParameter,0));
        controller.setAfterHowManyDaysShouldTune(1001);
        int target = 10;
        DynamicProcess process = systemDynamic ;
        if(noiseMaker != null)
            process.setRandomNoise(noiseMaker);

        //create the regression too


        //output starts at intercept
        float output = 0;
        //delayed input, useful for learning

        SummaryStatistics errorBeforeTuning = new SummaryStatistics();

        SummaryStatistics errorAfterTuning= new SummaryStatistics();
        SummaryStatistics finalError= new SummaryStatistics();

        for(int step =0; step < 2000; step++)
        {

            //PID step
            controller.adjust(new ControllerInput(target,output),true,mock(MacroII.class),null, ActionOrder.DAWN);

            //process reacts
            float input = controller.getCurrentMV();
            assert !Float.isNaN(input);
            assert !Float.isInfinite(input);
            output = (float) process.newStep(input);




            if(step <= 1000)
                errorBeforeTuning.addValue(Math.pow(target-output,2));
            else
                errorAfterTuning.addValue(Math.pow(target-output,2));
            if(step>1900)
                finalError.addValue(Math.pow(target-output,2));


            //shock target with 10%
            if(random.nextBoolean(.10)) {
                if (random.nextBoolean())
                    target++;
                else
                    target = Math.max(target-1,0);
            }



        }
        System.out.println("errors: " + errorBeforeTuning.getMean() + " --- " + errorAfterTuning.getMean());
        System.out.println("final error: " + finalError.getMean());
        System.out.println("regression: " + controller.getRegression());

        RegressionStatics.tracksAcceptably(controller.getRegression(), process,
                RegressionStatics.MAXIMUM_ABS_ERROR, 100,
                Math.max(controller.getCurrentMV(), RegressionStatics.FIXED_INPUT));
        Assert.assertTrue(errorAfterTuning.getMean() < errorBeforeTuning.getMean());
        //either have a very low error, or at least have improved by a factor of over 100
        Assert.assertTrue(finalError.getMean() < 10 || finalError.getMean() < errorBeforeTuning.getMean() / 100 );



        return controller;

    }



}