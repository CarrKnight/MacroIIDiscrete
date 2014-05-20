/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats;

import financial.market.Market;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.stats.collectors.MarketData;
import model.utilities.stats.collectors.enums.MarketDataType;
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
 * @version 2013-08-16
 * @see
 */
public class MarketDataTest
{

    @Test
    public void rescheduleItself()
    {
        Market market = mock(Market.class);
        MacroII model = mock(MacroII.class);
        MarketData data = new MarketData();

        data.start(model,market);
        verify(model).scheduleSoon(ActionOrder.CLEANUP_DATA_GATHERING, data);
        when(model.getCurrentPhase()).thenReturn(ActionOrder.CLEANUP_DATA_GATHERING);
        data.step(model);
        data.step(model);
        verify(model, times(2)).scheduleTomorrow(ActionOrder.CLEANUP_DATA_GATHERING, data);
        //turn off doesn't reschedule
        data.turnOff();
        data.step(model);
        verify(model, times(2)).scheduleTomorrow(ActionOrder.CLEANUP_DATA_GATHERING, data); //only the two old times are counted!
    }
    
    
    @Test 
    public void acceptableDays()
    {
        Market market = mock(Market.class);
        MacroII model = mock(MacroII.class);
        when(model.getCurrentPhase()).thenReturn(ActionOrder.CLEANUP_DATA_GATHERING);
        when(model.getMainScheduleTime()).thenReturn(-1d);
        MarketData data = new MarketData();

        data.start(model,market);
        //put in price data
        when(market.getLastPrice()).thenReturn(1,2,3);
        data.step(model);
        data.step(model);
        data.step(model);

        MarketData.MarketDataAcceptor acceptor = new MarketData.MarketDataAcceptor() {
            @Override
            public boolean acceptDay(Double lastPrice, Double volumeTraded, Double volumeProduced, Double volumeConsumed, Double demandGap, Double supplyGap) {
                return lastPrice >=2;
                
            }
        };
        
        Assert.assertArrayEquals(new int[]{1,2},data.getAcceptableDays(new int[]{0,1,2},acceptor));
        
        
    }


    @Test
    public void lastPriceTest()
    {

        Market market = mock(Market.class);
        MacroII model = mock(MacroII.class);
        when(model.getCurrentPhase()).thenReturn(ActionOrder.CLEANUP_DATA_GATHERING);
        when(model.getMainScheduleTime()).thenReturn(-1d);
        MarketData data = new MarketData();

        data.start(model,market);
        //put in price data
        when(market.getLastPrice()).thenReturn(1,2,3);
        data.step(model);
        data.step(model);
        data.step(model);

        //make sure it works!
        Assert.assertEquals(data.numberOfObservations(), 3);
        Assert.assertEquals(data.getObservationRecordedThisDay(MarketDataType.CLOSING_PRICE,0),1d,.00001d);
        Assert.assertEquals(data.getLatestObservation(MarketDataType.CLOSING_PRICE),3d,.00001d);
        Assert.assertEquals(data.getObservationRecordedThisDay(MarketDataType.CLOSING_PRICE,2),3d,.00001d);
        Assert.assertArrayEquals(data.getAllRecordedObservations(MarketDataType.CLOSING_PRICE),new double[]{1d,2d,3d},.0001d);
        Assert.assertArrayEquals(data.getObservationsRecordedTheseDays(MarketDataType.CLOSING_PRICE,new int[]{0, 2}),
                new double[]{1d,3d},.0001d);
        Assert.assertArrayEquals(data.getObservationsRecordedTheseDays(MarketDataType.CLOSING_PRICE,0, 1),
                new double[]{1d,2d},.0001d);

    }



