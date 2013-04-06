package tests.purchase;

import agents.Person;
import agents.firm.Firm;
import agents.firm.cost.EmptyCostStrategy;
import agents.firm.production.Blueprint;
import agents.firm.production.Plant;
import agents.firm.production.technology.LinearConstantMachinery;
import agents.firm.production.technology.Machinery;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.pid.PurchasesWeeklyPID;
import agents.firm.sales.SalesDepartment;
import financial.Market;
import financial.OrderBookBlindMarket;
import financial.utilities.Quote;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import org.junit.Test;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import tests.DummySeller;

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

        Market.TESTING_MODE = true;

        PurchasesDepartment dept = mock(PurchasesDepartment.class); //new stub
        Firm firm = mock(Firm.class); //new stub
        MacroII model = new MacroII(1l);          when(firm.getModel()).thenReturn(model);

        Blueprint b = Blueprint.simpleBlueprint(GoodType.GENERIC,6,GoodType.CAPITAL,1);
        Plant p = new Plant(b,firm);
        List<Plant> plants = new LinkedList<>(); plants.add(p);
        Machinery machinery = mock(Machinery.class);
        when(machinery.expectedWeeklyProductionRuns()).thenReturn(0f);
        p.setPlantMachinery(machinery);

        when(firm.getListOfPlantsUsingSpecificInput(GoodType.GENERIC)).thenReturn(plants);

        when(dept.getFirm()).thenReturn(firm);
        when(dept.getRandom()).thenReturn(model.random);
        when(dept.getGoodType()).thenReturn(GoodType.GENERIC);
        model.schedule = mock(Schedule.class);




        PurchasesWeeklyPID control = new PurchasesWeeklyPID(dept,.5f,2f,.05f);
        long pidPrice = control.maxPrice(GoodType.GENERIC);
        assertEquals(pidPrice, 0l);

        assertEquals(control.getWeeklyNeeds(), 0f, 0.0001f);
        when(machinery.expectedWeeklyProductionRuns()).thenReturn(1f);
        //although we changed the machinery, we bypassed the logEvent/listener structure. So let's addSalesDepartmentListener a useless worker for control to update its control
        p.addWorker(new Person(model));
        assertEquals(control.getWeeklyNeeds(), 6f, 0.0001f);


        for(int i=0; i < 100; i++){
            when(firm.hasHowMany(GoodType.GENERIC)).thenReturn(Math.abs((int)Math.floor(((float)pidPrice)/10f))); //tell the control how much you managed to buy
            model.scheduleSoon(ActionOrder.PREPARE_TO_TRADE,control);
            model.getPhaseScheduler().step(model);
            long oldPrice = pidPrice;
            pidPrice = control.maxPrice(GoodType.GENERIC);           //new price
            int currentInventory = firm.hasHowMany(GoodType.GENERIC); //what do you currently "have"
            //System.out.println(currentInventory + " ---> " + pidPrice);
            assertTrue((currentInventory <= 6 && pidPrice > oldPrice) ||
                    (currentInventory >= 6 && pidPrice < oldPrice) ||
                    (currentInventory == 6 && pidPrice == oldPrice));
            //test direction, my friend.
        }
        assertTrue(pidPrice >= 60 && pidPrice <= 70);

    }


    //simple test: you need 6 and you always get from the market yourprice/10 floored. Will the PID get there?
    @Test
    public void simpleStubTestFromAbove(){

        Market.TESTING_MODE = true;

        PurchasesDepartment dept = mock(PurchasesDepartment.class); //new stub
        Firm firm = mock(Firm.class); //new stub
        MacroII model = new MacroII(1l);
        Blueprint b = Blueprint.simpleBlueprint(GoodType.GENERIC,6,GoodType.CAPITAL,1);
        Plant p = new Plant(b,firm);
        List<Plant> plants = new LinkedList<>(); plants.add(p);

        when(firm.getListOfPlantsUsingSpecificInput(GoodType.GENERIC)).thenReturn(plants);

        when(dept.getFirm()).thenReturn(firm);
        when(dept.getRandom()).thenReturn(model.random);
        when(firm.getModel()).thenReturn(model);
        when(dept.getGoodType()).thenReturn(GoodType.GENERIC);
        model.schedule = mock(Schedule.class);


        Machinery machinery = mock(Machinery.class);         //stub machinery, makes it easy to control production speed.
        when(machinery.expectedWeeklyProductionRuns()).thenReturn(0f);
        p.setPlantMachinery(machinery);



        PurchasesWeeklyPID control = new PurchasesWeeklyPID(dept,.5f,2f,.05f);
        long pidPrice = control.maxPrice(GoodType.GENERIC);
        assertEquals(pidPrice, 0l);

        assertEquals(control.getWeeklyNeeds(), 0f, 0.0001f);
        when(machinery.expectedWeeklyProductionRuns()).thenReturn(1f);   //if they expect only one run, it should be relatively easy.
        //although we changed the machinery, we bypassed the logEvent/listener structure. So let's addSalesDepartmentListener a useless worker for control to update its control
        p.addWorker(new Person(model));
        assertEquals(control.getWeeklyNeeds(), 6f, 0.0001f);
        control.setInitialPrice(100);


        for(int i=0; i < 100; i++){
            when(firm.hasHowMany(GoodType.GENERIC)).thenReturn(Math.abs((int)Math.floor(((float)pidPrice)/10f))); //tell the control how much you managed to buy
            model.scheduleSoon(ActionOrder.PREPARE_TO_TRADE,control);
            model.getPhaseScheduler().step(model);
            long oldPrice = pidPrice;
            pidPrice = control.maxPrice(GoodType.GENERIC);           //new price
            int currentInventory = firm.hasHowMany(GoodType.GENERIC); //what do you currently "have"
            // System.out.println(currentInventory + " ---> " + pidPrice);
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

        Market.TESTING_MODE = true;

        PurchasesDepartment dept = mock(PurchasesDepartment.class); //new stub
        Firm firm = mock(Firm.class); //new stub
        MacroII model = new MacroII(1l);
        Blueprint b = Blueprint.simpleBlueprint(GoodType.GENERIC,6,GoodType.CAPITAL,1);
        Plant p = new Plant(b,firm);
        List<Plant> plants = new LinkedList<>(); plants.add(p);


        when(firm.getListOfPlantsUsingSpecificInput(GoodType.GENERIC)).thenReturn(plants);

        when(dept.getFirm()).thenReturn(firm);
        when(dept.getRandom()).thenReturn(model.random);
        when(firm.getModel()).thenReturn(model);
        when(dept.getGoodType()).thenReturn(GoodType.GENERIC);
        model.schedule = mock(Schedule.class);

        Machinery machinery = mock(Machinery.class);         //stub machinery, makes it easy to control production speed.
        when(machinery.expectedWeeklyProductionRuns()).thenReturn(0f);
        p.setPlantMachinery(machinery);


        PurchasesWeeklyPID control = new PurchasesWeeklyPID(dept,.5f,2f,.05f);
        long pidPrice = control.maxPrice(GoodType.GENERIC);
        assertEquals(pidPrice, 0l);

        assertEquals(control.getWeeklyNeeds(), 0f, 0.0001f);
        when(machinery.expectedWeeklyProductionRuns()).thenReturn(1f);   //if they expect only one run, it should be relatively easy.
        //although we changed the machinery, we bypassed the logEvent/listener structure. So let's addSalesDepartmentListener a useless worker for control to update its control
        p.addWorker(new Person(model));
        assertEquals(control.getWeeklyNeeds(), 6f, 0.0001f);

        model.scheduleSoon(ActionOrder.PREPARE_TO_TRADE,control);

        for(int i=0; i < 100; i++){
            when(firm.hasHowMany(GoodType.GENERIC)).thenReturn(Math.abs((int)Math.floor(((float)pidPrice)/10f))); //tell the control how much you managed to buy
            model.getPhaseScheduler().step(model);
            long oldPrice = pidPrice;
            pidPrice = control.maxPrice(GoodType.GENERIC);           //new price
            int currentInventory = firm.hasHowMany(GoodType.GENERIC); //what do you currently "have"
            // System.out.println(currentInventory + " ---> " + pidPrice);
            assertTrue((currentInventory <= 6 && pidPrice > oldPrice) ||
                    (currentInventory >= 6 && pidPrice < oldPrice) ||
                    (currentInventory == 6 && pidPrice == oldPrice));
            //test direction, my friend.
        }
        assertTrue(pidPrice >= 60 && pidPrice <= 70);


        //and now you need half of the money!
        for(int i=0; i < 50; i++){
            when(firm.hasHowMany(GoodType.GENERIC)).thenReturn(Math.abs((int)Math.floor(((float)pidPrice)/5f))); //tell the control how much you managed to buy
            model.getPhaseScheduler().step(model);
            long oldPrice = pidPrice;
            pidPrice = control.maxPrice(GoodType.GENERIC);           //new price
            int currentInventory = firm.hasHowMany(GoodType.GENERIC); //what do you currently "have"
            //   System.out.println(currentInventory + " ****> " + pidPrice);
            assertTrue((currentInventory <= 6 && pidPrice > oldPrice) ||
                    (currentInventory >= 6 && pidPrice < oldPrice) ||
                    (currentInventory == 6 && pidPrice == oldPrice));
            //test direction, my friend.
        }
        assertTrue(pidPrice >= 30 && pidPrice <= 40);

    }


    @Test
    public void fullDressRehearsal() throws NoSuchFieldException, IllegalAccessException {

        Market.TESTING_MODE = true;

        final MacroII model = new MacroII(1l){
            @Override
            public void start() {
                super.start();    //this model doesn't do anything special when starting, so we can test.
            }
        };
        final Market market = new OrderBookBlindMarket(GoodType.GENERIC);
        final Firm f = new Firm(model);
        Blueprint b = Blueprint.simpleBlueprint(GoodType.GENERIC,6,GoodType.CAPITAL,1);
        final Plant p = new Plant(b,f);
        final LinearConstantMachinery machinery = new LinearConstantMachinery(GoodType.CAPITAL,f,0,p);
        machinery.setOneWorkerThroughput(100);
        p.setPlantMachinery(machinery);
        p.setCostStrategy(new EmptyCostStrategy());

        f.addPlant(p);
        //addSalesDepartmentListener 20 workers and your completeProductionRunNow once every 25
        for(int i=0; i < 4; i++)
            p.addWorker(new Person(model));
        f.earn(10000000);
        model.start();
        f.registerSaleDepartment(mock(SalesDepartment.class),GoodType.GENERIC);
        f.registerSaleDepartment(mock(SalesDepartment.class),GoodType.CAPITAL);



        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartmentIntegrated(
                10000000,f,market,PurchasesWeeklyPID.class,null,null).getDepartment();
        f.registerPurchasesDepartment(dept,GoodType.GENERIC);

        Field field = PurchasesDepartment.class.getDeclaredField("control");
        field.setAccessible(true);
        final PurchasesWeeklyPID control = (PurchasesWeeklyPID) field.get(dept); //so we can start it!
        //your weekly needs should be weekLength/25
        assertEquals(control.getWeeklyNeeds(), 6 * model.getWeekLength() / 25f, .01);
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
                    Good good = new Good(GoodType.GENERIC,seller,i*10+10);
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



        do
            if (!model.schedule.step(model)) break;
        while(model.schedule.getSteps() < 5000);


        Market.TESTING_MODE = false;
        //I expect the price to go high so that the firm builds up its reserves and then drop so that it only needs to buy 2 a adjust to keep things constant
        //   assertTrue(dept.maxPrice(GoodType.GENERIC,market) >= 60 && dept.maxPrice(GoodType.GENERIC,market) <= 60);
    //    assertEquals(f.hasHowMany(GoodType.GENERIC),25); //has 6 but I just consumed 2



    }

}
