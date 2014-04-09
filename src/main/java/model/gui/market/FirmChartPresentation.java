/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui.market;

import com.google.common.base.Preconditions;
import goods.GoodType;
import javafx.application.Platform;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import model.utilities.stats.collectors.enums.SalesDataType;


/**
 * A container of a chart (usually price) with one series for each firm (no aggregate).
 *
 * It is an event-handler because it has only one button, so it's handy and that makes it easy to test too.
 * Created by carrknight on 4/9/14.
 */
public class FirmChartPresentation extends BorderPane implements EventHandler<ActionEvent>{

    private final SellingFirmToColorMap sellerMap;

    private final GoodType goodBeingTraded;

    /**
     * the data type to chart
     */
    private final SalesDataType dataType;


    public FirmChartPresentation(SellingFirmToColorMap sellerMap, GoodType goodBeingTraded, SalesDataType dataType) {
        this.sellerMap = sellerMap;
        this.goodBeingTraded = goodBeingTraded;
        this.dataType = dataType;

        //create a box with the "chart me" sign
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: #336699;");

        Button chartButton = new Button("Update Chart");
        chartButton.setPrefSize(100, 20);

        chartButton.setDefaultButton(true);
        chartButton.addEventHandler(ActionEvent.ACTION,this);
        hbox.getChildren().add(chartButton);
        setTop(hbox);
    }

    @Override
    public void handle(ActionEvent actionEvent) {
        Preconditions.checkState(Platform.isFxApplicationThread());
        CreateChartOnDemand task = new CreateChartOnDemand(sellerMap,goodBeingTraded,dataType);
        //the progress bar
        ProgressBar bar = new ProgressBar();
        bar.progressProperty().bind(task.progressProperty());
        this.setCenter(new StackPane(bar));
        //start it in his own thread!
        new Thread(task).start();
        //when done paste it
        task.onSucceededProperty().setValue(workerStateEvent ->{
            System.out.println("mission accomplished");
            this.setCenter(task.getChart());
        });


    }
}
