/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.scenario;

import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.purchases.prediction.FixedIncreasePurchasesPredictor;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.prediction.FixedDecreaseSalesPredictor;
import agents.firm.sales.pricing.pid.SalesControlFlowPIDWithFixedInventoryButTargetingFlowsOnly;
import agents.firm.sales.pricing.pid.SmoothedDailyInventoryPricingStrategy;
import goods.GoodType;
import model.MacroII;
import org.junit.Test;

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


    /*
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
                averageQ += macroII.getMarket(GoodType.GENERIC).getYesterdayVolume();

            }
            averagePrice = averagePrice/500f;
            averageQ = averageQ/500f;

            System.out.println(averagePrice + " - " + averageQ );
            assertEquals(averagePrice, 72,5);
            assertEquals(averageQ, 29,5);
        }





    }
    */


    @Test
    public void rightPriceAndQuantityTestAsMarginalNoPID()
    {

        for(int competitors=1;competitors<=7;competitors++)
        {
            System.out.println("FORCED COMPETITIVE FIRMS: " + (competitors+1));
            float averageResultingPrice = 0;
            float averageResultingQuantity = 0;
            for(int i=0; i<5; i++)
            {

                final MacroII macroII = new MacroII(System.currentTimeMillis());
                final TripolistScenario scenario1 = new TripolistScenario(macroII);
                scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
                scenario1.setAskPricingStrategy(SmoothedDailyInventoryPricingStrategy.class);
                scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
                scenario1.setAdditionalCompetitors(competitors);

                //scenario1.setSalesPricePreditorStrategy(FixedDecreaseSalesPredictor.class);
                //scenario1.setSalesPricePreditorStrategy(MarketSalesPredictor.class);
                //scenario1.setSalesPricePreditorStrategy(PricingSalesPredictor.class);
                //   scenario1.setPurchasesPricePreditorStrategy(PricingPurchasesPredictor.class);



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
                    assert !Float.isNaN(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice());
                    averagePrice += macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice();
                    averageQ += macroII.getMarket(GoodType.GENERIC).getYesterdayVolume();


                }
                averagePrice = averagePrice/500f;
                averageQ = averageQ/500f;
                System.out.println(averagePrice + " - " + averageQ + "----" + macroII.seed() );

                averageResultingPrice += averagePrice;
                averageResultingQuantity += averageQ;




                assertEquals(averagePrice, 58,5);
                assertEquals(averageQ, 44,5);
            }


            System.out.println(averageResultingPrice/5f + " --- " + averageResultingQuantity/5f);
        }




    }


    @Test
    public void rightPriceAndQuantityTestAsMarginalNoPIDAlreadyLearned()
    {

        for(int competitors=0;competitors<=7;competitors++)
        {
            System.out.println("FORCED COMPETITIVE FIRMS: " + (competitors+1));
            float averageResultingPrice = 0;
            float averageResultingQuantity = 0;
            for(int i=0; i<5; i++)
            {
                FixedDecreaseSalesPredictor.defaultDecrementDelta=0;

                final MacroII macroII = new MacroII(System.currentTimeMillis());
                final TripolistScenario scenario1 = new TripolistScenario(macroII);
                scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
                scenario1.setAskPricingStrategy(SmoothedDailyInventoryPricingStrategy.class);
                scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
                scenario1.setAdditionalCompetitors(competitors);

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
                    averageQ += macroII.getMarket(GoodType.GENERIC).getYesterdayVolume();


                }
                averagePrice = averagePrice/500f;
                averageQ = averageQ/500f;
                System.out.println(averagePrice + " - " + averageQ );

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
    public void rightPriceAndQuantityTestAsMarginalNoPIDAlreadyLearnedFlowsOnly()
    {

        for(int competitors=0;competitors<=7;competitors++)
        {
            System.out.println("FORCED COMPETITIVE FIRMS: " + (competitors+1));
            float averageResultingPrice = 0;
            float averageResultingQuantity = 0;
            for(int i=0; i<5; i++)
            {
                FixedDecreaseSalesPredictor.defaultDecrementDelta=0;

                final MacroII macroII = new MacroII(System.currentTimeMillis());
                final TripolistScenario scenario1 = new TripolistScenario(macroII);
                scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
                scenario1.setAskPricingStrategy(SalesControlFlowPIDWithFixedInventoryButTargetingFlowsOnly.class);
                scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
                scenario1.setAdditionalCompetitors(competitors);

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
                    averageQ += macroII.getMarket(GoodType.GENERIC).getYesterdayVolume();


                }
                averagePrice = averagePrice/500f;
                averageQ = averageQ/500f;
                System.out.println(averagePrice + " - " + averageQ );

                averageResultingPrice += averagePrice;
                averageResultingQuantity += averageQ;




                assertEquals(averagePrice, 58,5);
                assertEquals(averageQ, 44,5);
                FixedDecreaseSalesPredictor.defaultDecrementDelta=1;
            }


            System.out.println(averageResultingPrice/5f + " --- " + averageResultingQuantity/5f);
        }



    }






}
