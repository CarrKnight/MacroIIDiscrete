/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.experiments.tuningRuns;

import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.production.control.PlantControl;
import agents.firm.production.control.facades.MarginalPlantControl;
import agents.firm.purchases.prediction.FixedIncreasePurchasesPredictor;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.prediction.FixedDecreaseSalesPredictor;
import agents.firm.sales.pricing.pid.SalesControlWithFixedInventoryAndPID;
import agents.firm.utilities.ExponentialPriceAverager;
import agents.firm.utilities.PriceAverager;
import agents.firm.utilities.WeightedPriceAverager;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.scenario.MonopolistScenario;
import model.scenario.TripolistScenario;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;

/**
 * <h4>Description</h4>
 * <p> is there an averaging method that works okay?
 * <p>
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-06-14
 * @see
 */
public class CompetitiveAveragingGridSearch {





    public static void main(String[] args) throws IOException {
        FileWriter writer = new FileWriter(Paths.get("runs", "tunings", "averagingTuning.csv").toFile());

        for(float hrWeight= .1f; hrWeight<1f;hrWeight= round(hrWeight+.1f,1))
            for(float salesWeight= .1f; salesWeight<1f;salesWeight= round(salesWeight+.1f,1))
                for(PriceAverager.NoTradingDayPolicy hrPolicy : PriceAverager.NoTradingDayPolicy.values())
                    for(PriceAverager.NoTradingDayPolicy salesPolicy : PriceAverager.NoTradingDayPolicy.values())
                    {
                        final CompetitiveAveragingResult r = exponentialRuns(hrWeight, hrPolicy, salesWeight, salesPolicy,20);
                        final String line = hrWeight + "," + hrPolicy + "," + salesWeight + "," + salesPolicy + "," + r.getPrice() + "," + r.getQuantity() + "," + r.getStd();
                        System.out.println(line);
                        writer.write(line+"\n");
                    }


        writer = new FileWriter(Paths.get("runs", "tunings", "weightedAveragingTuning.csv").toFile());
        for(int hrDays = 1; hrDays <=30; hrDays++)
            for(int salesDays = 1; salesDays <=30; salesDays++)
            {
                final CompetitiveAveragingResult r = weightedRun(hrDays,salesDays);
                final String line = hrDays + "," + salesDays + "," + r.getPrice() + "," + r.getQuantity() + "," + r.getStd();
                System.out.println(line);
                writer.write(line+"\n");
                writer.flush();

            }

        writer = new FileWriter(Paths.get("runs", "tunings", "doesSpeedMatters.csv").toFile());

        for(int speed=1; speed<100; speed++) {
            final CompetitiveAveragingResult r = exponentialRuns(.8f, PriceAverager.NoTradingDayPolicy.COUNT_AS_LAST_CLOSING_PRICE, .8f,
                    PriceAverager.NoTradingDayPolicy.COUNT_AS_LAST_CLOSING_PRICE, speed);
            final String line = speed + "," + r.getPrice() + "," + r.getQuantity() + "," + r.getStd();
            System.out.println(line);
            writer.write(line+"\n");
        }


    }


