/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales;

import agents.firm.Firm;
import agents.firm.sales.exploration.BuyerSearchAlgorithm;
import agents.firm.sales.exploration.SellerSearchAlgorithm;
import com.google.common.base.Preconditions;
import financial.market.Market;
import financial.utilities.ActionsAllowed;
import goods.Good;
import model.MacroII;

import java.util.Arrays;


/**
 * <h4>Description</h4>
 * <p/> This kind of sales department places a quote whenever it has something to sell, so in an order book you will always see how much it has selling. This is helpful for others to make strategies, but it's rather slow.
 * <p/>
 * <p/>
 * The subclasses are different in the way they respond to HQ when asked what is next price going to be.
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version %I%, %G%
 * @see
 */
public class SalesDepartmentAllAtOnce extends SalesDepartment{



    @Override
    protected void newGoodsToSellEvent(Good... goods) {
        Preconditions.checkArgument(goods.length > 0);
        for(Good g : goods)
        {
            //now act
            if (market.getSellerRole() == ActionsAllowed.QUOTE) //if we are supposed to quote
            {
                prepareToPlaceAQuote(g);

            } else if (canPeddle) {
                peddle(g);

            }
        }
    }


    @Override
    protected void newGoodsToSellEvent(int amount) {
        Preconditions.checkArgument(amount > 0);

        //create fake goods
        Good[] array = new Good[amount];
        Arrays.fill(array,Good.getInstanceOfUndifferentiatedGood(market.getGoodType()));
        //new goods to sell!
        newGoodsToSellEvent(array);


    }

    /**
     * This is the constructor for the template sales department.  It also registers the firm as seller
     * @param firm The firm where the sales department belongs
     * @param market The market the sales department deals in
     */
    public SalesDepartmentAllAtOnce( Firm firm,  Market market) {
        this(firm,market,null,null,firm.getModel());



    }

    /**
     * This is the constructor for the template sales department.  It also registers the firm as seller
     * @param firm The firm where the sales department belongs
     * @param market The market the sales department deals in
     */
    private SalesDepartmentAllAtOnce( Firm firm,  Market market,  MacroII model) {
        this(firm,market,null,null,model);



    }


    /**
     * This is the constructor for the template sales department. It also registers the firm as seller
     * @param firm The firm where the sales department belongs
     * @param market The market the sales department deals in
     * @param buyerSearchAlgorithm the buyer search department
     * @param sellerSearchAlgorithm the seller search department
     */
    public SalesDepartmentAllAtOnce(Firm firm, Market market,
                                    BuyerSearchAlgorithm buyerSearchAlgorithm, SellerSearchAlgorithm sellerSearchAlgorithm) {
        this(firm, market, buyerSearchAlgorithm, sellerSearchAlgorithm,firm.getModel());


    }


    /**
     * This is the constructor for the template sales department. It also registers the firm as seller
     * @param firm The firm where the sales department belongs
     * @param market The market the sales department deals in
     * @param buyerSearchAlgorithm the buyer search department
     * @param sellerSearchAlgorithm the seller search department
     */
    private SalesDepartmentAllAtOnce(Firm firm, Market market, BuyerSearchAlgorithm buyerSearchAlgorithm,
                                     SellerSearchAlgorithm sellerSearchAlgorithm,
                                     MacroII model) {
        super(sellerSearchAlgorithm, market, model, firm, buyerSearchAlgorithm);



    }



}
