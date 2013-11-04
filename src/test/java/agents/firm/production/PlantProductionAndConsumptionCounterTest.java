/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.production;

import agents.Person;
import agents.firm.Firm;
import agents.firm.cost.EmptyCostStrategy;
import agents.firm.personell.HumanResources;
import agents.firm.production.technology.LinearConstantMachinery;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.scheduler.TrueRandomScheduler;
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
 * @version 2013-11-02
 * @see
 */
public class PlantProductionAndConsumptionCounterTest {


    /***********************
     * alone tests
     ************************/

    @Test
    public void testItSchedules()
    {
        //MODELLO
        MacroII model = mock(MacroII.class);
        PlantProductionAndConsumptionCounter counter = new PlantProductionAndConsumptionCounter(mock(Plant.class));
        //doesn't schedule till start
        verify(model,never()).scheduleSoon(ActionOrder.DAWN, counter);

        //start it
        counter.start(model);
        verify(model,times(1)).scheduleSoon(ActionOrder.DAWN, counter);

        //check that it reschedules after stepping
        counter.step(model);
        verify(model,times(1)).scheduleTomorrow(ActionOrder.DAWN,counter);

        //check that it doesn't reschedule after turning off
        counter.turnOff();
        model = mock(MacroII.class);
        counter.step(model);
        verify(model, never()).scheduleTomorrow(ActionOrder.DAWN,counter);




    }

    @Test
    public void testItCountsAndResetsDaily() throws Exception
    {
        MacroII model = mock(MacroII.class);
        PlantProductionAndConsumptionCounter counter = new PlantProductionAndConsumptionCounter(mock(Plant.class));

        counter.newProduction(GoodType.GENERIC);
        counter.newProduction(GoodType.GENERIC);
        counter.newProduction(GoodType.OIL);
        //today
        Assert.assertEquals(counter.getProducedToday()[GoodType.GENERIC.ordinal()], 2);
        Assert.assertEquals(counter.getProducedToday()[GoodType.OIL.ordinal()], 1);
        Assert.assertEquals(counter.getProducedToday()[GoodType.LABOR.ordinal()], 0);
        //yesterday
        Assert.assertEquals(counter.getProducedYesterday()[GoodType.GENERIC.ordinal()],0);
        Assert.assertEquals(counter.getProducedYesterday()[GoodType.OIL.ordinal()],0);
        Assert.assertEquals(counter.getProducedYesterday()[GoodType.LABOR.ordinal()],0);

        //next day...
        counter.step(model);
        //today
        Assert.assertEquals(counter.getProducedToday()[GoodType.GENERIC.ordinal()],0);
        Assert.assertEquals(counter.getProducedToday()[GoodType.OIL.ordinal()],0);
        Assert.assertEquals(counter.getProducedToday()[GoodType.LABOR.ordinal()],0);
        //yesterday
        Assert.assertEquals(counter.getProducedYesterday()[GoodType.GENERIC.ordinal()],2);
        Assert.assertEquals(counter.getProducedYesterday()[GoodType.OIL.ordinal()],1);
        Assert.assertEquals(counter.getProducedYesterday()[GoodType.LABOR.ordinal()],0);


        //next day
        counter.step(model);
        //today
        Assert.assertEquals(counter.getProducedToday()[GoodType.GENERIC.ordinal()],0);
        Assert.assertEquals(counter.getProducedToday()[GoodType.OIL.ordinal()],0);
        Assert.assertEquals(counter.getProducedToday()[GoodType.LABOR.ordinal()],0);
        //yesterday
        Assert.assertEquals(counter.getProducedYesterday()[GoodType.GENERIC.ordinal()],0);
        Assert.assertEquals(counter.getProducedYesterday()[GoodType.OIL.ordinal()],0);
        Assert.assertEquals(counter.getProducedYesterday()[GoodType.LABOR.ordinal()],0);





    }

