/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.scenario;

import agents.EconomicAgent;
import agents.people.Person;
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
import financial.market.OrderBookMarket;
import financial.utilities.Quote;
import financial.utilities.ShopSetPricePolicy;
import goods.DifferentiatedGoodType;
import goods.Good;
import goods.GoodType;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import sim.engine.SimState;
import sim.engine.Steppable;
import model.utilities.dummies.DummyBuyer;


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


    final public static GoodType LEATHER = new UndifferentiatedGoodType("leatherTest","Leather");

    final public static GoodType BEEF = new UndifferentiatedGoodType("beefTest","Beef");


    public MultiProduction(MacroII model) {
        super(model);
    }

    /**
     * Called by MacroII, it creates agents and then schedules them.
     */
    @Override
    public void start() {

        model.getGoodTypeMasterList().addNewSectors(LEATHER, BEEF, UndifferentiatedGoodType.LABOR);

        //LEATHER market
        final OrderBookMarket leatherMarket= new OrderBookMarket(LEATHER);
        leatherMarket.setPricePolicy(new ShopSetPricePolicy()); //make the seller price matter
        getMarkets().put(LEATHER,leatherMarket);

        //Beef Market
        final OrderBookMarket beefMarket= new OrderBookMarket(BEEF);
        beefMarket.setPricePolicy(new ShopSetPricePolicy()); //make the seller price matter
        getMarkets().put(BEEF,beefMarket);

        //create and record the labor market!
        final OrderBookMarket laborMarket= new OrderBookMarket(UndifferentiatedGoodType.LABOR);
        laborMarket.setPricePolicy(new ShopSetPricePolicy()); //make the seller price matter
        getMarkets().put(UndifferentiatedGoodType.LABOR,laborMarket);




        //50 buyers, from 2 to 100
        for(int i=0; i<50; i++)
        {


            /************************************************
             * Add LEATHER Buyers
             ************************************************/

            /**
             * For this scenario we use a different kind of dummy buyer that, after "period" passed, puts a new order in the market
             */
            final DummyBuyer buyer = new DummyBuyer(getModel(),2 + i*2,leatherMarket){
                @Override
                public void reactToFilledBidQuote(Quote quoteFilled, Good g, int price, final EconomicAgent b) {
                    //trick to get the steppable to recognize the anonymous me!
                    final DummyBuyer reference = this;
                    //schedule a new quote in period!
                    this.getModel().scheduleTomorrow(
                            ActionOrder.TRADE,new Steppable() {
                        @Override
                        public void step(SimState simState) {
                            reference.receiveMany(UndifferentiatedGoodType.MONEY,1000);
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
                    buyer.receiveMany(UndifferentiatedGoodType.MONEY,1000);
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
            final DummyBuyer buyer = new DummyBuyer(getModel(),2 + i*2,leatherMarket){
                @Override
                public void reactToFilledBidQuote(Quote quoteFilled, Good g, int price, final EconomicAgent b) {
                    //trick to get the steppable to recognize the anonymous me!
                    final DummyBuyer reference = this;
                    //schedule a new quote in period!
                    this.getModel().scheduleTomorrow(ActionOrder.TRADE,new Steppable() {
                        @Override
                        public void step(SimState simState) {
                            receiveMany(UndifferentiatedGoodType.MONEY, 1000);
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
                    buyer.receiveMany(UndifferentiatedGoodType.MONEY,1000);
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
            seller.receiveMany(UndifferentiatedGoodType.MONEY,1000000000);

            //set up the firm at time 1
            getModel().scheduleSoon(ActionOrder.PREPARE_TO_TRADE,new Steppable() {
                @Override
                public void step(SimState simState) {
                    //sales department
                    SalesDepartment dept = SalesDepartmentFactory.incompleteSalesDepartment(seller, beefMarket,
                            new SimpleBuyerSearch(beefMarket, seller), new SimpleSellerSearch(beefMarket, seller),
                            SalesDepartmentAllAtOnce.class);
                    seller.registerSaleDepartment(dept, BEEF);
                    dept.setAskPricingStrategy(new SimpleFlowSellerPID(dept)); //set strategy to PID

                    //sales department
                    SalesDepartment dept2 =SalesDepartmentFactory.incompleteSalesDepartment(seller, leatherMarket,
                            new SimpleBuyerSearch(leatherMarket, seller), new SimpleSellerSearch(leatherMarket, seller),
                            SalesDepartmentAllAtOnce.class);
                    seller.registerSaleDepartment(dept2, LEATHER);
                    dept2.setAskPricingStrategy(new SimpleFlowSellerPID(dept2)); //set strategy to PID


                    //add the plant
                    Blueprint blueprint = new Blueprint.Builder().output(BEEF,1).output(LEATHER,2).build();
                    Plant plant = new Plant(blueprint,seller);
                    plant.setPlantMachinery(new LinearConstantMachinery(DifferentiatedGoodType.CAPITAL,seller,0,plant));
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