    @Test
    public void lastVolumeTest()
    {

        Market market = mock(Market.class);
        MacroII model = mock(MacroII.class);
        when(model.getCurrentPhase()).thenReturn(ActionOrder.CLEANUP_DATA_GATHERING);
        when(model.getMainScheduleTime()).thenReturn(-1d);
        MarketData data = new MarketData();

        data.start(model, market);
        //put in price data
        when(market.getTodayVolume()).thenReturn(1,2,3);
        data.step(model);
        data.step(model);
        data.step(model);

        //make sure it works!
        //make sure it works!
        Assert.assertEquals(data.numberOfObservations(), 3);
        Assert.assertEquals(data.getObservationRecordedThisDay(MarketDataType.VOLUME_TRADED,0),1d,.00001d);
        Assert.assertEquals(data.getLatestObservation(MarketDataType.VOLUME_TRADED),3d,.00001d);
        Assert.assertEquals(data.getObservationRecordedThisDay(MarketDataType.VOLUME_TRADED,2),3d,.00001d);
        Assert.assertArrayEquals(data.getAllRecordedObservations(MarketDataType.VOLUME_TRADED),new double[]{1d,2d,3d},.0001d);
        Assert.assertArrayEquals(data.getObservationsRecordedTheseDays(MarketDataType.VOLUME_TRADED,new int[]{0, 2}),
                new double[]{1d,3d},.0001d);
        Assert.assertArrayEquals(data.getObservationsRecordedTheseDays(MarketDataType.VOLUME_TRADED,0, 1),
                new double[]{1d,2d},.0001d);

    }

    @Test
    public void consumptionTest()
    {

        Market market = mock(Market.class);
        MacroII model = mock(MacroII.class);
        when(model.getCurrentPhase()).thenReturn(ActionOrder.CLEANUP_DATA_GATHERING);
        when(model.getMainScheduleTime()).thenReturn(-1d);
        MarketData data = new MarketData();

        data.start(model, market);
        //put in price data
        when(market.countTodayConsumptionByRegisteredBuyers()).thenReturn(1,2,3);
        data.step(model);
        data.step(model);
        data.step(model);

        //make sure it works!
        //make sure it works!
        Assert.assertEquals(data.numberOfObservations(), 3);
        Assert.assertEquals(data.getObservationRecordedThisDay(MarketDataType.VOLUME_CONSUMED,0),1d,.00001d);
        Assert.assertEquals(data.getLatestObservation(MarketDataType.VOLUME_CONSUMED),3d,.00001d);
        Assert.assertEquals(data.getObservationRecordedThisDay(MarketDataType.VOLUME_CONSUMED,2),3d,.00001d);
        Assert.assertArrayEquals(data.getAllRecordedObservations(MarketDataType.VOLUME_CONSUMED),new double[]{1d,2d,3d},.0001d);
        Assert.assertArrayEquals(data.getObservationsRecordedTheseDays(MarketDataType.VOLUME_CONSUMED,new int[]{0, 2}),
                new double[]{1d,3d},.0001d);
        Assert.assertArrayEquals(data.getObservationsRecordedTheseDays(MarketDataType.VOLUME_CONSUMED,0, 1),
                new double[]{1d,2d},.0001d);

    }


    @Test
    public void productionTest()
    {

        Market market = mock(Market.class);
        MacroII model = mock(MacroII.class);
        when(model.getCurrentPhase()).thenReturn(ActionOrder.CLEANUP_DATA_GATHERING);
        when(model.getMainScheduleTime()).thenReturn(-1d);
        MarketData data = new MarketData();

        data.start(model, market);
        //put in price data
        when(market.countTodayProductionByRegisteredSellers()).thenReturn(1,2,3);
        data.step(model);
        data.step(model);
        data.step(model);

        //make sure it works!
        //make sure it works!
        Assert.assertEquals(data.numberOfObservations(), 3);
        Assert.assertEquals(data.getObservationRecordedThisDay(MarketDataType.VOLUME_PRODUCED,0),1d,.00001d);
        Assert.assertEquals(data.getLatestObservation(MarketDataType.VOLUME_PRODUCED),3d,.00001d);
        Assert.assertEquals(data.getObservationRecordedThisDay(MarketDataType.VOLUME_PRODUCED,2),3d,.00001d);
        Assert.assertArrayEquals(data.getAllRecordedObservations(MarketDataType.VOLUME_PRODUCED),new double[]{1d,2d,3d},.0001d);
        Assert.assertArrayEquals(data.getObservationsRecordedTheseDays(MarketDataType.VOLUME_PRODUCED,new int[]{0, 2}),
                new double[]{1d,3d},.0001d);
        Assert.assertArrayEquals(data.getObservationsRecordedTheseDays(MarketDataType.VOLUME_PRODUCED,0, 1),
                new double[]{1d,2d},.0001d);

    }



