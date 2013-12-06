/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.prediction;

import agents.firm.sales.SalesDepartmentAllAtOnce;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.prediction.RecursiveSalePredictor;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import agents.firm.sales.pricing.pid.SmoothedDailyInventoryPricingStrategy;
import goods.GoodType;
import model.MacroII;
import model.scenario.MonopolistScenario;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
 * @version 2013-09-30
 * @see
 */
public class AbstractWorkerLearningPredictorTest {


    @Test
    public void predictorLearnsSlope()
    {


        //run the test 15 times
        for(int i=0; i<30; i++)
        {
          //  final MacroII macroII = new MacroII(1384099470750l);
            final MacroII macroII = new MacroII(System.currentTimeMillis());
            System.err.println(macroII.seed());


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
            macroII.schedule.step(macroII);
            RecursiveSalePredictor predictor = new RecursiveSalePredictor(macroII,scenario1.getMonopolist().getSalesDepartment(GoodType.GENERIC));
           // predictor.setRegressingOnWorkers(true);
            scenario1.getMonopolist().getSalesDepartment(GoodType.GENERIC).setPredictorStrategy(predictor);
            while(macroII.schedule.getTime()<5000)
                macroII.schedule.step(macroII);


            System.out.println(predictor.getDecrementDelta() + " - " + p1+ " - " +macroII.seed());
      /*      System.out.println(macroII.getMarket(GoodType.GENERIC).getLastPrice() + " ---- " + predictor.predictPrice(0) + " ---- " + predictor.predictPrice(1) + "\n" +
                    predictor.predictPrice(1,10) +"---"+ predictor.predictPrice(1,100) +"---"+ predictor.predictPrice(1,1000)+"---"+ predictor.predictPrice(1,10000));
                    */
            assertEquals(predictor.getDecrementDelta(),(double)(p1 ) ,.5d);


        }





    }


}
