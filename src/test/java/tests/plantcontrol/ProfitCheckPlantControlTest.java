package tests.plantcontrol;

import agents.people.Person;
import agents.firm.Firm;
import agents.firm.cost.InputCostStrategy;
import agents.firm.personell.HumanResources;
import agents.firm.production.Blueprint;
import agents.firm.production.Plant;
import agents.firm.production.control.TargetAndMaximizePlantControl;
import agents.firm.production.control.facades.ProfitCheckPlantControl;
import agents.firm.production.control.targeter.PIDTargeterWithQuickFiring;
import agents.firm.production.technology.IRSExponentialMachinery;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.prediction.PricingPurchasesPredictor;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentAllAtOnce;
import agents.firm.sales.prediction.PricingSalesPredictor;
import agents.firm.utilities.ProfitReport;
import financial.market.ImmediateOrderHandler;
import financial.market.Market;
import financial.market.OrderBookBlindMarket;
import financial.market.OrderBookMarket;
import goods.DifferentiatedGoodType;
import goods.UndifferentiatedGoodType;
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

        MacroII model = new MacroII(10);
        Firm firm = new Firm(model); firm.receiveMany(UndifferentiatedGoodType.MONEY,1000000000);
        Plant p = new Plant(Blueprint.simpleBlueprint(UndifferentiatedGoodType.GENERIC, 1, UndifferentiatedGoodType.GENERIC, 1),firm);
        p.setPlantMachinery(new IRSExponentialMachinery(DifferentiatedGoodType.CAPITAL,firm,10,p,1f));
        p.setCostStrategy(new InputCostStrategy(p));

        firm.addPlant(p);



        model.schedule = mock(Schedule.class);  //give it a fake schedule; this way we control time!
        final List<Steppable> steppableList = new LinkedList<>();

        //make this always profitable
        SalesDepartment dept = mock(SalesDepartmentAllAtOnce.class);
        dept.setPredictorStrategy(new PricingSalesPredictor());
        when(dept.getLastClosingCost()).thenReturn(1);
        when(dept.getLastClosingPrice()).thenReturn(2);
        //should be profitable
        firm.registerSaleDepartment(dept, UndifferentiatedGoodType.GENERIC);




        OrderBookMarket market = new OrderBookBlindMarket(UndifferentiatedGoodType.LABOR);
        market.setOrderHandler(new ImmediateOrderHandler(),model);
        assertEquals(p.maximumWorkersPossible(), 100);
        HumanResources humanResources = HumanResources.getHumanResourcesIntegrated(10000000,firm,market,
                p,ProfitCheckPlantControl.class,null,null).getDepartment(); //create!!!

        humanResources.setPredictor(new PricingPurchasesPredictor());

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
        assertTrue(p.getNumberOfWorkers()==0);

        //start the human resources
        humanResources.start(model);
        //some stuff might have happened, but surely the control should have called "schedule in"
        assertTrue(control.toString(),!steppableList.contains(control));
