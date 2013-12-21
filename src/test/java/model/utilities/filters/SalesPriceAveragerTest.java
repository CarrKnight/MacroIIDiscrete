/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.filters;

import agents.firm.sales.SalesDepartment;
import model.MacroII;
import model.utilities.ActionOrder;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * <h4>Description</h4>
 * <p/>
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-12-20
 * @see
 */
public class SalesPriceAveragerTest {


    @Test
    public void testscheduledCorrectly() throws Exception {

        MacroII model = mock(MacroII.class);
        SalesDepartment department = mock(SalesDepartment.class);

        SalesPriceAverager averager = new SalesPriceAverager(model,department,5);
        //it should have scheduled itself
        verify(model,times(1)).scheduleSoon(ActionOrder.CLEANUP_DATA_GATHERING,averager);
        //step it
        averager.step(model);
        //should have scheduled itself again
        verify(model,times(1)).scheduleTomorrow(ActionOrder.CLEANUP_DATA_GATHERING, averager);
        //and again
        averager.step(model);
        verify(model,times(2)).scheduleTomorrow(ActionOrder.CLEANUP_DATA_GATHERING, averager);

        //if i turn it off, it stays off
        averager.turnOff();
        averager.step(model);
        verify(model,times(2)).scheduleTomorrow(ActionOrder.CLEANUP_DATA_GATHERING,averager); //didn't get called a third time


    }

    @Test
    public void testRecordsRightNumbers() throws Exception
    {
        MacroII model = mock(MacroII.class);
        SalesDepartment department = mock(SalesDepartment.class);

        SalesPriceAverager averager = new SalesPriceAverager(model,department,3);

        when(department.getTodayOutflow()).thenReturn(24);
        when(department.getLastClosingPrice()).thenReturn(78l);
        when(department.getTodayInflow()).thenReturn(12);

        averager.step(model);
        when(department.getTodayOutflow()).thenReturn(0);
        when(department.getLastClosingPrice()).thenReturn(78l);
        when(department.getTodayInflow()).thenReturn(12);
        averager.step(model);
        Assert.assertEquals(86,averager.getAveragedPrice(),.0001d);

        //now try in groups of four
        averager = new SalesPriceAverager(model,department,4);

        when(department.getTodayOutflow()).thenReturn(16);
        when(department.getLastClosingPrice()).thenReturn(86l);
        when(department.getTodayInflow()).thenReturn(4);
        averager.step(model);

        for(int i=0; i<3 ; i++)
        {
            when(department.getTodayOutflow()).thenReturn(0);
            when(department.getLastClosingPrice()).thenReturn(86l);
            when(department.getTodayInflow()).thenReturn(4);
            averager.step(model);
        }
        Assert.assertEquals(86,averager.getAveragedPrice(),.0001d);




    }
}
