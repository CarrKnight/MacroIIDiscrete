/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.scenario;

import agents.firm.Firm;
import agents.firm.cost.InputCostStrategy;
import agents.firm.personell.FactoryProducedHumanResourcesWithMaximizerAndTargeter;
import agents.firm.personell.HumanResources;
import agents.firm.production.Blueprint;
import agents.firm.production.Plant;
import agents.firm.production.control.maximizer.SetTargetThenTryAgainMaximizer;
import agents.firm.production.control.maximizer.algorithms.otherMaximizers.FixedTargetMaximizationAlgorithm;
import agents.firm.production.control.targeter.PIDTargeterWithQuickFiring;
import agents.firm.production.technology.LinearConstantMachinery;
import financial.market.Market;
import goods.DifferentiatedGoodType;
import goods.GoodType;
import model.MacroII;

/**
 * <h4>Description</h4>
 * <p/> This is more for testing than a real scenario, what this is supposed to do is force a monopolist to always produce 16, which is the solution of the supply chain
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-07-09
 * @see
 */
public class OneLinkSupplyChainScenarioCheatingBuyPriceAndForcedMonopolist extends OneLinkSupplyChainScenarioWithCheatingBuyingPrice {

    private final int beefWorkerTarget = 17;

    private final GoodType monopolistGoodType;

    public OneLinkSupplyChainScenarioCheatingBuyPriceAndForcedMonopolist(MacroII model, GoodType monopolistGood) {
        super(model);
        monopolistGoodType = monopolistGood;

        if(monopolistGood.equals(OneLinkSupplyChainScenario.INPUT_GOOD))
        {
            //make beef a monopolist
            setNumberOfBeefProducers(1);
            setNumberOfFoodProducers(5);
        }
        else
        {
            assert monopolistGood.equals(OneLinkSupplyChainScenario.OUTPUT_GOOD);
            setNumberOfBeefProducers(5);
            setNumberOfFoodProducers(1);

        }
    }


    /**
     * This create plant forces the beef producer to always hire a fixed number of workers
     * (default is 16 because 16 is the beef monopolist solution)
     */
    @Override
    protected HumanResources createPlant(Blueprint blueprint, Firm firm, Market laborMarket) {
        if(!blueprint.getOutputs().containsKey(monopolistGoodType))
        {
            return super.createPlant(blueprint, firm, laborMarket);
        }
        else
        {



            Plant plant = new Plant(blueprint, firm);
            plant.setPlantMachinery(new LinearConstantMachinery(DifferentiatedGoodType.CAPITAL, firm, 0, plant));
            plant.setCostStrategy(new InputCostStrategy(plant));
            firm.addPlant(plant);
            FactoryProducedHumanResourcesWithMaximizerAndTargeter produced =
                    HumanResources.getHumanResourcesIntegrated(Integer.MAX_VALUE, firm,
                            laborMarket, plant, PIDTargeterWithQuickFiring.class, SetTargetThenTryAgainMaximizer.class,
                            FixedTargetMaximizationAlgorithm.class, null, null);

            ((SetTargetThenTryAgainMaximizer< FixedTargetMaximizationAlgorithm >) produced.getWorkforceMaximizer()).
                    getMaximizationAlgorithm().setWorkerTarget(beefWorkerTarget);


            HumanResources hr = produced.getDepartment();
            hr.setFixedPayStructure(true);
            return hr;
        }
    }


    /**
     * Gets beefWorkerTarget.
     *
     * @return Value of beefWorkerTarget.
     */
    public int getBeefWorkerTarget() {
        return beefWorkerTarget;
    }





}
