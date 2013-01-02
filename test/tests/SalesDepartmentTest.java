package tests;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.sales.SaleResult;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.prediction.SurveySalesPredictor;
import agents.firm.sales.pricing.CostAskPricing;
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

import java.lang.reflect.Field;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * <h4>Description</h4>
 * <p/> 10 buyers, sales department is alone doing stuff.
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
public class SalesDepartmentTest {

    SalesDepartment dept;

    @Before
    public void simpleScenarioSetup(){
        MacroII model = new MacroII(0);
        final Market market = new OrderBookMarket(GoodType.GENERIC);
        Firm firm = new Firm(model);
        dept = SalesDepartment.incompleteSalesDepartment(firm,market,new SimpleBuyerSearch(market,firm),new SimpleSellerSearch(market,firm));
        firm.registerSaleDepartment(dept,market.getGoodType());

        for(int i=0; i<10; i++) //create 10 buyers!!!!!!!!
        {
            DummyBuyer buyer = new DummyBuyer(model,10+i*10){

                @Override
                public void pay(long money, EconomicAgent receiver,Market reason) throws Bankruptcy {
                    super.pay(money, receiver,null);    //To change body of overridden methods use File | Settings | File Templates.
                    market.deregisterBuyer(this);
                }
            };  //these dummies are modified so that if they do trade once, they quit the market entirely
            buyer.earn(100);
            market.registerBuyer(buyer);
            market.submitBuyQuote(buyer,buyer.quotedPrice);
        }

        //set strategies for the department
        dept.setPredictorStrategy(new SurveySalesPredictor());
        dept.setAskPricingStrategy(new CostAskPricing(dept));



    }

    @Test
    public void testAskedForASalePrice() throws NoSuchFieldException, IllegalAccessException{

        DummyBuyer dummy = new DummyBuyer(dept.getFirm().getModel(),100l); //just ask stuff
        Assert.assertEquals(-1,dept.askedForASalePrice(dummy).getPriceQuoted()); //we have no goods, can't answer that question!
        Good toQuote = new Good(GoodType.GENERIC,dept.getFirm(),10);
        dept.getFirm().receive(toQuote,null);
        Assert.assertEquals(-1,dept.askedForASalePrice(dummy).getPriceQuoted()); //the good is still not in the "toSell" list
        //force it in the toSell list
        Field field = SalesDepartment.class.getDeclaredField("toSell");
        field.setAccessible(true);
        Set<Good> toSell = (Set<Good>) field.get (dept);
        toSell.add(toQuote);

        try{
            Assert.assertEquals(10, dept.askedForASalePrice(dummy));    //this should fail because the department doesn't have a quote yet.
            Assert.fail();
        }catch (NoSuchElementException ex){

        }


        Quote q = dept.getMarket().submitSellQuote(dept.getFirm(),1000000,toQuote);
        field = SalesDepartment.class.getDeclaredField("goodsQuotedOnTheMarket");
        field.setAccessible(true);
        Map<Good,Quote> quotes = (Map<Good, Quote>) field.get(dept);
        quotes.put(toQuote,q);
        //now it works!
        Assert.assertEquals(1000000, dept.askedForASalePrice(dummy).getPriceQuoted());    //this should fail because the department doesn't have a quote yet.





    }

    @Test
    public void testPredictSalePrice() throws Exception {

        Assert.assertEquals(100l,dept.predictSalePrice(10)); //just queries the order book.
    }

    @Test
    public void testPrice() throws Exception {

        for(int i=0; i < 10; i++)
        {
            Good good = new Good(GoodType.GENERIC,dept.getFirm(),10*i);
            dept.getFirm().receive(good, null);
            Assert.assertEquals(10*i,dept.price(good));     //cost pricing is easy
        }

    }

