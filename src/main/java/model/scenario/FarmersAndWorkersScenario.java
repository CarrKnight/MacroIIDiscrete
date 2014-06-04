/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.scenario;

import agents.firm.Firm;
import agents.firm.personell.FactoryProducedHumanResources;
import agents.firm.personell.HumanResources;
import agents.firm.production.Blueprint;
import agents.firm.production.Plant;
import agents.firm.production.control.PlantControl;
import agents.firm.production.control.facades.MarginalPlantControl;
import agents.firm.purchases.prediction.FixedIncreasePurchasesPredictor;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentFactory;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.exploration.BuyerSearchAlgorithm;
import agents.firm.sales.exploration.SellerSearchAlgorithm;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.prediction.FixedDecreaseSalesPredictor;
import agents.firm.sales.pricing.pid.SalesControlWithFixedInventoryAndPID;
import agents.people.*;
import financial.market.EndOfPhaseOrderHandler;
import financial.market.OrderBookMarket;
import financial.utilities.BuyerSetPricePolicy;
import financial.utilities.ShopSetPricePolicy;
import goods.GoodType;
import goods.UndifferentiatedGoodType;
import model.MacroII;

import java.util.LinkedList;
import java.util.List;

/**
 * <h4>Description</h4>
 * <p>
 * <p>
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-06-02
 * @see
 */
public class FarmersAndWorkersScenario extends Scenario {

    /**
     * agricultural goods (as well as money!)
     */
    public static final UndifferentiatedGoodType AGRICULTURE = new UndifferentiatedGoodType("10","agriculture");

    public static final UndifferentiatedGoodType MANUFACTURED = new UndifferentiatedGoodType("20","manufactured");




    /**
     * a list of all agents!
     */
    private final List<Person> people;

    /**
     * a list of all agents!
     */
    private final List<Firm> producers;

    /**
     * total number of agents in the model
     */
    private int numberOfAgents = 200;

    /**
     * how much gets produced daily by each worker hired.
     */
    private int linearProductionPerWorker = 10;

    /**
     * Creates the scenario object, so that it links to the model.
     *
     *
     * @param model
     */
    protected FarmersAndWorkersScenario(MacroII model) {
        super(model);
        people = new LinkedList<>();
        producers = new LinkedList<>();
    }

    /**
     * Called by MacroII, it creates agents and then schedules them.
     */
    @Override
    public void start() {
        getModel().getGoodTypeMasterList().addNewSectors(AGRICULTURE,MANUFACTURED);

        //create markets
        OrderBookMarket laborMarket = new OrderBookMarket(UndifferentiatedGoodType.LABOR);
        laborMarket.setMoney(AGRICULTURE);
        laborMarket.setOrderHandler(new EndOfPhaseOrderHandler(),model);
        laborMarket.setPricePolicy(new BuyerSetPricePolicy());
        getMarkets().put(UndifferentiatedGoodType.LABOR, laborMarket);

        OrderBookMarket goodMarket = new OrderBookMarket(MANUFACTURED);
        goodMarket.setMoney(AGRICULTURE);
        goodMarket.setOrderHandler(new EndOfPhaseOrderHandler(),model);
        goodMarket.setPricePolicy(new ShopSetPricePolicy());
        getMarkets().put(MANUFACTURED, goodMarket);


        for(int i=0; i<numberOfAgents; i++)
        {
            people.add(createPerson(i+1,.5f,getModel(),laborMarket,goodMarket));
        }

        //create monopolist!
        producers.add(createFirm(model, laborMarket, goodMarket));


    }

