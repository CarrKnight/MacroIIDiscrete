/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.dummies;

import agents.EconomicAgent;
import financial.market.Market;
import financial.market.OrderBookMarket;
import financial.utilities.Quote;
import financial.utilities.ShopSetPricePolicy;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
 * @author carrknight
 * @version 2013-04-23
 * @see
 */
public class DummyBuyerWithDelayTest
{

    @Test
    public void dummyBuyerWithDelayMarketMock() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Market market = mock(Market.class);
        DummyBuyerWithDelay delay = new DummyBuyerWithDelay(mock(MacroII.class),100,5,market);

        assertTrue(!delay.isRealOffer());
        assertEquals(delay.getFixedPrice(),-1);
        when(market.getBestSellPrice()).thenReturn(-1l);
        when(market.getLastFilledAsk()).thenReturn(50l); //selling for 50, you are willing to pay 100, it should work1
        //get the private method ready
        Method updateMethod = DummyBuyerWithDelay.class.getDeclaredMethod("checkPriceStep", Market.class);
        updateMethod.setAccessible(true);

        for(int i=0; i<4;i++)
        {

            updateMethod.invoke(delay,market);

            assertTrue(!delay.isRealOffer());
            assertEquals(delay.getFixedPrice(),-1);
        }

        //the fifth is the charm!
        updateMethod.invoke(delay,market);

        assertTrue(delay.isRealOffer());
        assertEquals(delay.getFixedPrice(), 100);

        //now it becomes too expensive, but it'll take a bit to notice
        when(market.getLastFilledAsk()).thenReturn(150l);
        for(int i=0; i<4;i++)
        {
            updateMethod.invoke(delay,market);
            assertTrue(delay.isRealOffer());
            assertEquals(delay.getFixedPrice(),100);
        }
        //finally it will show up
        updateMethod.invoke(delay,market);
        assertTrue(!delay.isRealOffer());
        assertEquals(delay.getFixedPrice(),-1);
    }

    //same test as before, but now the market is not a mock, but no trade occurs
    @Test
    public void dummyBuyerWithRealQuotes() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Market market = new OrderBookMarket(GoodType.GENERIC);
        DummyBuyerWithDelay delay = new DummyBuyerWithDelay(mock(MacroII.class),100,5,market);

        assertTrue(!delay.isRealOffer());
        assertEquals(delay.getFixedPrice(), -1);
        EconomicAgent mocki = mock(EconomicAgent.class);
        market.registerSeller(mocki);

        //get the private method ready
        Method updateMethod = DummyBuyerWithDelay.class.getDeclaredMethod("checkPriceStep", Market.class);
        updateMethod.setAccessible(true);

        for(int i=0; i<4;i++)
        {
            Quote q =  market.submitSellQuote(mocki, 100l, new Good(GoodType.GENERIC, mock(EconomicAgent.class), 0l));
            updateMethod.invoke(delay,market);

            assertTrue(!delay.isRealOffer());
            assertEquals(delay.getFixedPrice(),-1);
            market.removeSellQuote(q);
        }

        Quote q1 =  market.submitSellQuote(mocki, 100l, new Good(GoodType.GENERIC, mock(EconomicAgent.class), 0l));

        //the fifth is the charm!
        updateMethod.invoke(delay,market);

        assertTrue(delay.isRealOffer());
        assertEquals(delay.getFixedPrice(),100);
        market.removeSellQuote(q1);


        //now it becomes too expensive, but it'll take a bit to notice
        for(int i=0; i<4;i++)
        {
            Quote q =  market.submitSellQuote(mocki, 150l, new Good(GoodType.GENERIC, mock(EconomicAgent.class), 0l));
            updateMethod.invoke(delay,market);
            assertTrue(delay.isRealOffer());
            assertEquals(delay.getFixedPrice(),100);
            market.removeSellQuote(q);
        }
        //finally it will show up
        q1 =  market.submitSellQuote(mocki, 150l, new Good(GoodType.GENERIC, mock(EconomicAgent.class), 0l));
        updateMethod.invoke(delay,market);
        assertTrue(!delay.isRealOffer());
        assertEquals(delay.getFixedPrice(),-1);
        market.removeSellQuote(q1);

    }
    //same test as before, but now the market is not a mock, and trades occur.
    @Test
    public void dummyBuyerWithRealTrades() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Market market = new OrderBookMarket(GoodType.GENERIC);
        Market.TESTING_MODE = true;
        market.setPricePolicy(new ShopSetPricePolicy());
        DummyBuyerWithDelay delay = new DummyBuyerWithDelay(mock(MacroII.class),100,5,market);

        assertTrue(!delay.isRealOffer());
        assertEquals(delay.getFixedPrice(), -1);
        EconomicAgent mocki = mock(EconomicAgent.class);  when(mocki.has(any(Good.class))).thenReturn(true);
        EconomicAgent buyer = mock(EconomicAgent.class);  when(buyer.hasEnoughCash(anyLong())).thenReturn(true);
        market.registerSeller(mocki);
        market.registerBuyer(buyer);


        //get the private method ready
        Method updateMethod = DummyBuyerWithDelay.class.getDeclaredMethod("checkPriceStep", Market.class);
        updateMethod.setAccessible(true);

        for(int i=0; i<4;i++)
        {
            market.submitSellQuote(mocki, 100l, new Good(GoodType.GENERIC, mock(EconomicAgent.class), 0l));
            market.submitBuyQuote(buyer,1000l);
            updateMethod.invoke(delay,market);

            assertTrue(!delay.isRealOffer());
            assertEquals(delay.getFixedPrice(), -1);
        }

        market.submitSellQuote(mocki, 100l, new Good(GoodType.GENERIC, mock(EconomicAgent.class), 0l));
        market.submitBuyQuote(buyer,1000l);

        //the fifth is the charm!
        updateMethod.invoke(delay,market);

        assertTrue(delay.isRealOffer());
        assertEquals(delay.getFixedPrice(),100);


        //now it becomes too expensive, but it'll take a bit to notice
        for(int i=0; i<4;i++)
        {
            market.submitSellQuote(mocki, 150l, new Good(GoodType.GENERIC, mock(EconomicAgent.class), 0l));
            market.submitBuyQuote(buyer,1000l);

            updateMethod.invoke(delay,market);
            assertTrue(delay.isRealOffer());
            assertEquals(delay.getFixedPrice(),100);
        }
        //finally it will show up
        market.submitSellQuote(mocki, 150l, new Good(GoodType.GENERIC, mock(EconomicAgent.class), 0l));
        market.submitBuyQuote(buyer,1000l);

        updateMethod.invoke(delay,market);
        assertTrue(!delay.isRealOffer());
        assertEquals(delay.getFixedPrice(),-1);
        Market.TESTING_MODE = false;


    }

}
