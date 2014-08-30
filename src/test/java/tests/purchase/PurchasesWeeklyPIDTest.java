/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package tests.purchase;

import agents.people.Person;
import agents.firm.Firm;
import agents.firm.cost.EmptyCostStrategy;
import agents.firm.production.Blueprint;
import agents.firm.production.Plant;
import agents.firm.production.technology.LinearConstantMachinery;
import agents.firm.production.technology.Machinery;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.pid.PurchasesDailyPID;
import agents.firm.sales.SalesDepartmentAllAtOnce;
import financial.market.Market;
import financial.market.OrderBookBlindMarket;
import financial.utilities.Quote;
import goods.DifferentiatedGoodType;
import goods.Good;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import org.junit.Test;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import model.utilities.dummies.DummySeller;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
 * @version 2012-08-18
 * @see
 */
public class PurchasesWeeklyPIDTest {




    //simple test: you need 6 and you always get from the market yourprice/10 floored. Will the PID get there?
    @Test
    public void simpleStubTestFromBelow(){

        

        PurchasesDepartment dept = mock(PurchasesDepartment.class); //new stub
        Firm firm = mock(Firm.class); //new stub
        MacroII model = new MacroII(1);          when(firm.getModel()).thenReturn(model);

        Blueprint b = Blueprint.simpleBlueprint(UndifferentiatedGoodType.GENERIC,6, DifferentiatedGoodType.CAPITAL,1);
        Plant p = new Plant(b,firm);
        List<Plant> plants = new LinkedList<>(); plants.add(p);
        Machinery machinery = mock(Machinery.class);
        when(machinery.expectedWeeklyProductionRuns()).thenReturn(0f);
        when(machinery.maximumWorkersPossible()).thenReturn(100);

        p.setPlantMachinery(machinery);

        when(firm.getListOfPlantsUsingSpecificInput(UndifferentiatedGoodType.GENERIC)).thenReturn(plants);

        when(dept.getFirm()).thenReturn(firm);
        when(dept.getRandom()).thenReturn(model.random);
        when(dept.getModel()).thenReturn(model);
        when(dept.getGoodType()).thenReturn(UndifferentiatedGoodType.GENERIC);
        model.schedule = mock(Schedule.class);




        PurchasesDailyPID control = new PurchasesDailyPID(dept,.5f,2f,.05f);
        control.setHowManyDaysOfInventoryToHold(7);

        int pidPrice = control.maxPrice(UndifferentiatedGoodType.GENERIC);
        assertEquals(pidPrice, 0);

        assertEquals(control.getDailyTarget(), 0f, 0.0001f);
        when(machinery.expectedWeeklyProductionRuns()).thenReturn(1f);
        //although we changed the machinery, we bypassed the logEvent/listener structure. So let's addSalesDepartmentListener a useless worker for control to update its control
        p.addWorker(new Person(model));
        assertEquals(control.getDailyTarget(), 6f, 0.0001f);


        for(int i=0; i < 100; i++){
            when(firm.hasHowMany(UndifferentiatedGoodType.GENERIC)).thenReturn(Math.abs((int)Math.floor(((float)pidPrice)/10f))); //tell the control how much you managed to buy
            model.scheduleSoon(ActionOrder.ADJUST_PRICES,control);
            model.getPhaseScheduler().step(model);
            int oldPrice = pidPrice;
            pidPrice = control.maxPrice(UndifferentiatedGoodType.GENERIC);           //new price
            int currentInventory = firm.hasHowMany(UndifferentiatedGoodType.GENERIC); //what do you currently "have"
            //System.out.println(getCurrentInventory + " ---> " + pidPrice);
        /*    assertTrue((currentInventory <= 6 && pidPrice > oldPrice) ||
                    (currentInventory >= 6 && pidPrice < oldPrice) ||
                    (currentInventory == 6 && pidPrice == oldPrice));*/
            //test direction, my friend.
        }
        assertTrue(pidPrice >= 60 && pidPrice <= 70);

    }