    @Test
    public void testItCountsAndResetsWeekly() throws Exception {

        PlantProductionAndConsumptionCounter counter = new PlantProductionAndConsumptionCounter(mock(Plant.class));

        counter.newProduction(GoodType.GENERIC);
        counter.newProduction(GoodType.GENERIC);
        counter.step(mock(MacroII.class)); //not important
        counter.newProduction(GoodType.OIL);
        //today
        Assert.assertEquals(counter.getProducedThisWeek()[GoodType.GENERIC.ordinal()],2);
        Assert.assertEquals(counter.getProducedThisWeek()[GoodType.OIL.ordinal()],1);
        Assert.assertEquals(counter.getProducedThisWeek()[GoodType.LABOR.ordinal()],0);
        //yesterday
        Assert.assertEquals(counter.getProducedLastWeek()[GoodType.GENERIC.ordinal()],0);
        Assert.assertEquals(counter.getProducedLastWeek()[GoodType.OIL.ordinal()],0);
        Assert.assertEquals(counter.getProducedLastWeek()[GoodType.LABOR.ordinal()],0);

        //next day...
        counter.weekEnd();
        //today
        Assert.assertEquals(counter.getProducedThisWeek()[GoodType.GENERIC.ordinal()],0);
        Assert.assertEquals(counter.getProducedThisWeek()[GoodType.OIL.ordinal()],0);
        Assert.assertEquals(counter.getProducedThisWeek()[GoodType.LABOR.ordinal()],0);
        //yesterday
        Assert.assertEquals(counter.getProducedLastWeek()[GoodType.GENERIC.ordinal()],2);
        Assert.assertEquals(counter.getProducedLastWeek()[GoodType.OIL.ordinal()],1);
        Assert.assertEquals(counter.getProducedLastWeek()[GoodType.LABOR.ordinal()],0);


        //next day
        counter.weekEnd();
        //today
        Assert.assertEquals(counter.getProducedThisWeek()[GoodType.GENERIC.ordinal()],0);
        Assert.assertEquals(counter.getProducedThisWeek()[GoodType.OIL.ordinal()],0);
        Assert.assertEquals(counter.getProducedThisWeek()[GoodType.LABOR.ordinal()],0);
        //yesterday
        Assert.assertEquals(counter.getProducedLastWeek()[GoodType.GENERIC.ordinal()],0);
        Assert.assertEquals(counter.getProducedLastWeek()[GoodType.OIL.ordinal()],0);
        Assert.assertEquals(counter.getProducedLastWeek()[GoodType.LABOR.ordinal()],0);

    }

    /********************
     * embedded tests
     *****************/


