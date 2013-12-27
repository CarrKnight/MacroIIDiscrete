package tests.purchase;

import agents.EconomicAgent;
import agents.Inventory;
import agents.InventoryListener;
import agents.firm.Firm;
import agents.firm.purchases.prediction.PricingPurchasesPredictor;
import agents.firm.purchases.pricing.decorators.LookAtTheMarketBidPricingDecorator;
import agents.firm.utilities.NumberOfPlantsListener;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.inventoryControl.FixedInventoryControl;
import agents.firm.purchases.inventoryControl.InventoryControl;
import agents.firm.purchases.inventoryControl.Level;
import agents.firm.purchases.inventoryControl.DailyInventoryControl;
import agents.firm.purchases.pricing.BidPricingStrategy;
import agents.firm.sales.exploration.BuyerSearchAlgorithm;
import agents.firm.sales.exploration.SellerSearchAlgorithm;
import ec.util.MersenneTwisterFast;
import financial.market.Market;
import financial.market.OrderBookBlindMarket;
import financial.market.OrderBookMarket;
import financial.utilities.Quote;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import model.utilities.dummies.DummySeller;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
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
 * @version 2012-08-18
 * @see
 */
public class PurchasesDepartmentTest {

    @Test
    public void testGetPurchasesDepartmentIntegrated() throws Exception {

    }

    @Test
    public void testGetPurchasesDepartment() throws Exception {

        Firm firm = mock(Firm.class);
        MacroII model = new MacroII(1l);
        when(firm.getRandom()).thenReturn(new MersenneTwisterFast());
        when(firm.getModel()).thenReturn(model);

        //make sure stuff gets generated at random!
        for(int i=0; i < 10; i++)
        {
            PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(1000,firm,mock(Market.class),
                    (Class<? extends InventoryControl>) null,null,null,null).getDepartment();


            Field field = PurchasesDepartment.class.getDeclaredField("control");
            field.setAccessible(true);
            assertTrue(field.get(dept) != null); //so we can start it!
            assertTrue(field.get(dept) instanceof InventoryControl); //so we can start it!
            assertTrue(!(field.get(dept) instanceof BidPricingStrategy)); //so we can start it!



            field = PurchasesDepartment.class.getDeclaredField("pricingStrategy");
            field.setAccessible(true);
            assertTrue(field.get(dept) != null); //so we can start it!
            assertTrue(field.get(dept) instanceof BidPricingStrategy); //so we can start it!
            assertTrue(!(field.get(dept) instanceof InventoryControl)); //so we can start it!


            field = PurchasesDepartment.class.getDeclaredField("opponentSearch");
            field.setAccessible(true);
            assertTrue(field.get(dept) != null); //so we can start it!
            assertTrue(field.get(dept) instanceof BuyerSearchAlgorithm); //so we can start it!

            field = PurchasesDepartment.class.getDeclaredField("supplierSearch");
            field.setAccessible(true);
            assertTrue(field.get(dept) != null); //so we can start it!
            assertTrue(field.get(dept) instanceof SellerSearchAlgorithm); //so we can start it!


        }

        //make sure stuff gets generated at random with integrated stuff!
        for(int i=0; i < 10; i++)
        {
            PurchasesDepartment dept = PurchasesDepartment.
                    getPurchasesDepartmentIntegrated(1000,firm,mock(Market.class),null,null,null).getDepartment() ;


            Field field = PurchasesDepartment.class.getDeclaredField("control");
            field.setAccessible(true);
            assertTrue(field.get(dept) != null); //so we can start it!
            assertTrue(field.get(dept) instanceof BidPricingStrategy); //should be inheriting from both!
            assertTrue(field.get(dept) instanceof InventoryControl);


            field = PurchasesDepartment.class.getDeclaredField("pricingStrategy");
            field.setAccessible(true);
            assertTrue(field.get(dept) != null); //so we can start it!
            assertTrue(field.get(dept) instanceof BidPricingStrategy); //should be inheriting from both!
            assertTrue(field.get(dept) instanceof InventoryControl);


            field = PurchasesDepartment.class.getDeclaredField("opponentSearch");
            field.setAccessible(true);
            assertTrue(field.get(dept) != null); //so we can start it!
            assertTrue(field.get(dept) instanceof BuyerSearchAlgorithm); //so we can start it!

            field = PurchasesDepartment.class.getDeclaredField("supplierSearch");
            field.setAccessible(true);
            assertTrue(field.get(dept) != null); //so we can start it!
            assertTrue(field.get(dept) instanceof SellerSearchAlgorithm); //so we can start it!


        }

        //make sure stuff gets generated when the string name is correct
        for(int i=0; i < 10; i++)
        {
            PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(1000, firm, mock(Market.class), "FixedInventoryControl",
                    "SurveyMaxPricing", "SimpleBuyerSearch", "SimpleSellerSearch").getDepartment() ;


            Field field = PurchasesDepartment.class.getDeclaredField("control");
            field.setAccessible(true);
            assertTrue(field.get(dept) != null); //so we can start it!
            assertTrue(field.get(dept) instanceof InventoryControl); //so we can start it!


            field = PurchasesDepartment.class.getDeclaredField("pricingStrategy");
            field.setAccessible(true);
            assertTrue(field.get(dept) != null); //so we can start it!
            assertTrue(field.get(dept) instanceof BidPricingStrategy); //so we can start it!

            field = PurchasesDepartment.class.getDeclaredField("opponentSearch");
            field.setAccessible(true);
            assertTrue(field.get(dept) != null); //so we can start it!
            assertTrue(field.get(dept) instanceof BuyerSearchAlgorithm); //so we can start it!

            field = PurchasesDepartment.class.getDeclaredField("supplierSearch");
            field.setAccessible(true);
            assertTrue(field.get(dept) != null); //so we can start it!
            assertTrue(field.get(dept) instanceof SellerSearchAlgorithm); //so we can start it!


        }




    }


