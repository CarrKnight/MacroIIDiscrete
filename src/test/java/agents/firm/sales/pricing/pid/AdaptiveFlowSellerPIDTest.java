package agents.firm.sales.pricing.pid;

import agents.firm.sales.SalesDepartment;
import financial.market.Market;
import goods.Good;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.scheduler.Priority;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class AdaptiveFlowSellerPIDTest {


    //moves in the right direction:


    @Test
    public void tooHighFlowMeansPriceShouldGoUp() throws Exception {

        final SalesDepartment sd = mock(SalesDepartment.class);
        final MacroII model = mock(MacroII.class); when(model.getCurrentPhase()).thenReturn(ActionOrder.ADJUST_PRICES);
        AdaptiveFlowSellerPID seller =
                new AdaptiveFlowSellerPID(sd,1,1,0,0,mock(Market.class),10, model);

        //okay, so price should start at 10
        Assert.assertEquals(10, seller.price(mock(Good.class)));

        //now for 10 times we step the seller, price should keep getting higher
        for(int i=0; i<10; i++)
        {
            int oldPrice = seller.price(mock(Good.class));
            when(sd.getTodayInflow()).thenReturn(500);
            when(sd.getTodayOutflow()).thenReturn(600);

            seller.step(model);


            Assert.assertTrue(seller.price(mock(Good.class)) > oldPrice);
        }


    }



    @Test
    public void tooLowFlowMeansPriceShouldGoUp() throws Exception {

        final SalesDepartment sd = mock(SalesDepartment.class);
        final MacroII model = mock(MacroII.class); when(model.getCurrentPhase()).thenReturn(ActionOrder.ADJUST_PRICES);
        AdaptiveFlowSellerPID seller =
                new AdaptiveFlowSellerPID(sd,1,1,0,0,mock(Market.class),10, model);

        //okay, so price should start at 10
        Assert.assertEquals(10, seller.price(mock(Good.class)));

        //now for 10 times we step the seller, price should keep getting higher
        for(int i=0; i<10; i++)
        {
            int oldPrice = seller.price(mock(Good.class));
            when(sd.getTodayInflow()).thenReturn(500);
            when(sd.getTodayOutflow()).thenReturn(400);

            seller.step(model);


            Assert.assertTrue(seller.price(mock(Good.class)) < oldPrice || oldPrice == 0);
        }


    }

    //test mathematically correct
    @Test
    public void correctMath() throws Exception {

        final SalesDepartment sd = mock(SalesDepartment.class);
        final MacroII model = mock(MacroII.class); when(model.getCurrentPhase()).thenReturn(ActionOrder.ADJUST_PRICES);
        AdaptiveFlowSellerPID seller =
                new AdaptiveFlowSellerPID(sd,1,1,0,0,mock(Market.class),100, model);

        //okay, so price should start at 100
        Assert.assertEquals(100, seller.price(mock(Good.class)));

        //if you are too low by 10
        when(sd.getTodayOutflow()).thenReturn(10);
        when(sd.getTodayInflow()).thenReturn(0);
        seller.step(model);
        //the price will be offset 100+10+10 (p,i = 1)
        Assert.assertEquals(120,seller.price(mock(Good.class)));


    }


    //test it reschedules
    @Test
    public void reschedules(){

        final SalesDepartment sd = mock(SalesDepartment.class);
        final MacroII model = mock(MacroII.class); when(model.getCurrentPhase()).thenReturn(ActionOrder.ADJUST_PRICES);
        AdaptiveFlowSellerPID seller =
                new AdaptiveFlowSellerPID(sd,1,1,0,0,mock(Market.class),100, model);

        verify(model).scheduleSoon(ActionOrder.ADJUST_PRICES,seller);
        seller.step(model);
        verify(model).scheduleTomorrow(ActionOrder.ADJUST_PRICES, seller, Priority.STANDARD);

    }

}