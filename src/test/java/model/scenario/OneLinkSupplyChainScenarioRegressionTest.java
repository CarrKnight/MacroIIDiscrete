package model.scenario;

import agents.firm.production.control.facades.MarginalPlantControlWithPIDUnit;
import agents.firm.sales.SalesDepartmentOneAtATime;
import ec.util.MersenneTwisterFast;
import goods.GoodType;
import model.MacroII;
import model.utilities.filters.ExponentialFilter;
import org.junit.Assert;
import org.junit.Test;

import static model.experiments.tuningRuns.MarginalMaximizerPIDTuning.printProgressBar;

/**
 * <h4>Description</h4>
 * <p/> So the idea behind this class is to store the parameters by which the one-link supply chain give me the right result.
 * This way if I ever change anything i am going to be able to make sure these parameters value still stand: no regression.
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-06-06
 * @see
 */
public class OneLinkSupplyChainScenarioRegressionTest
{






    /**
     * With these parameters the beef seller waits for 100 days before changing its price
     */
    @Test
    public void testWithStickyPrices()
    {
        //this will take a looong time
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        for(int i=0; i <5; i++)
        {
            final MacroII macroII = new MacroII(random.nextLong());
            final OneLinkSupplyChainScenario scenario1 = new OneLinkSupplyChainScenario(macroII);
            scenario1.setControlType(MarginalPlantControlWithPIDUnit.class);
            scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
            //use standard PID parameters
            scenario1.setDivideProportionalGainByThis(1f);
            scenario1.setDivideIntegrativeGainByThis(1f);
            //100 days delay
            scenario1.setBeefPricingSpeed(100);
            //add a very big filterer (since we can wait 100 turns)
            scenario1.setBeefPriceFilterer(new ExponentialFilter<Integer>(.01f));



            macroII.setScenario(scenario1);
            macroII.start();


            while(macroII.schedule.getTime()<15000)
            {
                macroII.schedule.step(macroII);
                printProgressBar(15001,(int)macroII.schedule.getSteps(),100);
            }

            System.out.println("done with price: " +macroII.getMarket(GoodType.BEEF).getLastPrice() );
            System.out.println();
            //the beef price is in the ballpark
            Assert.assertTrue(macroII.getMarket(GoodType.BEEF).getLastPrice() >=27 && macroII.getMarket(GoodType.BEEF).getLastPrice() <=32 );

        }

    }


    /**
     * With these parameters the beef seller adjusts its prices everyday, but only ever so slightly!
     */
    @Test
    public void testWithSlowPID()
    {

        //this will take a looong time
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        for(int i=0; i <5; i++)
        {
            final MacroII macroII = new MacroII(random.nextLong());
            final OneLinkSupplyChainScenario scenario1 = new OneLinkSupplyChainScenario(macroII);
            scenario1.setControlType(MarginalPlantControlWithPIDUnit.class);
            scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
            //divide standard PID parameters by 100
            scenario1.setDivideProportionalGainByThis(100f);
            scenario1.setDivideIntegrativeGainByThis(100f);
            //100 days delay
            scenario1.setBeefPricingSpeed(0);
            //no real need of filter at this slow speed
            scenario1.setBeefPriceFilterer(null);



            macroII.setScenario(scenario1);
            macroII.start();


            while(macroII.schedule.getTime()<15000)
            {
                macroII.schedule.step(macroII);
                printProgressBar(15001,(int)macroII.schedule.getSteps(),100);
            }

            System.out.println("done with price: " +macroII.getMarket(GoodType.BEEF).getLastPrice() );
            System.out.println();
            //the beef price is in the ballpark
            Assert.assertTrue(macroII.getMarket(GoodType.BEEF).getLastPrice() >=27 && macroII.getMarket(GoodType.BEEF).getLastPrice() <=32 );


        }
    }


}
