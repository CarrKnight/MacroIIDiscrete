package agents.firm.sales.pricing.decorators;

import agents.firm.Firm;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.pricing.AskPricingStrategy;
import financial.Market;
import goods.Good;
import junit.framework.Assert;
import model.MacroII;
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
 * @version 2012-11-12
 * @see
 */
@RunWith(value = Parameterized.class)
public class AskReservationPriceDecoratorTest {

    private long decoratedPrice;

    private long reservationPrice;

    private long expected;

    @Parameterized.Parameters
    public static Collection<Long[]> getTestParameters(){

        return Arrays.asList(new Long[][]{
                {10l,20l,20l},
                {20l,10l,20l},
                {10l,10l,10l},
                {0l,5l,5l}


        });


    }

    public AskReservationPriceDecoratorTest(long decoratedPrice, long reservationPrice, long expected) {
        this.decoratedPrice = decoratedPrice;
        this.reservationPrice = reservationPrice;
        this.expected = expected;
    }

    @Test
    public void testMockPrice() throws Exception {


        AskPricingStrategy strategy = mock(AskPricingStrategy.class);
        when(strategy.price(any(Good.class))).thenReturn(decoratedPrice);
        strategy = new AskReservationPriceDecorator(strategy,reservationPrice);
        Assert.assertEquals(strategy.price(mock(Good.class)),expected);


    }


    //test through salesdepartment
    @Test
    public void testSalesPrice() throws Exception {

        MacroII macroII = new MacroII(1l);
        Firm firm = mock(Firm.class); when(firm.getModel()).thenReturn(macroII);

        SalesDepartment dept = SalesDepartment.incompleteSalesDepartment(firm,mock(Market.class));
        AskPricingStrategy strategy = mock(AskPricingStrategy.class);
        when(strategy.price(any(Good.class))).thenReturn(decoratedPrice);

        dept.setAskPricingStrategy(strategy);
        dept.addReservationPrice(reservationPrice);

        Assert.assertEquals(dept.price(mock(Good.class)),expected);


    }
}
