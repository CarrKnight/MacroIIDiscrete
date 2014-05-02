/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.sales.SalesDepartment;

/**
 * <h4>Description</h4>
 * <p/> This prediction strategy makes it so that, whenever asked, the prediction is just the average last price the department managed to sell its good for.
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-07-27
 * @see
 */
public class MemorySalesPredictor extends BaseSalesPredictor {


    /**
     * the prediction is just the last price the department managed to sell its good for.
     *
     *
     * @param dept the sales department that has to answer this question
     * @param expectedProductionCost the HQ estimate of costs in producing whatever it wants to sell. It isn't necesarilly used.
     * @param increaseStep  ignored
     * @return the best offer available/predicted or -1 if there are no quotes/good predictions
     */
    @Override
    public long predictSalePriceAfterIncreasingProduction(SalesDepartment dept, long expectedProductionCost, int increaseStep) {
        return memoryLookup(dept);
    }

    private long memoryLookup(SalesDepartment dept) {
        if(Double.isNaN(dept.getAveragedLastPrice()) || dept.getAveragedLastPrice() < 0)
        {
            return -1;
        }
        //do we not have anything in memory or did we screw up so badly
        //in the past term that we didn't sell a single item?

        return Math.round(dept.getAveragedLastPrice());
    }

    /**
     * This is called by the firm when it wants to predict the price they can sell to if they increase production
     *
     *
     * @param dept                   the sales department that has to answer this question
     * @param expectedProductionCost the HQ estimate of costs in producing whatever it wants to sell. It isn't necesarilly used.
     * @param decreaseStep ignored
     * @return the best offer available/predicted or -1 if there are no quotes/good predictions
     */
    @Override
    public long predictSalePriceAfterDecreasingProduction(SalesDepartment dept, long expectedProductionCost, int decreaseStep) {
        return memoryLookup(dept);

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
        return memoryLookup(dept);


    }



}
