/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package tests.tuningRuns;

import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.production.Plant;
import agents.firm.production.control.TargetAndMaximizePlantControl;
import agents.firm.production.control.maximizer.algorithms.hillClimbers.PIDHillClimber;
import au.com.bytecode.opencsv.CSVWriter;
import model.MacroII;

import java.io.FileWriter;
import java.io.IOException;

import static org.mockito.Mockito.*;

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
 * @version 2013-02-15
 * @see
 */
public class PIDHillClimberTuning {

    /**
     *go through many PID parameters to find out what's best to maximize
     * PI =  -futureTarget*futureTarget + 20 * futureTarget +2 (whose max should be 11)
     */
    public static void main(String[] args)
    {
        try {
            CSVWriter writer = new CSVWriter(new FileWriter("tuning.csv"));
            writer.writeNext(new String[]{"proportional","integrative","derivative","maxFound","deviance","variance"});


            for(float proportional = 0.001f; proportional <=0.3f; proportional += 0.010f ){
                for(float integral = 0.001f; integral <=0.3f; integral += 0.010f ){
                    for(float derivative = 0f; derivative <=0.1f; derivative += 0.001f ){
                        //do each run 5 times and take averages
                        float futureTargetAverage =0f;
                        double deviation = 0;
                        double variance = 0;
                        for(int times = 0; times<5; times++)
                        {


                            HumanResources hr = mock(HumanResources.class);
                            TargetAndMaximizePlantControl control = mock(TargetAndMaximizePlantControl.class);
                            Plant plant = mock(Plant.class);
                            Firm firm = mock(Firm.class);  when(hr.getFirm()).thenReturn(firm);
                            when(firm.getModel()).thenReturn(new MacroII(times));
                            when(control.getPlant()).thenReturn(plant);
                            when(plant.maximumWorkersPossible()).thenReturn(30); when(plant.getBuildingCosts()).thenReturn(1l);
                            when(plant.minimumWorkersNeeded()).thenReturn(1);
                            when(hr.getFirm()).thenReturn(firm);
                            when(firm.getModel()).thenReturn(new MacroII(1l));
                            when(control.getHr()).thenReturn(hr); when(hr.maximumWorkersPossible()).thenReturn(30);
                            when(hr.getPlant()).thenReturn(plant);




                            //maximize!
                            PIDHillClimber maximizer = new PIDHillClimber(hr,proportional,integral,derivative);

                            long oldRevenue=0;
                            long oldCosts = 0;
                            long oldProfits = 0;
                            int oldTarget = 0;
                            int currentWorkerTarget = 1;
                            //start the parameters

                            for(int i=0; i < 1000; i++)
                            {

                                int futureTarget =  maximizer.chooseWorkerTarget(currentWorkerTarget,
                                        revenuePerWorker(currentWorkerTarget) - costPerWorker(currentWorkerTarget),
                                        revenuePerWorker(currentWorkerTarget),costPerWorker(currentWorkerTarget),
                                        oldRevenue,oldCosts, oldTarget,oldProfits);

                                oldTarget=currentWorkerTarget;
                                oldProfits = revenuePerWorker(oldTarget) - costPerWorker(oldTarget);
                                oldRevenue = revenuePerWorker(oldTarget);
                                oldCosts = costPerWorker(oldTarget);
                                currentWorkerTarget=futureTarget;

                                variance += Math.pow(currentWorkerTarget-oldTarget,2);
                                deviation += Math.pow(currentWorkerTarget-22,2);


                            }


                            futureTargetAverage +=currentWorkerTarget;
                        }

                        futureTargetAverage= futureTargetAverage/5f;
                        deviation = deviation/5f;
                        variance = variance/5f;
                        System.out.println(proportional + "," + integral + "," + derivative + "," + futureTargetAverage);

                        writer.writeNext(new String[]{Float.toString(proportional),Float.toString(integral)
                                ,Float.toString(derivative),Float.toString(futureTargetAverage),Double.toString(deviation)
                                ,Double.toString(variance)});
                    }

                }
            }

            writer.close();


        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }


    //use the functions from the monopolist

    public static long costPerWorker(int workers)
    {
        long wages;
        if(workers > 0)
            wages = 105 + (workers -1)*7;
        else
            wages = 0;

        return wages*workers;

    }

    public static long revenuePerWorker(int workers)
    {
        int quantity = workers * 7;
        int price = 101 - workers;

        return quantity * price;
    }









}
