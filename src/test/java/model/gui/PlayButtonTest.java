/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.gui;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import model.MacroII;
import model.gui.market.MarketPresentationTest;
import model.utilities.ActionOrder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 4/11/14.
 */
public class PlayButtonTest
{
    @Before
    public void setUp() throws Exception {
        //should start the Platform
        JFXPanel panel = new JFXPanel();

    }

    //we are going to increase a number each day in the model, then after 10 elements we are going to press the button to pause and hope to god it pauses
    @Test
    public void testPlayButton() throws Exception {


        final AtomicInteger counter = new AtomicInteger(0);

        MacroII model = new MacroII(0);
        model.start();
        final PlayButton button = new PlayButton(model);

        //check initialization, kind of pointless
        Assert.assertEquals(button.getText(), "Play");

        model.scheduleSoon(ActionOrder.DAWN, new Steppable() {
            @Override
            public void step(SimState simState) {
                counter.incrementAndGet();
                Assert.assertEquals("Pause",button.getText()); //should be waiting for pause
                try {
                    if (counter.get() % 10 == 0) //at ten press the button!
                    {
                        Platform.runLater(() -> button.handle(mock(ActionEvent.class)));
                        System.out.println("done!");

                        MarketPresentationTest.waitForRunLater();

                    }

                    Thread.sleep(5); //slow down to make sure the fx thread has time
                }
                catch (Exception e){}
                model.scheduleTomorrow(ActionOrder.DAWN, this);
            }
        });

        //press it once to start it!
        Platform.runLater(() -> button.handle(mock(ActionEvent.class)));
        //sleep for a second
        //counter should be 10
        Thread.sleep(1500);
        Assert.assertEquals(String.valueOf(counter.get()), button.getText(), "Play");
        Assert.assertEquals(10,counter.get());


        //then press it again!
        Platform.runLater(() -> button.handle(mock(ActionEvent.class)));
        //sleep for a second
        //counter should be 20
        Thread.sleep(1500);
        Assert.assertEquals(String.valueOf(counter.get()), button.getText(), "Play");
        Assert.assertEquals(20,counter.get());


    }
}