    @Test
    public void testMaxPrice() throws Exception {
        Firm firm = mock(Firm.class);
        MacroII model = new MacroII(1l);
        Market market = mock(Market.class); when(market.isBestSalePriceVisible()).thenReturn(true); when(market.getBestSellPrice()).thenReturn(-1l); //stub market, price visible but no best price around!
        when(firm.getRandom()).thenReturn(new MersenneTwisterFast());
        when(firm.getModel()).thenReturn(model);


        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(1,firm,mock(Market.class),
                (Class<? extends InventoryControl>) null,null,null,null).getDepartment();


        //addSalesDepartmentListener a stub pricing
        BidPricingStrategy pricingStrategy = mock(BidPricingStrategy.class);
        dept.setPricingStrategy(new LookAtTheMarketBidPricingDecorator(pricingStrategy,market));   //addSalesDepartmentListener it as new strategy

        when(pricingStrategy.maxPrice((Good) any())).thenReturn(100l); //this always prices at 100!
        when(pricingStrategy.maxPrice((GoodType) any())).thenReturn(100l); //this always prices at 100!


        assertEquals(dept.maxPrice(GoodType.GENERIC, market), 100l); //bounded by pricing choice
        when(market.getBestSellPrice()).thenReturn(30l);
        assertEquals(dept.maxPrice(GoodType.GENERIC, market), 30l); //bounded by best visible price











    }

    @Test
    public void testReactToFilledQuote() throws Exception {

        Market.TESTING_MODE = true;

        MacroII model = new MacroII(1l);
        Firm firm = new Firm(model);
        firm.earn(100);
        Market market = new OrderBookMarket(GoodType.GENERIC);
        market.start(model);


        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(100,firm,market,
                FixedInventoryControl.class,null,null,null).getDepartment();

        firm.registerPurchasesDepartment(dept,GoodType.GENERIC);
        dept.buy(); //place a bid or whatever
        model.getPhaseScheduler().step(model);
        Field field = PurchasesDepartment.class.getDeclaredField("quotePlaced");
        field.setAccessible(true);
        Quote q = (Quote) field.get(dept);
        assertTrue(q != null);
        assertTrue(q.getPriceQuoted() > 0);
        assertTrue(q.getPriceQuoted() <= 100);

        //if I put in a bad quote it should throw an exception
        try{
            dept.reactToFilledQuote(new Good(GoodType.GENERIC,firm,10l),10l,null);
            fail();
        }
        catch (AssertionError e){}

        DummySeller seller = new DummySeller(model,0l); market.registerSeller(seller);
        Good good = new Good(GoodType.GENERIC,firm,10l);
        seller.receive(good,null );
        market.submitSellQuote(seller,0l,good);
        model.getPhaseScheduler().step(model);



        field = PurchasesDepartment.class.getDeclaredField("quotePlaced");
        field.setAccessible(true);
        Quote newQuote = (Quote) field.get(dept);
        assertTrue(newQuote != null); //a new quote is needed.
        assertTrue(q != newQuote);






        Market.TESTING_MODE = false;









    }

