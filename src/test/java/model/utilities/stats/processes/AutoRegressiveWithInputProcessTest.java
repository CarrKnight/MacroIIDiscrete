/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.processes;

import org.junit.Assert;
import org.junit.Test;

public class AutoRegressiveWithInputProcessTest {


    @Test
    public void test3and3() throws Exception {
        AutoRegressiveWithInputProcess process = new AutoRegressiveWithInputProcess(3,3,new double[]{1,2,3,4},new double[]{1,2,3},100);
        Assert.assertEquals(0,process.getLaggedInput(1),.0001);
        Assert.assertEquals(0,process.getLaggedInput(2),.0001);
        Assert.assertEquals(0,process.getLaggedInput(3),.0001);
        Assert.assertEquals(0,process.getLaggedOutput(1),.0001);
        Assert.assertEquals(0,process.getLaggedOutput(2),.0001);
        Assert.assertEquals(0,process.getLaggedOutput(3),.0001);

        double output = process.newStep(5);
        Assert.assertEquals(105d,output,.0001d);
        Assert.assertEquals(5,process.getLaggedInput(1),.0001);
        Assert.assertEquals(0,process.getLaggedInput(2),.0001);
        Assert.assertEquals(0,process.getLaggedInput(3),.0001);
        Assert.assertEquals(105,process.getLaggedOutput(1),.0001);
        Assert.assertEquals(0,process.getLaggedOutput(2),.0001);
        Assert.assertEquals(0,process.getLaggedOutput(3),.0001);

        output = process.newStep(1);
        Assert.assertEquals(100+105+1+5*2,output,.0001d);
        Assert.assertEquals(1,process.getLaggedInput(1),.0001);
        Assert.assertEquals(5,process.getLaggedInput(2),.0001);
        Assert.assertEquals(0,process.getLaggedInput(3),.0001);
        Assert.assertEquals(100+105+1+5*2,process.getLaggedOutput(1),.0001);
        Assert.assertEquals(105,process.getLaggedOutput(2),.0001);
        Assert.assertEquals(0,process.getLaggedOutput(3),.0001);


        output = process.newStep(0);
        Assert.assertEquals(5,process.getLaggedInput(3),.0001);
        Assert.assertEquals(105,process.getLaggedOutput(3),.0001);


    }
}