    public static CompetitiveAveragingResult exponentialRuns(float hrWeight, PriceAverager.NoTradingDayPolicy hrPolicy,
                                                             float salesWeight, PriceAverager.NoTradingDayPolicy salesPolicy,
                                                             int maximizerAveragePeriod){

        SummaryStatistics averageResultingPrice = new SummaryStatistics();
        SummaryStatistics averageResultingQuantity = new SummaryStatistics();
        SummaryStatistics averageStandardDeviation = new SummaryStatistics();
        for (int i = 0; i < 5; i++)
        {
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            final TripolistScenario scenario1 = new TripolistScenario(macroII);

            scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
            scenario1.setAskPricingStrategy(SalesControlWithFixedInventoryAndPID.class);
            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
            scenario1.setAdditionalCompetitors(4);
            scenario1.setWorkersToBeRehiredEveryDay(true);
            scenario1.setDemandIntercept(102);


            scenario1.setSalesPricePreditorStrategy(FixedDecreaseSalesPredictor.class);


            //assign scenario
            macroII.setScenario(scenario1);

            macroII.start();

            macroII.schedule.step(macroII);
            for (Firm firm : scenario1.getCompetitors()) {
                for (HumanResources hr : firm.getHRs()) {
                    hr.setPredictor(new FixedIncreasePurchasesPredictor(0));
                    hr.setPriceAverager(new ExponentialPriceAverager(hrWeight, hrPolicy));
                }
                firm.getSalesDepartment(UndifferentiatedGoodType.GENERIC).setPriceAverager(new ExponentialPriceAverager(salesWeight, salesPolicy));
                firm.getSalesDepartment(UndifferentiatedGoodType.GENERIC).setPredictorStrategy(new FixedDecreaseSalesPredictor(0));
            }


            for(final PlantControl control : scenario1.getMaximizers())
            {
                ((MarginalPlantControl)control).getMaximizer().setHowManyDaysBeforeEachCheck(maximizerAveragePeriod);
            }

            while (macroII.schedule.getTime() < 10000) {
                macroII.schedule.step(macroII);
            }

            SummaryStatistics prices = new SummaryStatistics();
            SummaryStatistics quantities = new SummaryStatistics();
            for (int j = 0; j < 500; j++) {
                macroII.schedule.step(macroII);
                prices.addValue(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayAveragePrice());
                quantities.addValue(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayVolume());


            }




            //okay?
            averageResultingPrice.addValue(prices.getMean());
            averageResultingQuantity.addValue(quantities.getMean());
            averageStandardDeviation.addValue(prices.getStandardDeviation());

        }

        //okay?
        return new CompetitiveAveragingResult(averageResultingPrice.getMean(),averageResultingQuantity.getMean(),averageStandardDeviation.getMean());


    }



    public static CompetitiveAveragingResult weightedRun(int hrDays, int salesDays){

        SummaryStatistics averageResultingPrice = new SummaryStatistics();
        SummaryStatistics averageResultingQuantity = new SummaryStatistics();
        SummaryStatistics averageStandardDeviation = new SummaryStatistics();
        for (int i = 0; i < 5; i++)
        {
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            final TripolistScenario scenario1 = new TripolistScenario(macroII);

            scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
            scenario1.setAskPricingStrategy(SalesControlWithFixedInventoryAndPID.class);
            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
            scenario1.setAdditionalCompetitors(4);
            scenario1.setWorkersToBeRehiredEveryDay(true);
            scenario1.setDemandIntercept(102);


            scenario1.setSalesPricePreditorStrategy(FixedDecreaseSalesPredictor.class);


            //assign scenario
            macroII.setScenario(scenario1);

            macroII.start();

            macroII.schedule.step(macroII);
            for (Firm firm : scenario1.getCompetitors()) {
                for (HumanResources hr : firm.getHRs()) {
                    hr.setPredictor(new FixedIncreasePurchasesPredictor(0));
                    hr.setPriceAverager(new WeightedPriceAverager(hrDays));
                }
                firm.getSalesDepartment(UndifferentiatedGoodType.GENERIC).setPriceAverager(new WeightedPriceAverager(salesDays));
                firm.getSalesDepartment(UndifferentiatedGoodType.GENERIC).setPredictorStrategy(new FixedDecreaseSalesPredictor(0));
            }

            while (macroII.schedule.getTime() < 10000) {
                macroII.schedule.step(macroII);
            }

            SummaryStatistics prices = new SummaryStatistics();
            SummaryStatistics quantities = new SummaryStatistics();
            for (int j = 0; j < 500; j++) {
                macroII.schedule.step(macroII);
                prices.addValue(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayAveragePrice());
                quantities.addValue(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayVolume());


            }




            //okay?
            averageResultingPrice.addValue(prices.getMean());
            averageResultingQuantity.addValue(quantities.getMean());
            averageStandardDeviation.addValue(prices.getStandardDeviation());

        }

        //okay?
        return new CompetitiveAveragingResult(averageResultingPrice.getMean(),averageResultingQuantity.getMean(),averageStandardDeviation.getMean());


    }


    private static class CompetitiveAveragingResult{
        private final double price;
        private final double quantity;
        private final double std;

        private CompetitiveAveragingResult(double price, double quantity, double std) {
            this.price = price;
            this.quantity = quantity;
            this.std = std;
        }

        public double getPrice() {
            return price;
        }

        public double getQuantity() {
            return quantity;
        }

        public double getStd() {
            return std;
        }
    }






    /**
     * Round to certain number of decimals
     *
     * @param d
     * @param decimalPlace
     * @return
     */
    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }



}
