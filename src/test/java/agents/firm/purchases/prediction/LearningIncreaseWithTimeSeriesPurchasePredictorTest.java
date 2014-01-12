/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.prediction;

import agents.firm.purchases.PurchasesDepartment;
import au.com.bytecode.opencsv.CSVReader;
import com.google.common.primitives.Doubles;
import model.utilities.stats.collectors.PeriodicMarketObserver;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import static org.mockito.Mockito.*;

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
public class LearningIncreaseWithTimeSeriesPurchasePredictorTest {

    //read from csv and solve, please.
    @Test
    public void testing() throws IOException {

        //read data from file!
        CSVReader reader = new CSVReader(new FileReader("./src/test/regressionTest.csv"));
        reader.readNext(); //ignore the header!

        ArrayList<Double> prices = new ArrayList<>(900);
        ArrayList<Double> production = new ArrayList<>(900);


        String[] newLine;
        while(( newLine = reader.readNext()) != null)
        {

            production.add(Double.parseDouble(newLine[1]));
            prices.add(Double.parseDouble(newLine[2]));
        }

        reader.close();

        //make data visible through the periodic observer
        PeriodicMarketObserver observer = mock(PeriodicMarketObserver.class);
        when(observer.getQuantitiesProducedObservedAsArray()).thenReturn(Doubles.toArray(production));
        when(observer.getPricesObservedAsArray()).thenReturn(Doubles.toArray(prices));
        when(observer.getNumberOfObservations()).thenReturn(prices.size());

        //create the predictor
        LearningIncreaseWithTimeSeriesPurchasePredictor predictor = new LearningIncreaseWithTimeSeriesPurchasePredictor(observer);
        //force an update
        predictor.setUsingWeights(false);
        predictor.predictPurchasePriceWhenIncreasingProduction(mock(PurchasesDepartment.class));
        //0.8527228*price - 6.596947
        //notice here that there is a LOT of difference between the R results and my results.
        // The regression are only slightly off, but unfortunately the division amplifies the discrepancy
        Assert.assertEquals(0.8527228,predictor.extractSlopeOfDemandFromRegression(),.025);
        Assert.assertEquals(-6.596947,predictor.extractInterceptOfDemandFromRegression(),.5);

        PurchasesDepartment department = mock(PurchasesDepartment.class);
        when(department.getAveragedClosingPrice()).thenReturn(100f);
        Assert.assertEquals(Math.round(100+1d/0.8527228),predictor.predictPurchasePriceWhenIncreasingProduction(department));
        Assert.assertEquals(Math.round(100-1d/0.8527228),predictor.predictPurchasePriceWhenDecreasingProduction(department));



    }



    //another solution, in this one I copy pasted the results!
    @Test
    public void testing2() throws IOException {

        //read data from file!


        double[] prices = new double[]{
                100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 99.0, 99.0, 99.0, 99.0, 99.0, 99.0, 98.0, 98.0, 98.0,
                98.0, 98.0, 98.0, 98.0, 98.0, 97.0, 97.0, 97.0, 97.0, 97.0, 97.0, 97.0, 97.0, 96.0, 96.0, 96.0, 95.0,
                95.0, 94.0, 94.0, 94.0, 94.0, 94.0, 94.0, 94.0, 94.0, 93.0, 93.0, 93.0, 93.0, 93.0, 92.0, 92.0, 92.0,
                92.0, 92.0, 92.0, 92.0, 91.0, 91.0, 91.0
        };
        double[] production = new double[]{
                1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 3.0, 3.0, 3.0, 3.0,
                3.0, 3.0, 3.0, 3.0, 4.0, 4.0, 4.0, 4.0, 4.0, 4.0, 4.0, 4.0, 5.0, 5.0, 5.0, 6.0, 6.0, 7.0, 7.0, 7.0,
                7.0, 7.0, 7.0, 7.0,
                7.0, 8.0, 8.0, 8.0, 8.0, 8.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 10.0, 10.0, 10.0
        };
        double[] gap = new double[production.length];
        for(int i=0; i< gap.length; i++)
        {
            gap[i] = 0;
        }



        //make data visible through the periodic observer
        PeriodicMarketObserver observer = mock(PeriodicMarketObserver.class);
        when(observer.getQuantitiesProducedObservedAsArray()).thenReturn(production);
        when(observer.getPricesObservedAsArray()).thenReturn(prices);
        when(observer.getDemandGapsAsArray()).thenReturn(gap);
        when(observer.getSupplyGapsAsArray()).thenReturn(gap);
        when(observer.getNumberOfObservations()).thenReturn(prices.length);

        //create the predictor
        LearningIncreaseWithTimeSeriesPurchasePredictor predictor = new LearningIncreaseWithTimeSeriesPurchasePredictor(observer);
        //force an update
        predictor.predictPurchasePriceWhenIncreasingProduction(mock(PurchasesDepartment.class));

        Assert.assertEquals(-1, Math.round(predictor.extractSlopeOfDemandFromRegression()), .01);

        PurchasesDepartment department = mock(PurchasesDepartment.class);
        when(department.getAveragedClosingPrice()).thenReturn(100f);
        Assert.assertEquals(99,predictor.predictPurchasePriceWhenIncreasingProduction(department));
        Assert.assertEquals(101,predictor.predictPurchasePriceWhenDecreasingProduction(department));




    }





}
