package tests;

import agents.firm.Firm;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentFactory;
import agents.firm.sales.pricing.AskPricingStrategy;
import agents.firm.sales.pricing.CostAskPricing;
import financial.Market;
import financial.OrderBookMarket;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import org.junit.Before;
import org.junit.Test;

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
public class CostPricingTest {

    Firm producer;

    Firm other;
    AskPricingStrategy strategyAsk;

    @Before
    public void setup(){
        MacroII model = new MacroII(10);
        producer = new Firm(model);
        other = new Firm(model);

        Market market = new OrderBookMarket(GoodType.GENERIC);
        SalesDepartment dept = SalesDepartmentFactory.incompleteSalesDepartment(producer, market); //useless null is useless
        strategyAsk = new CostAskPricing(dept);



    }


    @Test
    public void good1() throws Exception {

        Good good = new Good(GoodType.GENERIC,producer,10);

        long price = strategyAsk.price(good);
        assertEquals(price, 10);
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
        assertEquals(price, 50);
    }
}
