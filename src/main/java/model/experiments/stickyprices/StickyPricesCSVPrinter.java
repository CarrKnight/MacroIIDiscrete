/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.experiments.stickyprices;

import agents.firm.purchases.prediction.FixedIncreasePurchasesPredictor;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.prediction.FixedDecreaseSalesPredictor;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.base.Preconditions;
import goods.GoodType;
import model.MacroII;
import model.experiments.tuningRuns.MarginalMaximizerPIDTuning;
import model.scenario.MonopolistScenario;
import model.scenario.OneLinkSupplyChainResult;
import model.scenario.SimpleSellerScenario;
import model.utilities.stats.collectors.DailyStatCollector;
import model.utilities.stats.collectors.enums.SalesDataType;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.Arrays;

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
        //beefMonopolistRuns()
        //runWithDelay(50,0,11,true,true,0) ;
//        simpleDelaySweep(50,50,50,5);
        woodMonopolistSweep(new BigDecimal("1.1"),new BigDecimal("4.1"),new BigDecimal("1.1"),new BigDecimal("4.1"),new BigDecimal(".5"),2);



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
                                            final BigDecimal increment,final int runsPerParameterCombination)
    {

        BigDecimal currentP = minimumP;
        BigDecimal currentI = minimumI;
        while(currentP.compareTo(maximumP) <= 0)
        {

            while(currentI.compareTo(maximumI) <= 0)
            {
                for(int run =0 ; run < runsPerParameterCombination; run++)
                {

                    //create the run
                    MacroII macroII = new MacroII(run);
                    MonopolistScenario scenario = new MonopolistScenario(macroII);
                    macroII.setScenario(scenario);
                    //set the demand
                    scenario.setDemandIntercept(102);
                    scenario.setDemandSlope(2);
                    scenario.setAskPricingStrategy(SimpleFlowSellerPID.class);

                    //start it and have one step
                    macroII.start();
                    macroII.schedule.step(macroII);

                    //now set the right parameters
                    final SalesDepartment salesDepartment = scenario.getMonopolist().getSalesDepartment(GoodType.GENERIC);
                    final SimpleFlowSellerPID strategy = new SimpleFlowSellerPID(salesDepartment, currentP.floatValue(), currentI.floatValue(), 0f, 0);
                    strategy.setInitialPrice(macroII.getRandom().nextInt(102));
                    salesDepartment.setAskPricingStrategy(strategy);
                    //and make it learned!
                    salesDepartment.setPredictorStrategy(new FixedDecreaseSalesPredictor(2));
                    scenario.getMonopolist().getHRs().iterator().next().setPredictor(new FixedIncreasePurchasesPredictor(1));

                    //run the model
                    for(int i=0; i<10000; i++)
                    {
                        macroII.schedule.step(macroII);
                        MarginalMaximizerPIDTuning.printProgressBar(10000, i, 100);
                    }

                    System.out.println(macroII.getMarket(GoodType.GENERIC).getYesterdayVolume());
                    System.out.println(macroII.getMarket(GoodType.GENERIC).getLastPrice());


                }




                currentI = currentI.add(increment);
                System.out.println();

            }






            currentP = currentP.add(increment);

        }





    }









}
