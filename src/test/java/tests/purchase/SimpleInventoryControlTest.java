/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package tests.purchase;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.production.Blueprint;
import agents.firm.production.Plant;
import agents.firm.production.PlantListener;
import agents.firm.production.technology.CRSExponentialMachinery;
import agents.firm.production.technology.Machinery;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.inventoryControl.FixedInventoryControl;
import agents.firm.purchases.inventoryControl.InventoryControl;
import agents.firm.purchases.inventoryControl.Level;
import agents.firm.purchases.inventoryControl.SimpleInventoryControl;
import agents.firm.purchases.pricing.BidPricingStrategy;
import agents.firm.purchases.pricing.decorators.LookAtTheMarketBidPricingDecorator;
import agents.firm.utilities.NumberOfPlantsListener;
import financial.market.Market;
import financial.market.OrderBookMarket;
import financial.utilities.Quote;
import goods.DifferentiatedGoodType;
import goods.Good;
import goods.Inventory;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.utilities.dummies.DummySeller;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Set;

import static org.junit.Assert.*;
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
        model = new MacroII(1);
        market = new OrderBookMarket(UndifferentiatedGoodType.GENERIC);

    }

    //with no plants, it is basically useless
    @Test
    public void stubInventoryRatingNoPlant()
    {
        Firm f = mock(Firm.class);
        //set up the firm so that it returns first 1 then 10 then 5
        when(f.hasHowMany(UndifferentiatedGoodType.GENERIC)).thenReturn(1);
        when(f.getModel()).thenReturn(model);  when(f.getRandom()).thenReturn(model.random);


        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(0, f, market, SimpleInventoryControl.class,
                null, null, null).getDepartment();

        //assuming target inventory is 6~~

        assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);
        when(f.hasHowMany(UndifferentiatedGoodType.GENERIC)).thenReturn(10);
        assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);
        when(f.hasHowMany(UndifferentiatedGoodType.GENERIC)).thenReturn(5);
        assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);

    }

    @Test
    public void InventoryRatingNoPlant()
    {
        Firm f = new Firm(model);



        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(0, f, market, SimpleInventoryControl.class,
                null, null, null).getDepartment();


        //assuming target inventory is 6~~

        f.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC),null);
        assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);
        assertEquals(dept.canBuy(), false);

        for(int i=0; i <9; i++)
            f.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC),null);
        assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);
        assertEquals(dept.canBuy(), false);

        for(int i=0; i <5; i++)
            f.consume(UndifferentiatedGoodType.GENERIC);
        assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);
        assertEquals(dept.canBuy(), false);


    }



    @Test
    public void stubInventoryRating()
    {
        Firm f = mock(Firm.class);
        //set up the firm so that it returns first 1 then 10 then 5
        when(f.getModel()).thenReturn(model);  when(f.getRandom()).thenReturn(model.random);

        Plant p = mock(Plant.class);
        Blueprint b = Blueprint.simpleBlueprint(UndifferentiatedGoodType.GENERIC,6, DifferentiatedGoodType.CAPITAL,1);
        when(p.getBlueprint()).thenReturn(b);
        LinkedList<Plant> list = new LinkedList<>();
        list.add(p);
        when(f.getListOfPlantsUsingSpecificInput(UndifferentiatedGoodType.GENERIC)).thenReturn(list);


        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(0, f, market, SimpleInventoryControl.class,
                null, null, null).getDepartment();

        //assuming target inventory is 6~~

        when(dept.getCurrentInventory()).thenReturn(1);
        assertEquals(dept.rateCurrentLevel(), Level.DANGER);
        when(dept.getCurrentInventory()).thenReturn(10);
        assertEquals(dept.rateCurrentLevel(), Level.BARELY);
        when(dept.getCurrentInventory()).thenReturn(5);
        assertEquals(dept.rateCurrentLevel(), Level.DANGER);

    }

    @Test
    public void InventoryRating()
    {
        Firm f = new Firm(model);
        Plant p = new Plant( Blueprint.simpleBlueprint(UndifferentiatedGoodType.GENERIC,6, DifferentiatedGoodType.CAPITAL,1),f);
        p.setPlantMachinery(mock(Machinery.class));
        f.addPlant(p);


        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(0, f, market, SimpleInventoryControl.class,
                null, null, null).getDepartment();


        //assuming target inventory is 6~~

        f.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC),null);
        assertEquals(dept.rateCurrentLevel(), Level.DANGER);
        assertEquals(dept.canBuy(), true);

        for(int i=0; i <9; i++)
            f.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC),null);
        assertEquals(dept.rateCurrentLevel(), Level.BARELY);
        assertEquals(dept.canBuy(), true);

        for(int i=0; i <5; i++)
            f.consume(UndifferentiatedGoodType.GENERIC);
        assertEquals(dept.rateCurrentLevel(), Level.DANGER);
        assertEquals(dept.canBuy(), true);

        for(int i=0; i <100; i++)
            f.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC),null);
        assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);
        assertEquals(dept.canBuy(), false);

        //now test the listener
        p = new Plant( Blueprint.simpleBlueprint(UndifferentiatedGoodType.GENERIC,100, DifferentiatedGoodType.CAPITAL,1),f);
        p.setPlantMachinery(mock(Machinery.class));
        f.addPlant(p);
        //the new requirement should automatically be put to 100
        assertEquals(dept.rateCurrentLevel(), Level.DANGER);
        assertEquals(dept.canBuy(), true);

        for(int i=0; i <5; i++)
            f.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC), null);
        assertEquals(dept.rateCurrentLevel(), Level.BARELY);
        assertEquals(dept.canBuy(), true);







    }


    //mock testing to count how many times buy() gets called by the inventory control reacting!
    @Test
    public void InventoryRatingShouldBuyMock() throws IllegalAccessException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
        Firm f = new Firm(model);
        Plant p = new Plant( Blueprint.simpleBlueprint(UndifferentiatedGoodType.GENERIC,6, DifferentiatedGoodType.CAPITAL,1),f);
        p.setPlantMachinery(mock(Machinery.class));
        f.addPlant(p);


        PurchasesDepartment dept = PurchasesDepartment.getEmptyPurchasesDepartment(0,f,market,model);
        dept = spy(dept);

        InventoryControl control = new SimpleInventoryControl(dept);
        //make sure it's registered as a listener
        Method method = EconomicAgent.class.getDeclaredMethod("getInventory");
        method.setAccessible(true);
        Inventory inv = (Inventory) method.invoke(f);


        //make sure it is an inventory listener!
        assertTrue(inv.removeListener(control));
        inv.addListener(control); //re-addSalesDepartmentListener it!


        for(int i=0; i <20; i++)
        {//under 6 is DANGER, under 12 is barely. Since buy is called every-time you receive one you should call it until you have 12. Basically you call it 11 times
            f.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC), null);
        }
        verify(dept,times(11)).buy();





    }

    @Test
    public void InventoryRatingShouldBuy() throws IllegalAccessException     //basically like above it's testing that buy gets called but without stubs so with full functioning market instead
    {

        model.start();


        Firm f = new Firm(model); f.receiveMany(UndifferentiatedGoodType.MONEY,1000);
        Plant p = new Plant( Blueprint.simpleBlueprint(UndifferentiatedGoodType.GENERIC,6, DifferentiatedGoodType.CAPITAL,1),f);
        p.setPlantMachinery(mock(Machinery.class));
        f.addPlant(p);

        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(1000, f, market, SimpleInventoryControl.class,
                null, null, null).getDepartment();

        f.registerPurchasesDepartment(dept, UndifferentiatedGoodType.GENERIC);

        BidPricingStrategy stubStrategy = mock(BidPricingStrategy.class); //addSalesDepartmentListener this stub as a new strategy so we can fix prices as we prefer
        when(stubStrategy.maxPrice(UndifferentiatedGoodType.GENERIC)).thenReturn(80); //price everything at 80; who cares
        stubStrategy =  new LookAtTheMarketBidPricingDecorator(stubStrategy,market);
        dept.setPricingStrategy(stubStrategy);


        assertEquals(market.getBestBuyPrice(), -1);
        assertEquals(market.getBestSellPrice(), -1);


        //addSalesDepartmentListener 10 sellers!
        for(int i=0; i<10; i++) //create 10 buyers!!!!!!!!
        {
            DummySeller seller = new DummySeller(model,10+i*10){
                @Override
                public void reactToFilledAskedQuote(Quote quoteFilled, Good g, int price, EconomicAgent buyer) {
                    super.reactToFilledAskedQuote(quoteFilled, g, price, buyer);
                    market.deregisterSeller(this);
                }
            };
            seller.receiveMany(UndifferentiatedGoodType.MONEY,1000000000);

            //these dummies are modified so that if they do trade once, they quit the market entirely
            market.registerSeller(seller);
            Good toSell =Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC);
            seller.receive(toSell,null);
            market.submitSellQuote(seller,seller.saleQuote,toSell);
        }

        assertEquals(market.getBestBuyPrice(), -1);
        assertEquals(market.getBestSellPrice(), 10);

        assertEquals(f.hasHowMany(UndifferentiatedGoodType.GENERIC), 0);
        //you could buy 10 but your max price is 80 so you end up buying only 8!
        dept.buy(); //goooooooooo
        market.start(model);
        model.schedule.step(model);
        assertEquals(f.hasHowMany(UndifferentiatedGoodType.GENERIC), 8);

        //when the dust settles...
        assertEquals(80, dept.getMarket().getBestBuyPrice()); //there shouldn't be a new buy order!
        assertEquals(90, dept.getMarket().getBestSellPrice());  //all the others should have been taken
        assertEquals(80, dept.getMarket().getLastPrice());    //although the max price is 80, the buyer defaults to the order book since it's visible

        assertEquals(640, dept.getFirm().hasHowMany(UndifferentiatedGoodType.MONEY));   //1000 - 360




    }


    @Test
    public void gracefulTurnOff() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {


        Firm f = new Firm(model); f.receiveMany(UndifferentiatedGoodType.MONEY,1000);
        Plant plant = new Plant( Blueprint.simpleBlueprint(UndifferentiatedGoodType.GENERIC,6, DifferentiatedGoodType.CAPITAL,1),f);
        plant.setPlantMachinery(new CRSExponentialMachinery(DifferentiatedGoodType.CAPITAL, f, 0, plant, 1));
        f.addPlant(plant);

        PurchasesDepartment dept = null;
        for(int i=0; i<0; i++) //doing this a million times to make sure that an ancient bug doesn't come bag
        //and also to check that inventory listeners are properly disposed of
        {
            dept = PurchasesDepartment.getPurchasesDepartment(1000, f, market, (Class<? extends InventoryControl>) null,
                    null, null, null).getDepartment();
            dept.turnOff();

        }
        dept = PurchasesDepartment.getPurchasesDepartment(1000, f, market, (Class<? extends InventoryControl>) null,
                null, null, null).getDepartment();

        SimpleInventoryControl control = new SimpleInventoryControl(dept);
        dept.setControl(control);

        //make sure it's registered as an inventory listener
        Method method = EconomicAgent.class.getDeclaredMethod("getInventory");
        method.setAccessible(true);
        Inventory inv = (Inventory) method.invoke(f);
        //make sure it is an inventory listener!
        assertTrue(inv.removeListener(control));
        inv.addListener(control); //re-addSalesDepartmentListener it!


        //check plant listeners
        Field field = Firm.class.getDeclaredField("numberOfPlantsListeners");
        field.setAccessible(true);
        Set<NumberOfPlantsListener> listeners = (Set<NumberOfPlantsListener>) field.get(f);
        //make sure it's registered!
        assertTrue(listeners.contains(control));




        //now set a new inventory control. dept should call turnoff automatically
        dept.setControl(new FixedInventoryControl(dept));

        //now make sure turn off made the old inventory control
        assertFalse(inv.removeListener(control));
        assertFalse(listeners.contains(control));



        //now turn off the last dept and make sure the listeners are empty
        dept.turnOff();
        assertTrue(listeners.isEmpty());     //no plant creation listeners
        field = Plant.class.getDeclaredField("listeners");
        field.setAccessible(true);
        Set<PlantListener> plantListeners = (Set<PlantListener>) field.get(plant);
        assertTrue(plantListeners.isEmpty());     //no plant creation listeners








    }


    @Test
    public void demandGap() throws Exception
    {
        Firm f = mock(Firm.class);
        //set up the firm so that it returns first 1 then 10 then 5
        when(f.getModel()).thenReturn(model);  when(f.getRandom()).thenReturn(model.random);

        Plant p = mock(Plant.class);
        Blueprint b = Blueprint.simpleBlueprint(UndifferentiatedGoodType.GENERIC,6, DifferentiatedGoodType.CAPITAL,1);    //daily needs are 6
        when(p.getBlueprint()).thenReturn(b);
        LinkedList<Plant> list = new LinkedList<>();
        list.add(p);
        when(f.getListOfPlantsUsingSpecificInput(UndifferentiatedGoodType.GENERIC)).thenReturn(list);


        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(0, f, market, SimpleInventoryControl.class,
                null, null, null).getDepartment();

        //assuming target inventory is 6~~

        when(dept.getCurrentInventory()).thenReturn(1);
        assertEquals(-11,dept.estimateDemandGap());
        when(dept.getCurrentInventory()).thenReturn(10);
        assertEquals(-2,dept.estimateDemandGap());
        when(dept.getCurrentInventory()).thenReturn(5);
        assertEquals(-7,dept.estimateDemandGap());
        when(dept.getCurrentInventory()).thenReturn(12);
        assertEquals(0,dept.estimateDemandGap());
        when(dept.getCurrentInventory()).thenReturn(24);
        assertEquals(6,dept.estimateDemandGap());
        when(dept.getCurrentInventory()).thenReturn(25);
        assertEquals(7,dept.estimateDemandGap());

    }
}
