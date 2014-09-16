/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.prediction;

import model.MacroII;
import model.scenario.MonopolistScenario;
import model.scenario.OneSectorStatics;
import org.junit.Test;

public class AutoRegressivePurchasesPredictorTest {


    @Test
    public void testLearningMonopolist() throws Exception {
        //run the test 5 times
        for(int i=0; i<5; i++)
        {
            int seed = (int)System.currentTimeMillis();

            final MacroII macroII = new MacroII(seed);
            MonopolistScenario scenario1 = new MonopolistScenario(macroII);
            scenario1.setPurchasesPricePreditorStrategy(AutoRegressivePurchasesPredictor.class);

            OneSectorStatics.testRandomSlopeMonopolist(seed, macroII, scenario1);

        }

    }



    @Test
    public void testCompetitive() {

        OneSectorStatics.testCompetitiveHrCustomPredictor((hr) -> new AutoRegressivePurchasesPredictor(hr.getModel(), hr));
    }

}