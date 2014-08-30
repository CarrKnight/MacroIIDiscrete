/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.pricing.pid;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.purchases.prediction.FixedIncreasePurchasesPredictor;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.prediction.FixedDecreaseSalesPredictor;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.scenario.MonopolistScenario;
import model.scenario.TripolistScenario;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SimpleStockSellerTest {


    //solves monopolist scenario


    @Test
    public void testAlreadyLearnedMonopolist() throws Exception {
        //run the test 5 times
        for(int i=0; i<5; i++)
        {
            long seed = System.currentTimeMillis();

            final MacroII macroII = new MacroII(seed);
            MonopolistScenario scenario1 = new MonopolistScenario(macroII);





            //generate random parameters for labor supply and good demand
            int p0= macroII.random.nextInt(100)+100; int p1= macroII.random.nextInt(3)+1;
            scenario1.setDemandIntercept(p0); scenario1.setDemandSlope(p1);
            int w0=macroII.random.nextInt(10)+10; int w1=macroII.random.nextInt(3)+1;
            scenario1.setDailyWageIntercept(w0); scenario1.setDailyWageSlope(w1);
            int a=macroII.random.nextInt(3)+1;
            scenario1.setLaborProductivity(a);


            //    scenario1.setAlwaysMoving(true);
            //   MonopolistScenario scenario1 = new MonopolistScenario(macroII);
            macroII.setScenario(scenario1);
            //choose a control at random, but avoid always moving

            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
            scenario1.setWorkersToBeRehiredEveryDay(true);
            //choose a sales control at random, but don't mix hill-climbing with inventory building since they aren't really compatible
            scenario1.setAskPricingStrategy(SimpleStockSeller.class);


            System.out.println(p0 + "," + p1 + "," + w0 + "," + w1 +"," + a);
            System.out.println(scenario1.getControlType() + "," + scenario1.getAskPricingStrategy() + "," + scenario1.getSalesDepartmentType() + " -- " + macroII.seed());

            macroII.start();
            macroII.schedule.step(macroII);
            scenario1.getMonopolist().getSalesDepartment(UndifferentiatedGoodType.GENERIC).setPredictorStrategy(new FixedDecreaseSalesPredictor(p1));
            scenario1.getMonopolist().getHRs().iterator().next().setPredictor(new FixedIncreasePurchasesPredictor(w1));
            while(macroII.schedule.getTime()<5000)
                macroII.schedule.step(macroII);


            //the pi maximizing labor force employed is:
            int profitMaximizingLaborForce = MonopolistScenario.findWorkerTargetThatMaximizesProfits(p0,p1,w0,w1,a);
            int profitMaximizingQuantity = profitMaximizingLaborForce*a;

            System.out.println(p0 + "," + p1 + "," + w0 + "," + w1 +"," + a);
            System.out.println(scenario1.getControlType() + "," + scenario1.getAskPricingStrategy() + "," + scenario1.getSalesDepartmentType() + " -- " + macroII.seed());
            System.out.flush();

            //you must be at most wrong by two (not well tuned and anyway sometimes it's hard!)
            assertEquals(scenario1.getMonopolist().getTotalWorkers(), profitMaximizingLaborForce,2);



            System.out.println(i + "---------------------------------------------------------------------------------------------");
            macroII.finish();

        }








    }


    @Test
    public void testAlreadyLearnedCompetitive(){
        int competitors = 4;

        for(int i=0; i<5; i++)
        {

            final MacroII macroII = new MacroII(System.currentTimeMillis());   //1387582416533
            final TripolistScenario scenario1 = new TripolistScenario(macroII);

            scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
            scenario1.setAskPricingStrategy(SimpleStockSeller.class);
            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
            scenario1.setAdditionalCompetitors( competitors);
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





            assertEquals(prices.getMean(), 58, 5);
//                assertTrue(String.valueOf(prices.getStandardDeviation()),prices.getStandardDeviation() < 5.5);
            assertEquals(quantities.getMean(), 44,5);
//                assertTrue(String.valueOf(prices.getStandardDeviation()),quantities.getStandardDeviation() < 5.5);

        }



}
}