    //simple test: you need 6 and you always get from the market yourprice/10 floored. Will the PID get there?
    @Test
    public void simpleStubTestFromAbove(){

        

        PurchasesDepartment dept = mock(PurchasesDepartment.class); //new stub
        Firm firm = mock(Firm.class); //new stub
        MacroII model = new MacroII(1);
        Blueprint b = Blueprint.simpleBlueprint(UndifferentiatedGoodType.GENERIC,6, DifferentiatedGoodType.CAPITAL,1);
        Plant p = new Plant(b,firm);
        List<Plant> plants = new LinkedList<>(); plants.add(p);

        when(firm.getListOfPlantsUsingSpecificInput(UndifferentiatedGoodType.GENERIC)).thenReturn(plants);

        when(dept.getFirm()).thenReturn(firm);
        when(dept.getModel()).thenReturn(model);
        when(dept.getRandom()).thenReturn(model.random);
        when(firm.getModel()).thenReturn(model);
        when(dept.getGoodType()).thenReturn(UndifferentiatedGoodType.GENERIC);
        model.schedule = mock(Schedule.class);


        Machinery machinery = mock(Machinery.class);         //stub machinery, makes it easy to control production speed.
        when(machinery.expectedWeeklyProductionRuns()).thenReturn(0f);
        when(machinery.maximumWorkersPossible()).thenReturn(100);
        p.setPlantMachinery(machinery);



        PurchasesDailyPID control = new PurchasesDailyPID(dept,.5f,2f,.05f);
        control.setHowManyDaysOfInventoryToHold(7);
        int pidPrice = control.maxPrice(UndifferentiatedGoodType.GENERIC);
        assertEquals(pidPrice, 0);

        assertEquals(control.getDailyTarget(), 0f, 0.0001f);
        when(machinery.expectedWeeklyProductionRuns()).thenReturn(1f);   //if they expect only one run, it should be relatively easy.
        //although we changed the machinery, we bypassed the logEvent/listener structure. So let's addSalesDepartmentListener a useless worker for control to update its control
        p.addWorker(new Person(model));
        assertEquals(control.getDailyTarget(), 6f, 0.0001f);
        control.setInitialPrice(100);


        for(int i=0; i < 100; i++){
            when(firm.hasHowMany(UndifferentiatedGoodType.GENERIC)).thenReturn(Math.abs((int)Math.floor(((float)pidPrice)/10f))); //tell the control how much you managed to buy
            model.scheduleSoon(ActionOrder.ADJUST_PRICES,control);
            model.getPhaseScheduler().step(model);
            int oldPrice = pidPrice;
            pidPrice = control.maxPrice(UndifferentiatedGoodType.GENERIC);           //new price
            int currentInventory = firm.hasHowMany(UndifferentiatedGoodType.GENERIC); //what do you currently "have"
            // System.out.println(getCurrentInventory + " ---> " + pidPrice);
            assertTrue((currentInventory <= 6 && pidPrice > oldPrice) ||
                    (currentInventory >= 6 && pidPrice < oldPrice) ||
                    (currentInventory == 6 && pidPrice == oldPrice));
            //test direction, my friend.
        }
        assertTrue(pidPrice >= 60 && pidPrice <= 70);

    }



    //simple test: you need 6 and you always get from the market yourprice/10 floored. Will the PID get there?
    @Test
    public void simpleStubTestMidway(){

        

        PurchasesDepartment dept = mock(PurchasesDepartment.class); //new stub
        Firm firm = mock(Firm.class); //new stub
        MacroII model = new MacroII(1);
        Blueprint b = Blueprint.simpleBlueprint(UndifferentiatedGoodType.GENERIC,6, DifferentiatedGoodType.CAPITAL,1);
        Plant p = new Plant(b,firm);
        List<Plant> plants = new LinkedList<>(); plants.add(p);


        when(firm.getListOfPlantsUsingSpecificInput(UndifferentiatedGoodType.GENERIC)).thenReturn(plants);

        when(dept.getFirm()).thenReturn(firm);
        when(dept.getRandom()).thenReturn(model.random);
        when(firm.getModel()).thenReturn(model);
        when(dept.getGoodType()).thenReturn(UndifferentiatedGoodType.GENERIC);
        when(dept.getModel()).thenReturn(model);

        model.schedule = mock(Schedule.class);

        Machinery machinery = mock(Machinery.class);         //stub machinery, makes it easy to control production speed.
        when(machinery.expectedWeeklyProductionRuns()).thenReturn(0f);
        when(machinery.maximumWorkersPossible()).thenReturn(100);

        p.setPlantMachinery(machinery);


        PurchasesDailyPID control = new PurchasesDailyPID(dept,.5f,2f,.05f);
        control.setHowManyDaysOfInventoryToHold(7);

        int pidPrice = control.maxPrice(UndifferentiatedGoodType.GENERIC);
        assertEquals(pidPrice, 0);

        assertEquals(control.getDailyTarget(), 0f, 0.0001f);
        when(machinery.expectedWeeklyProductionRuns()).thenReturn(1f);   //if they expect only one run, it should be relatively easy.
        //although we changed the machinery, we bypassed the logEvent/listener structure. So let's addSalesDepartmentListener a useless worker for control to update its control
        p.addWorker(new Person(model));
        assertEquals(control.getDailyTarget(), 6f, 0.0001f);

        model.scheduleSoon(ActionOrder.ADJUST_PRICES,control);

        for(int i=0; i < 1000; i++){
            when(firm.hasHowMany(UndifferentiatedGoodType.GENERIC)).thenReturn(Math.abs((int)Math.floor(((float)pidPrice)/10f))); //tell the control how much you managed to buy
            model.getPhaseScheduler().step(model);
            int oldPrice = pidPrice;
            pidPrice = control.maxPrice(UndifferentiatedGoodType.GENERIC);           //new price
            int currentInventory = firm.hasHowMany(UndifferentiatedGoodType.GENERIC); //what do you currently "have"
            System.out.println(currentInventory + " ---> " + pidPrice);
/*            assertTrue((currentInventory <= 6 && pidPrice > oldPrice) ||
                    (currentInventory >= 6 && pidPrice < oldPrice) ||
                    (currentInventory == 6 && pidPrice == oldPrice));*/
            //test direction, my friend.
        }
        assertTrue(pidPrice >= 60 && pidPrice <= 70);


        //and now you need half of the money!
        for(int i=0; i < 5000; i++){
            when(firm.hasHowMany(UndifferentiatedGoodType.GENERIC)).thenReturn(Math.abs((int)Math.floor(((float)pidPrice)/5f))); //tell the control how much you managed to buy
            model.getPhaseScheduler().step(model);
            int oldPrice = pidPrice;
            pidPrice = control.maxPrice(UndifferentiatedGoodType.GENERIC);           //new price
            int currentInventory = firm.hasHowMany(UndifferentiatedGoodType.GENERIC); //what do you currently "have"
            //   System.out.println(getCurrentInventory + " ****> " + pidPrice);
    /*        assertTrue((currentInventory <= 6 && pidPrice > oldPrice) ||
                    (currentInventory >= 6 && pidPrice < oldPrice) ||
                    (currentInventory == 6 && pidPrice == oldPrice));
                    */
            //test direction, my friend.
        }
        assertTrue(pidPrice >= 30 && pidPrice <= 40);

    }


