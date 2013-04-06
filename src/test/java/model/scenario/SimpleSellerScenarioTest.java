package model.scenario;

import agents.firm.sales.pricing.pid.SalesControlWithFixedInventoryAndPID;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import agents.firm.sales.pricing.pid.SmoothedDailyInventoryPricingStrategy;
import goods.GoodType;
import static org.junit.Assert.*;import model.MacroII;
import org.junit.Test;

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
 * @version 2013-02-06
 * @see
 */
public class SimpleSellerScenarioTest {


    @Test
    public void rightPriceAndQuantityTestWithNoInventoryNoShift()
    {
        for(int i=0; i<5; i++)
        {
            //to sell 4 you need to price them between 60 and 51 everytime
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            SimpleSellerScenario scenario = new SimpleSellerScenario(macroII);
            scenario.setSellerStrategy(SimpleFlowSellerPID.class);
            scenario.setDemandShifts(false);


            macroII.setScenario(scenario);
            macroII.start();
            while(macroII.schedule.getTime()<3500)
                macroII.schedule.step(macroII);


            //price should be any between 60 and 51
            assertTrue(macroII.getMarket(GoodType.GENERIC).getLastPrice() <= 60);
            assertTrue(macroII.getMarket(GoodType.GENERIC).getLastPrice() >= 51);
            assertEquals(macroII.getMarket(GoodType.GENERIC).getLastWeekVolume(), 4 * 7); //every day 4 goods should have been traded

        }


    }



    @Test
    public void rightPriceAndQuantityTestWithNoInventoryYesShift()
    {
        for(int i=0; i<5; i++)
        {
            //to sell 4 you need to price them between 60 and 51 everytime
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            SimpleSellerScenario scenario = new SimpleSellerScenario(macroII);
            scenario.setSellerStrategy(SimpleFlowSellerPID.class);
            scenario.setDemandShifts(true);

            macroII.setScenario(scenario);
            macroII.start();
            while(macroII.schedule.getTime()<3500)
                macroII.schedule.step(macroII);


            //price should be any between 60 and 51
            assertTrue(macroII.getMarket(GoodType.GENERIC).getLastPrice() <= 160);
            assertTrue(macroII.getMarket(GoodType.GENERIC).getLastPrice() >= 151);
            assertEquals(macroII.getMarket(GoodType.GENERIC).getLastWeekVolume(), 4 * 7); //every day 4 goods should have been traded

        }


    }

    @Test
    public void rightPriceAndQuantityNoShiftWithSalesControlFlowPIDWithFixedInventory()
    {
        for(int i=0; i<5; i++)
        {
            //to sell 4 you need to price them between 60 and 51 everytime
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            SimpleSellerScenario scenario = new SimpleSellerScenario(macroII);
            scenario.setSellerStrategy(SalesControlWithFixedInventoryAndPID.class);
            scenario.setDemandShifts(false);


            macroII.setScenario(scenario);
            macroII.start();
            while(macroII.schedule.getTime()<3500)
                macroII.schedule.step(macroII);


            //price should be any between 60 and 51
            assertTrue(macroII.getMarket(GoodType.GENERIC).getLastPrice() <= 60);
            assertTrue(macroII.getMarket(GoodType.GENERIC).getLastPrice() >= 51);
            assertEquals(macroII.getMarket(GoodType.GENERIC).getLastWeekVolume(), 4 * 7); //every day 4 goods should have been traded

        }
    }

    @Test
    public void rightPriceAndQuantityShiftWithSalesControlFlowPIDWithFixedInventory(){
        for(int i=0; i<5; i++)
        {
            //to sell 4 you need to price them between 60 and 51 everytime
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            SimpleSellerScenario scenario = new SimpleSellerScenario(macroII);
            scenario.setSellerStrategy(SalesControlWithFixedInventoryAndPID.class);
            scenario.setDemandShifts(true);

            macroII.setScenario(scenario);
            macroII.start();
            while(macroII.schedule.getTime()<3500)
                macroII.schedule.step(macroII);


            //price should be any between 60 and 51
            assertTrue(macroII.getMarket(GoodType.GENERIC).getLastPrice() <= 160);
            assertTrue(macroII.getMarket(GoodType.GENERIC).getLastPrice() >= 151);
            assertEquals(macroII.getMarket(GoodType.GENERIC).getLastWeekVolume(), 4 * 7); //every day 4 goods should have been traded

        }


    }


