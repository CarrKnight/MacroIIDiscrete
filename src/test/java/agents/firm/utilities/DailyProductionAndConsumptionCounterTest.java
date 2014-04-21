/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.utilities;

import agents.EconomicAgent;
import agents.Person;
import agents.firm.Firm;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import org.junit.Assert;
import org.junit.Test;

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

    private static GoodType FOOD = new GoodType("testFood","food");
    
    
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
        MacroII mocked = mock(MacroII.class);
        DailyProductionAndConsumptionCounter counter = new DailyProductionAndConsumptionCounter();
        counter.start(mocked);
        //now check that step makes it schedule again!
        counter.turnOff();
        counter.step(mocked);
        verify(mocked,times(0)).scheduleTomorrow(ActionOrder.DAWN,counter); //0 times!


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
        counter.countNewConsumption(GoodType.LABOR, 2);
        counter.countNewConsumption(GoodType.LABOR);
        Assert.assertEquals(counter.getTodayConsumption(GoodType.LABOR), 3);
        Assert.assertEquals(counter.getTodayConsumption(FOOD), 0);
        Assert.assertEquals(counter.getYesterdayConsumption(GoodType.LABOR), 0);
        Assert.assertEquals(counter.getYesterdayConsumption(FOOD),0);
        counter.step(mock(MacroII.class));
        Assert.assertEquals(counter.getTodayConsumption(GoodType.LABOR), 0);
        Assert.assertEquals(counter.getTodayConsumption(FOOD),0);
        Assert.assertEquals(counter.getYesterdayConsumption(GoodType.LABOR), 3);
        Assert.assertEquals(counter.getYesterdayConsumption(FOOD),0);


    }

    @Test
    public void testNormalCountsConsumptionEmbedded() throws Exception
    {

        EconomicAgent agent = new Person(mock(MacroII.class));
        agent.receive(new Good(GoodType.LABOR,agent,0l),null);
        agent.receive(new Good(GoodType.LABOR,agent,0l),null);
        agent.receive(new Good(GoodType.LABOR,agent,0l),null);
        agent.consume(GoodType.LABOR);
        agent.consume(GoodType.LABOR);
        agent.consume(GoodType.LABOR);
        Assert.assertEquals(agent.getTodayConsumption(GoodType.LABOR), 3);
        Assert.assertEquals(agent.getTodayConsumption(FOOD), 0);
        Assert.assertEquals(agent.getYesterdayConsumption(GoodType.LABOR), 0);
        Assert.assertEquals(agent.getYesterdayConsumption(FOOD),0);

        Field field = EconomicAgent.class.getDeclaredField("counter");
        field.setAccessible(true);
        DailyProductionAndConsumptionCounter counter = (DailyProductionAndConsumptionCounter) field.get(agent);
        counter.step(mock(MacroII.class));
        Assert.assertEquals(agent.getTodayConsumption(GoodType.LABOR), 0);
        Assert.assertEquals(agent.getTodayConsumption(FOOD),0);
        Assert.assertEquals(agent.getYesterdayConsumption(GoodType.LABOR), 3);
        Assert.assertEquals(agent.getYesterdayConsumption(FOOD),0);


    }

    @Test
    public void testNormalCountsProductionAcquisition() throws Exception
    {

        DailyProductionAndConsumptionCounter counter = new DailyProductionAndConsumptionCounter();
        counter.countNewReceive(GoodType.LABOR, 5);
        counter.countNewProduction(GoodType.LABOR);
        Assert.assertEquals(counter.getTodayProduction(GoodType.LABOR), 1);
        Assert.assertEquals(counter.getTodayAcquisitions(GoodType.LABOR), 4);
        Assert.assertEquals(counter.getYesterdayProduction(GoodType.LABOR), 0);
        Assert.assertEquals(counter.getYesterdayAcquisitions(GoodType.LABOR),0);
        Assert.assertEquals(counter.getTodayProduction(FOOD), 0);
        Assert.assertEquals(counter.getTodayAcquisitions(FOOD), 0);
        Assert.assertEquals(counter.getYesterdayProduction(FOOD), 0);
        Assert.assertEquals(counter.getYesterdayAcquisitions(FOOD),0);

        counter.step(mock(MacroII.class));
        Assert.assertEquals(counter.getTodayProduction(GoodType.LABOR), 0);
        Assert.assertEquals(counter.getTodayAcquisitions(GoodType.LABOR), 0);
        Assert.assertEquals(counter.getYesterdayProduction(GoodType.LABOR), 1);
        Assert.assertEquals(counter.getYesterdayAcquisitions(GoodType.LABOR), 4);
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
            agent.receive(new Good(GoodType.LABOR,agent,0l),null);
        agent.countNewProduction(GoodType.LABOR);

        Assert.assertEquals(agent.getTodayProduction(GoodType.LABOR), 1);
        Assert.assertEquals(agent.getTodayAcquisitions(GoodType.LABOR), 4);
        Assert.assertEquals(agent.getYesterdayProduction(GoodType.LABOR), 0);
        Assert.assertEquals(agent.getYesterdayAcquisitions(GoodType.LABOR),0);
        Assert.assertEquals(agent.getTodayProduction(FOOD), 0);
        Assert.assertEquals(agent.getTodayAcquisitions(FOOD), 0);
        Assert.assertEquals(agent.getYesterdayProduction(FOOD), 0);
        Assert.assertEquals(agent.getYesterdayAcquisitions(FOOD),0);

        Field field = EconomicAgent.class.getDeclaredField("counter");
        field.setAccessible(true);
        DailyProductionAndConsumptionCounter counter = (DailyProductionAndConsumptionCounter) field.get(agent);
        counter.step(mock(MacroII.class));
        Assert.assertEquals(agent.getTodayProduction(GoodType.LABOR), 0);
        Assert.assertEquals(agent.getTodayAcquisitions(GoodType.LABOR), 0);
        Assert.assertEquals(agent.getYesterdayProduction(GoodType.LABOR), 1);
        Assert.assertEquals(agent.getYesterdayAcquisitions(GoodType.LABOR), 4);
        Assert.assertEquals(agent.getTodayProduction(FOOD), 0);
        Assert.assertEquals(agent.getTodayAcquisitions(FOOD), 0);
        Assert.assertEquals(agent.getYesterdayProduction(FOOD), 0);
        Assert.assertEquals(agent.getYesterdayAcquisitions(FOOD),0);


    }

}
