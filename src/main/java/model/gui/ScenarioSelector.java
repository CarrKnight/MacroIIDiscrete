/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.gui;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.Pane;
import model.MacroII;
import model.scenario.Scenario;

import java.util.*;

/**
 * A pane with very simple listview to choose the scenario.
 * It is "self-contained" in the sense that the selection on the list is immediately set as the scenario of the given model
 * Created by carrknight on 4/2/14.
 */
public class ScenarioSelector extends Pane
{


    /**
     * we probably don't need a full property, but whatever
     */
    private SimpleObjectProperty<Class<? extends Scenario>> selectedScenario;

    private final ChangeListener<Class<? extends Scenario>> scenarioChangeListener;
    private final ListView<Class<? extends Scenario>> scenarioListView;

    public ScenarioSelector(final MacroII model)
    {

        //create the combo-box
        scenarioListView = new ListView<>();
        scenarioListView.setEditable(false);
        scenarioListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);


        //get all scenarios
        List<Class<? extends Scenario>> scenarios = new ArrayList<>(Scenario.allScenarios);
        //sort
        Collections.sort(scenarios, (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));

        //add them
        scenarioListView.getItems().addAll(scenarios);
        //set as selected whatever is the default scenario!
        scenarioListView.getSelectionModel().clearSelection();
        scenarioListView.getSelectionModel().select(model.getScenario().getClass());

        //create and bind the property
        selectedScenario = new SimpleObjectProperty<>();
        selectedScenario.bind(scenarioListView.getSelectionModel().selectedItemProperty());
        //if the binding works, it should now be very simply tied to the initial scenario.
        assert selectedScenario.getValue().equals(model.getScenario().getClass());

        //the listener that changes the scenario whenever it is needed.
        scenarioChangeListener = (observableValue, aClass, aClass2) -> {
            try {
                Scenario newScenario = observableValue.getValue().newInstance();
                model.setScenario(newScenario);
            } catch (Exception e) {
                System.err.println("Failed to instantiate the new scenario!");
                System.exit(-1);
            }
        };
        selectedScenario.addListener(scenarioChangeListener);

        this.getChildren().add(scenarioListView);

    }


}
