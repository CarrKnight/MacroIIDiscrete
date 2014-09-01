/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package tests;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentFactory;
import agents.firm.sales.pricing.AskPricingStrategy;
import agents.firm.sales.pricing.MarkupFollower;
import financial.market.ImmediateOrderHandler;
import financial.market.OrderBookMarket;
import financial.utilities.Quote;
import goods.DifferentiatedGoodType;
import goods.Good;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

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
 * @version 2012-07-26
 * @see
 */
public class MarkupFollowerTest {

    Firm producer;

    Firm other;
    AskPricingStrategy strategyAsk;

    SalesDepartment dept;


    OrderBookMarket market;

    MacroII model;


    @Before
    public void setup(){

        model = new MacroII(10);
        model.setCluelessDefaultMarkup(.20f);
        producer = new Firm(model);
        other = new Firm(model);

        market = new OrderBookMarket(DifferentiatedGoodType.CAPITAL);
        market.setOrderHandler(new ImmediateOrderHandler(),model);
        dept = SalesDepartmentFactory.incompleteSalesDepartment(producer, market, null, null,
                agents.firm.sales.SalesDepartmentAllAtOnce.class); //useless null is useless
        producer.registerSaleDepartment(dept, DifferentiatedGoodType.CAPITAL);

 //       dept.getFirm().getSalesDepartments().put(GoodType.CAPITAL,dept);
        strategyAsk = new MarkupFollower(dept);



    }

    @Test
    public void imitationTest() throws Exception {

        
        Firm seller = new Firm(model){
            @Override
            public void reactToFilledAskedQuote(Quote quoteFilled, Good g, int price, EconomicAgent agent) {
                //ignore quotes
            }
        }; market.registerSeller(seller);
        Good good =    Good.getInstanceOfDifferentiatedGood(DifferentiatedGoodType.CAPITAL,seller,10);
        seller.receive(good,null);
        Firm buyer = dept.getFirm(); market.registerBuyer(buyer);
        buyer.registerPurchasesDepartment(mock(PurchasesDepartment.class), DifferentiatedGoodType.CAPITAL);


        assertEquals(strategyAsk.price(good), 12); //since there is no history, a seller using this strategyAsk would just addSalesDepartmentListener 20%

        buyer.receiveMany(UndifferentiatedGoodType.MONEY,100);


        assertTrue(seller.has(good));
        assertTrue(!buyer.has(good));

        market.submitSellQuote(seller,20,good);
        market.submitBuyQuote(buyer,20);

        assertTrue(!seller.has(good));
        assertTrue(buyer.has(good));

        assertEquals(good.getLastValidPrice(), 20);
        assertEquals(strategyAsk.price(good), 40); //now markup should be 100% and lastValue is 20

        good.setLastValidPrice(100);
        assertEquals(strategyAsk.price(good), 200); //now markup should be 100% and lastValue is 20




    }


    @Test
    public void uselessgood1() throws Exception {

        Good good = Good.getInstanceOfDifferentiatedGood(DifferentiatedGoodType.CAPITAL,producer,10);

        int price = strategyAsk.price(good);
        assertEquals(price, 12);
    }

    @Test
    public void uselessgood2() throws Exception {

        Good good = Good.getInstanceOfDifferentiatedGood(DifferentiatedGoodType.CAPITAL,producer,0);

        int price = strategyAsk.price(good);
        assertEquals(price, 0);
    }

    @Test
    public void uselessgood3() throws Exception {

        Good good = Good.getInstanceOfDifferentiatedGood(DifferentiatedGoodType.CAPITAL,producer,10);
        producer.receive(good,null);
        other.receiveMany(UndifferentiatedGoodType.MONEY,100);
        producer.deliver(good,other,50);  other.deliverMany(UndifferentiatedGoodType.MONEY,producer,50);
        assertEquals(50, other.hasHowMany(UndifferentiatedGoodType.MONEY));
        assertEquals(50, producer.hasHowMany(UndifferentiatedGoodType.MONEY));




        int price = strategyAsk.price(good);
        assertEquals(price, 60);
    }


    @Test
    public void uselessAdaptiveTest() throws Exception {

        Good good = Good.getInstanceOfDifferentiatedGood(DifferentiatedGoodType.CAPITAL,producer,10);
        producer.receive(good,null);
        other.receiveMany(UndifferentiatedGoodType.MONEY,100);
        producer.deliver(good,other,50);  other.deliverMany(UndifferentiatedGoodType.MONEY,producer,50);
        assertEquals(50, other.hasHowMany(UndifferentiatedGoodType.MONEY));
        assertEquals(50, producer.hasHowMany(UndifferentiatedGoodType.MONEY));




        int price = strategyAsk.price(good);
        assertEquals(price, 60);






    }

}
