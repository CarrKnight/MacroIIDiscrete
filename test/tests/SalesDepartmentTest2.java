package tests;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.sales.SaleResult;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.prediction.MemorySalesPredictor;
import agents.firm.sales.prediction.SurveySalesPredictor;
import agents.firm.sales.pricing.PriceFollower;
import agents.firm.sales.pricing.UndercuttingAskPricing;
import financial.Bankruptcy;
import financial.Market;
import financial.OrderBookMarket;
import financial.utilities.Quote;
import goods.Good;
import goods.GoodType;
import junit.framework.Assert;
import model.MacroII;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

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

    @Before
    public void simpleScenarioSetup(){

        model = new MacroII(0);
        final Market market = new OrderBookMarket(GoodType.GENERIC);
        Firm firm1 = new Firm(model);
        dept1 = SalesDepartment.incompleteSalesDepartment(firm1,market,new SimpleBuyerSearch(market,firm1),new SimpleSellerSearch(market,firm1));
        firm1.registerSaleDepartment(dept1,market.getGoodType());
        Firm firm2 = new Firm(model);
        dept2 = SalesDepartment.incompleteSalesDepartment(firm2,market,new SimpleBuyerSearch(market,firm2),new SimpleSellerSearch(market,firm2));
        firm2.registerSaleDepartment(dept2,market.getGoodType());

        for(int i=0; i<10; i++) //create 10 buyers!!!!!!!!
        {
            DummyBuyer buyer = new DummyBuyer(model,10+i*10){

                @Override
                public void pay(long money,@Nonnull EconomicAgent receiver,Market market1) throws Bankruptcy {
                    super.pay(money, receiver,null);    //To change body of overridden methods use File | Settings | File Templates.
                    market.deregisterBuyer(this);
                }
            };  //these dummies are modified so that if they do trade once, they quit the market entirely
            buyer.earn(100);
            market.registerBuyer(buyer);
            market.submitBuyQuote(buyer,buyer.quotedPrice);
        }

        //set strategies for the department
        dept1.setPredictorStrategy(new SurveySalesPredictor());
        strategy1 = new PriceFollower(dept1);
        dept1.setAskPricingStrategy(strategy1);

        dept2.setPredictorStrategy(new MemorySalesPredictor());
        strategy2 = new UndercuttingAskPricing(dept2);
        dept2.setAskPricingStrategy(strategy2);



    }

    @Test
    public void testAskedForASalePrice() throws NoSuchFieldException, IllegalAccessException{

        DummyBuyer dummy = new DummyBuyer(dept1.getFirm().getModel(),100l); //just ask stuff
        Assert.assertEquals(-1, dept1.askedForASalePrice(dummy).getPriceQuoted()); //we have no goods, can't answer that question!
        Good toQuote = new Good(GoodType.GENERIC,dept1.getFirm(),1000l);
        dept1.getFirm().receive(toQuote,null);
        Assert.assertEquals(-1,dept1.askedForASalePrice(dummy).getPriceQuoted()); //the good is still not in the "toSell" list
        //force it in the toSell list
        Field field = SalesDepartment.class.getDeclaredField("toSell");
        field.setAccessible(true);
        Set<Good> toSell = (Set<Good>) field.get (dept1);
        toSell.add(toQuote);

        try{
            Assert.assertEquals(10, dept1.askedForASalePrice(dummy));    //this should fail because the department doesn't have a quote yet.
            Assert.fail();
        }catch (NoSuchElementException ex){

        }


        Quote q = dept1.getMarket().submitSellQuote(dept1.getFirm(),dept1.price(toQuote),toQuote);
        field = SalesDepartment.class.getDeclaredField("goodsQuotedOnTheMarket");
        field.setAccessible(true);
        Map<Good,Quote> quotes = (Map<Good, Quote>) field.get(dept1);
        quotes.put(toQuote,q);
        //now it works!
        Assert.assertEquals(1200, dept1.askedForASalePrice(dummy).getPriceQuoted());    //this just returns the quote, since we can't copy the competition we just return 10+ clueless percentage which is 20%





    }

    @Test
    public void testPredictSalePrice() throws Exception {

        Assert.assertEquals(100l,dept2.predictSalePrice(10)); //just queries the order book.
    }

    @Test
    public void testPrice() throws Exception {

        for(int i=0; i < 10; i++)
        {
            Good good = new Good(GoodType.GENERIC,dept2.getFirm(),10*i);
            dept2.getFirm().receive(good,null);
            if(i==0)
                Assert.assertEquals(0,dept2.price(good));     //defaults to 20% markup for the first
            else if(i==1)
                Assert.assertEquals(12,dept2.price(good));     //defaults to 20% markup for the first
            else
                Assert.assertEquals((i*10)*1.2f,dept2.price(good),.001);  //defaults to 20% markup for the first
        }

    }

    @Test
    public void testSellThis() throws Exception {

        Market.TESTING_MODE = true;

        Assert.assertEquals(dept1.getMarket().getBestBuyPrice(),100l); //the best offer is visible
        Assert.assertEquals(dept2.getMarket().getBestBuyPrice(),100l); //the best offer is visible


        Good good = new Good(GoodType.GENERIC,dept1.getFirm(),10);
        dept1.getFirm().receive(good,null);
        dept1.sellThis(good); //sell this, now!
        model.getPhaseScheduler().step(model);


        Assert.assertEquals(dept1.getMarket().getLastPrice(),56l);


        good = new Good(GoodType.GENERIC,dept1.getFirm(),10);
        dept1.getFirm().receive(good,null);
        dept1.sellThis(good); //sell this, now!
        model.getPhaseScheduler().step(model);


        Assert.assertEquals(dept1.getMarket().getLastPrice(),73l);


        good = new Good(GoodType.GENERIC,dept2.getFirm(),10);
        dept2.getFirm().receive(good,null);
        dept2.sellThis(good); //sell this, now!
        model.getPhaseScheduler().step(model);



        Assert.assertEquals(dept1.getMarket().getLastPrice(),46l);


        //addSalesDepartmentListener dummy
        DummySeller seller = new DummySeller(dept1.getFirm().getModel(),73l);  dept1.getMarket().registerSeller(seller);
        Quote q = dept1.getMarket().submitSellQuote(seller,73l,new Good(GoodType.GENERIC,seller,73l));


        strategy2.step(dept1.getFirm().getModel()); //now it should undercut to 72

        dept1.getMarket().removeSellQuote(q); dept1.getMarket().deregisterSeller(seller);  //exit



        good = new Good(GoodType.GENERIC,dept2.getFirm(),10);
        dept2.getFirm().receive(good,null);
        dept2.sellThis(good); //sell this, now!
        model.getPhaseScheduler().step(model);


        Assert.assertEquals(dept1.getMarket().getLastPrice(),46l); //UNSOLD
        Assert.assertEquals(dept1.getMarket().getBestBuyPrice(),70l); //UNSOLD
        Assert.assertEquals(dept1.getMarket().getBestSellPrice(),72l); //UNSOLD



        strategy2.step(dept1.getFirm().getModel()); //shouldn't undercut himself so it automatically reverts to 12

        good = new Good(GoodType.GENERIC,dept2.getFirm(),10);
        dept2.getFirm().receive(good,null);
        dept2.sellThis(good); //sell this, now!
        model.getPhaseScheduler().step(model);


        Assert.assertEquals(dept1.getMarket().getLastPrice(),36l); //should have sold both now!
        Assert.assertEquals(dept1.getMarket().getBestBuyPrice(),50l);
        Assert.assertEquals(dept1.getMarket().getBestSellPrice(),-1l);

        good = new Good(GoodType.GENERIC,dept1.getFirm(),10);
        dept1.getFirm().receive(good,null);
        dept1.sellThis(good); //sell this, now!
        model.getPhaseScheduler().step(model);


        Assert.assertEquals(dept1.getMarket().getLastPrice(),43l);
        Assert.assertEquals(dept1.getMarket().getBestBuyPrice(),40l);
        Assert.assertEquals(dept1.getMarket().getBestSellPrice(),-1l);

        good = new Good(GoodType.GENERIC,dept1.getFirm(),30);
        dept1.getFirm().receive(good,null);
        dept1.sellThis(good); //sell this, now!
        model.getPhaseScheduler().step(model);


        Assert.assertEquals(dept1.getMarket().getLastPrice(),43l);
        Assert.assertEquals(dept1.getMarket().getBestBuyPrice(),40l);
        Assert.assertEquals(dept1.getMarket().getBestSellPrice(),43l);


        Assert.assertEquals(43l,dept1.getLastClosingPrice());
        Assert.assertEquals(36l,dept2.getLastClosingPrice());

        Assert.assertEquals(172l,dept1.getFirm().getCash());
        Assert.assertEquals(123l,dept2.getFirm().getCash());



        Field field = SalesDepartment.class.getDeclaredField("toSell");
        field.setAccessible(true);
        Set<Good> toSell = (Set<Good>) field.get (dept1);
        Assert.assertEquals(1,toSell.size()); //2 things left to sell

        field = SalesDepartment.class.getDeclaredField("salesResults");
        field.setAccessible(true);
        Map<Good,SaleResult> results = (Map<Good, SaleResult>) field.get(dept1);
        int successes=0; int failures =0; int quotes=0;
        for(SaleResult result : results.values()){
            if(result.getResult() == SaleResult.Result.SOLD)
                successes++;
            else if(result.getResult() == SaleResult.Result.QUOTED)
                quotes++;
            else
                failures++;

        }
        Assert.assertEquals(3,successes); //8 succesfully sold!!
        Assert.assertEquals(1,quotes); //2 things left to sell
        Assert.assertEquals(0,failures); //2 things left to sell

        Assert.assertEquals(1,dept1.getFirm().getTotalInventory().size());


        results = (Map<Good, SaleResult>) field.get(dept2);
        successes=0; failures =0; quotes=0;
        for(SaleResult result : results.values()){
            if(result.getResult() == SaleResult.Result.SOLD)
                successes++;
            else if(result.getResult() == SaleResult.Result.QUOTED)
                quotes++;
            else
                failures++;

        }
        Assert.assertEquals(3,successes); //8 succesfully sold!!
        Assert.assertEquals(0,quotes); //2 things left to sell
        Assert.assertEquals(0,failures); //2 things left to sell

        Assert.assertEquals(0,dept2.getFirm().getTotalInventory().size());




        //NEW BUYER
        DummyBuyer buyer = new DummyBuyer(dept1.getFirm().getModel(),100);
        buyer.earn(100);
        dept1.getMarket().registerBuyer(buyer);
        dept1.getMarket().submitBuyQuote(buyer,buyer.quotedPrice);


        results = (Map<Good, SaleResult>) field.get(dept1);
        successes=0; failures =0; quotes=0;
        for(SaleResult result : results.values()){
            if(result.getResult() == SaleResult.Result.SOLD)
                successes++;
            else if(result.getResult() == SaleResult.Result.QUOTED)
                quotes++;
            else
                failures++;

        }
        Assert.assertEquals(4,successes); //8 succesfully sold!!
        Assert.assertEquals(0,quotes); //2 things left to sell
        Assert.assertEquals(0,failures); //2 things left to sell

        Assert.assertEquals(0,dept2.getFirm().getTotalInventory().size());
        Assert.assertEquals(dept1.getMarket().getLastPrice(),72l);



    }

    @Test
    public void testReactToFilledQuote() throws Exception {


        Market.TESTING_MODE = true;


        //make sure it throws errors when it's not set up correctly
        Good toQuote = new Good(GoodType.GENERIC,dept1.getFirm(),10);
        dept1.getFirm().receive(toQuote,null);
        //force it in the toSell list
        dept1.setAskPricingStrategy(new PriceFollower(dept1));
        dept1.sellThis(toQuote);
        model.getPhaseScheduler().step(model);



        Field field = SalesDepartment.class.getDeclaredField("salesResults");
        field.setAccessible(true);
        Map<Good,SaleResult> results = (Map<Good, SaleResult>) field.get(dept1);
        Assert.assertEquals(results.size(),1);
        Assert.assertEquals(results.get(toQuote).getPriceSold(),56);
        Assert.assertEquals(results.get(toQuote).getPreviousCost(),10);





    }

