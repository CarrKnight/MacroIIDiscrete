package model.scenario;

import agents.firm.Firm;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentAllAtOnce;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.pricing.pid.SalesControlFlowPIDWithFixedInventoryButTargetingFlowsOnly;
import agents.firm.sales.pricing.pid.SalesControlWithFixedInventoryAndPID;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import agents.firm.sales.pricing.pid.salesControlWithSmoothedinventoryAndPID;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.stats.collectors.enums.MarketDataType;
import org.junit.Test;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.Iterator;
import java.util.List;

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
            scenario.setDemandSlope(-10);


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
            scenario.setDemandSlope(-10);


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
            scenario.setDemandSlope(-10);



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
            scenario.setDemandSlope(-10);


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
            scenario.setDemandSlope(-10);


            macroII.setScenario(scenario);
            macroII.start();
            while(macroII.schedule.getTime()<3500) {
                  macroII.schedule.step(macroII);
                System.out.println(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice());
            }

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
            scenario.setDemandSlope(-10);


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
            scenario.setSellerStrategy(salesControlWithSmoothedinventoryAndPID.class);
            scenario.setDemandShifts(false);
            scenario.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);
            scenario.setDemandSlope(-10);


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
            scenario.setSellerStrategy(salesControlWithSmoothedinventoryAndPID.class);
            scenario.setDemandShifts(true);
            scenario.setSalesDepartmentType(SalesDepartmentAllAtOnce.class);
            scenario.setDemandSlope(-10);


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
            scenario.setDemandSlope(-10);


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
            scenario.setSellerStrategy(salesControlWithSmoothedinventoryAndPID.class);
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
            scenario.setSellerStrategy(salesControlWithSmoothedinventoryAndPID.class);
            scenario.setDemandShifts(true);
            scenario.setSalesDepartmentType(SalesDepartmentOneAtATime.class);


            macroII.setScenario(scenario);
            macroII.start();
            while(macroII.schedule.getTime()<3500)
            {
                macroII.schedule.step(macroII);
            }


            //price should be any between 60 and 51
            System.out.println(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice());
            assertTrue(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() <= 160);
            assertTrue(macroII.getMarket(GoodType.GENERIC).getTodayAveragePrice() >= 151);
            assertEquals(macroII.getMarket(GoodType.GENERIC).getLastWeekVolume(), 4 * 7); //every day 4 goods should have been traded

        }




    }

    /////////////////////////////////////////////////////////////////////////////////////////

    private SimpleSellerScenario setup102minusq(MacroII macroII, int numberOfSellers) {
        SimpleSellerScenario scenario = new SimpleSellerScenario(macroII);
        scenario.setDemandShifts(false);
        scenario.setDemandIntercept(102);
        scenario.setDemandSlope(-1);
        scenario.setInflowPerSeller(16/numberOfSellers);
        scenario.setNumberOfSellers(numberOfSellers);
        scenario.setSalesDepartmentType(SalesDepartmentOneAtATime.class);

        return scenario;
    }


    private void run102minusqscenario(MacroII macroII, SimpleSellerScenario scenario) {
        macroII.setScenario(scenario);
        macroII.start();
        while(macroII.schedule.getTime()<3500)
        {
            macroII.schedule.step(macroII);
        }

        float averagePrice = 0;
        float averageQ = 0;
        List<SalesDepartment> departments = scenario.getDepartments();
        for(int i=0; i<500; i++)
        {
            macroII.schedule.step(macroII);
            averageQ += macroII.getMarket(GoodType.GENERIC).getLatestObservation(MarketDataType.VOLUME_TRADED);
            averagePrice +=macroII.getMarket(GoodType.GENERIC).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE);
            System.out.println("------------------------------------------------------------");
            for(int k=0; k< scenario.getNumberOfSellers(); k++)
            {
                SalesDepartment department = departments.get(k);
                System.out.println("department " + k + ", price: " + department.getLastAskedPrice() +
                        ", today sold: " + department.getTodayOutflow() + ", averaged price: " + department.getAveragedLastPrice() );


            }

        }

        averageQ /= 500;
        averagePrice /= 500;
        System.out.println( averageQ + "  ----- " + averagePrice);
        //price should be 86
        assertEquals(86,averagePrice,.3d);
        assertEquals(averageQ, 16,.3d); //every day 4 goods should have been traded

    }



    @Test
    public void rightPriceAndQuantityTestWithNoInventoryOneAtATime101MinusQ()
    {
        for(int i=0; i<5; i++)
        {
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            SimpleSellerScenario scenario = setup102minusq(macroII,1);
            scenario.setSellerStrategy(SimpleFlowSellerPID.class);




            run102minusqscenario(macroII, scenario);

        }


    }

    @Test
    public void SalesControlFlowPIDWithFixedInventoryOneAtATime101MinusQ()
    {
        for(int i=0; i<5; i++)
        {
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            SimpleSellerScenario scenario = setup102minusq(macroII,1);
            scenario.setSellerStrategy(SalesControlWithFixedInventoryAndPID.class);




            run102minusqscenario(macroII, scenario);

            additionalTest(scenario);

        }


    }

    private void additionalTest(SimpleSellerScenario scenario) {
        List<SalesDepartment> departments = scenario.getDepartments();
        for(int k=0; k< scenario.getNumberOfSellers(); k++)
        {
            SalesDepartment department = departments.get(k);
            assertEquals(86,department.getAveragedLastPrice(),1d);
        }
    }


    @Test
    public void SmoothedDailyInventoryPricingStrategyOneAtATime101MinusQ()
    {
        for(int i=0; i<5; i++)
        {
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            SimpleSellerScenario scenario = setup102minusq(macroII,1);
            scenario.setSellerStrategy(salesControlWithSmoothedinventoryAndPID.class);




            run102minusqscenario(macroII, scenario);
            additionalTest(scenario);

        }


    }

    @Test
    public void FlowsOnly101MinusQ()
    {
        for(int i=0; i<5; i++)
        {
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            SimpleSellerScenario scenario = setup102minusq(macroII,1);
            scenario.setSellerStrategy(SalesControlFlowPIDWithFixedInventoryButTargetingFlowsOnly.class);




            run102minusqscenario(macroII, scenario);
            additionalTest(scenario);

        }


    }




    ////////////////////////////////////////////////////////////////////////////////////////
    //4 sellers!


    @Test
    public void rightPriceAndQuantityTestWithNoInventory4Sellers()
    {
        for(int i=0; i<5; i++)
        {
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            SimpleSellerScenario scenario = setup102minusq(macroII,4);
            scenario.setSellerStrategy(SimpleFlowSellerPID.class);




            run102minusqscenario(macroII, scenario);

        }


    }

    @Test
    public void SalesControlFlowPIDWithFixedInventory4Sellers()
    {
        for(int i=0; i<5; i++)
        {
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            SimpleSellerScenario scenario = setup102minusq(macroII,4);
            scenario.setSellerStrategy(SalesControlWithFixedInventoryAndPID.class);




            run102minusqscenario(macroII, scenario);

        }


    }


    @Test
    public void SmoothedDailyInventoryPricingStrategy4Sellers()
    {
        for(int i=0; i<5; i++)
        {
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            SimpleSellerScenario scenario = setup102minusq(macroII,4);
            scenario.setSellerStrategy(salesControlWithSmoothedinventoryAndPID.class);




            run102minusqscenario(macroII, scenario);

        }


    }

    @Test
    public void FlowsOnly4Sellers()
    {
        for(int i=0; i<5; i++)
        {
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            SimpleSellerScenario scenario = setup102minusq(macroII,4);
            scenario.setSellerStrategy(SalesControlFlowPIDWithFixedInventoryButTargetingFlowsOnly.class);




            run102minusqscenario(macroII, scenario);

        }


    }
    

    ///////////////////////////////////////////////////////////////////////////////////////////
    //2 unequal sellers



    private SimpleSellerScenario setup102minusqUnequal(MacroII macroII) {
        final SimpleSellerScenario scenario = new SimpleSellerScenario(macroII);
        scenario.setDemandShifts(false);
        scenario.setDemandIntercept(102);
        scenario.setDemandSlope(-1);
        scenario.setInflowPerSeller(8);
        scenario.setNumberOfSellers(2);
        scenario.setSalesDepartmentType(SalesDepartmentOneAtATime.class);

        //change inflow at dawn!
        macroII.scheduleSoon(ActionOrder.DAWN, new Steppable() {
            @Override
            public void step(SimState simState) {
                Iterator<Firm> iterator = scenario.getSellerToInflowMap().keySet().iterator();
                Firm firm1 = iterator.next();
                Firm firm2 = iterator.next();
                assert !iterator.hasNext();
                scenario.getSellerToInflowMap().put(firm1,12);
                scenario.getSellerToInflowMap().put(firm2,4);
            }
        });

        return scenario;
    }


    @Test
    public void rightPriceAndQuantityTestWithNoInventory2UnequalSellers()
    {
        for(int i=0; i<5; i++)
        {
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            SimpleSellerScenario scenario = setup102minusqUnequal(macroII);
            scenario.setSellerStrategy(SimpleFlowSellerPID.class);




            run102minusqscenario(macroII, scenario);

        }


    }

    @Test
    public void SalesControlFlowPIDWithFixedInventory2UnequalSellers()
    {
        for(int i=0; i<1; i++)
        {
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            SimpleSellerScenario scenario = setup102minusqUnequal(macroII);
            scenario.setSellerStrategy(SalesControlWithFixedInventoryAndPID.class);




            run102minusqscenario(macroII, scenario);

        }


    }


    @Test
    public void SmoothedDailyInventoryPricingStrategy2UnequalSellers()
    {
        for(int i=0; i<5; i++)
        {
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            SimpleSellerScenario scenario = setup102minusqUnequal(macroII);
            scenario.setSellerStrategy(salesControlWithSmoothedinventoryAndPID.class);




            run102minusqscenario(macroII, scenario);

        }


    }

    @Test
    public void FlowsOnly2UnequalSellers()
    {
        for(int i=0; i<5; i++)
        {
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            SimpleSellerScenario scenario = setup102minusqUnequal(macroII);
            scenario.setSellerStrategy(SalesControlFlowPIDWithFixedInventoryButTargetingFlowsOnly.class);




            run102minusqscenario(macroII, scenario);

        }


    }
    
    
    

}
