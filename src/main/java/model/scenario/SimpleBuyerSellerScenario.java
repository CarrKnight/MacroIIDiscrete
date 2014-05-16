/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.scenario;

import agents.firm.Firm;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.pid.FlowAndStockFixedPID;
import agents.firm.purchases.pricing.decorators.MaximumBidPriceDecorator;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentAllAtOnce;
import agents.firm.sales.SalesDepartmentFactory;
import agents.firm.sales.exploration.SimpleBuyerSearch;
import agents.firm.sales.exploration.SimpleSellerSearch;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import financial.market.Market;
import financial.market.OrderBookMarket;
import financial.utilities.ShopSetPricePolicy;
import goods.Good;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * <h4>Description</h4>
 * <p/> Create firms that are only either buyers or sellers, the seller receive a constant stream of goods
 * while the buyers have an inventory target and a constant outflow.
 * <p/> This scenario needs to test 3 cases, the first two of which are very simple:
 * <ul>
 *     <li>
 *         If the stream of production is bigger than the stream of consumption, price will go to minimumPrice
 *     </li>
 *     <li>
 *         If the stream of production is smaller than the stream of consumption, price will go to infinity
 *     </li>
 * </ul>
 * <p/> The third case is to add different minimum prices for sellers to show that only the "cheapest" actually make any trade
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2012-12-15
 * @see
 */
public class SimpleBuyerSellerScenario extends Scenario {

    /**
     * How many goods does the seller have and want to sell?
     */
    private int goodPerSeller = 3;

    /**
     *  The number of sellers in the market
     */
    private int numberOfSellers = 5;

    /**
     *  The number of buyer in the market
     */
    private int numberOfBuyers = 3;

    /**
     * The period in which the seller expects to have all its goods sold AND the time
     * it passes between receiving a bunch of new goods to sell
     */
    private float sellerPeriod = 10;

    /**
     * The "cost of production" of goods being sold
     */
    private long minimumPrice = 2;

    /**
     * How often the purchase department checks its targets
     */
    private int buyerPeriod = 0;

    /**
     * After how much do the buyers "consume" their goods
     */
    private float consumptionPeriod = 10;

    /**
     * How many goods each period does a buyer consume?
     */
    private int consumptionRate = 3;

    /**
     * Does the buyer smooth its pids?
     */
    private boolean filteredBuyer = true;

    /**
     * If this is set to true, each seller gets a minimum price, the first has minimumPrice, the second minimumPrice+10 and so on.
     */
    private boolean minimumPrices = false;

    /**
     * If this is set to true, each buyer has a maximum price above which it never goes. The first is 30, the second one 40 and so on...
     */
    private boolean maximumPrices = false;

    /**
     * Creates the scenario object, so that it links to the model.
     * =
     */
    public SimpleBuyerSellerScenario(MacroII model) {
        super(model);
    }


    /**
     * Called by MacroII, it creates agents and then schedules them.
     */
    @Override
    public void start() {

        //create and record a new market!
        final OrderBookMarket market= new OrderBookMarket(UndifferentiatedGoodType.GENERIC);
        market.setPricePolicy(new ShopSetPricePolicy()); //make the seller price matter

        getMarkets().put(UndifferentiatedGoodType.GENERIC,market);

        //creates sellers
        for(int i=0; i < numberOfSellers; i++)
        {
            Firm seller;
            if(!minimumPrices)
                seller = createSeller(market,minimumPrice);
            else
            {
                seller = createSeller(market,minimumPrice + i*10);
                seller.setName("seller: " + i*10);

            }


            getAgents().add(seller);
        }



        //creates buyers
        for(int i=0; i < numberOfBuyers; i++)
        {
            Firm buyer;
            if(!maximumPrices)
            {
                buyer = createBuyer(market,-1);
                buyer.setName("buyer");

            }
            else
            {
                buyer = createBuyer(market,30+i*10);
                buyer.setName("buyer " + (30+i*10));   //useless comment

            }
            getAgents().add(buyer);
        }

    }

    /**
     * Creates a seller that receives every "period" a fixed number of goods to sell.
     * @param market the market to trade into
     * @param minimumPrice "price of production" (below which, automatically, the price is never set)
     * @return the new seller
     */
    private Firm createSeller(final Market market, final long minimumPrice) {

        //only one seller
        final Firm seller = new Firm(getModel());
        //give it a seller department at time 1
        getModel().schedule.scheduleOnce(new Steppable() {
            @Override
            public void step(SimState simState) {
                SalesDepartment dept = SalesDepartmentFactory.incompleteSalesDepartment(seller, market,
                        new SimpleBuyerSearch(market, seller), new SimpleSellerSearch(market, seller),
                        SalesDepartmentAllAtOnce.class);
                seller.registerSaleDepartment(dept, UndifferentiatedGoodType.GENERIC);
                dept.setAskPricingStrategy(new SimpleFlowSellerPID(dept)); //set strategy to PID
                getAgents().add(seller);
            }
        });




        //arrange for goods to drop periodically in the firm
        getModel().schedule.scheduleRepeating(5f + getModel().random.nextGaussian(),new Steppable() {
            @Override
            public void step(SimState simState) {
                //sell 4 goods!
                for(int i=0; i<goodPerSeller; i++){
                    Good good = Good.getInstanceOfUndifferentiatedGood(UndifferentiatedGoodType.GENERIC);
                    seller.receive(good,null);
                    seller.reactToPlantProduction(good);
                }

            }
        },sellerPeriod );


        return seller;

    }


