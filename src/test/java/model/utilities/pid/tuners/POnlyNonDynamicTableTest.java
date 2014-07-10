/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.pid.tuners;

import ec.util.MersenneTwisterFast;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.pid.PIDController;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class POnlyNonDynamicTableTest {

    //imagine the regression having found:
    //sales = 100-2*p where sales is the output and p the controller mv
    @Test
    public void easyDemand() throws Exception
    {
        POnlyNonDynamicTable table = new POnlyNonDynamicTable();

        Assert.assertEquals(0,table.getProportionalParameter(-2, 0, 100, 0),.0001);
        Assert.assertEquals(-1f/2,table.getIntegralParameter(-2,0,100,0),.0001);
        Assert.assertEquals(0,table.getDerivativeParameter(-2, 0, 100, 0),.0001);
        Assert.assertEquals(50,table.getBaseline(-2, 0, 100, 0),.0001);



    }

    //starts already tuned
    @Test
    public void doesItWork1() throws Exception {
        final MersenneTwisterFast random = new MersenneTwisterFast();
        PIDController controller = new PIDController(0,-1f/2f,0);
        controller.setOffset(50, true);
        //you want to sell 20, you need to price it at 60 (today position doesn't matter)
        for(int i=0; i<100; i++) {
            int quantityTarget = random.nextInt(40);
            float expectedPrice = 50-quantityTarget/2;
            float currentDemand = Math.max(Math.round(100 - 2 * controller.getCurrentMV()),0);
            currentDemand = Float.isNaN(currentDemand)? 0 : currentDemand;
            System.out.println(currentDemand + " ---- " + quantityTarget + "-----" + expectedPrice + " ------- " + controller.getCurrentMV());
            controller.setGains(0,-1f/2f,0);
            controller.setOffset(50, false);
            controller.adjust(quantityTarget, currentDemand, true, mock(MacroII.class), null, ActionOrder.DAWN);
            Assert.assertEquals(expectedPrice, Math.round(controller.getCurrentMV()), .0001f);
        }
    }


    //starts weird, gets tuned later
    @Test
    public void doesItWork2() throws Exception {
        final MersenneTwisterFast random = new MersenneTwisterFast();
        PIDController controller = new PIDController(1,2,0);
        //you want to sell 20, you need to price it at 60 (today position doesn't matter)
        for(int i=0; i<500; i++) {
            int quantityTarget = random.nextInt(100);
            float currentDemand = Math.max(Math.round(100 - 2 * controller.getCurrentMV()),0);
            currentDemand = Float.isNaN(currentDemand)? 0 : currentDemand;
            controller.adjust(quantityTarget, currentDemand, true, mock(MacroII.class), null, ActionOrder.DAWN);
        }

        controller.setGains(0,-1f/2f,0);
        controller.setOffset(50, true);
        for(int i=0; i<100; i++) {
            int quantityTarget = random.nextInt(100);
            float expectedPrice = 50-quantityTarget/2;
            float currentDemand = Math.max(Math.round(100 - 2 * controller.getCurrentMV()),0);
            currentDemand = Float.isNaN(currentDemand)? 0 : currentDemand;
            System.out.println(currentDemand + " ---- " + quantityTarget + "-----" + expectedPrice + " ------- " + controller.getCurrentMV());
            controller.setGains(0,-1f/2f,0);
            controller.setOffset(50, false);
            controller.adjust(quantityTarget, currentDemand, true, mock(MacroII.class), null, ActionOrder.DAWN);
            Assert.assertEquals(expectedPrice, Math.round(controller.getCurrentMV()), .0001f);
        }
    }
}