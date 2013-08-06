/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.prediction;

import agents.firm.purchases.PurchasesDepartment;
import financial.Market;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.scheduler.Priority;
import model.utilities.stats.PeriodicMarketObserver;
import org.junit.Assert;
import org.junit.Test;
import sim.engine.Steppable;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyFloat;
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
        when(market.getYesterdayLastPrice()).thenReturn(86l);
        when(market.countYesterdayProductionByRegisteredSellers()).thenReturn(8);
        model.getPhaseScheduler().step(model);
        //observation 2
        when(market.getYesterdayLastPrice()).thenReturn(84l);
        when(market.countYesterdayProductionByRegisteredSellers()).thenReturn(7);
        model.getPhaseScheduler().step(model);
        //observation 3
        when(market.getYesterdayLastPrice()).thenReturn(81l);
        when(market.countYesterdayProductionByRegisteredSellers()).thenReturn(6);
        model.getPhaseScheduler().step(model);


        //this should regress to p=101.9 -2.6  * q
        //now Q doesn't matter anymore, only previous Price

        PurchasesDepartment department = mock(PurchasesDepartment.class);
        when(department.maxPrice(any(GoodType.class),any(Market.class))).thenReturn(200l);
        //the sales predictor will be predict for 9 (yesterdayVolume + 1)
        Assert.assertEquals(predictor.predictPurchasePrice(department), 203l); //200+2.6 (rounded)

    }




    @Test
    public void testScheduledProperly()
    {

        Market market = mock(Market.class);
        MacroII macroII = mock(MacroII.class);
        PeriodicMarketObserver.defaultDailyProbabilityOfObserving = .2f;

        new LearningIncreasePurchasesPredictor(market,macroII);

        verify(macroII).scheduleAnotherDayWithFixedProbability(any(ActionOrder.class),any(Steppable.class),
                anyFloat(),any(Priority.class));
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

        LearningIncreasePurchasesPredictor predictor = new LearningIncreasePurchasesPredictor(market,model );
        when(department.maxPrice(any(GoodType.class),any(Market.class))).thenReturn(50l);
        Assert.assertEquals(predictor.predictPurchasePrice(department), 50l);

        //with one observation, it still returns whatever the sales department says
        when(market.getYesterdayLastPrice()).thenReturn(10l);
        when(market.getYesterdayVolume()).thenReturn(1);
        model.getPhaseScheduler().step(model);
        Assert.assertEquals(predictor.predictPurchasePrice(department), 50l);

        //with no volume the observation is ignored
        when(market.getYesterdayLastPrice()).thenReturn(10l);
        when(market.getYesterdayVolume()).thenReturn(0);
        model.getPhaseScheduler().step(model);
        Assert.assertEquals(predictor.predictPurchasePrice(department), 50l);


        //two observations, everything back to normal! (but the slope is 0, so no effect)
        when(market.getYesterdayLastPrice()).thenReturn(10l);
        when(market.getYesterdayVolume()).thenReturn(1);
        model.getPhaseScheduler().step(model);
        Assert.assertEquals(predictor.predictPurchasePrice(department), 50l);


    }


}
