/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.scenario;

import agents.firm.Firm;
import agents.firm.cost.InputCostStrategy;
import agents.firm.personell.FactoryProducedHumanResourcesWithMaximizerAndTargeter;
import agents.firm.personell.HumanResources;
import agents.firm.production.Blueprint;
import agents.firm.production.Plant;
import agents.firm.production.control.maximizer.WeeklyWorkforceMaximizer;
import agents.firm.production.control.maximizer.algorithms.marginalMaximizers.MarginalMaximizerWithUnitPID;
import agents.firm.production.control.maximizer.algorithms.otherMaximizers.FixedTargetMaximizationAlgorithm;
import agents.firm.production.control.targeter.PIDTargeter;
import agents.firm.production.technology.LinearConstantMachinery;
import agents.firm.sales.SalesDepartmentOneAtATime;
import au.com.bytecode.opencsv.CSVWriter;
import financial.market.Market;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.stats.collectors.DailyStatCollector;
import model.utilities.stats.collectors.ProducersStatCollector;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.io.FileWriter;
import java.io.IOException;

import static model.experiments.tuningRuns.MarginalMaximizerPIDTuning.printProgressBar;
import static org.mockito.Mockito.*;

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

    private final int beefWorkerTarget = 16;

    private final GoodType monopolistGoodType;

    public OneLinkSupplyChainScenarioCheatingBuyPriceAndForcedMonopolist(MacroII model, GoodType monopolistGood) {
        super(model);
        monopolistGoodType = monopolistGood;

        if(monopolistGood.equals(GoodType.BEEF))
        {
            //make beef a monopolist
            setNumberOfBeefProducers(1);
            setNumberOfFoodProducers(10);
        }
        else
        {
            assert monopolistGood.equals(GoodType.FOOD);
            setNumberOfBeefProducers(10);
            setNumberOfFoodProducers(1);

        }
    }


    /**
     * This create plant forces the beef producer to always hire a fixed number of workers
     * (default is 16 because 16 is the beef monopolist solution)
     */
    @Override
    protected void createPlant(Blueprint blueprint, Firm firm, Market laborMarket) {
        if(!blueprint.getOutputs().containsKey(monopolistGoodType))
            super.createPlant(blueprint, firm, laborMarket);
        else
        {

            Plant plant = new Plant(blueprint, firm);
            plant.setPlantMachinery(new LinearConstantMachinery(GoodType.CAPITAL, mock(Firm.class), 0, plant));
            plant.setCostStrategy(new InputCostStrategy(plant));
            firm.addPlant(plant);
            FactoryProducedHumanResourcesWithMaximizerAndTargeter produced =
                    HumanResources.getHumanResourcesIntegrated(Long.MAX_VALUE, firm,
                            laborMarket, plant, PIDTargeter.class, WeeklyWorkforceMaximizer.class,
                            FixedTargetMaximizationAlgorithm.class, null, null);

            ((WeeklyWorkforceMaximizer < FixedTargetMaximizationAlgorithm >) produced.getWorkforceMaximizer()).
                    getMaximizationAlgorithm().setWorkerTarget(beefWorkerTarget);


            HumanResources hr = produced.getDepartment();
            hr.setFixedPayStructure(true);
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


    /**
     * Runs the supply chain with no GUI and writes a big CSV file
     * @param args
     */
    public static void main(String[] args)
    {



        final MacroII macroII = new MacroII(System.currentTimeMillis());
        final OneLinkSupplyChainScenarioCheatingBuyPriceAndForcedMonopolist scenario1 =
                new OneLinkSupplyChainScenarioCheatingBuyPriceAndForcedMonopolist(macroII, GoodType.BEEF);
        scenario1.setControlType(MarginalMaximizerWithUnitPID.class);
        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        scenario1.setBeefPriceFilterer(null);

        //competition!





        macroII.setScenario(scenario1);
        macroII.start();

        //create the CSVWriter
        try {
            CSVWriter writer = new CSVWriter(new FileWriter("runs/supplychai/forcedmonopolistTest.csv"));
            DailyStatCollector collector = new DailyStatCollector(macroII,writer);
            collector.start();

            final CSVWriter prices = new CSVWriter(new FileWriter("runs/supplychai/forcedmonopolistTestprices.csv"));
            final CSVWriter quantities = new CSVWriter(new FileWriter("runs/supplychai/forcedmonopolistTestQuantities.csv"));
            ProducersStatCollector collector2 = new ProducersStatCollector(macroII,GoodType.BEEF,prices,quantities);
            collector2.start();
        } catch (IOException e) {
            System.err.println("failed to create the file!");
        }


        //create the CSVWriter  for purchases prices
        try {
            final CSVWriter writer2 = new CSVWriter(new FileWriter("runs/supplychai/forcedmonopolistTestOfferPricesWithCompetition.csv"));
            writer2.writeNext(new String[]{"buyer offer price","target","filtered Outflow"});
            macroII.scheduleSoon(ActionOrder.CLEANUP_DATA_GATHERING, new Steppable() {
                @Override
                public void step(SimState state) {
                    try {
                        writer2.writeNext(new String[]{String.valueOf(
                                macroII.getMarket(GoodType.BEEF).getBestBuyPrice()),
                                String.valueOf(scenario1.strategy2.getTarget()),
                                String.valueOf(scenario1.strategy2.getFilteredOutflow())});
                        writer2.flush();
                        ((MacroII) state).scheduleTomorrow(ActionOrder.CLEANUP_DATA_GATHERING, this);
                    } catch (IllegalAccessException | IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }

                }
            });

        } catch (IOException e) {
            System.err.println("failed to create the file!");
        }




        while(macroII.schedule.getTime()<15000)
        {
            macroII.schedule.step(macroII);
            printProgressBar(15001,(int)macroII.schedule.getSteps(),100);
        }


    }
}
