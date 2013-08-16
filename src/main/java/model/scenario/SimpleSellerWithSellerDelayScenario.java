/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.scenario;

import agents.firm.Firm;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentFactory;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.pricing.AskPricingStrategy;
import agents.firm.sales.pricing.pid.SalesControlFlowPIDWithFixedInventory;
import com.google.common.base.Preconditions;
import financial.market.OrderBookMarket;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * <h4>Description</h4>
 * <p/> This is basically the simple seller except that we force the ask strategy to be SalesControlFlowPIDWithFixedInventory.class and we
 * allow to set the sampling speed. This is used to test how delays can help.
 * <p/> Also the demand for production is a little bit weird in the sense that it is step-linear 10-20-30-40-50-50+x-70-80-90; where x is the acceptable range of prices that are equilibria.
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-04-25
 * @see
 */
public class SimpleSellerWithSellerDelayScenario extends SimpleSellerScenario {

    /**
     * the distance in reservation prices between the 5th and 6th buyer
     */
    private int acceptablePriceRange=10;

    private int sellerDelay = 0;

    /**
     * Creates the scenario object, so that it links to the model.
     * =
     */
    public SimpleSellerWithSellerDelayScenario(MacroII model) {
        super(model);
    }


    /**
     * Sets new sellerDelay.
     *
     * @param sellerDelay New value of sellerDelay.
     */
    public void setSellerDelay(int sellerDelay) {
        Preconditions.checkState(sellerDelay >= 0);
        this.sellerDelay = sellerDelay;
    }

    /**
     * Sets new the distance in reservation prices between the 5th and 6th buyer.
     *
     * @param acceptablePriceRange New value of the distance in reservation prices between the 5th and 6th buyer.
     */
    public void setAcceptablePriceRange(int acceptablePriceRange) {
        Preconditions.checkState(acceptablePriceRange >= 1 && acceptablePriceRange<19);

        this.acceptablePriceRange = acceptablePriceRange;
    }

    /**
     * Gets sellerDelay.
     *
     * @return Value of sellerDelay.
     */
    public int getSellerDelay() {
        return sellerDelay;
    }

    /**
     * Gets the distance in reservation prices between the 5th and 6th buyer.
     *
     * @return Value of the distance in reservation prices between the 5th and 6th buyer.
     */
    public int getAcceptablePriceRange() {
        return acceptablePriceRange;
    }


    @Override
    protected void buildBuyer(OrderBookMarket market, final long price) {
        if(price!=60)
            super.buildBuyer(market, price);    //To change body of overridden methods use File | Settings | File Templates.
        else
            super.buildBuyer(market,50 + getAcceptablePriceRange());
    }

    @Override
    protected Firm buildSeller(final OrderBookMarket market) {

        final Firm seller = new Firm(getModel());
        getAgents().add(seller);

        //give it a seller department at time 1
        getModel().scheduleSoon(ActionOrder.DAWN,new Steppable() {
            @Override
            public void step(SimState simState) {
                SalesDepartment dept = SalesDepartmentFactory.incompleteSalesDepartment(seller, market, new SimpleBuyerSearch(market, seller),
                        new SimpleSellerSearch(market, seller), salesDepartmentType);
                seller.registerSaleDepartment(dept, GoodType.GENERIC);

                SalesControlFlowPIDWithFixedInventory strategy = AskPricingStrategy.Factory.newAskPricingStrategy(SalesControlFlowPIDWithFixedInventory.class,dept);
                if(sellerDelay > 0 )
                    strategy.setSpeed(sellerDelay);

                //strategy.setSpeed(sellerDelay);
                dept.setAskPricingStrategy(strategy); //set strategy to PID


            }
        });
        return seller;
    }
}
