/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.scenario;

import agents.people.AfterWorkStrategy;
import agents.people.Person;
import agents.firm.Firm;
import agents.firm.cost.InputCostStrategy;
import agents.firm.personell.FactoryProducedHumanResources;
import agents.firm.personell.HumanResources;
import agents.firm.production.Blueprint;
import agents.firm.production.Plant;
import agents.firm.production.control.HillClimberThroughPredictionControl;
import agents.firm.production.control.PlantControl;
import agents.firm.production.control.facades.DiscreteSlowPlantControl;
import agents.firm.production.control.facades.DumbClimberControl;
import agents.firm.production.control.facades.MarginalPlantControl;
import agents.firm.production.control.facades.MarginalPlantControlWithPIDUnit;
import agents.firm.production.technology.LinearConstantMachinery;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.prediction.PurchasesPredictor;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentFactory;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.exploration.BuyerSearchAlgorithm;
import agents.firm.sales.exploration.SellerSearchAlgorithm;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.prediction.SalesPredictor;
import agents.firm.sales.pricing.AskPricingStrategy;
import agents.firm.sales.pricing.pid.AdaptiveFlowSellerPID;
import agents.people.QuitJobAfterWorkStrategy;
import com.google.common.base.Preconditions;
import financial.market.EndOfPhaseOrderHandler;
import financial.market.Market;
import financial.market.OrderBookMarket;
import financial.utilities.BuyerSetPricePolicy;
import financial.utilities.ShopSetPricePolicy;
import goods.DifferentiatedGoodType;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.dummies.Customer;
import model.utilities.dummies.CustomerWithDelay;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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
     * should you rehire workers every day? To have an effect it has to be set BEFORE start is called!
     */
    private boolean workersToBeRehiredEveryDay = true;



    /**
     * The blueprint that the monopolist will use
     */
    protected Blueprint blueprint= new Blueprint.Builder().output(UndifferentiatedGoodType.GENERIC, laborProductivity).build();
    protected Firm monopolist;

    private int buyerDelay=0;


    public MonopolistScenario(MacroII macroII) {
        super(macroII);
        workers= new LinkedList<>();
        demand = new LinkedList<>();
        maximizers = new LinkedList<>();
    }


    /**
     * Does hr charge a single wage across the plant?
     */
    private boolean fixedPayStructure = true;

    static public MonopolistScenarioIntegratedControlEnum defaultControlType = MonopolistScenarioIntegratedControlEnum.HILL_CLIMBER_SIMPLE;

    /**
     * this is somewhat of an ugly trick to get the gui to be able to select the kind of class
     * the human resource should use. Mason GUI knows how to deal with enums
     */
    private MonopolistScenarioIntegratedControlEnum controlType = defaultControlType;

    protected  OrderBookMarket goodMarket;

    protected OrderBookMarket laborMarket;


    final private List<Person> workers;

    final private List<Customer> demand;

    private List<PlantControl> maximizers;

    /**
     * the strategy used by the sales department of the monopolist
     */
    protected Class<? extends AskPricingStrategy> askPricingStrategy = AdaptiveFlowSellerPID.class;

    /**
     * The type of sales predictor the sales department should use
     */
    protected Class<? extends SalesPredictor> salesPricePreditorStrategy = SalesDepartment.defaultPredictorStrategy;

    /**
     * The kind of sales department to use
     */
    protected Class<? extends SalesDepartment> salesDepartmentType = SalesDepartmentOneAtATime.class;


    /**
     * The type of price predictor the human resources department should use
     */
    protected Class<? extends PurchasesPredictor> purchasesPricePreditorStrategy = PurchasesDepartment.defaultPurchasePredictor;

    /**
     * Called by MacroII, it creates agents and then schedules them.
     */
    @Override
    public void start() {
        maximizers.clear();
        model.getGoodTypeMasterList().addNewSectors(UndifferentiatedGoodType.GENERIC, UndifferentiatedGoodType.LABOR, DifferentiatedGoodType.CAPITAL);

        //create and record a new market!
        goodMarket= new OrderBookMarket(UndifferentiatedGoodType.GENERIC);
        goodMarket.setOrderHandler(new EndOfPhaseOrderHandler(),model);
        goodMarket.setPricePolicy(new ShopSetPricePolicy()); //make the seller price matter
        getMarkets().put(UndifferentiatedGoodType.GENERIC,goodMarket);

        //create and record the labor market!
        laborMarket= new OrderBookMarket(UndifferentiatedGoodType.LABOR);
        laborMarket.setOrderHandler(new EndOfPhaseOrderHandler(),model);
        laborMarket.setPricePolicy(new BuyerSetPricePolicy()); //make the employer offer matter
        getMarkets().put(UndifferentiatedGoodType.LABOR,laborMarket);

        //this prepares the production of the firms
        blueprint.getOutputs().put(UndifferentiatedGoodType.GENERIC,laborProductivity);



        /************************************************
         * Add Good Buyers
         ************************************************/
        createDemand();


        /************************************************
         * Add  Monopolist
         ************************************************/

        monopolist = buildFirm();
        monopolist.setName("monopolist");


        /************************************************
         * Add workers
         ************************************************/
        createLaborSupply();


    }

    private void createLaborSupply() {
        Collection<Person> workersCreated = fillLaborSupply(dailyWageIntercept,dailyWageSlope,workersToBeRehiredEveryDay,
                120,laborMarket,getModel());
        workers.addAll(workersCreated);

    }

    /**
     * A simple utility method that takes a market and fills it with workers. It also adds them to the model.
     * @param dailyWageIntercept the intercept of the labor supply
     * @param dailyWageSlope the slope of the labor supply
     * @param workersToBeRehiredEveryDay are workers changing job automatically every day?
     * @param totalNumberOfWorkers the total number of workers in the labor supply curve
     * @param laborMarket the market to fill
     * @param model the model (to schedule workers)
     * @return a list with all the new
     */
    public static Collection<Person> fillLaborSupply(int dailyWageIntercept, int dailyWageSlope, boolean workersToBeRehiredEveryDay,
                                                     int totalNumberOfWorkers,
                                                     Market laborMarket, MacroII model) {
        Preconditions.checkState(dailyWageIntercept >= 0);
        Preconditions.checkState(dailyWageSlope > 0);
        Preconditions.checkState(totalNumberOfWorkers > 0);

        LinkedList<Person> workers = new LinkedList<>();
        //with minimum wage from 15 to 65
        for(int i=1; i<=totalNumberOfWorkers; i++)
        {

            int dailyWage = dailyWageIntercept + dailyWageSlope * i;
            //dummy worker, really
            final Person p = new Person(model,0l,(dailyWage),laborMarket);
            if(workersToBeRehiredEveryDay)
                p.setAfterWorkStrategy(AfterWorkStrategy.Factory.build(QuitJobAfterWorkStrategy.class));



            workers.add(p);
            model.addAgent(p);

        }
        return workers;
    }

    /**
     * create a 100 dummy buyers
     */
    private void createDemand() {
        for(int i=1; i< Math.max(100,demandIntercept+1); i++)
        {




            /*
             * For this scenario we use a different kind of dummy buyer that, after "period" passed, puts a new order in the market
             */
            int buyerPrice = demandIntercept - demandSlope * i;
            if(buyerPrice <= 0) //break if the price is 0 or lower, we are done drawing!
                break;

            final Customer buyer = buildBuyer(buyerPrice);

            //buyer.setName("Dummy Buyer");

            //make it adjust once to register and submit the first quote



            demand.add(buyer);
            model.addAgent(buyer);

        }
    }

    private Customer buildBuyer(int buyerPrice) {

        if(buyerDelay == 0 )

          return new Customer(model,buyerPrice,goodMarket);
        else
            return new CustomerWithDelay(model,Math.max((buyerPrice),1),buyerDelay,goodMarket);

    }

    public Firm buildFirm() {
        //only one seller
        final Firm built= new Firm(getModel());
        built.receiveMany(UndifferentiatedGoodType.MONEY,500000000);
        // built.setName("monopolist");
        //set up the firm at time 1
        getModel().scheduleSoon(ActionOrder.DAWN, new Steppable() {
            @Override
            public void step(SimState simState) {
                buildSalesDepartmentToFirm(built);
                Plant plant = buildPlantForFirm(built);
                buildHrForFirm(plant, built);


            }
        });

        model.addAgent(built);
        return built;
    }

    protected void buildHrForFirm(Plant plant, Firm built) {
        //human resources
        HumanResources hr;

        final FactoryProducedHumanResources<? extends PlantControl,BuyerSearchAlgorithm,SellerSearchAlgorithm> factoryMadeHR =
                HumanResources.getHumanResourcesIntegrated(Integer.MAX_VALUE, built,
                laborMarket, plant, controlType.getController(), null, null);

        hr = factoryMadeHR.getDepartment();

        //seller.registerHumanResources(plant, hr);
        hr.setFixedPayStructure(fixedPayStructure);


        maximizers.add(factoryMadeHR.getPlantControl());


        //    hr.setPredictor(PurchasesPredictor.Factory.newPurchasesPredictor(purchasesPricePreditorStrategy,hr));
    }

    protected Plant buildPlantForFirm(Firm built) {
        //add the plant
        Plant plant = new Plant(blueprint, built);
        plant.setPlantMachinery(new LinearConstantMachinery(DifferentiatedGoodType.CAPITAL, built, 0, plant));
        plant.setCostStrategy(new InputCostStrategy(plant));
        built.addPlant(plant);
        return plant;
    }

    protected void buildSalesDepartmentToFirm(Firm built) {
        //sales department
        SalesDepartment dept = SalesDepartmentFactory.incompleteSalesDepartment(built, goodMarket,
                new SimpleBuyerSearch(goodMarket, built), new SimpleSellerSearch(goodMarket, built),
                salesDepartmentType);
        dept.setPredictorStrategy(SalesPredictor.Factory.newSalesPredictor(salesPricePreditorStrategy,dept));
        built.registerSaleDepartment(dept, UndifferentiatedGoodType.GENERIC);
        AskPricingStrategy askPricingStrategy = AskPricingStrategy.Factory.newAskPricingStrategy(this.askPricingStrategy, dept);
        dept.setAskPricingStrategy(askPricingStrategy); //set strategy to PID

    }


    public boolean isFixedPayStructure() {
        return fixedPayStructure;
    }

    public void setFixedPayStructure(boolean fixedPayStructure) {
        this.fixedPayStructure = fixedPayStructure;
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


        MARGINAL_WITH_UNIT_PID(MarginalPlantControlWithPIDUnit.class),

        CRAZY_MARGINAL(HillClimberThroughPredictionControl.class);




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


    public void resetDemand(int intercept, int slope)
    {

        //turn off all demand
        for(Customer c : demand)
        {
            c.turnOff();
        }
        model.getAgents().removeAll(demand);
        demand.clear();
        setDemandSlope(slope);
        setDemandIntercept(intercept);
        assert goodMarket.getBuyers().size() == 0;

        createDemand();


    }


    public void resetLaborSupply(int intercept, int slope)
    {

        //turn off all demand
        for(Person p : workers)
        {
            p.turnOff();
        }
        model.getAgents().removeAll(workers);
        workers.clear();
        setDailyWageSlope(slope);
        setDailyWageIntercept(intercept);

        createLaborSupply();



    }

    /**
     * Sets new should you rehire workers every day? To have an effect it has to be set BEFORE start is called!.
     *
     * @param workersToBeRehiredEveryDay New value of should you rehire workers every day? To have an effect it has to be set BEFORE start is called!.
     */
    public void setWorkersToBeRehiredEveryDay(boolean workersToBeRehiredEveryDay) {
        this.workersToBeRehiredEveryDay = workersToBeRehiredEveryDay;
    }

    /**
     * Gets should you rehire workers every day? To have an effect it has to be set BEFORE start is called!.
     *
     * @return Value of should you rehire workers every day? To have an effect it has to be set BEFORE start is called!.
     */
    public boolean isWorkersToBeRehiredEveryDay() {
        return workersToBeRehiredEveryDay;
    }

    public List<PlantControl> getMaximizers() {
        return maximizers;
    }

    public int getBuyerDelay() {
        return buyerDelay;
    }

    public void setBuyerDelay(int buyerDelay) {
        this.buyerDelay = buyerDelay;
    }

}
