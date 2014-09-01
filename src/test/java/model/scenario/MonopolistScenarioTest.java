/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.scenario;

import agents.EconomicAgent;
import agents.firm.personell.HumanResources;
import agents.firm.purchases.prediction.FixedIncreasePurchasesPredictor;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentAllAtOnce;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.prediction.ErrorCorrectingSalesPredictor;
import agents.firm.sales.prediction.FixedDecreaseSalesPredictor;
import agents.firm.sales.pricing.pid.AdaptiveStockSellerPID;
import agents.firm.sales.pricing.pid.SalesControlFlowPIDWithFixedInventoryButTargetingFlowsOnly;
import agents.firm.sales.pricing.pid.SalesControlWithFixedInventoryAndPID;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import financial.market.Market;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.utilities.logs.LogLevel;
import model.utilities.logs.LogToConsole;
import model.utilities.stats.collectors.enums.PurchasesDataType;
import model.utilities.stats.collectors.enums.SalesDataType;
import org.junit.Assert;
import org.junit.Test;
import tests.MemoryLeakVerifier;

import java.nio.file.Paths;
import java.util.LinkedList;

import static org.junit.Assert.assertEquals;

/**
 * <h4>Description</h4>
 * <p/> Run the monopolist scenario, make sure the prices are where they should be
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-01-13
 * @see
 */
public class MonopolistScenarioTest {


    //all these use simpleFlowSellerPID

