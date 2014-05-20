/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.experiments;

import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.production.Blueprint;
import agents.firm.production.control.maximizer.algorithms.marginalMaximizers.MarginalMaximizer;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.prediction.FixedIncreasePurchasesPredictor;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.prediction.FixedDecreaseSalesPredictor;
import agents.firm.sales.prediction.RecursiveSalePredictor;
import agents.firm.sales.prediction.SalesPredictor;
import au.com.bytecode.opencsv.CSVWriter;
import financial.market.Market;
import model.MacroII;
import model.scenario.OneLinkSupplyChainScenario;
import model.scenario.OneLinkSupplyChainScenarioWithCheatingBuyingPrice;
import model.utilities.stats.collectors.DailyStatCollector;
import model.utilities.stats.collectors.enums.MarketDataType;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

import static model.experiments.tuningRuns.MarginalMaximizerPIDTuning.printProgressBar;

/**
 * <h4>Description</h4>
 * <p/>
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-01-21
 * @see
 */
public class LearningSupplyChainExperiment {


    public static void main(String[] args){
        monopolist(true, false,-4874356916490738738l);
        //  beefLearned();
    }

    public static void monopolist(final boolean beefLearned, final boolean foodLearned, long seed)
    {
        final MacroII macroII = new MacroII(seed);

        final OneLinkSupplyChainScenarioWithCheatingBuyingPrice scenario1= new OneLinkSupplyChainScenarioWithCheatingBuyingPrice(macroII){


            @Override
            protected void buildBeefSalesPredictor(SalesDepartment dept) {
                if(beefLearned)
                {
                    FixedDecreaseSalesPredictor predictor  = SalesPredictor.Factory.
                            newSalesPredictor(FixedDecreaseSalesPredictor.class, dept);
                    predictor.setDecrementDelta(2);
                    dept.setPredictorStrategy(predictor);
                }
                else{
                    assert dept.getPredictorStrategy() instanceof RecursiveSalePredictor; //assuming here nothing has been changed and we are still dealing with recursive sale predictors
                    dept.setPredictorStrategy(new RecursiveSalePredictor(model,dept,500));
                }
            }



            @Override
            public void buildFoodPurchasesPredictor(PurchasesDepartment department) {
                if(foodLearned)
                    department.setPredictor(new FixedIncreasePurchasesPredictor(0));

            }

            @Override
            protected SalesDepartment createSalesDepartment(Firm firm, Market goodmarket) {
                SalesDepartment department = super.createSalesDepartment(firm, goodmarket);
                if(goodmarket.getGoodType().equals(OneLinkSupplyChainScenario.OUTPUT_GOOD))  {
                    if(foodLearned)
                        department.setPredictorStrategy(new FixedDecreaseSalesPredictor(0));
                }
                return department;
            }

            @Override
            protected HumanResources createPlant(Blueprint blueprint, Firm firm, Market laborMarket) {
                HumanResources hr = super.createPlant(blueprint, firm, laborMarket);
                if(blueprint.getOutputs().containsKey(OneLinkSupplyChainScenario.INPUT_GOOD))
                {
                    if(beefLearned){
                        hr.setPredictor(new FixedIncreasePurchasesPredictor(1));
                    }
                }
                if(blueprint.getOutputs().containsKey(OneLinkSupplyChainScenario.OUTPUT_GOOD))
                {
                    if(foodLearned)
                        hr.setPredictor(new FixedIncreasePurchasesPredictor(0));
                }
                return hr;
            }

        };

        scenario1.setControlType(MarginalMaximizer.class);
        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        scenario1.setBeefPriceFilterer(null);

        //competition!
        scenario1.setNumberOfBeefProducers(1);
        scenario1.setNumberOfFoodProducers(5);

        scenario1.setDivideProportionalGainByThis(100f);
        scenario1.setDivideIntegrativeGainByThis(100f);
        //no delay
        scenario1.setBeefPricingSpeed(0);
        scenario1.setBeefTargetInventory(1000);

        try {
            File toWriteTo = Paths.get("runs","supplychai","beefshouldlearn.csv").toFile();
            CSVWriter writer = new CSVWriter(new FileWriter( toWriteTo));
            DailyStatCollector collector = new DailyStatCollector(macroII,writer);
            collector.start();


        } catch (IOException e) {
            System.err.println("failed to create the file!");
        }



        macroII.setScenario(scenario1);
        macroII.start();


        while(macroII.schedule.getTime()<14000)
        {
            macroII.schedule.step(macroII);
            printProgressBar(14001,(int)macroII.schedule.getSteps(),100);
        }


        SummaryStatistics averageFoodPrice = new SummaryStatistics();
        SummaryStatistics averageBeefProduced = new SummaryStatistics();
        SummaryStatistics averageBeefPrice=new SummaryStatistics();
        for(int j=0; j< 1000; j++)
        {
            //make the model run one more day:
            macroII.schedule.step(macroII);
            averageFoodPrice.addValue(macroII.getMarket(OneLinkSupplyChainScenario.OUTPUT_GOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE));
            averageBeefProduced.addValue(macroII.getMarket(OneLinkSupplyChainScenario.INPUT_GOOD).getYesterdayVolume());
            averageBeefPrice.addValue(macroII.getMarket(OneLinkSupplyChainScenario.INPUT_GOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE));
        }



        ((Firm)macroII.getMarket(OneLinkSupplyChainScenario.OUTPUT_GOOD).getSellers().iterator().next()).getPurchaseDepartment(OneLinkSupplyChainScenario.INPUT_GOOD).getPurchasesData().writeToCSVFile(Paths.get("runs","supplychai","beefBuying.csv").toFile());

        System.out.println("beef price: " +averageBeefPrice.getMean() );
        System.out.println("food price: " +averageFoodPrice.getMean() );
        System.out.println("produced: " +averageBeefProduced.getMean() );
        System.out.println(); System.out.flush();



    }


}