    /**
     * Utility method to create a FlowAndStock buyer
     * @param market the market trading in
     * @param maximumPrice the price ABOVE which the buyer never bids. It is only activated if the number is >0; the argument is ignored otherwise
     * @return the buyer
     */
    public Firm createBuyer(final Market market, int maximumPrice)
    {
        //create the buyer!
        final Firm buyer = new Firm(model); buyer.receiveMany(UndifferentiatedGoodType.MONEY,100000000);
        final PurchasesDepartment department = PurchasesDepartment.getEmptyPurchasesDepartment(100000000, buyer, market);
//        float proportionalGain = (float) (proportionalGainAVG + model.random.nextGaussian()*.01f); proportionalGain = Math.max(proportionalGain,0); //never below 0
  //      float integralGain = (float) (integralGainAVG + model.random.nextGaussian()*.05f); integralGain = Math.max(integralGain,0);
    //    float derivativeGain =(float) (derivativeGainAVG + model.random.nextGaussian()*.005f); derivativeGain = Math.max(derivativeGain,0);

        department.setOpponentSearch(new SimpleBuyerSearch(market, buyer));
        department.setSupplierSearch(new SimpleSellerSearch(market, buyer));
            //  final PurchasesFixedPID control = new PurchasesFixedPID(department,proportionalGain,integralGain,derivativeGain,targetInventory);
        final FlowAndStockFixedPID control = new FlowAndStockFixedPID(department);
        control.setSpeed(buyerPeriod);
        // control.setInvertInputs(flowTargeting);
        if(filteredBuyer)
            control.smoothFlowPID(.5f);


        buyer.registerPurchasesDepartment(department, UndifferentiatedGoodType.GENERIC);
        //set up as the control!!
        department.setControl(control);
        if(maximumPrice <=0)
            department.setPricingStrategy(control);
        else
            department.setPricingStrategy(new MaximumBidPriceDecorator(control,maximumPrice));

        //set up the department to start buying soon
   /*     getModel().schedule.scheduleOnceIn(Math.max(5.0f + getModel().random.nextGaussian(),1f), new Steppable() {
            @Override
            public void step(SimState state) {
                department.start();
            }
        });
     */


        getModel().schedule.scheduleRepeating(5.0f + getModel().random.nextGaussian(), new Steppable() {
            @Override
            public void step(SimState state) {
                for (int i = 0; i < consumptionRate; i++) {
                    if (buyer.hasAny(UndifferentiatedGoodType.GENERIC))
                        buyer.consume(UndifferentiatedGoodType.GENERIC);
                    else{
                        buyer.fireFailedToConsumeEvent(UndifferentiatedGoodType.GENERIC,consumptionRate - i);
                        break;
                    }
                }



            }
        }, consumptionPeriod);


        return buyer;


    }


    public int getGoodPerSeller() {
        return goodPerSeller;
    }

    public void setGoodPerSeller(int goodPerSeller) {
        this.goodPerSeller = goodPerSeller;
    }

    public int getNumberOfSellers() {
        return numberOfSellers;
    }

    public void setNumberOfSellers(int numberOfSellers) {
        this.numberOfSellers = numberOfSellers;
    }

    public int getNumberOfBuyers() {
        return numberOfBuyers;
    }

    public void setNumberOfBuyers(int numberOfBuyers) {
        this.numberOfBuyers = numberOfBuyers;
    }

    public float getSellerPeriod() {
        return sellerPeriod;
    }

    public void setSellerPeriod(float sellerPeriod) {
        this.sellerPeriod = sellerPeriod;
    }

    public long getMinimumPrice() {
        return minimumPrice;
    }

    public void setMinimumPrice(long minimumPrice) {
        this.minimumPrice = minimumPrice;
    }

    public int getBuyerPeriod() {
        return buyerPeriod;
    }

    public void setBuyerPeriod(int buyerPeriod) {
        this.buyerPeriod = buyerPeriod;
    }

    public float getConsumptionPeriod() {
        return consumptionPeriod;
    }

    public void setConsumptionPeriod(float consumptionPeriod) {
        this.consumptionPeriod = consumptionPeriod;
    }

    public int getConsumptionRate() {
        return consumptionRate;
    }

    public void setConsumptionRate(int consumptionRate) {
        this.consumptionRate = consumptionRate;
    }

    public boolean isFilteredBuyer() {
        return filteredBuyer;
    }

    public void setFilteredBuyer(boolean filteredBuyer) {
        this.filteredBuyer = filteredBuyer;
    }


    public boolean isMinimumPrices() {
        return minimumPrices;
    }

    public void setMinimumPrices(boolean minimumPrices) {
        this.minimumPrices = minimumPrices;
    }


    public boolean isMaximumPrices() {
        return maximumPrices;
    }

    public void setMaximumPrices(boolean maximumPrices) {
        this.maximumPrices = maximumPrices;
    }
}
