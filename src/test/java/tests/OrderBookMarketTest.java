package tests;

import agents.EconomicAgent;
import agents.Person;
import financial.Market;
import financial.OrderBookMarket;
import financial.utilities.Quote;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created with IntelliJ IDEA.
 * User: carrknight
 * Date: 7/17/12
 * Time: 8:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class OrderBookMarketTest {

    OrderBookMarket market;
    MacroII model;
    Person buyer;
    Person seller;

    @Before
    public void setUp() throws Exception {
        Market.TESTING_MODE = true;
        market = new OrderBookMarket(GoodType.GENERIC);
        model = new MacroII(1l);
        buyer = new Person(model){
            @Override
            public void reactToFilledBidQuote(Good g, long price, EconomicAgent seller) {

            }
        };
        seller = new Person(model){
            @Override
            public void reactToFilledAskedQuote(Good g, long price, EconomicAgent seller) {

            }
        };

        market.registerBuyer(buyer);
        market.registerSeller(seller);

    }

    @Test
    public void testSubmitSellQuote() throws Exception {


        buyer.earn(100);
        Good one =  new Good(GoodType.GENERIC,null,0l);
        seller.receive(one,null);
        Good two =  new Good(GoodType.GENERIC,null,1l);
        seller.receive(two,null);


        Quote q = market.submitSellQuote(seller, 10, one);
        market.submitSellQuote(seller, 20, two);

        assertTrue(seller.has(one));   //one is owned by seller
        market.submitBuyQuote(buyer,12); //this should start a trade
        assertEquals(market.getBestSellPrice(), 20);
        assertEquals(market.getBestBuyPrice(), -1);
        assertTrue(buyer.has(one));
        assertEquals(seller.getCash(), 11l); //this should be the price agreed





    }

    @Test
    public void testRemoveSellQuote() throws Exception {

        Good one =  new Good(GoodType.GENERIC,null,0l);
        seller.receive(one,null);
        Good two =  new Good(GoodType.GENERIC,null,1l);
        seller.receive(two,null);


        Quote q = market.submitSellQuote(seller, 10, one);
        market.submitSellQuote(seller, 20, two);

        assertEquals(market.getBestSellPrice(), 10);
        assertEquals(market.getBestBuyPrice(), -1);

        market.removeSellQuote(q);

        assertEquals(market.getBestSellPrice(), 20);

    }

    @Test (expected=IllegalArgumentException.class)
    public void testRemoveSellQuote2() throws Exception {

        Quote q = null;
        try{
            Good one =  new Good(GoodType.GENERIC,null,0l);
            seller.receive(one,null);
            Good two =  new Good(GoodType.GENERIC,null,1l);
            seller.receive(two,null);


            q = market.submitSellQuote(seller, 10, one);
            market.submitSellQuote(seller, 20, two);


            market.removeSellQuote(q);

        }catch(Exception e){
            fail();   //shouldn't fail here!
        }


        market.removeSellQuote(q);

        fail(); //shouldn't be here
    }

    @Test
    public void testSubmitBuyQuote() throws Exception {
        for(int i=0; i < 5; i++){
            Good g =  new Good(GoodType.GENERIC,null,10l * i);
            seller.receive(g,null);
            market.submitSellQuote(seller,10l * i+10l,g);
        }

        assertTrue(buyer.getTotalInventory().size() == 0);
        assertTrue(seller.getTotalInventory().size() == 5);

        buyer.earn(100l);

        for(int i=0; i < 5; i++)
            market.submitBuyQuote(buyer,30l);


        assertEquals(market.getBestSellPrice(), 40);
        assertEquals(market.getBestBuyPrice(), 30);

        assertEquals(buyer.getTotalInventory().size(), 3);
        assertEquals(seller.getTotalInventory().size(), 2);

        assertEquals(buyer.getCash(), 25l);



    }


    @Test
    public void testIsBestBuyPriceVisible() throws Exception {

        assertTrue(market.isBestBuyPriceVisible());
        assertTrue(market.isBestSalePriceVisible());
    }

    @Test
    public void testGetBestBuyPrice() throws Exception {

        market.submitBuyQuote(buyer,30l);
        market.submitBuyQuote(buyer,40l);
        market.submitBuyQuote(buyer,20l);
        assertEquals(market.getBestBuyPrice(), 40);


    }
}
