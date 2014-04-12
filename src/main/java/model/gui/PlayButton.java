/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui;

import com.google.common.base.Preconditions;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import model.MacroII;

import java.util.concurrent.Semaphore;

/**
 * This small presentation is very important in the sense that it holds the main model thread.
 * The model thread can be paused/restarted at the end of each step through the use of a simple binary semaphore.
 * It's a button and its own listener
 * Created by carrknight on 4/11/14.
 */
public class PlayButton extends Button implements EventHandler<ActionEvent>{

    private final Semaphore playPauseSemaphore;
    private final Thread modelThread;


    public PlayButton(MacroII modelToRun) {
        super("Play");
        Preconditions.checkState(modelToRun.hasStarted(), "need to have started before this is spawned!");


        //create the pause metaphore
        playPauseSemaphore = new Semaphore(0);

        //create the thread running the simulation
        modelThread = new Thread(() -> {
            try {
                //this trick I found by asking on :
                // http://stackoverflow.com/questions/23017888/boolean-semaphore-in-java/23018659?noredirect=1#23018659
                playPauseSemaphore.acquire();   // Block if paused
                playPauseSemaphore.release();
                while(true) {
                    //step the model
                    modelToRun.schedule.step(modelToRun);
                    playPauseSemaphore.acquire();   // Block if paused
                    playPauseSemaphore.release();
                }
            } catch (InterruptedException ignored) {System.err.println("interrupted");}
        });
        modelThread.setName("Simulation Thread");
        modelThread.setDaemon(true); //close with the simulation

        //you can start it immediately, it will immediately pause
        modelThread.start();

        this.setPrefSize(200, 40);
        this.addEventHandler(javafx.event.ActionEvent.ACTION, this);

    }

    @Override
    public void handle(ActionEvent actionEvent) {
        assert Platform.isFxApplicationThread(); //just making sure here
        //to avoid mistakes with "this" in the runnables, let me copy a reference to myself
        PlayButton play = this;

        //you pressed the button. If it was "play"
        if(play.getText().equals("Play"))
        {
            //change your text to "Pause"
            play.setText("Pause");
            //start the engine!
            playPauseSemaphore.release();

        }
        else
        {
            //if it wasn't play, it was pause!
            Preconditions.checkState(play.getText().equals("Pause"));
            play.setDisable(true); //grey it out until the pause takes effect!
            play.setText("Pausing...");
            //spawn a simple thread to stop the model and then reset the button!
            Thread pausingThread = new Thread(() -> {
                try {
                    playPauseSemaphore.acquire();
                    Preconditions.checkState(playPauseSemaphore.availablePermits()==0);
                    //fix the button
                    Platform.runLater(() -> { //to be done in the JAVAFX thread
                        play.setText("Play");
                        play.setDisable(false);
                    });
                }
                catch (Exception ignored){System.err.println("Failed to unpause!");}
            });
            pausingThread.setDaemon(true);
            pausingThread.start(); //go pause

        }


    }
}
