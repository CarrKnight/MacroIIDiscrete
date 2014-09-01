/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.processes;

import model.utilities.DelayBin;
import org.junit.Assert;
import org.junit.Test;

public class ErrorCorrectingDynamicProcessTest {


    @Test
    public void testDynamic() throws Exception {

        DelayBin<Double> delayedX = new DelayBin< >(5,0d);
        for(int i=0; i<=5; i++)
            delayedX.addAndRetrieve((double) i);

        ErrorCorrectingDynamicProcess errorCorrectingDynamicProcess =
                new ErrorCorrectingDynamicProcess(10,5,2,1,0,0,delayedX);

        Assert.assertEquals(15,errorCorrectingDynamicProcess.newStep(6),.0001);
        Assert.assertEquals(61
                ,errorCorrectingDynamicProcess.newStep(6),.0001);
        Assert.assertEquals(200
                ,errorCorrectingDynamicProcess.newStep(6),.0001);
        Assert.assertEquals(618
                ,errorCorrectingDynamicProcess.newStep(6),.0001);



    }
}