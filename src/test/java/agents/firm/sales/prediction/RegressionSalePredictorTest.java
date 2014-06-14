/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

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
 * @version 2013-07-11
 * @see
 */
public class RegressionSalePredictorTest {
    @Test
    public void testPredictSalePrice() throws Exception
    {


        PeriodicMarketObserver observer = mock(PeriodicMarketObserver.class);
        RegressionSalePredictor predictor = new RegressionSalePredictor(observer );



        when(observer.getNumberOfObservations()).thenReturn(3);
        when(observer.getPricesObservedAsArray()).thenReturn(new double[]{86,84,81});
        when(observer.getQuantitiesConsumedObservedAsArray()).thenReturn(new double[]{6,7,8});
        when(observer.getLastUntrasformedQuantityTraded()).thenReturn(8d);

        //this should regress to p=101.2 - 2.5 * q


        //the sales predictor will be predict for 9 (yesterdayVolume + 1)
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(mock(SalesDepartment.class), 100, 1),79,.0001f);








    }


    @Test
    public void testPredictSalePriceWithLogs() throws Exception
    {

        PeriodicMarketObserver observer = mock(PeriodicMarketObserver.class);
        MacroII model = new MacroII(1);
        RegressionSalePredictor predictor = new RegressionSalePredictor(observer );
        predictor.setQuantityTransformer(LearningFixedElasticitySalesPredictor.logTransformer);
        predictor.setPriceTransformer(LearningFixedElasticitySalesPredictor.logTransformer,
                LearningFixedElasticitySalesPredictor.expTransformer);

        //observation 1

        when(observer.getNumberOfObservations()).thenReturn(3);
        when(observer.getPricesObservedAsArray()).thenReturn(new double[]{86,84,81});
        when(observer.getQuantitiesConsumedObservedAsArray()).thenReturn(new double[]{6, 7, 8});
        when(observer.getLastUntrasformedQuantityTraded()).thenReturn(8d);


        //this should regress to log(p)=4.8275  -0.2068 * log(q)

        //the sales predictor will be predict for 9 (yesterdayVolume + 1)
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(mock(SalesDepartment.class), 100, 1),79,.0001f);








    }


    @Test
    public void testScheduledProperly() throws NoSuchFieldException, IllegalAccessException {

        Market market = mock(Market.class);
        MacroII macroII = mock(MacroII.class);
        PeriodicMarketObserver.defaultDailyProbabilityOfObserving = .2f;

        RegressionSalePredictor predictor = new RegressionSalePredictor(market, macroII);

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


        RegressionSalePredictor predictor = new RegressionSalePredictor(market,model );
        when(department.hypotheticalSalePrice()).thenReturn(50);
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(department, 1000, 1), 50,.0001f);

        //with one observation, it still returns whatever the sales department says
        when(market.getLastObservedDay()).thenReturn(0);
        when(market.getLatestObservation(MarketDataType.CLOSING_PRICE)).thenReturn(0d);
        model.getPhaseScheduler().step(model);
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(department, 1000, 1), 50,.0001f);

        //with no volume the observation is ignored
        when(market.getLastObservedDay()).thenReturn(1);
        when(market.getLatestObservation(MarketDataType.CLOSING_PRICE)).thenReturn(-1d);
        model.getPhaseScheduler().step(model);
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(department, 1000, 1),50,.0001f);


        //two observations, everything back to normal! (but the slope is 0, so no effect)
        when(market.getLastObservedDay()).thenReturn(2);
        when(market.getLatestObservation(MarketDataType.CLOSING_PRICE)).thenReturn(1d);
        when(market.getObservationsRecordedTheseDays(MarketDataType.CLOSING_PRICE, new int[]{0, 2})).thenReturn(
                new double[]{
                        50,50
                });
        when(market.getObservationsRecordedTheseDays(MarketDataType.VOLUME_CONSUMED, new int[]{0, 2})).thenReturn(
                new double[]{
                        1,1
                });
        model.getPhaseScheduler().step(model);
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(department, 50, 1),50,.0001f);


    }
}
