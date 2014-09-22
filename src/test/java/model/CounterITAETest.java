package model;

import model.utilities.pid.CounterITAE;
import model.utilities.stats.collectors.SalesData;
import model.utilities.stats.collectors.enums.SalesDataType;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class CounterITAETest
{


    @Test
    public void countsCorrectly() throws Exception {

        SalesData data = mock(SalesData.class);
        SalesDataType output = SalesDataType.CLOSING_PRICES;

        when(data.getLatestObservation(output)).thenReturn(10d);
        when(data.numberOfObservations()).thenReturn(1);

        CounterITAE<SalesDataType> counter = new CounterITAE<>(data,output);

        Assert.assertEquals(0,counter.getITAE(),.0001);

        //10*1 + 10*2
        counter.collectData();
        counter.collectData();
        Assert.assertEquals(10+20,counter.getITAE(),.0001);
        counter.reset();
        Assert.assertEquals(0,counter.getITAE(),.0001);

        //do it again being wrong on the other side
        when(data.getLatestObservation(output)).thenReturn(20d);
        counter.setTarget(10);
        counter.collectData();
        counter.collectData();
        Assert.assertEquals(10+20,counter.getITAE(),.0001);




    }


    @Test
    public void skipsCorrectly(){


        SalesData data = mock(SalesData.class);
        SalesDataType output = SalesDataType.CLOSING_PRICES;
        MacroII model = mock(MacroII.class);


        when(data.getLatestObservation(output)).thenReturn(0d);
        when(data.numberOfObservations()).thenReturn(1);

        CounterITAE<SalesDataType> counter = new CounterITAE<>(data,output);

        //make it skip (because.)
        counter.setOutputValidator(salesDataType -> false);

        //step
        counter.collectData();
        //now the error should be
        Assert.assertEquals(CounterITAE.PENALTY_FOR_SKIP,counter.getITAE(),.0001d);

    }


}