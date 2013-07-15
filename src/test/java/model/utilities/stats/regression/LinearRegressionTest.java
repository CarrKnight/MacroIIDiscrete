/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import org.junit.Assert;
import org.junit.Test;

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
 * @version 2013-07-11
 * @see
 */
public class LinearRegressionTest {


    @Test
    public void linearRegressionTest()
    {

        //I am going to estimate this in R, and the results ought to be the same!

        double[] x = new double[20];
        for(int i=1; i <=20; i++)
            x[i-1]=i;

        double[] y = new double[]{
                5.544928,  10.462137,  14.655520,  21.852715,  24.383909 , 32.130177,  35.646123,  42.884142,  43.967171,
                51.520706,  56.528435 , 62.053947 , 66.639255 , 72.551495 , 77.795903 , 82.147417 ,88.025807  ,91.112676 ,
                96.816806 ,100.589558

        };


        LinearRegression regression = new LinearRegression();
        regression.estimateModel(x,y,null);

        //make sure the intercept and slope are correct!
        Assert.assertEquals(regression.getIntercept(),0.4674,.01);
        Assert.assertEquals(regression.getSlope(),5.0855,.01);

        //make sure predictions are correct
        Assert.assertEquals(regression.predict(20),102.177986,.01 );
        Assert.assertEquals(regression.predict(10),51.322676,.01 );
        Assert.assertEquals(regression.predict(11.1),56.91645,.01 );



    }

    @Test
    public void weightedLinearRegressionTest()
    {

        //I am going to estimate this in R, and the results ought to be the same!

        double[] x = new double[20];
        double[] weights = new double[20];

        for(int i=1; i <=20; i++)
        {
            x[i-1]=i;
            weights[i-1]=i;
        }

        double[] y = new double[]{
                5.544928,  10.462137,  14.655520,  21.852715,  24.383909 , 32.130177,  35.646123,  42.884142,  43.967171,
                51.520706,  56.528435 , 62.053947 , 66.639255 , 72.551495 , 77.795903 , 82.147417 ,88.025807  ,91.112676 ,
                96.816806 ,100.589558

        };




        LinearRegression regression = new LinearRegression();
        regression.estimateModel(x,y,weights);

        //make sure the intercept and slope are correct!
        Assert.assertEquals(regression.getIntercept(),0.958,.01);
        Assert.assertEquals(regression.getSlope(),5.050,.01);

        //make sure predictions are correct
        Assert.assertEquals(regression.predict(20),101.958,.01 );
        Assert.assertEquals(regression.predict(10),51.458,.01 );
        Assert.assertEquals(regression.predict(11.1),57.013,.01 );



    }



    //if we input every x being the same, it should produce a y = a model
    @Test
    public void onlyIntercept()
    {
        double[] x = new double[20];
        for(int i=1; i <=20; i++)
            x[i-1]=5; //always 5

        //y is always different
        double[] y = new double[]{
                26.45868, 25.28469, 25.89083 ,27.73012, 25.25402, 25.70781, 25.81495, 27.48750 ,27.70585, 25.74754,
                26.87425, 26.10124, 24.12670, 27.25951 ,26.05331, 26.32221, 26.20726, 25.61510, 25.67823
                ,27.00796

        };

        LinearRegression regression = new LinearRegression();
        regression.estimateModel(x,y,null);

        //make sure the intercept and slope are correct!
        Assert.assertEquals(regression.getIntercept(), 26.22,.01);
        Assert.assertEquals(regression.getSlope(),0,.01);

        //make sure predictions are correct
        Assert.assertEquals(regression.predict(20),26.22,.01 );
        Assert.assertEquals(regression.predict(10),26.22,.01 );
        Assert.assertEquals(regression.predict(11.1),26.22,.01 );



    }


    //if we input every x being the same, it should produce a y = a model
    @Test
    public void noEstimationReturnsNaN()
    {
        LinearRegression regression = new LinearRegression();


        //make sure the intercept and slope are correct!
        Assert.assertTrue(Double.isNaN(regression.getIntercept()));
        Assert.assertTrue(Double.isNaN(regression.getSlope()));

        //make sure predictions are correct
        Assert.assertTrue(Double.isNaN(regression.predict(20)) );
        Assert.assertTrue(Double.isNaN(regression.predict(10)) );
        Assert.assertTrue(Double.isNaN(regression.predict(11.1)) );



    }


}
