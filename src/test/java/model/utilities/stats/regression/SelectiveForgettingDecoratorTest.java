/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import ec.util.MersenneTwisterFast;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public class SelectiveForgettingDecoratorTest {

    private final RegressionTestData data = new RegressionTestData();


    @Before
    public void setUp() throws Exception
    {

        data.initializeData();


    }

    @Test
    public void testUnweightedRegression() throws Exception {
        //2 dimensions, data.getX() + intercept
        RecursiveLinearRegression regression = new SelectiveForgettingDecorator(new KalmanRecursiveRegression(2));

        for(int i=0; i < data.getX().length; i++)
        {
            regression.addObservation(1,data.getY()[i],1,data.getX()[i]);
        }
        Assert.assertEquals(2, regression.getBeta()[1], .1d);
        Assert.assertEquals(4.992,regression.getBeta()[0],.1d);


    }

    @Test
    public void testWeightedRegression() throws Exception {
        //2 dimensions, data.getX() + intercept
        RecursiveLinearRegression regression = new SelectiveForgettingDecorator(new KalmanRecursiveRegression(2));

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

        KalmanBasedRecursiveRegression regression = new SelectiveForgettingDecorator(
                new KalmanRecursiveRegression(2));
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

        Assert.assertEquals(regression.getBeta()[0], 94.95367, 5d);//larger margin, really hard
        Assert.assertEquals(regression.getBeta()[1], -1.65746d ,.2d);
    }


    @Test
    public void trackingWithEquilibriumTest()
    {
        MersenneTwisterFast random = new MersenneTwisterFast();

        //do 2000 steps at one regression, 2000 at equilibrium, 2000 at new regression
        KalmanBasedRecursiveRegression regression = new SelectiveForgettingDecorator(new KalmanRecursiveRegression(2),3000,.99);
        int randomIntercept = random.nextInt(500);
        int randomSlope = random.nextInt(6)-3;
        double xObservation=0; double yObservation;
        for(int step =0; step < 2000; step ++)
        {
            xObservation = random.nextInt(100);
            yObservation = randomIntercept + randomSlope * xObservation + random.nextGaussian() * 3;
            regression.addObservation(1,yObservation,1,xObservation);

        }
        System.out.println(Arrays.toString(regression.getBeta()) + "--------|" + randomSlope + "," + randomIntercept);
        System.out.println(Arrays.deepToString(regression.getpCovariance()));
        Assert.assertEquals(randomSlope,regression.getBeta()[1],.25);

        //stuck at equilibrium
        for(int step =0; step < 20000; step ++)
        {
            yObservation = randomIntercept + randomSlope * xObservation + random.nextGaussian() * 3;
            regression.addObservation(1,yObservation,1,xObservation);

        }
        System.out.println(Arrays.toString(regression.getBeta()) + "--------|" + randomSlope + "," + randomIntercept);
        System.out.println(Arrays.deepToString(regression.getpCovariance()));
        Assert.assertEquals(randomSlope,regression.getBeta()[1],.25);
        yObservation = randomIntercept + randomSlope * xObservation + random.nextGaussian() * 3;
        regression.addObservation(1,yObservation,1,xObservation);

        //new slope!
        randomIntercept = random.nextInt(500);
        randomSlope = random.nextInt(6)-3;
        for(int step =0; step < 2000; step ++)
        {
            xObservation = random.nextInt(100);
            yObservation = randomIntercept + randomSlope * xObservation + random.nextGaussian() * 3;
            regression.addObservation(1,yObservation,1,xObservation);

        }
        System.out.println(Arrays.toString(regression.getBeta()) + "--------|" + randomSlope + "," + randomIntercept);
        System.out.println(Arrays.deepToString(regression.getpCovariance()));

        Assert.assertEquals(randomSlope,regression.getBeta()[1],.25);

    }

    //what does it mean to multiply eigenvalues?
    @Test
    public void differenceBetweenDiscountingEigenValuesAndDiscountingDirectly()
    {
        MersenneTwisterFast randomizer = new MersenneTwisterFast(1l);
        Matrix generatingX = new Matrix(new double[]{randomizer.nextDouble(),randomizer.nextDouble(),randomizer.nextDouble()},3);
        Matrix p = generatingX.times(generatingX.transpose());
        System.out.println(Arrays.deepToString(p.getArray()));

        System.out.println(Arrays.deepToString(p.times(1d / .99).getArray()));

        //now eigen-multiply, it should be the same matrix!
        final EigenvalueDecomposition eig = p.eig();
        final double[] realEigenvalues = eig.getRealEigenvalues();
        for(int i=0; i< realEigenvalues.length; i++)
            realEigenvalues[i] *= 1/.99f;

        Matrix forgot = new Matrix(realEigenvalues.length, realEigenvalues.length);
        for (int i = 0; i < realEigenvalues.length; i++)
            forgot.set(i, i, realEigenvalues[i]);
        //by using transpose I am already assuming simmetricity, which I think it's fine, given that
        Matrix newP = eig.getV().times(forgot.times(eig.getV().transpose()));
        System.out.println(Arrays.deepToString(newP.getArray()));

    }


}