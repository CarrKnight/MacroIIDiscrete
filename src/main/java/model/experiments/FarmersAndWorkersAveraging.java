/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.experiments;

import agents.firm.purchases.prediction.FixedIncreasePurchasesPredictor;
import agents.firm.sales.prediction.FixedDecreaseSalesPredictor;
import model.MacroII;
import model.scenario.FarmersAndWorkersScenario;

import static model.experiments.tuningRuns.MarginalMaximizerPIDTuning.printProgressBar;

/**
 * <h4>Description</h4>
 * <p>
 * <p>
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-06-24
 * @see
 */
public class FarmersAndWorkersAveraging
{













    public static class RunResult{

        private double squaredDistanceFromPrice;

        private double squaredDistanceFromQuantity;

        public double getSquaredDistanceFromPrice() {
            return squaredDistanceFromPrice;
        }

        public double getSquaredDistanceFromQuantity() {
            return squaredDistanceFromQuantity;
        }


        public RunResult(double squaredDistanceFromPrice, double squaredDistanceFromQuantity) {
            this.squaredDistanceFromPrice = squaredDistanceFromPrice;
            this.squaredDistanceFromQuantity = squaredDistanceFromQuantity;
        }
    }






    public static RunResult fiveLearnedRuns(int productionPerWorker,
                                                          int numberOfFirms, int numberOfAgents,
                                                          int salesAverageLenght, int hrAverageLenght, int steps)
    {

        double squaredDistanceFromPrice=0;

        double squaredDistanceFromQuantity=0;

        for(int seed=0; seed<5; seed++)
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

            macroII.start();

            while (macroII.schedule.getTime() < steps) {
                macroII.schedule.step(macroII);
                printProgressBar(steps + 501, (int) macroII.schedule.getSteps(), 100);

            }
            for (int i = 0; i < 500; i++) {
                macroII.schedule.step(macroII);
                printProgressBar(steps + 501, (int) macroII.schedule.getSteps(), 100);
                squaredDistanceFromPrice += Math.pow(scenario.getGoodMarket().getYesterdayLastPrice() - 2.7,2);
                squaredDistanceFromQuantity += Math.pow(scenario.countManufacturedProduction() - 280,2);


            }

            macroII.finish();

        }
       return new RunResult(squaredDistanceFromPrice,squaredDistanceFromQuantity);
    }

}
