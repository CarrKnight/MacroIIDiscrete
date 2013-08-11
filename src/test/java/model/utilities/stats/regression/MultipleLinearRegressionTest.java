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
 * @version 2013-08-07
 * @see
 */
public class MultipleLinearRegressionTest {

    /**
     * These are a copy of the linear regression (univariate)
     */


    @Test
    public void linearRegressionTest() throws LinearRegression.CollinearityException {

        //I am going to estimate this in R, and the results ought to be the same!

        double[] x = new double[20];
        for(int i=1; i <=20; i++)
            x[i-1]=i;

        double[] y = new double[]{
                5.544928,  10.462137,  14.655520,  21.852715,  24.383909 , 32.130177,  35.646123,  42.884142,  43.967171,
                51.520706,  56.528435 , 62.053947 , 66.639255 , 72.551495 , 77.795903 , 82.147417 ,88.025807  ,91.112676 ,
                96.816806 ,100.589558

        };


        MultipleLinearRegression regression = new MultipleLinearRegression();
        regression.estimateModel(y,null,x);

        //make sure the intercept and slope are correct!
        Assert.assertEquals(regression.getResultMatrix()[0], 0.4674, .01);
        Assert.assertEquals(regression.getResultMatrix()[1],5.0855,.01);

        //make sure predictions are correct
        Assert.assertEquals(regression.predict(20),102.177986,.01 );
        Assert.assertEquals(regression.predict(10),51.322676,.01 );
        Assert.assertEquals(regression.predict(11.1),56.91645,.01 );



    }

    @Test
    public void weightedLinearRegressionTest() throws LinearRegression.CollinearityException {

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




        MultipleLinearRegression regression = new MultipleLinearRegression();
        regression.estimateModel(y,weights,x);

        //make sure the intercept and slope are correct!
        Assert.assertEquals(regression.getResultMatrix()[0],0.958,.01);
        Assert.assertEquals(regression.getResultMatrix()[1],5.050,.01);

        //make sure predictions are correct
        Assert.assertEquals(regression.predict(20),101.958,.01 );
        Assert.assertEquals(regression.predict(10),51.458,.01 );
        Assert.assertEquals(regression.predict(11.1),57.013,.01 );



    }






    //if we input every x being the same, it should produce a y = a model
    @Test
    public void noEstimationReturnsNaN()
    {
        MultipleLinearRegression regression = new MultipleLinearRegression();


        //make sure the intercept and slope are correct!
        Assert.assertNull(regression.getResultMatrix());

        //make sure predictions are correct
        Assert.assertTrue(Double.isNaN(regression.predict(20)) );
        Assert.assertTrue(Double.isNaN(regression.predict(10)) );
        Assert.assertTrue(Double.isNaN(regression.predict(11.1)) );



    }


    //multiple linear regressions here

    @Test
    public void multipleLinearRegressionTest() throws LinearRegression.CollinearityException {

        //I am going to estimate this in R, and the results ought to be the same!

        double[] x = new double[20];
        double[] z = new double[20];
        for(int i=1; i <=20; i++)
        {
            x[i-1]=i;
            z[i-1] = i*i;
        }

        double[] y = new double[]{
                0.220132,-1.308092,-8.711355,-20.348017,-34.321316,-52.201061,-76.714972,-105.452581,
                -134.808097,-170.476175,-208.826978,-252.084882,-298.449173,-350.293456,-403.583906,
                -462.481705,-524.983629,-593.838584, -665.814769,-740.413043

        };


        MultipleLinearRegression regression = new MultipleLinearRegression();
        regression.estimateModel(y,null,x,z);

        //make sure the intercept and slope are correct!
        Assert.assertEquals(regression.getResultMatrix()[0], -0.1997, .01);
        Assert.assertEquals(regression.getResultMatrix()[1],3.0936,.01);
        Assert.assertEquals(regression.getResultMatrix()[2],-2.0037,.01);

        //make sure predictions are correct
        Assert.assertEquals(regression.predict(20,20*20),-739.8077,.01 );
        Assert.assertEquals(regression.predict(10,100),-169.6337,.01 );
        Assert.assertEquals(regression.predict(11.1,11.1*11.1),-212.7366,.01 );



    }


    @Test
    public void collinearity() throws LinearRegression.CollinearityException {

        //I am going to estimate this in R, and the results ought to be the same!

        double[] x = new double[20];
        double[] z = new double[20];
        for(int i=1; i <=20; i++)
        {
            x[i-1]=i;
            z[i-1] = i;
        }

        double[] y = new double[]{
                0.220132,-1.308092,-8.711355,-20.348017,-34.321316,-52.201061,-76.714972,-105.452581,
                -134.808097,-170.476175,-208.826978,-252.084882,-298.449173,-350.293456,-403.583906,
                -462.481705,-524.983629,-593.838584, -665.814769,-740.413043

        };


        MultipleLinearRegression regression = new MultipleLinearRegression();
        boolean collinearityFound = false;
        try{
            regression.estimateModel(y,null,x,z);
        }
        catch (LinearRegression.CollinearityException ex)
        {
            collinearityFound = true;
            regression.estimateModel(y,null,x);

        }
        Assert.assertTrue(collinearityFound);
        //make sure the intercept and slope are correct!
        Assert.assertEquals(regression.getResultMatrix()[0], 154.08, .01);
        Assert.assertEquals(regression.getResultMatrix()[1],-38.98,.01);
        //Assert.assertEquals(regression.getResultMatrix()[2],0,.01);

        //make sure predictions are correct
        Assert.assertEquals(regression.predict(20),-625.5892,.01 );
        Assert.assertEquals(regression.predict(10),-235.7528,.01 );
        Assert.assertEquals(regression.predict(11.1), -278.6348,.01 );



    }




}
