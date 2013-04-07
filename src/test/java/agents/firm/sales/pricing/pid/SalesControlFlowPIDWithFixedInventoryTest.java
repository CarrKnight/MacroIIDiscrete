package agents.firm.sales.pricing.pid;

import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentAllAtOnce;
import ec.util.MersenneTwisterFast;
import model.MacroII;
import model.utilities.ActionOrder;
import org.junit.Test;

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
 * @author carrknight
 * @version 2013-03-19
 * @see
 */
public class SalesControlFlowPIDWithFixedInventoryTest {

    //check that the target is 0 until it gets above acceptable
    @Test
    public void activatesCorrectly()
    {
        SalesDepartment department = mock(SalesDepartmentAllAtOnce.class);
        MacroII state = mock(MacroII.class); when(state.getCurrentPhase()).thenReturn(ActionOrder.THINK);
        SalesControlFlowPIDWithFixedInventory pricing = new SalesControlFlowPIDWithFixedInventory(department,10,50,state,0,0,0,new MersenneTwisterFast());


        //go through the build up phase
        for(int i=1; i <10; i++)
        {
            when(department.getTodayInflow()).thenReturn(5);
            when(department.getHowManyToSell()).thenReturn(i*5);
            pricing.step(state);
            assertEquals(pricing.getTarget(),0); //because you are building up
        }

        //now we are going to be ready
        when(department.getTodayInflow()).thenReturn(5);
        when(department.getHowManyToSell()).thenReturn(50);
        pricing.step(state);
        assertEquals(pricing.getTarget(),5); //because you are done building up
        assertEquals(pricing.getPhase(), SalesControlFlowPIDWithFixedInventory.SimpleInventoryAndFlowPIDPhase.SELL);

        //inventory goes down, but we should still be in sell phase
        when(department.getTodayInflow()).thenReturn(5);
        when(department.getHowManyToSell()).thenReturn(25);
        pricing.step(state);
        assertEquals(pricing.getTarget(),5); //still you should do your job, even though you are below that limit
        assertEquals(pricing.getPhase(), SalesControlFlowPIDWithFixedInventory.SimpleInventoryAndFlowPIDPhase.SELL);


        //back to buildup
        when(department.getTodayInflow()).thenReturn(5);
        when(department.getHowManyToSell()).thenReturn(5);
        pricing.step(state);
        assertEquals(pricing.getTarget(),0); //back to buildup!
        assertEquals(pricing.getPhase(), SalesControlFlowPIDWithFixedInventory.SimpleInventoryAndFlowPIDPhase.BUILDUP);



    }


    @Test
    public void rescheduleItselfTest()
    {
        MacroII state = mock(MacroII.class); when(state.getCurrentPhase()).thenReturn(ActionOrder.THINK);
        SalesDepartment department = mock(SalesDepartmentAllAtOnce.class);
        SalesControlFlowPIDWithFixedInventory pricing = new SalesControlFlowPIDWithFixedInventory(department,10,50,state,0,0,0,new MersenneTwisterFast());

        verify(state).scheduleSoon(ActionOrder.THINK,pricing);
        pricing.step(state);
        verify(state).scheduleTomorrow(ActionOrder.THINK,pricing);


    }

    @Test
    public void priceChanges()
    {

        SalesDepartment department = mock(SalesDepartmentAllAtOnce.class);
        MacroII state = mock(MacroII.class); when(state.getCurrentPhase()).thenReturn(ActionOrder.THINK);
        SalesControlFlowPIDWithFixedInventory pricing = new SalesControlFlowPIDWithFixedInventory(department,10,50,state,2,1,.01f,new MersenneTwisterFast());
        pricing.setInitialPrice(100); //so it's not 0
        //outflow stays at 0 for most of the test
        when(department.getTodayOutflow()).thenReturn(0);



        //go through the build up phase
        for(int i=1; i <10; i++)
        {
            when(department.getTodayInflow()).thenReturn(5);
            when(department.getHowManyToSell()).thenReturn(i * 5);
            pricing.step(state);
            assertEquals(pricing.getPrice(),100);
        }

        //now we are going to be ready
        when(department.getTodayInflow()).thenReturn(5);
        when(department.getHowManyToSell()).thenReturn(50);
        pricing.step(state);
        assertTrue(Long.toString(pricing.getPrice()),pricing.getPrice() < 100); //price should have gone down!!!!
        long newPrice = pricing.getPrice();


        //inventory goes down, but we should still be in sell phase
        when(department.getTodayInflow()).thenReturn(5);
        when(department.getHowManyToSell()).thenReturn(25);
        pricing.step(state);
        assertTrue(pricing.getPrice() < newPrice); //price should have gone down further
        newPrice = pricing.getPrice();

        //same target, but now today outflow is high!
        when(department.getTodayOutflow()).thenReturn(20);
        pricing.step(state);
        assertTrue(pricing.getPrice()>newPrice); //price should have gone up!
        newPrice = pricing.getPrice();


        when(department.getTodayOutflow()).thenReturn(0);
        //back to buildup
        when(department.getTodayInflow()).thenReturn(5);
        when(department.getHowManyToSell()).thenReturn(5);
        pricing.step(state);

        //this is actually not true:
//        assertEquals(pricing.getPrice(),newPrice); //price is back to be stuck
        //because the P makes it RAISE price

    }


    //as customary, the "fully dressed" test is carried out in the SimpleSeller and Monopolist scenario tests

}
