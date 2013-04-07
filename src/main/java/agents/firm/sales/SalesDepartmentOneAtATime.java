/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.sales.exploration.BuyerSearchAlgorithm;
import agents.firm.sales.exploration.SellerSearchAlgorithm;
import financial.Market;
import financial.utilities.ActionsAllowed;
import goods.Good;
import model.MacroII;

import javax.annotation.Nonnull;

/**
 * <h4>Description</h4>
 * <p/> This sales department peddles as usual, but is allowed to place only one quote at a time in the order book.
 * <p/> This give less information to other players but improve performance.
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
public class SalesDepartmentOneAtATime extends SalesDepartment
{

    /**
     * the lock is true while we are preparing to quote but we haven't quoted yet. This is to ensure that no ask is scheduled before the previous one is settled.
     */
    boolean lock = false;


    public SalesDepartmentOneAtATime(Firm firm, Market market, BuyerSearchAlgorithm buyerSearchAlgorithm,
                                     SellerSearchAlgorithm sellerSearchAlgorithm,
                                     @Nonnull MacroII model) {
        super(sellerSearchAlgorithm, market, model, firm, buyerSearchAlgorithm);
    }


    public SalesDepartmentOneAtATime(Firm firm, Market market, BuyerSearchAlgorithm buyerSearchAlgorithm,
                                     SellerSearchAlgorithm sellerSearchAlgorithm) {
        this(firm, market, buyerSearchAlgorithm, sellerSearchAlgorithm,firm.getModel());
    }


    /**
     * This is the constructor for the template sales department.  It also registers the firm as seller
     * @param firm The firm where the sales department belongs
     * @param market The market the sales department deals in
     */
    private SalesDepartmentOneAtATime(@Nonnull Firm firm, @Nonnull Market market) {
        this(firm,market,null,null,firm.getModel());



    }


    /**
     * lock before placing
     *
     * @param g the good to quote
     */
    @Override
    protected void prepareToPlaceAQuote(Good g) {
        lock = true;
        super.prepareToPlaceAQuote(g);
    }

    /**
     * unlock before placing it
     * @param g
     */
    @Override
    protected void placeAQuoteNow(Good g) {
        assert lock; //you should have locked!
        lock = false;
        super.placeAQuoteNow(g);
    }



    /**
     * The real difference in sales departments is just how they handle new goods to sell!
     *
     * @param g
     */
    @Override
    protected void newGoodToSell(Good g) {

        //now act
        if(market.getSellerRole() == ActionsAllowed.QUOTE) //if we are supposed to quote
        {
            placeIfAble();

        }
        else  if(canPeddle)
        {
            peddle(g);

        }
    }

    /**
     * tries to sell a good on the order book conditional on not being waiting for other orders.
     */
    private void placeIfAble() {
        if(!this.hasItPlacedAtLeastOneOrder() && toSell.size() >0 && !lock)
        {
            Good g =  toSell.iterator().next();
            prepareToPlaceAQuote(g);
        }
    }


    /**
     * Calls superclass and checks if it can place another order
     *
     * @param g     the good sold
     * @param price the price for which it sold
     */
    @Override
    public void reactToFilledQuote(Good g, long price, EconomicAgent buyer) {
        super.reactToFilledQuote(g, price, buyer);

        placeIfAble();
    }
}
