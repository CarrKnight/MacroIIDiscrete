package model.utilities.stats.regression;

import javafx.util.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.util.function.Function;

import static org.mockito.Mockito.*;

public class MultipleModelRegressionWithSwitchingTest
{

    @Test
    public void makeSureItInstantiatesCorrectlyHeterogeneousModels() throws Exception
    {

        Function<Integer, SISORegression> fakeBuilder1 = mock(Function.class);
        Function<Integer, SISORegression> fakeBuilder2 = mock(Function.class);

        MultipleModelRegressionWithSwitching switcher = new MultipleModelRegressionWithSwitching(
                new Pair<>(fakeBuilder1,new Integer[]{1,2,3}) ,
                new Pair<>(fakeBuilder2,new Integer[]{4,5,6}));


        //make sure the builder got called the right amount of times
        verify(fakeBuilder1,times(1)).apply(1);
        verify(fakeBuilder1,times(1)).apply(2);
        verify(fakeBuilder1,times(1)).apply(3);
        verify(fakeBuilder1,never()).apply(4);
        verify(fakeBuilder1,never()).apply(5);
        verify(fakeBuilder1,never()).apply(6);

        verify(fakeBuilder2,never()).apply(1);
        verify(fakeBuilder2,never()).apply(2);
        verify(fakeBuilder2,never()).apply(3);
        verify(fakeBuilder2,times(1)).apply(4);
        verify(fakeBuilder2,times(1)).apply(5);
        verify(fakeBuilder2,times(1)).apply(6);

        Assert.assertEquals(switcher.getNumberOfModels(),6);

    }

    @Test
    public void instantiatesCorrectly() throws Exception
    {

        Function<Integer, SISORegression> fakeBuilder1 = mock(Function.class);

        MultipleModelRegressionWithSwitching switcher = new MultipleModelRegressionWithSwitching(
                fakeBuilder1,1,2,3);


        //make sure the builder got called the right amount of times
        verify(fakeBuilder1,times(1)).apply(1);
        verify(fakeBuilder1,times(1)).apply(2);
        verify(fakeBuilder1,times(1)).apply(3);
        verify(fakeBuilder1,never()).apply(4);
        verify(fakeBuilder1,never()).apply(5);
        verify(fakeBuilder1,never()).apply(6);

        Assert.assertEquals(switcher.getNumberOfModels(),3);

    }


    @Test
    public void getsTheRightModel() throws Exception {

        //builder creates 2 regressions, one is right, the other is wrong
        Function<Integer, SISORegression> fakeBuilder = mock(Function.class);
        final SISORegression wrongRegression = mock(SISORegression.class);
        final SISORegression correctRegression = mock(SISORegression.class);
        when(fakeBuilder.apply(1)).thenReturn(wrongRegression);
        when(fakeBuilder.apply(2)).thenReturn(correctRegression);

        MultipleModelRegressionWithSwitching switcher = new MultipleModelRegressionWithSwitching(fakeBuilder,1,2);
        switcher.setExcludeLinearFallback(true); //don't do fallback
        switcher.setHowManyObservationsBeforeModelSelection(10); //ignore the first 10 observations!
        //for the first 10, ignored, observations wrong regression is much better
        for(int i=0; i<10; i++)
        {
            when(wrongRegression.predictNextOutput(anyInt(),any())).thenReturn(100d); //wrong predicts 100
            when(correctRegression.predictNextOutput(anyInt(),any())).thenReturn(0d); //correct predicts 0

            switcher.addObservation(100,i);

            //also make sure they were asked to learn
            verify(wrongRegression,times(1)).addObservation(100,i);
            verify(correctRegression,times(1)).addObservation(100, i);
            //it shouldn't have predicted since these observations are ignored
            verify(wrongRegression, never()).predictNextOutput(anyDouble(), any());
            verify(correctRegression, never()).predictNextOutput(anyDouble(), any());
        }

        //add once more, this time correct is better
        when(wrongRegression.predictNextOutput(anyDouble())).thenReturn(100d); //wrong predicts 100
        when(correctRegression.predictNextOutput(anyDouble())).thenReturn(101d);
        switcher.addObservation(101, 0);
        //should have asked us to learn AND predict (to get the error)
        verify(wrongRegression,times(1)).addObservation(101,0);
        verify(correctRegression,times(1)).addObservation(101,0);
        //it shouldn't have predicted since these observations are ignored
        verify(wrongRegression,times(1)).predictNextOutput(0);
        verify(correctRegression,times(1)).predictNextOutput(0);

        Assert.assertEquals(switcher.predictNextOutput(999,999),101d,.0001d); //calls the correct regression for predictor


    }


