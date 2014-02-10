package model.scenario;

import agents.firm.production.control.maximizer.algorithms.marginalMaximizers.MarginalMaximizer;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.prediction.FixedIncreasePurchasesPredictor;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.prediction.FixedDecreaseSalesPredictor;
import ec.util.MersenneTwisterFast;
import goods.GoodType;
import model.MacroII;
import model.utilities.filters.ExponentialFilter;
import model.utilities.stats.collectors.DailyStatCollector;
import model.utilities.stats.collectors.enums.MarketDataType;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

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

    /**
     * With these parameters the beef seller waits for 100 days before changing its price
     */
    //@Test
    public void testWithStickyPrices() throws InterruptedException, ExecutionException {
        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        ExecutorService testRunner = Executors.newFixedThreadPool(5);
        ArrayList<Future<OneLinkSupplyChainResult>> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            Future<OneLinkSupplyChainResult> testReceipt =
                    testRunner.submit(new Callable<OneLinkSupplyChainResult>(){
                        /**
                         * Computes a result, or throws an exception if unable to do so.
                         *
                         * @return computed result
                         * @throws Exception if unable to compute a result
                         */
                        @Override
                        public OneLinkSupplyChainResult call() throws Exception {
                            return testWithStickyPriceOneRun(random.nextLong());
                        }
                    });

            testResults.add(testReceipt);

        }

        for(Future<OneLinkSupplyChainResult> receipt : testResults)
        {
            long beefPrice = (long) receipt.get().getBeefPrice();
            Assert.assertTrue(beefPrice >= 27 && beefPrice <= 32);

        }


    }

    private OneLinkSupplyChainResult testWithStickyPriceOneRun(long seed) {
        final MacroII macroII = new MacroII(seed);
        final OneLinkSupplyChainScenario scenario1 = new OneLinkSupplyChainScenario(macroII);
        scenario1.setControlType(MarginalMaximizer.class);
        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        //use standard PID parameters
        scenario1.setDivideProportionalGainByThis(1f);
        scenario1.setDivideIntegrativeGainByThis(1f);
        //100 days delay
        scenario1.setBeefPricingSpeed(100);
        //add a very big filterer (since we can wait 100 turns)
        scenario1.setBeefPriceFilterer(new ExponentialFilter<Integer>(.01f));


        macroII.setScenario(scenario1);
        macroII.start();


        while(macroII.schedule.getTime()<15000)
        {
            macroII.schedule.step(macroII);
            printProgressBar(15001,(int)macroII.schedule.getSteps(),100);
        }

        System.out.println("done with price: " +macroII.getMarket(GoodType.BEEF).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE) );
        System.out.println();
        //the beef price is in the ballpark
        System.out.println("done!");
        return new OneLinkSupplyChainResult(macroII.getMarket(GoodType.BEEF).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE),
                macroII.getMarket(GoodType.FOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE),macroII.getMarket(GoodType.BEEF).getYesterdayVolume());
    }


    /**
     * With these parameters the beef seller adjusts its prices everyday, but only ever so slightly!
     */
    //@Test
    public void testWithSlowPID() throws ExecutionException, InterruptedException {

        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        ExecutorService testRunner = Executors.newFixedThreadPool(5);
        ArrayList<Future<OneLinkSupplyChainResult>> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            Future<OneLinkSupplyChainResult> testReceipt =
                    testRunner.submit(new Callable<OneLinkSupplyChainResult>(){
                        /**
                         * Computes a result, or throws an exception if unable to do so.
                         *
                         * @return computed result
                         * @throws Exception if unable to compute a result
                         */
                        @Override
                        public OneLinkSupplyChainResult call() throws Exception {
                            return testWithSlowPidOneRun(random.nextLong());
                        }
                    });

            testResults.add(testReceipt);

        }

        for(Future<OneLinkSupplyChainResult> receipt : testResults)
        {
            long beefPrice = (long) receipt.get().getBeefPrice();
            Assert.assertTrue(beefPrice >= 27 && beefPrice <= 32);

        }


    }

    private OneLinkSupplyChainResult testWithSlowPidOneRun(long seed) {
        final MacroII macroII = new MacroII(seed);
        final OneLinkSupplyChainScenario scenario1 = new OneLinkSupplyChainScenario(macroII);
        scenario1.setControlType(MarginalMaximizer.class);
        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        //divide standard PID parameters by 100
        scenario1.setDivideProportionalGainByThis(100f);
        scenario1.setDivideIntegrativeGainByThis(100f);
        //100 days delay
        scenario1.setBeefPricingSpeed(0);
        //no real need of filter at this slow speed
        scenario1.setBeefPriceFilterer(null);


        macroII.setScenario(scenario1);
        macroII.start();


        while(macroII.schedule.getTime()<15000)
        {
            macroII.schedule.step(macroII);
            printProgressBar(15001,(int)macroII.schedule.getSteps(),100);
        }

        System.out.println("done with price: " +macroII.getMarket(GoodType.BEEF).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE) );
        System.out.println();
        System.out.println("done!");
        return new OneLinkSupplyChainResult(macroII.getMarket(GoodType.BEEF).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE),
                macroII.getMarket(GoodType.FOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE),macroII.getMarket(GoodType.BEEF).getYesterdayVolume());
        //the beef price is in the ballpark
    }


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
        ExecutorService testRunner = Executors.newFixedThreadPool(5);
        ArrayList<Future<OneLinkSupplyChainResult>> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            Future<OneLinkSupplyChainResult> testReceipt =
                    testRunner.submit(new Callable<OneLinkSupplyChainResult>(){
                        /**
                         * Computes a result, or throws an exception if unable to do so.
                         *
                         * @return computed result
                         * @throws Exception if unable to compute a result
                         */
                        @Override
                        public OneLinkSupplyChainResult call() throws Exception {
                            return OneLinkSupplyChainResult.beefMonopolistFixedProductionsOneRun(random.nextLong(), 1, 100, false, null);
                        }
                    });

            testResults.add(testReceipt);

        }

        for(Future<OneLinkSupplyChainResult> receipt : testResults)
        {
            OneLinkSupplyChainResult result = receipt.get();
            checkBeefMonopolistResult(result);

        }





    }

    @Test
    public void testBeefMonopolistFixedProductionWithSlowPID() throws ExecutionException, InterruptedException {
        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        ExecutorService testRunner = Executors.newFixedThreadPool(5);
        ArrayList<Future<OneLinkSupplyChainResult>> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            Future<OneLinkSupplyChainResult> testReceipt =
                    testRunner.submit(new Callable<OneLinkSupplyChainResult>(){
                        /**
                         * Computes a result, or throws an exception if unable to do so.
                         *
                         * @return computed result
                         * @throws Exception if unable to compute a result
                         */
                        @Override
                        public OneLinkSupplyChainResult call() throws Exception {
                            return OneLinkSupplyChainResult.beefMonopolistFixedProductionsOneRun(random.nextLong(), 100, 0, false, null);
                        }
                    });

            testResults.add(testReceipt);

        }

        for(Future<OneLinkSupplyChainResult> receipt : testResults)
        {
            OneLinkSupplyChainResult result = receipt.get();
            checkBeefMonopolistResult(result);
        }


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
        Assert.assertEquals(result.getBeefPrice(), 68, 5l);
        Assert.assertEquals(result.getFoodPrice(),85,5l );
    }

    private void checkCompetitiveResult(OneLinkSupplyChainResult result) {
        Assert.assertEquals(result.getQuantity(), 34, 3);
        Assert.assertEquals(result.getBeefPrice(), 34, 5l);
        Assert.assertEquals(result.getFoodPrice(),68,5l );
    }



    //here the food is actually a monopolist "acting competitive"
    @Test
    public void testBeefMonopolistFixedProductionWithSlowPIDAlreadyLearned() throws ExecutionException, InterruptedException {
        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        ExecutorService testRunner = Executors.newFixedThreadPool(5);
        ArrayList<Future<OneLinkSupplyChainResult>> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <1; i++)
        {
            //run the test, add it as a future so I can check the results!
            Future<OneLinkSupplyChainResult> testReceipt =
                    testRunner.submit(new Callable<OneLinkSupplyChainResult>(){
                        /**
                         * Computes a result, or throws an exception if unable to do so.
                         *
                         * @return computed result
                         * @throws Exception if unable to compute a result
                         */
                        @Override
                        public OneLinkSupplyChainResult call() throws Exception {
                            return OneLinkSupplyChainResult.beefMonopolistFixedProductionsOneRun(random.nextLong(), 100, 0, true, null);
                        }
                    });

            testResults.add(testReceipt);

        }

        for(Future<OneLinkSupplyChainResult> receipt : testResults)
        {
            OneLinkSupplyChainResult result = receipt.get();
            checkBeefMonopolistResult(result);
        }
    }



    @Test
    public void testBeefMonopolistFixedProductionWithStickyPricesAlreadyLearned() throws ExecutionException, InterruptedException {
        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        ExecutorService testRunner = Executors.newFixedThreadPool(5);
        ArrayList<Future<OneLinkSupplyChainResult>> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            Future<OneLinkSupplyChainResult> testReceipt =
                    testRunner.submit(new Callable<OneLinkSupplyChainResult>(){
                        /**
                         * Computes a result, or throws an exception if unable to do so.
                         *
                         * @return computed result
                         * @throws Exception if unable to compute a result
                         */
                        @Override
                        public OneLinkSupplyChainResult call() throws Exception {
                            return OneLinkSupplyChainResult.beefMonopolistFixedProductionsOneRun(random.nextLong(), 1, 100, true, null);
                        }
                    });

            testResults.add(testReceipt);

        }

        for(Future<OneLinkSupplyChainResult> receipt : testResults)
        {
            OneLinkSupplyChainResult result = receipt.get();
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
        ExecutorService testRunner = Executors.newFixedThreadPool(5);
        ArrayList<Future<OneLinkSupplyChainResult>> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            Future<OneLinkSupplyChainResult> testReceipt =
                    testRunner.submit(new Callable<OneLinkSupplyChainResult>(){
                        /**
                         * Computes a result, or throws an exception if unable to do so.
                         *
                         * @return computed result
                         * @throws Exception if unable to compute a result
                         */
                        @Override
                        public OneLinkSupplyChainResult call() throws Exception {
                            return   OneLinkSupplyChainResult.beefMonopolistOneRun(random.nextLong(), 100, 0, true, true, null);
                        }
                    });

            testResults.add(testReceipt);

        }

        for(Future<OneLinkSupplyChainResult> receipt : testResults)
        {
            OneLinkSupplyChainResult result = receipt.get();
            checkBeefMonopolistResult(result);
        }



    }


    //here both the beef monopolist and the food competitors have given predictors.
    @Test
    public  void everybodyLearnedBeefMonopolistStickyPID() throws ExecutionException, InterruptedException {

        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        ExecutorService testRunner = Executors.newFixedThreadPool(5);
        ArrayList<Future<OneLinkSupplyChainResult>> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            Future<OneLinkSupplyChainResult> testReceipt =
                    testRunner.submit(new Callable<OneLinkSupplyChainResult>(){
                        /**
                         * Computes a result, or throws an exception if unable to do so.
                         *
                         * @return computed result
                         * @throws Exception if unable to compute a result
                         */
                        @Override
                        public OneLinkSupplyChainResult call() throws Exception {
                            return   OneLinkSupplyChainResult.beefMonopolistOneRun(random.nextLong(), 1, 100, true, true, null);
                        }
                    });

            testResults.add(testReceipt);

        }

        for(Future<OneLinkSupplyChainResult> receipt : testResults)
        {
            OneLinkSupplyChainResult result = receipt.get();
            checkBeefMonopolistResult(result);
        }



    }



    //here the food competitors are given good predictors
    @Test
    public  void foodLearnedBeefMonopolistSlowPID() throws ExecutionException, InterruptedException {

        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        ExecutorService testRunner = Executors.newFixedThreadPool(5);
        ArrayList<Future<OneLinkSupplyChainResult>> testResults = new ArrayList<>(5);


        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            Future<OneLinkSupplyChainResult> testReceipt =
                    testRunner.submit(new Callable<OneLinkSupplyChainResult>(){
                        /**
                         * Computes a result, or throws an exception if unable to do so.
                         *
                         * @return computed result
                         * @throws Exception if unable to compute a result
                         */
                        @Override
                        public OneLinkSupplyChainResult call() throws Exception {
                            return   OneLinkSupplyChainResult.beefMonopolistOneRun(random.nextLong(), 100, 0, false, true, null);
                        }
                    });

            testResults.add(testReceipt);

        }

        List<OneLinkSupplyChainResult> results = new LinkedList<>();
        for(Future<OneLinkSupplyChainResult> receipt : testResults)
        {
            results.add(receipt.get());
        }


        for(OneLinkSupplyChainResult result : results)
            checkBeefMonopolistResult(result);


    }

    //here the food competitors are given good predictors
    @Test
    public  void foodLearnedBeefMonopolistStickyPID() throws ExecutionException, InterruptedException {

        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        ExecutorService testRunner = Executors.newFixedThreadPool(5);
        ArrayList<Future<OneLinkSupplyChainResult>> testResults = new ArrayList<>(5);


        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            Future<OneLinkSupplyChainResult> testReceipt =
                    testRunner.submit(new Callable<OneLinkSupplyChainResult>(){
                        /**
                         * Computes a result, or throws an exception if unable to do so.
                         *
                         * @return computed result
                         * @throws Exception if unable to compute a result
                         */
                        @Override
                        public OneLinkSupplyChainResult call() throws Exception {
                            return   OneLinkSupplyChainResult.beefMonopolistOneRun(random.nextLong(), 1, 100, false, true, null);
                        }
                    });

            testResults.add(testReceipt);

        }

        List<OneLinkSupplyChainResult> results = new LinkedList<>();
        for(Future<OneLinkSupplyChainResult> receipt : testResults)
        {
            results.add(receipt.get());
        }


        for(OneLinkSupplyChainResult result : results)
            checkBeefMonopolistResult(result);


    }



    //here the food competitors are given good predictors
    @Test
    public  void beefLearnedBeefMonopolistSlowPID() throws ExecutionException, InterruptedException {

        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        ExecutorService testRunner = Executors.newFixedThreadPool(5);
        ArrayList<Future<OneLinkSupplyChainResult>> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            Future<OneLinkSupplyChainResult> testReceipt =
                    testRunner.submit(new Callable<OneLinkSupplyChainResult>(){
                        /**
                         * Computes a result, or throws an exception if unable to do so.
                         *
                         * @return computed result
                         * @throws Exception if unable to compute a result
                         */
                        @Override
                        public OneLinkSupplyChainResult call() throws Exception {
                            return   OneLinkSupplyChainResult.beefMonopolistOneRun(random.nextLong(), 100, 0, true, false, null);
                        }
                    });

            testResults.add(testReceipt);

        }

        for(Future<OneLinkSupplyChainResult> receipt : testResults)
        {
            OneLinkSupplyChainResult result = receipt.get();
            checkBeefMonopolistResult(result);
        }

    }

 


    

    @Test
    public  void beefLearnedBeefMonopolistStickyPID() throws ExecutionException, InterruptedException {

        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        ExecutorService testRunner = Executors.newFixedThreadPool(5);
        ArrayList<Future<OneLinkSupplyChainResult>> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            Future<OneLinkSupplyChainResult> testReceipt =
                    testRunner.submit(new Callable<OneLinkSupplyChainResult>(){
                        /**
                         * Computes a result, or throws an exception if unable to do so.
                         *
                         * @return computed result
                         * @throws Exception if unable to compute a result
                         */
                        @Override
                        public OneLinkSupplyChainResult call() throws Exception {
                            return   OneLinkSupplyChainResult.beefMonopolistOneRun(random.nextLong(), 1, 100, true, false, null);
                        }
                    });

            testResults.add(testReceipt);

        }

        for(Future<OneLinkSupplyChainResult> receipt : testResults)
        {
            OneLinkSupplyChainResult result = receipt.get();
            checkBeefMonopolistResult(result);
        }

    }



    @Test
    public  void everybodyLearnedCompetitiveSlowPID() throws ExecutionException, InterruptedException {


        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        ExecutorService testRunner = Executors.newFixedThreadPool(5);
        ArrayList<Future<OneLinkSupplyChainResult>> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            Future<OneLinkSupplyChainResult> testReceipt =
                    testRunner.submit(new Callable<OneLinkSupplyChainResult>(){
                        /**
                         * Computes a result, or throws an exception if unable to do so.
                         *
                         * @return computed result
                         * @throws Exception if unable to compute a result
                         */
                        @Override
                        public OneLinkSupplyChainResult call() throws Exception {
                            return             OneLinkSupplyChainResult.everybodyLearnedCompetitivePIDRun(random.nextLong(), 100, 0, null);
                        }
                    });

            testResults.add(testReceipt);

        }

        for(Future<OneLinkSupplyChainResult> receipt : testResults)
        {
            OneLinkSupplyChainResult result = receipt.get();
            checkCompetitiveResult(result);
        }





    }

    @Test
    public  void everybodyLearnedCompetitiveStickyPID() throws ExecutionException, InterruptedException {


        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        ExecutorService testRunner = Executors.newFixedThreadPool(5);
        ArrayList<Future<OneLinkSupplyChainResult>> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            Future<OneLinkSupplyChainResult> testReceipt =
                    testRunner.submit(new Callable<OneLinkSupplyChainResult>(){
                        /**
                         * Computes a result, or throws an exception if unable to do so.
                         *
                         * @return computed result
                         * @throws Exception if unable to compute a result
                         */
                        @Override
                        public OneLinkSupplyChainResult call() throws Exception {
                            return             OneLinkSupplyChainResult.everybodyLearnedCompetitivePIDRun(random.nextLong(), 1, 100, null);
                        }
                    });

            testResults.add(testReceipt);

        }

        for(Future<OneLinkSupplyChainResult> receipt : testResults)
        {
            OneLinkSupplyChainResult result = receipt.get();
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
        ExecutorService testRunner = Executors.newFixedThreadPool(5);
        ArrayList<Future<OneLinkSupplyChainResult>> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            Future<OneLinkSupplyChainResult> testReceipt =
                    testRunner.submit(new Callable<OneLinkSupplyChainResult>(){
                        /**
                         * Computes a result, or throws an exception if unable to do so.
                         *
                         * @return computed result
                         * @throws Exception if unable to compute a result
                         */
                        @Override
                        public OneLinkSupplyChainResult call() throws Exception {
                            return   OneLinkSupplyChainResult.everybodyLearningCompetitiveSlowPIDRun(random.nextLong());

                        }
                    });

            testResults.add(testReceipt);

        }

        for(Future<OneLinkSupplyChainResult> receipt : testResults)
        {
            OneLinkSupplyChainResult result = receipt.get();
            checkCompetitiveResult(result);
        }





    }


    @Test
    public  void everybodyLearningCompetitiveStickyPID() throws ExecutionException, InterruptedException {


        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        ExecutorService testRunner = Executors.newFixedThreadPool(5);
        ArrayList<Future<OneLinkSupplyChainResult>> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            Future<OneLinkSupplyChainResult> testReceipt =
                    testRunner.submit(new Callable<OneLinkSupplyChainResult>(){
                        /**
                         * Computes a result, or throws an exception if unable to do so.
                         *
                         * @return computed result
                         * @throws Exception if unable to compute a result
                         */
                        @Override
                        public OneLinkSupplyChainResult call() throws Exception {
                            return             everybodyLearningCompetitiveStickyPIDRun(random.nextLong());
                        }
                    });

            testResults.add(testReceipt);

        }

        for(Future<OneLinkSupplyChainResult> receipt : testResults)
        {
            OneLinkSupplyChainResult result = receipt.get();
            checkCompetitiveResult(result);
        }





    }

    private OneLinkSupplyChainResult everybodyLearningCompetitiveStickyPIDRun(long random) {
        final MacroII macroII = new MacroII(random);
        final OneLinkSupplyChainScenarioWithCheatingBuyingPrice scenario1 = new OneLinkSupplyChainScenarioWithCheatingBuyingPrice(macroII);

        scenario1.setControlType(MarginalMaximizer.class);        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        scenario1.setBeefPriceFilterer(null);


        //competition!
        scenario1.setNumberOfBeefProducers(5);
        scenario1.setNumberOfFoodProducers(5);

        scenario1.setDivideProportionalGainByThis(1f);
        scenario1.setDivideIntegrativeGainByThis(1f);
        //no delay
        scenario1.setBeefPricingSpeed(50);


        macroII.setScenario(scenario1);
        macroII.start();


        while(macroII.schedule.getTime()<9000)
        {
            macroII.schedule.step(macroII);
            printProgressBar(9001,(int)macroII.schedule.getSteps(),100);
        }


        //I used to assert this:
        //Assert.assertEquals(macroII.getMarket(GoodType.FOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE),85l,6l );
        //but that's too hard because while on average the price hovers there, competition is noisy. Sometimes a lot.
        //so what I did was to attach a daily stat collector and then check the average of the last 10 prices
        float averageFoodPrice = 0;
        float averageBeefProduced = 0;
        float averageBeefPrice=0;
        for(int j=0; j< 1000; j++)
        {
            //make the model run one more day:
            macroII.schedule.step(macroII);
            averageFoodPrice += macroII.getMarket(GoodType.FOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE);
            averageBeefProduced+= macroII.getMarket(GoodType.BEEF).countTodayProductionByRegisteredSellers();
            averageBeefPrice+= macroII.getMarket(GoodType.BEEF).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE);
        }

        System.out.println("beef price: " +averageBeefPrice/1000f );
        System.out.println("food price: " +averageFoodPrice/1000f );
        System.out.println("produced: " +averageBeefProduced/1000f );
        System.out.println();


        return new OneLinkSupplyChainResult(averageBeefPrice/1000f,averageFoodPrice/1000f,averageBeefProduced/1000f);

    }

    @Test
    public  void everybodyLearningBeefMonopolistSlowPID() throws ExecutionException, InterruptedException {


        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        ExecutorService testRunner = Executors.newFixedThreadPool(5);
        ArrayList<Future<OneLinkSupplyChainResult>> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            Future<OneLinkSupplyChainResult> testReceipt =
                    testRunner.submit(new Callable<OneLinkSupplyChainResult>(){
                        /**
                         * Computes a result, or throws an exception if unable to do so.
                         *
                         * @return computed result
                         * @throws Exception if unable to compute a result
                         */
                        @Override
                        public OneLinkSupplyChainResult call() throws Exception {
                            return             OneLinkSupplyChainResult.beefMonopolistOneRun(random.nextLong(), 100, 0, false, false, null);
                        }
                    });

            testResults.add(testReceipt);

        }

        for(Future<OneLinkSupplyChainResult> receipt : testResults)
        {
            OneLinkSupplyChainResult result = receipt.get();
            checkBeefMonopolistResult(result);
        }





    }



    @Test
    public  void everybodyLearningBeefMonopolistStickyPID() throws ExecutionException, InterruptedException {

        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        ExecutorService testRunner = Executors.newFixedThreadPool(5);
        ArrayList<Future<OneLinkSupplyChainResult>> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            Future<OneLinkSupplyChainResult> testReceipt =
                    testRunner.submit(new Callable<OneLinkSupplyChainResult>(){
                        /**
                         * Computes a result, or throws an exception if unable to do so.
                         *
                         * @return computed result
                         * @throws Exception if unable to compute a result
                         */
                        @Override
                        public OneLinkSupplyChainResult call() throws Exception {
                            return             OneLinkSupplyChainResult.beefMonopolistOneRun(random.nextLong(), 1, 100, false, false, null);
                        }
                    });

            testResults.add(testReceipt);


        }

        for(Future<OneLinkSupplyChainResult> receipt : testResults)
        {
            OneLinkSupplyChainResult result = receipt.get();
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
        ExecutorService testRunner = Executors.newFixedThreadPool(5);
        ArrayList<Future<OneLinkSupplyChainResult>> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            Future<OneLinkSupplyChainResult> testReceipt =
                    testRunner.submit(new Callable<OneLinkSupplyChainResult>(){
                        /**
                         * Computes a result, or throws an exception if unable to do so.
                         *
                         * @return computed result
                         * @throws Exception if unable to compute a result
                         */
                        @Override
                        public OneLinkSupplyChainResult call() throws Exception {
                            return testFoodMonopolistWithFixedProductionRun(random.nextLong(), false, 100, 1, null);
                        }
                    });

            testResults.add(testReceipt);

        }

        for(Future<OneLinkSupplyChainResult> receipt : testResults)
        {
            checkResultsOfFoodMonopolist(receipt.get());
        }


    }

    private OneLinkSupplyChainResult testFoodMonopolistWithFixedProductionRun(long random, final boolean competitorsLearned, int speed, float divideGainsByThis,
                                                                              @Nullable File csvFileToWrite) {
        final MacroII macroII = new MacroII(random);
        final OneLinkSupplyChainScenarioCheatingBuyPriceAndForcedMonopolist scenario1 =
                new OneLinkSupplyChainScenarioCheatingBuyPriceAndForcedMonopolist(macroII, GoodType.FOOD)
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
        //Assert.assertEquals(macroII.getMarket(GoodType.FOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE),85l,6l );
        //but that's too hard because while on average the price hovers there, competition is noisy. Sometimes a lot.
        //so what I did was to attach a daily stat collector and then check the average of the last 10 prices
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


        System.out.println("beef price: " +averageBeefPrice.getMean() );
        System.out.println("food price: " + averageFoodPrice.getMean() );
        System.out.println("produced: " + averageBeefProduced.getMean() );
        System.out.println();

        return new OneLinkSupplyChainResult(averageBeefPrice.getMean(),averageFoodPrice.getMean(),averageBeefProduced.getMean());

    }
      


    private void checkResultsOfFoodMonopolist(OneLinkSupplyChainResult result) {
        System.out.println("done with food price: " + result.getFoodPrice() + " quantity " + result.getQuantity() );
        System.out.println("beef price: " + result.getBeefPrice() );
        System.out.println();
        //the food price is in the ballpark
        //with competition, you are better off testing an MA

        Assert.assertEquals(result.getBeefPrice(),17,4l );
        Assert.assertEquals(result.getFoodPrice(), 85l, 5l);
        Assert.assertEquals(result.getQuantity(),17,3);
    }

    @Test
    public void testFoodMonopolistWithSlowPIDAndFixedQuantity() throws ExecutionException, InterruptedException {

        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        ExecutorService testRunner = Executors.newFixedThreadPool(5);
        ArrayList<Future<OneLinkSupplyChainResult>> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            Future<OneLinkSupplyChainResult> testReceipt =
                    testRunner.submit(new Callable<OneLinkSupplyChainResult>(){
                        /**
                         * Computes a result, or throws an exception if unable to do so.
                         *
                         * @return computed result
                         * @throws Exception if unable to compute a result
                         */
                        @Override
                        public OneLinkSupplyChainResult call() throws Exception {
                            return testFoodMonopolistWithFixedProductionRun(random.nextLong(), false, 0, 100, null);
                        }
                    });

            testResults.add(testReceipt);

        }

        for(Future<OneLinkSupplyChainResult> receipt : testResults)
        {
            checkResultsOfFoodMonopolist(receipt.get());
        }

    }

    @Test
    public void testFoodMonopolistWithSlowPIDAndFixedQuantityAndLearnedCompetitors() throws ExecutionException, InterruptedException {

        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        ExecutorService testRunner = Executors.newFixedThreadPool(5);
        ArrayList<Future<OneLinkSupplyChainResult>> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            Future<OneLinkSupplyChainResult> testReceipt =
                    testRunner.submit(new Callable<OneLinkSupplyChainResult>(){
                        /**
                         * Computes a result, or throws an exception if unable to do so.
                         *
                         * @return computed result
                         * @throws Exception if unable to compute a result
                         */
                        @Override
                        public OneLinkSupplyChainResult call() throws Exception {
                            return testFoodMonopolistWithFixedProductionRun(random.nextLong(),true,0,100, null);//Paths.get("runs","supplychai","foodMonopolist.csv").toFile());
                        }
                    });

            testResults.add(testReceipt);

        }

        for(Future<OneLinkSupplyChainResult> receipt : testResults)
        {
            checkResultsOfFoodMonopolist(receipt.get());
        }

    }


    /**
     * force the beef monopolist to target the right production
     */
    @Test
    public void testFoodMonopolistWithStickyPricesAndFixedQuantityAndLearnedCompetitors() throws ExecutionException, InterruptedException {
        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        ExecutorService testRunner = Executors.newFixedThreadPool(5);
        ArrayList<Future<OneLinkSupplyChainResult>> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            Future<OneLinkSupplyChainResult> testReceipt =
                    testRunner.submit(new Callable<OneLinkSupplyChainResult>(){
                        /**
                         * Computes a result, or throws an exception if unable to do so.
                         *
                         * @return computed result
                         * @throws Exception if unable to compute a result
                         */
                        @Override
                        public OneLinkSupplyChainResult call() throws Exception {
                            return testFoodMonopolistWithFixedProductionRun(random.nextLong(),true,100,1, null);
                        }
                    });

            testResults.add(testReceipt);

        }

        for(Future<OneLinkSupplyChainResult> receipt : testResults)
        {
            checkResultsOfFoodMonopolist(receipt.get());
        }


    }


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // Now the food monopolist isn't told to produce the right amount, but it knows the price drops by 2 every increase in production
    //////////////////////////////////////////////////////////////////////////////////////////////////

    // @Test
    public  void alreadyLearnedFoodMonopolistSlowPID() throws ExecutionException, InterruptedException {


        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        ExecutorService testRunner = Executors.newFixedThreadPool(5);
        ArrayList<Future<OneLinkSupplyChainResult>> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            Future<OneLinkSupplyChainResult> testReceipt =
                    testRunner.submit(new Callable<OneLinkSupplyChainResult>(){
                        /**
                         * Computes a result, or throws an exception if unable to do so.
                         *
                         * @return computed result
                         * @throws Exception if unable to compute a result
                         */
                        @Override
                        public OneLinkSupplyChainResult call() throws Exception {
                            return alreadyLearnedFoodMonopolistSlowPIDRun(random.nextLong());
                        }
                    });

            testResults.add(testReceipt);

        }

        for(Future<OneLinkSupplyChainResult> receipt : testResults)
        {
            checkResultsOfFoodMonopolist(receipt.get());
        }



    }

    private OneLinkSupplyChainResult alreadyLearnedFoodMonopolistSlowPIDRun(long random) {
        final MacroII macroII = new MacroII(random);
        final OneLinkSupplyChainScenarioWithCheatingBuyingPrice scenario1 = new OneLinkSupplyChainScenarioWithCheatingBuyingPrice(macroII){

            @Override
            public void buildFoodPurchasesPredictor(PurchasesDepartment dept) {
                FixedIncreasePurchasesPredictor predictor  = FixedIncreasePurchasesPredictor.Factory.
                        newPurchasesPredictor(FixedIncreasePurchasesPredictor.class, dept);
                predictor.setIncrementDelta(1);
                dept.setPredictor(predictor);
            }


        };
        scenario1.setControlType(MarginalMaximizer.class);        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        scenario1.setBeefPriceFilterer(null);


        //competition!
        scenario1.setNumberOfBeefProducers(5);
        scenario1.setNumberOfFoodProducers(1);

        scenario1.setDivideProportionalGainByThis(100f);
        scenario1.setDivideIntegrativeGainByThis(100f);
        //no delay
        scenario1.setBeefPricingSpeed(0);


        macroII.setScenario(scenario1);
        macroII.start();



        while(macroII.schedule.getTime()<14000)
        {
            macroII.schedule.step(macroII);
            printProgressBar(14001,(int)macroII.schedule.getSteps(),100);
        }


        //I used to assert this:
        //Assert.assertEquals(macroII.getMarket(GoodType.FOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE),85l,6l );
        //but that's too hard because while on average the price hovers there, competition is noisy. Sometimes a lot.
        //so what I did was to attach a daily stat collector and then check the average of the last 10 prices
        float averageFoodPrice = 0;
        float averageBeefProduced = 0;
        float averageBeefPrice=0;
        for(int j=0; j< 1000; j++)
        {
            //make the model run one more day:
            macroII.schedule.step(macroII);
            averageFoodPrice += macroII.getMarket(GoodType.FOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE);
            averageBeefProduced+= macroII.getMarket(GoodType.BEEF).getYesterdayVolume();
            averageBeefPrice+= macroII.getMarket(GoodType.BEEF).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE);

        }

        return new OneLinkSupplyChainResult(averageBeefPrice/1000f,averageFoodPrice/1000f,averageBeefProduced/1000f);
    }

    //    @Test
    public  void alreadyLearnedFoodMonopolistStickyPID() throws ExecutionException, InterruptedException {


        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        ExecutorService testRunner = Executors.newFixedThreadPool(5);
        ArrayList<Future<OneLinkSupplyChainResult>> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            Future<OneLinkSupplyChainResult> testReceipt =
                    testRunner.submit(new Callable<OneLinkSupplyChainResult>(){
                        /**
                         * Computes a result, or throws an exception if unable to do so.
                         *
                         * @return computed result
                         * @throws Exception if unable to compute a result
                         */
                        @Override
                        public OneLinkSupplyChainResult call() throws Exception {
                            return alreadyLearnedFoodMonopolistStickyPIDRun(random.nextLong());
                        }
                    });



            testResults.add(testReceipt);

        }

        for(Future<OneLinkSupplyChainResult> receipt : testResults)
        {
            checkResultsOfFoodMonopolist(receipt.get());
        }


    }

    private OneLinkSupplyChainResult alreadyLearnedFoodMonopolistStickyPIDRun(long random) {
        final MacroII macroII = new MacroII(random);
        final OneLinkSupplyChainScenarioWithCheatingBuyingPrice scenario1 = new OneLinkSupplyChainScenarioWithCheatingBuyingPrice(macroII){

            @Override
            public void buildFoodPurchasesPredictor(PurchasesDepartment dept) {
                FixedIncreasePurchasesPredictor predictor  = FixedIncreasePurchasesPredictor.Factory.
                        newPurchasesPredictor(FixedIncreasePurchasesPredictor.class, dept);
                predictor.setIncrementDelta(1);
                dept.setPredictor(predictor);
            }



        };
        scenario1.setControlType(MarginalMaximizer.class);        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        scenario1.setBeefPriceFilterer(null);

        //competition!
        scenario1.setNumberOfBeefProducers(5);
        scenario1.setNumberOfFoodProducers(1);

        scenario1.setDivideProportionalGainByThis(1f);
        scenario1.setDivideIntegrativeGainByThis(1f);
        //no delay
        scenario1.setBeefPricingSpeed(100);


        macroII.setScenario(scenario1);
        macroII.start();


        while(macroII.schedule.getTime()<14000)
        {
            macroII.schedule.step(macroII);
            printProgressBar(14001,(int)macroII.schedule.getSteps(),100);
        }


        //I used to assert this:
        //Assert.assertEquals(macroII.getMarket(GoodType.FOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE),85l,6l );
        //but that's too hard because while on average the price hovers there, competition is noisy. Sometimes a lot.
        //so what I did was to attach a daily stat collector and then check the average of the last 10 prices
        float averageFoodPrice = 0;
        float averageBeefProduced = 0;
        float averageBeefPrice=0;
        for(int j=0; j< 1000; j++)
        {
            //make the model run one more day:
            macroII.schedule.step(macroII);
            averageFoodPrice += macroII.getMarket(GoodType.FOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE);
            averageBeefProduced+= macroII.getMarket(GoodType.BEEF).getYesterdayVolume();
            averageBeefPrice+= macroII.getMarket(GoodType.BEEF).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE);

        }

        return new OneLinkSupplyChainResult(averageBeefPrice/1000f,averageFoodPrice/1000f,averageBeefProduced/1000f);
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // These are the important ones!
    //////////////////////////////////////////////////////////////////////////////////////////////////


    // @Test
    public  void learningFoodMonopolistSlowPID() throws ExecutionException, InterruptedException {


        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        ExecutorService testRunner = Executors.newFixedThreadPool(5);
        ArrayList<Future<OneLinkSupplyChainResult>> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            Future<OneLinkSupplyChainResult> testReceipt =
                    testRunner.submit(new Callable<OneLinkSupplyChainResult>(){
                        /**
                         * Computes a result, or throws an exception if unable to do so.
                         *
                         * @return computed result
                         * @throws Exception if unable to compute a result
                         */
                        @Override
                        public OneLinkSupplyChainResult call() throws Exception {
                            return learningFoodMonopolistSlowPIDRun(random.nextLong());
                        }
                    });

            testResults.add(testReceipt);

        }

        for(Future<OneLinkSupplyChainResult> receipt : testResults)
        {
            checkResultsOfFoodMonopolist(receipt.get());
        }



    }

    private OneLinkSupplyChainResult learningFoodMonopolistSlowPIDRun(long random) {
        final MacroII macroII = new MacroII(random);
        final OneLinkSupplyChainScenarioWithCheatingBuyingPrice scenario1 = new OneLinkSupplyChainScenarioWithCheatingBuyingPrice(macroII);

        scenario1.setControlType(MarginalMaximizer.class);        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        scenario1.setBeefPriceFilterer(null);


        //competition!
        scenario1.setNumberOfBeefProducers(5);
        scenario1.setNumberOfFoodProducers(1);

        scenario1.setDivideProportionalGainByThis(100f);
        scenario1.setDivideIntegrativeGainByThis(100f);
        //no delay
        scenario1.setBeefPricingSpeed(0);


        macroII.setScenario(scenario1);
        macroII.start();


        while(macroII.schedule.getTime()<14000)
        {
            macroII.schedule.step(macroII);
            printProgressBar(14001,(int)macroII.schedule.getSteps(),100);
        }


        //I used to assert this:
        //Assert.assertEquals(macroII.getMarket(GoodType.FOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE),85l,6l );
        //but that's too hard because while on average the price hovers there, competition is noisy. Sometimes a lot.
        //so what I did was to attach a daily stat collector and then check the average of the last 10 prices
        float averageFoodPrice = 0;
        float averageBeefProduced = 0;
        float averageBeefPrice=0;
        for(int j=0; j< 1000; j++)
        {
            //make the model run one more day:
            macroII.schedule.step(macroII);
            averageFoodPrice += macroII.getMarket(GoodType.FOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE);
            averageBeefProduced+= macroII.getMarket(GoodType.BEEF).getYesterdayVolume();
            averageBeefPrice+= macroII.getMarket(GoodType.BEEF).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE);

        }

        return new OneLinkSupplyChainResult(averageBeefPrice/1000f,averageFoodPrice/1000f,averageBeefProduced/1000f);
    }


    // @Test
    public  void learningFoodMonopolistStickyPID() throws ExecutionException, InterruptedException {

        //this will take a looong time
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        ExecutorService testRunner = Executors.newFixedThreadPool(5);
        ArrayList<Future<OneLinkSupplyChainResult>> testResults = new ArrayList<>(5);

        //run the test 5 times!
        for(int i=0; i <5; i++)
        {
            //run the test, add it as a future so I can check the results!
            Future<OneLinkSupplyChainResult> testReceipt =
                    testRunner.submit(new Callable<OneLinkSupplyChainResult>(){
                        /**
                         * Computes a result, or throws an exception if unable to do so.
                         *
                         * @return computed result
                         * @throws Exception if unable to compute a result
                         */
                        @Override
                        public OneLinkSupplyChainResult call() throws Exception {
                            return learningFoodMonopolistStickyPIDRun(random.nextLong());
                        }
                    });

            testResults.add(testReceipt);

        }

        for(Future<OneLinkSupplyChainResult> receipt : testResults)
        {
            checkResultsOfFoodMonopolist(receipt.get());
        }



    }

    private OneLinkSupplyChainResult learningFoodMonopolistStickyPIDRun(long random) {
        final MacroII macroII = new MacroII(random);
        final OneLinkSupplyChainScenarioWithCheatingBuyingPrice scenario1 = new OneLinkSupplyChainScenarioWithCheatingBuyingPrice(macroII);
        scenario1.setControlType(MarginalMaximizer.class);        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        scenario1.setBeefPriceFilterer(null);

        //competition!
        scenario1.setNumberOfBeefProducers(5);
        scenario1.setNumberOfFoodProducers(1);

        scenario1.setDivideProportionalGainByThis(1f);
        scenario1.setDivideIntegrativeGainByThis(1f);
        //no delay
        scenario1.setBeefPricingSpeed(100);


        macroII.setScenario(scenario1);
        macroII.start();


        while(macroII.schedule.getTime()<14000)
        {
            macroII.schedule.step(macroII);
            printProgressBar(14001,(int)macroII.schedule.getSteps(),100);
        }


        //I used to assert this:
        //Assert.assertEquals(macroII.getMarket(GoodType.FOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE),85l,6l );
        //but that's too hard because while on average the price hovers there, competition is noisy. Sometimes a lot.
        //so what I did was to attach a daily stat collector and then check the average of the last 10 prices
        float averageFoodPrice = 0;
        float averageBeefProduced = 0;
        float averageBeefPrice=0;
        for(int j=0; j< 1000; j++)
        {
            //make the model run one more day:
            macroII.schedule.step(macroII);
            averageFoodPrice += macroII.getMarket(GoodType.FOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE);
            averageBeefProduced+= macroII.getMarket(GoodType.BEEF).getYesterdayVolume();
            averageBeefPrice+= macroII.getMarket(GoodType.BEEF).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE);

        }

        return new OneLinkSupplyChainResult(averageBeefPrice/1000f,averageFoodPrice/1000f,averageBeefProduced/1000f);
    }




}
