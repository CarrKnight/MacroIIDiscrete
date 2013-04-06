package tests.purchase;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.prediction.LookAheadPredictor;
import agents.firm.sales.exploration.SellerSearchAlgorithm;
import agents.firm.sales.exploration.SimpleSellerSearch;
import financial.Market;
import financial.OrderBookBlindMarket;
import financial.OrderBookMarket;
import financial.utilities.Quote;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
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
public class LookAheadPredictorTest {


    @Test
    public void defaultTest() throws Exception {

        //this is exactly the same test as the survey test. This is because lookAhead reverts to survey if the market doesn't allow for the book to be visible
        MacroII model = new MacroII(1l);
        Market market = new OrderBookBlindMarket(GoodType.GENERIC);
        Firm buyer = mock(Firm.class); market.registerBuyer(buyer);   when(buyer.getModel()).thenReturn(model);

        for(int i=0; i<2; i++)
        {
            Firm seller = mock(Firm.class);
           // Good stubGood = new Good(GoodType.GENERIC,mock(Firm.class),0l);
           //  q = Quote.newSellerQuote(seller, 10 + i * 10, stubGood);
            Quote q = mock(Quote.class); when(q.getAgent()).thenReturn(seller); when(q.getPriceQuoted()).thenReturn(10l+i*10l);
            when(seller.askedForASaleQuote(any(EconomicAgent.class), any(GoodType.class))).thenReturn(q);
            market.registerSeller(seller);
        }

        PurchasesDepartment dept = mock(PurchasesDepartment.class);   when(dept.getMarket()).thenReturn(market);
        final SellerSearchAlgorithm algorithm = new SimpleSellerSearch(market,buyer);
        when(dept.getBestSupplierFound()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return algorithm.getBestInSampleSeller();
            }
        });

        LookAheadPredictor predictor = new LookAheadPredictor();
        assertEquals(predictor.predictPurchasePrice(dept), 10);

        Firm seller = mock(Firm.class);
        Quote q = mock(Quote.class); when(q.getAgent()).thenReturn(seller); when(q.getPriceQuoted()).thenReturn(1l);
        when(seller.askedForASaleQuote(any(EconomicAgent.class), any(GoodType.class))).thenReturn(q);
        market.registerSeller(seller);


        assertEquals(predictor.predictPurchasePrice(dept), 1);

    }


    @Test
    public void properTest() throws Exception {

        //this is exactly the same test as the survey test. This is because lookAhead reverts to survey if the market doesn't allow for the book to be visible
        MacroII model = new MacroII(1l);
        Market market = new OrderBookMarket(GoodType.GENERIC);
        Firm buyer = mock(Firm.class); market.registerBuyer(buyer);   when(buyer.getModel()).thenReturn(model);
        LookAheadPredictor predictor = new LookAheadPredictor();

        PurchasesDepartment dept = mock(PurchasesDepartment.class); when(dept.getMarket()).thenReturn(market);
        assertEquals(predictor.predictPurchasePrice(dept), -1); //there is no price!


        for(int i=0; i<5; i++)
        {
            Firm seller = mock(Firm.class); when(seller.has(any(Good.class))).thenReturn(true);
            market.registerSeller(seller);
            market.submitSellQuote(seller,10+i*10,mock(Good.class));

        }



        assertEquals(predictor.predictPurchasePrice(dept), 10);

        Firm seller = mock(Firm.class);
        Good stubGood = mock(Good.class); when(stubGood.getType()).thenReturn(GoodType.GENERIC);   Quote q = Quote.newSellerQuote(seller, 1, stubGood);
        when(seller.askedForASaleQuote(any(EconomicAgent.class), any(GoodType.class))).thenReturn(q);
        market.registerSeller(seller);                              //he didn't submit any quote yet
        assertEquals(predictor.predictPurchasePrice(dept), 10);
        market.submitSellQuote(seller, 1, mock(Good.class));
        assertEquals(predictor.predictPurchasePrice(dept), 1);

    }
}
