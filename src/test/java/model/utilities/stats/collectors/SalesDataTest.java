/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.collectors;

import agents.firm.sales.SalesDepartment;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.stats.collectors.enums.SalesDataType;
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
 * @version 2013-08-27
 * @see
 */
public class SalesDataTest {


    @Test
    public void rescheduleItself()
    {
        SalesDepartment department = mock(SalesDepartment.class);
        MacroII model = mock(MacroII.class);
        SalesData data = new SalesData();

        data.start(model,department);
        verify(model).scheduleSoon(ActionOrder.CLEANUP_DATA_GATHERING, data);
        when(model.getCurrentPhase()).thenReturn(ActionOrder.CLEANUP_DATA_GATHERING);
        data.step(model);
        data.step(model);
        verify(model, times(2)).scheduleTomorrow(ActionOrder.CLEANUP_DATA_GATHERING, data);
        //turn off doesn't reschedule
        data.turnOff();
        data.step(model);
        verify(model, times(2)).scheduleTomorrow(ActionOrder.CLEANUP_DATA_GATHERING, data); //only the two old times are counted!
    }




    @Test
    public void lastPriceTest()
    {


        SalesDepartment department = mock(SalesDepartment.class);
        MacroII model = mock(MacroII.class);
        when(model.getCurrentPhase()).thenReturn(ActionOrder.CLEANUP_DATA_GATHERING);
        when(model.getMainScheduleTime()).thenReturn(-1d);
        SalesData data = new SalesData();


        data.start(model,department);
        //put in price data
        when(department.getLastClosingPrice()).thenReturn(1l,2l,3l);
        when(model.getMainScheduleTime()).thenReturn(0d,1d,2d);
        data.step(model);
        data.step(model);
        data.step(model);

        //make sure it works!
        SalesDataType type = SalesDataType.CLOSING_PRICES;
        Assert.assertEquals(data.numberOfObservations(), 3);
        Assert.assertEquals(data.getObservationRecordedThisDay(type,0),1d,.00001d);
        Assert.assertEquals(data.getLatestObservation(type),3d,.00001d);
        Assert.assertEquals(data.getObservationRecordedThisDay(type,2),3d,.00001d);
        Assert.assertArrayEquals(data.getAllRecordedObservations(type),new double[]{1d,2d,3d},.0001d);
        Assert.assertArrayEquals(data.getObservationsRecordedTheseDays(type,new int[]{0, 2}),
                new double[]{1d,3d},.0001d);
        Assert.assertArrayEquals(data.getObservationsRecordedTheseDays(type,0, 1),
                new double[]{1d,2d},.0001d);

    }

  @Test
    public void avgPriceTest()
    {


        SalesDepartment department = mock(SalesDepartment.class);
        MacroII model = mock(MacroII.class);
        when(model.getCurrentPhase()).thenReturn(ActionOrder.CLEANUP_DATA_GATHERING);
        when(model.getMainScheduleTime()).thenReturn(-1d);
        SalesData data = new SalesData();


        data.start(model,department);
        //put in price data
        when(department.getAverageClosingPrice()).thenReturn(1f,2f,3f);
        when(model.getMainScheduleTime()).thenReturn(0d,1d,2d);
        data.step(model);
        data.step(model);
        data.step(model);

        //make sure it works!
        SalesDataType type = SalesDataType.AVERAGE_CLOSING_PRICES;
        Assert.assertEquals(data.numberOfObservations(), 3);
        Assert.assertEquals(data.getObservationRecordedThisDay(type,0),1d,.00001d);
        Assert.assertEquals(data.getLatestObservation(type),3d,.00001d);
        Assert.assertEquals(data.getObservationRecordedThisDay(type,2),3d,.00001d);
        Assert.assertArrayEquals(data.getAllRecordedObservations(type),new double[]{1d,2d,3d},.0001d);
        Assert.assertArrayEquals(data.getObservationsRecordedTheseDays(type,new int[]{0, 2}),
                new double[]{1d,3d},.0001d);
        Assert.assertArrayEquals(data.getObservationsRecordedTheseDays(type,0, 1),
                new double[]{1d,2d},.0001d);

    }


