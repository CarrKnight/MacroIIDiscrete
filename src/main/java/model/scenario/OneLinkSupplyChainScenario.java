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
import agents.firm.personell.FactoryProducedHumanResources;
import agents.firm.personell.HumanResources;
import agents.firm.production.Blueprint;
import agents.firm.production.Plant;
import agents.firm.production.control.PlantControl;
import agents.firm.production.control.facades.MarginalPlantControl;
import agents.firm.production.control.facades.MarginalPlantControlWithPIDUnit;
import agents.firm.production.technology.LinearConstantMachinery;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.pid.PurchasesWeeklyPID;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentAllAtOnce;
import agents.firm.sales.SalesDepartmentFactory;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.pricing.pid.SmoothedDailyInventoryPricingStrategy;
import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.base.Preconditions;
import financial.Market;
import financial.OrderBookMarket;
import financial.utilities.BuyerSetPricePolicy;
import financial.utilities.ShopSetPricePolicy;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.DailyStatCollector;
import model.utilities.pid.CascadePIDController;
import sim.engine.SimState;
import sim.engine.Steppable;
import tests.DummyBuyer;

import java.io.FileWriter;
import java.io.IOException;

import static org.mockito.Mockito.*;
import static tests.tuningRuns.MarginalMaximizerWithUnitPIDTuningMultiThreaded.printProgressBar;

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
public class OneLinkSupplyChainScenario extends Scenario {




    public OneLinkSupplyChainScenario(MacroII model) {
        super(model);
        //instantiate the map
    }


    /**
     * The type of integrated control that is used by human resources in firms to choose production
     */
    private Class<? extends PlantControl> controlType = MarginalPlantControl.class;

    /**
     * the type of sales department firms use
     */
    private Class<? extends  SalesDepartment> salesDepartmentType = SalesDepartmentAllAtOnce.class;

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

    /**
     * flag that when activated and inventories are above 1500, it reduces them by a 1000
     * I am using a strategy that ignores inventory, then I can set this to true.
     *
     */
    private boolean reduceInventoriesHack = false;

    /**
     * Called by MacroII, it creates agents and then schedules them.
     */
    @Override
    public void start()
    {



        //build markets and put firms in them
        instantiateMarkets();
        populateMarkets();

        //create consumers
        buildFoodDemand(0,100,1,getMarkets().get(GoodType.FOOD));

        //create workers
        buildLaborSupplies();

        if(reduceInventoriesHack)
        {
            model.scheduleSoon(ActionOrder.CLEANUP, new Steppable() {
                @Override
                public void step(SimState state) {

                    for(EconomicAgent e : model.getAgents() )
                    {
                        for(GoodType g : GoodType.values())
                        {
                            while(e.hasHowMany(g) > 800)
                            {
                                for(int i=0; i <500; i++)
                                    e.consume(g);
                            }
                        }
                    }
                    model.scheduleTomorrow(ActionOrder.CLEANUP,this);
                }

            });
        }

    }

    private void buildLaborSupplies() {
        addWorkers(getMarkets().get(GoodType.LABOR_BEEF),5,1200,5);
        addWorkers(getMarkets().get(GoodType.LABOR_FOOD),5,1200,5);
    }

    private void populateMarkets() {
        for(int i=0; i < numberOfBeefProducers; i++)
            createFirm(getMarkets().get(GoodType.BEEF),getMarkets().get(GoodType.LABOR_BEEF));
        //food
        for(int i=0; i < numberOfBeefProducers; i++)
            createFirm(getMarkets().get(GoodType.FOOD),getMarkets().get(GoodType.LABOR_FOOD));
    }

