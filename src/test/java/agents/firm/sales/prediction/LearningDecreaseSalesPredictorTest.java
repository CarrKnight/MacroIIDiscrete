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
import org.junit.Assert;
import org.junit.Test;
import sim.engine.Steppable;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
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
 * @version 2013-07-12
 * @see
 */
public class LearningDecreaseSalesPredictorTest
{

    @Test
    public void testPredictSalePrice() throws Exception
    {


        Market market = mock(Market.class);
        when(market.getYesterdayVolume()).thenReturn(1);
        MacroII model = new MacroII(System.currentTimeMillis());
        LearningDecreaseSalesPredictor predictor = new LearningDecreaseSalesPredictor(market,model,1f,true );

        //observation 1
        when(market.getNumberOfObservations()).thenReturn(1,2,3);
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


        //this should regress to p=101.9 -2.6  * q
        //now Q doesn't matter anymore, only previous Price

        SalesDepartment department = mock(SalesDepartment.class);
        when(department.getAveragedPrice()).thenReturn(200d);
        //the sales predictor will be predict for 9 (yesterdayVolume + 1)
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(department, 100, 1), 197,.0001f); //200-2.6 (rounded)








    }


    @Test
    public void testScheduledProperly()
    {

        Market market = mock(Market.class);
        MacroII macroII = mock(MacroII.class);

        new LearningDecreaseSalesPredictor(market,macroII,.2f,true);

        verify(macroII).scheduleAnotherDay(any(ActionOrder.class),any(Steppable.class),
                anyInt(),any(Priority.class));
    }


    //Check defaults
    @Test
    public void testExtremes()
    {

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


        LearningDecreaseSalesPredictor predictor = new LearningDecreaseSalesPredictor(market,model ,1f,true);
        when(department.getAveragedPrice()).thenReturn(50d); //current department pricing 100$
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(department, 1000, 1),50,.0001f);

        //with one observation, it still returns whatever the sales department says
        when(market.getLastObservedDay()).thenReturn(0);
        when(market.getLatestObservation(MarketDataType.CLOSING_PRICE)).thenReturn(0d);
        model.getPhaseScheduler().step(model);
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(department, 1000, 1),50,.0001f);

        //with no volume the observation is ignored
        when(market.getLastObservedDay()).thenReturn(1);
        when(market.getLatestObservation(MarketDataType.CLOSING_PRICE)).thenReturn(-1d);
        model.getPhaseScheduler().step(model);
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(department, 1000, 1),50,.0001f);


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
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(department, 50, 1),50,.0001f);


    }

}
