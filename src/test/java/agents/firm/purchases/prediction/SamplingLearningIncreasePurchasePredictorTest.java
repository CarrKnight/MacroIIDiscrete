/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.prediction;

import agents.firm.Firm;
import agents.firm.purchases.PurchasesDepartment;
import financial.market.Market;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.utilities.stats.collectors.PeriodicMarketObserver;
import model.utilities.stats.collectors.enums.MarketDataType;
import model.utilities.stats.collectors.enums.PurchasesDataType;
import org.junit.Assert;
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
 * @version 2013-09-13
 * @see
 */
public class SamplingLearningIncreasePurchasePredictorTest 
{
    @Test
    public void testPredictSalePrice() throws Exception
    {
        PeriodicMarketObserver.defaultDailyProbabilityOfObserving = 1f;



        PurchasesDepartment department = mock(PurchasesDepartment.class);
        when(department.getFirm()).thenReturn(mock(Firm.class));
        MacroII model = new MacroII(System.currentTimeMillis());
        SamplingLearningIncreasePurchasePredictor predictor = new SamplingLearningIncreasePurchasePredictor();
        predictor.setHowManyDaysOnAverageToSample(1);

        //observation 1
        when(department.getLastObservedDay()).thenReturn(2);
        when(department.getStartingDay()).thenReturn(0);
        when(department.getGoodType()).thenReturn(UndifferentiatedGoodType.GENERIC);

        //these are the data you were looking for:
        when(department.getObservationsRecordedTheseDays(PurchasesDataType.CLOSING_PRICES, new int[]{0, 1, 2})).thenReturn(
                new double[]{
                        86, 84, 81
                });
        when(department.getObservationsRecordedTheseDays(PurchasesDataType.WORKERS_CONSUMING_THIS_GOOD,new int[]{0,1,2})).thenReturn(
                new double[]{
                        8,7,6
                });
        when(department.getObservationsRecordedTheseDays(PurchasesDataType.DEMAND_GAP,new int[]{0,1,2})).thenReturn(
                new double[]{
                        0,0,0
                });




        //this should regress to p=101.9 -2.6  * q
        //now Q doesn't matter anymore, only previous Price

        when(department.getAveragedClosingPrice()).thenReturn(200f);
        //the sales predictor will be predict for 9 (yesterdayVolume + 1)
        Assert.assertEquals(predictor.predictPurchasePriceWhenIncreasingProduction(department), 203); //200+2.6 (rounded)

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

        FixedIncreasePurchasesPredictor.defaultIncrementDelta=0;


        when(department.getLastObservedDay()).thenReturn(0);
        when(department.getStartingDay()).thenReturn(0);
        when(department.getFirm()).thenReturn(mock(Firm.class));




        SamplingLearningIncreasePurchasePredictor predictor = new SamplingLearningIncreasePurchasePredictor();
        when(department.getAveragedClosingPrice()).thenReturn(50f);
        Assert.assertEquals(predictor.predictPurchasePriceWhenIncreasingProduction(department), 50);

        //with one observation, it still returns whatever the sales department says

        when(department.getLastObservedDay()).thenReturn(1);
        when(department.getStartingDay()).thenReturn(0);

        Assert.assertEquals(predictor.predictPurchasePriceWhenIncreasingProduction(department), 50);

        //with negative price the observation is ignored
        when(market.getLastObservedDay()).thenReturn(3);
        //these are the data you were looking for:
        when(department.getObservationsRecordedTheseDays(PurchasesDataType.CLOSING_PRICES,new int[]{0,1,2})).thenReturn(
                new double[]{
                        1, -1, 1
                });
        when(department.getObservationsRecordedTheseDays(PurchasesDataType.WORKERS_CONSUMING_THIS_GOOD,new int[]{0,1,2})).thenReturn(
                new double[]{
                        5,5,5
                });
        when(department.getObservationsRecordedTheseDays(PurchasesDataType.DEMAND_GAP,new int[]{0,1,2})).thenReturn(
                new double[]{
                        0,0,0
                });
        Assert.assertEquals(predictor.predictPurchasePriceWhenIncreasingProduction(department), 50);


        //two observations, everything back to normal! (but the slope is 0, so no effect)
        when(market.getLastObservedDay()).thenReturn(3);
        when(market.getLatestObservation(MarketDataType.CLOSING_PRICE)).thenReturn(0d);
        model.getPhaseScheduler().step(model);
        when(department.getObservationsRecordedTheseDays(PurchasesDataType.CLOSING_PRICES,new int[]{0,1,2})).thenReturn(
                new double[]{
                        1, 1, 1
                });
        when(department.getObservationsRecordedTheseDays(PurchasesDataType.WORKERS_CONSUMING_THIS_GOOD,new int[]{0,1,2})).thenReturn(
                new double[]{
                        5,5,5
                });
        when(department.getObservationsRecordedTheseDays(PurchasesDataType.DEMAND_GAP,new int[]{0,1,2})).thenReturn(
                new double[]{
                        0,0,0
                });
        Assert.assertEquals(predictor.predictPurchasePriceWhenIncreasingProduction(department), 50);


        FixedIncreasePurchasesPredictor.defaultIncrementDelta=1;

    }
    
    
}
