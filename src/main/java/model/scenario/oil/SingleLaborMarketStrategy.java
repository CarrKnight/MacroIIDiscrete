/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.scenario.oil;

import agents.firm.GeographicalFirm;
import financial.market.GeographicalMarket;
import financial.market.Market;
import financial.market.OrderBookMarket;
import goods.GoodType;
import javafx.beans.property.SimpleIntegerProperty;
import model.MacroII;
import model.scenario.MonopolistScenario;
import model.scenario.OilDistributorScenario;

/**
 * A single labor market, created at initialization.
 * Created by carrknight on 4/21/14.
 */
public class SingleLaborMarketStrategy implements LaborMarketOilScenarioStrategy {

    private final OrderBookMarket laborMarket;

    private final SimpleIntegerProperty laborSupplySlope;

    private final SimpleIntegerProperty laborSupplyIntercept;

    private final SimpleIntegerProperty totalNumberOfWorkers;




    public SingleLaborMarketStrategy() {
        this(1,0,50);
    }


    public SingleLaborMarketStrategy(int laborSupplySlope, int laborSupplyIntercept, int totalNumberOfWorkers)
    {
        this.laborMarket = new OrderBookMarket(GoodType.LABOR);
        this.laborSupplySlope = new SimpleIntegerProperty(laborSupplySlope);
        this.laborSupplyIntercept = new SimpleIntegerProperty(laborSupplyIntercept);
        this.totalNumberOfWorkers = new SimpleIntegerProperty(totalNumberOfWorkers);


    }

    @Override
    public void initializeLaborMarkets(OilDistributorScenario scenario, GeographicalMarket oilMarket, MacroII model) {
        //register the market
        model.getGoodTypeMasterList().addNewSector(GoodType.LABOR);
        scenario.getMarkets().put(GoodType.LABOR,laborMarket);

        //fill labor market
        MonopolistScenario.fillLaborSupply(laborSupplyIntercept.get(),laborSupplySlope.get(),true,false,
                totalNumberOfWorkers.get(),laborMarket,model);

        //get ready to update the market if any observable changes
        new LaborMarketForOilUpdater(laborSupplySlope,
                laborSupplyIntercept,totalNumberOfWorkers,model,laborMarket);
        //the updater lives on its own, listens and deactivates itself. We really don't even need a reference to it

    }

    @Override
    public Market assignLaborMarketToFirm(GeographicalFirm oilStation, OilDistributorScenario scenario, MacroII model) {
        return laborMarket;
    }






}
