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
import agents.firm.production.control.facades.MarginalPlantControl;
import agents.firm.production.control.facades.MarginalPlantControlWithPIDUnit;
import agents.firm.production.technology.LinearConstantMachinery;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.pid.PurchasesWeeklyPID;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentAllAtOnce;
import agents.firm.sales.SalesDepartmentFactory;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.prediction.MarketSalesPredictor;
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
import model.utilities.stats.DailyStatCollector;
import model.utilities.pid.CascadePIDController;
import sim.engine.SimState;
import sim.engine.Steppable;
import model.utilities.dummies.DummyBuyer;

import java.io.FileWriter;
import java.io.IOException;

import static org.mockito.Mockito.*;

/**
 * <h4>Description</h4>
 * <p/>  There are going to be three goods. CATTLE ---> BEEF ----> FOOD. So that beef needs cattle as input, food needs beef.
 * <p/> Beef is consumed by dummy buyers, the rest is consumed only by other firms.
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-01-09
 * @see
 */
public class SupplyChainScenario extends Scenario
{

    public SupplyChainScenario(MacroII model) {
        super(model);
        //instantiate the map
    }

    /**
     * total number of firms producing cattle
     */
    private int numberOfCattleProducers = 1;


    /**
     * The type of integrated control that is used by human resources in firms to choose production
     */
    private Class<? extends PlantControl> controlType = MarginalPlantControl.class;

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
















    }

    private void buildLaborSupplies() {
        addWorkers(getMarkets().get(GoodType.LABOR),5,600,5);
        addWorkers(getMarkets().get(GoodType.LABOR_BEEF),5,600,5);
        addWorkers(getMarkets().get(GoodType.LABOR_FOOD),5,600,5);
    }

    private void populateMarkets() {
        for(int i=0; i < numberOfCattleProducers; i++)
            createFirm(getMarkets().get(GoodType.CATTLE),getMarkets().get(GoodType.LABOR));
        //beef market
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
        Market cattle = new OrderBookMarket(GoodType.CATTLE);
        cattle.setPricePolicy(new ShopSetPricePolicy());
        getMarkets().put(cattle.getGoodType(),cattle);
        Market beef = new OrderBookMarket(GoodType.BEEF);
        beef.setPricePolicy(new ShopSetPricePolicy());
        getMarkets().put(beef.getGoodType(),beef);
        Market food = new OrderBookMarket(GoodType.FOOD);
        food.setPricePolicy(new ShopSetPricePolicy());
        getMarkets().put(food.getGoodType(),food);


        Market cattleLabor = new OrderBookMarket(GoodType.LABOR);
        cattleLabor.setPricePolicy(new BuyerSetPricePolicy());
        getMarkets().put(cattleLabor.getGoodType(),cattleLabor);
        Market beefLabor = new OrderBookMarket(GoodType.LABOR_BEEF);
        beefLabor.setPricePolicy(new BuyerSetPricePolicy());
        getMarkets().put(beefLabor.getGoodType(),beefLabor);
        Market foodLabor = new OrderBookMarket(GoodType.LABOR_FOOD);
        beefLabor.setPricePolicy(new BuyerSetPricePolicy());
        getMarkets().put(foodLabor.getGoodType(),foodLabor);
    }

    private void createFirm(final Market goodmarket, final Market laborMarket)
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
                        SalesDepartmentAllAtOnce.class);
                firm.registerSaleDepartment(dept, goodmarket.getGoodType());
                SmoothedDailyInventoryPricingStrategy strategy = new SmoothedDailyInventoryPricingStrategy(dept);
               // strategy.setProductionCostOverride(false);
                dept.setAskPricingStrategy(strategy); //set strategy to PID
                dept.setPredictorStrategy(new MarketSalesPredictor());


                //CREATE THE PLANT + Human resources
                Blueprint blueprint =  getBluePrint(goodmarket.getGoodType());
                Plant plant = new Plant(blueprint, firm);
                plant.setPlantMachinery(new LinearConstantMachinery(GoodType.CAPITAL, mock(Firm.class), 0, plant));
                plant.setCostStrategy(new InputCostStrategy(plant));
                firm.addPlant(plant);
                HumanResources hr = HumanResources.getHumanResourcesIntegrated(Long.MAX_VALUE, firm,
                        laborMarket, plant, controlType, null, null).getDepartment();
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
        final DummyBuyer buyer = new DummyBuyer(getModel(), reservationPrice,marketToBuyFrom){
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
            case CATTLE:
                return new Blueprint.Builder().output(GoodType.CATTLE,1).build();
            case BEEF:
                return Blueprint.simpleBlueprint(GoodType.CATTLE,1,GoodType.BEEF,beefMultiplier);
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

            p.start(getModel());

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
     * Sets new total number of firms producing cattle.
     *
     * @param numberOfCattleProducers New value of total number of firms producing cattle.
     */
    public void setNumberOfCattleProducers(int numberOfCattleProducers) {
        this.numberOfCattleProducers = numberOfCattleProducers;
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
     * Gets total number of firms producing cattle.
     *
     * @return Value of total number of firms producing cattle.
     */
    public int getNumberOfCattleProducers() {
        return numberOfCattleProducers;
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
        SupplyChainScenario scenario1 = new SupplyChainScenario(macroII);
        scenario1.controlType = MarginalPlantControlWithPIDUnit.class;





        macroII.setScenario(scenario1);
        macroII.start();

        //create the CSVWriter
        try {
            CSVWriter writer = new CSVWriter(new FileWriter("supplychain.csv"));
            DailyStatCollector collector = new DailyStatCollector(macroII,writer);
            collector.start();

        } catch (IOException e) {
            System.err.println("failed to create the file!");
        }





        while(macroII.schedule.getTime()<3500)
            macroII.schedule.step(macroII);


    }

}