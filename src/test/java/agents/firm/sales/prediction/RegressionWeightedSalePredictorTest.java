package agents.firm.sales.prediction;

import agents.firm.sales.SalesDepartment;
import financial.market.Market;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.scheduler.Priority;
import model.utilities.stats.collectors.enums.MarketDataType;
import model.utilities.stats.collectors.PeriodicMarketObserver;
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

        verify(macroII).scheduleAnotherDay(ActionOrder.CLEANUP_DATA_GATHERING,observer,5, Priority.AFTER_STANDARD);
        predictor.setDailyProbabilityOfObserving(.3f);
        observer.step(macroII);
        verify(macroII).scheduleAnotherDay(ActionOrder.CLEANUP_DATA_GATHERING,observer,3, Priority.AFTER_STANDARD);


    }


    //Check defaults
    @Test
    public void testExtremes()
    {
        PeriodicMarketObserver.defaultDailyProbabilityOfObserving = 1f;

        //no observations, should return whatever the sales department says
        Market market = mock(Market.class);
        MacroII model = new MacroII(System.currentTimeMillis());
        SalesDepartment department = mock(SalesDepartment.class);


        //these are the data you were looking for:
        when(market.getObservationsRecordedTheseDays(MarketDataType.CLOSING_PRICE, new int[]{0})).thenReturn(
                new double[]{
                        50
                });
        when(market.getObservationsRecordedTheseDays(MarketDataType.VOLUME_TRADED,new int[]{0})).thenReturn(
                new double[]{
                        1
                });


        RegressionWeightedSalePredictor predictor = new RegressionWeightedSalePredictor(market,model );
        when(department.hypotheticalSalePrice()).thenReturn(50l);
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(department, 1000l, 1), 50l);

        //with one observation, it still returns whatever the sales department says
        when(market.getLastObservedDay()).thenReturn(0);
        when(market.getLatestObservation(MarketDataType.CLOSING_PRICE)).thenReturn(0d);
        model.getPhaseScheduler().step(model);
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(department, 1000l, 1), 50l);

        //with no volume the observation is ignored
        when(market.getLastObservedDay()).thenReturn(1);
        when(market.getLatestObservation(MarketDataType.CLOSING_PRICE)).thenReturn(-1d);
        model.getPhaseScheduler().step(model);
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(department, 1000l, 1),50l);


        //two observations, everything back to normal! (but the slope is 0, so no effect)
        when(market.getLastObservedDay()).thenReturn(2);
        when(market.getLatestObservation(MarketDataType.CLOSING_PRICE)).thenReturn(1d);
        when(market.getObservationsRecordedTheseDays(MarketDataType.CLOSING_PRICE, new int[]{0, 2})).thenReturn(
                new double[]{
                        50,50
                });
        when(market.getObservationsRecordedTheseDays(MarketDataType.VOLUME_CONSUMED, new int[]{0, 2})).thenReturn(
                new double[]{
                        1, 1
                });
        model.getPhaseScheduler().step(model);
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(department, 50l, 1),50l);


    }
    
}
