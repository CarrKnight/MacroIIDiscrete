/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package tests.purchase;

import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.prediction.PricingPurchasesPredictor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
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
 * @author Ernesto
 * @version 2012-10-04
 * @see
 */
public class PricingPurchasesPredictorTest {


    @Test
    public void mockTest() throws Exception {
        //simple mock test
        PurchasesDepartment dept = mock(PurchasesDepartment.class);
        PricingPurchasesPredictor predictor = new PricingPurchasesPredictor();

        for(int i=0; i<100; i++){
            when(dept.getAveragedClosingPrice()).thenReturn(Float.valueOf(i));
            assertEquals(predictor.predictPurchasePriceWhenIncreasingProduction(dept), i,.0001f);
        }





    }

}
