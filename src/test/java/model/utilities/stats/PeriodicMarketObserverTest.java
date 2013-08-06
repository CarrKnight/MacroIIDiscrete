/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.stats;

import financial.Market;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.scheduler.Priority;
import org.junit.Assert;
import org.junit.Test;
import sim.engine.Steppable;

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
public class PeriodicMarketObserverTest {


    @Test
    public void basicObservationTest()
    {


        PeriodicMarketObserver.defaultDailyProbabilityOfObserving = 1f;



        Market market = mock(Market.class);
        MacroII model = new MacroII(System.currentTimeMillis());
        PeriodicMarketObserver observer = new PeriodicMarketObserver(market,model );

        //observation 1
        when(market.getYesterdayLastPrice()).thenReturn(86l);
        when(market.getYesterdayVolume()).thenReturn(6);
        when(market.countYesterdayConsumptionByRegisteredBuyers()).thenReturn(1);
        when(market.countYesterdayProductionByRegisteredSellers()).thenReturn(10);
        model.getPhaseScheduler().step(model);
        //observation 2
        when(market.getYesterdayLastPrice()).thenReturn(84l);
        when(market.getYesterdayVolume()).thenReturn(7);
        when(market.countYesterdayConsumptionByRegisteredBuyers()).thenReturn(2);
        when(market.countYesterdayProductionByRegisteredSellers()).thenReturn(20);
        model.getPhaseScheduler().step(model);
        //observation 3
        when(market.getYesterdayLastPrice()).thenReturn(81l);
        when(market.getYesterdayVolume()).thenReturn(8);
        when(market.countYesterdayConsumptionByRegisteredBuyers()).thenReturn(3);
        when(market.countYesterdayProductionByRegisteredSellers()).thenReturn(30);
        model.getPhaseScheduler().step(model);


        Assert.assertEquals(3,observer.getNumberOfObservations());
        Assert.assertEquals(86,observer.getPricesObservedAsArray()[0],.0001);
        Assert.assertEquals(84,observer.getPricesObservedAsArray()[1],.0001);
        Assert.assertEquals(81,observer.getPricesObservedAsArray()[2],.0001);
        Assert.assertEquals(6,observer.getQuantitiesTradedObservedAsArray()[0],.0001);
        Assert.assertEquals(7,observer.getQuantitiesTradedObservedAsArray()[1],.0001);
        Assert.assertEquals(8,observer.getQuantitiesTradedObservedAsArray()[2],.0001);

        Assert.assertEquals(1,observer.getQuantitiesConsumedObservedAsArray()[0],.0001);
        Assert.assertEquals(2,observer.getQuantitiesConsumedObservedAsArray()[1],.0001);
        Assert.assertEquals(3,observer.getQuantitiesConsumedObservedAsArray()[2],.0001);
        Assert.assertEquals(10,observer.getQuantitiesProducedObservedAsArray()[0],.0001);
        Assert.assertEquals(20,observer.getQuantitiesProducedObservedAsArray()[1],.0001);
        Assert.assertEquals(30,observer.getQuantitiesProducedObservedAsArray()[2],.0001);



    }

    @Test
    public void basicScheduling()
    {
        Market market = mock(Market.class);
        MacroII macroII = mock(MacroII.class);
        PeriodicMarketObserver.defaultDailyProbabilityOfObserving = .2f;

        new PeriodicMarketObserver(market,macroII );

        verify(macroII).scheduleAnotherDayWithFixedProbability(any(ActionOrder.class),any(Steppable.class),
                anyFloat(),any(Priority.class));
    }


    @Test
    public void knowsWhenNotToObserve()
    {
        //no observations, should return whatever the sales department says
        Market market = mock(Market.class);
        MacroII model = mock(MacroII.class);

        PeriodicMarketObserver observer = new PeriodicMarketObserver(market,model );
        Assert.assertEquals(observer.getNumberOfObservations(),0);




        //with one observation, it still returns whatever the sales department says
        when(market.getYesterdayLastPrice()).thenReturn(10l);
        when(market.getYesterdayVolume()).thenReturn(1);
        observer.step(mock(MacroII.class));
        Assert.assertEquals(observer.getNumberOfObservations(),1);
        Assert.assertEquals((double)observer.getLastPriceObserved(),10,.01);
        Assert.assertEquals((double)observer.getLastQuantityTradedObserved(),1,.01);

        //with no volume the observation is ignored
        when(market.getYesterdayLastPrice()).thenReturn(10l);
        when(market.getYesterdayVolume()).thenReturn(0);
        observer.step(mock(MacroII.class));
        Assert.assertEquals(observer.getNumberOfObservations(),1);
        Assert.assertEquals((double)observer.getLastPriceObserved(),10,.01);
        Assert.assertEquals((double)observer.getLastQuantityTradedObserved(),1,.01);

        //two observations, everything back to normal!
        when(market.getYesterdayLastPrice()).thenReturn(10l);
        when(market.getYesterdayVolume()).thenReturn(1);
        observer.step(mock(MacroII.class));
        Assert.assertEquals(observer.getNumberOfObservations(),2);
        Assert.assertEquals((double)observer.getLastPriceObserved(),10,.01);
        Assert.assertEquals((double)observer.getLastQuantityTradedObserved(),1,.01);
    }

}
