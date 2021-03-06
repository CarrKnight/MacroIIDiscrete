/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.prediction.PurchasesPredictor;
import agents.firm.sales.SalesDepartment;
import model.utilities.NonDrawable;

/**
 * <h4>Description</h4>
 * <p/> A very simple predictor that always predict the same price. Useful for testing.
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-01-19
 * @see
 */
@NonDrawable
public class FixedPriceFakePredictor extends BaseSalesPredictor implements PurchasesPredictor {


    private int priceToPredict;


    public FixedPriceFakePredictor(int priceToPredict) {
        this.priceToPredict = priceToPredict;
    }

    /**
     * This is called by the firm when it wants to predict the price they can sell to if they increase production
     *
     * @param dept                   the sales department that has to answer this question
     * @param expectedProductionCost the HQ estimate of costs in producing whatever it wants to sell. It isn't necesarilly used.
     * @param increaseStep           by how much the daily production will increase (has to be a positive number)
     * @return the best offer available/predicted or -1 if there are no quotes/good predictions
     */
    @Override
    public float predictSalePriceAfterIncreasingProduction(SalesDepartment dept, int expectedProductionCost, int increaseStep) {
        return priceToPredict;
    }

    /**
     * This is called by the firm when it wants to predict the price they can sell to if they increase production
     *
     * @param dept                   the sales department that has to answer this question
     * @param expectedProductionCost the HQ estimate of costs in producing whatever it wants to sell. It isn't necesarilly used.
     * @param decreaseStep           by how much the daily production will decrease (has to be a positive number)
     * @return the best offer available/predicted or -1 if there are no quotes/good predictions
     */
    @Override
    public float predictSalePriceAfterDecreasingProduction(SalesDepartment dept, int expectedProductionCost, int decreaseStep) {
        return priceToPredict;
    }

    /**
     * This is a little bit weird to predict, but basically you want to know what will be "tomorrow" price if you don't change production.
     * Most predictors simply return today closing price, because maybe this will be useful in some cases. It's used by Marginal Maximizer Statics
     *
     * @param dept the sales department
     * @return predicted price
     */
    @Override
    public float predictSalePriceWhenNotChangingProduction(SalesDepartment dept) {
        return priceToPredict;
    }


    /**
     * Predicts the future price of the next good to buy
     *
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    @Override
    public float predictPurchasePriceWhenIncreasingProduction(PurchasesDepartment dept) {
        return priceToPredict;
    }

    /**
     * Predicts the future price of the next good to buy
     *
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    @Override
    public float predictPurchasePriceWhenDecreasingProduction(PurchasesDepartment dept) {
        return priceToPredict;
    }

    /**
     * Predicts the future price of the next good to buy
     *
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    @Override
    public float predictPurchasePriceWhenNoChangeInProduction(PurchasesDepartment dept) {
        return priceToPredict;
    }

    /**
     * Call this to kill the predictor
     */
    @Override
    public void turnOff() {
    }

    public int getPriceToPredict() {
        return priceToPredict;
    }

    public void setPriceToPredict(int priceToPredict) {
        this.priceToPredict = priceToPredict;
    }
}
