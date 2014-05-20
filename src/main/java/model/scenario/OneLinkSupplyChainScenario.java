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
import agents.firm.personell.FactoryProducedHumanResourcesWithMaximizerAndTargeter;
import agents.firm.personell.HumanResources;
import agents.firm.production.Blueprint;
import agents.firm.production.Plant;
import agents.firm.production.control.TargetAndMaximizePlantControl;
import agents.firm.production.control.maximizer.PeriodicMaximizer;
import agents.firm.production.control.maximizer.WorkforceMaximizer;
import agents.firm.production.control.maximizer.algorithms.WorkerMaximizationAlgorithm;
import agents.firm.production.control.maximizer.algorithms.marginalMaximizers.MarginalMaximizer;
import agents.firm.production.control.targeter.PIDTargeterWithQuickFiring;
import agents.firm.production.technology.LinearConstantMachinery;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.pid.PurchasesFixedPID;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentFactory;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.exploration.BuyerSearchAlgorithm;
import agents.firm.sales.exploration.SellerSearchAlgorithm;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.pricing.pid.SalesControlWithFixedInventoryAndPID;
import agents.people.QuitJobAfterWorkStrategy;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import financial.market.EndOfPhaseOrderHandler;
import financial.market.Market;
import financial.market.OrderBookMarket;
import financial.utilities.BuyerSetPricePolicy;
import financial.utilities.ShopSetPricePolicy;
import goods.DifferentiatedGoodType;
import goods.GoodType;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.Deactivatable;
import model.utilities.dummies.Customer;
import model.utilities.filters.ExponentialFilter;
import model.utilities.pid.PIDController;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.LinkedList;


/**
 * <h4>Description</h4>
 * <p/> Very similar to supply-chain scenario, but simpler: Beef-->Food.
 * <p/> Beef only requires labor, food deals directly with fixed market
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-02-20
 * @see
 */
public class OneLinkSupplyChainScenario extends Scenario implements Deactivatable {


    /**
     * what used to be "beef" and now is wood. The input good for the final output
     */
    public static final GoodType INPUT_GOOD = new UndifferentiatedGoodType("inputTest","wood");


    /**
     * what used to be "beef" and now is wood. The input good for the final output
     */
    public static final GoodType INPUT_LABOR = new UndifferentiatedGoodType("inputLaborTest","Lumberjacks",false,true);

    /**
     * the final outpu!
     */
    public static final GoodType OUTPUT_GOOD = new UndifferentiatedGoodType("outputTest","furniture");

    /**
     * what used to be "beef" and now is wood. The input good for the final output
     */
    public static final GoodType OUTPUT_LABOR = new UndifferentiatedGoodType("outputLaborTest","Carpenters",false,true);




    private int beefTargetInventory = 100;
    private int foodTargetInventory = 100;
    /**
     * The filter to attach to the beef ask pricing strategy
     */
    public ExponentialFilter<Integer> beefPriceFilterer = null;
    /**
     * If you want to change the proportional gain of BEEF selling pid
     * for this scenario run by dividing it, here's what you are dividing it for
     */
    public float divideProportionalGainByThis = 1f;
    /**
     * If you want to change the proportional gain of BEEF selling pid
     * for this scenario run by dividing it, here's what you are dividing it for
     */
    public float divideIntegrativeGainByThis = 1f;

    /**
     * the sampling speed of the BEEF selling pid
     */
    public int beefPricingSpeed = 100;

    private Class< ? extends WorkforceMaximizer> maximizerType =  PeriodicMaximizer.class;

    private LinkedList<WorkforceMaximizer> maximizers = new LinkedList<>();

    /**
     * should workers act like a flow rather than a stock?
     */
    private boolean workersToBeRehiredEveryDay = true;

    public OneLinkSupplyChainScenario(MacroII model) {
        super(model);
        //instantiate the map
    }


    /**
     * The type of integrated control that is used by human resources in firms to choose production
     */
    private Class<? extends WorkerMaximizationAlgorithm> controlType = MarginalMaximizer.class;

    /**
     * the type of sales department firms use
     */
    private Class<? extends  SalesDepartment> salesDepartmentType = SalesDepartmentOneAtATime.class;

    /**
     * total number of firms producing beef
     */
    private int numberOfBeefProducers = 1;

