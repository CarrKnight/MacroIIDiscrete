/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.experiments.stickyprices;

import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.production.Blueprint;
import agents.firm.production.control.maximizer.algorithms.marginalMaximizers.MarginalMaximizer;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.prediction.FixedIncreasePurchasesPredictor;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.prediction.FixedDecreaseSalesPredictor;
import agents.firm.sales.prediction.SalesPredictor;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.base.Preconditions;
import financial.market.Market;
import goods.GoodType;
import model.MacroII;
import model.experiments.tuningRuns.MarginalMaximizerPIDTuning;
import model.scenario.*;
import model.utilities.filters.WeightedMovingAverage;
import model.utilities.stats.collectors.DailyStatCollector;
import model.utilities.stats.collectors.enums.MarketDataType;
import model.utilities.stats.collectors.enums.SalesDataType;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.Arrays;

import static model.experiments.tuningRuns.MarginalMaximizerPIDTuning.printProgressBar;

/**
 * <h4>Description</h4>
 * <p/> All csvs needed for paper 2 are here
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-02-10
 * @see
 */
public class StickyPricesCSVPrinter {


    public static void main(String[] args) throws IOException {
        //simpleSellerRuns();
        //runWithDelay(50,0,11,true,true,0) ;
//        simpleDelaySweep(50,50,50,5);
      //  sampleMonopolistRunLearned(0,101,1,1,14,.1f,.1f,"sampleMonopolist.csv");
      //  sampleCompetitiveRunLearned(0, 101, 1, 1, 14, .1f, .1f, "sampleCompetitive.csv");
        //    woodMonopolistSweep(new BigDecimal("0.01"),new BigDecimal("1"),new BigDecimal("0.01"),new BigDecimal("1"),new BigDecimal(".01"),5);

   //     badlyOptimizedNoInventorySupplyChain(0,.08f,.16f,0,Paths.get("runs","supplychai","paper","badlyOptimized.csv").toFile());
    //    badlyOptimizedNoInventorySupplyChain(0,.08f/100f,.16f/100f, 0, Paths.get("runs","supplychai","paper","slowedBadlyOptimized.csv").toFile());
        badlyOptimizedNoInventorySupplyChain(0,.08f,.16f, 100, Paths.get("runs","supplychai","paper","stickyBadlyOptimized.csv").toFile());

        //beefMonopolistRuns()


    }

    //isolate PID seller, its issues with delays and simple ways around it
    private static void simpleSellerRuns()
    {

        runWithoutDelay();
        System.out.println("done");
        runWithDelay(10,0,1, true, false, 100);
        System.out.println("done");
        runWithDelay(20,0,1, true, false, 100);
        System.out.println("done");
        runWithDelay(20,20,1, true, false, 100);
        System.out.println("done");
        runWithDelay(20,0,10, true, false, 100);






    }

    //simple seller run without delay.
    private static void runWithoutDelay() {
        MacroII macroII = new MacroII(100);
        SimpleSellerScenario scenario = new SimpleSellerScenario(macroII);
        macroII.setScenario(scenario);

        scenario.setDemandSlope(-1);
        scenario.setDemandIntercept(101);
        scenario.setDemandShifts(false);
        scenario.setSellerStrategy(SimpleFlowSellerPID.class); // no inventory
        SimpleFlowSellerPID.flowTargetingDefault = false; //needs to be false here because inventory readings at dawn are misinformative.
        scenario.setDestroyUnsoldInventoryEachDay(true);
        scenario.setNumberOfSellers(1);
        scenario.setInflowPerSeller(50);
        scenario.setBuyerDelay(0);
        //fix the pid parameters


        DailyStatCollector.addDailyStatCollectorToModel(Paths.get("runs", "supplychai", "paper", "simpleSeller.csv").toFile(), macroII);
        macroII.start();
        final SimpleFlowSellerPID askPricingStrategy = new SimpleFlowSellerPID(scenario.getDepartments().get(0),
                .1f, .1f, 0f, 0);
        scenario.getDepartments().get(0).setAskPricingStrategy(askPricingStrategy);
        askPricingStrategy.setInitialPrice(80); //so the run is the same for all possible runs

        for(int i=0; i< 15000; i++)
        {
            macroII.schedule.step(macroII);
            MarginalMaximizerPIDTuning.printProgressBar(15000, i, 20);
        }
    }


