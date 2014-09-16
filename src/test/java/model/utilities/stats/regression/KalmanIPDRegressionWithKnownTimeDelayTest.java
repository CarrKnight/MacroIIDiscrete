/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import ec.util.MersenneTwisterFast;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.DelayBin;
import model.utilities.pid.PIDController;
import org.junit.Assert;
import org.junit.Test;
import sim.engine.Steppable;

import static org.mockito.Mockito.*;

public class KalmanIPDRegressionWithKnownTimeDelayTest {


    @Test
    public void regressCorrectlyNoDelay() throws Exception {

        //ipd: deltay= -50+2 u+ inflow
        //this creates a left censoring of sorts
        KalmanIPDRegressionWithKnownTimeDelay ipd = new KalmanIPDRegressionWithKnownTimeDelay(0);
        float stock = 0;

        final MersenneTwisterFast random = new MersenneTwisterFast();
        PIDController controller = new PIDController(.1f,.2f,.1f);
        int target = 100;
        int inflow = 3;
        for(int i=0; i<1000; i++)
        {
            controller.adjust(target,stock,true,mock(MacroII.class),mock(Steppable.class), ActionOrder.DAWN);
            stock = stock + inflow -50 + 2* controller.getCurrentMV();
            ipd.addObservation(stock,controller.getCurrentMV(),inflow);
            System.out.println(stock + "----" + controller.getCurrentMV());

        }

        Assert.assertEquals(-50d,ipd.getIntercept(),.01f);
        Assert.assertEquals(2,ipd.getGain(),.01f);



        //long run equilibrium don't matter
        for(int i=0; i<1000; i++)
        {
            ipd.addObservation(stock, controller.getCurrentMV(), inflow);
        }


        Assert.assertEquals(-50d,ipd.getIntercept(),.01f);
        Assert.assertEquals(2,ipd.getGain(),.01f);

    }


    @Test
    public void regressCorrectlyWithDelay() throws Exception {

        //ipd: deltay= -50+2 u+ inflow
        //this creates a left censoring of sorts
        KalmanIPDRegressionWithKnownTimeDelay ipd = new KalmanIPDRegressionWithKnownTimeDelay(2);
        DelayBin<Float> policy = new DelayBin<>(2,0f);
        float stock = 0;

        final MersenneTwisterFast random = new MersenneTwisterFast();
        PIDController controller = new PIDController(.01f,.002f,0); controller.setCanGoNegative(true); controller.setWindupStop(false);
        int target = 100;
        int inflow = 3;
        for(int i=0; i<5000; i++)
        {
            controller.adjust(target, stock, true, mock(MacroII.class), mock(Steppable.class), ActionOrder.DAWN);
            final Float input = policy.addAndRetrieve(controller.getCurrentMV());
            stock = stock + inflow -50 + 2* input;
            ipd.addObservation(stock,controller.getCurrentMV(),inflow);
            System.out.println(stock + "," + input + "," + inflow);

        }

        Assert.assertEquals(-50d,ipd.getIntercept(),.01f);
        Assert.assertEquals(2,ipd.getGain(),.01f);



        //long run equilibrium don't matter
        for(int i=0; i<1000; i++)
        {
            ipd.addObservation(stock,controller.getCurrentMV(),inflow);
        }


        Assert.assertEquals(2,ipd.getGain(),.01f);
        Assert.assertEquals(-50d,ipd.getIntercept(),.01f);


    }
}