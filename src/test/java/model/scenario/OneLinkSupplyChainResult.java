/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.scenario;

/**
 * <h4>Description</h4>
 * <p/> A simple struct to return when a test is done in multithreading!
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-08-05
 * @see
 */
public class OneLinkSupplyChainResult {

    final private float beefPrice;

    final private float foodPrice;

    final private float quantity;

    public OneLinkSupplyChainResult(float beefPrice, float foodPrice, float quantity) {
        this.beefPrice = beefPrice;
        this.foodPrice = foodPrice;
        this.quantity = quantity;
    }


    public float getBeefPrice() {
        return beefPrice;
    }

    public float getFoodPrice() {
        return foodPrice;
    }

    public float getQuantity() {
        return quantity;
    }
}
