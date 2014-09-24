/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.people;

import financial.market.EndOfPhaseOrderHandler;
import financial.market.ImmediateOrderHandler;
import financial.utilities.AveragePricePolicy;
import financial.utilities.BuyerSetPricePolicy;
import financial.utilities.ShopSetPricePolicy;
import goods.GoodType;
import model.MacroII;
import model.scenario.EdgeworthBoxScenario;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EdgeworthBoxScenarioTest {


    //two people with the same cobb-douglas utility function. They have simmetrical endowment. They should trade until they
    //split the endowment evenly


    @Test
    public void simpleEdgeworthBox() throws Exception {
        //immediate order handler, shop set price
        MacroII model = new MacroII(System.currentTimeMillis());
        EdgeworthBoxScenario scenario1 = new EdgeworthBoxScenario(model);
        model.setScenario(scenario1);

        runEdgeworthBox(model, scenario1, 5, 5, 5, 5);


    }


    @Test
    public void worksRegardlessOfOrderHandler() throws Exception {
        MacroII model = new MacroII(System.currentTimeMillis());
        EdgeworthBoxScenario scenario = new EdgeworthBoxScenario(model);
        model.setScenario(scenario);
        scenario.setOrderHandler(new ImmediateOrderHandler());

        runEdgeworthBox(model, scenario, 5, 5, 5, 5);


        model = new MacroII(System.currentTimeMillis());
        scenario = new EdgeworthBoxScenario(model);
        model.setScenario(scenario);
        scenario.setOrderHandler(new EndOfPhaseOrderHandler());

        runEdgeworthBox(model, scenario, 5, 5, 5, 5);

    }



    @Test
    public void pricePolicyDeterminesSplit() throws Exception {
        MacroII model = new MacroII(System.currentTimeMillis());
        EdgeworthBoxScenario scenario = new EdgeworthBoxScenario(model);
        model.setScenario(scenario);
        scenario.setPricePolicy(new ShopSetPricePolicy());

        runEdgeworthBox(model, scenario, 5, 5, 5, 5);

        //more advantageous to the seller
        model = new MacroII(System.currentTimeMillis());
        scenario = new EdgeworthBoxScenario(model);
        model.setScenario(scenario);
        scenario.setPricePolicy(new AveragePricePolicy());

        runEdgeworthBox(model, scenario, 7, 6, 3, 4);

        //the advantage is all to the seller now
        model = new MacroII(System.currentTimeMillis());
        scenario = new EdgeworthBoxScenario(model);
        model.setScenario(scenario);
        scenario.setPricePolicy(new BuyerSetPricePolicy());

        runEdgeworthBox(model, scenario, 7, 7, 3, 3);




    }

    public void runEdgeworthBox(MacroII model, EdgeworthBoxScenario scenario1,
                                int expectedGood1Person1, int expectedGood2Person1,
                                int expectedGood1Person2, int expectedGood2Person2) {
        model.start();
        //nobody has anything!
        final GoodType good1 = scenario1.getGoodMarket().getGoodType();
        final GoodType good2 = scenario1.getGoodMarket().getMoney();

        Assert.assertEquals(0, scenario1.getPerson1().hasHowMany(good1));
        Assert.assertEquals(0,scenario1.getPerson1().hasHowMany(good2));
        Assert.assertEquals(0,scenario1.getPerson2().hasHowMany(good1));
        Assert.assertEquals(0,scenario1.getPerson2().hasHowMany(good2));

        model.schedule.step(model);
        //should have split evenly!
        Assert.assertEquals(expectedGood1Person1,scenario1.getPerson1().hasHowMany(good1));
        Assert.assertEquals(expectedGood2Person1,scenario1.getPerson1().hasHowMany(good2));
        Assert.assertEquals(expectedGood1Person2,scenario1.getPerson2().hasHowMany(good1));
        Assert.assertEquals(expectedGood2Person2,scenario1.getPerson2().hasHowMany(good2));
        //utility should have increased!
        Person stub1 = mock(Person.class);
        when(stub1.hasHowMany(good1)).thenReturn(scenario1.getFirstPersonDailyEndowmentOfX());
        when(stub1.hasHowMany(good2)).thenReturn(0);
        //if there was any trading your utility should have increased
        if(scenario1.getGoodMarket().getTodayVolume() > 0)
            Assert.assertTrue(scenario1.getPerson1().computesUtility() > scenario1.getPerson1().getUtilityFunction().computesUtility(stub1) );
        else
            Assert.assertEquals(scenario1.getPerson1().computesUtility(),scenario1.getPerson1().getUtilityFunction().computesUtility(stub1),0001);
        //utility should have increased!
        Person stub2 = mock(Person.class);
        when(stub2.hasHowMany(good1)).thenReturn(0);
        when(stub2.hasHowMany(good2)).thenReturn(scenario1.getSecondPersonDailyEndowmentOfY());
        //if there was any trading your utility should have increased
        if(scenario1.getGoodMarket().getTodayVolume() > 0)
            Assert.assertTrue(scenario1.getPerson2().computesUtility() > scenario1.getPerson2().getUtilityFunction().computesUtility(stub2) );
        else
            Assert.assertEquals(scenario1.getPerson2().computesUtility(),scenario1.getPerson2().getUtilityFunction().computesUtility(stub2),0001);


        model.schedule.step(model);
        //should have split evenly!
        Assert.assertEquals(expectedGood1Person1,scenario1.getPerson1().hasHowMany(good1));
        Assert.assertEquals(expectedGood2Person1,scenario1.getPerson1().hasHowMany(good2));
        Assert.assertEquals(expectedGood1Person2,scenario1.getPerson2().hasHowMany(good1));
        Assert.assertEquals(expectedGood2Person2,scenario1.getPerson2().hasHowMany(good2));
    }


}