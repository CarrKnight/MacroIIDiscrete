/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui.market;

import financial.market.GeographicalMarket;
import model.MacroII;
import model.gui.AddAgentsToMapTitledPane;
import model.scenario.ControllableGeographicalScenario;

/**
 * A geographical market view that has a button to add/remove firms!
 * Created by carrknight on 4/29/14.
 */
public class ControllableGeographicalMarketView extends GeographicalMarketView {
    public ControllableGeographicalMarketView(GeographicalMarket market, MacroII model,
                                              ControllableGeographicalScenario scenario) {
        //this creates the old views
        super(market, model);

        //create an add/remove agents panel!
        AddAgentsToMapTitledPane control = new AddAgentsToMapTitledPane(getPresentation(),scenario);
        associateControlToTab(control,getMapTab());



    }
}
