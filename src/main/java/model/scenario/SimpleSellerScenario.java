/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.scenario;

import agents.firm.Firm;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentAllAtOnce;
import agents.firm.sales.SalesDepartmentFactory;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.pricing.AskPricingStrategy;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.base.Preconditions;
import financial.market.OrderBookMarket;
import financial.utilities.ShopSetPricePolicy;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.dummies.Customer;
import model.utilities.dummies.CustomerWithDelay;
import model.utilities.stats.collectors.DailyStatCollector;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import static model.experiments.tuningRuns.MarginalMaximizerPIDTuning.printProgressBar;

/**
 * <h4>Description</h4>
 * <p/> This is actually a copy of scenario1 for the test class SimpleFlowSellerPIDTest.
 * Very simple matching of supply and demand
 * <p/> Basically we periodically
 * create new buyers (after removing the old ones) to give the impression of a constant demand and let the
 * flow seller get to the right price
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-09-10
 * @see
 */
public class SimpleSellerScenario extends Scenario {




    private boolean demandShifts = false;

    /**
     * when this is >0 the demand responds to prices quoted "buyerDelay" days ago rather than current prices.
     */
    private int buyerDelay = 0;

    /**
     * the strategy used by the sales department to tinker with prices
     */
    private Class<? extends AskPricingStrategy> sellerStrategy = SimpleFlowSellerPID.class;

    /**
     * The kind of sales department to use
     */
    protected Class<? extends SalesDepartment> salesDepartmentType = SalesDepartmentAllAtOnce.class;

    /**
     * number of sellers in the model
     */
    protected int numberOfSellers = 1;

    /**
     * How many goods the seller receives every day
     */
    private int inflowPerSeller = 4;

    /**
     * If there is a demand shift, when will it appear?
     */
    public int howManyDaysLaterShockWillOccur = 2000;

    private int demandIntercept = 100;

    private int demandSlope = -10;

    private List<SalesDepartment> departments = new LinkedList<>();

    private List<AskPricingStrategy> pricingStrategies = new LinkedList<>();


    /**
     * when this is set to true during the production phase all previously unsold inventory is destroyed
     */
    private boolean destroyUnsoldInventoryEachDay = false;

    /**
     * how much inflow each firm gets
     */
    private LinkedHashMap<Firm, Integer> sellerToInflowMap = new LinkedHashMap< >();

    /**
     * the strategy used by the seller, after it has been instantiated
     */
    protected AskPricingStrategy strategy;



    /**
     * Called by MacroII, it creates agents and then schedules them.
     */
    @Override
    public void start() {

        departments = new LinkedList<>();
        pricingStrategies = new LinkedList<>();

        //create and record a new market!
        final OrderBookMarket market= new OrderBookMarket(UndifferentiatedGoodType.GENERIC);
        market.setPricePolicy(new ShopSetPricePolicy()); //make the seller price matter

        getMarkets().put(UndifferentiatedGoodType.GENERIC,market);


        //create sellers
        for(int i=0; i < numberOfSellers; i++)
        {
            final Firm seller = buildSeller(market);
            //arrange for goods to drop periodically in the firm
            setupProduction(seller);
        }


        //create demand
        int i=1;
        while(demandIntercept + i*demandSlope>0){
            buildBuyer(market, demandIntercept + i*demandSlope);
            i++;
        }



        //if demands shifts, add 10 more buyers after adjust 2000
        if(demandShifts)
        {
            //create 10 buyers
            for(i=0;i<10;i++){
                createAdditionalBuyer(market, i);




            }
        }


        //hopefully that's that!






    }

