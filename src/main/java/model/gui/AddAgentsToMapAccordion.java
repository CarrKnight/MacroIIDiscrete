/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.Accordion;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import model.utilities.geography.NewFirmPortrait;

/**
 * A series of buttons to press to
 * Created by carrknight on 4/24/14.
 */
public class AddAgentsToMapAccordion extends Accordion{


    ObservableValue<MouseMode> currentMouseMode;



    public AddAgentsToMapAccordion() {

        ToggleButton normalSelection = new ToggleButton("Normal");
        ToggleButton addOilPump = new ToggleButton("Add Firm",new NewFirmPortrait());
        ToggleButton addHousehold = new ToggleButton("Add Household");

        ToggleGroup mouseSelection = new ToggleGroup();

        normalSelection.setToggleGroup(mouseSelection);
        addOilPump.setToggleGroup(mouseSelection);
        addHousehold.setToggleGroup(mouseSelection);

        //default to normal selection
        mouseSelection.selectToggle(normalSelection);







    }
}
