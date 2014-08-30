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
import agents.firm.sales.prediction.PricingSalesPredictor;
import agents.firm.sales.prediction.SalesPredictor;
import agents.firm.sales.pricing.UndercuttingAskPricing;
import financial.market.ImmediateOrderHandler;
import financial.market.OrderBookMarket;
import goods.DifferentiatedGoodType;
import goods.Good;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.utilities.dummies.DummyBuyer;
import model.utilities.dummies.DummySeller;
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
 * @version 2012-07-28
 * @see
 */
public class PricingPredictorStrategyTest {


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
        market = new OrderBookMarket(DifferentiatedGoodType.CAPITAL);
        f = new Firm(model);
        department = SalesDepartmentFactory.incompleteSalesDepartment(f, market, new SimpleBuyerSearch(market, f), new SimpleSellerSearch(market, f), agents.firm.sales.SalesDepartmentAllAtOnce.class);
        strategy = new PricingSalesPredictor();
        department.setPredictorStrategy(strategy);
        UndercuttingAskPricing pricing = new UndercuttingAskPricing(department);

        department.setAskPricingStrategy(pricing);

        DummyBuyer buyer1 = new DummyBuyer(model,100,market); market.registerBuyer(buyer1);
        market.submitBuyQuote(buyer1,100);
        DummyBuyer buyer2 = new DummyBuyer(model,200,market); market.registerBuyer(buyer2);
        market.submitBuyQuote(buyer2,200);
        DummySeller seller = new DummySeller(model, 300); market.registerSeller(seller);
        market.submitSellQuote(seller,300,Good.getInstanceOfDifferentiatedGood(DifferentiatedGoodType.CAPITAL,seller,300));