    @Test
    public void demandGapTest()
    {

        Market market = mock(Market.class);
        MacroII model = mock(MacroII.class);
        when(model.getCurrentPhase()).thenReturn(ActionOrder.CLEANUP_DATA_GATHERING);
        when(model.getMainScheduleTime()).thenReturn(-1d);
        MarketData data = new MarketData();

        data.start(model, market);
        //put in price data
        when(market.sumDemandGaps()).thenReturn(1,2,3);
        data.step(model);
        data.step(model);
        data.step(model);

        //make sure it works!
        //make sure it works!
        Assert.assertEquals(data.numberOfObservations(), 3);
        Assert.assertEquals(data.getObservationRecordedThisDay(MarketDataType.DEMAND_GAP,0),1d,.00001d);
        Assert.assertEquals(data.getLatestObservation(MarketDataType.DEMAND_GAP),3d,.00001d);
        Assert.assertEquals(data.getObservationRecordedThisDay(MarketDataType.DEMAND_GAP,2),3d,.00001d);
        Assert.assertArrayEquals(data.getAllRecordedObservations(MarketDataType.DEMAND_GAP),new double[]{1d,2d,3d},.0001d);
        Assert.assertArrayEquals(data.getObservationsRecordedTheseDays(MarketDataType.DEMAND_GAP,new int[]{0, 2}),
                new double[]{1d,3d},.0001d);
        Assert.assertArrayEquals(data.getObservationsRecordedTheseDays(MarketDataType.DEMAND_GAP,0, 1),
                new double[]{1d,2d},.0001d);

    }



    @Test
    public void supplyGapTest()
    {

        Market market = mock(Market.class);
        MacroII model = mock(MacroII.class);
        when(model.getCurrentPhase()).thenReturn(ActionOrder.CLEANUP_DATA_GATHERING);
        when(model.getMainScheduleTime()).thenReturn(-1d);
        MarketData data = new MarketData();

        data.start(model, market);
        //put in price data
        when(market.sumSupplyGaps()).thenReturn(1,2,3);
        data.step(model);
        data.step(model);
        data.step(model);

        //make sure it works!
        //make sure it works!
        Assert.assertEquals(data.numberOfObservations(), 3);
        Assert.assertEquals(data.getObservationRecordedThisDay(MarketDataType.SUPPLY_GAP,0),1d,.00001d);
        Assert.assertEquals(data.getLatestObservation(MarketDataType.SUPPLY_GAP),3d,.00001d);
        Assert.assertEquals(data.getObservationRecordedThisDay(MarketDataType.SUPPLY_GAP,2),3d,.00001d);
        Assert.assertArrayEquals(data.getAllRecordedObservations(MarketDataType.SUPPLY_GAP),new double[]{1d,2d,3d},.0001d);
        Assert.assertArrayEquals(data.getObservationsRecordedTheseDays(MarketDataType.SUPPLY_GAP,new int[]{0, 2}),
                new double[]{1d,3d},.0001d);
        Assert.assertArrayEquals(data.getObservationsRecordedTheseDays(MarketDataType.SUPPLY_GAP,0, 1),
                new double[]{1d,2d},.0001d);

    }



}
