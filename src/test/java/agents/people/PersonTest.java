/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.people;

import model.MacroII;
import model.utilities.scheduler.PhaseScheduler;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class PersonTest {


    @Test
    public void productionStrategiesAreActivatedCorrectly() throws Exception {

        PersonalProductionStrategy strategy1 = mock(PersonalProductionStrategy.class);
        ConsumptionStrategy strategy2 = mock(ConsumptionStrategy.class);

        MacroII model = new MacroII(1l);
        Person p = new Person(model);
        p.setProductionStrategy(strategy1);
        p.setConsumptionStrategy(strategy2);

        model.addAgent(p);
        model.start();

        for(int i=0; i<10; i++)
        {
            model.schedule.step(model);
            verify(strategy1,times(i+1)).produce(p, model);
            verify(strategy2,times(i+1)).consume(p, model);
        }
        //remove them
        p.setConsumptionStrategy(mock(ConsumptionStrategy.class));
        p.setProductionStrategy(mock(PersonalProductionStrategy.class));
        for(int i=0; i<10; i++)
        {
            model.schedule.step(model);
            //stays at 10
            verify(strategy1,times(10)).produce(p,model);
            verify(strategy2,times(10)).consume(p,model);
        }
    }

    //if i set the production/consumption set AFTER start is called, the system works the same!
    @Test
    public void productionStrategiesAreActivatedCorrectly2() throws Exception {

        PersonalProductionStrategy strategy1 = mock(PersonalProductionStrategy.class);
        ConsumptionStrategy strategy2 = mock(ConsumptionStrategy.class);

        MacroII model = new MacroII(1l);
        Person p = new Person(model);

        model.addAgent(p);
        model.start();

        p.setProductionStrategy(strategy1);
        p.setConsumptionStrategy(strategy2);


        for(int i=0; i<10; i++)
        {
            model.schedule.step(model);
            verify(strategy1,times(i+1)).produce(p,model);
            verify(strategy2,times(i+1)).consume(p,model);
        }
        //remove them
        p.setConsumptionStrategy(mock(ConsumptionStrategy.class));
        p.setProductionStrategy(mock(PersonalProductionStrategy.class));
        for(int i=0; i<10; i++)
        {
            model.schedule.step(model);
            //stays at 10
            verify(strategy1,times(10)).produce(p,model);
            verify(strategy2,times(10)).consume(p,model);
        }
    }

    //make sure that no PRODUCTION step is registered until AFTER production strategy is set!
    @Test
    public void productionStarts() throws Exception {



        MacroII model = new MacroII(1l);
        final PhaseScheduler spy = spy(model.getPhaseScheduler());
        model.setPhaseScheduler(spy);


        Person p = new Person(model);


        model.addAgent(p);
        model.start();


        for(int i=0; i<10; i++)
        {
            model.schedule.step(model);
            Assert.assertFalse(p.isSteppingOnProduction());

        }
        //now it should step!
        p.setProductionStrategy(mock(PersonalProductionStrategy.class));
        for(int i=0; i<10; i++)
        {
            model.schedule.step(model);
            Assert.assertTrue(p.isSteppingOnProduction());
        }
    }
}