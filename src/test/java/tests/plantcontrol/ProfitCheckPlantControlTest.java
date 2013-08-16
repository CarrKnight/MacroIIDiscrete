package tests.plantcontrol;

import agents.Person;
import agents.firm.Firm;
import agents.firm.WeeklyProfitReport;
import agents.firm.cost.InputCostStrategy;
import agents.firm.personell.HumanResources;
import agents.firm.production.Blueprint;
import agents.firm.production.Plant;
import agents.firm.production.control.TargetAndMaximizePlantControl;
import agents.firm.production.control.facades.ProfitCheckPlantControl;
import agents.firm.production.control.targeter.PIDTargeter;
import agents.firm.production.technology.IRSExponentialMachinery;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentAllAtOnce;
import financial.market.Market;
import financial.market.OrderBookBlindMarket;
import goods.GoodType;
import model.MacroII;
import model.utilities.NonDrawable;
import org.junit.Test;
import sim.engine.Schedule;
import sim.engine.Steppable;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
 * @version 2012-08-25
 * @see
 */
public class ProfitCheckPlantControlTest {


    @Test
    public void nonDrawable(){

        //make sure retention policy works
        assertTrue(TargetAndMaximizePlantControl.class.isAnnotationPresent(NonDrawable.class));


    }


    @Test
    public void hiringTest() throws NoSuchFieldException, IllegalAccessException {
        Market.TESTING_MODE = true;


        System.out.println("--------------------------------------------------------------------------------------");

        MacroII model = new MacroII(10l);
        Firm firm = new Firm(model); firm.earn(1000000000000l);
        Plant p = new Plant(Blueprint.simpleBlueprint(GoodType.GENERIC, 1, GoodType.GENERIC, 1),firm);
        p.setPlantMachinery(new IRSExponentialMachinery(GoodType.CAPITAL,firm,10,p,1f));
        p.setCostStrategy(new InputCostStrategy(p));

        firm.addPlant(p);



        model.schedule = mock(Schedule.class);  //give it a fake schedule; this way we control time!
        final List<Steppable> steppableList = new LinkedList<>();

        //make this always profitable
        SalesDepartment dept = mock(SalesDepartmentAllAtOnce.class);
        when(dept.getLastClosingCost()).thenReturn(1l);
        when(dept.getLastClosingPrice()).thenReturn(2l);
        when(dept.getSoldPercentage()).thenReturn(.7f);
        //should be profitable
        firm.registerSaleDepartment(dept,GoodType.GENERIC);




        Market market = new OrderBookBlindMarket(GoodType.LABOR);
        assertEquals(p.maximumWorkersPossible(),100);
        HumanResources humanResources = HumanResources.getHumanResourcesIntegrated(10000000,firm,market,
                p,ProfitCheckPlantControl.class,null,null).getDepartment(); //create!!!

        Field field = PurchasesDepartment.class.getDeclaredField("control");
        field.setAccessible(true);
        ProfitCheckPlantControl control = (ProfitCheckPlantControl) field.get(humanResources);

        for(int i=0; i < 200; i++){
            Person worker = new Person(model,0,i,market);
            worker.start(model);
        }
        //make them search for job

        model.getPhaseScheduler().step(model);

        //there should be 200 asks
        assertEquals(200, market.getSellers().size());
        assertEquals(1,market.getBuyers().size());  //the human resources should be the only one registerd
        assertTrue(market.getBuyers().contains(humanResources.getFirm()));

        //check before start
        assertTrue(!steppableList.contains(control));
        assertTrue(p.workerSize()==0);

        //start the human resources
        humanResources.start();
        //some stuff might have happened, but surely the control should have called "schedule in"
        assertTrue(control.toString(),!steppableList.contains(control));
//        assertTrue(p.workerSize() > 0);


        WeeklyProfitReport profits = mock(WeeklyProfitReport.class);
        firm.setProfitReport(profits);


        //now "adjust" 5000 times
        for(int i=0; i < 5000; i++)
        {

            when(profits.getPlantProfits(p)).thenReturn(p.workerSize() * 2f);

            //put the stuff to adjust in its own list
            Set<Steppable> toStep = new HashSet<>(steppableList);
            steppableList.clear();
            int oldTarget = control.getTarget();
            long oldWage = humanResources.maxPrice(GoodType.LABOR,market);


            //notice that this is un-natural as profitStep occurs only once every 3 pid steps in reality
            model.getPhaseScheduler().step(model);


            long newWage = humanResources.maxPrice(GoodType.LABOR,market);
            System.out.println("old wage:" + oldWage +" , new wage: " + newWage + " , worker size: " + p.workerSize() + ", old target: " + oldTarget + ", new target: " + control.getTarget());


//            assertTrue(steppableList.contains(control));



            //make sure it is adjusting in the right direction
            /*       assertTrue((p.workerSize() <= oldTarget && newWage >= oldWage) ||       // this controller is very imprecise
(p.workerSize() >= oldTarget && newWage <= oldWage) ||
(p.workerSize() == oldTarget && newWage == oldWage) || i<10);
            */


        }
        System.out.println("--------------------------------------------------------------------------------------");

        assertTrue(p.workerSize() == 100);






    }