    @Test
    public void rightPriceAndQuantityTestWithInventoryNoShift()
    {
        for(int i=0; i<5; i++)
        {
            //to sell 4 you need to price them between 60 and 51 everytime, even when you stock up some inventory initially
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            SimpleSellerScenario scenario = new SimpleSellerScenario(macroII);
            scenario.setSellerStrategy(SalesControlWithFixedInventoryAndPID.class);
            scenario.setDemandShifts(false);

            macroII.setScenario(scenario);
            macroII.start();
            while(macroII.schedule.getTime()<3500)
                macroII.schedule.step(macroII);


            //price should be any between 60 and 51
            assertTrue(macroII.getMarket(GoodType.GENERIC).getLastPrice() <= 60);
            assertTrue(macroII.getMarket(GoodType.GENERIC).getLastPrice() >= 51);
            assertEquals(macroII.getMarket(GoodType.GENERIC).getLastWeekVolume(), 4 * 7); //every day 4 goods should have been traded

        }


    }


    @Test
    public void rightPriceAndQuantityTestWithInventoryShift()
    {
        for(int i=0; i<5; i++)
        {
            //to sell 4 you need to price them between 60 and 51 everytime, even when you stock up some inventory initially
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            SimpleSellerScenario scenario = new SimpleSellerScenario(macroII);
            scenario.setSellerStrategy(SalesControlWithFixedInventoryAndPID.class);
            scenario.setDemandShifts(true);

            macroII.setScenario(scenario);
            macroII.start();
            while(macroII.schedule.getTime()<3500)
                macroII.schedule.step(macroII);


            //price should be any between 60 and 51
            assertTrue(macroII.getMarket(GoodType.GENERIC).getLastPrice() <= 160);
            assertTrue(macroII.getMarket(GoodType.GENERIC).getLastPrice() >= 151);
            assertEquals(macroII.getMarket(GoodType.GENERIC).getLastWeekVolume(), 4 * 7); //every day 4 goods should have been traded

        }


    }

    @Test
    public void rightPriceAndQuantityTestWithSmoothedInventoryNoShift()
    {
        for(int i=0; i<5; i++)
        {
            //to sell 4 you need to price them between 60 and 51 everytime, even when you stock up some inventory initially
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            SimpleSellerScenario scenario = new SimpleSellerScenario(macroII);
            scenario.setSellerStrategy(SmoothedDailyInventoryPricingStrategy.class);
            scenario.setDemandShifts(false);

            macroII.setScenario(scenario);
            macroII.start();
            while(macroII.schedule.getTime()<3500)
                macroII.schedule.step(macroII);


            //price should be any between 60 and 51
            assertTrue(macroII.getMarket(GoodType.GENERIC).getLastPrice() <= 60);
            assertTrue(macroII.getMarket(GoodType.GENERIC).getLastPrice() >= 51);
            assertEquals(macroII.getMarket(GoodType.GENERIC).getLastWeekVolume(), 4 * 7); //every day 4 goods should have been traded

        }


    }


    @Test
    public void rightPriceAndQuantityTestWithSmoothedInventoryShift()
    {
        for(int i=0; i<5; i++)
        {
            //to sell 4 you need to price them between 60 and 51 everytime, even when you stock up some inventory initially
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            SimpleSellerScenario scenario = new SimpleSellerScenario(macroII);
            scenario.setSellerStrategy(SmoothedDailyInventoryPricingStrategy.class);
            scenario.setDemandShifts(true);

            macroII.setScenario(scenario);
            macroII.start();
            while(macroII.schedule.getTime()<3500)
                macroII.schedule.step(macroII);


            //price should be any between 60 and 51
            assertTrue(macroII.getMarket(GoodType.GENERIC).getLastPrice() <= 160);
            assertTrue(macroII.getMarket(GoodType.GENERIC).getLastPrice() >= 151);
            assertEquals(macroII.getMarket(GoodType.GENERIC).getLastWeekVolume(), 4 * 7); //every day 4 goods should have been traded

        }


    }




}
