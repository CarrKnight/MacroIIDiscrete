/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.sales.SalesDepartment;
import financial.Market;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.scheduler.Priority;
import org.junit.Assert;
import org.junit.Test;

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
 * @version 2013-07-11
 * @see
 */
public class RegressionSalePredictorTest {
    @Test
    public void testPredictSalePrice() throws Exception
    {

        Market market = mock(Market.class);
        RegressionSalePredictor predictor = new RegressionSalePredictor(market,mock(MacroII.class) );

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


        //this should regress to p=101.2 - 2.5 * q

        when(market.getYesterdayVolume()).thenReturn(8);
        //the sales predictor will be predict for 9 (yesterdayVolume + 1)
        Assert.assertEquals(predictor.predictSalePrice(mock(SalesDepartment.class),100l),79l);








    }


    @Test
    public void testPredictSalePriceWithLogs() throws Exception
    {

        Market market = mock(Market.class);
        MacroII model = new MacroII(1l);
        RegressionSalePredictor predictor = new RegressionSalePredictor(market,model );
        predictor.setQuantityTransformer(LearningFixedElasticitySalesPredictor.logTransformer);
        predictor.setPriceTransformer(LearningFixedElasticitySalesPredictor.logTransformer,
                LearningFixedElasticitySalesPredictor.expTransformer);

        //observation 1
        when(market.getYesterdayLastPrice()).thenReturn(86l);
        when(market.getYesterdayVolume()).thenReturn(6);
        predictor.step(model);
        //observation 2
        when(market.getYesterdayLastPrice()).thenReturn(84l);
        when(market.getYesterdayVolume()).thenReturn(7);
        predictor.step(model);
        //observation 3
        when(market.getYesterdayLastPrice()).thenReturn(81l);
        when(market.getYesterdayVolume()).thenReturn(8);
        predictor.step(model);


        //this should regress to log(p)=4.8275  -0.2068 * log(q)

        when(market.getYesterdayVolume()).thenReturn(8);
        //the sales predictor will be predict for 9 (yesterdayVolume + 1)
        Assert.assertEquals(predictor.predictSalePrice(mock(SalesDepartment.class),100l),79l);








    }


    @Test
    public void testScheduledProperly()
    {

        Market market = mock(Market.class);
        MacroII macroII = mock(MacroII.class);
        RegressionSalePredictor.defaultDailyProbabilityOfObserving = .2f;

        RegressionSalePredictor predictor = new RegressionSalePredictor(market, macroII);

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

        RegressionSalePredictor predictor = new RegressionSalePredictor(market, macroII);
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
