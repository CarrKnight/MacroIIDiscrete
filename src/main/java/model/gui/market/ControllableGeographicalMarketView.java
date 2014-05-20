/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
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
        getMapTab().getControls().add(control);



    }
}
