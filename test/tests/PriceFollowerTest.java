package tests;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.pricing.AskPricingStrategy;
import agents.firm.sales.pricing.PriceFollower;
import financial.Market;
import financial.OrderBookMarket;
import goods.Good;
import goods.GoodType;
import junit.framework.Assert;
import model.MacroII;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

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
 * @author Ernesto
 * @version 2012-07-26
 * @see
 */
public class PriceFollowerTest {

    Firm producer;

    Firm other;
    AskPricingStrategy strategyAsk;

    SalesDepartment dept;


    Market market;

    MacroII model;


    @Before
    public void setup(){

        model = new MacroII(10);
        model.setCluelessDefaultMarkup(.20f);
        producer = new Firm(model);
        other = new Firm(model);

        market = new OrderBookMarket(GoodType.GENERIC);
        dept = SalesDepartment.incompleteSalesDepartment(producer,market,null,null); //useless null is useless
        producer.registerSaleDepartment(dept,GoodType.GENERIC);
//        dept.getFirm().getSalesDepartments().put(GoodType.GENERIC,dept);
        strategyAsk = new PriceFollower(dept);


    }

    @Test
    public void imitationTest() throws Exception {

        Firm seller = new Firm(model){    //don't want to deal with it
            @Override
            public void reactToFilledAskedQuote(Good g, long price, EconomicAgent agent) {
                //ignore quotes
            }
        }; market.registerSeller(seller);
        Good good =    new Good(GoodType.GENERIC,seller,10l);
        seller.receive(good,null);
        Firm buyer = dept.getFirm(); market.registerBuyer(buyer);
        buyer.registerPurchasesDepartment(mock(PurchasesDepartment.class),GoodType.GENERIC);


        Assert.assertEquals(strategyAsk.price(good), 12); //since there is no history, a seller using this strategyAsk would just addSalesDepartmentListener 20%

        buyer.earn(100);


        Assert.assertTrue(seller.has(good));
        Assert.assertTrue(!buyer.has(good));

        market.submitSellQuote(seller,20l,good);
        market.submitBuyQuote(buyer,20l);

        Assert.assertTrue(!seller.has(good));
        Assert.assertTrue(buyer.has(good));

        Assert.assertEquals(good.getLastValidPrice(), 20l);
        Assert.assertEquals(strategyAsk.price(good), 20); //now markup should be 100% and lastValue is 20

        good.setLastValidPrice(10l);
        Assert.assertEquals(strategyAsk.price(good), 20l); //copy price

        good.setLastValidPrice(100l);
        Assert.assertEquals(strategyAsk.price(good), 100l); //fail to copy price and instead price them minimum



    }


    @Test
    public void uselessgood1() throws Exception {

        Good good = new Good(GoodType.GENERIC,producer,10);

        long price = strategyAsk.price(good);
        Assert.assertEquals(price, 12);
    }

    @Test
    public void uselessgood2() throws Exception {

        Good good = new Good(GoodType.GENERIC,producer,0);

        long price = strategyAsk.price(good);
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




        long price = strategyAsk.price(good);
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




        long price = strategyAsk.price(good);
        Assert.assertEquals(price,60);


        //now fudge the sales report to get what you need
        Field f = SalesDepartment.class.getDeclaredField("soldPercentage");
        f.setAccessible(true);
        f.set(dept,.45f);
        other.getModel().setMarkupIncreases(.01f);
        for(int i=0; i < 10; i++)
            strategyAsk.weekEnd(); //now it should be 10%
        Assert.assertEquals(60, strategyAsk.price(good));


    }


}
