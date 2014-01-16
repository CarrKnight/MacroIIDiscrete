/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package financial.market;

import agents.EconomicAgent;
import financial.utilities.Quote;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import model.scenario.Scenario;
import model.utilities.dummies.Customer;
import model.utilities.dummies.DailyGoodTree;
import model.utilities.scheduler.Priority;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.Queue;

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
 * @version 2014-01-16
 * @see
 */
public class ImmediateOrderHandlerTest {
    @Test
    public void testMatchQuotes() throws Exception {

        OrderBookMarket market = mock(OrderBookMarket.class);
        EconomicAgent fakeSeller = mock(EconomicAgent.class);
        EconomicAgent fakebuyer = mock(EconomicAgent.class);
        final Good fakeGood = new Good(GoodType.GENERIC, fakeSeller, 100);

        ImmediateOrderHandler orderHandler = new ImmediateOrderHandler();

        //two non crossing quotes:
        Queue<Quote> asks = new LinkedList<>();
        asks.add(Quote.newSellerQuote(fakeSeller,100, fakeGood));
        Queue<Quote> bids = new LinkedList<>();
        bids.add(Quote.newBuyerQuote(fakeSeller, 10, GoodType.GENERIC));
        boolean traded = ImmediateOrderHandler.matchQuotes(asks, bids, market);
        Assert.assertTrue(!traded);
        Assert.assertEquals(1, asks.size());
        Assert.assertEquals(1, bids.size());

        //now two crossing quotes
        bids.clear();
        bids.add(Quote.newBuyerQuote(fakeSeller, 1000, GoodType.GENERIC));
        when(market.price(anyLong(),anyLong())).thenReturn(100l);
        traded = ImmediateOrderHandler.matchQuotes(asks, bids, market);
        Assert.assertTrue(traded);
        Assert.assertEquals(0,asks.size());
        Assert.assertEquals(0,bids.size());
    }


    @Test
    public void marketClearsCorrectly() throws IllegalAccessException {

        MacroII model = new MacroII(10l);
        model.setScenario(new Scenario(model) {
            @Override
            public void start() {
                OrderBookMarket market= new OrderBookMarket(GoodType.GENERIC);
                market.setOrderHandler(new ImmediateOrderHandler(),model);
                getMarkets().put(GoodType.GENERIC,market);

                //20 buyers, from 100 to 120
                for(int i=0; i < 20; i++)
                {
                    Customer customer = new Customer(model,100+i,market);
                    getAgents().add(customer);
                    customer.setTradePriority(Priority.STANDARD);
                }
                //1 seller, pricing 100, trying to sell 10 things!
                DailyGoodTree tree = new DailyGoodTree(model,10,100,market);
                tree.setTradePriority(Priority.STANDARD);
                getAgents().add(tree);
            }
        });
        model.start();

        for(int i=0; i< 100; i++)
        {
            model.schedule.step(model);
            Assert.assertEquals(10, model.getMarket(GoodType.GENERIC).getTodayVolume());
            //this isn't always true: (which is the point of having it clear later or using strange priorities)
        //    Assert.assertEquals(109,model.getMarket(GoodType.GENERIC).getBestBuyPrice());
        }









    }
}

