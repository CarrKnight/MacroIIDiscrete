/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.scenario;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.production.Blueprint;
import agents.firm.production.control.maximizer.algorithms.marginalMaximizers.MarginalMaximizer;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.prediction.ErrorCorrectingPurchasePredictor;
import agents.firm.purchases.prediction.FixedIncreasePurchasesPredictor;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.prediction.ErrorCorrectingSalesPredictor;
import agents.firm.sales.prediction.FixedDecreaseSalesPredictor;
import agents.firm.sales.prediction.SalesPredictor;
import agents.firm.sales.pricing.AskPricingStrategy;
import com.google.common.base.Preconditions;
import financial.market.Market;
import javafx.collections.ObservableSet;
import model.MacroII;
import model.utilities.logs.LogLevel;
import model.utilities.logs.LogToFile;
import model.utilities.stats.collectors.DailyStatCollector;
import model.utilities.stats.collectors.enums.MarketDataType;
import model.utilities.stats.collectors.enums.PurchasesDataType;
import model.utilities.stats.collectors.enums.SalesDataType;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.Function;

import static model.experiments.tuningRuns.MarginalMaximizerPIDTuning.printProgressBar;

/**
 * <h4>Description</h4>
 * <p/> A simple struct to return when a test is done in multithreading!
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-08-05
 * @see
 */
public class OneLinkSupplyChainResult {

    final private double beefPrice;

    final private double foodPrice;

    final private double quantity;

    final private MacroII macroII;

    public OneLinkSupplyChainResult(double beefPrice, double foodPrice, double quantity, MacroII macroII) {
        this.beefPrice = beefPrice;
        this.foodPrice = foodPrice;
        this.quantity = quantity;
        this.macroII = macroII;
    }

    public static OneLinkSupplyChainResult beefMonopolistOneRun(long random, float divideMonopolistGainsByThis, int monopolistSpeed,
                                                                final boolean beefLearned, final boolean foodLearned)
    {
        return beefMonopolistOneRun(random, divideMonopolistGainsByThis, monopolistSpeed, beefLearned, foodLearned,null,null,null);
    }

