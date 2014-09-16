package agents.firm.purchases.prediction;

import agents.firm.purchases.PurchasesDepartment;
import agents.firm.sales.prediction.SISOPredictorBase;
import model.utilities.stats.collectors.enums.PurchasesDataType;
import model.utilities.stats.regression.AutoRegressiveWithInputRegression;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AutoRegressivePurchasesPredictorUnitTest
{

    @Test
    public void testNotReady() throws Exception {

        SISOPredictorBase<PurchasesDataType,
                AutoRegressiveWithInputRegression> fakeBase = mock(SISOPredictorBase.class);
        PurchasesDepartment pd = mock(PurchasesDepartment.class);
        when(pd.getLastClosingPrice()).thenReturn(100);
        AutoRegressivePurchasesPredictor predictor = new AutoRegressivePurchasesPredictor(fakeBase,PurchasesDataType.WORKERS_CONSUMING_THIS_GOOD);

        //not ready should default to closing price:
        when(fakeBase.readyForPrediction()).thenReturn(false);
        Assert.assertEquals(predictor.predictPurchasePriceWhenIncreasingProduction(pd), 100, .0001);
        Assert.assertEquals(predictor.predictPurchasePriceWhenDecreasingProduction(pd), 100, .0001);
        Assert.assertEquals(predictor.predictPurchasePriceWhenNoChangeInProduction(pd),100,.0001);



    }


    @Test
    public void testBaseIsNan() throws Exception {

        SISOPredictorBase<PurchasesDataType,
                AutoRegressiveWithInputRegression> fakeBase = mock(SISOPredictorBase.class);
        PurchasesDepartment pd = mock(PurchasesDepartment.class);
        when(pd.getLastClosingPrice()).thenReturn(100);
        AutoRegressivePurchasesPredictor predictor = new AutoRegressivePurchasesPredictor(fakeBase,PurchasesDataType.WORKERS_CONSUMING_THIS_GOOD);

        //not ready should default to closing price:
        when(fakeBase.readyForPrediction()).thenReturn(true);
        when(fakeBase.predictYAfterChangingXBy(anyInt())).thenReturn(Float.NaN);
        Assert.assertEquals(predictor.predictPurchasePriceWhenIncreasingProduction(pd),100,.0001);
        Assert.assertEquals(predictor.predictPurchasePriceWhenDecreasingProduction(pd),100,.0001);
        Assert.assertEquals(predictor.predictPurchasePriceWhenNoChangeInProduction(pd),100,.0001);



    }


    @Test
    public void worksFine() throws Exception {

        SISOPredictorBase<PurchasesDataType,
                AutoRegressiveWithInputRegression> fakeBase = mock(SISOPredictorBase.class);
        PurchasesDepartment pd = mock(PurchasesDepartment.class);
        when(pd.getLastClosingPrice()).thenReturn(100);
        AutoRegressivePurchasesPredictor predictor = new AutoRegressivePurchasesPredictor(fakeBase,PurchasesDataType.WORKERS_CONSUMING_THIS_GOOD);

        //not ready should default to closing price:
        when(fakeBase.readyForPrediction()).thenReturn(true);
        when(fakeBase.predictYAfterChangingXBy(anyInt())).thenReturn(50f);
        Assert.assertEquals(predictor.predictPurchasePriceWhenIncreasingProduction(pd),50,.0001);
        Assert.assertEquals(predictor.predictPurchasePriceWhenDecreasingProduction(pd),50,.0001);
        Assert.assertEquals(predictor.predictPurchasePriceWhenNoChangeInProduction(pd),50,.0001);



    }



}