package model.scenario;

import goods.GoodType;
import junit.framework.Assert;
import model.MacroII;
import org.junit.Test;

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

            macroII.start();
            while(macroII.schedule.getTime()<3500)
                macroII.schedule.step(macroII);


            Assert.assertEquals(macroII.getMarket(GoodType.GENERIC).getLastPrice(),79);
            Assert.assertEquals(macroII.getMarket(GoodType.GENERIC).getLastWeekVolume(),154);
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

            macroII.start();
            while(macroII.schedule.getTime()<3500)
                macroII.schedule.step(macroII);


            Assert.assertEquals(macroII.getMarket(GoodType.GENERIC).getLastPrice(),79);
            Assert.assertEquals(macroII.getMarket(GoodType.GENERIC).getLastWeekVolume(),154);







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

            macroII.start();
            while(macroII.schedule.getTime()<3500)
                macroII.schedule.step(macroII);


            Assert.assertEquals(macroII.getMarket(GoodType.GENERIC).getLastPrice(),79);
            Assert.assertEquals(macroII.getMarket(GoodType.GENERIC).getLastWeekVolume(),154);






        }


    }

}