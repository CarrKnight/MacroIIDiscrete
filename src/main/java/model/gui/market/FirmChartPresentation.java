/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui.market;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import goods.GoodType;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
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
    private SalesDataType currentDataType;

    private ChoiceBox<String> choiceBox;

    private final static BiMap<String,SalesDataType> dataTypesAllowed = HashBiMap.create(5);
    static {
        dataTypesAllowed.put("Price",SalesDataType.LAST_ASKED_PRICE);
        dataTypesAllowed.put("Inventory",SalesDataType.HOW_MANY_TO_SELL);
        dataTypesAllowed.put("Output Produced",SalesDataType.INFLOW);
        dataTypesAllowed.put("Output Sold",SalesDataType.OUTFLOW);
        dataTypesAllowed.put("Workers Hired",SalesDataType.WORKERS_PRODUCING_THIS_GOOD);
        dataTypesAllowed.put("Estimated Demand Slope",SalesDataType.PREDICTED_DEMAND_SLOPE);
    }


    public FirmChartPresentation(SellingFirmToColorMap sellerMap, GoodType goodBeingTraded, SalesDataType initialDataType) {
        this.sellerMap = sellerMap;
        this.goodBeingTraded = goodBeingTraded;
        this.currentDataType = initialDataType;

        //create a box with the "chart me" sign
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: #336699;");

        Button chartButton = new Button("Update Chart");
        chartButton.setPrefSize(200, 20);

        chartButton.setDefaultButton(true);
        chartButton.addEventHandler(ActionEvent.ACTION,this);
        hbox.getChildren().add(chartButton);

        //create the choice box to decide what to chart

        choiceBox= new ChoiceBox<>(FXCollections.observableArrayList(dataTypesAllowed.keySet()));
        choiceBox.setPrefSize(200, 20);
        choiceBox.setValue(dataTypesAllowed.inverse().get(initialDataType));
        choiceBox.getSelectionModel().selectedItemProperty().addListener((observableValue, s, s2) -> {
            currentDataType = dataTypesAllowed.get(s2);
            Preconditions.checkNotNull(currentDataType);
        });

        Platform.runLater(() -> Tooltip.install(choiceBox, new Tooltip("Data to plot")));

        hbox.getChildren().add(choiceBox);



        //put the box in place
        setTop(hbox);


    }

    @Override
    public void handle(ActionEvent actionEvent) {
        Preconditions.checkState(Platform.isFxApplicationThread());
        CreateChartOnDemand task = new CreateChartOnDemand(sellerMap,goodBeingTraded, currentDataType);
        //the progress bar
        ProgressBar bar = new ProgressBar();
        bar.progressProperty().bind(task.progressProperty());
        Label barLabel = new Label("Loading",bar);
        barLabel.textProperty().bind(task.messageProperty());
        barLabel.setContentDisplay(ContentDisplay.BOTTOM);
        this.setCenter(new StackPane(bar,barLabel));
        //start it in his own thread!
        new Thread(task).start();
        //when done paste it
        task.onSucceededProperty().setValue(workerStateEvent ->{
            System.out.println("mission accomplished");
            this.setCenter(task.getChart());
        });


    }
}