    @Test
    public void testEmbeddedCountsAndResetsDaily() throws Exception
    {
        //create a plant that produces 5 generics per worker
        MacroII model = new MacroII(1l);
        Firm owner = mock(Firm.class);
        when(owner.getModel()).thenReturn(model);
        Plant plant = new Plant(Blueprint.simpleBlueprint(GoodType.BEEF,0,GoodType.GENERIC,5),owner);
        plant.addWorker(mock(Person.class));
        TrueRandomScheduler scheduler = new TrueRandomScheduler(100, model.getRandom());
        model.setPhaseScheduler(scheduler);
        plant.setPlantMachinery(new LinearConstantMachinery(GoodType.CAPITAL,mock(Firm.class),0,plant));
        plant.setCostStrategy(new EmptyCostStrategy());   when(owner.getHR(plant)).thenReturn(mock(HumanResources.class));

        model.start(); plant.start(); //need to start the plant separetely because it isn't registered as an agent
        model.scheduleSoon(ActionOrder.PRODUCTION,plant);
        model.schedule.step(model);


        //today
        Assert.assertEquals(plant.getProducedToday()[GoodType.GENERIC.ordinal()],5);
        Assert.assertEquals(plant.getProducedToday()[GoodType.LABOR.ordinal()],0);
        //yesterday
        Assert.assertEquals(plant.getProducedYesterday()[GoodType.GENERIC.ordinal()],0);
        Assert.assertEquals(plant.getProducedYesterday()[GoodType.LABOR.ordinal()],0);

        //remove the worker
        plant.removeLastWorker();

        //next day...
        model.schedule.step(model);
        //today
        Assert.assertEquals(plant.getProducedToday()[GoodType.GENERIC.ordinal()],0);
        Assert.assertEquals(plant.getProducedToday()[GoodType.LABOR.ordinal()],0);
        //yesterday
        Assert.assertEquals(plant.getProducedYesterday()[GoodType.GENERIC.ordinal()],5);
        Assert.assertEquals(plant.getProducedYesterday()[GoodType.LABOR.ordinal()],0);


        //next day...
        model.schedule.step(model);
        //today
        Assert.assertEquals(plant.getProducedToday()[GoodType.GENERIC.ordinal()],0);
        Assert.assertEquals(plant.getProducedToday()[GoodType.LABOR.ordinal()],0);
        //yesterday
        Assert.assertEquals(plant.getProducedYesterday()[GoodType.GENERIC.ordinal()],0);
        Assert.assertEquals(plant.getProducedYesterday()[GoodType.LABOR.ordinal()],0);





    }

    @Test
    public void testEmbeddedCountsAndResetsWeekly() throws Exception
    {
        //create a plant that produces 5 generics per worker
        MacroII model = mock(MacroII.class);
        when(model.getCurrentPhase()).thenReturn(ActionOrder.PRODUCTION); when(model.getWeekLength()).thenReturn(7f); //set the model up
        Firm owner = mock(Firm.class);
        when(owner.getModel()).thenReturn(model);
        Plant plant = new Plant(Blueprint.simpleBlueprint(GoodType.BEEF,0,GoodType.GENERIC,5),owner);
        plant.addWorker(mock(Person.class));
        TrueRandomScheduler scheduler = new TrueRandomScheduler(100, model.getRandom());
        model.setPhaseScheduler(scheduler);
        plant.setPlantMachinery(new LinearConstantMachinery(GoodType.CAPITAL,mock(Firm.class),0,plant));
        plant.setCostStrategy(new EmptyCostStrategy());

        plant.step(model);




        //today
        Assert.assertEquals(plant.getThisWeekThroughput()[GoodType.GENERIC.ordinal()],5);
        Assert.assertEquals(plant.getThisWeekThroughput()[GoodType.LABOR.ordinal()],0);
        //yesterday
        Assert.assertEquals(plant.getLastWeekThroughput()[GoodType.GENERIC.ordinal()],0);
        Assert.assertEquals(plant.getLastWeekThroughput()[GoodType.LABOR.ordinal()],0);

        //next week
        plant.weekEnd(1);
        //today
        Assert.assertEquals(plant.getThisWeekThroughput()[GoodType.GENERIC.ordinal()],0);
        Assert.assertEquals(plant.getThisWeekThroughput()[GoodType.LABOR.ordinal()],0);
        //yesterday
        Assert.assertEquals(plant.getLastWeekThroughput()[GoodType.GENERIC.ordinal()],5);
        Assert.assertEquals(plant.getLastWeekThroughput()[GoodType.LABOR.ordinal()],0);


        //next week
        plant.weekEnd(2);
        //today
        Assert.assertEquals(plant.getThisWeekThroughput()[GoodType.GENERIC.ordinal()],0);
        Assert.assertEquals(plant.getThisWeekThroughput()[GoodType.LABOR.ordinal()],0);
        //yesterday
        Assert.assertEquals(plant.getLastWeekThroughput()[GoodType.GENERIC.ordinal()],0);
        Assert.assertEquals(plant.getLastWeekThroughput()[GoodType.LABOR.ordinal()],0);

    }
}
