package agents.firm.purchases.pricing.decorators;

import agents.firm.Firm;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.inventoryControl.InventoryControl;
import agents.firm.purchases.pricing.BidPricingStrategy;
import financial.Market;
import financial.OrderBookMarket;
import goods.Good;
import goods.GoodType;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

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


    final private long decoratedPrice;

    final private long reservationPrice;

    final private long expected;


    @Parameterized.Parameters
    public static Collection<Long[]> getTestParameters(){

        return Arrays.asList(new Long[][]{
                {10l, 20l, 10l},
                {20l, 10l, 10l},
                {10l, 10l, 10l},
                {0l, 5l, 0l}


        });


    }

    public MaximumBidPriceDecoratorTest(long decoratedPrice, long reservationPrice, long expected) {
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

        Assert.assertEquals(decorator.maxPrice(GoodType.GENERIC),expected);
        Assert.assertEquals(decorator.maxPrice(mock(Good.class)),expected);



    }


    //test it from Purchase department
    @Test
    public void testDeptMaxPrice() throws Exception {

        Market market = new OrderBookMarket(GoodType.GENERIC);


        PurchasesDepartment department = PurchasesDepartment.getEmptyPurchasesDepartment(10000l,mock(Firm.class),market);

        BidPricingStrategy strategy = mock(BidPricingStrategy.class);
        when(strategy.maxPrice(any(GoodType.class))).thenReturn(decoratedPrice);
        when(strategy.maxPrice(any(Good.class))).thenReturn(decoratedPrice);
        department.setPricingStrategy(strategy);
        InventoryControl control = mock(InventoryControl.class); when(control.canBuy()).thenReturn(true);
        department.setControl(control);



        department.setReservationPrice(reservationPrice);

        Assert.assertEquals(department.maxPrice(GoodType.GENERIC,market),expected);
        Assert.assertEquals(department.maximumOffer(mock(Good.class)),expected);




    }


}
