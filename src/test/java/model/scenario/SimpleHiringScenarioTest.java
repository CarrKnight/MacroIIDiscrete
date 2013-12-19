/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.scenario;

import agents.firm.personell.HumanResources;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.scheduler.Priority;
import model.utilities.stats.collectors.enums.PurchasesDataType;
import org.junit.Assert;
import org.junit.Test;
import sim.engine.SimState;
import sim.engine.Steppable;

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
 * @version 2013-12-19
 * @see
 */
public class SimpleHiringScenarioTest {

    @Test
    public void oneFirmTest()
    {
        for(int k=0; k< 5; k++)
        {
            MacroII model = new MacroII(System.currentTimeMillis());
            SimpleHiringScenario scenario = setupTest(model,1);
            model.start();
            scheduleSanityCheck(model, scenario);


            runModel(model, scenario);
        }
    }

    private void runModel(MacroII model, SimpleHiringScenario scenario) {
        for(int i=0; i<3500; i++){
            model.schedule.step(model);


        }

        //another 100, take averages
        float averageWorkers=0;
        float clearingWages = 0;
        for(int i=0; i<100; i++)
        {
            model.schedule.step(model);
            System.out.println("----------------------------------------");
            for(HumanResources hr : scenario.getHrs())
            {
                averageWorkers += hr.getNumberOfWorkers();
                int day = hr.getPurchasesData().getLastObservedDay();
                Assert.assertEquals(hr.getNumberOfWorkers(), hr.getTodayInflow());
                //yesterday hires are today workforce:
                Assert.assertEquals(hr.getPurchasesData().getObservationRecordedThisDay(PurchasesDataType.INFLOW, day - 1),hr.getPlant().getNumberOfWorkersDuringProduction(),.0001d);
                System.out.println(hr.getNumberOfWorkers() + " , " + hr.getLastOfferedPrice() + ", " + hr.getTodayInflow());
            }
            clearingWages += scenario.getMarket().getTodayAveragePrice();
        }
        averageWorkers /= 100;
        clearingWages /=100;
        Assert.assertEquals(16,averageWorkers,.0001d);
        Assert.assertEquals(16,clearingWages,.0001d);
    }

    private SimpleHiringScenario setupTest(MacroII model, int buyers) {
        SimpleHiringScenario scenario = new SimpleHiringScenario(model);
        scenario.setLaborSupplySlope(1);
        scenario.setLaborSupplyIntercept(0);
        scenario.setMaximumWorkers(100);
        scenario.setNumberOfFirms(buyers);
        scenario.setTargetEmploymentPerFirm(16/buyers);

        model.setScenario(scenario);
        return scenario;
    }

    @Test
    public void fourFirmTest()
    {
        for(int k=0; k< 5; k++)
        {
            final MacroII model = new MacroII(System.currentTimeMillis());
            final SimpleHiringScenario scenario = setupTest(model,4);
            model.start();
            scheduleSanityCheck(model, scenario);


            runModel(model, scenario);
        }
    }

    private void scheduleSanityCheck(final MacroII model, final SimpleHiringScenario scenario) {
        //schedule a sanity check, there should be no new hires at the end of prepare to trade
        model.scheduleSoon(ActionOrder.PREPARE_TO_TRADE,new Steppable() {
            @Override
            public void step(SimState state) {
                for(HumanResources hr : scenario.getHrs())
                {
                    Assert.assertEquals(0, hr.getNumberOfWorkers());
                    Assert.assertEquals(0,hr.getPlant().getNumberOfWorkers());
                }
                model.scheduleTomorrow(ActionOrder.PREPARE_TO_TRADE,this, Priority.FINAL);
            }
        }, Priority.FINAL);
    }

}
