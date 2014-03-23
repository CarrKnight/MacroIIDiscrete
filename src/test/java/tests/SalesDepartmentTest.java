package tests;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.sales.SaleResult;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentFactory;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.prediction.SurveySalesPredictor;
import agents.firm.sales.pricing.CostAskPricing;
import agents.firm.sales.pricing.PriceFollower;
import agents.firm.sales.pricing.UndercuttingAskPricing;
import financial.Bankruptcy;
import financial.market.Market;
import financial.market.OrderBookMarket;
import financial.utilities.Quote;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import model.utilities.dummies.DummyBuyer;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
    MacroII model;
    Market market;


    @Before
    public void simpleScenarioSetup(){
        model = new MacroII(0);
        market = new OrderBookMarket(GoodType.GENERIC);
        Firm firm = new Firm(model);
        dept = SalesDepartmentFactory.incompleteSalesDepartment(firm, market, new SimpleBuyerSearch(market, firm), new SimpleSellerSearch(market, firm), agents.firm.sales.SalesDepartmentAllAtOnce.class);
        firm.registerSaleDepartment(dept,market.getGoodType());

        for(int i=0; i<10; i++) //create 10 buyers!!!!!!!!
        {
            DummyBuyer buyer = new DummyBuyer(model,10+i*10,market){

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

        DummyBuyer dummy = new DummyBuyer(dept.getFirm().getModel(),100l,market); //just ask stuff
        assertEquals(-1, dept.askedForASalePrice(dummy).getPriceQuoted()); //we have no goods, can't answer that question!
        Good toQuote = new Good(GoodType.GENERIC,dept.getFirm(),10);
        dept.getFirm().receive(toQuote,null);
        assertEquals(-1, dept.askedForASalePrice(dummy).getPriceQuoted()); //the good is still not in the "toSell" list
        //force it in the toSell list
        Field field = SalesDepartment.class.getDeclaredField("toSell");
        field.setAccessible(true);
        LinkedList<Good> toSell = (LinkedList<Good>) field.get (dept);
        toSell.add(toQuote);

        try{
            assertEquals(10, dept.askedForASalePrice(dummy));    //this should fail because the department doesn't have a quote yet.
            fail();
        }catch (NoSuchElementException ex){

        }


        Quote q = dept.getMarket().submitSellQuote(dept.getFirm(),1000000,toQuote);
        field = SalesDepartment.class.getDeclaredField("goodsQuotedOnTheMarket");
        field.setAccessible(true);
        Map<Good,Quote> quotes = (Map<Good, Quote>) field.get(dept);
        quotes.put(toQuote,q);
        //now it works!
        assertEquals(1000000, dept.askedForASalePrice(dummy).getPriceQuoted());    //this should fail because the department doesn't have a quote yet.





    }



    @Test
    public void testPrice() throws Exception {

        for(int i=0; i < 10; i++)
        {
            Good good = new Good(GoodType.GENERIC,dept.getFirm(),10*i);
            dept.getFirm().receive(good, null);
            assertEquals(10 * i, dept.price(good));     //cost pricing is easy
        }

    }

    @Test
    public void testSellThis() throws Exception {

        Market.TESTING_MODE = true;


        assertEquals(dept.getMarket().getBestBuyPrice(), 100l); //all the others should have been taken
        dept.start();

        for(int i=0; i<10; i++){
            Good good = new Good(GoodType.GENERIC,dept.getFirm(),30);
            dept.getFirm().receive(good,null);
            dept.sellThis(good); //sell this, now!
        }
        model.start();

        model.schedule.step(model);


        //when the dust settles...
        assertEquals(20l, dept.getMarket().getBestBuyPrice()); //all the others should have been taken
        assertEquals(30l, dept.getMarket().getBestSellPrice());
        assertEquals(30l, dept.getMarket().getLastPrice());

        assertEquals(30l, dept.getLastClosingPrice());
        assertEquals(380, dept.getFirm().getCash());


        Field field = SalesDepartment.class.getDeclaredField("toSell");
        field.setAccessible(true);
        LinkedList<Good> toSell = (LinkedList<Good>) field.get (dept);
        assertEquals(2, toSell.size()); //2 things left to sell




        assertEquals(2, dept.getFirm().getTotalInventory().size());




        //NEW BUYER
        DummyBuyer buyer = new DummyBuyer(dept.getFirm().getModel(),30,market);
        buyer.earn(100);
        dept.getMarket().registerBuyer(buyer);
        dept.getMarket().submitBuyQuote(buyer,buyer.quotedPrice);


        //when the dust settles...
        assertEquals(20l, dept.getMarket().getBestBuyPrice()); //all the others should have been taken
        assertEquals(30l, dept.getMarket().getBestSellPrice());
        assertEquals(30l, dept.getMarket().getLastPrice());

        assertEquals(30l, dept.getLastClosingPrice());
        assertEquals(410, dept.getFirm().getCash());




        assertEquals(1, toSell.size()); //2 things left to sell




        assertEquals(1, dept.getFirm().getTotalInventory().size());




    }

    @Test
    public void testReactToFilledQuote() throws Exception {

        Market.TESTING_MODE = true;



        //make sure it throws errors when it's not set up correctly
        Good toQuote = new Good(GoodType.GENERIC,dept.getFirm(),10);
        dept.getFirm().receive(toQuote,null);
        //force it in the toSell list
        dept.setAskPricingStrategy(new PriceFollower(dept));
        dept.start();
        dept.sellThis(toQuote);
        model.start();
        model.schedule.step(model);





        assertEquals(dept.getLastClosingPrice(), 56);
        assertEquals(toQuote.getLastValidPrice(), 56);
        assertEquals(toQuote.getCostOfProduction(), 10);





    }


    @Test
    public void testPeddle() throws Exception {

        Market.TESTING_MODE = true;


        assertEquals(dept.getMarket().getBestBuyPrice(), 100l); //all the others should have been taken

        Field field = SalesDepartment.class.getDeclaredField("toSell");
        field.setAccessible(true);
        LinkedList<Good> toSell = (LinkedList<Good>) field.get (dept);


        for(int i=0; i<10; i++){
            Good good = new Good(GoodType.GENERIC,dept.getFirm(),30);
            dept.getFirm().receive(good,null);
            toSell.add(good);
            dept.peddleNow(good); //sell this, now!
        }



        //the results are the same because the market is small and we are using cost pricing
        assertEquals(2, toSell.size()); //2 things left to sell
        assertEquals(30l, dept.getLastClosingPrice());
        assertEquals(380, dept.getFirm().getCash());








        //NEW BUYER
        DummyBuyer buyer = new DummyBuyer(dept.getFirm().getModel(),30,market);
        buyer.earn(100);
        dept.getMarket().registerBuyer(buyer);
        dept.getMarket().submitBuyQuote(buyer,buyer.quotedPrice);


        //nothing should change
        assertEquals(30l, dept.getLastClosingPrice());
        assertEquals(380, dept.getFirm().getCash());


    }




    @Test
    public void testUpdateQuotes() throws IllegalAccessException {

        Good g =new Good(GoodType.GENERIC,dept.getFirm(),200);
        dept.getFirm().receive(g,null);
        dept.start();
        dept.sellThis(g);
        model.start();
        model.schedule.step(model);

        //should lay unsold
        assertEquals(dept.getMarket().getBestSellPrice(), 200l);
        dept.setAskPricingStrategy(new UndercuttingAskPricing(dept));
        dept.updateQuotes();
        model.getPhaseScheduler().step(model);

        assertEquals(dept.getMarket().getBestSellPrice(), 240l);


    }

}
