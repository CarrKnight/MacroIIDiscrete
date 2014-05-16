package tests.purchase;

import agents.firm.Firm;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.pricing.BidPricingStrategy;
import agents.firm.purchases.pricing.SurveyMaxPricing;
import agents.firm.purchases.pricing.decorators.LookAtTheMarketBidPricingDecorator;
import ec.util.MersenneTwisterFast;
import financial.market.Market;
import financial.market.OrderBookMarket;
import financial.utilities.Quote;
import goods.Good;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import org.junit.Before;
import org.junit.Test;
import model.utilities.dummies.DummySeller;

import java.lang.reflect.Field;

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
public class SurveyMaxPricingTest {


    MacroII model;

    @Before
    public void setup(){
        model = new MacroII(1);
    }


    @Test
    public void testEmptyPricing()
    {
        //when there is nobody to search just go at random.
        PurchasesDepartment dept = mock(PurchasesDepartment.class);
        //search will always return null
        when(dept.getAvailableBudget()).thenReturn(100);
        MersenneTwisterFast random = new MersenneTwisterFast(0);
        when(dept.getRandom()).thenReturn(random);


        BidPricingStrategy pricing = new SurveyMaxPricing(dept);
        for(int i=0; i < 10000; i++)
        {
            int maxPrice =pricing.maxPrice(UndifferentiatedGoodType.GENERIC);
            assertTrue(maxPrice>=0);
            assertTrue(maxPrice<=100);

        }

        //there should be about 10% of the data in any decile
        int decile =0;
        for(int i=0; i < 10000; i++)
        {
            int maxPrice =pricing.maxPrice(UndifferentiatedGoodType.GENERIC);
            if(maxPrice >= 40 && maxPrice < 50)
                decile++;

        }
        assertTrue(decile >= 900 && decile <= 1100);

    }

    @Test
    public void testPricingSeparateTest() throws NoSuchFieldException, IllegalAccessException {

        Firm f = new Firm(model);
        Market market = new OrderBookMarket(UndifferentiatedGoodType.GENERIC);



        //when there is nobody to search just go at random.
        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(100, f, market, null,
                SurveyMaxPricing.class, null, null).getDepartment();
        Field field = PurchasesDepartment.class.getDeclaredField("pricingStrategy");
        field.setAccessible(true);
        BidPricingStrategy pricingStrategy = (BidPricingStrategy) field.get(dept);

        //I assume the search depth is 5
        for(int i=0; i < 6; i++)
        {
            DummySeller seller = new DummySeller(model,100-i*10);
            market.registerSeller(seller);
            seller.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC),null); //get a good to sell


        }

        for(int i=0; i < 100; i++)
        {
            int price = pricingStrategy.maxPrice(UndifferentiatedGoodType.GENERIC);
            assertTrue("price found: " + price ,price == 50 ||price == 60);
        }




    }


    @Test
    public void testPricingFullTest() throws NoSuchFieldException, IllegalAccessException {

        Firm f = new Firm(model);
        Market market = new OrderBookMarket(UndifferentiatedGoodType.GENERIC);



        //when there is nobody to search just go at random.
        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(100, f,
                market, null, SurveyMaxPricing.class, null, null).getDepartment();
        dept.setPricingStrategy(new LookAtTheMarketBidPricingDecorator(new SurveyMaxPricing(dept),market));


        //I assume the search depth is 5
        DummySeller seller = null;
        for(int i=0; i < 6; i++)
        {
            seller = new DummySeller(model,100-i*10);
            market.registerSeller(seller);
            seller.receive(Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC),null); //get a good to sell!


        }

        //test that purchase department overrides the strategic price when there is a lower market bet
        Quote q =market.submitSellQuote(seller, 20,Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC)); //notice here the quote is only 20

        for(int i=0; i < 100; i++){
            int price = dept.maxPrice(UndifferentiatedGoodType.GENERIC,market);
            assertTrue("price found: " + price ,price == 20);
        }


        //remove the quote
        market.removeSellQuote(q);
        q =market.submitSellQuote(seller, 90, Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC)); //notice here the quote is 100, so it doesn't override the pricing strategy




        for(int i=0; i < 100; i++){
            int price = dept.maxPrice(UndifferentiatedGoodType.GENERIC,market);
            assertTrue("price found: " + price ,price == 50 || price == 60 );
        }



    }

}
