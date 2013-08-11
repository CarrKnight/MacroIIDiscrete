package agents.firm.sales.prediction;

import agents.firm.sales.SalesDepartment;
import financial.Market;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.scheduler.Priority;
import model.utilities.stats.PeriodicMarketObserver;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
 * @version 2013-07-13
 * @see
 */
public class RegressionWeightedSalePredictorTest {


    @Test
    public void testPredictSalePrice() throws Exception
    {

        PeriodicMarketObserver observer = mock(PeriodicMarketObserver.class);
        Market market = mock(Market.class);
        RegressionWeightedSalePredictor predictor = new RegressionWeightedSalePredictor(observer );

        when(observer.getNumberOfObservations()).thenReturn(3);
        when(observer.getPricesObservedAsArray()).thenReturn(new double[]{86,84,81});
        when(observer.getQuantitiesConsumedObservedAsArray()).thenReturn(new double[]{6, 7, 8});
        when(observer.getLastUntrasformedQuantityTraded()).thenReturn(8d);


        //this should regress to p=101.9 -2.6 * q

        predictor.updateModel();
        Assert.assertEquals(predictor.getSlope(),-2.6d,.01);
        Assert.assertEquals(predictor.getIntercept(),101.9d,.01);
        //the sales predictor will be predict for 9 (yesterdayVolume + 1)










    }


    @Test
    public void testScheduledProperly() throws NoSuchFieldException, IllegalAccessException {

        Market market = mock(Market.class);
        MacroII macroII = mock(MacroII.class);
        PeriodicMarketObserver.defaultDailyProbabilityOfObserving = .2f;

        RegressionWeightedSalePredictor predictor = new RegressionWeightedSalePredictor(market, macroII);


        //grab through reflection the reference to the observer!
        Field field = RegressionSalePredictor.class.getDeclaredField("observer");
        field.setAccessible(true);
        PeriodicMarketObserver observer = (PeriodicMarketObserver) field.get(predictor);

        verify(macroII).scheduleAnotherDay(ActionOrder.DAWN,observer,5, Priority.AFTER_STANDARD);
        predictor.setDailyProbabilityOfObserving(.3f);
        observer.step(macroII);
        verify(macroII).scheduleAnotherDay(ActionOrder.DAWN,observer,3, Priority.AFTER_STANDARD);


    }


    //Check defaults
    @Test
    public void testExtremes()
    {
        PeriodicMarketObserver.defaultDailyProbabilityOfObserving = 1f;

        //no observations, should return whatever the sales department says
        Market market = mock(Market.class);
        MacroII macroII = new MacroII(1l);
        SalesDepartment department = mock(SalesDepartment.class);

        RegressionWeightedSalePredictor predictor = new RegressionWeightedSalePredictor(market, macroII);
        when(department.hypotheticalSalePrice()).thenReturn(50l);
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(department, 1000l, 1),50l);

        //with one observation, it still returns whatever the sales department says
        when(market.getYesterdayLastPrice()).thenReturn(10l);
        when(market.getYesterdayVolume()).thenReturn(1);
        macroII.getPhaseScheduler().step(macroII);
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(department, 1000l,1 ),50l);

        //with no price the observation is ignored
        when(market.getYesterdayLastPrice()).thenReturn(-1l);
        when(market.getYesterdayVolume()).thenReturn(0);
        macroII.getPhaseScheduler().step(macroII);
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(department, 1000l,1 ),50l);


        //two observations, everything back to normal!
        when(market.getYesterdayLastPrice()).thenReturn(10l);
        when(market.getYesterdayVolume()).thenReturn(1);
        macroII.getPhaseScheduler().step(macroII);
        when(department.hypotheticalSalePrice()).thenReturn(10l);
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(department, 1000l,1 ),10l);


    }
    
}
