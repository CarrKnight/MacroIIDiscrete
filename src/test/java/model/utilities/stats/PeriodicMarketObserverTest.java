/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.stats;

import financial.market.Market;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.scheduler.Priority;
import model.utilities.stats.collectors.enums.MarketDataType;
import model.utilities.stats.collectors.PeriodicMarketObserver;
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

        //observations should all go!
        when(market.getLatestObservation(MarketDataType.CLOSING_PRICE)).thenReturn(1d);
        for(int i=0; i<3; i++)
        {
            when(market.getNumberOfObservations()).thenReturn(i+1);
            when(market.getLastObservedDay()).thenReturn(i);
            model.getPhaseScheduler().step(model);
        }

        when(market.getObservationsRecordedTheseDays(MarketDataType.CLOSING_PRICE, new int[]{0, 1, 2})).thenReturn(
                new double[]{
                        86,84,81
                });
        when(market.getObservationsRecordedTheseDays(MarketDataType.VOLUME_TRADED, new int[]{0,1, 2})).thenReturn(
                new double[]{
                        6,7,8
                });
        when(market.getObservationsRecordedTheseDays(MarketDataType.VOLUME_CONSUMED, new int[]{0,1, 2})).thenReturn(
                new double[]{
                        1,2,3
                });
        when(market.getObservationsRecordedTheseDays(MarketDataType.VOLUME_PRODUCED, new int[]{0,1, 2})).thenReturn(
                new double[]{
                        10,20,30
                });


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

        verify(macroII).scheduleAnotherDay(any(ActionOrder.class),any(Steppable.class),
                anyInt(),any(Priority.class));
    }


    @Test
    public void knowsWhenNotToObserve()
    {
        //no observations, should return whatever the sales department says
        Market market = mock(Market.class);
        MacroII model = mock(MacroII.class);

        PeriodicMarketObserver observer = new PeriodicMarketObserver(market,model );
        Assert.assertEquals(observer.getNumberOfObservations(),0);

        when(market.getNumberOfObservations()).thenReturn(1,2,3);



        //with one observation, it still returns whatever the sales department says
        when(market.getLatestObservation(MarketDataType.CLOSING_PRICE)).thenReturn(10d);
        when(market.getLastObservedDay()).thenReturn(0);
        when(market.getLastObservedDay()).thenReturn(0);
        observer.step(mock(MacroII.class));

        //with no price the observation is ignored
        when(market.getLastObservedDay()).thenReturn(1);
        when(market.getLatestObservation(MarketDataType.CLOSING_PRICE)).thenReturn(-1d);
        observer.step(mock(MacroII.class));

        //two observations, everything back to normal!
        when(market.getLastObservedDay()).thenReturn(2);
        when(market.getLatestObservation(MarketDataType.CLOSING_PRICE)).thenReturn(10d);
        observer.step(mock(MacroII.class));

        Assert.assertArrayEquals(observer.getDaysObserved(),new int[]{0,2});





    }

}