    @Test
    public void skipsNaNs() throws Exception {

        Function<Integer, SISORegression> fakeBuilder = mock(Function.class);
        final SISORegression regression = mock(SISORegression.class);
        when(fakeBuilder.apply(1)).thenReturn(regression);
        MultipleModelRegressionWithSwitching switcher = new MultipleModelRegressionWithSwitching(fakeBuilder,1);

        //NANS ARE NOT GOOD
        boolean exceptionThrown = false;
        try {
            switcher.addObservation(Double.NaN, 1);
        }catch (Exception e){ exceptionThrown = true; }
        Assert.assertTrue(exceptionThrown);
        exceptionThrown = false;
        try {
            switcher.addObservation(1,Double.NaN);
        }catch (Exception e){ exceptionThrown = true; }
        Assert.assertTrue(exceptionThrown);

        verify(regression,never()).addObservation(anyDouble(),anyDouble(),any());
        verify(regression,never()).addObservation(anyDouble(),anyDouble());
        verify(regression,never()).skipObservation(anyDouble(),anyDouble());
        verify(regression,never()).skipObservation(anyDouble(),anyDouble(),any());

        //INFINITES ARE NOT GOOD
        try {
            switcher.addObservation(Double.NEGATIVE_INFINITY, 1);
        }catch (Exception e){ exceptionThrown = true; }
        Assert.assertTrue(exceptionThrown);
        exceptionThrown = false;
        try {
            switcher.addObservation(1,Double.POSITIVE_INFINITY);
        }catch (Exception e){ exceptionThrown = true; }
        Assert.assertTrue(exceptionThrown);
        verify(regression,never()).addObservation(anyDouble(),anyDouble(),any());
        verify(regression,never()).addObservation(anyDouble(),anyDouble());
        verify(regression,never()).skipObservation(anyDouble(),anyDouble());
        verify(regression,never()).skipObservation(anyDouble(),anyDouble(),any());

        switcher.addObservation(1,1);
        verify(regression,times(1)).addObservation(1,1);

    }

    @Test
    public void linearFallbackFTW()
    {

        //we are going to pass an easy line and make the mock regression fail.
        //This test became necessary when i embarrassingly noticed i fucked up the getter. The getters, man, come on.

        Function<Integer, SISORegression> fakeBuilder = mock(Function.class);
        final SISORegression regression = mock(SISORegression.class);
        when(fakeBuilder.apply(1)).thenReturn(regression);
        when(regression.predictNextOutput(anyDouble(),anyDouble())).thenReturn(99999d); //sucks at predicting
        MultipleModelRegressionWithSwitching switcher = new MultipleModelRegressionWithSwitching(fakeBuilder,1);
        switcher.setHowManyObservationsBeforeModelSelection(50);
        switcher.setExcludeLinearFallback(false);

        for(int i=0; i<100; i++)
        {
            switcher.addObservation(i,i); //45 degree line
        }
        //linear fallback should win this easy
        Assert.assertTrue(switcher.isFallbackBetter());

        //make sure getGains and company refer to the linear fallback
        when(regression.getGain()).thenReturn(1000d);
        when(regression.getIntercept()).thenReturn(1000d);
        when(regression.getDelay()).thenReturn(1000);
        when(regression.getTimeConstant()).thenReturn(1000d);

        //the regression parameters should come from the linear regression
        Assert.assertEquals(switcher.getGain(),1,.001d);
        Assert.assertEquals(switcher.getIntercept(),0,.001d);
        Assert.assertEquals(switcher.getTimeConstant(),0,.001d);
        Assert.assertEquals(switcher.getDelay(),0);




    }
}