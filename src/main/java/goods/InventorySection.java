/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package goods;

/**
 * Inventory section is the unit of inventory. That is the storage/counter associated with a particular good type. It comes
 * into two varieties: storage for diffferentiated goods and counter for undifferentiated goods
 * Created by carrknight on 5/12/14.
 */
public interface InventorySection {

    /**
     * store this item
     * @param good
     */
    public void store(Good good);

    /**
     * remove this item
     * @param good
     */
    public void remove(Good good);

    /**
     * remove all and return the number removed
     */
    public int removeAll();

    /**
     * count how many you have
     */
    public int size();

    /**
     * Ask if section contains this very specific item. If the type is undifferentiated that is equal to ask size>0
     * @param g the specific good required
     * @return if differentiated, true if this specific item is stored. If undifferentiated, true if any item is stored.
     */
    public boolean containSpecificGood(Good g);

    /**
     * check the first item available in storage. Do not remove it.
     * @returnthe first item available in storage.
     */
    public Good peek();

    /**
     * what are you storing, exactly.
     */
    public GoodType getGoodType();

}
