/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.scenario.oil;

import agents.firm.Firm;
import agents.firm.GeographicalFirm;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentFactory;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.pricing.pid.SalesControlWithFixedInventoryAndPID;
import com.google.common.base.Preconditions;
import financial.market.GeographicalMarket;
import goods.Good;
import goods.GoodType;
import goods.UndifferentiatedGoodType;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.geography.Location;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * The oil pumps has a fixed "production". They don't need to hire any worker.
 *
 * Created by carrknight on 4/23/14.
 */
public class FixedProductionOilFirmStrategy implements OilFirmsScenarioStrategy {


    IntegerProperty productionRate = new SimpleIntegerProperty(10);


    /**
     * creates a simple oil pump that receives  free goods everyday to resell
     * @param location  the location of the firm
     * @param market the market to trade in
     * @param name the name of the oil-pump
     * @param scenario oil distributor scenario.
     * @param model the reference to MacroII
     */
    @Override
    public Firm createOilPump(Location location, GeographicalMarket market, String name, OilDistributorScenario scenario,
                              MacroII model)
    {

        final GeographicalFirm oilPump = new GeographicalFirm(model,location.getxLocation(),location.getyLocation());
        oilPump.setName(name);
        //create a salesDepartment
        SalesDepartment salesDepartment = SalesDepartmentFactory.incompleteSalesDepartment(oilPump, market,
                new SimpleBuyerSearch(market, oilPump), new SimpleSellerSearch(market, oilPump), SalesDepartmentOneAtATime.class);
        //give the sale department a simple PID
        salesDepartment.setAskPricingStrategy(new SalesControlWithFixedInventoryAndPID(salesDepartment,100));
        //finally register it!
        final GoodType goodTypeSold = market.getGoodType();
        oilPump.registerSaleDepartment(salesDepartment, goodTypeSold);

        model.addAgent(oilPump);

        //create a steppable refilling oilPump every day
        model.scheduleSoon(ActionOrder.PRODUCTION, new Steppable() {
            @Override
            public void step(SimState simState) {
                int dailyProduction = productionRate.get();
                Preconditions.checkState(dailyProduction > 0, "production rate must be positive");

                if(goodTypeSold.isDifferentiated())
                {
                    for (int i = 0; i < dailyProduction; i++) {
                        Good barrel = Good.getInstanceOfDifferentiatedGood(goodTypeSold, oilPump, 0);
                        oilPump.receive(barrel, null);
                        oilPump.reactToPlantProduction(barrel);
                    }
                }
                else {
                    oilPump.receiveMany((UndifferentiatedGoodType) goodTypeSold, dailyProduction);
                    oilPump.reactToPlantProduction((UndifferentiatedGoodType)goodTypeSold,dailyProduction);
                }


                model.scheduleTomorrow(ActionOrder.PRODUCTION, this);
            }
        });
        return oilPump;
    }




    public int getProductionRate() {
        return productionRate.get();
    }

    public IntegerProperty productionRateProperty() {
        return productionRate;
    }

    public void setProductionRate(int productionRate) {
        this.productionRate.set(productionRate);
    }
}
