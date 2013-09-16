package model.scenario;

import agents.firm.sales.SalesDepartmentAllAtOnce;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.pricing.pid.SalesControlWithFixedInventoryAndPID;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import agents.firm.sales.pricing.pid.SmoothedDailyInventoryPricingStrategy;
import goods.GoodType;
import model.MacroII;
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
            scenario.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);


            macroII.setScenario(scenario);
            macroII.start();
            while(macroII.schedule.getTime()<3500)
                macroII.schedule.step(macroII);


            //price should be any between 60 and 51
            assertTrue(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() <= 60);
            assertTrue(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() >= 51);
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
            scenario.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);


            macroII.setScenario(scenario);
            macroII.start();
            while(macroII.schedule.getTime()<3500)
                macroII.schedule.step(macroII);


            //price should be any between 60 and 51
            assertTrue(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() <= 160);
            assertTrue(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() >= 151);
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
            scenario.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);



            macroII.setScenario(scenario);
            macroII.start();
            while(macroII.schedule.getTime()<3500)
                macroII.schedule.step(macroII);


            //price should be any between 60 and 51
            assertTrue(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() <= 60);
            assertTrue(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() >= 51);
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
            scenario.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);


            macroII.setScenario(scenario);
            macroII.start();
            while(macroII.schedule.getTime()<3500)
                macroII.schedule.step(macroII);


            //price should be any between 60 and 51
            assertTrue(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() <= 160);
            assertTrue(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() >= 151);
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
            scenario.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);


            macroII.setScenario(scenario);
            macroII.start();
            while(macroII.schedule.getTime()<3500)
                macroII.schedule.step(macroII);


            //price should be any between 60 and 51
            assertTrue(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() <= 60);
            assertTrue(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() >= 51);
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
            scenario.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);


            macroII.setScenario(scenario);
            macroII.start();
            while(macroII.schedule.getTime()<3500)
                macroII.schedule.step(macroII);


            //price should be any between 60 and 51
            assertTrue(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() <= 160);
            assertTrue(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() >= 151);
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
            scenario.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);


            macroII.setScenario(scenario);
            macroII.start();
            while(macroII.schedule.getTime()<3500)
                macroII.schedule.step(macroII);


            //price should be any between 60 and 51
            System.out.println("price:" + macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() );
            assertTrue(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() <= 60);
            assertTrue(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() >= 51);
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
            scenario.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);


            macroII.setScenario(scenario);
            macroII.start();
            while(macroII.schedule.getTime()<3500)
                macroII.schedule.step(macroII);


            //price should be any between 60 and 51
            assertTrue(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() <= 160);
            assertTrue(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() >= 151);
            assertEquals(macroII.getMarket(GoodType.GENERIC).getLastWeekVolume(), 4 * 7); //every day 4 goods should have been traded

        }


    }



    
    
    
    
    
    
    
    
    //////////////////////////////////////////////////////////////
    //This side uses sales department one at a time
    @Test
    public void rightPriceAndQuantityTestWithNoInventoryNoShiftOneAtATime()
    {
        for(int i=0; i<5; i++)
        {
            //to sell 4 you need to price them between 60 and 51 everytime
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            SimpleSellerScenario scenario = new SimpleSellerScenario(macroII);
            scenario.setSellerStrategy(SimpleFlowSellerPID.class);
            scenario.setDemandShifts(false);
            scenario.setSalesDepartmentType(SalesDepartmentOneAtATime.class);


            macroII.setScenario(scenario);
            macroII.start();
            while(macroII.schedule.getTime()<3500)
                macroII.schedule.step(macroII);


            //price should be any between 60 and 51
            assertTrue(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() <= 60);
            assertTrue(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() >= 51);
            assertEquals(macroII.getMarket(GoodType.GENERIC).getLastWeekVolume(), 4 * 7); //every day 4 goods should have been traded

        }


    }



    @Test
    public void rightPriceAndQuantityTestWithNoInventoryYesShiftOneAtATime()
    {
        for(int i=0; i<5; i++)
        {
            //to sell 4 you need to price them between 60 and 51 everytime
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            SimpleSellerScenario scenario = new SimpleSellerScenario(macroII);
            scenario.setSellerStrategy(SimpleFlowSellerPID.class);
            scenario.setDemandShifts(true);
            scenario.setSalesDepartmentType(SalesDepartmentOneAtATime.class);


            macroII.setScenario(scenario);
            macroII.start();
            while(macroII.schedule.getTime()<3500)
                macroII.schedule.step(macroII);


            //price should be any between 60 and 51
            assertTrue(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() <= 160);
            assertTrue(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() >= 151);
            assertEquals(macroII.getMarket(GoodType.GENERIC).getLastWeekVolume(), 4 * 7); //every day 4 goods should have been traded

        }


    }

    @Test
    public void rightPriceAndQuantityNoShiftWithSalesControlFlowPIDWithFixedInventoryOneAtATime()
    {
        for(int i=0; i<5; i++)
        {
            //to sell 4 you need to price them between 60 and 51 everytime
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            SimpleSellerScenario scenario = new SimpleSellerScenario(macroII);
            scenario.setSellerStrategy(SalesControlWithFixedInventoryAndPID.class);
            scenario.setDemandShifts(false);
            scenario.setSalesDepartmentType(SalesDepartmentOneAtATime.class);



            macroII.setScenario(scenario);
            macroII.start();
            while(macroII.schedule.getTime()<3500)
                macroII.schedule.step(macroII);


            //price should be any between 60 and 51
            System.out.println(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice());
            assertTrue(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() <= 60);
            assertTrue(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() >= 51);
            assertEquals(macroII.getMarket(GoodType.GENERIC).getLastWeekVolume(), 4 * 7); //every day 4 goods should have been traded

        }
    }

    @Test
    public void rightPriceAndQuantityShiftWithSalesControlFlowPIDWithFixedInventoryOneAtATime(){
        for(int i=0; i<1; i++)
        {
            System.out.println(i);
            //to sell 4 you need to price them between 60 and 51 everytime
            final MacroII macroII = new MacroII(0);
            long seed = System.currentTimeMillis();
            macroII.setSeed(1377166300844l);
            SimpleSellerScenario scenario = new SimpleSellerScenario(macroII);
            scenario.setSellerStrategy(SalesControlWithFixedInventoryAndPID.class);
            scenario.setDemandShifts(true);
            scenario.setSalesDepartmentType(SalesDepartmentOneAtATime.class);


            macroII.setScenario(scenario);
            macroII.start();
            while(macroII.schedule.getTime()<3500)
            {
                macroII.schedule.step(macroII);
              //  System.out.println(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice());
            }

            //price should be any between 60 and 51
            assertTrue(String.valueOf(seed),macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() <= 160);
            assertTrue(String.valueOf(seed),macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() >= 151);
            assertEquals(macroII.getMarket(GoodType.GENERIC).getYesterdayVolume(), 4); //every day 4 goods should have been traded

        }

        //1377162214716


    }


    @Test
    public void rightPriceAndQuantityTestWithInventoryNoShiftOneAtATime()
    {
        for(int i=0; i<5; i++)
        {
            //to sell 4 you need to price them between 60 and 51 everytime, even when you stock up some inventory initially
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            SimpleSellerScenario scenario = new SimpleSellerScenario(macroII);
            scenario.setSellerStrategy(SalesControlWithFixedInventoryAndPID.class);
            scenario.setDemandShifts(false);
            scenario.setSalesDepartmentType(SalesDepartmentOneAtATime.class);


            macroII.setScenario(scenario);
            macroII.start();
            while(macroII.schedule.getTime()<3500)
                macroII.schedule.step(macroII);


            //price should be any between 60 and 51
            assertTrue(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() <= 60);
            assertTrue(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() >= 51);
            assertEquals(macroII.getMarket(GoodType.GENERIC).getLastWeekVolume(), 4 * 7); //every day 4 goods should have been traded

        }


    }


    @Test
    public void rightPriceAndQuantityTestWithInventoryShiftOneAtATime()
    {
        for(int i=0; i<5; i++)
        {
            //to sell 4 you need to price them between 60 and 51 everytime, even when you stock up some inventory initially
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            SimpleSellerScenario scenario = new SimpleSellerScenario(macroII);
            scenario.setSellerStrategy(SalesControlWithFixedInventoryAndPID.class);
            scenario.setDemandShifts(true);
            scenario.setSalesDepartmentType(SalesDepartmentOneAtATime.class);


            macroII.setScenario(scenario);
            macroII.start();
            while(macroII.schedule.getTime()<3500)
                macroII.schedule.step(macroII);


            //price should be any between 60 and 51
            assertTrue(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() <= 160);
            assertTrue(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() >= 151);
            assertEquals(macroII.getMarket(GoodType.GENERIC).getLastWeekVolume(), 4 * 7); //every day 4 goods should have been traded

        }


    }

    @Test
    public void rightPriceAndQuantityTestWithSmoothedInventoryNoShiftOneAtATime()
    {
        for(int i=0; i<5; i++)
        {
            //to sell 4 you need to price them between 60 and 51 everytime, even when you stock up some inventory initially
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            SimpleSellerScenario scenario = new SimpleSellerScenario(macroII);
            scenario.setSellerStrategy(SmoothedDailyInventoryPricingStrategy.class);
            scenario.setDemandShifts(false);
            scenario.setSalesDepartmentType(SalesDepartmentOneAtATime.class);


            macroII.setScenario(scenario);
            macroII.start();
            while(macroII.schedule.getTime()<3500)
                macroII.schedule.step(macroII);


            //price should be any between 60 and 51
            System.out.println("price:" + macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() );
            assertTrue(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() <= 60);
            assertTrue(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() >= 51);
            assertEquals(macroII.getMarket(GoodType.GENERIC).getLastWeekVolume(), 4 * 7); //every day 4 goods should have been traded

        }


    }


    @Test
    public void rightPriceAndQuantityTestWithSmoothedInventoryShiftOneAtATime()
    {
        for(int i=0; i<50; i++)
        {
            //to sell 4 you need to price them between 60 and 51 everytime, even when you stock up some inventory initially
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            SimpleSellerScenario scenario = new SimpleSellerScenario(macroII);
            scenario.setSellerStrategy(SmoothedDailyInventoryPricingStrategy.class);
            scenario.setDemandShifts(true);
            scenario.setSalesDepartmentType(SalesDepartmentOneAtATime.class);


            macroII.setScenario(scenario);
            macroII.start();
            while(macroII.schedule.getTime()<3500)
                macroII.schedule.step(macroII);


            //price should be any between 60 and 51
            assertTrue(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() <= 160);
            assertTrue(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() >= 151);
            assertEquals(macroII.getMarket(GoodType.GENERIC).getLastWeekVolume(), 4 * 7); //every day 4 goods should have been traded

        }


    }
    
    
    
    
    
    
    
    
    

}