    /**
     * total number of firms producing food
     */
    private int numberOfFoodProducers = 1;

    /**
     * how many cattles you need for one unit of beef
     */
    private int beefMultiplier = 1;

    /**
     * how many units of beef you need for 1 unit of food
     */
    private int foodMultiplier = 1;


    //this is public only so that I can log it!
    @VisibleForTesting
    public SalesControlWithFixedInventoryAndPID strategy2;


    /**
     * Called by MacroII, it creates agents and then schedules them.
     */
    @Override
    public void start()
    {
        maximizers.clear();


        //build markets and put firms in them
        instantiateMarkets();
        populateMarkets();

        //create consumers
        buildFoodDemand(0,101,1,getMarkets().get(OUTPUT_GOOD));

        //create workers
        buildLaborSupplies();

        model.registerDeactivable(this);



    }

    private void buildLaborSupplies() {
        addWorkers(getMarkets().get(INPUT_LABOR),1,120,1);
        addWorkers(getMarkets().get(OUTPUT_LABOR),1,120,1);
    }

    private void populateMarkets() {
        for(int i=0; i < numberOfBeefProducers; i++)
            createFirm(getMarkets().get(INPUT_GOOD),getMarkets().get(INPUT_LABOR));
        //food
        for(int i=0; i < numberOfFoodProducers; i++)
            createFirm(getMarkets().get(OUTPUT_GOOD),getMarkets().get(OUTPUT_LABOR));
    }

    /**
     * Instantiate all the markets
     */
    private void instantiateMarkets() {
        OrderBookMarket beef = new OrderBookMarket(INPUT_GOOD);
        beef.setPricePolicy(new ShopSetPricePolicy());
        beef.setOrderHandler(new EndOfPhaseOrderHandler(),model);
        getMarkets().put(beef.getGoodType(), beef);
        OrderBookMarket food = new OrderBookMarket(OUTPUT_GOOD);
        food.setPricePolicy(new ShopSetPricePolicy());
        getMarkets().put(food.getGoodType(), food);
        food.setOrderHandler(new EndOfPhaseOrderHandler(),model);



        OrderBookMarket beefLabor = new OrderBookMarket(INPUT_LABOR);
        beefLabor.setPricePolicy(new BuyerSetPricePolicy());
        getMarkets().put(beefLabor.getGoodType(),beefLabor);
        beefLabor.setOrderHandler(new EndOfPhaseOrderHandler(),model);

        OrderBookMarket foodLabor = new OrderBookMarket(OUTPUT_LABOR);
        beefLabor.setPricePolicy(new BuyerSetPricePolicy());
        getMarkets().put(foodLabor.getGoodType(),foodLabor);
        foodLabor.setOrderHandler(new EndOfPhaseOrderHandler(),model);

    }

    protected Firm createFirm(final Market goodmarket, final Market laborMarket)
    {
        final Firm firm = new Firm(getModel());
        firm.receiveMany(UndifferentiatedGoodType.MONEY,1000000000);
        firm.setName(goodmarket.getGoodType().getName() + " producer " + getModel().random.nextInt());
        //give it a seller department at time 1

        //set up the firm at time 1
        getModel().scheduleSoon(ActionOrder.DAWN, new Steppable() {
            @Override
            public void step(SimState simState) {
                //CREATE THE SALES DEPARTMENT
                createSalesDepartment(firm, goodmarket);


                //CREATE THE PLANT + Human resources
                Blueprint blueprint =  getBluePrint(goodmarket.getGoodType());
                createPlant(blueprint, firm, laborMarket);

                //CREATE THE PURCHASES DEPARTMENTS NEEDED
                createPurchaseDepartment(blueprint, firm);

            }
        });

        getAgents().add(firm);
        return firm;
    }

