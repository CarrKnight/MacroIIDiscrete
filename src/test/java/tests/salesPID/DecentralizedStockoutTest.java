package tests.salesPID;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentAllAtOnce;
import agents.firm.sales.SalesDepartmentFactory;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.pricing.pid.DecentralizedStockout;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import agents.firm.sales.pricing.pid.StockoutEstimator;
import financial.DecentralizedMarket;
import financial.Market;
import financial.OrderBookBlindMarket;
import financial.utilities.Quote;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import model.scenario.SimpleDecentralizedSellerScenario;
import model.utilities.ActionOrder;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import sim.engine.Schedule;
import sim.engine.Steppable;
import model.utilities.dummies.DummyBuyer;
import model.utilities.dummies.DummySeller;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
 * @version 2012-08-28
 * @see
 */
public class DecentralizedStockoutTest {

    //since the order book is not visible, for all this stuff stockout will be 0

    @Test
    public void stockouts() throws IllegalAccessException {

        Firm owner = new Firm(new MacroII(1l));
        SalesDepartment dept = mock(SalesDepartmentAllAtOnce.class);
        when(dept.hasAnythingToSell()).thenReturn(false); //you are always empty for this example
        SimpleFlowSellerPID strategy = mock(SimpleFlowSellerPID.class);
        when(strategy.getTargetPrice()).thenReturn(10l);
        when(strategy.getSales()).thenReturn(dept);
        when(dept.getFirm()).thenReturn(owner); //the owner is just a random


        DecentralizedStockout stockouts = new DecentralizedStockout(strategy);

        List<Quote> fakeBids = new LinkedList<>();

        //buyer 1
        EconomicAgent buyer1 = mock(EconomicAgent.class);
        stockouts.newBidEvent(buyer1,20,null); //new bid we would like to fill but don't have any good!
        assertEquals(stockouts.getStockouts(), 0);
        stockouts.removedBidEvent(buyer1, Quote.newBuyerQuote(buyer1,20, GoodType.GENERIC));
        assertEquals(stockouts.getStockouts(),0);
        stockouts.newBidEvent(buyer1,30,null); //new bid we would like to fill but don't have any good!
        assertEquals(stockouts.getStockouts(), 0);
        stockouts.tradeEvent(buyer1,mock(Firm.class),mock(Good.class),25,Quote.newSellerQuote(mock(Firm.class),9,mock(Good.class)),
                Quote.emptyBidQuote(GoodType.GENERIC)); //fake seller sells a fake good with empty fake bid quote. None of that is checked. The point here is that we were outcompeted so it's not really a valid stockout anymore

        assertEquals(stockouts.getStockouts(), 0);
        buyer1=null;


        //buyer 2: same as buyer 1 but we aren't outcompeted
        EconomicAgent buyer2 = mock(EconomicAgent.class);
        stockouts.newBidEvent(buyer2,20,null); //new bid we would like to fill but don't have any good!
        assertEquals(stockouts.getStockouts(), 0);
        stockouts.removedBidEvent(buyer2, Quote.newBuyerQuote(buyer2,20, GoodType.GENERIC));
        assertEquals(stockouts.getStockouts(),0);
        stockouts.newBidEvent(buyer2,30,null); //new bid we would like to fill but don't have any good!
        assertEquals(stockouts.getStockouts(), 0);
        stockouts.tradeEvent(buyer2,mock(Firm.class),mock(Good.class),25,Quote.newSellerQuote(mock(Firm.class),11,mock(Good.class)),
                Quote.emptyBidQuote(GoodType.GENERIC)); //fake seller sells a fake good with empty fake bid quote. None of that is checked. The point here is that we were outcompeted so it's not really a valid stockout anymore

        assertEquals(stockouts.getStockouts(), 0);
        buyer2=null;


        //buyer 3 places a bid and forgets about it
        EconomicAgent buyer3 = mock(EconomicAgent.class);
        Quote quote3 = Quote.newBuyerQuote(buyer3,15,GoodType.GENERIC);
        stockouts.newBidEvent(buyer3,10,null); //new bid we would like to fill but don't have any good!
        fakeBids.add(quote3);
        //       buyer3=null;
        assertEquals(stockouts.getStockouts(), 0);


        //immediate trade, outcompeted
        EconomicAgent buyer4 = mock(EconomicAgent.class);
        stockouts.newBidEvent(buyer4, 50, Quote.newSellerQuote(mock(Firm.class), 9, mock(Good.class)));
        stockouts.tradeEvent(buyer4,mock(Firm.class),mock(Good.class),25,Quote.newSellerQuote(mock(Firm.class),9,mock(Good.class)),
                Quote.emptyBidQuote(GoodType.GENERIC)); //fake seller sells a fake good with empty fake bid quote. None of that is checked. The point here is that we were outcompeted so it's not really a valid stockout anymore
        assertEquals(stockouts.getStockouts(), 0);

        //immediate trade, stockout
        EconomicAgent buyer5 = mock(EconomicAgent.class);
        stockouts.newBidEvent(buyer5,50,Quote.newSellerQuote(mock(Firm.class),11,mock(Good.class)));
        stockouts.tradeEvent(buyer5,mock(Firm.class),mock(Good.class),25,Quote.newSellerQuote(mock(Firm.class),11,mock(Good.class)),
                Quote.emptyBidQuote(GoodType.GENERIC)); //fake seller sells a fake good with empty fake bid quote. None of that is checked. The point here is that we were outcompeted so it's not really a valid stockout anymore
        assertEquals(stockouts.getStockouts(), 0);

        //like above, but this time we aren't empty
        when(dept.hasAnythingToSell()).thenReturn(true); //non-empty for a second
        EconomicAgent buyer6 = mock(EconomicAgent.class);
        stockouts.newBidEvent(buyer6,50,Quote.newSellerQuote(mock(Firm.class),11,mock(Good.class)));
        stockouts.tradeEvent(buyer6,mock(Firm.class),mock(Good.class),25,Quote.newSellerQuote(mock(Firm.class),11,mock(Good.class)),
                Quote.emptyBidQuote(GoodType.GENERIC)); //fake seller sells a fake good with empty fake bid quote. None of that is checked. The point here is that we were outcompeted so it's not really a valid stockout anymore
        assertEquals(stockouts.getStockouts(), 0);




        when(dept.hasAnythingToSell()).thenReturn(false); //empty again
        Market market = mock(Market.class);
        when(market.getIteratorForBids()).thenReturn(fakeBids.iterator());
        when(market.areAllQuotesVisibile()).thenReturn(true);
        assertEquals(stockouts.getStockouts(), 0);
        stockouts.newPIDStep(market);
        assertEquals(stockouts.getStockouts(), 0);
        //and now we are outcompeted of our last opportunity
        stockouts.tradeEvent(buyer3,mock(Firm.class),mock(Good.class),25,Quote.newSellerQuote(mock(Firm.class),9,mock(Good.class)),
                Quote.emptyBidQuote(GoodType.GENERIC)); //fake seller sells a fake good with empty fake bid quote. None of that is checked. The point here is that we were outcompeted so it's not really a valid stockout anymore
        assertEquals(stockouts.getStockouts(), 0);

    }

