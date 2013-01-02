package agents.firm.sales.pricing;

import agents.firm.sales.SalesDepartment;
import goods.Good;

/**
 * <h4>Description</h4>
 * <p/> This is a sales department that always chooses to charge its prices equal to the cost of the good trying to sell
 * <p/> If we couple this with a costing function that counts only marginal costs then we are going to copy perfectly competitive behavior from econ
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-07-22
 * @see
 */
public class CostAskPricing implements AskPricingStrategy {

    private final SalesDepartment sales;

    public CostAskPricing(SalesDepartment sales) {
        this.sales = sales;
    }

    @Override
    public long price(Good g) {
        return g.getLastValidPrice(); //the good is going to be either the price of production or just the price for which  the good was bought

    }

    /**
     * Turnoff doesn't really affect CostAskPricing
     */
    @Override
    public void turnOff() {
    }

    /**
     * Week end doesn't bother the cost pricing strategy
     */
    @Override
    public void weekEnd() {
    }
}
