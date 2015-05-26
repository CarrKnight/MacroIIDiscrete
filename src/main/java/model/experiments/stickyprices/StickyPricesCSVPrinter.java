/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.experiments.stickyprices;

import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.production.Blueprint;
import agents.firm.production.control.PlantControl;
import agents.firm.production.control.facades.MarginalPlantControl;
import agents.firm.production.control.maximizer.PeriodicMaximizer;
import agents.firm.production.control.maximizer.WorkforceMaximizer;
import agents.firm.production.control.maximizer.algorithms.marginalMaximizers.MarginalMaximizer;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.prediction.FixedIncreasePurchasesPredictor;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.prediction.ErrorCorrectingSalesPredictor;
import agents.firm.sales.prediction.FixedDecreaseSalesPredictor;
import agents.firm.sales.prediction.RecursiveSalePredictor;
import agents.firm.sales.prediction.SalesPredictor;
import agents.firm.sales.pricing.AskPricingStrategy;
import agents.firm.sales.pricing.pid.InventoryBufferSalesControl;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import agents.firm.utilities.LastClosingPriceEcho;
import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.base.Preconditions;
import financial.market.Market;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.experiments.tuningRuns.MarginalMaximizerPIDTuning;
import model.scenario.*;
import model.utilities.logs.LogLevel;
import model.utilities.logs.LogToFile;
import model.utilities.pid.Controller;
import model.utilities.pid.PIDController;
import model.utilities.pid.decorator.MovingAverageFilterInputDecorator;
import model.utilities.pid.decorator.MovingAverageFilterOutputDecorator;
import model.utilities.pid.decorator.PIDStickinessHillClimberTuner;
import model.utilities.pid.decorator.PIDStickinessSalesTuner;
import model.utilities.stats.collectors.DailyStatCollector;
import model.utilities.stats.collectors.enums.MarketDataType;
import model.utilities.stats.collectors.enums.SalesDataType;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

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


    public static final int DEFAULT_STICKINESS =50;

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        //set defaults
        //create directory


        Files.createDirectories(Paths.get("runs", "rawdata"));






        System.out.println("SELLERS");
        System.out.println("figure 1 to 5");
        simpleSellerRuns();
        System.out.println("figure 6-7");
        simpleDelaySweep(50,50,50,5);

        System.out.println("ONE SECTOR");
        System.out.println("figure 8");
        sampleMonopolistRunLearned(0,102,1,1,14,.1f,.1f,"sampleMonopolist.csv");
        System.out.println("figure 9");


        sampleCompetitiveRunLearned(0, 102, 1, 1, 14, .1f, .1f, "sampleCompetitive.csv");


        System.out.println("figure 10");

        woodMonopolistSweep(new BigDecimal("0.00"),new BigDecimal("3"),new BigDecimal("0.00"),new BigDecimal("3"),new BigDecimal(".01"),1);



        System.out.println("SUPPLY CHAIN");
        System.out.println("figure 11");
        badlyOptimizedNoInventorySupplyChain(0,0f,2f,0,Paths.get("runs","rawdata","badlyOptimized.csv").toFile(), false);
        System.out.println("figure 12");
        badlyOptimizedNoInventorySupplyChain(0,0f,.2f,0,Paths.get("runs","rawdata","timidBadlyOptimized.csv").toFile(), false);
        System.out.println("figure 13");
        badlyOptimizedNoInventorySupplyChain(0,0f,.2f, 100, Paths.get("runs","rawdata","stickyBadlyOptimized.csv").toFile(),false);



        System.out.println("figure 14");
        woodMonopolistSupplyChainSweep();


        System.out.println("Market Structure");
        System.out.println("figure 15-16-17");


        oneHundredAllLearnedRuns(Paths.get("runs","rawdata", "learnedInventoryChain100.csv").toFile(),null
                , null);
        oneHundredAllLearnedCompetitiveRuns(Paths.get("runs","rawdata", "learnedCompetitiveInventoryChain100.csv").toFile());
        oneHundredAllLearnedFoodRuns(Paths.get("runs","rawdata", "learnedInventoryFoodChain100.csv").toFile());


        System.out.println("figure 18-19");
        oneHundredLearningMonopolist(Paths.get("runs","rawdata", "100Monopolists.csv").toFile());
        oneHundredLearningCompetitive(Paths.get("runs","rawdata", "100Competitive.csv").toFile(), 1);

        System.out.println("figure 20-21-22");
        oneHundredAllLearningRuns(Paths.get("runs","rawdata", "learningInventoryChain100.csv").toFile(),
                                  null, null);

        oneHundredAllLearningCompetitiveRuns(Paths.get("runs","rawdata", "learningCompetitiveInventoryChain100.csv").toFile());

        oneHundredAllLearningFoodRuns(Paths.get("runs","rawdata", "learningInventoryFoodChain100.csv").toFile());


          System.out.println("figure 23");
        badlyOptimizedNoInventorySupplyChain(1,0f,0.2f,0,Paths.get("runs","rawdata","tuningTrial.csv").toFile(), true);



        //added after first revision:
        //show that MAs don't really solve much
        //input MA
        runWithDelay(20, 0, 1, true, false, 100, 20, 0, "inputMADelay.csv");
        System.out.println("done");
        //output MA
        runWithDelay(20, 0, 1, true, false, 100, 0, 20, "outputMADelay.csv");
        System.out.println("done");
        //no MA
        runWithDelay(20, 0, 1, true, false, 100, 0, 0, "noMADelay.csv");
        System.out.println("done");




    }



    //isolate PID seller, its issues with delays and simple ways around it
    private static void simpleSellerRuns()
    {

        //figure 1
        runWithoutDelay();
        System.out.println("done");
        //figure 2
        runWithDelay(10,0,1, true, false, 100, 0, 0, "simpleSeller_withDelays" + 10 + ".csv");
        System.out.println("done");
        //figure 3
        runWithDelay(20,0,1, true, false, 100, 0, 0, "simpleSeller_withDelays" + 20 + ".csv");
        System.out.println("done");
        //figure 4
        runWithDelay(20,20,1, true, false, 100, 0, 0,
                     "simpleSeller_demandDelay" + 20 + "speed" + 20 + "slowness" + 1 + ".csv");
        System.out.println("done");
        //figure 5
        runWithDelay(20,0,10, true, false, 100, 0, 0,
                     "simpleSeller_demandDelay" + 20 + "speed" + 0 + "slowness" + 10 + ".csv");






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
        scenario.setDestroyUnsoldInventoryEachDay(true);
        scenario.setNumberOfSellers(1);
        scenario.setInflowPerSeller(50);
        scenario.setBuyerDelay(0);
        //fix the pid parameters


        DailyStatCollector.addDailyStatCollectorToModel(Paths.get("runs","rawdata", "simpleSeller.csv").toFile(), macroII);
        macroII.start();
        final SimpleFlowSellerPID askPricingStrategy = new SimpleFlowSellerPID(scenario.getDepartments().get(0),
                                                                               .1f, .1f, 0f, 0, scenario.getDepartments().get(0).getMarket(), scenario.getDepartments().get(0).getRandom().nextInt(100), scenario.getDepartments().get(0).getFirm().getModel());
        askPricingStrategy.setFlowTargeting(false);
        scenario.getDepartments().get(0).setAskPricingStrategy(askPricingStrategy);
        askPricingStrategy.setInitialPrice(80); //so the run is the same for all possible runs

        for(int i=0; i< 15000; i++)
        {
            macroII.schedule.step(macroII);
            MarginalMaximizerPIDTuning.printProgressBar(15000, i, 20);
        }
    }


    //simple seller with delay
    public static SimpleSellerScenario runWithDelay(
            int buyerDelay, int pidSpeed,
            float dividePIParametersByThis,
            boolean writeToFile, boolean randomize,
            int seed,
            int inputMovingAverage,
            int outputMovingAverage,
            final String filename) {
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
        scenario.setDestroyUnsoldInventoryEachDay(true);
        scenario.setNumberOfSellers(1);
        scenario.setInflowPerSeller(50);
        if(buyerDelay > 0)
            scenario.setBuyerDelay(buyerDelay);

        macroII.start();

        //the explicit cast has to be true because we set the strategy to be so earlier
        //change the PI values if needed!
        float proportionalAndIntegralGain = .1f / dividePIParametersByThis;
        final SimpleFlowSellerPID askPricingStrategy =
                new SimpleFlowSellerPID(scenario.getDepartments().get(0),
                                        proportionalAndIntegralGain,
                                        proportionalAndIntegralGain, 0f,
                                        pidSpeed, scenario.getDepartments().get(0).getMarket(),
                                        scenario.getDepartments().get(0).getRandom().nextInt(100),
                                        scenario.getDepartments().get(0).getFirm().getModel());
        askPricingStrategy.setFlowTargeting(false);
        //add filters if needed
        if(inputMovingAverage > 0)
            askPricingStrategy.decorateController(
                    pidController -> new MovingAverageFilterInputDecorator(pidController,inputMovingAverage));
        if(outputMovingAverage > 0)
            askPricingStrategy.decorateController(
                    pidController -> new MovingAverageFilterOutputDecorator(pidController,outputMovingAverage));


        //MovingAverageFilterOutputDecorator


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
            scenario.getDepartments().get(0).getData().writeToCSVFile(Paths.get("runs","rawdata", filename).toFile());
        }


        macroII.finish();

        return scenario;


    }


    //one link supply chains with monopolists
    private static void beefMonopolistRuns() {
        //print out a simple run for all to see in my beautiful paper!

        long seed = 0;

        //all learned
        OneLinkSupplyChainResult.beefMonopolistOneRun(seed, 100, 0, true, true, Paths.get("runs","rawdata", "everybodyLearnedSlow_withInventory.csv").toFile(), null, null);
        OneLinkSupplyChainResult.beefMonopolistOneRun(seed,1,100,true,true, Paths.get("runs","rawdata","everybodyLearnedSticky_withInventory.csv").toFile(), null, null);
        //beef learned
        OneLinkSupplyChainResult.beefMonopolistOneRun(seed,100,0,true,false, Paths.get("runs","rawdata","beefLearnedSlow_withInventory.csv").toFile(), null, null);
        OneLinkSupplyChainResult.beefMonopolistOneRun(seed,1,100,true,false, Paths.get("runs","rawdata","beefLearnedSticky_withInventory.csv").toFile(), null, null);
        //food learned
        OneLinkSupplyChainResult.beefMonopolistOneRun(seed,100,0,false,true, Paths.get("runs","rawdata","foodLearnedSlow_withInventory.csv").toFile(), null, null);
        OneLinkSupplyChainResult.beefMonopolistOneRun(seed,1,100,false,true, Paths.get("runs","rawdata","foodLearnedSticky_withInventory.csv").toFile(), null, null);
        //learning
        OneLinkSupplyChainResult.beefMonopolistOneRun(seed,100,0,false,false, Paths.get("runs","rawdata","learningSlow_withInventory.csv").toFile(), null, null);
        OneLinkSupplyChainResult.beefMonopolistOneRun(seed,1,100,false,false, Paths.get("runs","rawdata","learningSticky_withInventory.csv").toFile(), null, null);


        //non sticky
        OneLinkSupplyChainResult.beefMonopolistOneRun(seed,1,0,true,true, Paths.get("runs","rawdata","nonsticky_withInventory.csv").toFile(), null, null);
    }


    //go through many possible combination of delaying PID to see their effects!
    private static void simpleDelaySweep(int maxDivider,int maxSpeed, int demandDelay, int experimentsPerSetup) throws IOException {

        CSVWriter writer = new CSVWriter(new FileWriter(Paths.get("runs","rawdata","delaySweep.csv").toFile()));
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
                    final SimpleSellerScenario run = runWithDelay(demandDelay, speed, divider, false, true, runNumber,
                                                                  0, 0, speed == 0 && divider == 1 ?
                                                                          "simpleSeller_withDelays" + demandDelay + ".csv" : "simpleSeller_demandDelay" + demandDelay + "speed" + speed + "slowness" + divider + ".csv");

                    final double[] pricesInRun = run.getDepartments().get(0).getData().
                            getObservationsRecordedTheseDays(SalesDataType.LAST_ASKED_PRICE, 0, 14999);
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

        CSVWriter writer = new CSVWriter(new FileWriter(Paths.get("runs","rawdata","monoSweep.csv").toFile()));
        writer.writeNext(new String[]{"P","I","distance","variance","success"});


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
                    scenario.setWorkersToBeRehiredEveryDay(true);
                    scenario.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
                    scenario.setBuyerDelay(0);

                    //start it and have one step
                    macroII.start();
                    macroII.schedule.step(macroII);

                    //now set the right parameters
                    final SalesDepartment salesDepartment = scenario.getMonopolist().getSalesDepartment(UndifferentiatedGoodType.GENERIC);
                    final SimpleFlowSellerPID strategy = new SimpleFlowSellerPID(salesDepartment, currentP.floatValue(),
                                                                                 currentI.floatValue(), 0f, 0,
                                                                                 salesDepartment.getMarket(),
                                                                                 salesDepartment.getRandom().nextInt(100),
                                                                                 salesDepartment.getFirm().getModel());
                    //  strategy.setInitialPrice(102);
                    //start them all at the same price, otherwise you advantage the slow by being so slow initially that they end up being right later

                    salesDepartment.setAskPricingStrategy(strategy);

                    //and make it learned!
                    salesDepartment.setPredictorStrategy(new FixedDecreaseSalesPredictor(2));
                    final HumanResources hr = scenario.getMonopolist().getHRs().iterator().next();
                    hr.setPredictor(new FixedIncreasePurchasesPredictor(1));





                    float totalDistance = 0;
                    SummaryStatistics prices = new SummaryStatistics();
                    //run the model
                    double price = 0; double quantity = 0;
                    for(int i=0; i<1000; i++)
                    {
                        macroII.schedule.step(macroII);
                        price = strategy.getTargetPrice();
                        quantity = salesDepartment.getTodayInflow();
                        totalDistance +=  Math.pow(Math.min(price - (102-2*quantity), price - (102-2*quantity-1)), 2);
                        prices.addValue(price);
                    }


                    //Model over, now compute statistics



                    averageSquaredDistance.addValue(Math.sqrt(totalDistance));
                    averageVariance.addValue(prices.getVariance());
                    if(price <= 68 && price >=67 )
                        successes++;

                    //            System.out.println(salesDepartment.getLatestObservation(SalesDataType.LAST_ASKED_PRICE));
                    macroII.finish();


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
        final SalesDepartment salesDepartment = scenario.getMonopolist().getSalesDepartment(UndifferentiatedGoodType.GENERIC);
        final SimpleFlowSellerPID strategy = new SimpleFlowSellerPID(salesDepartment, proportionalGain, integralGain, 0f, 0, salesDepartment.getMarket(), salesDepartment.getRandom().nextInt(100), salesDepartment.getFirm().getModel());
        salesDepartment.setAskPricingStrategy(strategy);

        //and make it learned!
        salesDepartment.setPredictorStrategy(new FixedDecreaseSalesPredictor(demandSlope));
        scenario.getMonopolist().getHRs().iterator().next().setPredictor(new FixedIncreasePurchasesPredictor(wageSlope));

        //run the model
        for(int i=0; i<5000; i++)
        {
            macroII.schedule.step(macroII);
            MarginalMaximizerPIDTuning.printProgressBar(5000, i, 100);
        }



        salesDepartment.getData().writeToCSVFile(Paths.get("runs","rawdata",filename).toFile());

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
            final SalesDepartment salesDepartment = firm.getSalesDepartment(UndifferentiatedGoodType.GENERIC);
            final SimpleFlowSellerPID strategy = new SimpleFlowSellerPID(salesDepartment, proportionalGain + (float)macroII.random.nextGaussian()/100f,
                                                                         integralGain + (float)macroII.random.nextGaussian()/100f, 0f, 0, salesDepartment.getMarket(), salesDepartment.getRandom().nextInt(100), salesDepartment.getFirm().getModel()); //added a bit of noise
            salesDepartment.setAskPricingStrategy(strategy);

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



        macroII.getMarket(UndifferentiatedGoodType.GENERIC).getData().writeToCSVFile(Paths.get("runs","rawdata",filename).toFile());

    }



    //sweep over the period of maximization to see what happens if the frequency is too high or too low.
    private static void competitiveSweepRun(int additionalCompetitors) throws IOException {

        CSVWriter writer = new CSVWriter(new FileWriter(Paths.get("runs","rawdata","competitivePeriodSweep" + additionalCompetitors +".csv").toFile()));
        writer.writeNext(new String[]{"speed","distance","finaldistance","variance"});

        for(int speed =1; speed < 30; speed++)
        {
            SummaryStatistics distance = new SummaryStatistics();
            SummaryStatistics finalDistance = new SummaryStatistics();
            SummaryStatistics variance = new SummaryStatistics();
            for(int seed =0; seed < 50; seed++)
            {

                final double[] result = competitiveSweepRun(seed, 101, 1, 1, 14, .1f, .1f, speed, 58, null, additionalCompetitors);
                distance.addValue(result[0]);
                finalDistance.addValue(result[1]);
                variance.addValue(result[2]);

            }

            final String[] nextLine = {String.valueOf(speed), String.valueOf(distance.getMean()),
                    String.valueOf(finalDistance.getMean()),String.valueOf(variance.getMean())};
            System.out.println(Arrays.toString(nextLine));
            writer.writeNext(nextLine);
            writer.flush();
        }

        writer.close();



    }


    private static double[] competitiveSweepRun(int seed, int demandIntercept, int demandSlope, int wageSlope,
                                                int dailyWageIntercept, float proportionalGain, float integralGain, int maximizationTimePeriod,
                                                double correctCompetitivePrice,
                                                String filename, int additionalCompetitors){




        //create the run
        MacroII macroII = new MacroII(seed);
        TripolistScenario scenario = new TripolistScenario(macroII);
        macroII.setScenario(scenario);
        scenario.setAdditionalCompetitors(additionalCompetitors);
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
            final SalesDepartment salesDepartment = firm.getSalesDepartment(UndifferentiatedGoodType.GENERIC);
            final SimpleFlowSellerPID strategy = new SimpleFlowSellerPID(salesDepartment, proportionalGain + (float)macroII.random.nextGaussian()/100f,
                                                                         integralGain + (float)macroII.random.nextGaussian()/100f, 0f, 0, salesDepartment.getMarket(), salesDepartment.getRandom().nextInt(100), salesDepartment.getFirm().getModel()); //added a bit of noise
            salesDepartment.setAskPricingStrategy(strategy);

            //all impacts are 0 because it's perfect competitive
            salesDepartment.setPredictorStrategy(new FixedDecreaseSalesPredictor(0));
            firm.getHRs().iterator().next().setPredictor(new FixedIncreasePurchasesPredictor(0));
        }
        for(final PlantControl control : scenario.getMaximizers())
        {
            ((MarginalPlantControl)control).getMaximizer().setHowManyDaysBeforeEachCheck(maximizationTimePeriod);
        }

        SummaryStatistics distance = new SummaryStatistics();
        SummaryStatistics finalPrice = new SummaryStatistics();
        SummaryStatistics finalDistance = new SummaryStatistics();


        //run the model
        for(int i=0; i<4000; i++)
        {
            macroII.schedule.step(macroII);
            MarginalMaximizerPIDTuning.printProgressBar(5000, i, 100);
            final double closingPrice = macroII.getMarket(UndifferentiatedGoodType.GENERIC).getData().getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE);
            double distanceFromCorrect;
            if(Double.isNaN(closingPrice) || closingPrice < 0)
            {
                distanceFromCorrect = correctCompetitivePrice;
            }
            else
            {
                distanceFromCorrect = Math.pow(correctCompetitivePrice - closingPrice, 2);
            }
            distance.addValue(distanceFromCorrect);
        }

        //run the model
        for(int i=0; i<1000; i++)
        {
            macroII.schedule.step(macroII);
            MarginalMaximizerPIDTuning.printProgressBar(1000, i, 100);
            double closingPrice = macroII.getMarket(UndifferentiatedGoodType.GENERIC).getData().getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE);
            finalPrice.addValue(closingPrice);

            double distanceFromCorrect;
            if(Double.isNaN(closingPrice) || closingPrice < 0)
            {
                closingPrice = 0;
            }

            distanceFromCorrect = Math.pow(correctCompetitivePrice - closingPrice, 2);

            distance.addValue(distanceFromCorrect);
            finalDistance.addValue(distanceFromCorrect);
        }



        if(filename != null)
            macroII.getMarket(UndifferentiatedGoodType.GENERIC).getData().writeToCSVFile(Paths.get("runs","rawdata",filename).toFile());


        return new double[]{distance.getMean(),finalDistance.getMean(),finalPrice.getVariance()};



    }





    private static void tuningTrial(int seed, final float proportionalGain,
                                    final float integralGain, final int initialSpeed, File csvFileToWrite, final Path departmentLog){


        OneLinkSupplyChainResult.beefMonopolistOneRun(seed,1,1,true,true,new Function<SalesDepartment, AskPricingStrategy>() {
            @Override
            public AskPricingStrategy apply(SalesDepartment department) {
                if(departmentLog != null)
                    department.addLogEventListener(new LogToFile(departmentLog,
                                                                 LogLevel.TRACE,department.getModel()));
                SimpleFlowSellerPID pricer = new SimpleFlowSellerPID(department,
                                                                     proportionalGain,integralGain,0,0, department.getMarket(), department.getRandom().nextInt(100), department.getFirm().getModel());
                pricer.decorateController(new Function<PIDController, Controller>() {
                    @Override
                    public Controller apply(PIDController pidController) {
                        final PIDStickinessSalesTuner pidStickinessSalesTuner = new PIDStickinessSalesTuner(pidController,
                                                                                                            department,department.getModel());
                        pidStickinessSalesTuner.setObservationsBeforeTuning(100);
                        return pidStickinessSalesTuner;
                    }
                });
                return pricer;
            }
        },null,csvFileToWrite,null,null, 1000);
    }

    private static void badlyOptimizedNoInventorySupplyChain(int seed, final float proportionalGain,
                                                             final float integralGain, final int speed, File csvFileToWrite, final boolean tuning)
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
                if(goodmarket.getGoodType().equals(OneLinkSupplyChainScenario.OUTPUT_GOOD))  {
                    department.setPredictorStrategy(new FixedDecreaseSalesPredictor(0));
                }
                else
                {
                    final InventoryBufferSalesControl askPricingStrategy =
                            new InventoryBufferSalesControl(department,10,200,macroII, proportionalGain, integralGain, 0, macroII.getRandom());
                    askPricingStrategy.setSpeed(speed);
                    if(tuning)
                        askPricingStrategy.decorateController(pidController -> {
                            final PIDStickinessHillClimberTuner pidStickinessSalesTuner = new PIDStickinessHillClimberTuner(
                                    department.getModel(),pidController,department,1000);
                            pidStickinessSalesTuner.setStepSize(5);
                            pidStickinessSalesTuner.attachWriter(Paths.get("runs","rawdata","stickLog.csv"));
                            //                   pidStickinessSalesTuner.setObservationsBeforeTuning(5000);
                            //                    pidStickinessSalesTuner.setLogToWrite();
                            return pidStickinessSalesTuner;
                        });
                    department.setAskPricingStrategy(askPricingStrategy);

                }
                return department;
            }

            @Override
            protected PurchasesDepartment createPurchaseDepartment(Blueprint blueprint, Firm firm) {
                final PurchasesDepartment purchaseDepartment = super.createPurchaseDepartment(blueprint, firm);
                if(purchaseDepartment != null)
                    purchaseDepartment.setPriceAverager(new LastClosingPriceEcho());
                return purchaseDepartment;
            }

            @Override
            protected HumanResources createPlant(Blueprint blueprint, Firm firm, Market laborMarket) {
                HumanResources hr = super.createPlant(blueprint, firm, laborMarket);
                hr.setPriceAverager(new LastClosingPriceEcho());

                if(blueprint.getOutputs().containsKey(OneLinkSupplyChainScenario.INPUT_GOOD))
                {
                    hr.setPredictor(new FixedIncreasePurchasesPredictor(1));
                }
                if(blueprint.getOutputs().containsKey(OneLinkSupplyChainScenario.OUTPUT_GOOD))
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
            System.out.println(macroII.getMarket(OneLinkSupplyChainScenario.OUTPUT_GOOD).getLatestObservation(MarketDataType.VOLUME_PRODUCED));
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


        System.out.println("seed: " + seed);
        System.out.println("beef price: " +averageBeefPrice.getMean() );
        System.out.println("food price: " + averageFoodPrice.getMean() );
        System.out.println("produced: " + averageBeefProduced.getMean() );
        System.out.println();

    }








    private static void oneHundredAllLearnedRuns(File csvFileToWrite,
                                                 Function<SalesDepartment, AskPricingStrategy> woodPricingFactory,
                                                 Function<SalesDepartment, AskPricingStrategy> furniturePricingFactory)
            throws ExecutionException, InterruptedException, IOException {

        CSVWriter writer = new CSVWriter(new FileWriter(csvFileToWrite));
        writer.writeNext(new String[]{"production","beefPrice","foodPrice"});


        //this will take a looong time

        //run the test 5 times!
        for(long i=0; i <100; i++)
        {
            OneLinkSupplyChainResult result = OneLinkSupplyChainResult.beefMonopolistOneRun(i, 1, DEFAULT_STICKINESS, true,
                                                                                            true, woodPricingFactory, furniturePricingFactory,null,
                                                                                            null, null, 1);
            String[] resultString = new String[3];
            resultString[0]= String.valueOf(result.getQuantity());
            resultString[1]= String.valueOf(result.getBeefPrice());
            resultString[2]= String.valueOf(result.getFoodPrice());
            System.out.println(Arrays.toString(resultString));
            writer.writeNext(resultString);
            writer.flush();
        }



        writer.close();


    }



    private static void oneHundredAllLearningRuns(File csvFileToWrite,
                                                  Function<SalesDepartment, AskPricingStrategy> woodPricingFactory,
                                                  Function<SalesDepartment, AskPricingStrategy> furniturePricingFactory) throws ExecutionException, InterruptedException, IOException {

        CSVWriter writer = new CSVWriter(new FileWriter(csvFileToWrite));
        writer.writeNext(new String[]{"production","beefPrice","foodPrice"});


        //this will take a looong time

        //run the test 5 times!
        for(long i=0; i <100; i++)
        {

            OneLinkSupplyChainResult result = OneLinkSupplyChainResult.beefMonopolistOneRun(i, 1, DEFAULT_STICKINESS, false, false,
                                                                                            woodPricingFactory,furniturePricingFactory,
                                                                                            null, null, null, 1);
            String[] resultString = new String[3];
            resultString[0]= String.valueOf(result.getQuantity());
            resultString[1]= String.valueOf(result.getBeefPrice());
            resultString[2]= String.valueOf(result.getFoodPrice());
            System.out.println(Arrays.toString(resultString));
            writer.writeNext(resultString);
            writer.flush();
        }



        writer.close();


    }





    private static void oneHundredAllLearnedCompetitiveRuns(File csvFileToWrite) throws ExecutionException, InterruptedException, IOException {

        CSVWriter writer = new CSVWriter(new FileWriter(csvFileToWrite));
        writer.writeNext(new String[]{"production","beefPrice","foodPrice"});


        //this will take a looong time

        //run the test 5 times!
        for(long i=0; i <100; i++)
        {
            OneLinkSupplyChainResult result = OneLinkSupplyChainResult.everybodyLearnedCompetitivePIDRun(i,1, DEFAULT_STICKINESS,null,
                                                                                                         1);
            String[] resultString = new String[3];
            resultString[0]= String.valueOf(result.getQuantity());
            resultString[1]= String.valueOf(result.getBeefPrice());
            resultString[2]= String.valueOf(result.getFoodPrice());
            System.out.println(Arrays.toString(resultString));
            writer.writeNext(resultString);
            writer.flush();
        }



        writer.close();


    }



    private static void oneHundredAllLearningCompetitiveRuns(File csvFileToWrite) throws ExecutionException, InterruptedException, IOException {

        CSVWriter writer = new CSVWriter(new FileWriter(csvFileToWrite));
        writer.writeNext(new String[]{"production","beefPrice","foodPrice"});


        //this will take a looong time

        //run the test 5 times!
        for(long i=0; i <100; i++)
        {
            OneLinkSupplyChainResult result = OneLinkSupplyChainResult.everybodyLearningCompetitiveStickyPIDRun(i, 1f, DEFAULT_STICKINESS,
                                                                                                                1);
            String[] resultString = new String[3];
            resultString[0]= String.valueOf(result.getQuantity());
            resultString[1]= String.valueOf(result.getBeefPrice());
            resultString[2]= String.valueOf(result.getFoodPrice());
            System.out.println(Arrays.toString(resultString));
            writer.writeNext(resultString);
            writer.flush();
        }



        writer.close();


    }


    private static void oneHundredAllLearnedFoodRuns(File file) throws IOException {

        CSVWriter writer = new CSVWriter(new FileWriter(file));
        writer.writeNext(new String[]{"production","beefPrice","foodPrice"});


        //this will take a looong time

        //run the test 5 times!
        for(long i=0; i <100; i++)
        {
            OneLinkSupplyChainResult result = OneLinkSupplyChainResult.foodMonopolistOneRun(i,1, DEFAULT_STICKINESS,true,true,null, null,
                                                                                            1);
            String[] resultString = new String[3];
            resultString[0]= String.valueOf(result.getQuantity());
            resultString[1]= String.valueOf(result.getBeefPrice());
            resultString[2]= String.valueOf(result.getFoodPrice());
            System.out.println(Arrays.toString(resultString));
            writer.writeNext(resultString);
            writer.flush();
        }



        writer.close();

    }


    private static void oneHundredAllLearningFoodRuns(File file) throws IOException {

        CSVWriter writer = new CSVWriter(new FileWriter(file));
        writer.writeNext(new String[]{"production","beefPrice","foodPrice"});


        //this will take a looong time

        //run the test 5 times!
        for(long i=0; i <100; i++)
        {
            OneLinkSupplyChainResult result = OneLinkSupplyChainResult.foodMonopolistOneRun(i,1, DEFAULT_STICKINESS,false,false,null, null,
                                                                                            1);
            String[] resultString = new String[3];
            resultString[0]= String.valueOf(result.getQuantity());
            resultString[1]= String.valueOf(result.getBeefPrice());
            resultString[2]= String.valueOf(result.getFoodPrice());
            System.out.println(Arrays.toString(resultString));
            writer.writeNext(resultString);
            writer.flush();
        }



        writer.close();

    }



    private static void oneHundredLearningMonopolist(File file) throws IOException {

        CSVWriter writer = new CSVWriter(new FileWriter(file));
        writer.writeNext(new String[]{"production","price"});


        //this will take a looong time

        //run the test 5 times!
        for(long i=0; i <100; i++)
        {



            //create the run
            MacroII macroII = new MacroII(i);
            MonopolistScenario scenario = new MonopolistScenario(macroII);
            macroII.setScenario(scenario);
            //set the demand
            scenario.setDemandIntercept(102);
            scenario.setDemandSlope(1);
            scenario.setDailyWageIntercept(14);
            scenario.setDailyWageSlope(1);

            scenario.setAskPricingStrategy(InventoryBufferSalesControl.class);
            scenario.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);

            //start it and have one step
            macroII.start();
            macroII.schedule.step(macroII);

            //now set the right parameters
            final SalesDepartment salesDepartment = scenario.getMonopolist().getSalesDepartment(UndifferentiatedGoodType.GENERIC);

            //learning
            assert salesDepartment.getPredictorStrategy() instanceof ErrorCorrectingSalesPredictor;


            //run the model
            for(int j=0; j<5000; j++)
            {
                macroII.schedule.step(macroII);
                MarginalMaximizerPIDTuning.printProgressBar(5000, j, 100);
            }




            String[] resultString = new String[2];
            resultString[0]= String.valueOf(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getData().getLatestObservation(MarketDataType.VOLUME_TRADED));
            resultString[1]= String.valueOf(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getData().getLatestObservation(MarketDataType.CLOSING_PRICE));
            System.out.println(Arrays.toString(resultString));
            writer.writeNext(resultString);
            writer.flush();
        }



        writer.close();

    }






    private static void oneHundredLearningCompetitive(File file, final int timeAveraging) throws IOException {

        CSVWriter writer = new CSVWriter(new FileWriter(file));
        writer.writeNext(new String[]{"production","price"});


        //this will take a looong time

        //run the test 5 times!
        for(long i=0; i <100; i++)
        {



            //create the run
            MacroII macroII = new MacroII(i);
            TripolistScenario scenario = new TripolistScenario(macroII);
            macroII.setScenario(scenario);
            scenario.setAdditionalCompetitors(4);
            //set the demand
            //set the demand
            scenario.setDemandIntercept(102);
            scenario.setDemandSlope(1);
            scenario.setDailyWageIntercept(14);
            scenario.setDailyWageSlope(1);
            scenario.setAskPricingStrategy(InventoryBufferSalesControl.class);
            scenario.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);

            //start it and have one step
            macroII.start();
            macroII.schedule.step(macroII);

            //now set the right parameters
            for(Firm firm : scenario.getCompetitors() )
            {
                final SalesDepartment salesDepartment = firm.getSalesDepartment(UndifferentiatedGoodType.GENERIC);
                //learning
                assert salesDepartment.getPredictorStrategy() instanceof RecursiveSalePredictor;
            }



            //run the model
            for(int j=0; j<5000; j++)
            {
                macroII.schedule.step(macroII);
                MarginalMaximizerPIDTuning.printProgressBar(5000, j, 100);
            }

            //average over the last 500 steps
            SummaryStatistics prices = new SummaryStatistics();
            SummaryStatistics quantities = new SummaryStatistics();
            for(int j=0; j< timeAveraging; j++)
            {
                macroII.schedule.step(macroII);
                assert !Float.isNaN(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayAveragePrice());
                prices.addValue(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayAveragePrice());
                quantities.addValue(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayVolume());


            }


            String[] resultString = new String[2];
            resultString[0]= String.valueOf(quantities.getMean());
            resultString[1]= String.valueOf(prices.getMean());
            System.out.println(Arrays.toString(resultString));
            writer.writeNext(resultString);
            writer.flush();
        }



        writer.close();

    }





    public static double[] beefMonopolistOneRun(
            long seed, float divideMonopolistGainsByThis, int monopolistSpeed,
            final boolean beefLearned, final boolean foodLearned, int maximizationSpeed,
            File csvFileToWrite, final int timeForAveraging) {
        SummaryStatistics distance = new SummaryStatistics();
        SummaryStatistics last1000Distance = new SummaryStatistics();

        final MacroII macroII = new MacroII(seed);
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
                    assert dept.getPredictorStrategy() instanceof RecursiveSalePredictor; //assuming here nothing has been changed and we are still dealing with recursive sale predictors
                    dept.setPredictorStrategy( new RecursiveSalePredictor(model,dept,500));
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
        scenario1.setBeefTargetInventory(100);
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
        Preconditions.checkState(scenario1.getMaximizers().size()==6,scenario1.getMaximizers().size() ); // 1 monopolist, 5 competitors
        for(WorkforceMaximizer control : scenario1.getMaximizers())
            ((PeriodicMaximizer) control).setHowManyDaysBeforeEachCheck(maximizationSpeed);


        while(macroII.schedule.getTime()<5000)
        {
            macroII.schedule.step(macroII);
            printProgressBar(14001,(int)macroII.schedule.getSteps(),100);
            long price = macroII.getMarket(OneLinkSupplyChainScenario.INPUT_GOOD).getLastPrice();
            if(price<0)
                price=0;
            distance.addValue(Math.pow(68 - price,2));
        }


        SummaryStatistics averageFoodPrice = new SummaryStatistics();
        SummaryStatistics averageBeefProduced = new SummaryStatistics();
        SummaryStatistics averageBeefPrice= new SummaryStatistics();
        for(int j=0; j< timeForAveraging; j++)
        {
            //make the model run one more day:
            macroII.schedule.step(macroII);
            averageFoodPrice.addValue(macroII.getMarket(OneLinkSupplyChainScenario.OUTPUT_GOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE));
            averageBeefProduced.addValue(macroII.getMarket(OneLinkSupplyChainScenario.INPUT_GOOD).getYesterdayVolume());
            averageBeefPrice.addValue(macroII.getMarket(OneLinkSupplyChainScenario.INPUT_GOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE));

            long price = macroII.getMarket(OneLinkSupplyChainScenario.INPUT_GOOD).getLastPrice();
            if(price<0)
                price=0;
            distance.addValue(Math.pow(68 - price,2));
            last1000Distance.addValue(Math.pow(68 - price, 2));
        }



        return new double[]{distance.getMean(),last1000Distance.getMean()};


    }



    //sweep over the period of maximization to see what happens if the frequency is too high or too low.
    private static void woodMonopolistSupplyChainSweep() throws IOException {

        CSVWriter writer = new CSVWriter(new FileWriter(Paths.get("runs","rawdata",
                                                                  "woodMonopolistStickinessesSweep.csv").toFile()));
        final String[] header = {"decisionSpeed", "stickiness", "distance", "finaldistance"};
        System.out.println(Arrays.toString(header));
        writer.writeNext(header);

        for(int speed =1; speed < 30; speed++)
        {
            for(int stickiness = 0; stickiness < 50; stickiness++)
            {
                SummaryStatistics distance = new SummaryStatistics();
                SummaryStatistics finalDistance = new SummaryStatistics();
                for(int seed =0; seed < 5; seed++)
                {

                    final double[] result = beefMonopolistOneRun(seed,1,stickiness,true,true,speed,null, 1);
                    distance.addValue(result[0]);
                    finalDistance.addValue(result[1]);

                }

                final String[] nextLine = {String.valueOf(speed), String.valueOf(stickiness),String.valueOf(distance.getMean()),
                        String.valueOf(finalDistance.getMean())};
                System.out.println(Arrays.toString(nextLine));
                writer.writeNext(nextLine);
                writer.flush();
            }
        }

        writer.close();



    }







}
