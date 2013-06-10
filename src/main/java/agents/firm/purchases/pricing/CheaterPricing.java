package agents.firm.purchases.pricing;

import agents.firm.purchases.PurchasesDepartment;
import goods.Good;
import goods.GoodType;

/**
 * <h4>Description</h4>
 * <p/> Cheater Pricing "cheats" by simply always offering the best ask price if there is one and it's visible and defaulting to
 * defaultOffer otherwise!
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-06-07
 * @see
 */
public class CheaterPricing implements BidPricingStrategy {

    /**
     * how much to offer for a good when the price is not visible.
     *
     */
    private long defaultOffer = 1000;

    /**
     * the purchase department we work for
     */
    private final PurchasesDepartment department;


    /**
     * creates the pricing strategy that looks at the market
     * @param department
     */
    public CheaterPricing(PurchasesDepartment department) {
        this.department = department;
    }

    /**
     * Returns the best sale price if there is one and is visible or default offer
     *
     * @param type the type of good you want to buy
     * @return the maximum price I am willing to pay for this good
     */
    @Override
    public long maxPrice(GoodType type) {
        //if the best sale is visible and exists, return it otherwise default
        try {
            if(department.getMarket().isBestSalePriceVisible() && department.getMarket().getBestSellPrice() > -1)
                return department.getMarket().getBestSellPrice();
            else
                return defaultOffer;
        } catch (IllegalAccessException e) {
            //damn exception, making everything ugly
            throw new IllegalStateException("I must have screwed up with short-circuit, because I thought the best sale price was visible but it wasn't!");

        }
    }

    /**
     * Answer the purchase strategy question: how much am I willing to pay for this specific good?
     *
     * @param good the specific good being offered to you
     * @return the maximum price I am willing to pay for this good
     */
    @Override
    public long maxPrice(Good good) {
        return maxPrice(good.getType()); //delegate
    }

    /**
     * Useful to stop the bid pricing strategy from giving orders
     */
    @Override
    public void turnOff() {
        //nothing really
    }


    /**
     * Sets new how much to offer for a good when the price is not visible..
     *
     * @param defaultOffer New value of how much to offer for a good when the price is not visible..
     */
    public void setDefaultOffer(long defaultOffer) {
        this.defaultOffer = defaultOffer;
    }

    /**
     * Gets how much to offer for a good when the price is not visible..
     *
     * @return Value of how much to offer for a good when the price is not visible..
     */
    public long getDefaultOffer() {
        return defaultOffer;
    }
}
