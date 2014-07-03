/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.pid.tuners;

import org.junit.Assert;
import org.junit.Test;

public class ControlGuruTableFOPDTTest {


    @Test
    public void testSimple() throws Exception {

        ControlGuruTableFOPDT table = new ControlGuruTableFOPDT();

        Assert.assertEquals(table.getDerivativeParameter(1f,2f,0),0,.0001f);
        Assert.assertEquals(table.getProportionalParameter(1f,2f,0),1,.0001f);
        Assert.assertEquals(table.getIntegralParameter(1f,2f,0),0.5,.0001f);

    }
}