    protected SalesDepartment createSalesDepartment(Firm firm, Market goodmarket) {
        SalesDepartment dept = SalesDepartmentFactory.incompleteSalesDepartment(firm, goodmarket,
                new SimpleBuyerSearch(goodmarket, firm), new SimpleSellerSearch(goodmarket, firm),
                salesDepartmentType);
        firm.registerSaleDepartment(dept, goodmarket.getGoodType());




        if(!goodmarket.getGoodType().equals(OUTPUT_GOOD))
        {

            strategy2 = new SalesControlWithFixedInventoryAndPID(dept, beefTargetInventory);
            strategy2.setGainsSlavePID(strategy2.getProportionalGain()/divideProportionalGainByThis,
                    strategy2.getIntegralGain()/divideIntegrativeGainByThis,
                    strategy2.getDerivativeGain());


            strategy2.setInitialPrice(50);
            //if you can, filter it!
            strategy2.setSpeed(beefPricingSpeed);
            dept.setAskPricingStrategy(strategy2);

/*            SimpleFlowSellerPID strategy = new SimpleFlowSellerPID(dept);
            strategy.setGains(strategy.getProportionalGain()/divideProportionalGainByThis,strategy.getIntegralGain()/divideIntegrativeGainByThis,0);
            strategy.setSpeed(beefPricingSpeed);
            dept.setAskPricingStrategy(strategy);
  */

            buildBeefSalesPredictor(dept);


        }
        else
        {
            SalesControlWithFixedInventoryAndPID strategy;
            strategy = new SalesControlWithFixedInventoryAndPID(dept, foodTargetInventory);
            strategy.setInitialPrice(model.random.nextInt(30)+70);
            // strategy.setProductionCostOverride(false);
            dept.setAskPricingStrategy(strategy); //set strategy to PID
        }

        return dept;
    }

    protected void buildBeefSalesPredictor(SalesDepartment dept) {


    }

    protected PurchasesDepartment createPurchaseDepartment(Blueprint blueprint, Firm firm) {
        PurchasesDepartment department = null;

        for(GoodType input : blueprint.getInputs().keySet()){
            department = PurchasesDepartment.getEmptyPurchasesDepartment(Integer.MAX_VALUE, firm,
                    getMarkets().get(input));
            Market market = model.getMarket(input);

            department.setOpponentSearch(new SimpleBuyerSearch(market, firm));
            department.setSupplierSearch(new SimpleSellerSearch(market, firm));


            PurchasesFixedPID control = new PurchasesFixedPID(department,200, PIDController.class,model);

            department.setControl(control);
            department.setPricingStrategy(control);
            firm.registerPurchasesDepartment(department, input);


            if(input.equals(INPUT_GOOD))
                buildFoodPurchasesPredictor(department);


        }
        return department;

    }

    public void buildFoodPurchasesPredictor(PurchasesDepartment department) {
    }


    protected HumanResources createPlant(Blueprint blueprint, Firm firm, Market laborMarket) {
        Plant plant = new Plant(blueprint, firm);
        plant.setPlantMachinery(new LinearConstantMachinery(DifferentiatedGoodType.CAPITAL, firm, 0, plant));
        plant.setCostStrategy(new InputCostStrategy(plant));
        firm.addPlant(plant);
        FactoryProducedHumanResourcesWithMaximizerAndTargeter<TargetAndMaximizePlantControl,BuyerSearchAlgorithm,
                SellerSearchAlgorithm,PIDTargeterWithQuickFiring,WorkforceMaximizer<WorkerMaximizationAlgorithm>,WorkerMaximizationAlgorithm>
                produced =
                HumanResources.getHumanResourcesIntegrated(Integer.MAX_VALUE,firm,laborMarket,plant,
                        PIDTargeterWithQuickFiring.class, maximizerType,controlType,null,null);



        maximizers.add(produced.getWorkforceMaximizer());

        HumanResources hr = produced.getDepartment();
        hr.setFixedPayStructure(true);
        return hr;
    }

    /**
     * creates dummy buyers (consumers) eating food
     */
    private void buildFoodDemand(int minPrice, int maxPrice, int increments,
                                 final Market marketToBuyFrom)
    {
        Preconditions.checkArgument(minPrice <= maxPrice);

        for(int i=minPrice; i<=maxPrice; i = i + increments)
        {
            createFoodConsumer(marketToBuyFrom, i);

        }



    }

    private void createFoodConsumer(final Market marketToBuyFrom, final int reservationPrice) {
        /**
         * For this scenario we use a different kind of dummy buyer that, after "period" passed, puts a new order in the market
         */
        final Customer buyer = new Customer(model,reservationPrice,marketToBuyFrom){

            @Override
            public String toString() {
                return "Food buyer, price: " + reservationPrice;

            }
        };

        getAgents().add(buyer);
    }


