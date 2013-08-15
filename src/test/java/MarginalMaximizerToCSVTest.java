/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

import agents.firm.sales.SalesDepartmentAllAtOnce;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.prediction.LearningDecreaseWithTimeSeriesSalesPredictor;
import agents.firm.sales.pricing.pid.SalesControlFlowPIDWithFixedInventory;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import au.com.bytecode.opencsv.CSVWriter;
import goods.GoodType;
import model.MacroII;
import model.scenario.MonopolistScenario;
import model.utilities.ActionOrder;
import model.utilities.scheduler.Priority;
import model.utilities.stats.DailyStatCollector;
import model.utilities.stats.PeriodicMarketObserver;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.io.FileWriter;
import java.io.IOException;

/**
 * <h4>Description</h4>
 * <p/> this is really a run, not a test. To analyze with R
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-08-09
 * @see
 */
public class MarginalMaximizerToCSVTest {


    public static void main1(String[] args)
    {


        //we know the profit maximizing equilibrium is q=220, price = 72
        final MacroII macroII = new MacroII(1376037116257l);
        MonopolistScenario scenario1 = new MonopolistScenario(macroII);
        //    scenario1.setAlwaysMoving(true);
        //   MonopolistScenario scenario1 = new MonopolistScenario(macroII);
        macroII.setScenario(scenario1);
        scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
        scenario1.setAskPricingStrategy(SimpleFlowSellerPID.class);
        if(macroII.random.nextBoolean())
            scenario1.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);
        else
            scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);


        //csv writer
        try {
            CSVWriter writer = new CSVWriter(new FileWriter("runs/maximizerTest.csv"));
            DailyStatCollector collector = new DailyStatCollector(macroII,writer);
            collector.start();
        } catch (IOException e) {
            System.err.println("failed to create the file!");
        }



        macroII.start();
        while(macroII.schedule.getTime()<30000)
            macroII.schedule.step(macroII);

        macroII.kill();
        macroII.finish();


    }


    public static void main2(String[] args)
    {

        final MacroII macroII = new MacroII(1376050709303l);



        MonopolistScenario scenario1 = new MonopolistScenario(macroII);


        //165,1,11,3,3
//        MARGINAL_WITH_UNIT_PID,class agents.firm.sales.pricing.pid.SimpleFlowSellerPID,class agents.firm.sales.SalesDepartmentOneAtATime -- 1376050709303

        System.out.println(MonopolistScenario.findWorkerTargetThatMaximizesProfits(165,1,11,3,3));

        //generate random parameters for labor supply and good demand
        int p0= 165; int p1= 1;
        scenario1.setDemandIntercept(p0); scenario1.setDemandSlope(p1);
        int w0=11; int w1=3;
        scenario1.setDailyWageIntercept(w0); scenario1.setDailyWageSlope(w1);
        int a=3;
        scenario1.setLaborProductivity(a);


        //    scenario1.setAlwaysMoving(true);
        //   MonopolistScenario scenario1 = new MonopolistScenario(macroII);
        macroII.setScenario(scenario1);
        //choose a control at random, but avoid always moving
        scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_WITH_UNIT_PID);

        //choose a sales control at random, but don't mix hill-climbing with inventory building since they aren't really compatible
        //        scenario1.setAskPricingStrategy(SalesControlFlowPIDWithFixedInventory.class);
        scenario1.setAskPricingStrategy(SimpleFlowSellerPID.class);

        //     scenario1.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);

        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);


        //csv writer
        try {
            CSVWriter writer = new CSVWriter(new FileWriter("runs/maximizerTest.csv"));
            DailyStatCollector collector = new DailyStatCollector(macroII,writer);
            collector.start();
        } catch (IOException e) {
            System.err.println("failed to create the file!");
        }


        macroII.start();
        while(macroII.schedule.getTime()<30000)
            macroII.schedule.step(macroII);

        System.out.println(scenario1.getMonopolist().getTotalWorkers());
        System.out.println(macroII.seed());

        //the pi maximizing labor force employed is:
     /*   int profitMaximizingLaborForce = MonopolistScenario.findWorkerTargetThatMaximizesProfits(p0,p1,w0,w1,a);
        int profitMaximizingQuantity = profitMaximizingLaborForce*a;
        int profitMaximizingPrice = p0 - p1 * profitMaximizingQuantity;

        System.out.println(scenario1.getControlType() + "," + scenario1.getAskPricingStrategy() + "," + scenario1.getSalesDepartmentType());
       */


        //you must be at most wrong by two (not well tuned and anyway sometimes it's hard!)

    }

    public static void main(String[] args)
    {

        final MacroII macroII = new MacroII(1376214006782l);



        final MonopolistScenario scenario1 = new MonopolistScenario(macroII);

        //generate random parameters for labor supply and good demand
        int p0= macroII.random.nextInt(100)+100; int p1= macroII.random.nextInt(3)+1;
        p0= 169; p1= 2;
        scenario1.setDemandIntercept(p0); scenario1.setDemandSlope(p1);
        int w0=macroII.random.nextInt(10)+10; int w1=macroII.random.nextInt(3)+1;
        w0=15; w1=1;
        int a=macroII.random.nextInt(3)+1;
        a=2;
        scenario1.setLaborProductivity(a);


        //    scenario1.setAlwaysMoving(true);
        //   MonopolistScenario scenario1 = new MonopolistScenario(macroII);
        macroII.setScenario(scenario1);
        //choose a control at random, but avoid always moving
        do{
            int controlChoices =  MonopolistScenario.MonopolistScenarioIntegratedControlEnum.values().length;
            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.values()[macroII.random.nextInt(controlChoices)]);
        }
        while (scenario1.getControlType().equals(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.HILL_CLIMBER_ALWAYS_MOVING));
        //choose a sales control at random, but don't mix hill-climbing with inventory building since they aren't really compatible
        if(macroII.random.nextBoolean() && scenario1.getControlType() != MonopolistScenario.MonopolistScenarioIntegratedControlEnum.HILL_CLIMBER_SIMPLE)
            scenario1.setAskPricingStrategy(SalesControlFlowPIDWithFixedInventory.class);
        else
            scenario1.setAskPricingStrategy(SimpleFlowSellerPID.class);

        if(macroII.random.nextBoolean())
            scenario1.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);
        else
            scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);

        //csv writer
        try {
            CSVWriter writer = new CSVWriter(new FileWriter("runs/maximizerTest.csv"));
            DailyStatCollector collector = new DailyStatCollector(macroII,writer);
            collector.start();
        } catch (IOException e) {
            System.err.println("failed to create the file!");
        }





        macroII.start();

        //give the purchase department a periodic observer that outputs to file
        try {
            CSVWriter writer = new CSVWriter(new FileWriter("runs/maximizerTestPeriodic.csv"));
            PeriodicMarketObserver observer = new PeriodicMarketObserver(scenario1.getMarkets().get(GoodType.GENERIC),macroII);
            observer.attachCSVWriter(writer);
            final LearningDecreaseWithTimeSeriesSalesPredictor predictor = new LearningDecreaseWithTimeSeriesSalesPredictor(observer);
            macroII.scheduleSoon(ActionOrder.DAWN, new Steppable() {
                @Override
                public void step(SimState state) {
                    scenario1.getMonopolist().getSalesDepartment(GoodType.GENERIC).setPredictorStrategy(predictor);
                }
            }, Priority.AFTER_STANDARD);
        } catch (IOException e) {
            System.err.println("failed to create the file!");
        }


        while(macroII.schedule.getTime()<10000)
            macroII.schedule.step(macroII);



        //the pi maximizing labor force employed is:
        int profitMaximizingLaborForce = MonopolistScenario.findWorkerTargetThatMaximizesProfits(p0,p1,w0,w1,a);
        int profitMaximizingQuantity = profitMaximizingLaborForce*a;
        int profitMaximizingPrice = p0 - p1 * profitMaximizingQuantity;

        System.out.println(p0 + "," + p1 + "," + w0 + "," + w1 + "," + a);
        System.out.println(scenario1.getControlType() + "," + scenario1.getAskPricingStrategy() + "," + scenario1.getSalesDepartmentType() + " -- " + macroII.seed());

        //you must be at most wrong by two (not well tuned and anyway sometimes it's hard!)
        System.out.println(scenario1.getMonopolist().getTotalWorkers());

    }


    public static void main5(String[] args)
    {

        final MacroII macroII = new MacroII(System.currentTimeMillis());


        MonopolistScenario scenario1 = new MonopolistScenario(macroII);

        //generate random parameters for labor supply and good demand
        int p0= 118; int p1= 3;
        scenario1.setDemandIntercept(p0); scenario1.setDemandSlope(p1);
        int w0=11; int w1=2;
        scenario1.setDailyWageIntercept(w0); scenario1.setDailyWageSlope(w1);
        int a=3;
        scenario1.setLaborProductivity(a);


        //    scenario1.setAlwaysMoving(true);
        //   MonopolistScenario scenario1 = new MonopolistScenario(macroII);
        macroII.setScenario(scenario1);
        //choose a control at random, but avoid always moving

        scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_WITH_UNIT_PID);

        //choose a sales control at random, but don't mix hill-climbing with inventory building since they aren't really compatible
        scenario1.setAskPricingStrategy(SimpleFlowSellerPID.class);

        scenario1.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);

        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println(p0 + "," + p1 + "," + w0 + "," + w1 +"," + a);
        System.out.println(scenario1.getControlType() + "," + scenario1.getAskPricingStrategy() + "," + scenario1.getSalesDepartmentType());


        //csv writer
        try {
            CSVWriter writer = new CSVWriter(new FileWriter("runs/maximizerTest.csv"));
            DailyStatCollector collector = new DailyStatCollector(macroII,writer);
            collector.start();
        } catch (IOException e) {
            System.err.println("failed to create the file!");
        }

        macroII.start();
        while(macroII.schedule.getTime()<10000)
            macroII.schedule.step(macroII);


        //the pi maximizing labor force employed is:
        int profitMaximizingLaborForce = MonopolistScenario.findWorkerTargetThatMaximizesProfits(p0,p1,w0,w1,a);
        int profitMaximizingQuantity = profitMaximizingLaborForce*a;
        int profitMaximizingPrice = p0 - p1 * profitMaximizingQuantity;

        System.out.println(p0 + "," + p1 + "," + w0 + "," + w1 +"," + a);
        System.out.println(scenario1.getControlType() + "," + scenario1.getAskPricingStrategy() + "," + scenario1.getSalesDepartmentType());

        //you must be at most wrong by two (not well tuned and anyway sometimes it's hard!)
        System.out.println(scenario1.getMonopolist().getTotalWorkers());

    }


    public static void main4(String[] args)
    {

        final MacroII macroII = new MacroII(1376144694474l);

        //we know the profit maximizing equilibrium is q=220, price = 72
        MonopolistScenario scenario1 = new MonopolistScenario(macroII);
        //    scenario1.setAlwaysMoving(true);
        //   MonopolistScenario scenario1 = new MonopolistScenario(macroII);
        macroII.setScenario(scenario1);
        scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
        scenario1.setAskPricingStrategy(SalesControlFlowPIDWithFixedInventory.class);
        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);

        if(macroII.random.nextBoolean())
            scenario1.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);
        else
            scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);

        //csv writer
        try {
            CSVWriter writer = new CSVWriter(new FileWriter("runs/maximizerTest.csv"));
            DailyStatCollector collector = new DailyStatCollector(macroII,writer);
            collector.start();
        } catch (IOException e) {
            System.err.println("failed to create the file!");
        }


        macroII.start();
        while(macroII.schedule.getTime()<10000)
            macroII.schedule.step(macroII);

     //   MARGINAL_PLANT_CONTROL,class agents.firm.sales.pricing.pid.SalesControlFlowPIDWithFixedInventory,class agents.firm.sales.SalesDepartmentAllAtOnce -- 1376124166923
     //   MARGINAL_PLANT_CONTROL,class agents.firm.sales.pricing.pid.SalesControlFlowPIDWithFixedInventory,class agents.firm.sales.SalesDepartmentOneAtATime -- 1376125667329


        System.out.println(scenario1.getControlType() + "," + scenario1.getAskPricingStrategy() + "," + scenario1.getSalesDepartmentType() + " -- " + macroII.seed());
        System.out.println(scenario1.getMonopolist().getTotalWorkers());
        System.out.println(macroII.getMarket(GoodType.GENERIC).getLastPrice());

    }

    public static void main3(String[] args)
    {

        final MacroII macroII = new MacroII(System.currentTimeMillis());

        MonopolistScenario scenario1 = new MonopolistScenario(macroII);

        //generate random parameters for labor supply and good demand


        int p0= 167; int p1= 3;
        scenario1.setDemandIntercept(p0); scenario1.setDemandSlope(p1);
        int w0=19; int w1=1;
        scenario1.setDailyWageIntercept(w0); scenario1.setDailyWageSlope(w1);
        int a=3;
        scenario1.setLaborProductivity(a);


        //    scenario1.setAlwaysMoving(true);
        //   MonopolistScenario scenario1 = new MonopolistScenario(macroII);
        macroII.setScenario(scenario1);
        //choose a control at random, but avoid always moving
        scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);

        //choose a sales control at random, but don't mix hill-climbing with inventory building since they aren't really compatible
        scenario1.setAskPricingStrategy(SalesControlFlowPIDWithFixedInventory.class);
        //        scenario1.setAskPricingStrategy(SimpleFlowSellerPID.class);

        //     scenario1.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);

        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);


        //csv writer
        try {
            CSVWriter writer = new CSVWriter(new FileWriter("runs/maximizerTest.csv"));
            DailyStatCollector collector = new DailyStatCollector(macroII,writer);
            collector.start();
        } catch (IOException e) {
            System.err.println("failed to create the file!");
        }


        macroII.start();
        while(macroII.schedule.getTime()<30000)
            macroII.schedule.step(macroII);


        //the pi maximizing labor force employed is:
     /*   int profitMaximizingLaborForce = MonopolistScenario.findWorkerTargetThatMaximizesProfits(p0,p1,w0,w1,a);
        int profitMaximizingQuantity = profitMaximizingLaborForce*a;
        int profitMaximizingPrice = p0 - p1 * profitMaximizingQuantity;

        System.out.println(scenario1.getControlType() + "," + scenario1.getAskPricingStrategy() + "," + scenario1.getSalesDepartmentType());
       */


        //you must be at most wrong by two (not well tuned and anyway sometimes it's hard!)

    }

}
