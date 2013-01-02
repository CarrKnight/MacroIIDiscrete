package tests.purchase;

import agents.EconomicAgent;
import agents.Inventory;
import agents.firm.Firm;
import agents.firm.NumberOfPlantsListener;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.inventoryControl.FixedInventoryControl;
import agents.firm.purchases.inventoryControl.InventoryControl;
import agents.firm.purchases.inventoryControl.Level;
import agents.firm.purchases.inventoryControl.SimpleInventoryControl;
import agents.firm.purchases.pricing.BidPricingStrategy;
import financial.Bankruptcy;
import financial.Market;
import financial.OrderBookMarket;
import goods.Good;
import goods.GoodType;
import agents.firm.production.Blueprint;
import agents.firm.production.Plant;
import agents.firm.production.PlantListener;
import agents.firm.production.technology.CRSExponentialMachinery;
import agents.firm.production.technology.Machinery;
import junit.framework.Assert;
import model.MacroII;
import org.junit.Before;
import org.junit.Test;
import tests.DummySeller;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Set;

import static org.mockito.Mockito.*;

/**
 * @author Ernesto
 * @version 2012-08-09
 * @see
 */
public class SimpleInventoryControlTest {

    MacroII model;
    Market market;


    @Before
    public void setup(){
        model = new MacroII(1l);
        market = new OrderBookMarket(GoodType.GENERIC);

    }

