/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.sales.SalesDepartment;
import goods.UndifferentiatedGoodType;
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
 * @version 2013-07-01
 * @see
 */
public class FixedDecreaseSalesPredictorTest {
    @Test
    public void testPredictSalePrice() throws Exception {

        SalesDepartment department = mock(SalesDepartment.class);
        when(department.getAveragedLastPrice()).thenReturn(100d); //current department pricing 100$
        when(department.getGoodType()).thenReturn(UndifferentiatedGoodType.GENERIC); //type of good produced



        FixedDecreaseSalesPredictor predictor = new FixedDecreaseSalesPredictor();

        //predicts a price 3 dollar lower
        predictor.setDecrementDelta(3);
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(department, 2, 1), 97);

        //predicts a price 50 dollars lower
        predictor.setDecrementDelta(50);
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(department, 2, 1),50);





    }


}
