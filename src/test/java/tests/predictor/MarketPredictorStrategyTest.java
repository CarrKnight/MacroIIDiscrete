/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package tests.predictor;

import agents.firm.Firm;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentFactory;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.prediction.MarketSalesPredictor;
import agents.firm.sales.prediction.SalesPredictor;
import financial.market.ImmediateOrderHandler;
import financial.market.Market;
import financial.market.OrderBookMarket;
import goods.Good;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.utilities.dummies.DummyBuyer;
import model.utilities.dummies.DummySeller;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
 * @version 2012-07-28
 * @see
 */
public class MarketPredictorStrategyTest {


    SalesDepartment department;
    SalesPredictor strategy;
    MacroII model;
    OrderBookMarket market;
    Firm f;

    /**
     * Create two bids and an ask
     */
    @Test
    public void scenario1() throws Exception {


        model = new MacroII(100);
        market = new OrderBookMarket(UndifferentiatedGoodType.GENERIC);
        f = new Firm(model);
        department = SalesDepartmentFactory.incompleteSalesDepartment(f, market, new SimpleBuyerSearch(market, f), new SimpleSellerSearch(market, f), agents.firm.sales.SalesDepartmentAllAtOnce.class);
        strategy = new MarketSalesPredictor();
        department.setPredictorStrategy(strategy);


        DummyBuyer buyer1 = new DummyBuyer(model,100,market); market.registerBuyer(buyer1);
        market.submitBuyQuote(buyer1,100);
        DummyBuyer buyer2 = new DummyBuyer(model,200,market); market.registerBuyer(buyer2);
        market.submitBuyQuote(buyer2,200);
        DummySeller seller = new DummySeller(model, 300); market.registerSeller(seller);
        market.submitSellQuote(seller,300,Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC));


