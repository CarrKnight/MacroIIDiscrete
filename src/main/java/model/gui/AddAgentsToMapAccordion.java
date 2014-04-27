/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import model.gui.market.GeographicalMarketPresentation;
import model.scenario.GeographicalScenario;
import model.utilities.Deactivatable;
import model.utilities.geography.NewFirmPortrait;

/**
 * Created by carrknight on 4/24/14.
 * A series of buttons to press to switch mouse mode. Unfortunately because toggleGroup property is readOnly my Heterogeneous Bidirectional Binder
 * doesn't apply and I need to duplicate again a lot of listening code!
 * Created by carrknight on 4/24/14.
 */
public class AddAgentsToMapAccordion extends TitledPane implements ChangeListener<Object>, Deactivatable {


    /**
     * The mouse mode
     */
    private final ObjectProperty<MouseMode> currentMouseMode;

    /**
     * A link to the rest of the presentation
     */
    private final GeographicalMarketPresentation geographicalMarketPresentation;

    /**
     * A link to the scenario is needed to produce agents
     */
    private final GeographicalScenario scenario;

    //all the toggles
    private final ToggleButton normalSelection;
    private final ToggleButton addOilPump;
    private final ToggleButton addHousehold;
    private final ToggleGroup mouseSelection;

    private boolean updating = false;

    private final MouseModeSwitcher mouseModeSwitcher;



    public AddAgentsToMapAccordion(GeographicalMarketPresentation geographicalMarketPresentation, GeographicalScenario scenario) {

        this.geographicalMarketPresentation = geographicalMarketPresentation;
        this.scenario = scenario;



        normalSelection = new ToggleButton("Normal");
        addOilPump = new ToggleButton("Add Firm",new NewFirmPortrait());
        addHousehold = new ToggleButton("Add Household");

        mouseSelection = new ToggleGroup();
        normalSelection.setToggleGroup(mouseSelection);
        addOilPump.setToggleGroup(mouseSelection);
        addHousehold.setToggleGroup(mouseSelection);

        //default to normal selection
        mouseSelection.selectToggle(normalSelection);
        currentMouseMode = new SimpleObjectProperty<>(MouseMode.SELECTION);
        //create the mouse mode "actuator"
        mouseModeSwitcher = new MouseModeSwitcher(currentMouseMode,geographicalMarketPresentation,scenario);

        //start listening
        mouseSelection.selectedToggleProperty().addListener(this);
        currentMouseMode.addListener(this);
        //add the buttons to the accordion
        HBox toggleContainer = new HBox(normalSelection,addOilPump,addHousehold);
        super.setContent(toggleContainer);



    }

    @Override
    public void changed(ObservableValue sourceProperty, Object oldValue, Object newValue) {
        if(updating) //avoid infinite recursion
            return;
        //this is designed so that the properties never go to null
        assert (mouseSelection.selectedToggleProperty()) != null;
        assert (currentMouseMode.getValue()) != null;


        updating=true;
        if(sourceProperty.getValue().equals(mouseSelection.selectedToggleProperty()))
        {
            Toggle newToggle = (Toggle) newValue;
            //the toggle has changed
            if(newToggle.equals(normalSelection))
                currentMouseMode.setValue(MouseMode.SELECTION);
            else if(newToggle.equals(addOilPump))
                currentMouseMode.setValue(MouseMode.ADD_FIRM);
            else{
                assert newToggle.equals(addHousehold);
                currentMouseMode.setValue(MouseMode.ADD_CONSUMER);
            }

        }
        else
        {
            assert sourceProperty.getValue().equals(currentMouseMode);

            MouseMode mode = (MouseMode) newValue;
            switch (mode)
            {
                default:
                case SELECTION:
                    normalSelection.setSelected(true);
                    break;
                case ADD_FIRM:
                    addOilPump.setSelected(true);
                    break;
                case ADD_CONSUMER:
                    addHousehold.setSelected(true);
                    break;
            }


        }
        updating =false;


    }

    @Override
    public void turnOff() {
        mouseModeSwitcher.turnOff();
        mouseSelection.selectedToggleProperty().removeListener(this);
        currentMouseMode.removeListener(this);
    }
}
