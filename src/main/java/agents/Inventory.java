/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents;

import goods.Good;
import goods.GoodType;
import model.MacroII;

import java.util.*;

/**
 * This is the inventory class. It is basically an enum map and a few utilities methods to make life easier
 */
public class Inventory {



    /**
     * The inventory is a map, for each good type to a Queue
     */
    final private EnumMap<GoodType,PriorityQueue<Good>> inventory;

    /**
     * link to the simulation proper; useful for graphing and stuff
     */
    final private MacroII model;

    /**
     * The person that owns this inventory
     */
    final private HasInventory owner;

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
     * @param owner the owner of the inventory
     */
    public Inventory(MacroII model, HasInventory owner) {
        this.model = model;
        this.owner = owner;
        inventory = new EnumMap<>(GoodType.class);

        for(GoodType t : GoodType.values())
        {
            inventory.put(t,new PriorityQueue<Good>());
        }

        listeners = new LinkedHashSet<>(); //instantiate the listeners

    }

    /**
     * This method is called when inventory has to increase by 1. The reference to the sender is for accounting purpose only
     * @param g what good is delivered?
     * @param sender who sent it?
     */

    public void receive(Good g, HasInventory sender) {
        PriorityQueue<Good> rightInventory =   inventory.get(g.getType());
        //put the new element in!
        boolean isThisSomethingIDidntOwned = !rightInventory.contains(g);
        rightInventory.add(g);
        if(!isThisSomethingIDidntOwned){
            //MacroII.logger.log(Level.SEVERE,this + " just received " + g + " that he already owned from " + sender);
            throw new RuntimeException("What the hell; Am I receiving something I already have?");
        }
        else
        {
            //MacroII.logger.log(Level.FINER,this + " received a " + g + " from " + sender);
            //tell the gui
            if(sender!= null)
                model.registerInventoryDelivery(sender, owner, g.getType());
            //tell the listeners
            for(InventoryListener l : listeners)
                l.inventoryIncreaseEvent(owner, g.getType(), rightInventory.size(),1);
        }


    }





    /**
     * This method sends the first good of type g available to destination.
     * this method doesn't call "pay" or "earn"; that's the responsibility of whoever calls this method
     * @param g what kind of good is delivered?
     * @param destination who is going to receive it?
     * @param newPrice this is the price for which it was sold, it's not charging destination but it's going to record it in the good
     */
    public void deliver(GoodType g, HasInventory destination,long newPrice ) {

        PriorityQueue<Good> rightHasInventory =   inventory.get(g);
        Good toDeliver = null;
        try{
            toDeliver = rightHasInventory.peek();
        }
        catch (NoSuchElementException e)
        {
            //MacroI.logger.log(Level.SEVERE,this + " couldn't deliver any " + g + "since he doesn't have it");
            e.printStackTrace();
            System.exit(-1);
        }
        assert  toDeliver != null;
        //okay, you have one, now deliver it
        deliver(toDeliver,destination,newPrice);


    }



    /**
     * This method sends a specific good g to destination after recording the price for which it was sold.    <br>
     * this method doesn't call "pay" or "earn"; that's the responsibility of whoever calls this method
     * @param g what kind of good is delivered?
     * @param destination who is going to receive it?
     * @param newPrice this is the price for which it was sold, it's not charging destination but it's going to record it in the good
     */
    public void deliver(Good g, HasInventory destination,long newPrice ) {

        PriorityQueue<Good> rightInventory =   inventory.get(g.getType()); //choose the right inventory
        assert rightInventory.contains(g); //it must be contained

        rightInventory.remove(g); //remove from inventory

        g.setLastValidPrice(newPrice); //record the new price

        //now send
        destination.receive(g, owner);
        //tell the listeners!
        for(InventoryListener l : listeners)
            l.inventoryDecreaseEvent(owner, g.getType(), rightInventory.size(),1);


    }

    /**
     * This method burns inventory by 1
     * @param type what good is consumed?
     * @return the good consumed
     */
    public Good consume(GoodType type) {

        PriorityQueue<Good> rightInventory =   inventory.get(type);
        Good eaten = null;
        try{
            eaten = rightInventory.poll();
        }
        catch (NoSuchElementException e)
        {
            //MacroII.logger.log(Level.SEVERE,this + " couldn't consume any " + type + "since he doesn't have it");
            e.printStackTrace();
            System.exit(-1);
        }

        assert  eaten != null;
        assert !rightInventory.contains(eaten);

        //tell the listeners!
        for(InventoryListener l : listeners)
            l.inventoryDecreaseEvent(owner, type, rightInventory.size(),1);

        //MacroII.logger.log(Level.FINER,this + " consumed " + eaten);
        return eaten;


    }


    /**
     * Eat all
     */
    public void consumeAll()
    {
        //go through each goodType
        for(Map.Entry<GoodType,PriorityQueue<Good>> inventorySection : inventory.entrySet())
        {
            //check the size of the inventory
            int size = inventorySection.getValue().size();
            if(size > 0) //if there is any
            {
                //destroy it
                inventorySection.getValue().clear();
                for(InventoryListener l : listeners) //then tell the listeners about it
                    l.inventoryDecreaseEvent(owner, inventorySection.getKey(), 0,size);
            }
        }

       assert getTotalInventory().size() == 0;


    }

    /**
     * Does this agent have the specified good in his inventory?
     * @param g good to check for
     * @return true if it has in inventory (owned and not consumed)
     */
    public boolean has( Good g){
        assert g != null;
        PriorityQueue<Good> set = inventory.get(g.getType());
        return set.contains(g);

    }

    /**
     * Do you have at least one of this?
     * @param t the type of good you are checking if you have
     * @return true if it has any
     */
    public boolean hasAny(GoodType t){
        PriorityQueue<Good> list = inventory.get(t); //open the right inventory
        return !list.isEmpty(); //is it not empty?


    }

    /**
     * How much of something do you have?
     * @param t the type of good you are checking
     * @return how many do you have.
     */
    public int hasHowMany(GoodType t){
        return inventory.get(t).size(); //open the right inventory


    }




    /**
     * Put all the inventories in a single list and return it
     * @return the total stuff owned
     */
    public List<Good> getTotalInventory() {

        List<Good> totalStuff = new LinkedList<>();
        for(GoodType t : GoodType.values())
        {
            totalStuff.addAll(inventory.get(t));
        }

        //return an unmodifiable list. Don't want foreign objects to call consume or the listeners will not know about it!!!!
        return Collections.unmodifiableList(totalStuff);


    }


    /**
     * Peek at the topmost good of a specific type from the inventory
     * @param type the type of good
     * @return a good or -1 if you have none
     */

    public Good peekGood(GoodType type){
        return inventory.get(type).peek();
    }


    /**
     * notify all listeners that somebody tried to consume a specific good we had none
     * @param type the type of goof needed
     * @param numberNeeded the quantity needed
     */
    public void fireFailedToConsumeEvent(GoodType type, int numberNeeded)
    {
        assert hasHowMany(type) < numberNeeded;

        for(InventoryListener l : listeners)
            l.failedToConsumeEvent(owner,type,numberNeeded);
    }

}
