/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui;

import javafx.geometry.Insets;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import model.MacroII;

/**
 * A simple accordion where I store my play/pause button and maybe one day also parameter control
 * Created by carrknight on 4/11/14.
 */
public class ModelControlBar extends TitledPane
{

    private final MacroII modelToRun;

    private final PlayButton play;
    private final HBox contentBox;


    /**t
     * the model control bar creates a play-button and the model thread within it.
     * @param modelToRun
     */
    public ModelControlBar(MacroII modelToRun)
    {
        this.modelToRun = modelToRun;
        this.setText("Model Control");
        //create an hbox within the titledPane
        contentBox = new HBox();
        contentBox.setPadding(new Insets(15, 12, 15, 12));
        contentBox.setSpacing(10);
        play = new PlayButton(modelToRun);
        contentBox.getChildren().add(play);

        this.setContent(contentBox);
        this.setVisible(true);
        this.setAnimated(true);

        this.setBackground(new Background(new BackgroundFill(Color.RED,null,null)));
        this.setTextFill(Color.WHITE);


    }


    public HBox getContentBox() {
        return contentBox;
    }
}