        assertEquals(240, strategy.predictSalePriceAfterIncreasingProduction(department, 200, 1),.0001f); //markup 20% (since you haven't stepped and seen the competition)
        pricing.step(mock(MacroII.class));
        assertEquals(297, strategy.predictSalePriceAfterIncreasingProduction(department, 200, 1),.0001f); //undercut by 1%


    }


    //like 1, but let a trade occur first
    @Test
    public void scenario2() throws Exception {




        model = new MacroII(100);
        market = new OrderBookMarket(DifferentiatedGoodType.CAPITAL);
        market.setOrderHandler(new ImmediateOrderHandler(),model);
        f = new Firm(model);
        department = SalesDepartmentFactory.incompleteSalesDepartment(f, market, new SimpleBuyerSearch(market, f), new SimpleSellerSearch(market, f), agents.firm.sales.SalesDepartmentAllAtOnce.class);
        strategy = new PricingSalesPredictor();
        department.setPredictorStrategy(strategy);
        UndercuttingAskPricing pricing = new UndercuttingAskPricing(department);

        department.setAskPricingStrategy(pricing);



        DummyBuyer buyer1 = new DummyBuyer(model,100,market); market.registerBuyer(buyer1);
        market.submitBuyQuote(buyer1,100);
        DummyBuyer buyer2 = new DummyBuyer(model,200,market); market.registerBuyer(buyer2);
        market.submitBuyQuote(buyer2,200);
        DummySeller seller = new DummySeller(model, 300); market.registerSeller(seller);
        market.submitSellQuote(seller,300,Good.getInstanceOfDifferentiatedGood(DifferentiatedGoodType.CAPITAL,seller,300));


        Good sold = Good.getInstanceOfDifferentiatedGood(DifferentiatedGoodType.CAPITAL,seller,200);
        DummyBuyer buyer3 = new DummyBuyer(model,250,market); market.registerBuyer(buyer3);   buyer3.receiveMany(UndifferentiatedGoodType.MONEY,300);
        market.submitBuyQuote(buyer3,250);
        DummySeller seller2 = new DummySeller(model, 250); market.registerSeller(seller2);
        seller2.receive(sold,null);
        market.submitSellQuote(seller2,250,sold);

        assertTrue(buyer3.has(sold));
        assertTrue(!seller2.has(sold));
        assertEquals(50, buyer3.hasHowMany(UndifferentiatedGoodType.MONEY));
        assertEquals(250, seller2.hasHowMany(UndifferentiatedGoodType.MONEY));

        assertEquals(240, strategy.predictSalePriceAfterIncreasingProduction(department, 200, 1),.0001f); //markup 20% (since you haven't stepped and seen the competition)
        pricing.step(mock(MacroII.class));
        assertEquals(297, strategy.predictSalePriceAfterIncreasingProduction(department, 200,1 ),.0001f); //undercute by 1%


    }


    //as 2, but best bid is invisible
    @Test
    public void scenario3() throws Exception {


        model = new MacroII(100);
        market = new OrderBookMarket(DifferentiatedGoodType.CAPITAL){ //break the order book so that the best buyer is not visible anymore
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

        strategy = new PricingSalesPredictor();
        department.setPredictorStrategy(strategy);
        UndercuttingAskPricing pricing = new UndercuttingAskPricing(department);

        department.setAskPricingStrategy(pricing);



        DummyBuyer buyer1 = new DummyBuyer(model,100,market); market.registerBuyer(buyer1);
        market.submitBuyQuote(buyer1,100);
        DummyBuyer buyer2 = new DummyBuyer(model,200,market); market.registerBuyer(buyer2);
        market.submitBuyQuote(buyer2,200);
        DummySeller seller = new DummySeller(model, 300); market.registerSeller(seller);
        market.submitSellQuote(seller,300,Good.getInstanceOfDifferentiatedGood(DifferentiatedGoodType.CAPITAL,seller,300));


        Good sold = Good.getInstanceOfDifferentiatedGood(DifferentiatedGoodType.CAPITAL,seller,200);
        DummyBuyer buyer3 = new DummyBuyer(model,250,market); market.registerBuyer(buyer3);   buyer3.receiveMany(UndifferentiatedGoodType.MONEY,300);
        market.submitBuyQuote(buyer3,250);
        DummySeller seller2 = new DummySeller(model, 250); market.registerSeller(seller2);
        seller2.receive(sold,null);
        market.submitSellQuote(seller2,250,sold);

        assertTrue(buyer3.has(sold));
        assertTrue(!seller2.has(sold));
        assertEquals(50, buyer3.hasHowMany(UndifferentiatedGoodType.MONEY));
        assertEquals(250, seller2.hasHowMany(UndifferentiatedGoodType.MONEY));

        assertEquals(240, strategy.predictSalePriceAfterIncreasingProduction(department, 200, 1),.0001f); //markup 20% (since you haven't stepped and seen the competition)
        pricing.step(mock(MacroII.class));
        assertEquals(297, strategy.predictSalePriceAfterIncreasingProduction(department, 200,1 ),.0001f); //undercut 1%
        assertEquals(297, department.predictSalePriceAfterIncreasingProduction(200,1 ),.0001f); //not overriden this time!

    }


    //empty market
    @Test
    public void scenario4() throws Exception {


        model = new MacroII(100);
        market = new OrderBookMarket(DifferentiatedGoodType.CAPITAL);
        f = new Firm(model);
        strategy = new PricingSalesPredictor();
        department = SalesDepartmentFactory.incompleteSalesDepartment(f, market, new SimpleBuyerSearch(market, f), new SimpleSellerSearch(market, f), agents.firm.sales.SalesDepartmentAllAtOnce.class);
        department.setPredictorStrategy(strategy);
        UndercuttingAskPricing pricing = new UndercuttingAskPricing(department);

        department.setAskPricingStrategy(pricing) ;
        assertEquals(240, strategy.predictSalePriceAfterIncreasingProduction(department, 200, 1),.0001f); //markup 20% (since you haven't stepped and seen the competition)
        assertEquals(240, department.predictSalePriceAfterIncreasingProduction(200, 1),.0001f); //can't find anything on the order book, better ask the predict strategy

    }


    //like scenario 2 but the trade is carried out by us rather than a bystander
    @Test
    public void scenario5() throws Exception {




        model = new MacroII(100);
        model.start();
        market = new OrderBookMarket(DifferentiatedGoodType.CAPITAL);
        market.setOrderHandler(new ImmediateOrderHandler(),model);
        f = new Firm(model);
        department = SalesDepartmentFactory.incompleteSalesDepartment(f, market, new SimpleBuyerSearch(market, f), new SimpleSellerSearch(market, f), agents.firm.sales.SalesDepartmentAllAtOnce.class);
        f.registerSaleDepartment(department, DifferentiatedGoodType.CAPITAL);
        department.start(model);

        strategy = new PricingSalesPredictor();
        department.setPredictorStrategy(strategy);
        UndercuttingAskPricing pricing = new UndercuttingAskPricing(department);
        department.setAskPricingStrategy(pricing) ;




        DummyBuyer buyer1 = new DummyBuyer(model,100,market); market.registerBuyer(buyer1);
        market.submitBuyQuote(buyer1,100);
        DummyBuyer buyer2 = new DummyBuyer(model,200,market); market.registerBuyer(buyer2);
        market.submitBuyQuote(buyer2,200);
        DummySeller seller = new DummySeller(model, 300); market.registerSeller(seller);
        market.submitSellQuote(seller,300,Good.getInstanceOfDifferentiatedGood(DifferentiatedGoodType.CAPITAL,seller,300));


        Good sold = Good.getInstanceOfDifferentiatedGood(DifferentiatedGoodType.CAPITAL,seller,200);
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

        assertEquals(240, strategy.predictSalePriceAfterIncreasingProduction(department, 200, 1),.0001f); //markup 20% (since you haven't stepped and seen the competition)
        pricing.step(mock(MacroII.class));
        assertEquals(297, strategy.predictSalePriceAfterIncreasingProduction(department, 200, 1),.0001f); //copy the last closing price (which is our closing price anyway)

    }

}
