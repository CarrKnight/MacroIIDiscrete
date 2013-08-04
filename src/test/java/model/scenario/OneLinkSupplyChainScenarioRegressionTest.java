package model.scenario;

import agents.firm.Firm;
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
import org.junit.Assert;
import org.junit.Test;

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


    /**
     * With these parameters the beef seller waits for 100 days before changing its price
     */
   // @Test these two are disabled as long as I don't know what is the right result
    public void  testWithStickyPrices()
    {
        //this will take a looong time
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        for(int i=0; i <5; i++)
        {
            final MacroII macroII = new MacroII(random.nextLong());
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

            System.out.println("done with price: " +macroII.getMarket(GoodType.BEEF).getLastPrice() );
            System.out.println();
            //the beef price is in the ballpark
            Assert.assertTrue(macroII.getMarket(GoodType.BEEF).getLastPrice() >=27 && macroII.getMarket(GoodType.BEEF).getLastPrice() <=32 );

        }

    }


    /**
     * With these parameters the beef seller adjusts its prices everyday, but only ever so slightly!
     */
    // @Test these two are disabled as long as I don't know what is the right result
    public void testWithSlowPID()
    {

        //this will take a looong time
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        for(int i=0; i <5; i++)
        {
            final MacroII macroII = new MacroII(random.nextLong());
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

            System.out.println("done with price: " +macroII.getMarket(GoodType.BEEF).getLastPrice() );
            System.out.println();
            //the beef price is in the ballpark
            Assert.assertTrue(macroII.getMarket(GoodType.BEEF).getLastPrice() >=27 && macroII.getMarket(GoodType.BEEF).getLastPrice() <=32 );


        }
    }

    /********************************************************************************
     *
     * BEEF MONOPOLIST
     *
     ********************************************************************************/

    /////////////////////////////////////////////////////////////////////////////
    // Beef Monopolists with forced quantity (just check prices)
    //////////////////////////////////////////////////////////////////////////////

    /**
     * force the beef monopolist to target the right production
     */
    @Test
    public void testBeefMonopolistWithStickyPricesAndFixedQuantity()
    {
        //this will take a looong time
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        for(int i=0; i <5; i++)
        {
            final MacroII macroII = new MacroII(random.nextLong());
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


            while(macroII.schedule.getTime()<15000)
            {
                macroII.schedule.step(macroII);
                printProgressBar(15001,(int)macroII.schedule.getSteps(),100);
            }

            System.out.println("done with price: " +macroII.getMarket(GoodType.BEEF).getLastPrice() );
            System.out.println();
            //the beef price is in the ballpark
            Assert.assertEquals(macroII.getMarket(GoodType.BEEF).getLastPrice(),62l,5l );
            //I used to assert this:
            //Assert.assertEquals(macroII.getMarket(GoodType.FOOD).getLastPrice(),85l,6l );
            //but that's too hard because while on average the price hovers there, competition is noisy. Sometimes a lot.
            //so what I did was to attach a daily stat collector and then check the average of the last 10 prices
            float averageFoodPrice = 0;
            for(int j=0; j< 25; j++)
            {
                //make the model run one more day:
                macroII.schedule.step(macroII);
                averageFoodPrice += macroII.getMarket(GoodType.FOOD).getLastPrice();
            }
            Assert.assertEquals(averageFoodPrice/25f,85l,4l );

        }

    }

    @Test
    public void testBeefMonopolistWithSlowPIDAndFixedQuantity()
    {

        //this will take a looong time
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        for(int i=0; i <5; i++)
        {
            final MacroII macroII = new MacroII(random.nextLong());
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


            while(macroII.schedule.getTime()<15000)
            {
                macroII.schedule.step(macroII);
                printProgressBar(15001,(int)macroII.schedule.getSteps(),100);
            }

            System.out.println("done with price: " +macroII.getMarket(GoodType.BEEF).getLastPrice() );
            System.out.println();
            //the beef price is in the ballpark
            Assert.assertEquals(macroII.getMarket(GoodType.BEEF).getLastPrice(),62l,5l );
            Assert.assertEquals(macroII.getMarket(GoodType.FOOD).getLastPrice(),85l,5l );


        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // Now the beef monopolist isn't told to produce the right amount, but it knows the price drops by 2 every increase in production
    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public  void alreadyLearnedBeefMonopolistSlowPID(){


        //this will take a looong time
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());

        for(int i=0; i<5; i++)
        {

            final MacroII macroII = new MacroII(random.nextLong());
            final OneLinkSupplyChainScenarioWithCheatingBuyingPrice scenario1 = new OneLinkSupplyChainScenarioWithCheatingBuyingPrice(macroII){

                @Override
                protected void buildBeefSalesPredictor(SalesDepartment dept) {
                    FixedDecreaseSalesPredictor predictor  = SalesPredictor.Factory.newSalesPredictor(FixedDecreaseSalesPredictor.class, dept);
                    predictor.setDecrementDelta(2);
                    dept.setPredictorStrategy(predictor);
                }
            };
            scenario1.setControlType(MarginalMaximizerWithUnitPID.class);
            scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
            scenario1.setBeefPriceFilterer(null);



            //competition!
            scenario1.setNumberOfBeefProducers(1);
            scenario1.setNumberOfFoodProducers(5);

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


            Assert.assertEquals(((Firm) macroII.getMarket(GoodType.BEEF).getSellers().iterator().next()).getTotalWorkers(), 16, 2);
            Assert.assertEquals(macroII.getMarket(GoodType.BEEF).getLastPrice(), 62l, 6l);

            //I used to assert this:
            //Assert.assertEquals(macroII.getMarket(GoodType.FOOD).getLastPrice(),85l,6l );
            //but that's too hard because while on average the price hovers there, competition is noisy. Sometimes a lot.
            //so what I did was to attach a daily stat collector and then check the average of the last 10 prices
            float averageFoodPrice = 0;
            for(int j=0; j< 25; j++)
            {
                //make the model run one more day:
                macroII.schedule.step(macroII);
                averageFoodPrice += macroII.getMarket(GoodType.FOOD).getLastPrice();
            }
            Assert.assertEquals(averageFoodPrice/25f,85l,4l );

        }



    }

    @Test
    public  void alreadyLearnedBeefMonopolistStickyPID(){

        //this will take a looong time
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());

        for(int i=0; i<5; i++)
        {


            final MacroII macroII = new MacroII(random.nextLong());
            final OneLinkSupplyChainScenarioWithCheatingBuyingPrice scenario1 = new OneLinkSupplyChainScenarioWithCheatingBuyingPrice(macroII){

                @Override
                protected void buildBeefSalesPredictor(SalesDepartment dept) {
                    FixedDecreaseSalesPredictor predictor  = SalesPredictor.Factory.newSalesPredictor(FixedDecreaseSalesPredictor.class, dept);
                    predictor.setDecrementDelta(2);
                    dept.setPredictorStrategy(predictor);
                }
            };
            scenario1.setControlType(MarginalMaximizerWithUnitPID.class);
            scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
            scenario1.setBeefPriceFilterer(null);

            //competition!
            scenario1.setNumberOfBeefProducers(1);
            scenario1.setNumberOfFoodProducers(5);

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


            Assert.assertEquals(((Firm)macroII.getMarket(GoodType.BEEF).getSellers().iterator().next()).getTotalWorkers(),16,2);
            Assert.assertEquals(macroII.getMarket(GoodType.BEEF).getLastPrice(),62l,6l );

            //I used to assert this:
            //Assert.assertEquals(macroII.getMarket(GoodType.FOOD).getLastPrice(),85l,6l );
            //but that's too hard because while on average the price hovers there, competition is noisy. Sometimes a lot.
            //so what I did was to attach a daily stat collector and then check the average of the last 10 prices
            float averageFoodPrice = 0;
            for(int j=0; j< 25; j++)
            {
                //make the model run one more day:
                macroII.schedule.step(macroII);
                averageFoodPrice += macroII.getMarket(GoodType.FOOD).getLastPrice();
            }
            Assert.assertEquals(averageFoodPrice/25f,85l,4l );


        }


    }


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // These are the important ones!
    //////////////////////////////////////////////////////////////////////////////////////////////////


    @Test
    public  void learningBeefMonopolistSlowPID(){


        //this will take a looong time
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());

        for(int i=0; i<5; i++)
        {

            final MacroII macroII = new MacroII(random.nextLong());
            final OneLinkSupplyChainScenarioWithCheatingBuyingPrice scenario1 = new OneLinkSupplyChainScenarioWithCheatingBuyingPrice(macroII);

            scenario1.setControlType(MarginalMaximizerWithUnitPID.class);
            scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
            scenario1.setBeefPriceFilterer(null);



            //competition!
            scenario1.setNumberOfBeefProducers(1);
            scenario1.setNumberOfFoodProducers(5);

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


            Assert.assertEquals(((Firm)macroII.getMarket(GoodType.BEEF).getSellers().iterator().next()).getTotalWorkers(),16,2);
            Assert.assertEquals(macroII.getMarket(GoodType.BEEF).getLastPrice(),62l,6l );

            //I used to assert this:
            //Assert.assertEquals(macroII.getMarket(GoodType.FOOD).getLastPrice(),85l,6l );
            //but that's too hard because while on average the price hovers there, competition is noisy. Sometimes a lot.
            //so what I did was to attach a daily stat collector and then check the average of the last 10 prices
            float averageFoodPrice = 0;
            for(int j=0; j< 25; j++)
            {
                //make the model run one more day:
                macroII.schedule.step(macroII);
                averageFoodPrice += macroII.getMarket(GoodType.FOOD).getLastPrice();
            }
            Assert.assertEquals(averageFoodPrice/25f,85l,4l );

        }



    }


    @Test
    public  void learningBeefMonopolistStickyPID(){

        //this will take a looong time
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());

        for(int i=0; i<5; i++)
        {


            final MacroII macroII = new MacroII(random.nextLong());
            final OneLinkSupplyChainScenarioWithCheatingBuyingPrice scenario1 = new OneLinkSupplyChainScenarioWithCheatingBuyingPrice(macroII);
            scenario1.setControlType(MarginalMaximizerWithUnitPID.class);
            scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
            scenario1.setBeefPriceFilterer(null);

            //competition!
            scenario1.setNumberOfBeefProducers(1);
            scenario1.setNumberOfFoodProducers(5);

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


            Assert.assertEquals(((Firm)macroII.getMarket(GoodType.BEEF).getSellers().iterator().next()).getTotalWorkers(),16,2);
            Assert.assertEquals(macroII.getMarket(GoodType.BEEF).getLastPrice(),62l,6l );

            //I used to assert this:
            //Assert.assertEquals(macroII.getMarket(GoodType.FOOD).getLastPrice(),85l,6l );
            //but that's too hard because while on average the price hovers there, competition is noisy. Sometimes a lot.
            //so what I did was to attach a daily stat collector and then check the average of the last 10 prices
            float averageFoodPrice = 0;
            for(int j=0; j< 25; j++)
            {
                //make the model run one more day:
                macroII.schedule.step(macroII);
                averageFoodPrice += macroII.getMarket(GoodType.FOOD).getLastPrice();
            }
            Assert.assertEquals(averageFoodPrice/25f,85l,4l );


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
    public void testFoodMonopolistWithStickyPricesAndFixedQuantity()
    {
        //this will take a looong time
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        for(int i=0; i <5; i++)
        {
            final MacroII macroII = new MacroII(random.nextLong());
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


            while(macroII.schedule.getTime()<15000)
            {
                macroII.schedule.step(macroII);
                printProgressBar(15001,(int)macroII.schedule.getSteps(),100);
            }




            checkResultsOfFoodMonopolist(macroII);


        }

    }

    private void checkResultsOfFoodMonopolist(MacroII macroII) {
        System.out.println("done with food price: " +macroII.getMarket(GoodType.FOOD).getLastPrice() );
        System.out.println();
        //the food price is in the ballpark
        Assert.assertEquals(macroII.getMarket(GoodType.FOOD).getLastPrice(), 85l, 5l);
        //with competition, you are better off testing an MA
        float averageBeefPrice = 0;
        for(int j=0; j< 25; j++)
        {
            //make the model run one more day:
            macroII.schedule.step(macroII);
            averageBeefPrice += macroII.getMarket(GoodType.BEEF).getLastPrice();
        }
        Assert.assertEquals(averageBeefPrice/25f,23l,4l );
        //
        Assert.assertEquals(macroII.getMarket(GoodType.FOOD).getYesterdayVolume(),16,2);
        Assert.assertEquals(macroII.getMarket(GoodType.BEEF).getYesterdayVolume(),16,2);
    }

    @Test
    public void testFoodMonopolistWithSlowPIDAndFixedQuantity()
    {

        //this will take a looong time
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        for(int i=0; i <5; i++)
        {
            final MacroII macroII = new MacroII(random.nextLong());
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


            while(macroII.schedule.getTime()<15000)
            {
                macroII.schedule.step(macroII);
                printProgressBar(15001,(int)macroII.schedule.getSteps(),100);
            }

            checkResultsOfFoodMonopolist(macroII);



        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // Now the beef monopolist isn't told to produce the right amount, but it knows the price drops by 2 every increase in production
    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public  void alreadyLearnedFoodMonopolistSlowPID(){


        //this will take a looong time
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());

        for(int i=0; i<5; i++)
        {

            final MacroII macroII = new MacroII(random.nextLong());
            final OneLinkSupplyChainScenarioWithCheatingBuyingPrice scenario1 = new OneLinkSupplyChainScenarioWithCheatingBuyingPrice(macroII){

                @Override
                public void buildFoodPurchasesDepartment(PurchasesDepartment dept) {
                    FixedIncreasePurchasesPredictor predictor  = FixedIncreasePurchasesPredictor.Factory.
                            newPurchasesPredictor(FixedIncreasePurchasesPredictor.class,dept);
                    predictor.setIncrementDelta(10f/7f);
                    dept.setPredictor(predictor);
                }
            };
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


            while(macroII.schedule.getTime()<15000)
            {
                macroII.schedule.step(macroII);
                printProgressBar(15001,(int)macroII.schedule.getSteps(),100);
            }

            checkResultsOfFoodMonopolist(macroII);

        }



    }

    @Test
    public  void alreadyLearnedFoodMonopolistStickyPID(){

        //this will take a looong time
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());

        for(int i=0; i<5; i++)
        {


            final MacroII macroII = new MacroII(random.nextLong());
            final OneLinkSupplyChainScenarioWithCheatingBuyingPrice scenario1 = new OneLinkSupplyChainScenarioWithCheatingBuyingPrice(macroII){

                @Override
                public void buildFoodPurchasesDepartment(PurchasesDepartment dept) {
                    FixedIncreasePurchasesPredictor predictor  = FixedIncreasePurchasesPredictor.Factory.
                            newPurchasesPredictor(FixedIncreasePurchasesPredictor.class,dept);
                    predictor.setIncrementDelta(10f/7f);
                    dept.setPredictor(predictor);
                }
            };
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


            while(macroII.schedule.getTime()<15000)
            {
                macroII.schedule.step(macroII);
                printProgressBar(15001,(int)macroII.schedule.getSteps(),100);
            }


            checkResultsOfFoodMonopolist(macroII);



        }


    }


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // These are the important ones!
    //////////////////////////////////////////////////////////////////////////////////////////////////


    @Test
    public  void learningFoodMonopolistSlowPID(){


        //this will take a looong time
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());

        for(int i=0; i<5; i++)
        {

            final MacroII macroII = new MacroII(random.nextLong());
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


            while(macroII.schedule.getTime()<15000)
            {
                macroII.schedule.step(macroII);
                printProgressBar(15001,(int)macroII.schedule.getSteps(),100);
            }


            checkResultsOfFoodMonopolist(macroII);


        }



    }


    @Test
    public  void learningFoodMonopolistStickyPID(){

        //this will take a looong time
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());

        for(int i=0; i<5; i++)
        {


            final MacroII macroII = new MacroII(random.nextLong());
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


            while(macroII.schedule.getTime()<15000)
            {
                macroII.schedule.step(macroII);
                printProgressBar(15001,(int)macroII.schedule.getSteps(),100);
            }


            checkResultsOfFoodMonopolist(macroII);


        }


    }



}
