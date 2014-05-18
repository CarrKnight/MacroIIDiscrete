/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package goods;

/**
 * A very simple pair of goodtype and amount
 * Created by carrknight on 5/18/14.
 */
public class GoodVector {

    private final Integer amount;

    private final GoodType type;

    public GoodVector(Integer amount, GoodType type) {
        this.amount = amount;
        this.type = type;

    }

    public Integer getAmount() {
        return amount;
    }

    public GoodType getType() {
        return type;
    }


}
