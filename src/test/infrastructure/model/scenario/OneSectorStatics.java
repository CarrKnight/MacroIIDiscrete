package model.scenario;

import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.purchases.prediction.FixedIncreasePurchasesPredictor;
import agents.firm.purchases.prediction.PurchasesPredictor;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentAllAtOnce;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.prediction.ErrorCorrectingSalesPredictor;
import agents.firm.sales.prediction.FixedDecreaseSalesPredictor;
import agents.firm.sales.prediction.SalesPredictor;
import agents.firm.sales.pricing.AskPricingStrategy;
import agents.firm.sales.pricing.pid.InventoryBufferSalesControl;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import com.google.common.base.Preconditions;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.utilities.stats.collectors.enums.PurchasesDataType;
import model.utilities.stats.collectors.enums.SalesDataType;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

/**
 * Created by carrknight on 9/16/14.
 */
public class OneSectorStatics {


    public static void testClassicMonopolistScenaro(Class<? extends AskPricingStrategy> askPricingStrategy)
    {

        testClassicMonopolistScenaro(askPricingStrategy,(hr)-> PurchasesPredictor.Factory.newPurchasesPredictor(
                HumanResources.defaultPurchasePredictor,hr),
                (sales)-> SalesPredictor.Factory.newSalesPredictor(SalesDepartment.defaultPredictorStrategy,sales) );

    }

