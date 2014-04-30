/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.scenario;

import financial.market.GeographicalMarket;
import model.utilities.geography.Location;

/**
 * A geographical scenario must be able to add agents on the fly in markets (this plays well with GUI)
 * Created by carrknight on 4/25/14.
 */
public interface ControllableGeographicalScenario {

    public void createNewProducer(Location location, GeographicalMarket market, String name);


    public void createNewConsumer(Location location, GeographicalMarket market, long price);

}
