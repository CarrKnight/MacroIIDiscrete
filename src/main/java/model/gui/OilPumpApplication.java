/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.concurrent.ExecutionException;

/**
 * A standalone test of javafx view of a geographical mdel
 * Created by carrknight on 4/8/14.
 */
public class OilPumpApplication extends Application
{


    @Override
    public void start(Stage stage) throws Exception {
        //this stage always ends the game
        stage.setOnCloseRequest(windowEvent -> Platform.exit());



        //create a splash screen
        ProgressBar loadingBar = new ProgressBar();
        StackPane loadingPane = new StackPane(loadingBar);
        Scene loadingScene  = new Scene(loadingPane,300,200);
        stage.setScene(loadingScene);
        Label loadingLabel = new Label("Starting...",loadingBar);
        loadingPane.getChildren().add(loadingLabel);
        stage.show();

        //initialize
        final OilPumpStartup startup = new OilPumpStartup();
        loadingBar.progressProperty().bind(startup.progressProperty());
        loadingLabel.textProperty().bind(startup.messageProperty());
        startup.onSucceededProperty().setValue(workerStateEvent -> {
            try {
                stage.setScene(new Scene(startup.get(),896,504));
                stage.setMaximized(true);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

        });

        new Thread(startup).start();





        ///////////////////////////////////////////

    }

    public static void main(String[] args)
    {
        Application.launch(OilPumpApplication.class,args);
    }
}