    @Test
    public void rightPriceAndQuantityTestAsHillClimber()
    {

        //run the test 5 times
        for(int i=0; i<10; i++)
        {
            long seed = System.currentTimeMillis();

            System.err.println("======================================= " + seed);
            //we know the profit maximizing equilibrium is q=220, price = 72
            final MacroII macroII = new MacroII(seed);
            MonopolistScenario scenario1 = new MonopolistScenario(macroII);
            //    scenario1.setAlwaysMoving(true);
            //   MonopolistScenario scenario1 = new MonopolistScenario(macroII);
            macroII.setScenario(scenario1);
            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.HILL_CLIMBER_SIMPLE);
            scenario1.setAskPricingStrategy(SimpleFlowSellerPID.class);
            if(macroII.random.nextBoolean())
                scenario1.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);
            else
                scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);


            macroII.start();
            while(macroII.schedule.getTime()<3500)
                macroII.schedule.step(macroII);


            assertEquals(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getLastPrice(), 79,1);
            assertEquals(scenario1.monopolist.getTotalWorkers(), 22,1);

            macroII.finish();
        }





    }

    //this one had failed, I needed to find out why!
    @Test
    public void problematicScenario1()
    {

        //run the test 15 times
        for(int i=0; i<5; i++)
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

            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);

            //choose a sales control at random, but don't mix hill-climbing with inventory building since they aren't really compatible
            scenario1.setAskPricingStrategy(SimpleFlowSellerPID.class);

            scenario1.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);

            System.out.println("-----------------------------------------------------------------------------------------------");
            System.out.println("-----------------------------------------------------------------------------------------------");
            System.out.println(p0 + "," + p1 + "," + w0 + "," + w1 +"," + a);
            System.out.println(scenario1.getControlType() + "," + scenario1.getAskPricingStrategy() + "," + scenario1.getSalesDepartmentType());


            macroII.start();
            while(macroII.schedule.getTime()<5000)
                macroII.schedule.step(macroII);


            //the pi maximizing labor force employed is:
            int profitMaximizingLaborForce = MonopolistScenario.findWorkerTargetThatMaximizesProfits(p0,p1,w0,w1,a);
            int profitMaximizingQuantity = profitMaximizingLaborForce*a;
            int profitMaximizingPrice = p0 - p1 * profitMaximizingQuantity;

            System.out.println(p0 + "," + p1 + "," + w0 + "," + w1 +"," + a);
            System.out.println(scenario1.getControlType() + "," + scenario1.getAskPricingStrategy() + "," + scenario1.getSalesDepartmentType());

            //you must be at most wrong by two (not well tuned and anyway sometimes it's hard!)
            assertEquals(scenario1.monopolist.getTotalWorkers(), profitMaximizingLaborForce,2);

            macroII.finish();

        }





    }

    @Test
    public void problematicScenario3()
    {

        //run the test 15 times
        for(int i=0; i<5; i++)
        {
            final MacroII macroII = new MacroII(System.currentTimeMillis());



            MonopolistScenario scenario1 = new MonopolistScenario(macroII);

            //generate random parameters for labor supply and good demand
            int p0= macroII.random.nextInt(100)+100; int p1= macroII.random.nextInt(3)+1;
            p0= 169; p1= 2;
            scenario1.setDemandIntercept(p0); scenario1.setDemandSlope(p1);
            int w0=macroII.random.nextInt(10)+10; int w1=macroII.random.nextInt(3)+1;
            w0=15; w1=1;
            scenario1.setDailyWageIntercept(w0); scenario1.setDailyWageSlope(w1);
            int a=macroII.random.nextInt(3)+1;
            a=2;
            scenario1.setLaborProductivity(a);


            //    scenario1.setAlwaysMoving(true);
            //   MonopolistScenario scenario1 = new MonopolistScenario(macroII);
            macroII.setScenario(scenario1);
            //choose a control at random, but avoid always moving
            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);

            //choose a sales control at random, but don't mix hill-climbing with inventory building since they aren't really compatible
            if(macroII.random.nextBoolean() && scenario1.getControlType() != MonopolistScenario.MonopolistScenarioIntegratedControlEnum.HILL_CLIMBER_SIMPLE)
                scenario1.setAskPricingStrategy(SalesControlWithFixedInventoryAndPID.class);
            else
                scenario1.setAskPricingStrategy(SimpleFlowSellerPID.class);

            if(macroII.random.nextBoolean())
                scenario1.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);
            else
                scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);



            macroII.start();
            while(macroII.schedule.getTime()<10000)
                macroII.schedule.step(macroII);



            //the pi maximizing labor force employed is:
            int profitMaximizingLaborForce = MonopolistScenario.findWorkerTargetThatMaximizesProfits(p0,p1,w0,w1,a);
            int profitMaximizingQuantity = profitMaximizingLaborForce*a;
            int profitMaximizingPrice = p0 - p1 * profitMaximizingQuantity;

            System.out.println(p0 + "," + p1 + "," + w0 + "," + w1 + "," + a);
            System.err.println(macroII.seed());
            System.out.println(scenario1.getControlType() + "," + scenario1.getAskPricingStrategy() + "," + scenario1.getSalesDepartmentType() + " -- " + macroII.seed());
            System.out.flush();

            assertEquals(scenario1.monopolist.getTotalWorkers(), profitMaximizingLaborForce,2);

            macroII.finish();

        }





    }

    @Test
    public void problematicScenario2()
    {

        //run the test 15 times
        for(int i=0; i<5; i++)
        {
            final MacroII macroII = new MacroII(1409111395812l);



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
            macroII.setScenario(scenario1);
            //choose a control at random, but avoid always moving
            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);

            //choose a sales control at random, but don't mix hill-climbing with inventory building since they aren't really compatible
            scenario1.setAskPricingStrategy(SimpleFlowSellerPID.class);

            //     scenario1.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);

            scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);



            macroII.start();
            while(macroII.schedule.getTime()<5000)
                macroII.schedule.step(macroII);

            System.out.println(p0 + "," + p1 + "," + w0 + "," + w1 +"," + a);
            System.out.println(scenario1.getControlType() + "," + scenario1.getAskPricingStrategy() + "," + scenario1.getSalesDepartmentType());
            System.out.println(macroII.seed());
            System.out.flush();

            //you must be at most wrong by two (not well tuned and anyway sometimes it's hard!)
            assertEquals(scenario1.monopolist.getTotalWorkers(),20,2);

            macroII.finish();

        }





    }

    //1385965894319
    //1386000975892
    //1386001620319


    @Test
    public void rightPriceAndQuantityTestRandomControlRandomSlopes()
    {

        //run the tests on failures first
        LinkedList<Long> previouslyFailedSeeds = new LinkedList<>();
        previouslyFailedSeeds.add(1409388720784l);
        previouslyFailedSeeds.add(1386007873067l);

        previouslyFailedSeeds.add(1386283852300l);

        previouslyFailedSeeds.add(1386003448078l);
        previouslyFailedSeeds.add(1386001620319l);
        previouslyFailedSeeds.add(1386000975892l);
        previouslyFailedSeeds.add(1385965894319l);
        previouslyFailedSeeds.add(1386019418405l);
        previouslyFailedSeeds.add(1409375048870l);
        previouslyFailedSeeds.add(1386089949520l);
        previouslyFailedSeeds.add(1386194999853l);
        previouslyFailedSeeds.add(1386263994865l);
        previouslyFailedSeeds.add(1386278630528l);
        previouslyFailedSeeds.add(1386280613790l);
        previouslyFailedSeeds.add(1386345532821l);


        //run the test 15 times
        for(int i=0; i<50; i++)
        {
            long seed = i < previouslyFailedSeeds.size() ? previouslyFailedSeeds.get(i) : System.currentTimeMillis();

            final MacroII macroII = new MacroII(seed);
            MonopolistScenario scenario1 = new MonopolistScenario(macroII);
            testRandomSlopeMonopolist(i, macroII, scenario1);


        }





    }

    public static void testRandomSlopeMonopolist(int seed, MacroII macroII, MonopolistScenario scenario1) {
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
        if(macroII.random.nextBoolean())
            scenario1.setAskPricingStrategy(SalesControlWithFixedInventoryAndPID.class);
        else
            scenario1.setAskPricingStrategy(SimpleFlowSellerPID.class);

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

    @Test
    public void rightPriceAndQuantityTestRandomControlRandomSlopesFlowsOnly()
    {





        //run the test 15 times
        for(int i=0; i<15; i++)
        {
            long seed = System.currentTimeMillis();

            final MacroII macroII = new MacroII(seed);
            MonopolistScenario scenario1 = new MonopolistScenario(macroII);





            //generate random parameters for labor supply and good demand
            int p0= macroII.random.nextInt(100)+100; int p1= macroII.random.nextInt(3)+1;
            scenario1.setDemandIntercept(p0); scenario1.setDemandSlope(p1);
            int w0=macroII.random.nextInt(10)+10; int w1=macroII.random.nextInt(3)+1;
            scenario1.setDailyWageIntercept(w0); scenario1.setDailyWageSlope(w1);
            int a=macroII.random.nextInt(3)+1;
            scenario1.setLaborProductivity(a);


            //    scenario1.setAlwaysMoving(true);
            //   MonopolistScenario scenario1 = new MonopolistScenario(macroII);
            macroII.setScenario(scenario1);
            //choose a control at random, but avoid always moving

            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
            scenario1.setWorkersToBeRehiredEveryDay(true);
            //choose a sales control at random, but don't mix hill-climbing with inventory building since they aren't really compatible
            scenario1.setAskPricingStrategy(SalesControlFlowPIDWithFixedInventoryButTargetingFlowsOnly.class);


            System.out.println(p0 + "," + p1 + "," + w0 + "," + w1 +"," + a);
            System.out.println(scenario1.getControlType() + "," + scenario1.getAskPricingStrategy() + "," + scenario1.getSalesDepartmentType() + " -- " + macroII.seed());

            macroII.start();
            while(macroII.schedule.getTime()<5000)
                macroII.schedule.step(macroII);


            //the pi maximizing labor force employed is:
            int profitMaximizingLaborForce = MonopolistScenario.findWorkerTargetThatMaximizesProfits(p0,p1,w0,w1,a);
            int profitMaximizingQuantity = profitMaximizingLaborForce*a;
            int profitMaximizingPrice = p0 - p1 * profitMaximizingQuantity;

            System.out.println(p0 + "," + p1 + "," + w0 + "," + w1 +"," + a);
            System.out.println(scenario1.getControlType() + "," + scenario1.getAskPricingStrategy() + "," + scenario1.getSalesDepartmentType() + " -- " + macroII.seed());
            System.out.flush();



            //you must be at most wrong by two (not well tuned and anyway sometimes it's hard!)
            assertEquals(scenario1.monopolist.getTotalWorkers(), profitMaximizingLaborForce,2);



            System.out.println(i + "---------------------------------------------------------------------------------------------");
            macroII.finish();

        }





    }


    @Test
    public void rightPriceAndQuantityTestRandomControlRandomSlopesWithShift()
    {        //run the tests on failures first
        LinkedList<Long> previouslyFailedSeeds = new LinkedList<>();
        previouslyFailedSeeds.add(1405625557302l);
        previouslyFailedSeeds.add(1405722912876l);
        previouslyFailedSeeds.add(1386003448078l);
        previouslyFailedSeeds.add(1386001620319l);
        previouslyFailedSeeds.add(1386000975892l);
        previouslyFailedSeeds.add(1385965894319l);
        previouslyFailedSeeds.add(1386007873067l);
        previouslyFailedSeeds.add(1386019418405l);
        previouslyFailedSeeds.add(1386020834125l);
        previouslyFailedSeeds.add(1386089949520l);
        previouslyFailedSeeds.add(1386194999853l);
        previouslyFailedSeeds.add(1386263994865l);
        previouslyFailedSeeds.add(1386278630528l);
        previouslyFailedSeeds.add(1386280613790l);
        previouslyFailedSeeds.add(1386283852300l);
        previouslyFailedSeeds.add(1386285136045l);
        previouslyFailedSeeds.add(1386287152277l);
        previouslyFailedSeeds.add(1386287670714l);


        //run the test 15 times
        for(int i=0; i<150; i++)
        {
            long seed = i < previouslyFailedSeeds.size() ? previouslyFailedSeeds.get(i) : System.currentTimeMillis();

            final MacroII macroII = new MacroII(seed);
            MonopolistScenario scenario1 = new MonopolistScenario(macroII);

            //generate random parameters for labor supply and good demand
            int p0= macroII.random.nextInt(100)+100; int p1= macroII.random.nextInt(3)+1;
            scenario1.setDemandIntercept(p0); scenario1.setDemandSlope(p1);
            int w0=macroII.random.nextInt(10)+10; int w1=macroII.random.nextInt(3)+1;
            scenario1.setDailyWageIntercept(w0); scenario1.setDailyWageSlope(w1);
            int a=macroII.random.nextInt(3)+1;
            scenario1.setLaborProductivity(a);



            macroII.setScenario(scenario1);
            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
            scenario1.setWorkersToBeRehiredEveryDay(true);
            //    if(macroII.random.nextBoolean())
            scenario1.setAskPricingStrategy(SalesControlWithFixedInventoryAndPID.class);
//            else
//                scenario1.setAskPricingStrategy(SimpleFlowSellerPID.class);

            if(macroII.random.nextBoolean())
                scenario1.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);
            else
                scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);


            System.out.println(p0 + "," + p1 + "," + w0 + "," + w1 +"," + a);
            System.out.println(scenario1.getControlType() + "," + scenario1.getAskPricingStrategy() + "," + scenario1.getSalesDepartmentType() + " -- " + macroII.seed());


            macroII.start();
            macroII.schedule.step(macroII);

            scenario1.monopolist.getSalesDepartment(UndifferentiatedGoodType.GENERIC).getPredictorStrategy().addLogEventListener(new LogToConsole(LogLevel.DEBUG,macroII));

            while(macroII.schedule.getTime()<5000)
                macroII.schedule.step(macroII);

            //first checkpoint
            System.out.println(p0 + "," + p1 + "," + w0 + "," + w1 +"," + a);
            System.out.flush();
            int profitMaximizingLaborForce = MonopolistScenario.findWorkerTargetThatMaximizesProfits(p0,p1,w0,w1,a);
            System.out.println("sales slope: " + (scenario1.monopolist.getSalesDepartment(UndifferentiatedGoodType.GENERIC).predictSalePriceAfterIncreasingProduction(0,1) -
                    scenario1.monopolist.getSalesDepartment(UndifferentiatedGoodType.GENERIC).predictSalePriceWhenNotChangingPoduction()) );
            System.out.println("purchaseSlope slope: " + (scenario1.monopolist.getHRs().iterator().next().predictPurchasePriceWhenIncreasingProduction() -
                            scenario1.monopolist.getHRs().iterator().next().predictPurchasePriceWhenNoChangeInProduction())
            );
            assertEquals(scenario1.monopolist.getTotalWorkers(), profitMaximizingLaborForce, 2);



            //now reset
            //generate random parameters for labor supply and good demand
            p0= macroII.random.nextInt(100)+100; p1= macroII.random.nextInt(3)+1;
            scenario1.resetDemand(p0,p1);
            w0=macroII.random.nextInt(10)+10; w1=macroII.random.nextInt(3)+1;
            scenario1.resetLaborSupply(w0,w1);
            System.out.println(p0 + "," + p1 + "," + w0 + "," + w1 +"," + a);
            System.out.flush();


            //another 5000 observations
            while(macroII.schedule.getTime()<10000)
                macroII.schedule.step(macroII);

            //the pi maximizing labor force employed is:
            profitMaximizingLaborForce = MonopolistScenario.findWorkerTargetThatMaximizesProfits(p0,p1,w0,w1,a);

            System.out.println(p0 + "," + p1 + "," + w0 + "," + w1 +"," + a);
            System.out.println(scenario1.getControlType() + "," + scenario1.getAskPricingStrategy() + "," + scenario1.getSalesDepartmentType() + " -- " + macroII.seed());
            System.out.println("sales slope: " + (scenario1.monopolist.getSalesDepartment(UndifferentiatedGoodType.GENERIC).predictSalePriceAfterIncreasingProduction(0,1) -
                            scenario1.monopolist.getSalesDepartment(UndifferentiatedGoodType.GENERIC).predictSalePriceWhenNotChangingPoduction())
            );
            System.out.println("purchaseSlope slope: " + (scenario1.monopolist.getHRs().iterator().next().predictPurchasePriceWhenIncreasingProduction() -
                            scenario1.monopolist.getHRs().iterator().next().predictPurchasePriceWhenNoChangeInProduction())
            );
            System.out.flush();



            //you must be at most wrong by two (not well tuned and anyway sometimes it's hard!)
            assertEquals(scenario1.monopolist.getTotalWorkers(), profitMaximizingLaborForce,3);



            System.out.println(i + "---------------------------------------------------------------------------------------------");
            macroII.finish();
        }





    }




    @Test
    public void problematicScenario5()
    {


        //run the test 5 times
        for(int i=0; i<5; i++)
        {
            final MacroII macroII = new MacroII(System.currentTimeMillis());



            MonopolistScenario scenario1 = new MonopolistScenario(macroII);

            //generate random parameters for labor supply and good demand
            int p0= macroII.random.nextInt(100)+100; int p1= macroII.random.nextInt(3)+1;
            scenario1.setDemandIntercept(p0); scenario1.setDemandSlope(p1);
            int w0=macroII.random.nextInt(10)+10; int w1=macroII.random.nextInt(3)+1;
            scenario1.setDailyWageIntercept(w0); scenario1.setDailyWageSlope(w1);
            int a=macroII.random.nextInt(3)+1;
            scenario1.setLaborProductivity(a);


            //    scenario1.setAlwaysMoving(true);
            //   MonopolistScenario scenario1 = new MonopolistScenario(macroII);
            macroII.setScenario(scenario1);
            //choose a control at random, but avoid always moving
          /*  do{
                int controlChoices =  MonopolistScenario.MonopolistScenarioIntegratedControlEnum.values().length;
                scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.values()[macroII.random.nextInt(controlChoices)]);
            }
            while (scenario1.getControlType().equals(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.HILL_CLIMBER_ALWAYS_MOVING));*/
            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
            //choose a sales control at random, but don't mix hill-climbing with inventory building since they aren't really compatible
            if(macroII.random.nextBoolean())
                scenario1.setAskPricingStrategy(SalesControlWithFixedInventoryAndPID.class);
            else
                scenario1.setAskPricingStrategy(SimpleFlowSellerPID.class);

            if(macroII.random.nextBoolean())
                scenario1.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);
            else
                scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);



            macroII.start();
            while(macroII.schedule.getTime()<5000)
                macroII.schedule.step(macroII);


            //the pi maximizing labor force employed is:
            int profitMaximizingLaborForce = MonopolistScenario.findWorkerTargetThatMaximizesProfits(p0,p1,w0,w1,a);
            int profitMaximizingQuantity = profitMaximizingLaborForce*a;
            int profitMaximizingPrice = p0 - p1 * profitMaximizingQuantity;

            System.out.println(p0 + "," + p1 + "," + w0 + "," + w1 +"," + a);
            System.out.println(scenario1.getControlType() + "," + scenario1.getAskPricingStrategy() + "," + scenario1.getSalesDepartmentType() + " -- " + macroII.seed());
            System.out.flush();
            //you must be at most wrong by two (not well tuned and anyway sometimes it's hard!)
            assertEquals(scenario1.monopolist.getTotalWorkers(), profitMaximizingLaborForce,2);
            macroII.finish();

        }





    }



    @Test
    public void rightPriceAndQuantityTestAsMarginalNoInventory()
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
            scenario1.setAskPricingStrategy(SimpleFlowSellerPID.class);
            if(macroII.random.nextBoolean())
                scenario1.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);
            else
                scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);


            //csv writer




            macroII.start();

            while(macroII.schedule.getTime()<5000)
            {
                macroII.schedule.step(macroII);
                //make sure the labor market is functioning properly!
                marketSanityCheck(macroII, scenario1);
            }

            System.out.println(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getYesterdayVolume());

            assertEquals(scenario1.monopolist.getTotalWorkers(), 22,1);
            assertEquals(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getLastPrice(), 79,1);


            macroII.finish();






        }


    }

    //already learned, this is from figure 10---> monosweep.
    @Test
    public void rightPriceAndQuantityTestAsMarginalNoInventoryFromStickyPricesPaper()
    {
        for(int i=0; i<5; i++)
        {
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            System.out.println("----------------------------------------------------------");
            System.out.println(macroII.seed());
            System.out.println("----------------------------------------------------------");
            MonopolistScenario scenario1 = new MonopolistScenario(macroII);
            //    scenario1.setAlwaysMoving(true);
            //   MonopolistScenario scenario1 = new MonopolistScenario(macroII);
            macroII.setScenario(scenario1);
            scenario1.setDemandIntercept(102);
            scenario1.setDemandSlope(2);
            scenario1.setDailyWageSlope(1);
            scenario1.setDailyWageIntercept(0);
            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
            scenario1.setAskPricingStrategy(SimpleFlowSellerPID.class);
            scenario1.setWorkersToBeRehiredEveryDay(true);

            if(macroII.random.nextBoolean())
                scenario1.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);
            else
                scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);


            //csv writer




            macroII.start();
            macroII.schedule.step(macroII);