    @Test
    public void fullDressRehearsal() throws NoSuchFieldException, IllegalAccessException {

        

        final MacroII model = new MacroII(1){
            @Override
            public void start() {
                super.start();    //this model doesn't do anything special when starting, so we can test.
            }
        };
        final Market market = new OrderBookBlindMarket(UndifferentiatedGoodType.GENERIC);
        final Firm f = new Firm(model);
        Blueprint b = Blueprint.simpleBlueprint(UndifferentiatedGoodType.GENERIC,6, DifferentiatedGoodType.CAPITAL,1);
        final Plant p = new Plant(b,f);
        final LinearConstantMachinery machinery = new LinearConstantMachinery(DifferentiatedGoodType.CAPITAL,f,0,p);
        machinery.setOneWorkerThroughput(100);
        p.setPlantMachinery(machinery);
        p.setCostStrategy(new EmptyCostStrategy());

        f.addPlant(p);
        //addSalesDepartmentListener 20 workers and your completeProductionRunNow once every 25
        for(int i=0; i < 4; i++)
            p.addWorker(new Person(model));
        f.receiveMany(UndifferentiatedGoodType.MONEY,10000000);
        model.start();
        f.registerSaleDepartment(mock(SalesDepartmentAllAtOnce.class), UndifferentiatedGoodType.GENERIC);
        f.registerSaleDepartment(mock(SalesDepartmentAllAtOnce.class), DifferentiatedGoodType.CAPITAL);



        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartmentIntegrated(
                10000000,f,market,PurchasesDailyPID.class,null,null).getDepartment();
        f.registerPurchasesDepartment(dept, UndifferentiatedGoodType.GENERIC);

        Field field = PurchasesDepartment.class.getDeclaredField("control");
        field.setAccessible(true);
        final PurchasesDailyPID control = (PurchasesDailyPID) field.get(dept); //so we can start it!
        control.setHowManyDaysOfInventoryToHold(7);

        assertEquals(control.getDailyTarget(), 6 * model.getWeekLength() / 25f, .01);
        control.start();

        final ArrayList<Quote> quotes = new ArrayList<>(); //here we'll store all the seller quotes



        model.schedule.scheduleRepeating(new Steppable() {
            @Override
            public void step(SimState simState) {
                //create 10 sellers
                for(int i=0; i<100; i++)
                {
                    DummySeller seller = new DummySeller(model,i*10 + 10);
                    market.registerSeller(seller);
                    Good good = Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC);
                    seller.receive(good,null);
                    Quote q = market.submitSellQuote(seller,seller.saleQuote,good);
                    quotes.add(q);
                }

                //this can only be tested by printing out results and making sure it works
                //System.out.println(f.hasHowMany(GoodType.GENERIC) + " ---> " + control.maxPrice(GoodType.GENERIC) + " ======== " + p.getStatus());
                //   model.schedule.adjust(model);






                //at the end of the day remove all quotes
                for(Quote q : quotes)
                {
                    try{
                        market.removeSellQuote(q);
                    }
                    catch (IllegalArgumentException e){} //some of them will have been bought. That's okay
                }
                quotes.clear();
            }
        },10);


        market.start(model);
        do
        {
            if (!model.schedule.step(model)) break;
            if(model.schedule.getSteps() % 100 == 0)
                System.out.println(model.schedule.getSteps());
        }
        while(model.schedule.getSteps() < 5000);


        //I expect the price to go high so that the firm builds up its reserves and then drop so that it only needs to buy 2 a adjust to keep things constant
        //   assertTrue(dept.maxPrice(GoodType.GENERIC,market) >= 60 && dept.maxPrice(GoodType.GENERIC,market) <= 60);
    //    assertEquals(f.hasHowMany(GoodType.GENERIC),25); //has 6 but I just consumed 2



    }

}
