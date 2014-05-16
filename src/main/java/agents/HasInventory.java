/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents;

import goods.Good;
import goods.GoodType;
import goods.InventoryListener;
import goods.UndifferentiatedGoodType;

import java.util.Set;

/**
 * This is an interface that I assume everybody who has an inventory will implement. The idea is simply to delegate all these methods to the inventory object you have
 */
public interface HasInventory {


    /**
     * Add a new inventory listener
     */
    public void addInventoryListener(InventoryListener listener);

    /**
     * Remove specific listener
     * @param listener the listener to remove
     * @return true if it was removed succesfully.
     */
    public boolean removeInventoryListener(InventoryListener listener);


    /**
     * This method is called when inventory has to increase by 1. The reference to the sender is for accounting purpose only
     * @param g what good is delivered?
     * @param sender who sent it?
     */

    public void receive(Good g, HasInventory sender) ;


    public void receiveMany(UndifferentiatedGoodType type, int amount);

    /**
     * This method sends the first good of type g available to destination.
     * this method doesn't call "pay" or "earn"; that's the responsibility of whoever calls this method
     * @param g what kind of good is delivered?
     * @param destination who is going to receive it?
     * @param newPrice this is the price for which it was sold, it's not charging destination but it's going to record it in the good
     */
    public void deliver(GoodType g, HasInventory destination,int newPrice );



    /**
     * This method sends a specific good g to destination after recording the price for which it was sold.    <br>
     * this method doesn't call "pay" or "earn"; that's the responsibility of whoever calls this method
     * @param g what kind of good is delivered?
     * @param destination who is going to receive it?
     * @param newPrice this is the price for which it was sold, it's not charging destination but it's going to record it in the good
     */
    public void deliver(Good g, HasInventory destination,int newPrice ) ;

    /**
     * This method burns inventory by 1
     * @param g what good is consumed?
     */
    public Good consume(GoodType g) ;


    /**
     * Eat all
     */
    public abstract void consumeAll();

    /**
     * Does this agent have the specified good in his inventory?
     * @param g good to check for
     * @return true if it has in inventory (owned and not consumed)
     */
    public boolean has(Good g);

    /**
     * Do you have at least one of this?
     * @param t the type of good you are checking if you have
     * @return true if it has any
     */
    public boolean hasAny(GoodType t);

    /**
     * How much of something do you have?
     * @param t the type of good you are checking
     * @return how many do you have.
     */
    public int hasHowMany(GoodType t);


    public void deliverMany(UndifferentiatedGoodType type, HasInventory destination,int amount );

    /**
     * A set of all goods consumed/produced/glimpsed by the agent
     */
    public Set<GoodType> goodTypesEncountered();

    public void consumeMany(UndifferentiatedGoodType type, int amount);


    public Good peekGood(GoodType type);


}
