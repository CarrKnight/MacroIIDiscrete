/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.dummies;

import agents.EconomicAgent;
import financial.market.EndOfPhaseOrderHandler;
import financial.market.ImmediateOrderHandler;
import financial.market.Market;
import financial.market.OrderBookMarket;
import financial.utilities.Quote;
import financial.utilities.ShopSetPricePolicy;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import model.scenario.Scenario;
import model.utilities.scheduler.Priority;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
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
public class CustomerWithDelayTest
{

    @Test
    public void dummyBuyerWithDelayMarketMock() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Market market = mock(Market.class);
        CustomerWithDelay delay = new CustomerWithDelay(mock(MacroII.class),100,5,market);
        delay.earn(101);


        when(market.getBestSellPrice()).thenReturn(0l);
        when(market.getLastFilledAsk()).thenReturn(50l); //selling for 50, you are willing to pay 100, it should work1
        //get the private method ready
        Method updateMethod = CustomerWithDelay.class.getDeclaredMethod("checkPriceStep", Market.class);
        updateMethod.setAccessible(true);

        for(int i=0; i<4;i++)
        {

            updateMethod.invoke(delay,market);

            assertEquals(delay.getMaxPrice(),-1l);
        }

        //the fifth is the charm!
        updateMethod.invoke(delay,market);

        assertEquals(delay.getMaxPrice(), 100);

