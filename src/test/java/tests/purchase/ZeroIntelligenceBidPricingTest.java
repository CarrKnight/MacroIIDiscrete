package tests.purchase;

import agents.firm.Firm;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.pricing.ZeroIntelligenceBidPricing;
import ec.util.MersenneTwisterFast;
import financial.Market;
import financial.OrderBookMarket;
import goods.GoodType;
import model.MacroII;
import org.junit.Before;
import org.junit.Test;

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
 * @version 2012-08-11
 * @see
 */
public class ZeroIntelligenceBidPricingTest {


    MacroII model;

    @Before
    public void setup(){
        model = new MacroII(1l);
    }

    @Test
    public void testInBudgetStub()
    {

        PurchasesDepartment dept = mock(PurchasesDepartment.class);
        when(dept.getRandom()).thenReturn(new MersenneTwisterFast());
        when(dept.getAvailableBudget()).thenReturn(100l); //fix the budget
        when(dept.getGoodType()).thenReturn(GoodType.GENERIC);
        ZeroIntelligenceBidPricing pricing = new ZeroIntelligenceBidPricing(dept);

        for(int i=0; i < 10000; i++)
        {
            long maxPrice =pricing.maxPrice(GoodType.GENERIC);
            assertTrue(maxPrice>=0);
            assertTrue(maxPrice<=100);

        }

        //there should be about 10% of the data in any decile
        int decile =0;
        for(int i=0; i < 10000; i++)
        {
            long maxPrice =pricing.maxPrice(GoodType.GENERIC);
            if(maxPrice >= 40 && maxPrice < 50)
                decile++;

        }
        assertTrue(decile >= 900 && decile <= 1100);

    }

    @Test
    public void testInBudgetFull()
    {

        Firm firm = new Firm(model);
        Market market = new OrderBookMarket(GoodType.GENERIC);
        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(100, firm, market,
                null, ZeroIntelligenceBidPricing.class, null, null).getDepartment();

        for(int i=0; i < 10000; i++)
        {
            long maxPrice =dept.maxPrice(GoodType.GENERIC,market);
            assertTrue(maxPrice>=0);
            assertTrue(maxPrice<=100);

        }

        //there should be about 10% of the data in any decile
        int decile =0;
        for(int i=0; i < 10000; i++)
        {
            long maxPrice =dept.maxPrice(GoodType.GENERIC,market);
            if(maxPrice >= 40 && maxPrice < 50)
                decile++;

        }
        assertTrue(decile >= 900 && decile <= 1100);

    }
}
