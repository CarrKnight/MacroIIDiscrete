/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.prediction;

import agents.firm.purchases.PurchasesDepartment;
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
 * @version 2013-08-04
 * @see
 */
public class FixedIncreasePurchasesPredictorTest
{

    @Test
    public void testPredictSalePrice() throws Exception {

        PurchasesDepartment department = mock(PurchasesDepartment.class);
        when(department.getAveragedClosingPrice()).thenReturn(100f); //current department pricing 100$!
        when(department.getGoodType()).thenReturn(UndifferentiatedGoodType.GENERIC); //type of good produced



        FixedIncreasePurchasesPredictor predictor = new FixedIncreasePurchasesPredictor();

        //predicts a price 3 dollar higher
        predictor.setIncrementDelta(3);
        Assert.assertEquals(predictor.predictPurchasePriceWhenIncreasingProduction(department), 103,.0001f);

        //predicts a price 50 dollars higher
        predictor.setIncrementDelta(50);
        Assert.assertEquals(predictor.predictPurchasePriceWhenIncreasingProduction(department),150,.0001f);





    }

}
