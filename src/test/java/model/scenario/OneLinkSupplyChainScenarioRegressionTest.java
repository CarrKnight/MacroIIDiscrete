/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.scenario;

import agents.firm.production.control.maximizer.algorithms.marginalMaximizers.MarginalMaximizer;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.prediction.FixedDecreaseSalesPredictor;
import ec.util.MersenneTwisterFast;
import model.MacroII;
import model.utilities.stats.collectors.DailyStatCollector;
import model.utilities.stats.collectors.enums.MarketDataType;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static model.experiments.tuningRuns.MarginalMaximizerPIDTuning.printProgressBar;

/**
 * <h4>Description</h4>
 * <p/> So the idea behind this class is to store the parameters by which the one-link supply chain give me the right result.
 * This way if I ever change anything i am going to be able to make sure these parameters value still stand: no regression.
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-06-06
 * @see
 */
public class OneLinkSupplyChainScenarioRegressionTest
{



    /////////////////////////////////////////////////////////////////////////////
    // Both Monopolists
    //////////////////////////////////////////////////////////////////////////////

    //these two tests are off mostly because I have no idea what the right numbers ought to be



    /////////////////////////////////////////////////////////////////////////////
    // Beef Monopolists
    //////////////////////////////////////////////////////////////////////////////

    /**
     * force the beef monopolist to target the right production
     */
    @Test
    public void testBeefMonopolistFixedProductionWithStickyPrices() throws ExecutionException, InterruptedException {
        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        ArrayList<OneLinkSupplyChainResult> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {


            testResults.add(OneLinkSupplyChainResult.beefMonopolistFixedProductionsOneRun(random.nextInt(), 1, 10, false, null));

        }

        for(OneLinkSupplyChainResult result : testResults)
        {
            checkBeefMonopolistResult(result);

        }





    }

    @Test
    public void testBeefMonopolistFixedProductionWithSlowPID() throws ExecutionException, InterruptedException {
        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        
        ArrayList<OneLinkSupplyChainResult> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            
                    testResults.add(OneLinkSupplyChainResult.beefMonopolistFixedProductionsOneRun(random.nextInt(), 10, 0, false, null));

            

        }

