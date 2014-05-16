/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package financial.market;

import agents.EconomicAgent;
import agents.firm.GeographicalFirm;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentFactory;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.pricing.pid.SalesControlWithFixedInventoryAndPID;
import com.google.common.collect.Multimap;
import financial.utilities.Quote;
import financial.utilities.ShopSetPricePolicy;
import goods.Good;
import goods.GoodType;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.scenario.Scenario;
import model.utilities.ActionOrder;
import model.utilities.dummies.GeographicalCustomer;
import model.utilities.scheduler.Priority;
import model.utilities.stats.collectors.enums.MarketDataType;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import sim.engine.SimState;
import sim.engine.Steppable;

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
 * @version 2013-11-04
 * @see
 */
public class GeographicalMarketTest
{

    final public static GoodType INPUT = new UndifferentiatedGoodType("testInput","Input");


    //make sure it schedule itself correctly
    @Test
    public void testMakeSureItSchedules() throws Exception
    {

        //make sure start() puts it on the schedule
        MacroII mocked = mock(MacroII.class);
        GeographicalMarket market = new GeographicalMarket(INPUT);

        market.start(mocked);
        verify(mocked,times(1)).scheduleSoon(ActionOrder.TRADE,market, Priority.FINAL);
        //now check that step makes it schedule again!
        market.step(mocked);
        verify(mocked,times(1)).scheduleTomorrow(ActionOrder.TRADE,market,Priority.FINAL);

        market.turnOff();
        mocked = mock(MacroII.class);
        market.step(mocked);
        verify(mocked,never()).scheduleTomorrow(ActionOrder.TRADE,market,Priority.FINAL); //shouldn't reschedule


    }

    //simple trade test
    // 1 seller with supply = 2 at price 10
    //3 customers maxPrice 20-30-40, only the last 2 buy
    //this is all mocked so that I can check the right methods are called!
    @Test
    public void testSellsToTheHigherCustomers()
    {
        //model to step
        MacroII macroII = new MacroII(1);

        //geographical market for oil!
        final GeographicalMarket market = new GeographicalMarket(INPUT);
        market.setPricePolicy(new ShopSetPricePolicy());
        market.start(macroII);

        //create the seller
        final GeographicalFirm seller = mock(GeographicalFirm.class);
        market.registerSeller(seller);

        //schedule the seller to sell two goods
        macroII.scheduleSoon(ActionOrder.TRADE, new Steppable() {
            @Override
            public void step(SimState state) {
                for (int i = 0; i < 2; i++) {
                    Good toSell = Good.getInstanceOfUndifferentiatedGood(INPUT);
                    Quote quote = market.submitSellQuote(seller, 10, toSell);
                    when(seller.has(toSell)).thenReturn(true);
                    //it should not have been cleared!
                    Assert.assertTrue(quote.getPriceQuoted() == 10);
                }

            }
        });

        //create 3 fake buyers
        //create and start the 3 buyers
        final GeographicalCustomer customers[] = new GeographicalCustomer[3];
        for(int i=0; i<3; i++)
        {
            customers[i] = mock(GeographicalCustomer.class);
            when(customers[i].getModel()).thenReturn(macroII);
            //if they are ever asked to choose, choose the seller
            when(customers[i].chooseSupplier(any(Multimap.class))).thenReturn(seller);
            //make sure buyer is never bankrupt
            when(customers[i].hasHowMany(UndifferentiatedGoodType.MONEY)).thenReturn(Integer.MAX_VALUE);
            market.registerBuyer(customers[i]);
        }
        //step them so that they place a quote during trade!
        macroII.scheduleSoon(ActionOrder.TRADE,new Steppable() {
            @Override
            public void step(SimState state) {

                for(int i=0; i<3; i++)
                {
                    when(customers[i].getMaxPrice()).thenReturn(Integer.valueOf(20+i*10)); //need this so that the market sorts the customers correctly
                    //make the customers buy
                    Quote quote = market.submitBuyQuote(customers[i],20+i*10);
                    //it should not have been cleared!
                    Assert.assertTrue(quote.getPriceQuoted() ==20+i*10);

                }


            }
        });

        macroII.start();
        macroII.schedule.step(macroII);

        //only the last two customers should have received it!
        verify(customers[1],times(1)).reactToFilledBidQuote(any(), any(Good.class), anyInt(), any(EconomicAgent.class));
        verify(customers[2],times(1)).reactToFilledBidQuote(any(), any(Good.class), anyInt(), any(EconomicAgent.class));
        verify(customers[0],never()).reactToFilledBidQuote(any(), any(Good.class), anyInt(), any(EconomicAgent.class));

    }

