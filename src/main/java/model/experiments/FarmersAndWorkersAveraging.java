/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.experiments;

import agents.firm.purchases.prediction.FixedIncreasePurchasesPredictor;
import agents.firm.sales.prediction.FixedDecreaseSalesPredictor;
import agents.firm.utilities.LastClosingPriceEcho;
import agents.firm.utilities.WeightedPriceAverager;
import model.MacroII;
import model.scenario.FarmersAndWorkersScenario;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;

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








    public static void main(String[] args) throws IOException {

        PrintWriter writer;/*  = new PrintWriter(new FileWriter(Paths.get("runs","averageLearned.csv").toFile()));
        writer.println("numberOfCompetitors" + " , " + "salesPeriod" + " ," + "hrPeriod" + ","+ "QDistance "+ "," + "PDistance");


        for(int numberOfCompetitors = 1; numberOfCompetitors<10; numberOfCompetitors++)

            for(int salesPeriod = 1; salesPeriod<20; salesPeriod++)
                for(int hrPeriod = 1; hrPeriod<20; hrPeriod++)
                {
                    final RunResult runResult = farmersAndWorkersCompetitiveRun(10, numberOfCompetitors, 50, salesPeriod, hrPeriod, 7000, true, true);
                    final String parameter = numberOfCompetitors + " , " + salesPeriod + " ," + hrPeriod + "," + runResult.getSquaredDistanceFromQuantity() + "," +
                            runResult.getSquaredDistanceFromPrice();
                    writer.println(parameter);
                    writer.flush();
                }

        */
        writer = new PrintWriter(new FileWriter(Paths.get("runs", "averageNotLearned.csv").toFile()));
        writer.println("numberOfCompetitors" + " , " + "salesPeriod" + " ," + "hrPeriod" + ","+ "QDistance "+ "," + "PDistance");
        for(int numberOfCompetitors = 1; numberOfCompetitors<10; numberOfCompetitors++)

            for(int salesPeriod = 0; salesPeriod<20; salesPeriod++)
                for(int hrPeriod = 0; hrPeriod<20; hrPeriod++)
                {
                    final RunResult runResult = farmersAndWorkersCompetitiveRun(10, numberOfCompetitors, 50, salesPeriod, hrPeriod, 7000, false, false);
                    final String result = numberOfCompetitors + " , " + salesPeriod + " ," + hrPeriod + "," + runResult.getSquaredDistanceFromQuantity() + "," +
                            runResult.getSquaredDistanceFromPrice();
                    System.out.println(result);
                    writer.println(result);
                    writer.flush();
                }


    }






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






    public static RunResult farmersAndWorkersCompetitiveRun(int productionPerWorker,
                                                            int numberOfFirms, int numberOfAgents,
                                                            int salesAverageLength, int hrAverageLength, int steps, boolean salesLearned, boolean hrLearned)
    {

        double squaredDistanceFromPrice=0;

        double squaredDistanceFromQuantity=0;

        for(int seed=0; seed<5; seed++)
        {
            final MacroII macroII = new MacroII(seed);
            FarmersAndWorkersScenario scenario = new FarmersAndWorkersScenario(macroII);
            if(salesLearned)
                scenario.setSalesPredictorSupplier(() -> new FixedDecreaseSalesPredictor(0));
            if(hrLearned)
                scenario.setHrPredictorSupplier(() -> new FixedIncreasePurchasesPredictor(0));

            if(salesAverageLength == 0)
                scenario.setSalesDepartmentManipulator(salesDepartment -> salesDepartment.setPriceAverager(new LastClosingPriceEcho()));
            else
                scenario.setSalesDepartmentManipulator(salesDepartment -> salesDepartment.setPriceAverager(new WeightedPriceAverager(salesAverageLength)));
            if(hrAverageLength == 0)
                scenario.setHrManipulator(hr -> hr.setPriceAverager(new LastClosingPriceEcho()));
            else
                scenario.setHrManipulator(hr -> hr.setPriceAverager(new WeightedPriceAverager(hrAverageLength)));

            macroII.setScenario(scenario);

            scenario.setLinearProductionPerWorker(productionPerWorker);
            scenario.setNumberOfFirms(numberOfFirms);
            scenario.setNumberOfAgents(numberOfAgents);

            macroII.start();

            while (macroII.schedule.getTime() < steps) {
                macroII.schedule.step(macroII);
                printProgressBar(steps + 501, (int) macroII.schedule.getSteps(), 100);

            }
            //make sure it's correct!
            if(hrAverageLength == 0)
                assert scenario.getProducers().get(0).getHRs().iterator().next().getPriceAverager() instanceof  LastClosingPriceEcho;
            else
                assert scenario.getProducers().get(0).getHRs().iterator().next().getPriceAverager() instanceof  WeightedPriceAverager;

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