        //now it becomes too expensive, but it'll take a bit to notice
        when(market.getBestSellPrice()).thenReturn(150l);
        for(int i=0; i<4;i++)
        {
            updateMethod.invoke(delay, market);
            assertEquals(delay.getMaxPrice(), 100);
        }
        //finally it will show up
        updateMethod.invoke(delay, market);
        assertEquals(delay.getMaxPrice(), -1);
    }

    //same test as before, but now the market is not a mock, but no trade occurs
    @Test
    public void dummyBuyerWithRealQuotes() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Market market = new OrderBookMarket(GoodType.GENERIC);
        CustomerWithDelay delay = new CustomerWithDelay(mock(MacroII.class),100,5,market);
        delay.earn(101);


        assertEquals(delay.getMaxPrice(), -1l);

        EconomicAgent mocki = mock(EconomicAgent.class);
        market.registerSeller(mocki);

        //get the private method ready
        Method updateMethod = CustomerWithDelay.class.getDeclaredMethod("checkPriceStep", Market.class);
        updateMethod.setAccessible(true);

        for(int i=0; i<4;i++)
        {
            Quote q =  market.submitSellQuote(mocki, 100l, new Good(GoodType.GENERIC, mock(EconomicAgent.class), 0l));
            updateMethod.invoke(delay,market);

            assertEquals(delay.getMaxPrice(), -1);

            market.removeSellQuote(q);
        }

        Quote q1 =  market.submitSellQuote(mocki, 100l, new Good(GoodType.GENERIC, mock(EconomicAgent.class), 0l));

        //the fifth is the charm!
        updateMethod.invoke(delay,market);

        assertEquals(delay.getMaxPrice(), 100);

        market.removeSellQuote(q1);


        //now it becomes too expensive, but it'll take a bit to notice
        for(int i=0; i<4;i++)
        {
            Quote q =  market.submitSellQuote(mocki, 150l, new Good(GoodType.GENERIC, mock(EconomicAgent.class), 0l));
            updateMethod.invoke(delay,market);
            assertEquals(delay.getMaxPrice(), 100);

            market.removeSellQuote(q);
        }
        //finally it will show up
        q1 =  market.submitSellQuote(mocki, 150l, new Good(GoodType.GENERIC, mock(EconomicAgent.class), 0l));
        updateMethod.invoke(delay,market);
        assertEquals(delay.getMaxPrice(), -1);

        market.removeSellQuote(q1);

    }
    //same test as before, but now the market is not a mock, and trades occur.
    @Test
    public void dummyBuyerWithRealTrades() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        OrderBookMarket market = new OrderBookMarket(GoodType.GENERIC);
        market.setOrderHandler(new ImmediateOrderHandler(),mock(MacroII.class));
        Market.TESTING_MODE = true;
        market.setPricePolicy(new ShopSetPricePolicy());
        CustomerWithDelay delay = new CustomerWithDelay(mock(MacroII.class),100,5,market);
        delay.earn(101);


        assertEquals(delay.getMaxPrice(), -1);

        EconomicAgent mocki = mock(EconomicAgent.class);  when(mocki.has(any(Good.class))).thenReturn(true);
        EconomicAgent buyer = mock(EconomicAgent.class);  when(buyer.hasEnoughCash(anyLong())).thenReturn(true);
        market.registerSeller(mocki);
        market.registerBuyer(buyer);


        //get the private method ready
        Method updateMethod = CustomerWithDelay.class.getDeclaredMethod("checkPriceStep", Market.class);
        updateMethod.setAccessible(true);

        for(int i=0; i<4;i++)
        {
            market.submitSellQuote(mocki, 100l, new Good(GoodType.GENERIC, mock(EconomicAgent.class), 0l));
            market.submitBuyQuote(buyer,1000l);
            updateMethod.invoke(delay,market);

            assertEquals(delay.getMaxPrice(), -1);

        }

        market.submitSellQuote(mocki, 100l, new Good(GoodType.GENERIC, mock(EconomicAgent.class), 0l));
        market.submitBuyQuote(buyer,1000l);

        //the fifth is the charm!
        updateMethod.invoke(delay,market);

        assertEquals(delay.getMaxPrice(), 100);



        //now it becomes too expensive, but it'll take a bit to notice
        for(int i=0; i<4;i++)
        {
            market.submitSellQuote(mocki, 150l, new Good(GoodType.GENERIC, mock(EconomicAgent.class), 0l));
            market.submitBuyQuote(buyer,1000l);

            updateMethod.invoke(delay,market);
            assertEquals(delay.getMaxPrice(), 100);

        }
        //finally it will show up
        market.submitSellQuote(mocki, 150l, new Good(GoodType.GENERIC, mock(EconomicAgent.class), 0l));
        market.submitBuyQuote(buyer,1000l);

        updateMethod.invoke(delay,market);
        assertEquals(delay.getMaxPrice(), -1);

        Market.TESTING_MODE = false;


    }


    @Test
    public void testmarketTest() throws Exception
    {

        //create 100 delayed customers willing to pay from 0 to 100 and delay of 5
        final MacroII test = new MacroII(System.currentTimeMillis());
        final DailyGoodTree[] seller = new DailyGoodTree[1]; //the trick to keep a reference to it
        Scenario testScenario = new Scenario(test) {
            @Override
            public void start() {

                OrderBookMarket market= new OrderBookMarket(GoodType.GENERIC);
                market.setOrderHandler(new EndOfPhaseOrderHandler(),model);
                getMarkets().put(GoodType.GENERIC,market);
                //buyers
                for(int i =0 ; i<101; i++)
                {
                    CustomerWithDelay delayed = new CustomerWithDelay(test,i,5,market);
                    getAgents().add(delayed);
                }

                //seller
                seller[0] = new DailyGoodTree(model,100,101,market);
                seller[0].setTradePriority(Priority.BEFORE_STANDARD);
                getAgents().add(seller[0]);


            }
        };

        test.setScenario(testScenario);


        //start the model
        test.start();
        Market market = test.getMarket(GoodType.GENERIC);
        //bring gently the price down from 100 to 0
        for(int i=0; i<100; i++){
            seller[0].setMinPrice(100-i);
            test.schedule.step(test);
            System.out.println("price: " + seller[0].getMinPrice() + " --- quantity: " + market.getTodayVolume());
            if(i<4)
            {
                Assert.assertEquals(0,market.getTodayVolume());
            }
            else
            {
                Assert.assertEquals(i-4,market.getTodayVolume());
            }

        }
        System.out.println("going back up!");
        //now bring it back up to 100
        for(int i=0; i<100; i++){
            seller[0].setMinPrice(i+1);
            test.schedule.step(test);
            System.out.println("price: " + seller[0].getMinPrice() + " --- quantity: " + market.getTodayVolume());
            if(i<5)
            {
                switch (i){
                    case 0:
                        Assert.assertEquals(96,market.getTodayVolume());
                        break;
                    case 1:
                        Assert.assertEquals(97,market.getTodayVolume());
                        break;
                    case 2:
                        Assert.assertEquals(98,market.getTodayVolume());
                        break;
                    case 3:
                        Assert.assertEquals(99,market.getTodayVolume());
                        break;
                    case 4:
                        Assert.assertEquals(100,market.getTodayVolume());
                        break;
                    default:
                        Assert.assertTrue(false);

                }
            }
            else
            {
                Assert.assertEquals(100-(i-5),market.getTodayVolume());
            }

        }




    }
}
