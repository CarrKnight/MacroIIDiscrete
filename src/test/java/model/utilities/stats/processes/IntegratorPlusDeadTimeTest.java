/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.processes;

import org.junit.Assert;
import org.junit.Test;

public class IntegratorPlusDeadTimeTest {


    @Test
    public void simpleRun() throws Exception {

        IntegratorPlusDeadTime process = new IntegratorPlusDeadTime(2,-2,.90,0, 0);

        double y=0;
        for(int i=0; i<14; i++)
            y= process.newStep(i,5);

        Assert.assertEquals(y,-58.630377,.00001);


    }
}