        assertEquals(-1, strategy.predictSalePriceAfterIncreasingProduction(department, 200, 1),.0001f); //fails because there is no closing price in memory


    }


    //like 1, but let a trade occur first
    @Test
    public void scenario2() throws Exception {




        model = new MacroII(100);
        market = new OrderBookMarket(UndifferentiatedGoodType.GENERIC);
        market.setOrderHandler(new ImmediateOrderHandler(),model);
        f = new Firm(model);
        department = SalesDepartmentFactory.incompleteSalesDepartment(f, market, new SimpleBuyerSearch(market, f), new SimpleSellerSearch(market, f), agents.firm.sales.SalesDepartmentAllAtOnce.class);
        strategy = new MarketSalesPredictor();
        department.setPredictorStrategy(strategy);




        DummyBuyer buyer1 = new DummyBuyer(model,100,market); market.registerBuyer(buyer1);
        market.submitBuyQuote(buyer1,100);
        DummyBuyer buyer2 = new DummyBuyer(model,200,market); market.registerBuyer(buyer2);
        market.submitBuyQuote(buyer2,200);
        DummySeller seller = new DummySeller(model, 300); market.registerSeller(seller);
        market.submitSellQuote(seller,300,Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC));


        Good sold = Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC);
        DummyBuyer buyer3 = new DummyBuyer(model,250,market); market.registerBuyer(buyer3);   buyer3.receiveMany(UndifferentiatedGoodType.MONEY,300);
        market.submitBuyQuote(buyer3,250);
        DummySeller seller2 = new DummySeller(model, 250); market.registerSeller(seller2);
        seller2.receive(sold,null);
        market.submitSellQuote(seller2,250,sold);

        assertTrue(buyer3.has(sold));
        assertTrue(!seller2.has(sold));
        assertEquals(50, buyer3.hasHowMany(UndifferentiatedGoodType.MONEY));
        assertEquals(250, seller2.hasHowMany(UndifferentiatedGoodType.MONEY));

        assertEquals(250, strategy.predictSalePriceAfterIncreasingProduction(department, 200, 1),.0001f); //copy last closing price


    }


    //as 2, but best bid is invisible
    @Test
    public void scenario3() throws Exception {


        model = new MacroII(100);
        market = new OrderBookMarket(UndifferentiatedGoodType.GENERIC){ //break the order book so that the best buyer is not visible anymore
            /**
             * Best bid and asks are visible.
             */
            @Override
            public boolean isBestBuyPriceVisible() {
                return false;
            }
        };
        market.setOrderHandler(new ImmediateOrderHandler(),model);
        f = new Firm(model);
        department = SalesDepartmentFactory.incompleteSalesDepartment(f, market, new SimpleBuyerSearch(market, f), new SimpleSellerSearch(market, f), agents.firm.sales.SalesDepartmentAllAtOnce.class);
        strategy = new MarketSalesPredictor();
        department.setPredictorStrategy(strategy);




        DummyBuyer buyer1 = new DummyBuyer(model,100,market); market.registerBuyer(buyer1);
        market.submitBuyQuote(buyer1,100);
        DummyBuyer buyer2 = new DummyBuyer(model,200,market); market.registerBuyer(buyer2);
        market.submitBuyQuote(buyer2,200);
        DummySeller seller = new DummySeller(model, 300); market.registerSeller(seller);
        market.submitSellQuote(seller,300,Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC));


        Good sold = Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC);
        DummyBuyer buyer3 = new DummyBuyer(model,250,market); market.registerBuyer(buyer3);   buyer3.receiveMany(UndifferentiatedGoodType.MONEY,300);
        market.submitBuyQuote(buyer3,250);
        DummySeller seller2 = new DummySeller(model, 250); market.registerSeller(seller2);
        seller2.receive(sold,null);
        market.submitSellQuote(seller2,250,sold);

        assertTrue(buyer3.has(sold));
        assertTrue(!seller2.has(sold));
        assertEquals(50, buyer3.hasHowMany(UndifferentiatedGoodType.MONEY));
        assertEquals(250, seller2.hasHowMany(UndifferentiatedGoodType.MONEY));

        assertEquals(250, strategy.predictSalePriceAfterIncreasingProduction(department, 200, 1),.0001f); //copy last closing price
        assertEquals(250, department.predictSalePriceAfterIncreasingProduction(200, 1),.0001f); //not overriden this time!

    }


    //empty market
    @Test
    public void scenario4() throws Exception {


        model = new MacroII(100);
        market = new OrderBookMarket(UndifferentiatedGoodType.GENERIC);
        f = new Firm(model);
        department = SalesDepartmentFactory.incompleteSalesDepartment(f, market, new SimpleBuyerSearch(market, f), new SimpleSellerSearch(market, f), agents.firm.sales.SalesDepartmentAllAtOnce.class);
        strategy = new MarketSalesPredictor();
        department.setPredictorStrategy(strategy);

        assertEquals(-1, strategy.predictSalePriceAfterIncreasingProduction(department, 200, 1),.0001f); //useless
        assertEquals(-1, department.predictSalePriceAfterIncreasingProduction(200, 1),.0001f); //useless

    }


    //like scenario 2 but the trade is carried out by us rather than a bystander
    @Test
    public void scenario5() throws Exception {




        model = new MacroII(100);
    //    model.start();
        market = new OrderBookMarket(UndifferentiatedGoodType.GENERIC);
        market.setOrderHandler(new ImmediateOrderHandler(),model);
        f = new Firm(model);
        department = SalesDepartmentFactory.incompleteSalesDepartment(f, market, new SimpleBuyerSearch(market, f), new SimpleSellerSearch(market, f), agents.firm.sales.SalesDepartmentAllAtOnce.class);
        f.registerSaleDepartment(department, UndifferentiatedGoodType.GENERIC);
        strategy = new MarketSalesPredictor();
        department.setPredictorStrategy(strategy);
      //  model.addAgent(f);
        department.start(model);




        DummyBuyer buyer1 = new DummyBuyer(model,100,market); market.registerBuyer(buyer1);
        market.submitBuyQuote(buyer1,100);
        DummyBuyer buyer2 = new DummyBuyer(model,200,market); market.registerBuyer(buyer2);
        market.submitBuyQuote(buyer2,200);
        DummySeller seller = new DummySeller(model, 300); market.registerSeller(seller);
        market.submitSellQuote(seller,300,Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC));


        Good sold = Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC);
        DummyBuyer buyer3 = new DummyBuyer(model,250,market); market.registerBuyer(buyer3);   buyer3.receiveMany(UndifferentiatedGoodType.MONEY,300);
        market.submitBuyQuote(buyer3, 250);
        //market.registerSeller(department.getFirm()); Automatically registered when you create the sales department
        department.getFirm().receive(sold,null);
        //hack to simulate sellThis without actually calling it
        department.sellThis(sold);

        market.submitSellQuote(department.getFirm(),250,sold);

        assertTrue(buyer3.has(sold));
        assertTrue(!f.has(sold));
        assertEquals(50, buyer3.hasHowMany(UndifferentiatedGoodType.MONEY));
        assertEquals(250, f.hasHowMany(UndifferentiatedGoodType.MONEY));

        assertEquals(250, strategy.predictSalePriceAfterIncreasingProduction(department, 200, 1),.0001f); //copy the last closing price (which is our closing price anyway)

    }

}
