/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.scenario.oil;

import agents.firm.GeographicalFirm;
import financial.market.GeographicalMarket;
import financial.market.Market;
import model.MacroII;

/**
 * <h4>Description</h4>
 * <p>  This strategy defines how/whether to create labor market(s) in the oil distribution scenario
 * <p>
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-04-19
 * @see
 */
public interface LaborMarketOilScenarioStrategy {


    /**
     * Called at the start() of scenario. It's called before firms are initialized.
     * @param scenario the scenario whose start() is currently proceeding
     * @param oilMarket the geographical market this strategy focuses on
     * @param model the model of the scenario, for scheduling and such.
     */
    public void initializeLaborMarkets(OilDistributorScenario scenario, GeographicalMarket oilMarket, MacroII model);

    /**
     * Called every-time a new firm is created so that it is returned the labor market it has to hire from
     * @param scenario the scenario whose start() is currently proceeding
     * @param oilMarket the geographical market this strategy focuses on
     * @param model the model of the scenario, for scheduling and such.
     */
    public Market assignLaborMarketToFirm(GeographicalFirm oilStation, OilDistributorScenario scenario, MacroII model);



}
