package tests;

import agents.firm.Firm;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentFactory;
import agents.firm.sales.pricing.AskPricingStrategy;
import agents.firm.sales.pricing.CostAskPricing;
import financial.market.Market;
import financial.market.OrderBookMarket;
import goods.DifferentiatedGoodType;
import goods.Good;
import goods.UndifferentiatedGoodType;
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

        Market market = new OrderBookMarket(DifferentiatedGoodType.CAPITAL);
        SalesDepartment dept = SalesDepartmentFactory.incompleteSalesDepartment(producer, market); //useless null is useless
        strategyAsk = new CostAskPricing(dept);



    }


    @Test
    public void good1() throws Exception {

        Good good = Good.getInstanceOfDifferentiatedGood(DifferentiatedGoodType.CAPITAL,producer,10);

        int price = strategyAsk.price(good);
        assertEquals(price, 10);
    }

    @Test
    public void good2() throws Exception {

        Good good = Good.getInstanceOfDifferentiatedGood(DifferentiatedGoodType.CAPITAL,producer,0);

        int price = strategyAsk.price(good);
        assertEquals(price, 0);
    }

    @Test
    public void good3() throws Exception {

        Good good = Good.getInstanceOfDifferentiatedGood(DifferentiatedGoodType.CAPITAL,producer,10);
        producer.receive(good,null);
        other.receiveMany(UndifferentiatedGoodType.MONEY,100);
        producer.deliver(good,other,50);
        other.deliverMany(UndifferentiatedGoodType.MONEY,producer,50);
        assertEquals(50, other.hasHowMany(UndifferentiatedGoodType.MONEY));
        assertEquals(50, producer.hasHowMany(UndifferentiatedGoodType.MONEY));




        int price = strategyAsk.price(good);
        assertEquals(price, 50);
    }
}
