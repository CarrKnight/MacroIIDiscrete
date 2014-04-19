/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.scenario.oil;

import agents.firm.GeographicalFirm;
import financial.market.GeographicalMarket;
import financial.market.Market;
import model.MacroII;
import model.scenario.OilDistributorScenario;

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
public interface OilLaborMarketScenarioStrategy {


    public void initializeLaborMarkets(OilDistributorScenario scenario, GeographicalMarket oilMarket, MacroII model);


    public Market assignLaborMarketToPlant(GeographicalFirm oilStation);



}