    @Test
    public void testShop() throws Exception {
        Market.TESTING_MODE = true;


        MacroII model = new MacroII(1l);
        Firm firm = new Firm(model);
        firm.earn(1000);
        Market market = new OrderBookBlindMarket(GoodType.GENERIC);
        market.start(model);


        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(200,firm,market,
                FixedInventoryControl.class,null,null,null).getDepartment();

        firm.registerPurchasesDepartment(dept,GoodType.GENERIC);
        BidPricingStrategy pricingStrategy = mock(BidPricingStrategy.class);//i am going to force the dept to offer maxPrice = 150
        when(pricingStrategy.maxPrice(any(GoodType.class))).thenReturn(150l);
        dept.setPricingStrategy(pricingStrategy);



        DummySeller seller1 = new DummySeller(model,100l*(1),market);
        market.registerSeller(seller1);
        Good good = new Good(GoodType.GENERIC,firm,10l);
        seller1.receive(good,null );
        assert seller1.has(good);
        assert seller1.hasAny(good.getType());
        //     market.submitSellQuote(seller1,5l,good);   //even though it places a quote for 0, it will answer "100" when the shopper asks him

        DummySeller seller2 = new DummySeller(model,100l*(2));
        market.registerSeller(seller2);
        Good good2 = new Good(GoodType.GENERIC,firm,10l);
        seller2.receive(good2,null );



        dept.shop(); //shop should find and trade with the seller1
        model.getPhaseScheduler().step(model);

        assertTrue(firm.has(good));
        assertTrue(!firm.has(good2));
        //should have paid 125!
        assertEquals(firm.getCash(), 1000 - 125);



        Market.TESTING_MODE = false;









    }

    @Test
    public void testBuy() throws Exception {

        Market.TESTING_MODE = true;


        MacroII model = new MacroII(1l);
        Firm firm = new Firm(model);
        firm.earn(1000);
        final Market market = new OrderBookMarket(GoodType.GENERIC);
        market.start(model);


        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(2000,firm,market,
                FixedInventoryControl.class,null,null,null).getDepartment();

        firm.registerPurchasesDepartment(dept,GoodType.GENERIC);
        BidPricingStrategy pricingStrategy = mock(BidPricingStrategy.class);//i am going to force the dept to offer maxPrice = 150
        when(pricingStrategy.maxPrice(any(GoodType.class))).thenReturn(150l);
        dept.setPricingStrategy(pricingStrategy);


        for(int i=0; i<10; i++)
        {
            final DummySeller seller = new DummySeller(model,75 * (i)){
                @Override
                public void reactToFilledAskedQuote(Good g, long price,EconomicAgent agent) {
                    market.deregisterSeller(this);

                }
            };

            market.registerSeller(seller);
            Good good = new Good(GoodType.GENERIC,seller,0l);
            seller.receive(good,null);
            market.submitSellQuote(seller,seller.saleQuote,good);

        }

        assertEquals(firm.hasHowMany(GoodType.GENERIC), 0);


        //now cascade!
        dept.buy();

        model.getPhaseScheduler().step(model);


        assertEquals(firm.hasHowMany(GoodType.GENERIC), 3);
        assertEquals(market.getBestSellPrice(), 75 * 3);
        assertEquals(market.getBestBuyPrice(), 150);



    }

    @Test
    public void testGetGoodType() throws Exception {

        MacroII model = new MacroII(1l);
        Firm firm = new Firm(model);
        firm.earn(1000);
        final Market market = new OrderBookMarket(GoodType.GENERIC);


        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(200,firm,market,
                (Class<? extends InventoryControl>) null,null,null,null).getDepartment();
        assertEquals(dept.getGoodType(), market.getGoodType());
        market.deregisterBuyer(firm);
        dept = PurchasesDepartment.getPurchasesDepartmentIntegrated(200,firm,market,
                null,null,null).getDepartment();
        assertEquals(dept.getGoodType(), market.getGoodType());
        market.deregisterBuyer(firm);
        dept = PurchasesDepartment.getPurchasesDepartment(1000, firm,market, "FixedInventoryControl",
                "SurveyMaxPricing", "SimpleBuyerSearch", "SimpleSellerSearch").getDepartment();
        assertEquals(dept.getGoodType(), market.getGoodType());

    }

