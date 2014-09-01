/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.scenario;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.purchases.prediction.ErrorCorrectingPurchasePredictor;
import agents.firm.purchases.prediction.FixedIncreasePurchasesPredictor;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.prediction.FixedDecreaseSalesPredictor;
import agents.firm.sales.prediction.MarketSalesPredictor;
import agents.firm.sales.pricing.AskPricingStrategy;
import agents.firm.sales.pricing.pid.InventoryBufferSalesControl;
import agents.firm.sales.pricing.pid.SalesControlWithFixedInventoryAndPID;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.utilities.stats.collectors.enums.MarketDataType;
import model.utilities.stats.collectors.enums.SalesDataType;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
 * @version 2013-08-15
 * @see
 */
public class CompetitiveScenarioTest {



    @Test
    public void rightPriceAndQuantityTestAsHillClimber()
    {

        //run the test 5 times
        for(int i=0; i<10; i++)
        {
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            final TripolistScenario scenario1 = new TripolistScenario(macroII);
            scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
            scenario1.setAskPricingStrategy(SimpleFlowSellerPID.class);
            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.HILL_CLIMBER_ALWAYS_MOVING);
            scenario1.setAdditionalCompetitors(3);
            scenario1.setWorkersToBeRehiredEveryDay(false);
            scenario1.setDemandIntercept(102);

            // scenario1.setSalesPricePreditorStrategy(FixedDecreaseSalesPredictor.class);
            scenario1.setSalesPricePreditorStrategy(MarketSalesPredictor.class);
            //   scenario1.setPurchasesPricePreditorStrategy(PricingPurchasesPredictor.class);



            //assign scenario
            macroII.setScenario(scenario1);

            macroII.start();

            while(macroII.schedule.getTime()<5000)
                macroII.schedule.step(macroII);

            float averagePrice = 0;
            float averageQ = 0;
            for(int j=0; j<500; j++)
            {
                macroII.schedule.step(macroII);
                averagePrice += macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayAveragePrice();
                averageQ += macroII.getMarket(UndifferentiatedGoodType.GENERIC).countTodayProductionByRegisteredSellers();

            }
            averagePrice = averagePrice/500f;
            averageQ = averageQ/500f;

            System.out.println(averagePrice + " - " + averageQ );
            assertEquals(averagePrice, 72,5);
            assertEquals(averageQ, 29,5);
        }





    }



    @Test
    public void rightPriceAndQuantityLearningFlows()
    {

        for(int competitors=4;competitors<=7;competitors++)
        {
            System.out.println("FORCED COMPETITIVE FIRMS: " + (competitors+1));
            for(int i=0; i<5; i++)
            {

                final MacroII macroII = new MacroII(System.currentTimeMillis());
                final TripolistScenario scenario1 = new TripolistScenario(macroII);
                scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
                scenario1.setAskPricingStrategy(InventoryBufferSalesControl.class);
                scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
                scenario1.setAdditionalCompetitors(competitors);
                scenario1.setWorkersToBeRehiredEveryDay(true);
                scenario1.setDemandIntercept(102);



                //assign scenario
                macroII.setScenario(scenario1);

                macroII.start();
                macroII.schedule.step(macroII);


                try {
                final HumanResources hr = scenario1.getMonopolist().getHRs().iterator().next();
                final ErrorCorrectingPurchasePredictor predictor = new ErrorCorrectingPurchasePredictor(macroII, hr);
                hr.setPredictor(predictor);

                    predictor.setDebugWriter(Paths.get("runs", "tmp.csv"));
                } catch (IOException e) {
                    e.printStackTrace();
                }


                while(macroII.schedule.getTime()<8000)
                {
                    macroII.schedule.step(macroII);
              /*      System.out.println("sales: " + scenario1.getCompetitors().get(0).getSalesDepartment(GoodType.GENERIC).
                            getLatestObservation(SalesDataType.OUTFLOW) +","
                            + scenario1.getCompetitors().get(1).getSalesDepartment(GoodType.GENERIC).
                            getLatestObservation(SalesDataType.OUTFLOW));
                */


                }
                SummaryStatistics prices = new SummaryStatistics();
                SummaryStatistics quantities = new SummaryStatistics();
                SummaryStatistics target = new SummaryStatistics();
                for(int j=0; j<500; j++)
                {
                    macroII.schedule.step(macroII);
                    assert !Float.isNaN(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayAveragePrice());
                    prices.addValue(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayAveragePrice());
                    quantities.addValue(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayVolume());

                    for(EconomicAgent agent : macroII.getMarket(UndifferentiatedGoodType.GENERIC).getSellers())
                    {
                        SalesDepartment department = ((Firm) agent).getSalesDepartment(UndifferentiatedGoodType.GENERIC);
                        target.addValue(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayVolume());
                    }


                }


                System.out.println(prices.getMean() + " - " + quantities.getMean() +"/" +target.getMean()+ "----" + macroII.seed() + " | " + macroII.getMarket(UndifferentiatedGoodType.GENERIC).getLastDaysAveragePrice());
                System.out.println("standard deviations: price : " + prices.getStandardDeviation() + " , quantity: " + quantities.getStandardDeviation());
                printSlopes(scenario1);


                if(competitors>=4)
                {
                    assertEquals(prices.getMean(), 58, 5);
                    assertTrue(prices.getStandardDeviation() < 5.5);
                    assertEquals(quantities.getMean(), 44,5);
                    assertTrue(quantities.getStandardDeviation() < 5.5);
                }
                macroII.finish();
            }


        }




    }

    public static void printSlopes(TripolistScenario scenario1) {
        int additionalCompetitors = scenario1.getAdditionalCompetitors();
        //slopes
        double[] salesSlopes = new double[additionalCompetitors+1];
        double[] hrSlopes = new double[additionalCompetitors+1];
        final LinkedList<Firm> competitorList = scenario1.getCompetitors();
        for(int k=0; k<salesSlopes.length; k++)
        {
            salesSlopes[k] =competitorList.get(k).getSalesDepartment(UndifferentiatedGoodType.GENERIC).getLatestObservation(SalesDataType.PREDICTED_DEMAND_SLOPE);
            final HumanResources hr = competitorList.get(k).getHRs().iterator().next();
            hrSlopes[k] =  hr.predictPurchasePriceWhenIncreasingProduction()-hr.predictPurchasePriceWhenNoChangeInProduction();
        }
        System.out.println("learned sales slopes: " + Arrays.toString(salesSlopes));
        System.out.println("learned purchases slopes: " + Arrays.toString(hrSlopes));
    }

    @Test
    public void rightPriceAndQuantityLearningInventory()
    {

        for(int competitors=4;competitors<=7;competitors++)
        {
            System.out.println("FORCED COMPETITIVE FIRMS: " + (competitors+1));
            for(int i=0; i<5; i++)
            {

                final MacroII macroII = new MacroII(System.currentTimeMillis());
                final TripolistScenario scenario1 = new TripolistScenario(macroII);
                scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
                scenario1.setAskPricingStrategy(SalesControlWithFixedInventoryAndPID.class);
                scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
                scenario1.setAdditionalCompetitors(competitors);
                scenario1.setWorkersToBeRehiredEveryDay(true);
                scenario1.setDemandIntercept(102);



                //assign scenario
                macroII.setScenario(scenario1);

                macroII.start();


                while(macroII.schedule.getTime()<8000)
                {
                    macroII.schedule.step(macroII);
              /*      System.out.println("sales: " + scenario1.getCompetitors().get(0).getSalesDepartment(GoodType.GENERIC).
                            getLatestObservation(SalesDataType.OUTFLOW) +","
                            + scenario1.getCompetitors().get(1).getSalesDepartment(GoodType.GENERIC).
                            getLatestObservation(SalesDataType.OUTFLOW));
                */


                }
                SummaryStatistics prices = new SummaryStatistics();
                SummaryStatistics quantities = new SummaryStatistics();
                SummaryStatistics target = new SummaryStatistics();
                for(int j=0; j<500; j++)
                {
                    macroII.schedule.step(macroII);
                    assert !Float.isNaN(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayAveragePrice());
                    prices.addValue(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayAveragePrice());
                    quantities.addValue(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayVolume());

                    for(EconomicAgent agent : macroII.getMarket(UndifferentiatedGoodType.GENERIC).getSellers())
                    {
                        SalesDepartment department = ((Firm) agent).getSalesDepartment(UndifferentiatedGoodType.GENERIC);
                        target.addValue(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayVolume());
                    }


                }


                System.out.println(prices.getMean() + " - " + quantities.getMean() +"/" +target.getMean()+ "----" + macroII.seed() + " | " + macroII.getMarket(UndifferentiatedGoodType.GENERIC).getLastDaysAveragePrice());
                System.out.println("standard deviations: price : " + prices.getStandardDeviation() + " , quantity: " + quantities.getStandardDeviation());
                printSlopes(scenario1);
                if(competitors>=4)
                {
                    assertEquals(prices.getMean(), 58, 5);
//                    assertTrue(prices.getStandardDeviation() < 5.5);
                    assertEquals(quantities.getMean(), 44,5);
  //                  assertTrue(quantities.getStandardDeviation() < 5.5);
                }
                macroII.finish();
            }


        }




    }


    @Test
    public void rightPriceAndQuantityTestAsMarginalInventoryTargetAlreadyLearned()
    {

        List<Integer> competitors = new LinkedList<>();
        for(int competitor=0;competitor<=7;competitor++)
            competitors.add(competitor);
        competitors.add(25);

        for(Integer competitor : competitors)
        {
            System.out.println("FORCED COMPETITIVE FIRMS: " + (competitor+1));
            float averageResultingPrice = 0;
            float averageResultingQuantity = 0;
            for(int i=0; i<5; i++)
            {

                final MacroII macroII = new MacroII(System.currentTimeMillis());   //1387582416533
                final TripolistScenario scenario1 = new TripolistScenario(macroII);

                scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
                scenario1.setAskPricingStrategy(SalesControlWithFixedInventoryAndPID.class);
                scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
                scenario1.setAdditionalCompetitors(competitor);
                scenario1.setWorkersToBeRehiredEveryDay(true);
                scenario1.setDemandIntercept(102);


                scenario1.setSalesPricePreditorStrategy(FixedDecreaseSalesPredictor.class);



                //assign scenario
                macroII.setScenario(scenario1);

                macroII.start();

                macroII.schedule.step(macroII);
                for(Firm firm : scenario1.getCompetitors())
                {
                    for(HumanResources hr : firm.getHRs())
                        hr.setPredictor(new FixedIncreasePurchasesPredictor(0));
                    for(SalesDepartment dept : firm.getSalesDepartments().values())
                        dept.setPredictorStrategy(new FixedDecreaseSalesPredictor(0));
                }


                while(macroII.schedule.getTime()<10000)
                {
                    macroII.schedule.step(macroII);



                }

                SummaryStatistics prices = new SummaryStatistics();
                SummaryStatistics quantities = new SummaryStatistics();
                SummaryStatistics target = new SummaryStatistics();
                for(int j=0; j<500; j++)
                {
                    macroII.schedule.step(macroII);
//                    assert !Float.isNaN(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayAveragePrice());
                    prices.addValue(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayAveragePrice());
                    quantities.addValue(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayVolume());

                    for(EconomicAgent agent : macroII.getMarket(UndifferentiatedGoodType.GENERIC).getSellers())
                    {
                        SalesDepartment department = ((Firm) agent).getSalesDepartment(UndifferentiatedGoodType.GENERIC);
                        target.addValue(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayVolume());
                    }


                }


                System.out.println(prices.getMean() + " - " + quantities.getMean() +"/" +target.getMean()+ "----" + macroII.seed() + " | " + macroII.getMarket(UndifferentiatedGoodType.GENERIC).getLastDaysAveragePrice());
                System.out.println("standard deviations: price : " + prices.getStandardDeviation() + " , quantity: " + quantities.getStandardDeviation());

                averageResultingPrice += prices.getMean();
                averageResultingQuantity += quantities.getMean();




                assertEquals(prices.getMean(), 58, 5);
//                assertTrue(String.valueOf(prices.getStandardDeviation()),prices.getStandardDeviation() < 5.5);
                assertEquals(quantities.getMean(), 44,5);
//                assertTrue(String.valueOf(prices.getStandardDeviation()),quantities.getStandardDeviation() < 5.5);

            }


            System.out.println(averageResultingPrice/5f + " --- " + averageResultingQuantity/5f);
        }




    }


    @Test
    public void rightPriceAndQuantityTestAsMarginalNoPIDAlreadyLearnedFlows()
    {

        for(int competitors=4;competitors<=7;competitors++)
        {
            //  System.out.println("FORCED COMPETITIVE FIRMS: " + (competitors+1));

            for(int i=0; i<5; i++)
            {

                final MacroII macroII = new MacroII(System.currentTimeMillis());
                final TripolistScenario scenario1 = new TripolistScenario(macroII);
                scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
                scenario1.setAskPricingStrategy(InventoryBufferSalesControl.class);
                scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
                scenario1.setAdditionalCompetitors(competitors);
                scenario1.setWorkersToBeRehiredEveryDay(true);
                scenario1.setDemandIntercept(102);


                scenario1.setSalesPricePreditorStrategy(FixedDecreaseSalesPredictor.class);

                //assign scenario
                macroII.setScenario(scenario1);

                macroII.start();

                macroII.schedule.step(macroII);
                for(Firm firm : scenario1.getCompetitors())
                {
                    for(HumanResources hr : firm.getHRs())
                        hr.setPredictor(new FixedIncreasePurchasesPredictor(0));

                    firm.getSalesDepartment(UndifferentiatedGoodType.GENERIC).setPredictorStrategy(new FixedDecreaseSalesPredictor(0));
                }


                while(macroII.schedule.getTime()<5000)
                {
                    macroII.schedule.step(macroII);



                }

                SummaryStatistics prices = new SummaryStatistics();
                SummaryStatistics quantities = new SummaryStatistics();
                SummaryStatistics target = new SummaryStatistics();
                for(int j=0; j<500; j++)
                {
                    macroII.schedule.step(macroII);
                    assert !Float.isNaN(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayAveragePrice());
                    prices.addValue(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayAveragePrice());
                    quantities.addValue(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayVolume());

                    for(EconomicAgent agent : macroII.getMarket(UndifferentiatedGoodType.GENERIC).getSellers())
                    {
                        SalesDepartment department = ((Firm) agent).getSalesDepartment(UndifferentiatedGoodType.GENERIC);
                        target.addValue(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayVolume());
                    }


                }


                System.out.println(prices.getMean() + " - " + quantities.getMean() +"/" +target.getMean()+ "----" + macroII.seed() + " | " + macroII.getMarket(UndifferentiatedGoodType.GENERIC).getLastDaysAveragePrice());
                System.out.println("standard deviations: price : " + prices.getStandardDeviation() + " , quantity: " + quantities.getStandardDeviation());

                assertEquals(prices.getMean(), 58, 5);
                assertTrue(prices.getStandardDeviation() < 5.5);
                assertEquals(quantities.getMean(), 44,5);
                assertTrue(quantities.getStandardDeviation() < 5.5);
            }


        }



    }


    @Test
    public void tooManyLearnedCompetitors()
    {

        int competitors = 20;

        //  System.out.println("FORCED COMPETITIVE FIRMS: " + (competitors+1));
        Class<? extends AskPricingStrategy> strategies[] = new Class[2];
        strategies[1] = InventoryBufferSalesControl.class;
        strategies[0] = SalesControlWithFixedInventoryAndPID.class;
        //    strategies[2] = SalesControlWithFixedInventoryAndPID.class;


        for(Class<? extends AskPricingStrategy> strategy : strategies)
        {
            System.out.println(strategy.getSimpleName());
            for(int i=0; i<5; i++)
            {

                final MacroII macroII = new MacroII(System.currentTimeMillis());
                final TripolistScenario scenario1 = new TripolistScenario(macroII);
                scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
                scenario1.setAskPricingStrategy(strategy);
                scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
                scenario1.setAdditionalCompetitors(competitors);
                scenario1.setWorkersToBeRehiredEveryDay(true);
                scenario1.setDemandIntercept(102);


                //scenario1.setSalesPricePreditorStrategy(MarketSalesPredictor.class);
                //scenario1.setSalesPricePreditorStrategy(PricingSalesPredictor.class);
                //   scenario1.setPurchasesPricePreditorStrategy(PricingPurchasesPredictor.class);



                //assign scenario
                macroII.setScenario(scenario1);

                macroII.start();

                macroII.schedule.step(macroII);
                for(Firm firm : scenario1.getCompetitors())
                {
                    for(HumanResources hr : firm.getHRs())
                        hr.setPredictor(new FixedIncreasePurchasesPredictor(0));

                    firm.getSalesDepartment(UndifferentiatedGoodType.GENERIC).setPredictorStrategy(new FixedDecreaseSalesPredictor(0));
                }


                while(macroII.schedule.getTime()<15000)
                {
                    macroII.schedule.step(macroII);



                }

                float averagePrice = 0;
                float averageQ = 0;
                float averageInventory = 0;
                float workers = 0;

                for(int j=0; j<500; j++)
                {
                    macroII.schedule.step(macroII);
                    for(Firm f : scenario1.getCompetitors() )
                        workers += f.getHRs().iterator().next().getWorkerTarget();
                    assert !Float.isNaN(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayAveragePrice());
                    averagePrice += macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayAveragePrice();
                    averageQ += macroII.getMarket(UndifferentiatedGoodType.GENERIC).countTodayProductionByRegisteredSellers();
                    averageInventory += macroII.getMarket(UndifferentiatedGoodType.GENERIC).getLatestObservation(MarketDataType.SELLERS_INVENTORY);


                }
                averagePrice = averagePrice/500f;
                workers /=500f;
                averageQ = averageQ/500f;
                averageInventory = averageInventory/500f;
                averageInventory = averageInventory/(competitors+1);
                System.out.println(averagePrice + "," + averageQ );
                System.out.println((competitors+1) +","+averagePrice + "," + averageQ + "," + averageInventory + "," + workers);
                for(Firm f : scenario1.getCompetitors())
                    System.out.print(f.getHRs().iterator().next().getWorkerTarget() + ",");
                System.out.println();




                assertEquals(averagePrice, 57, 5);
                assertEquals(averageQ, 44,5);
            }
        }






    }




    @Test
    public void rightPriceAndQuantityTestAsMarginalNoPIDStickyLearned()
    {

        for(int competitors=2;competitors<=7;competitors++)
        {
            System.out.println("FORCED COMPETITIVE FIRMS: " + (competitors+1));
            float averageResultingPrice = 0;
            float averageResultingQuantity = 0;
            for(int i=0; i<5; i++)
            {

                final MacroII macroII = new MacroII(System.currentTimeMillis());
                final TripolistScenario scenario1 = new TripolistScenario(macroII);
                scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
                scenario1.setAskPricingStrategy(SalesControlWithFixedInventoryAndPID.class);
                scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
                scenario1.setAdditionalCompetitors(competitors);
                scenario1.setWorkersToBeRehiredEveryDay(true);
                scenario1.setDemandIntercept(102);
                scenario1.setBuyerDelay(50);



                //assign scenario
                macroII.setScenario(scenario1);

                macroII.start();
                macroII.schedule.step(macroII);
                for(Firm firm : scenario1.getCompetitors())
                {
                    SalesDepartment department = firm.getSalesDepartment(UndifferentiatedGoodType.GENERIC);
                    final SimpleFlowSellerPID askPricingStrategy = new SimpleFlowSellerPID(department);
                   // askPricingStrategy.setTargetInventory(1000);
                    askPricingStrategy.setSpeed(0); //stickiness!
                    department.setAskPricingStrategy(askPricingStrategy);
                    department.setPredictorStrategy(new FixedDecreaseSalesPredictor(0));
                }


                while(macroII.schedule.getTime()<8000)
                {
                    macroII.schedule.step(macroII);



                }
                SummaryStatistics prices = new SummaryStatistics();
                SummaryStatistics quantities = new SummaryStatistics();
                SummaryStatistics target = new SummaryStatistics();
                for(int j=0; j<3000; j++)
                {
                    macroII.schedule.step(macroII);
 //                   assert !Float.isNaN(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice());
                    prices.addValue(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getLastPrice());
                    quantities.addValue(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayVolume());

                    for(EconomicAgent agent : macroII.getMarket(UndifferentiatedGoodType.GENERIC).getSellers())
                    {
                        SalesDepartment department = ((Firm) agent).getSalesDepartment(UndifferentiatedGoodType.GENERIC);
                        target.addValue(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayVolume());
                    }


                }


                System.out.println(prices.getMean() + " - " + quantities.getMean() +"/" +target.getMean()+ "----" + macroII.seed() + " | " + macroII.getMarket(UndifferentiatedGoodType.GENERIC).getLastDaysAveragePrice());
                System.out.println("standard deviations: price : " + prices.getStandardDeviation() + " , quantity deviation: " + quantities.getStandardDeviation());
                if(competitors>=4)
                {
                    assertEquals(prices.getMean(), 58, 5);
//                    assertTrue(prices.getStandardDeviation() < 5.5);         these are probably a lot higher with stickiness
                    assertEquals(quantities.getMean(), 44,5);
                    //         assertTrue(quantities.getStandardDeviation() < 5.5);
                }
            }


        }




    }





}
