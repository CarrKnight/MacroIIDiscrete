/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.sales.SalesDepartment;

/**
 * <h4>Description</h4>
 * <p/> This prediction strategy makes it so that, whenever asked, the prediction is just the last price the department managed to sell its good for.
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
public class MemorySalesPredictor implements SalesPredictor {


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
        long lastPrice = dept.getLastClosingPrice();       //get the last closing price
         //do we not have anything in memory or did we screw up so badly
        //in the past term that we didn't sell a single item?
        if(lastPrice == -1)
            return -1;
        else
        {
            //return your memory.
            assert lastPrice >= 0 : lastPrice;

            return lastPrice;

        }
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
        long lastPrice = dept.getLastClosingPrice();       //get the last closing price
        //do we not have anything in memory or did we screw up so badly
        //in the past term that we didn't sell a single item?
        if(lastPrice == -1)
            return -1;
        else
        {
            //return your memory.
            assert lastPrice >= 0 : lastPrice;

            return lastPrice;

        }
    }

    /**
     * Call this to kill the predictor
     */
    @Override
    public void turnOff() {


    }
}