    //simple seller with delay
    private static SimpleSellerScenario runWithDelay(int buyerDelay, int pidSpeed, float dividePIParametersByThis, boolean writeToFile, boolean randomize, int seed) {
        Preconditions.checkArgument(pidSpeed >= 0);
        Preconditions.checkArgument(dividePIParametersByThis > 0);

        MacroII macroII;
        macroII = new MacroII(seed);

        SimpleSellerScenario scenario = new SimpleSellerScenario(macroII);
        macroII.setScenario(scenario);

        scenario.setDemandSlope(-1);
        scenario.setDemandIntercept(101);
        scenario.setDemandShifts(false);
        scenario.setSellerStrategy(SimpleFlowSellerPID.class); // no inventory
        SimpleFlowSellerPID.flowTargetingDefault = false; //needs to be false here because inventory readings at dawn are misinformative.
        scenario.setDestroyUnsoldInventoryEachDay(true);
        scenario.setNumberOfSellers(1);
        scenario.setInflowPerSeller(50);
        scenario.setBuyerDelay(buyerDelay);

        macroII.start();

        //the explicit cast has to be true because we set the strategy to be so earlier
        //change the PI values if needed!
        float proportionalAndIntegralGain = .1f / dividePIParametersByThis;
        final SimpleFlowSellerPID askPricingStrategy = new SimpleFlowSellerPID(scenario.getDepartments().get(0),
                proportionalAndIntegralGain, proportionalAndIntegralGain, 0f, pidSpeed);
        if(!randomize)
            askPricingStrategy.setInitialPrice(80); //so the run is the same for all possible runs
        else
            askPricingStrategy.setInitialPrice(macroII.getRandom().nextInt(100));
        scenario.getDepartments().get(0).setAskPricingStrategy(askPricingStrategy);








        for(int i=0; i< 15000; i++)
        {
            macroII.schedule.step(macroII);
            MarginalMaximizerPIDTuning.printProgressBar(15000, i, 20);
        }
        if(writeToFile)
        {
            //i use the sales department data as it shows the "offered" price rather than just the closing one
            final String filename = pidSpeed == 0 && dividePIParametersByThis == 1 ? "simpleSeller_withDelays" + buyerDelay + ".csv" : "simpleSeller_demandDelay" + buyerDelay + "speed" + pidSpeed + "slowness" + dividePIParametersByThis + ".csv";
            scenario.getDepartments().get(0).getData().writeToCSVFile(Paths.get("runs", "supplychai", "paper", filename).toFile());
        }


        macroII.finish();

        return scenario;


    }


    //one link supply chains with monopolists
    private static void beefMonopolistRuns() {
        //print out a simple run for all to see in my beautiful paper!

        long seed = 0;

        //all learned
        OneLinkSupplyChainResult.beefMonopolistOneRun(seed, 100, 0, true, true, Paths.get("runs", "supplychai", "paper", "everybodyLearnedSlow_withInventory.csv").toFile());
        OneLinkSupplyChainResult.beefMonopolistOneRun(seed,1,100,true,true, Paths.get("runs","supplychai","paper","everybodyLearnedSticky_withInventory.csv").toFile());
        //beef learned
        OneLinkSupplyChainResult.beefMonopolistOneRun(seed,100,0,true,false, Paths.get("runs","supplychai","paper","beefLearnedSlow_withInventory.csv").toFile());
        OneLinkSupplyChainResult.beefMonopolistOneRun(seed,1,100,true,false, Paths.get("runs","supplychai","paper","beefLearnedSticky_withInventory.csv").toFile());
        //food learned
        OneLinkSupplyChainResult.beefMonopolistOneRun(seed,100,0,false,true, Paths.get("runs","supplychai","paper","foodLearnedSlow_withInventory.csv").toFile());
        OneLinkSupplyChainResult.beefMonopolistOneRun(seed,1,100,false,true, Paths.get("runs","supplychai","paper","foodLearnedSticky_withInventory.csv").toFile());
        //learning
        OneLinkSupplyChainResult.beefMonopolistOneRun(seed,100,0,false,false, Paths.get("runs","supplychai","paper","learningSlow_withInventory.csv").toFile());
        OneLinkSupplyChainResult.beefMonopolistOneRun(seed,1,100,false,false, Paths.get("runs","supplychai","paper","learningSticky_withInventory.csv").toFile());


        //non sticky
        OneLinkSupplyChainResult.beefMonopolistOneRun(seed,1,0,true,true, Paths.get("runs","supplychai","paper","nonsticky_withInventory.csv").toFile());
    }


