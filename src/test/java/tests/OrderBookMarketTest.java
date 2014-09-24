/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package tests;

import agents.EconomicAgent;
import agents.people.Person;
import financial.market.ImmediateOrderHandler;
import financial.market.OrderBookMarket;
import financial.utilities.Quote;
import goods.Good;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: carrknight
 * Date: 7/17/12
 * Time: 8:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class OrderBookMarketTest {

    OrderBookMarket market;
    MacroII model;
    Person buyer;
    Person seller;

    @Before
    public void setUp() throws Exception {
        market = new OrderBookMarket(UndifferentiatedGoodType.GENERIC);
        model = new MacroII(1);
        buyer = new Person(model){
            @Override
            public void reactToFilledBidQuote(Quote quoteFilled, Good g, int price, EconomicAgent seller) {

            }
        };
        seller = new Person(model){
            @Override
            public void reactToFilledAskedQuote(Quote quoteFilled, Good g, int price, EconomicAgent seller) {

            }
        };

        market.registerBuyer(buyer);
        market.registerSeller(seller);
        market.setOrderHandler(new ImmediateOrderHandler(),model);

    }

    @Test
    public void testSubmitSellQuote() throws Exception {


        buyer.receiveMany(UndifferentiatedGoodType.MONEY,100);
        Good one =  Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC);
        seller.receive(one,null);
        Good two =  Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC);
        seller.receive(two,null);


        Quote q = market.submitSellQuote(seller, 10, one);
        market.submitSellQuote(seller, 20, two);

        assertTrue(seller.has(one));   //one is owned by seller
        market.submitBuyQuote(buyer,12); //this should start a trade
        assertEquals(market.getBestSellPrice(), 20);
        assertEquals(market.getBestBuyPrice(), -1);
        assertTrue(buyer.has(one));
        assertEquals(seller.hasHowMany(UndifferentiatedGoodType.MONEY), 11); //this should be the price agreed





    }

    @Test
    public void testRemoveSellQuote() throws Exception {

        Good one =  Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC);
        seller.receive(one,null);
        Good two =  Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC);
        seller.receive(two,null);


        Quote q = market.submitSellQuote(seller, 10, one);
        market.submitSellQuote(seller, 20, two);

        assertEquals(market.getBestSellPrice(), 10);
        assertEquals(market.getBestBuyPrice(), -1);

        market.removeSellQuote(q);

        assertEquals(market.getBestSellPrice(), 20);

    }

    @Test (expected=IllegalArgumentException.class)
    public void testRemoveSellQuote2() throws Exception {

        Quote q = null;
        try{
            Good one = Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC);
            seller.receive(one,null);
            Good two =  Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC);
            seller.receive(two,null);


            q = market.submitSellQuote(seller, 10, one);
            market.submitSellQuote(seller, 20, two);


            market.removeSellQuote(q);

        }catch(Exception e){
            fail();   //shouldn't fail here!
        }


        market.removeSellQuote(q);

        fail(); //shouldn't be here
    }

    @Test
    public void testSubmitBuyQuote() throws Exception {
        for(int i=0; i < 5; i++){
            Good g =  Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC);
            seller.receive(g,null);
            market.submitSellQuote(seller,10 * i+10,g);
        }

        assertTrue(buyer.hasHowMany(UndifferentiatedGoodType.GENERIC) == 0);
        assertTrue(seller.hasHowMany(UndifferentiatedGoodType.GENERIC) == 5);

        buyer.receiveMany(UndifferentiatedGoodType.MONEY,100);

        for(int i=0; i < 5; i++)
            market.submitBuyQuote(buyer,30);


        assertEquals(market.getBestSellPrice(), 40);
        assertEquals(market.getBestBuyPrice(), 30);

        assertEquals(buyer.hasHowMany(UndifferentiatedGoodType.GENERIC), 3);
        assertEquals(seller.hasHowMany(UndifferentiatedGoodType.GENERIC), 2);

        assertEquals(buyer.hasHowMany(UndifferentiatedGoodType.MONEY), 25);



    }


    @Test
    public void testIsBestBuyPriceVisible() throws Exception {

        assertTrue(market.isBestBuyPriceVisible());
        assertTrue(market.isBestSalePriceVisible());
    }

    @Test
    public void testGetBestBuyPrice() throws Exception {

        market.submitBuyQuote(buyer,30);
        market.submitBuyQuote(buyer,40);
        market.submitBuyQuote(buyer,20);
        assertEquals(market.getBestBuyPrice(), 40);


    }
}
