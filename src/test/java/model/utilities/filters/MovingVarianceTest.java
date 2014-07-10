/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.filters;

import org.junit.Assert;
import org.junit.Test;

public class MovingVarianceTest {


    @Test
    public void movingVariance() throws Exception {

        MovingVariance<Integer> variance = new MovingVariance<>(4);
        for(int i=1; i<4; i++) {
            variance.addObservation(i);
            Assert.assertTrue(Float.isNaN(variance.getSmoothedObservation()));
            Assert.assertTrue(Float.isNaN(variance.getAverage()));
        }
        variance.addObservation(4);
        Assert.assertEquals(2.5f,variance.getAverage(),.001f);
        Assert.assertEquals(1.25f,variance.getSmoothedObservation(),.001f);
        variance.addObservation(5);
        Assert.assertEquals(3.5f,variance.getAverage(),.001f);
        Assert.assertEquals(1.25f,variance.getSmoothedObservation(),.001f);
        variance.addObservation(10);
        Assert.assertEquals(5.5f,variance.getAverage(),.001f);
        Assert.assertEquals(7.25f,variance.getSmoothedObservation(),.001f);
    }
}

