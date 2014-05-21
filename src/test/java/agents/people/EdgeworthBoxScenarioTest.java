/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.people;

import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.scenario.EdgeworthBoxScenario;
import org.junit.Assert;
import org.junit.Test;

public class EdgeworthBoxScenarioTest {


    //two people with the same cobb-douglas utility function. They have simmetrical endowment. They should trade until they
    //split the endowment evenly


    @Test
    public void simpleEdgeworthBox() throws Exception {
        //immediate order handler, shop set price
        MacroII model = new MacroII(1l);
        final EdgeworthBoxScenario scenario1 = new EdgeworthBoxScenario(model);
        model.setScenario(scenario1);

        model.start();
        model.schedule.step(model);
        //should have split evenly!
        Assert.assertEquals(5,scenario1.getPerson1().hasHowMany(UndifferentiatedGoodType.GENERIC));
        Assert.assertEquals(5,scenario1.getPerson1().hasHowMany(UndifferentiatedGoodType.GENERIC));
        Assert.assertEquals(5,scenario1.getPerson2().hasHowMany(UndifferentiatedGoodType.MONEY));
        Assert.assertEquals(5,scenario1.getPerson2().hasHowMany(UndifferentiatedGoodType.MONEY));



    }



}