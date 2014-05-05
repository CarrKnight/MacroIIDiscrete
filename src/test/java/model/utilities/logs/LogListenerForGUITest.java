/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.logs;

import javafx.embed.swing.JFXPanel;
import model.MacroII;
import model.utilities.ActionOrder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LogListenerForGUITest {



    //give it 10 log events
    //log event is still empty
    //verify it is stepped only once
    //verify that only after step log event gets filled.


    @Test
    public void dailyEvents() throws Exception {
        MacroII model = mock(MacroII.class);
        when(model.getMainScheduleTime()).thenReturn(1d);
        when(model.getCurrentPhase()).thenReturn(ActionOrder.DAWN);

        LogListenerForGUI listenerForGUI = new LogListenerForGUI(model,LogLevel.INFO);

        Assert.assertEquals(listenerForGUI.getEventList().size(),0);
        //add 5 WARN events
        for(int i=0; i<5;i ++)
            listenerForGUI.handleNewEvent(new LogEvent(this,LogLevel.WARN,"lame{},{}",i,i));
        //add 5 TRACE events (they should be ignored)
        for(int i=0; i<5;i ++)
            listenerForGUI.handleNewEvent(new LogEvent(this,LogLevel.TRACE,"lame{},{}",i,i));

        //they shouldn't be inside the listener just yet
        Assert.assertEquals(listenerForGUI.getEventList().size(),5);
        Assert.assertEquals(listenerForGUI.getEventList().get(0).getMessage(),"lame0,0");


    }



}