    @Test
    public void testGetFirm() throws Exception {
        MacroII model = new MacroII(1l);
        Firm firm = new Firm(model);
        firm.earn(1000);
        final Market market = new OrderBookMarket(GoodType.GENERIC);


        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(200,firm,market,
                (Class<? extends InventoryControl>) null,null,null,null).getDepartment();
        assertEquals(dept.getFirm(), firm);
        market.deregisterBuyer(firm);
        dept = PurchasesDepartment.getPurchasesDepartmentIntegrated(200,firm,market,
                null,null,null).getDepartment();
        assertEquals(dept.getFirm(), firm);
        market.deregisterBuyer(firm);
        dept = PurchasesDepartment.getPurchasesDepartment(1000, firm,market, "FixedInventoryControl",
                "SurveyMaxPricing", "SimpleBuyerSearch", "SimpleSellerSearch").getDepartment();
        assertEquals(dept.getFirm(), firm);
    }

    @Test
    public void testSetInventoryControl() throws Exception {
        //make sure you set the right thing and kill off listeners
        MacroII model = new MacroII(1l);
        Firm firm = new Firm(model);
        firm.earn(1000);
        final Market market = new OrderBookMarket(GoodType.GENERIC);


        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(200,firm,market,
                (Class<? extends InventoryControl>) null,null,null,null).getDepartment();
        for(int i=0; i <100; i++)
        {
            InventoryControl control = new DailyInventoryControl(dept);
            dept.setControl(control);
            Field field = PurchasesDepartment.class.getDeclaredField("control");
            field.setAccessible(true);
            assertEquals(control, field.get(dept));
        }

        //check the listeners now
        Field field = Firm.class.getDeclaredField("numberOfPlantsListeners");
        field.setAccessible(true);

        Set<NumberOfPlantsListener> listenerList = (Set<NumberOfPlantsListener>) field.get(firm);
        assertTrue(listenerList.size() == 1);
        assertTrue(listenerList.iterator().next() instanceof DailyInventoryControl);

        field = EconomicAgent.class.getDeclaredField("inventory");
        field.setAccessible(true);
        Inventory inventory = (Inventory) field.get(firm);
        field = Inventory.class.getDeclaredField("listeners");
        field.setAccessible(true);
        Set<InventoryListener> listeners = (Set<InventoryListener>) field.get(inventory);
        assertTrue(listeners.size() == 1);
        assertTrue(listeners.iterator().next() instanceof DailyInventoryControl);







    }

    @Test
    public void testSetPricingStrategy() throws Exception {

        //make sure you set the right thing and kill off listeners
        MacroII model = new MacroII(1l);
        Firm firm = new Firm(model);
        firm.earn(1000);
        final Market market = new OrderBookMarket(GoodType.GENERIC);


        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(200,firm,market,
                (Class<? extends InventoryControl>) null,null,null,null).getDepartment();
        BidPricingStrategy stub = null;
        for(int i=0; i <100; i++)
        {


            BidPricingStrategy strategy = mock(BidPricingStrategy.class);

            dept.setPricingStrategy(strategy);
            if(stub != null)
                verify(stub).turnOff(); //make sure it was turned off

            stub = strategy;

            //make sure it's assigned correctly
            Field field = PurchasesDepartment.class.getDeclaredField("pricingStrategy");
            field.setAccessible(true);
            assertEquals(strategy, field.get(dept));
        }



    }

    @Test
    public void testSetOpponentSearch() throws Exception {

        //make sure you set the right thing and kill off listeners
        MacroII model = new MacroII(1l);
        Firm firm = new Firm(model);
        firm.earn(1000);
        final Market market = new OrderBookMarket(GoodType.GENERIC);


        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(200,firm,market,
                (Class<? extends InventoryControl>) null,null,null,null).getDepartment();
        BuyerSearchAlgorithm stub = null;
        for(int i=0; i <100; i++)
        {


            BuyerSearchAlgorithm searchAlgorithm = mock(BuyerSearchAlgorithm.class);

            dept.setOpponentSearch(searchAlgorithm);
            if(stub != null)
                verify(stub).turnOff(); //make sure it was turned off

            stub = searchAlgorithm;

            //make sure it's assigned correctly
            Field field = PurchasesDepartment.class.getDeclaredField("opponentSearch");
            field.setAccessible(true);
            assertEquals(searchAlgorithm, field.get(dept));
        }


    }

