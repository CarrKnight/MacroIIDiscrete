/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.experiments;

import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.purchases.prediction.FixedIncreasePurchasesPredictor;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.prediction.FixedDecreaseSalesPredictor;
import agents.firm.sales.pricing.AskPricingStrategy;
import agents.firm.sales.pricing.pid.SalesControlFlowPIDWithFixedInventoryButTargetingFlowsOnly;
import agents.firm.sales.pricing.pid.SalesControlWithFixedInventoryAndPID;
import agents.firm.utilities.WeightedPriceAverager;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.scenario.MonopolistScenario;
import model.scenario.TripolistScenario;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

/**
 * <h4>Description</h4>
 * <p> Already learned, is inventory targeting messing up with averaging while flows only has no issue?
 * <p>
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-06-23
 * @see
 */
public class WhenDoesAveragingMatters {

    public static void main(String[] args){



        for(int competitors = 0; competitors<5; competitors++) {
            for (int average = 1; average < 20; average += 5) {
                System.out.println("competitors: " + competitors + ", average: " + average);
                learnedRun(competitors, SalesControlFlowPIDWithFixedInventoryButTargetingFlowsOnly.class, average);
            }
            System.out.println("==================================================");
        }

        System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");

        for(int competitors = 0; competitors<5; competitors++) {
            for (int average = 1; average < 20; average += 5) {
                System.out.println("competitors: " + competitors + ", average: " + average);
                learnedRun(competitors, SalesControlWithFixedInventoryAndPID.class, average);
            }
            System.out.println("==================================================");
        }





    }


    public static void learnedRun(int competitors, Class<? extends AskPricingStrategy> pricing, int weightedAverageSize){

        final MacroII macroII = new MacroII(System.currentTimeMillis());
        final TripolistScenario scenario1 = new TripolistScenario(macroII);
        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        scenario1.setAskPricingStrategy(pricing);
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


            final SalesDepartment salesDepartment = firm.getSalesDepartment(UndifferentiatedGoodType.GENERIC);
            salesDepartment.setPriceAverager(new WeightedPriceAverager(weightedAverageSize));
            salesDepartment.setPredictorStrategy(new FixedDecreaseSalesPredictor(0));
        }


        while(macroII.schedule.getTime()<5000)
        {
            macroII.schedule.step(macroII);



        }

        SummaryStatistics prices = new SummaryStatistics();
        SummaryStatistics quantities = new SummaryStatistics();
        for(int j=0; j<500; j++)
        {
            macroII.schedule.step(macroII);
            assert !Float.isNaN(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayAveragePrice());
            prices.addValue(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayAveragePrice());
            quantities.addValue(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayVolume());



        }


        System.out.println(prices.getMean() + " - " + quantities.getMean()+ "----" + macroII.seed() + " | " + macroII.getMarket(UndifferentiatedGoodType.GENERIC).getLastDaysAveragePrice());
        System.out.println("standard deviations: price : " + prices.getStandardDeviation() + " , quantity: " + quantities.getStandardDeviation());


    }


}
