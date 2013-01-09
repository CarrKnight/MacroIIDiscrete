package model.scenario;

import agents.EconomicAgent;
import agents.Person;
import agents.firm.Firm;
import agents.firm.cost.InputCostStrategy;
import agents.firm.personell.HumanResources;
import agents.firm.production.Blueprint;
import agents.firm.production.Plant;
import agents.firm.production.control.DiscreteSlowPlantControl;
import agents.firm.production.technology.LinearConstantMachinery;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.pid.PurchasesFixedPID;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import com.google.common.base.Preconditions;
import financial.Market;
import financial.OrderBookMarket;
import financial.utilities.BuyerSetPricePolicy;
import financial.utilities.ShopSetPricePolicy;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.pid.CascadePIDController;
import sim.engine.SimState;
import sim.engine.Steppable;
import tests.DummyBuyer;

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
    }

    /**
     * total number of firms producing cattle
     */
    private int numberOfCattleProducers = 1;

    /**
     * total number of firms producing beef
     */
    private int numberOfBeefProducers = 1;

    /**
     * total number of firms producing food
     */
    private int numberOfFoodProducers = 1    ;

    /**
     * how many cattles you need for one unit of beef
     */
    private int beefMultiplier = 2;

    /**
     * how many units of beef you need for 1 unit of food
     */
    private int foodMultiplier = 2;


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
                SalesDepartment dept = SalesDepartment.incompleteSalesDepartment(firm, goodmarket,
                        new SimpleBuyerSearch(goodmarket, firm), new SimpleSellerSearch(goodmarket, firm));
                firm.registerSaleDepartment(dept, goodmarket.getGoodType());
                dept.setAskPricingStrategy(new SimpleFlowSellerPID(dept)); //set strategy to PID

                //CREATE THE PLANT + Human resources
                Blueprint blueprint =  getBluePrint(goodmarket.getGoodType());
                Plant plant = new Plant(blueprint, firm);
                plant.setPlantMachinery(new LinearConstantMachinery(GoodType.CAPITAL, mock(Firm.class), 0, plant));
                plant.setCostStrategy(new InputCostStrategy(plant));
                firm.addPlant(plant);
                HumanResources hr = HumanResources.getHumanResourcesIntegrated(Long.MAX_VALUE, firm,
                        laborMarket, plant, DiscreteSlowPlantControl.class, null, null);
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

                    PurchasesFixedPID control = new PurchasesFixedPID(department,3, CascadePIDController.class,model);

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
            case CATTLE:
                return new Blueprint.Builder().output(GoodType.CATTLE,1).build();
            case BEEF:
                return Blueprint.simpleBlueprint(GoodType.CATTLE,beefMultiplier,GoodType.BEEF,1);
            case FOOD:
                return Blueprint.simpleBlueprint(GoodType.BEEF,foodMultiplier,GoodType.FOOD,1);
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

    public int getNumberOfCattleProducers() {
        return numberOfCattleProducers;
    }

    public int getNumberOfBeefProducers() {
        return numberOfBeefProducers;
    }

    public int getNumberOfFoodProducers() {
        return numberOfFoodProducers;
    }

    public int getBeefMultiplier() {
        return beefMultiplier;
    }

    public int getFoodMultiplier() {
        return foodMultiplier;
    }
}