//already learned
            final SalesDepartment salesDepartment = scenario1.getMonopolist().getSalesDepartment(UndifferentiatedGoodType.GENERIC);
            salesDepartment.setPredictorStrategy(new FixedDecreaseSalesPredictor(2));
            scenario1.getMonopolist().getHRs().iterator().next().setPredictor(new FixedIncreasePurchasesPredictor(1));

            while(macroII.schedule.getTime()<5000)
            {
                macroII.schedule.step(macroII);

            }

            System.out.println(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getYesterdayVolume());

            assertEquals(scenario1.monopolist.getTotalWorkers(), 17,1);
            assertEquals(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getLastPrice(), 68,2);

            macroII.finish();






        }


    }


    @Test
    public void rightPriceAndQuantityTestAsMarginalSimpleInventory()
    {
        for(int i=0; i<10; i++)
        {
            //we know the profit maximizing equilibrium is q=220, price = 72
            final MacroII macroII = new MacroII(1404576744501l);
            System.out.println("----------------------------------------------------------");
            System.out.println(macroII.seed());
            System.out.println("----------------------------------------------------------");
            MonopolistScenario scenario1 = new MonopolistScenario(macroII);
            //    scenario1.setAlwaysMoving(true);
            //   MonopolistScenario scenario1 = new MonopolistScenario(macroII);
            macroII.setScenario(scenario1);
            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
            scenario1.setAskPricingStrategy(AdaptiveStockSellerPID.class);
            if(macroII.random.nextBoolean())
                scenario1.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);
            else
                scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);


            //csv writer




            macroII.start();

            while(macroII.schedule.getTime()<5000)
            {
                macroII.schedule.step(macroII);
                System.out.println((macroII.getMarket(UndifferentiatedGoodType.GENERIC).getLastPrice()));
                //make sure the labor market is functioning properly!
            }

            System.out.println(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getYesterdayVolume());

            assertEquals(scenario1.monopolist.getTotalWorkers(), 22,1);
            assertEquals(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getLastPrice(), 79,1);


            macroII.finish();






        }


    }

    @Test
    public void rightPriceAndQuantityTestAsMarginalShouldRecoverIfIScrambleThePrice()
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
            scenario1.setAskPricingStrategy(SimpleFlowSellerPID.class);
            if(macroII.random.nextBoolean())
                scenario1.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);
            else
                scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);


            //strategy



            macroII.start();
            macroII.schedule.step(macroII);
            final SalesDepartment department = scenario1.getMonopolist().getSalesDepartment(UndifferentiatedGoodType.GENERIC);
            final SimpleFlowSellerPID strategy = new SimpleFlowSellerPID(department);
            department.setPredictorStrategy(new FixedDecreaseSalesPredictor(1));
            department.setAskPricingStrategy(strategy);



            while(macroII.schedule.getTime()<2000)
            {
                macroII.schedule.step(macroII);
            }
            strategy.setInitialPrice(20);   //scrambled  price
            while(macroII.schedule.getTime()<3000)
            {
                macroII.schedule.step(macroII);
            }
            System.out.println(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getYesterdayVolume());

            assertEquals(scenario1.monopolist.getTotalWorkers(), 22,1);
            assertEquals(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getLastPrice(), 79,1);



            macroII.finish();




        }


    }

    @Test
    public void rightPriceAndQuantityTestAsMarginalLearnedWithStickyAndDelay() throws IllegalAccessException {
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
            scenario1.setAskPricingStrategy(SimpleFlowSellerPID.class);
            scenario1.setBuyerDelay(50);
            if(macroII.random.nextBoolean())
                scenario1.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);
            else
                scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);


            //csv writer




            macroII.start();
            macroII.schedule.step(macroII);
            final SalesDepartment department = scenario1.getMonopolist().getSalesDepartment(UndifferentiatedGoodType.GENERIC);
            final SimpleFlowSellerPID stickyStrategy = new SimpleFlowSellerPID(department);
            department.setPredictorStrategy(new FixedDecreaseSalesPredictor(1));
            scenario1.getMonopolist().getHRs().iterator().next().setPredictor(new FixedIncreasePurchasesPredictor(1));
            stickyStrategy.setSpeed(50);
            department.setAskPricingStrategy(stickyStrategy);

            while(macroII.schedule.getTime()<5000)
            {
                macroII.schedule.step(macroII);
                //         System.out.println(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getLastPrice());
            }


            System.out.println(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getYesterdayVolume());

            assertEquals(scenario1.monopolist.getTotalWorkers(), 22,1);
            assertEquals(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getLastPrice(), 79,1);


            macroII.finish();



        }


    }



    private void marketSanityCheck(MacroII macroII, MonopolistScenario scenario1) {
        double workers = scenario1.monopolist.getHRs().iterator().next().getLatestObservation(PurchasesDataType.INFLOW);
        if(macroII.schedule.getTime()>1 && workers > 0){
            double wagesOffered = scenario1.monopolist.getHRs().iterator().next().getLatestObservation(PurchasesDataType.CLOSING_PRICES);
            Assert.assertEquals(14 + workers, wagesOffered, .01d);
            //because it's precario, at the end of the day you should have no workers!
        }

        double goods = scenario1.monopolist.getSalesDepartment(UndifferentiatedGoodType.GENERIC).getLatestObservation(SalesDataType.OUTFLOW);
        if(macroII.schedule.getTime()>1 && goods > 0){
            double askprice =  scenario1.monopolist.getSalesDepartment(UndifferentiatedGoodType.GENERIC).getLatestObservation(SalesDataType.CLOSING_PRICES);
            Assert.assertEquals("price: " + askprice + ", supposed price given trade: " + (101-goods),101-goods,askprice,.01d);
        }
    }


    //all these use SalesControlWithFixedInventoryAndPID




    @Test
    public void rightPriceAndQuantityTestAsMarginalWithSalesControlIDWithFixedInventory()
    {
        for(int i=0; i<10; i++)
        {
            //we know the profit maximizing equilibrium is q=220, price = 72
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            MonopolistScenario scenario1 = new MonopolistScenario(macroII);
            //    scenario1.setAlwaysMoving(true);
            //   MonopolistScenario scenario1 = new MonopolistScenario(macroII);
            macroII.setScenario(scenario1);
            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
            scenario1.setAskPricingStrategy(SalesControlWithFixedInventoryAndPID.class);

            if(macroII.random.nextBoolean())
                scenario1.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);
            else
                scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);

            //csv writer




            macroII.start();
            while(macroII.schedule.getTime()<5000)
                macroII.schedule.step(macroII);




            System.out.println(scenario1.getControlType() + "," + scenario1.getAskPricingStrategy() + "," + scenario1.getSalesDepartmentType() + " -- " + macroII.seed());
            assertEquals(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getTodayAveragePrice(), 79, 1);
            assertEquals(scenario1.monopolist.getTotalWorkers(), 22,1);




            macroII.finish();



        }


    }





    @Test
    public void testfindWorkerTargetThatMaximizesProfits()
    {
        Assert.assertEquals(MonopolistScenario.findWorkerTargetThatMaximizesProfits(101,1,14,1,1),22);


    }





    @Test
    public void memoryLeakTest() {
        for (int i = 0; i < 3; i++)
        {

            //we know the profit maximizing equilibrium is q=220, price = 72
            MacroII macroII = new MacroII(System.currentTimeMillis());
            System.out.println("----------------------------------------------------------");
            System.out.println(macroII.seed());
            System.out.println("----------------------------------------------------------");
            MonopolistScenario scenario1 = new MonopolistScenario(macroII);
            //    scenario1.setAlwaysMoving(true);
            //   MonopolistScenario scenario1 = new MonopolistScenario(macroII);
            macroII.setScenario(scenario1);
            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
            scenario1.setAskPricingStrategy(SimpleFlowSellerPID.class);
            if (macroII.random.nextBoolean())
                scenario1.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);
            else
                scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);


            //csv writer


            macroII.start();
            MemoryLeakVerifier[] agents = new MemoryLeakVerifier[macroII.getAgents().size()];
            int k = 0;
            for (EconomicAgent agent : macroII.getAgents()) {
                agents[k] = new MemoryLeakVerifier(agent);
                k++;
            }
            MemoryLeakVerifier[] markets = new MemoryLeakVerifier[macroII.getMarkets().size()];
            k = 0;
            for (Market market : macroII.getMarkets()) {
                markets[k] = new MemoryLeakVerifier(market);
                k++;
            }
            MemoryLeakVerifier model = new MemoryLeakVerifier(macroII);
            MemoryLeakVerifier scenarioLeak = new MemoryLeakVerifier(scenario1);

            while (macroII.schedule.getTime() < 5000) {
                macroII.schedule.step(macroII);
                //make sure the labor market is functioning properly!
            }

            System.out.println(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getYesterdayVolume());

            assertEquals(scenario1.monopolist.getTotalWorkers(), 22, 1);
            assertEquals(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getLastPrice(), 79, 1);


            macroII.finish();
            macroII = null;
            scenario1=null;

            System.gc();

            for (MemoryLeakVerifier ref : agents) {
                ref.assertGarbageCollected("agent");
            }
            for (MemoryLeakVerifier ref : markets)
                ref.assertGarbageCollected("market");

            model.assertGarbageCollected("model");
            scenarioLeak.assertGarbageCollected("scenario");


        }
    }









}