    //go through many possible combination of delaying PID to see their effects!
    private static void simpleDelaySweep(int maxDivider,int maxSpeed, int demandDelay, int experimentsPerSetup) throws IOException {

        CSVWriter writer = new CSVWriter(new FileWriter(Paths.get("runs", "supplychai", "paper","delaySweep.csv").toFile()));
        writer.writeNext(new String[]{"speed","divider","distance","variance","success"});


        for(int divider = 1; divider< maxDivider; divider++)
        {
            //for all speeds
            for(int speed =0; speed < maxSpeed; speed++)
            {

                SummaryStatistics averageSquaredDistance  = new SummaryStatistics();
                SummaryStatistics averageVariance  = new SummaryStatistics();
                int successes = 0;


                for(int runNumber=0; runNumber<experimentsPerSetup; runNumber++ )
                {
                    float totalDistance = 0;
                    SummaryStatistics prices  = new SummaryStatistics();



                    //runNumber!
                    final SimpleSellerScenario run = runWithDelay(demandDelay, speed, divider, false, true, runNumber);

                    final double[] pricesInRun = run.getDepartments().get(0).getData().getObservationsRecordedTheseDays(SalesDataType.LAST_ASKED_PRICE, 0, 14999);
                    for(double price : pricesInRun)
                    {
                        totalDistance += Math.pow(price-51,2);
                        prices.addValue(price);
                    }

                    averageSquaredDistance.addValue(Math.sqrt(totalDistance));
                    averageVariance.addValue(prices.getVariance());
                    if(pricesInRun[pricesInRun.length-1] == 51)
                        successes++;
                }

                String[] csvLine = new String[5];
                csvLine[0] = String.valueOf(speed);
                csvLine[1] = String.valueOf(divider);
                csvLine[2] = String.valueOf(averageSquaredDistance.getMean());
                csvLine[3] = String.valueOf(averageVariance.getMean());
                csvLine[4] = String.valueOf(successes);
                writer.writeNext(csvLine);
                writer.flush();
                System.out.println(Arrays.toString(csvLine));
            }


        }


    }


    //go through many possible combination of PI parameters to show what is the "optimal" set when dealing with adapting demand
    //using big decimals because they are more precise (really pointless since then it's fed as a float, but whatever)
    private static void woodMonopolistSweep(final BigDecimal minimumP, final BigDecimal maximumP, final BigDecimal minimumI, final BigDecimal maximumI,
                                            final BigDecimal increment,final int runsPerParameterCombination) throws IOException {

        CSVWriter writer = new CSVWriter(new FileWriter(Paths.get("runs", "supplychai", "paper","monoSweep.csv").toFile()));
        writer.writeNext(new String[]{"P","I","distance","variance","success"});


        SimpleFlowSellerPID.flowTargetingDefault=true;
        BigDecimal currentP = minimumP;
        while(currentP.compareTo(maximumP) <= 0)
        {
            BigDecimal currentI = minimumI;

            while(currentI.compareTo(maximumI) <= 0)
            {


                SummaryStatistics averageSquaredDistance  = new SummaryStatistics();
                SummaryStatistics averageVariance  = new SummaryStatistics();
                int successes = 0;


                for(int run =0 ; run < runsPerParameterCombination; run++)
                {

                    //create the run
                    MacroII macroII = new MacroII(run);
                    MonopolistScenario scenario = new MonopolistScenario(macroII);
                    macroII.setScenario(scenario);
                    //set the demand
                    scenario.setDemandIntercept(102);
                    scenario.setDemandSlope(2);
                    scenario.setDailyWageSlope(1);
                    scenario.setDailyWageIntercept(0);
                    scenario.setAskPricingStrategy(SimpleFlowSellerPID.class);

                    //start it and have one step
                    macroII.start();
                    macroII.schedule.step(macroII);

                    //now set the right parameters
                    final SalesDepartment salesDepartment = scenario.getMonopolist().getSalesDepartment(GoodType.GENERIC);
                    final SimpleFlowSellerPID strategy = new SimpleFlowSellerPID(salesDepartment, currentP.floatValue(), currentI.floatValue(), 0f, 0);
                    salesDepartment.setAskPricingStrategy(strategy);

                    //and make it learned!
                    salesDepartment.setPredictorStrategy(new FixedDecreaseSalesPredictor(2));
                    scenario.getMonopolist().getHRs().iterator().next().setPredictor(new FixedIncreasePurchasesPredictor(1));
                    salesDepartment.setAveragedPrice(new WeightedMovingAverage<Long, Double>(2)); //doesn't really need/care about Moving averages!

                    //run the model
                    for(int i=0; i<5000; i++)
                    {
                        macroII.schedule.step(macroII);
                        MarginalMaximizerPIDTuning.printProgressBar(5000, i, 100);
                    }


                    //Model over, now compute statistics
                    float totalDistance = 0;
                    SummaryStatistics prices = new SummaryStatistics();

                    final double[] pricesInRun = salesDepartment.getData().getObservationsRecordedTheseDays(SalesDataType.LAST_ASKED_PRICE, 0, 4999);
                    for(double price : pricesInRun)
                    {
                        totalDistance += Math.pow(price-68,2);
                        prices.addValue(price);
                    }

                    averageSquaredDistance.addValue(Math.sqrt(totalDistance));
                    averageVariance.addValue(prices.getVariance());
                    if(pricesInRun[pricesInRun.length-1] == 68)
                        successes++;

                    System.out.println(salesDepartment.getLatestObservation(SalesDataType.LAST_ASKED_PRICE));

                }

                String[] csvLine = new String[5];
                csvLine[0] = currentP.toString();
                csvLine[1] = currentI.toString();
                csvLine[2] = String.valueOf(averageSquaredDistance.getMean());
                csvLine[3] = String.valueOf(averageVariance.getMean());
                csvLine[4] = String.valueOf(successes);
                writer.writeNext(csvLine);
                writer.flush();
                System.out.println(Arrays.toString(csvLine));



                currentI = currentI.add(increment).setScale(2);
                System.out.println();

            }






            currentP = currentP.add(increment).setScale(2);

        }





    }






