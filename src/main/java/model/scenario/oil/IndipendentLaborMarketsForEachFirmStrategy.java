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
import financial.utilities.BuyerSetPricePolicy;
import goods.GoodType;
import goods.UndifferentiatedGoodType;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableIntegerValue;
import model.MacroII;
import model.scenario.MonopolistScenario;

import java.util.HashMap;

/**
 * Each firm that requests it, gets its own labor market
 * Created by carrknight on 4/22/14.
 */
public class IndipendentLaborMarketsForEachFirmStrategy implements LaborMarketOilScenarioStrategy {

    public final static int defaultSupplySlope = 1 ;

    public final static int defaultSupplyIntercept = 20 ;

    public final static int defaultNumberOfWorkers = 50 ;

    private final HashMap<Market, ObservableIntegerValue> supplySlopes = new HashMap<>();

    private final HashMap<Market, ObservableIntegerValue> supplyIntercepts = new HashMap<>();

    private final HashMap<Market, ObservableIntegerValue> supplyNumberOfWorkers = new HashMap<>();



    @Override
    public void initializeLaborMarkets(OilDistributorScenario scenario, GeographicalMarket oilMarket, MacroII model)
    {
        //nothing really happens here!
    }

    @Override
    public Market assignLaborMarketToFirm(GeographicalFirm oilStation, OilDistributorScenario scenario, MacroII model)
    {
        //create a new goodType, a labor for this firm
        int id = supplySlopes.size();
        GoodType laborType = new UndifferentiatedGoodType("labor"+id,"Workers for " +oilStation.toString(),false,true );
        model.getGoodTypeMasterList().addNewSector(laborType);

        //create the market
        Market laborMarket = new OrderBookMarket(laborType);
        laborMarket.setPricePolicy(new BuyerSetPricePolicy());
        MonopolistScenario.fillLaborSupply(defaultSupplyIntercept,defaultSupplySlope,true, defaultNumberOfWorkers,laborMarket,model);
        ObservableIntegerValue slope = new SimpleIntegerProperty(defaultSupplySlope); supplySlopes.put(laborMarket,slope);
        ObservableIntegerValue intercept = new SimpleIntegerProperty(defaultSupplyIntercept);supplyIntercepts.put(laborMarket,intercept);
        ObservableIntegerValue workers = new SimpleIntegerProperty(defaultNumberOfWorkers);supplyNumberOfWorkers.put(laborMarket,workers);
        assert supplyIntercepts.size() == supplySlopes.size();
        assert supplyNumberOfWorkers.size() == supplySlopes.size();
        new LaborMarketForOilUpdater(slope,intercept,workers,model,laborMarket); //apply an updater to it
        if(!model.hasStarted())
            scenario.getMarkets().put(laborType,laborMarket);
        else
            model.addMarket(laborMarket);

        //return it!
        return laborMarket;



    }



}