    //like above, but the most expensive customer replaces its bid with a new one so that it should be the only one buying
    //simple trade test
    @Test
    public void testSellsToTheHighestCustomer()
    {
        //model to step
        MacroII macroII = new MacroII(1);

        //geographical market for oil!
        final GeographicalMarket market = new GeographicalMarket(INPUT);
        market.setPricePolicy(new ShopSetPricePolicy());
        market.start(macroII);

        //create the seller
        final GeographicalFirm seller = mock(GeographicalFirm.class);
        market.registerSeller(seller);

        //schedule the seller to sell two goods
        macroII.scheduleSoon(ActionOrder.TRADE, new Steppable() {
            @Override
            public void step(SimState state) {
                for (int i = 0; i < 2; i++) {
                    Good toSell = Good.getInstanceOfUndifferentiatedGood(INPUT);
                    Quote quote = market.submitSellQuote(seller, 10, toSell);
                    when(seller.has(toSell)).thenReturn(true);
                    //it should not have been cleared!
                    Assert.assertTrue(quote.getPriceQuoted() == 10);
                }

            }
        });

        //create 3 fake buyers
        //create and start the 3 buyers
        final GeographicalCustomer customers[] = new GeographicalCustomer[3];
        for(int i=0; i<3; i++)
        {
            customers[i] = mock(GeographicalCustomer.class);
            when(customers[i].getModel()).thenReturn(macroII);
            //if they are ever asked to choose, choose the seller
            when(customers[i].chooseSupplier(any(Multimap.class))).thenReturn(seller);
            //make sure buyer is never bankrupt
            when(customers[i].hasHowMany(UndifferentiatedGoodType.MONEY)).thenReturn(Integer.MAX_VALUE);
            market.registerBuyer(customers[i]);
        }
        //step them so that they place a quote during trade!
        macroII.scheduleSoon(ActionOrder.TRADE,new Steppable() {
            @Override
            public void step(SimState state) {

                for(int i=0; i<3; i++)
                {
                    when(customers[i].getMaxPrice()).thenReturn(Integer.valueOf(20+i*10)); //need this so that the market sorts the customers correctly
                    //make the customers buy
                    Quote quote = market.submitBuyQuote(customers[i],20+i*10);
                    //it should not have been cleared!
                    Assert.assertTrue(quote.getPriceQuoted() ==20+i*10);

                }


            }
        });

        //ADDITION:
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                //a new quote!
                Quote quote = market.submitBuyQuote(customers[2],40);
                //it should not have been cleared!
                Assert.assertTrue(quote.getPriceQuoted() ==40);
                return null;
            }
        }).when(customers[2]).reactToFilledBidQuote(any(), any(Good.class), anyInt(), any(EconomicAgent.class));

        macroII.start();
        macroII.schedule.step(macroII);

        //only the last two customers should have received it!
        verify(customers[1],never()).reactToFilledBidQuote(any(), any(Good.class), anyInt(), any(EconomicAgent.class));
        verify(customers[2],times(2)).reactToFilledBidQuote(any(), any(Good.class), anyInt(), any(EconomicAgent.class));
        verify(customers[0],never()).reactToFilledBidQuote(any(), any(Good.class), anyInt(), any(EconomicAgent.class));

    }

    @Test
    public void noSuccesfulSale()
    {

        //the only seller prices its good to high, nobody buys.
        //model to step
        MacroII macroII = new MacroII(1);

        //geographical market for oil!
        final GeographicalMarket market = new GeographicalMarket(INPUT);
        market.setPricePolicy(new ShopSetPricePolicy());
        market.start(macroII);

        //create the seller
        final GeographicalFirm seller = mock(GeographicalFirm.class);
        market.registerSeller(seller);

        //schedule the seller to sell two goods
        macroII.scheduleSoon(ActionOrder.TRADE, new Steppable() {
            @Override
            public void step(SimState state) {
                for (int i = 0; i < 2; i++) {
                    Good toSell = Good.getInstanceOfUndifferentiatedGood(INPUT);
                    Quote quote = market.submitSellQuote(seller, 10000, toSell);
                    when(seller.has(toSell)).thenReturn(true);
                    //it should not have been cleared!
                    Assert.assertTrue(quote.getPriceQuoted() == 10000);
                }

            }
        });

        //create 3 fake buyers
        //create and start the 3 buyers
        final GeographicalCustomer customers[] = new GeographicalCustomer[3];
        for(int i=0; i<3; i++)
        {
            customers[i] = mock(GeographicalCustomer.class);
            //if they are ever asked to choose, choose the seller
            when(customers[i].chooseSupplier(any(Multimap.class))).thenReturn(null);
            //make sure buyer is never bankrupt
            when(customers[i].hasHowMany(UndifferentiatedGoodType.MONEY)).thenReturn(Integer.MAX_VALUE);
            market.registerBuyer(customers[i]);
        }
        //step them so that they place a quote during trade!
        macroII.scheduleSoon(ActionOrder.TRADE,new Steppable() {
            @Override
            public void step(SimState state) {

                for(int i=0; i<3; i++)
                {
                    when(customers[i].getMaxPrice()).thenReturn(Integer.valueOf(20+i*10)); //need this so that the market sorts the customers correctly
                    //make the customers buy
                    Quote quote = market.submitBuyQuote(customers[i],20+i*10);
                    //it should not have been cleared!
                    Assert.assertTrue(quote.getPriceQuoted() ==20+i*10);

                }


            }
        });

        macroII.start();
        macroII.schedule.step(macroII);

        //only the last two customers should have received it!
        verify(customers[1],never()).reactToFilledBidQuote(any(), any(Good.class), anyInt(), any(EconomicAgent.class));
        verify(customers[2],never()).reactToFilledBidQuote(any(), any(Good.class), anyInt(), any(EconomicAgent.class));
        verify(customers[0],never()).reactToFilledBidQuote(any(), any(Good.class), anyInt(), any(EconomicAgent.class));

    }


    //a test with no mocks
    @Test
    public void dressedSimpleSellerTest()
    {

        //the only seller prices its good to high, nobody buys.
        //model to step
        MacroII macroII = new MacroII(1);
        final GeographicalCustomer customers[] = new GeographicalCustomer[100];
        final GeographicalCustomer farCustomers[] = new GeographicalCustomer[100];

        Scenario simpleSellerOilScenario = new Scenario(macroII) {
            @Override
            public void start() {

                //geographical market for oil!
                final GeographicalMarket market = new GeographicalMarket(INPUT);
                market.setPricePolicy(new ShopSetPricePolicy());
                getMarkets().put(INPUT,market);

                //create the seller
                final GeographicalFirm seller =new GeographicalFirm(getModel(),0,0);
                getAgents().add(seller);
                //add the sales department to the seller
                SalesDepartment salesDepartment = SalesDepartmentFactory.incompleteSalesDepartment(seller, market,
                        new SimpleBuyerSearch(market,seller), new SimpleSellerSearch(market,seller), SalesDepartmentOneAtATime.class);
                //give the sale department a simple PID
                salesDepartment.setAskPricingStrategy(new SalesControlWithFixedInventoryAndPID(salesDepartment,50));
                //finally register it!
                seller.registerSaleDepartment(salesDepartment, INPUT);

                //receive 10 units a day!

                //schedule the seller to sell two goods
                getModel().scheduleSoon(ActionOrder.PRODUCTION, new Steppable() {
                    @Override
                    public void step(SimState state) {
                        for (int i = 0; i < 10; i++) {
                            Good toSell = Good.getInstanceOfUndifferentiatedGood(INPUT);
                            seller.receive(toSell,null);
                            seller.reactToPlantProduction(toSell);

                        }
                        getModel().scheduleTomorrow(ActionOrder.PRODUCTION,this);

                    }



                });

                //create 100 fake buyers, with max price from 1 to 100
                //create and start the 3 buyers
                for(int i=0; i<customers.length; i++)
                {
                    customers[i] =new GeographicalCustomer(getModel(),i+1,0,0,market);
                    getAgents().add(customers[i]);
                }
                //create another 100 fake buyers that all want to pay 200 but they are very very far. They shouldn't be able to buy anything!
                for(int i=0; i<farCustomers.length; i++)
                {
                    farCustomers[i] =new GeographicalCustomer(getModel(),200,500,500,market);
                    getAgents().add(farCustomers[i]);
                }
            }
        };
        macroII.setScenario(simpleSellerOilScenario);
        macroII.start();
        Market market = macroII.getMarket(INPUT);
        for(int i=0; i< 3000; i++)
        {
            macroII.schedule.step(macroII); //make 3000 step pass
            System.out.println(market.getLatestObservation(MarketDataType.CLOSING_PRICE));
        }
        //last price should be 90
        Assert.assertEquals(10,market.getLatestObservation(MarketDataType.VOLUME_TRADED),.0001d);
        Assert.assertEquals(91d,market.getLatestObservation(MarketDataType.CLOSING_PRICE),.0001d);

        //all distant agents bought nothing
        for(GeographicalCustomer farCustomer : farCustomers)
            Assert.assertEquals(0,farCustomer.hasHowMany(INPUT));

        //cheap people got nothing
        for(int i=0; i<90; i++)
        {
            Assert.assertEquals(0,customers[i].hasHowMany(INPUT));
        }

        //rich people got everything
        for(int i=90; i<customers.length; i++)
        {
            Assert.assertEquals(1,customers[i].hasHowMany(INPUT));
        }





    }



    //treemap consistency errors
    @Test
    public void samePriceDoesNotMeanSamePerson()
    {
        MacroII macroII = new MacroII(1);
        macroII.start();

        final GeographicalMarket market = new GeographicalMarket(INPUT);
        market.setPricePolicy(new ShopSetPricePolicy());
        market.start(macroII);

        //add two customers with the same price. then remove one. If the backing tree map is done correctly it will not remove both quotes
        final GeographicalCustomer customers[] = new GeographicalCustomer[2];
        customers[0] = new GeographicalCustomer(macroII,100,0,0,market);
        customers[1] = new GeographicalCustomer(macroII,100,0,0,market);
        customers[0].start(macroII);
        customers[1].start(macroII);

        macroII.schedule.step(macroII); //one step

        Assert.assertEquals(1,market.removeAllBuyQuoteByBuyer(customers[0]).size());

    }




}
