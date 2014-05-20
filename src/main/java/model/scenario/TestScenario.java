/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.scenario;

import financial.market.Market;
import financial.market.OrderBookMarket;
import financial.utilities.Quote;
import goods.Good;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import sim.engine.SimState;
import sim.engine.Steppable;
import model.utilities.dummies.DummyBuyer;
import model.utilities.dummies.DummySeller;

/**
 * <h4>Description</h4>
 * <p/> This is the scenario I used to test that the GUI and the scenario wouldn't blow up in my face
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-09-10
 * @see
 */
public class TestScenario extends Scenario {

    public TestScenario(MacroII model) {
        super(model);
    }

    /**
     * Called by MacroII, it creates agents and then schedules them.
     */
    @Override
    public void start() {

        //create a useless market
        final Market testMarket = new OrderBookMarket(UndifferentiatedGoodType.GENERIC);
        //add it to the collections!
        getMarkets().put(UndifferentiatedGoodType.GENERIC,testMarket);

        //create fake buyer
        final DummyBuyer buyer = new DummyBuyer(getModel(),100,testMarket);
        buyer.receiveMany(UndifferentiatedGoodType.MONEY,10000000);

        //create fake seller
        final DummySeller seller = new DummySeller(getModel(),100);
        //add them to the list
        getAgents().add(buyer); getAgents().add(seller);

        //schedule them at t=1
        getModel().scheduleSoon(ActionOrder.DAWN,new Steppable() {
            @Override
            public void step(SimState simState) {
                testMarket.registerBuyer(buyer);
                testMarket.registerSeller(seller);


            }
        });

        //schedule them to trade to check that it gets recorded
        getModel().scheduleSoon(ActionOrder.TRADE,new Steppable() {
            @Override
            public void step(SimState simState) {
                Good good = Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC);
                seller.receive(good,null);

                testMarket.trade(buyer, seller,good ,
                        10, Quote.emptyBidQuote(UndifferentiatedGoodType.GENERIC), Quote.emptySellQuote(good));

            }
        });

        //schedule them to trade one more time
        getModel().scheduleAnotherDay(ActionOrder.TRADE,new Steppable() {
            @Override
            public void step(SimState simState) {
                Good good =  Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC);
                seller.receive(good,null);
                testMarket.trade(buyer, seller, good,
                        30, Quote.emptyBidQuote(UndifferentiatedGoodType.GENERIC),Quote.emptySellQuote(good));

            }
        },2);

        //force them to submit random quotes so that
        getModel().scheduleAnotherDay(ActionOrder.TRADE,new Steppable() {
            @Override
            public void step(SimState simState) {
                testMarket.submitBuyQuote(buyer,10);
                testMarket.submitBuyQuote(buyer,30);
            }
        },3);

        //force them to submit more random quotes
        getModel().scheduleAnotherDay(ActionOrder.TRADE,new Steppable() {
            @Override
            public void step(SimState simState) {
                testMarket.submitBuyQuote(buyer,40);
                testMarket.submitSellQuote(seller, 100,
                        Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC));
            }
        },4);





    }
}
