/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.scenario;

import agents.HasInventory;
import agents.InventoryListener;
import agents.firm.Firm;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.pid.PurchasesFixedPID;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import financial.MarketEvents;
import financial.market.Market;
import financial.market.OrderBookMarket;
import financial.utilities.BuyerSetPricePolicy;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.dummies.DailyGoodTree;
import model.utilities.pid.CascadePIDController;
import model.utilities.pid.Controller;
import sim.engine.SimState;
import sim.engine.Steppable;

import javax.annotation.Nonnull;
import java.util.LinkedList;

/**
 * <h4>Description</h4>
 * <p/> In this scenario we put a simple supply of 10 dummy sellers and a purchase department tasked to buy 4 goods each tenth of a week.
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *                                                              f
 * @author carrknight
 * @version 2012-11-13
 * @see
 */
public class SimpleBuyerScenario extends Scenario {

    /**
     * How often goods are consumed and supply replenished
     */
    int period = 0;

    /**
     * number of buyers in the model
     */
    int numberOfBuyers = 1;

    /**
     * Are there additional people coming to supply after 20 weeks?
     */
    boolean supplyShift = false;


    /**
     * Does the burning of the input happens immediately or in one big burst?
     */
    boolean burstConsumption = true;

    boolean filtersOn = false;

    float inputWeight = 0.5f;

    int targetInventory = 20;

    int consumptionRate = 4;


    float outputWeight = .35f;

    int pidPeriod = period;

    public int consumedThisWeek = 0;

    // private Class<? extends Controller> controllerType = CascadePIDController.class;
    private Class<? extends Controller> controllerType = CascadePIDController.class;


    private int supplyIntercept = 90;

    private int supplySlope = 10;

    private int numberOfSuppliers = 10;

    private LinkedList<PurchasesDepartment> departments;


    private float proportionalGainAVG = .5f;
    private float integralGainAVG =.5f;
    private float derivativeGainAVG =.01f;
    private PurchasesDepartment department;



    public SimpleBuyerScenario(MacroII model) {
        super(model);
    }


    /**
     * Called by MacroII, it creates agents and then schedules them.
     */
    @Override
    public void start() {

        //markets
        OrderBookMarket market = new OrderBookMarket(GoodType.GENERIC);
        getMarkets().put(GoodType.GENERIC,market);
        market.setPricePolicy(new BuyerSetPricePolicy());

        departments = new LinkedList<>();
        //create the buyer!
        for(int i=0; i< numberOfBuyers; i++)
        {
            final Firm firm = createBuyer(market);

            getAgents().add(firm); //add the buyer
        }


        for(int i=1; i <=numberOfSuppliers; i++)
            createSeller(i*supplySlope+supplyIntercept,market,0f);


        if(supplyShift)
            for(int i=0; i <10; i++)
                createSeller(i*10,market,5000f);

    }

    private Firm createBuyer(OrderBookMarket market) {
        final Firm firm = new Firm(model);
        firm.earn(Long.MAX_VALUE);
        department = PurchasesDepartment.getEmptyPurchasesDepartment(Long.MAX_VALUE, firm, market);

        department.setOpponentSearch(new SimpleBuyerSearch(market, firm));
        department.setSupplierSearch(new SimpleSellerSearch(market, firm));
        //  final PurchasesFixedPID control = new PurchasesFixedPID(department,proportionalGain,integralGain,derivativeGain,targetInventory);
        final PurchasesFixedPID control = new PurchasesFixedPID(department,targetInventory, controllerType,model);
        control.setHowManyTimesOverInventoryHasToBeOverTargetToBeTooMuch(1000f);
        control.setSpeed(pidPeriod);
        if(filtersOn){
            //filtering flows
            assert !control.isInvertInputs();
            control.filterInputExponentially(inputWeight,1);
            control.filterTargetExponentially(inputWeight,1);

        }
        department.setControl(control);
        department.setPricingStrategy(control);

        firm.registerPurchasesDepartment(department, GoodType.GENERIC);



        if(burstConsumption)
            setUpBurstConsumption(firm, department);
        else
            setUpImmediateConsumption(firm, department);

        departments.add(department);


        return firm;
    }

    /**
     * Utility method to create a seller of a specific kind!
     * @param reservationPrice the reservation price below which the seller never sells
     * @param market the market the seller sells to
     * @param initialDelay 0 if the seller starts selling immediately, any positive number if you want the seller to start acting later
     */
    private void createSeller(final int reservationPrice, final Market market,float initialDelay)
    {


        //We modify the seller to repeatedly receive and sell a new good each unit of time!
        final DailyGoodTree seller = new DailyGoodTree(model,1,reservationPrice,market);

        if(initialDelay >0)
            getModel().addAgent(seller);
        else
            getAgents().add(seller);
    }




    public boolean isBurstConsumption() {
        return burstConsumption;
    }

    public void setBurstConsumption(boolean burstConsumption) {
        this.burstConsumption = burstConsumption;
    }



    public int getConsumedThisWeek() {
        return consumedThisWeek;
    }