    /**
     * Given the good you have to produce, make the blueprint that makes sense. An helper method
     * @param output the goodtype you are supposed to produce
     * @return
     */
    private Blueprint getBluePrint(GoodType output)
    {
        if(output.equals(INPUT_GOOD)) {
            return new Blueprint.Builder().output(INPUT_GOOD, 1).build();
        }
        else {
            assert output.equals(OUTPUT_GOOD);
            return Blueprint.simpleBlueprint(INPUT_GOOD, 1, OUTPUT_GOOD, foodMultiplier);
        }

    }



    private void addWorkers(Market laborMarket, int minWage, int maxWage, int increments)
    {
        /************************************************
         * Add workers
         ************************************************/

        for(int i=minWage; i<maxWage; i= i + increments)
        {
            //dummy worker, really
            final Person p = new Person(getModel(),0l,i,laborMarket);
            if(workersToBeRehiredEveryDay)
                p.setAfterWorkStrategy(AfterWorkStrategy.Factory.build(QuitJobAfterWorkStrategy.class));


            getAgents().add(p);

        }
    }




    /**
     * Sets new total number of firms producing beef.
     *
     * @param numberOfBeefProducers New value of total number of firms producing beef.
     */
    public void setNumberOfBeefProducers(int numberOfBeefProducers) {
        this.numberOfBeefProducers = numberOfBeefProducers;
    }

    /**
     * Gets total number of firms producing food.
     *
     * @return Value of total number of firms producing food.
     */
    public int getNumberOfFoodProducers() {
        return numberOfFoodProducers;
    }

    /**
     * Sets new how many units of beef you need for 1 unit of food.
     *
     * @param foodMultiplier New value of how many units of beef you need for 1 unit of food.
     */
    public void setFoodMultiplier(int foodMultiplier) {
        this.foodMultiplier = foodMultiplier;
    }

    /**
     * Gets how many units of beef you need for 1 unit of food.
     *
     * @return Value of how many units of beef you need for 1 unit of food.
     */
    public int getFoodMultiplier() {
        return foodMultiplier;
    }

    /**
     * Sets new how many cattles you need for one unit of beef.
     *
     * @param beefMultiplier New value of how many cattles you need for one unit of beef.
     */
    public void setBeefMultiplier(int beefMultiplier) {
        this.beefMultiplier = beefMultiplier;
    }

    /**
     * Gets how many cattles you need for one unit of beef.
     *
     * @return Value of how many cattles you need for one unit of beef.
     */
    public int getBeefMultiplier() {
        return beefMultiplier;
    }

    /**
     * Gets total number of firms producing beef.
     *
     * @return Value of total number of firms producing beef.
     */
    public int getNumberOfBeefProducers() {
        return numberOfBeefProducers;
    }

    /**
     * Sets new total number of firms producing food.
     *
     * @param numberOfFoodProducers New value of total number of firms producing food.
     */
    public void setNumberOfFoodProducers(int numberOfFoodProducers) {
        this.numberOfFoodProducers = numberOfFoodProducers;
    }









    /**
     * Gets the type of sales department firms use.
     *
     * @return Value of the type of sales department firms use.
     */
    public Class<? extends SalesDepartment> getSalesDepartmentType() {
        return salesDepartmentType;
    }

    /**
     * Sets new the type of sales department firms use.
     *
     * @param salesDepartmentType New value of the type of sales department firms use.
     */
    public void setSalesDepartmentType(Class<? extends SalesDepartment> salesDepartmentType) {
        this.salesDepartmentType = salesDepartmentType;
    }


    /**
     * Gets If you want to change the proportional gain of BEEF selling pid
     * for this scenario run by dividing it, here's what you are dividing it for.
     *
     * @return Value of If you want to change the proportional gain of BEEF selling pid
     *         for this scenario run by dividing it, here's what you are dividing it for.
     */
    public float getDivideIntegrativeGainByThis() {
        return divideIntegrativeGainByThis;
    }

    /**
     * Gets If you want to change the proportional gain of BEEF selling pid
     * for this scenario run by dividing it, here's what you are dividing it for.
     *
     * @return Value of If you want to change the proportional gain of BEEF selling pid
     *         for this scenario run by dividing it, here's what you are dividing it for.
     */
    public float getDivideProportionalGainByThis() {
        return divideProportionalGainByThis;
    }

