/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package tests.plantcontrol;

import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.production.Plant;
import agents.firm.production.control.TargetAndMaximizePlantControl;
import agents.firm.production.control.maximizer.algorithms.hillClimbers.GradientMaximizer;
import ec.util.MersenneTwisterFast;
import model.MacroII;
import org.junit.Test;

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
 * @version 2012-09-24
 * @see
 */
public class GradientMaximizerTest {

    /**
     * Profits are getNumberOfWorkers^2
     */
    @Test
    public void scenario1Test()
    {

        HumanResources hr = mock(HumanResources.class);
        TargetAndMaximizePlantControl control = mock(TargetAndMaximizePlantControl.class);
        Plant plant = mock(Plant.class);
        Firm firm = mock(Firm.class);
        when(control.getPlant()).thenReturn(plant);
        when(plant.maximumWorkersPossible()).thenReturn(30); when(plant.getBuildingCosts()).thenReturn(-1);   when(hr.getRandom()).thenReturn(new MersenneTwisterFast());
        when(plant.minimumWorkersNeeded()).thenReturn(1);
        when(hr.getFirm()).thenReturn(firm);
        when(firm.getModel()).thenReturn(new MacroII(1));
        when(control.getHr()).thenReturn(hr);
        when(hr.getPlant()).thenReturn(plant);




        //maximize!
        GradientMaximizer maximizer = new GradientMaximizer(1,1,30);

        //start the parameters
        int target = 1;
        float currentProfits = 1;
        int oldTarget = 0;
        float oldProfits = -1;

        for(int i=0; i < 100; i++)
        {
            int futureTarget =  maximizer.chooseWorkerTarget(target,currentProfits,-1,-1,-1,-1,oldTarget,oldProfits);
            float futureProfits = futureTarget*futureTarget;



            oldTarget=target; oldProfits = currentProfits;
            target = futureTarget; currentProfits = futureProfits;


        }

        assertEquals(target, 30);





    }

    /**
     * Profits are getNumberOfWorkers^2 for x<10 and 0 otherwise!
     */
    @Test
    public void scenario2Test()
    {

        HumanResources hr = mock(HumanResources.class);
        TargetAndMaximizePlantControl control = mock(TargetAndMaximizePlantControl.class);
        Plant plant = mock(Plant.class);
        Firm firm = mock(Firm.class);
        when(control.getPlant()).thenReturn(plant);
        when(plant.maximumWorkersPossible()).thenReturn(30); when(plant.getBuildingCosts()).thenReturn(-1);
        when(plant.minimumWorkersNeeded()).thenReturn(1);
        when(hr.getFirm()).thenReturn(firm);
        when(firm.getModel()).thenReturn(new MacroII(1));
        when(control.getHr()).thenReturn(hr);
        when(hr.getPlant()).thenReturn(plant);




        //maximize!
        GradientMaximizer maximizer = new GradientMaximizer(0,1,30);

        //start the parameters
        int target = 1;
        float currentProfits = 1;
        int oldTarget = 0;
        float oldProfits = -1;

        for(int i=0; i < 100; i++)
        {
            int futureTarget =  maximizer.chooseWorkerTarget(target,currentProfits,-1,-1,-1,-1,oldTarget,oldProfits);
            float futureProfits = futureTarget < 10 ? futureTarget*futureTarget : 0;

            System.out.println(target);


            oldTarget=target; oldProfits = currentProfits;
            target = futureTarget; currentProfits = futureProfits;


        }

        assertEquals(target, 9);





    }

    /**
     * Profits are getNumberOfWorkers^2 for x<10 and 0 otherwise for the first 100 steps and just x^2 afterwards
     */
    @Test
    public void scenario3Test()
    {

        HumanResources hr = mock(HumanResources.class);
        TargetAndMaximizePlantControl control = mock(TargetAndMaximizePlantControl.class);
        Plant plant = mock(Plant.class);
        Firm firm = mock(Firm.class);
        when(control.getPlant()).thenReturn(plant);
        when(plant.maximumWorkersPossible()).thenReturn(30); when(plant.getBuildingCosts()).thenReturn(1);
        when(plant.minimumWorkersNeeded()).thenReturn(1);
        when(hr.getFirm()).thenReturn(firm);
        when(firm.getModel()).thenReturn(new MacroII(1));
        when(control.getHr()).thenReturn(hr);
        when(hr.getPlant()).thenReturn(plant);




        //maximize!
        GradientMaximizer maximizer = new GradientMaximizer(0,1,30);

        //start the parameters
        int target = 1;
        float currentProfits = 1;
        int oldTarget = 0;
        float oldProfits = -1;

        for(int i=0; i < 100; i++)
        {
            int futureTarget =  maximizer.chooseWorkerTarget(target,currentProfits,-1,-1,-1,-1,oldTarget,oldProfits);
            float futureProfits = futureTarget < 10 ? futureTarget*futureTarget : 0;

            oldTarget=target; oldProfits = currentProfits;
            target = futureTarget; currentProfits = futureProfits;



        }

        assertEquals(target, 9);

        for(int i=0; i < 100; i++)
        {
            int futureTarget =  maximizer.chooseWorkerTarget(target,currentProfits,-1,-1,-1,-1,oldTarget,oldProfits);
            float futureProfits = futureTarget < 10 ? 1f/((float)futureTarget) : futureTarget * futureTarget;

            oldTarget=target; oldProfits = currentProfits;
            target = futureTarget; currentProfits = futureProfits;


        }
        assertEquals(target, 1);





    }
    
    
}
