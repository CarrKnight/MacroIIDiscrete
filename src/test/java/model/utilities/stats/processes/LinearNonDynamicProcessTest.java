/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.processes;

import org.junit.Assert;
import org.junit.Test;

public class LinearNonDynamicProcessTest {


    @Test
    public void testdelayed() throws Exception {


        LinearNonDynamicProcess nonDynamicProcess = new LinearNonDynamicProcess(1,2,2,1d);
        Assert.assertEquals(5d,nonDynamicProcess.newStep(100,1),0.0001); //2 + 2*1 + covariate (1)

        nonDynamicProcess.setRandomNoise(()-> 4d);    //add "noise" which is just an additional 4

        Assert.assertEquals(2+2*100+4,nonDynamicProcess.newStep(0,0),0001d);


    }
}