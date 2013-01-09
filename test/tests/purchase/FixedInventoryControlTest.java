package tests.purchase;

import agents.EconomicAgent;
import agents.Inventory;
import agents.firm.Firm;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.inventoryControl.FixedInventoryControl;
import agents.firm.purchases.inventoryControl.InventoryControl;
import agents.firm.purchases.inventoryControl.Level;
import agents.firm.purchases.pricing.BidPricingStrategy;
import financial.Bankruptcy;
import financial.Market;
import financial.OrderBookMarket;
import goods.Good;
import goods.GoodType;
import junit.framework.Assert;
import model.MacroII;
import org.junit.Before;
import org.junit.Test;
import tests.DummySeller;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
 * @version 2012-08-08
 * @see
 */
public class FixedInventoryControlTest {

    MacroII model;
    Market market;


    @Before
    public void setup(){
        model = new MacroII(1l);
        market = new OrderBookMarket(GoodType.GENERIC);

    }

    @Test
    public void mockInventoryRating()
    {
        Firm f = mock(Firm.class);
        //set up the firm so that it returns first 1 then 10 then 5
        when(f.hasHowMany(GoodType.GENERIC)).thenReturn(1);
        when(f.getModel()).thenReturn(model);  when(f.getRandom()).thenReturn(model.random);


        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(0, f, market, FixedInventoryControl.class,
                null, null, null);

        //assuming target inventory is 6~~

        Assert.assertEquals(dept.rateCurrentLevel(), Level.DANGER);
        when(f.hasHowMany(GoodType.GENERIC)).thenReturn(10);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);
        when(f.hasHowMany(GoodType.GENERIC)).thenReturn(5);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.BARELY);






    }

    @Test
    public void InventoryRating()
    {
        Firm f = new Firm(model);



        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(0, f, market, FixedInventoryControl.class,
                null, null, null);


        //assuming target inventory is 6~~

        f.receive(new Good(GoodType.GENERIC,f,0l),null);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.DANGER);
        Assert.assertEquals(dept.canBuy(), true);

        for(int i=0; i <9; i++)
            f.receive(new Good(GoodType.GENERIC,f,0l),null);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.TOOMUCH);
        Assert.assertEquals(dept.canBuy(), false);

        for(int i=0; i <5; i++)
            f.consume(GoodType.GENERIC);
        Assert.assertEquals(dept.rateCurrentLevel(), Level.BARELY);
        Assert.assertEquals(dept.canBuy(), true);







    }


    //mock testing to count how many times buy() gets called by the inventory control reacting!
    @Test
    public void InventoryRatingShouldBuyMock() throws IllegalAccessException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
        Firm f = new Firm(model);
        PurchasesDepartment dept = mock(PurchasesDepartment.class);

        when(dept.getFirm()).thenReturn(f); //make sure you link to f
        when(dept.getGoodType()).thenReturn(GoodType.GENERIC); //make sure you link to f


        InventoryControl control = new FixedInventoryControl(dept);
        //make sure it's registered as a listener
        Method method = EconomicAgent.class.getDeclaredMethod("getInventory");
        method.setAccessible(true);
        Inventory inv = (Inventory) method.invoke(f);


        Assert.assertTrue(inv.removeListener(control));
        inv.addListener(control); //re-addSalesDepartmentListener it!


        for(int i=0; i <10; i++){ //you receive 10, every time you receive one and it's less than 6 in total you call buy() so you should have called it 5 times
            f.receive(new Good(GoodType.GENERIC,f,0l),null);
        }
        verify(dept,times(5)).buy();





    }

    @Test
    public void InventoryRatingShouldBuy() throws IllegalAccessException {

        for(int k=0; k<100; k++){ //do this test 100 times because it used to fail only every now and then
            setup();
            Market.TESTING_MODE = true;


            Firm f = new Firm(model); f.earn(1000);
            PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartment(1000, f, market, FixedInventoryControl.class,
                    null, null, null);
            dept.setLooksAhead(true);

            f.registerPurchasesDepartment(dept,GoodType.GENERIC);

            BidPricingStrategy stubStrategy = mock(BidPricingStrategy.class); //addSalesDepartmentListener this stub as a new strategy so we can fix prices as we prefer
            when(stubStrategy.maxPrice(GoodType.GENERIC)).thenReturn(80l); //price everything at 80; who cares
            dept.setPricingStrategy(stubStrategy);

            // SalesDepartment salesDepartment = new SalesDepartment(seller,market,new SimpleBuyerSearch(market,seller),new SimpleSellerSearch(market,seller)); //create the sales department for the seller

            Assert.assertEquals(market.getBestBuyPrice(),-1);
            Assert.assertEquals(market.getBestSellPrice(),-1);


            //addSalesDepartmentListener 10 sellers!
            for(int i=0; i<10; i++) //create 10 buyers!!!!!!!!
            {
                DummySeller seller = new DummySeller(model,10+i*10){

                    @Override
                    public void earn(long money) throws Bankruptcy {
                        super.earn(money);    //To change body of overridden methods use File | Settings | File Templates.
                        market.deregisterSeller(this);
                    }
                };  //these dummies are modified so that if they do trade once, they quit the market entirely
                market.registerSeller(seller);
                Good toSell = new Good(GoodType.GENERIC,seller,0);
                seller.receive(toSell,null);
                market.submitSellQuote(seller,seller.saleQuote,toSell);
            }

            Assert.assertEquals(market.getBestBuyPrice(),-1);
            Assert.assertEquals(market.getBestSellPrice(),10);

            Assert.assertEquals(f.hasHowMany(GoodType.GENERIC),0);
            //now force the first buy, hopefully it'll cascade up until the firm has inventory 6
            dept.buy(); //goooooooooo
            dept.getFirm().getModel().getPhaseScheduler().step(dept.getFirm().getModel());
            Assert.assertEquals(dept.toString(),f.hasHowMany(GoodType.GENERIC),6);

            //when the dust settles...
            Assert.assertEquals(-1l,dept.getMarket().getBestBuyPrice()); //there shouldn't be a new buy order!
            Assert.assertEquals(70l,dept.getMarket().getBestSellPrice());  //all the others should have been taken
            Assert.assertEquals(60l,dept.getMarket().getLastPrice());    //although the max price is 80, the buyer defaults to the order book since it's visible

            Assert.assertEquals(790l,dept.getFirm().getCash());   //1000 - 345




            Market.TESTING_MODE = false;


        }






    }







}
