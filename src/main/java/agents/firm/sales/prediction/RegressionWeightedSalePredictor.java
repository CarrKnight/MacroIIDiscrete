package agents.firm.sales.prediction;

import financial.market.Market;
import model.MacroII;
import model.utilities.stats.collectors.PeriodicMarketObserver;

/**
 * <h4>Description</h4>
 * <p/> This works exactly like the Regression Sale Predictor except that it weights more recent observations higher.
 * <p/> If there are 3 observations, the first has weight 1, the second has weight 2 and the third has weight 3
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-07-13
 * @see
 */
public class RegressionWeightedSalePredictor extends RegressionSalePredictor {
    /**
     * @param market  the market to observe
     * @param macroII a link to the model to schedule yourself
     */
    public RegressionWeightedSalePredictor(Market market, MacroII macroII) {
        super(market, macroII);
    }

    /**
     * Give a premade observer to sale predictor. The observer will be turned Off when the predictor is turned off!
     *
     * @param observer
     */
    public RegressionWeightedSalePredictor(PeriodicMarketObserver observer) {
        super(observer);
    }

    /**
     * Force the predictor to run a regression, if possible
     */
    @Override
    public void updateModel() {
        if(observer.getNumberOfObservations() >1)
        {

            while (observer.getNumberOfObservations() > 1000)
            {
               observer.forgetOldestObservation();
            }


            //create the weights
            double weight[] = new double[observer.getNumberOfObservations()];
            double dayObserved[] = observer.getObservationDaysAsArray();
            for(int i=0; i<weight.length; i++)
            {
                weight[i]=1+i;
            }


            getRegression().estimateModel(observer.getQuantitiesConsumedObservedAsArray(),
                    observer.getPricesObservedAsArray(),weight);


        }

    }
}