    @Test
    public void hiringTestFromAbove() throws NoSuchFieldException, IllegalAccessException {
        Market.TESTING_MODE = true;

        MacroII model = new MacroII(10l);
        Firm firm = new Firm(model); firm.earn(1000000000000l);
        Plant p = new Plant(Blueprint.simpleBlueprint(GoodType.GENERIC, 1, GoodType.GENERIC, 1),firm);
        p.setPlantMachinery(new IRSExponentialMachinery(GoodType.CAPITAL,firm,10,p,1f));
        p.setCostStrategy(new InputCostStrategy(p));

        firm.addPlant(p);




        model.schedule = mock(Schedule.class);  //give it a fake schedule; this way we control time!
        final List<Steppable> steppableList = new LinkedList<>();

        //make this always profitable
        SalesDepartment dept = mock(SalesDepartmentAllAtOnce.class);
        //should be profitable
        firm.registerSaleDepartment(dept, GoodType.GENERIC);


        System.out.println("--------------------------------------------------------------------------------------");







        Market market = new OrderBookBlindMarket(GoodType.LABOR);
        assertEquals(p.maximumWorkersPossible(),100);
        HumanResources humanResources = HumanResources.getHumanResourcesIntegrated(10000000,
                firm,market,p,ProfitCheckPlantControl.class,null,null).getDepartment(); //create!!!
//        firm.registerHumanResources(p,humanResources);

        Field field = PurchasesDepartment.class.getDeclaredField("control");
        field.setAccessible(true);
        ProfitCheckPlantControl control = (ProfitCheckPlantControl) field.get(humanResources);


        field = ProfitCheckPlantControl.class.getDeclaredField("control"); field.setAccessible(true);
        TargetAndMaximizePlantControl innerControl = (TargetAndMaximizePlantControl)field.get(control);

        field = TargetAndMaximizePlantControl.class.getDeclaredField("targeter"); field.setAccessible(true);
        PIDTargeter targeter = (PIDTargeter) field.get(innerControl);
        targeter.setInitialWage(200);


        for(int i=0; i < 200; i++){
            Person worker = new Person(model,0,i,market);
            worker.start(model);
        }
        //make them search for job
        ArrayList<Steppable> todaySteppables = new ArrayList<>(steppableList);
        steppableList.clear();
        model.getPhaseScheduler().step(model);

        //there should be 200 asks
   //     System.out.println("what?");

        assertEquals(200, market.getSellers().size());
        assertEquals(1,market.getBuyers().size());  //the human resources should be the only one registerd
        assertTrue(market.getBuyers().contains(humanResources.getFirm()));

        //check before start
        //assertEquals(steppableList.size(),0);
        assertTrue(control.toString(),!steppableList.contains(control));
        assertTrue(p.workerSize()==0);

        //start the human resources
        humanResources.start();
        //some stuff might have happened, but surely the control should have called "schedule in"
        assertTrue(control.toString(),!steppableList.contains(control));
     //   assertTrue(p.workerSize() > 0);

        WeeklyProfitReport profits = mock(WeeklyProfitReport.class);
        firm.setProfitReport(profits);


        //now "adjust" 5000 times
        for(int i=0; i < 5000; i++)
        {
            //always profitable
            when(profits.getPlantProfits(p)).thenReturn(p.workerSize() * 2f);

            //put the stuff to adjust in its own list
            Set<Steppable> toStep = new HashSet<>(steppableList);
            steppableList.clear();
            int oldTarget = control.getTarget();
            long oldWage = humanResources.maxPrice(GoodType.LABOR,market);


            //notice that this is un-natural as profitStep occurs only once every 3 pid steps in reality
            model.getPhaseScheduler().step(model);


            long newWage = humanResources.maxPrice(GoodType.LABOR,market);
            System.out.println("old wage:" + oldWage +" , new wage: " + newWage + " , worker size: " + p.workerSize() + ", old target: " + oldTarget + ", new target: " +
                    control.getTarget());


            assertTrue(control.toString(),!steppableList.contains(control));
            System.out.println(p.workerSize());


            //make sure it is adjusting in the right direction
            /*       assertTrue((p.workerSize() <= oldTarget && newWage >= oldWage) ||       // this controller is very imprecise
(p.workerSize() >= oldTarget && newWage <= oldWage) ||
(p.workerSize() == oldTarget && newWage == oldWage) || i<10);
            */

        }

        System.out.println("--------------------------------------------------------------------------------------");

        assertTrue(p.workerSize() == 100);






    }


}
