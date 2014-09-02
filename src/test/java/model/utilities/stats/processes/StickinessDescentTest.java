/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.processes;

import model.utilities.pid.PIDController;
import model.utilities.stats.regression.SISORegression;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StickinessDescentTest {


    @Test
    public void testCorrectStickiness() throws Exception {



        SISORegression regression = mock(SISORegression.class);
        when(regression.generateDynamicProcessImpliedByRegression()).thenAnswer(invocation -> new LinearNonDynamicProcess(50,0,1));
        //you want y to be 1, the best thing to do would be to wait 50 turns then increase x by one.
        //make sure at 50 you choose to stay
        StickinessDescent descent = new StickinessDescent(regression,new PIDController(0,1,0,1),1,100);
        Assert.assertEquals(descent.getNewSpeed(),2);

        //at 10 it is still better to go slower
        descent = new StickinessDescent(regression,new PIDController(0,1,0,10),1,100);
        Assert.assertEquals(descent.getNewSpeed(),11);
        //at 50 it doesn't matter where you go.
        descent = new StickinessDescent(regression,new PIDController(0,1,0,50),1,100);
        Assert.assertEquals(descent.getNewSpeed(),50);
    }
}