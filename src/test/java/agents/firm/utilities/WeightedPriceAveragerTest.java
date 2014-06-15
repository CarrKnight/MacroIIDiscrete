/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.utilities;

import agents.firm.Department;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WeightedPriceAveragerTest {


    @Test
    public void testAveraging() throws Exception {

        WeightedPriceAverager averager1 = new WeightedPriceAverager(2);


        Department department = mock(Department.class);
        for(int i=1; i<=10; i++ )
        {
            when(department.getTodayTrades()).thenReturn(i);
            when(department.getLastClosingPrice()).thenReturn(i);
            averager1.endOfTheDay(department);

        }
        Assert.assertEquals(9.526315789,averager1.getAveragedPrice(),.001f);

        //two more with no trades
        when(department.getTodayTrades()).thenReturn(0);
        for(int i=0; i<2; i++)
        {
            averager1.endOfTheDay(department);
        }

        Assert.assertTrue(Float.isNaN(averager1.getAveragedPrice()));




    }

}