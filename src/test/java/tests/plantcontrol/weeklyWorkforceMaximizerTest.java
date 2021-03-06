/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package tests.plantcontrol;

import agents.firm.Firm;
import agents.firm.cost.PlantCostStrategy;
import agents.firm.personell.HumanResources;
import agents.firm.production.Plant;
import agents.firm.production.control.TargetAndMaximizePlantControl;
import agents.firm.production.control.maximizer.SetTargetThenTryAgainMaximizer;
import agents.firm.production.control.maximizer.algorithms.hillClimbers.AnnealingReactingMaximizer;
import agents.firm.production.control.maximizer.algorithms.hillClimbers.HillClimberMaximizer;
import ec.util.MersenneTwisterFast;
import model.MacroII;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import sim.engine.Schedule;

import static org.junit.Assert.assertEquals;
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
 * @author Ernesto
 * @version 2012-09-25
 * @see
 */
public class weeklyWorkforceMaximizerTest {


    //since the behavior part is mostly tested somewhere else, here we really need to make sure things get scheduled and so on

    int currentTarget = 0;

    int profits = 0;


    /**
     * Profits are getNumberOfWorkers^2
     */
    @Test
    public void scenario1Test()
    {

        currentTarget = 0;

        //model
        MacroII model = new MacroII(1);



        HumanResources hr = mock(HumanResources.class);
        TargetAndMaximizePlantControl control = mock(TargetAndMaximizePlantControl.class);
        Plant plant = mock(Plant.class);  when(plant.getModel()).thenReturn(model);
        Firm firm = mock(Firm.class);
        when(control.getPlant()).thenReturn(plant);
        when(hr.getPlant()).thenReturn(plant);
        when(plant.maximumWorkersPossible()).thenReturn(30); when(plant.getBuildingCosts()).thenReturn(100);
        when(plant.minimumWorkersNeeded()).thenReturn(1);
        when(hr.getFirm()).thenReturn(firm);
        when(hr.getPlant()).thenReturn(plant);
        when(firm.getModel()).thenReturn(model);
        when(firm.getRandom()).thenReturn(model.random);
        when(control.getHr()).thenReturn(hr);
        PlantCostStrategy strategy = mock(PlantCostStrategy.class);
        when(plant.getCostStrategy()).thenReturn(strategy);
        when(strategy.weeklyFixedCosts()).thenReturn(100);




        //make the setter change the field
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                currentTarget = (Integer) invocation.getArguments()[0];
                return null;
            }
        }).when(control).setTarget(anyInt());




        //maximize!
        SetTargetThenTryAgainMaximizer<HillClimberMaximizer> maximizer = new SetTargetThenTryAgainMaximizer<>(hr,control,HillClimberMaximizer.class);

        maximizer.start();
        when(plant.getNumberOfWorkers()).thenReturn(currentTarget);
        when(control.getTarget()).thenReturn(currentTarget);

        for(int i=0; i<100; i++)
        {
            //we are being stepped each turn by the stepper!

            maximizer.step(model);


            //profit function
            when(firm.getPlantProfits(any(Plant.class))).thenReturn((float)
                    (currentTarget * currentTarget)
            );
            when(plant.getNumberOfWorkers()).thenReturn(currentTarget);
            when(control.getTarget()).thenReturn(currentTarget);


        }

        assertEquals(currentTarget, 30);




    }


    /**
     * Profits are getNumberOfWorkers^2
     */
    @Test
    public void scenario2Test()
    {

        currentTarget = 0;

        //model
        MacroII model = new MacroII(1);
        model.schedule = mock(Schedule.class);


        HumanResources hr = mock(HumanResources.class);
        TargetAndMaximizePlantControl control = mock(TargetAndMaximizePlantControl.class);
        Plant plant = mock(Plant.class);  when(plant.getModel()).thenReturn(model);
        Firm firm = mock(Firm.class);
        when(control.getPlant()).thenReturn(plant);
        when(hr.getPlant()).thenReturn(plant);
        when(plant.maximumWorkersPossible()).thenReturn(30); when(plant.getBuildingCosts()).thenReturn(100);
        when(plant.minimumWorkersNeeded()).thenReturn(1);
        when(hr.getFirm()).thenReturn(firm);
        when(hr.getPlant()).thenReturn(plant);
        when(control.getHr()).thenReturn(hr);
        when(firm.getModel()).thenReturn(model);
        when(firm.getRandom()).thenReturn(model.random);
        PlantCostStrategy strategy = mock(PlantCostStrategy.class);
        when(plant.getCostStrategy()).thenReturn(strategy);
        when(strategy.weeklyFixedCosts()).thenReturn(100);




        //make the setter change the field
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                currentTarget = (Integer) invocation.getArguments()[0];
                return null;
            }
        }).when(control).setTarget(anyInt());




        //maximize!
        SetTargetThenTryAgainMaximizer<HillClimberMaximizer> maximizer = new SetTargetThenTryAgainMaximizer<>(hr,control,
                HillClimberMaximizer.class);

        maximizer.start();
        when(plant.getNumberOfWorkers()).thenReturn(currentTarget);
        when(control.getTarget()).thenReturn(currentTarget);

        for(int i=0; i<100; i++)
        {
            //we are being stepped each turn by the stepper!
//            verify(schedule,times(i+1)).scheduleOnceIn(anyDouble(),any(Steppable.class));

            maximizer.step(model);


            //profit function
            when(firm.getPlantProfits(any(Plant.class))).thenReturn(
                    currentTarget < 10 ? (float) (currentTarget * currentTarget) : 0f
            );
            when(plant.getNumberOfWorkers()).thenReturn(currentTarget);
            when(control.getTarget()).thenReturn(currentTarget);


        }

        assertEquals(currentTarget, 9);




    }



    /**
     * Profits are getNumberOfWorkers^2
     */
    @Test
    public void scenario3Test()
    {
        currentTarget = 0;


        //model
        MacroII model = new MacroII(1);
        model.schedule = mock(Schedule.class);


        HumanResources hr = mock(HumanResources.class);
        TargetAndMaximizePlantControl control = mock(TargetAndMaximizePlantControl.class);
        Plant plant = mock(Plant.class);  when(plant.getModel()).thenReturn(model);
        Firm firm = mock(Firm.class);
        when(control.getPlant()).thenReturn(plant);
        when(hr.getPlant()).thenReturn(plant); when(hr.getRandom()).thenReturn(new MersenneTwisterFast(1));
        when(plant.maximumWorkersPossible()).thenReturn(30); when(plant.weeklyFixedCosts()).thenReturn(100);
        when(plant.minimumWorkersNeeded()).thenReturn(1);
        when(hr.getFirm()).thenReturn(firm);
        when(firm.getModel()).thenReturn(model);
        when(firm.getRandom()).thenReturn(model.random);
        when(control.getHr()).thenReturn(hr);
        PlantCostStrategy strategy = mock(PlantCostStrategy.class);
        when(plant.getCostStrategy()).thenReturn(strategy);
        when(strategy.weeklyFixedCosts()).thenReturn(100);





        //make the setter change the field
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                currentTarget = (Integer) invocation.getArguments()[0];
                return null;
            }
        }).when(control).setTarget(anyInt());

        System.out.println("------------------------------------------");


        //maximize!
        SetTargetThenTryAgainMaximizer<AnnealingReactingMaximizer> maximizer =
                new SetTargetThenTryAgainMaximizer<>(hr,control,AnnealingReactingMaximizer.class);

        maximizer.start();
        when(plant.getNumberOfWorkers()).thenReturn(currentTarget);
        when(control.getTarget()).thenReturn(currentTarget);

        for(int i=0; i<100; i++)
        {
            //we are being stepped each turn by the stepper!
//            verify(schedule,times(i+1)).scheduleOnceIn(anyDouble(),any(Steppable.class));

            maximizer.step(model);


            //profit function
            when(firm.getPlantProfits(any(Plant.class))).thenReturn(
                    currentTarget < 10 ? (float) (currentTarget * currentTarget) : 0f
            );
            when(plant.getNumberOfWorkers()).thenReturn(currentTarget);
            when(control.getTarget()).thenReturn(currentTarget);


        }

        assertEquals(currentTarget, 9);




        for(int i=0; i<100; i++)
        {
            //we are being stepped each turn by the stepper!

            maximizer.step(model);
            System.out.println(currentTarget);

            //profit function
            when(firm.getPlantProfits(any(Plant.class))).thenReturn(
                    currentTarget < 10 ? 1f/((float)currentTarget) : currentTarget * currentTarget
            );
            when(plant.getNumberOfWorkers()).thenReturn(currentTarget);
            when(control.getTarget()).thenReturn(currentTarget);


        }

        assertEquals(currentTarget, 30);



    }




}
