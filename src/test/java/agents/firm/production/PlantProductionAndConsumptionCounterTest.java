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
import agents.firm.utilities.DailyProductionAndConsumptionCounter;
import goods.DifferentiatedGoodType;
import goods.UndifferentiatedGoodType;
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
        DailyProductionAndConsumptionCounter counter = new DailyProductionAndConsumptionCounter();
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
        DailyProductionAndConsumptionCounter counter = new DailyProductionAndConsumptionCounter();

        counter.countNewProduction(UndifferentiatedGoodType.GENERIC);
        counter.countNewProduction(UndifferentiatedGoodType.GENERIC);
        counter.countNewProduction(DifferentiatedGoodType.CAPITAL);
        //today
        Assert.assertEquals(counter.getTodayProduction(UndifferentiatedGoodType.GENERIC), 2);
        Assert.assertEquals(counter.getTodayProduction(DifferentiatedGoodType.CAPITAL), 1);
        Assert.assertEquals(counter.getTodayProduction(UndifferentiatedGoodType.LABOR), 0);
        //yesterday
        Assert.assertEquals(counter.getYesterdayProduction(UndifferentiatedGoodType.GENERIC), 0);
        Assert.assertEquals(counter.getYesterdayProduction(DifferentiatedGoodType.CAPITAL), 0);
        Assert.assertEquals(counter.getYesterdayProduction(UndifferentiatedGoodType.LABOR), 0);


        //next day...
        counter.step(model);
        //today
        Assert.assertEquals(counter.getTodayProduction(UndifferentiatedGoodType.GENERIC), 0);
        Assert.assertEquals(counter.getTodayProduction(DifferentiatedGoodType.CAPITAL), 0);
        Assert.assertEquals(counter.getTodayProduction(UndifferentiatedGoodType.LABOR), 0);
        //yesterday
        Assert.assertEquals(counter.getYesterdayProduction(UndifferentiatedGoodType.GENERIC), 2);
        Assert.assertEquals(counter.getYesterdayProduction(DifferentiatedGoodType.CAPITAL), 1);
        Assert.assertEquals(counter.getYesterdayProduction(UndifferentiatedGoodType.LABOR), 0);


        //next day
        counter.step(model);
        //today
        Assert.assertEquals(counter.getTodayProduction(UndifferentiatedGoodType.GENERIC), 0);
        Assert.assertEquals(counter.getTodayProduction(DifferentiatedGoodType.CAPITAL), 0);
        Assert.assertEquals(counter.getTodayProduction(UndifferentiatedGoodType.LABOR), 0);
        //yesterday
        Assert.assertEquals(counter.getYesterdayProduction(UndifferentiatedGoodType.GENERIC), 0);
        Assert.assertEquals(counter.getYesterdayProduction(DifferentiatedGoodType.CAPITAL), 0);
        Assert.assertEquals(counter.getYesterdayProduction(UndifferentiatedGoodType.LABOR), 0);





    }

    @Test
    public void testItCountsAndResetsWeekly() throws Exception {

        DailyProductionAndConsumptionCounter counter = new DailyProductionAndConsumptionCounter();

        counter.countNewProduction(UndifferentiatedGoodType.GENERIC);
        counter.countNewProduction(UndifferentiatedGoodType.GENERIC);
        counter.countNewProduction(DifferentiatedGoodType.CAPITAL);
        //ThisWeek
        Assert.assertEquals(counter.getThisWeekProduction(UndifferentiatedGoodType.GENERIC), 2);
        Assert.assertEquals(counter.getThisWeekProduction(DifferentiatedGoodType.CAPITAL), 1);
        Assert.assertEquals(counter.getThisWeekProduction(UndifferentiatedGoodType.LABOR), 0);
        //LastWeek
        Assert.assertEquals(counter.getLastWeekProduction(UndifferentiatedGoodType.GENERIC), 0);
        Assert.assertEquals(counter.getLastWeekProduction(DifferentiatedGoodType.CAPITAL), 0);
        Assert.assertEquals(counter.getLastWeekProduction(UndifferentiatedGoodType.LABOR), 0);

        //next day...
        counter.weekEnd();
        //ThisWeek
        Assert.assertEquals(counter.getThisWeekProduction(UndifferentiatedGoodType.GENERIC), 0);
        Assert.assertEquals(counter.getThisWeekProduction(DifferentiatedGoodType.CAPITAL), 0);
        Assert.assertEquals(counter.getThisWeekProduction(UndifferentiatedGoodType.LABOR), 0);
        //LastWeek
        Assert.assertEquals(counter.getLastWeekProduction(UndifferentiatedGoodType.GENERIC), 2);
        Assert.assertEquals(counter.getLastWeekProduction(DifferentiatedGoodType.CAPITAL), 1);
        Assert.assertEquals(counter.getLastWeekProduction(UndifferentiatedGoodType.LABOR), 0);


        //next day
        counter.weekEnd();
        //ThisWeek
        Assert.assertEquals(counter.getThisWeekProduction(UndifferentiatedGoodType.GENERIC), 0);
        Assert.assertEquals(counter.getThisWeekProduction(DifferentiatedGoodType.CAPITAL), 0);
        Assert.assertEquals(counter.getThisWeekProduction(UndifferentiatedGoodType.LABOR), 0);
        //LastWeek
        Assert.assertEquals(counter.getLastWeekProduction(UndifferentiatedGoodType.GENERIC), 0);
        Assert.assertEquals(counter.getLastWeekProduction(DifferentiatedGoodType.CAPITAL), 0);
        Assert.assertEquals(counter.getLastWeekProduction(UndifferentiatedGoodType.LABOR), 0);

    }

    /********************
     * embedded tests
     *****************/


    @Test
    public void testEmbeddedCountsAndResetsDaily() throws Exception
    {
        //create a plant that produces 5 generics per worker
        MacroII model = new MacroII(1);
        Firm owner = mock(Firm.class);
        when(owner.getModel()).thenReturn(model);
        Plant plant = new Plant(Blueprint.simpleBlueprint(new UndifferentiatedGoodType("fake","useless"),0, UndifferentiatedGoodType.GENERIC,5),owner);
        plant.addWorker(mock(Person.class));
        TrueRandomScheduler scheduler = new TrueRandomScheduler(100, model.getRandom());
        model.setPhaseScheduler(scheduler);
        plant.setPlantMachinery(new LinearConstantMachinery(DifferentiatedGoodType.CAPITAL,mock(Firm.class),0,plant));
        plant.setCostStrategy(new EmptyCostStrategy());   when(owner.getHR(plant)).thenReturn(mock(HumanResources.class));

        model.start(); plant.start(model); //need to start the plant separetely because it isn't registered as an agent
        model.scheduleSoon(ActionOrder.PRODUCTION,plant);
        model.schedule.step(model);


        //today
        Assert.assertEquals(plant.getProducedToday(UndifferentiatedGoodType.GENERIC),5);
        Assert.assertEquals(plant.getProducedToday(UndifferentiatedGoodType.LABOR),0);
        //yesterday
        Assert.assertEquals(plant.getProducedYesterday(UndifferentiatedGoodType.GENERIC),0);
        Assert.assertEquals(plant.getProducedYesterday(UndifferentiatedGoodType.LABOR),0);

        //remove the worker
        plant.removeLastWorker();

        //next day...
        model.schedule.step(model);
        //today
        Assert.assertEquals(plant.getProducedToday(UndifferentiatedGoodType.GENERIC),0);
        Assert.assertEquals(plant.getProducedToday(UndifferentiatedGoodType.LABOR),0);
        //yesterday
        Assert.assertEquals(plant.getProducedYesterday(UndifferentiatedGoodType.GENERIC),5);
        Assert.assertEquals(plant.getProducedYesterday(UndifferentiatedGoodType.LABOR),0);


        //next day...
        model.schedule.step(model);
        //today
        Assert.assertEquals(plant.getProducedToday(UndifferentiatedGoodType.GENERIC),0);
        Assert.assertEquals(plant.getProducedToday(UndifferentiatedGoodType.LABOR),0);
        //yesterday
        Assert.assertEquals(plant.getProducedYesterday(UndifferentiatedGoodType.GENERIC),0);
        Assert.assertEquals(plant.getProducedYesterday(UndifferentiatedGoodType.LABOR),0);





    }

    @Test
    public void testEmbeddedCountsAndResetsWeekly() throws Exception
    {
        //create a plant that produces 5 generics per worker
        MacroII model = mock(MacroII.class);
        when(model.getCurrentPhase()).thenReturn(ActionOrder.PRODUCTION); when(model.getWeekLength()).thenReturn(7f); //set the model up
        Firm owner = mock(Firm.class);
        when(owner.getModel()).thenReturn(model);
        Plant plant = new Plant(Blueprint.simpleBlueprint(new UndifferentiatedGoodType("fake","useless"),0, UndifferentiatedGoodType.GENERIC,5),owner);
        plant.addWorker(mock(Person.class));
        TrueRandomScheduler scheduler = new TrueRandomScheduler(100, model.getRandom());
        model.setPhaseScheduler(scheduler);
        plant.setPlantMachinery(new LinearConstantMachinery(DifferentiatedGoodType.CAPITAL,mock(Firm.class),0,plant));
        plant.setCostStrategy(new EmptyCostStrategy());

        plant.step(model);




        Assert.assertEquals(plant.getThisWeekThroughput(UndifferentiatedGoodType.GENERIC),5);
        Assert.assertEquals(plant.getThisWeekThroughput(UndifferentiatedGoodType.LABOR),0);
        //LastWEek
        Assert.assertEquals(plant.getLastWeekThroughput(UndifferentiatedGoodType.GENERIC),0);
        Assert.assertEquals(plant.getLastWeekThroughput(UndifferentiatedGoodType.LABOR),0);

        //next week
        plant.weekEnd(1);
        //ThisWeek
        //ThisWeek
        Assert.assertEquals(plant.getThisWeekThroughput(UndifferentiatedGoodType.GENERIC),0);
        Assert.assertEquals(plant.getThisWeekThroughput(UndifferentiatedGoodType.LABOR),0);
        //LastWEek
        Assert.assertEquals(plant.getLastWeekThroughput(UndifferentiatedGoodType.GENERIC),5);
        Assert.assertEquals(plant.getLastWeekThroughput(UndifferentiatedGoodType.LABOR),0);


        //next week
        plant.weekEnd(2);
        //ThisWeek
        Assert.assertEquals(plant.getThisWeekThroughput(UndifferentiatedGoodType.GENERIC),0);
        Assert.assertEquals(plant.getThisWeekThroughput(UndifferentiatedGoodType.LABOR),0);
        //LastWEek
        Assert.assertEquals(plant.getLastWeekThroughput(UndifferentiatedGoodType.GENERIC),0);
        Assert.assertEquals(plant.getLastWeekThroughput(UndifferentiatedGoodType.LABOR),0);

    }
}
