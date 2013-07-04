/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.scenario;

import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.pricing.AskPricingStrategy;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.base.Preconditions;
import model.MacroII;
import model.utilities.stats.DailyStatCollector;

import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import static model.experiments.tuningRuns.MarginalMaximizerWithUnitPIDTuningMultiThreaded.printProgressBar;

/**
 * <h4>Description</h4>
 * <p/>  like simple seller scenario, but it forces to use just one strategy so we can manipulate PID variables easily
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-05-16
 * @see
 */
public class SimpleSellerScenarioUsingFlowSellerPID extends SimpleSellerScenario {

    /**
     * Creates the scenario object, so that it links to the model.
     *
     */
    public SimpleSellerScenarioUsingFlowSellerPID(MacroII model) {
        super(model);
        super.setSellerStrategy(SimpleFlowSellerPID.class);
    }

    /**
     * Sets new the strategy used by the sales department to tinker with prices.
     *
     * @param sellerStrategy New value of the strategy used by the sales department to tinker with prices.
     */
    public void setSellerStrategy(Class<? extends AskPricingStrategy> sellerStrategy) {
        Preconditions.checkArgument(sellerStrategy.getClass().equals(SimpleFlowSellerPID.class)); //can't be set to anything else
    }


    public SimpleFlowSellerPID getStrategy(){
        return (SimpleFlowSellerPID) super.strategy;
    }

    public int getSpeed() {
        return getStrategy().getSpeed();
    }

    public void setDerivativeGain(float derivativeGain) {
        getStrategy().setDerivativeGain(derivativeGain);
    }

    public float getDerivativeGain() {
        return getStrategy().getDerivativeGain();
    }

    public float getProportionalGain() {
        return getStrategy().getProportionalGain();
    }

    public void setProportionalGain(float proportionalGain) {
        getStrategy().setProportionalGain(proportionalGain);
    }

    public float getIntegralGain() {
        return getStrategy().getIntegralGain();
    }

    public void setIntegralGain(float integralGain) {
        getStrategy().setIntegralGain(integralGain);
    }


    /**
     * Runs the simple seller scenario with no GUI and writes a big CSV file  (to draw graphs for the paper)
     * @param args
     */
    public static void main(String[] args)
    {

        DecimalFormat df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.HALF_UP);

        //change P from 0.1 to 1
        //do it 10 times
        for(float i=0.2f; i<=5.71;i+=0.5)
        {

            //set up
            final MacroII macroII = new MacroII(999);
            SimpleSellerScenarioUsingFlowSellerPID scenario1 = new SimpleSellerScenarioUsingFlowSellerPID(macroII);
            scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);

            //assign scenario
            macroII.setScenario(scenario1);
            macroII.start();


            scenario1.setIntegralGain(.25f);
            scenario1.setDerivativeGain(.0001f);
            scenario1.setProportionalGain(i);



            //CSV writer set up
            try {
                CSVWriter writer = new CSVWriter(new FileWriter("runs/simpleSeller/P/"+
                        "simpleSellerP"+df.format(i) +".csv"));
                DailyStatCollector collector = new DailyStatCollector(macroII,writer);
                collector.start();

            } catch (IOException e) {
                System.err.println("failed to create the file!");
            }


            //run!
            while(macroII.schedule.getTime()<1000)
            {
                macroII.schedule.step(macroII);
                printProgressBar(1001,(int)macroII.schedule.getSteps(),100);
            }
            System.out.println("runs/simpleSeller/P"+
                    "simpleSellerP"+df.format(i) +".csv");


        }

        //the same, now for I
        //change P from 0.1 to 1
        //do it 10 times
        for(float i=0.2f; i<=5.71;i+=0.5)
        {

            //set up
            final MacroII macroII = new MacroII(999);
            SimpleSellerScenarioUsingFlowSellerPID scenario1 = new SimpleSellerScenarioUsingFlowSellerPID(macroII);
            scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);

            //assign scenario
            macroII.setScenario(scenario1);

            macroII.start();


            scenario1.setIntegralGain(i);
            scenario1.setDerivativeGain(.0001f);
            scenario1.setProportionalGain(.25f);



            //CSV writer set up
            try {
                CSVWriter writer = new CSVWriter(new FileWriter("runs/simpleSeller/I/"+
                        "simpleSellerI"+df.format(i) +".csv"));
                DailyStatCollector collector = new DailyStatCollector(macroII,writer);
                collector.start();

            } catch (IOException e) {
                System.err.println("failed to create the file!");
            }


            //run!
            while(macroII.schedule.getTime()<1000)
            {
                macroII.schedule.step(macroII);
                printProgressBar(1001,(int)macroII.schedule.getSteps(),100);
            }


        }


        //the same, now for D
        //change P from .0001 to 0.1
        //do it 10 times
        for(float i=.0001f; i<=0.11f;i*=2f)
        {

            //set up
            final MacroII macroII = new MacroII(999);
            SimpleSellerScenarioUsingFlowSellerPID scenario1 = new SimpleSellerScenarioUsingFlowSellerPID(macroII);
            scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);

            //assign scenario
            macroII.setScenario(scenario1);

            macroII.start();


            scenario1.setIntegralGain(.25f);
            scenario1.setDerivativeGain(i);
            scenario1.setProportionalGain(.25f);



            //CSV writer set up
            try {
                CSVWriter writer = new CSVWriter(new FileWriter("runs/simpleSeller/D/"+
                        "simpleSellerD"+df.format(i) +".csv"));
                DailyStatCollector collector = new DailyStatCollector(macroII,writer);
                collector.start();

            } catch (IOException e) {
                System.err.println("failed to create the file!");
            }


            //run!
            while(macroII.schedule.getTime()<1000)
            {
                macroII.schedule.step(macroII);
                printProgressBar(1001,(int)macroII.schedule.getSteps(),100);
            }


        }

    }



}
