/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui;

import agents.EconomicAgent;
import com.google.common.base.Preconditions;
import financial.market.GeographicalMarket;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import model.MacroII;
import model.gui.agents.AgentLogView;
import model.gui.market.ControllableGeographicalMarketView;
import model.gui.market.GeographicalMarketView;
import model.gui.utilities.GUINode;
import model.gui.utilities.InformationTab;
import model.gui.utilities.SelectionEvent;
import model.gui.utilities.TabEvent;
import model.scenario.Scenario;
import model.scenario.oil.OilDistributorScenario;
import model.utilities.dummies.GeographicalCustomer;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a placeholder for a more general model view. For now I just need something that make the Oil distribution work
 * Created by carrknight on 5/7/14.
 */
public class ModelViewDemo extends BorderPane implements GUINode
{

    private final MacroII model;

    private final Scenario scenario;

    /**
     * a simple way to go back from a view open to its container
     */
    private final Map<Tab,InformationTab> viewToContainerMap;

    private TabPane center;

    private Accordion controls;

    private ModelControlBar modelControl;



    /**
     * the constructor is private. This should really only be initialized through a gui Task to grab from the static
     * method
     * @param model the model to use
     * @param scenario the scenario to use
     */
    private ModelViewDemo(MacroII model, Scenario scenario)
    {
        this.model = model;
        this.scenario = scenario;
        model.setScenario(scenario);
        viewToContainerMap = new HashMap<>();
    }

    private void startModel()
    {
        model.start();
    }

    private void buildGUI()
    {
        //create the guis
        center = new TabPane();
        center.prefHeightProperty().bind(this.prefHeightProperty().multiply(.8f));
        this.setCenter(center);
        controls = new Accordion();
        modelControl = new ModelControlBar(model);
        controls.getPanes().add(modelControl);
        this.setBottom(controls);

        //make sure that whenever a new tab is selected, the appropriate controllers are shown
        center.getSelectionModel().selectedItemProperty().addListener((observableValue, oldTab, newTab) -> {
            controls.getPanes().clear();
            controls.getPanes().add(modelControl); //always add model control
            //if a new tab has been selected add all the appropriate tabs as well
            if(newTab != null)
            {
                Preconditions.checkState(viewToContainerMap.containsKey(newTab));
                controls.getPanes().addAll(viewToContainerMap.get(newTab).getControls());
            }
        });

        //whenever a tab is closed, remove it from the map
        center.getTabs().addListener((ListChangeListener<Tab>) change -> {
            while(change.next())
            {
                for(Tab view : change.getRemoved()) {
                    final InformationTab removed = viewToContainerMap.remove(view);
                    //stop listening
                    removed.setGUINodeParent(null);
                }
            }
        });


    }

    //one day this is going to be properly initializing a list of markets, but for now it just assumes the scenario is correct somehow
    private void buildInitialTabs()
    {
        assert scenario instanceof OilDistributorScenario;
        assert model.getGoodTypeMasterList().getGoodTypeCorrespondingToThisCode(OilDistributorScenario.oilGoodType.getCode()) != null; //make sure oil is properly registered.
        final GeographicalMarket market = (financial.market.GeographicalMarket)model.getMarket(OilDistributorScenario.oilGoodType);
        //create the view
        GeographicalMarketView view = new ControllableGeographicalMarketView(market ,model, (model.scenario.ControllableGeographicalScenario) scenario);
        //let the view populate your center
        view.buildTabs(this);

        //create the slider. TODO This really ought to be moved
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

        modelControl.getContentBox().getChildren().add(distancePenalty);
        modelControl.getContentBox().getChildren().add(label);
        controls.setExpandedPane(modelControl);




    }



    @Override
    public void setGUINodeParent(GUINode parent) {
        //ignored
    }

    @Override
    public void representSelectedObject(SelectionEvent event) {
        final Object objectSelected = event.getObjectSelected();
        if(objectSelected instanceof EconomicAgent) {
            //create new tab
            final AgentLogView logTab = new AgentLogView(model, (EconomicAgent) objectSelected);
            //handle it
            handleNewTab(new TabEvent(logTab));
        }
    }

    /**
     * this being the root we take the tab event and show the full tab
     */
    @Override
    public void handleNewTab(TabEvent event)
    {
        //put the tab in the map for easy access
        final InformationTab tab = event.getNewTab();
        viewToContainerMap.put(tab.getView(),tab);
        //listen to it
        tab.setGUINodeParent(this);


        //visualize the tab
        center.getTabs().add(tab.getView());

    }


    /**
     * the only way to initialize a model view is through this task, useful to cover the long loading time.
     * @return
     */
    public static Task<ModelViewDemo> getInitializer(){
        return new Task<ModelViewDemo>() {
            @Override
            protected ModelViewDemo call() throws Exception {
                //create model and agents
                updateProgress(0,4);
                updateMessage("Creating Agents");
                MacroII model = new MacroII(0);
                OilDistributorScenario scenario = new OilDistributorScenario(model);
                ModelViewDemo demo = new ModelViewDemo(model,scenario);
                updateProgress(1,4);

                updateMessage("Scheduling Agents");
                demo.startModel();
                updateProgress(2, 4);

                updateMessage("Building Main GUI");
                demo.buildGUI();
                updateProgress(3,4);


                updateMessage("Initializing Tabs");
                demo.buildInitialTabs();
                updateProgress(4,4);
                return demo;
            }
        };
    }
}
