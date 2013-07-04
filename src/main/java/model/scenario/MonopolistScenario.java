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
import agents.firm.purchases.prediction.LookAheadPredictor;
import agents.firm.purchases.prediction.PurchasesPredictor;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentAllAtOnce;
import agents.firm.sales.SalesDepartmentFactory;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.prediction.LinearExtrapolationPredictor;
import agents.firm.sales.prediction.SalesPredictor;
import agents.firm.sales.pricing.AskPricingStrategy;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import au.com.bytecode.opencsv.CSVWriter;
import financial.OrderBookMarket;
import financial.utilities.BuyerSetPricePolicy;
import financial.utilities.ShopSetPricePolicy;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.stats.DailyStatCollector;
import model.utilities.dummies.DummyBuyer;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.io.FileWriter;
import java.io.IOException;

import static model.experiments.tuningRuns.MarginalMaximizerWithUnitPIDTuningMultiThreaded.printProgressBar;
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

    private int laborProductivity = 1;
    /**
     * The intercept of the demand
     */
    private int demandIntercept = 101;

    /**
     * the slope of the demand (as a positive)
     */
    private int demandSlope = 1;

    /**
     * the intercept of the wage curve expressed in days (it gets multiplied in the model)
     */
    private int dailyWageIntercept = 14;

    /**
     * the slope of the wage
     */
    private int dailyWageSlope = 1;

    /**
     * The blueprint that the monopolist will use
     */
    protected Blueprint blueprint= new Blueprint.Builder().output(GoodType.GENERIC, laborProductivity).build();
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

    /**
     * The type of sales predictor the sales department should use
     */
    protected Class<? extends SalesPredictor> salesPricePreditorStrategy = LinearExtrapolationPredictor.class;

    /**
     * The kind of sales department to use
     */
    protected Class<? extends SalesDepartment> salesDepartmentType = SalesDepartmentAllAtOnce.class;


    /**
     * The type of price predictor the human resources department should use
     */
    protected Class<? extends PurchasesPredictor> purchasesPricePreditorStrategy = LookAheadPredictor.class;

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

        //this prepares the production of the firms
        blueprint.getOutputs().put(GoodType.GENERIC,laborProductivity);




        //buyers from 100 to 41 with increments of1
        for(int i=1; i< 100; i++)
        {



            /************************************************
             * Add Good Buyers
             ************************************************/

            /*
             * For this scenario we use a different kind of dummy buyer that, after "period" passed, puts a new order in the market
             */
            long buyerPrice = demandIntercept - demandSlope * i;
            if(buyerPrice <= 0) //break if the price is 0 or lower, we are done drawing!
                break;


            final DummyBuyer buyer = new DummyBuyer(getModel(),buyerPrice){
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

        monopolist = buildFirm();
        monopolist.setName("monopolist");


        /************************************************
         * Add workers
         ************************************************/

        //with minimum wage from 15 to 65
        for(int i=1; i<120; i++)
        {

            int dailyWage = dailyWageIntercept + dailyWageSlope * i;
            //dummy worker, really
            final Person p = new Person(getModel(),0l,(dailyWage)*7,laborMarket);

            p.setSearchForBetterOffers(lookForBetterOffers);

            p.start();

            getAgents().add(p);

        }






    }

    public Firm buildFirm() {
        //only one seller
        final Firm built= new Firm(getModel());
        built.earn(1000000000l);
       // built.setName("monopolist");
        //set up the firm at time 1
        getModel().scheduleSoon(ActionOrder.DAWN, new Steppable() {
            @Override
            public void step(SimState simState) {
                //sales department
                SalesDepartment dept = SalesDepartmentFactory.incompleteSalesDepartment(built, goodMarket,
                        new SimpleBuyerSearch(goodMarket, built), new SimpleSellerSearch(goodMarket, built),
                        salesDepartmentType);
                dept.setPredictorStrategy(SalesPredictor.Factory.newSalesPredictor(salesPricePreditorStrategy,dept));
                built.registerSaleDepartment(dept, GoodType.GENERIC);
                dept.setAskPricingStrategy(AskPricingStrategy.Factory.newAskPricingStrategy(askPricingStrategy,dept)); //set strategy to PID
                //add the plant
                Plant plant = new Plant(blueprint, built);
                plant.setPlantMachinery(new LinearConstantMachinery(GoodType.CAPITAL, mock(Firm.class), 0, plant));
                plant.setCostStrategy(new InputCostStrategy(plant));
                built.addPlant(plant);


                //human resources
                HumanResources hr;

                hr = HumanResources.getHumanResourcesIntegrated(Long.MAX_VALUE, built,
                        laborMarket, plant,controlType.getController(), null, null).getDepartment();

                //       seller.registerHumanResources(plant, hr);
                hr.setFixedPayStructure(fixedPayStructure);

                hr.setPredictor(PurchasesPredictor.Factory.newPurchasesPredictor(purchasesPricePreditorStrategy,hr));
                hr.start();


            }
        });

        getAgents().add(built);
        return built;
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






    public static void main(String[] args)
    {
        //set up
        final MacroII macroII = new MacroII(System.currentTimeMillis());
        MonopolistScenario scenario1 = new MonopolistScenario(macroII);
        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        scenario1.setAskPricingStrategy(SimpleFlowSellerPID.class);
        scenario1.setControlType(MonopolistScenarioIntegratedControlEnum.MARGINAL_WITH_UNIT_PID);

     //   scenario1.setSalesPricePreditorStrategy(PricingSalesPredictor.class);



        //assign scenario
        macroII.setScenario(scenario1);

        macroII.start();



        //CSV writer set up
        try {
            CSVWriter writer = new CSVWriter(new FileWriter("runs/monopolist/"+"marginalNoPredictor"+".csv"));
            DailyStatCollector collector = new DailyStatCollector(macroII,writer);
            collector.start();

        } catch (IOException e) {
            System.err.println("failed to create the file!");
        }


        //run!
        while(macroII.schedule.getTime()<3000)
        {
            macroII.schedule.step(macroII);
            printProgressBar(3001,(int)macroII.schedule.getSteps(),100);
        }


    }




    /**
     * Sets new The type of sales predictor the sales department should use.
     *
     * @param salesPricePreditorStrategy New value of The type of sales predictor the sales department should use.
     */
    public void setSalesPricePreditorStrategy(Class<? extends SalesPredictor> salesPricePreditorStrategy) {
        this.salesPricePreditorStrategy = salesPricePreditorStrategy;
    }

    /**
     * Gets The type of sales predictor the sales department should use.
     *
     * @return Value of The type of sales predictor the sales department should use.
     */
    public Class<? extends SalesPredictor> getSalesPricePreditorStrategy() {
        return salesPricePreditorStrategy;
    }

    /**
     * Sets new The type of price predictor the human resources department should use.
     *
     * @param purchasesPricePreditorStrategy New value of The type of price predictor the human resources department should use.
     */
    public void setPurchasesPricePreditorStrategy(Class<? extends PurchasesPredictor> purchasesPricePreditorStrategy) {
        this.purchasesPricePreditorStrategy = purchasesPricePreditorStrategy;
    }

    /**
     * Gets The type of price predictor the human resources department should use.
     *
     * @return Value of The type of price predictor the human resources department should use.
     */
    public Class<? extends PurchasesPredictor> getPurchasesPricePreditorStrategy() {
        return purchasesPricePreditorStrategy;
    }


    public static int findWorkerTargetThatMaximizesProfits(int demandIntercept, int demandSlope, int dailyWageIntercept,
                                                           int dailyWageSlope, int laborProductivity)
    {
        int workerMax = 0;
        int profitsMax = 0;
        //go through each possibility
        for(int i=1; i<200; i++)
        {
            int quantity =  i * laborProductivity;
            int totalRevenues = (demandIntercept - demandSlope * quantity)*quantity;
            int totalWages =  (dailyWageIntercept + dailyWageSlope * i) * i;

            int profits =  totalRevenues - totalWages;
            if(profits>profitsMax)
            {
                profitsMax = profits;
                workerMax = i ;
            }



        }

        return workerMax;

        /*
        this is the formula, far more efficient but i guess there is really no point to have to deal with rounding errors

        float  numerator = laborProductivity * demandIntercept - dailyWageIntercept;
        float denominator = 2*(laborProductivity * laborProductivity * demandSlope + dailyWageSlope);

        return Math.round(numerator/denominator );
        */
    }


    /**
     * Sets new the intercept of the wage curve expressed in days it gets multiplied in the model.
     *
     * @param dailyWageIntercept New value of the intercept of the wage curve expressed in days it gets multiplied in the model.
     */
    public void setDailyWageIntercept(int dailyWageIntercept) {
        this.dailyWageIntercept = dailyWageIntercept;
    }

    /**
     * Sets new The intercept of the demand.
     *
     * @param demandIntercept New value of The intercept of the demand.
     */
    public void setDemandIntercept(int demandIntercept) {
        this.demandIntercept = demandIntercept;
    }

    /**
     * Sets new the slope of the demand as a positive.
     *
     * @param demandSlope New value of the slope of the demand as a positive.
     */
    public void setDemandSlope(int demandSlope) {
        this.demandSlope = demandSlope;
    }

    /**
     * Gets The intercept of the demand.
     *
     * @return Value of The intercept of the demand.
     */
    public int getDemandIntercept() {
        return demandIntercept;
    }

    /**
     * Sets new the slope of the wage.
     *
     * @param dailyWageSlope New value of the slope of the wage.
     */
    public void setDailyWageSlope(int dailyWageSlope) {
        this.dailyWageSlope = dailyWageSlope;
    }

    /**
     * Gets the intercept of the wage curve expressed in days it gets multiplied in the model.
     *
     * @return Value of the intercept of the wage curve expressed in days it gets multiplied in the model.
     */
    public int getDailyWageIntercept() {
        return dailyWageIntercept;
    }

    /**
     * Gets the slope of the demand as a positive.
     *
     * @return Value of the slope of the demand as a positive.
     */
    public int getDemandSlope() {
        return demandSlope;
    }

    /**
     * Gets the slope of the wage.
     *
     * @return Value of the slope of the wage.
     */
    public int getDailyWageSlope() {
        return dailyWageSlope;
    }





    /**
     * Gets laborProductivity.
     *
     * @return Value of laborProductivity.
     */
    public int getLaborProductivity() {
        return laborProductivity;
    }

    /**
     * Sets new laborProductivity.
     *
     * @param laborProductivity New value of laborProductivity.
     */
    public void setLaborProductivity(int laborProductivity) {
        this.laborProductivity = laborProductivity;
    }
}
