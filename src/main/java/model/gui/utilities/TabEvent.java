/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui.utilities;

import javafx.scene.control.Tab;
import model.gui.market.MarketView;

import java.util.EventObject;

/**
 * Not really a javafx event, much simpler. It basically says a new tab wants to be created.
 * Created by carrknight on 4/29/14.
 */
public class TabEvent extends EventObject {

    private final MarketView source;

    private final Tab newTab;


    public TabEvent(MarketView source, Tab newTab) {
        super(source);
        this.source = source;
        this.newTab = newTab;
    }


    public MarketView getSource() {
        return source;
    }

    public Tab getNewTab() {
        return newTab;
    }
}
