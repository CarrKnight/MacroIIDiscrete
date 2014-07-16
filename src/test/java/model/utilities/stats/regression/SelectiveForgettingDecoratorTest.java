/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import au.com.bytecode.opencsv.CSVReader;
import ec.util.MersenneTwisterFast;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileReader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class SelectiveForgettingDecoratorTest {

    private double[] x;
    private double[] y;
    private double[] weights;


    private double[] x1;
    private double[] y1;
    private double[] weights1;




    @Before
    public void setUp() throws Exception
    {

        try (CSVReader reader = new CSVReader(new FileReader(Paths.get("testresources", "recursive.csv").toFile())))
        {

            List<String[]> lines = reader.readAll();
            x = new double[lines.size()-1];
            y = new double[lines.size()-1];
            weights = new double[lines.size()-1];
            for(int i=1; i< lines.size(); i++)
            {   String[] line = lines.get(i);

                x[i-1] = Double.parseDouble(line[1]);
                y[i-1] = Double.parseDouble(line[2]);
                weights[i-1] = Double.parseDouble(line[3]);

            }


        }

        try (CSVReader reader = new CSVReader(new FileReader(Paths.get("testresources", "tolearn.csv").toFile())))
        {

            List<String[]> lines = reader.readAll();
            x1 = new double[lines.size()-1];
            y1 = new double[lines.size()-1];
            weights1 = new double[lines.size()-1];
            for(int i=1; i< lines.size(); i++)   //skip header
            {   String[] line = lines.get(i);

                x1[i-1] = Double.parseDouble(line[1]);
                y1[i-1] = Double.parseDouble(line[2]);
                weights1[i-1] = Double.parseDouble(line[3]);

            }


        }


    }

    @Test
    public void testUnweightedRegression() throws Exception {
        //2 dimensions, x + intercept
        RecursiveLinearRegression regression = new SelectiveForgettingDecorator(new KalmanRecursiveRegression(2));

        for(int i=0; i < x.length; i++)
        {
            regression.addObservation(1,y[i],1,x[i]);
        }
        Assert.assertEquals(2, regression.getBeta()[1], .1d);
        Assert.assertEquals(4.992,regression.getBeta()[0],.1d);


    }

    @Test
    public void testWeightedRegression() throws Exception {
        //2 dimensions, x + intercept
        RecursiveLinearRegression regression = new SelectiveForgettingDecorator(new KalmanRecursiveRegression(2));

        for(int i=0; i < x.length; i++)
        {
            regression.addObservation(weights[i],y[i],1,x[i]);
        }
        Assert.assertEquals(1.997189,regression.getBeta()[1],.1d);
        Assert.assertEquals(4.994421,regression.getBeta()[0],.1d);


    }


    @Test
    public void testAnotherWeightedRegression()
    {

        KalmanBasedRecursiveRegression regression = new SelectiveForgettingDecorator(new KalmanRecursiveRegression(2));
        Assert.assertEquals(y1[0], 50, .0001);
        Assert.assertEquals(x1[0], 1, .0001);
        Assert.assertEquals(weights1[0], 100, 0);

        for(int i=0; i<x1.length; i++)
        {
            double weight = 2d/(1d+Math.exp(Math.abs(weights1[i])));
            regression.addObservation(weight,y1[i],1,x1[i]);
            System.out.println(regression.getBeta()[1]);


        }
        System.out.println(Arrays.toString(regression.getBeta()));

        Assert.assertEquals(regression.getBeta()[0], 94.95367, 1d);
        Assert.assertEquals(regression.getBeta()[1], -1.65746d ,.1d);
    }


    @Test
    public void trackingWithEquilibriumTest()
    {
        MersenneTwisterFast random = new MersenneTwisterFast();

        //do 2000 steps at one regression, 2000 at equilibrium, 2000 at new regression
        KalmanBasedRecursiveRegression regression = new SelectiveForgettingDecorator(new KalmanRecursiveRegression(2));
        int randomIntercept = random.nextInt(500);
        int randomSlope = random.nextInt(6)-3;
        double xObservation=0; double yObservation;
        for(int step =0; step < 2000; step ++)
        {
            xObservation = random.nextInt(100);
            yObservation = randomIntercept + randomSlope * xObservation + random.nextGaussian() * 3;
            regression.addObservation(1,yObservation,1,xObservation);

        }
        System.out.println(Arrays.toString(regression.getBeta()) + "--------" + randomSlope + "," + randomIntercept);
        Assert.assertEquals(randomIntercept,regression.getBeta()[0],1);
        Assert.assertEquals(randomSlope,regression.getBeta()[1],.25);

        //stuck at equilibrium
        for(int step =0; step < 2000; step ++)
        {
            yObservation = randomIntercept + randomSlope * xObservation + random.nextGaussian() * 3;
            regression.addObservation(1,yObservation,1,xObservation);

        }
        System.out.println(Arrays.toString(regression.getBeta()) + "--------" + randomSlope + "," + randomIntercept);
        Assert.assertEquals(randomIntercept,regression.getBeta()[0],1);
        Assert.assertEquals(randomSlope,regression.getBeta()[1],.25);

        //new slope!
        randomIntercept = random.nextInt(500);
        randomSlope = random.nextInt(6)-3;
        for(int step =0; step < 2000; step ++)
        {
            xObservation = random.nextInt(100);
            yObservation = randomIntercept + randomSlope * xObservation + random.nextGaussian() * 3;
            regression.addObservation(1,yObservation,1,xObservation);

        }
        System.out.println(Arrays.toString(regression.getBeta()) + "--------" + randomSlope + "," + randomIntercept);
        Assert.assertEquals(randomIntercept,regression.getBeta()[0],1);
        Assert.assertEquals(randomSlope,regression.getBeta()[1],.25);

    }

}