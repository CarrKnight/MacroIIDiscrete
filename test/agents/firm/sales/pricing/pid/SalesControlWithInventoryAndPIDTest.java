package agents.firm.sales.pricing.pid;

import agents.firm.sales.SalesDepartment;
import goods.Good;
import junit.framework.Assert;
import model.MacroII;
import org.junit.Test;

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
 * @version 2013-02-06
 * @see
 */
public class SalesControlWithInventoryAndPIDTest {


    @Test
    public void priceShouldGoDownWhenInventoryIsTooMuch()
    {

        SalesDepartment department = mock(SalesDepartment.class);
        SalesControlWithFixedInventoryAndPID pid = new SalesControlWithFixedInventoryAndPID(department);
        pid.setInitialPrice(100); //set initial price at 100

        Assert.assertEquals(pid.getTargetInventory(),5); //assume target is 5
        when(department.getHowManyToSell()).thenReturn(10); //you still have 10 to sell
        //step it
        pid.step(mock(MacroII.class));
        //now price should be BELOW 100
        Assert.assertTrue(pid.price(mock(Good.class)) < 100l);
        System.out.println("new price is:" + pid.price(mock(Good.class) ));






    }

    @Test
    public void priceShouldGoUpWhenInventoryIsTooLow()
    {

        SalesDepartment department = mock(SalesDepartment.class);
        SalesControlWithFixedInventoryAndPID pid = new SalesControlWithFixedInventoryAndPID(department);
        pid.setInitialPrice(100); //set initial price at 100

        Assert.assertEquals(pid.getTargetInventory(),5); //assume target is 5
        when(department.getHowManyToSell()).thenReturn(0); //you have none to sell
        //step it
        pid.step(mock(MacroII.class));
        //now price should be BELOW 100
        Assert.assertTrue(pid.price(mock(Good.class)) > 100l);
        System.out.println("new price is:" + pid.price(mock(Good.class) ));


        //now also test that it goes down
        long newPrice =   pid.price(mock(Good.class));
        Assert.assertEquals(pid.getTargetInventory(),5); //your target should still be 5
        when(department.getHowManyToSell()).thenReturn(10); //you have too many
        Assert.assertTrue(pid.price(mock(Good.class)) > newPrice ); // price should have gone up









    }


}
