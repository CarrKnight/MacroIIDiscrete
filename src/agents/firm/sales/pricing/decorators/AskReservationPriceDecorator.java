package agents.firm.sales.pricing.decorators;

import agents.firm.sales.pricing.AskPricingStrategy;
import goods.Good;

/**
 * <h4>Description</h4>
 * <p/> This reservation decorator intercepts all strategy calls of price and ceils all the prices below the reservation price given at constructor time
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2012-11-12
 * @see
 */
public class AskReservationPriceDecorator extends AskPricingDecorator {

    /**
     * The reservation price below which the sales department will never sell
     */
    final private long reservationPrice;

    /**
     * This reservation decorator intercepts all strategy calls of price() and ceils all the prices below the reservation price given at constructor time
     * @param toDecorate the ask strategy to decorate
     * @param reservationPrice the reservation price below which the sales department will never sell
     */
    public AskReservationPriceDecorator(AskPricingStrategy toDecorate, long reservationPrice) {
        super(toDecorate);
        this.reservationPrice = reservationPrice;
    }

    /**
     * The sales department is asked at what should be the sale price for a specific good; this, I guess, is the fundamental
     * part of the sales department
     *
     * @param g the good to price
     * @return the price given to that good
     */
    @Override
    public long price(Good g) {
        return Math.max(toDecorate.price(g), reservationPrice);

    }
}
