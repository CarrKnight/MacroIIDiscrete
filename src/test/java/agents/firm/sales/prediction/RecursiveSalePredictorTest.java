/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.sales.SalesDepartment;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.stats.collectors.SalesData;
import model.utilities.stats.collectors.enums.SalesDataType;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

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
 * @version 2013-11-11
 * @see
 */
public class RecursiveSalePredictorTest {



    @Test
    public void testSimulatePriceStatic()
    {

        //feed data to run against
        SalesDepartment department = mock(SalesDepartment.class);
        MacroII model = mock(MacroII.class);
        when(model.getCurrentPhase()).thenReturn(ActionOrder.CLEANUP_DATA_GATHERING);
        when(model.getMainScheduleTime()).thenReturn(-1d);
        SalesData data = new SalesData();


        data.start(model,department);
        //put in price data
        when(department.getAverageClosingPrice()).thenReturn(5f,4f,3f,2f,1f);
        when(department.getTodayOutflow()).thenReturn(5,4,3,2,1);
        when(model.getMainScheduleTime()).thenReturn(0d);
        for(int i=0; i<5; i++) //should be well fed now!
            data.step(model);

        double[] beta = new double[7];
        Arrays.fill(beta,1);

        when(model.getMainScheduleTime()).thenReturn(5d);

        //without time delay
        Assert.assertEquals(42, RecursiveSalePredictor.simulateFuturePrice(data, model, 3, 3, 0, SalesDataType.OUTFLOW,
                SalesDataType.AVERAGE_CLOSING_PRICES, 3,beta,1 ),.0001);
        //with time delay
        Assert.assertEquals(49, RecursiveSalePredictor.simulateFuturePrice(data, model, 3, 3, 1, SalesDataType.OUTFLOW,
                SalesDataType.AVERAGE_CLOSING_PRICES, 3,beta,1 ),.0001);



    }

    @Test
    public void testSimulatePrice()
    {

        //feed data to run against
        SalesDepartment department = mock(SalesDepartment.class);
        MacroII model = mock(MacroII.class);
        when(model.getCurrentPhase()).thenReturn(ActionOrder.CLEANUP_DATA_GATHERING);
        when(model.getMainScheduleTime()).thenReturn(-1d);
        SalesData data = new SalesData();


        data.start(model,department);
        //put in price data
        when(department.getLastClosingPrice()).thenReturn(5l,4l,3l,2l,1l);
        when(department.getLastAskedPrice()).thenReturn(5l,4l,3l,2l,1l);
        when(department.getTodayOutflow()).thenReturn(5,4,3,2,1);
        RecursiveSalePredictor predictor = new RecursiveSalePredictor(model,department,new double[]{0,1,0,0,0,0,0},3,3,1);
        predictor.setTimeDelay(1);
        predictor.setRegressingOnWorkers(false);
        when(department.getData()).thenReturn(data);
        for(int i=0; i<5; i++) //should be well fed now!
        {
            when(model.getMainScheduleTime()).thenReturn(Double.valueOf(i));
            data.step(model);
            when(model.getMainScheduleTime()).thenReturn(Double.valueOf(i+1));
            predictor.step(model);
        }


        Assert.assertEquals(predictor.getNumberOfValidObservations(),2);
        //forcefully put a nice beta in
        for(int i=0; i < 7; i++)
            predictor.getRegression().setBeta(i,1);

        predictor.setInitialOpenLoopLearningTime(0);
        predictor.setHowFarIntoTheFutureToPredict(3);
        //with time delay
        Assert.assertEquals(49,predictor.predictPrice(1),.001d);
        //without time delay
        predictor.setTimeDelay(0);
        Assert.assertEquals(42,predictor.predictPrice(1),.001d);



    }
}