    @Test
    public void testSetSupplierSearch() throws Exception
    {

        //make sure you set the right thing and kill off listeners
        MacroII model = new MacroII(1l);
        Firm firm = new Firm(model);
        firm.earn(1000);
        final Market market = new OrderBookMarket(GoodType.GENERIC);


        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(200,firm,market,
                (Class<? extends InventoryControl>) null,null,null,null).getDepartment();
        SellerSearchAlgorithm stub = null;
        for(int i=0; i <100; i++)
        {


            SellerSearchAlgorithm searchAlgorithm = mock(SellerSearchAlgorithm.class);

            dept.setSupplierSearch(searchAlgorithm);
            if(stub != null)
                verify(stub).turnOff(); //make sure it was turned off

            stub = searchAlgorithm;

            //make sure it's assigned correctly
            Field field = PurchasesDepartment.class.getDeclaredField("supplierSearch");
            field.setAccessible(true);
            assertEquals(searchAlgorithm, field.get(dept));
        }


    }

    @Test
    public void testWeekEnd() throws Exception {

        //make sure you set the right thing and kill off listeners
        MacroII model = new MacroII(1l);
        Firm firm = new Firm(model);
        firm.earn(1000);
        final Market market = new OrderBookMarket(GoodType.GENERIC);


        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(200,firm,market,
                (Class<? extends InventoryControl>) null,null,null,null).getDepartment();

        //make sure it's assigned correctly
        Field field = PurchasesDepartment.class.getDeclaredField("budgetSpent");
        field.setAccessible(true);
        field.setLong(dept,100);
        assertTrue(dept.getAvailableBudget() == 100);
        dept.weekEnd();
        //should adjust the budget
        assertTrue(dept.getAvailableBudget() == 100);
        assertEquals(field.get(dept), 0l);
        field = PurchasesDepartment.class.getDeclaredField("budgetGiven");
        field.setAccessible(true);
        assertEquals(100l, field.get(dept));
    }

    @Test
    public void testUpdatePrices() throws Exception
    {

        Market.TESTING_MODE = true;


        MacroII model = new MacroII(1l);
        Firm firm = new Firm(model);
        firm.earn(1000);
        final Market market = new OrderBookMarket(GoodType.GENERIC);
        market.start(model);


        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(200,firm,market,
                FixedInventoryControl.class,null,null,null).getDepartment();
        dept.setPredictor(new PricingPurchasesPredictor());

        firm.registerPurchasesDepartment(dept,GoodType.GENERIC);
        BidPricingStrategy pricingStrategy = mock(BidPricingStrategy.class);//i am going to force the dept to offer maxPrice = 150
        when(pricingStrategy.maxPrice(any(GoodType.class))).thenReturn(150l);
        dept.setPricingStrategy(pricingStrategy);

        dept.start();
        model.start();
        model.schedule.step(model);

        assertEquals(market.getBestBuyPrice(), 150l);
        when(pricingStrategy.maxPrice(any(GoodType.class))).thenReturn(75l);
        assertEquals(market.getBestBuyPrice(), 150l);
        dept.updateOfferPrices();
        model.getPhaseScheduler().step(model);

        assertEquals(market.getBestBuyPrice(), 75l);




    }

    @Test
    public void testGetAvailableBudgetShop() throws Exception
    {

        //this is a copy of shop, after it's done we make sure the budget was updated
        Market.TESTING_MODE = true;


        MacroII model = new MacroII(1l);
        Firm firm = new Firm(model);
        firm.earn(1000);
        Market market = new OrderBookBlindMarket(GoodType.GENERIC);
        market.start(model);


        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(200,firm,market,
                FixedInventoryControl.class,null,null,null).getDepartment();

        firm.registerPurchasesDepartment(dept,GoodType.GENERIC);
        BidPricingStrategy pricingStrategy = mock(BidPricingStrategy.class);//i am going to force the dept to offer maxPrice = 150
        when(pricingStrategy.maxPrice(any(GoodType.class))).thenReturn(150l);
        dept.setPricingStrategy(pricingStrategy);



        DummySeller seller1 = new DummySeller(model,100l*(1),market);
        market.registerSeller(seller1);
        Good good = new Good(GoodType.GENERIC,firm,10l);
        seller1.receive(good,null );
        assert seller1.has(good);
        assert seller1.hasAny(good.getType());
        //     market.submitSellQuote(seller1,5l,good);   //even though it places a quote for 0, it will answer "100" when the shopper asks him

        DummySeller seller2 = new DummySeller(model,100l*(2));
        market.registerSeller(seller2);
        Good good2 = new Good(GoodType.GENERIC,firm,10l);
        seller2.receive(good2,null );



        dept.shop(); //shop should find and trade with the seller1
        model.getPhaseScheduler().step(model);

        assertTrue(firm.has(good));
        assertTrue(!firm.has(good2));
        //should have paid 125!
        assertEquals(firm.getCash(), 1000 - 125);



        Market.TESTING_MODE = false;

        assertEquals(dept.getAvailableBudget(), 75l);






    }

