/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.experiments.tuningRuns;

import agents.firm.Firm;
import agents.firm.cost.InputCostStrategy;
import agents.firm.personell.HumanResources;
import agents.firm.production.Plant;
import agents.firm.production.control.TargetAndMaximizePlantControl;
import agents.firm.production.control.maximizer.SetTargetThenTryAgainMaximizer;
import agents.firm.production.control.maximizer.algorithms.marginalMaximizers.MarginalAndPIDMaximizer;
import agents.firm.production.control.targeter.PIDTargeterWithQuickFiring;
import agents.firm.production.technology.LinearConstantMachinery;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentAllAtOnce;
import agents.firm.sales.SalesDepartmentFactory;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import au.com.bytecode.opencsv.CSVWriter;
import goods.DifferentiatedGoodType;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.scenario.MonopolistScenario;
import model.utilities.ActionOrder;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <h4>Description</h4>
 * <p/>  a tuning where each thread is on its own, let's hope for the best
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-04-07
 * @see
 */
public class MarginalMaximizerPIDTuning {

    private static Lock writerlock = new ReentrantLock();

    private static DecimalFormat df = new DecimalFormat("#.##");




    /**
     * simple progress bar that exploits '\r'
     */
    public static void printProgressBar(int total, int done, int length)
    {
        assert total >= done;
        char[] bar = new char[length];

        int complete = (int) Math.floor(length * ((float)done)/((float)total));
        for(int i=0; i < complete; i++)
            bar[i]='=';
        for(int i = complete; i < bar.length; i++)
            bar[i]=' ';


        System.out.print("|" + new String(bar) + "| " + done + "/" + total +"\r");

    }



    public static class SingleRun implements Runnable
    {

        final private CSVWriter writer;

        final float proportional;

        final float integrative;

        final float derivative;


        public SingleRun(CSVWriter writer, float proportional, float integrative, float derivative) {
            this.writer = writer;
            this.proportional = proportional;
            this.integrative = integrative;
            this.derivative = derivative;
        }

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p/>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {

            float corrects =0f;
            double deviation = 0;
            double variance = 0;
            for(int i=0; i< 15; i++){



                final MacroII macroII = new MacroII(System.currentTimeMillis());
                MonopolistScenario scenario1 = new MonopolistScenario(macroII)
                {
                    //override the monopolist construction in order to sweep through the pid parameters
                    @Override
                    public Firm buildFirm() {
                        //only one seller
                        final Firm built = new Firm(getModel());
                        built.receiveMany(UndifferentiatedGoodType.MONEY,1000000000);
                        built.setName("monopolist");
                        //set up the firm at time 1
                        getModel().scheduleSoon(ActionOrder.DAWN, new Steppable() {
                            @Override
                            public void step(SimState simState) {
                                //sales department
                                SalesDepartment dept = SalesDepartmentFactory.incompleteSalesDepartment(built, goodMarket,
                                        new SimpleBuyerSearch(goodMarket, built), new SimpleSellerSearch(goodMarket, built),
                                        SalesDepartmentAllAtOnce.class);
                                built.registerSaleDepartment(dept, UndifferentiatedGoodType.GENERIC);
                                dept.setAskPricingStrategy(new SimpleFlowSellerPID(dept)); //set strategy to PID

                                //add the plant
                                Plant plant = new Plant(blueprint, built);
                                plant.setPlantMachinery(new LinearConstantMachinery(DifferentiatedGoodType.CAPITAL, built, 0, plant));
                                plant.setCostStrategy(new InputCostStrategy(plant));
                                built.addPlant(plant);


                                //human resources
                                HumanResources hr;
                                //set up!
                                hr = HumanResources.getEmptyHumanResources(1000000000, built, laborMarket, plant);
                                TargetAndMaximizePlantControl control = TargetAndMaximizePlantControl.emptyTargetAndMaximizePlantControl(hr);
                                control.setTargeter(new PIDTargeterWithQuickFiring(hr,control));
                                MarginalAndPIDMaximizer algorithm = new MarginalAndPIDMaximizer(hr,control,plant,plant.getOwner(),
                                        proportional,integrative,derivative,model.getRandom());
                                SetTargetThenTryAgainMaximizer<MarginalAndPIDMaximizer> maximizer =
                                        new SetTargetThenTryAgainMaximizer<>
                                                (hr,control,algorithm);

                                maximizer.getMaximizationAlgorithm().setGains(proportional,integrative,derivative);



                                control.setMaximizer(maximizer);
                                hr.setPricingStrategy(control);
                                hr.setControl(control);

                                //       seller.registerHumanResources(plant, hr);
                                hr.setFixedPayStructure(isFixedPayStructure());
                                hr.start(model);


                            }
                        });

                        getAgents().add(built);
                        return built;
                    }
                };





                macroII.setScenario(scenario1);
                macroII.start();


                int oldWorkers = 0;
                while(macroII.schedule.getTime()<3500)
                {

                    macroII.schedule.step(macroII);
                    variance += Math.pow(scenario1.getMonopolist().getTotalWorkers()-oldWorkers,2);
                    oldWorkers = scenario1.getMonopolist().getTotalWorkers();
                    deviation += Math.pow(scenario1.getMonopolist().getTotalWorkers()-22,2);

                }

                if(scenario1.getMonopolist().getTotalWorkers()==22)
                    corrects++;
            }
            deviation = deviation/5f;
            variance = Math.sqrt(variance/5f);
            System.out.println("done");
            writerlock.lock();
            try {
                writer.writeNext(new String[]{df.format(proportional),df.format(integrative)
                        ,df.format(derivative),df.format(corrects),df.format(deviation)
                        ,df.format(variance)});
                writer.flush();
            } catch (IOException e) {
                System.err.println("fallito a scrivere!");
            }
            finally {
                //              System.out.println("unlocked");
                writerlock.unlock();

            }

        }
    }

}
