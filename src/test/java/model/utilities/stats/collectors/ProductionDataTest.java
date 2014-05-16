/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.collectors;

import agents.firm.Firm;
import agents.firm.production.Plant;
import goods.GoodType;
import goods.GoodTypeMasterList;
import goods.UndifferentiatedGoodType;
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
 * @version 2013-11-03
 * @see
 */
public class ProductionDataTest
{

    final static private GoodType OIL = new UndifferentiatedGoodType("oiltest","oil");

    @Test
    public void rescheduleItself()
    {
        MacroII model = mock(MacroII.class);
        ProductionData data = new ProductionData();
        when(model.getGoodTypeMasterList()).thenReturn(new GoodTypeMasterList());

        data.start(model,mock(Plant.class));
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
    public void productionTest()
    {



        Plant plant = mock(Plant.class);
        Firm owner = mock(Firm.class);
        MacroII model = mock(MacroII.class);
        when(model.getCurrentPhase()).thenReturn(ActionOrder.CLEANUP_DATA_GATHERING);
        when(model.getMainScheduleTime()).thenReturn(-1d);
        ProductionData data = new ProductionData();

        //initialize master-list
        GoodTypeMasterList list = new GoodTypeMasterList();
        list.addNewSectors(UndifferentiatedGoodType.GENERIC);
        list.addNewSectors(OIL);
        when(model.getGoodTypeMasterList()).thenReturn(list);

        data.start(model,plant);
        //put in price data
        when(plant.getProducedToday(UndifferentiatedGoodType.GENERIC)).thenReturn(1,2,3);
        when(model.getMainScheduleTime()).thenReturn(0d,1d,2d);
        data.step(model);
        data.step(model);
        data.step(model);

        //make sure it works!
        GoodType type = UndifferentiatedGoodType.GENERIC;
        Assert.assertEquals(data.numberOfObservations(), 3);
        Assert.assertEquals(data.getObservationRecordedThisDay(type,0),1d,.00001d);
        Assert.assertEquals(data.getLatestObservation(type),3d,.00001d);
        Assert.assertEquals(data.getObservationRecordedThisDay(type,2),3d,.00001d);
        Assert.assertArrayEquals(data.getAllRecordedObservations(type),new double[]{1d,2d,3d},.0001d);
        Assert.assertArrayEquals(data.getObservationsRecordedTheseDays(type,new int[]{0, 2}),
                new double[]{1d,3d},.0001d);
        Assert.assertArrayEquals(data.getObservationsRecordedTheseDays(type,0, 1),
                new double[]{1d,2d},.0001d);


        type = OIL;
        Assert.assertEquals(data.numberOfObservations(), 3);
        Assert.assertEquals(data.getObservationRecordedThisDay(type,0),0,.00001d);
        Assert.assertEquals(data.getLatestObservation(type),0,.00001d);
        Assert.assertEquals(data.getObservationRecordedThisDay(type,2),0,.00001d);
        Assert.assertArrayEquals(data.getAllRecordedObservations(type),new double[]{0,0,0},.0001d);
        Assert.assertArrayEquals(data.getObservationsRecordedTheseDays(type,new int[]{0, 0}),
                new double[]{0,0},.0001d);
        Assert.assertArrayEquals(data.getObservationsRecordedTheseDays(type,0, 1),
                new double[]{0,0},.0001d);

    }



}