    @Test
    public void react(){


        Firm owner = new Firm(new MacroII(1l));
        SalesDepartment dept = mock(SalesDepartmentAllAtOnce.class);
        when(dept.hasAnythingToSell()).thenReturn(false); //you are always empty for this example
        SimpleFlowSellerPID strategy = mock(SimpleFlowSellerPID.class);
        when(strategy.getTargetPrice()).thenReturn(10l);
        when(strategy.getSales()).thenReturn(dept);
        when(dept.getFirm()).thenReturn(owner); //the owner is just a random


        DecentralizedStockout stockouts = new DecentralizedStockout(strategy);


        for(int i=0; i < 5; i++)
            stockouts.stockOutEvent(owner,dept,mock(Firm.class));
        assertEquals(stockouts.getStockouts(),5);

        Firm buyer = mock(Firm.class);
        for(int i=0; i<5; i++)
            stockouts.stockOutEvent(owner,dept,buyer);
        assertEquals(stockouts.getStockouts(),6);

        stockouts.tradeEvent(buyer,mock(Firm.class),mock(Good.class),12,null,null);
        assertEquals(stockouts.getStockouts(), 6);
        for(int i=0; i<5; i++)
            stockouts.stockOutEvent(owner,dept,buyer);
        assertEquals(stockouts.getStockouts(),7);

        stockouts.newPIDStep(mock(Market.class));
        assertEquals(stockouts.getStockouts(),0);


    }

