package agents.firm.sales.pricing.decorators;

import agents.firm.Firm;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentFactory;
import agents.firm.sales.pricing.AskPricingStrategy;
import financial.market.Market;
import goods.Good;
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
 * @version 2012-11-12
 * @see
 */
@RunWith(value = Parameterized.class)
public class AskReservationPriceDecoratorTest {

    private int decoratedPrice;

    private int reservationPrice;

    private int expected;

    @Parameterized.Parameters
    public static Collection<Integer[]> getTestParameters(){

        return Arrays.asList(new Integer[][]{
                {10,20,20},
                {20,10,20},
                {10,10,10},
                {0,5,5}


        });


    }

    public AskReservationPriceDecoratorTest(int decoratedPrice, int reservationPrice, int expected) {
        this.decoratedPrice = decoratedPrice;
        this.reservationPrice = reservationPrice;
        this.expected = expected;
    }

    @Test
    public void testMockPrice() throws Exception {


        AskPricingStrategy strategy = mock(AskPricingStrategy.class);
        when(strategy.price(any(Good.class))).thenReturn(decoratedPrice);
        strategy = new AskReservationPriceDecorator(strategy,reservationPrice);
        assertEquals(strategy.price(mock(Good.class)), expected);


    }


    //test through salesdepartment
    @Test
    public void testSalesPrice() throws Exception {

        MacroII macroII = new MacroII(1);
        Firm firm = mock(Firm.class); when(firm.getModel()).thenReturn(macroII);

        final Market stub = mock(Market.class);
        when(stub.getGoodType()).thenReturn(UndifferentiatedGoodType.GENERIC);
        SalesDepartment dept = SalesDepartmentFactory.incompleteSalesDepartment(firm, stub);
        AskPricingStrategy strategy = mock(AskPricingStrategy.class);
        when(strategy.price(any(Good.class))).thenReturn(decoratedPrice);

        dept.setAskPricingStrategy(strategy);
        dept.addReservationPrice(reservationPrice);

        assertEquals(dept.price(mock(Good.class)), expected);


    }
}
