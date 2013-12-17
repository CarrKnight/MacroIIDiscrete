package agents.firm.sales.pricing.pid;

import agents.firm.Firm;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentAllAtOnce;
import ec.util.MersenneTwisterFast;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.scheduler.PhaseScheduler;
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
 * @version 2013-02-08
 * @see
 */
public class SmoothedDailyInventoryPricingStrategyTest {

    /**
     * check that moving average is actually occurring
     */
    @Test
    public void testMovingAverage()
    {

        //I assume initially target is 0
        SalesDepartment department = mock(SalesDepartmentAllAtOnce.class);
        when(department.getRandom()).thenReturn(new MersenneTwisterFast());
        Firm firm = mock(Firm.class); when(department.getFirm()).thenReturn(firm);
        MacroII model = new MacroII(1l); when(firm.getModel()).thenReturn(model); when(department.getModel()).thenReturn(model);
        PhaseScheduler scheduler = mock(PhaseScheduler.class);
        model.setPhaseScheduler(scheduler);
        when(scheduler.getCurrentPhase()).thenReturn(ActionOrder.PREPARE_TO_TRADE);




        SmoothedDailyInventoryPricingStrategy strategy = new SmoothedDailyInventoryPricingStrategy(department);
        strategy.setHowManyTimesTheDailyInflowShouldTheInventoryBe(7);
        //force MA to be of length 10


        assertEquals(strategy.getTargetInventory(),0);
        //from now on inflow is 100
        when(department.getTodayInflow()).thenReturn(100); //
        strategy.step(model);
        //should be somewhere between 0 and 100 but neither of the two extremes
        assertTrue(strategy.getTargetInventory() > 0);
        assertTrue(strategy.getTargetInventory() < 100*7);

        for(int i=0; i<10;i++)
        {
            strategy.step(model);
        }

        assertEquals(strategy.getTargetInventory(),700); //now it should be exactly 100*7







    }



}
