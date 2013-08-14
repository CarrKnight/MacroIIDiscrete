/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents;


import agents.firm.DailyProductionAndConsumptionCounter;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import financial.Bankruptcy;
import financial.Market;
import financial.utilities.PurchaseResult;
import financial.utilities.Quote;
import goods.Good;
import goods.GoodType;
import model.MacroII;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.Inspector;
import sim.portrayal.SimpleInspector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * This class is abstract and basically takes care of having cash,
 * giving it away or gaining some. <br>
 *
 *
 *
 * Created by IntelliJ IDEA.
 * User: Ernesto
 * Date: 3/17/12
 * Time: 12:47 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class EconomicAgent implements Agent, HasInventory{


    private long cash = 0;

    /**
     * This boolean represents whether or not the agent is still scheduled. Might change to stoppable later.
     */
    private boolean isActive = true;

    /**
     * The inventory to use. Its instantiation is delayed within the getter
     */
    private Inventory inventory;

    /**
     * link to the old stuff
     */
    final protected MacroII model;

    /**
     * the object that counts all our productions
     */
    final protected DailyProductionAndConsumptionCounter counter;

    /**
     * True if the start() method has already been called
     */
    protected boolean startWasCalled = false;




    protected EconomicAgent(@Nonnull final MacroII model) {

        this(model, 0l);


    }





    protected EconomicAgent(@Nonnull final MacroII model,final long cash) {
        this.model = model;
        this.cash = cash;
        this.counter = new DailyProductionAndConsumptionCounter();

    }

    /**
     * called when the agent has to start acting!
     */
    @Override
    public void start(MacroII state) {
        Preconditions.checkArgument(!startWasCalled);
        startWasCalled = true;

        counter.start(state);

    }

    public void earn(long money)
    {
        Preconditions.checkArgument(money >=0);

        long oldcash = cash;
        cash += money;
        //   MacroII.logger.log(Level.FINEST, this + " just earned: " + money +", now it has " + getCash() );

        assert oldcash <= cash;


    }


    /**
     * Removes cash from this agent and transfers it to the receiver
     * @param money how much money paid
     * @param receiver who should receive it? (if null just call burn money instead)
     * @param reason the market in which the transaction occurs, can be null
     * @throws financial.Bankruptcy if you end up with negative money. If thrown the receiver received nothing!
     */
    public void pay(long money,@Nonnull EconomicAgent receiver,@Nullable Market reason) throws Bankruptcy {
        cash -= money;  //money is gone!
        if(cash < 0 )
            throw new Bankruptcy(this);

        receiver.earn(money); //give the money to the receiver



        //MacroII.logger.log(Level.FINEST, this + " just paid: " + money + ", now it has " + getCash());
        model.registerCashDelivery(this,receiver,money);


    }







    /**
     * This is probably used only in testing.
     * @param money
     * @throws Bankruptcy
     */
    public void burnMoney(long money) throws Bankruptcy {
        cash -= money;
        if(cash < 0)
            throw new Bankruptcy(this);


        // MacroII.logger.log(Level.FINEST, this + " just paid: " + money + ", now it has " + getCash());


    }





    public long getCash(){
        return cash;
    }

    /**
     * Checks if the agent is bankrupt
     * @return true if cash>0
     */
    public boolean isLiquid(){
        return cash >=0;
    }

    public boolean hasEnoughCash(long money){
        return cash >= money;
    }


    /**
     * reset cash holdings to 0
     */
    public void zeroCash(){
        cash = 0;
    }


    @Override
    public void weekEnd(double time) {

    }

    @Override
    public void step(SimState simState) {

    }




    /**
     * link back to the model
     * @return
     */
    public MacroII getModel() {
        return model;
    }


    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * The standard inspector is just a simpleInspector, but it can be overriden.
     * @return
     */
    public Inspector getInspector(GUIState gui){
        return new SimpleInspector(this,gui);

    }


    abstract public MersenneTwisterFast getRandom();


    abstract  public void reactToFilledAskedQuote(Good g, long price, EconomicAgent buyer);

    abstract public void reactToFilledBidQuote(Good g, long price, EconomicAgent seller);


    /**
     * This is called by a peddler/survey to the agent and asks what would be the maximum he's willing to pay to buy a new good.
     * @param g the good the peddler/survey is pushing
     * @return the maximum price willing to pay or -1 if no price is good.
     */
    abstract public long maximumOffer(Good g);

    /**
     * This is called by a peddler/survey to the agent and asks what would be the maximum he's willing to pay to buy a new good.
     * @param t the type good the peddler/survey is pushing
     * @return the maximum price willing to pay or -1 if no price is good.
     */
    abstract public long askedForABuyOffer(GoodType t);

    /**
     * A buyer asks the sales department for the price they are willing to sell one of their good.
     * @param buyer the economic agent that asks you that
     * @return a price quoted or -1 if there are no quotes
     */
    public abstract Quote askedForASaleQuote(EconomicAgent buyer, GoodType type);

    /**
     * This getter is also used to lazily instantiate the inventory
     * @return inventory
     */
    private Inventory getInventory() {
        if(inventory == null)
            inventory = new Inventory(getModel(),this);
        return inventory;
    }

    /**
     * This method is called when inventory has to increase by 1. The reference to the sender is for accounting purpose only
     * @param g what good is delivered?
     * @param sender who sent it?
     */
    @Override
    public void receive(Good g, @Nullable HasInventory sender) {
        counter.countNewReceive(g.getType());
        getInventory().receive(g, sender);
    }

    /**
     * This method sends the first good of type g available to destination.
     * this method doesn't call "pay" or "earn"; that's the responsibility of whoever calls this method
     * @param g what kind of good is delivered?
     * @param destination who is going to receive it?
     * @param newPrice this is the price for which it was sold, it's not charging destination but it's going to record it in the good
     */
    @Override
    public void deliver(GoodType g, HasInventory destination, long newPrice) {
        getInventory().deliver(g, destination, newPrice);
    }

    /**
     * This method sends a specific good g to destination after recording the price for which it was sold.    <br>
     * this method doesn't call "pay" or "earn"; that's the responsibility of whoever calls this method
     * @param g what kind of good is delivered?
     * @param destination who is going to receive it?
     * @param newPrice this is the price for which it was sold, it's not charging destination but it's going to record it in the good
     */
    @Override
    public void deliver(Good g, HasInventory destination, long newPrice) {
        getInventory().deliver(g, destination, newPrice);
    }

    /**
     * This method burns inventory by 1
     * @param g what good is consumed?
     * @return the good consumed
     */
    @Override
    public Good consume(GoodType g) {
        countNewConsumption(g);
        return getInventory().consume(g);
    }

    /**
     * Eat all
     */
    @Override
    public void consumeAll() {
        for(GoodType g : GoodType.values())
            countNewConsumption(g,getInventory().hasHowMany(g));
        getInventory().consumeAll();
    }

    /**
     * Does this agent have the specified good in his inventory?
     * @param g good to check for
     * @return true if it has in inventory (owned and not consumed)
     */
    @Override
    public boolean has(@Nonnull Good g) {
        return getInventory().has(g);
    }

    /**
     * Do you have at least one of this?
     * @param t the type of good you are checking if you have
     * @return true if it has any
     */
    @Override
    public boolean hasAny(GoodType t) {
        return getInventory().hasAny(t);
    }

    /**
     * How much of something do you have?
     * @param t the type of good you are checking
     * @return how many do you have.
     */
    @Override
    public int hasHowMany(GoodType t) {
        return getInventory().hasHowMany(t);
    }

    /**
     * Put all the inventories in a single list and return it
     * @return the total stuff owned
     */
    @Override
    public List<Good> getTotalInventory() {
        return getInventory().getTotalInventory();
    }

    /**
     * Add a new inventory listener
     */
    @Override
    public void addInventoryListener(InventoryListener listener) {
        getInventory().addListener(listener);
    }

    /**
     * Remove specific listener
     * @param listener the listener to remove
     * @return true if it was removed succesfully.
     */
    @Override
    public boolean removeInventoryListener(InventoryListener listener) {
        return getInventory().removeListener(listener);
    }

    /**
     * peek at the topmost good of a specific type in your inventory.
     * @return the first good found or null if there are none
     */
    @Override @Nullable
    public Good peekGood(@Nonnull GoodType type) {
        return getInventory().peekGood(type);

    }

    /**
     * Called by a buyer that wants to buy directly from this agent, not going through the market.
     * @param buyerQuote  the quote (including the offer price) of the buyer
     * @param sellerQuote the quote (including the offer price) of the seller; I expect the buyer to have achieved this through asked for an offer function
     * @return a purchaseResult including whether the trade was succesful and if so the final price
     */
    @Nonnull
    public abstract PurchaseResult shopHere(@Nonnull Quote buyerQuote,@Nonnull Quote sellerQuote);


    /**
     * notify all listeners that somebody tried to consume a specific good we had none
     * @param type the type of goof needed
     * @param numberNeeded the quantity needed
     */
    public void fireFailedToConsumeEvent(GoodType type, int numberNeeded) {
        Preconditions.checkArgument(numberNeeded > 0);
        Preconditions.checkState(hasHowMany(type) < numberNeeded);
        inventory.fireFailedToConsumeEvent(type, numberNeeded);
    }


    @Override
    public void turnOff() {
        counter.turnOff();
    }


    /**
     * how "far" purchases inventory are from target.
     */
    public abstract int estimateDemandGap(GoodType type);

    /**
     * how "far" sales inventory are from target.
     */
    public abstract int estimateSupplyGap(GoodType type);

    /**
     * get today Production
     */
    public int getTodayAcquisitions(GoodType type) {
        return counter.getTodayAcquisitions(type);
    }

    /**
     * get yesterday Production
     */
    public int getYesterdayAcquisitions(GoodType type) {
        return counter.getYesterdayAcquisitions(type);
    }

    /**
     * get yesterday consumption
     */
    public int getYesterdayConsumption(GoodType type) {
        return counter.getYesterdayConsumption(type);
    }

    /**
     * Tell the counter something has been consumed multiple times
     */
    public void countNewReceive(GoodType type, Integer n) {
        counter.countNewReceive(type, n);
    }

    /**
     * Tell the counter something has been consumed multiple times
     */
    public void countNewConsumption(GoodType type, Integer n) {
        counter.countNewConsumption(type, n);
    }

    /**
     * Tell the counter something has been consumed
     */
    public void countNewConsumption(GoodType type) {
        counter.countNewConsumption(type);
    }

    /**
     * get today Production
     */
    public int getTodayProduction(GoodType type) {
        return counter.getTodayProduction(type);
    }

    /**
     * get yesterday Production
     */
    public int getYesterdayProduction(GoodType type) {
        return counter.getYesterdayProduction(type);
    }

    /**
     * Tell the counter something has been consumed
     */
    public void countNewReceive(GoodType type) {
        counter.countNewReceive(type);
    }

    /**
     * Tell the counter something has been consumed multiple times
     */
    public void countNewProduction(GoodType type, Integer n) {
        counter.countNewProduction(type, n);
    }

    /**
     * get today consumption
     */
    public int getTodayConsumption(GoodType type) {
        return counter.getTodayConsumption(type);
    }

    /**
     * Tell the counter something has been consumed
     */
    public void countNewProduction(GoodType type) {
        counter.countNewProduction(type);
    }
}