    //with no plants, it is basically useless
    @Test
    public void stubInventoryRatingNoPlant()
    {
        Firm f = mock(Firm.class);
        //set up the firm so that it returns first 1 then 10 then 5
        when(f.hasHowMany(GoodType.GENERIC)).thenReturn(1);
        when(f.getModel()).thenReturn(model);  when(f.getRandom()).thenReturn(model.random);


        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(0, f, market, SimpleInventoryControl.class,
                null, null, null);

        //assuming target inventory is 6~~

        Assert.assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);
        when(f.hasHowMany(GoodType.GENERIC)).thenReturn(10);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);
        when(f.hasHowMany(GoodType.GENERIC)).thenReturn(5);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);

    }

    @Test
    public void InventoryRatingNoPlant()
    {
        Firm f = new Firm(model);



        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(0, f, market, SimpleInventoryControl.class,
                null, null, null);


        //assuming target inventory is 6~~

        f.receive(new Good(GoodType.GENERIC,f,0l),null);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);
        Assert.assertEquals(dept.canBuy(), false);

        for(int i=0; i <9; i++)
            f.receive(new Good(GoodType.GENERIC,f,0l),null);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);
        Assert.assertEquals(dept.canBuy(), false);

        for(int i=0; i <5; i++)
            f.consume(GoodType.GENERIC);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);
        Assert.assertEquals(dept.canBuy(), false);


    }



    @Test
    public void stubInventoryRating()
    {
        Firm f = mock(Firm.class);
        //set up the firm so that it returns first 1 then 10 then 5
        when(f.getModel()).thenReturn(model);  when(f.getRandom()).thenReturn(model.random);

        Plant p = mock(Plant.class);
        Blueprint b = Blueprint.simpleBlueprint(GoodType.GENERIC,6,GoodType.CAPITAL,1);
        when(p.getBlueprint()).thenReturn(b);
        LinkedList<Plant> list = new LinkedList<>();
        list.add(p);
        when(f.getListOfPlantsUsingSpecificInput(GoodType.GENERIC)).thenReturn(list);


        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(0, f, market, SimpleInventoryControl.class,
                null, null, null);

        //assuming target inventory is 6~~

        when(f.hasHowMany(GoodType.GENERIC)).thenReturn(1);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.DANGER);
        when(f.hasHowMany(GoodType.GENERIC)).thenReturn(10);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.BARELY);
        when(f.hasHowMany(GoodType.GENERIC)).thenReturn(5);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.DANGER);

    }

    @Test
    public void InventoryRating()
    {
        Firm f = new Firm(model);
        Plant p = new Plant( Blueprint.simpleBlueprint(GoodType.GENERIC,6,GoodType.CAPITAL,1),f);
        p.setPlantMachinery(mock(Machinery.class));
        f.addPlant(p);


        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(0, f, market, SimpleInventoryControl.class,
                null, null, null);


        //assuming target inventory is 6~~

        f.receive(new Good(GoodType.GENERIC,f,0l),null);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.DANGER);
        Assert.assertEquals(dept.canBuy(), true);

        for(int i=0; i <9; i++)
            f.receive(new Good(GoodType.GENERIC,f,0l),null);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.BARELY);
        Assert.assertEquals(dept.canBuy(), true);

        for(int i=0; i <5; i++)
            f.consume(GoodType.GENERIC);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.DANGER);
        Assert.assertEquals(dept.canBuy(), true);

        for(int i=0; i <100; i++)
            f.receive(new Good(GoodType.GENERIC,f,0l),null);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);
        Assert.assertEquals(dept.canBuy(), false);

        //now test the listener
        p = new Plant( Blueprint.simpleBlueprint(GoodType.GENERIC,100,GoodType.CAPITAL,1),f);
        p.setPlantMachinery(mock(Machinery.class));
        f.addPlant(p);
        //the new requirement should automatically be put to 100
        Assert.assertEquals(dept.rateCurrentLevel(), Level.DANGER);
        Assert.assertEquals(dept.canBuy(), true);

        for(int i=0; i <5; i++)
            f.receive(new Good(GoodType.GENERIC, f, 0l), null);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.BARELY);
        Assert.assertEquals(dept.canBuy(), true);







    }


    //mock testing to count how many times buy() gets called by the inventory control reacting!
    @Test
    public void InventoryRatingShouldBuyMock() throws IllegalAccessException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
        Firm f = new Firm(model);
        Plant p = new Plant( Blueprint.simpleBlueprint(GoodType.GENERIC,6,GoodType.CAPITAL,1),f);
        p.setPlantMachinery(mock(Machinery.class));
        f.addPlant(p);


        PurchasesDepartment dept = mock(PurchasesDepartment.class);

        when(dept.getFirm()).thenReturn(f); //make sure you link to f
        when(dept.getGoodType()).thenReturn(GoodType.GENERIC); //make sure you link to f


        InventoryControl control = new SimpleInventoryControl(dept);
        //make sure it's registered as a listener
        Method method = EconomicAgent.class.getDeclaredMethod("getInventory");
        method.setAccessible(true);
        Inventory inv = (Inventory) method.invoke(f);


        //make sure it is an inventory listener!
        Assert.assertTrue(inv.removeListener(control));
        inv.addListener(control); //re-addSalesDepartmentListener it!


        for(int i=0; i <20; i++)
        {//under 6 is DANGER, under 12 is barely. Since buy is called every-time you receive one you should call it until you have 12. Basically you call it 11 times
            f.receive(new Good(GoodType.GENERIC, f, 0l), null);
        }
        verify(dept,times(11)).buy();





    }

    @Test
    public void InventoryRatingShouldBuy() throws IllegalAccessException     //basically like above it's testing that buy gets called but without stubs so with full functioning market instead
    {

        Market.TESTING_MODE = true;


        Firm f = new Firm(model); f.earn(1000);
        Plant p = new Plant( Blueprint.simpleBlueprint(GoodType.GENERIC,6,GoodType.CAPITAL,1),f);
        p.setPlantMachinery(mock(Machinery.class));
        f.addPlant(p);

        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(1000, f, market, SimpleInventoryControl.class,
                null, null, null);

        f.registerPurchasesDepartment(dept,GoodType.GENERIC);

        BidPricingStrategy stubStrategy = mock(BidPricingStrategy.class); //addSalesDepartmentListener this stub as a new strategy so we can fix prices as we prefer
        when(stubStrategy.maxPrice(GoodType.GENERIC)).thenReturn(80l); //price everything at 80; who cares
        dept.setPricingStrategy(stubStrategy);


        Assert.assertEquals(market.getBestBuyPrice(),-1);
        Assert.assertEquals(market.getBestSellPrice(),-1);


        //addSalesDepartmentListener 10 sellers!
        for(int i=0; i<10; i++) //create 10 buyers!!!!!!!!
        {
            DummySeller seller = new DummySeller(model,10+i*10){

                @Override
                public void earn(long money) throws Bankruptcy {
                    super.earn(money);    //To change body of overridden methods use File | Settings | File Templates.
                    market.deregisterSeller(this);
                }
            };  //these dummies are modified so that if they do trade once, they quit the market entirely
            market.registerSeller(seller);
            Good toSell = new Good(GoodType.GENERIC,seller,0);
            seller.receive(toSell,null);
            market.submitSellQuote(seller,seller.saleQuote,toSell);
        }

        Assert.assertEquals(market.getBestBuyPrice(),-1);
        Assert.assertEquals(market.getBestSellPrice(),10);

        Assert.assertEquals(f.hasHowMany(GoodType.GENERIC),0);
        //you could buy 10 but your max price is 80 so you end up buying only 8!
        dept.buy(); //goooooooooo
        model.getPhaseScheduler().step(model);
        Assert.assertEquals(f.hasHowMany(GoodType.GENERIC),8);

        //when the dust settles...
        Assert.assertEquals(80l,dept.getMarket().getBestBuyPrice()); //there shouldn't be a new buy order!
        Assert.assertEquals(90l,dept.getMarket().getBestSellPrice());  //all the others should have been taken
        Assert.assertEquals(80l,dept.getMarket().getLastPrice());    //although the max price is 80, the buyer defaults to the order book since it's visible

        Assert.assertEquals(640l,dept.getFirm().getCash());   //1000 - 360




        Market.TESTING_MODE = false;
    }


    @Test
    public void gracefulTurnOff() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {

         Market.TESTING_MODE = true;

        Firm f = new Firm(model); f.earn(1000);
        Plant plant = new Plant( Blueprint.simpleBlueprint(GoodType.GENERIC,6,GoodType.CAPITAL,1),f);
        plant.setPlantMachinery(new CRSExponentialMachinery(GoodType.CAPITAL, f, 0, plant, 1));
        f.addPlant(plant);

        PurchasesDepartment dept = null;
        for(int i=0; i<0; i++) //doing this a million times to make sure that an ancient bug doesn't come bag
        //and also to check that inventory listeners are properly disposed of
        {
            dept = PurchasesDepartment.getPurchasesDepartment(1000, f, market, (Class<? extends InventoryControl>) null,
                    null, null, null);
            dept.turnOff();

        }
        dept = PurchasesDepartment.getPurchasesDepartment(1000, f, market, (Class<? extends InventoryControl>) null,
                null, null, null);

        SimpleInventoryControl control = new SimpleInventoryControl(dept);
        dept.setControl(control);

        //make sure it's registered as an inventory listener
        Method method = EconomicAgent.class.getDeclaredMethod("getInventory");
        method.setAccessible(true);
        Inventory inv = (Inventory) method.invoke(f);
        //make sure it is an inventory listener!
        Assert.assertTrue(inv.removeListener(control));
        inv.addListener(control); //re-addSalesDepartmentListener it!


        //check plant listeners
        Field field = Firm.class.getDeclaredField("numberOfPlantsListeners");
        field.setAccessible(true);
        Set<NumberOfPlantsListener> listeners = (Set<NumberOfPlantsListener>) field.get(f);
        //make sure it's registered!
        Assert.assertTrue(listeners.contains(control));




        //now set a new inventory control. dept should call turnoff automatically
        dept.setControl(new FixedInventoryControl(dept));

        //now make sure turn off made the old inventory control
        Assert.assertFalse(inv.removeListener(control));
        Assert.assertFalse(listeners.contains(control));



        //now turn off the last dept and make sure the listeners are empty
        dept.turnOff();
        Assert.assertTrue(listeners.isEmpty());     //no plant creation listeners
        field = Plant.class.getDeclaredField("listeners");
        field.setAccessible(true);
        Set<PlantListener> plantListeners = (Set<PlantListener>) field.get(plant);
        Assert.assertTrue(plantListeners.isEmpty());     //no plant creation listeners








    }

}
