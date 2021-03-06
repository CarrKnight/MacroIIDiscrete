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
import model.scenario.OneSectorStatics;
import model.scenario.TripolistScenario;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SimpleStockSellerIntegrativeTest {


    //solves monopolist scenario


    @Test
    public void testLearningMonopolist() throws Exception {
        //run the test 5 times
        for(int i=0; i<5; i++)
        {
            int seed = (int)System.currentTimeMillis();

            final MacroII macroII = new MacroII(seed);
            MonopolistScenario scenario1 = new MonopolistScenario(macroII);

            OneSectorStatics.testRandomSlopeMonopolist(seed, macroII, scenario1, SimpleStockSeller.class);

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


  //          scenario1.setSalesPricePreditorStrategy(FixedDecreaseSalesPredictor.class);



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