    @Test
    public void testGetAvailableBudgetBid() throws Exception
    {

        //this is a copy of testbuy

        Market.TESTING_MODE = true;


        MacroII model = new MacroII(1l);
        Firm firm = new Firm(model);
        firm.earn(1000);
        final Market market = new OrderBookMarket(GoodType.GENERIC);
        market.start(model);


        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(2000,firm,market,
                FixedInventoryControl.class,null,null,null).getDepartment();

        firm.registerPurchasesDepartment(dept,GoodType.GENERIC);
        BidPricingStrategy pricingStrategy = mock(BidPricingStrategy.class);//i am going to force the dept to offer maxPrice = 150
        when(pricingStrategy.maxPrice(any(GoodType.class))).thenReturn(150l);
        dept.setPricingStrategy(new LookAtTheMarketBidPricingDecorator(pricingStrategy,market));   //addSalesDepartmentListener it as new strategy


        for(int i=0; i<10; i++)
        {
            final DummySeller seller = new DummySeller(model,75 * (i)){
                @Override
                public void reactToFilledAskedQuote(Good g, long price, EconomicAgent agent) {
                    market.deregisterSeller(this);

                }
            };

            market.registerSeller(seller);
            Good good = new Good(GoodType.GENERIC,seller,0l);
            seller.receive(good,null);
            market.submitSellQuote(seller,seller.saleQuote,good);

        }

        assertEquals(firm.hasHowMany(GoodType.GENERIC), 0);


        //now cascade!
        dept.buy();
        model.getPhaseScheduler().step(model);


        assertEquals(firm.hasHowMany(GoodType.GENERIC), 3);
        assertEquals(market.getBestSellPrice(), 75 * 3);
        assertEquals(market.getBestBuyPrice(), 150);

        //new check
        assertEquals(dept.getAvailableBudget(), 2000 - (150 + 75));

    }

    @Test
    public void testRateInventoryLevel() throws Exception {

        //this is a copy of testbuy

        Market.TESTING_MODE = true;


        MacroII model = new MacroII(1l);
        Firm firm = new Firm(model);
        firm.earn(1000);
        final Market market = new OrderBookMarket(GoodType.GENERIC);


        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(2000,firm,market,
                (Class<? extends InventoryControl>) null,null,null,null).getDepartment();

        firm.registerPurchasesDepartment(dept,GoodType.GENERIC);
        InventoryControl control = mock(InventoryControl.class);//i am going to force the dept to offer maxPrice = 150
        //make sure it asks inventory control
        when(control.rateCurrentLevel()).thenReturn(Level.DANGER);
        dept.setControl(control);
        assertEquals(dept.rateCurrentLevel(), Level.DANGER);
        when(control.rateCurrentLevel()).thenReturn(Level.BARELY);
        assertEquals(dept.rateCurrentLevel(), Level.BARELY);
        when(control.rateCurrentLevel()).thenReturn(Level.ACCEPTABLE);
        assertEquals(dept.rateCurrentLevel(), Level.ACCEPTABLE);

    }

    @Test
    public void testGetMarket() throws Exception {
        //this is a copy of testbuy

        Market.TESTING_MODE = true;


        MacroII model = new MacroII(1l);
        Firm firm = new Firm(model);
        firm.earn(1000);
        final Market market = new OrderBookMarket(GoodType.GENERIC);


        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(2000,firm,market,
                (Class<? extends InventoryControl>) null,null,null,null).getDepartment();

        assertEquals(dept.getMarket(), market);


    }

