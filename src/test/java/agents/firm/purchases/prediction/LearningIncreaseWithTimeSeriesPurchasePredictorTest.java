/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.prediction;

import agents.firm.purchases.PurchasesDepartment;
import au.com.bytecode.opencsv.CSVReader;
import com.google.common.primitives.Doubles;
import financial.Market;
import goods.GoodType;
import model.utilities.stats.PeriodicMarketObserver;
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


        //make data visible through the periodic observer
        PeriodicMarketObserver observer = mock(PeriodicMarketObserver.class);
        when(observer.getQuantitiesProducedObservedAsArray()).thenReturn(Doubles.toArray(production));
        when(observer.getPricesObservedAsArray()).thenReturn(Doubles.toArray(prices));
        when(observer.getNumberOfObservations()).thenReturn(prices.size());

        //create the predictor
        LearningIncreaseWithTimeSeriesPurchasePredictor predictor = new LearningIncreaseWithTimeSeriesPurchasePredictor(observer);
        //force an update
        predictor.predictPurchasePrice(mock(PurchasesDepartment.class));
        //0.8527228*price - 6.596947
        //notice here that there is a LOT of difference between the R results and my results.
        // The regression are only slightly off, but unfortunately the division amplifies the discrepancy
        Assert.assertEquals(0.8527228,predictor.extractSlopeOfDemandFromRegression(),.025);
        Assert.assertEquals(-6.596947,predictor.extractInterceptOfDemandFromRegression(),.5);

        PurchasesDepartment department = mock(PurchasesDepartment.class);
        when(department.maxPrice(any(GoodType.class),any(Market.class))).thenReturn(100l);
        Assert.assertEquals(101,predictor.predictPurchasePrice(department));




    }





}
