/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.scenario;

import agents.Person;
import agents.firm.Firm;
import agents.firm.cost.InputCostStrategy;
import agents.firm.personell.FactoryProducedHumanResourcesWithMaximizerAndTargeter;
import agents.firm.personell.HumanResources;
import agents.firm.production.Blueprint;
import agents.firm.production.Plant;
import agents.firm.production.control.maximizer.PeriodicMaximizer;
import agents.firm.production.control.maximizer.algorithms.otherMaximizers.FixedTargetMaximizationAlgorithm;
import agents.firm.production.control.targeter.PIDTargeterWithQuickFiring;
import agents.firm.production.technology.LinearConstantMachinery;
import agents.firm.utilities.DummyProfitReport;
import financial.market.Market;
import financial.market.OrderBookMarket;
import financial.utilities.BuyerSetPricePolicy;
import goods.DifferentiatedGoodType;
import goods.Good;
import goods.UndifferentiatedGoodType;
import model.MacroII;

import java.util.LinkedList;
import java.util.List;

/**
 * <h4>Description</h4>
 * <p/> Very similar to simple buyer, but this involves only a human resources object
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-12-19
 * @see
 */
public class SimpleHiringScenario extends Scenario
{

    private int laborSupplyIntercept = 0;

    private int laborSupplySlope = 1;

    private int maximumWorkers = 100;

    private int numberOfFirms = 1;

    private int targetEmploymentPerFirm = 16;

    private List<HumanResources> hrs;

    private Market market;


    public SimpleHiringScenario(MacroII model) {
        super(model);
    }


    /**
     * Called by MacroII, it creates agents and then schedules them.
     */
    @Override
    public void start()
    {
        market = new OrderBookMarket(UndifferentiatedGoodType.LABOR);
        getMarkets().put(UndifferentiatedGoodType.LABOR,market);
        market.setPricePolicy(new BuyerSetPricePolicy());

        hrs = new LinkedList<>();

        for(int i=0; i< numberOfFirms ; i++ )
        {
            Firm firm = createEmployer(market);

            getAgents().add(firm);

        }

        buildLaborSupply(market);

    }

    private void buildLaborSupply(Market market) {
        for(int i=1; i<=maximumWorkers; i++)
        {
            //dummy worker, really
            final Person p = new Person(getModel(),0l,laborSupplyIntercept+i*laborSupplySlope,market);
            p.setPrecario(true);

            p.setSearchForBetterOffers(false);


            getAgents().add(p);

        }
    }

    private Firm createEmployer(Market market) {
        //this firm ignores its own production
        Firm firm = new Firm(model){
            @Override
            public void reactToPlantProduction(Good g) {
                consume(g.getType());
            }
        };
        firm.setProfitReport(new DummyProfitReport());
        firm.receiveMany(UndifferentiatedGoodType.MONEY,1000000000);
        //give it a plant
        Plant plant = new Plant(new Blueprint.Builder().output(UndifferentiatedGoodType.GENERIC,1).build(),firm);
        plant.setPlantMachinery( new LinearConstantMachinery(DifferentiatedGoodType.CAPITAL,firm,0,plant));
        plant.setCostStrategy(new InputCostStrategy(plant));
        firm.addPlant(plant);

        //create an hr with fixed target
        FactoryProducedHumanResourcesWithMaximizerAndTargeter produced =
                HumanResources.getHumanResourcesIntegrated(1000000000, firm,
                        market, plant, PIDTargeterWithQuickFiring.class, PeriodicMaximizer.class,
                        FixedTargetMaximizationAlgorithm.class, null, null);
        //take out all breaks
        ((PIDTargeterWithQuickFiring)produced.getWorkforceTargeter()).setMaximumPercentageOverTargetOfWorkersToHire(100f);

        ((PeriodicMaximizer< FixedTargetMaximizationAlgorithm >) produced.getWorkforceMaximizer()).
                getMaximizationAlgorithm().setWorkerTarget(targetEmploymentPerFirm);

        HumanResources hr = produced.getDepartment();
        hr.setFixedPayStructure(true);
        hrs.add(hr);
        return firm;
    }

    public int getLaborSupplyIntercept() {
        return laborSupplyIntercept;
    }

    public void setLaborSupplyIntercept(int laborSupplyIntercept) {
        this.laborSupplyIntercept = laborSupplyIntercept;
    }

    public int getLaborSupplySlope() {
        return laborSupplySlope;
    }

    public void setLaborSupplySlope(int laborSupplySlope) {
        this.laborSupplySlope = laborSupplySlope;
    }

    public int getMaximumWorkers() {
        return maximumWorkers;
    }

    public void setMaximumWorkers(int maximumWorkers) {
        this.maximumWorkers = maximumWorkers;
    }

    public int getNumberOfFirms() {
        return numberOfFirms;
    }

    public void setNumberOfFirms(int numberOfFirms) {
        this.numberOfFirms = numberOfFirms;
    }

    public int getTargetEmploymentPerFirm() {
        return targetEmploymentPerFirm;
    }

    public void setTargetEmploymentPerFirm(int targetEmploymentPerFirm) {
        this.targetEmploymentPerFirm = targetEmploymentPerFirm;

    }

    public List<HumanResources> getHrs() {
        return hrs;
    }

    public Market getMarket() {
        return market;
    }
}
