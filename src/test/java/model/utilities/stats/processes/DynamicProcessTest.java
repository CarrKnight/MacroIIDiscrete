/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.processes;

import org.junit.Assert;
import org.junit.Test;

public class DynamicProcessTest {

    @Test
    public void testSimulateManyStepsWithFixedInput() throws Exception
    {

        FirstOrderPlusDeadTime fopdt = new FirstOrderPlusDeadTime(0,10,2,2,0,0d,0d);

        //if I feed it 2 it should go to 4!
        Assert.assertEquals(20,
                DynamicProcess.simulateManyStepsWithFixedInput(fopdt,100,2),
                .0001d);



    }
}