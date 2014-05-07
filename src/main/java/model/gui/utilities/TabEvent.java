/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui.utilities;

/**
 * Not really a javafx event, much simpler. It basically says a new tab wants to be created.
 * Created by carrknight on 4/29/14.
 */
public class TabEvent {

    private final InformationTab newTab;


    public TabEvent(InformationTab newTab) {
        this.newTab = newTab;
    }



    public InformationTab getNewTab() {
        return newTab;
    }
}
