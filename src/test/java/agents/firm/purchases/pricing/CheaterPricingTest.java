package agents.firm.purchases.pricing;

import agents.firm.Firm;
import agents.firm.purchases.FactoryProducedPurchaseDepartment;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.inventoryControl.FixedInventoryControl;
import agents.firm.sales.exploration.BuyerSearchAlgorithm;
import agents.firm.sales.exploration.SellerSearchAlgorithm;
import financial.market.Market;
import financial.market.OrderBookMarket;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.dummies.DummySeller;
import org.junit.Assert;
import org.junit.Test;
import sim.engine.SimState;
import sim.engine.Steppable;

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
 * @author carrknight
 * @version 2013-06-10
 * @see
 */
public class CheaterPricingTest {


    //check that it submits the right price if it's visible
    @Test
    public void submitRightPrice() throws IllegalAccessException {
        //create the market, the purchase department and the pricer
        Market market = mock(Market.class); when(market.getGoodType()).thenReturn(GoodType.GENERIC);
        PurchasesDepartment department = mock(PurchasesDepartment.class); when(department.getMarket()).thenReturn(market);
        CheaterPricing pricing = new CheaterPricing(department);

        //make the market visible and the best ask wanting 100$
        when(market.isBestSalePriceVisible()).thenReturn(true);
        when(market.getBestSellPrice()).thenReturn(100l);

        //should be the right price!
        Assert.assertEquals(pricing.maxPrice(GoodType.GENERIC),100l);

    }

    //check that it submits the default offer if not visible
    @Test
    public void submitDefaultOfferWhenNotVisible() throws IllegalAccessException {
        //create the market, the purchase department and the pricer
        Market market = mock(Market.class); when(market.getGoodType()).thenReturn(GoodType.GENERIC);
        PurchasesDepartment department = mock(PurchasesDepartment.class); when(department.getMarket()).thenReturn(market);
        CheaterPricing pricing = new CheaterPricing(department);
        //set the default offer to 999
        pricing.setDefaultOffer(999l);;

        //best sale is not visible!
        when(market.isBestSalePriceVisible()).thenReturn(false);

        //should be the right price!
        Assert.assertEquals(pricing.maxPrice(GoodType.GENERIC),999l);

    }

    //the price is visible, but there is no ask so just default offer again!
    @Test
    public void submitDefaultOfferWhenNoAsk() throws IllegalAccessException {
        //create the market, the purchase department and the pricer
        Market market = mock(Market.class); when(market.getGoodType()).thenReturn(GoodType.GENERIC);
        PurchasesDepartment department = mock(PurchasesDepartment.class); when(department.getMarket()).thenReturn(market);
        CheaterPricing pricing = new CheaterPricing(department);
        //set the default offer to 999
        pricing.setDefaultOffer(999l);

        //best sale is visible but there is none!
        when(market.isBestSalePriceVisible()).thenReturn(true);
        when(market.getBestSellPrice()).thenReturn(-1l);

        //should be the right price!
        Assert.assertEquals(pricing.maxPrice(GoodType.GENERIC),999l);

    }


    //acceptance test:
    //Market with 3 asks, see that it clears the first two!
    @Test
    public void buy2Goods() throws IllegalAccessException {
        for(int j =0; j<10; j++)
        {
            //================================================================================
            // Create instances
            //================================================================================
            //create the model, the market, the firm and the purchase department
            final MacroII model = new MacroII(System.currentTimeMillis());
            final OrderBookMarket market = new OrderBookMarket(GoodType.GENERIC);
            Firm firm = new Firm(model); firm.earn(1000000l);
            FactoryProducedPurchaseDepartment<FixedInventoryControl,CheaterPricing,
                    BuyerSearchAlgorithm,SellerSearchAlgorithm> factoryMade
                    =
                    PurchasesDepartment.getPurchasesDepartment(999999l,firm,market, FixedInventoryControl.class,CheaterPricing.class,null,null);
            //set target 2, very strict!
            factoryMade.getInventoryControl().setInventoryTarget(2);
            factoryMade.getInventoryControl().setHowManyTimesOverInventoryHasToBeOverTargetToBeTooMuch(1f);
            //finally register it back to the firm
            firm.registerPurchasesDepartment(factoryMade.getDepartment(),GoodType.GENERIC);
            factoryMade.getDepartment().start();


            //================================================================================
            // Create 3 fake sellers
            //================================================================================
            for(int i=0;i<3;i++)
            {

                final long price=30+i;
                model.scheduleSoon(ActionOrder.TRADE,new Steppable() {
                    @Override
                    public void step(SimState state) {
                        Good good = new Good(GoodType.GENERIC,mock(Firm.class),0l);
                        DummySeller seller = new DummySeller(model,price);
                        seller.receive(good,null);
                        market.registerSeller(seller);
                        market.submitSellQuote(seller,price,good);
                    }
                });


            }

            //before:
            Assert.assertEquals(market.numberOfAsks(),0);
            Assert.assertTrue(firm.hasHowMany(GoodType.GENERIC)==0);

            //punch it
            model.start();
            model.schedule.step(model);
            //after:
            Assert.assertEquals(market.numberOfAsks(),1);
            Assert.assertTrue(firm.hasHowMany(GoodType.GENERIC)==2);


        }

    }

}