    private static void sampleMonopolistRunLearned(int seed, int demandIntercept, int demandSlope, int wageSlope,
                                                   int dailyWageIntercept, float proportionalGain, float integralGain, String filename){



        //create the run
        MacroII macroII = new MacroII(seed);
        MonopolistScenario scenario = new MonopolistScenario(macroII);
        macroII.setScenario(scenario);
        //set the demand
        scenario.setDemandIntercept(demandIntercept);
        scenario.setDemandSlope(demandSlope);
        scenario.setDailyWageSlope(wageSlope);
        scenario.setDailyWageIntercept(dailyWageIntercept);
        scenario.setAskPricingStrategy(SimpleFlowSellerPID.class);
        scenario.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);

        //start it and have one step
        macroII.start();
        macroII.schedule.step(macroII);

        //now set the right parameters
        final SalesDepartment salesDepartment = scenario.getMonopolist().getSalesDepartment(GoodType.GENERIC);
        final SimpleFlowSellerPID strategy = new SimpleFlowSellerPID(salesDepartment, proportionalGain, integralGain, 0f, 0);
        salesDepartment.setAskPricingStrategy(strategy);
        salesDepartment.setAveragedPrice(new WeightedMovingAverage<Long, Double>(2)); //doesn't really need/care about Moving averages!

        //and make it learned!
        salesDepartment.setPredictorStrategy(new FixedDecreaseSalesPredictor(demandSlope));
        scenario.getMonopolist().getHRs().iterator().next().setPredictor(new FixedIncreasePurchasesPredictor(wageSlope));

        //run the model
        for(int i=0; i<5000; i++)
        {
            macroII.schedule.step(macroII);
            MarginalMaximizerPIDTuning.printProgressBar(5000, i, 100);
        }



