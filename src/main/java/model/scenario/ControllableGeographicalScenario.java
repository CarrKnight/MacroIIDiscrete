/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.scenario;

import agents.EconomicAgent;
import agents.firm.Firm;
import financial.market.GeographicalMarket;
import model.utilities.geography.Location;

/**
 * A geographical scenario must be able to add agents on the fly in markets (this plays well with GUI)
 * Created by carrknight on 4/25/14.
 */
public interface ControllableGeographicalScenario {

    public Firm createNewProducer(Location location, GeographicalMarket market, String name);


    public EconomicAgent createNewConsumer(Location location, GeographicalMarket market, int price);

}
