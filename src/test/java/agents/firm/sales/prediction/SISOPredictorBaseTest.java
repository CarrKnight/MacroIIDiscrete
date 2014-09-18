package agents.firm.sales.prediction;

import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.stats.collectors.enums.SalesDataType;
import model.utilities.stats.processes.LinearNonDynamicProcess;
import model.utilities.stats.regression.SISORegression;
import org.junit.Assert;
import org.junit.Test;

import java.util.function.Consumer;

import static org.mockito.Mockito.*;

public class SISOPredictorBaseTest {


    @Test
    public void initializerStarts() throws Exception {
        MacroII model = mock(MacroII.class);
        RegressionDataCollector<SalesDataType> collector = mock(RegressionDataCollector.class);
        SISORegression regression = mock(SISORegression.class);
        Consumer<SISORegression> initializer = mock(Consumer.class);

        new SISOPredictorBase(model,collector,regression,initializer);

        verify(initializer,times(1)).accept(regression);

    }

    @Test
    public void schedulesCorrectly()
    {

        MacroII model = mock(MacroII.class);
        RegressionDataCollector<SalesDataType> collector = mock(RegressionDataCollector.class);
        SISORegression regression = mock(SISORegression.class);

        SISOPredictorBase base = new SISOPredictorBase(model,
                collector,regression);

        //should have scheduled itself
        verify(model,times(1)).scheduleSoon(ActionOrder.PREPARE_TO_TRADE,base);


        base.step(model);
        //should have scheduled itself!
        verify(model,times(1)).scheduleTomorrow(ActionOrder.PREPARE_TO_TRADE,base);

        //stop scheduling yourself
        base.turnOff();
        model = mock(MacroII.class);
        base.step(model);
        verify(model, never()).scheduleTomorrow(any(),any());


    }

    @Test
    public void collectsCorrectly(){

        MacroII model = mock(MacroII.class);
        RegressionDataCollector<SalesDataType> collector = mock(RegressionDataCollector.class);
        SISORegression regression = mock(SISORegression.class);

        SISOPredictorBase base = new SISOPredictorBase(model,
                collector,regression);

        base.step(model);
        verify(collector).collect();

    }

    @Test
    public void addsObservations()
    {

        MacroII model = mock(MacroII.class);
        RegressionDataCollector<SalesDataType> collector = mock(RegressionDataCollector.class);
        SISORegression regression = mock(SISORegression.class);

        SISOPredictorBase base = new SISOPredictorBase(model,
                collector,regression);
        base.setMaximumGap(10);

        //invalid observation
        when(collector.isLatestObservationValid()).thenReturn(false);
        base.step(model);
        verify(regression,never()).addObservation(anyDouble(),anyDouble());

        //now gap is too much
        when(collector.isLatestObservationValid()).thenReturn(true);
        when(collector.isLastYValid()).thenReturn(true);
        when(collector.getLastObservedGap()).thenReturn(1000d);
        base.step(model);
        verify(regression, never()).addObservation(anyDouble(), anyDouble());

        //now is fine
        when(collector.getLastObservedGap()).thenReturn(0d);
        base.step(model);
        verify(regression,times(1)).addObservation(anyDouble(), anyDouble());
    }

    @Test
    public void predictOnlyWhenReady() throws Exception {
        MacroII model = mock(MacroII.class);
        RegressionDataCollector<SalesDataType> collector = mock(RegressionDataCollector.class);
        SISORegression regression = mock(SISORegression.class);
        final LinearNonDynamicProcess process =
                new LinearNonDynamicProcess(0,100,0);
        when(regression.
                generateDynamicProcessImpliedByRegression()).
                thenReturn(process);

        SISOPredictorBase base = new SISOPredictorBase(model,
                collector,regression);
        base.setBurnOut(100);
        when(regression.getNumberOfObservations()).thenReturn(99);
        //will return NaN
        Assert.assertTrue(Float.isNaN(base.predictYAfterChangingXBy(1)));


        //will return a prediction
        when(regression.getNumberOfObservations()).thenReturn(100);
        when(collector.isLastXValid()).thenReturn(true);
        Assert.assertEquals(100,base.predictYAfterChangingXBy(5),.0001);
        verify(regression).generateDynamicProcessImpliedByRegression();

        //will not return a prediction if last x is not valid
        when(collector.isLastXValid()).thenReturn(false);
        Assert.assertTrue(Float.isNaN(base.predictYAfterChangingXBy(1)));


    }
}