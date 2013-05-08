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
import agents.firm.production.control.PlantControl;
import agents.firm.production.control.facades.*;
import agents.firm.production.technology.LinearConstantMachinery;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentAllAtOnce;
import agents.firm.sales.SalesDepartmentFactory;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.pricing.AskPricingStrategy;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import financial.OrderBookMarket;
import financial.utilities.BuyerSetPricePolicy;
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
 * @version 2012-09-14
 * @see
 */
public class MonopolistScenario extends Scenario {


    /**
     * The blueprint that the monopolist will use
     */
    protected Blueprint blueprint = new Blueprint.Builder().output(GoodType.GENERIC, 1).build();
    protected Firm monopolist;


    public MonopolistScenario(MacroII macroII) {
        super(macroII);
    }


    /**
     * Does hr charge a single wage across the plant?
     */
    private boolean fixedPayStructure = true;

    /**
     * this is somewhat of an ugly trick to get the gui to be able to select the kind of class
     * the human resource should use. Mason GUI knows how to deal with enums
     */
    private MonopolistScenarioIntegratedControlEnum controlType = MonopolistScenarioIntegratedControlEnum.HILL_CLIMBER_SIMPLE;

    /**
     * Do agents go around look for better offers all the time?
     */
    private boolean lookForBetterOffers = false;

    protected  OrderBookMarket goodMarket;

    protected OrderBookMarket laborMarket;

    /**
     * the strategy used by the sales department of the monopolist
     */
    protected Class<? extends AskPricingStrategy> askPricingStrategy = SimpleFlowSellerPID.class;


    protected Class<? extends SalesDepartment> salesDepartmentType = SalesDepartmentAllAtOnce.class;

    /**
     * Called by MacroII, it creates agents and then schedules them.
     */
    @Override
    public void start() {

        //create and record a new market!
        goodMarket= new OrderBookMarket(GoodType.GENERIC);
        goodMarket.setPricePolicy(new ShopSetPricePolicy()); //make the seller price matter
        getMarkets().put(GoodType.GENERIC,goodMarket);

        //create and record the labor market!
        laborMarket= new OrderBookMarket(GoodType.LABOR);
        laborMarket.setPricePolicy(new BuyerSetPricePolicy()); //make the seller price matter
        getMarkets().put(GoodType.LABOR,laborMarket);




        //buyers from 100 to 41 with increments of1
        for(int i=41; i<101; i++)
        {


            /************************************************
             * Add Good Buyers
             ************************************************/

            /**
             * For this scenario we use a different kind of dummy buyer that, after "period" passed, puts a new order in the market
             */
            final DummyBuyer buyer = new DummyBuyer(getModel(),i){
                @Override
                public void reactToFilledBidQuote(Good g, long price, final EconomicAgent b) {
                    //trick to get the steppable to recognize the anonymous me!
                    final DummyBuyer reference = this;
                    //schedule a new quote in period!
                    this.getModel().scheduleTomorrow(ActionOrder.TRADE, new Steppable() {
                        @Override
                        public void step(SimState simState) {
                            earn(1000l);
                            //put another quote
                            goodMarket.submitBuyQuote(reference, getFixedPrice());

                        }
                    });

                }
            };

            buyer.setName("Dummy Buyer");

            //make it adjust once to register and submit the first quote

            getModel().scheduleSoon(ActionOrder.DAWN, new Steppable() {
                @Override
                public void step(SimState simState) {
                    goodMarket.registerBuyer(buyer);
                    buyer.earn(1000l);
                    //make the buyer submit a quote soon.
                    goodMarket.submitBuyQuote(buyer, buyer.getFixedPrice());
                }
            });




            getAgents().add(buyer);

        }


        /************************************************
         * Add  Monopolist
         ************************************************/

        buildMonopolist();


        /************************************************
         * Add workers
         ************************************************/

        //with minimum wage from 15 to 65
        for(int i=0; i<120; i++)
        {
            //dummy worker, really
            final Person p = new Person(getModel(),0l,(15+i)*7,laborMarket);

            p.setSearchForBetterOffers(lookForBetterOffers);

            p.start();

            getAgents().add(p);

        }






    }

