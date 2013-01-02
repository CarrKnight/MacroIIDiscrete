package tests;

import agents.Person;
import agents.firm.Firm;
import agents.firm.sales.SalesDepartment;
import agents.firm.cost.EmptyCostStrategy;
import goods.Good;
import goods.GoodType;
import agents.firm.production.Blueprint;
import agents.firm.production.Plant;
import agents.firm.production.PlantListener;
import agents.firm.production.PlantStatus;
import agents.firm.production.technology.CRSExponentialMachinery;
import agents.firm.production.technology.DRSExponentialMachinery;
import agents.firm.production.technology.IRSExponentialMachinery;
import junit.framework.Assert;
import model.MacroII;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import sim.engine.Schedule;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created with IntelliJ IDEA.
 * User: carrknight
 * Date: 7/14/12
 * Time: 1:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlantTest {


    Plant crs;
    Plant irs;
    Plant drs;
    MacroII macro;


    @Before
    public void setUp() throws Exception {

        Blueprint b = new Blueprint.Builder().output(GoodType.GENERIC,2).build(); //create a simple output
        macro = new MacroII(1l);
        macro.schedule = mock(Schedule.class); //put in a fake schedule so we avoid steppables firing at random


        Firm f = new Firm(macro);
        crs = new Plant(b,new Firm(macro)); crs.setPlantMachinery(new CRSExponentialMachinery(GoodType.CAPITAL,f,0l,crs, 1f, 1f));
        irs = new Plant(b,new Firm(macro)); irs.setPlantMachinery(new IRSExponentialMachinery(GoodType.CAPITAL,f,0l,irs, 1f, 1f));
        drs = new Plant(b,new Firm(macro)); drs.setPlantMachinery(new DRSExponentialMachinery(GoodType.CAPITAL,f,0l,drs, 1f, 1f));

        Person w1 = new Person(macro); Person w2 = new Person(macro);
        crs.addWorker(w1);crs.addWorker(w2);    w1.hired(crs.getOwner(),9999999); w2.hired(crs.getOwner(), 9999999);
        w1 = new Person(macro); w2 = new Person(macro);
        irs.addWorker(w1);irs.addWorker(w2);    w1.hired(crs.getOwner(),9999999); w2.hired(crs.getOwner(), 9999999);
        w1 = new Person(macro);  w2 = new Person(macro);
        drs.addWorker(w1);drs.addWorker(w2);   w1.hired(crs.getOwner(),9999999); w2.hired(crs.getOwner(), 9999999);



        crs.getOwner().registerSaleDepartment(mock(SalesDepartment.class),GoodType.GENERIC); //fake sales department so that you don't sell the stuff you completeProductionRunNow
        irs.getOwner().registerSaleDepartment(mock(SalesDepartment.class),GoodType.GENERIC); //fake sales department so that you don't sell the stuff you completeProductionRunNow
        drs.getOwner().registerSaleDepartment(mock(SalesDepartment.class), GoodType.GENERIC); //fake sales department so that you don't sell the stuff you completeProductionRunNow


        crs.setCostStrategy(new EmptyCostStrategy());
        irs.setCostStrategy(new EmptyCostStrategy());
        drs.setCostStrategy(new EmptyCostStrategy());

    }


    @Test
    public void testGetModel() throws Exception {

        Assert.assertNotNull(crs.getModel());
        Assert.assertNotNull(irs.getModel());
        Assert.assertNotNull(drs.getModel());

        Assert.assertSame(crs.getModel(),irs.getModel());
        Assert.assertSame(drs.getModel(), irs.getModel());


    }

    @Test
    public void testGetBlueprint() throws Exception {

        Assert.assertNotNull(crs.getBlueprint());
        Assert.assertNotNull(irs.getBlueprint());
        Assert.assertNotNull(drs.getBlueprint());

        Assert.assertSame(crs.getModel(),irs.getModel());
        Assert.assertSame(drs.getModel(),irs.getModel());

    }

    @Test
    public void testWorkerSize() throws Exception {

        Assert.assertEquals(crs.workerSize(),2);
        Person w = crs.removeLastWorker();
        Assert.assertEquals(crs.workerSize(),1);
        crs.addWorker(w);
        Assert.assertEquals(crs.workerSize(),2);
    }

    @Test
    public void testCheckForInputs() throws Exception {

        Assert.assertTrue(crs.checkForInputs());  //the setup has no input need
        crs.getModel().schedule.scheduleOnce(Schedule.EPOCH,new Person(crs.getModel())); //this is to have the steppable not at time -1
        crs.getModel().schedule.step(crs.getModel());

        Blueprint b = Blueprint.simpleBlueprint(GoodType.GENERIC,1,GoodType.GENERIC,2); //addSalesDepartmentListener one for input
        crs.setBlueprint(b); //set it as blueprint
        Assert.assertFalse(crs.checkForInputs()); //you should miss one
        crs.getOwner().receive(new Good(GoodType.GENERIC,drs.getOwner(),1l),drs.getOwner()); //receive it
        //this automatically steps production (because it's inventory listener!)


        Assert.assertTrue(crs.checkForInputs()); //all good now


    }

    @Test
    public void testCheckForWorkers() throws Exception {

        Assert.assertTrue(crs.checkForWorkers());  //you have two workers, the minimum is one, you should be okay

        crs.removeLastWorker(); //remove a worker
        Assert.assertTrue(crs.checkForWorkers()); //should still work
        Person w = crs.removeLastWorker();
        Assert.assertTrue(!crs.checkForWorkers()); //should still work
        crs.addWorker(w);
        Assert.assertTrue(crs.checkForWorkers()); //should still work

    }

    @Test(expected=IllegalArgumentException.class)
    public void addWorkerTwiceErrorTest(){

        Person newWorker = new Person(crs.getModel());
        crs.addWorker(newWorker);
        crs.addWorker(newWorker);


    }


    @Test
    public void addWorkers(){

        Person worker1 = new Person(crs.getModel());
        Person worker2 = new Person(crs.getModel());

        Assert.assertEquals(crs.getWorkers().size(),2);  //it starts with two
        PlantListener fakeListener = mock(PlantListener.class); //addSalesDepartmentListener a fake listener
        crs.addListener(fakeListener);
        crs.addWorkers(worker1, worker2);  worker1.hired(crs.getOwner(),9999999); worker2.hired(crs.getOwner(),9999999);
        Assert.assertEquals(crs.getWorkers().size(),4);
        Assert.assertTrue(crs.getWorkers().contains(worker1));
        Assert.assertTrue(crs.getWorkers().contains(worker2));
        Mockito.verify(fakeListener,times(1)).changeInWorkforceEvent(any(Plant.class),any(Integer.class)); //make sure it was called just once!!! That's the all point of this method




    }

    @Test
    public void testProduce() throws Exception {

        macro.schedule = new Schedule(); //create a real schedule, please.
        Assert.assertTrue(crs.getModel().schedule.scheduleComplete()); //the schedule should be empty!
        Assert.assertTrue(!crs.getOwner().hasAny(GoodType.GENERIC));
        Assert.assertTrue(crs.getOwner().hasHowMany(GoodType.GENERIC) == 0);


        crs.getModel().schedule.reset();
        crs.getModel().schedule.scheduleOnce(Schedule.EPOCH, new Person(crs.getModel())); //this is to have the steppable not at time -1
        crs.setCostStrategy(new EmptyCostStrategy());
        //    crs.completeProductionRunNow();



        crs.startProductionRun();
        // System.out.println(crs.getModel().schedule.getTime());
        Assert.assertEquals(crs.getStatus(),PlantStatus.READY);

        Assert.assertTrue(crs.getOwner().hasAny(GoodType.GENERIC));
        Assert.assertTrue(crs.getOwner().hasHowMany(GoodType.GENERIC) == 2);






        crs.setBlueprint(Blueprint.simpleBlueprint(GoodType.GENERIC, 2, GoodType.GENERIC, 1)); //dumb technology to test inputs
        crs.startProductionRun();
        Assert.assertEquals(crs.getStatus(),PlantStatus.READY);
        Assert.assertTrue(crs.getOwner().hasAny(GoodType.GENERIC));       //should still have the stuff
        Assert.assertTrue(crs.getOwner().hasHowMany(GoodType.GENERIC) == 1);  //burned two, made one
        crs.startProductionRun();
        Assert.assertEquals(crs.getStatus(),PlantStatus.WAITING_FOR_INPUT);
        Assert.assertTrue(crs.getOwner().hasAny(GoodType.GENERIC));       //no change!
        Assert.assertTrue(crs.getOwner().hasHowMany(GoodType.GENERIC) == 1);










    }

    @Test
    public void testStep() throws Exception {


        Blueprint b = new Blueprint.Builder().output(GoodType.GENERIC,2).build(); //create a simple output
        MacroII localMacroII = new MacroII(1l);
        localMacroII.schedule = mock(Schedule.class); //put in a fake schedule so we avoid steppables firing at random


        Firm f = new Firm(localMacroII);
        Plant localCRS = new Plant(b,new Firm(localMacroII)); localCRS.setPlantMachinery(new CRSExponentialMachinery(GoodType.CAPITAL, f, 0l, localCRS, 1f, 1f));
        f.addPlant(localCRS);


        Person w1 = new Person(localMacroII); Person w2 = new Person(localMacroII);
        localCRS.addWorker(w1);localCRS.addWorker(w2);    w1.hired(localCRS.getOwner(),9999999); w2.hired(localCRS.getOwner(), 9999999);



        localCRS.getOwner().registerSaleDepartment(mock(SalesDepartment.class),GoodType.GENERIC); //fake sales department so that you don't sell the stuff you completeProductionRunNow


        localCRS.setCostStrategy(new EmptyCostStrategy());
        try{
            localCRS.getModel();

            localCRS.removeLastWorker(); localCRS.removeLastWorker();
            localCRS.setBlueprint(Blueprint.simpleBlueprint(GoodType.GENERIC, 1, GoodType.GENERIC, 1)); //dumb technology to test inputs

            localCRS.getModel().getPhaseScheduler().step(localCRS.getModel());



            Assert.assertEquals(localCRS.getStatus(), PlantStatus.WAITING_FOR_WORKERS);
            localCRS.addWorker(new Person(localCRS.getModel()));
            localCRS.addWorker(new Person(localCRS.getModel()));


            localCRS.getModel().getPhaseScheduler().step(localCRS.getModel());

            Assert.assertEquals(localCRS.getStatus(),PlantStatus.WAITING_FOR_INPUT);
            localCRS.getOwner().receive(new Good(GoodType.GENERIC,drs.getOwner(),1l),drs.getOwner());

            localCRS.getModel().getPhaseScheduler().step(localCRS.getModel());
            Assert.assertEquals(localCRS.getStatus(),PlantStatus.READY); //you should automatically start production!

        }catch(Exception e){
            Assert.fail("Can't throw exceptions now");
        }








    }


    @Test
    public void testWeekEnd() throws Exception {

        Assert.assertEquals(irs.getStatus(),PlantStatus.READY);

        for(int i=0; i < irs.getUsefulLife(); i++){
            Assert.assertEquals(irs.getStatus(),PlantStatus.READY);
            irs.weekEnd(0l);
        }

        Assert.assertEquals(irs.getStatus(),PlantStatus.OBSOLETE);


    }

    @Test
    public void testGetPlantTechnology() throws Exception {
        //make sure all delegate methods work
        Assert.assertEquals(1.0f, crs.hypotheticalWaitingTime(1), .0001f);
        Assert.assertEquals(1.0f,irs.hypotheticalWaitingTime(1),.0001f);
        Assert.assertEquals(1.0f,drs.hypotheticalWaitingTime(1),.0001f);


        Assert.assertEquals(0.5f,crs.hypotheticalWaitingTime(2),.0001f);
        Assert.assertEquals(0.25f,irs.hypotheticalWaitingTime(2),.0001f);
        Assert.assertEquals(1f/1.41421356f,drs.hypotheticalWaitingTime(2),.0001f);


        Assert.assertEquals(1.0f * crs.getModel().getWeekLength(),crs.hypotheticalWeeklyProductionRuns(1),.0001f);
        Assert.assertEquals(1.0f * irs.getModel().getWeekLength(),irs.hypotheticalWeeklyProductionRuns(1),.0001f);
        Assert.assertEquals(1.0f * drs.getModel().getWeekLength(),drs.hypotheticalWeeklyProductionRuns(1),.0001f);

        Assert.assertEquals(2.0f * crs.getModel().getWeekLength(),crs.hypotheticalWeeklyProductionRuns(2),.0001f);
        Assert.assertEquals(4.0f * irs.getModel().getWeekLength(),irs.hypotheticalWeeklyProductionRuns(2),.0001f);
        Assert.assertEquals(1.41421356f * drs.getModel().getWeekLength(),drs.hypotheticalWeeklyProductionRuns(2),.0001f);

        Assert.assertEquals(0.5f,crs.expectedWaitingTime(),.0001f);
        Assert.assertEquals(0.25f,irs.expectedWaitingTime(),.0001f);
        Assert.assertEquals(1f/1.41421356f,drs.expectedWaitingTime(),.0001f);


        Assert.assertEquals(2f*2.0f * crs.getModel().getWeekLength(),crs.expectedWeeklyProduction(GoodType.GENERIC),.0001f);
        Assert.assertEquals(2f*4.0f * drs.getModel().getWeekLength(),irs.expectedWeeklyProduction(GoodType.GENERIC),.0001f);
        Assert.assertEquals(2f* 1.41421356f * irs.getModel().getWeekLength(),drs.expectedWeeklyProduction(GoodType.GENERIC),.0001f);

        Assert.assertEquals(2f* crs.getModel().getWeekLength(),crs.marginalProductOfWorker(GoodType.GENERIC),.0001f);
        Assert.assertEquals(2f*5.0f * irs.getModel().getWeekLength(),irs.marginalProductOfWorker(GoodType.GENERIC),.0001f);
        Assert.assertEquals(2f* 0.317837245 * drs.getModel().getWeekLength(),drs.marginalProductOfWorker(GoodType.GENERIC),.0001f);




    }
}
