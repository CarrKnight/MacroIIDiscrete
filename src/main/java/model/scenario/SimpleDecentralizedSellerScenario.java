/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.scenario;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentAllAtOnce;
import agents.firm.sales.SalesDepartmentFactory;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.pricing.pid.salesControlWithSmoothedinventoryAndPID;
import ec.util.MersenneTwisterFast;
import financial.market.DecentralizedMarket;
import financial.market.Market;
import financial.utilities.ShopSetPricePolicy;
import goods.Good;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.dummies.DummyBuyer;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * <h4>Description</h4>
 * <p/>  This is basically a copy of the simple sceller scenario except that the market is decentralized.
 * The reason I do have a different class for this scenario is because right now the buyers are dummy and not other agents and so I have to re-program them
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-11-08
 * @see
 */
public class SimpleDecentralizedSellerScenario extends Scenario
{



    int period = 10;

    boolean demandShifts = false;


    /**
     * Called by MacroII, it creates agents and then schedules them.
     */
    @Override
    public void start() {

        //create the market
        final DecentralizedMarket market = new DecentralizedMarket(UndifferentiatedGoodType.GENERIC);
        market.setPricePolicy(new ShopSetPricePolicy());

        getMarkets().put(UndifferentiatedGoodType.GENERIC,market);


        //only one seller
        final Firm seller = new Firm(getModel());
        //give it a seller department at time 1
        SalesDepartment dept = SalesDepartmentFactory.incompleteSalesDepartment(seller, market, new SimpleBuyerSearch(market, seller), new SimpleSellerSearch(market, seller),
                SalesDepartmentAllAtOnce.class);
        seller.registerSaleDepartment(dept, UndifferentiatedGoodType.GENERIC);

        //create a seller PID with the right speed
        salesControlWithSmoothedinventoryAndPID sellerPID = new salesControlWithSmoothedinventoryAndPID(dept);
        dept.setAskPricingStrategy(sellerPID);

        dept.setCanPeddle(false);
        getAgents().add(seller);

        //arrange for goods to drop periodically in the firm
        getModel().scheduleSoon(ActionOrder.PRODUCTION,new Steppable() {
            @Override
            public void step(SimState simState) {
                //sell 4 goods!
                for(int i=0; i<4; i++){
                    Good good = Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC);
                    seller.receive(good,null);
                    seller.reactToPlantProduction(good);
                }

                getModel().scheduleTomorrow(ActionOrder.PRODUCTION,this);
            }
        });

        //create 10 buyers
        for(int i=0;i<10;i++){

            createBuyer(seller,market,i*10,5f);

        }

        if(demandShifts)
        {
            //create 10 buyers
            for(int i=0;i<10;i++){
                createBuyer(seller,market,i*10+100,2000f);

            }
        }

    }




    private void createBuyer(final EconomicAgent seller, Market market, int price, float time){

        /**
         * For this scenario we use dummy buyers that shop only once every "period"
         */
        final DummyBuyer buyer = new DummyBuyer(getModel(),price,market);  market.registerBuyer(buyer);
        buyer.receiveMany(UndifferentiatedGoodType.MONEY,1000000);

        //Make it shop once a day for one good only!
        getModel().scheduleSoon(ActionOrder.TRADE,
                new Steppable() {
                    @Override
                    public void step(SimState simState) {


                        DummyBuyer.goShopping(buyer,seller, UndifferentiatedGoodType.GENERIC);
                        getModel().scheduleTomorrow(ActionOrder.TRADE,this);
                    }
                }
        );


        getAgents().add(buyer);
    }


    public SimpleDecentralizedSellerScenario(MacroII model) {
        super(model);
        model.random = new MersenneTwisterFast(0l);


    }

    public boolean isDemandShifts() {
        return demandShifts;
    }

    public void setDemandShifts(boolean demandShifts) {
        this.demandShifts = demandShifts;
    }
}
