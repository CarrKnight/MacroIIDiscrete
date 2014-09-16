/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public class RecursiveLeastSquaresFilterTest {

    private final RegressionTestData RegressionTestData = new RegressionTestData();


    @Before
    public void setUp() throws Exception
    {

        RegressionTestData.initializeData();


    }



    @Test
    public void testUnweightedRegression() throws Exception {
        //2 dimensions, x + intercept
        RecursiveLinearRegression regression = new RecursiveLeastSquaresFilter(2);

        for(int i=0; i < RegressionTestData.getX().length; i++)
        {
            regression.addObservation(1, RegressionTestData.getY()[i],1, RegressionTestData.getX()[i]);
        }
        System.out.println(Arrays.toString(regression.getBeta()));
        Assert.assertEquals(2, regression.getBeta()[1], .1d);
        Assert.assertEquals(4.992,regression.getBeta()[0],.1d);


    }



    @Test
    public void testWeightedRegression() throws Exception {
        //2 dimensions, x + intercept
        RecursiveLinearRegression regression = new RecursiveLeastSquaresFilter(2);

        for(int i=0; i < RegressionTestData.getX().length; i++)
        {
            regression.addObservation(RegressionTestData.getWeights()[i], RegressionTestData.getY()[i],1, RegressionTestData.getX()[i]);
        }
        System.out.println(Arrays.toString(regression.getBeta()));

        Assert.assertEquals(1.997189,regression.getBeta()[1],.01d);
        Assert.assertEquals(4.994421,regression.getBeta()[0],.01d);


    }


}