    private void createAdditionalBuyer(final OrderBookMarket market, final int i) {
        /**
         * For this scenario we use a different kind of dummy buyer that, after "period" passed, puts a new order in the market
         */
        model.scheduleAnotherDay(ActionOrder.DAWN,new Steppable() {
            @Override
            public void step(SimState state) {
                if(buyerDelay == 0)
                {
                    final Customer customer = new Customer(getModel(),(i+10)*10,market);
                    model.addAgent(customer);
                }
                else
                {
                    assert buyerDelay > 0;
                    CustomerWithDelay delay = new CustomerWithDelay(model,(i+10)*10,buyerDelay,market);
                    model.addAgent(delay);
                }
            }},howManyDaysLaterShockWillOccur);


    }

    private void setupProduction(final Firm seller) {
        sellerToInflowMap.put(seller,inflowPerSeller);
        getModel().scheduleSoon(ActionOrder.PRODUCTION,new Steppable() {
            @Override
            public void step(SimState simState) {
                if(destroyUnsoldInventoryEachDay)
                    seller.consumeAll();

                //sell 4 goods!
                int inflow = sellerToInflowMap.get(seller);
                seller.receiveMany(UndifferentiatedGoodType.GENERIC,inflow);
                seller.reactToPlantProduction(UndifferentiatedGoodType.GENERIC,inflow);

                //every day
                getModel().scheduleTomorrow(ActionOrder.PRODUCTION,this);
            }
        });
    }

    protected Firm buildSeller(final OrderBookMarket market) {
        final Firm seller = new Firm(getModel());
        getAgents().add(seller);




        SalesDepartment dept = SalesDepartmentFactory.incompleteSalesDepartment(seller, market, new SimpleBuyerSearch(market, seller),
                new SimpleSellerSearch(market, seller), salesDepartmentType);
        seller.registerSaleDepartment(dept, UndifferentiatedGoodType.GENERIC);

        strategy = AskPricingStrategy.Factory.newAskPricingStrategy(sellerStrategy,dept);



        //strategy.setSpeed(sellerDelay);
        dept.setAskPricingStrategy(strategy); //set strategy to PID

        departments.add(dept);
        pricingStrategies.add(strategy);

        return seller;
    }

    protected void buildBuyer(final OrderBookMarket market, final int price) {
        if(buyerDelay == 0 )
        {
            final Customer buyer = new Customer(getModel(),Math.max((price),1),market);
            getAgents().add(buyer);
        }
        else
        {
            CustomerWithDelay delay = new CustomerWithDelay(model,Math.max((price),1),buyerDelay,market);
            getAgents().add(delay);
        }
    }

    /**
     * Creates the scenario object, so that it links to the model.
     * =
     */
    public SimpleSellerScenario(MacroII model) {
        super(model);
    }


    public boolean isDemandShifts() {
        return demandShifts;
    }

    public void setDemandShifts(boolean demandShifts) {
        this.demandShifts = demandShifts;
    }

    /**
     * Gets the strategy used by the sales department to tinker with prices.
     *
     * @return Value of the strategy used by the sales department to tinker with prices.
     */
    public Class<? extends AskPricingStrategy> getSellerStrategy() {
        return sellerStrategy;
    }

    /**
     * Sets new the strategy used by the sales department to tinker with prices.
     *
     * @param sellerStrategy New value of the strategy used by the sales department to tinker with prices.
     */
    public void setSellerStrategy(Class<? extends AskPricingStrategy> sellerStrategy) {
        this.sellerStrategy = sellerStrategy;
    }


    /**
     * Gets The kind of sales department to use.
     *
     * @return Value of The kind of sales department to use.
     */
    public Class<? extends SalesDepartment> getSalesDepartmentType() {
        return salesDepartmentType;
    }

    /**
     * Sets new The kind of sales department to use.
     *
     * @param salesDepartmentType New value of The kind of sales department to use.
     */
    public void setSalesDepartmentType(Class<? extends SalesDepartment> salesDepartmentType) {
        this.salesDepartmentType = salesDepartmentType;
    }

    public int getBuyerDelay() {
        return buyerDelay;
    }

    public void setBuyerDelay(int buyerDelay) {
        Preconditions.checkArgument(buyerDelay >=0);
        this.buyerDelay = buyerDelay;
    }


