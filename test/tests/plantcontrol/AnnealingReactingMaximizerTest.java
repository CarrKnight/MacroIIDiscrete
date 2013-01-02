package tests.plantcontrol;

import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import goods.production.Plant;
import goods.production.control.TargetAndMaximizePlantControl;
import goods.production.control.maximizer.AnnealingReactingMaximizer;
import junit.framework.Assert;
import model.MacroII;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
public class AnnealingReactingMaximizerTest {


    //these are exactly the hill climber maximizer tests. Simulated annealing should just be slower but get to the same point

    /**
     * Profits are workerSize^2
     */
    @Test
    public void scenario1Test()
    {

        HumanResources hr = mock(HumanResources.class);
        TargetAndMaximizePlantControl control = mock(TargetAndMaximizePlantControl.class);
        Plant plant = mock(Plant.class);
        Firm firm = mock(Firm.class);
        when(control.getPlant()).thenReturn(plant);
        when(plant.maximumWorkersPossible()).thenReturn(30); when(plant.getBuildingCosts()).thenReturn(-1l);
        when(plant.minimumWorkersNeeded()).thenReturn(1);
        when(hr.getFirm()).thenReturn(firm);
        MacroII model = new MacroII(1l);
        when(firm.getModel()).thenReturn(model);
        when(hr.getPlant()).thenReturn(plant);
        when(firm.getRandom()).thenReturn(model.random);
        when(control.getHr()).thenReturn(hr);




        //maximize!
        AnnealingReactingMaximizer maximizer = new AnnealingReactingMaximizer(hr,control);

        //start the parameters
        int target = 1;
        float currentProfits = 1;
        int oldTarget = 0;
        float oldProfits = -1;

        for(int i=0; i < 100; i++)
        {
            int futureTarget = maximizer.chooseWorkerTarget(target,currentProfits,oldTarget,oldProfits);
            float futureProfits = futureTarget*futureTarget;

            oldTarget=target; oldProfits = currentProfits;
            target = futureTarget; currentProfits = futureProfits;


        }

        Assert.assertEquals(target, 30);





    }

    /**
     * Profits are workerSize^2 for x<10 and 0 otherwise!
     */
    @Test
    public void scenario2Test()
    {

        HumanResources hr = mock(HumanResources.class);
        TargetAndMaximizePlantControl control = mock(TargetAndMaximizePlantControl.class);
        Plant plant = mock(Plant.class);
        Firm firm = mock(Firm.class);
        when(control.getPlant()).thenReturn(plant);
        when(plant.maximumWorkersPossible()).thenReturn(30); when(plant.getBuildingCosts()).thenReturn(-1l);
        when(plant.minimumWorkersNeeded()).thenReturn(1);
        when(hr.getFirm()).thenReturn(firm);
        MacroII model = new MacroII(1l);
        when(firm.getModel()).thenReturn(model);
        when(hr.getPlant()).thenReturn(plant);
        when(firm.getRandom()).thenReturn(model.random);
        when(control.getHr()).thenReturn(hr);




        //maximize!
        AnnealingReactingMaximizer maximizer = new AnnealingReactingMaximizer(hr,control);

        //start the parameters
        int target = 1;
        float currentProfits = 1;
        int oldTarget = 0;
        float oldProfits = -1;

        for(int i=0; i < 100; i++)
        {
            int futureTarget = maximizer.chooseWorkerTarget(target,currentProfits,oldTarget,oldProfits);
            float futureProfits = futureTarget < 10 ? futureTarget*futureTarget : 0;

            oldTarget=target; oldProfits = currentProfits;
            target = futureTarget; currentProfits = futureProfits;


        }

        Assert.assertEquals(target,9);





    }

    /**
     * Profits are workerSize^2 for x<10 and 0 otherwise for the first 100 steps and just x^2 afterwards
     */
    @Test
    public void scenario3Test()
    {

        HumanResources hr = mock(HumanResources.class);
        TargetAndMaximizePlantControl control = mock(TargetAndMaximizePlantControl.class);
        Plant plant = mock(Plant.class);
        Firm firm = mock(Firm.class);
        when(control.getPlant()).thenReturn(plant);
        when(plant.maximumWorkersPossible()).thenReturn(30); when(plant.getBuildingCosts()).thenReturn(-1l);
        when(plant.minimumWorkersNeeded()).thenReturn(1);
        when(hr.getFirm()).thenReturn(firm);
        MacroII model = new MacroII(1l);
        when(firm.getModel()).thenReturn(model);
        when(hr.getPlant()).thenReturn(plant);
        when(control.getHr()).thenReturn(hr);
        when(firm.getRandom()).thenReturn(model.random);


        //maximize!
        AnnealingReactingMaximizer maximizer = new AnnealingReactingMaximizer(hr,control);

        //start the parameters
        int target = 1;
        float currentProfits = 1;
        int oldTarget = 0;
        float oldProfits = -1;

        for(int i=0; i < 100; i++)
        {
            int futureTarget = maximizer.chooseWorkerTarget(target,currentProfits,oldTarget,oldProfits);
            float futureProfits = futureTarget < 10 ? futureTarget*futureTarget : 0;

            oldTarget=target; oldProfits = currentProfits;
            target = futureTarget; currentProfits = futureProfits;


        }

        Assert.assertEquals(target,9);

        for(int i=0; i < 100; i++)
        {
            int futureTarget = maximizer.chooseWorkerTarget(target,currentProfits,oldTarget,oldProfits);
            float futureProfits = futureTarget < 10 ? 1f/((float)futureTarget) : futureTarget * futureTarget;
       //     System.out.println(target);
            oldTarget=target; oldProfits = currentProfits;
            target = futureTarget; currentProfits = futureProfits;


        }

        //THIS SHOULD WORK. Unfortunately probably not every time!!
        Assert.assertEquals(target,30);





    }
    
    
}