    /**
     * Gets the sampling speed of the BEEF selling pid.
     *
     * @return Value of the sampling speed of the BEEF selling pid.
     */
    public int getBeefPricingSpeed() {
        return beefPricingSpeed;
    }

    /**
     * Sets new the sampling speed of the BEEF selling pid.
     *
     * @param beefPricingSpeed New value of the sampling speed of the BEEF selling pid.
     */
    public void setBeefPricingSpeed(int beefPricingSpeed) {
        this.beefPricingSpeed = beefPricingSpeed;
    }


    /**
     * Sets new If you want to change the proportional gain of BEEF selling pid
     * for this scenario run by dividing it, here's what you are dividing it for.
     *
     * @param divideIntegrativeGainByThis New value of If you want to change the proportional gain of BEEF selling pid
     *                                    for this scenario run by dividing it, here's what you are dividing it for.
     */
    public void setDivideIntegrativeGainByThis(float divideIntegrativeGainByThis) {
        this.divideIntegrativeGainByThis = divideIntegrativeGainByThis;
    }

    /**
     * Sets new If you want to change the proportional gain of BEEF selling pid
     * for this scenario run by dividing it, here's what you are dividing it for.
     *
     * @param divideProportionalGainByThis New value of If you want to change the proportional gain of BEEF selling pid
     *                                     for this scenario run by dividing it, here's what you are dividing it for.
     */
    public void setDivideProportionalGainByThis(float divideProportionalGainByThis) {
        this.divideProportionalGainByThis = divideProportionalGainByThis;
    }


    /**
     * Gets The filter to attach to the beef ask pricing strategy.
     *
     * @return Value of The filter to attach to the beef ask pricing strategy.
     */
    public  ExponentialFilter<Integer> getBeefPriceFilterer() {
        return beefPriceFilterer;
    }


    /**
     * Sets new The filter to attach to the beef ask pricing strategy.
     *
     * @param beefPriceFilterer New value of The filter to attach to the beef ask pricing strategy.
     */
    public void setBeefPriceFilterer( ExponentialFilter<Integer> beefPriceFilterer) {
        this.beefPriceFilterer = beefPriceFilterer;
    }

    /**
     * Gets The type of integrated control that is used by human resources in firms to choose production.
     *
     * @return Value of The type of integrated control that is used by human resources in firms to choose production.
     */
    public Class<? extends WorkerMaximizationAlgorithm> getControlType() {
        return controlType;
    }

    /**
     * Sets new The type of integrated control that is used by human resources in firms to choose production.
     *
     * @param controlType New value of The type of integrated control that is used by human resources in firms to choose production.
     */
    public void setControlType(Class<? extends WorkerMaximizationAlgorithm> controlType) {
        this.controlType = controlType;
    }


    public Class<? extends WorkforceMaximizer> getMaximizerType() {
        return maximizerType;
    }

    public void setMaximizerType(Class<? extends WorkforceMaximizer> maximizerType) {
        this.maximizerType = maximizerType;
    }

    /**
     * Sets new should workers act like a flow rather than a stock?.
     *
     * @param workersToBeRehiredEveryDay New value of should workers act like a flow rather than a stock?.
     */
    public void setWorkersToBeRehiredEveryDay(boolean workersToBeRehiredEveryDay) {
        this.workersToBeRehiredEveryDay = workersToBeRehiredEveryDay;
    }

    /**
     * Gets should workers act like a flow rather than a stock?.
     *
     * @return Value of should workers act like a flow rather than a stock?.
     */
    public boolean isWorkersToBeRehiredEveryDay() {
        return workersToBeRehiredEveryDay;
    }

    public int getBeefTargetInventory() {
        return beefTargetInventory;
    }

    public void setBeefTargetInventory(int beefTargetInventory) {
        this.beefTargetInventory = beefTargetInventory;
    }

    public int getFoodTargetInventory() {
        return foodTargetInventory;
    }

    public void setFoodTargetInventory(int foodTargetInventory) {
        this.foodTargetInventory = foodTargetInventory;
    }

    public LinkedList<WorkforceMaximizer> getMaximizers() {
        return maximizers;
    }

    @Override
    public void turnOff() {
        maximizers.clear();
        strategy2 = null;
    }
}
