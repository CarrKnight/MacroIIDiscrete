package model.scenario;

import agents.EconomicAgent;
import agents.HasInventory;
import agents.InventoryListener;
import agents.firm.Firm;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.pid.PurchasesFixedPID;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import financial.Market;
import financial.MarketEvents;
import financial.OrderBookMarket;
import financial.utilities.BuyerSetPricePolicy;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.pid.CascadePIDController;
import model.utilities.pid.FlowAndStockController;
import sim.engine.SimState;
import sim.engine.Steppable;
import tests.DummySeller;

import javax.annotation.Nonnull;

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
 *
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
     * Are there additional people coming to supply after 20 weeks?
     */
    boolean supplyShift = false;


    /**
     * Does the burning of the input happens immediately or in one big burst?
     */
    boolean burstConsumption = true;

    boolean filtersOn = false;

    float inputWeight = 0.5f;

    int targetInventory = 6;

    int consumptionRate = 2;


    float outputWeight = .35f;

    int pidPeriod = period;

    public int consumedThisWeek = 0;

    private boolean cascadeControl = false;


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

        //create the buyer!
        final Firm firm = new Firm(model); firm.earn(Long.MAX_VALUE);
        department = PurchasesDepartment.getEmptyPurchasesDepartment(Long.MAX_VALUE, firm, market);
        float proportionalGain = (float) (proportionalGainAVG + model.random.nextGaussian()*.01f); proportionalGain = Math.max(proportionalGain,0); //never below 0
        float integralGain = (float) (integralGainAVG + model.random.nextGaussian()*.05f); integralGain = Math.max(integralGain,0);
        float derivativeGain =(float) (derivativeGainAVG + model.random.nextGaussian()*.005f); derivativeGain = Math.max(derivativeGain,0);

        department.setOpponentSearch(new SimpleBuyerSearch(market, firm));
        department.setSupplierSearch(new SimpleSellerSearch(market, firm));
        //  final PurchasesFixedPID control = new PurchasesFixedPID(department,proportionalGain,integralGain,derivativeGain,targetInventory);
        if(cascadeControl){
            final PurchasesFixedPID control = new PurchasesFixedPID(department,targetInventory, CascadePIDController.class,model);
            control.setSpeed(pidPeriod);
            if(filtersOn){
                //filtering flows
                assert !control.isInvertInputs();
                control.filterInputExponentially(inputWeight,1);
                control.filterTargetExponentially(inputWeight,1);

            }
            department.setControl(control);
            department.setPricingStrategy(control);
        }
        else {
       /*     final FlowAndStockFixedPID control = new FlowAndStockFixedPID(department,proportionalGain,integralGain,derivativeGain,targetInventory);
            control.setSpeed(pidPeriod);

            // control.setInvertInputs(flowTargeting);

            if(filtersOn){

                control.smoothFlowPID(inputWeight);
                //       control.filterInputExponentially(inputWeight);

            } */
            final PurchasesFixedPID control = new PurchasesFixedPID(department,targetInventory, FlowAndStockController.class,model);
            control.setSpeed(pidPeriod);
          /*  if(filtersOn){
                //filtering flows
                assert !control.isInvertInputs();
                control.filterInputExponentially(inputWeight,1);
                control.filterTargetExponentially(inputWeight,1);

            } */


            //set up as the control!!
            department.setControl(control);
            department.setPricingStrategy(control);
        }
        firm.registerPurchasesDepartment(department,GoodType.GENERIC);

        getAgents().add(firm); //add the buyer


        //set up the department to start buying soon
        getModel().scheduleSoon(ActionOrder.PREPARE_TO_TRADE, new Steppable() {
            @Override
            public void step(SimState state) {
                department.start();
            }
        });


        if(burstConsumption)
            setUpBurstConsumption(firm, department);
        else
            setUpImmediateConsumption(firm, department);


        for(int i=0; i <10; i++)
            createSeller(i*10+100,market,0f);


        if(supplyShift)
            for(int i=0; i <10; i++)
                createSeller(i*10,market,5000f);

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
        final DummySeller seller = new DummySeller(getModel(),reservationPrice,market)

        {
            @Override
            public void reactToFilledAskedQuote(Good g, long price, EconomicAgent buyer) {
                super.reactToFilledAskedQuote(g, price, buyer);

                final DummySeller reference = this;

                //in "period time"
                getModel().scheduleTomorrow(ActionOrder.TRADE,new Steppable() {
                    @Override
                    public void step(SimState state) {
                        //receive a new good
                        Good newGood = new Good(market.getGoodType(),reference,reference.saleQuote);
                        reference.receive(newGood, null);
                        //place it for sale!!!
                        market.submitSellQuote(reference,reference.saleQuote,newGood);

                    }
                });




            }
        };

        market.registerSeller(seller);
        //at a random point in the near future we need to have the dummy seller receive its first good and quote it!
        getModel().scheduleSoon(ActionOrder.TRADE, new Steppable() {
            @Override
            public void step(SimState state) {
                //receive a new good
                Good newGood = new Good(market.getGoodType(), seller, seller.saleQuote);
                seller.receive(newGood, null);
                //place it for sale!!!
                market.submitSellQuote(seller, seller.saleQuote, newGood);

            }
        });


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
                            //System.out.println("Consumed!");
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
                getModel().scheduleSoon(ActionOrder.CLEANUP,new Steppable() {
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

                getModel().scheduleTomorrow(ActionOrder.CLEANUP,this);

            }


        });

    }

    private void setUpBurstConsumption(final Firm firm, final PurchasesDepartment department){

        assert burstConsumption;
        getModel().scheduleSoon(ActionOrder.CLEANUP, new Steppable() {
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
                System.out.println("finished consuming:  " + firm.hasHowMany(GoodType.GENERIC));
                firm.logEvent(department, MarketEvents.EXOGENOUS, getModel().getCurrentSimulationTimeInMillis(), "initialInventory: " + initialInventory
                        + ",final inventory: " + firm.hasHowMany(GoodType.GENERIC));

                //do it again tomorrow
                getModel().scheduleTomorrow(ActionOrder.CLEANUP,this);
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

    public boolean isCascadeControl() {
        return cascadeControl;
    }

    public void setCascadeControl(boolean cascadeControl) {
        this.cascadeControl = cascadeControl;
    }

    public boolean isSupplyShift() {
        return supplyShift;
    }

    public void setSupplyShift(boolean supplyShift) {
        this.supplyShift = supplyShift;
    }
}
