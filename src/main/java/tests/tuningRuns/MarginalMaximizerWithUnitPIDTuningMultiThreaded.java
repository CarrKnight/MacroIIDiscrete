/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package tests.tuningRuns;

import agents.firm.Firm;
import agents.firm.cost.InputCostStrategy;
import agents.firm.personell.HumanResources;
import agents.firm.production.Plant;
import agents.firm.production.control.TargetAndMaximizePlantControl;
import agents.firm.production.control.maximizer.WeeklyWorkforceMaximizer;
import agents.firm.production.control.maximizer.algorithms.marginalMaximizers.MarginalMaximizerWithUnitPID;
import agents.firm.production.control.targeter.PIDTargeter;
import agents.firm.production.technology.LinearConstantMachinery;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import au.com.bytecode.opencsv.CSVWriter;
import goods.GoodType;
import model.MacroII;
import model.scenario.MonopolistScenario;
import model.utilities.ActionOrder;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.mockito.Mockito.*;

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
public class MarginalMaximizerWithUnitPIDTuningMultiThreaded {

    public static Lock writerlock = new ReentrantLock();


    public static void main(String[] args){


        //create the writer
        try {
            CSVWriter writer = new CSVWriter(new FileWriter("multithreadedTuning.csv"));



            ExecutorCompletionService executor = new ExecutorCompletionService(Executors.newFixedThreadPool(5));

            int combinations = 0;
            for(float proportional=0.01f; proportional< 4f; proportional = proportional +.02f)
                for(float integral=0.01f; integral< 4f; integral=  integral + .02f)
                    for(float derivative = 0.01f; derivative <=.03; derivative = derivative + .01f)
                    {
                        combinations++;

                        SingleRun run = new SingleRun(writer,proportional,integral,derivative);
                        executor.submit(run, null);
                    }


            System.out.println("total combinations to try: " + combinations );


            printProgressBar(combinations, 0, 20);

            //now start them all
            int i =0;
            while(i < combinations)
            {
                executor.take();
                i++;
                printProgressBar(combinations,i,20);
            }


            System.out.println("done!");
            writer.close();
            System.exit(0);

        } catch (IOException e) {
            System.err.println("couldn't write!!");
            System.exit(0);

        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

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
 //           System.out.println("started");

            float futureTargetAverage =0f;
            double deviation = 0;
            double variance = 0;
            for(int i=0; i< 5; i++){


                final MacroII macroII = new MacroII(System.currentTimeMillis());
                MonopolistScenario scenario1 = new MonopolistScenario(macroII)
                {
                    //override the monopolist construction in order to sweep through the pid parameters
                    @Override
                    public void buildMonopolist() {
                        //only one seller
                        monopolist = new Firm(getModel());
                        monopolist.earn(1000000000l);
                        monopolist.setName("monopolist");
                        //set up the firm at time 1
                        getModel().scheduleSoon(ActionOrder.DAWN, new Steppable() {
                            @Override
                            public void step(SimState simState) {
                                //sales department
                                SalesDepartment dept = SalesDepartment.incompleteSalesDepartment(monopolist, goodMarket,
                                        new SimpleBuyerSearch(goodMarket, monopolist), new SimpleSellerSearch(goodMarket, monopolist));
                                monopolist.registerSaleDepartment(dept, GoodType.GENERIC);
                                dept.setAskPricingStrategy(new SimpleFlowSellerPID(dept)); //set strategy to PID

                                //add the plant
                                Plant plant = new Plant(blueprint, monopolist);
                                plant.setPlantMachinery(new LinearConstantMachinery(GoodType.CAPITAL, mock(Firm.class), 0, plant));
                                plant.setCostStrategy(new InputCostStrategy(plant));
                                monopolist.addPlant(plant);


                                //human resources
                                HumanResources hr;
                                //set up!
                                hr = HumanResources.getEmptyHumanResources(10000000000l, monopolist, laborMarket, plant);
                                TargetAndMaximizePlantControl control = TargetAndMaximizePlantControl.emptyTargetAndMaximizePlantControl(hr);
                                control.setTargeter(new PIDTargeter(hr,control));
                                MarginalMaximizerWithUnitPID algorithm = new MarginalMaximizerWithUnitPID(hr,control,plant,plant.getOwner(),
                                        model.random, hr.getPlant().workerSize());
                                WeeklyWorkforceMaximizer<MarginalMaximizerWithUnitPID> maximizer =
                                        new WeeklyWorkforceMaximizer<>
                                                (hr,control,algorithm);

                                maximizer.getMaximizationAlgorithm().setSigmoid(true);
                                maximizer.getMaximizationAlgorithm().setGains(proportional,integrative,derivative);



                                control.setMaximizer(maximizer);
                                hr.setPricingStrategy(control);
                                hr.setControl(control);

                                //       seller.registerHumanResources(plant, hr);
                                hr.setFixedPayStructure(isFixedPayStructure());
                                hr.start();


                            }
                        });

                        getAgents().add(monopolist);
                    }
                };





                macroII.setScenario(scenario1);
                macroII.start();

                int oldWorkers = 0;
                while(macroII.schedule.getTime()<3500)
                {

                    macroII.schedule.step(macroII);
                    variance += Math.pow(scenario1.getMonopolist().getTotalWorkers()-oldWorkers,2);
                    deviation += Math.pow(scenario1.getMonopolist().getTotalWorkers()-22,2);

                }


                futureTargetAverage+= scenario1.getMonopolist().getTotalWorkers();
            }
            futureTargetAverage= futureTargetAverage/5f;
            deviation = deviation/5f;
            variance = variance/5f;
 //           System.out.println("done");
            writerlock.lock();
            try {
                writer.writeNext(new String[]{Float.toString((proportional)),Float.toString((integrative))
                        ,Float.toString((derivative)),Float.toString(futureTargetAverage),Double.toString(deviation)
                        ,Double.toString(variance)});
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
