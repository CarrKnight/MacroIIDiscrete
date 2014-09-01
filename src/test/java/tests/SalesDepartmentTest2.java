/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package tests;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentFactory;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.prediction.LookupSalesPredictor;
import agents.firm.sales.prediction.PricingSalesPredictor;
import agents.firm.sales.prediction.SurveySalesPredictor;
import agents.firm.sales.pricing.PriceFollower;
import agents.firm.sales.pricing.UndercuttingAskPricing;
import financial.market.ImmediateOrderHandler;
import financial.market.OrderBookMarket;
import financial.utilities.AveragePricePolicy;
import financial.utilities.BuyerSetPricePolicy;
import financial.utilities.Quote;
import goods.DifferentiatedGoodType;
import goods.Good;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.utilities.dummies.DummyBuyer;
import model.utilities.dummies.DummySeller;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
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
 * @version 2012-07-30
 * @see
 */
public class SalesDepartmentTest2 {

    SalesDepartment dept1;
    PriceFollower strategy1;
    SalesDepartment dept2;
    UndercuttingAskPricing strategy2;
    MacroII model;
    OrderBookMarket market;


    @Before
    public void simpleScenarioSetup(){

        model = new MacroII(0);
        market = new OrderBookMarket(DifferentiatedGoodType.CAPITAL);
        market.setOrderHandler(new ImmediateOrderHandler(),model);
        Firm firm1 = new Firm(model);
        dept1 = SalesDepartmentFactory.incompleteSalesDepartment(firm1, market, new SimpleBuyerSearch(market, firm1), new SimpleSellerSearch(market, firm1), agents.firm.sales.SalesDepartmentAllAtOnce.class);
        firm1.registerSaleDepartment(dept1,market.getGoodType());
        Firm firm2 = new Firm(model);
        dept2 = SalesDepartmentFactory.incompleteSalesDepartment(firm2, market, new SimpleBuyerSearch(market, firm2), new SimpleSellerSearch(market, firm2), agents.firm.sales.SalesDepartmentAllAtOnce.class);
        firm2.registerSaleDepartment(dept2,market.getGoodType());

        for(int i=0; i<10; i++) //create 10 buyers!!!!!!!!
        {
            DummyBuyer buyer = new DummyBuyer(model,10+i*10,market){

                @Override
                public void reactToFilledBidQuote(Quote quoteFilled, Good g, int price, EconomicAgent seller) {
                    super.reactToFilledBidQuote(quoteFilled, g, price, seller);
                    market.deregisterBuyer(this);
                }
            };  //these dummies are modified so that if they do trade once, they quit the market entirely
            buyer.receiveMany(UndifferentiatedGoodType.MONEY,100);
            market.registerBuyer(buyer);
            market.submitBuyQuote(buyer,buyer.quotedPrice);
        }
        market.setPricePolicy(new BuyerSetPricePolicy());


        //set strategies for the department
        dept1.setPredictorStrategy(new SurveySalesPredictor());
        strategy1 = new PriceFollower(dept1);
        dept1.setAskPricingStrategy(strategy1);

        dept2.setPredictorStrategy(new LookupSalesPredictor());
        strategy2 = new UndercuttingAskPricing(dept2);
        dept2.setAskPricingStrategy(strategy2);



    }




    @Test
    public void testPrice() throws Exception {

        for(int i=0; i < 10; i++)
        {
            Good good = Good.getInstanceOfDifferentiatedGood(DifferentiatedGoodType.CAPITAL,dept2.getFirm(),10*i);
            dept2.getFirm().receive(good,null);
            if(i==0)
                assertEquals(0, dept2.price(good));     //defaults to 20% markup for the first
            else if(i==1)
                assertEquals(12, dept2.price(good));     //defaults to 20% markup for the first
            else
                assertEquals((i * 10) * 1.2f, dept2.price(good), .001);  //defaults to 20% markup for the first
        }

    }

