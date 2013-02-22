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


    //marginal efficency is above 1: increase production
    @Test
    public void marginalEfficencyIsAbove1()
    {
        float oldRevenue = 100;
        float newRevenue = 120;
        float oldCosts = 10;
        float newCosts = 15;

        //marginal efficency is 4!!!!!!!!!!!!!!

        //build the various items and the maximizer
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
        PIDHillClimber maximizer = new PIDHillClimber(hr,control,2,1,0); //big numbers so that there is an effect!
        //assume you had moved from 1 to 2
        int newWorkerTarget = maximizer.chooseWorkerTarget(2,newRevenue-newCosts,newRevenue,newCosts,oldRevenue,oldCosts,
                1,oldRevenue-oldCosts);
        Assert.assertTrue(newWorkerTarget > 2);
        //check that it doesn't break with division by 0
        oldCosts = newCosts;
        newWorkerTarget = maximizer.chooseWorkerTarget(2,newRevenue-newCosts,newRevenue,newCosts,oldRevenue,oldCosts,
                1,oldRevenue-oldCosts);
        Assert.assertTrue(newWorkerTarget > 2);



    }


    //marginal efficency is below 1: decrease production
    @Test
    public void marginalEfficencyIsBelow1()
    {
        float oldRevenue = 100;
        float newRevenue = 105;
        float oldCosts = 10;
        float newCosts = 20;

        //marginal efficency is 0.25!!!!!!!!!!!!!!

        //build the various items and the maximizer
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
        PIDHillClimber maximizer = new PIDHillClimber(hr,control);
        //assume you had moved from 10 to 11
        int newWorkerTarget = maximizer.chooseWorkerTarget(11,newRevenue-newCosts,newRevenue,newCosts,oldRevenue,oldCosts,
                10,oldRevenue-oldCosts);
        Assert.assertTrue(newWorkerTarget < 11);



    }

    @Test
    public void monopolistScenario()
    {


        //build the various items and the maximizer
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
        PIDHillClimber maximizer = new PIDHillClimber(hr,control);
        //assume you had moved from 10 to 11


        long oldRevenue=0;
        long oldCosts = 0;
        long oldProfits = 0;
        int oldTarget = 0;
        int currentWorkerTarget = 1;
        //start the parameters
        float currentProfits = 1;

        for(int i=0; i < 1000; i++)
        {

            int futureTarget =  maximizer.chooseWorkerTarget(currentWorkerTarget,
                    revenuePerWorker(currentWorkerTarget) - costPerWorker(currentWorkerTarget),
                    revenuePerWorker(currentWorkerTarget),costPerWorker(currentWorkerTarget),
                    oldRevenue,oldCosts, oldTarget,oldProfits);

            oldTarget=currentWorkerTarget;
            oldProfits = revenuePerWorker(oldTarget) - costPerWorker(oldTarget);
            oldRevenue = revenuePerWorker(oldTarget);
            oldCosts = costPerWorker(oldTarget);
            currentWorkerTarget=futureTarget;
            System.out.println(futureTarget);




        }


    }


    //use the functions from the monopolist

    private long costPerWorker(int workers)
    {
        long wages;
        if(workers > 0)
            wages = 105 + (workers -1)*7;
        else
            wages = 0;

        return wages*workers;

    }

    private long revenuePerWorker(int workers)
    {
        int quantity = workers * 7;
        int price = 101 - workers;

        return quantity * price;
    }







}