    @Test
    public void inflowTest()
    {


        SalesDepartment department = mock(SalesDepartment.class);
        MacroII model = mock(MacroII.class);
        when(model.getCurrentPhase()).thenReturn(ActionOrder.CLEANUP_DATA_GATHERING);
        when(model.getMainScheduleTime()).thenReturn(-1d);
        SalesData data = new SalesData();


        data.start(model,department);
        //put in price data
        when(department.getTodayInflow()).thenReturn(1,2,3);
        when(model.getMainScheduleTime()).thenReturn(0d,1d,2d);
        data.step(model);
        data.step(model);
        data.step(model);

        //make sure it works!
        SalesDataType type = SalesDataType.INFLOW;
        Assert.assertEquals(data.numberOfObservations(), 3);
        Assert.assertEquals(data.getObservationRecordedThisDay(type,0),1d,.00001d);
        Assert.assertEquals(data.getLatestObservation(type),3d,.00001d);
        Assert.assertEquals(data.getObservationRecordedThisDay(type,2),3d,.00001d);
        Assert.assertArrayEquals(data.getAllRecordedObservations(type),new double[]{1d,2d,3d},.0001d);
        Assert.assertArrayEquals(data.getObservationsRecordedTheseDays(type,new int[]{0, 2}),
                new double[]{1d,3d},.0001d);
        Assert.assertArrayEquals(data.getObservationsRecordedTheseDays(type,0, 1),
                new double[]{1d,2d},.0001d);

    }

    @Test
    public void outflowTest()
    {


        SalesDepartment department = mock(SalesDepartment.class);
        MacroII model = mock(MacroII.class);
        when(model.getCurrentPhase()).thenReturn(ActionOrder.CLEANUP_DATA_GATHERING);
        when(model.getMainScheduleTime()).thenReturn(-1d);
        SalesData data = new SalesData();


        data.start(model,department);
        //put in price data
        when(department.getTodayOutflow()).thenReturn(1,2,3);
        when(model.getMainScheduleTime()).thenReturn(0d,1d,2d);
        data.step(model);
        data.step(model);
        data.step(model);

        //make sure it works!
        SalesDataType type = SalesDataType.OUTFLOW;
        Assert.assertEquals(data.numberOfObservations(), 3);
        Assert.assertEquals(data.getObservationRecordedThisDay(type,0),1d,.00001d);
        Assert.assertEquals(data.getLatestObservation(type),3d,.00001d);
        Assert.assertEquals(data.getObservationRecordedThisDay(type,2),3d,.00001d);
        Assert.assertArrayEquals(data.getAllRecordedObservations(type),new double[]{1d,2d,3d},.0001d);
        Assert.assertArrayEquals(data.getObservationsRecordedTheseDays(type,new int[]{0, 2}),
                new double[]{1d,3d},.0001d);
        Assert.assertArrayEquals(data.getObservationsRecordedTheseDays(type,0, 1),
                new double[]{1d,2d},.0001d);

    }




    @Test
    public void inventoryTest()
    {


        SalesDepartment department = mock(SalesDepartment.class);
        MacroII model = mock(MacroII.class);
        when(model.getCurrentPhase()).thenReturn(ActionOrder.CLEANUP_DATA_GATHERING);
        when(model.getMainScheduleTime()).thenReturn(-1d);
        SalesData data = new SalesData();


        data.start(model,department);
        //put in price data
        when(department.getHowManyToSell()).thenReturn(1, 2, 3);
        when(model.getMainScheduleTime()).thenReturn(0d,1d,2d);
        data.step(model);
        data.step(model);
        data.step(model);

        //make sure it works!
        SalesDataType type = SalesDataType.HOW_MANY_TO_SELL;
        Assert.assertEquals(data.numberOfObservations(), 3);
        Assert.assertEquals(data.getObservationRecordedThisDay(type,0),1d,.00001d);
        Assert.assertEquals(data.getLatestObservation(type),3d,.00001d);
        Assert.assertEquals(data.getObservationRecordedThisDay(type,2),3d,.00001d);
        Assert.assertArrayEquals(data.getAllRecordedObservations(type),new double[]{1d,2d,3d},.0001d);
        Assert.assertArrayEquals(data.getObservationsRecordedTheseDays(type,new int[]{0, 2}),
                new double[]{1d,3d},.0001d);
        Assert.assertArrayEquals(data.getObservationsRecordedTheseDays(type,0, 1),
                new double[]{1d,2d},.0001d);

    }


}