    /**
     * Instantiate all the markets
     */
    private void instantiateMarkets() {
        Market beef = new OrderBookMarket(GoodType.BEEF);
        beef.setPricePolicy(new ShopSetPricePolicy());
        getMarkets().put(beef.getGoodType(),beef);
        Market food = new OrderBookMarket(GoodType.FOOD);
        food.setPricePolicy(new ShopSetPricePolicy());
        getMarkets().put(food.getGoodType(),food);


        Market beefLabor = new OrderBookMarket(GoodType.LABOR_BEEF);
        beefLabor.setPricePolicy(new BuyerSetPricePolicy());
        getMarkets().put(beefLabor.getGoodType(),beefLabor);
        Market foodLabor = new OrderBookMarket(GoodType.LABOR_FOOD);
        beefLabor.setPricePolicy(new BuyerSetPricePolicy());
        getMarkets().put(foodLabor.getGoodType(),foodLabor);
    }

    protected Firm createFirm(final Market goodmarket, final Market laborMarket)
    {
        final Firm firm = new Firm(getModel());
        firm.earn(Integer.MAX_VALUE);
        firm.setName(goodmarket.getGoodType().name() + " producer " + getModel().random.nextInt());
        //give it a seller department at time 1

        //set up the firm at time 1
        getModel().scheduleSoon(ActionOrder.DAWN, new Steppable() {
            @Override
            public void step(SimState simState) {
                //CREATE THE SALES DEPARTMENT
                SalesDepartment dept = SalesDepartmentFactory.incompleteSalesDepartment(firm, goodmarket,
                        new SimpleBuyerSearch(goodmarket, firm), new SimpleSellerSearch(goodmarket, firm),
                        salesDepartmentType);
                firm.registerSaleDepartment(dept, goodmarket.getGoodType());
               /* SalesControlFlowPIDWithFixedInventory strategy;
                strategy = new SalesControlFlowPIDWithFixedInventory(dept);
                */
                SmoothedDailyInventoryPricingStrategy strategy;
                strategy = new SmoothedDailyInventoryPricingStrategy(dept);
                strategy.setInitialPrice(model.random.nextInt(30)+70);

                if(!goodmarket.getGoodType().equals(GoodType.FOOD))
                {


                    strategy.setGains(strategy.getProportionalGain()/100f,0,0);
                    strategy.setInitialPrice(50);
                }

                // strategy.setProductionCostOverride(false);
                dept.setAskPricingStrategy(strategy); //set strategy to PID


                //CREATE THE PLANT + Human resources
                Blueprint blueprint =  getBluePrint(goodmarket.getGoodType());
                Plant plant = new Plant(blueprint, firm);
                plant.setPlantMachinery(new LinearConstantMachinery(GoodType.CAPITAL, mock(Firm.class), 0, plant));
                plant.setCostStrategy(new InputCostStrategy(plant));
                firm.addPlant(plant);
                FactoryProducedHumanResources produced =  HumanResources.getHumanResourcesIntegrated(Long.MAX_VALUE, firm,
                        laborMarket, plant, controlType, null, null);
                HumanResources hr = produced.getDepartment();
                hr.setFixedPayStructure(true);
                hr.start();

                //CREATE THE PURCHASES DEPARTMENTS NEEDED
                for(GoodType input : blueprint.getInputs().keySet()){
                    PurchasesDepartment department = PurchasesDepartment.getEmptyPurchasesDepartment(Long.MAX_VALUE, firm,
                            getMarkets().get(input));
                    float proportionalGain = model.drawProportionalGain();
                    float integralGain = model.drawIntegrativeGain();
                    float derivativeGain = model.drawDerivativeGain();
                    Market market = model.getMarket(input);

                    department.setOpponentSearch(new SimpleBuyerSearch(market, firm));
                    department.setSupplierSearch(new SimpleSellerSearch(market, firm));

                    PurchasesWeeklyPID control = new PurchasesWeeklyPID(department, CascadePIDController.class,model);

                    department.setControl(control);
                    department.setPricingStrategy(control);
                    firm.registerPurchasesDepartment(department, input);
                    department.start();

                }

            }
        });

        getAgents().add(firm);
        return firm;
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
        final DummyBuyer buyer = new DummyBuyer(getModel(), reservationPrice){
            @Override
            public void reactToFilledBidQuote(Good g, long price, final EconomicAgent b) {
                //trick to get the steppable to recognize the anonymous me!
                final DummyBuyer reference = this;
                //schedule a new quote in period!
                this.getModel().scheduleTomorrow(ActionOrder.TRADE, new Steppable() {
                    @Override
                    public void step(SimState simState) {
                        earn(Math.max(1000l - reference.getCash(),0));
                        //put another quote
                        marketToBuyFrom.submitBuyQuote(reference, getFixedPrice());

                    }
                });

            }

            @Override
            public String toString() {
                return "Food buyer, price: " + reservationPrice;

            }
        };


        //make it adjust once to register and submit the first quote

        getModel().scheduleSoon(ActionOrder.DAWN, new Steppable() {
            @Override
            public void step(SimState simState) {
                marketToBuyFrom.registerBuyer(buyer);
                buyer.earn(1000l);
                //make the buyer submit a quote soon.
                marketToBuyFrom.submitBuyQuote(buyer, buyer.getFixedPrice());
            }
        });

        getAgents().add(buyer);
    }


