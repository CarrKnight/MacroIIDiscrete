package tests.purchase;

import agents.firm.Firm;
import agents.firm.production.Blueprint;
import agents.firm.production.Plant;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.pid.PurchasesFixedPID;
import financial.market.Market;
import financial.market.OrderBookBlindMarket;
import financial.utilities.Quote;
import goods.DifferentiatedGoodType;
import goods.Good;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.dummies.DummySeller;
import model.utilities.pid.CascadePToPIDController;
import org.junit.Test;
import sim.engine.Schedule;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;

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
 * @version 2012-08-13
 * @see
 */
public class PurchasesFixedPIDTest {




    //simple test: you need 6 and you always get from the market yourprice/10 floored. Will the PID get there?
    @Test
    public void simpleStubTestFromBelow(){

        PurchasesDepartment dept = mock(PurchasesDepartment.class); //new stub
        Firm firm = mock(Firm.class); //new stub
        MacroII model = new MacroII(1);
        Blueprint b = Blueprint.simpleBlueprint(UndifferentiatedGoodType.GENERIC,6, DifferentiatedGoodType.CAPITAL,1);
        Plant p = new Plant(b,firm);
        LinkedList<Plant> plants = new LinkedList<>(); plants.add(p);

        when(firm.getListOfPlantsUsingSpecificInput(UndifferentiatedGoodType.GENERIC)).thenReturn(plants);


        when(dept.getFirm()).thenReturn(firm);
        when(dept.getRandom()).thenReturn(model.random);
        when(firm.getModel()).thenReturn(model);
        when(dept.getGoodType()).thenReturn(UndifferentiatedGoodType.GENERIC);
        model.schedule = mock(Schedule.class);



        PurchasesFixedPID control = new PurchasesFixedPID(dept,.5f,2f,.05f);
        int pidPrice = control.maxPrice(UndifferentiatedGoodType.GENERIC);
        assertEquals(pidPrice, 0);
        assertEquals(control.getInventoryTarget(), 6);

        for(int i=0; i < 100; i++){
            when(firm.hasHowMany(UndifferentiatedGoodType.GENERIC)).thenReturn(Math.abs((int)Math.floor(((float)pidPrice)/10f))); //tell the control how much you managed to buy
            model.scheduleSoon(ActionOrder.ADJUST_PRICES, control);
            model.getPhaseScheduler().step(model);
            int oldPrice = pidPrice;
            pidPrice = control.maxPrice(UndifferentiatedGoodType.GENERIC);           //new price
            int currentInventory = firm.hasHowMany(UndifferentiatedGoodType.GENERIC); //what do you currently "have"
            //    System.out.println(getCurrentInventory + " ---> " + pidPrice);
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

        PurchasesDepartment dept = mock(PurchasesDepartment.class); //new stub
        Firm firm = mock(Firm.class); //new stub
        MacroII model = new MacroII(1l);


        when(dept.getFirm()).thenReturn(firm);
        when(dept.getRandom()).thenReturn(model.random);
        when(firm.getModel()).thenReturn(model);
        model.setFixedInventoryTarget(6);
        when(dept.getGoodType()).thenReturn(UndifferentiatedGoodType.GENERIC);
        model.schedule = mock(Schedule.class);



        PurchasesFixedPID control = new PurchasesFixedPID(dept,.5f,2f,.05f);
        control.setInitialPrice(100);
        int pidPrice = control.maxPrice(UndifferentiatedGoodType.GENERIC);
        assertEquals(pidPrice, 100);
        assertEquals(control.getInventoryTarget(), 6);

        for(int i=0; i < 100; i++){
            when(firm.hasHowMany(UndifferentiatedGoodType.GENERIC)).thenReturn(Math.abs((int)Math.floor(((float)pidPrice)/10f))); //tell the control how much you managed to buy
            model.scheduleSoon(ActionOrder.ADJUST_PRICES,control);
            model.getPhaseScheduler().step(model);
            int oldPrice = pidPrice;
            pidPrice = control.maxPrice(UndifferentiatedGoodType.GENERIC);           //new price
            int currentInventory = firm.hasHowMany(UndifferentiatedGoodType.GENERIC); //what do you currently "have"
            assertTrue((currentInventory <= 6 && pidPrice > oldPrice) ||
                    (currentInventory >= 6 && pidPrice < oldPrice) ||
                    (currentInventory == 6 && pidPrice == oldPrice));
            //test direction, my friend.
        }
        assertTrue(pidPrice >= 60 && pidPrice <= 70);


    }


    //messy test: after 50 turns the market changes, will I adapt?
    @Test
    public void simpleStubTestMidwayChange(){

        PurchasesDepartment dept = mock(PurchasesDepartment.class); //new stub
        Firm firm = mock(Firm.class); //new stub
        MacroII model = new MacroII(1l);

        when(dept.getFirm()).thenReturn(firm);
        when(dept.getRandom()).thenReturn(model.random);
        when(firm.getModel()).thenReturn(model);
        model.setFixedInventoryTarget(6);
        when(dept.getGoodType()).thenReturn(UndifferentiatedGoodType.GENERIC);
        model.schedule = mock(Schedule.class);



        PurchasesFixedPID control = new PurchasesFixedPID(dept,.5f,2f,.05f);
        int pidPrice = control.maxPrice(UndifferentiatedGoodType.GENERIC);
        assertEquals(pidPrice, 0l);
       assertEquals(control.getInventoryTarget(), 6);

        //for the first 50 turns is like the first test
        model.scheduleSoon(ActionOrder.ADJUST_PRICES,control);

        for(int i=0; i < 50; i++){
            when(firm.hasHowMany(UndifferentiatedGoodType.GENERIC)).thenReturn(Math.abs((int)Math.floor(((float)pidPrice)/10f))); //tell the control how much you managed to buy
            model.getPhaseScheduler().step(model);
            int oldPrice = pidPrice;
            pidPrice = control.maxPrice(UndifferentiatedGoodType.GENERIC);           //new price
            int currentInventory = firm.hasHowMany(UndifferentiatedGoodType.GENERIC); //what do you currently "have"
            System.out.println(currentInventory + " ---> " + pidPrice);
            assertTrue((currentInventory <= 6 && pidPrice > oldPrice) ||
                    (currentInventory >= 6 && pidPrice < oldPrice) ||
                    (currentInventory == 6 && pidPrice == oldPrice));
            //test direction, my friend.
        }
        assertTrue(pidPrice >= 60 && pidPrice <= 70);

        //and now you need half of the money!
        for(int i=0; i < 50; i++){
            when(firm.hasHowMany(UndifferentiatedGoodType.GENERIC)).thenReturn(Math.abs((int)Math.floor(((float)pidPrice)/5f))); //tell the control how much you managed to buy
            model.getPhaseScheduler().step(model);
            int oldPrice = pidPrice;
            pidPrice = control.maxPrice(UndifferentiatedGoodType.GENERIC);           //new price
            int currentInventory = firm.hasHowMany(UndifferentiatedGoodType.GENERIC); //what do you currently "have"
            System.out.println(currentInventory + " ****> " + pidPrice);
            assertTrue((currentInventory <= 6 && pidPrice > oldPrice) ||
                    (currentInventory >= 6 && pidPrice < oldPrice) ||
                    (currentInventory == 6 && pidPrice == oldPrice));
            //test direction, my friend.
        }
        assertTrue(pidPrice >= 30 && pidPrice <= 40);

    }


    //simple test: you need 6 and you always get from the market yourprice/10 floored. Will the PID get there?
    //this time without mocks
    @Test
    public void simpleFullTestFromBelow() throws NoSuchFieldException, IllegalAccessException {
        Market.TESTING_MODE = true;

        MacroII model = new MacroII(1l);
        Market market = new OrderBookBlindMarket(UndifferentiatedGoodType.GENERIC);
        Firm f = new Firm(model);
        f.receiveMany(UndifferentiatedGoodType.MONEY,100000000);
        model.start();
        market.start(model);

        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartmentIntegrated(
                10000000,f,market,PurchasesFixedPID.class,null,null).getDepartment();    //i'm assuming fixed target is 6
        f.registerPurchasesDepartment(dept, UndifferentiatedGoodType.GENERIC);

        Field field = PurchasesDepartment.class.getDeclaredField("control");
        field.setAccessible(true);
        PurchasesFixedPID control = (PurchasesFixedPID) field.get(dept); //so we can start it!
        control.start();

        final ArrayList<Quote> quotes = new ArrayList<>(); //here we'll store all the seller quotes

        market.start(model);
        for(int  j =0; j<100; j++) //for 50 stinking turns
        {

            //create 10 sellers
            for(int i=0; i<10; i++)
            {
                DummySeller seller = new DummySeller(model,i*10 + 10);
                market.registerSeller(seller);
                Good good =Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC);
                seller.receive(good,null);
                Quote q = market.submitSellQuote(seller,seller.saleQuote,good);
                quotes.add(q);
            }

            System.out.println(f.hasHowMany(UndifferentiatedGoodType.MONEY));
            System.out.println(f.hasHowMany(UndifferentiatedGoodType.GENERIC) + " ---> " + control.maxPrice(UndifferentiatedGoodType.GENERIC)  );
            model.schedule.step(model);






            //at the end of the day remove all quotes
            for(Quote q : quotes)
            {
                try{
                    market.removeSellQuote(q);
                }
                catch (IllegalArgumentException e){} //some of them will have been bought. That's okay
            }
            quotes.clear();

            //outflow
            if(f.hasAny(UndifferentiatedGoodType.GENERIC))
                f.consume(UndifferentiatedGoodType.GENERIC);
            if(f.hasAny(UndifferentiatedGoodType.GENERIC))
                f.consume(UndifferentiatedGoodType.GENERIC);


        }

        Market.TESTING_MODE = false;
        //I expect the price to go high so that the firm builds up its reserves and then drop so that it only needs to buy 2 a adjust to keep things constant
        assertTrue(dept.maxPrice(UndifferentiatedGoodType.GENERIC, market) >= 20 && dept.maxPrice(UndifferentiatedGoodType.GENERIC, market) <= 30);


    }

    //make sure the "customized" controller is instantiated succesfully
    @Test
    public void customizedControllerConstructor()
    {

        PurchasesDepartment department = mock(PurchasesDepartment.class); when(department.getFirm()).thenReturn(mock(Firm.class));
        MacroII model = new MacroII(1l);

        PurchasesFixedPID cascade = new PurchasesFixedPID(department,1, CascadePToPIDController.class,model);
        assertEquals(cascade.getKindOfController(), CascadePToPIDController.class);

    }


}
