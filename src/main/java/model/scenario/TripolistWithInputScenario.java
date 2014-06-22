/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.scenario;

import agents.firm.Firm;
import agents.firm.production.Blueprint;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.pid.PurchasesFixedPID;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import financial.market.EndOfPhaseOrderHandler;
import financial.market.Market;
import financial.market.OrderBookMarket;
import financial.utilities.ShopSetPricePolicy;
import goods.GoodType;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.utilities.dummies.DailyGoodTree;

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
public class TripolistWithInputScenario extends TripolistScenario {

    final public static GoodType INPUT = new UndifferentiatedGoodType("testInput","Input");


    public TripolistWithInputScenario(MacroII macroII) {
        super(macroII);
    }


    /**
     * Called by MacroII, it creates agents and then schedules them.
     */
    @Override
    public void start() {
        model.getGoodTypeMasterList().addNewSectors(INPUT);
        //change the blueprint so that it's done with inputs
        blueprint = Blueprint.simpleBlueprint(INPUT,1, UndifferentiatedGoodType.GENERIC,1);

        super.start();    //create the monopolist
        monopolist.setName("Monopolist");


        //market for input
        OrderBookMarket inputMarket = new OrderBookMarket(INPUT);
        inputMarket.setOrderHandler(new EndOfPhaseOrderHandler(),model);
        inputMarket.setPricePolicy(new ShopSetPricePolicy());
        getMarkets().put(INPUT,inputMarket);

        //create the sellers
        createSuppliers();

        //register a purchase department to the monopolist
        addPurchaseDepartmentToFirms();



    }

    protected void addPurchaseDepartmentToFirms() {
        assert super.getCompetitors() != null;
        for(Firm f : super.getCompetitors())
        {

            PurchasesDepartment department = PurchasesDepartment.
                    getEmptyPurchasesDepartment(Integer.MAX_VALUE, f,
                            getMarkets().get(INPUT));
            Market market = getMarkets().get(INPUT);


            department.setOpponentSearch(new SimpleBuyerSearch(market, f));
            department.setSupplierSearch(new SimpleSellerSearch(market, f));

            PurchasesFixedPID control = new PurchasesFixedPID(department,50);


            department.setControl(control);
            department.setPricingStrategy(control);
            // department.setPredictor(new LookAheadPredictor());
            f.registerPurchasesDepartment(department, INPUT);
        }
    }

    private void createSuppliers() {
        //create the suppliers
        //min price = 2, max price = 50, increments of 1. Each of them sells one a day
        for(int price=2;price<50; price++)
        {
            final DailyGoodTree seller = new DailyGoodTree(model,price,getMarkets().get(INPUT));


            getAgents().add(seller);
        }
    }


}