    public static void testClassicMonopolistScenaro(Class<? extends AskPricingStrategy> askPricingStrategy,
                                                    Function<HumanResources, PurchasesPredictor> hrPredictor,
                                                    Function<SalesDepartment, SalesPredictor> salesPredictor)
    {

        for(int i=0; i<5; i++)
        {
            //we know the profit maximizing equilibrium is q=220, price = 72
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            System.out.println("----------------------------------------------------------");
            System.out.println(macroII.seed());
            System.out.println("----------------------------------------------------------");
            MonopolistScenario scenario1 = new MonopolistScenario(macroII);
            //    scenario1.setAlwaysMoving(true);
            //   MonopolistScenario scenario1 = new MonopolistScenario(macroII);
            macroII.setScenario(scenario1);
            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
            scenario1.setAskPricingStrategy(askPricingStrategy);
            scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);

            //csv writer




            macroII.start();
            macroII.schedule.step(macroII);
            final SalesDepartment dept = scenario1.getMonopolist().getSalesDepartment(UndifferentiatedGoodType.GENERIC);
            dept.setPredictorStrategy(salesPredictor.apply(dept));
            final HumanResources hr = scenario1.getMonopolist().getHRs().iterator().next();
            hr.setPredictor(hrPredictor.apply(hr));

            while(macroII.schedule.getTime()<5000)
            {
                macroII.schedule.step(macroII);

            }

            System.out.println(scenario1.monopolist.getTotalWorkers());
            System.out.println(scenario1.monopolist.getSalesDepartment(UndifferentiatedGoodType.GENERIC).
                    getData().getLatestObservation(SalesDataType.PREDICTED_DEMAND_SLOPE));
            System.out.println(scenario1.monopolist.getHRs().iterator().next().getData().getLatestObservation(PurchasesDataType.PREDICTED_SUPPLY_SLOPE));

            assertEquals(scenario1.monopolist.getTotalWorkers(), 22,1);
            assertEquals(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getLastPrice(), 79,1);


            macroII.finish();






        }

    }

    public static void testRandomSlopeMonopolist(int seed, MacroII macroII, MonopolistScenario scenario1,
                                                 Class<? extends AskPricingStrategy> strategy)
    {

        //generate random parameters for labor supply and good demand
        int p0= macroII.random.nextInt(100)+100;
        int p1= macroII.random.nextInt(3)+1;
        scenario1.setDemandIntercept(p0);
        scenario1.setDemandSlope(p1);
        int w0=macroII.random.nextInt(10)+10;
        int w1=macroII.random.nextInt(3)+1;
        scenario1.setDailyWageIntercept(w0);
        scenario1.setDailyWageSlope(w1);
        int a=macroII.random.nextInt(3)+1;
        scenario1.setLaborProductivity(a);


        //    scenario1.setAlwaysMoving(true);
        //   MonopolistScenario scenario1 = new MonopolistScenario(macroII);
        macroII.setScenario(scenario1);
        //choose a control at random, but avoid always moving

        scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
        scenario1.setWorkersToBeRehiredEveryDay(true);
        //choose a sales control at random, but don't mix hill-climbing with inventory building since they aren't really compatible
        scenario1.setAskPricingStrategy(strategy);

        if(macroII.random.nextBoolean())
            scenario1.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);
        else
            scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);


        System.out.println(p0 + "," + p1 + "," + w0 + "," + w1 +"," + a);
        System.out.println(scenario1.getControlType() + "," + scenario1.getAskPricingStrategy() + "," + scenario1.getSalesDepartmentType() + " -- " + macroII.seed());

        macroII.start();
        macroII.schedule.step(macroII);
        try {
            final SalesDepartment department = scenario1.getMonopolist().getSalesDepartment(UndifferentiatedGoodType.GENERIC);
            ((ErrorCorrectingSalesPredictor)department.getPredictorStrategy()).setDebugWriter(Paths.get("runs", "tmp.csv"));

        }catch (Exception e){}

        while(macroII.schedule.getTime()<5000)
            macroII.schedule.step(macroII);


        //the pi maximizing labor force employed is:
        int profitMaximizingLaborForce = MonopolistScenario.findWorkerTargetThatMaximizesProfits(p0,p1,w0,w1,a);
        int profitMaximizingQuantity = profitMaximizingLaborForce*a;
        int profitMaximizingPrice = p0 - p1 * profitMaximizingQuantity;

        System.out.println("sales learned slope: " + scenario1.getMonopolist().
                getSalesDepartment(UndifferentiatedGoodType.GENERIC).getLatestObservation(SalesDataType.PREDICTED_DEMAND_SLOPE));
        final HumanResources hr = scenario1.getMonopolist().getHRs().iterator().next();
        System.out.println("hr learned slope: " + (hr.predictPurchasePriceWhenIncreasingProduction()-hr.predictPurchasePriceWhenNoChangeInProduction()));
        System.out.println(p0 + "," + p1 + "," + w0 + "," + w1 +"," + a);
        System.out.println(scenario1.getControlType() + "," + scenario1.getAskPricingStrategy() + "," + scenario1.getSalesDepartmentType() + " -- " + macroII.seed());
        System.out.flush();


        //you must be at most wrong by two (not well tuned and anyway sometimes it's hard!)
        assertEquals(scenario1.monopolist.getTotalWorkers(), profitMaximizingLaborForce,2);


        System.out.println(seed + "---------------------------------------------------------------------------------------------");
        macroII.finish();
    }

    public static void testRandomSlopeMonopolist(int seed, MacroII macroII, MonopolistScenario scenario1) {

        testRandomSlopeMonopolist(seed,macroII,scenario1,macroII.random.nextBoolean() ? InventoryBufferSalesControl.class : SimpleFlowSellerPID.class);
    }


    /**
     * defaults to 0 marginal prediction for sales
     * @param hrPredictor
     */
    public static void testCompetitiveHrCustomPredictor(Function<HumanResources, PurchasesPredictor> hrPredictor)
    {
        testCompetitive(hrPredictor, (sales) -> new FixedDecreaseSalesPredictor(0));
    }

    /**
     * defaults to 0 marginal prediction for sales
     */
    public static void testCompetitiveSalesCustomPredictor(Function<SalesDepartment, SalesPredictor> salesPredictor)
    {
        testCompetitive( (hr)-> new FixedIncreasePurchasesPredictor(0), salesPredictor);
    }


    /**
     * a simple 5 competitors, 5 run tests, useful for integration tests of some predictors
     * @param hrPredictor the hr predictor, cannot be null
     * @param salesPredictor the sales predictor, cannot be null
     */
    public static void testCompetitive(Function<HumanResources, PurchasesPredictor> hrPredictor,
                                       Function<SalesDepartment, SalesPredictor> salesPredictor) {
        Preconditions.checkNotNull(hrPredictor);
        Preconditions.checkNotNull(salesPredictor);
        int competitors = 4;

        for (int i = 0; i < 5; i++) {

            final MacroII macroII = new MacroII(System.currentTimeMillis());   //1387582416533
            final TripolistScenario scenario1 = new TripolistScenario(macroII);

            scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
            scenario1.setAdditionalCompetitors(competitors);
            scenario1.setWorkersToBeRehiredEveryDay(true);
            scenario1.setDemandIntercept(102);

            //assign scenario
            macroII.setScenario(scenario1);

            macroII.start();

            macroII.schedule.step(macroII);
            for (Firm firm : scenario1.getCompetitors()) {
                for (HumanResources hr : firm.getHRs())
                    hr.setPredictor(hrPredictor.apply(hr));
                final SalesDepartment salesDepartment = firm.getSalesDepartment(UndifferentiatedGoodType.GENERIC);
                salesDepartment.setPredictorStrategy(salesPredictor.apply(salesDepartment));
            }


            while (macroII.schedule.getTime() < 10000) {
                macroII.schedule.step(macroII);


            }

            SummaryStatistics prices = new SummaryStatistics();
            SummaryStatistics quantities = new SummaryStatistics();
            for (int j = 0; j < 500; j++) {
                macroII.schedule.step(macroII);
//                    assert !Float.isNaN(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayAveragePrice());
                prices.addValue(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayAveragePrice());
                quantities.addValue(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayVolume());


            }


            System.out.println(prices.getMean() + " - " + quantities.getMean() + "/"  +
                    "----" + macroII.seed() + " | " + macroII.getMarket(UndifferentiatedGoodType.GENERIC).getLastDaysAveragePrice());
            System.out.println("standard deviations: price : " + prices.getStandardDeviation() + " , quantity: " + quantities.getStandardDeviation());
            printSlopes(scenario1);

            assertEquals(prices.getMean(), 58, 5);
//                assertTrue(String.valueOf(prices.getStandardDeviation()),prices.getStandardDeviation() < 5.5);
            assertEquals(quantities.getMean(), 44, 5);
//                assertTrue(String.valueOf(prices.getStandardDeviation()),quantities.getStandardDeviation() < 5.5);

        }
    }

    public static void printSlopes(TripolistScenario scenario1) {
        int additionalCompetitors = scenario1.getAdditionalCompetitors();
        //slopes
        double[] salesSlopes = new double[additionalCompetitors+1];
        double[] hrSlopes = new double[additionalCompetitors+1];
        final LinkedList<Firm> competitorList = scenario1.getCompetitors();
        for(int k=0; k<salesSlopes.length; k++)
        {
            salesSlopes[k] =competitorList.get(k).getSalesDepartment(UndifferentiatedGoodType.GENERIC).getLatestObservation(SalesDataType.PREDICTED_DEMAND_SLOPE);
            final HumanResources hr = competitorList.get(k).getHRs().iterator().next();
            hrSlopes[k] =  hr.predictPurchasePriceWhenIncreasingProduction()-hr.predictPurchasePriceWhenNoChangeInProduction();
        }
        System.out.println("learned sales slopes: " + Arrays.toString(salesSlopes));
        System.out.println("learned purchases slopes: " + Arrays.toString(hrSlopes));
    }
}
