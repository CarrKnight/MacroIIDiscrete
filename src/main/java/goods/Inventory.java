/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package goods;

import agents.HasInventory;
import com.google.common.base.Preconditions;
import model.MacroII;
import model.utilities.Deactivatable;

import java.util.*;

/**
 * This is the inventory class. It is basically an enum map and a few utilities methods to make life easier
 */
public class Inventory implements Deactivatable {



    /**
     * The inventory is a map, for each good type to a Queue
     */
    final private HashMap<GoodType,InventorySection> inventory;

    /**
     * a second map where i store the same link as the previous map, but I make sure it is only for undifferentiated types
     */
    final private HashMap<UndifferentiatedGoodType,InventorySectionCounter> counters;


    /**
     * link to the simulation proper; useful for graphing and stuff
     */
    private MacroII model;

    /**
     * This list contains all the inventory listeners registered
     */
    final private Set<InventoryListener> listeners;

    /**
     * Add a new inventory listener
     */
    public void addListener(InventoryListener listener)
    {
        listeners.add(listener);
    }

    /**
     * Remove specific listener
     * @param listener the listener to remove
     * @return true if it was removed succesfully.
     */
    public boolean removeListener(InventoryListener listener){
        return listeners.remove(listener);
    }


    /**
     * The constructor for the inventory. It is safe to leak the reference to the owner to the constructor since it's not used but only stored. <br>
     *     If it makes you queasy try using a factory method instead or even delayed instantiation
     * @param model the simstate reference (useful for graphing)
     *
     */
    public Inventory(MacroII model) {
        this.model = model;
        inventory = new HashMap<>();
        counters = new HashMap<>();

        listeners = new HashSet<>(); //instantiate the listeners

    }

    /**
     * This method is called when inventory has to increase by 1. The reference to the sender is for accounting purpose only
     * @param g what good is delivered?
     * @param sender who sent it?
     * @param owner
     */

    public void receive(Good g, HasInventory sender, HasInventory owner) {
        final GoodType type = g.getType();
        InventorySection rightInventory =   inventory.get(type);
        //if you there is no priority queue yet, create it!
        if(rightInventory == null)
        {
            if(type.isDifferentiated())
                rightInventory = new InventorySectionStorage(type);
            else
            {
                //ugly casts, but what of it.
                rightInventory = new InventorySectionCounter((UndifferentiatedGoodType)type);
                counters.put((UndifferentiatedGoodType)type,(InventorySectionCounter)rightInventory);
            }
            inventory.put(type, rightInventory);
        }


        //put the new element in!
        boolean isThisSomethingIDidntOwn = !type.isDifferentiated() || !rightInventory.containSpecificGood(g);
        rightInventory.store(g);
        if(!isThisSomethingIDidntOwn){
            //MacroII.logger.log(Level.SEVERE,this + " just received " + g + " that he already owned from " + sender);
            throw new RuntimeException("What the hell; Am I receiving something I already have?");
        }
        else
        {
            //MacroII.logger.log(Level.FINER,this + " received a " + g + " from " + sender);
            //tell the gui
            if(sender!= null)
                model.registerInventoryDelivery(sender, owner, type);
            //tell the listeners
            for(InventoryListener l : listeners)
                l.inventoryIncreaseEvent(owner, type, rightInventory.size(),1);
        }


    }





    /**
     * This method sends the first good of type g available to destination.
     * this method doesn't call "pay" or "earn"; that's the responsibility of whoever calls this method
     * @param g what kind of good is delivered?
     * @param destination who is going to receive it?
     * @param newPrice this is the price for which it was sold, it's not charging destination but it's going to record it in the good
     * @param inventoryOwner
     */
    public void deliver(GoodType g, HasInventory destination, int newPrice, HasInventory inventoryOwner) {

        InventorySection rightInventory =   inventory.get(g);
        Preconditions.checkNotNull(rightInventory, "does not have any item to deliver");
        Good toDeliver = null;
        try{
            toDeliver = rightInventory.peek();
        }
        catch (NoSuchElementException e)
        {
            //MacroI.logger.log(Level.SEVERE,this + " couldn't deliver any " + g + "since he doesn't have it");
            e.printStackTrace();
            System.exit(-1);
        }
        assert  toDeliver != null;
        //okay, you have one, now deliver it
        deliver(toDeliver,destination,newPrice, inventoryOwner);


    }



    /**
     * This method sends a specific good g to destination after recording the price for which it was sold.    <br>
     * this method doesn't call "pay" or "earn"; that's the responsibility of whoever calls this method
     * @param g what kind of good is delivered?
     * @param destination who is going to receive it?
     * @param newPrice this is the price for which it was sold, it's not charging destination but it's going to record it in the good
     * @param inventoryOwner
     */
    public void deliver(Good g, HasInventory destination, int newPrice, HasInventory inventoryOwner) {

        InventorySection rightInventory =   inventory.get(g.getType());
        assert rightInventory.containSpecificGood(g); //it must be contained

        rightInventory.remove(g); //remove from inventory

        g.setLastValidPrice(newPrice); //record the new price

        //now send
        destination.receive(g, inventoryOwner);
        //tell the listeners!
        for(InventoryListener l : listeners)
            l.inventoryDecreaseEvent(inventoryOwner, g.getType(), rightInventory.size(),1);


    }

