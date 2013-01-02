package agents.firm.purchases.prediction;

import agents.firm.purchases.PurchasesDepartment;

/**
 * <h4>Description</h4>
 * <p/> This is the strategy owned by the purchases department (but more likely used by the production side) to predict what the price of future
 * purchases will be like.
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-10-04
 * @see
 */
public interface PurchasesPredictor
{

    /**
     * Predicts the future price of the next good to buy
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    public long predictPurchasePrice(PurchasesDepartment dept);


    /**
     * Call this to kill the predictor
     */
    public void turnOff();
}
