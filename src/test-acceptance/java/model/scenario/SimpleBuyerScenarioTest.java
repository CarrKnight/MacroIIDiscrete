/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.scenario;

import agents.firm.purchases.PurchasesDepartment;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.utilities.pid.CascadePIDController;
import model.utilities.pid.PIDController;
import model.utilities.scheduler.Priority;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * <h4>Description</h4>
 * <p/>
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-04-19
 * @see
 */
public class SimpleBuyerScenarioTest
{


    @Test
    public void rightPriceAndQuantityTestWithPIDControllerFixed()
    {
        for(int i=0; i<200; i++)
        {
            //to sell 4 you need to price them between 60 and 51 everytime
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            SimpleBuyerScenario scenario = new SimpleBuyerScenario(macroII);
            scenario.setTargetInventory(20);
            scenario.setConsumptionRate(4);
            scenario.setControllerType(PIDController.class);




            macroII.setScenario(scenario);
            macroII.start();
            while(macroII.schedule.getTime()<3500)
            {
                macroII.schedule.step(macroII);
            }

            System.out.println(scenario.getDepartment().getLastOfferedPrice());

            //price should be any between 60 and 51
            assertTrue(String.valueOf(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getLastPrice()),macroII.getMarket(UndifferentiatedGoodType.GENERIC).getLastPrice() < 140);
            assertTrue(String.valueOf(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getLastPrice()),macroII.getMarket(UndifferentiatedGoodType.GENERIC).getLastPrice() >= 130);
            assertEquals(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getYesterdayVolume(), 4); //every day 4 goods should have been traded
            assertEquals(Integer.toString(scenario.getDepartment().getFirm().hasHowMany(UndifferentiatedGoodType.GENERIC)),
                    scenario.getDepartment().getFirm().hasHowMany(UndifferentiatedGoodType.GENERIC),20,1);

            macroII.finish();
        }


    }

    @Test
    public void rightPriceAndQuantityTestWithCascadeControllerFixed()
    {
        for(int i=0; i<20; i++)
        {
            //to sell 4 you need to price them between 60 and 51 everytime
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            SimpleBuyerScenario scenario = new SimpleBuyerScenario(macroII);
            scenario.setControllerType(CascadePIDController.class);
            scenario.setTargetInventory(20);
            scenario.setConsumptionRate(4);



            macroII.setScenario(scenario);
            macroII.start();
            while(macroII.schedule.getTime()<3500)
            {
                macroII.schedule.step(macroII);

            }
            System.out.println(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getLastPrice());

            //price should be any between 60 and 51
            assertTrue(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getLastPrice() < 140);
            assertTrue(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getLastPrice() >= 130);
            assertEquals(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getYesterdayVolume(), 4); //every day 4 goods should have been traded
            assertEquals(scenario.getDepartment().getFirm().hasHowMany(UndifferentiatedGoodType.GENERIC),20);

        }


    }




    @Test
    public void rightPriceAndQuantityTestWithCascadeControllerFixed4Buyers()
    {
        for(int i=0; i<20; i++)
        {
            //to sell 4 you need to price them between 60 and 51 everytime
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            SimpleBuyerScenario scenario = new SimpleBuyerScenario(macroII);
            scenario.setControllerType(CascadePIDController.class);
            scenario.setTargetInventory(20);
            scenario.setConsumptionRate(4);
            scenario.setNumberOfBuyers(4);
            scenario.setNumberOfSuppliers(50);
            scenario.setSupplyIntercept(0);
            scenario.setSupplySlope(1);


            //4 buyers, buying 4 each, should be 16 units in total

            macroII.setScenario(scenario);
            macroII.start();
            for(PurchasesDepartment department : scenario.getDepartments())
                department.setTradePriority(Priority.BEFORE_STANDARD);

            while(macroII.schedule.getTime()<3500)
            {
                System.out.println("--------------------------------");
                macroII.schedule.step(macroII);

                for(PurchasesDepartment department : scenario.getDepartments())
                    System.out.println("inflow: " + department.getTodayInflow() + ", price: " + department.getLastOfferedPrice() + ", averaged: " + department.getAveragedClosingPrice() );

            }

            SummaryStatistics averagePrice = new SummaryStatistics();
            for(int j=0; j<1000; j++) {
                macroII.schedule.step(macroII);
                averagePrice.addValue(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getLastPrice());
            }
            //price should be any between 60 and 51
            assertEquals(16,averagePrice.getMean(),.5d);
            assertEquals(macroII.getMarket(UndifferentiatedGoodType.GENERIC).getYesterdayVolume(), 16,1d); //every day 4 goods should have been traded


        }


    }
}
