/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.scenario;

import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.prediction.FixedDecreaseSalesPredictor;
import agents.firm.sales.prediction.MarketSalesPredictor;
import agents.firm.sales.prediction.PricingSalesPredictor;
import agents.firm.sales.pricing.pid.SalesControlFlowPIDWithFixedInventory;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import goods.GoodType;
import model.MacroII;
import model.utilities.stats.collectors.enums.MarketDataType;
import model.utilities.stats.collectors.enums.PurchasesDataType;
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
            scenario1.setAdditionalCompetitors(4);

            // scenario1.setSalesPricePreditorStrategy(FixedDecreaseSalesPredictor.class);
            scenario1.setSalesPricePreditorStrategy(PricingSalesPredictor.class);
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


    @Test
    public void rightPriceAndQuantityMarginalPIDUnit()
    {

        for(int i=0; i<10; i++)
        {
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            final TripolistScenario scenario1 = new TripolistScenario(macroII);
            scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
            scenario1.setAskPricingStrategy(SimpleFlowSellerPID.class);
            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_WITH_UNIT_PID);
            scenario1.setAdditionalCompetitors(macroII.random.nextInt(5)+2);

            // scenario1.setSalesPricePreditorStrategy(FixedDecreaseSalesPredictor.class);
            scenario1.setSalesPricePreditorStrategy(PricingSalesPredictor.class);
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

    @Test
    public void rightPriceAndQuantityMarginalNoPID()
    {

        for(int i=0; i<10; i++)
        {
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            final TripolistScenario scenario1 = new TripolistScenario(macroII);
            scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
            scenario1.setAskPricingStrategy(SimpleFlowSellerPID.class);
            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
            scenario1.setAdditionalCompetitors(macroII.random.nextInt(5)+2);





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


    @Test
    public void rightPriceAndQuantityTestAsMarginalPIDAlreadyLearned()
    {

        for(int i=0; i<10; i++)
        {
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            final TripolistScenario scenario1 = new TripolistScenario(macroII);
            scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
            scenario1.setAskPricingStrategy(SimpleFlowSellerPID.class);
            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_WITH_UNIT_PID);
            scenario1.setAdditionalCompetitors(macroII.random.nextInt(5)+2);

            scenario1.setSalesPricePreditorStrategy(FixedDecreaseSalesPredictor.class);
            FixedDecreaseSalesPredictor.defaultDecrementDelta=0;
            //scenario1.setSalesPricePreditorStrategy(PricingSalesPredictor.class);
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
            FixedDecreaseSalesPredictor.defaultDecrementDelta=1;

        }




    }


    @Test
    public void rightPriceAndQuantityTestAsMarginalNoPIDAlreadyLearned()
    {

        for(int i=0; i<10; i++)
        {
            FixedDecreaseSalesPredictor.defaultDecrementDelta=0;

            final MacroII macroII = new MacroII(System.currentTimeMillis());
            final TripolistScenario scenario1 = new TripolistScenario(macroII);
            scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
            scenario1.setAskPricingStrategy(SimpleFlowSellerPID.class);
            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
            scenario1.setAdditionalCompetitors(macroII.random.nextInt(5)+2);

            FixedDecreaseSalesPredictor.defaultDecrementDelta=0;
            //scenario1.setSalesPricePreditorStrategy(FixedDecreaseSalesPredictor.class);
            scenario1.setSalesPricePreditorStrategy(MarketSalesPredictor.class);
            //scenario1.setSalesPricePreditorStrategy(PricingSalesPredictor.class);
            //   scenario1.setPurchasesPricePreditorStrategy(PricingPurchasesPredictor.class);



            //assign scenario
            macroII.setScenario(scenario1);

            macroII.start();

            while(macroII.schedule.getTime()<5000)
            {
                macroII.schedule.step(macroII);
       /*         System.out.println("produced: " + macroII.getMarket(GoodType.GENERIC).getLatestObservation(MarketDataType.VOLUME_PRODUCED) +
                        ", wages:" + macroII.getMarket(GoodType.LABOR).getLatestObservation(MarketDataType.CLOSING_PRICE) +
                        ", wage0: " + scenario1.monopolist.getHR(scenario1.monopolist.getPlants().iterator().next()).getLatestObservation(PurchasesDataType.AVERAGE_CLOSING_PRICES) ); */
            }

            float averagePrice = 0;
            float averageQ = 0;
            for(int j=0; j<500; j++)
            {
                macroII.schedule.step(macroII);
                averagePrice += macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice();
                averageQ += macroII.getMarket(GoodType.GENERIC).getYesterdayVolume();
                System.out.println("produced: " + macroII.getMarket(GoodType.GENERIC).getLatestObservation(MarketDataType.VOLUME_PRODUCED) +
                        ", wages:" + macroII.getMarket(GoodType.LABOR).getLatestObservation(MarketDataType.CLOSING_PRICE) +
                        ", wage0: " + scenario1.monopolist.getHR(scenario1.monopolist.getPlants().iterator().next()).getLatestObservation(PurchasesDataType.AVERAGE_CLOSING_PRICES) );


            }
            averagePrice = averagePrice/500f;
            averageQ = averageQ/500f;
            System.out.println(averagePrice + " - " + averageQ );




            assertEquals(averagePrice, 72,5);
            assertEquals(averageQ, 29,5);
            FixedDecreaseSalesPredictor.defaultDecrementDelta=1;

        }




    }



    @Test
    public void monopolistActingAsCompetitiveMarginalNoPID()
    {

        for(int i=0; i<5; i++)
        {
            FixedDecreaseSalesPredictor.defaultDecrementDelta=0;

            final MacroII macroII = new MacroII(System.currentTimeMillis());
            final TripolistScenario scenario1 = new TripolistScenario(macroII);
            scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
            scenario1.setAskPricingStrategy(SalesControlFlowPIDWithFixedInventory.class);
            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
            scenario1.setAdditionalCompetitors(0); //it is a monopolist; just doesn't know it.

            FixedDecreaseSalesPredictor.defaultDecrementDelta=0;
            scenario1.setSalesPricePreditorStrategy(FixedDecreaseSalesPredictor.class);
            //scenario1.setSalesPricePreditorStrategy(MarketSalesPredictor.class);
            //scenario1.setSalesPricePreditorStrategy(PricingSalesPredictor.class);
            //   scenario1.setPurchasesPricePreditorStrategy(PricingPurchasesPredictor.class);



            //assign scenario
            macroII.setScenario(scenario1);

            macroII.start();

            while(macroII.schedule.getTime()<5000)
            {
                macroII.schedule.step(macroII);
       /*         System.out.println("produced: " + macroII.getMarket(GoodType.GENERIC).getLatestObservation(MarketDataType.VOLUME_PRODUCED) +
                        ", wages:" + macroII.getMarket(GoodType.LABOR).getLatestObservation(MarketDataType.CLOSING_PRICE) +
                        ", wage0: " + scenario1.monopolist.getHR(scenario1.monopolist.getPlants().iterator().next()).getLatestObservation(PurchasesDataType.AVERAGE_CLOSING_PRICES) ); */
            }

            float averagePrice = 0;
            float averageQ = 0;
            for(int j=0; j<500; j++)
            {
                macroII.schedule.step(macroII);
                averagePrice += macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice();
                averageQ += macroII.getMarket(GoodType.GENERIC).getYesterdayVolume();
                System.out.println("produced: " + macroII.getMarket(GoodType.GENERIC).getLatestObservation(MarketDataType.VOLUME_PRODUCED) +
                        ", wages:" + macroII.getMarket(GoodType.LABOR).getLatestObservation(MarketDataType.CLOSING_PRICE) +
                        ", wage0: " + scenario1.monopolist.getHR(scenario1.monopolist.getPlants().iterator().next()).getLatestObservation(PurchasesDataType.AVERAGE_CLOSING_PRICES) );


            }
            averagePrice = averagePrice/500f;
            averageQ = averageQ/500f;
            System.out.println(averagePrice + " - " + averageQ );




            assertEquals(averagePrice, 72,5);
            assertEquals(averageQ, 29,5);
            FixedDecreaseSalesPredictor.defaultDecrementDelta=1;

        }




    }

}
