/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.scenario;

import model.MacroII;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.Assert;
import org.junit.Test;

import static model.experiments.tuningRuns.MarginalMaximizerPIDTuning.printProgressBar;

public class FarmersAndWorkersScenarioTest {

    @Test
    public void forcedCompetitiveRun200People() throws Exception {


        for(int j=0; j< 5; j++) {
            final FarmersAndWorkersResult farmersAndWorkersResult = runFarmersAndWorkersScenario(10, 200);
            Assert.assertEquals(1100, farmersAndWorkersResult.getManufacturingProduction(), 50); //5% error allowed
            Assert.assertEquals(13995, farmersAndWorkersResult.getAgriculturalProduction(), 500);
            Assert.assertEquals(10.95, farmersAndWorkersResult.getManufacturingPrice(),5);

        }
    }


    @Test
    public void forcedCompetitiveRun50People() throws Exception {


        for(int j=0; j< 5; j++) {
            final FarmersAndWorkersResult farmersAndWorkersResult = runFarmersAndWorkersScenario(10, 50);
            Assert.assertEquals(280, farmersAndWorkersResult.getManufacturingProduction(), 50); //5% error allowed
            Assert.assertEquals(869, farmersAndWorkersResult.getAgriculturalProduction(), 500);
            Assert.assertEquals(2.7, farmersAndWorkersResult.getManufacturingPrice(),5);

        }
    }


    public static class FarmersAndWorkersResult
    {
        private final double manufacturingProduction;
        private final double agriculturalProduction;
        private final double manufacturingPrice;

        public FarmersAndWorkersResult(double manufacturingProduction, double agriculturalProduction, double manufacturingPrice) {
            this.manufacturingProduction = manufacturingProduction;
            this.agriculturalProduction = agriculturalProduction;
            this.manufacturingPrice = manufacturingPrice;
        }

        public double getManufacturingProduction() {
            return manufacturingProduction;
        }

        public double getAgriculturalProduction() {
            return agriculturalProduction;
        }

        public double getManufacturingPrice() {
            return manufacturingPrice;
        }
    }


    public static FarmersAndWorkersResult runFarmersAndWorkersScenario(int productionPerWorker, int numberOfAgents)
    {
        final MacroII macroII = new MacroII(System.currentTimeMillis());
        FarmersAndWorkersScenario scenario1 = new FarmersAndWorkersScenario(macroII);

        macroII.setScenario(scenario1);

        scenario1.setLinearProductionPerWorker(productionPerWorker);
        scenario1.setNumberOfAgents(numberOfAgents);

        macroII.start();

        while (macroII.schedule.getTime() < 3000) {
            macroII.schedule.step(macroII);
            printProgressBar(3501, (int) macroII.schedule.getSteps(), 100);

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

        macroII.finish();

        return new FarmersAndWorkersResult(manufacturingProduction.getMean(),agriculturalProduction.getMean(), manufacturingPrice.getMean());
    }
}