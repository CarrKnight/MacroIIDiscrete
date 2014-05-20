/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.sales;

/**
 * Just an enum describing what was the result of an attempt to sell something
 * User: carrknight
 * Date: 7/18/12
 * Time: 9:09 PM
 */
public class SaleResult {

    public enum Result{

        /**
         * This means that the seller quote the product on the market, but didn't sell immediately
         */
        QUOTED,


        /**
         * This means that the seller managed to sell the good
         */
        SOLD{

        },


        /**
         * The seller failed to sell the good, at least for now!
         */
        UNSOLD,


        /**
         * This is a transitory state where the good was quoted but it's being repriced!
         */
        BEING_UPDATED
    }

    /**
     * The result (sold, unsold, quoted)
     */
    private Result result;


    /**
     * At what price the sale occurred. Only makes sense for the price sold
     */
    private long priceSold = -1;

    /**
     * At what price the sale occurred
     */
    public long getPriceSold() {
        return priceSold;
    }


    /**
     * What was the cost of the good to the seller
     */
    private long previousCost = -1;

    /**
     * What was the cost of the good to the seller
     */
    public long getPreviousCost() {
        return previousCost;
    }


    private SaleResult(Result result, long priceSold, long previousCost) {
        this.result = result;
        this.priceSold = priceSold;
        this.previousCost = previousCost;
    }

    /**
     * This is to force the sold enum to record the sale and cost
     * @return
     */
    public static SaleResult sold(long priceSold, long initialCost){
        return new SaleResult(Result.SOLD,priceSold,initialCost);
    }

    //just have one and return it, don't instantiate millions of these
    private static SaleResult quotedToken = new SaleResult(Result.QUOTED,-1,-1);

    /**
     * Return a singleton with result "quoted"
     */
    public static SaleResult quoted(){
        return quotedToken;
    }

    //just have one and return it, don't instantiate millions of these
    private static SaleResult unsoldToken = new SaleResult(Result.UNSOLD,-1,-1);

    /**
     * Return a singleton with result "quoted"
     */
    public static SaleResult unsold(){
        return unsoldToken;
    }

    public Result getResult() {
        return result;
    }


    //just have one and return it, don't instantiate millions of these
    private static SaleResult updatedToken = new SaleResult(Result.BEING_UPDATED,-1,-1);

    /**
     * Returns a singleton describing the sale result as being in the process of updating itself
     */
    public static SaleResult updating(){

      return updatedToken;
    }


    /**
     * Returns a string representation of the object. In general, the
     * {@code toString} method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p/>
     * The {@code toString} method for class {@code Object}
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `{@code @}', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return result.name();

    }
}

