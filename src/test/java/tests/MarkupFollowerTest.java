package tests;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentFactory;
import agents.firm.sales.pricing.AskPricingStrategy;
import agents.firm.sales.pricing.MarkupFollower;
import financial.market.Market;
import financial.market.OrderBookMarket;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
public class MarkupFollowerTest {

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
        dept = SalesDepartmentFactory.incompleteSalesDepartment(producer, market, null, null,
                agents.firm.sales.SalesDepartmentAllAtOnce.class); //useless null is useless
        producer.registerSaleDepartment(dept,GoodType.GENERIC);

 //       dept.getFirm().getSalesDepartments().put(GoodType.GENERIC,dept);
        strategyAsk = new MarkupFollower(dept);



    }

    @Test
    public void imitationTest() throws Exception {

        Market.TESTING_MODE = true;
        Firm seller = new Firm(model){
            @Override
            public void reactToFilledAskedQuote(Good g, long price,EconomicAgent agent) {
                //ignore quotes
            }
        }; market.registerSeller(seller);
        Good good =    new Good(GoodType.GENERIC,seller,10l);
        seller.receive(good,null);
        Firm buyer = dept.getFirm(); market.registerBuyer(buyer);
        buyer.registerPurchasesDepartment(mock(PurchasesDepartment.class),GoodType.GENERIC);


        assertEquals(strategyAsk.price(good), 12); //since there is no history, a seller using this strategyAsk would just addSalesDepartmentListener 20%

        buyer.earn(100);


        assertTrue(seller.has(good));
        assertTrue(!buyer.has(good));

        market.submitSellQuote(seller,20l,good);
        market.submitBuyQuote(buyer,20l);

        assertTrue(!seller.has(good));
        assertTrue(buyer.has(good));

        assertEquals(good.getLastValidPrice(), 20l);
        assertEquals(strategyAsk.price(good), 40); //now markup should be 100% and lastValue is 20

        good.setLastValidPrice(100l);
        assertEquals(strategyAsk.price(good), 200); //now markup should be 100% and lastValue is 20



        Market.TESTING_MODE = false;

    }


    @Test
    public void uselessgood1() throws Exception {

        Good good = new Good(GoodType.GENERIC,producer,10);

        long price = strategyAsk.price(good);
        assertEquals(price, 12);
    }

    @Test
    public void uselessgood2() throws Exception {

        Good good = new Good(GoodType.GENERIC,producer,0);

        long price = strategyAsk.price(good);
        assertEquals(price, 0);
    }

    @Test
    public void uselessgood3() throws Exception {

        Good good = new Good(GoodType.GENERIC,producer,10);
        producer.receive(good,null);
        other.earn(100);
        producer.deliver(good,other,50);  other.pay(50,producer,null);
        assertEquals(50, other.getCash());
        assertEquals(50, producer.getCash());




        long price = strategyAsk.price(good);
        assertEquals(price, 60);
    }


    @Test
    public void uselessAdaptiveTest() throws Exception {

        Good good = new Good(GoodType.GENERIC,producer,10);
        producer.receive(good,null);
        other.earn(100);
        producer.deliver(good,other,50);  other.pay(50,producer,null);
        assertEquals(50, other.getCash());
        assertEquals(50, producer.getCash());




        long price = strategyAsk.price(good);
        assertEquals(price, 60);






    }

}
