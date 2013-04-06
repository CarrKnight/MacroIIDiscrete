package tests;

import agents.Person;
import agents.firm.Firm;
import agents.firm.cost.DirectCosts;
import agents.firm.personell.HumanResources;
import agents.firm.production.Blueprint;
import agents.firm.production.Plant;
import agents.firm.production.technology.Machinery;
import goods.GoodType;
import model.MacroII;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * <h4>Description</h4>
 * <p/> TEST TEST TEST
 *
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-24
 * @see
 */
public class StandardPlantCostStrategyTest {

    @Test
    public void initialTest(){

        Plant p = mock(Plant.class);
        Blueprint b = new Blueprint.Builder().input(GoodType.GENERIC,2).output(GoodType.CAPITAL,4).build();


        List<Person> fakeRoster = new LinkedList<>();
        when(p.getWorkers()).thenReturn(fakeRoster);
        when(p.workerSize()).thenReturn(fakeRoster.size());
        when(p.getBlueprint()).thenReturn(b);
        when(p.getOutputMultiplier(any(GoodType.class))).thenReturn(1f);

        HumanResources hr = mock(HumanResources.class); when(p.getHr()).thenReturn(hr);
        when(hr.getWagesPaid()).thenReturn(0l);



        DirectCosts strategy = new DirectCosts(p);
        assertEquals(strategy.getTotalProductionPerRun(), 4);
        assertEquals(strategy.getWageCostsPerUnit(), 0); //there is no worker
        assertEquals(strategy.unitOutputCost(GoodType.CAPITAL, 400), 100);


        boolean exceptionThrown = false;
        try{
            assertEquals(strategy.unitOutputCost(GoodType.GENERIC, 400), 0);
            fail(); //should trow an exception
        }
        catch (IllegalArgumentException e){
            exceptionThrown=true;

        }
        assertTrue(exceptionThrown);

        //addSalesDepartmentListener 3 workers. Each of them paid 3
        for(int i=0; i < 3; i++)
        {
            Person w = mock(Person.class);
            when(w.getWage()).thenReturn(3l);
            fakeRoster.add(w); //addSalesDepartmentListener to roster
        }

        when(p.expectedWeeklyProductionRuns()).thenReturn(1f);

        //it still shouldn't have updated because the listener wasn't fired
        assertEquals(strategy.getWageCostsPerUnit(), 0); //there is no worker
        when(hr.getWagesPaid()).thenReturn(3l * fakeRoster.size());
        when(p.hypotheticalTotalThroughput(anyInt())).thenReturn(4f);
        strategy.changeInWorkforceEvent(p,3);
        //get wages paid
        assertEquals(strategy.getWageCostsPerUnit(), Math.round(3f * 3f / 4f)); // 3 workers paid 3, divided by 4 (weekly production rate)

        when(p.hypotheticalTotalThroughput(anyInt())).thenReturn(8f);
        strategy.changeInWorkforceEvent(p,3);
        assertEquals(strategy.getWageCostsPerUnit(), Math.round(3f * 3f / 8f)); // 3 workers paid 3, divided by 8 (weekly production rate)



        //change the blue print and see if it has any effect
        when(p.hypotheticalTotalThroughput(anyInt())).thenReturn(12f);
        strategy.changeInMachineryEvent(p,null);
        assertEquals(strategy.getWageCostsPerUnit(), Math.round(3f * 3f / 12f)); // 3 workers paid 3, divided by 12 (weekly production rate : 4+2 produced twice)






    }


    @Test
    public void BetterDressedTest(){

        Blueprint b = new Blueprint.Builder().input(GoodType.GENERIC,2).output(GoodType.CAPITAL,4).build();
        Firm f = mock(Firm.class);
        when(f.getModel()).thenReturn(new MacroII(1l));
        final Plant p = new Plant(b,f);
        HumanResources hr = mock(HumanResources.class);
        when(f.getHR(p)).thenReturn(hr);
        when(hr.getWagesPaid()).then(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return p.workerSize() * 3f;

            }
        });

        //give it a machinery
        Machinery machinery = mock(Machinery.class);
        when(machinery.getOutputMultiplier(any(GoodType.class))).thenReturn(1f);
        when(machinery.expectedWeeklyProductionRuns()).thenReturn(1f);
        when(machinery.hypotheticalTotalThroughput(anyInt())).thenReturn(1f);

        p.setPlantMachinery(machinery);


        DirectCosts strategy = new DirectCosts(p);
        p.setCostStrategy(strategy);

        assertEquals(strategy.getTotalProductionPerRun(), 4);
        assertEquals(strategy.getWageCostsPerUnit(), 0); //there is no worker
        assertEquals(strategy.unitOutputCost(GoodType.CAPITAL, 400), 100);
        boolean exceptionThrown = false;
        try{
            assertEquals(strategy.unitOutputCost(GoodType.GENERIC, 400), 0);
            fail(); //should trow an exception
        }
        catch (IllegalArgumentException e){
            exceptionThrown=true;

        }
        assertTrue(exceptionThrown);

        when(machinery.hypotheticalTotalThroughput(anyInt())).thenReturn(4f);
        //addSalesDepartmentListener 3 workers. Each of them paid 3
        for(int i=0; i < 3; i++)
        {
            Person w = mock(Person.class);
            when(w.getWage()).thenReturn(3l);
            p.addWorker(w);

        }

        //should have been automatically updated
        assertEquals(strategy.getWageCostsPerUnit(), Math.round(3f * 3f / 4f)); // 3 workers paid 3, divided by 4 (weekly production rate)

        //this isn't automatically listened because I am fudging the state manually
        when(machinery.hypotheticalTotalThroughput(anyInt())).thenReturn(8f);
        strategy.changeInWorkforceEvent(p,3);
        assertEquals(strategy.getWageCostsPerUnit(), Math.round(3f * 3f / 8f)); // 3 workers paid 3, divided by 4 (weekly production rate)


        when(machinery.hypotheticalTotalThroughput(anyInt())).thenReturn(12f);
        assertEquals(strategy.getWageCostsPerUnit(), Math.round(3f * 3f / 12f)); // 3 workers paid 3, divided by 12 (weekly production rate : 4+2 produced twice)




    }



}
