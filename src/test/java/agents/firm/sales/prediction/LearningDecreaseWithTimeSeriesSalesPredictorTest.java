/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.sales.SalesDepartment;
import au.com.bytecode.opencsv.CSVReader;
import com.google.common.primitives.Doubles;
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
public class LearningDecreaseWithTimeSeriesSalesPredictorTest {


    //read from csv and solve, please.
    @Test
    public void testing() throws IOException {

        //read data from file!
        CSVReader reader = new CSVReader(new FileReader("./src/test/regressionTestConsumption.csv"));
        reader.readNext(); //ignore the header!

        ArrayList<Double> prices = new ArrayList<>(900);
        ArrayList<Double> consumption = new ArrayList<>(900);


        String[] newLine;
        while(( newLine = reader.readNext()) != null)
        {

            consumption.add(Double.parseDouble(newLine[1]));
            prices.add(Double.parseDouble(newLine[2]));
        }


        //make data visible through the periodic observer
        PeriodicMarketObserver observer = mock(PeriodicMarketObserver.class);
        when(observer.getQuantitiesConsumedObservedAsArray()).thenReturn(Doubles.toArray(consumption));
        when(observer.getPricesObservedAsArray()).thenReturn(Doubles.toArray(prices));
        when(observer.getNumberOfObservations()).thenReturn(prices.size());

        //create the predictor
        LearningDecreaseWithTimeSeriesSalesPredictor predictor = new LearningDecreaseWithTimeSeriesSalesPredictor(observer);
        //force an update
        predictor.predictSalePrice(mock(SalesDepartment.class),1l);
        //0.8527228*price - 6.596947
        //notice here that there is a LOT of difference between the R results and my results.
        // The regression are only slightly off, but unfortunately the division amplifies the discrepancy
        Assert.assertEquals(-0.09629347, predictor.extractSlopeOfDemandFromRegression(), .025);
        Assert.assertEquals(18.02149,predictor.extractInterceptOfDemandFromRegression(),.5);

        SalesDepartment department = mock(SalesDepartment.class);
        when(department.hypotheticalSalePrice(anyLong())).thenReturn(100l);
        Assert.assertEquals(100,predictor.predictSalePrice(department,100));




    }

}
