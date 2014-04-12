/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui;

import agents.EconomicAgent;
import financial.market.GeographicalMarket;
import goods.GoodType;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import model.MacroII;
import model.gui.market.GeographicalMarketView;
import model.scenario.OilDistributorScenario;
import model.scenario.oil.GeographicalCustomer;

/**
 * A standalone test of javafx view of a geographical mdel
 * Created by carrknight on 4/8/14.
 */
public class OilPumpApplication extends Application
{


    @Override
    public void start(Stage stage) throws Exception {

        //create the model and start it
        MacroII model = new MacroII(System.currentTimeMillis());
        OilDistributorScenario scenario = new OilDistributorScenario(model);
        model.setScenario(scenario);
        model.start();



        //////////////////////////////////
        final GeographicalMarket market = (financial.market.GeographicalMarket)model.getMarket(GoodType.OIL);
        GeographicalMarketView view = new GeographicalMarketView(market ,model);
        BorderPane mainPane = new BorderPane();
        mainPane.setCenter(view);
        ModelControlBar modelControlBar = new ModelControlBar(model);
        Slider distancePenalty = new Slider(0,5,.1);
        distancePenalty.setShowTickLabels(true);
        distancePenalty.setSnapToTicks(true);
        distancePenalty.setMajorTickUnit(1);
        distancePenalty.valueProperty().addListener((observableValue, number, number2) -> {
            for(EconomicAgent buyer : market.getBuyers())
            {
              //  System.out.println(observableValue.getValue().doubleValue());
                if( buyer instanceof GeographicalCustomer)
                    ((GeographicalCustomer) buyer).setDistanceExponent(observableValue.getValue().doubleValue());
            }
        });
        distancePenalty.setValue(5);
        Label label = new Label("Distance Penalty",distancePenalty);
        label.setVisible(true);
        label.setContentDisplay(ContentDisplay.TOP);

        modelControlBar.getContentBox().getChildren().add(distancePenalty);
        modelControlBar.getContentBox().getChildren().add(label);

        mainPane.setBottom(modelControlBar);
        stage.setScene(new Scene(mainPane));
        stage.setMaximized(true);
        stage.show();

        ///////////////////////////////////////////

    }

    public static void main(String[] args)
    {
        Application.launch(OilPumpApplication.class,args);
    }
}
