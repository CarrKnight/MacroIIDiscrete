/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui.utilities;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Tab;
import javafx.scene.control.TitledPane;
import model.utilities.Deactivatable;

import java.util.Collection;

/**
 * A model tab with its associated control panes. Pushes all events up the nodes
 * Created by carrknight on 5/6/14.
 */
public class InformationTab implements GUINode, Deactivatable
{

    BooleanProperty active = new SimpleBooleanProperty(true);

    /**
     * a collection of all the controls. Mutable
     */
    private final Collection<TitledPane> controls;

    /**
     * the view. Immutable
     */
    private final Tab view;

    /**
     * a simple information tab uses the delegate to push up all the events
     */
    private GUINodeSimple delegate;

    public InformationTab(Collection<TitledPane> controls, Tab view)
    {
        delegate = new GUINodeSimple();

        this.controls = controls;
        this.view = view;

        //connect to the view so that if something closes it we turn off
        view.onClosedProperty().setValue(event -> turnOff());
    }



    public Collection<TitledPane> getControls() {
        return controls;
    }

    public Tab getView() {
        return view;
    }

    @Override
    public void setGUINodeParent(GUINode parent) {
        delegate.setGUINodeParent(parent);
    }


    @Override
    public void turnOff() {
        active.setValue(false);
        delegate.setGUINodeParent(null);
        controls.clear();
    }

    @Override
    public void handleNewTab(TabEvent event) {
        delegate.handleNewTab(event);
    }

    @Override
    public void representSelectedObject(SelectionEvent event) {
        delegate.representSelectedObject(event);
    }



    public ReadOnlyBooleanProperty isActive() {
        return active;
    }
}
