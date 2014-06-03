/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.scenario;

import financial.market.Market;
import model.MacroII;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.Assert;
import org.junit.Test;

import static model.experiments.tuningRuns.MarginalMaximizerPIDTuning.printProgressBar;

public class FarmersAndWorkersScenarioTest {

    @Test
    public void forcedCompetitiveRun() throws Exception {


        for(int j=0; j< 5; j++) {
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            FarmersAndWorkersScenario scenario1 = new FarmersAndWorkersScenario(macroII);

            macroII.setScenario(scenario1);

            scenario1.setLinearProductionPerWorker(10);
            scenario1.setNumberOfAgents(200);

            macroII.start();

            Market market = macroII.getMarket(FarmersAndWorkersScenario.MANUFACTURED);

            while (macroII.schedule.getTime() < 3000) {
                macroII.schedule.step(macroII);
                printProgressBar(3501, (int) macroII.schedule.getSteps(), 100);

                //make sure the market is functioning properly!
            /*
            System.out.println(macroII.schedule.getTime());
            System.out.println("A consumed: " + scenario1.countAgricultureConsumption() + ", A produced: " + scenario1.countAgricultureProduction() +
                    ", M consumed: " + scenario1.countManufacturedConsumption() + ", M produced: " + scenario1.countManufacturedProduction());

            System.out.println("M Ask:" + scenario1.getProducers().get(0).getSalesDepartment(scenario1.MANUFACTURED).getLastAskedPrice() + ", M Traded: " + market.getYesterdayVolume() + ", M inventory: " + scenario1.getProducers().get(0).hasHowMany(scenario1.MANUFACTURED));
            System.out.println("==========================================================================================================");
              */

            }
            SummaryStatistics manufacturingProduction = new SummaryStatistics();
            SummaryStatistics agriculturalProduction = new SummaryStatistics();
            SummaryStatistics manufacturingPrice = new SummaryStatistics();
            for (int i = 0; i < 500; i++) {
                macroII.schedule.step(macroII);
                printProgressBar(3501, (int) macroII.schedule.getSteps(), 100);
                manufacturingProduction.addValue(scenario1.countManufacturedProduction());
                agriculturalProduction.addValue(scenario1.countAgricultureProduction());
                manufacturingPrice.addValue(scenario1.getProducers().get(0).getSalesDepartment(FarmersAndWorkersScenario.MANUFACTURED).getLastAskedPrice());
            }

            System.out.println("Agricultural Production: " + agriculturalProduction.getMean() + ", Manufacturing Production: "
                    + manufacturingProduction.getMean() + ", manufacturing price: " + manufacturingPrice.getMean());

            Assert.assertEquals(1100, manufacturingProduction.getMean(), 50); //5% error allowed
            Assert.assertEquals(13995, agriculturalProduction.getMean(), 500);
            Assert.assertEquals(10.95, manufacturingPrice.getMean(), 0.5);


            macroII.finish();
        }
    }
}