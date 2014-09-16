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

/**
 * <h4>Description</h4>
 * <p/>
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-11-10
 * @see
 */
public class KalmanBasedRecursiveRegressionTest {
    private final RegressionTestData data = new RegressionTestData();


    @Before
    public void setUp() throws Exception
    {

        data.initializeData();


    }


    @Test
    public void testUnweightedRegression() throws Exception 
    {
        //2 dimensions, data.getX() + intercept
        RecursiveLinearRegression regression = new KalmanRecursiveRegression(2);

        for(int i=0; i < data.getX().length; i++)
        {
            regression.addObservation(1,data.getY()[i],1,data.getX()[i]);
        }
        Assert.assertEquals(2,regression.getBeta()[1],.1d);
        Assert.assertEquals(4.992,regression.getBeta()[0],.1d);


    }

    @Test
    public void testWeightedRegression() throws Exception {
        //2 dimensions, data.getX() + intercept
        RecursiveLinearRegression regression = new KalmanRecursiveRegression(2);

        for(int i=0; i < data.getX().length; i++)
        {
            regression.addObservation(data.getWeights()[i],data.getY()[i],1,data.getX()[i]);
        }
        Assert.assertEquals(1.997189,regression.getBeta()[1],.1d);
        Assert.assertEquals(4.994421,regression.getBeta()[0],.1d);


    }


    @Test
    public void testAnotherWeightedRegression()
    {

        KalmanRecursiveRegression regression = new KalmanRecursiveRegression(2);
        Assert.assertEquals(data.getY1()[0], 50, .0001);
        Assert.assertEquals(data.getX1()[0], 1, .0001);
        Assert.assertEquals(data.getWeights1()[0], 100, 0);

        for(int i=0; i<data.getX1().length; i++)
        {
            double weight = 2d/(1d+Math.exp(Math.abs(data.getWeights1()[i])));
            regression.addObservation(weight,data.getY1()[i],1,data.getX1()[i]);
            System.out.println(regression.getBeta()[1]);


        }
        System.out.println(Arrays.toString(regression.getBeta()));

        Assert.assertEquals(regression.getBeta()[0], 94.95367, .1d);
        Assert.assertEquals(regression.getBeta()[1], -1.65746d ,.1d);
    }      
}
