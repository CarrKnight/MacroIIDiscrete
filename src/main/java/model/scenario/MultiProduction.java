/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.scenario;

import agents.EconomicAgent;
import agents.Person;
import agents.firm.Firm;
import agents.firm.cost.InputCostStrategy;
import agents.firm.personell.HumanResources;
import agents.firm.production.Blueprint;
import agents.firm.production.Plant;
import agents.firm.production.control.facades.DiscreteSlowPlantControl;
import agents.firm.production.technology.LinearConstantMachinery;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentAllAtOnce;
import agents.firm.sales.SalesDepartmentFactory;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import financial.OrderBookMarket;
import financial.utilities.ShopSetPricePolicy;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import sim.engine.SimState;
import sim.engine.Steppable;
import model.utilities.dummies.DummyBuyer;

import static org.mockito.Mockito.*;

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
 * @version 2012-09-15
 * @see
 */
public class MultiProduction extends Scenario{


    public MultiProduction(MacroII model) {
        super(model);
    }

    /**
     * Called by MacroII, it creates agents and then schedules them.
     */
    @Override
    public void start() {


        //leather market
        final OrderBookMarket leatherMarket= new OrderBookMarket(GoodType.LEATHER);
        leatherMarket.setPricePolicy(new ShopSetPricePolicy()); //make the seller price matter
        getMarkets().put(GoodType.LEATHER,leatherMarket);

        //Beef Market
        final OrderBookMarket beefMarket= new OrderBookMarket(GoodType.BEEF);
        beefMarket.setPricePolicy(new ShopSetPricePolicy()); //make the seller price matter
        getMarkets().put(GoodType.BEEF,beefMarket);

        //create and record the labor market!
        final OrderBookMarket laborMarket= new OrderBookMarket(GoodType.LABOR);
        laborMarket.setPricePolicy(new ShopSetPricePolicy()); //make the seller price matter
        getMarkets().put(GoodType.LABOR,laborMarket);




        //50 buyers, from 2 to 100
        for(int i=0; i<50; i++)
        {


            /************************************************
             * Add LEATHER Buyers
             ************************************************/

            /**
             * For this scenario we use a different kind of dummy buyer that, after "period" passed, puts a new order in the market
             */
            final DummyBuyer buyer = new DummyBuyer(getModel(),2 + i*2){
                @Override
                public void reactToFilledBidQuote(Good g, long price, final EconomicAgent b) {
                    //trick to get the steppable to recognize the anonymous me!
                    final DummyBuyer reference = this;
                    //schedule a new quote in period!
                    this.getModel().scheduleTomorrow(
                            ActionOrder.TRADE,new Steppable() {
                        @Override
                        public void step(SimState simState) {
                            earn(1000l);
                            //put another quote
                            leatherMarket.submitBuyQuote(reference,getFixedPrice());

                        }
                    });

                }
            };


            //make it adjust once to register and submit the first quote

            getModel().scheduleSoon(ActionOrder.TRADE,new Steppable() {
                @Override
                public void step(SimState simState) {
                    leatherMarket.registerBuyer(buyer);
                    buyer.earn(1000l);
                    //make the buyer submit a quote soon.
                    leatherMarket.submitBuyQuote(buyer,buyer.getFixedPrice());                }
            }  );




            getAgents().add(buyer);



        }

        /************************************************
         * Add BEEF Buyers
         ************************************************/
        //50 buyers, from 2 to 100
        for(int i=0; i<50; i++)
        {




            /**
             * For this scenario we use a different kind of dummy buyer that, after "period" passed, puts a new order in the market
             */
            final DummyBuyer buyer = new DummyBuyer(getModel(),2 + i*2){
                @Override
                public void reactToFilledBidQuote(Good g, long price, final EconomicAgent b) {
                    //trick to get the steppable to recognize the anonymous me!
                    final DummyBuyer reference = this;
                    //schedule a new quote in period!
                    this.getModel().scheduleTomorrow(ActionOrder.TRADE,new Steppable() {
                        @Override
                        public void step(SimState simState) {
                            earn(1000l);
                            //put another quote
                            beefMarket.submitBuyQuote(reference,getFixedPrice());

                        }
                    });

                }
            };


            //make it adjust once to register and submit the first quote

            getModel().scheduleSoon(ActionOrder.TRADE,new Steppable() {
                @Override
                public void step(SimState simState) {
                    beefMarket.registerBuyer(buyer);
                    buyer.earn(1000l);
                    //make the buyer submit a quote soon.
                    beefMarket.submitBuyQuote(buyer,buyer.getFixedPrice());                }
            }  );




            getAgents().add(buyer);



        }


        /************************************************
         * Add  TRIPOLY
         ************************************************/

        for(int i=0;i<3;i++){
            //only one seller
            final Firm seller = new Firm(getModel());
            seller.earn(1000000000l);
            //set up the firm at time 1
            getModel().scheduleSoon(ActionOrder.PREPARE_TO_TRADE,new Steppable() {
                @Override
                public void step(SimState simState) {
                    //sales department
                    SalesDepartment dept = SalesDepartmentFactory.incompleteSalesDepartment(seller, beefMarket,
                            new SimpleBuyerSearch(beefMarket, seller), new SimpleSellerSearch(beefMarket, seller),
                            SalesDepartmentAllAtOnce.class);
                    seller.registerSaleDepartment(dept,GoodType.BEEF);
                    dept.setAskPricingStrategy(new SimpleFlowSellerPID(dept)); //set strategy to PID

                    //sales department
                    SalesDepartment dept2 =SalesDepartmentFactory.incompleteSalesDepartment(seller, leatherMarket,
                            new SimpleBuyerSearch(leatherMarket, seller), new SimpleSellerSearch(leatherMarket, seller),
                            SalesDepartmentAllAtOnce.class);
                    seller.registerSaleDepartment(dept2,GoodType.LEATHER);
                    dept2.setAskPricingStrategy(new SimpleFlowSellerPID(dept2)); //set strategy to PID


                    //add the plant
                    Blueprint blueprint = new Blueprint.Builder().output(GoodType.BEEF,1).output(GoodType.LEATHER,2).build();
                    Plant plant = new Plant(blueprint,seller);
                    plant.setPlantMachinery(new LinearConstantMachinery(GoodType.CAPITAL,mock(Firm.class),0,plant));
                    plant.setCostStrategy(new InputCostStrategy(plant));
                    seller.addPlant(plant);


                    //human resources
                    HumanResources hr = HumanResources.getHumanResourcesIntegrated(100000000,seller,
                            laborMarket,plant,DiscreteSlowPlantControl.class,null,null).getDepartment();
               //     seller.registerHumanResources(plant, hr);
                //    hr.setProbabilityForgetting(.05f);


                }
            });

            getAgents().add(seller);


        }



        /************************************************
         * Add workers
         ************************************************/

        //50 workers with minimum wage from 10 to 500
        for(int i=0; i<50; i++)
        {
            //dummy worker, really
            final Person p = new Person(getModel(),0l,10+i*10,laborMarket);



            getAgents().add(p);

        }


    }
}
