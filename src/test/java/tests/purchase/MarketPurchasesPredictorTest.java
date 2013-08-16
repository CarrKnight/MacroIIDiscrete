package tests.purchase;

import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.prediction.MarketPurchasesPredictor;
import financial.market.Market;
import financial.market.OrderBookBlindMarket;
import goods.GoodType;
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
 * @version 2012-10-04
 * @see
 */
public class MarketPurchasesPredictorTest {
    @Test
    public void mockTest() throws Exception
    {

        Market market = mock(Market.class);
        PurchasesDepartment dept = mock(PurchasesDepartment.class);
        when(dept.getMarket()).thenReturn(market);

        MarketPurchasesPredictor predictor = new MarketPurchasesPredictor();

        for(long i=0; i<100; i++)
        {
            when(market.getLastPrice()).thenReturn(i);
            assertEquals(predictor.predictPurchasePriceWhenIncreasingProduction(dept), i);
        }

    }


    @Test
    public void dressedTest() throws Exception
    {

        Market market = new OrderBookBlindMarket(GoodType.GENERIC);
        PurchasesDepartment dept = MemoryPurchasesPredictorTest.fixedPIDTest(market);
        dept.setLooksAhead(true);

        MarketPurchasesPredictor predictor = new MarketPurchasesPredictor();
        assertTrue(predictor.predictPurchasePriceWhenIncreasingProduction(dept) >= 20 && predictor.predictPurchasePriceWhenIncreasingProduction(dept) <= 30);


    }
}
