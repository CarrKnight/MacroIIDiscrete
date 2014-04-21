/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.dummies;

import agents.EconomicAgent;
import financial.market.GeographicalMarket;
import financial.market.ImmediateOrderHandler;
import financial.market.OrderBookMarket;
import financial.utilities.Quote;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
 * @version 2013-11-30
 * @see
 */
public class CustomerTest
{

    private final GeographicalMarket market = mock(GeographicalMarket.class);

    final static private GoodType OIL = new GoodType("oiltest","oil");


    @Before
    public void setUp() throws Exception {
        when(market.getGoodType()).thenReturn(OIL);


        when(market.submitBuyQuote(any(EconomicAgent.class), anyLong())).thenAnswer(invocation ->
                Quote.newBuyerQuote((EconomicAgent)invocation.getArguments()[0],(Long)invocation.getArguments()[1],market.getGoodType()));


    }

    @Test
    public void testThatTheCustomerEatsOilEveryDay()
    {
        //create the model
        MacroII macroII = new MacroII(1l);
        //create the customer
        Customer customer = new Customer(macroII,10,market);
        //give it two units of oil
        customer.receive(new Good(OIL,null,0),null);
        customer.receive(new Good(OIL,null,0),null);
        Assert.assertEquals(customer.hasHowMany(OIL), 2);

        //make one day pass
        macroII.start();
        macroII.schedule.step(macroII);
        //it should have consumed them both!
        Assert.assertEquals(customer.hasHowMany(OIL),0);




    }

    @Test
    public void testThatTheCustomerKeepTheSameAmountOfMoney()
    {
        //target higher than what you have

        //create the model
        MacroII macroII = new MacroII(1l);
        //create the customer
        Customer customer = new Customer(macroII,10,market);
        long targetCash = customer.getResetCashTo() + 100;
        customer.setResetCashTo(targetCash);
        Assert.assertFalse(targetCash == customer.getCash());
        //make one day pass
        macroII.start();
        macroII.schedule.step(macroII);
        //it should have consumed them both!
        Assert.assertEquals(customer.getCash(),targetCash);



        //target lower than what you have
        macroII = new MacroII(1l);
        //create the customer
        customer = new GeographicalCustomer(macroII,10,2,2, market);
        targetCash = customer.getResetCashTo() -1;
        customer.setResetCashTo(targetCash);
        Assert.assertFalse(targetCash == customer.getCash());
        //make one day pass
        macroII.start();
        macroII.schedule.step(macroII);
        //it should have consumed them both!
        Assert.assertEquals(customer.getCash(),targetCash);


    }





    @Test
    public void placingQuotes()
    {
        //daily demand 2, with empty inventory that's 0
        GeographicalMarket geographicalMarket = market;
        MacroII model = new MacroII(1l);
        final         Customer customer = new Customer(model,100,market);
        customer.setDailyDemand(2);
        customer.start(model);
        model.start();
        model.schedule.step(model);

        //doesn't call the remove quotes because it hadn't made any quote
        verify(geographicalMarket,times(0)).removeBuyQuotes(anyCollection());
        //should have placed two quotes
        verify(geographicalMarket,times(2)).submitBuyQuote(customer,100);



        //step again
        model.schedule.step(model);
        //two more quotes, total 4
        verify(geographicalMarket,times(4)).submitBuyQuote(customer,100);
        //and should have removed the quotes twice
        verify(geographicalMarket,times(1)).removeBuyQuotes(anyCollection());

        //now give the customer one unit of good between trade and production
        model.scheduleSoon(ActionOrder.PREPARE_TO_TRADE, new Steppable() {
            @Override
            public void step(SimState state) {
                customer.receive(new Good(OIL,mock(EconomicAgent.class),100l),null);

            }
        });

        //now it should only add one more quote!
        model.schedule.step(model);
        //two more quotes, total 4
        verify(geographicalMarket,times(5)).submitBuyQuote(customer,100);
        //and should have removed the quotes twice
        verify(geographicalMarket,times(2)).removeBuyQuotes(anyCollection());


    }

    @Test
    public void buyAndSell()
    {
        //daily demand 2, with empty inventory that's 0
        OrderBookMarket market = new OrderBookMarket(GoodType.GENERIC);
        MacroII model = new MacroII(1l);
        market.setOrderHandler(new ImmediateOrderHandler(),model);

        final Customer buyer = new Customer(model,100,market);
        buyer.setDailyDemand(2);
        buyer.start(model);
        model.start();
        model.schedule.step(model);

        Assert.assertEquals(0,market.numberOfAsks());
        Assert.assertEquals(2,market.numberOfBids());

        //now have one unit being sold
        DummySeller seller = new DummySeller(model,50); market.registerSeller(seller);
        Good toSell = new Good(GoodType.GENERIC, seller, 0); seller.receive(toSell,null);
        market.submitSellQuote(seller,50, toSell);

        Assert.assertEquals(0,market.numberOfAsks());
        Assert.assertEquals(1,market.numberOfBids());
        Assert.assertEquals(1,buyer.hasHowMany(GoodType.GENERIC));


        //step again
        model.schedule.step(model);

        Assert.assertEquals(0,market.numberOfAsks());
        Assert.assertEquals(2,market.numberOfBids());
        Assert.assertEquals(0,buyer.hasHowMany(GoodType.GENERIC));

    }


    //when we turn the buyer off, all the quotes are gone and the buyer isn't even listed anymore
    @Test
    public void turnOff()
    {
        //daily demand 2, with empty inventory that's 0
        OrderBookMarket market = new OrderBookMarket(GoodType.GENERIC);
        MacroII model = new MacroII(1l);
        final Customer buyer = new Customer(model,100,market);
        buyer.setDailyDemand(2);
        buyer.start(model);
        model.start();
        model.schedule.step(model);

        Assert.assertEquals(1,market.getBuyers().size());
        Assert.assertEquals(2,market.numberOfBids());

        buyer.turnOff();

        Assert.assertEquals(0,market.getBuyers().size());
        Assert.assertEquals(0,market.numberOfBids());


        //step again
        model.schedule.step(model);

        Assert.assertEquals(0,market.getBuyers().size());
        Assert.assertEquals(0,market.numberOfBids());


    }
}