    /**
     * creates and register a new person!~
     * @param farmingProduction
     * @param agricultureExponent
     * @param model
     * @param laborMarket
     * @param goodMarket
     * @return
     */
    private Person createPerson(int farmingProduction, float agricultureExponent, MacroII model, OrderBookMarket laborMarket, OrderBookMarket goodMarket)
    {

        Person p = new Person(model);
        p.setConsumptionStrategy(ConsumptionStrategy.Factory.build(ConsumeAllStrategy.class)); //always consumes all
        p.setProductionStrategy(new ConstantProductionIfUnemployedStrategy(farmingProduction,AGRICULTURE));
        p.setUtilityFunction(new CobbDouglas2GoodsUtility(AGRICULTURE,MANUFACTURED,agricultureExponent));
        p.setMinimumDailyWagesRequired(farmingProduction);
        p.setTradingStrategy(new OneGoodBuyingAndSellingStrategy(goodMarket,p,model));
        p.setAfterWorkStrategy(AfterWorkStrategy.Factory.build(QuitJobAfterWorkStrategy.class));


        assert laborMarket.getMoney().equals(AGRICULTURE);
        p.setLaborMarket(laborMarket);
        laborMarket.registerSeller(p);
        model.addAgent(p);
        p.setName("person" + farmingProduction);

        return p;


    }

    private Firm createFirm(MacroII model, OrderBookMarket laborMarket, OrderBookMarket goodMarket)
    {
        Firm firm = new Firm(model);
        firm.receiveMany(AGRICULTURE,50000);

        //sales department
        SalesDepartment salesDepartment = SalesDepartmentFactory.incompleteSalesDepartment(firm, goodMarket,
                new SimpleBuyerSearch(goodMarket, firm), new SimpleSellerSearch(goodMarket, firm), SalesDepartmentOneAtATime.class);
        //give the sale department a simple PID
        final SalesControlWithFixedInventoryAndPID strategy = new SalesControlWithFixedInventoryAndPID(salesDepartment,1000);
        strategy.setGainsSlavePID(strategy.getProportionalGain() / 100, strategy.getIntegralGain() / 100, strategy.getDerivativeGain() / 100);

        salesDepartment.setAskPricingStrategy(strategy);

        salesDepartment.setPredictorStrategy(new FixedDecreaseSalesPredictor(0));
        //finally register it!
        final GoodType goodTypeSold = MANUFACTURED;
        firm.registerSaleDepartment(salesDepartment, goodTypeSold);

        //plant
        Blueprint blueprint = (new Blueprint.Builder()).output(goodTypeSold,linearProductionPerWorker).build();
        Plant plant =  Plant.buildSimplePlantToFirm(firm, blueprint);

        //hr
        HumanResources hr;
        final FactoryProducedHumanResources<? extends PlantControl,BuyerSearchAlgorithm,SellerSearchAlgorithm> factoryMadeHR =
                HumanResources.getHumanResourcesIntegrated(Integer.MAX_VALUE, firm,
                        laborMarket, plant, MarginalPlantControl.class, null, null);
        hr = factoryMadeHR.getDepartment();
        hr.setPredictor(new FixedIncreasePurchasesPredictor(0));
        hr.setFixedPayStructure(true);


        model.addAgent(firm);

        return firm;
    }


    public int countAgricultureConsumption()
    {
        int sum = 0;
        for(Person p : people)
            sum += p.getTodayConsumption(AGRICULTURE);

        return sum;

    }

    public int countAgricultureProduction()
    {
        int sum = 0;
        for(Person p : people)
            sum += p.getTodayProduction(AGRICULTURE);

        return sum;

    }

    public int countManufacturedConsumption()
    {
        int sum = 0;
        for(Person p : people)
            sum += p.getTodayConsumption(MANUFACTURED);

        return sum;

    }

    public int countManufacturedProduction()
    {
        int sum = 0;
        for(Firm f : producers)
            sum += f.getTodayProduction(MANUFACTURED);

        return sum;

    }

    public int getNumberOfAgents() {
        return numberOfAgents;
    }

    public void setNumberOfAgents(int numberOfAgents) {
        this.numberOfAgents = numberOfAgents;
    }

    public int getLinearProductionPerWorker() {
        return linearProductionPerWorker;
    }

    public void setLinearProductionPerWorker(int linearProductionPerWorker) {
        this.linearProductionPerWorker = linearProductionPerWorker;
    }

    public List<Person> getPeople() {
        return people;
    }

    public List<Firm> getProducers() {
        return producers;
    }
}