    private void setUpImmediateConsumption(final Firm firm, final  PurchasesDepartment department){

        //add a "thing" that consume X units of input as soon as they are available each week
        firm.addInventoryListener(new InventoryListener() {
            @Override
            public void inventoryIncreaseEvent(@Nonnull HasInventory source, @Nonnull GoodType type, int quantity) {
                System.out.println("about to consume: " + firm.hasHowMany(GoodType.GENERIC) + ", consumedThisWeek: " + consumedThisWeek);
                if(consumedThisWeek<consumptionRate)
                {
                    //schedule it to be eaten
                    getModel().scheduleASAP(new Steppable() {
                        @Override
                        public void step(SimState state) {
                            assert firm.hasAny(GoodType.GENERIC);
                            firm.consume(GoodType.GENERIC);
                            System.out.println("Consumed!");
                            consumedThisWeek++;
                        }
                    });

                }

            }

            @Override
            public void inventoryDecreaseEvent(@Nonnull HasInventory source, @Nonnull GoodType type, int quantity) {
            }

            //ignored
            @Override
            public void failedToConsumeEvent(@Nonnull HasInventory source, @Nonnull GoodType type, int numberNeeded) {


            }
        });


        //reset the counter
        getModel().scheduleSoon(ActionOrder.PRODUCTION,new Steppable() {
            @Override
            public void step(SimState state) {
                consumedThisWeek=0;
                System.out.println("reset consumed!");
                //eat leftovers, if needed
                do{
                    if(firm.hasAny(GoodType.GENERIC))
                    {
                        firm.consume(GoodType.GENERIC);
                        System.out.println("Consumed!");
                        consumedThisWeek++;
                    }
                    else{
                        firm.fireFailedToConsumeEvent(GoodType.GENERIC,consumptionRate - consumedThisWeek);
                        break;
                    }

                }while (consumedThisWeek<consumptionRate && firm.hasAny(GoodType.GENERIC));

                getModel().scheduleTomorrow(ActionOrder.PRODUCTION,this);

            }


        });

    }

    private void setUpBurstConsumption(final Firm firm, final PurchasesDepartment department){

        assert burstConsumption;
        getModel().scheduleSoon(ActionOrder.PRODUCTION, new Steppable() {
            @Override
            public void step(SimState state) {
                int initialInventory = firm.hasHowMany(GoodType.GENERIC);
                for (int i = 0; i < consumptionRate; i++) {
                    if (firm.hasAny(GoodType.GENERIC))
                        firm.consume(GoodType.GENERIC);
                    else{
                        firm.fireFailedToConsumeEvent(GoodType.GENERIC,consumptionRate - i);
                        break;
                    }
                }
                firm.logEvent(department, MarketEvents.EXOGENOUS, getModel().getCurrentSimulationTimeInMillis(), "initialInventory: " + initialInventory
                        + ",final inventory: " + firm.hasHowMany(GoodType.GENERIC));

                //do it again tomorrow
                getModel().scheduleTomorrow(ActionOrder.PRODUCTION,this);
            }
        });
    }

    public boolean isFiltersOn() {
        return filtersOn;
    }

    public void setFiltersOn(boolean filtersOn) {
        this.filtersOn = filtersOn;
    }

    public float getInputWeight() {
        return inputWeight;
    }

    public void setInputWeight(float inputWeight) {
        this.inputWeight = inputWeight;
    }


    public float getOutputWeight() {
        return outputWeight;
    }

    public void setOutputWeight(float outputWeight) {
        this.outputWeight = outputWeight;
    }

    public int getConsumptionRate() {
        return consumptionRate;
    }

    public void setConsumptionRate(int consumptionRate) {
        this.consumptionRate = consumptionRate;
    }

    public int getTargetInventory() {
        return targetInventory;
    }

    public void setTargetInventory(int targetInventory) {
        this.targetInventory = targetInventory;
    }

    public int getPidPeriod() {
        return pidPeriod;
    }

    public void setPidPeriod(int pidPeriod) {
        this.pidPeriod = pidPeriod;
    }

    public float getProportionalGainAVG() {
        return proportionalGainAVG;
    }

    public void setProportionalGainAVG(float proportionalGainAVG) {
        this.proportionalGainAVG = proportionalGainAVG;
    }

    public float getIntegralGainAVG() {
        return integralGainAVG;
    }

    public void setIntegralGainAVG(float integralGainAVG) {
        this.integralGainAVG = integralGainAVG;
    }

    public float getDerivativeGainAVG() {
        return derivativeGainAVG;
    }

    public void setDerivativeGainAVG(float derivativeGainAVG) {
        this.derivativeGainAVG = derivativeGainAVG;
    }

    public PurchasesDepartment getDepartment() {
        return department;
    }



    public boolean isSupplyShift() {
        return supplyShift;
    }

    public void setSupplyShift(boolean supplyShift) {
        this.supplyShift = supplyShift;
    }

    /**
     * Sets new controllerType.
     *
     * @param controllerType New value of controllerType.
     */
    public void setControllerType(Class<? extends Controller> controllerType) {
        this.controllerType = controllerType;
    }

    /**
     * Gets controllerType.
     *
     * @return Value of controllerType.
     */
    public Class<? extends Controller> getControllerType() {
        return controllerType;
    }

    public int getSupplySlope() {
        return supplySlope;
    }

    public void setSupplySlope(int supplySlope) {
        this.supplySlope = supplySlope;
    }

    public int getSupplyIntercept() {
        return supplyIntercept;
    }

    public void setSupplyIntercept(int supplyIntercept) {
        this.supplyIntercept = supplyIntercept;
    }

    public int getNumberOfSuppliers() {
        return numberOfSuppliers;
    }

    public void setNumberOfSuppliers(int numberOfSuppliers) {
        this.numberOfSuppliers = numberOfSuppliers;
    }

    public int getNumberOfBuyers() {
        return numberOfBuyers;
    }

    public void setNumberOfBuyers(int numberOfBuyers) {
        this.numberOfBuyers = numberOfBuyers;
    }

    public LinkedList<PurchasesDepartment> getDepartments() {
        return departments;
    }
}
