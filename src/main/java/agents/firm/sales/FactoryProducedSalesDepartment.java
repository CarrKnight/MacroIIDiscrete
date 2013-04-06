/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales;

import agents.firm.sales.exploration.BuyerSearchAlgorithm;
import agents.firm.sales.exploration.SellerSearchAlgorithm;
import agents.firm.sales.prediction.SalesPredictor;
import agents.firm.sales.pricing.AskPricingStrategy;

/**
 * This is a weird one. This is the output of a sales department factory. It has the sales department, but it also has a link to its strategies so that if
 * the one who calls the factory needs a link to those strategies, it can just grab them from here. This is handy for further tuning!
 * @param <BS> The buyer search algorithm type
 * @param <SS> The seller search algorithm type
 * @param <AP> The ask pricing strategy type
 * @param <SP> The sales predictor type
 */
public class FactoryProducedSalesDepartment<BS extends BuyerSearchAlgorithm,SS extends SellerSearchAlgorithm,
        AP extends AskPricingStrategy, SP extends SalesPredictor>
{
    /**
     * the buyer search algorithm
     */
    final private BS buyerSearchAlgorithm;

    /**
     * the seller search object
     */
    final private SS sellerSearchAlgorithm;

    /**
     * the ask pricing object
     */
    final private AP askPricingStrategy;

    /**
     * the sales predictor object
     */
    final private SP salesPredictor;

    /**
     * the sales department created
     */
    final private SalesDepartment salesDepartment;

    /**
     This is the output of a sales department factory. It has the sales department, but it also has a link to its strategies so that if
     * the one who calls the factory needs a link to those strategies, it can just grab them from here. This is handy for further tuning!
     * @param buyerSearchAlgorithm  the buyer search algorithm created
     * @param sellerSearchAlgorithm the seller search algorithm created
     * @param askPricingStrategy the pricing strategy created
     * @param salesPredictor the sales predictor created
     */
    public FactoryProducedSalesDepartment(SalesDepartment salesDepartment, BS buyerSearchAlgorithm,
                                          SS sellerSearchAlgorithm, AP askPricingStrategy, SP salesPredictor) {
        this.salesDepartment = salesDepartment;
        this.buyerSearchAlgorithm = buyerSearchAlgorithm;
        this.sellerSearchAlgorithm = sellerSearchAlgorithm;
        this.askPricingStrategy = askPricingStrategy;
        this.salesPredictor = salesPredictor;




    }



    /**
     * Gets the ask pricing object.
     *
     * @return Value of the ask pricing object.
     */
    public AP getAskPricingStrategy() {
        return askPricingStrategy;
    }

    /**
     * Gets the buyer search algorithm.
     *
     * @return Value of the buyer search algorithm.
     */
    public BS getBuyerSearchAlgorithm() {
        return buyerSearchAlgorithm;
    }

    /**
     * Gets the seller search object.
     *
     * @return Value of the seller search object.
     */
    public SS getSellerSearchAlgorithm() {
        return sellerSearchAlgorithm;
    }

    /**
     * Gets the sales predictor object.
     *
     * @return Value of the sales predictor object.
     */
    public SP getSalesPredictor() {
        return salesPredictor;
    }


    /**
     * Gets the sales department created.
     *
     * @return Value of the sales department created.
     */
    public SalesDepartment getSalesDepartment() {
        return salesDepartment;
    }


}
