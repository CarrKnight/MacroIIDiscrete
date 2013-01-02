package tests.plantcontrol;

import agents.Person;
import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.purchases.PurchasesDepartment;
import financial.Market;
import financial.OrderBookBlindMarket;
import goods.GoodType;
import goods.production.Blueprint;
import goods.production.Plant;
import goods.production.control.MaxCapacityControl;
import goods.production.technology.IRSExponentialMachinery;
import junit.framework.Assert;
import model.MacroII;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import sim.engine.Schedule;
import sim.engine.Steppable;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
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
 * @version 2012-08-24
 * @see
 */
public class MaxCapacityControlTest {


    @Test
    public void hiringTest2() throws NoSuchFieldException, IllegalAccessException {


        for(int i=0; i<10; i++)
            hiringTest();

    }

    @Test
    public void hiringTest() throws NoSuchFieldException, IllegalAccessException {
        Market.TESTING_MODE = true;

        MacroII model = new MacroII(10l);
        Firm firm = new Firm(model); firm.earn(1000000000000l);
        Plant p = new Plant(Blueprint.simpleBlueprint(GoodType.GENERIC,1,GoodType.GENERIC,1),firm);
        p.setPlantMachinery(new IRSExponentialMachinery(GoodType.CAPITAL,firm,10,p,1f));
        firm.addPlant(p);



        model.schedule = mock(Schedule.class);  //give it a fake schedule; this way we control time!
        final List<Steppable> steppableList = new LinkedList<>();
        //capture all "scheduleOnceIn"
        when(model.schedule.scheduleOnceIn(any(Double.class),any(Steppable.class))).then(
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
        //capture all "scheduleOnceIn"
        when(model.schedule.scheduleOnceIn(any(Double.class), any(Steppable.class),anyInt())).then(
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
        HumanResources humanResources = HumanResources.getHumanResourcesIntegrated(10000000,firm,market,p,MaxCapacityControl.class,null,null); //create!!!

        Field field = PurchasesDepartment.class.getDeclaredField("control");
        field.setAccessible(true);
        MaxCapacityControl control = (MaxCapacityControl) field.get(humanResources);
        firm.addPlant(p);

        for(int i=0; i < 200; i++){
            Person worker = new Person(model,0,i,market);
            worker.lookForWork();
        }

        //there should be 200 asks
        assertEquals(200, market.getSellers().size());
        assertEquals(1,market.getBuyers().size());  //the human resources should be the only one registerd
        assertTrue(market.getBuyers().contains(humanResources.getFirm()));

        //check before start
        assertEquals(steppableList.size(),0);
     //   assertTrue(!steppableList.contains(control));
        assertTrue(p.workerSize()==0);

        //start the human resources
        humanResources.start();
        //some stuff might have happened, but surely the control should have called "schedule in"
//        assertEquals(steppableList.size(),1);
     //   assertTrue(steppableList.contains(control));
//        assertTrue(p.workerSize() > 0);


        //now "adjust" 100 times
        for(int i=0; i < 1000; i++)
        {
            //put the stuff to adjust in its own list
            Set<Steppable> toStep = new HashSet<>(steppableList);
            steppableList.clear();
            long oldWage = humanResources.maxPrice(GoodType.LABOR,market);

            for(Steppable s : toStep)
                s.step(model);

            long newWage = humanResources.maxPrice(GoodType.LABOR,market);

     //       assertTrue(steppableList.contains(control));

         //   System.out.println("old wage:" + oldWage +" , new wage: " + newWage + " , worker size: " + p.workerSize());


            //make sure it is adjusting in the right direction
            Assert.assertTrue((p.workerSize() <= 90 && newWage > oldWage) ||       //notice 90, 110: this controller is very imprecise
                    (p.workerSize() >= 110 && newWage < oldWage) ||
                    (p.workerSize() == 100 && newWage == oldWage) ||
                    (p.workerSize() > 90 && newWage < 110) ||
                   i<10);//derivative is pretty strong here


        }







    }




}
