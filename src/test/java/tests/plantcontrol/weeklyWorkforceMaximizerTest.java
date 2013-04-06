package tests.plantcontrol;

import agents.firm.Firm;
import agents.firm.cost.PlantCostStrategy;
import agents.firm.personell.HumanResources;
import agents.firm.production.Plant;
import agents.firm.production.control.TargetAndMaximizePlantControl;
import agents.firm.production.control.maximizer.WeeklyWorkforceMaximizer;
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
     * Profits are workerSize^2
     */
    @Test
    public void scenario1Test()
    {

        currentTarget = 0;

        //model
        MacroII model = new MacroII(1l);



        HumanResources hr = mock(HumanResources.class);
        TargetAndMaximizePlantControl control = mock(TargetAndMaximizePlantControl.class);
        Plant plant = mock(Plant.class);  when(plant.getModel()).thenReturn(model);
        Firm firm = mock(Firm.class);
        when(control.getPlant()).thenReturn(plant);
        when(hr.getPlant()).thenReturn(plant);
        when(plant.maximumWorkersPossible()).thenReturn(30); when(plant.getBuildingCosts()).thenReturn(100l);
        when(plant.minimumWorkersNeeded()).thenReturn(1);
        when(hr.getFirm()).thenReturn(firm);
        when(hr.getPlant()).thenReturn(plant);
        when(firm.getModel()).thenReturn(model);
        when(firm.getRandom()).thenReturn(model.random);
        when(control.getHr()).thenReturn(hr);
        PlantCostStrategy strategy = mock(PlantCostStrategy.class);
        when(plant.getCostStrategy()).thenReturn(strategy);
        when(strategy.weeklyFixedCosts()).thenReturn(100l);




        //make the setter change the field
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                currentTarget = (Integer) invocation.getArguments()[0];
                return null;
            }
        }).when(control).setTarget(anyInt());




        //maximize!
        WeeklyWorkforceMaximizer<HillClimberMaximizer> maximizer = new WeeklyWorkforceMaximizer<>(hr,control,HillClimberMaximizer.class);

        maximizer.start();
        when(plant.workerSize()).thenReturn(currentTarget);
        when(control.getTarget()).thenReturn(currentTarget);

        for(int i=0; i<100; i++)
        {
            //we are being stepped each turn by the stepper!

            maximizer.step(model);


            //profit function
            when(firm.getPlantProfits(any(Plant.class))).thenReturn((float)
                    (currentTarget * currentTarget)
            );
            when(plant.workerSize()).thenReturn(currentTarget);
            when(control.getTarget()).thenReturn(currentTarget);


        }

        assertEquals(currentTarget, 30);




    }


    /**
     * Profits are workerSize^2
     */
    @Test
    public void scenario2Test()
    {

        currentTarget = 0;

        //model
        MacroII model = new MacroII(1l);
        Schedule schedule = mock(Schedule.class);
        model.schedule =  schedule;


        HumanResources hr = mock(HumanResources.class);
        TargetAndMaximizePlantControl control = mock(TargetAndMaximizePlantControl.class);
        Plant plant = mock(Plant.class);  when(plant.getModel()).thenReturn(model);
        Firm firm = mock(Firm.class);
        when(control.getPlant()).thenReturn(plant);
        when(hr.getPlant()).thenReturn(plant);
        when(plant.maximumWorkersPossible()).thenReturn(30); when(plant.getBuildingCosts()).thenReturn(100l);
        when(plant.minimumWorkersNeeded()).thenReturn(1);
        when(hr.getFirm()).thenReturn(firm);
        when(hr.getPlant()).thenReturn(plant);
        when(control.getHr()).thenReturn(hr);
        when(firm.getModel()).thenReturn(model);
        when(firm.getRandom()).thenReturn(model.random);
        PlantCostStrategy strategy = mock(PlantCostStrategy.class);
        when(plant.getCostStrategy()).thenReturn(strategy);
        when(strategy.weeklyFixedCosts()).thenReturn(100l);




        //make the setter change the field
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                currentTarget = (Integer) invocation.getArguments()[0];
                return null;
            }
        }).when(control).setTarget(anyInt());




        //maximize!
        WeeklyWorkforceMaximizer<HillClimberMaximizer> maximizer = new WeeklyWorkforceMaximizer<>(hr,control,
                HillClimberMaximizer.class);

        maximizer.start();
        when(plant.workerSize()).thenReturn(currentTarget);
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
            when(plant.workerSize()).thenReturn(currentTarget);
            when(control.getTarget()).thenReturn(currentTarget);


        }

        assertEquals(currentTarget, 9);




    }



    /**
     * Profits are workerSize^2
     */
    @Test
    public void scenario3Test()
    {
        currentTarget = 0;


        //model
        MacroII model = new MacroII(1l);
        Schedule schedule = mock(Schedule.class);
        model.schedule =  schedule;


        HumanResources hr = mock(HumanResources.class);
        TargetAndMaximizePlantControl control = mock(TargetAndMaximizePlantControl.class);
        Plant plant = mock(Plant.class);  when(plant.getModel()).thenReturn(model);
        Firm firm = mock(Firm.class);
        when(control.getPlant()).thenReturn(plant);
        when(hr.getPlant()).thenReturn(plant); when(hr.getRandom()).thenReturn(new MersenneTwisterFast(1));
        when(plant.maximumWorkersPossible()).thenReturn(30); when(plant.weeklyFixedCosts()).thenReturn(100l);
        when(plant.minimumWorkersNeeded()).thenReturn(1);
        when(hr.getFirm()).thenReturn(firm);
        when(firm.getModel()).thenReturn(model);
        when(firm.getRandom()).thenReturn(model.random);
        when(control.getHr()).thenReturn(hr);
        PlantCostStrategy strategy = mock(PlantCostStrategy.class);
        when(plant.getCostStrategy()).thenReturn(strategy);
        when(strategy.weeklyFixedCosts()).thenReturn(100l);





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
        WeeklyWorkforceMaximizer<AnnealingReactingMaximizer> maximizer =
                new WeeklyWorkforceMaximizer<>(hr,control,AnnealingReactingMaximizer.class);

        maximizer.start();
        when(plant.workerSize()).thenReturn(currentTarget);
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
            when(plant.workerSize()).thenReturn(currentTarget);
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
            when(plant.workerSize()).thenReturn(currentTarget);
            when(control.getTarget()).thenReturn(currentTarget);


        }

        assertEquals(currentTarget, 30);



    }




}
