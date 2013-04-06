package tests.plantcontrol;

import agents.Person;
import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.production.Blueprint;
import agents.firm.production.Plant;
import agents.firm.production.control.TargetAndMaximizePlantControl;
import agents.firm.production.control.decorators.FixedWageDecorator;
import agents.firm.production.control.decorators.PlantControlDecorator;
import agents.firm.production.control.facades.FixWagesPlantControl;
import agents.firm.production.control.targeter.PIDTargeter;
import agents.firm.production.technology.IRSExponentialMachinery;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.sales.SalesDepartment;
import ec.util.MersenneTwisterFast;
import financial.Market;
import financial.OrderBookBlindMarket;
import goods.GoodType;
import model.MacroII;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import sim.engine.Schedule;
import sim.engine.Steppable;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * <h4>Description</h4>
 * <p/>  These tests used to be run for fix wages decorator. Then I deleted the decorator and now they are just dead code
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-26
 * @see
 */
public class FixWagesPlantControlTest {



    public void checkTargets()
    {
        //randomizer is needed for the constructor of the control
        MersenneTwisterFast random = new MersenneTwisterFast(1l);
        HumanResources hr = mock(HumanResources.class);
        when(hr.getRandom()).thenReturn(random);

        Firm firm = mock(Firm.class);
        when(firm.getModel()).thenReturn(new MacroII(1l));

        Plant p = mock(Plant.class);
        when(p.maximumWorkersPossible()).thenReturn(100);
        when(hr.getPlant()).thenReturn(p);
        when(hr.getFirm()).thenReturn(firm);
        when(p.getModel()).thenReturn(new MacroII(1l));


        FixWagesPlantControl control = new FixWagesPlantControl(hr);
        assertEquals(control.getTarget(), 0 );
        control.start();
        assertEquals(control.getTarget(), 1);










    }


    public void hiringTest() throws NoSuchFieldException, IllegalAccessException {
        Market.TESTING_MODE = true;


        System.out.println("--------------------------------------------------------------------------------------");
        System.out.println("FIXWAGES:");


        MacroII model = new MacroII(10l);
        Firm firm = new Firm(model); firm.earn(1000000000000l);
        Plant p = new Plant(Blueprint.simpleBlueprint(GoodType.GENERIC, 1, GoodType.GENERIC, 1),firm);
        p.setPlantMachinery(new IRSExponentialMachinery(GoodType.CAPITAL,firm,10,p,1f));

        firm.addPlant(p);



        model.schedule = mock(Schedule.class);  //give it a fake schedule; this way we control time!
        final List<Steppable> steppableList = new LinkedList<>();

        //make this always profitable
        SalesDepartment dept = mock(SalesDepartment.class);
        when(dept.getLastClosingCost()).thenReturn(1l);
        when(dept.getLastClosingPrice()).thenReturn(2l);
        when(dept.getSoldPercentage()).thenReturn(.7f);
        //should be profitable
        firm.registerSaleDepartment(dept,GoodType.GENERIC);




        //capture all "scheduleOnceIn"
        when(model.schedule.scheduleOnceIn(any(Double.class), any(Steppable.class))).then(
                new Answer<Object>() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        steppableList.add((Steppable) invocation.getArguments()[1]);
                        return true;
                    }

                }
        );
        //capture all scheduleOnce
        when(model.schedule.scheduleOnce(any(Double.class), any(Steppable.class))).then(
                new Answer<Object>() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        steppableList.add((Steppable) invocation.getArguments()[1]);
                        return true;
                    }

                }
        );




        Market market = new OrderBookBlindMarket(GoodType.LABOR);
        assertEquals(p.maximumWorkersPossible(),100);
        HumanResources humanResources = HumanResources.getHumanResourcesIntegrated(10000000,firm,market,
                p,FixWagesPlantControl.class,null,null).getDepartment(); //create!!!
        firm.registerHumanResources(p,humanResources);

        Field field = PurchasesDepartment.class.getDeclaredField("control");
        field.setAccessible(true);
        FixWagesPlantControl control = (FixWagesPlantControl) field.get(humanResources);

        for(int i=0; i < 200; i++){
            Person worker = new Person(model,0,i,market);
            worker.lookForWorkSoon();
        }

        //there should be 200 asks
        assertEquals(200, market.getSellers().size());
        assertEquals(1,market.getBuyers().size());  //the human resources should be the only one registerd
        assertTrue(market.getBuyers().contains(humanResources.getFirm()));

        //check before start
        assertEquals(steppableList.size(),0);
        assertTrue(!steppableList.contains(control));
        assertTrue(p.workerSize()==0);

        //start the human resources
        humanResources.start();
        //some stuff might have happened, but surely the control should have called "schedule in"
        assertEquals(steppableList.size(),2);     //should be 2: both the profit check and the pid adjust
