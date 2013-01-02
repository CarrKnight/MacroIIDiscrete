package tests;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.pricing.UndercuttingAskPricing;
import financial.Market;
import financial.OrderBookMarket;
import goods.Good;
import goods.GoodType;
import junit.framework.Assert;
import model.MacroII;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;

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
public class UndercuttingPricingTest {


    Firm producer;

    Firm other;
    UndercuttingAskPricing strategy;

    SalesDepartment dept;


    Market market;

    MacroII model;


    @Before
    public void setup(){

        Market.TESTING_MODE = true;

        model = new MacroII(10);
        model.setCluelessDefaultMarkup(.20f);
        producer = new Firm(model);
        other = new Firm(model);

        market = new OrderBookMarket(GoodType.GENERIC);
        dept = SalesDepartment.incompleteSalesDepartment(producer,market,null,null); //useless null is useless
        strategy = new UndercuttingAskPricing(dept);


    }

    @Test
    public void imitationTest() throws Exception {
        Market.TESTING_MODE = true;


        Firm seller = new Firm(model){    //don't want to deal with it
            @Override
            public void reactToFilledAskedQuote(Good g, long price, EconomicAgent agent) {
                //ignore quotes
            }
        }; market.registerSeller(seller);
        Good good =    new Good(GoodType.GENERIC,seller,10l);
        seller.receive(good,null);
        Firm buyer = dept.getFirm(); market.registerBuyer(buyer);
        buyer.registerPurchasesDepartment(Mockito.mock(PurchasesDepartment.class),GoodType.GENERIC);

        Assert.assertEquals(strategy.price(good), 12); //with no adjust, it just goes default

        buyer.earn(100);



        Assert.assertTrue(seller.has(good));
        Assert.assertTrue(!buyer.has(good));

        market.submitSellQuote(seller,20l,good);
      //  market.submitBuyQuote(buyer,10l);



        Assert.assertEquals(strategy.price(good), 12);  //with no adjust, it just goes default
        strategy.step(seller.getModel());   //you should have been able to find 20
        Assert.assertEquals(strategy.price(good), 19);  //now just undercut 1% the opponent


        market.submitBuyQuote(buyer,20l);
        Assert.assertTrue(!seller.has(good));
        Assert.assertTrue(buyer.has(good));

        Assert.assertEquals(good.getLastValidPrice(), 20l);
        Assert.assertEquals(strategy.price(good), 20); //now it's forced to 20 by the cost of production

        good.setLastValidPrice(10l);
        Assert.assertEquals(strategy.price(good), 19l); //now it's the undercut.

        good.setLastValidPrice(100l);
        Assert.assertEquals(strategy.price(good), 100l); //fail to copy price and instead price them minimum


        strategy.step(seller.getModel()); //now there is nobody anymore. Go back to ask 20%
        Assert.assertEquals(strategy.price(good), 120l);



        market.submitSellQuote(seller,21l,good);
        strategy.step(seller.getModel());   //you should have been able to find 21 and undercut him instead!
        Assert.assertEquals(strategy.price(good), 100); //limited by the cost of production
        good.setLastValidPrice(1l);
        Assert.assertEquals(strategy.price(good), 20); //now markup should be 100% and lastValue is 20



    }


    @Test
    public void uselessgood1() throws Exception {

        Good good = new Good(GoodType.GENERIC,producer,10);

        long price = strategy.price(good);
        Assert.assertEquals(price, 12);
    }

    @Test
    public void uselessgood2() throws Exception {

        Good good = new Good(GoodType.GENERIC,producer,0);

        long price = strategy.price(good);
        Assert.assertEquals(price,0);
    }

    @Test
    public void uselessgood3() throws Exception {

        Good good = new Good(GoodType.GENERIC,producer,10);
        producer.receive(good,null);
        other.earn(100);
        producer.deliver(good,other,50);  other.pay(50,producer,null);
        Assert.assertEquals(50,other.getCash());
        Assert.assertEquals(50,producer.getCash());




        long price = strategy.price(good);
        Assert.assertEquals(price,60);
    }


    @Test
    public void uselessAdaptiveTest() throws Exception {

        Good good = new Good(GoodType.GENERIC,producer,10);
        producer.receive(good,null);
        other.earn(100);
        producer.deliver(good,other,50);  other.pay(50,producer,null);
        Assert.assertEquals(50,other.getCash());
        Assert.assertEquals(50,producer.getCash());




        long price = strategy.price(good);
        Assert.assertEquals(price,60);


        //now fudge the sales report to get what you need
        Field f = SalesDepartment.class.getDeclaredField("soldPercentage");
        f.setAccessible(true);
        f.set(dept,.45f);
        other.getModel().setMarkupIncreases(.01f);
        for(int i=0; i < 10; i++)
            strategy.weekEnd(); //now it should be 10%
        Assert.assertEquals(60,strategy.price(good));


    }


}
