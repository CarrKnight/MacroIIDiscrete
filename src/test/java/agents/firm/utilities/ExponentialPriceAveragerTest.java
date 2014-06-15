/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.utilities;

import agents.firm.Department;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class ExponentialPriceAveragerTest {


    @Test
    public void testAveraging() throws Exception {

        ExponentialPriceAverager averager1 = new ExponentialPriceAverager(.9f,
                PriceAverager.NoTradingDayPolicy.COUNT_AS_0);
        ExponentialPriceAverager averager2 = new ExponentialPriceAverager(.9f,
                PriceAverager.NoTradingDayPolicy.COUNT_AS_LAST_CLOSING_PRICE);
        ExponentialPriceAverager averager3 = new ExponentialPriceAverager(.9f,
                PriceAverager.NoTradingDayPolicy.IGNORE);

        Department department = mock(Department.class);
        for(int i=1; i<=10; i++ )
        {
            when(department.getTodayTrades()).thenReturn(i);
            when(department.getLastClosingPrice()).thenReturn(i);
            averager1.endOfTheDay(department);
            averager2.endOfTheDay(department);
            averager3.endOfTheDay(department);
        }

        //two more with no trades
        when(department.getTodayTrades()).thenReturn(0);
        for(int i=0; i<2; i++)
        {
            averager1.endOfTheDay(department);
            averager2.endOfTheDay(department);
            averager3.endOfTheDay(department);
        }

        Assert.assertEquals(0,averager1.getAveragedPrice(),.01f);
        Assert.assertEquals(10f,averager2.getAveragedPrice(),.01f);
        Assert.assertEquals(9.888888889f,averager3.getAveragedPrice(),.01f);



    }
}