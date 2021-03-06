/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.scenario;

import agents.firm.personell.HumanResources;
import agents.firm.production.control.FactoryProducedTargetAndMaximizePlantControl;
import agents.firm.production.control.TargetAndMaximizePlantControl;
import agents.firm.production.control.maximizer.PeriodicMaximizer;
import agents.firm.production.control.maximizer.algorithms.otherMaximizers.FixedTargetMaximizationAlgorithm;
import agents.firm.production.control.targeter.PIDTargeterWithQuickFiring;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.scheduler.Priority;
import model.utilities.stats.collectors.enums.PurchasesDataType;
import org.junit.Assert;
import org.junit.Test;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.Iterator;

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


            runModel(model, scenario,0,1);
        }
    }

    private void runModel(MacroII model, SimpleHiringScenario scenario, int intercept, int slope) {
        for(int i=0; i<5000; i++){
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
                int day = hr.getData().getLastObservedDay();
                Assert.assertEquals(hr.getNumberOfWorkers(), hr.getTodayInflow());
                //yesterday hires are today workforce:
                Assert.assertEquals(hr.getData().getObservationRecordedThisDay(PurchasesDataType.INFLOW, day - 1),hr.getPlant().getNumberOfWorkersDuringProduction(),.0001d);
                System.out.println("average wage paid: " + hr.getAveragedClosingPrice() + " , last offered wages: " + hr.getLastOfferedPrice() + ", today we hired: " + hr.getTodayInflow() + ", target: " + hr.getWorkerTarget());
       //         Assert.assertEquals(intercept + slope *16,hr.getAveragedClosingPrice(),1); //everybody's averages need to be correct + o -
            }
            clearingWages += scenario.getMarket().getTodayAveragePrice();
        }
        averageWorkers /= 100;
        clearingWages /=100;
        System.out.println("average workers: " + averageWorkers + ", average wages: " + clearingWages);
        Assert.assertEquals(16,averageWorkers,.1d);
        Assert.assertEquals(intercept + slope *16,clearingWages,.1d);
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


            runModel(model, scenario,0,1);
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

    @Test
    public void threeUnequalFirmsTest()
    {
        for(int k=0; k< 5; k++)
        {
            final MacroII model = new MacroII(System.currentTimeMillis());
            final SimpleHiringScenario scenario = setupTest(model,3);
            scenario.setLaborSupplyIntercept(14);
            scenario.setLaborSupplySlope(1);

            model.start();



            //first firm will target 11 workers
            Iterator<HumanResources> iterator = scenario.getHrs().iterator();
            HumanResources hr1 = iterator.next();
 //           hr1.setPriceAverager(new ExponentialPriceAverager(.1f, PriceAverager.NoTradingDayPolicy.COUNT_AS_LAST_CLOSING_PRICE));
            FactoryProducedTargetAndMaximizePlantControl produced;
            produced = TargetAndMaximizePlantControl.PlantControlFactory(hr1, PIDTargeterWithQuickFiring.class, PeriodicMaximizer.class, FixedTargetMaximizationAlgorithm.class);
            ((PeriodicMaximizer< FixedTargetMaximizationAlgorithm >) produced.getWorkforceMaximizer()).getMaximizationAlgorithm().setWorkerTarget(11);
            ((PIDTargeterWithQuickFiring)produced.getWorkforceTargeter()).setMaximumPercentageOverTargetOfWorkersToHire(10000f);
            hr1.setControl(produced.getControl());
            hr1.setPricingStrategy(produced.getControl());
            produced.getControl().start();

            //second firm will target 4 workers
            HumanResources hr2 = iterator.next();
//            hr2.setPriceAverager(new ExponentialPriceAverager(.1f, PriceAverager.NoTradingDayPolicy.COUNT_AS_LAST_CLOSING_PRICE));
            produced = TargetAndMaximizePlantControl.PlantControlFactory(hr2, PIDTargeterWithQuickFiring.class, PeriodicMaximizer.class, FixedTargetMaximizationAlgorithm.class);
            ((PeriodicMaximizer< FixedTargetMaximizationAlgorithm >) produced.getWorkforceMaximizer()).getMaximizationAlgorithm().setWorkerTarget(4);
            ((PIDTargeterWithQuickFiring)produced.getWorkforceTargeter()).setMaximumPercentageOverTargetOfWorkersToHire(10000f);
            hr2.setControl(produced.getControl());
            hr2.setPricingStrategy(produced.getControl());
            produced.getControl().start();

            //the third will target 1 worker
            HumanResources hr3 = iterator.next();
//            hr3.setPriceAverager(new ExponentialPriceAverager(.1f, PriceAverager.NoTradingDayPolicy.COUNT_AS_LAST_CLOSING_PRICE));
            produced = TargetAndMaximizePlantControl.PlantControlFactory(hr3, PIDTargeterWithQuickFiring.class, PeriodicMaximizer.class, FixedTargetMaximizationAlgorithm.class);
            ((PeriodicMaximizer< FixedTargetMaximizationAlgorithm >) produced.getWorkforceMaximizer()).getMaximizationAlgorithm().setWorkerTarget(1);
            ((PIDTargeterWithQuickFiring)produced.getWorkforceTargeter()).setMaximumPercentageOverTargetOfWorkersToHire(10000f);
            hr3.setControl(produced.getControl());
            hr3.setPricingStrategy(produced.getControl());
            produced.getControl().start();

            assert !iterator.hasNext();



            scheduleSanityCheck(model, scenario);


            runModel(model, scenario,14,1);
        }
    }


}
