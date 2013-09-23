package model.scenario;

import agents.firm.sales.SalesDepartmentAllAtOnce;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.pricing.pid.SalesControlWithFixedInventoryAndPID;
import agents.firm.sales.pricing.pid.SmoothedDailyInventoryPricingStrategy;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import au.com.bytecode.opencsv.CSVWriter;
import goods.GoodType;
import model.MacroII;
import model.utilities.stats.collectors.DailyStatCollector;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;

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
        for(int i=0; i<5; i++)
        {
            //we know the profit maximizing equilibrium is q=220, price = 72
            final MacroII macroII = new MacroII(System.currentTimeMillis());
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


            assertEquals(macroII.getMarket(GoodType.GENERIC).getLastPrice(), 79,1);
            assertEquals(scenario1.monopolist.getTotalWorkers(), 22,1);
        }





    }

    //this one had failed, I needed to find out why!
    @Test
    public void problematicScenario1()
    {

        //run the test 15 times
        for(int i=0; i<15; i++)
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

        }





    }

    @Test
    public void problematicScenario3()
    {

        //run the test 15 times
        for(int i=0; i<15; i++)
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
                scenario1.setAskPricingStrategy(SmoothedDailyInventoryPricingStrategy.class);
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

        }





    }

  @Test
    public void problematicScenario2()
    {

        //run the test 15 times
        for(int i=0; i<15; i++)
        {
            final MacroII macroII = new MacroII(System.currentTimeMillis());



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

        }





    }

    @Test
    public void rightPriceAndQuantityTestRandomControlRandomSlopes()
    {


        //run the test 15 times
        for(int i=0; i<150; i++)
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
                scenario1.setAskPricingStrategy(SmoothedDailyInventoryPricingStrategy.class);
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

            //you must be at most wrong by two (not well tuned and anyway sometimes it's hard!)
            assertEquals(scenario1.monopolist.getTotalWorkers(), profitMaximizingLaborForce,2);

        }





    }


    @Test
    public void rightPriceAndQuantityTestAsMarginal()
    {
        for(int i=0; i<25; i++)
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
                macroII.schedule.step(macroII);


            assertEquals(macroII.getMarket(GoodType.GENERIC).getLastPrice(), 79,1);
            assertEquals(scenario1.monopolist.getTotalWorkers(), 22,1);








        }


    }






    //all these use SmoothedDailyInventoryPricingStrategy




    @Test
    public void rightPriceAndQuantityTestAsMarginalWithSalesControlFlowPIDWithFixedInventory()
    {
        for(int i=0; i<50; i++)
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



            try {
                CSVWriter writer = new CSVWriter(new FileWriter("runs/maximizerTest.csv"));
                DailyStatCollector collector = new DailyStatCollector(macroII,writer);
                collector.start();
            } catch (IOException e) {
                System.err.println("failed to create the file!");
            }

            macroII.start();
            while(macroII.schedule.getTime()<5000)
                macroII.schedule.step(macroII);




            System.out.println(scenario1.getControlType() + "," + scenario1.getAskPricingStrategy() + "," + scenario1.getSalesDepartmentType() + " -- " + macroII.seed());
            assertEquals(macroII.getMarket(GoodType.GENERIC).getLastPrice(), 79, 1);
            assertEquals(scenario1.monopolist.getTotalWorkers(), 22,1);







        }


    }





    @Test
    public void testfindWorkerTargetThatMaximizesProfits()
    {
        Assert.assertEquals(MonopolistScenario.findWorkerTargetThatMaximizesProfits(101,1,14,1,1),22);


    }
}