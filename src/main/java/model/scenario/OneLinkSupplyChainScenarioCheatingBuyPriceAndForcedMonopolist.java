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
import agents.firm.production.control.maximizer.PeriodicMaximizer;
import agents.firm.production.control.maximizer.SetTargetThenTryAgainMaximizer;
import agents.firm.production.control.maximizer.algorithms.marginalMaximizers.MarginalMaximizer;
import agents.firm.production.control.maximizer.algorithms.otherMaximizers.FixedTargetMaximizationAlgorithm;
import agents.firm.production.control.targeter.PIDTargeterWithQuickFiring;
import agents.firm.production.technology.LinearConstantMachinery;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.prediction.FixedIncreasePurchasesPredictor;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.prediction.FixedDecreaseSalesPredictor;
import agents.firm.sales.pricing.pid.InventoryBufferSalesControl;
import au.com.bytecode.opencsv.CSVWriter;
import financial.market.Market;
import goods.DifferentiatedGoodType;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.stats.collectors.DailyStatCollector;
import model.utilities.stats.collectors.ProducersStatCollector;
import model.utilities.stats.collectors.enums.SalesDataType;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static model.experiments.tuningRuns.MarginalMaximizerPIDTuning.printProgressBar;

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


    /**
     * Runs the supply chain with no GUI and writes a big CSV file
     * @param args
     */
    public static void main2(String[] args)
    {



        final MacroII macroII = new MacroII(System.currentTimeMillis());
        final OneLinkSupplyChainScenarioCheatingBuyPriceAndForcedMonopolist scenario1 =
                new OneLinkSupplyChainScenarioCheatingBuyPriceAndForcedMonopolist(macroII, OneLinkSupplyChainScenario.INPUT_GOOD);
        scenario1.setControlType(MarginalMaximizer.class);
        scenario1.setMaximizerType(PeriodicMaximizer.class);
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
            ProducersStatCollector collector2 = new ProducersStatCollector(macroII,OneLinkSupplyChainScenario.INPUT_GOOD,prices,quantities);
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
                                macroII.getMarket(OneLinkSupplyChainScenario.INPUT_GOOD).getBestBuyPrice()),
                                String.valueOf(scenario1.strategy2.getTargetInventory()),
                                String.valueOf(scenario1.strategy2.getDepartment().getLatestObservation(SalesDataType.HOW_MANY_TO_SELL))});
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

    /**
     * Runs the supply chain with no GUI and writes a big CSV file
     * @param args
     */
    public static void main(String[] args)
    {



        final MacroII macroII = new MacroII(System.currentTimeMillis());
        final OneLinkSupplyChainScenarioCheatingBuyPriceAndForcedMonopolist scenario1 =
                new OneLinkSupplyChainScenarioCheatingBuyPriceAndForcedMonopolist(macroII, OneLinkSupplyChainScenario.INPUT_GOOD)
                {



                    @Override
                    public void buildFoodPurchasesPredictor(PurchasesDepartment department) {
                        department.setPredictor(new FixedIncreasePurchasesPredictor(0));

                    }


                    @Override
                    protected SalesDepartment createSalesDepartment(Firm firm, Market goodmarket) {
                        final SalesDepartment department = super.createSalesDepartment(firm, goodmarket);
                        if(goodmarket.getGoodType().equals(OneLinkSupplyChainScenario.OUTPUT_GOOD))
                            department.setPredictorStrategy(new FixedDecreaseSalesPredictor(0));


                        if(goodmarket.getGoodType().equals(OneLinkSupplyChainScenario.INPUT_GOOD))
                        {
                            InventoryBufferSalesControl askPricingStrategy =
                                    new InventoryBufferSalesControl(department);
                            department.setAskPricingStrategy(askPricingStrategy);

                            askPricingStrategy.setProportionalGain(askPricingStrategy.getProportionalGain()/divideProportionalGainByThis);
                            askPricingStrategy.setIntegralGain(askPricingStrategy.getIntegralGain()/divideIntegrativeGainByThis);
                            model.scheduleAnotherDay(ActionOrder.CLEANUP_DATA_GATHERING,new Steppable() {
                                @Override
                                public void step(SimState state) {
                                    department.getData().writeToCSVFile(new File("supplySales.csv"));
                                }
                            },10000);
                        }

                        return department;
                    }

                    @Override
                    protected HumanResources createPlant(Blueprint blueprint, Firm firm, Market laborMarket) {
                        HumanResources hr = super.createPlant(blueprint, firm, laborMarket);    //To change body of overridden methods use File | Settings | File Templates.
                        if(blueprint.getOutputs().containsKey(OneLinkSupplyChainScenario.INPUT_GOOD))
                            hr.setPredictor(new FixedIncreasePurchasesPredictor(1));
                        else
                            hr.setPredictor(new FixedIncreasePurchasesPredictor(0));
                        return hr;
                    }

                }  ;
        scenario1.setControlType(MarginalMaximizer.class);
        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        scenario1.setBeefPriceFilterer(null);

        //competition!
        scenario1.setNumberOfFoodProducers(1);


        scenario1.setDivideProportionalGainByThis(100f);
        scenario1.setDivideIntegrativeGainByThis(100f);
        //no delay
        scenario1.setBeefPricingSpeed(0);
        scenario1.setBeefPriceFilterer(null);


        macroII.setScenario(scenario1);
        macroII.start();




        //create the CSVWriter
        try {
            CSVWriter writer = new CSVWriter(new FileWriter("runs/supplychai/newrun.csv"));
            DailyStatCollector collector = new DailyStatCollector(macroII,writer);
            collector.start();

            final CSVWriter prices = new CSVWriter(new FileWriter("runs/supplychai/forcedmonopolistTestprices.csv"));
            final CSVWriter quantities = new CSVWriter(new FileWriter("runs/supplychai/forcedmonopolistTestQuantities.csv"));
            ProducersStatCollector collector2 = new ProducersStatCollector(macroII,OneLinkSupplyChainScenario.INPUT_GOOD,prices,quantities);
            collector2.start();
        } catch (IOException e) {
            System.err.println("failed to create the file!");
        }





        while(macroII.schedule.getTime()<45000)
        {
            macroII.schedule.step(macroII);
            printProgressBar(45001, (int) macroII.schedule.getSteps(), 100);


        }

         System.out.println("done");
    }
    /**
     * Runs the supply chain with no GUI and writes a big CSV file
     * @param args
     */
    public static void main3(String[] args)
    {



        final MacroII macroII = new MacroII(System.currentTimeMillis());
        final OneLinkSupplyChainScenarioCheatingBuyPriceAndForcedMonopolist scenario1 =
                new OneLinkSupplyChainScenarioCheatingBuyPriceAndForcedMonopolist(macroII, OneLinkSupplyChainScenario.INPUT_GOOD)
        {
            @Override
            public void buildFoodPurchasesPredictor(PurchasesDepartment department) {
                department.setPredictor(new FixedIncreasePurchasesPredictor(0));
            }

            @Override
            protected SalesDepartment createSalesDepartment(Firm firm, Market goodmarket) {
                SalesDepartment department = super.createSalesDepartment(firm, goodmarket);
                if(goodmarket.getGoodType().equals(OneLinkSupplyChainScenario.OUTPUT_GOOD))
                    firm.getSalesDepartment(OneLinkSupplyChainScenario.OUTPUT_GOOD).setPredictorStrategy(new FixedDecreaseSalesPredictor(0));
                return department;
            }
        };
        scenario1.setControlType(MarginalMaximizer.class);
        scenario1.setMaximizerType(PeriodicMaximizer.class);
        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        //divide standard PID parameters by 100
        scenario1.setDivideProportionalGainByThis(10f);
        scenario1.setDivideIntegrativeGainByThis(10f);
        //no delay
        scenario1.setBeefPricingSpeed(1);
        //no real need of filter at this slow speed
        scenario1.setBeefPriceFilterer(null);
        scenario1.setWorkersToBeRehiredEveryDay(true);

        scenario1.setNumberOfBeefProducers(1);
        scenario1.setNumberOfFoodProducers(1);



        macroII.setScenario(scenario1);
        macroII.start();



        //create the CSVWriter
        try {
            CSVWriter writer = new CSVWriter(new FileWriter("runs/supplychai/forcedmonopolistTest.csv"));
            DailyStatCollector collector = new DailyStatCollector(macroII,writer);
            collector.start();

            final CSVWriter prices = new CSVWriter(new FileWriter("runs/supplychai/forcedmonopolistTestprices.csv"));
            final CSVWriter quantities = new CSVWriter(new FileWriter("runs/supplychai/forcedmonopolistTestQuantities.csv"));
            ProducersStatCollector collector2 = new ProducersStatCollector(macroII,OneLinkSupplyChainScenario.INPUT_GOOD,prices,quantities);
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
                        Double inventory = scenario1.strategy2.getDepartment().getLastObservedDay() > 0 ?
                                scenario1.strategy2.getDepartment().getLatestObservation(SalesDataType.HOW_MANY_TO_SELL) : 0
                                ;
                        writer2.writeNext(new String[]{String.valueOf(
                                macroII.getMarket(OneLinkSupplyChainScenario.INPUT_GOOD).getBestBuyPrice()),
                                String.valueOf(scenario1.strategy2.getTargetInventory()),
                                String.valueOf(inventory)});
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
