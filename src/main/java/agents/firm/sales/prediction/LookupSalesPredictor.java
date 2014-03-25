/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.sales.SalesDepartment;
import financial.market.Market;
import model.utilities.stats.collectors.enums.MarketDataType;

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
     *
     * @param dept                   the sales department that has to answer this question
     * @param expectedProductionCost the HQ estimate of costs in producing whatever it wants to sell. It isn't necesarilly used.
     * @param increaseStep ignored
     * @return the best offer available/predicted or -1 if there are no quotes/good predictions
     */
    @Override
    public long predictSalePriceAfterIncreasingProduction(SalesDepartment dept, long expectedProductionCost, int increaseStep) {

        return Math.round(dept.getMarket().getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE));

    }

    /**
     * defaults to the last closing price.
     *
     * @param dept the sales department that has to answer this question
     * @param expectedProductionCost the HQ estimate of costs in producing whatever it wants to sell. It isn't necesarilly used.
     * @param decreaseStep ignored
     * @return
     */
    @Override
    public long predictSalePriceAfterDecreasingProduction(SalesDepartment dept, long expectedProductionCost, int decreaseStep) {

        return Math.round(dept.getMarket().getLastDaysAveragePrice());
    }

    private long getBuyerPrice(Market market) throws IllegalAccessException {
        long price = market.getBestBuyPrice();
        if(price >=0)
            return price;

        //if the price is -1, there is no quote
        assert price == -1 ;

        return market.getLastFilledBid();




    }

    /**
     * Call this to kill the predictor
     */
    @Override
    public void turnOff() {

    }

    /**
     * This is a little bit weird to predict, but basically you want to know what will be "tomorrow" price if you don't change production.
     * Most predictors simply return today closing price, because maybe this will be useful in some cases. It's used by Marginal Maximizer Statics
     *
     * @param dept the sales department
     * @return predicted price
     */
    @Override
    public long predictSalePriceWhenNotChangingProduction(SalesDepartment dept) {
        return Math.round(dept.getMarket().getLastDaysAveragePrice());

    }
}
