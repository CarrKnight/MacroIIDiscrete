/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui;

import agents.EconomicAgent;
import financial.market.GeographicalMarket;
import javafx.concurrent.Task;
import javafx.scene.Parent;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import model.MacroII;
import model.gui.market.GeographicalMarketView;
import model.scenario.OilDistributorScenario;
import model.utilities.dummies.GeographicalCustomer;

/**
 * Starts the scenario, starts the model and creates the main GUI scene. Then it returns it and we are doooone.
 * Created by carrknight on 4/14/14.
 */
public class OilPumpStartup extends Task<Parent>
{


    @Override
    protected Parent call() throws Exception {

        //create model and agents
        updateProgress(0,3);
        updateMessage("Creating Agents");
        MacroII model = new MacroII(System.currentTimeMillis());
        OilDistributorScenario scenario = new OilDistributorScenario(model);
        model.setScenario(scenario);
        updateProgress(1,3);

        //model.start
        updateMessage("Scheduling Agents");
        model.start();
        updateProgress(2,3);

        //gui building
        updateMessage("Building GUI");
        assert model.getGoodTypeMasterList().getGoodTypeCorrespondingToThisCode(OilDistributorScenario.oilGoodType.getCode()) != null; //make sure oil is properly registered.
        final GeographicalMarket market = (financial.market.GeographicalMarket)model.getMarket(OilDistributorScenario.oilGoodType);
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
        updateProgress(3, 3);

        //return the gui and be done with it!
        updateMessage("Done!");
        return mainPane;


    }

}