    @Test
    public void fullyDressedTest() throws NoSuchFieldException, IllegalAccessException {

        Market.TESTING_MODE = true;
        //like before but without stubs
        MacroII model = new MacroII(1l);
        Firm owner = new Firm(model);
        OrderBookBlindMarket market = new OrderBookBlindMarket(GoodType.GENERIC);
        SalesDepartment dept = SalesDepartmentFactory.incompleteSalesDepartment(owner, market, new SimpleBuyerSearch(market, owner), new SimpleSellerSearch(market, owner), SalesDepartmentAllAtOnce.class);
        SimpleFlowSellerPID strategy = new SimpleFlowSellerPID(dept);
        strategy.setInitialPrice(10l);
        dept.setAskPricingStrategy(strategy);
//        market.registerSeller(owner);


        Field f = SimpleFlowSellerPID.class.getDeclaredField("stockOuts");
        f.setAccessible(true);
        StockoutEstimator stockouts = (DecentralizedStockout) f.get(strategy);

        DummySeller competitor = new DummySeller(model,100);market.registerSeller(competitor);


        //buyer 1
        EconomicAgent buyer1 = new DummyBuyer(model,20);  buyer1.earn(10000);
        market.registerBuyer(buyer1);
        Quote q = market.submitBuyQuote(buyer1,20);
        assertEquals(stockouts.getStockouts(), 0);
        market.removeBuyQuote(q);
        assertEquals(stockouts.getStockouts(), 0);
        market.submitBuyQuote(buyer1,30);
        assertEquals(stockouts.getStockouts(), 0);
        Good good = new Good(GoodType.GENERIC,competitor,0); competitor.receive(good,null);
        market.submitSellQuote(competitor,9,good);

        assertEquals(stockouts.getStockouts(), 0);
        buyer1=null;

        //buyer 2: same as buyer 1 but we aren't outcompeted
        EconomicAgent buyer2 = new DummyBuyer(model,20);  buyer2.earn(10000);
        market.registerBuyer(buyer2);
        q = market.submitBuyQuote(buyer2,20);
        assertEquals(stockouts.getStockouts(), 0);
        market.removeBuyQuote(q);
        assertEquals(stockouts.getStockouts(), 0);
        market.submitBuyQuote(buyer2,30);
        assertEquals(stockouts.getStockouts(), 0);
        good = new Good(GoodType.GENERIC,competitor,0); competitor.receive(good,null);
        market.submitSellQuote(competitor,11,good);

        assertEquals(stockouts.getStockouts(), 0);
        buyer2=null;


        //buyer 3 places a bid and forgets about it
        EconomicAgent buyer3 = new DummyBuyer(model,20);  buyer3.earn(10000);
        market.registerBuyer(buyer3);
        market.submitBuyQuote(buyer3,20);
        assertEquals(stockouts.getStockouts(), 0);


        //buyer4
        EconomicAgent buyer4 = new DummyBuyer(model,20);  buyer4.earn(10000);
        market.registerBuyer(buyer4);
        market.submitBuyQuote(buyer4,19);         good = new Good(GoodType.GENERIC,competitor,0); competitor.receive(good,null);
        market.submitSellQuote(competitor,9,good);
        //The point here is that we were outcompeted so it's not really a valid stockout anymore
        assertEquals(stockouts.getStockouts(), 0);

        //buyer5
        EconomicAgent buyer5 = new DummyBuyer(model,20);  buyer5.earn(10000);
        market.registerBuyer(buyer5);
        market.submitBuyQuote(buyer5,19);         good = new Good(GoodType.GENERIC,competitor,0); competitor.receive(good,null);
        market.submitSellQuote(competitor,11,good);
        //we weren't outcompeted, this is a stockout
        assertEquals(stockouts.getStockouts(), 0);

        model.scheduleSoon(ActionOrder.ADJUST_PRICES,strategy);
        model.getPhaseScheduler().step(model);
        assertEquals(stockouts.getStockouts(), 0);





    }


