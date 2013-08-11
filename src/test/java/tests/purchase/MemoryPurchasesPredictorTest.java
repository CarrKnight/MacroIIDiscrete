package tests.purchase;

import agents.firm.Firm;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.pid.PurchasesFixedPID;
import agents.firm.purchases.prediction.MemoryPurchasesPredictor;
import financial.Market;
import financial.OrderBookMarket;
import financial.utilities.Quote;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import org.junit.Test;
import sim.engine.SimState;
import sim.engine.Steppable;
import model.utilities.dummies.DummySeller;

import java.lang.reflect.Field;
import java.util.ArrayList;

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
 * @version 2012-10-04
 * @see
 */
public class MemoryPurchasesPredictorTest {
    @Test
    public void mockTest() throws Exception {
        //simple mock test
        PurchasesDepartment dept = mock(PurchasesDepartment.class);
        MemoryPurchasesPredictor predictor = new MemoryPurchasesPredictor();

        for(long i=0; i<100; i++){
            when(dept.getLastClosingPrice()).thenReturn(i);
            assertEquals(predictor.predictPurchasePriceWhenIncreasingProduction(dept), i);
        }

        when(dept.getLastClosingPrice()).thenReturn(-1l);
        assertEquals(predictor.predictPurchasePriceWhenIncreasingProduction(dept), -1l);




    }

    @Test
    public void fullyDressedTest() throws Exception {

        Market market = new OrderBookMarket(GoodType.GENERIC);
        PurchasesDepartment dept = fixedPIDTest(market);

        /**********************************************
         * Make sure the last closing price is correctly predicted by the predictor!
         *********************************************/
        assertTrue(dept.getLastClosingPrice() >= 20 && dept.maxPrice(GoodType.GENERIC, market) <= 30);
        MemoryPurchasesPredictor predictor = new MemoryPurchasesPredictor();
        assertTrue(predictor.predictPurchasePriceWhenIncreasingProduction(dept) >= 20 && predictor.predictPurchasePriceWhenIncreasingProduction(dept) <= 30);
    }

    /**
     * This is just a copy of the purchases fixed pid test, I am going to repeat it in many predictor tests so it might just as well be as a static method in order not to paste it over and over again
     * @return
     */
    public static PurchasesDepartment fixedPIDTest(final Market market){
        /**********************************************
         * THIS IS A COPY OF PURCHASES FIXED PID TEST
         *********************************************/
        Market.TESTING_MODE = true;

        final MacroII model = new MacroII(1l);
        final Firm f = new Firm(model);
        f.earn(10000000);
        model.start();

        PurchasesDepartment dept = PurchasesDepartment.getPurchasesDepartmentIntegrated(
                10000000,f,market,PurchasesFixedPID.class,null,null).getDepartment();
        f.registerPurchasesDepartment(dept,GoodType.GENERIC);

        Field field = null;
        PurchasesFixedPID control=null;
        try {
            field = PurchasesDepartment.class.getDeclaredField("control");

            field.setAccessible(true);
            control = (PurchasesFixedPID) field.get(dept); //so we can start it!
            control.start();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        final ArrayList<Quote> quotes = new ArrayList<>(); //here we'll store all the seller quotes

        final int  inventoryAtProduction[] = new int[]{0};

        for(int  j =0; j<500; j++) //for 500 stinking turns
        {



            model.scheduleSoon(ActionOrder.DAWN,new Steppable() {
                @Override
                public void step(SimState state) {
//create 10 sellers
                    for(int i=0; i<10; i++)
                    {
                        DummySeller seller = new DummySeller(model,i*10 + 10);
                        market.registerSeller(seller);
                        Good good = new Good(GoodType.GENERIC,seller,i*10+10);
                        seller.receive(good,null);
                        Quote q = market.submitSellQuote(seller,seller.saleQuote,good);
                        quotes.add(q);
                    }

                    System.out.println("DAWN INV:" + f.hasHowMany(GoodType.GENERIC));
                }});

            model.scheduleSoon(ActionOrder.CLEANUP,new Steppable() {
                @Override
                public void step(SimState state) {

                    for(Quote q : quotes)
                    {
                        try{
                            market.removeSellQuote(q);
                        }
                        catch (IllegalArgumentException e){} //some of them will have been bought. That's okay
                    }
                    quotes.clear();


                }
            });
            //at the end of the day remove all quotes


            model.scheduleSoon(ActionOrder.PRODUCTION,new Steppable() {
                @Override
                public void step(SimState state) {
                    //outflow
                    if(f.hasAny(GoodType.GENERIC))
                        f.consume(GoodType.GENERIC);
                    if(f.hasAny(GoodType.GENERIC))
                        f.consume(GoodType.GENERIC);


                    inventoryAtProduction[0] = f.hasHowMany(GoodType.GENERIC);
                }
            });


            f.earn(10000l);
            model.getPhaseScheduler().step(model);
            System.out.println(inventoryAtProduction[0] + " ---> " + control.maxPrice(GoodType.GENERIC));



        }

        Market.TESTING_MODE = false;
        //I expect the price to go high so that the firm builds up its reserves and then drop so that it only needs to buy 2 a adjust to keep things constant
        assertTrue(dept.maxPrice(GoodType.GENERIC, market) >= 20 && dept.maxPrice(GoodType.GENERIC, market) <= 30);
        assertEquals(inventoryAtProduction[0], 6); //has 6 but I just consumed 2
        return dept;

    }


}
