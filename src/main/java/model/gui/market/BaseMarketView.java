/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui.market;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.control.Tab;
import javafx.scene.control.TitledPane;
import model.gui.utilities.TabEvent;
import model.gui.utilities.TabEventListener;

import java.util.*;

/**
 * A simple "skeletal" market view, deals with the upkeep of listeners and such
 * Created by carrknight on 4/29/14.
 */
public abstract class BaseMarketView implements MarketView {

    private final Map<Tab, ObservableSet<TitledPane>> viewToControlsMap = new HashMap<>();

    /**
     * tabs from the start
     */
    private final List<Tab> initialTabs = new ArrayList<>();

    /**
     * Controls that are associate to all tabs
     */
    private final Set<TitledPane> defaultControls = new HashSet<>();

    /**
     * where we hold the listeners for tab events
     */
    private final Set<TabEventListener> listeners = new HashSet<>();

    @Override
    public Collection<Tab> getInitialTabs() {
        return initialTabs;
    }

    @Override
    public boolean removeListener(TabEventListener listener) {
        return listeners.remove(listener);

    }

    @Override
    public void addListener(TabEventListener listener) {
        listeners.add(listener);
    }

    /**
     * the initial tabs are called whenever the gui focuses on this view. It should be ready by construction
     * @param tab the new tab to show
     */
    protected void addToInitialTabs(Tab tab)
    {
        initialTabs.add(tab);
    }

    /**
     * tell the listeners a new tab has been created. You want to have the controls already mapped when you do it
     * @param tab the new tab to show
     */
    protected void broadcast(Tab tab)
    {
        TabEvent event = new TabEvent(this,tab);
        for(TabEventListener listener : listeners)
            listener.getNotifiedOfNewTab(event);

    }

    /**
     * assign a control to a view
     * @param newControl the new control
     */
    protected void associateControlToTab(TitledPane newControl, Tab view)
    {
        ObservableSet<TitledPane> controls = getOrCreateControlSet(view);
        assert controls != null;

        controls.add(newControl);
    }

    private ObservableSet<TitledPane> getOrCreateControlSet(Tab view) {
        ObservableSet<TitledPane> controls = viewToControlsMap.get(view);
        if(controls == null) //create the collection if needed
        {
            controls = FXCollections.observableSet(new LinkedHashSet<>()) ;
            viewToControlsMap.put(view,controls);
        }
        return controls;
    }

    @Override
    public ObservableSet<TitledPane> getControls(Tab view) {

        ObservableSet<TitledPane> controls = getOrCreateControlSet(view);
        controls.addAll(defaultControls); //make sure all the defaults are in
        return controls;

    }
}
