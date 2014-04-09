/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.scenario;

import agents.firm.GeographicalFirm;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentFactory;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.pricing.pid.SalesControlWithFixedInventoryAndPID;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import financial.market.GeographicalMarket;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import model.scenario.oil.GeographicalCustomer;
import model.utilities.ActionOrder;
import model.utilities.geography.Location;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * Simple Geographical Model, two neighborhoods, the rich one around 5,5 the poor one around -5,-5 and 3 distributors at 5,5 0,0 and -5,-5.
 * As of now each distributor produces fixed 10 units of goods
 * Created by carrknight on 4/8/14.
 */
public class OilDistributorScenario extends Scenario
{

    /**
     * each neighborhood is 30 people
     */
    private int neighborhoodSize = 30;

    /**
     * the minimum paying price of customers in the poor neighborhood
     */
    private int minPricePoorNeighborhood = 10;

    /**
     * the maximum paying price of customers in the poor neighborhood
     */
    private int maxPricePoorNeighborhood = 80;


    /**
     * the minimum paying price of customers in the rich neighborhood
     */
    private int minPriceRichNeighborhood = 60;

    /**
     * the maximum paying price of customers in the rich neighborhood
     */
    private int maxPriceRichNeighborhood = 200;

    /**
     * how much each oil pump produces each day!
     */
    private int dailyProductionPerFirm = 10;


    public OilDistributorScenario(MacroII model) {
        super(model);
    }

    @Override
    public void start() {
        GeographicalMarket market = new GeographicalMarket(GoodType.OIL);
        getMarkets().put(GoodType.OIL,market);
        //poor neighborhood
        createNeighborhood(new Location(-5,-5),1.5,minPricePoorNeighborhood,maxPricePoorNeighborhood,
                neighborhoodSize,market,model.getRandom());
        //rich neighborhood
        createNeighborhood(new Location(5,5),1.5,minPriceRichNeighborhood,maxPriceRichNeighborhood,
                neighborhoodSize,market,model.getRandom());

        //three pumps
        createOilPump(new Location(-5,-5), 10,market);
        createOilPump(new Location(0,0), 10,market);
        createOilPump(new Location(5,5), 10,market);
    }

    public void createNeighborhood(Location center, double centerStandardDeviation, int minPrice, int maxPrice,
                                   int howManyPeople, GeographicalMarket market, MersenneTwisterFast randomizer)
    {
        Preconditions.checkState(minPrice >=0, "min price can't be negative");
        Preconditions.checkState(maxPrice > minPrice, "max price must be above min price");
        for(int i=0; i<howManyPeople; i++)
        {
            double xNoise = randomizer.nextGaussian()* centerStandardDeviation;
            double yNoise = randomizer.nextGaussian()* centerStandardDeviation;
            int price = randomizer.nextInt(maxPrice-minPrice)+minPrice;
            assert price >= minPrice;
            assert price <=maxPrice;
            GeographicalCustomer customer = new GeographicalCustomer(getModel(),price,center.getxLocation()+xNoise,
                    center.getyLocation()+yNoise,market);
            getAgents().add(customer);
        }

    }

    public void createOilPump(Location location, int productionRate,GeographicalMarket market)
    {
        Preconditions.checkState(productionRate >0, "min price can't be negative");

        final GeographicalFirm oilPump = new GeographicalFirm(getModel(),location.getxLocation(),location.getyLocation());
        //create a salesDepartment
        SalesDepartment salesDepartment = SalesDepartmentFactory.incompleteSalesDepartment(oilPump, market,
                new SimpleBuyerSearch(market, oilPump), new SimpleSellerSearch(market, oilPump), SalesDepartmentOneAtATime.class);
        //give the sale department a simple PID
        salesDepartment.setAskPricingStrategy(new SalesControlWithFixedInventoryAndPID(salesDepartment,10));
        //finally register it!
        oilPump.registerSaleDepartment(salesDepartment, GoodType.OIL);

        //create a steppable refilling oilPump every day
        getModel().scheduleSoon(ActionOrder.PRODUCTION,new Steppable() {
                    @Override
                    public void step(SimState simState) {
                        for(int i=0;i<productionRate; i++)
                        {
                            Good barrel = new Good(GoodType.OIL, oilPump, 0);
                            oilPump.receive(barrel,null);
                            oilPump.reactToPlantProduction(barrel);
                        }
                        getModel().scheduleTomorrow(ActionOrder.PRODUCTION,this);
                    }
        });

    }



    public int getNeighborhoodSize() {
        return neighborhoodSize;
    }

    public void setNeighborhoodSize(int neighborhoodSize) {
        this.neighborhoodSize = neighborhoodSize;
    }

    public int getMinPricePoorNeighborhood() {
        return minPricePoorNeighborhood;
    }

    public void setMinPricePoorNeighborhood(int minPricePoorNeighborhood) {
        this.minPricePoorNeighborhood = minPricePoorNeighborhood;
    }

    public int getMaxPricePoorNeighborhood() {
        return maxPricePoorNeighborhood;
    }

    public void setMaxPricePoorNeighborhood(int maxPricePoorNeighborhood) {
        this.maxPricePoorNeighborhood = maxPricePoorNeighborhood;
    }

    public int getMinPriceRichNeighborhood() {
        return minPriceRichNeighborhood;
    }

    public void setMinPriceRichNeighborhood(int minPriceRichNeighborhood) {
        this.minPriceRichNeighborhood = minPriceRichNeighborhood;
    }

    public int getMaxPriceRichNeighborhood() {
        return maxPriceRichNeighborhood;
    }

    public void setMaxPriceRichNeighborhood(int maxPriceRichNeighborhood) {
        this.maxPriceRichNeighborhood = maxPriceRichNeighborhood;
    }

    public int getDailyProductionPerFirm() {
        return dailyProductionPerFirm;
    }

    public void setDailyProductionPerFirm(int dailyProductionPerFirm) {
        this.dailyProductionPerFirm = dailyProductionPerFirm;
    }



}
