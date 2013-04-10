/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.sales.SalesDepartment;
import financial.Market;
import financial.utilities.AveragePricePolicy;
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
                long buyerPrice = getBuyerPrice(market);
                //ask the market for best price (if there is none, assume old)

                Good hypotheticalGood = mock(Good.class);
                when(hypotheticalGood.getCostOfProduction()).thenReturn(expectedProductionCost);
                when(hypotheticalGood.getLastValidPrice()).thenReturn(expectedProductionCost);
                long sellerPrice = dept.price(hypotheticalGood);
                if(buyerPrice >= sellerPrice)
                {
                    //it's strange to be here, since they are buying at more than we are willing to sell, we should have sold!
                    //must be that we are out of stock or we don't want to sell or we have scheduled to update prices
                    assert !dept.hasAnythingToSell() || !dept.isInventoryAcceptable() ||
                            dept.isAboutToUpdateQuotes() || market.getBestBuyPrice() == -1  : sellerPrice + "-" + buyerPrice;
                    //take the average, it doesn't matter because it'll close itself
                    return Math.min(new AveragePricePolicy().price(sellerPrice,buyerPrice),(sellerPrice+1)*5);
                }
                else
                {
                    //more likely place, now if we have sold everything, we will have to lower the price to get it out
                    if(dept.isInventoryAcceptable())
                    {
                        return buyerPrice;
                    }
                    else //otherwise, if we have stock left, just trust your pricing strategy
                        return sellerPrice;
                }

            } catch (IllegalAccessException e) {
                //this should really never happen;
                assert false;
                return dept.getLastClosingPrice();
            }
        else
            return dept.getLastClosingPrice();
    }

    private long getBuyerPrice(Market market) throws IllegalAccessException {
        long price = market.getBestBuyPrice();
        if(price >=0)
            return price;

        //if the price is -1, there is no quote
        assert price == -1 ;

        System.out.println("lastFilledBid:" + market.getLastFilledBid());
        return market.getLastFilledBid();




    }

    /**
     * Call this to kill the predictor
     */
    @Override
    public void turnOff() {

    }
}
