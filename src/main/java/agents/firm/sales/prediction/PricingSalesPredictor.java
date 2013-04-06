/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.sales.SalesDepartment;
import goods.Good;

/**
 * <h4>Description</h4>
 * <p/> This class has a weird name but all it says is that it
 * uses the pricing strategy of the sales department as its predicted price (basically it doesn't
 * care about market conditions)
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
public class PricingSalesPredictor implements SalesPredictor {

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
        //imagine a good with the right conditions
        Good imaginaryGood =new Good(dept.getMarket().getGoodType(),dept.getFirm(),expectedProductionCost);
        //what would the pricing section do?
        return dept.price(imaginaryGood);
    }

    /**
     * Call this to kill the predictor
     */
    @Override
    public void turnOff() {


    }
}
