/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package tests;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.sales.exploration.SimpleFavoriteBuyerSearch;
import financial.market.OrderBookMarket;
import financial.utilities.PurchaseResult;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.utilities.dummies.DummyBuyer;
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
public class SimpleFavoriteSearchTest {


    SimpleFavoriteBuyerSearch toTest;
    OrderBookMarket market = new OrderBookMarket(UndifferentiatedGoodType.GENERIC);  //notice that the search algorithm will always ignore the quoted price, if any. That's the sales department business
    MacroII model = new MacroII(1);


    @Before
    public void setUp() throws Exception {

        toTest = new SimpleFavoriteBuyerSearch(market,new Firm(model));




    }

    @Test
    public void scenario1(){

        DummyBuyer buyer1 = new DummyBuyer(model,10,market); market.registerBuyer(buyer1);
        DummyBuyer buyer2 = new DummyBuyer(model,20,market); market.registerBuyer(buyer2);
        DummyBuyer buyer3 = new DummyBuyer(model,30,market); market.registerBuyer(buyer3);
        DummyBuyer buyer4 = new DummyBuyer(model,40,market); market.registerBuyer(buyer4);
        DummyBuyer buyer5 = new DummyBuyer(model,50,market); market.registerBuyer(buyer5);

        List<EconomicAgent> sample = Arrays.asList(toTest.sampleBuyers());
        assertTrue(sample.size() == 5);
        assertTrue(sample.contains(buyer1));
        assertTrue(sample.contains(buyer2));
        assertTrue(sample.contains(buyer3));
        assertTrue(sample.contains(buyer4));
        assertTrue(sample.contains(buyer5));

        EconomicAgent bestBuyer = toTest.getBestInSampleBuyer();
        assertEquals(buyer5, bestBuyer);
        assertEquals(50, bestBuyer.askedForABuyOffer(UndifferentiatedGoodType.GENERIC));

        buyer5.setQuotedPrice(5);


        toTest.reactToSuccess(buyer5, PurchaseResult.SUCCESS);


        bestBuyer = toTest.getBestInSampleBuyer();
        assertEquals(buyer5, bestBuyer);
        assertEquals(5, bestBuyer.askedForABuyOffer(UndifferentiatedGoodType.GENERIC));

        toTest.reactToFailure(buyer5,PurchaseResult.SUCCESS);

        bestBuyer = toTest.getBestInSampleBuyer();
        assertEquals(buyer4, bestBuyer);
        assertEquals(40, bestBuyer.askedForABuyOffer(UndifferentiatedGoodType.GENERIC));




    }


    @Test
    public void scenario2(){

        DummyBuyer buyer1 = new DummyBuyer(model,-1,market); market.registerBuyer(buyer1);
        DummyBuyer buyer2 = new DummyBuyer(model,-1,market); market.registerBuyer(buyer2);
        DummyBuyer buyer3 = new DummyBuyer(model,100,market); market.registerBuyer(buyer3);
        DummyBuyer buyer4 = new DummyBuyer(model,2000,market); market.registerBuyer(buyer4);
        DummyBuyer buyer5 = new DummyBuyer(model,-1,market); market.registerBuyer(buyer5);
        DummyBuyer buyer6 = new DummyBuyer(model,-1,market); market.registerBuyer(buyer6);


        List<EconomicAgent> sample = Arrays.asList(toTest.sampleBuyers());
        assertTrue(sample.size() == 5);


        EconomicAgent bestBuyer = toTest.getBestInSampleBuyer();
        assertTrue(bestBuyer == buyer3 || bestBuyer == buyer4);


    }

    @Test
    public void scenario3(){

        DummyBuyer buyer1 = new DummyBuyer(model,100,market); market.registerBuyer(buyer1);
        DummyBuyer buyer2 = new DummyBuyer(model,-1,market); market.registerBuyer(buyer2);



        List<EconomicAgent> sample = Arrays.asList(toTest.sampleBuyers());
        assertTrue(sample.size() == 2);


        EconomicAgent bestBuyer = toTest.getBestInSampleBuyer();
        assertTrue(bestBuyer == buyer1);


    }


    @Test
    public void scenario4(){

        DummyBuyer buyer1 = new DummyBuyer(model,-1,market); market.registerBuyer(buyer1);
        DummyBuyer buyer2 = new DummyBuyer(model,-1,market); market.registerBuyer(buyer2);



        List<EconomicAgent> sample = Arrays.asList(toTest.sampleBuyers());
        assertTrue(sample.size() == 2);


        EconomicAgent bestBuyer = toTest.getBestInSampleBuyer();
        assertTrue(bestBuyer == null);


    }
}
