/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.scenario.oil;

import agents.firm.Firm;
import agents.firm.GeographicalFirm;
import agents.firm.personell.FactoryProducedHumanResources;
import agents.firm.personell.HumanResources;
import agents.firm.production.Blueprint;
import agents.firm.production.Plant;
import agents.firm.production.control.PlantControl;
import agents.firm.production.control.facades.MarginalPlantControl;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.prediction.PurchasesPredictor;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentFactory;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.exploration.BuyerSearchAlgorithm;
import agents.firm.sales.exploration.SellerSearchAlgorithm;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.prediction.SalesPredictor;
import agents.firm.sales.pricing.pid.SalesControlWithFixedInventoryAndPID;
import financial.market.GeographicalMarket;
import financial.market.Market;
import goods.GoodType;
import model.MacroII;
import model.utilities.geography.Location;

/**
 * Something of an abstract class, building sales department/purchases department/human resources, but allows subclass to define how exactly.
 * Doesn't assume inputs
 * Created by carrknight on 4/23/14.
 */
public class ProducingOilFirmsScenarioStrategy implements OilFirmsScenarioStrategy {

    Class<? extends PlantControl> plantControl;

    Class<? extends PurchasesPredictor> hrPredictor;

    Class<? extends SalesPredictor> salesPredictor;


    public ProducingOilFirmsScenarioStrategy(){
        this(MarginalPlantControl.class, PurchasesDepartment.defaultPurchasePredictor,SalesDepartment.defaultPredictorStrategy);
    }

    public ProducingOilFirmsScenarioStrategy(Class<? extends PlantControl> plantControl,
                                             Class<? extends PurchasesPredictor> hrPredictor,
                                             Class<? extends SalesPredictor> salesPredictor) {
        this.plantControl = plantControl;
        this.hrPredictor = hrPredictor;
        this.salesPredictor = salesPredictor;
    }

    @Override
    public Firm createOilPump(Location location, GeographicalMarket market, String name,
                              OilDistributorScenario scenario, MacroII model)
    {
        //initialize the firm
        final GeographicalFirm oilPump = new GeographicalFirm(model,location.getxLocation(),location.getyLocation());
        oilPump.setName(name);
        oilPump.earn(100000000l);

        //create the sales department
        buildSalesDepartment(market, oilPump);

        //create the plant
        Plant plant = buildPlant(oilPump, market.getGoodType());
        //human resources
        Market laborMarket = scenario.assignLaborMarketToFirm(oilPump);
        HumanResources hr = buildHumanResources(oilPump, plant, laborMarket);

        model.addAgent(oilPump);

        return oilPump;
    }

    protected SalesDepartment buildSalesDepartment(GeographicalMarket market, GeographicalFirm oilPump) {
        SalesDepartment salesDepartment = SalesDepartmentFactory.incompleteSalesDepartment(oilPump, market,
                new SimpleBuyerSearch(market, oilPump), new SimpleSellerSearch(market, oilPump), SalesDepartmentOneAtATime.class);
        //give the sale department a simple PID
        final SalesControlWithFixedInventoryAndPID askPricingStrategy =
                new SalesControlWithFixedInventoryAndPID(salesDepartment, 5);
        askPricingStrategy.setInitialPrice(200);
        salesDepartment.setAskPricingStrategy(askPricingStrategy);

        salesDepartment.setPredictorStrategy(SalesPredictor.Factory.newSalesPredictor(salesPredictor,salesDepartment));
        //finally register it!
        final GoodType goodTypeSold = market.getGoodType();
        oilPump.registerSaleDepartment(salesDepartment, goodTypeSold);
        return salesDepartment;
    }

    protected HumanResources buildHumanResources(GeographicalFirm oilPump, Plant plant, Market laborMarket) {
        HumanResources hr;
        final FactoryProducedHumanResources<? extends PlantControl,BuyerSearchAlgorithm,SellerSearchAlgorithm> factoryMadeHR =
                HumanResources.getHumanResourcesIntegrated(Long.MAX_VALUE, oilPump,
                        laborMarket, plant,plantControl, null, null);
        hr = factoryMadeHR.getDepartment();
        hr.setPredictor(PurchasesPredictor.Factory.newPurchasesPredictor(hrPredictor,hr));
        hr.setFixedPayStructure(true);
        return hr;
    }

    protected Plant buildPlant(GeographicalFirm oilPump, GoodType goodTypeSold) {
        Blueprint blueprint = (new Blueprint.Builder()).output(goodTypeSold,1).build();
        return Plant.buildSimplePlantToFirm(oilPump, blueprint);
    }


}
