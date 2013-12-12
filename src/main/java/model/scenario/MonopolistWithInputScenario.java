/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.scenario;

import agents.EconomicAgent;
import agents.firm.production.Blueprint;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.pid.PurchasesDailyPID;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import financial.market.Market;
import financial.market.OrderBookMarket;
import financial.utilities.BuyerSetPricePolicy;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.dummies.DummySeller;
import sim.engine.SimState;
import sim.engine.Steppable;

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
        inputMarket.setPricePolicy(new BuyerSetPricePolicy());
        getMarkets().put(GoodType.LEATHER,inputMarket);

        //create the sellers
        createSuppliers();

        //register a purchase department to the monopolist
        addPurchaseDepartmentToMonopolist();



    }

    private void addPurchaseDepartmentToMonopolist() {
        PurchasesDepartment department = PurchasesDepartment.getEmptyPurchasesDepartment(Long.MAX_VALUE, monopolist,
                getMarkets().get(GoodType.LEATHER));
        Market market = getMarkets().get(GoodType.LEATHER);


        department.setOpponentSearch(new SimpleBuyerSearch(market, monopolist));
        department.setSupplierSearch(new SimpleSellerSearch(market, monopolist));

      //  PurchasesFixedPID control = new PurchasesFixedPID(department,4, CascadePIDController.class,model);
        PurchasesDailyPID control = new PurchasesDailyPID(department);

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
            final DummySeller seller = createSupplier(getMarkets().get(GoodType.LEATHER),price);


            getAgents().add(seller);
        }
    }

    private DummySeller createSupplier(final Market inputMarket, final long price) {
        /**
         * For this scenario we use a different kind of dummy buyer that, after "period" passed, puts a new order in the market
         */
        final DummySeller seller = new DummySeller(getModel(),price){

            @Override
            public void reactToFilledAskedQuote(Good g, long price, EconomicAgent buyer) {
                final DummySeller reference = this;
                assert !reference.has(g);
                //schedule a new quote in period!
                this.getModel().scheduleTomorrow(ActionOrder.TRADE, new Steppable() {
                    @Override
                    public void step(SimState simState) {
                        //receive new good
                        Good input = new Good(GoodType.LEATHER,reference,reference.saleQuote);
                        reference.receive(input,null);

                        //put another quote
                        inputMarket.submitSellQuote(reference,reference.saleQuote,input );

                    }
                });


            }
        };

        assert seller.saleQuote == price;
        seller.setName("supplier:" + seller.saleQuote);

        //make it adjust once to register and submit the first quote

        getModel().scheduleSoon(ActionOrder.DAWN, new Steppable() {
            @Override
            public void step(SimState simState) {
                inputMarket.registerSeller(seller);
                //receive new good
                Good input = new Good(GoodType.LEATHER,seller,seller.saleQuote);
                seller.receive(input, null);

                //put another quote
                inputMarket.submitSellQuote(seller,seller.saleQuote,input );
            }
        });
        return seller;
    }
}
