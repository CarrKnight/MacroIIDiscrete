package agents.firm.sales.prediction;

import agents.firm.sales.SalesDepartment;
import financial.Market;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.scheduler.Priority;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
 * @version 2013-07-13
 * @see
 */
public class RegressionWeightedSalePredictorTest {


    @Test
    public void testPredictSalePrice() throws Exception
    {

        Market market = mock(Market.class);
        RegressionWeightedSalePredictor predictor = new RegressionWeightedSalePredictor(market,mock(MacroII.class) );

        //observation 1
        when(market.getYesterdayLastPrice()).thenReturn(86l);
        when(market.getYesterdayVolume()).thenReturn(6);
        predictor.step(mock(MacroII.class));
        //observation 2
        when(market.getYesterdayLastPrice()).thenReturn(84l);
        when(market.getYesterdayVolume()).thenReturn(7);
        predictor.step(mock(MacroII.class));
        //observation 3
        when(market.getYesterdayLastPrice()).thenReturn(81l);
        when(market.getYesterdayVolume()).thenReturn(8);
        predictor.step(mock(MacroII.class));


        //this should regress to p=101.9 -2.6 * q

        when(market.getYesterdayVolume()).thenReturn(8);
        predictor.updateModel();
        Assert.assertEquals(predictor.getSlope(),-2.6d,.01);
        Assert.assertEquals(predictor.getIntercept(),101.9d,.01);
        //the sales predictor will be predict for 9 (yesterdayVolume + 1)










    }


    @Test
    public void testScheduledProperly()
    {

        Market market = mock(Market.class);
        MacroII macroII = mock(MacroII.class);
        RegressionWeightedSalePredictor.defaultDailyProbabilityOfObserving = .2f;

        RegressionWeightedSalePredictor predictor = new RegressionWeightedSalePredictor(market, macroII);

        verify(macroII).scheduleAnotherDayWithFixedProbability(ActionOrder.DAWN,predictor,0.2f, Priority.AFTER_STANDARD);
        predictor.setDailyProbabilityOfObserving(.3f);
        predictor.step(macroII);
        verify(macroII).scheduleAnotherDayWithFixedProbability(ActionOrder.DAWN,predictor,0.3f, Priority.AFTER_STANDARD);


    }


    //Check defaults
    @Test
    public void testExtremes()
    {
        //no observations, should return whatever the sales department says
        Market market = mock(Market.class);
        MacroII macroII = mock(MacroII.class);
        SalesDepartment department = mock(SalesDepartment.class);

        RegressionWeightedSalePredictor predictor = new RegressionWeightedSalePredictor(market, macroII);
        when(department.hypotheticalSalePrice()).thenReturn(50l);
        Assert.assertEquals(predictor.predictSalePrice(department,1000l),50l);

        //with one observation, it still returns whatever the sales department says
        when(market.getYesterdayLastPrice()).thenReturn(10l);
        when(market.getYesterdayVolume()).thenReturn(1);
        predictor.step(mock(MacroII.class));
        Assert.assertEquals(predictor.predictSalePrice(department,1000l),50l);

        //with no volume the observation is ignored
        when(market.getYesterdayLastPrice()).thenReturn(10l);
        when(market.getYesterdayVolume()).thenReturn(0);
        predictor.step(mock(MacroII.class));
        Assert.assertEquals(predictor.predictSalePrice(department,1000l),50l);


        //two observations, everything back to normal!
        when(market.getYesterdayLastPrice()).thenReturn(10l);
        when(market.getYesterdayVolume()).thenReturn(1);
        predictor.step(mock(MacroII.class));
        Assert.assertEquals(predictor.predictSalePrice(department,1000l),10l);


    }
    
}
