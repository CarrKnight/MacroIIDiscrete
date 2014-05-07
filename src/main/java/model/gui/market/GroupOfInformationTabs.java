/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui.market;

import model.gui.utilities.InformationTab;
import model.gui.utilities.TabEvent;
import model.gui.utilities.TabNode;

import java.util.Collection;

/**
 * This class represents a group of of informationTabs: viewable tabs to show on the main panel and a series
 * of titledPanes to place in the control accordion. The tabs show something about the market, the titledPanes are for control/manipulation
 * When created the object simply creates the tab, propagates them through TabEvents and does nothing else. <br>
 * This is kind of a silly way of having multiple tabs show up together.
 * Created by carrknight on 4/29/14.
 */
public abstract class GroupOfInformationTabs  {





    /**
     * this defines the information tabs to create at startup.
     * @return a collection of information tabs that will be created at startup!
     */
    protected abstract Collection<InformationTab> getTabGroup();


    public void buildTabs(TabNode tabParent){
        //create the related tabs
        Collection<InformationTab> tabs = getTabGroup();
        //dispatch the new tabs upward on the responsibility chain
        for(InformationTab tab : tabs)
            tabParent.handleNewTab(new TabEvent(tab));
    }






}
