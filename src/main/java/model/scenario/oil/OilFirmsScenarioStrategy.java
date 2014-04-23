/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.scenario.oil;

import agents.firm.Firm;
import financial.market.GeographicalMarket;
import model.MacroII;
import model.scenario.OilDistributorScenario;
import model.utilities.geography.Location;

/**
 * <h4>Description</h4>
 * <p> A strategy to vary the kind of firm that are build and live within the oil-distributor scenario
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
public interface OilFirmsScenarioStrategy
{

    /**
     * Creates a new firm, adds it as an agent in the model and returns it
     * this method can be called by the GUI to build more oil pumps, so it's important for it to make sense outside the initialization context
     * @param location  the location of the firm
     * @param market the market to trade in
     * @param name the name of the oil-pump
     * @param scenario oil distributor scenario.
     */
    public Firm createOilPump(Location location,GeographicalMarket market, String name, OilDistributorScenario scenario,
                              MacroII model);



}
