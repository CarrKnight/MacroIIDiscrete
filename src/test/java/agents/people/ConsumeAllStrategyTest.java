/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.people;

import goods.UndifferentiatedGoodType;
import model.MacroII;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class ConsumeAllStrategyTest {


    @Test
    public void consumeCorrectly() throws Exception {

        Person p = new Person(mock(MacroII.class));
        ConsumptionStrategy strategy = new ConsumeAllStrategy();

        p.receiveMany(UndifferentiatedGoodType.MONEY,1000);
        p.receiveMany(UndifferentiatedGoodType.GENERIC,1000);
        Assert.assertEquals(1000, p.hasHowMany(UndifferentiatedGoodType.MONEY));
        Assert.assertEquals(1000,p.hasHowMany(UndifferentiatedGoodType.GENERIC));

        strategy.consume(p,mock(MacroII.class));
        Assert.assertEquals(0, p.hasHowMany(UndifferentiatedGoodType.MONEY));
        Assert.assertEquals(0,p.hasHowMany(UndifferentiatedGoodType.GENERIC));



    }

    @Test
    public void testFactoryAlwaysGetsTheSame() throws Exception {
        ConsumptionStrategy strategy1 = ConsumptionStrategy.Factory.build(ConsumeAllStrategy.class);
        ConsumptionStrategy strategy2 = ConsumptionStrategy.Factory.build(ConsumeAllStrategy.class);
        Assert.assertEquals(strategy1,strategy2);
        Assert.assertTrue(strategy1 == strategy2);

    }
}