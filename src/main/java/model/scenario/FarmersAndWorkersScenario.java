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
import agents.firm.purchases.prediction.PurchasesPredictor;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentFactory;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.exploration.BuyerSearchAlgorithm;
import agents.firm.sales.exploration.SellerSearchAlgorithm;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.prediction.SalesPredictor;
import agents.firm.sales.pricing.pid.InventoryBufferSalesControl;
import agents.people.*;
import com.google.common.base.Preconditions;
import financial.market.EndOfPhaseOrderHandler;
import financial.market.OrderBookMarket;
import financial.utilities.BuyerSetPricePolicy;
import financial.utilities.ShopSetPricePolicy;
import goods.GoodType;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.utilities.logs.LogLevel;
import model.utilities.logs.LogToFile;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

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

    private final static int FIRM_BUDGET = 5000000;

    /**
     * total number of agents in the model
     */
    private int numberOfAgents = 200;

    /**
     * how much gets produced daily by each worker hired.
     */
    private int linearProductionPerWorker = 10;

    /**
     * number of firms producing
     */
    private int numberOfFirms = 1;


    /**
     * optional, if supplied it allows for any modification to the original/default sales-department. This is called AFTER the predictor supplier
     */
    private Consumer<SalesDepartment> salesDepartmentManipulator;

    /**
     * optional, if supplied it allows for any modification to the original/default hr-department. This is called AFTER the predictor supplier
     */
    private Consumer<HumanResources> hrManipulator;

    /**
     * optional supplier for a custom hr predictor
     */
    private Supplier<PurchasesPredictor> hrPredictorSupplier;


    /**
     * optional supplier for a custom sales predictor
     */
    private Supplier<SalesPredictor> salesPredictorSupplier;
    private OrderBookMarket goodMarket;


    /**
     * Creates the scenario object, so that it links to the model.
     *
     *
     * @param model
     */
    public FarmersAndWorkersScenario(MacroII model) {
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

        goodMarket = new OrderBookMarket(MANUFACTURED);
        goodMarket.setMoney(AGRICULTURE);
        goodMarket.setOrderHandler(new EndOfPhaseOrderHandler(), model);
        goodMarket.setPricePolicy(new ShopSetPricePolicy());
        getMarkets().put(MANUFACTURED, goodMarket);


        for(int i=0; i<numberOfAgents; i++)
        {
            people.add(createPerson(i+1,.5f,getModel(),laborMarket, goodMarket));
        }

        //create firm
        for(int i=0; i<numberOfFirms; i++)
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
        firm.receiveMany(AGRICULTURE, FIRM_BUDGET);

        //sales department
        SalesDepartment salesDepartment = SalesDepartmentFactory.incompleteSalesDepartment(firm, goodMarket,
                new SimpleBuyerSearch(goodMarket, firm), new SimpleSellerSearch(goodMarket, firm), SalesDepartmentOneAtATime.class);
        //give the sale department a simple PID
        final InventoryBufferSalesControl strategy = new InventoryBufferSalesControl(salesDepartment,100,200);
        strategy.setGains(strategy.getProportionalGain() / 100, strategy.getIntegralGain() / 100,  0);

        /*
        strategy.decorateController(pid->{PIDAutotuner a = new PIDAutotuner(pid);
            a.setValidateInput( input -> strategy.getPhase().equals(InventoryBufferSalesControl.SimpleInventoryAndFlowPIDPhase.SELL));
            return a;
        });
*/
        salesDepartment.setAskPricingStrategy(strategy);

        //create custom predictor, if needed.
        if(salesPredictorSupplier != null)
            salesDepartment.setPredictorStrategy(salesPredictorSupplier.get());
        //finally register it!
        final GoodType goodTypeSold = MANUFACTURED;

        //further manipulate sales if needed
        if(salesDepartmentManipulator != null)
            salesDepartmentManipulator.accept(salesDepartment);


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
        if(hrPredictorSupplier != null)
            hr.setPredictor(hrPredictorSupplier.get());
        hr.setFixedPayStructure(true);

        //further manipulate hr if needed
        if(hrManipulator != null)
            hrManipulator.accept(hr);

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


    private static enum PopulationDataType{

        TOTAL_AGRICULTURAL_PRODUCTION,

        TOTAL_MANUFACTURED_PRODUCTION,

        TOTAL_AGRICULTURAL_CONSUMPTION,

        TOTAL_MANUFACTURED_CONSUMPTION,

        CASH_RESERVES,

        OUTPUT_INVENTORY,

        CHANGE_IN_INVENTORY,

        CHANGE_IN_CASH_RESERVES,

        GDP,

        TOTAL_UTILITY,

        GINI_COEFFICIENT


    }


    public void attachLogger(Path file)
    {
        Preconditions.checkArgument(producers.size() > 0, "start hasn't been called yet!");
        producers.get(0).addLogEventListener(new LogToFile(file, LogLevel.DEBUG,model));
    }


    /**
     * Sets new number of firms producing.
     *
     * @param numberOfFirms New value of number of firms producing.
     */
    public void setNumberOfFirms(int numberOfFirms) {
        this.numberOfFirms = numberOfFirms;
    }

    /**
     * Gets number of firms producing.
     *
     * @return Value of number of firms producing.
     */
    public int getNumberOfFirms() {
        return numberOfFirms;
    }


    /**
     * Gets optional supplier for a custom sales predictor.
     *
     * @return Value of optional supplier for a custom sales predictor.
     */
    public Supplier<SalesPredictor> getSalesPredictorSupplier() {
        return salesPredictorSupplier;
    }

    /**
     * Sets new optional supplier for a custom sales predictor.
     *
     * @param salesPredictorSupplier New value of optional supplier for a custom sales predictor.
     */
    public void setSalesPredictorSupplier(Supplier<SalesPredictor> salesPredictorSupplier) {
        this.salesPredictorSupplier = salesPredictorSupplier;
    }

    /**
     * Sets new optional supplier for a custom hr predictor.
     *
     * @param hrPredictorSupplier New value of optional supplier for a custom hr predictor.
     */
    public void setHrPredictorSupplier(Supplier<PurchasesPredictor> hrPredictorSupplier) {
        this.hrPredictorSupplier = hrPredictorSupplier;
    }

    /**
     * Gets optional supplier for a custom hr predictor.
     *
     * @return Value of optional supplier for a custom hr predictor.
     */
    public Supplier<PurchasesPredictor> getHrPredictorSupplier() {
        return hrPredictorSupplier;
    }

    public OrderBookMarket getGoodMarket() {
        return goodMarket;
    }

    public Consumer<HumanResources> getHrManipulator() {
        return hrManipulator;
    }

    public void setHrManipulator(Consumer<HumanResources> hrManipulator) {
        this.hrManipulator = hrManipulator;
    }

    public Consumer<SalesDepartment> getSalesDepartmentManipulator() {
        return salesDepartmentManipulator;
    }

    public void setSalesDepartmentManipulator(Consumer<SalesDepartment> salesDepartmentManipulator) {
        this.salesDepartmentManipulator = salesDepartmentManipulator;
    }
}
