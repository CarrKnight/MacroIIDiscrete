/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.sales.SalesDepartment;
import financial.Market;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.scheduler.Priority;
import model.utilities.stats.PeriodicMarketObserver;
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
        MacroII model = new MacroII(System.currentTimeMillis());
        when(market.getYesterdayVolume()).thenReturn(1);

        LearningFixedElasticitySalesPredictor predictor = new LearningFixedElasticitySalesPredictor(market,model );

        //observation 1
        when(market.getYesterdayLastPrice()).thenReturn(86l);
        when(market.countYesterdayConsumptionByRegisteredBuyers()).thenReturn(6);
        model.getPhaseScheduler().step(model);
        //observation 2
        when(market.getYesterdayLastPrice()).thenReturn(84l);
        when(market.countYesterdayConsumptionByRegisteredBuyers()).thenReturn(7);
        model.getPhaseScheduler().step(model);
        //observation 3
        when(market.getYesterdayLastPrice()).thenReturn(81l);
        when(market.countYesterdayConsumptionByRegisteredBuyers()).thenReturn(8);
        model.getPhaseScheduler().step(model);


        //now Q doesn't matter anymore, only previous Price

        SalesDepartment department = mock(SalesDepartment.class);
        when(department.hypotheticalSalePrice(anyLong())).thenReturn(200l);
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

        LearningFixedElasticitySalesPredictor predictor = new LearningFixedElasticitySalesPredictor(market,model );
        when(department.hypotheticalSalePrice(anyLong())).thenReturn(50l);
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(department, 1000l, 1),50l);

        //with one observation, it still returns whatever the sales department says
        when(market.getYesterdayLastPrice()).thenReturn(10l);
        when(market.getYesterdayVolume()).thenReturn(1);
        model.getPhaseScheduler().step(model);
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(department, 1000l, 1),50l);

        //with no volume the observation is ignored
        when(market.getYesterdayLastPrice()).thenReturn(10l);
        when(market.getYesterdayVolume()).thenReturn(0);
        model.getPhaseScheduler().step(model);
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(department, 1000l, 1),50l);


        //two observations, everything back to normal! (but the slope is 0, so no effect)
        when(market.getYesterdayLastPrice()).thenReturn(10l);
        when(market.getYesterdayVolume()).thenReturn(1);
        model.getPhaseScheduler().step(model);
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(department, 50l,1 ),50l);


    }

}
