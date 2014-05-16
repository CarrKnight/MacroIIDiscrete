/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package financial.utilities;

/**
 * <h4>Description</h4>
 * <p/> Just a method that is called by the market to adjudicate the price of two matching offers
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-09-01
 * @see
 */
public interface PricePolicy {

    public int price(int sellerPrice, int buyerPrice);

}

