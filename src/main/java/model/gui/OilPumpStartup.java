/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui;

import agents.EconomicAgent;
import financial.market.GeographicalMarket;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import model.MacroII;
import model.gui.market.ControllableGeographicalMarketView;
import model.gui.market.GeographicalMarketView;
import model.scenario.oil.OilDistributorScenario;
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
        GeographicalMarketView view = new ControllableGeographicalMarketView(market ,model,scenario);
        BorderPane mainPane = new BorderPane();
        TabPane center = new TabPane();
        center.prefHeightProperty().bind(mainPane.prefHeightProperty().multiply(.8f));
        center.getTabs().addAll(view.getInitialTabs());
        mainPane.setCenter(center);


        Accordion controls = new Accordion();
        final ModelControlBar modelControlBar = new ModelControlBar(model);
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
        controls.getPanes().add(modelControlBar);
        controls.setExpandedPane(modelControlBar);
        mainPane.setBottom(controls);

        //listen to tab changes
        center.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> observableValue, Tab tab, Tab tab2) {
                controls.getPanes().clear();
                controls.getPanes().add(modelControlBar);
                controls.getPanes().addAll(view.getControls(tab2));
            }
        });

        updateProgress(3, 3);

        //return the gui and be done with it!
        updateMessage("Done!");
        return mainPane;


    }

}
