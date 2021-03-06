/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package tests.purchase;

import agents.firm.Firm;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.inventoryControl.FixedInventoryControl;
import agents.firm.purchases.inventoryControl.Level;
import agents.firm.purchases.pricing.BidPricingStrategy;
import agents.firm.purchases.pricing.UrgentPriceFollowerStrategy;
import ec.util.MersenneTwisterFast;
import financial.market.Market;
import financial.market.OrderBookMarket;
import goods.Good;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import org.junit.Before;
import org.junit.Test;
import sim.engine.Schedule;

import java.lang.reflect.Field;

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
 * @version 2012-08-11
 * @see
 */
public class UrgentPriceFollowerStrategyTest {

    MacroII model;

    @Before
    public void setup(){
        model = new MacroII(1);
    }


    @Test
    public void testEmptyPricing()
    {
        //when there is nobody to search just go at random.
        PurchasesDepartment dept = mock(PurchasesDepartment.class);
        //search will always return null
        when(dept.getAvailableBudget()).thenReturn(100);
        when(dept.getGoodType()).thenReturn(UndifferentiatedGoodType.GENERIC);

        MersenneTwisterFast random = new MersenneTwisterFast(0);
        when(dept.getRandom()).thenReturn(random);
        //we need a stub market
        Market market = mock(Market.class);
        when(market.getLastPrice()).thenReturn(-1);
        when(dept.getMarket()).thenReturn(market);


        BidPricingStrategy pricing = new UrgentPriceFollowerStrategy(dept);
        for(int i=0; i < 10000; i++) //because there is no last price in the market we get to randomize
        {
            int maxPrice =pricing.maxPrice(UndifferentiatedGoodType.GENERIC);
            assertTrue(maxPrice>=0);
            assertTrue(maxPrice<=100);

        }

        //there should be about 10% of the data in any decile
        int decile =0;
        for(int i=0; i < 10000; i++)
        {
            int maxPrice =pricing.maxPrice(UndifferentiatedGoodType.GENERIC);
            if(maxPrice >= 40 && maxPrice < 50)
                decile++;

        }
        assertTrue(decile >= 900 && decile <= 1100);

    }

    @Test
    public void testEmptyPricingNonStub() throws NoSuchFieldException, IllegalAccessException {
        //when there is nobody to search just go at random.
        Firm f = new Firm(model);
        Market market = new OrderBookMarket(UndifferentiatedGoodType.GENERIC);



        //when there is nobody to search just go at random.
        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(100, f, market, null,
                UrgentPriceFollowerStrategy.class, null, null).getDepartment();
        Field field = PurchasesDepartment.class.getDeclaredField("pricingStrategy");
        field.setAccessible(true);


        BidPricingStrategy pricingStrategy = (BidPricingStrategy) field.get(dept);
        for(int i=0; i < 10000; i++) //because there is no last price in the market we get to randomize
        {
            int maxPrice =pricingStrategy.maxPrice(UndifferentiatedGoodType.GENERIC);
            assertTrue(maxPrice>=0);
            assertTrue(maxPrice<=100);

        }

        //there should be about 10% of the data in any decile
        int decile =0;
        for(int i=0; i < 10000; i++)
        {
            int maxPrice =pricingStrategy.maxPrice(UndifferentiatedGoodType.GENERIC);
            if(maxPrice >= 40 && maxPrice < 50)
                decile++;

        }
        assertTrue(decile >= 900 && decile <= 1100);

    }

    @Test
    public void testUrgencyStub()
    {
        //we are going to check that prices change when urgency chnge

        PurchasesDepartment dept = mock(PurchasesDepartment.class);
        when(dept.getGoodType()).thenReturn(UndifferentiatedGoodType.GENERIC);
        when(dept.getAvailableBudget()).thenReturn(100);
        MersenneTwisterFast random = new MersenneTwisterFast(0);
        when(dept.getRandom()).thenReturn(random);
        //we need a stub market
        Market market = mock(Market.class);
        when(market.getLastPrice()).thenReturn(50); //so last price is 50
        when(dept.getMarket()).thenReturn(market);
        BidPricingStrategy pricing = new UrgentPriceFollowerStrategy(dept);



        //give fake danger signal!
        when(dept.rateCurrentLevel()).thenReturn(Level.DANGER);
        for(int i=0; i<10; i++)
            assertEquals(pricing.maxPrice(UndifferentiatedGoodType.GENERIC),60);

        //give fake barely signal!
        when(dept.rateCurrentLevel()).thenReturn(Level.BARELY);
        for(int i=0; i<10; i++)
            assertEquals(pricing.maxPrice(UndifferentiatedGoodType.GENERIC),50);

        //give fake acceptable signal!
        when(dept.rateCurrentLevel()).thenReturn(Level.ACCEPTABLE);
        for(int i=0; i<10; i++)
            assertEquals(pricing.maxPrice(UndifferentiatedGoodType.GENERIC),40);

        //give fake too much signal!
        when(dept.rateCurrentLevel()).thenReturn(Level.TOOMUCH);
        for(int i=0; i<10; i++)
            assertEquals(pricing.maxPrice(UndifferentiatedGoodType.GENERIC),25);


    }

    @Test
    public void testUrgencyFull() throws NoSuchFieldException, IllegalAccessException {
        //we are going to check that prices change when urgency change
        //the only stub is the market
        Market market = mock(Market.class);
        when(market.getLastPrice()).thenReturn(50); //so last price is 50
        model.schedule = mock(Schedule.class); //we also mock the schedule to avoid the inventory control from spamming buy orders in the schedule


        when(market.getGoodType()).thenReturn(UndifferentiatedGoodType.GENERIC);
        Firm f = new Firm(model);


        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(100, f, market,
                FixedInventoryControl.class, UrgentPriceFollowerStrategy.class, null, null).getDepartment();

        //when there is nobody to search just go at random.
        Field field = PurchasesDepartment.class.getDeclaredField("pricingStrategy");
        field.setAccessible(true);
        BidPricingStrategy pricing = (BidPricingStrategy) field.get(dept);

        //assuming the fixed inventory control wants 6

        //right now it's danger
        for(int i=0; i<10; i++)
            assertEquals(pricing.maxPrice(UndifferentiatedGoodType.GENERIC),60);

        //barely
        for(int i=0; i<3; i++)
            f.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC),null);
        for(int i=0; i<10; i++)
            assertEquals(pricing.maxPrice(UndifferentiatedGoodType.GENERIC),50);

        //acceptable
        for(int i=0; i<3; i++)
            f.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC),null);
        for(int i=0; i<10; i++)
            assertEquals(pricing.maxPrice(UndifferentiatedGoodType.GENERIC),40);

        //too much
        for(int i=0; i<30; i++)
            f.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC),null);
        for(int i=0; i<10; i++)
            assertEquals(pricing.maxPrice(UndifferentiatedGoodType.GENERIC),25);


    }


}
