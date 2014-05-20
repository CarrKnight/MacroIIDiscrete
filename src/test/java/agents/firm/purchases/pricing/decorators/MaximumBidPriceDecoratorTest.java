/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.pricing.decorators;

import agents.firm.Firm;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.inventoryControl.InventoryControl;
import agents.firm.purchases.pricing.BidPricingStrategy;
import financial.market.Market;
import financial.market.OrderBookMarket;
import goods.Good;
import goods.GoodType;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
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
 * @author carrknight
 * @version 2012-11-13
 * @see
 */

@RunWith(value = Parameterized.class)
public class MaximumBidPriceDecoratorTest {


    final private int decoratedPrice;

    final private int reservationPrice;

    final private int expected;


    @Parameterized.Parameters
    public static Collection<Integer[]> getTestParameters(){

        return Arrays.asList(new Integer[][]{
                {10, 20, 10},
                {20, 10, 10},
                {10, 10, 10},
                {0, 5, 0}


        });


    }

    public MaximumBidPriceDecoratorTest(int decoratedPrice, int reservationPrice, int expected) {
        this.decoratedPrice = decoratedPrice;
        this.reservationPrice = reservationPrice;
        this.expected = expected;
    }

    //test it as mock
    @Test
    public void testMockMaxPrice() throws Exception {

        BidPricingStrategy strategy = mock(BidPricingStrategy.class);
        when(strategy.maxPrice(any(GoodType.class))).thenReturn(decoratedPrice);
        when(strategy.maxPrice(any(Good.class))).thenReturn(decoratedPrice);

        BidPricingDecorator decorator = new MaximumBidPriceDecorator(strategy,reservationPrice);

        assertEquals(decorator.maxPrice(UndifferentiatedGoodType.GENERIC),expected);
        assertEquals(decorator.maxPrice(mock(Good.class)),expected);



    }


    //test it from Purchase department
    @Test
    public void testDeptMaxPrice() throws Exception {

        Market market = new OrderBookMarket(UndifferentiatedGoodType.GENERIC);

        Firm firm = mock(Firm.class);
        when(firm.getModel()).thenReturn(mock(MacroII.class));
        PurchasesDepartment department = PurchasesDepartment.getEmptyPurchasesDepartment(10000,firm,market);

        BidPricingStrategy strategy = mock(BidPricingStrategy.class);
        when(strategy.maxPrice(any(GoodType.class))).thenReturn(decoratedPrice);
        when(strategy.maxPrice(any(Good.class))).thenReturn(decoratedPrice);
        department.setPricingStrategy(strategy);
        InventoryControl control = mock(InventoryControl.class); when(control.canBuy()).thenReturn(true);
        department.setControl(control);



        department.setReservationPrice(reservationPrice);

        assertEquals(department.maxPrice(UndifferentiatedGoodType.GENERIC,market),expected);
        assertEquals(department.maximumOffer(mock(Good.class)),expected);




    }


}
