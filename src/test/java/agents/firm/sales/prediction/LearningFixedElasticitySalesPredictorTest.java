/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
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
import sim.engine.Steppable;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
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
 * @version 2013-07-17
 * @see
 */
public class LearningFixedElasticitySalesPredictorTest {


    @Test
    public void testPredictSalePrice() throws Exception
    {
        PeriodicMarketObserver.defaultDailyProbabilityOfObserving = 1f;


        Market market = mock(Market.class);
        when(market.getYesterdayVolume()).thenReturn(1);
        MacroII model = new MacroII(System.currentTimeMillis());
        LearningFixedElasticitySalesPredictor predictor = new LearningFixedElasticitySalesPredictor(market,model );

        //observation 1
        when(market.getLastObservedDay()).thenReturn(0,1,2);
        when(market.getLatestObservation(MarketDataType.CLOSING_PRICE)).thenReturn(86d, 84d, 81d);
        //these are the data you were looking for:
        when(market.getObservationsRecordedTheseDays(MarketDataType.CLOSING_PRICE, new int[]{0, 1, 2})).thenReturn(
                new double[]{
                        86,84,81
                });
        when(market.getObservationsRecordedTheseDays(MarketDataType.VOLUME_CONSUMED, new int[]{0, 1, 2})).thenReturn(
                new double[]{
                        6,7,8
                });

        for (int i=0; i<3; i++)
            model.getPhaseScheduler().step(model);


        //now Q doesn't matter anymore, only previous Price

        SalesDepartment department = mock(SalesDepartment.class);
        when(department.hypotheticalSalePrice(anyLong())).thenReturn(200l);
        when(market.getObservationRecordedThisDay(MarketDataType.CLOSING_PRICE,2)).thenReturn(81d);
        when(market.getObservationRecordedThisDay(MarketDataType.VOLUME_CONSUMED,2)).thenReturn(8d);
        //the sales predictor will be predicting for 9 (yesterdayVolume + 1)
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(department, 100l, 1), 198l); //200-2.09 (rounded)








    }


    @Test
    public void testScheduledProperly()
    {

        Market market = mock(Market.class);
        MacroII macroII = mock(MacroII.class);
        PeriodicMarketObserver.defaultDailyProbabilityOfObserving = .2f;

        new LearningFixedElasticitySalesPredictor(market,macroII);

        verify(macroII).scheduleAnotherDay(any(ActionOrder.class),any(Steppable.class),
                anyInt(),any(Priority.class));
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
        when(market.getObservationsRecordedTheseDays(MarketDataType.CLOSING_PRICE,new int[]{0})).thenReturn(
                new double[]{
                        50
                });
        when(market.getObservationsRecordedTheseDays(MarketDataType.VOLUME_CONSUMED,new int[]{0})).thenReturn(
                new double[]{
                        1
                });


        LearningFixedElasticitySalesPredictor predictor = new LearningFixedElasticitySalesPredictor(market,model );
        when(department.hypotheticalSalePrice(anyLong())).thenReturn(50l);
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(department, 1000l, 1),50l);

        //with one observation, it still returns whatever the sales department says
        when(market.getLastObservedDay()).thenReturn(0);
        when(market.getLatestObservation(MarketDataType.CLOSING_PRICE)).thenReturn(0d);
        model.getPhaseScheduler().step(model);
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(department, 1000l, 1),50l);

        //with no volume the observation is ignored
        when(market.getLastObservedDay()).thenReturn(1);
        when(market.getLatestObservation(MarketDataType.CLOSING_PRICE)).thenReturn(-1d);
        model.getPhaseScheduler().step(model);
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(department, 1000l, 1),50l);


        //two observations, everything back to normal! (but the slope is 0, so no effect)
        when(market.getLastObservedDay()).thenReturn(2);
        when(market.getLatestObservation(MarketDataType.CLOSING_PRICE)).thenReturn(1d);
        when(market.getObservationsRecordedTheseDays(MarketDataType.CLOSING_PRICE,new int[]{0,2})).thenReturn(
                new double[]{
                        50,50
                });
        when(market.getObservationsRecordedTheseDays(MarketDataType.VOLUME_CONSUMED,new int[]{0,2})).thenReturn(
                new double[]{
                        1,1
                });
        model.getPhaseScheduler().step(model);
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(department, 50l, 1),50l);


    }

}
