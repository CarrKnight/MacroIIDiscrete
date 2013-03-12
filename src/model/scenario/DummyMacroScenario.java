package model.scenario;

import agents.DummyPerson;
import agents.Person;
import agents.firm.Firm;
import agents.firm.cost.InputCostStrategy;
import agents.firm.owner.TotalDividendStrategy;
import agents.firm.personell.HumanResources;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import financial.OrderBookMarket;
import financial.utilities.ShopSetPricePolicy;
import goods.GoodType;
import agents.firm.production.Blueprint;
import agents.firm.production.Plant;
import agents.firm.production.control.facades.DiscreteSlowPlantControl;
import agents.firm.production.technology.LinearConstantMachinery;
import model.MacroII;
import sim.engine.SimState;
import sim.engine.Steppable;

import static org.mockito.Mockito.*;

/**
 * <h4>Description</h4>
 * <p/> A complete market (with workers being also the buyers and sellers) and a single monopolist firm.
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-09-20
 * @see
 */
public class DummyMacroScenario extends Scenario {


    private float averageDemandFood = 50;

    private float stdDemandFood = 10;

    private float averageConsumptionSpeed = 10;

    private float stdConsumptionSpeed = 2;

    private float averageMinimumWage = 200;

    private float stdMinimumWage = 50;

    private float averageShoppingSpeed = 5;

    private float stdShoppingSpeed = 1;

    private int population = 200;

    private int firmPopulation = 1;

    private boolean initialCoupon = false;


    /**
     * Called by MacroII, it creates agents and then schedules them.
     */
    @Override
    public void start() {

        //Beef Market
        final OrderBookMarket beefMarket= new OrderBookMarket(GoodType.BEEF);
       // beefMarket.setPricePolicy(new ShopSetPricePolicy()); //make the seller price matter
        getMarkets().put(GoodType.BEEF,beefMarket);


        //labor market!
        final OrderBookMarket laborMarket= new OrderBookMarket(GoodType.LABOR);
        laborMarket.setPricePolicy(new ShopSetPricePolicy()); //make the seller price matter
        getMarkets().put(GoodType.LABOR,laborMarket);

        /*******************************
         * Create 50 workers
         ******************************/
        Person[] persons = new Person[population]; //to randomly choose a owner
        for(int i=0; i<population; i++)
        {

            int minimumWage = Math.round((float)(averageMinimumWage + getModel().random.nextGaussian() *  stdMinimumWage));
            final DummyPerson p = new DummyPerson(getModel(),0l,minimumWage,laborMarket);
            p.setName("p"+i);

            getAgents().add(p);
            persons[i] = p;

            if(initialCoupon)
                p.earn(100l);

            //schedule entry in the labor market AND demand
            getModel().schedule.scheduleOnceIn(Math.max(5f + getModel().random.nextGaussian(),.01f),new Steppable() {
                @Override
                public void step(SimState simState) {


                    p.start();
                    //generate demand
                    long maxPrice =  Math.max(Math.round((float)(averageDemandFood + getModel().random.nextGaussian() *  stdDemandFood)),0);
                    long consumptionTime =  Math.max(Math.round((float)(averageConsumptionSpeed + getModel().random.nextGaussian() *  stdConsumptionSpeed)),1l);
                    long speed =  Math.max(Math.round((float)(averageShoppingSpeed + getModel().random.nextGaussian() *  stdShoppingSpeed)),2);

                    beefMarket.registerBuyer(p);
                    p.addDemand(GoodType.BEEF,maxPrice,consumptionTime,speed,beefMarket);




                }
            }  );


        }


        /*******************************
         * Firms!
         ******************************/

        for(int i=0; i < firmPopulation; i++)
        {
            //only one seller
            final Firm seller = new Firm(getModel());
            seller.setOwner(persons[i]);
            seller.setDividendStrategy(TotalDividendStrategy.getInstance());
            seller.earn(1000000000l);
            //set up the firm at time 1
            getModel().schedule.scheduleOnce(new Steppable() {
                @Override
                public void step(SimState simState) {
                    //sales department
                    SalesDepartment dept =SalesDepartment.incompleteSalesDepartment(seller, beefMarket,
                            new SimpleBuyerSearch(beefMarket, seller), new SimpleSellerSearch(beefMarket, seller));
                    seller.registerSaleDepartment(dept, GoodType.BEEF);
                    dept.setAskPricingStrategy(new SimpleFlowSellerPID(dept)); //set strategy to PID

                    //add the plant
                    Blueprint blueprint = new Blueprint.Builder().output(GoodType.BEEF, 1).build();
                    Plant plant = new Plant(blueprint, seller);
                    plant.setPlantMachinery(new LinearConstantMachinery(GoodType.CAPITAL, mock(Firm.class), 0, plant));
                    plant.setCostStrategy(new InputCostStrategy(plant));
                    seller.addPlant(plant);


                    //human resources
                    HumanResources hr = HumanResources.getHumanResourcesIntegrated(100000000, seller,
                            laborMarket, plant, DiscreteSlowPlantControl.class, null, null).getDepartment();
          //          seller.registerHumanResources(plant, hr);
            //        hr.setProbabilityForgetting(.05f);
                    hr.start();


                }
            });

            getAgents().add(seller);
        }


    }

    /**
     * Creates the scenario object, so that it links to the model.
     * =
     */
    public DummyMacroScenario(MacroII model) {
        super(model);
    }


    public float getAverageDemandFood() {
        return averageDemandFood;
    }

    public void setAverageDemandFood(float averageDemandFood) {
        this.averageDemandFood = averageDemandFood;
    }

    public float getStdDemandFood() {
        return stdDemandFood;
    }

    public void setStdDemandFood(float stdDemandFood) {
        this.stdDemandFood = stdDemandFood;
    }

    public float getAverageConsumptionSpeed() {
        return averageConsumptionSpeed;
    }

    public void setAverageConsumptionSpeed(float averageConsumptionSpeed) {
        this.averageConsumptionSpeed = averageConsumptionSpeed;
    }

    public float getStdConsumptionSpeed() {
        return stdConsumptionSpeed;
    }

    public void setStdConsumptionSpeed(float stdConsumptionSpeed) {
        this.stdConsumptionSpeed = stdConsumptionSpeed;
    }

    public float getAverageMinimumWage() {
        return averageMinimumWage;
    }

    public void setAverageMinimumWage(float averageMinimumWage) {
        this.averageMinimumWage = averageMinimumWage;
    }

    public float getStdMinimumWage() {
        return stdMinimumWage;
    }

    public void setStdMinimumWage(float stdMinimumWage) {
        this.stdMinimumWage = stdMinimumWage;
    }

    public float getAverageShoppingSpeed() {
        return averageShoppingSpeed;
    }

    public void setAverageShoppingSpeed(float averageShoppingSpeed) {
        this.averageShoppingSpeed = averageShoppingSpeed;
    }

    public float getStdShoppingSpeed() {
        return stdShoppingSpeed;
    }

    public void setStdShoppingSpeed(float stdShoppingSpeed) {
        this.stdShoppingSpeed = stdShoppingSpeed;
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    public int getFirmPopulation() {
        return firmPopulation;
    }

    public void setFirmPopulation(int firmPopulation) {
        this.firmPopulation = firmPopulation;
    }


    public boolean isInitialCoupon() {
        return initialCoupon;
    }

    public void setInitialCoupon(boolean initialCoupon) {
        this.initialCoupon = initialCoupon;
    }
}
