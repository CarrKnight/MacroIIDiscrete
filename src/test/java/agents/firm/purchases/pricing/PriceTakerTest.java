/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.pricing;

import agents.firm.Firm;
import agents.firm.purchases.FactoryProducedPurchaseDepartment;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.inventoryControl.FixedInventoryControl;
import agents.firm.sales.exploration.BuyerSearchAlgorithm;
import agents.firm.sales.exploration.SellerSearchAlgorithm;
import ec.util.MersenneTwisterFast;
import financial.market.ImmediateOrderHandler;
import financial.market.Market;
import financial.market.OrderBookMarket;
import goods.Good;
import goods.UndifferentiatedGoodType;
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
public class PriceTakerTest {


    //this isn't true anymore!
    public void submitRightPrice() throws IllegalAccessException {
        //create the market, the purchase department and the pricer
        Market market = mock(Market.class); when(market.getGoodType()).thenReturn(UndifferentiatedGoodType.GENERIC);
        PurchasesDepartment department = mock(PurchasesDepartment.class); when(department.getMarket()).thenReturn(market);  when(department.getRandom()).thenReturn(new MersenneTwisterFast());
        when(department.getModel()).thenReturn(mock(MacroII.class));
        PriceTaker pricing = new PriceTaker(department);

        //make the market visible and the best ask wanting 100$
        when(market.isBestSalePriceVisible()).thenReturn(true);
        when(market.getBestSellPrice()).thenReturn(100);

        //should be the right price!
        Assert.assertEquals(pricing.maxPrice(UndifferentiatedGoodType.GENERIC),100);

    }

    //check that it submits the default offer if not visible
    @Test
    public void submitDefaultOfferWhenNotVisible() throws IllegalAccessException {
        //create the market, the purchase department and the pricer
        Market market = mock(Market.class); when(market.getGoodType()).thenReturn(UndifferentiatedGoodType.GENERIC);
        PurchasesDepartment department = mock(PurchasesDepartment.class); when(department.getMarket()).thenReturn(market);   when(department.getRandom()).thenReturn(new MersenneTwisterFast());
        when(department.getModel()).thenReturn(mock(MacroII.class));
        PriceTaker pricing = new PriceTaker(department);
        //set the default offer to 999
        pricing.setDefaultOffer(999);

        //best sale is not visible!
        when(market.isBestSalePriceVisible()).thenReturn(false);

        //should be the right price!
        Assert.assertEquals(pricing.maxPrice(UndifferentiatedGoodType.GENERIC),999);

    }

    //the price is visible, but there is no ask so just default offer again!
    @Test
    public void submitDefaultOfferWhenNoAsk() throws IllegalAccessException {
        //create the market, the purchase department and the pricer
        Market market = mock(Market.class); when(market.getGoodType()).thenReturn(UndifferentiatedGoodType.GENERIC);
        PurchasesDepartment department = mock(PurchasesDepartment.class); when(department.getMarket()).thenReturn(market);
        when(department.getRandom()).thenReturn(new MersenneTwisterFast()); when(department.getModel()).thenReturn(mock(MacroII.class));

        PriceTaker pricing = new PriceTaker(department);
        //set the default offer to 999
        pricing.setDefaultOffer(999);

        //best sale is visible but there is none!
        when(market.isBestSalePriceVisible()).thenReturn(true);
        when(market.getBestSellPrice()).thenReturn(-1);

        //should be the right price!
        Assert.assertEquals(pricing.maxPrice(UndifferentiatedGoodType.GENERIC),999);

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
            final OrderBookMarket market = new OrderBookMarket(UndifferentiatedGoodType.GENERIC);
            market.setOrderHandler(new ImmediateOrderHandler(),model);
            Firm firm = new Firm(model); firm.receiveMany(UndifferentiatedGoodType.MONEY,1000000);
            FactoryProducedPurchaseDepartment<FixedInventoryControl,PriceTaker,
                    BuyerSearchAlgorithm,SellerSearchAlgorithm> factoryMade
                    =
                    PurchasesDepartment.getPurchasesDepartment(999999,firm,market, FixedInventoryControl.class,PriceTaker.class,null,null);
            //set target 2, very strict!
            factoryMade.getInventoryControl().setInventoryTarget(2);
            factoryMade.getInventoryControl().setHowManyTimesOverInventoryHasToBeOverTargetToBeTooMuch(1f);
            //finally register it back to the firm
            firm.registerPurchasesDepartment(factoryMade.getDepartment(), UndifferentiatedGoodType.GENERIC);
            factoryMade.getDepartment().start(model);


            //================================================================================
            // Create 3 fake sellers
            //================================================================================
            for(int i=0;i<3;i++)
            {

                final int price=30+i;
                model.scheduleSoon(ActionOrder.TRADE,new Steppable() {
                    @Override
                    public void step(SimState state) {
                        Good good = Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC);
                        DummySeller seller = new DummySeller(model,price);
                        seller.receive(good,null);
                        market.registerSeller(seller);
                        market.submitSellQuote(seller,price,good);
                    }
                });


            }

            //before:
            Assert.assertEquals(market.numberOfAsks(),0);
            Assert.assertTrue(firm.hasHowMany(UndifferentiatedGoodType.GENERIC)==0);

            //punch it
            model.start();
            model.schedule.step(model);
            //after:
            Assert.assertTrue(firm.hasHowMany(UndifferentiatedGoodType.GENERIC)==2);
            Assert.assertEquals(market.numberOfAsks(), 1);


        }

    }

}