    @Test
    public void testSellThis() throws Exception {

        
        dept1.start(model);
        dept2.start(model);
        market.setPricePolicy(new AveragePricePolicy());

        //remove predictors, you don't need them anyway
        dept1.setPredictorStrategy(new PricingSalesPredictor());
        dept2.setPredictorStrategy(new PricingSalesPredictor());

        assertEquals(dept1.getMarket().getBestBuyPrice(), 100); //the best offer is visible
        assertEquals(dept2.getMarket().getBestBuyPrice(), 100); //the best offer is visible


        Good good = Good.getInstanceOfDifferentiatedGood(DifferentiatedGoodType.CAPITAL,dept1.getFirm(),10);
        dept1.getFirm().receive(good,null);
        dept1.sellThis(good); //sell this, now!
        model.start();

        model.schedule.step(model);


        assertEquals(dept1.getMarket().getLastPrice(), 56);


        good = Good.getInstanceOfDifferentiatedGood(DifferentiatedGoodType.CAPITAL,dept1.getFirm(),10);
        dept1.getFirm().receive(good,null);
        dept1.sellThis(good); //sell this, now!
        model.getPhaseScheduler().step(model);


        assertEquals(dept1.getMarket().getLastPrice(), 73);


        good = Good.getInstanceOfDifferentiatedGood(DifferentiatedGoodType.CAPITAL,dept2.getFirm(),10);
        dept2.getFirm().receive(good,null);
        dept2.sellThis(good); //sell this, now!
        model.getPhaseScheduler().step(model);



        assertEquals(dept1.getMarket().getLastPrice(), 46);


        //addSalesDepartmentListener dummy
        DummySeller seller = new DummySeller(dept1.getFirm().getModel(),73);  dept1.getMarket().registerSeller(seller);
        Quote q = dept1.getMarket().submitSellQuote(seller,73,Good.getInstanceOfDifferentiatedGood(DifferentiatedGoodType.CAPITAL,seller,73));


        MacroII fakeModel = mock(MacroII.class);
        strategy2.step(fakeModel); //now it should undercut to 72

        dept1.getMarket().removeSellQuote(q); dept1.getMarket().deregisterSeller(seller);  //exit



        good = Good.getInstanceOfDifferentiatedGood(DifferentiatedGoodType.CAPITAL,dept2.getFirm(),10);
        dept2.getFirm().receive(good,null);
        dept2.sellThis(good); //sell this, now!
        model.getPhaseScheduler().step(model);


        assertEquals(dept1.getMarket().getLastPrice(), 46); //UNSOLD
        assertEquals(dept1.getMarket().getBestBuyPrice(), 70); //UNSOLD




        good = Good.getInstanceOfDifferentiatedGood(DifferentiatedGoodType.CAPITAL,dept2.getFirm(),10);
        dept2.getFirm().receive(good,null);
        dept2.sellThis(good); //sell this, now!
        model.getPhaseScheduler().step(model);


        assertEquals(dept1.getMarket().getLastPrice(), 36); //should have sold both now!
        assertEquals(dept1.getMarket().getBestBuyPrice(), 50);
        assertEquals(dept1.getMarket().getBestSellPrice(), -1);

        good = Good.getInstanceOfDifferentiatedGood(DifferentiatedGoodType.CAPITAL,dept1.getFirm(),10);
        dept1.getFirm().receive(good,null);
        dept1.sellThis(good); //sell this, now!
        model.getPhaseScheduler().step(model);


        assertEquals(dept1.getMarket().getLastPrice(), 43);
        assertEquals(dept1.getMarket().getBestBuyPrice(), 40);
        assertEquals(dept1.getMarket().getBestSellPrice(), -1);

        good = Good.getInstanceOfDifferentiatedGood(DifferentiatedGoodType.CAPITAL,dept1.getFirm(),30);
        dept1.getFirm().receive(good,null);
        dept1.sellThis(good); //sell this, now!
        model.getPhaseScheduler().step(model);


        assertEquals(dept1.getMarket().getLastPrice(), 43);
        assertEquals(dept1.getMarket().getBestBuyPrice(), 40);
        assertEquals(dept1.getMarket().getBestSellPrice(), 43);


        assertEquals(43, dept1.getLastClosingPrice());
        assertEquals(36, dept2.getLastClosingPrice());

        assertEquals(172, dept1.getFirm().hasHowMany(UndifferentiatedGoodType.MONEY));
        assertEquals(123, dept2.getFirm().hasHowMany(UndifferentiatedGoodType.MONEY));




        int size = dept1.getFirm().hasHowMany(DifferentiatedGoodType.CAPITAL);
        assertEquals(1, size); //2 things left to sell




        assertEquals(1, dept1.getFirm().hasHowMany(DifferentiatedGoodType.CAPITAL));





        assertEquals(0, dept2.getFirm().hasHowMany(DifferentiatedGoodType.CAPITAL));




        //NEW BUYER
        DummyBuyer buyer = new DummyBuyer(dept1.getFirm().getModel(),100,market);
        buyer.receiveMany(UndifferentiatedGoodType.MONEY,100);
        dept1.getMarket().registerBuyer(buyer);
        dept1.getMarket().submitBuyQuote(buyer,buyer.quotedPrice);




        assertEquals(0, dept2.getFirm().hasHowMany(DifferentiatedGoodType.CAPITAL));
        assertEquals(dept1.getMarket().getLastPrice(), 72);


        market.setPricePolicy(new BuyerSetPricePolicy());



    }

