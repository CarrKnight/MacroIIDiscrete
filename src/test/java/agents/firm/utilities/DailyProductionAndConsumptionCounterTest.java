/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.utilities;

import agents.EconomicAgent;
import agents.people.Person;
import agents.firm.Firm;
import goods.Good;
import goods.GoodType;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import org.junit.Assert;
import org.junit.Test;
import tests.MemoryLeakVerifier;

import java.lang.reflect.Field;

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
 * @version 2013-08-06
 * @see
 */
public class DailyProductionAndConsumptionCounterTest {

    private static GoodType FOOD = new UndifferentiatedGoodType("testFood","food");


    @Test
    public void testMakeSureItSchedules() throws Exception
    {

        //make sure start() puts it on the schedule
        MacroII mocked = mock(MacroII.class);
        DailyProductionAndConsumptionCounter counter = new DailyProductionAndConsumptionCounter();
        counter.start(mocked);
        verify(mocked,times(1)).scheduleSoon(ActionOrder.DAWN,counter);
        //now check that step makes it schedule again!
        counter.step(mocked);
        verify(mocked,times(1)).scheduleTomorrow(ActionOrder.DAWN,counter);


    }

    //same test as above, but the counter is embedded in the firm this time
    @Test
    public void testMakeSureItSchedulesEmbedded() throws Exception
    {

        //make sure start() puts it on the schedule
        MacroII mocked = mock(MacroII.class);
        EconomicAgent agent = new Firm(mocked);
        agent.start(mocked);

        //let's grab the reference to the counter
        Field field = EconomicAgent.class.getDeclaredField("counter");
        field.setAccessible(true);
        DailyProductionAndConsumptionCounter counter = (DailyProductionAndConsumptionCounter) field.get(agent);


        verify(mocked, times(1)).scheduleSoon(ActionOrder.DAWN,counter);
        //now check that step makes it schedule again!
        counter.step(mocked);
        verify(mocked,times(1)).scheduleTomorrow(ActionOrder.DAWN,counter);


    }


    @Test
    public void testTurnOff() throws Exception {

        //make sure that turnOff stops it from scheduling!
        for(int i=0; i<100; i++) {
            MemoryLeakVerifier verifier;
            {

                MacroII mocked = mock(MacroII.class);
                DailyProductionAndConsumptionCounter counter = new DailyProductionAndConsumptionCounter();
                verifier = new MemoryLeakVerifier(counter);
                counter.start(mocked);
                //now check that step makes it schedule again!
                counter.turnOff();
                counter.step(mocked);
                verify(mocked, times(0)).scheduleTomorrow(ActionOrder.DAWN, counter); //0 times!
                counter = null;
                mocked = null;
            }
            verifier.assertGarbageCollected("counter");
            Assert.assertNull(verifier.getObject());
            System.out.println(Runtime.getRuntime().freeMemory());

        }
    }

    //same test as above, but the counter is embedded in the firm this time
    @Test
    public void testMakeSureItTurnsOffEmbedded() throws Exception
    {

        //make sure start() puts it on the schedule
        MacroII mocked = mock(MacroII.class);
        EconomicAgent agent = new Firm(mocked);
        agent.start(mocked);

        //let's grab the reference to the counter
        Field field = EconomicAgent.class.getDeclaredField("counter");
        field.setAccessible(true);
        DailyProductionAndConsumptionCounter counter = (DailyProductionAndConsumptionCounter) field.get(agent);


        //now check that step makes it schedule again!
        agent.turnOff();
        counter.step(mocked);
        verify(mocked,times(0)).scheduleTomorrow(ActionOrder.DAWN,counter);


    }

    /**
     * make sure counts are correct
     */
    @Test
    public void testNormalCountsConsumption() throws Exception
    {

        DailyProductionAndConsumptionCounter counter = new DailyProductionAndConsumptionCounter();
        counter.countNewConsumption(UndifferentiatedGoodType.LABOR, 2);
        counter.countNewConsumption(UndifferentiatedGoodType.LABOR);
        Assert.assertEquals(counter.getTodayConsumption(UndifferentiatedGoodType.LABOR), 3);
        Assert.assertEquals(counter.getTodayConsumption(FOOD), 0);
        Assert.assertEquals(counter.getYesterdayConsumption(UndifferentiatedGoodType.LABOR), 0);
        Assert.assertEquals(counter.getYesterdayConsumption(FOOD),0);
        counter.step(mock(MacroII.class));
        Assert.assertEquals(counter.getTodayConsumption(UndifferentiatedGoodType.LABOR), 0);
        Assert.assertEquals(counter.getTodayConsumption(FOOD),0);
        Assert.assertEquals(counter.getYesterdayConsumption(UndifferentiatedGoodType.LABOR), 3);
        Assert.assertEquals(counter.getYesterdayConsumption(FOOD),0);


    }