//        assertTrue(steppableList.contains(control));
//        assertTrue(p.workerSize() > 0);


        //now "adjust" 100 times
        for(int i=0; i < 200; i++)
        {
            //put the stuff to adjust in its own list
            Set<Steppable> toStep = new HashSet<>(steppableList);
            steppableList.clear();
            int oldTarget = control.getTarget();
            long oldWage = humanResources.maxPrice(GoodType.LABOR,market);


            firm.weekEnd(100*i);

            //notice that this is un-natural as profitStep occurs only once every 3 pid steps in reality
            for(Steppable s : toStep)
                s.step(model);

            long newWage = humanResources.maxPrice(GoodType.LABOR,market);
            System.out.println("old wage:" + oldWage +" , new wage: " + newWage + " , worker size: " + p.workerSize() + ", old target: " + oldTarget + ", new target: " + control.getTarget());


     //       assertTrue(steppableList.contains(control));



            //make sure it is adjusting in the right direction
            /*       assertTrue((p.workerSize() <= oldTarget && newWage >= oldWage) ||       // this controller is very imprecise
(p.workerSize() >= oldTarget && newWage <= oldWage) ||
(p.workerSize() == oldTarget && newWage == oldWage) || i<10);
            */


        }
        System.out.println("--------------------------------------------------------------------------------------");

        assertTrue(p.workerSize() == 100);






    }


    public void hiringTestFromAbove() throws NoSuchFieldException, IllegalAccessException {
        Market.TESTING_MODE = true;

        System.out.println("--------------------------------------------------------------------------------------");
        System.out.println("FIXWAGES (from above):");

        MacroII model = new MacroII(10l);
        Firm firm = new Firm(model); firm.earn(1000000000000l);
        Plant p = new Plant(Blueprint.simpleBlueprint(GoodType.GENERIC, 1, GoodType.GENERIC, 1),firm);
        p.setPlantMachinery(new IRSExponentialMachinery(GoodType.CAPITAL,firm,10,p,1f));

        firm.addPlant(p);



        model.schedule = mock(Schedule.class);  //give it a fake schedule; this way we control time!
        final List<Steppable> steppableList = new LinkedList<>();

        //make this always profitable
        SalesDepartment dept = mock(SalesDepartment.class);
        when(dept.getLastClosingCost()).thenReturn(1l);
        when(dept.getLastClosingPrice()).thenReturn(2l);
        when(dept.getSoldPercentage()).thenReturn(.7f);
        //should be profitable
        firm.registerSaleDepartment(dept,GoodType.GENERIC);


        System.out.println("--------------------------------------------------------------------------------------");



        //capture all "scheduleOnceIn"
        when(model.schedule.scheduleOnceIn(any(Double.class), any(Steppable.class))).then(
                new Answer<Object>() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        steppableList.add((Steppable) invocation.getArguments()[1]);
                        return true;
                    }

                }
        );
        //capture all scheduleOnce
        when(model.schedule.scheduleOnce(any(Double.class), any(Steppable.class))).then(
                new Answer<Object>() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        steppableList.add((Steppable) invocation.getArguments()[1]);
                        return true;
                    }

                }
        );




        Market market = new OrderBookBlindMarket(GoodType.LABOR);
        assertEquals(p.maximumWorkersPossible(),100);
        HumanResources humanResources = HumanResources.getHumanResourcesIntegrated(10000000,firm,market,
                p,FixWagesPlantControl.class,null,null).getDepartment(); //create!!!
        firm.registerHumanResources(p,humanResources);

        Field field = PurchasesDepartment.class.getDeclaredField("control");
        field.setAccessible(true);
        FixWagesPlantControl control = (FixWagesPlantControl) field.get(humanResources);


        field = FixWagesPlantControl.class.getDeclaredField("control"); field.setAccessible(true);
        FixedWageDecorator decoratedControl = (FixedWageDecorator)field.get(control);

        field = PlantControlDecorator.class.getDeclaredField("toDecorate"); field.setAccessible(true);
        TargetAndMaximizePlantControl innerControl = (TargetAndMaximizePlantControl)field.get(decoratedControl);

        field = TargetAndMaximizePlantControl.class.getDeclaredField("targeter"); field.setAccessible(true);
        PIDTargeter targeter = (PIDTargeter) field.get(innerControl);
        targeter.setInitialWage(200);







        for(int i=0; i < 200; i++){
            Person worker = new Person(model,0,i,market);
            worker.lookForWorkSoon();
        }

        //there should be 200 asks
        assertEquals(200, market.getSellers().size());
        assertEquals(1,market.getBuyers().size());  //the human resources should be the only one registerd
        assertTrue(market.getBuyers().contains(humanResources.getFirm()));

        //check before start
        assertEquals(steppableList.size(),0);
        assertTrue(!steppableList.contains(control));
        assertTrue(p.workerSize()==0);

        //start the human resources
        humanResources.start();
        //some stuff might have happened, but surely the control should have called "schedule in"
        assertEquals(steppableList.size(),2);     //should be 2: both the profit check and the pid adjust
//        assertTrue(steppableList.contains(control));
        assertTrue(p.workerSize() > 0);


        //now "adjust" 100 times
        for(int i=0; i < 100; i++)
        {
            //put the stuff to adjust in its own list
            Set<Steppable> toStep = new HashSet<>(steppableList);
            steppableList.clear();
            int oldTarget = control.getTarget();
            long oldWage = humanResources.maxPrice(GoodType.LABOR,market);


            //notice that this is un-natural as profitStep occurs only once every 3 pid steps in reality
            for(Steppable s : toStep)
                s.step(model);

            long newWage = humanResources.maxPrice(GoodType.LABOR,market);
            System.out.println("old wage:" + oldWage +" , new wage: " + newWage + " , worker size: " + p.workerSize() + ", old target: " + oldTarget + ", new target: " + control.getTarget());


   //         assertTrue(steppableList.contains(control));



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