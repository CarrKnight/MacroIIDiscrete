package agents.firm.sales.prediction;

import agents.firm.sales.SalesDepartment;
import model.utilities.stats.collectors.enums.SalesDataType;
import model.utilities.stats.regression.AutoRegressiveWithInputRegression;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AutoRegressiveSalesPredictorUnitTest {

    @Test
    public void testNotReady() throws Exception {

        SISOPredictorBase<SalesDataType,
                AutoRegressiveWithInputRegression> fakeBase = mock(SISOPredictorBase.class);
        SalesDepartment sd = mock(SalesDepartment.class);
        when(sd.getLastClosingPrice()).thenReturn(100);
        AutoRegressiveSalesPredictor predictor = new AutoRegressiveSalesPredictor(fakeBase);

        //not ready should default to closing price:
        when(fakeBase.readyForPrediction()).thenReturn(false);
        Assert.assertEquals(predictor.predictSalePriceAfterDecreasingProduction(sd,1234,5678), 100, .0001);
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(sd, 1234, 5678), 100, .0001);
        Assert.assertEquals(predictor.predictSalePriceWhenNotChangingProduction(sd),100,.0001);



    }


    @Test
    public void testBaseIsNan() throws Exception {

        SISOPredictorBase<SalesDataType,
                AutoRegressiveWithInputRegression> fakeBase = mock(SISOPredictorBase.class);
        SalesDepartment sd = mock(SalesDepartment.class);
        when(sd.getLastClosingPrice()).thenReturn(100);
        AutoRegressiveSalesPredictor predictor = new AutoRegressiveSalesPredictor(fakeBase);

        //not ready should default to closing price:
        when(fakeBase.readyForPrediction()).thenReturn(true);
        when(fakeBase.predictYAfterChangingXBy(anyInt())).thenReturn(Float.NaN);
        Assert.assertEquals(predictor.predictSalePriceAfterDecreasingProduction(sd, 1234, 5678), 100, .0001);
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(sd, 1234, 5678), 100, .0001);
        Assert.assertEquals(predictor.predictSalePriceWhenNotChangingProduction(sd),100,.0001);



    }


    @Test
    public void worksFine() throws Exception {

        SISOPredictorBase<SalesDataType,
                AutoRegressiveWithInputRegression> fakeBase = mock(SISOPredictorBase.class);
        SalesDepartment sd = mock(SalesDepartment.class);
        when(sd.getLastClosingPrice()).thenReturn(100);
        AutoRegressiveSalesPredictor predictor = new AutoRegressiveSalesPredictor(fakeBase);

        //not ready should default to closing price:
        when(fakeBase.readyForPrediction()).thenReturn(true);
        when(fakeBase.predictYAfterChangingXBy(anyInt())).thenReturn(50f);
        Assert.assertEquals(predictor.predictSalePriceAfterDecreasingProduction(sd,1234,5678), 50f, .0001);
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(sd,1234,5678), 50f, .0001);
        Assert.assertEquals(predictor.predictSalePriceWhenNotChangingProduction(sd),50f,.0001);


    }




}