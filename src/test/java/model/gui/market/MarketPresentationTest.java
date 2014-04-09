/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui.market;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import model.MacroII;
import model.utilities.stats.collectors.MarketData;
import model.utilities.stats.collectors.enums.MarketDataType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Semaphore;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 4/2/14.
 */
public class MarketPresentationTest {

    @Before
    public void setUp() throws Exception {
        //should start the Platform
        JFXPanel panel = new JFXPanel();

    }



    @Test
    public void stubbyTest() throws Exception {

        //create the presentation. Series should immediately exist
        MarketPresentation presentation = new MarketPresentation();
        Assert.assertNotNull(presentation.getClosingPriceSeries());
        Assert.assertNotNull(presentation.getVolumeConsumed());
        Assert.assertNotNull(presentation.getVolumeProduced());
        Assert.assertNotNull(presentation.getVolumeTraded());

        //they should also be empty
        Assert.assertEquals(presentation.getClosingPriceSeries().getData().size(),0);
        Assert.assertEquals(presentation.getVolumeTraded().getData().size(), 0);
        Assert.assertEquals(presentation.getVolumeConsumed().getData().size(), 0);
        Assert.assertEquals(presentation.getVolumeProduced().getData().size(), 0);

        //start it and step it
        MarketData dataStub = mock(MarketData.class);
        presentation.start(mock(MacroII.class),dataStub);
        when(dataStub.getLastObservedDay()).thenReturn(1);
        when(dataStub.getLatestObservation(MarketDataType.CLOSING_PRICE)).thenReturn(1d);
        when(dataStub.getLatestObservation(MarketDataType.VOLUME_TRADED)).thenReturn(2d);
        when(dataStub.getLatestObservation(MarketDataType.VOLUME_CONSUMED)).thenReturn(3d);
        when(dataStub.getLatestObservation(MarketDataType.VOLUME_PRODUCED)).thenReturn(4d);
        presentation.step(mock(MacroII.class));
        waitForRunLater();
        Assert.assertEquals(presentation.getClosingPriceSeries().getData().size(),1);
        Assert.assertEquals(presentation.getVolumeTraded().getData().size(), 1);
        Assert.assertEquals(presentation.getVolumeConsumed().getData().size(), 1);
        Assert.assertEquals(presentation.getVolumeProduced().getData().size(), 1);
        Assert.assertEquals(presentation.getClosingPriceSeries().getData().get(0).getXValue(),1);
        Assert.assertEquals(presentation.getVolumeTraded().getData().get(0).getXValue(),1);
        Assert.assertEquals(presentation.getVolumeConsumed().getData().get(0).getXValue(),1);
        Assert.assertEquals(presentation.getVolumeProduced().getData().get(0).getXValue(),1);
        Assert.assertEquals(presentation.getClosingPriceSeries().getData().get(0).getYValue(),1d);
        Assert.assertEquals(presentation.getVolumeTraded().getData().get(0).getYValue(),2d);
        Assert.assertEquals(presentation.getVolumeConsumed().getData().get(0).getYValue(),3d);
        Assert.assertEquals(presentation.getVolumeProduced().getData().get(0).getYValue(),4d);


        //step it again
        when(dataStub.getLastObservedDay()).thenReturn(2);
        when(dataStub.getLatestObservation(MarketDataType.CLOSING_PRICE)).thenReturn(5d);
        when(dataStub.getLatestObservation(MarketDataType.VOLUME_TRADED)).thenReturn(6d);
        when(dataStub.getLatestObservation(MarketDataType.VOLUME_CONSUMED)).thenReturn(7d);
        when(dataStub.getLatestObservation(MarketDataType.VOLUME_PRODUCED)).thenReturn(8d);
        presentation.step(mock(MacroII.class));
        waitForRunLater();
        Assert.assertEquals(presentation.getClosingPriceSeries().getData().size(),2);
        Assert.assertEquals(presentation.getVolumeTraded().getData().size(), 2);
        Assert.assertEquals(presentation.getVolumeConsumed().getData().size(), 2);
        Assert.assertEquals(presentation.getVolumeProduced().getData().size(), 2);
        Assert.assertEquals(presentation.getClosingPriceSeries().getData().get(1).getXValue(),2);
        Assert.assertEquals(presentation.getVolumeTraded().getData().get(1).getXValue(),2);
        Assert.assertEquals(presentation.getVolumeConsumed().getData().get(1).getXValue(),2);
        Assert.assertEquals(presentation.getVolumeProduced().getData().get(1).getXValue(),2);
        Assert.assertEquals(presentation.getClosingPriceSeries().getData().get(1).getYValue(),5d);
        Assert.assertEquals(presentation.getVolumeTraded().getData().get(1).getYValue(),6d);
        Assert.assertEquals(presentation.getVolumeConsumed().getData().get(1).getYValue(),7d);
        Assert.assertEquals(presentation.getVolumeProduced().getData().get(1).getYValue(),8d);



    }


    @Test
    public void testCorrectlyStepped() throws Exception
    {
        MarketPresentation presentation = new MarketPresentation();
        MarketData dataStub = mock(MarketData.class);
        MacroII model = new MacroII(System.currentTimeMillis());
        //presentation should be scheduling itself now
        presentation.start(model, dataStub);
        when(dataStub.getLatestObservation(MarketDataType.CLOSING_PRICE)).thenReturn(0d);
        when(dataStub.getLatestObservation(MarketDataType.VOLUME_TRADED)).thenReturn(0d);
        when(dataStub.getLatestObservation(MarketDataType.VOLUME_CONSUMED)).thenReturn(0d);
        when(dataStub.getLatestObservation(MarketDataType.VOLUME_PRODUCED)).thenReturn(0d);

        model.start();
        for(int i=0; i<10; i++)
        {
            when(dataStub.getLastObservedDay()).thenReturn(i);
            model.schedule.step(model);
        }
        waitForRunLater();
        Assert.assertEquals(presentation.getClosingPriceSeries().getData().size(),10);
        Assert.assertEquals(presentation.getVolumeTraded().getData().size(), 10);
        Assert.assertEquals(presentation.getVolumeConsumed().getData().size(), 10);
        Assert.assertEquals(presentation.getVolumeProduced().getData().size(), 10);

        //turn off correctly
        presentation.turnOff();
        //these steps do not increase the datasize
        for(int i=0; i<10; i++)
        {
            when(dataStub.getLastObservedDay()).thenReturn(i);
            model.schedule.step(model);
        }
        waitForRunLater();
        Assert.assertEquals(presentation.getClosingPriceSeries().getData().size(),10);
        Assert.assertEquals(presentation.getVolumeTraded().getData().size(), 10);
        Assert.assertEquals(presentation.getVolumeConsumed().getData().size(), 10);
        Assert.assertEquals(presentation.getVolumeProduced().getData().size(), 10);

    }

    public static void waitForRunLater() throws InterruptedException {
        Semaphore semaphore = new Semaphore(0);
        Platform.runLater(() -> semaphore.release());
        semaphore.acquire();

    }
}