        for(OneLinkSupplyChainResult result : testResults)
        {
            
            checkBeefMonopolistResult(result);
        }


    }

    @Test
    public void problematicScenario1() throws Exception {
        final OneLinkSupplyChainResult result = testFoodMonopolistWithFixedProductionRun(0,true,0,10, null);
        checkResultsOfFoodMonopolist(result);
    }
    @Test
    public void problematicScenario2() throws Exception {
            final OneLinkSupplyChainResult result = OneLinkSupplyChainResult.beefMonopolistOneRun(0, 10, 0, true, true,
                    null, null, null);

            checkBeefMonopolistResult(result);

    }

    /*
        \[ q_B = 17 \]
        \[ q_F = 17 \]
        \[ w_B = w_F = 17 \]
        \[p_B =68\]
        \[p_F = 85 \]
         */
    private void checkBeefMonopolistResult(OneLinkSupplyChainResult result) {
        Assert.assertEquals(result.getQuantity(), 17, 3);
        Assert.assertEquals(result.getBeefPrice(), 68, 5);
        Assert.assertEquals(result.getFoodPrice(),85,5 );
    }

    private void checkCompetitiveResult(OneLinkSupplyChainResult result) {
        Assert.assertEquals(result.getQuantity(), 34, 3);
        Assert.assertEquals(result.getBeefPrice(), 34, 5);
        Assert.assertEquals(result.getFoodPrice(),68,5 );
    }

    private void checkResultsOfFoodMonopolist(OneLinkSupplyChainResult result) {
        System.out.println("done with food price: " + result.getFoodPrice() + " quantity " + result.getQuantity() );
        System.out.println("beef price: " + result.getBeefPrice() );
        System.out.println();
        //the food price is in the ballpark
        //with competition, you are better off testing an MA

        Assert.assertEquals(result.getBeefPrice(),17,4 );
        Assert.assertEquals(result.getFoodPrice(), 85, 5);
        Assert.assertEquals(result.getQuantity(),17,3);
    }



    //here the food is actually a monopolist "acting competitive"
    @Test
    public void testBeefMonopolistFixedProductionWithSlowPIDAlreadyLearned() throws ExecutionException, InterruptedException {
        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        ArrayList<OneLinkSupplyChainResult> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            
                    testResults.add(OneLinkSupplyChainResult.beefMonopolistFixedProductionsOneRun(random.nextInt(), 10, 0, true, null));

            

        }

        for(OneLinkSupplyChainResult result : testResults)
        {
            
            checkBeefMonopolistResult(result);
        }
    }



    @Test
    public void testBeefMonopolistFixedProductionWithStickyPricesAlreadyLearned() throws ExecutionException, InterruptedException {
        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        
        ArrayList<OneLinkSupplyChainResult> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            
                    testResults.add(OneLinkSupplyChainResult.beefMonopolistFixedProductionsOneRun(random.nextInt(), 1, 10, true, null));

            

        }

        for(OneLinkSupplyChainResult result : testResults)
        {
            
            checkBeefMonopolistResult(result);
        }





    }






    //////////////////////////////////////////////////////////////////////////////////////////////////
    // Now the beef monopolist isn't told to produce the right amount, but it knows the price drops by 2 every increase in production
    //////////////////////////////////////////////////////////////////////////////////////////////////

    //here both the beef monopolist and the food competitors have given predictors.
    @Test
    public  void everybodyLearnedBeefMonopolistSlowPID() throws ExecutionException, InterruptedException {

        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        
        ArrayList<OneLinkSupplyChainResult> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            
                    testResults.add(OneLinkSupplyChainResult.beefMonopolistOneRun(
                            random.nextInt(), 10, 0, true, true, null, null, null));

            

        }

        for(OneLinkSupplyChainResult result : testResults)
        {
            
            checkBeefMonopolistResult(result);
        }



    }


    //here both the beef monopolist and the food competitors have given predictors.
    @Test
    public  void everybodyLearnedBeefMonopolistStickyPID() throws ExecutionException, InterruptedException {

        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        
        ArrayList<OneLinkSupplyChainResult> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            
                    testResults.add(OneLinkSupplyChainResult.beefMonopolistOneRun(random.nextInt(), 1, 50, true, true, null, null, null));

            

        }

        for(OneLinkSupplyChainResult result : testResults)
        {
            
            checkBeefMonopolistResult(result);
        }



    }



    //here the food competitors are given good predictors
    @Test
    public  void foodLearnedBeefMonopolistSlowPID() throws ExecutionException, InterruptedException {

        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        
        ArrayList<OneLinkSupplyChainResult> testResults = new ArrayList<>(5);


        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            
                    testResults.add(OneLinkSupplyChainResult.
                            beefMonopolistOneRun(0, 50, 0, false, true, null, null,
                                    null));

            

        }

        List<OneLinkSupplyChainResult> results = new LinkedList<>();
        for(OneLinkSupplyChainResult result : testResults)
        {
            results.add(result);
        }


        for(OneLinkSupplyChainResult result : results)
            checkBeefMonopolistResult(result);


    }

    //here the food competitors are given good predictors
    @Test
    public  void foodLearnedBeefMonopolistStickyPID() throws ExecutionException, InterruptedException {

        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        
        ArrayList<OneLinkSupplyChainResult> testResults = new ArrayList<>(5);


        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            
                    testResults.add(OneLinkSupplyChainResult.beefMonopolistOneRun(random.nextInt(),
                            1, 50, false, true, null, null, null));

            

        }

        List<OneLinkSupplyChainResult> results = new LinkedList<>();
        for(OneLinkSupplyChainResult result : testResults)
        {
            results.add(result);
        }


        for(OneLinkSupplyChainResult result : results)
            checkBeefMonopolistResult(result);


    }



    //here the food competitors are given good predictors
    @Test
    public  void beefLearnedBeefMonopolistSlowPID() throws ExecutionException, InterruptedException {

        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        
        ArrayList<OneLinkSupplyChainResult> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            
                    testResults.add(OneLinkSupplyChainResult.beefMonopolistOneRun(random.nextInt(), 10, 0, true, false, null, null, null));

            

        }

        for(OneLinkSupplyChainResult result : testResults)
        {
            
            checkBeefMonopolistResult(result);
        }

    }






    @Test
    public  void beefLearnedBeefMonopolistStickyPID() throws ExecutionException, InterruptedException {

        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        ArrayList<OneLinkSupplyChainResult> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            
                    testResults.add(OneLinkSupplyChainResult.beefMonopolistOneRun(random.nextInt(), 1, 10, true, false, null, null, null));

            

        }

        for(OneLinkSupplyChainResult result : testResults)
        {
            
            checkBeefMonopolistResult(result);
        }

    }



    @Test
    public  void everybodyLearnedCompetitiveSlowPID() throws ExecutionException, InterruptedException {


        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        
        ArrayList<OneLinkSupplyChainResult> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            
                    testResults.add(OneLinkSupplyChainResult.everybodyLearnedCompetitivePIDRun(random.nextInt(), 10, 0, null));

            

        }

        for(OneLinkSupplyChainResult result : testResults)
        {

            checkCompetitiveResult(result);
        }





    }

    @Test
    public  void everybodyLearnedCompetitiveStickyPID() throws ExecutionException, InterruptedException {


        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        
        ArrayList<OneLinkSupplyChainResult> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            
                    testResults.add(OneLinkSupplyChainResult.everybodyLearnedCompetitivePIDRun(random.nextInt(), 1, 10, null));

            

        }

        for(OneLinkSupplyChainResult result : testResults)
        {
            
            checkCompetitiveResult(result);
        }





    }


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // These are the important ones!
    //////////////////////////////////////////////////////////////////////////////////////////////////


    @Test
    public  void everybodyLearningCompetitiveSlowPID() throws ExecutionException, InterruptedException {


        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        
        ArrayList<OneLinkSupplyChainResult> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            
                    testResults.add(OneLinkSupplyChainResult.everybodyLearningCompetitiveSlowPIDRun(random.nextInt()));

            

        }

        for(OneLinkSupplyChainResult result : testResults)
        {
            
            checkCompetitiveResult(result);
        }





    }


    @Test
    public  void everybodyLearningCompetitiveStickyPID() throws ExecutionException, InterruptedException {


        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        
        ArrayList<OneLinkSupplyChainResult> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            
                    testResults.add(OneLinkSupplyChainResult.everybodyLearningCompetitiveStickyPIDRun(random.nextInt()));

            

        }

        for(OneLinkSupplyChainResult result : testResults)
        {
            
            checkCompetitiveResult(result);
        }





    }

    @Test
    public  void everybodyLearningBeefMonopolistSlowPID() throws ExecutionException, InterruptedException {


        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        
        ArrayList<OneLinkSupplyChainResult> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            
                    testResults.add(OneLinkSupplyChainResult.beefMonopolistOneRun(random.nextInt(), 10, 0, false, false, null, null, null));

            

        }

        for(OneLinkSupplyChainResult result : testResults)
        {
            
            checkBeefMonopolistResult(result);
        }





    }



    @Test
    public  void everybodyLearningBeefMonopolistStickyPID() throws ExecutionException, InterruptedException {

        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        
        ArrayList<OneLinkSupplyChainResult> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            
                    testResults.add(OneLinkSupplyChainResult.beefMonopolistOneRun(random.nextInt(),
                            1, 50, false, false, null, null, null));

            


        }

        for(OneLinkSupplyChainResult result : testResults)
        {
            
            checkBeefMonopolistResult(result);
        }



    }




    /********************************************************************************
     *
     * FOOD MONOPOLIST
     *
     ********************************************************************************/

    /////////////////////////////////////////////////////////////////////////////
    // Food Monopolists with forced quantity (just check prices)
    //////////////////////////////////////////////////////////////////////////////

    /**
     * force the beef monopolist to target the right production
     */
    @Test
    public void testFoodMonopolistWithStickyPricesAndFixedQuantity() throws ExecutionException, InterruptedException {
        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        
        ArrayList<OneLinkSupplyChainResult> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            
                    testResults.add(testFoodMonopolistWithFixedProductionRun(random.nextInt(), false, 10, 1, null));

            

        }

        for(OneLinkSupplyChainResult result : testResults)
        {
            checkResultsOfFoodMonopolist(result);
        }


    }

    private OneLinkSupplyChainResult testFoodMonopolistWithFixedProductionRun(int random, final boolean competitorsLearned, int speed, float divideGainsByThis,
                                                                              File csvFileToWrite) {
        final MacroII macroII = new MacroII(random);
        final OneLinkSupplyChainScenarioCheatingBuyPriceAndForcedMonopolist scenario1 =
                new OneLinkSupplyChainScenarioCheatingBuyPriceAndForcedMonopolist(macroII, OneLinkSupplyChainScenario.OUTPUT_GOOD)
                {
                    @Override
                    protected void buildBeefSalesPredictor(SalesDepartment dept) {
                        if(competitorsLearned)
                            dept.setPredictorStrategy(new FixedDecreaseSalesPredictor(0));
                    }
                };
        scenario1.setControlType(MarginalMaximizer.class);        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        //use standard PID parameters
        scenario1.setDivideProportionalGainByThis(divideGainsByThis);
        scenario1.setDivideIntegrativeGainByThis(divideGainsByThis);
        //100 days delay
        scenario1.setBeefPricingSpeed(speed);
        //no need for filter with the cheating price
        scenario1.setBeefPriceFilterer(null);
        scenario1.setBeefTargetInventory(10);



        //add csv writer if needed
        if(csvFileToWrite != null)
            DailyStatCollector.addDailyStatCollectorToModel(csvFileToWrite,macroII);


        macroII.setScenario(scenario1);
        macroII.start();


        while(macroII.schedule.getTime()<9000)
        {
            macroII.schedule.step(macroII);
            printProgressBar(10001,(int)macroII.schedule.getSteps(),100);
        }


        //I used to assert this:
        //Assert.assertEquals(macroII.getMarket(OneLinkSupplyChainScenario.OUTPUT_GOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE),85,6 );
        //but that's too hard because while on average the price hovers there, competition is noisy. Sometimes a lot.
        //so what I did was to attach a daily stat collector and then check the average of the last 10 prices
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


        System.out.println("beef price: " +averageBeefPrice.getMean() );
        System.out.println("food price: " + averageFoodPrice.getMean() );
        System.out.println("produced: " + averageBeefProduced.getMean() );
        System.out.println();

        return new OneLinkSupplyChainResult(averageBeefPrice.getMean(),averageFoodPrice.getMean(),averageBeefProduced.getMean(), macroII);

    }





    @Test
    public void testFoodMonopolistWithSlowPIDAndFixedQuantity() throws ExecutionException, InterruptedException {

        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        
        ArrayList<OneLinkSupplyChainResult> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            
                    testResults.add(testFoodMonopolistWithFixedProductionRun(random.nextInt(), false, 0, 10, null));

            

        }

        for(OneLinkSupplyChainResult result : testResults)
        {
            checkResultsOfFoodMonopolist(result);
        }

    }

    @Test
    public void testFoodMonopolistWithSlowPIDAndFixedQuantityAndLearnedCompetitors() throws ExecutionException, InterruptedException {

        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        
        ArrayList<OneLinkSupplyChainResult> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            
                    testResults.add(testFoodMonopolistWithFixedProductionRun(random.nextInt(),true,0,10, null));

            

        }

        for(OneLinkSupplyChainResult result : testResults)
        {
            checkResultsOfFoodMonopolist(result);
        }

    }


    /**
     * force the beef monopolist to target the right production
     */
    @Test
    public void testFoodMonopolistWithStickyPricesAndFixedQuantityAndLearnedCompetitors() throws ExecutionException, InterruptedException {
        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        
        ArrayList<OneLinkSupplyChainResult> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            
                    testResults.add(testFoodMonopolistWithFixedProductionRun(random.nextInt(),true,10,1, null));

            

        }

        for(OneLinkSupplyChainResult result : testResults)
        {
            checkResultsOfFoodMonopolist(result);
        }


    }


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // Now the food monopolist isn't told to produce the right amount, but it knows the price drops by 2 every increase in production
    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public  void alreadyLearnedFoodMonopolistSlowPID() throws ExecutionException, InterruptedException {


        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        
        ArrayList<OneLinkSupplyChainResult> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            
                    testResults.add(
                        OneLinkSupplyChainResult.foodMonopolistOneRun(random.nextInt(),10,
                                    0,true,true,null, null)

                    );

            

        }

        for(OneLinkSupplyChainResult result : testResults)
        {
            checkResultsOfFoodMonopolist(result);
        }



    }

    @Test
    public  void alreadyLearnedFoodMonopolistStickyPID() throws ExecutionException, InterruptedException {


        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        
        ArrayList<OneLinkSupplyChainResult> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            
                    testResults.add(OneLinkSupplyChainResult.foodMonopolistOneRun(
                            random.nextInt(), 1, 10, true, true, null, null));



            

        }

        for(OneLinkSupplyChainResult result : testResults)
        {
            checkResultsOfFoodMonopolist(result);
        }


    }



    //////////////////////////////////////////////////////////////////////////////////////////////////
    // These are the important ones!
    //////////////////////////////////////////////////////////////////////////////////////////////////


    @Test
    public  void learningFoodMonopolistSlowPID() throws ExecutionException, InterruptedException {


        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        
        ArrayList<OneLinkSupplyChainResult> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            
                    testResults.add(OneLinkSupplyChainResult.foodMonopolistOneRun(
                            random.nextInt(), 10, 0, false, false, null, null));

            

        }

        for(OneLinkSupplyChainResult result : testResults)
        {
            checkResultsOfFoodMonopolist(result);
        }



    }



    @Test
    public  void learningFoodMonopolistStickyPID() throws ExecutionException, InterruptedException {

        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        
        ArrayList<OneLinkSupplyChainResult> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            
                    testResults.add(OneLinkSupplyChainResult.foodMonopolistOneRun(random.nextInt(), 1, 100,
                            false, false,null, null ));

            

        }

        for(OneLinkSupplyChainResult result : testResults)
        {
            checkResultsOfFoodMonopolist(result);
        }



    }








}
