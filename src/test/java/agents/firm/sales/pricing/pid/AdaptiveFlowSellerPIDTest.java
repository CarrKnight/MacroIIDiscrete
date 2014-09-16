/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.pricing.pid;

import model.MacroII;
import model.scenario.MonopolistScenario;
import model.scenario.OneSectorStatics;
import org.junit.Test;

public class AdaptiveFlowSellerPIDTest
{


    @Test
    public void testLearningMonopolist() throws Exception {
        //run the test 5 times
        for(int i=0; i<5; i++)
        {
            int seed = (int)System.currentTimeMillis();

            final MacroII macroII = new MacroII(seed);
            MonopolistScenario scenario1 = new MonopolistScenario(macroII);

            OneSectorStatics.testRandomSlopeMonopolist(seed, macroII, scenario1, AdaptiveFlowSellerPID.class);

        }









    }

}