    /**
     * Given the good you have to produce, make the blueprint that makes sense. An helper method
     * @param output the goodtype you are supposed to produce
     * @return
     */
    private Blueprint getBluePrint(GoodType output)
    {
        switch (output)
        {
            case BEEF:
                return new Blueprint.Builder().output(GoodType.BEEF,1).build();
            case FOOD:
                return Blueprint.simpleBlueprint(GoodType.BEEF,1,GoodType.FOOD,foodMultiplier);
            default:
                assert false;
                return null;
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

            p.setSearchForBetterOffers(false);

            p.start();

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

    public Class<? extends PlantControl> getControlType() {
        return controlType;
    }

    public void setControlType(Class<? extends PlantControl> controlType) {
        this.controlType = controlType;
    }



    /**
     * Runs the supply chain with no GUI and writes a big CSV file
     * @param args
     */
    public static void main(String[] args)
    {


        final MacroII macroII = new MacroII(System.currentTimeMillis());
        OneLinkSupplyChainScenario scenario1 = new OneLinkSupplyChainScenario(macroII);
        scenario1.setControlType(MarginalPlantControlWithPIDUnit.class);
        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        scenario1.setReduceInventoriesHack(false);
        // scenario1.setControlType(MarginalPlantControlWithPAIDUnitAndEfficiencyAdjustment.class);





        macroII.setScenario(scenario1);
        macroII.start();

        //create the CSVWriter
        try {
            CSVWriter writer = new CSVWriter(new FileWriter("supplychainSigmoidNewPredictor.csv"));
            DailyStatCollector collector = new DailyStatCollector(macroII,writer);
            collector.start();

        } catch (IOException e) {
            System.err.println("failed to create the file!");
        }





        while(macroII.schedule.getTime()<15000)
        {
            macroII.schedule.step(macroII);
            printProgressBar(15001,(int)macroII.schedule.getSteps(),100);
        }


    }


    /**
     * Sets new flag that when activated and inventories are above 1500, it reduces them by a 1000
     * I am using a strategy that ignores inventory, then I can set this to true..
     *
     * @param reduceInventoriesHack New value of flag that when activated and inventories are above 1500, it reduces them by a 1000
     *                              I am using a strategy that ignores inventory, then I can set this to true..
     */
    public void setReduceInventoriesHack(boolean reduceInventoriesHack) {
        this.reduceInventoriesHack = reduceInventoriesHack;
    }

    /**
     * Gets flag that when activated and inventories are above 1500, it reduces them by a 1000
     * I am using a strategy that ignores inventory, then I can set this to true..
     *
     * @return Value of flag that when activated and inventories are above 1500, it reduces them by a 1000
     *         I am using a strategy that ignores inventory, then I can set this to true..
     */
    public boolean isReduceInventoriesHack() {
        return reduceInventoriesHack;
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
}