    /**
     * This method sends multiple quantities of "type" to destination by calling consumeMany and then receiveMany
     * @param destination who is going to receive it?
     * @param owner
     */
    public void deliverMany(UndifferentiatedGoodType type, HasInventory destination, int amount, HasInventory owner) {

        Preconditions.checkArgument(amount >0);
        InventorySectionCounter rightInventory =   counters.get(type);
        Preconditions.checkArgument(rightInventory.size() >= amount);


        rightInventory.remove(amount);


        //now send
        destination.receiveMany(type,amount);
        //tell the listeners!
        for(InventoryListener l : listeners)
            l.inventoryDecreaseEvent(owner, type, rightInventory.size(),amount);


    }

    /**
     * This method burns inventory by 1
     * @param type what good is consumed?
     * @param owner
     * @return the good consumed
     */
    public Good consume(GoodType type, HasInventory owner) {

        InventorySection rightInventory =   inventory.get(type);

        Preconditions.checkArgument(rightInventory.size() >= 1, "cannot consume something I don't have");

        final Good eaten = rightInventory.peek();
        rightInventory.remove(eaten);



        assert !type.isDifferentiated() || !rightInventory.containSpecificGood(eaten);

        //tell the listeners!
        for(InventoryListener l : listeners)
            l.inventoryDecreaseEvent(owner, type, rightInventory.size(),1);


        return eaten;
    }


    /**
     * Consume all and return total consumption
     * @param owner
     */
    public int consumeAll(HasInventory owner)
    {
        int totalConsumption = 0;
        //go through each goodType
        for(Map.Entry<GoodType,InventorySection> inventorySection : inventory.entrySet())
        {
            //check the size of the inventory
            int size = inventorySection.getValue().removeAll();
            if(size > 0) //if there is any
            {
                //destroy it
                for(InventoryListener l : listeners) //then tell the listeners about it
                    l.inventoryDecreaseEvent(owner, inventorySection.getKey(), 0,size);
                totalConsumption+=size;
            }
        }

        return totalConsumption;


    }

    /**
     * Does this agent have the specified good in his inventory? If this good is of undifferentiated type, it is like asking hasAny()
     * @param g good to check for
     * @return true if it has in inventory (owned and not consumed)
     */
    public boolean has( Good g){
        Preconditions.checkNotNull(g);


        final InventorySection inventorySection = inventory.get(g.getType());
        return inventorySection != null && inventorySection.containSpecificGood(g);


    }

    /**
     * Do you have at least one of this?
     * @param t the type of good you are checking if you have
     * @return true if it has any
     */
    public boolean hasAny(GoodType t){
        final InventorySection inventorySection = inventory.get(t);
        return inventorySection != null && inventorySection.size()>0;


    }

    /**
     * How much of something do you have?
     * @param t the type of good you are checking
     * @return how many do you have.
     */
    public int hasHowMany(GoodType t){
        final InventorySection inventorySection = inventory.get(t);

        return inventorySection==null? 0 : inventorySection.size(); //open the right inventory


    }





    /**
     * notify all listeners that somebody tried to consume a specific good we had none
     * @param type the type of goof needed
     * @param numberNeeded the quantity needed
     * @param owner
     */
    public void fireFailedToConsumeEvent(GoodType type, int numberNeeded, HasInventory owner)
    {
        assert hasHowMany(type) < numberNeeded;

        for(InventoryListener l : listeners)
            l.failedToConsumeEvent(owner,type,numberNeeded);
    }

    /**
     * Everytime we encounter a goodtype we record it, here we return them all
     */
    public Set<GoodType> goodTypesEncountered()
    {
        return inventory.keySet();
    }

    /**
     * shortcut to add many undifferentiated good types
     * @param type type of good received
     * @param amount quantity received
     * @param owner
     */
    public void receiveMany(UndifferentiatedGoodType type, int amount, HasInventory owner)
    {
        Preconditions.checkArgument(amount >0);
        InventorySectionCounter counter = counters.get(type);
        if(counter == null){
            counter = new InventorySectionCounter(type);
            counters.put(type,counter);
            assert !inventory.containsKey(type);
            inventory.put(type,counter);
        }
        counter.store(amount);
        assert counter.equals(inventory.get(type)); //make sure they are still linked

        for(InventoryListener l : listeners)
            l.inventoryIncreaseEvent(owner, type, counter.size(), amount);

    }


    /**
     * shortcut to remove many undifferentiated good types
     * @param type type of good removed
     * @param amount quantity removed
     * @param owner
     */
    public void removeMany(UndifferentiatedGoodType type, int amount, HasInventory owner)
    {
        Preconditions.checkArgument(amount >0);
        final InventorySectionCounter counter = counters.get(type);
        counter.remove(amount);
        Preconditions.checkState(counter.size() >=0, "removed more than I had!");
        assert counter.equals(inventory.get(type)); //make sure they are still linked

        for(InventoryListener l : listeners)
            l.inventoryDecreaseEvent(owner, type, counter.size(),amount);
    }

    public Good peekGood(GoodType type)
    {
        return inventory.get(type).peek();
    }


    @Override
    public void turnOff() {
        for(InventorySection section : inventory.values())
            section.removeAll();
        inventory.clear();
        counters.clear();
        listeners.clear();
        model= null;
    }
}
