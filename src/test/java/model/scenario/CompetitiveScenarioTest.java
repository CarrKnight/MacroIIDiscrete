/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.scenario;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.production.Blueprint;
import agents.firm.production.control.maximizer.algorithms.marginalMaximizers.MarginalMaximizerStatics;
import agents.firm.production.control.maximizer.algorithms.marginalMaximizers.RobustMarginalMaximizer;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.prediction.FixedIncreasePurchasesPredictor;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.prediction.FixedDecreaseSalesPredictor;
import agents.firm.sales.prediction.MarketSalesPredictor;
import agents.firm.sales.prediction.SalesPredictor;
import agents.firm.sales.pricing.AskPricingStrategy;
import agents.firm.sales.pricing.pid.SalesControlFlowPIDWithFixedInventoryButTargetingFlowsOnly;
import agents.firm.sales.pricing.pid.SalesControlWithFixedInventoryAndPID;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import agents.firm.sales.pricing.pid.SmoothedDailyInventoryPricingStrategy;
import financial.market.Market;
import goods.GoodType;
import model.MacroII;
import model.utilities.stats.collectors.enums.MarketDataType;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static model.experiments.tuningRuns.MarginalMaximizerPIDTuning.printProgressBar;
import static org.junit.Assert.assertEquals;

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
                averagePrice += macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice();
                averageQ += macroII.getMarket(GoodType.GENERIC).countTodayProductionByRegisteredSellers();

            }
            averagePrice = averagePrice/500f;
            averageQ = averageQ/500f;

            System.out.println(averagePrice + " - " + averageQ );
            assertEquals(averagePrice, 72,5);
            assertEquals(averageQ, 29,5);
        }





    }



    @Test
    public void rightPriceAndQuantityTestAsMarginalNoPID()
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

                float averagePrice = 0;
                float averageQ = 0;
                for(int j=0; j<500; j++)
                {
                    macroII.schedule.step(macroII);
//                    assert !Float.isNaN(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice());
                    averagePrice += macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice();
                    averageQ += macroII.getMarket(GoodType.GENERIC).countYesterdayProductionByRegisteredSellers();


                }
                averagePrice = averagePrice/500f;
                averageQ = averageQ/500f;
                System.out.println(averagePrice + " - " + averageQ + "----" + macroII.seed() + " | " + macroII.getMarket(GoodType.GENERIC).getLastDaysAveragePrice());

                averageResultingPrice += averagePrice;
                averageResultingQuantity += averageQ;




                if(competitors>4)
                {
                    assertEquals(averagePrice, 58,5);
                    assertEquals(averageQ, 44,5);
                }
            }


        }




    }




    @Test
    public void rightPriceAndQuantityTestAsMarginalNoPIDAlreadyLearned()
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
                FixedDecreaseSalesPredictor.defaultDecrementDelta=0;

                final MacroII macroII = new MacroII(System.currentTimeMillis());   //1387582416533l
                final TripolistScenario scenario1 = new TripolistScenario(macroII);

                scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
                scenario1.setAskPricingStrategy(SalesControlWithFixedInventoryAndPID.class);
                scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
                scenario1.setAdditionalCompetitors( competitor);
                scenario1.setWorkersToBeRehiredEveryDay(true);
                scenario1.setDemandIntercept(102);


                FixedDecreaseSalesPredictor.defaultDecrementDelta=0;
                scenario1.setSalesPricePreditorStrategy(FixedDecreaseSalesPredictor.class);
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
                }


                while(macroII.schedule.getTime()<10000)
                {
                    macroII.schedule.step(macroII);



                }

                float averagePrice = 0;
                float averageQ = 0;
                float averageWorkerTarget = 0;
                for(int j=0; j<500; j++)
                {
                    MarginalMaximizerStatics.printOutDiagnostics = false;
                    macroII.schedule.step(macroII);
                    assert !Float.isNaN(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice());
                    averagePrice += macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice();
                    averageQ += macroII.getMarket(GoodType.GENERIC).countTodayProductionByRegisteredSellers(); //shortcut to check how much is produced rather than just traded

           //         System.out.println("---------------------------------------------------------------------");
                    int k =0; int totalSold = 0;
                    for(EconomicAgent agent : macroII.getMarket(GoodType.GENERIC).getSellers())
                    {
                        SalesDepartment department = ((Firm) agent).getSalesDepartment(GoodType.GENERIC);
          /*             System.out.println("department "+ k + ", offered price: " + department.getLastAskedPrice() + ", outflow: " + department.getTodayOutflow() + ", inflow: " +department.getTodayInflow()
                                + ", worker target: " +((Firm) agent).getHRs().iterator().next().getWorkerTarget() + ", averagedPrice:" + department.getAveragedLastPrice() );

                        totalSold+= department.getTodayOutflow();
                        */
                        averageWorkerTarget+=  ((Firm) agent).getHRs().iterator().next().getWorkerTarget();
                    }
                    //System.out.println("---> total production : " + macroII.getMarket(GoodType.GENERIC).countTodayProductionByRegisteredSellers() + " , prices:: " + macroII.getMarket(GoodType.GENERIC).getLastDaysAveragePrice());


                }
                MarginalMaximizerStatics.printOutDiagnostics = false;

                averagePrice = averagePrice/500f;
                averageQ = averageQ/500f;
                averageWorkerTarget = averageWorkerTarget/500f;
                System.out.println(averagePrice + " - " + averageQ +"/" +averageWorkerTarget+ "----" + macroII.seed() + " | " + macroII.getMarket(GoodType.GENERIC).getLastDaysAveragePrice());

                averageResultingPrice += averagePrice;
                averageResultingQuantity += averageQ;




                assertEquals(averagePrice, 58, 5);
                assertEquals(averageQ, 44,5);
                FixedDecreaseSalesPredictor.defaultDecrementDelta=1;
            }


            System.out.println(averageResultingPrice/5f + " --- " + averageResultingQuantity/5f);
        }




    }


    @Test
    public void rightPriceAndQuantityTestAsMarginalNoPIDAlreadyLearnedFlows()
    {

        for(int competitors=0;competitors<=7;competitors++)
        {
            //  System.out.println("FORCED COMPETITIVE FIRMS: " + (competitors+1));

            for(int i=0; i<5; i++)
            {
                FixedDecreaseSalesPredictor.defaultDecrementDelta=0;

                final MacroII macroII = new MacroII(System.currentTimeMillis());
                final TripolistScenario scenario1 = new TripolistScenario(macroII);
                scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
                scenario1.setAskPricingStrategy(SalesControlFlowPIDWithFixedInventoryButTargetingFlowsOnly.class);
                scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
                scenario1.setAdditionalCompetitors(competitors);
                scenario1.setWorkersToBeRehiredEveryDay(true);
                scenario1.setDemandIntercept(102);


                FixedDecreaseSalesPredictor.defaultDecrementDelta=0;
                scenario1.setSalesPricePreditorStrategy(FixedDecreaseSalesPredictor.class);
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

                    firm.getSalesDepartment(GoodType.GENERIC).setPredictorStrategy(new FixedDecreaseSalesPredictor(0));
                }


                while(macroII.schedule.getTime()<5000)
                {
                    macroII.schedule.step(macroII);



                }

                float averagePrice = 0;
                float averageQ = 0;

                for(int j=0; j<500; j++)
                {
                    macroII.schedule.step(macroII);

                    assert !Float.isNaN(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice());
                    averagePrice += macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice();
                    averageQ += macroII.getMarket(GoodType.GENERIC).getLatestObservation(MarketDataType.VOLUME_PRODUCED);


                }
                averagePrice = averagePrice/500f;
                averageQ = averageQ/500f;
                System.out.println(averagePrice + "," + averageQ );
            /*    System.out.println((competitors+1) +","+averagePrice + "," + averageQ + "," + averageInventory + "," + workers);
                for(Firm f : scenario1.getCompetitors())
                    System.out.print(f.getHRs().iterator().next().getWorkerTarget() + ","); */




                assertEquals(averagePrice, 58, 5);
                assertEquals(averageQ, 44,5);
                FixedDecreaseSalesPredictor.defaultDecrementDelta=1;
            }


        }



    }


    @Test
    public void tooManyLearnedCompetitors()
    {

        int competitors = 80;

        //  System.out.println("FORCED COMPETITIVE FIRMS: " + (competitors+1));
        Class<? extends AskPricingStrategy> strategies[] = new Class[2];
        strategies[1] = SalesControlFlowPIDWithFixedInventoryButTargetingFlowsOnly.class;
        strategies[0] = SmoothedDailyInventoryPricingStrategy.class;
        //    strategies[2] = SalesControlWithFixedInventoryAndPID.class;


        for(Class<? extends AskPricingStrategy> strategy : strategies)
        {
            System.out.println(strategy.getSimpleName());
            for(int i=0; i<5; i++)
            {
                FixedDecreaseSalesPredictor.defaultDecrementDelta=0;

                final MacroII macroII = new MacroII(System.currentTimeMillis());
                final TripolistScenario scenario1 = new TripolistScenario(macroII);
                scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
                scenario1.setAskPricingStrategy(strategy);
                scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
                scenario1.setAdditionalCompetitors(competitors);
                scenario1.setWorkersToBeRehiredEveryDay(true);
                scenario1.setDemandIntercept(102);


                FixedDecreaseSalesPredictor.defaultDecrementDelta=0;
                scenario1.setSalesPricePreditorStrategy(FixedDecreaseSalesPredictor.class);
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

                    firm.getSalesDepartment(GoodType.GENERIC).setPredictorStrategy(new FixedDecreaseSalesPredictor(0));
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
                    assert !Float.isNaN(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice());
                    averagePrice += macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice();
                    averageQ += macroII.getMarket(GoodType.GENERIC).countTodayProductionByRegisteredSellers();
                    averageInventory += macroII.getMarket(GoodType.GENERIC).getLatestObservation(MarketDataType.SELLERS_INVENTORY);


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
                FixedDecreaseSalesPredictor.defaultDecrementDelta=1;
            }
        }






    }



    @Test
    public void rightPriceAndQuantityBuyingLearning()
    {

        List<Integer> competitors = new LinkedList<>();

        competitors.add(4);
        competitors.add(5);
        competitors.add(6);
        competitors.add(7);

        competitors.add(30);

        for(Integer competitorNumber : competitors)
        {
            System.out.println("FORCED COMPETITIVE FIRMS: " + (competitorNumber+1));

            for(int i=0; i<1; i++)
            {
                float averagePrice = 0;
                float averageQ = 0;
                float averageConsumed = 0;

                final MacroII macroII = new MacroII(System.currentTimeMillis());
                final OneLinkSupplyChainScenarioWithCheatingBuyingPrice scenario1 = new OneLinkSupplyChainScenarioWithCheatingBuyingPrice(macroII){

                    @Override
                    protected void buildBeefSalesPredictor(SalesDepartment dept) {
                        FixedDecreaseSalesPredictor predictor  = SalesPredictor.Factory.newSalesPredictor(FixedDecreaseSalesPredictor.class, dept);
                        predictor.setDecrementDelta(2);
                        dept.setPredictorStrategy(predictor);
                    }



                    @Override
                    public void buildFoodPurchasesPredictor(PurchasesDepartment department) {

                    }

                    @Override
                    protected SalesDepartment createSalesDepartment(Firm firm, Market goodmarket) {
                        final SalesDepartment department = super.createSalesDepartment(firm, goodmarket);
                        if(goodmarket.getGoodType().equals(GoodType.FOOD))
                            department.setPredictorStrategy(new FixedDecreaseSalesPredictor(0));

                        SalesControlFlowPIDWithFixedInventoryButTargetingFlowsOnly askPricingStrategy =
                                new SalesControlFlowPIDWithFixedInventoryButTargetingFlowsOnly(department);
                        department.setAskPricingStrategy(askPricingStrategy);
                        if(goodmarket.getGoodType().equals(GoodType.BEEF))
                        {
                            askPricingStrategy.setProportionalGain(askPricingStrategy.getProportionalGain()/divideProportionalGainByThis);
                            askPricingStrategy.setIntegralGain(askPricingStrategy.getIntegralGain()/divideIntegrativeGainByThis);

                        }

                        return department;
                    }

                    @Override
                    protected HumanResources createPlant(Blueprint blueprint, Firm firm, Market laborMarket) {
                        HumanResources hr = super.createPlant(blueprint, firm, laborMarket);
                        if(blueprint.getOutputs().containsKey(GoodType.BEEF))      {
                            hr.setPredictor(new FixedIncreasePurchasesPredictor(1));
                        }
                        return hr;
                    }
                };
                scenario1.setControlType(RobustMarginalMaximizer.class);
                scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
                scenario1.setBeefPriceFilterer(null);


                //competition!
                scenario1.setNumberOfBeefProducers(1);
                scenario1.setNumberOfFoodProducers(competitorNumber+1);

                scenario1.setDivideProportionalGainByThis(100f);
                scenario1.setDivideIntegrativeGainByThis(100f);
                //no delay
                scenario1.setBeefPricingSpeed(0);


                macroII.setScenario(scenario1);
                macroII.start();





                while(macroII.schedule.getTime()<10000)
                {
                    macroII.schedule.step(macroII);
                    printProgressBar(10001,(int)macroII.schedule.getSteps(),100);
                }
                for(int k=0; k< 1000; k++)
                {
                    macroII.schedule.step(macroII);
                    averagePrice += macroII.getMarket(GoodType.BEEF).getLastPrice();
                    averageQ += macroII.getMarket(GoodType.BEEF).getTodayVolume();
                    averageConsumed += macroII.getMarket(GoodType.BEEF).countTodayConsumptionByRegisteredBuyers();
                }
                averagePrice /= 1000f;
                averageQ /= 1000f;
                averageConsumed /= 1000f;

                System.out.println("averagePrice : " + averagePrice);
                System.out.println("averageQuantity Traded : " + averageQ);
                System.out.println("averageQuantity Consumed: " + averageConsumed);
                System.out.println();





                assertEquals(averagePrice, 68,5);
                assertEquals(averageQ, 17,5);

            }


        }




    }



}
