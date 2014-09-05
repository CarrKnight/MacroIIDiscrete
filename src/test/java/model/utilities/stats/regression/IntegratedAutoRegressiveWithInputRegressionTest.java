/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import model.utilities.stats.processes.IntegratedAutoRegressiveWithInputProcess;
import org.junit.Assert;
import org.junit.Test;

public class IntegratedAutoRegressiveWithInputRegressionTest {



    @Test
    public void testRegressionFromCSV() throws Exception {

        IntegratedAutoRegressiveWithInputProcess process =
                new IntegratedAutoRegressiveWithInputProcess(new double[]{.25,.2,.01},new double[]{.05,.01},0,new double[]{0,0},
                        new double[]{0,0},0,0);

        IntegratedAutoRegressiveWithInputRegression reg = new IntegratedAutoRegressiveWithInputRegression(2,2);
        for(int i=0; i<10000; i++)
            reg.addObservation(process.newStep(i),i);

        //no noise. Should be easy!
        Assert.assertEquals(0,reg.getBeta()[0],.01);
        Assert.assertEquals(.25,reg.getBeta()[1],.01);
        Assert.assertEquals(.2,reg.getBeta()[2],.01);
        Assert.assertEquals(.01,reg.getBeta()[3],.01);
        Assert.assertEquals(.05,reg.getBeta()[4],.01);
        Assert.assertEquals(.01,reg.getBeta()[5],.01);






    }

}