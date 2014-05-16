/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package goods;

import com.google.common.base.Preconditions;

/**
 * This is a simple inventory section for undifferentiated goods. Under the hood it is just a integer.
 * It has the additional methods for changing large amounts quickly.
 * Created by carrknight on 5/12/14.
 */
public class InventorySectionCounter implements InventorySection {

    private final GoodType goodType;

    private  int counter;

    public InventorySectionCounter(UndifferentiatedGoodType goodType) {
        this.goodType = goodType;
        counter = 0;
    }

    @Override
    public void store(Good good) {
        assert  counter>=0;
        counter++;
    }

    public void store(int amount) {
        Preconditions.checkArgument(amount >0, "trying to store a negative/zero amount");
        assert  counter>=0;
        counter+= amount;
    }

    @Override
    public void remove(Good good) {
        counter--;
        assert  counter>=0;
    }

    public void remove(int amount) {
        Preconditions.checkArgument(amount >0, "trying to store a negative/zero amount");
        assert  counter>=0;
        counter-= amount;
    }

    @Override
    public int removeAll() {
        int oldSize = counter;
        counter = 0;
        return oldSize;
    }

    @Override
    public int size() {
        return counter;
    }

    @Override
    public GoodType getGoodType() {
        return goodType;
    }

    /**
     * for undifferentiated good, that's just asking "has any?"
     * @return
     */
    @Override
    public boolean containSpecificGood(Good g) {
        return size() >0;

    }

    @Override
    public Good peek() {
        return Good.getInstanceOfUndifferentiatedGood(goodType); //return singleton.
    }
}
