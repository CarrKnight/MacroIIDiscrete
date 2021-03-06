package model.scenario;

import agents.firm.purchases.prediction.FixedIncreasePurchasesPredictor;
import agents.firm.sales.prediction.FixedDecreaseSalesPredictor;
import model.MacroII;
import model.utilities.stats.collectors.DailyStatCollector;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.io.File;
import java.nio.file.Path;

import static model.experiments.tuningRuns.MarginalMaximizerPIDTuning.printProgressBar;

/**
 * Created by carrknight on 9/16/14.
 */
public class FarmersStatics {
    public static FarmersAndWorkersResult runLearningFarmersAndWorkers(int productionPerWorker, int numberOfFirms,
                                                                       int numberOfAgents, long seed,
                                                                       File csvFile, Path logFile, int steps)
    {
        System.out.println(seed);
        final MacroII macroII = new MacroII(seed);
        FarmersAndWorkersScenario scenario = new FarmersAndWorkersScenario(macroII);

        macroII.setScenario(scenario);

        scenario.setLinearProductionPerWorker(productionPerWorker);
        scenario.setNumberOfFirms(numberOfFirms);
        scenario.setNumberOfAgents(numberOfAgents);


        if(csvFile != null)
            DailyStatCollector.addDailyStatCollectorToModel(csvFile, macroII);

        macroII.start();

        if(logFile != null)
            scenario.attachLogger(logFile);

        while (macroII.schedule.getTime() < steps) {
            macroII.schedule.step(macroII);
            printProgressBar(steps+501, (int) macroII.schedule.getSteps(), 100);

        }
        SummaryStatistics manufacturingProduction = new SummaryStatistics();
        SummaryStatistics agriculturalProduction = new SummaryStatistics();
        SummaryStatistics manufacturingPrice = new SummaryStatistics();
        for (int i = 0; i < 500; i++) {
            macroII.schedule.step(macroII);
            printProgressBar(steps+501, (int) macroII.schedule.getSteps(), 100);
            manufacturingProduction.addValue(scenario.countManufacturedProduction());
            agriculturalProduction.addValue(scenario.countAgricultureProduction());
            manufacturingPrice.addValue(scenario.getGoodMarket().getTodayAveragePrice());
        }

        System.out.println("Manufacturing Production: " + manufacturingProduction.getMean() +
                ", Agricultural Production: " + agriculturalProduction.getMean() +
                ", manufacturing price: " + manufacturingPrice.getMean());

        macroII.finish();

        return new FarmersAndWorkersResult(manufacturingProduction.getMean(),agriculturalProduction.getMean(), manufacturingPrice.getMean());
    }

    public static FarmersAndWorkersResult runForcedCompetitiveFarmersAndWorkersScenario(int productionPerWorker,
                                                                                        int numberOfFirms, int numberOfAgents, long seed,
                                                                                        File csvFile, Path logFile, int steps)
    {
        System.out.println(seed);
        final MacroII macroII = new MacroII(seed);
        FarmersAndWorkersScenario scenario = new FarmersAndWorkersScenario(macroII);
        scenario.setSalesPredictorSupplier(() -> new FixedDecreaseSalesPredictor(0));
        scenario.setHrPredictorSupplier(() -> new FixedIncreasePurchasesPredictor(0));

        macroII.setScenario(scenario);

        scenario.setLinearProductionPerWorker(productionPerWorker);
        scenario.setNumberOfFirms(numberOfFirms);
        scenario.setNumberOfAgents(numberOfAgents);


        if(csvFile != null)
            DailyStatCollector.addDailyStatCollectorToModel(csvFile, macroII);

        macroII.start();

        if(logFile != null)
            scenario.attachLogger(logFile);

        while (macroII.schedule.getTime() < steps) {
            macroII.schedule.step(macroII);
            printProgressBar(steps+501, (int) macroII.schedule.getSteps(), 100);

        }
        SummaryStatistics manufacturingProduction = new SummaryStatistics();
        SummaryStatistics agriculturalProduction = new SummaryStatistics();
        SummaryStatistics manufacturingPrice = new SummaryStatistics();
        for (int i = 0; i < 500; i++) {
            macroII.schedule.step(macroII);
            printProgressBar(steps+501, (int) macroII.schedule.getSteps(), 100);
            manufacturingProduction.addValue(scenario.countManufacturedProduction());
            agriculturalProduction.addValue(scenario.countAgricultureProduction());
            manufacturingPrice.addValue(scenario.getGoodMarket().getTodayAveragePrice());
        }

        System.out.println("Manufacturing Production: " + manufacturingProduction.getMean() +
                        ", Agricultural Production: " + agriculturalProduction.getMean() +
                ", manufacturing price: " + manufacturingPrice.getMean());

        macroII.finish();

        return new FarmersAndWorkersResult(manufacturingProduction.getMean(),agriculturalProduction.getMean(), manufacturingPrice.getMean());
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
}