    public void buildMonopolist() {
        //only one seller
        monopolist = new Firm(getModel());
        monopolist.earn(1000000000l);
        monopolist.setName("monopolist");
        //set up the firm at time 1
        getModel().scheduleSoon(ActionOrder.DAWN, new Steppable() {
            @Override
            public void step(SimState simState) {
                //sales department
                SalesDepartment dept = SalesDepartmentFactory.incompleteSalesDepartment(monopolist, goodMarket,
                        new SimpleBuyerSearch(goodMarket, monopolist), new SimpleSellerSearch(goodMarket, monopolist),
                        salesDepartmentType);
                monopolist.registerSaleDepartment(dept, GoodType.GENERIC);
                dept.setAskPricingStrategy(AskPricingStrategy.Factory.newAskPricingStrategy(askPricingStrategy,dept)); //set strategy to PID
                //add the plant
                Plant plant = new Plant(blueprint, monopolist);
                plant.setPlantMachinery(new LinearConstantMachinery(GoodType.CAPITAL, mock(Firm.class), 0, plant));
                plant.setCostStrategy(new InputCostStrategy(plant));
                monopolist.addPlant(plant);


                //human resources
                HumanResources hr;

                hr = HumanResources.getHumanResourcesIntegrated(Long.MAX_VALUE, monopolist,
                        laborMarket, plant,controlType.getController(), null, null).getDepartment();

                //       seller.registerHumanResources(plant, hr);
                hr.setFixedPayStructure(fixedPayStructure);
                hr.start();


            }
        });

        getAgents().add(monopolist);
    }


    public boolean isFixedPayStructure() {
        return fixedPayStructure;
    }

    public void setFixedPayStructure(boolean fixedPayStructure) {
        this.fixedPayStructure = fixedPayStructure;
    }


    public boolean isLookForBetterOffers() {
        return lookForBetterOffers;
    }

    public void setLookForBetterOffers(boolean lookForBetterOffers) {
        this.lookForBetterOffers = lookForBetterOffers;
    }

    /**
     * Gets The blueprint that the monopolist will use.
     *
     * @return Value of The blueprint that the monopolist will use.
     */
    public Blueprint getBlueprint() {
        return blueprint;
    }

    /**
     * Sets new The blueprint that the monopolist will use.
     *
     * @param blueprint New value of The blueprint that the monopolist will use.
     */
    public void setBlueprint(Blueprint blueprint) {
        this.blueprint = blueprint;
    }



    /**
     * this is somewhat of an ugly trick to get the gui to be able to select the kind of class
     * the human resource should use. Mason GUI knows how to deal with enums
     */
    public enum MonopolistScenarioIntegratedControlEnum
    {
        MARGINAL_PLANT_CONTROL(MarginalPlantControl.class),

        HILL_CLIMBER_SIMPLE(DiscreteSlowPlantControl.class),

        HILL_CLIMBER_ALWAYS_MOVING(DumbClimberControl.class),

        MARGINAL_WITH_PID(MarginalPlantControlWithPID.class),

        MARGINAL_WITH_UNIT_PID(MarginalPlantControlWithPIDUnit.class);



        private MonopolistScenarioIntegratedControlEnum(Class<? extends PlantControl> controller) {
            this.controller = controller;
        }

        private  Class<? extends PlantControl> controller;


        public Class<? extends PlantControl> getController() {
            return controller;
        }
    }

    /**
     * Gets controlType.
     *
     * @return Value of controlType.
     */
    public MonopolistScenarioIntegratedControlEnum getControlType() {
        return controlType;
    }

    /**
     * Sets new controlType.
     *
     * @param controlType New value of controlType.
     */
    public void setControlType(MonopolistScenarioIntegratedControlEnum controlType) {
        this.controlType = controlType;
    }

    public Firm getMonopolist() {
        return monopolist;
    }


    /**
     * Gets the strategy used by the sales department of the monopolist.
     *
     * @return Value of the strategy used by the sales department of the monopolist.
     */
    public Class<? extends AskPricingStrategy> getAskPricingStrategy() {
        return askPricingStrategy;
    }

    /**
     * Sets new the strategy used by the sales department of the monopolist.
     *
     * @param askPricingStrategy New value of the strategy used by the sales department of the monopolist.
     */
    public void setAskPricingStrategy(Class<? extends AskPricingStrategy> askPricingStrategy) {
        this.askPricingStrategy = askPricingStrategy;
    }


    /**
     * Sets new salesDepartmentType.
     *
     * @param salesDepartmentType New value of salesDepartmentType.
     */
    public void setSalesDepartmentType(Class<? extends SalesDepartment> salesDepartmentType) {
        this.salesDepartmentType = salesDepartmentType;
    }

    /**
     * Gets salesDepartmentType.
     *
     * @return Value of salesDepartmentType.
     */
    public Class<? extends SalesDepartment> getSalesDepartmentType() {
        return salesDepartmentType;
    }
}
