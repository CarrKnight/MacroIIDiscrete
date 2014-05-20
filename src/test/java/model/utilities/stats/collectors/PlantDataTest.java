/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.collectors;

import agents.firm.Firm;
import agents.firm.production.Plant;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.stats.collectors.PlantData;
import model.utilities.stats.collectors.enums.PlantDataType;
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
 * @version 2013-08-22
 * @see
 */
public class PlantDataTest {

    @Test
    public void rescheduleItself()
    {
        MacroII model = mock(MacroII.class);
        PlantData data = new PlantData();

        data.start(model,mock(Plant.class),mock(Firm.class));
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
    public void profits()
    {


        Plant plant = mock(Plant.class);
        Firm owner = mock(Firm.class);
        MacroII model = mock(MacroII.class);
        when(model.getCurrentPhase()).thenReturn(ActionOrder.CLEANUP_DATA_GATHERING);
        when(model.getMainScheduleTime()).thenReturn(-1d);
        PlantData data = new PlantData();


        data.start(model,plant,owner);
        //put in price data
        when(owner.getPlantProfits(plant)).thenReturn(1f,2f,3f);
        when(model.getMainScheduleTime()).thenReturn(0d,1d,2d);
        data.step(model);
        data.step(model);
        data.step(model);

        //make sure it works!
        PlantDataType type = PlantDataType.PROFITS_THAT_WEEK;
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
    public void costs()
    {

        Plant plant = mock(Plant.class);
        Firm owner = mock(Firm.class);
        MacroII model = mock(MacroII.class);
        when(model.getCurrentPhase()).thenReturn(ActionOrder.CLEANUP_DATA_GATHERING);
        when(model.getMainScheduleTime()).thenReturn(-1d);
        PlantData data = new PlantData();


        data.start(model,plant,owner);
        //put in price data
        when(owner.getPlantCosts(plant)).thenReturn(1f,2f,3f);
        when(model.getMainScheduleTime()).thenReturn(0d,1d,2d);
        data.step(model);
        data.step(model);
        data.step(model);

        //make sure it works!
        PlantDataType type = PlantDataType.COSTS_THAT_WEEK;
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
    public void revenues()
    {



        Plant plant = mock(Plant.class);
        Firm owner = mock(Firm.class);
        MacroII model = mock(MacroII.class);
        when(model.getCurrentPhase()).thenReturn(ActionOrder.CLEANUP_DATA_GATHERING);
        when(model.getMainScheduleTime()).thenReturn(-1d);
        PlantData data = new PlantData();


        data.start(model,plant,owner);
        //put in price data
        when(owner.getPlantRevenues(plant)).thenReturn(1f,2f,3f);
        when(model.getMainScheduleTime()).thenReturn(0d,1d,2d);
        data.step(model);
        data.step(model);
        data.step(model);

        //make sure it works!
        PlantDataType type = PlantDataType.REVENUES_THAT_WEEK;
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
    public void workers()
    {



        Plant plant = mock(Plant.class);
        Firm owner = mock(Firm.class);
        MacroII model = mock(MacroII.class);
        when(model.getCurrentPhase()).thenReturn(ActionOrder.CLEANUP_DATA_GATHERING);
        when(model.getMainScheduleTime()).thenReturn(-1d);
        PlantData data = new PlantData();


        data.start(model,plant,owner);
        //put in price data
        when(plant.getNumberOfWorkers()).thenReturn(1,2,3);
        when(model.getMainScheduleTime()).thenReturn(0d,1d,2d);
        data.step(model);
        data.step(model);
        data.step(model);

        //make sure it works!
        PlantDataType type = PlantDataType.TOTAL_WORKERS;
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
