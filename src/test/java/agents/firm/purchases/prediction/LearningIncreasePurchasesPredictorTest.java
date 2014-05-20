/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.prediction;

import agents.firm.purchases.PurchasesDepartment;
import financial.market.Market;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.scheduler.Priority;
import model.utilities.stats.collectors.PeriodicMarketObserver;
import model.utilities.stats.collectors.enums.MarketDataType;
import org.junit.Assert;
import org.junit.Test;
import sim.engine.Steppable;

import static org.mockito.Matchers.any;
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
 * @version 2013-08-04
 * @see
 */
public class LearningIncreasePurchasesPredictorTest
{



    @Test
    public void testPredictSalePrice() throws Exception
    {
        PeriodicMarketObserver.defaultDailyProbabilityOfObserving = 1f;



        Market market = mock(Market.class);

        when(market.getYesterdayVolume()).thenReturn(1);
        MacroII model = new MacroII(System.currentTimeMillis());
        LearningIncreasePurchasesPredictor predictor = new LearningIncreasePurchasesPredictor(market,model );
        predictor.setUsingWeights(true);

        //observation 1
        when(market.getNumberOfObservations()).thenReturn(1,2,3);
        when(market.getLastObservedDay()).thenReturn(0,1,2);
        when(market.getLatestObservation(MarketDataType.CLOSING_PRICE)).thenReturn(86d,84d,81d);
        //these are the data you were looking for:
        when(market.getObservationsRecordedTheseDays(MarketDataType.CLOSING_PRICE,new int[]{0,1,2})).thenReturn(
                new double[]{
                   86,84,81
                });
        when(market.getObservationsRecordedTheseDays(MarketDataType.VOLUME_PRODUCED,new int[]{0,1,2})).thenReturn(
                new double[]{
                        8,7,6
                });

        for (int i=0; i<3; i++)
            model.getPhaseScheduler().step(model);


        //this should regress to p=101.9 -2.6  * q
        //now Q doesn't matter anymore, only previous Price

        PurchasesDepartment department = mock(PurchasesDepartment.class);
        when(department.getAveragedClosingPrice()).thenReturn(200f);
        //the sales predictor will be predict for 9 (yesterdayVolume + 1)
        Assert.assertEquals(predictor.predictPurchasePriceWhenIncreasingProduction(department), 203); //200+2.6 (rounded)

    }




    @Test
    public void testScheduledProperly()
    {

        Market market = mock(Market.class);
        MacroII macroII = mock(MacroII.class);
        PeriodicMarketObserver.defaultDailyProbabilityOfObserving = .2f;

        new LearningIncreasePurchasesPredictor(market,macroII);

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
        PurchasesDepartment department = mock(PurchasesDepartment.class);



        //these are the data you were looking for:
        when(market.getObservationsRecordedTheseDays(MarketDataType.CLOSING_PRICE,new int[]{0})).thenReturn(
                new double[]{
                        50
                });
        when(market.getObservationsRecordedTheseDays(MarketDataType.VOLUME_PRODUCED,new int[]{0})).thenReturn(
                new double[]{
                        1
                });



        LearningIncreasePurchasesPredictor predictor = new LearningIncreasePurchasesPredictor(market,model );
        when(department.getAveragedClosingPrice()).thenReturn(50f);
        Assert.assertEquals(predictor.predictPurchasePriceWhenIncreasingProduction(department), 50);

        //with one observation, it still returns whatever the sales department says
        when(market.getLastObservedDay()).thenReturn(0);
        when(market.getLatestObservation(MarketDataType.CLOSING_PRICE)).thenReturn(0d);
        model.getPhaseScheduler().step(model);
        Assert.assertEquals(predictor.predictPurchasePriceWhenIncreasingProduction(department), 50);

        //with negative price the observation is ignored
        when(market.getLastObservedDay()).thenReturn(1);
        when(market.getLatestObservation(MarketDataType.CLOSING_PRICE)).thenReturn(-1d);
        model.getPhaseScheduler().step(model);
        Assert.assertEquals(predictor.predictPurchasePriceWhenIncreasingProduction(department), 50);


        //two observations, everything back to normal! (but the slope is 0, so no effect)
        when(market.getLastObservedDay()).thenReturn(2);
        when(market.getLatestObservation(MarketDataType.CLOSING_PRICE)).thenReturn(0d);
        model.getPhaseScheduler().step(model);
        when(market.getObservationsRecordedTheseDays(MarketDataType.CLOSING_PRICE,new int[]{0,2})).thenReturn(
                new double[]{
                        50,50
                });
        when(market.getObservationsRecordedTheseDays(MarketDataType.VOLUME_PRODUCED,new int[]{0,2})).thenReturn(
                new double[]{
                        1,1
                });
        Assert.assertEquals(predictor.predictPurchasePriceWhenIncreasingProduction(department), 50);


    }


}