    public static OneLinkSupplyChainResult beefMonopolistOneRun(long random, float divideMonopolistGainsByThis, int monopolistSpeed,
                                                                final boolean beefLearned, final boolean foodLearned,
                                                                Function<SalesDepartment, AskPricingStrategy> woodPricingFactory,
                                                                Function<SalesDepartment, AskPricingStrategy> furniturePricingFactory,
                                                                File csvFileToWrite, File logFileToWrite, Path regressionLogToWrite)
    {
        final MacroII macroII = new MacroII(random);
        final OneLinkSupplyChainScenarioWithCheatingBuyingPrice scenario1 = new OneLinkSupplyChainScenarioWithCheatingBuyingPrice(macroII){

            @Override
            protected void buildBeefSalesPredictor(SalesDepartment dept) {
                if(beefLearned){
                    FixedDecreaseSalesPredictor predictor  = SalesPredictor.Factory.
                            newSalesPredictor(FixedDecreaseSalesPredictor.class, dept);
                    predictor.setDecrementDelta(2);
                    dept.setPredictorStrategy(predictor);
                }
                else{
                    assert dept.getPredictorStrategy() instanceof ErrorCorrectingSalesPredictor; //assuming here nothing has been changed and we are still dealing with recursive sale predictors
                    try {
                        if(regressionLogToWrite!=null)
                            ((ErrorCorrectingSalesPredictor)dept.getPredictorStrategy()).setDebugWriter(regressionLogToWrite);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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

        if(woodPricingFactory!=null)
            scenario1.setBeefPricingFactory(woodPricingFactory);
        if(furniturePricingFactory!=null)
            scenario1.setFoodPricingFactory(furniturePricingFactory);


        //competition!
        scenario1.setNumberOfBeefProducers(1);
        scenario1.setBeefTargetInventory(100);
        scenario1.setFoodTargetInventory(100);
        scenario1.setNumberOfFoodProducers(5);

        scenario1.setDivideProportionalGainByThis(divideMonopolistGainsByThis);
        scenario1.setDivideIntegrativeGainByThis(divideMonopolistGainsByThis);
        //no delay
        scenario1.setBeefPricingSpeed(monopolistSpeed);



        //add csv writer if needed
        if(csvFileToWrite != null)
            DailyStatCollector.addDailyStatCollectorToModel(csvFileToWrite, macroII);



        macroII.setScenario(scenario1);
        macroII.start();
        macroII.schedule.step(macroII);

        if(logFileToWrite!= null)
            scenario1.getMarkets().get(OneLinkSupplyChainScenario.INPUT_GOOD).getSellers().iterator().next().
                    addLogEventListener(new LogToFile(logFileToWrite, LogLevel.DEBUG,macroII));


        while(macroII.schedule.getTime()<14000)
        {
            macroII.schedule.step(macroII);
            
        }


        SummaryStatistics averageFoodPrice = new SummaryStatistics();
        SummaryStatistics averageBeefProduced = new SummaryStatistics();
        SummaryStatistics averageBeefPrice= new SummaryStatistics();
        SummaryStatistics averageSalesSlope= new SummaryStatistics();
        SummaryStatistics averageHrSlope= new SummaryStatistics();
        final Firm monopolist = (Firm) scenario1.getMarkets().get(OneLinkSupplyChainScenario.INPUT_GOOD).getSellers().iterator().next();
        for(int j=0; j< 1000; j++)
        {
            //make the model run one more day:
            macroII.schedule.step(macroII);
            

            averageFoodPrice.addValue(macroII.getMarket(OneLinkSupplyChainScenario.OUTPUT_GOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE));
            averageBeefProduced.addValue(macroII.getMarket(OneLinkSupplyChainScenario.INPUT_GOOD).getYesterdayVolume());
            averageBeefPrice.addValue(macroII.getMarket(OneLinkSupplyChainScenario.INPUT_GOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE));
            averageSalesSlope.addValue(monopolist.getSalesDepartment(OneLinkSupplyChainScenario.INPUT_GOOD).getLatestObservation(SalesDataType.PREDICTED_DEMAND_SLOPE));
            averageHrSlope.addValue(monopolist.getHRs().iterator().next().getLatestObservation(PurchasesDataType.PREDICTED_SUPPLY_SLOPE));
        }


        System.out.println("seed: " + random);
        System.out.println("beef price: " +averageBeefPrice.getMean() );
        System.out.println("food price: " + averageFoodPrice.getMean() );
        System.out.println("produced: " + averageBeefProduced.getMean() );
        extractAndPrintSlopesOfBeefSellers(macroII);
        System.out.println();

        macroII.finish();

        return new OneLinkSupplyChainResult(averageBeefPrice.getMean(),
                averageFoodPrice.getMean(), averageBeefProduced.getMean(), macroII);

    }

    public static OneLinkSupplyChainResult beefMonopolistOneRun(long random, float divideMonopolistGainsByThis, int monopolistSpeed,
                                                                final boolean beefLearned, final boolean foodLearned,
                                                                File csvFileToWrite, File logFileToWrite, Path regressionLogToWrite) {

        return beefMonopolistOneRun(random, divideMonopolistGainsByThis, monopolistSpeed, beefLearned, foodLearned,null,null,
                csvFileToWrite,logFileToWrite,regressionLogToWrite);
    }

    public static OneLinkSupplyChainResult beefMonopolistFixedProductionsOneRun(long seed,
                                                                                float divideMonopolistGainsByThis, int monopolistSpeed, final boolean foodLearned,
                                                                                File csvFileToWrite) {
        final MacroII macroII = new MacroII(seed);
        final OneLinkSupplyChainScenarioCheatingBuyPriceAndForcedMonopolist scenario1 = new OneLinkSupplyChainScenarioCheatingBuyPriceAndForcedMonopolist(macroII, OneLinkSupplyChainScenario.INPUT_GOOD){
            @Override
            public void buildFoodPurchasesPredictor(PurchasesDepartment department) {
                if(foodLearned)
                    department.setPredictor(new FixedIncreasePurchasesPredictor(0));

            }

            @Override
            protected SalesDepartment createSalesDepartment(Firm firm, Market goodmarket) {
                SalesDepartment department = super.createSalesDepartment(firm, goodmarket);    //To change body of overridden methods use File | Settings | File Templates.
                if(foodLearned && goodmarket.getGoodType().equals(OneLinkSupplyChainScenario.OUTPUT_GOOD))
                    department.setPredictorStrategy(new FixedDecreaseSalesPredictor(0));
                return department;
            }

            @Override
            protected HumanResources createPlant(Blueprint blueprint, Firm firm, Market laborMarket) {
                HumanResources hr = super.createPlant(blueprint, firm, laborMarket);    //To change body of overridden methods use File | Settings | File Templates.
                if(foodLearned && !blueprint.getOutputs().containsKey(OneLinkSupplyChainScenario.INPUT_GOOD))
                    hr.setPredictor(new FixedIncreasePurchasesPredictor(0));
                return hr;
            }

        };
        scenario1.setControlType(MarginalMaximizer.class);
        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        //use standard PID parameters
        scenario1.setDivideProportionalGainByThis(divideMonopolistGainsByThis);
        scenario1.setDivideIntegrativeGainByThis(divideMonopolistGainsByThis);
        //100 days delay
        scenario1.setBeefPricingSpeed(monopolistSpeed);
        //no need for filter with the cheating price
        scenario1.setBeefPriceFilterer(null);
        scenario1.setBeefTargetInventory(100);
        scenario1.setFoodTargetInventory(100);
        //add csv writer if needed
        if(csvFileToWrite != null)
            DailyStatCollector.addDailyStatCollectorToModel(csvFileToWrite,macroII);


        macroII.setScenario(scenario1);
        macroII.start();


        SummaryStatistics averageFoodPrice = new SummaryStatistics();
        SummaryStatistics averageBeefPrice = new SummaryStatistics();
        SummaryStatistics averageBeefTraded = new SummaryStatistics();
        while(macroII.schedule.getTime()<15000)
        {
            macroII.schedule.step(macroII);
            
            if(macroII.schedule.getTime() >= 14500)
            {
                averageFoodPrice.addValue(macroII.getMarket(OneLinkSupplyChainScenario.OUTPUT_GOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE));
                averageBeefPrice.addValue(macroII.getMarket(OneLinkSupplyChainScenario.INPUT_GOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE));
                averageBeefTraded.addValue(macroII.getMarket(OneLinkSupplyChainScenario.INPUT_GOOD).getYesterdayVolume());

            }
        }

        macroII.finish();

        System.out.println("done with price: " +averageBeefPrice.getMean() + ", and standard deviation : " + averageBeefPrice.getStandardDeviation() );
        System.out.println("seed: " + macroII.seed());
        System.out.println();
        //the beef price is in the ballpark


        return new OneLinkSupplyChainResult(averageBeefPrice.getMean(),
                averageFoodPrice.getMean(),averageBeefTraded.getMean(), macroII);
    }

    public static OneLinkSupplyChainResult everybodyLearnedCompetitivePIDRun(long random, final float dividePIByThis, final int beefPricingSpeed,
                                                                             File csvFileToWrite) {
        final MacroII macroII = new MacroII(random);
        final OneLinkSupplyChainScenarioWithCheatingBuyingPrice scenario1 = new OneLinkSupplyChainScenarioWithCheatingBuyingPrice(macroII){

            @Override
            protected void buildBeefSalesPredictor(SalesDepartment dept) {
                FixedDecreaseSalesPredictor predictor  = SalesPredictor.Factory.newSalesPredictor(FixedDecreaseSalesPredictor.class, dept);
                predictor.setDecrementDelta(0);
                dept.setPredictorStrategy(predictor);
            }



            @Override
            public void buildFoodPurchasesPredictor(PurchasesDepartment department) {
                department.setPredictor(new FixedIncreasePurchasesPredictor(0));

            }

            @Override
            protected SalesDepartment createSalesDepartment(Firm firm, Market goodmarket) {
                SalesDepartment department = super.createSalesDepartment(firm, goodmarket);
                if(goodmarket.getGoodType().equals(OneLinkSupplyChainScenario.OUTPUT_GOOD))
                    department.setPredictorStrategy(new FixedDecreaseSalesPredictor(0));
                return department;
            }

            @Override
            protected HumanResources createPlant(Blueprint blueprint, Firm firm, Market laborMarket) {
                HumanResources hr = super.createPlant(blueprint, firm, laborMarket);
                hr.setPredictor(new FixedIncreasePurchasesPredictor(0));
                return hr;
            }
        };

        scenario1.setControlType(MarginalMaximizer.class);        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        scenario1.setBeefPriceFilterer(null);


        //competition!
        scenario1.setNumberOfBeefProducers(5);
        scenario1.setNumberOfFoodProducers(5);


        scenario1.setDivideProportionalGainByThis(dividePIByThis);
        scenario1.setDivideIntegrativeGainByThis(dividePIByThis);
        //no delay
        scenario1.setBeefPricingSpeed(beefPricingSpeed);


        //add csv writer if needed
        if(csvFileToWrite != null)
            DailyStatCollector.addDailyStatCollectorToModel(csvFileToWrite,macroII);



        macroII.setScenario(scenario1);
        macroII.start();


        while(macroII.schedule.getTime()<14000)
        {
            macroII.schedule.step(macroII);
            
        }


        //I used to assert this:
        //Assert.assertEquals(macroII.getMarket(OneLinkSupplyChainScenario.OUTPUT_GOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE),85l,6l );
        //but that's too hard because while on average the price hovers there, competition is noisy. Sometimes a lot.
        //so what I did was to attach a daily stat collector and then check the average of the last 10 prices
        SummaryStatistics averageFoodPrice = new SummaryStatistics();
        SummaryStatistics averageBeefProduced = new SummaryStatistics();
        SummaryStatistics averageBeefPrice = new SummaryStatistics();
        for(int j=0; j< 1000; j++)
        {
            //make the model run one more day:
            macroII.schedule.step(macroII);
            averageFoodPrice.addValue(macroII.getMarket(OneLinkSupplyChainScenario.OUTPUT_GOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE));
            averageBeefProduced.addValue(macroII.getMarket(OneLinkSupplyChainScenario.INPUT_GOOD).countTodayProductionByRegisteredSellers());
            averageBeefPrice.addValue(macroII.getMarket(OneLinkSupplyChainScenario.INPUT_GOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE));
        }

        System.out.println("beef price: " +averageBeefPrice.getMean() );
        System.out.println("food price: " +averageFoodPrice.getMean() );
        System.out.println("produced: " +averageBeefProduced.getMean() );
        extractAndPrintSlopesOfBeefSellers(macroII);
        System.out.println();
        macroII.finish();


        return new OneLinkSupplyChainResult(averageBeefPrice.getMean(),averageFoodPrice.getMean(),averageBeefProduced.getMean(), macroII);

    }

    public static OneLinkSupplyChainResult everybodyLearningCompetitiveSlowPIDRun(long random) {
        final MacroII macroII = new MacroII(random);
        final OneLinkSupplyChainScenarioWithCheatingBuyingPrice scenario1 = new OneLinkSupplyChainScenarioWithCheatingBuyingPrice(macroII);

        scenario1.setControlType(MarginalMaximizer.class);        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        scenario1.setBeefPriceFilterer(null);


        //competition!
        scenario1.setNumberOfBeefProducers(5);
        scenario1.setNumberOfFoodProducers(5);

        scenario1.setDivideProportionalGainByThis(100f);
        scenario1.setDivideIntegrativeGainByThis(100f);
        //no delay
        scenario1.setBeefPricingSpeed(0);


        macroII.setScenario(scenario1);
        macroII.start();


        while(macroII.schedule.getTime()<14000)
        {
            macroII.schedule.step(macroII);
            
        }


        //I used to assert this:
        //Assert.assertEquals(macroII.getMarket(OneLinkSupplyChainScenario.OUTPUT_GOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE),85l,6l );
        //but that's too hard because while on average the price hovers there, competition is noisy. Sometimes a lot.
        //so what I did was to attach a daily stat collector and then check the average of the last 10 prices
        float averageFoodPrice = 0;
        float averageBeefProduced = 0;
        float averageBeefPrice=0;

        for(int j=0; j< 1000; j++)
        {
            //make the model run one more day:
            macroII.schedule.step(macroII);
            averageFoodPrice += macroII.getMarket(OneLinkSupplyChainScenario.OUTPUT_GOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE);
            averageBeefProduced+= macroII.getMarket(OneLinkSupplyChainScenario.INPUT_GOOD).countTodayProductionByRegisteredSellers();
            averageBeefPrice+= macroII.getMarket(OneLinkSupplyChainScenario.INPUT_GOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE);

        }

        System.out.println("beef price: " +averageBeefPrice/1000f );
        System.out.println("food price: " +averageFoodPrice/1000f );
        System.out.println("produced: " +averageBeefProduced/1000f );
        extractAndPrintSlopesOfBeefSellers(macroII);

        System.out.println();
        macroII.finish();


        return new OneLinkSupplyChainResult(averageBeefPrice/1000f,averageFoodPrice/1000f,averageBeefProduced/1000f, macroII);

    }




    public static OneLinkSupplyChainResult foodMonopolistOneRun(long random, float divideMonopolistGainsByThis, int beefSpeed,
                                                                final boolean beefLearned, final boolean foodLearned,
                                                                File csvFileToWrite, Path regressionCSV) {
        final MacroII macroII = new MacroII(random);
        final OneLinkSupplyChainScenarioWithCheatingBuyingPrice scenario1 = new OneLinkSupplyChainScenarioWithCheatingBuyingPrice(macroII){

            @Override
            protected void buildBeefSalesPredictor(SalesDepartment dept) {
                if(beefLearned){
                    FixedDecreaseSalesPredictor predictor  = SalesPredictor.Factory.
                            newSalesPredictor(FixedDecreaseSalesPredictor.class, dept);
                    predictor.setDecrementDelta(0);
                    dept.setPredictorStrategy(predictor);
                }
                else{

                }
            }



            @Override
            public void buildFoodPurchasesPredictor(PurchasesDepartment department) {
                if(foodLearned)
                    department.setPredictor(new FixedIncreasePurchasesPredictor(1));
                else{
                    final ErrorCorrectingPurchasePredictor predictor = new ErrorCorrectingPurchasePredictor(macroII, department);
                    try {
                        if(regressionCSV != null)
                            predictor.setDebugWriter(regressionCSV);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    department.setPredictor(predictor);

                }

            }

            @Override
            protected SalesDepartment createSalesDepartment(Firm firm, Market goodmarket) {
                SalesDepartment department = super.createSalesDepartment(firm, goodmarket);
                if(goodmarket.getGoodType().equals(OneLinkSupplyChainScenario.OUTPUT_GOOD))  {
                    if(foodLearned)
                        department.setPredictorStrategy(new FixedDecreaseSalesPredictor(1));
                }
                return department;
            }

            @Override
            protected HumanResources createPlant(Blueprint blueprint, Firm firm, Market laborMarket) {
                HumanResources hr = super.createPlant(blueprint, firm, laborMarket);
                if(blueprint.getOutputs().containsKey(OneLinkSupplyChainScenario.INPUT_GOOD))
                {
                    if(beefLearned){
                        hr.setPredictor(new FixedIncreasePurchasesPredictor(0));
                    }
                }
                if(blueprint.getOutputs().containsKey(OneLinkSupplyChainScenario.OUTPUT_GOOD))
                {
                    if(foodLearned)
                        hr.setPredictor(new FixedIncreasePurchasesPredictor(1));


                }
                return hr;
            }
        };
        scenario1.setControlType(MarginalMaximizer.class);
        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        scenario1.setBeefPriceFilterer(null);

        //competition!
        scenario1.setNumberOfBeefProducers(5);
        scenario1.setBeefTargetInventory(100);
        scenario1.setNumberOfFoodProducers(1);
        scenario1.setFoodTargetInventory(100);

        scenario1.setDivideProportionalGainByThis(divideMonopolistGainsByThis);
        scenario1.setDivideIntegrativeGainByThis(divideMonopolistGainsByThis);
        //no delay
        scenario1.setBeefPricingSpeed(beefSpeed);



        //add csv writer if needed
        if(csvFileToWrite != null)
            DailyStatCollector.addDailyStatCollectorToModel(csvFileToWrite, macroII);



        macroII.setScenario(scenario1);
        macroII.start();


        while(macroII.schedule.getTime()<14000)
        {
            macroII.schedule.step(macroII);
            
        }


        SummaryStatistics averageFoodPrice = new SummaryStatistics();
        SummaryStatistics averageBeefProduced = new SummaryStatistics();
        SummaryStatistics averageBeefPrice= new SummaryStatistics();
        for(int j=0; j< 1000; j++)
        {
            //make the model run one more day:
            macroII.schedule.step(macroII);
            

            averageFoodPrice.addValue(macroII.getMarket(OneLinkSupplyChainScenario.OUTPUT_GOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE));
            averageBeefProduced.addValue(macroII.getMarket(OneLinkSupplyChainScenario.INPUT_GOOD).getYesterdayVolume());
            averageBeefPrice.addValue(macroII.getMarket(OneLinkSupplyChainScenario.INPUT_GOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE));
        }


        System.out.println("seed: " + random);
        System.out.println("beef price: " +averageBeefPrice.getMean() );
        System.out.println("food price: " + averageFoodPrice.getMean() );
        System.out.println("produced: " + averageBeefProduced.getMean() );
        extractAndPrintSlopesOfBeefSellers(macroII);

        System.out.println();

        ((Firm)(macroII.getMarket(OneLinkSupplyChainScenario.OUTPUT_GOOD).getSellers().iterator().next())).getPurchaseDepartment(OneLinkSupplyChainScenario.INPUT_GOOD).
                getData().writeToCSVFile(
                Paths.get("runs","purchases.csv").toFile());
        macroII.finish();


        return new OneLinkSupplyChainResult(averageBeefPrice.getMean(),
                averageFoodPrice.getMean(), averageBeefProduced.getMean(), macroII);


    }

    public static OneLinkSupplyChainResult everybodyLearningCompetitiveStickyPIDRun(long random, float timidity, int stickiness) {
        final MacroII macroII = new MacroII(random);
        final OneLinkSupplyChainScenarioWithCheatingBuyingPrice scenario1 = new OneLinkSupplyChainScenarioWithCheatingBuyingPrice(macroII);

        scenario1.setControlType(MarginalMaximizer.class);        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        scenario1.setBeefPriceFilterer(null);


        //competition!
        scenario1.setNumberOfBeefProducers(5);
        scenario1.setNumberOfFoodProducers(5);

        scenario1.setDivideProportionalGainByThis(timidity);
        scenario1.setDivideIntegrativeGainByThis(timidity);
        //no delay
        scenario1.setBeefPricingSpeed(stickiness);


        macroII.setScenario(scenario1);
        macroII.start();


        while(macroII.schedule.getTime()<9000)
        {
            macroII.schedule.step(macroII);
            printProgressBar(9001,(int)macroII.schedule.getSteps(),100);
        }


        //I used to assert this:
        //Assert.assertEquals(macroII.getMarket(OneLinkSupplyChainScenario.OUTPUT_GOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE),85l,6l );
        //but that's too hard because while on average the price hovers there, competition is noisy. Sometimes a lot.
        //so what I did was to attach a daily stat collector and then check the average of the last 10 prices
        float averageFoodPrice = 0;
        float averageBeefProduced = 0;
        float averageBeefPrice=0;
        for(int j=0; j< 1000; j++)
        {
            //make the model run one more day:
            macroII.schedule.step(macroII);
            averageFoodPrice += macroII.getMarket(OneLinkSupplyChainScenario.OUTPUT_GOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE);
            averageBeefProduced+= macroII.getMarket(OneLinkSupplyChainScenario.INPUT_GOOD).countTodayProductionByRegisteredSellers();
            averageBeefPrice+= macroII.getMarket(OneLinkSupplyChainScenario.INPUT_GOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE);
        }

        System.out.println("beef price: " +averageBeefPrice/1000f );
        System.out.println("food price: " +averageFoodPrice/1000f );
        System.out.println("produced: " +averageBeefProduced/1000f );
        extractAndPrintSlopesOfBeefSellers(macroII);

        System.out.println();
        macroII.finish();


        return new OneLinkSupplyChainResult(averageBeefPrice/1000f,averageFoodPrice/1000f,averageBeefProduced/1000f, macroII);

    }


    public static void extractAndPrintSlopesOfBeefSellers(MacroII model)
    {
        Preconditions.checkState(OneLinkSupplyChainScenario.class.isAssignableFrom(model.getScenario().getClass()));
        //wood
        final ObservableSet<EconomicAgent> lumberMills =
                model.getMarket(OneLinkSupplyChainScenario.INPUT_GOOD).getSellers();
        double salesSlopes[] = new double[lumberMills.size()];
        double hrSlopes[] = new double[lumberMills.size()];
        int i=0;
        for(EconomicAgent e : lumberMills){
            Firm f = (Firm)e;
            final double hrSlope = f.getHRs().iterator().next().getLatestObservation(PurchasesDataType.PREDICTED_SUPPLY_SLOPE);
            hrSlopes[i] = hrSlope;
            final double saleSlope = f.getSalesDepartment(OneLinkSupplyChainScenario.INPUT_GOOD).getLatestObservation(SalesDataType.PREDICTED_DEMAND_SLOPE);
            salesSlopes[i] = saleSlope;
            i++;
        }
        System.out.println("wood slopes");
        System.out.println("sales: " + Arrays.toString(salesSlopes));
        System.out.println("hr: " + Arrays.toString(hrSlopes));

        //furniture
        final ObservableSet<EconomicAgent> furniturePlants =
                model.getMarket(OneLinkSupplyChainScenario.INPUT_GOOD).getBuyers();
        salesSlopes = new double[furniturePlants.size()];
        hrSlopes = new double[furniturePlants.size()];
        double pdSlopes[] = new double[furniturePlants.size()];

        i=0;
        for(EconomicAgent e : furniturePlants){
            Firm f = (Firm)e;
            final double hrSlope = f.getHRs().iterator().next().getLatestObservation(PurchasesDataType.PREDICTED_SUPPLY_SLOPE);
            hrSlopes[i] = hrSlope;
            final double saleSlope = f.getSalesDepartment(OneLinkSupplyChainScenario.OUTPUT_GOOD).getLatestObservation(SalesDataType.PREDICTED_DEMAND_SLOPE);
            salesSlopes[i] = saleSlope;
            final double pdSlope = f.getPurchaseDepartment(OneLinkSupplyChainScenario.INPUT_GOOD).getLatestObservation(
                    PurchasesDataType.PREDICTED_SUPPLY_SLOPE);
            pdSlopes[i] = pdSlope;
            i++;
        }
        System.out.println("furniture slopes");
        System.out.println("sales: " + Arrays.toString(salesSlopes));
        System.out.println("hr: " + Arrays.toString(hrSlopes));
        System.out.println("pd: " + Arrays.toString(pdSlopes));
    }


    public double getBeefPrice() {
        return beefPrice;
    }

    public double getFoodPrice() {
        return foodPrice;
    }

    public double getQuantity() {
        return quantity;
    }

    public MacroII getMacroII() {
        return macroII;
    }
}
