package agents.firm.sales.prediction;

import com.google.common.primitives.Doubles;
import financial.Market;
import model.MacroII;

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
     * Force the predictor to run a regression, if possible
     */
    @Override
    public void updateModel() {
        if(getQuantitiesObserved().size() >1)
        {
            //create the weights
            double weight[] = new double[getQuantitiesObserved().size()];
            for(int i=0; i<weight.length; i++)
            {
                weight[i]=i+1;
            }

            getRegression().estimateModel(Doubles.toArray(getQuantitiesObserved()),Doubles.toArray(getPricesObserved()),weight);
        }

    }
}