    @Test
    public void testReactToFilledQuote() throws Exception {


        
        market.setPricePolicy(new AveragePricePolicy());

        //make sure it throws errors when it's not set up correctly
        Good toQuote = Good.getInstanceOfDifferentiatedGood(DifferentiatedGoodType.CAPITAL,dept1.getFirm(),10);
        dept1.start(model);
        dept1.getFirm().receive(toQuote,null);
        //force it in the toSell list
        dept1.setAskPricingStrategy(new PriceFollower(dept1));
        dept1.sellThis(toQuote);
        model.start();
        model.schedule.step(model);




        assertEquals(dept1.getLastClosingPrice(), 56);
        assertEquals(toQuote.getCostOfProduction(), 10);


        market.setPricePolicy(new BuyerSetPricePolicy());



    }

/*
    @Test
    public void testPeddle() throws Exception {

        assertEquals(dept1.getMarket().getBestBuyPrice(),100); //all the others should have been taken

        Field field = SalesDepartment.class.getDeclaredField("toSell");
        field.setAccessible(true);
        Set<Good> toSell = (Set<Good>) field.get (dept1);


        for(int i=0; i<10; i++){
            Good good = new Good(GoodType.GENERIC,dept1.getFirm(),30);
            dept1.getFirm().receive(good,null);
            toSell.addSalesDepartmentListener(good);
            dept1.peddle(good); //sell this, now!
        }



        //the results are the same because the market is small and we are using cost pricing
        assertEquals(2,toSell.size()); //2 things left to sell
        assertEquals(30,dept2.getLastClosingPrice());
        assertEquals(380,dept2.getFirm().hasHowMany(UndifferentiatedGoodType.MONEY)());




        field = SalesDepartment.class.getDeclaredField("salesResults");
        field.setAccessible(true);
        Map<Good,SaleResult> results = (Map<Good, SaleResult>) field.get(dept);
        int successes=0; int failures =0; int quotes=0;
        for(SaleResult result : results.values()){
            if(result.getResult() == SaleResult.Result.SOLD)
                successes++;
            else if(result.getResult() == SaleResult.Result.QUOTED)
                quotes++;
            else
                failures++;

        }
        assertEquals(8,successes); //8 succesfully sold!!
        assertEquals(0,quotes); //2 things left to sell
        assertEquals(2,failures); //2 things left to sell

        assertEquals(2,dept.getFirm().getTotalInventory().size());




        //NEW BUYER
        DummyBuyer buyer = new DummyBuyer(dept.getFirm().getModel(),30);
        buyer.receiveMany(UndifferentiatedGoodType.MONEY,100);
        dept.getMarket().registerBuyer(buyer);
        dept.getMarket().submitBuyQuote(buyer,buyer.quotedPrice);


        //nothing should change
        assertEquals(30,dept.getLastClosingPrice());
        assertEquals(380,dept.getFirm().hasHowMany(UndifferentiatedGoodType.MONEY)());


    }

    @Test
    public void testWeekEnd() throws Exception {
        assertEquals(-1,dept.getLastClosingPrice());
        testSellThis(); //do the usual shenanigans
        assertEquals(30,dept.getLastClosingPrice());
        assertEquals(-1f,dept.getSoldPercentage(),.0001);
        assertEquals(dept.getGrossMargin().size(),0);
        assertEquals(dept.getTotalUnsold().size(),0);
        assertEquals(dept.getTotalSales().size(),0);
        dept.weekEnd();
        assertEquals(.90f,dept.getSoldPercentage());
        assertEquals(dept.getGrossMargin().size(),1);
        assertEquals(dept.getGrossMargin().getFirst().longValue(), 140);
        assertEquals(dept.getTotalUnsold().size(),1);
        assertEquals(dept.getTotalUnsold().getFirst().longValue(), 30);
        assertEquals(dept.getTotalSales().getFirst().longValue(), 410);








    }
       */

}
