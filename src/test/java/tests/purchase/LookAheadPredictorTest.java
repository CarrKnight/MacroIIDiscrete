/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package tests.purchase;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.prediction.LookAheadPredictor;
import financial.market.Market;
import financial.market.OrderBookMarket;
import financial.utilities.Quote;
import goods.Good;
import goods.GoodType;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
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
public class LookAheadPredictorTest {




    @Test
    public void properTest() throws Exception {

        //this is exactly the same test as the survey test. This is because lookAhead reverts to survey if the market doesn't allow for the book to be visible
        MacroII model = new MacroII(1);
        Market market = new OrderBookMarket(UndifferentiatedGoodType.GENERIC);
        Firm buyer = mock(Firm.class); market.registerBuyer(buyer);   when(buyer.getModel()).thenReturn(model);
        LookAheadPredictor predictor = new LookAheadPredictor();

        PurchasesDepartment dept = mock(PurchasesDepartment.class); when(dept.getMarket()).thenReturn(market);
        assertEquals(predictor.predictPurchasePriceWhenIncreasingProduction(dept), -1); //there is no price!


        for(int i=0; i<5; i++)
        {
            Firm seller = mock(Firm.class); when(seller.has(any(Good.class))).thenReturn(true);
            market.registerSeller(seller);
            market.submitSellQuote(seller,10+i*10,mock(Good.class));

        }



        assertEquals(predictor.predictPurchasePriceWhenIncreasingProduction(dept), 10);

        Firm seller = mock(Firm.class);
        Good stubGood = mock(Good.class); when(stubGood.getType()).thenReturn(UndifferentiatedGoodType.GENERIC);   Quote q = Quote.newSellerQuote(seller, 1, stubGood);
        when(seller.askedForASaleQuote(any(EconomicAgent.class), any(GoodType.class))).thenReturn(q);
        market.registerSeller(seller);                              //he didn't submit any quote yet
        assertEquals(predictor.predictPurchasePriceWhenIncreasingProduction(dept), 10);
        market.submitSellQuote(seller, 1, mock(Good.class));
        assertEquals(predictor.predictPurchasePriceWhenIncreasingProduction(dept), 1);

    }
}
