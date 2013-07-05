package model.scenario;

import agents.firm.sales.SalesDepartmentAllAtOnce;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.pricing.pid.SalesControlFlowPIDWithFixedInventory;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import goods.GoodType;
import model.MacroII;
import org.junit.Assert;
import org.junit.Test;

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

    //this one fails, I need to find out why!
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

            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_WITH_UNIT_PID);

            //choose a sales control at random, but don't mix hill-climbing with inventory building since they aren't really compatible
            scenario1.setAskPricingStrategy(SimpleFlowSellerPID.class);

            scenario1.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);




            macroII.start();
            while(macroII.schedule.getTime()<5500)
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
    public void rightPriceAndQuantityTestRandomControlRandomSlopes()
    {

        //run the test 15 times
        for(int i=0; i<15; i++)
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



            macroII.start();
            while(macroII.schedule.getTime()<5500)
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
    public void rightPriceAndQuantityTestAsMarginal()
    {
        for(int i=0; i<5; i++)
        {
            //we know the profit maximizing equilibrium is q=220, price = 72
            final MacroII macroII = new MacroII(System.currentTimeMillis());
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


            macroII.start();
            while(macroII.schedule.getTime()<3500)
                macroII.schedule.step(macroII);


            assertEquals(macroII.getMarket(GoodType.GENERIC).getLastPrice(), 79,1);
            assertEquals(scenario1.monopolist.getTotalWorkers(), 22,1);







        }


    }

    @Test
    public void rightPriceAndQuantityTestAsMarginalWithPID()
    {
        for(int i=0; i<5; i++)
        {
            //we know the profit maximizing equilibrium is q=220, price = 72
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            MonopolistScenario scenario1 = new MonopolistScenario(macroII);
            //    scenario1.setAlwaysMoving(true);
            //   MonopolistScenario scenario1 = new MonopolistScenario(macroII);
            macroII.setScenario(scenario1);
            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_WITH_PID);
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

    @Test
    public void rightPriceAndQuantityTestAsMarginalWithPIDUnit()
    {
        for(int i=0; i<5; i++)
        {
            //we know the profit maximizing equilibrium is q=220, price = 72
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            MonopolistScenario scenario1 = new MonopolistScenario(macroII);
            //    scenario1.setAlwaysMoving(true);
            //   MonopolistScenario scenario1 = new MonopolistScenario(macroII);
            macroII.setScenario(scenario1);
            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_WITH_UNIT_PID);
            scenario1.setAskPricingStrategy(SimpleFlowSellerPID.class);
            if(macroII.random.nextBoolean())
                scenario1.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);
            else
                scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);

            macroII.start();
            while(macroII.schedule.getTime()<5500)
                macroII.schedule.step(macroII);


            assertEquals(macroII.getMarket(GoodType.GENERIC).getLastPrice(), 79,1);
            assertEquals(scenario1.monopolist.getTotalWorkers(), 22,1);






        }


    }



    //all these use SalesControlFlowPIDWithFixedInventory




    @Test
    public void rightPriceAndQuantityTestAsMarginalWithSalesControlFlowPIDWithFixedInventory()
    {
        for(int i=0; i<5; i++)
        {
            //we know the profit maximizing equilibrium is q=220, price = 72
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            MonopolistScenario scenario1 = new MonopolistScenario(macroII);
            //    scenario1.setAlwaysMoving(true);
            //   MonopolistScenario scenario1 = new MonopolistScenario(macroII);
            macroII.setScenario(scenario1);
            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_PLANT_CONTROL);
            scenario1.setAskPricingStrategy(SalesControlFlowPIDWithFixedInventory.class);
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

    @Test
    public void rightPriceAndQuantityTestAsMarginalWithPIDWithSalesControlFlowPIDWithFixedInventory()
    {
        for(int i=0; i<5; i++)
        {
            //we know the profit maximizing equilibrium is q=220, price = 72
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            MonopolistScenario scenario1 = new MonopolistScenario(macroII);
            //    scenario1.setAlwaysMoving(true);
            //   MonopolistScenario scenario1 = new MonopolistScenario(macroII);
            macroII.setScenario(scenario1);
            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_WITH_PID);
            scenario1.setAskPricingStrategy(SalesControlFlowPIDWithFixedInventory.class);
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


    @Test
    public void rightPriceAndQuantityTestAsMarginalWithPIDUnitWithSalesControlFlowPIDWithFixedInventory()
    {
        for(int i=0; i<5; i++)
        {
            //we know the profit maximizing equilibrium is q=220, price = 72
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            MonopolistScenario scenario1 = new MonopolistScenario(macroII);
            //    scenario1.setAlwaysMoving(true);
            //   MonopolistScenario scenario1 = new MonopolistScenario(macroII);
            macroII.setScenario(scenario1);
            scenario1.setControlType(MonopolistScenario.MonopolistScenarioIntegratedControlEnum.MARGINAL_WITH_UNIT_PID);
            scenario1.setAskPricingStrategy(SalesControlFlowPIDWithFixedInventory.class);
            if(macroII.random.nextBoolean())
                scenario1.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);
            else
                scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);

            macroII.start();
            while(macroII.schedule.getTime()<5500)
                macroII.schedule.step(macroII);


            assertEquals(macroII.getMarket(GoodType.GENERIC).getLastPrice(), 79,1);
            assertEquals(scenario1.monopolist.getTotalWorkers(), 22,1);






        }
    }


    @Test
    public void testfindWorkerTargetThatMaximizesProfits()
    {
        Assert.assertEquals(MonopolistScenario.findWorkerTargetThatMaximizesProfits(101,1,14,1,1),22);


    }
}