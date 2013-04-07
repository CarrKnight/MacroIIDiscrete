/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.sales.SalesDepartment;
import financial.Market;
import goods.Good;

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
 * @version 2013-04-07
 * @see
 */
public class LookupSalesPredictor implements SalesPredictor {
    /**
     * This is called by the firm when it wants to predict the price they can sell to
     * (usually in order to guide production). <br>
     *
     * @param dept                   the sales department that has to answer this question
     * @param expectedProductionCost the HQ estimate of costs in producing whatever it wants to sell. It isn't necesarilly used.
     * @return the best offer available/predicted or -1 if there are no quotes/good predictions
     */
    @Override
    public long predictSalePrice(SalesDepartment dept, long expectedProductionCost) {
        Market market = dept.getMarket();
        if(market.isBestBuyPriceVisible())
            try {
                long buyerPrice =  market.getBestBuyPrice();  //ask the market for best price
                Good hypotheticalGood = mock(Good.class);
                when(hypotheticalGood.getCostOfProduction()).thenReturn(expectedProductionCost);
                when(hypotheticalGood.getLastValidPrice()).thenReturn(expectedProductionCost);
                long sellerPrice = dept.price(hypotheticalGood);
                if(buyerPrice >= sellerPrice)
                    return market.getPricePolicy().price(sellerPrice,buyerPrice);
                else
                    return buyerPrice;


            } catch (IllegalAccessException e) {
                //this should really never happen;
                assert false;
                return dept.getLastClosingPrice();
            }
        else
            return dept.getLastClosingPrice();
    }

    /**
     * Call this to kill the predictor
     */
    @Override
    public void turnOff() {
        throw new RuntimeException("not implemented yet!");
    }
}
