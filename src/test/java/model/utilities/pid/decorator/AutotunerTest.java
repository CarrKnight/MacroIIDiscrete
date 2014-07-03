/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.pid.decorator;

import ec.util.MersenneTwisterFast;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.pid.ControllerInput;
import model.utilities.pid.PIDController;
import model.utilities.pid.tuners.ShinskeyTableFOPIDT;
import model.utilities.stats.processes.FirstOrderIntegratingPlusDeadTime;
import model.utilities.stats.processes.FirstOrderPlusDeadTime;
import model.utilities.stats.regression.KalmanFOPIDTRegressionWithKnownTimeDelay;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.function.Supplier;

import static org.mockito.Mockito.*;

public class AutotunerTest {

    @Test
    public void prettyPictureTest() throws Exception {
        MacroII model = new MacroII(0);
        final PIDAutotuner PIDAutotuner = runLearningExperimentWithUnknownDeadTime(model.getRandom(), model.drawProportionalGain(), model.drawIntegrativeGain(), 0, 1.5f, 0.3f, 5, null);
        System.out.println(PIDAutotuner.getDelay() + "-----" + PIDAutotuner.getGain() + " ----" + PIDAutotuner.getTimeConstant());
        model = new MacroII(0);
       runLearningFOIPDT(model.getRandom(), model.drawProportionalGain(), model.drawIntegrativeGain(), -10, 1.5f, 0.3f, 5, null);

    }



    private PIDAutotuner runLearningExperimentWithUnknownDeadTime(MersenneTwisterFast random, float proportionalParameter,
                                                                                               float integrativeParameter, int intercept, float gain, float timeConstant, int deadTime,
                                                                                               Supplier<Double> noiseMaker) throws FileNotFoundException {
        PIDAutotuner controller = new PIDAutotuner(new PIDController(proportionalParameter,integrativeParameter,0,random));
        PrintWriter writer = new PrintWriter(Paths.get("tmp.csv").toFile());
        controller.setAfterHowManyDaysShouldTune(1000);
        int target = 10;
        FirstOrderPlusDeadTime process = new FirstOrderPlusDeadTime(intercept,gain,timeConstant, deadTime);
        if(noiseMaker != null)
            process.setRandomNoise(noiseMaker);

        //create the regression too


        //output starts at intercept
        float output = 0;
        //delayed input, useful for learning
        writer.println("input" + "," + "output" + "," + "target" + "," + "proportional" + "," + "integrative");


        for(int step =0; step < 2000; step++)
        {


            //PID step
            controller.adjust(new ControllerInput(target,output),true,mock(MacroII.class),null, ActionOrder.DAWN);

            //process reacts
            float input = controller.getCurrentMV();
            assert !Float.isNaN(input);
            assert !Float.isInfinite(input);
            output = (float) process.newStep(input);

            writer.println(input + "," + output + "," + target + "," + controller.getProportionalGain() + "," + controller.getIntegralGain());
            System.out.println(input + "," + output + "," + target + "," + controller.getProportionalGain() + "," + controller.getIntegralGain());





            //shock target with 10%
            if(random.nextBoolean(.10)) {
                if (random.nextBoolean())
                    target++;
                else
                    target = Math.max(target-1,0);
            }



        }
        return controller;

    }


    private PIDAutotuner runLearningFOIPDT(MersenneTwisterFast random, float proportionalParameter,
                                                                  float integrativeParameter, int intercept, float gain, float timeConstant, int deadTime,
                                                                  Supplier<Double> noiseMaker) throws FileNotFoundException {
        PIDAutotuner controller = new PIDAutotuner(new PIDController(proportionalParameter,integrativeParameter,0,random),
                KalmanFOPIDTRegressionWithKnownTimeDelay::new,new ShinskeyTableFOPIDT(),null);
        PrintWriter writer = new PrintWriter(Paths.get("tmp2.csv").toFile());
        controller.setAfterHowManyDaysShouldTune(1000);
        int target = 100;
        FirstOrderIntegratingPlusDeadTime process = new FirstOrderIntegratingPlusDeadTime(intercept,gain,timeConstant, deadTime);
        if(noiseMaker != null)
            process.setRandomNoise(noiseMaker);

        //create the regression too


        //output starts at intercept
        float output = 0;
        //delayed input, useful for learning
        writer.println("input" + "," + "output" + "," + "target" + "," + "proportional" + "," + "integrative");


        for(int step =0; step < 2000; step++)
        {


            //PID step
            controller.adjust(new ControllerInput(target,output),true,mock(MacroII.class),null, ActionOrder.DAWN);

            //process reacts
            float input = controller.getCurrentMV();
            assert !Float.isNaN(input);
            assert !Float.isInfinite(input);
            output = (float) process.newStep(input);

            writer.println(input + "," + output + "," + target + "," + controller.getProportionalGain() + "," + controller.getIntegralGain());
            System.out.println(input + "," + output + "," + target + "," + controller.getProportionalGain() + "," + controller.getIntegralGain());





            //shock target with 10%
            if(random.nextBoolean(.10)) {
                if (random.nextBoolean())
                    target++;
                else
                    target = Math.max(target-1,0);
            }



        }
        return controller;

    }
}