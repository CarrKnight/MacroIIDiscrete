package tests;

import agents.EconomicAgent;
import agents.Person;
import financial.Market;
import financial.OrderBookMarket;
import financial.utilities.Quote;
import goods.Good;
import goods.GoodType;
import junit.framework.Assert;
import model.MacroII;
import org.junit.Before;
import org.junit.Test;

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

        Assert.assertTrue(seller.has(one));   //one is owned by seller
        market.submitBuyQuote(buyer,12); //this should start a trade
        Assert.assertEquals(market.getBestSellPrice(),20);
        Assert.assertEquals(market.getBestBuyPrice(),-1);
        Assert.assertTrue(buyer.has(one));
        Assert.assertEquals(seller.getCash(),11l); //this should be the price agreed





    }

    @Test
    public void testRemoveSellQuote() throws Exception {

        Good one =  new Good(GoodType.GENERIC,null,0l);
        seller.receive(one,null);
        Good two =  new Good(GoodType.GENERIC,null,1l);
        seller.receive(two,null);


        Quote q = market.submitSellQuote(seller, 10, one);
        market.submitSellQuote(seller, 20, two);

        Assert.assertEquals(market.getBestSellPrice(),10);
        Assert.assertEquals(market.getBestBuyPrice(),-1);

        market.removeSellQuote(q);

        Assert.assertEquals(market.getBestSellPrice(),20);

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
            Assert.fail();   //shouldn't fail here!
        }


        market.removeSellQuote(q);

        Assert.fail(); //shouldn't be here
    }

    @Test
    public void testSubmitBuyQuote() throws Exception {
        for(int i=0; i < 5; i++){
            Good g =  new Good(GoodType.GENERIC,null,10l * i);
            seller.receive(g,null);
            market.submitSellQuote(seller,10l * i+10l,g);
        }

        Assert.assertTrue(buyer.getTotalInventory().size()==0);
        Assert.assertTrue(seller.getTotalInventory().size()==5);

        buyer.earn(100l);

        for(int i=0; i < 5; i++)
            market.submitBuyQuote(buyer,30l);


        Assert.assertEquals(market.getBestSellPrice(),40);
        Assert.assertEquals(market.getBestBuyPrice(),30);

        Assert.assertEquals(buyer.getTotalInventory().size(),3);
        Assert.assertEquals(seller.getTotalInventory().size(),2);

        Assert.assertEquals(buyer.getCash(),25l);



    }


    @Test
    public void testIsBestBuyPriceVisible() throws Exception {

        Assert.assertTrue(market.isBestBuyPriceVisible());
        Assert.assertTrue(market.isBestSalePriceVisible());
    }

    @Test
    public void testGetBestBuyPrice() throws Exception {

        market.submitBuyQuote(buyer,30l);
        market.submitBuyQuote(buyer,40l);
        market.submitBuyQuote(buyer,20l);
        Assert.assertEquals(market.getBestBuyPrice(),40);


    }
}
