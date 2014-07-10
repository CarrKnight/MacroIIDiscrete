/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.processes;

import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.pid.ControllerInput;
import model.utilities.pid.PIDController;
import model.utilities.stats.regression.KalmanFOPDTRegressionWithKnownTimeDelay;
import org.junit.Test;

import java.io.PrintWriter;
import java.nio.file.Paths;

import static org.mockito.Mockito.*;

public class PIGradientDescentTest {


    @Test
    public void pidGradient() throws Exception {
        MacroII model = new MacroII(1l);

        PIDController controller = new PIDController(model.drawProportionalGain(), model.drawIntegrativeGain(), 0);
        PrintWriter writer = new PrintWriter(Paths.get("tmp.csv").toFile());

        int target = 100;
        FirstOrderPlusDeadTime process = new FirstOrderPlusDeadTime(0, 1.5f, 0.3f, 5);
        KalmanFOPDTRegressionWithKnownTimeDelay regression = new KalmanFOPDTRegressionWithKnownTimeDelay(5);

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
            regression.addObservation(output,input);

            writer.println(input + "," + output + "," + target + "," + controller.getProportionalGain() + "," + controller.getIntegralGain());
        //    System.out.println(input + "," + output + "," + target + "," + controller.getProportionalGain() + "," + controller.getIntegralGain());



            if(step>200)
            {
                //try to maximize
                PIGradientDescent descent = new PIGradientDescent(regression,controller,target);
                final PIGradientDescent.PIDGains gains = descent.getNewGains();
                controller.setGains(gains.getProportional(),gains.getIntegral(),gains.getDerivative());
                System.out.println(gains);
            }



            //shock target with 10%
            if(model.getRandom().nextBoolean(.10)) {
                if (model.getRandom().nextBoolean())
                    target++;
                else
                    target = Math.max(target-1,0);
            }



        }

    }
}