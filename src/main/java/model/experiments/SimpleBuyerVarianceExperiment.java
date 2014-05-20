/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.experiments;

import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.scenario.SimpleBuyerScenario;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * <h4>Description</h4>
 * <p/>  An experiment to find out whether there are PID values that work well with the simple buyer setup.
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2012-12-02
 * @see
 */
public class SimpleBuyerVarianceExperiment {

    private SimpleBuyerVarianceExperiment() {
    }

    double distanceFrom10 = 0;

    double distanceFromTarget = 0;

    public static PrintWriter writer;


    public void runOneExperiment(float proportional,float integrative,float derivative)
    {

        System.out.println("**********************************************************************************************");

        final MacroII macroII = new MacroII(System.currentTimeMillis());
        final SimpleBuyerScenario scenario1 = new SimpleBuyerScenario(macroII);
        scenario1.setProportionalGainAVG(proportional);
        scenario1.setIntegralGainAVG(integrative);
        scenario1.setDerivativeGainAVG(integrative);
        scenario1.setPidPeriod(0);


        macroII.setScenario(scenario1);


        distanceFrom10 = 0;
        distanceFromTarget = 0;

        macroII.start();
        macroII.schedule.scheduleRepeating(500,0,new Steppable() {
            @Override
            public void step(SimState state) {
                distanceFrom10 += Math.pow(10-scenario1.getDepartment().maxPrice(scenario1.getDepartment().getGoodType(),scenario1.getDepartment().getMarket()),2);
                distanceFromTarget += Math.pow(scenario1.getTargetInventory()-scenario1.getDepartment().getFirm().hasHowMany(UndifferentiatedGoodType.GENERIC),2);
            }
        });
        while(macroII.schedule.getTime()<10000)
        {
            macroII.schedule.step(macroII);

        }

        writer.println(proportional + " , " + integrative + " ," + derivative + ","+ distanceFrom10 + "," + distanceFromTarget);
        writer.flush();

    }


    public static void main(String[] args)
    {
        SimpleBuyerVarianceExperiment variance = new SimpleBuyerVarianceExperiment();

        try {
            writer = new PrintWriter(new FileWriter("SimpleBuyerExperiment.txt"));
            writer.println("proportional" + " , " + "integrative" + " ," + "derivative" + ","+ "distanceFrom10 "+ "," + "distanceFromTarget");

            for(float proportional = 0; proportional < 3; proportional= proportional+0.1f)
            {
              for(float integrative = 0; integrative < 3; integrative = integrative + 0.1f)
              {
                for (float derivative = 0; derivative < 3; derivative = derivative + 0.1f)
                    variance.runOneExperiment(proportional,integrative,derivative);
              }

            }

            writer.close();

        } catch (IOException e) {


        }



    }





}