    /**
     * Gets How many goods the seller receives every day.
     *
     * @return Value of How many goods the seller receives every day.
     */
    public int getInflowPerSeller() {
        return inflowPerSeller;
    }

    /**
     * Sets new number of sellers in the model.
     *
     * @param numberOfSellers New value of number of sellers in the model.
     */
    public void setNumberOfSellers(int numberOfSellers) {
        this.numberOfSellers = numberOfSellers;
    }

    /**
     * Gets number of sellers in the model.
     *
     * @return Value of number of sellers in the model.
     */
    public int getNumberOfSellers() {
        return numberOfSellers;
    }

    /**
     * Sets new How many goods the seller receives every day.
     *
     * @param inflowPerSeller New value of How many goods the seller receives every day.
     */
    public void setInflowPerSeller(int inflowPerSeller) {
        this.inflowPerSeller = inflowPerSeller;
    }





    /**
     * Runs the simple seller scenario with no GUI and writes a big CSV file  (to draw graphs for the paper)
     * @param args
     */
    public static void main(String[] args)
    {

        //do it 10 times
        for(int i=0; i<10;i++)
        {

            //set up
            final MacroII macroII = new MacroII(i);
            macroII.getRandom().setSeed(i);
            SimpleSellerScenario scenario1 = new SimpleSellerScenario(macroII);
            scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
            scenario1.setSellerStrategy(SimpleFlowSellerPID.class);
            scenario1.setDemandShifts(true);
            scenario1.setHowManyDaysLaterShockWillOccur(500);


            //assign scenario
            macroII.setScenario(scenario1);

            macroII.start();



            //CSV writer set up
            try {
                CSVWriter writer = new CSVWriter(new FileWriter("runs/simpleSeller/"+"simpleSellerShock"+i +".csv"));
                DailyStatCollector collector = new DailyStatCollector(macroII,writer);
                collector.start();

            } catch (IOException e) {
                System.err.println("failed to create the file!");
            }


            //run!
            while(macroII.schedule.getTime()<1000)
            {
                macroII.schedule.step(macroII);
                printProgressBar(1001,(int)macroII.schedule.getSteps(),100);
            }


        }

    }


    /**
     * If there is a demand shift, when will it appear?.
     *
     * @return Value of If there is a demand shift, when will it appear?.
     */
    public int getHowManyDaysLaterShockWillOccur() {
        return howManyDaysLaterShockWillOccur;
    }

    /**
     * Sets new If there is a demand shift, when will it appear?.
     *
     * @param howManyDaysLaterShockWillOccur New value of If there is a demand shift, when will it appear?.
     */
    public void setHowManyDaysLaterShockWillOccur(int howManyDaysLaterShockWillOccur) {
        this.howManyDaysLaterShockWillOccur = howManyDaysLaterShockWillOccur;
    }


    public int getDemandIntercept() {
        return demandIntercept;
    }

    public void setDemandIntercept(int demandIntercept) {
        this.demandIntercept = demandIntercept;
    }

    public int getDemandSlope() {
        return demandSlope;
    }

    public void setDemandSlope(int demandSlope) {
        Preconditions.checkArgument(demandSlope <= 0, "slope can't be positive.");
        this.demandSlope = demandSlope;
    }

    public List<SalesDepartment> getDepartments() {
        return departments;
    }


    public LinkedHashMap<Firm, Integer> getSellerToInflowMap() {
        return sellerToInflowMap;
    }


    public boolean isDestroyUnsoldInventoryEachDay() {
        return destroyUnsoldInventoryEachDay;
    }

    public void setDestroyUnsoldInventoryEachDay(boolean destroyUnsoldInventoryEachDay) {
        this.destroyUnsoldInventoryEachDay = destroyUnsoldInventoryEachDay;
    }


    public List<AskPricingStrategy> getPricingStrategies() {
        return pricingStrategies;
    }
}
