package tests;

import agents.firm.Firm;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.pricing.AskPricingStrategy;
import agents.firm.sales.pricing.EverythingMustGoAdaptive;
import financial.Market;
import financial.OrderBookMarket;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

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
public class EverythingMustGoAdaptiveTest {


    Firm producer;

    Firm other;
    AskPricingStrategy strategyAsk;

    SalesDepartment dept;

    @Before
    public void setup(){

        MacroII model = new MacroII(10);
        model.setCluelessDefaultMarkup(.20f);
        producer = new Firm(model);
        other = new Firm(model);

        Market market = new OrderBookMarket(GoodType.GENERIC);
        dept = SalesDepartment.incompleteSalesDepartment(producer,market,null,null); //useless null is useless
        strategyAsk = new EverythingMustGoAdaptive(dept);




    }


    @Test
    public void good1() throws Exception {

        Good good = new Good(GoodType.GENERIC,producer,10);

        long price = strategyAsk.price(good);
        assertEquals(price, 12);
    }

    @Test
    public void good2() throws Exception {

        Good good = new Good(GoodType.GENERIC,producer,0);

        long price = strategyAsk.price(good);
        assertEquals(price, 0);
    }

    @Test
    public void good3() throws Exception {

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
    public void adaptiveTest() throws Exception {

        Good good = new Good(GoodType.GENERIC,producer,10);
        producer.receive(good,null);
        other.earn(100);
        producer.deliver(good,other,50);  other.pay(50,producer,null);
        assertEquals(50, other.getCash());
        assertEquals(50, producer.getCash());




        long price = strategyAsk.price(good);
        assertEquals(price, 60);


        //now fudge the sales report to get what you need
        Field f = SalesDepartment.class.getDeclaredField("soldPercentage");
        f.setAccessible(true);
        f.set(dept,.45f);
        other.getModel().setMarkupIncreases(.01f);
        for(int i=0; i < 10; i++)
            strategyAsk.weekEnd(); //now it should be 10%
        assertEquals(55, strategyAsk.price(good));


    }

}
