/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.experiments;

import au.com.bytecode.opencsv.CSVWriter;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.experiments.tuningRuns.MarginalMaximizerPIDTuning;
import model.scenario.SimpleSellerWithSellerDelayScenario;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static model.experiments.tuningRuns.MarginalMaximizerPIDTuning.printProgressBar;

/**
 * <h4>Description</h4>
 * <p/> Here I vary delays in seller and buyer and see what happens to prices
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-04-25
 * @see
 */
public class SimpleSellerDelayStickyPrices {


    public static Lock writerlock = new ReentrantLock();

    public static void main(String[] args) throws IOException, InterruptedException {

        CSVWriter writer = new CSVWriter(new FileWriter("simpleDelaySticky3.csv"));

        ExecutorCompletionService executor = new ExecutorCompletionService(Executors.newFixedThreadPool(5));

        int combinations = 0;

        for(int buyerDelays = 68; buyerDelays < 100; buyerDelays++)
        {
            for(int sellerDelays = 0; sellerDelays < 17; sellerDelays++)
            {

                for(int gap=5; gap<15; gap++)
                {

                    SingleRun run = new SingleRun(writer,buyerDelays,sellerDelays,gap);
                    combinations++;
                    executor.submit(run,null);


                }



            }


        }



        System.out.println("total combinations to try: " + combinations );


        MarginalMaximizerPIDTuning.printProgressBar(combinations, 0, 20);

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

    }


    public static class SingleRun implements Runnable
    {

        final private CSVWriter writer;

        final int buyerDelay;

        final int sellerDelay;

        final int gap;


        public SingleRun(CSVWriter writer, int buyerDelay, int sellerDelay, int gap) {
            this.writer = writer;
            this.buyerDelay = buyerDelay;
            this.sellerDelay = sellerDelay;
            this.gap = gap;
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

            int timesCorrect = 0;
            double deviation = 0;
            double variance = 0;
            for(int i=0; i< 5; i++){


                final MacroII macroII = new MacroII(System.currentTimeMillis());
                SimpleSellerWithSellerDelayScenario scenario1 = new SimpleSellerWithSellerDelayScenario(macroII);
                scenario1.setDemandShifts(false);
                scenario1.setBuyerDelay(buyerDelay);
                scenario1.setSellerDelay(sellerDelay);
                scenario1.setAcceptablePriceRange(gap);




                macroII.setScenario(scenario1);
                macroII.start();

                long oldPrice = 0;
                boolean correctLast500steps = true;
                while(macroII.schedule.getTime()<5000)
                {

                    macroII.schedule.step(macroII);

                    long price = 0;
                    try {
                        price = scenario1.getMarkets().get(UndifferentiatedGoodType.GENERIC).getBestSellPrice();
                    } catch (IllegalAccessException e) {

                    }
                    if(price==-1)
                        price=100;


                    //compute variance
                    variance += Math.pow(price-oldPrice,2);
                    oldPrice = price;

                    //compute deviation
                    if(price < 50)
                        deviation += Math.pow(price-50,2);
                    else if(price > 50 + gap)
                        deviation += Math.pow(50+gap-price,2);


                    if(macroII.schedule.getTime()>4500)
                        correctLast500steps = correctLast500steps && (price >50 && price<=price+gap);


                }

                if(correctLast500steps)
                    timesCorrect++;
            }

            deviation = deviation/5f;
            variance = variance/5f;
            //           System.out.println("done");
            writerlock.lock();
            try {
                writer.writeNext(new String[]{Integer.toString((buyerDelay)),Integer.toString((sellerDelay))
                        ,Integer.toString((gap)),Integer.toString(timesCorrect),Double.toString(deviation)
                        ,Double.toString(variance)});
                writer.flush();
            } catch (IOException e) {
                System.err.println("fallito a scrivere!");
            }
            finally {
                writerlock.unlock();

            }

        }
    }

}
