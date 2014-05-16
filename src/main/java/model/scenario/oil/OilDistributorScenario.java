/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.scenario.oil;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.GeographicalFirm;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import financial.market.GeographicalMarket;
import financial.market.Market;
import financial.utilities.ShopSetPricePolicy;
import goods.GoodType;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.scenario.ControllableGeographicalScenario;
import model.scenario.Scenario;
import model.utilities.dummies.GeographicalCustomer;
import model.utilities.geography.Location;

/**
 * Simple Geographical Model, two neighborhoods, the rich one around 5,5 the poor one around -5,-5 and 3 distributors at 5,5 0,0 and -5,-5.
 * As of now each distributor produces fixed 10 units of goods
 * Created by carrknight on 4/8/14.
 */
public class OilDistributorScenario extends Scenario implements ControllableGeographicalScenario
{


    public static final GoodType oilGoodType = new UndifferentiatedGoodType("oilTest","Oil",false,false);
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


    private LaborMarketOilScenarioStrategy laborMarketStrategy = new IndipendentLaborMarketsForEachFirmStrategy();


    private OilFirmsScenarioStrategy oilFirmsStrategy = new ProducingOilFirmsScenarioStrategy();


    public OilDistributorScenario(MacroII model) {
        super(model);
    }

    @Override
    public void start() {
        model.getGoodTypeMasterList().addNewSector(oilGoodType);

        GeographicalMarket market = new GeographicalMarket(oilGoodType);
        market.setPricePolicy(new ShopSetPricePolicy());
        getMarkets().put(oilGoodType,market);
        //poor neighborhood
        createNeighborhood(new Location(-10,-2),1.5,minPricePoorNeighborhood,maxPricePoorNeighborhood,
                neighborhoodSize,market,model.getRandom());
        //rich neighborhood
        createNeighborhood(new Location(10,2),1.5,minPriceRichNeighborhood,maxPriceRichNeighborhood,
                neighborhoodSize,market,model.getRandom());

        //labor market, if needed
        laborMarketStrategy.initializeLaborMarkets(this,market,getModel());

        //three pumps
        createNewProducer(new Location(-10, -2), market, "poor");
        createNewProducer(new Location(0, 0), market, "middle");
        createNewProducer(new Location(10, 2), market, "rich");

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
            final double y = center.getyLocation() + yNoise;
            final double x = center.getxLocation()+xNoise;
            GeographicalCustomer customer = new GeographicalCustomer(getModel(),price,x,y,market);
            getAgents().add(customer);
        }

    }

    public EconomicAgent createNewConsumer(Location location, GeographicalMarket market, int price)
    {
        GeographicalCustomer customer = new GeographicalCustomer(getModel(),price,
                location.getxLocation(),location.getyLocation(),market);
        getAgents().add(customer);
        return customer;
    }

    public Firm createNewProducer(Location location, GeographicalMarket market, String name)
    {
        return oilFirmsStrategy.createOilPump(location,market,name,this,getModel());

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

    public Market assignLaborMarketToFirm(GeographicalFirm oilStation) {
        return laborMarketStrategy.assignLaborMarketToFirm(oilStation, this, model);
    }
}
