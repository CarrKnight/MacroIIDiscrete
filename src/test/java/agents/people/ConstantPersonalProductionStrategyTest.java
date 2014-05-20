/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.people;

import goods.UndifferentiatedGoodType;
import model.MacroII;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ConstantPersonalProductionStrategyTest {


    @Test
    public void constantProduction() throws Exception {

        Person stub = mock(Person.class);
        ConstantPersonalProductionStrategy strategy = new ConstantPersonalProductionStrategy(10, UndifferentiatedGoodType.GENERIC);
        strategy.produce(stub,mock(MacroII.class));
        verify(stub,times(1)).receiveMany(UndifferentiatedGoodType.GENERIC,10);

        strategy.setDailyProductionRate(5);
        strategy.setDailyProductionType(UndifferentiatedGoodType.MONEY);
        strategy.produce(stub,mock(MacroII.class));
        verify(stub,times(1)).receiveMany(UndifferentiatedGoodType.MONEY,5);


    }
}