/*
    @Test
    public void testPeddle() throws Exception {

        Assert.assertEquals(dept1.getMarket().getBestBuyPrice(),100l); //all the others should have been taken

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
        Assert.assertEquals(2,toSell.size()); //2 things left to sell
        Assert.assertEquals(30l,dept2.getLastClosingPrice());
        Assert.assertEquals(380,dept2.getFirm().getCash());




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
        Assert.assertEquals(8,successes); //8 succesfully sold!!
        Assert.assertEquals(0,quotes); //2 things left to sell
        Assert.assertEquals(2,failures); //2 things left to sell

        Assert.assertEquals(2,dept.getFirm().getTotalInventory().size());




        //NEW BUYER
        DummyBuyer buyer = new DummyBuyer(dept.getFirm().getModel(),30);
        buyer.earn(100);
        dept.getMarket().registerBuyer(buyer);
        dept.getMarket().submitBuyQuote(buyer,buyer.quotedPrice);


        //nothing should change
        Assert.assertEquals(30l,dept.getLastClosingPrice());
        Assert.assertEquals(380,dept.getFirm().getCash());


    }

    @Test
    public void testWeekEnd() throws Exception {
        Assert.assertEquals(-1,dept.getLastClosingPrice());
        testSellThis(); //do the usual shenanigans
        Assert.assertEquals(30,dept.getLastClosingPrice());
        Assert.assertEquals(-1f,dept.getSoldPercentage(),.0001);
        Assert.assertEquals(dept.getGrossMargin().size(),0);
        Assert.assertEquals(dept.getTotalUnsold().size(),0);
        Assert.assertEquals(dept.getTotalSales().size(),0);
        dept.weekEnd();
        Assert.assertEquals(.90f,dept.getSoldPercentage());
        Assert.assertEquals(dept.getGrossMargin().size(),1);
        Assert.assertEquals(dept.getGrossMargin().getFirst().longValue(), 140l);
        Assert.assertEquals(dept.getTotalUnsold().size(),1);
        Assert.assertEquals(dept.getTotalUnsold().getFirst().longValue(), 30l);
        Assert.assertEquals(dept.getTotalSales().getFirst().longValue(), 410l);








    }
       */

}
