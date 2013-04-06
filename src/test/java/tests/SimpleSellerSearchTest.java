package tests;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.sales.exploration.SimpleSellerSearch;
import financial.OrderBookMarket;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
public class SimpleSellerSearchTest {



    SimpleSellerSearch toTest;
    OrderBookMarket market = new OrderBookMarket(GoodType.GENERIC);  //notice that the search algorithm will always ignore the quoted price, if any. That's the sales department business
    MacroII model = new MacroII(1l);

    Firm f;

    @Before
    public void setUp() throws Exception {

        f =           new Firm(model);
        toTest = new SimpleSellerSearch(market,f);




    }

    @Test
    public void scenario1(){

        DummySeller seller1 = new DummySeller(model,10); market.registerSeller(seller1); seller1.receive(new Good(GoodType.GENERIC,f,0l),null);
        DummySeller seller2 = new DummySeller(model,20); market.registerSeller(seller2);  seller2.receive(new Good(GoodType.GENERIC,f,0l),null);
        DummySeller seller3 = new DummySeller(model,30); market.registerSeller(seller3); seller3.receive(new Good(GoodType.GENERIC,f,0l),null);
        DummySeller seller4 = new DummySeller(model,40); market.registerSeller(seller4); seller4.receive(new Good(GoodType.GENERIC,f,0l),null);
        DummySeller seller5 = new DummySeller(model,50); market.registerSeller(seller5); seller5.receive(new Good(GoodType.GENERIC,f,0l),null);

        List<EconomicAgent> sample = Arrays.asList(toTest.sampleSellers());
        assertTrue(sample.size() == 5);
        assertTrue(sample.contains(seller1)); //TODO
        assertTrue(sample.contains(seller2));
        assertTrue(sample.contains(seller3));
        assertTrue(sample.contains(seller4));
        assertTrue(sample.contains(seller5));

        EconomicAgent bestSeller = toTest.getBestInSampleSeller();
        assertEquals(seller1, bestSeller);
        assertEquals(10, bestSeller.askedForASaleQuote(f, GoodType.GENERIC).getPriceQuoted());

        seller5.setSaleQuote(5l);

        bestSeller = toTest.getBestInSampleSeller();
        assertEquals(seller5, bestSeller);
        assertEquals(5, bestSeller.askedForASaleQuote(f, GoodType.GENERIC).getPriceQuoted());



    }


    @Test
    public void scenario2(){

        DummySeller seller1 = new DummySeller(model,-1); market.registerSeller(seller1); seller1.receive(new Good(GoodType.GENERIC,f,0l),null);
        DummySeller seller2 = new DummySeller(model,-1); market.registerSeller(seller2);  seller2.receive(new Good(GoodType.GENERIC,f,0l),null);
        DummySeller seller3 = new DummySeller(model,100); market.registerSeller(seller3);  seller3.receive(new Good(GoodType.GENERIC,f,0l),null);
        DummySeller seller4 = new DummySeller(model,2000); market.registerSeller(seller4);  seller4.receive(new Good(GoodType.GENERIC,f,0l),null);
        DummySeller seller5 = new DummySeller(model,-1); market.registerSeller(seller5);   seller5.receive(new Good(GoodType.GENERIC,f,0l),null);
        DummySeller seller6 = new DummySeller(model,-1); market.registerSeller(seller6);   seller6.receive(new Good(GoodType.GENERIC,f,0l),null);


        List<EconomicAgent> sample = Arrays.asList(toTest.sampleSellers());
        assertTrue(sample.size() == 5);


        EconomicAgent bestSeller = toTest.getBestInSampleSeller();
        assertTrue(bestSeller == seller3 || bestSeller == seller4);


    }

    @Test
    public void scenario3(){

        DummySeller seller1 = new DummySeller(model,100); market.registerSeller(seller1); seller1.receive(new Good(GoodType.GENERIC,f,0l),null);
        DummySeller seller2 = new DummySeller(model,-1); market.registerSeller(seller2); seller2.receive(new Good(GoodType.GENERIC,f,0l),null);



        List<EconomicAgent> sample = Arrays.asList(toTest.sampleSellers());
        assertTrue(sample.size() == 2);


        EconomicAgent bestSeller = toTest.getBestInSampleSeller();
        assertTrue(bestSeller == seller1);


    }


    @Test
    public void scenario4(){

        DummySeller seller1 = new DummySeller(model,-1); market.registerSeller(seller1); seller1.receive(new Good(GoodType.GENERIC,f,0l),null);
        DummySeller seller2 = new DummySeller(model,-1); market.registerSeller(seller2); seller2.receive(new Good(GoodType.GENERIC,f,0l),null);
        market.registerSeller(f);



        List<EconomicAgent> sample = Arrays.asList(toTest.sampleSellers());
        assertEquals(sample.size(), 2);


        EconomicAgent bestSeller = toTest.getBestInSampleSeller();
        assertTrue(bestSeller == null);


    }
    
}
