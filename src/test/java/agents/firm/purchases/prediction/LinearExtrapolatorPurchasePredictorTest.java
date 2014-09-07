/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.prediction;

import agents.firm.purchases.PurchasesDepartment;
import au.com.bytecode.opencsv.CSVReader;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.stats.collectors.PurchasesDepartmentData;
import model.utilities.stats.collectors.enums.PurchasesDataType;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
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
 * @version 2013-08-20
 * @see
 */
public class LinearExtrapolatorPurchasePredictorTest
{

    @Test
    public void rescheduleTest()
    {
        PurchasesDepartment department = mock(PurchasesDepartment.class);
        MacroII macroII = mock(MacroII.class);
        LinearExtrapolatorPurchasePredictor predictor = new LinearExtrapolatorPurchasePredictor(department, macroII);
        //should have rescheduled itself
        verify(macroII).scheduleSoon(ActionOrder.THINK,predictor);
        predictor.step(macroII);
        verify(macroII,times(1)).scheduleTomorrow(ActionOrder.THINK, predictor);
        //turn it off, it should have stopped!
        predictor.turnOff();
        predictor.step(macroII);
        verify(macroII,times(1)).scheduleTomorrow(ActionOrder.THINK,predictor);


    }

    @Test
    public void nodataTest()
    {
        //shouldn't set a slope  because there are never enough observations as every day is a shock
        PurchasesDepartment department = mock(PurchasesDepartment.class);
        MacroII macroII = mock(MacroII.class);  when(macroII.getCurrentPhase()).thenReturn(ActionOrder.CLEANUP_DATA_GATHERING);



        //where we store the data:
        final PurchasesDepartmentData data = new PurchasesDepartmentData();
        data.start(macroII,department);
        //what we are testing
        LinearExtrapolatorPurchasePredictor predictor =
                new LinearExtrapolatorPurchasePredictor(department, macroII);



        for(int day =0; day<200; day++)
        {
            //fake 200 days of observations
            when(department.getNumberOfWorkersWhoConsumeWhatWePurchase()).thenReturn(day);
            when(department.getAveragedClosingPrice()).thenReturn(Float.valueOf(day));
            when(macroII.getMainScheduleTime()).thenReturn(Double.valueOf(day));

            data.step(macroII);
            predictor.step(macroII);
        }

        //now delegate all department data to the data object
        when(department.getLastObservedDay()).thenReturn(data.getLastObservedDay());
        when(department.getObservationRecordedThisDay(any(PurchasesDataType.class),anyInt())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return data.getObservationRecordedThisDay((PurchasesDataType)invocation.getArguments()[0],
                        (Integer)invocation.getArguments()[1]);

            }
        });

        when(department.getObservationsRecordedTheseDays(any(PurchasesDataType.class),anyInt(),anyInt())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return data.getObservationsRecordedTheseDays((PurchasesDataType)invocation.getArguments()[0],
                        (Integer)invocation.getArguments()[1],(Integer)invocation.getArguments()[2]);

            }
        });




        //p2=32.5904 p1=32
        //delta workers -1!
        when(department.getAveragedClosingPrice()).thenReturn(100f);
        Assert.assertEquals(100,predictor.predictPurchasePriceWhenIncreasingProduction(department),.0001f); //this is the reverse of what we expect when estimating supply shocks, but that's because the data is pure bogus
        Assert.assertEquals(100, predictor.predictPurchasePriceWhenDecreasingProduction(department),.0001f); //this is the reverse of what we expect when estimating supply shocks, but that's because the data is pure bogus

        Assert.assertEquals(0,predictor.getCurrentSlope(),.001f); //this is the reverse of what we expect when estimating supply shocks, but that's because the data is pure bogus






    }


    @Test
    public void simpleTest() throws IOException {

        PurchasesDepartment department = mock(PurchasesDepartment.class);
        MacroII macroII = mock(MacroII.class);  when(macroII.getCurrentPhase()).thenReturn(ActionOrder.CLEANUP_DATA_GATHERING);


        //read data from file!
        CSVReader reader = new CSVReader(new FileReader(Paths.get("testresources", "src/test/resources/EMAtest.csv").toFile()));
        reader.readNext(); //ignore the header!

        ArrayList<Float> prices = new ArrayList<>(200);
        ArrayList<Integer> workers = new ArrayList<>(200);
        String[] newLine;
        while(( newLine = reader.readNext()) != null)
        {

            workers.add(Integer.parseInt(newLine[1]));
            prices.add(Float.parseFloat(newLine[2]));
        }

        reader.close();


        //where we store the data:
        final PurchasesDepartmentData data = new PurchasesDepartmentData();
        data.start(macroII,department);
        //what we are testing
        LinearExtrapolatorPurchasePredictor predictor =
                new LinearExtrapolatorPurchasePredictor(department, macroII);
        predictor.setWeight(.2f);
        predictor.setMaximumNumberOfDaysToLookAhead(150);
        predictor.setHowManyDaysBackShallILook(10);



        for(int day =0; day<200; day++)
        {
            //fake 200 days of observations
            when(department.getNumberOfWorkersWhoConsumeWhatWePurchase()).thenReturn(workers.get(day));
            when(department.getLastClosingPrice()).thenReturn(prices.get(day).intValue());
            when(macroII.getMainScheduleTime()).thenReturn(Double.valueOf(day));

            data.step(macroII);
            predictor.step(macroII);
        }

        //now delegate all department data to the data object
        when(department.getLastObservedDay()).thenReturn(data.getLastObservedDay());
        when(department.getObservationRecordedThisDay(any(PurchasesDataType.class),anyInt())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return data.getObservationRecordedThisDay((PurchasesDataType)invocation.getArguments()[0],
                        (Integer)invocation.getArguments()[1]);

            }
        });

        when(department.getObservationsRecordedTheseDays(any(PurchasesDataType.class),anyInt(),anyInt())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return data.getObservationsRecordedTheseDays((PurchasesDataType)invocation.getArguments()[0],
                        (Integer)invocation.getArguments()[1],(Integer)invocation.getArguments()[2]);

            }
        });




        //p2=32.5904 p1=32
        //delta workers -1!
        when(department.getAveragedClosingPrice()).thenReturn(100f);
        Assert.assertEquals(100-0.360,predictor.predictPurchasePriceWhenIncreasingProduction(department),.0001f); //this is the reverse of what we expect when estimating supply shocks, but that's because the data is pure bogus
        Assert.assertEquals(100+0.360, predictor.predictPurchasePriceWhenDecreasingProduction(department),.0001f); //this is the reverse of what we expect when estimating supply shocks, but that's because the data is pure bogus

        Assert.assertEquals(-0.360,predictor.getCurrentSlope(),.001f); //this is the reverse of what we expect when estimating supply shocks, but that's because the data is pure bogus





    }


}
