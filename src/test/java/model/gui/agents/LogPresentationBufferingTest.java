/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui.agents;

import javafx.embed.swing.JFXPanel;
import model.MacroII;
import model.gui.market.MarketPresentationTest;
import model.utilities.ActionOrder;
import model.utilities.logs.LogEvent;
import model.utilities.logs.LogLevel;
import model.utilities.logs.LogNodeSimple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class LogPresentationBufferingTest {

    @Before
    public void setUp() throws Exception {
        //should start the Platform
        JFXPanel panel = new JFXPanel();

    }

    @Test
    public void dailyEvents() throws Exception {
        MacroII model = mock(MacroII.class);
        when(model.getMainScheduleTime()).thenReturn(1d);
        when(model.getCurrentPhase()).thenReturn(ActionOrder.DAWN);
        LogNodeSimple simple = new LogNodeSimple();

        LogPresentationBuffering listenerForGUI = new LogPresentationBuffering(model,simple );

        Assert.assertEquals(listenerForGUI.getLogView().getItems().size(), 0);
        listenerForGUI.setMinimumLevelToListenTo(LogLevel.INFO); //filter
        //add 5 WARN events
        for(int i=0; i<5;i ++)
            simple.handleNewEvent(new LogEvent(this,LogLevel.WARN,"lame{},{}",i,i));
        //add 5 TRACE events (they should be ignored)
        for(int i=0; i<5;i ++)
            simple.handleNewEvent(new LogEvent(this,LogLevel.TRACE,"lame{},{}",i,i));

        //they shouldn't be inside the listener just yet
        Assert.assertEquals(listenerForGUI.getLogView().getItems().size(),0);
        //but it should be ready for stepping!
        verify(model,times(1)).scheduleSoon(ActionOrder.GUI_PHASE,
                listenerForGUI);

        //now step it!
        listenerForGUI.step(model);
        MarketPresentationTest.waitForRunLater();

        //should be filled
        Assert.assertEquals(listenerForGUI.getLogView().getItems().size(),5);


    }



}