        salesDepartment.getData().writeToCSVFile(Paths.get("runs", "supplychai", "paper",filename).toFile());

    }

    private static void sampleCompetitiveRunLearned(int seed, int demandIntercept, int demandSlope, int wageSlope,
                                                    int dailyWageIntercept, float proportionalGain, float integralGain, String filename){



        //create the run
        MacroII macroII = new MacroII(seed);
        TripolistScenario scenario = new TripolistScenario(macroII);
        macroII.setScenario(scenario);
        //set the demand
        scenario.setDemandIntercept(demandIntercept);
        scenario.setDemandSlope(demandSlope);
        scenario.setDailyWageSlope(wageSlope);
        scenario.setDailyWageIntercept(dailyWageIntercept);
        scenario.setAskPricingStrategy(SimpleFlowSellerPID.class);
        scenario.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);

        //start it and have one step
        macroII.start();
        macroII.schedule.step(macroII);

        //now set the right parameters
        for(final Firm firm : scenario.getCompetitors()){
            final SalesDepartment salesDepartment = firm.getSalesDepartment(GoodType.GENERIC);
            final SimpleFlowSellerPID strategy = new SimpleFlowSellerPID(salesDepartment, proportionalGain + (float)macroII.random.nextGaussian()/100f,
                    integralGain + (float)macroII.random.nextGaussian()/100f, 0f, 0); //added a bit of noise
            salesDepartment.setAskPricingStrategy(strategy);
            salesDepartment.setAveragedPrice(new WeightedMovingAverage<Long, Double>(2)); //doesn't really need/care about Moving averages!

            //all impacts are 0 because it's perfect competitive
            salesDepartment.setPredictorStrategy(new FixedDecreaseSalesPredictor(0));
            firm.getHRs().iterator().next().setPredictor(new FixedIncreasePurchasesPredictor(0));
        }


        //run the model
        for(int i=0; i<5000; i++)
        {
            macroII.schedule.step(macroII);
            MarginalMaximizerPIDTuning.printProgressBar(5000, i, 100);
        }



        macroII.getMarket(GoodType.GENERIC).getData().writeToCSVFile(Paths.get("runs", "supplychai", "paper",filename).toFile());

    }


    private static void badlyOptimizedNoInventorySupplyChain(int seed, final float proportionalGain, final float integralGain, final int speed, File csvFileToWrite)
    {
        final MacroII macroII = new MacroII(seed);
        final OneLinkSupplyChainScenarioWithCheatingBuyingPrice scenario1 = new OneLinkSupplyChainScenarioWithCheatingBuyingPrice(macroII){

            @Override
            protected void buildBeefSalesPredictor(SalesDepartment dept) {
                    FixedDecreaseSalesPredictor predictor  = SalesPredictor.Factory.
                            newSalesPredictor(FixedDecreaseSalesPredictor.class, dept);
                    predictor.setDecrementDelta(2);
                    dept.setPredictorStrategy(predictor);

            }



            @Override
            public void buildFoodPurchasesPredictor(PurchasesDepartment department) {
                    department.setPredictor(new FixedIncreasePurchasesPredictor(0));

            }

            @Override
            protected SalesDepartment createSalesDepartment(Firm firm, Market goodmarket) {
                SalesDepartment department = super.createSalesDepartment(firm, goodmarket);
                if(goodmarket.getGoodType().equals(GoodType.FOOD))  {
                        department.setPredictorStrategy(new FixedDecreaseSalesPredictor(0));
                }
                else
                {
                    final SimpleFlowSellerPID askPricingStrategy = new SimpleFlowSellerPID(department, proportionalGain, integralGain, 0, speed);
                    department.setAveragedPrice(new WeightedMovingAverage<Long, Double>(2)); // no need to MA
                    department.setAskPricingStrategy(askPricingStrategy);

                }
                return department;
            }

            @Override
            protected HumanResources createPlant(Blueprint blueprint, Firm firm, Market laborMarket) {
                HumanResources hr = super.createPlant(blueprint, firm, laborMarket);
                if(blueprint.getOutputs().containsKey(GoodType.BEEF))
                {
                        hr.setPredictor(new FixedIncreasePurchasesPredictor(1));
                }
                if(blueprint.getOutputs().containsKey(GoodType.FOOD))
                {
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


        //no delay

        //add csv writer if needed
        if(csvFileToWrite != null)
            DailyStatCollector.addDailyStatCollectorToModel(csvFileToWrite, macroII);



        macroII.setScenario(scenario1);
        macroII.start();


        while(macroII.schedule.getTime()<14000)
        {
            macroII.schedule.step(macroII);
            printProgressBar(14001,(int)macroII.schedule.getSteps(),100);
        }


        SummaryStatistics averageFoodPrice = new SummaryStatistics();
        SummaryStatistics averageBeefProduced = new SummaryStatistics();
        SummaryStatistics averageBeefPrice= new SummaryStatistics();
        for(int j=0; j< 1000; j++)
        {
            //make the model run one more day:
            macroII.schedule.step(macroII);
            averageFoodPrice.addValue(macroII.getMarket(GoodType.FOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE));
            averageBeefProduced.addValue(macroII.getMarket(GoodType.BEEF).getYesterdayVolume());
            averageBeefPrice.addValue(macroII.getMarket(GoodType.BEEF).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE));
        }


        System.out.println("seed: " + seed);
        System.out.println("beef price: " +averageBeefPrice.getMean() );
        System.out.println("food price: " + averageFoodPrice.getMean() );
        System.out.println("produced: " + averageBeefProduced.getMean() );
        System.out.println();

    }


}