    @Test
    public void testCanBuy() throws Exception {

        //this is a copy of testbuy

        Market.TESTING_MODE = true;


        MacroII model = new MacroII(1l);
        Firm firm = new Firm(model);
        firm.earn(1000);
        final Market market = new OrderBookMarket(GoodType.GENERIC);


        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(2000,firm,market,
                (Class<? extends InventoryControl>) null,null,null,null).getDepartment();

        firm.registerPurchasesDepartment(dept,GoodType.GENERIC);
        InventoryControl control = mock(InventoryControl.class);
        //make sure it asks inventory control
        when(control.canBuy()).thenReturn(true);
        dept.setControl(control);
        assertEquals(dept.canBuy(), true);
        when(control.canBuy()).thenReturn(false);
        assertEquals(dept.canBuy(), false);
    }


    @Test
    public void testTurnOff() throws Exception {


        Firm firm = mock(Firm.class);
        MacroII model = new MacroII(1l);
        Market market = new OrderBookMarket(GoodType.GENERIC);
        when(firm.getRandom()).thenReturn(new MersenneTwisterFast());
        when(firm.getModel()).thenReturn(model);
        final Set<InventoryListener> inventoryListenersRegistered = new HashSet<>();
        final Set<InventoryListener> inventoryListenersDeregistered = new HashSet<>();
        final Set<NumberOfPlantsListener> plantListenersRegistered  = new HashSet<>();
        final Set<NumberOfPlantsListener> plantListenersDeregistered = new HashSet<>();


        /*****************  ****************************************************************
         * INVENTORY LISTENERS
         *************************************************************************************/
        doAnswer(new Answer<Object>() {        //put the listeners in the the big set
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                inventoryListenersRegistered.add((InventoryListener) invocation.getArguments()[0]);

                return null;
            }}).when(firm).addInventoryListener(any(InventoryListener.class));


        //when removing the method is boolean, not void so we have to use this different syntax.
        when(firm.removeInventoryListener(any(InventoryListener.class))).then(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                inventoryListenersDeregistered.add((InventoryListener) invocation.getArguments()[0]);

                return true;
            }
        });
        /*****************  ****************************************************************
         * plant listeners
         *************************************************************************************/
        doAnswer(new Answer<Object>() {        //put the listeners in the the big set
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                plantListenersRegistered.add((NumberOfPlantsListener) invocation.getArguments()[0]);

                return null;
            }}).when(firm).addPlantCreationListener(any(NumberOfPlantsListener.class));


        //when removing the method is boolean, not void so we have to use this different syntax.
        when(firm.removePlantCreationListener(any(NumberOfPlantsListener.class))).then(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                plantListenersDeregistered.add((NumberOfPlantsListener) invocation.getArguments()[0]);

                return true;
            }
        });



        //we are going to count how many times listeners get registered and hopefully it matches the number of times they get deregistered
        for(int i=0; i < 100; i++){
            PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(2000,firm,market,
                    (Class<? extends InventoryControl>) null,null,null,null).getDepartment();
            dept.turnOff();
        }
        for(int i=0; i < 100; i++){
            PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartmentIntegrated(2000,firm,market,
                    null,null,null).getDepartment();
            dept.turnOff();
        }

        //make sure every inventory listener registered is deregistered
        assertTrue(inventoryListenersDeregistered.size() > 0);
        assertTrue(inventoryListenersRegistered.size() > 0);
        assertTrue(inventoryListenersDeregistered.containsAll(inventoryListenersRegistered));
        assertTrue(inventoryListenersRegistered.containsAll(inventoryListenersDeregistered));
        //make sure every plant creation registered is deregistered
        assertTrue(plantListenersRegistered.size() > 0);
        assertTrue(plantListenersDeregistered.size() > 0);
        assertTrue(plantListenersRegistered.containsAll(plantListenersDeregistered));
        assertTrue(plantListenersDeregistered.containsAll(plantListenersRegistered));


    }



    @Test
    public void testGetRandom() throws Exception {

    }

    @Test
    public void testGetBestSupplierFound() throws Exception {

    }

    @Test
    public void testGetBestOpponentFound() throws Exception {

    }

    @Test
    public void testSupplierSearchFailure() throws Exception {

    }

    @Test
    public void testSupplierSearchSuccess() throws Exception {

    }

    @Test
    public void testOpponentSearchFailure() throws Exception {

    }

    @Test
    public void testOpponentSearchSuccess() throws Exception {

    }

    @Test
    public void testGetBestOpponent() throws Exception {

    }

    @Test
    public void testStart() throws Exception {

    }
}
