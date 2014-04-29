/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui.market;

import javafx.collections.ObservableSet;
import javafx.scene.control.Tab;
import javafx.scene.control.TitledPane;
import model.gui.utilities.TabEventListener;

import java.util.Collection;

/**
 * This is supposed to be the basic market-view. Market views are just a series of tabs to show on the main panel and a series
 * of titledPanes to place in the control accordion. The tabs show something about the market, the titledPanes are for control/manipulation
 * The list of "initial" tabs must be ready after constructor. Other tabs can be opened and broadcasted to listeners.
 * TitlePane/Controls don't need to be broadcasted, rather they are tied to tabs in a map. This means that different tabs can have different controls.
 * Created by carrknight on 4/29/14.
 */
public interface MarketView {


    public Collection<Tab> getInitialTabs();

    public void addListener(TabEventListener listener);

    public boolean removeListener(TabEventListener listener);

    public ObservableSet<TitledPane> getControls(Tab tab);

}