//        assertTrue(p.getNumberOfWorkers() > 0);


        ProfitReport profits = mock(ProfitReport.class);
        firm.setProfitReport(profits);


        //now "adjust" 5000 times
        for(int i=0; i < 5000; i++)
        {

            when(profits.getPlantProfits(p)).thenReturn(p.getNumberOfWorkers() * 2f);

            //put the stuff to adjust in its own list
            Set<Steppable> toStep = new HashSet<>(steppableList);
            steppableList.clear();
            int oldTarget = control.getTarget();
            int oldWage = humanResources.maxPrice(UndifferentiatedGoodType.LABOR,market);


            //notice that this is un-natural as profitStep occurs only once every 3 pid steps in reality
            model.getPhaseScheduler().step(model);


            int newWage = humanResources.maxPrice(UndifferentiatedGoodType.LABOR,market);
            System.out.println("old wage:" + oldWage +" , new wage: " + newWage + " , worker size: " + p.getNumberOfWorkers() + ", old target: " + oldTarget + ", new target: " + control.getTarget());


//            assertTrue(steppableList.contains(control));



            //make sure it is adjusting in the right direction
            /*       assertTrue((p.getNumberOfWorkers() <= oldTarget && newWage >= oldWage) ||       // this controller is very imprecise
(p.getNumberOfWorkers() >= oldTarget && newWage <= oldWage) ||
(p.getNumberOfWorkers() == oldTarget && newWage == oldWage) || i<10);
            */


        }
        System.out.println("--------------------------------------------------------------------------------------");

        assertTrue(p.getNumberOfWorkers() == 100);






    }


    @Test
    public void hiringTestFromAbove() throws NoSuchFieldException, IllegalAccessException {
        Market.TESTING_MODE = true;

        MacroII model = new MacroII(10);
        Firm firm = new Firm(model); firm.receiveMany(UndifferentiatedGoodType.MONEY,1000000000);
        Plant p = new Plant(Blueprint.simpleBlueprint(UndifferentiatedGoodType.GENERIC, 1, UndifferentiatedGoodType.GENERIC, 1),firm);
        p.setPlantMachinery(new IRSExponentialMachinery(DifferentiatedGoodType.CAPITAL,firm,10,p,1f));
        p.setCostStrategy(new InputCostStrategy(p));

        firm.addPlant(p);




        model.schedule = mock(Schedule.class);  //give it a fake schedule; this way we control time!
        final List<Steppable> steppableList = new LinkedList<>();

        //make this always profitable
        SalesDepartment dept = mock(SalesDepartmentAllAtOnce.class);
        //should be profitable
        firm.registerSaleDepartment(dept, UndifferentiatedGoodType.GENERIC);


        System.out.println("--------------------------------------------------------------------------------------");







        OrderBookMarket market = new OrderBookBlindMarket(UndifferentiatedGoodType.LABOR);
        market.setOrderHandler(new ImmediateOrderHandler(),model);
        assertEquals(p.maximumWorkersPossible(), 100);
        HumanResources humanResources = HumanResources.getHumanResourcesIntegrated(10000000,
                firm,market,p,ProfitCheckPlantControl.class,null,null).getDepartment(); //create!!!
//        firm.registerHumanResources(p,humanResources);

        Field field = PurchasesDepartment.class.getDeclaredField("control");
        field.setAccessible(true);
        ProfitCheckPlantControl control = (ProfitCheckPlantControl) field.get(humanResources);


        field = ProfitCheckPlantControl.class.getDeclaredField("control"); field.setAccessible(true);
        TargetAndMaximizePlantControl innerControl = (TargetAndMaximizePlantControl)field.get(control);

        field = TargetAndMaximizePlantControl.class.getDeclaredField("targeter"); field.setAccessible(true);
        PIDTargeterWithQuickFiring targeter = (PIDTargeterWithQuickFiring) field.get(innerControl);
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
        assertTrue(p.getNumberOfWorkers()==0);

        //start the human resources
        humanResources.setPredictor(new PricingPurchasesPredictor());
        humanResources.start(model);
        //some stuff might have happened, but surely the control should have called "schedule in"
        assertTrue(control.toString(),!steppableList.contains(control));
     //   assertTrue(p.getNumberOfWorkers() > 0);

        ProfitReport profits = mock(ProfitReport.class);
        firm.setProfitReport(profits);


        //now "adjust" 5000 times
        for(int i=0; i < 5000; i++)
        {
            //always profitable
            when(profits.getPlantProfits(p)).thenReturn(p.getNumberOfWorkers() * 2f);

            //put the stuff to adjust in its own list
            Set<Steppable> toStep = new HashSet<>(steppableList);
            steppableList.clear();
            int oldTarget = control.getTarget();
            int oldWage = humanResources.maxPrice(UndifferentiatedGoodType.LABOR,market);


            //notice that this is un-natural as profitStep occurs only once every 3 pid steps in reality
            model.getPhaseScheduler().step(model);


            int newWage = humanResources.maxPrice(UndifferentiatedGoodType.LABOR,market);
            System.out.println("old wage:" + oldWage +" , new wage: " + newWage + " , worker size: " + p.getNumberOfWorkers() + ", old target: " + oldTarget + ", new target: " +
                    control.getTarget());


            assertTrue(control.toString(),!steppableList.contains(control));
            System.out.println(p.getNumberOfWorkers());


            //make sure it is adjusting in the right direction
            /*       assertTrue((p.getNumberOfWorkers() <= oldTarget && newWage >= oldWage) ||       // this controller is very imprecise
(p.getNumberOfWorkers() >= oldTarget && newWage <= oldWage) ||
(p.getNumberOfWorkers() == oldTarget && newWage == oldWage) || i<10);
            */

        }

        System.out.println("--------------------------------------------------------------------------------------");

        assertTrue(p.getNumberOfWorkers() == 100);






    }


}
