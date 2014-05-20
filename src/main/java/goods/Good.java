/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package goods;

import agents.HasInventory;
import com.google.common.base.Preconditions;
import model.MacroII;

import java.util.HashMap;


/**
 * Created by IntelliJ IDEA.
 * User: Ernesto
 * Date: 3/25/12
 * Time: 8:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class Good implements Comparable<Good>{

    private GoodType type;


    private int costOfProduction;

    private final long id;


    /**
     * Price this good was last sold for
     */
    private int lastValidPrice;

    /**
     * The price of the good before it was sold last time
     */
    private int secondLastValidPrice;



    protected Good(GoodType type, int costOfProduction) {
        this.type = type;
        this.costOfProduction = costOfProduction;
        this.id = MacroII.getCounter();

        lastValidPrice = costOfProduction;
    }

    public GoodType getType() {
        return type;
    }


    public int getCostOfProduction() {
        return costOfProduction;
    }

    public int getLastValidPrice() {
        return lastValidPrice;
    }

    public void setLastValidPrice(int lastValidPrice) {
        secondLastValidPrice = this.lastValidPrice;
        this.lastValidPrice =lastValidPrice;
    }

    /**
     * This compares goods by their last valid price, if the price is the same, they are compared by ID. This is so we can use nifty sorted sets/
     * @param o the good to compare this one to
     * @return 1 if this is bigger than o
     */
    @Override
    public int compareTo(Good o) {

        int comparison = Long.compare(lastValidPrice, o.getLastValidPrice());
        if(comparison != 0)
            return  comparison;
        else{
            comparison=  Long.compare(id,o.id);
            assert comparison != 0; //id should all be different
            return comparison;
        }


    }


    private static final HashMap<GoodType,Good> undifferentiatedGoods = new HashMap<>();

    public static Good getInstanceOfUndifferentiatedGood(GoodType type)
    {
        Preconditions.checkArgument(!type.isDifferentiated(), "this method only works for undifferentiated goods!");
        Good instance = undifferentiatedGoods.get(type);
        if(instance == null)
        {
            instance = new Good(type, -1);
            undifferentiatedGoods.put(type,instance);
        }

        return instance;
    }

    public static Good getInstanceOfDifferentiatedGood(GoodType type, HasInventory producer, int costOfProduction)
    {
        Preconditions.checkArgument(type.isDifferentiated(), "this method only works for differentiated goods!");

        return new Good(type, costOfProduction);

    }


}
