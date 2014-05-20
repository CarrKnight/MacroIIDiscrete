/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.people;

import goods.UndifferentiatedGoodType;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CobbDouglas2GoodsUtilityTest {

    @Test
    public void testUtilityNumbers() throws Exception
    {

        CobbDouglas2GoodsUtility utility =
                new CobbDouglas2GoodsUtility(UndifferentiatedGoodType.GENERIC,
                        UndifferentiatedGoodType.MONEY,.75f);

        Person p = mock(Person.class);
        when(p.hasHowMany(UndifferentiatedGoodType.GENERIC)).thenReturn(9);
        when(p.hasHowMany(UndifferentiatedGoodType.MONEY)).thenReturn(9);

        Assert.assertEquals(10,utility.computesUtility(p),.01f);

        when(p.hasHowMany(UndifferentiatedGoodType.MONEY)).thenReturn(4);
        Assert.assertEquals(8.40896, utility.computesUtility(p), .01f);

        when(p.hasHowMany(UndifferentiatedGoodType.MONEY)).thenReturn(9);
        when(p.hasHowMany(UndifferentiatedGoodType.GENERIC)).thenReturn(4);

        Assert.assertEquals(5.94604,utility.computesUtility(p),.01f);

    }


    @Test
    public void testMarginalUtility() throws Exception {

        CobbDouglas2GoodsUtility utility =
                new CobbDouglas2GoodsUtility(UndifferentiatedGoodType.GENERIC,
                        UndifferentiatedGoodType.MONEY,.75f);

        Person p = mock(Person.class);
        when(p.hasHowMany(UndifferentiatedGoodType.GENERIC)).thenReturn(9);
        when(p.hasHowMany(UndifferentiatedGoodType.MONEY)).thenReturn(9);
        Assert.assertEquals(10,utility.computesUtility(p),.01f);

        Assert.assertEquals(3.717,utility.howMuchOfThisGoodDoYouNeedToCompensateTheLossOfOneUnitOfAnother(UndifferentiatedGoodType.GENERIC,
                UndifferentiatedGoodType.MONEY,p),.001d);

        Assert.assertEquals(2.48685,utility.howMuchOfThisGoodWouldYouGiveAwayInExchangeForOneUnitOfAnother(UndifferentiatedGoodType.GENERIC,
                UndifferentiatedGoodType.MONEY,p),.001d);
        //at this point the person could place a bid 1

    }
}