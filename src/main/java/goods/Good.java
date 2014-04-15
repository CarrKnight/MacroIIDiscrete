/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package goods;

import agents.EconomicAgent;
import model.MacroII;


/**
 * Created by IntelliJ IDEA.
 * User: Ernesto
 * Date: 3/25/12
 * Time: 8:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class Good implements Comparable<Good>{
    
    private GoodType type;
    
    private EconomicAgent producer;
    
    private long costOfProduction;

    private final long id;


    /**
     * Price this good was last sold for
     */
    private long lastValidPrice;

    /**
     * The price of the good before it was sold last time
     */
    private long secondLastValidPrice;



    public Good( GoodType type, EconomicAgent producer, long costOfProduction) {
        this.type = type;
        this.producer = producer;
        this.costOfProduction = costOfProduction;
        this.id = MacroII.getCounter();
        
        lastValidPrice = costOfProduction;
    }

    public GoodType getType() {
        return type;
    }

    public EconomicAgent getProducer() {
        return producer;
    }

    public long getCostOfProduction() {
        return costOfProduction;
    }

    public long getLastValidPrice() {
        return lastValidPrice;
    }

    public void setLastValidPrice(long lastValidPrice) {
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

        int comparison = Long.compare(lastValidPrice,o.getLastValidPrice());
        if(comparison != 0)
            return  comparison;
        else{
            comparison=  Long.compare(id,o.id);
            assert comparison != 0; //id should all be different
            return comparison;
        }


    }


}
