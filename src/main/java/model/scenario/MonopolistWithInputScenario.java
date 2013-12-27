/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.scenario;

import agents.firm.production.Blueprint;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.pid.PurchasesFixedPID;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import financial.market.Market;
import financial.market.OrderBookMarket;
import financial.utilities.ShopSetPricePolicy;
import goods.GoodType;
import model.MacroII;
import model.utilities.dummies.DailyGoodTree;
import model.utilities.pid.CascadePIDController;

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
 * @author carrknight
 * @version 2013-01-10
 * @see
 */
public class MonopolistWithInputScenario extends MonopolistScenario {

    public MonopolistWithInputScenario(MacroII macroII) {
        super(macroII);
    }


    /**
     * Called by MacroII, it creates agents and then schedules them.
     */
    @Override
    public void start() {
        //change the blueprint so that it's done with inputs
        blueprint = Blueprint.simpleBlueprint(GoodType.LEATHER,1,GoodType.GENERIC,1);

        super.start();    //create the monopolist
        monopolist.setName("Monopolist");


        //market for input
        Market inputMarket = new OrderBookMarket(GoodType.LEATHER);
        inputMarket.setPricePolicy(new ShopSetPricePolicy());
        getMarkets().put(GoodType.LEATHER,inputMarket);

        //create the sellers
        createSuppliers();

        //register a purchase department to the monopolist
        addPurchaseDepartmentToMonopolist();



    }

    protected void addPurchaseDepartmentToMonopolist() {
        PurchasesDepartment department = PurchasesDepartment.
                getEmptyPurchasesDepartment(Long.MAX_VALUE, monopolist,
                getMarkets().get(GoodType.LEATHER));
        Market market = getMarkets().get(GoodType.LEATHER);


        department.setOpponentSearch(new SimpleBuyerSearch(market, monopolist));
        department.setSupplierSearch(new SimpleSellerSearch(market, monopolist));

      //  PurchasesFixedPID control = new PurchasesFixedPID(department,4, CascadePIDController.class,model);
        PurchasesFixedPID control = new PurchasesFixedPID(department,10,CascadePIDController.class,model);

        department.setControl(control);
        department.setPricingStrategy(control);
       // department.setPredictor(new LookAheadPredictor());
        monopolist.registerPurchasesDepartment(department, GoodType.LEATHER);
    }

    private void createSuppliers() {
        //create the suppliers
        //min price = 2, max price = 50, increments of 1. Each of them sells one a day
        for(long price=2;price<50; price++)
        {
            final DailyGoodTree seller = new DailyGoodTree(model,price,getMarkets().get(GoodType.LEATHER));


            getAgents().add(seller);
        }
    }


}
