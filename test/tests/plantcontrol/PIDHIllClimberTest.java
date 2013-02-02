package tests.plantcontrol;

import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.production.Plant;
import agents.firm.production.control.TargetAndMaximizePlantControl;
import agents.firm.production.control.maximizer.PIDHillClimber;
import junit.framework.Assert;
import model.MacroII;
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
 * @version 2013-02-02
 * @see
 */
public class PIDHIllClimberTest {

    /**
     * Profits are workerSize^2
     */
    @Test
    public void scenario1Test()
    {

        HumanResources hr = mock(HumanResources.class);
        TargetAndMaximizePlantControl control = mock(TargetAndMaximizePlantControl.class);
        Plant plant = mock(Plant.class);
        Firm firm = mock(Firm.class);  when(hr.getFirm()).thenReturn(firm);
        when(firm.getModel()).thenReturn(new MacroII(1l));
        when(control.getPlant()).thenReturn(plant);
        when(plant.maximumWorkersPossible()).thenReturn(30); when(plant.getBuildingCosts()).thenReturn(1l);
        when(plant.minimumWorkersNeeded()).thenReturn(1);
        when(hr.getFirm()).thenReturn(firm);
        when(firm.getModel()).thenReturn(new MacroII(1l));
        when(control.getHr()).thenReturn(hr); when(hr.maximumWorkersPossible()).thenReturn(30);
        when(hr.getPlant()).thenReturn(plant);


        //maximize!
        PIDHillClimber maximizer = new PIDHillClimber(hr,control);

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
        Firm firm = mock(Firm.class);  when(hr.getFirm()).thenReturn(firm);
        when(firm.getModel()).thenReturn(new MacroII(1l));
        when(control.getPlant()).thenReturn(plant);
        when(plant.maximumWorkersPossible()).thenReturn(30); when(plant.getBuildingCosts()).thenReturn(1l);
        when(plant.minimumWorkersNeeded()).thenReturn(1);
        when(hr.getFirm()).thenReturn(firm);
        when(firm.getModel()).thenReturn(new MacroII(1l));
        when(control.getHr()).thenReturn(hr); when(hr.maximumWorkersPossible()).thenReturn(30);
        when(hr.getPlant()).thenReturn(plant);




        //maximize!
        PIDHillClimber maximizer = new PIDHillClimber(hr,control);

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
            System.out.println(futureTarget);



        }

        Assert.assertEquals(target,9);





    }


    /**
     * Profits are -L^2+5L+2
     */
    @Test
    public void scenarioContinuousFunction()
    {

        HumanResources hr = mock(HumanResources.class);
        TargetAndMaximizePlantControl control = mock(TargetAndMaximizePlantControl.class);
        Plant plant = mock(Plant.class);
        Firm firm = mock(Firm.class);  when(hr.getFirm()).thenReturn(firm);
        when(firm.getModel()).thenReturn(new MacroII(1l));
        when(control.getPlant()).thenReturn(plant);
        when(plant.maximumWorkersPossible()).thenReturn(30); when(plant.getBuildingCosts()).thenReturn(1l);
        when(plant.minimumWorkersNeeded()).thenReturn(1);
        when(hr.getFirm()).thenReturn(firm);
        when(firm.getModel()).thenReturn(new MacroII(1l));
        when(control.getHr()).thenReturn(hr); when(hr.maximumWorkersPossible()).thenReturn(30);
        when(hr.getPlant()).thenReturn(plant);




        //maximize!
        PIDHillClimber maximizer = new PIDHillClimber(hr,control);

        //start the parameters
        int target = 1;
        float currentProfits = 1;
        int oldTarget = 0;
        float oldProfits = -1;

        for(int i=0; i < 100; i++)
        {
            int futureTarget = maximizer.chooseWorkerTarget(target,currentProfits,oldTarget,oldProfits);
            float futureProfits = -futureTarget*futureTarget + 5 * futureTarget +2;

            oldTarget=target; oldProfits = currentProfits;
            target = futureTarget; currentProfits = futureProfits;
            System.out.println(futureTarget);



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
        Firm firm = mock(Firm.class);  when(hr.getFirm()).thenReturn(firm);
        when(firm.getModel()).thenReturn(new MacroII(1l));
        when(control.getPlant()).thenReturn(plant);
        when(plant.maximumWorkersPossible()).thenReturn(30); when(plant.getBuildingCosts()).thenReturn(1l);
        when(plant.minimumWorkersNeeded()).thenReturn(1);
        when(hr.getFirm()).thenReturn(firm);
        when(firm.getModel()).thenReturn(new MacroII(1l));
        when(control.getHr()).thenReturn(hr); when(hr.maximumWorkersPossible()).thenReturn(30);
        when(hr.getPlant()).thenReturn(plant);
        when(hr.getPlant()).thenReturn(plant);





        //maximize!
        PIDHillClimber maximizer = new PIDHillClimber(hr,control);

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

            oldTarget=target; oldProfits = currentProfits;
            target = futureTarget; currentProfits = futureProfits;


        }
        Assert.assertEquals(target,1);





    }



}
