package model.scenario;

import agents.firm.production.control.maximizer.algorithms.marginalMaximizers.MarginalMaximizerWithUnitPID;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.prediction.FixedIncreasePurchasesPredictor;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.prediction.FixedDecreaseSalesPredictor;
import agents.firm.sales.prediction.SalesPredictor;
import ec.util.MersenneTwisterFast;
import goods.GoodType;
import model.MacroII;
import model.utilities.filters.ExponentialFilter;
import model.utilities.filters.MovingAverage;
import model.utilities.stats.collectors.enums.MarketDataType;
import org.junit.Assert;

import java.util.ArrayList;
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
        scenario1.setControlType(MarginalMaximizerWithUnitPID.class);
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
        scenario1.setControlType(MarginalMaximizerWithUnitPID.class);
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
    //@Test
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
                            return beefMonopolistFixedProductionWithStickyPricesOneRun(random.nextLong());
                        }
                    });

            testResults.add(testReceipt);

        }

        for(Future<OneLinkSupplyChainResult> receipt : testResults)
        {
            OneLinkSupplyChainResult result = receipt.get();
            Assert.assertEquals(result.getBeefPrice(), 62l, 6l);
            Assert.assertEquals(result.getFoodPrice(),85l,6l );
        }





    }

    private OneLinkSupplyChainResult beefMonopolistFixedProductionWithStickyPricesOneRun(long seed) {
        final MacroII macroII = new MacroII(seed);
        final OneLinkSupplyChainScenarioCheatingBuyPriceAndForcedMonopolist scenario1 = new OneLinkSupplyChainScenarioCheatingBuyPriceAndForcedMonopolist(macroII, GoodType.BEEF);
        scenario1.setControlType(MarginalMaximizerWithUnitPID.class);
        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        //use standard PID parameters
        scenario1.setDivideProportionalGainByThis(1f);
        scenario1.setDivideIntegrativeGainByThis(1f);
        //100 days delay
        scenario1.setBeefPricingSpeed(100);
        //no need for filter with the cheating price
        scenario1.setBeefPriceFilterer(null);


        macroII.setScenario(scenario1);
        macroII.start();


        MovingAverage<Double> averageFoodPrice = new MovingAverage<>(500);
        MovingAverage<Double> averageBeefPrice = new MovingAverage<>(500);
        MovingAverage<Integer> averageBeefTraded = new MovingAverage<>(500);
        while(macroII.schedule.getTime()<15000)
        {
            macroII.schedule.step(macroII);
            printProgressBar(15001,(int)macroII.schedule.getSteps(),100);
            if(macroII.schedule.getTime() >= 14500)
            {
                averageFoodPrice.addObservation(macroII.getMarket(GoodType.FOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE));
                averageBeefPrice.addObservation(macroII.getMarket(GoodType.BEEF).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE));
                averageBeefTraded.addObservation(macroII.getMarket(GoodType.BEEF).getYesterdayVolume());

            }
        }

        System.out.println("done with price: " +macroII.getMarket(GoodType.BEEF).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE) );
        System.out.println();
        //the beef price is in the ballpark


        return new OneLinkSupplyChainResult(averageBeefPrice.getSmoothedObservation(),
                averageFoodPrice.getSmoothedObservation(),averageBeefTraded.getSmoothedObservation());
    }

    //@Test
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
                            return             testBeefMonopolistFixedProductionWithSlowPIDOneRun(random.nextLong());
                        }
                    });

            testResults.add(testReceipt);

        }

        for(Future<OneLinkSupplyChainResult> receipt : testResults)
        {
            OneLinkSupplyChainResult result = receipt.get();
            Assert.assertEquals(result.getBeefPrice(), 62l, 6l);
            Assert.assertEquals(result.getFoodPrice(),85l,6l );
        }


    }

    private OneLinkSupplyChainResult testBeefMonopolistFixedProductionWithSlowPIDOneRun(long seed) {
        final MacroII macroII = new MacroII(seed);
        final OneLinkSupplyChainScenarioCheatingBuyPriceAndForcedMonopolist scenario1 = new OneLinkSupplyChainScenarioCheatingBuyPriceAndForcedMonopolist(macroII, GoodType.BEEF);
        scenario1.setControlType(MarginalMaximizerWithUnitPID.class);
        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        //divide standard PID parameters by 100
        scenario1.setDivideProportionalGainByThis(100f);
        scenario1.setDivideIntegrativeGainByThis(100f);
        //no delay
        scenario1.setBeefPricingSpeed(0);
        //no real need of filter at this slow speed
        scenario1.setBeefPriceFilterer(null);


        macroII.setScenario(scenario1);
        macroII.start();


        MovingAverage<Double> averageFoodPrice = new MovingAverage<>(500);
        MovingAverage<Double> averageBeefPrice = new MovingAverage<>(500);
        MovingAverage<Integer> averageBeefTraded = new MovingAverage<>(500);

        while(macroII.schedule.getTime()<15000)
        {
            macroII.schedule.step(macroII);
            printProgressBar(15001,(int)macroII.schedule.getSteps(),100);
            if(macroII.schedule.getTime() >= 14500)
            {
                averageFoodPrice.addObservation(macroII.getMarket(GoodType.FOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE));
                averageBeefPrice.addObservation(macroII.getMarket(GoodType.BEEF).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE));
                averageBeefTraded.addObservation(macroII.getMarket(GoodType.BEEF).getYesterdayVolume());

            }

        }






        System.out.println("done with price: " +macroII.getMarket(GoodType.BEEF).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE) );
        System.out.println();

        return new OneLinkSupplyChainResult(averageBeefPrice.getSmoothedObservation(),
                averageFoodPrice.getSmoothedObservation(),averageBeefTraded.getSmoothedObservation());
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // Now the beef monopolist isn't told to produce the right amount, but it knows the price drops by 2 every increase in production
    //////////////////////////////////////////////////////////////////////////////////////////////////

   //@Test
    public  void alreadyLearnedBeefMonopolistSlowPID() throws ExecutionException, InterruptedException {

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
                            return             alreadyLearnedBeefMonopolistSlowPIDRun(random.nextLong());
                        }
                    });

            testResults.add(testReceipt);

        }

        for(Future<OneLinkSupplyChainResult> receipt : testResults)
        {
            OneLinkSupplyChainResult result = receipt.get();
            Assert.assertEquals(result.getBeefPrice(), 62l, 5l);
            Assert.assertEquals(result.getFoodPrice(),85l,5l );
            Assert.assertEquals(result.getQuantity(), 16, 2);
        }



    }

    private OneLinkSupplyChainResult alreadyLearnedBeefMonopolistSlowPIDRun(long random) {
        final MacroII macroII = new MacroII(random);
        final OneLinkSupplyChainScenarioWithCheatingBuyingPrice scenario1 = new OneLinkSupplyChainScenarioWithCheatingBuyingPrice(macroII){

            @Override
            protected void buildBeefSalesPredictor(SalesDepartment dept) {
                FixedDecreaseSalesPredictor predictor  = SalesPredictor.Factory.newSalesPredictor(FixedDecreaseSalesPredictor.class, dept);
                predictor.setDecrementDelta(17f/7f);
                dept.setPredictorStrategy(predictor);
            }


        };
        scenario1.setControlType(MarginalMaximizerWithUnitPID.class);
        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        scenario1.setBeefPriceFilterer(null);

        //competition!
        scenario1.setNumberOfBeefProducers(1);
        scenario1.setNumberOfFoodProducers(10);

        scenario1.setDivideProportionalGainByThis(100f);
        scenario1.setDivideIntegrativeGainByThis(100f);
        //no delay
        scenario1.setBeefPricingSpeed(0);


        macroII.setScenario(scenario1);
        macroII.start();


        while(macroII.schedule.getTime()<15000)
        {
            macroII.schedule.step(macroII);
            printProgressBar(15001,(int)macroII.schedule.getSteps(),100);
        }

        return new OneLinkSupplyChainResult(macroII.getMarket(GoodType.BEEF).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE),
                macroII.getMarket(GoodType.FOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE),macroII.getMarket(GoodType.BEEF).getYesterdayVolume());


    }


   // @Test
    public  void alreadyLearnedBeefMonopolistStickyPID() throws ExecutionException, InterruptedException {

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
                            return             alreadyLearnedBeefMonopolistStickPIDRun(random.nextLong());
                        }
                    });

            testResults.add(testReceipt);

        }

        for(Future<OneLinkSupplyChainResult> receipt : testResults)
        {
            OneLinkSupplyChainResult result = receipt.get();
            Assert.assertEquals(result.getBeefPrice(), 62l, 5l);
            Assert.assertEquals(result.getFoodPrice(),85l,5l );
            Assert.assertEquals(result.getQuantity(), 16, 2);
        }

    }

    private OneLinkSupplyChainResult alreadyLearnedBeefMonopolistStickPIDRun(long random) {
        final MacroII macroII = new MacroII(random);
        final OneLinkSupplyChainScenarioWithCheatingBuyingPrice scenario1 = new OneLinkSupplyChainScenarioWithCheatingBuyingPrice(macroII){

            @Override
            protected void buildBeefSalesPredictor(SalesDepartment dept) {
                FixedDecreaseSalesPredictor predictor  = SalesPredictor.Factory.newSalesPredictor(FixedDecreaseSalesPredictor.class, dept);
                predictor.setDecrementDelta(17f/7f);
                dept.setPredictorStrategy(predictor);
            }

        };
        scenario1.setControlType(MarginalMaximizerWithUnitPID.class);
        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        scenario1.setBeefPriceFilterer(null);

        //competition!
        scenario1.setNumberOfBeefProducers(1);
        scenario1.setNumberOfFoodProducers(10);

        scenario1.setDivideProportionalGainByThis(1f);
        scenario1.setDivideIntegrativeGainByThis(1f);
        //no delay
        scenario1.setBeefPricingSpeed(100);


        macroII.setScenario(scenario1);
        macroII.start();


        while(macroII.schedule.getTime()<15000)
        {
            macroII.schedule.step(macroII);
            printProgressBar(15001,(int)macroII.schedule.getSteps(),100);
        }



        return new OneLinkSupplyChainResult(macroII.getMarket(GoodType.BEEF).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE),
                macroII.getMarket(GoodType.FOOD).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE),macroII.getMarket(GoodType.BEEF).getYesterdayVolume());
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // These are the important ones!
    //////////////////////////////////////////////////////////////////////////////////////////////////


   // @Test
    public  void learningBeefMonopolistSlowPID() throws ExecutionException, InterruptedException {


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
                            return             learningBeefMonopolistSlowPIDRun(random.nextLong());
                        }
                    });

            testResults.add(testReceipt);

        }

        for(Future<OneLinkSupplyChainResult> receipt : testResults)
        {
            OneLinkSupplyChainResult result = receipt.get();
            Assert.assertEquals(result.getBeefPrice(), 62l, 5l);
            Assert.assertEquals(result.getFoodPrice(),85l,5l );
            Assert.assertEquals(result.getQuantity(), 16, 2);
        }



    }

    private OneLinkSupplyChainResult learningBeefMonopolistSlowPIDRun(long random) {
        final MacroII macroII = new MacroII(random);
        final OneLinkSupplyChainScenarioWithCheatingBuyingPrice scenario1 = new OneLinkSupplyChainScenarioWithCheatingBuyingPrice(macroII);

        scenario1.setControlType(MarginalMaximizerWithUnitPID.class);
        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        scenario1.setBeefPriceFilterer(null);


        //competition!
        scenario1.setNumberOfBeefProducers(1);
        scenario1.setNumberOfFoodProducers(10);

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
    public  void learningBeefMonopolistStickyPID() throws ExecutionException, InterruptedException {

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
                            return             learningBeefMonopolistStickyPIDRun(random.nextLong());
                        }
                    });

            testResults.add(testReceipt);

        }

        for(Future<OneLinkSupplyChainResult> receipt : testResults)
        {
            OneLinkSupplyChainResult result = receipt.get();
            Assert.assertEquals(result.getBeefPrice(), 62l, 5l);
            Assert.assertEquals(result.getFoodPrice(),85l,5l );
            Assert.assertEquals(result.getQuantity(), 16, 2);
        }



    }

    private OneLinkSupplyChainResult learningBeefMonopolistStickyPIDRun(long random) {
        final MacroII macroII = new MacroII(random);
        final OneLinkSupplyChainScenarioWithCheatingBuyingPrice scenario1 = new OneLinkSupplyChainScenarioWithCheatingBuyingPrice(macroII);
        scenario1.setControlType(MarginalMaximizerWithUnitPID.class);
        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        scenario1.setBeefPriceFilterer(null);

        //competition!
        scenario1.setNumberOfBeefProducers(1);
        scenario1.setNumberOfFoodProducers(10);

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
   // @Test
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
                            return testFoodMonopolistWithStickyPricesAndFixedQuantityRun(random.nextLong());
                        }
                    });

            testResults.add(testReceipt);

        }

        for(Future<OneLinkSupplyChainResult> receipt : testResults)
        {
            checkResultsOfFoodMonopolist(receipt.get());
        }


    }

    private OneLinkSupplyChainResult testFoodMonopolistWithStickyPricesAndFixedQuantityRun(long random) {
        final MacroII macroII = new MacroII(random);
        final OneLinkSupplyChainScenarioCheatingBuyPriceAndForcedMonopolist scenario1 =
                new OneLinkSupplyChainScenarioCheatingBuyPriceAndForcedMonopolist(macroII, GoodType.FOOD);
        scenario1.setControlType(MarginalMaximizerWithUnitPID.class);
        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        //use standard PID parameters
        scenario1.setDivideProportionalGainByThis(1f);
        scenario1.setDivideIntegrativeGainByThis(1f);
        //100 days delay
        scenario1.setBeefPricingSpeed(100);
        //no need for filter with the cheating price
        scenario1.setBeefPriceFilterer(null);


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

    private void checkResultsOfFoodMonopolist(OneLinkSupplyChainResult result) {
        System.out.println("done with food price: " + result.getFoodPrice() + " quantity " + result.getQuantity() );
        System.out.println("beef price: " + result.getBeefPrice() );
        System.out.println();
        //the food price is in the ballpark
        //with competition, you are better off testing an MA

        Assert.assertEquals(result.getBeefPrice(),23l,4l );
        Assert.assertEquals(result.getFoodPrice(), 85l, 5l);
        Assert.assertEquals(result.getQuantity(),16,2);
    }

    //@Test
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
                            return testFoodMonopolistWithSlowPIDAndFixedQuantityRun(random.nextLong());
                        }
                    });

            testResults.add(testReceipt);

        }

        for(Future<OneLinkSupplyChainResult> receipt : testResults)
        {
            checkResultsOfFoodMonopolist(receipt.get());
        }

    }

    private OneLinkSupplyChainResult testFoodMonopolistWithSlowPIDAndFixedQuantityRun(long random) {
        final MacroII macroII = new MacroII(random);
        final OneLinkSupplyChainScenarioCheatingBuyPriceAndForcedMonopolist scenario1 = new OneLinkSupplyChainScenarioCheatingBuyPriceAndForcedMonopolist(macroII, GoodType.FOOD);
        scenario1.setControlType(MarginalMaximizerWithUnitPID.class);
        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        //divide standard PID parameters by 100
        scenario1.setDivideProportionalGainByThis(100f);
        scenario1.setDivideIntegrativeGainByThis(100f);
        //no delay
        scenario1.setBeefPricingSpeed(0);
        //no real need of filter at this slow speed
        scenario1.setBeefPriceFilterer(null);


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
                predictor.setIncrementDelta(10f/7f);
                dept.setPredictor(predictor);
            }


        };
        scenario1.setControlType(MarginalMaximizerWithUnitPID.class);
        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        scenario1.setBeefPriceFilterer(null);


        //competition!
        scenario1.setNumberOfBeefProducers(10);
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
                predictor.setIncrementDelta(10f/7f);
                dept.setPredictor(predictor);
            }



        };
        scenario1.setControlType(MarginalMaximizerWithUnitPID.class);
        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        scenario1.setBeefPriceFilterer(null);

        //competition!
        scenario1.setNumberOfBeefProducers(10);
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

        scenario1.setControlType(MarginalMaximizerWithUnitPID.class);
        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
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
        scenario1.setControlType(MarginalMaximizerWithUnitPID.class);
        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
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
