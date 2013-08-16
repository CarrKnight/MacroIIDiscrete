/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales;

import agents.firm.Firm;
import agents.firm.sales.exploration.BuyerSearchAlgorithm;
import agents.firm.sales.exploration.SellerSearchAlgorithm;
import agents.firm.sales.prediction.SalesPredictor;
import agents.firm.sales.pricing.AskPricingStrategy;
import financial.market.Market;
import goods.GoodType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SalesDepartmentFactory {
    /**
     * This is a factory method for sales department that retuns a NON-READY sales department. That is, one that is missing ask-pricing strategy and predictor pricing and the search algorithms.
     * Use only if you know what you are doing
     *
     * @param firm   the firm the sales department belongs to
     * @param market the market the sales department markets
     * @return a new sales department
     */
    public static SalesDepartment incompleteSalesDepartment(@Nonnull Firm firm, @Nonnull Market market) {
        return new SalesDepartmentAllAtOnce(firm, market);
    }

    /**
     * This is a factory method for sales department that retuns a NON-READY sales department. That is, one that is missing ask-pricing strategy and predictor pricing.
     * Use only if you know what you are doing
     *
     *
     * @param firm   the firm the sales department belongs to
     * @param market the market the sales department markets
     * @param SalesDepartmentType
     * @return a new sales department
     */
    public static SalesDepartment incompleteSalesDepartment(@Nonnull Firm firm, @Nonnull Market market,
                                                            @Nonnull BuyerSearchAlgorithm buyerSearchAlgorithm, @Nonnull SellerSearchAlgorithm sellerSearchAlgorithm,
                                                            Class<? extends SalesDepartment> SalesDepartmentType) {
        if(SalesDepartmentType.equals(SalesDepartmentAllAtOnce.class))
            return new SalesDepartmentAllAtOnce(firm, market, buyerSearchAlgorithm, sellerSearchAlgorithm);
        else
        {
            assert SalesDepartmentType.equals(SalesDepartmentOneAtATime.class);
            return new SalesDepartmentOneAtATime(firm, market, buyerSearchAlgorithm, sellerSearchAlgorithm);
        }
    }

    /**
     * This is a standard factory method to create a new sales department.
     * Any "null" type argument is randomized
     *
     *
     * @param firm   the firm the sales department belongs to
     * @param market the market the sales department markets
     * @param salesDepartmentType
     * @return a new sales department
     */
    public static <BS extends BuyerSearchAlgorithm, SS extends SellerSearchAlgorithm, AP extends AskPricingStrategy, SP extends SalesPredictor>
    FactoryProducedSalesDepartment<BS, SS, AP, SP> newSalesDepartment(@Nonnull Firm firm, @Nonnull Market market,
                                                                      @Nullable Class<BS> buyerSearch, @Nullable Class<SS> sellerSearch,
                                                                      @Nullable Class<AP> priceStrategy, @Nullable Class<SP> predictionStrategy,
                                                                      Class<? extends SalesDepartment> salesDepartmentType) {
        //create the search algorithms
        BS buyerSearchAlgorithm;
        if (buyerSearch == null)
            buyerSearchAlgorithm = (BS) BuyerSearchAlgorithm.Factory.randomBuyerSearchAlgorithm(market, firm);
        else
            buyerSearchAlgorithm = BuyerSearchAlgorithm.Factory.newBuyerSearchAlgorithm(buyerSearch, market, firm);

        SS sellerSearchAlgorithm;
        if (sellerSearch == null)
            sellerSearchAlgorithm = (SS) SellerSearchAlgorithm.Factory.randomSellerSearchAlgorithm(market, firm);
        else
            sellerSearchAlgorithm = SellerSearchAlgorithm.Factory.newSellerSearchAlgorithm(sellerSearch, market, firm);


        SalesDepartment dept;
        if(salesDepartmentType.equals(SalesDepartmentAllAtOnce.class))
            dept = new SalesDepartmentAllAtOnce(firm, market, buyerSearchAlgorithm, sellerSearchAlgorithm);
        else{
            assert salesDepartmentType.equals(SalesDepartmentOneAtATime.class);
            dept = new SalesDepartmentOneAtATime(firm, market, buyerSearchAlgorithm, sellerSearchAlgorithm);
        }

        firm.registerSaleDepartment(dept, GoodType.GENERIC);

        //now create the two pricing strategies
        AP askPricingStrategy;
        if (priceStrategy == null)
            askPricingStrategy = (AP) AskPricingStrategy.Factory.randomAskPricingStrategy(dept);
        else
            askPricingStrategy = AskPricingStrategy.Factory.newAskPricingStrategy(priceStrategy, dept);

        dept.setAskPricingStrategy(askPricingStrategy);

        SP salesPredictor;
        if (predictionStrategy == null)
            salesPredictor = (SP) SalesPredictor.Factory.randomSalesPredictor(firm.getRandom(),dept);
        else
            salesPredictor = SalesPredictor.Factory.newSalesPredictor(predictionStrategy,dept);


        dept.setPredictorStrategy(salesPredictor);
        //register and retun
        //finally return!
        FactoryProducedSalesDepartment<BS, SS, AP, SP> toReturn = new FactoryProducedSalesDepartment<BS, SS, AP, SP>(dept, buyerSearchAlgorithm, sellerSearchAlgorithm, askPricingStrategy, salesPredictor);

        //make sure we passed objects
        assert dept.buyerSearchAlgorithm == toReturn.getBuyerSearchAlgorithm();
        assert dept.sellerSearchAlgorithm == toReturn.getSellerSearchAlgorithm();
        assert dept.askPricingStrategy == toReturn.getAskPricingStrategy();
        assert dept.predictorStrategy == toReturn.getSalesPredictor();

        return toReturn;
    }
}