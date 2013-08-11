/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.sales.SalesDepartment;
import goods.GoodType;
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
        when(department.hypotheticalSalePrice(anyLong())).thenReturn(100l); //current department pricing 100$
        when(department.getGoodType()).thenReturn(GoodType.GENERIC); //type of good produced



        FixedDecreaseSalesPredictor predictor = new FixedDecreaseSalesPredictor();

        //predicts a price 3 dollar lower
        predictor.setDecrementDelta(3);
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(department, 2, 1), 97l);

        //predicts a price 50 dollars lower
        predictor.setDecrementDelta(50);
        Assert.assertEquals(predictor.predictSalePriceAfterIncreasingProduction(department, 2, 1),50l);





    }


}
