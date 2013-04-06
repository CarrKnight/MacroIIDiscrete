/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.pricing.pid;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.sales.SaleResult;
import agents.firm.sales.SalesDepartment;
import financial.Market;
import financial.utilities.Quote;
import goods.Good;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

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
 * @version 2012-08-28
 * @see
 */
public class DecentralizedStockout implements StockoutEstimator {

    /**
     * We count each customer order once because people might us multiple time
     */
    Set<EconomicAgent> displeasedCustomers = new HashSet<>();

    private int certifiedDispleasedCustomers = 0;

    final private SimpleFlowSellerPID strategy;

    public DecentralizedStockout(SimpleFlowSellerPID strategy) {
        this.strategy = strategy;
    }

    @Override
    public void newPIDStep(Market market) {

        displeasedCustomers.clear(); //reset yourself
        certifiedDispleasedCustomers = 0;
    }

    @Override
    public int getStockouts() {
        //if there is anything in the inventory, stockout is automatically 0


        return displeasedCustomers.size() + certifiedDispleasedCustomers; //reset yourself
    }

    /**
     * Tell the listener a new bid has been placed into the market
     *
     * @param buyer   the agent placing the bid
     * @param price   the price of the good
     * @param bestAsk the best ask when the bid was made
     */
    @Override
    public void newBidEvent(@Nonnull EconomicAgent buyer, long price, Quote bestAsk) {
        //probably never called

    }

    /**
     * Tell the listener a new bid has been placed into the market
     *
     * @param buyer the agent placing the bid
     * @param quote the removed quote
     */
    @Override
    public void removedBidEvent(@Nonnull EconomicAgent buyer, @Nonnull Quote quote) {
        //probably never called
    }

    /**
     * Tell the listener the firm just tasked the salesdepartment to sell a new good
     *
     * @param owner the owner of the sales department
     * @param dept  the sales department asked
     * @param good  the good being sold
     */
    @Override
    public void sellThisEvent(@Nonnull Firm owner, @Nonnull SalesDepartment dept, @Nonnull Good good) {
        //not our problem
    }

    /**
     * This logEvent is fired whenever the sales department managed to sell a good!
     *
     * @param dept   The department
     * @param result The saleResult object describing the trade!
     */
    @Override
    public void goodSoldEvent(@Nonnull SalesDepartment dept, @Nonnull SaleResult result) {
        //good, I guess
    }

    /**
     * Tell the listener a peddler just came by and we couldn't service him because we have no goods
     *
     * @param owner the owner of the sales department
     * @param dept  the sales department asked
     */
    @Override
    public void stockOutEvent(@Nonnull Firm owner, @Nonnull SalesDepartment dept, @Nonnull EconomicAgent buyer) {
    //    System.out.println("displeased!");
        displeasedCustomers.add(buyer); //the buyer won't be happy!
    }

    /**
     * Tell the listener a trade has been carried out
     *
     * @param buyer         the buyer of this trade
     * @param seller        the seller of this trade
     * @param goodExchanged the good that has been traded
     * @param price         the price of the trade
     */
    @Override
    public void tradeEvent(EconomicAgent buyer, EconomicAgent seller, Good goodExchanged, long price, Quote sellerQuote, Quote buyerQuote) {
        if((seller != strategy.getSales().getFirm()) && displeasedCustomers.remove(buyer))
        { //if we weren't the ones selling AND the buying person was displeased with us
  //          System.out.println("certified!");
            certifiedDispleasedCustomers++; //now it can be displeased with us again if he wants but it's a certified lost opportunity.

        }
    }
}
