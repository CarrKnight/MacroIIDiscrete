package tests.salesPID;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentAllAtOnce;
import agents.firm.sales.pricing.pid.OrderBookStockout;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import financial.market.Market;
import financial.utilities.Quote;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
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
 * @version 2012-08-28
 * @see
 */
public class OrderBookStockoutTest {

    @Test
    public void stockouts() throws IllegalAccessException {

        Firm owner = new Firm(new MacroII(1l));
        SalesDepartment dept = mock(SalesDepartmentAllAtOnce.class);
        when(dept.hasAnythingToSell()).thenReturn(false); //you are always empty for this example
        SimpleFlowSellerPID strategy = mock(SimpleFlowSellerPID.class);
        when(strategy.getTargetPrice()).thenReturn(10l);
        when(strategy.getSales()).thenReturn(dept);
        when(dept.getFirm()).thenReturn(owner); //the owner is just a random


        OrderBookStockout stockouts = new OrderBookStockout(strategy);

        List<Quote> fakeBids = new LinkedList<>();

        //buyer 1
        EconomicAgent buyer1 = mock(EconomicAgent.class);
        stockouts.newBidEvent(buyer1,20,null); //new bid we would like to fill but don't have any good!
        assertEquals(stockouts.getStockouts(),1);
        stockouts.removedBidEvent(buyer1, Quote.newBuyerQuote(buyer1,20, GoodType.GENERIC));
        assertEquals(stockouts.getStockouts(),0);
        stockouts.newBidEvent(buyer1,30,null); //new bid we would like to fill but don't have any good!
        assertEquals(stockouts.getStockouts(), 1);
        stockouts.tradeEvent(buyer1,mock(Firm.class),mock(Good.class),25,Quote.newSellerQuote(mock(Firm.class),9,mock(Good.class)),
                Quote.newBuyerQuote(buyer1,25,GoodType.GENERIC)); //fake seller sells a fake good with empty fake bid quote. None of that is checked. The point here is that we were outcompeted so it's not really a valid stockout anymore

        assertEquals(stockouts.getStockouts(), 0);
        buyer1=null;


        //buyer 2: same as buyer 1 but we aren't outcompeted
        EconomicAgent buyer2 = mock(EconomicAgent.class);
        stockouts.newBidEvent(buyer2,20,null); //new bid we would like to fill but don't have any good!
        assertEquals(stockouts.getStockouts(),1);
        stockouts.removedBidEvent(buyer2, Quote.newBuyerQuote(buyer2,20, GoodType.GENERIC));
        assertEquals(stockouts.getStockouts(),0);
        stockouts.newBidEvent(buyer2,30,null); //new bid we would like to fill but don't have any good!
        assertEquals(stockouts.getStockouts(), 1);
        stockouts.tradeEvent(buyer2,mock(Firm.class),mock(Good.class),25,Quote.newSellerQuote(mock(Firm.class),11,mock(Good.class)),
                Quote.newBuyerQuote(buyer2,25,GoodType.GENERIC)); //fake seller sells a fake good with empty fake bid quote. None of that is checked. The point here is that we were outcompeted so it's not really a valid stockout anymore

        assertEquals(stockouts.getStockouts(), 1);
        buyer2=null;


        //buyer 3 places a bid and forgets about it
        EconomicAgent buyer3 = mock(EconomicAgent.class);
        Quote quote3 = Quote.newBuyerQuote(buyer3,15,GoodType.GENERIC);
        stockouts.newBidEvent(buyer3,10,null); //new bid we would like to fill but don't have any good!
        fakeBids.add(quote3);
 //       buyer3=null;
        assertEquals(stockouts.getStockouts(), 2);


        //immediate trade, outcompeted
        EconomicAgent buyer4 = mock(EconomicAgent.class);
        stockouts.newBidEvent(buyer4, 50, Quote.newSellerQuote(mock(Firm.class), 9, mock(Good.class)));
        stockouts.tradeEvent(buyer4,mock(Firm.class),mock(Good.class),25,Quote.newSellerQuote(mock(Firm.class),9,mock(Good.class)),
                Quote.newBuyerQuote(buyer4,25,GoodType.GENERIC)); //fake seller sells a fake good with empty fake bid quote. None of that is checked. The point here is that we were outcompeted so it's not really a valid stockout anymore
        assertEquals(stockouts.getStockouts(), 2);

        //immediate trade, stockout
        EconomicAgent buyer5 = mock(EconomicAgent.class);
        stockouts.newBidEvent(buyer5,50,Quote.newSellerQuote(mock(Firm.class),11,mock(Good.class)));
        stockouts.tradeEvent(buyer5,mock(Firm.class),mock(Good.class),25,Quote.newSellerQuote(mock(Firm.class),11,mock(Good.class)),
                Quote.newBuyerQuote(buyer5,25,GoodType.GENERIC)); //fake seller sells a fake good with empty fake bid quote. None of that is checked. The point here is that we were outcompeted so it's not really a valid stockout anymore
        assertEquals(stockouts.getStockouts(), 3);

        //like above, but this time we aren't empty
        when(dept.hasAnythingToSell()).thenReturn(true); //non-empty for a second
        EconomicAgent buyer6 = mock(EconomicAgent.class);
        stockouts.newBidEvent(buyer6,50,Quote.newSellerQuote(mock(Firm.class),11,mock(Good.class)));
        stockouts.tradeEvent(buyer6,mock(Firm.class),mock(Good.class),25,Quote.newSellerQuote(mock(Firm.class),11,mock(Good.class)),
                Quote.newBuyerQuote(buyer6,25,GoodType.GENERIC)); //fake seller sells a fake good with empty fake bid quote. None of that is checked. The point here is that we were outcompeted so it's not really a valid stockout anymore
        assertEquals(stockouts.getStockouts(), 3);




        when(dept.hasAnythingToSell()).thenReturn(false); //empty again
        Market market = mock(Market.class);
        when(market.getIteratorForBids()).thenReturn(fakeBids.iterator());
        when(market.areAllQuotesVisibile()).thenReturn(true);
        assertEquals(stockouts.getStockouts(), 3);
        stockouts.newPIDStep(market);
        assertEquals(stockouts.getStockouts(), 1);
        //and now we are outcompeted of our last opportunity
        stockouts.tradeEvent(buyer3,mock(Firm.class),mock(Good.class),25,Quote.newSellerQuote(mock(Firm.class),9,mock(Good.class)),
                Quote.newBuyerQuote(buyer3,25,GoodType.GENERIC)); //fake seller sells a fake good with empty fake bid quote. None of that is checked. The point here is that we were outcompeted so it's not really a valid stockout anymore
        assertEquals(stockouts.getStockouts(), 0);

    }




}