    @Test
    public void testSellThis() throws Exception {

        Market.TESTING_MODE = true;


        Assert.assertEquals(dept.getMarket().getBestBuyPrice(),100l); //all the others should have been taken


        for(int i=0; i<10; i++){
            Good good = new Good(GoodType.GENERIC,dept.getFirm(),30);
            dept.getFirm().receive(good,null);
            dept.sellThis(good); //sell this, now!
        }
        //when the dust settles...
        Assert.assertEquals(20l,dept.getMarket().getBestBuyPrice()); //all the others should have been taken
        Assert.assertEquals(30l,dept.getMarket().getBestSellPrice());
        Assert.assertEquals(30l,dept.getMarket().getLastPrice());

        Assert.assertEquals(30l,dept.getLastClosingPrice());
        Assert.assertEquals(380,dept.getFirm().getCash());


        Field field = SalesDepartment.class.getDeclaredField("toSell");
        field.setAccessible(true);
        Set<Good> toSell = (Set<Good>) field.get (dept);
        Assert.assertEquals(2,toSell.size()); //2 things left to sell

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
        Assert.assertEquals(2,quotes); //2 things left to sell
        Assert.assertEquals(0,failures); //2 things left to sell

        Assert.assertEquals(2,dept.getFirm().getTotalInventory().size());




        //NEW BUYER
        DummyBuyer buyer = new DummyBuyer(dept.getFirm().getModel(),30);
        buyer.earn(100);
        dept.getMarket().registerBuyer(buyer);
        dept.getMarket().submitBuyQuote(buyer,buyer.quotedPrice);


        //when the dust settles...
        Assert.assertEquals(20l,dept.getMarket().getBestBuyPrice()); //all the others should have been taken
        Assert.assertEquals(30l,dept.getMarket().getBestSellPrice());
        Assert.assertEquals(30l,dept.getMarket().getLastPrice());

        Assert.assertEquals(30l,dept.getLastClosingPrice());
        Assert.assertEquals(410,dept.getFirm().getCash());




        Assert.assertEquals(1,toSell.size()); //2 things left to sell


        successes=0; failures =0; quotes=0;
        for(SaleResult result : results.values()){
            if(result.getResult() == SaleResult.Result.SOLD)
                successes++;
            else if(result.getResult() == SaleResult.Result.QUOTED)
                quotes++;
            else
                failures++;

        }
        Assert.assertEquals(9,successes); //8 succesfully sold!!
        Assert.assertEquals(1,quotes); //2 things left to sell
        Assert.assertEquals(0,failures); //2 things left to sell

        Assert.assertEquals(1,dept.getFirm().getTotalInventory().size());




    }

    @Test
    public void testReactToFilledQuote() throws Exception {

        Market.TESTING_MODE = true;



        //make sure it throws errors when it's not set up correctly
        Good toQuote = new Good(GoodType.GENERIC,dept.getFirm(),10);
        dept.getFirm().receive(toQuote,null);
        //force it in the toSell list
        dept.setAskPricingStrategy(new PriceFollower(dept));
        dept.sellThis(toQuote);


        Field field = SalesDepartment.class.getDeclaredField("salesResults");
        field.setAccessible(true);
        Map<Good,SaleResult> results = (Map<Good, SaleResult>) field.get(dept);
        Assert.assertEquals(results.size(),1);
        Assert.assertEquals(results.get(toQuote).getPriceSold(),56);
        Assert.assertEquals(results.get(toQuote).getPreviousCost(),10);





    }


    @Test
    public void testPeddle() throws Exception {

        Market.TESTING_MODE = true;


        Assert.assertEquals(dept.getMarket().getBestBuyPrice(),100l); //all the others should have been taken

        Field field = SalesDepartment.class.getDeclaredField("toSell");
        field.setAccessible(true);
        Set<Good> toSell = (Set<Good>) field.get (dept);


        for(int i=0; i<10; i++){
            Good good = new Good(GoodType.GENERIC,dept.getFirm(),30);
            dept.getFirm().receive(good,null);
            toSell.add(good);
            dept.peddle(good); //sell this, now!
        }



        //the results are the same because the market is small and we are using cost pricing
        Assert.assertEquals(2,toSell.size()); //2 things left to sell
        Assert.assertEquals(30l,dept.getLastClosingPrice());
        Assert.assertEquals(380,dept.getFirm().getCash());




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
        Market.TESTING_MODE = true;
        Assert.assertEquals(-1,dept.getLastClosingPrice());
        testSellThis(); //do the usual shenanigans
        Assert.assertEquals(30,dept.getLastClosingPrice());
        Assert.assertEquals(1f,dept.getSoldPercentage(),.0001); //at the beginning it defaults at 100%
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
        Market.TESTING_MODE = false;








    }



    @Test
    public void testUpdateQuotes() throws IllegalAccessException {

        Good g =new Good(GoodType.GENERIC,dept.getFirm(),200);
        dept.getFirm().receive(g,null);

        dept.sellThis(g);
        //should lay unsold
        Assert.assertEquals(dept.getMarket().getBestSellPrice(),200l);
        dept.setAskPricingStrategy(new UndercuttingAskPricing(dept));
        dept.updateQuotes();
        Assert.assertEquals(dept.getMarket().getBestSellPrice(),240l);


    }

}