    //we are going to test the decentralized stockout with peddling where there is only one seller and 3 customers, only one of which is willing to pay
    //no peddling allowed
    @Test
    public void simpleStockoutMockTest() throws IllegalAccessException, NoSuchFieldException {
        MacroII model = new MacroII(1l);
        //create a mock schedule so we can adjust the PID at the right time
        Schedule schedule = mock(Schedule.class);
        model.schedule = schedule;
        final ArrayList<Steppable> steppables = new ArrayList<>();


        //sales.getFirm().getModel().drawProportionalGain(),  sales.getFirm().getModel().drawIntegrativeGain(),
        //sales.getFirm().getModel().drawDerivativeGain(),sales.getFirm().getModel().drawPIDSpeed()


        when(schedule.scheduleOnceIn(anyDouble(),any(Steppable.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                steppables.add((Steppable) invocation.getArguments()[1]);
                return true;
            }
        });

        Market market = new DecentralizedMarket(GoodType.GENERIC);
        Firm firm = new Firm(model);
        SalesDepartment dept = SalesDepartmentFactory.newSalesDepartment(firm, market, null, SimpleSellerSearch.class, SimpleFlowSellerPID.class, null, SalesDepartmentAllAtOnce.class).getSalesDepartment();
        dept.setCanPeddle(false);
        //give the department 1 good to sell
        for(int i=0; i < 1; i++)
        {
            Good g = new Good(GoodType.GENERIC,firm,0l);
            firm.receive(g,null);
            dept.sellThis(g);
        }

        //create 2 new buyers
        LinkedList<DummyBuyer> dummyBuyers = new LinkedList<>();
        for(int i=0; i < 2; i++){
            DummyBuyer dummy = new DummyBuyer(model,30+i*10);
            dummyBuyers.add(dummy);
            market.registerBuyer(dummy); dummy.earn(1000000000000l);
        }

        //this means that any price between 31 and 40 is fine!

        //pretend they are looking for prices
        for(DummyBuyer buyer : dummyBuyers)
        {
            DummyBuyer.goShopping(buyer, dept,GoodType.GENERIC);
        }

        //make sure the PID has scheduled itself
        Field f = SalesDepartment.class.getDeclaredField("askPricingStrategy"); f.setAccessible(true);
        SimpleFlowSellerPID pid = (SimpleFlowSellerPID) f.get(dept);

        //okay, keep stepping it for 100 times
        for(int j=0; j<100; j++)
        {
            model.scheduleSoon(ActionOrder.ADJUST_PRICES,pid);
            //give the department 1 good to sell
            for(int i=0; i < 1; i++)
            {
                Good g = new Good(GoodType.GENERIC,firm,0l);
                firm.receive(g, null);
                dept.sellThis(g);
            }
            model.getPhaseScheduler().step(model);


            //pretend they are looking for prices
            for(DummyBuyer buyer : dummyBuyers){


                DummyBuyer.goShopping(buyer, dept,GoodType.GENERIC);


            }

            System.out.println(dept.getLastClosingPrice());
        }





    }

    @Test public void simpleDecentralizedScenario()
    {

        //run simple scenario without demand shifts 10 times!
        for(int i=0; i<10; i++)
        {
            MacroII macroII = new MacroII(System.currentTimeMillis());
            SimpleDecentralizedSellerScenario sellerScenario1 = new SimpleDecentralizedSellerScenario(macroII);
            sellerScenario1.setDemandShifts(false);

            macroII.setScenario(sellerScenario1);

            macroII.start();
            while(macroII.schedule.getTime()<4000)
                macroII.schedule.step(macroII);

            long finalPrice = sellerScenario1.getMarkets().get(GoodType.GENERIC).getLastPrice();
            assertTrue("finalPrice: " + finalPrice, finalPrice >= 51 && finalPrice <= 60);

        }

        //now with demand shifts!
        for(int i=0; i<10; i++)
        {
            MacroII macroII = new MacroII(System.currentTimeMillis());
            SimpleDecentralizedSellerScenario sellerScenario1 = new SimpleDecentralizedSellerScenario(macroII);
            sellerScenario1.setDemandShifts(true);

            macroII.setScenario(sellerScenario1);

            macroII.start();
            while(macroII.schedule.getTime()<4000)
                macroII.schedule.step(macroII);

            long finalPrice = sellerScenario1.getMarkets().get(GoodType.GENERIC).getLastPrice();
            assertTrue("finalPrice: " + finalPrice, finalPrice >= 151 && finalPrice <= 160);

        }

    }


}