    @Test
    public void testNormalCountsConsumptionEmbedded() throws Exception
    {

        EconomicAgent agent = new Person(mock(MacroII.class));
        agent.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.LABOR),null);
        agent.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.LABOR),null);
        agent.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.LABOR),null);
        agent.consume(UndifferentiatedGoodType.LABOR);
        agent.consume(UndifferentiatedGoodType.LABOR);
        agent.consume(UndifferentiatedGoodType.LABOR);
        Assert.assertEquals(agent.getTodayConsumption(UndifferentiatedGoodType.LABOR), 3);
        Assert.assertEquals(agent.getTodayConsumption(FOOD), 0);
        Assert.assertEquals(agent.getYesterdayConsumption(UndifferentiatedGoodType.LABOR), 0);
        Assert.assertEquals(agent.getYesterdayConsumption(FOOD),0);

        Field field = EconomicAgent.class.getDeclaredField("counter");
        field.setAccessible(true);
        DailyProductionAndConsumptionCounter counter = (DailyProductionAndConsumptionCounter) field.get(agent);
        counter.step(mock(MacroII.class));
        Assert.assertEquals(agent.getTodayConsumption(UndifferentiatedGoodType.LABOR), 0);
        Assert.assertEquals(agent.getTodayConsumption(FOOD),0);
        Assert.assertEquals(agent.getYesterdayConsumption(UndifferentiatedGoodType.LABOR), 3);
        Assert.assertEquals(agent.getYesterdayConsumption(FOOD),0);


    }

    @Test
    public void testNormalCountsProductionAcquisition() throws Exception
    {

        DailyProductionAndConsumptionCounter counter = new DailyProductionAndConsumptionCounter();
        counter.countNewReceive(UndifferentiatedGoodType.LABOR, 5);
        counter.countNewProduction(UndifferentiatedGoodType.LABOR);
        Assert.assertEquals(counter.getTodayProduction(UndifferentiatedGoodType.LABOR), 1);
        Assert.assertEquals(counter.getTodayAcquisitions(UndifferentiatedGoodType.LABOR), 4);
        Assert.assertEquals(counter.getYesterdayProduction(UndifferentiatedGoodType.LABOR), 0);
        Assert.assertEquals(counter.getYesterdayAcquisitions(UndifferentiatedGoodType.LABOR),0);
        Assert.assertEquals(counter.getTodayProduction(FOOD), 0);
        Assert.assertEquals(counter.getTodayAcquisitions(FOOD), 0);
        Assert.assertEquals(counter.getYesterdayProduction(FOOD), 0);
        Assert.assertEquals(counter.getYesterdayAcquisitions(FOOD),0);

        counter.step(mock(MacroII.class));
        Assert.assertEquals(counter.getTodayProduction(UndifferentiatedGoodType.LABOR), 0);
        Assert.assertEquals(counter.getTodayAcquisitions(UndifferentiatedGoodType.LABOR), 0);
        Assert.assertEquals(counter.getYesterdayProduction(UndifferentiatedGoodType.LABOR), 1);
        Assert.assertEquals(counter.getYesterdayAcquisitions(UndifferentiatedGoodType.LABOR), 4);
        Assert.assertEquals(counter.getTodayProduction(FOOD), 0);
        Assert.assertEquals(counter.getTodayAcquisitions(FOOD), 0);
        Assert.assertEquals(counter.getYesterdayProduction(FOOD), 0);
        Assert.assertEquals(counter.getYesterdayAcquisitions(FOOD),0);


    }


    @Test
    public void testNormalCountsProductionEmbedded() throws Exception
    {

        EconomicAgent agent = new Person(mock(MacroII.class));
        for(int i=0; i<5 ;i++)
            agent.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.LABOR),null);
        agent.countNewProduction(UndifferentiatedGoodType.LABOR);

        Assert.assertEquals(agent.getTodayProduction(UndifferentiatedGoodType.LABOR), 1);
        Assert.assertEquals(agent.getTodayAcquisitions(UndifferentiatedGoodType.LABOR), 4);
        Assert.assertEquals(agent.getYesterdayProduction(UndifferentiatedGoodType.LABOR), 0);
        Assert.assertEquals(agent.getYesterdayAcquisitions(UndifferentiatedGoodType.LABOR),0);
        Assert.assertEquals(agent.getTodayProduction(FOOD), 0);
        Assert.assertEquals(agent.getTodayAcquisitions(FOOD), 0);
        Assert.assertEquals(agent.getYesterdayProduction(FOOD), 0);
        Assert.assertEquals(agent.getYesterdayAcquisitions(FOOD),0);

        Field field = EconomicAgent.class.getDeclaredField("counter");
        field.setAccessible(true);
        DailyProductionAndConsumptionCounter counter = (DailyProductionAndConsumptionCounter) field.get(agent);
        counter.step(mock(MacroII.class));
        Assert.assertEquals(agent.getTodayProduction(UndifferentiatedGoodType.LABOR), 0);
        Assert.assertEquals(agent.getTodayAcquisitions(UndifferentiatedGoodType.LABOR), 0);
        Assert.assertEquals(agent.getYesterdayProduction(UndifferentiatedGoodType.LABOR), 1);
        Assert.assertEquals(agent.getYesterdayAcquisitions(UndifferentiatedGoodType.LABOR), 4);
        Assert.assertEquals(agent.getTodayProduction(FOOD), 0);
        Assert.assertEquals(agent.getTodayAcquisitions(FOOD), 0);
        Assert.assertEquals(agent.getYesterdayProduction(FOOD), 0);
        Assert.assertEquals(agent.getYesterdayAcquisitions(FOOD),0);


    }

}
