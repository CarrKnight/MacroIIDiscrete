/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.scenario;

import goods.GoodType;
import model.MacroII;
import model.utilities.pid.CascadePIDController;
import model.utilities.pid.FlowAndStockController;
import model.utilities.pid.PIDController;
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
        for(int i=0; i<50; i++)
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
                macroII.schedule.step(macroII);


            //price should be any between 60 and 51
            assertTrue(String.valueOf(macroII.getMarket(GoodType.GENERIC).getLastPrice()),macroII.getMarket(GoodType.GENERIC).getLastPrice() < 140);
            assertTrue(String.valueOf(macroII.getMarket(GoodType.GENERIC).getLastPrice()),macroII.getMarket(GoodType.GENERIC).getLastPrice() >= 130);
            assertEquals(macroII.getMarket(GoodType.GENERIC).getLastWeekVolume(), 4 * 7); //every day 4 goods should have been traded
            assertEquals(Integer.toString(scenario.getDepartment().getFirm().hasHowMany(GoodType.GENERIC)),
                    scenario.getDepartment().getFirm().hasHowMany(GoodType.GENERIC),20);

        }


    }

    @Test
    public void rightPriceAndQuantityTestWithCascadeControllerFixed()
    {
        for(int i=0; i<50; i++)
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
                macroII.schedule.step(macroII);


            //price should be any between 60 and 51
            assertTrue(String.valueOf(macroII.getMarket(GoodType.GENERIC).getLastPrice()),macroII.getMarket(GoodType.GENERIC).getLastPrice() < 140);
            assertTrue(String.valueOf(macroII.getMarket(GoodType.GENERIC).getLastPrice()),macroII.getMarket(GoodType.GENERIC).getLastPrice() >= 130);
            assertEquals(macroII.getMarket(GoodType.GENERIC).getYesterdayVolume(), 4); //every day 4 goods should have been traded
            assertEquals(scenario.getDepartment().getFirm().hasHowMany(GoodType.GENERIC),20);

        }


    }

    @Test
    public void rightPriceAndQuantityTestWithFlowControllerFixed()
    {
        for(int i=0; i<5; i++)
        {
            //to sell 4 you need to price them between 60 and 51 everytime
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            SimpleBuyerScenario scenario = new SimpleBuyerScenario(macroII);
            scenario.setControllerType(FlowAndStockController.class);
            scenario.setTargetInventory(20);
            scenario.setConsumptionRate(4);



            macroII.setScenario(scenario);
            macroII.start();
            while(macroII.schedule.getTime()<3500)
                macroII.schedule.step(macroII);


            //price should be any between 60 and 51
            assertTrue(macroII.getMarket(GoodType.GENERIC).getLastPrice() < 140);
            assertTrue(macroII.getMarket(GoodType.GENERIC).getLastPrice() >= 130);
            assertEquals(macroII.getMarket(GoodType.GENERIC).getLastWeekVolume(), 4 * 7); //every day 4 goods should have been traded

            //notice that here the controller isn't guaranteed to havet the right inventory (since it's flow first)
        }


    }

}
