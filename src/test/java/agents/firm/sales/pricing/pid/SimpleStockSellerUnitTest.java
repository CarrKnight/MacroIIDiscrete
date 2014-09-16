package agents.firm.sales.pricing.pid;

import agents.firm.sales.SalesDepartment;
import goods.Good;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.pid.PIDController;
import model.utilities.scheduler.Priority;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SimpleStockSellerUnitTest {




    //moves in the right direction:


    @Test
    public void tooLowStockMeansPriceShouldGoUp() throws Exception {

        final SalesDepartment sd = mock(SalesDepartment.class);
        final PIDController pid = new PIDController(1, 1, 0, 0);
        SimpleStockSeller seller = new SimpleStockSeller(sd,500,
                pid,10,mock(MacroII.class));

        //okay, so price should start at 10
        Assert.assertEquals(10,seller.price(mock(Good.class)));

        //now for 10 times we step the seller, price should keep getting higher
        for(int i=0; i<10; i++)
        {
            int oldPrice = seller.price(mock(Good.class));
            when(sd.getHowManyToSell()).thenReturn(400); //target is 500

            seller.step(mock(MacroII.class));

            Assert.assertTrue(seller.price(mock(Good.class)) > oldPrice);
        }


    }



    @Test
    public void tooHighStockMeansPriceShouldGoUp() throws Exception {

        final SalesDepartment sd = mock(SalesDepartment.class);
        final PIDController pid = new PIDController(1, 1, 0, 0);
        SimpleStockSeller seller = new SimpleStockSeller(sd,500,
                pid,100,mock(MacroII.class));

        //okay, so price should start at 10
        Assert.assertEquals(100,seller.price(mock(Good.class)));

        //now for 10 times we step the seller, price should keep getting higher
        for(int i=0; i<10; i++)
        {
            int oldPrice = seller.price(mock(Good.class));
            when(sd.getHowManyToSell()).thenReturn(600); //target is 500

            seller.step(mock(MacroII.class));

            Assert.assertTrue(seller.price(mock(Good.class)) < oldPrice || oldPrice == 0);
        }


    }

    //test mathematically correct
    @Test
    public void correctMath() throws Exception {

        final SalesDepartment sd = mock(SalesDepartment.class);
        final PIDController pid = new PIDController(1, 1, 0, 0);
        SimpleStockSeller seller = new SimpleStockSeller(sd,500,
                pid,100,mock(MacroII.class));

        //okay, so price should start at 100
        Assert.assertEquals(100,seller.price(mock(Good.class)));

        //if you are too low by 10
        when(sd.getHowManyToSell()).thenReturn(490);
        seller.step(mock(MacroII.class));
        //the price will be offset 100+10+10 (p,i = 1)
        Assert.assertEquals(120,seller.price(mock(Good.class)));


    }


    //test it reschedules
    @Test
    public void reschedules(){

        final PIDController pid = new PIDController(1, 1, 0, 0);
        final MacroII model = mock(MacroII.class);
        SimpleStockSeller seller = new SimpleStockSeller(mock(SalesDepartment.class),500,
                pid,100, model);

        verify(model).scheduleSoon(ActionOrder.ADJUST_PRICES,seller);
        seller.step(model);
        verify(model).scheduleTomorrow(ActionOrder.ADJUST_PRICES, seller, Priority.STANDARD);

    }
}