/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.sales.SalesDepartment;
import com.google.common.base.Preconditions;
import financial.market.Market;
import model.MacroII;

/**
 * <h4>Description</h4>
 * <p/> This is a very slightly modified RegressionSalesPredictor. Much like it, this predictor regresses p over q.
 * But rather than then trying to use today's quantity + 1 to estimate prices, it only looks at the estimated slope and predicts price will change by that slope.
 * This should make it indepedent of whether or not the firm is selling all its products and so on.
 * <p/> Code-wise this class just HAS a RegressionSalePredictor and a FixedDecreaseSalesPredictor to use.
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-07-12
 * @see
 */
public class LearningDecreaseSalesPredictor implements SalesPredictor {

    /**
     * The regression predictor. It does all the stepping for itself, so just use it rather than duplicate code
     */
    protected final RegressionSalePredictor regressor;

    /**
     * The object that actually makes predictions. We just feed it the slope every time
     */
    protected final FixedDecreaseSalesPredictor predictor;

    private final MacroII model;


    /**
     * Create a learning decrease sales predictor that uses weighted OLS
     * @param market
     * @param model
     */
    public LearningDecreaseSalesPredictor(Market market, MacroII model)
    {

        this(market, model,true);

    }

    public LearningDecreaseSalesPredictor(Market market, MacroII model,boolean weighted)
    {
        if(weighted)
            regressor = new RegressionWeightedSalePredictor(market,model);
        else
            regressor = new RegressionSalePredictor(market,model);
        predictor = new FixedDecreaseSalesPredictor(0);
        this.model = model;

    }



    /**
     * This is called by the firm when it wants to predict the price they can sell to
     * (usually in order to guide production). <br>
     *
     *
     * @param dept                   the sales department that has to answer this question
     * @param expectedProductionCost the HQ estimate of costs in producing whatever it wants to sell. It isn't necesarilly used.
     * @param increaseStep
     * @return the best offer available/predicted or -1 if there are no quotes/good predictions
     */
    @Override
    public long predictSalePriceAfterIncreasingProduction(SalesDepartment dept, long expectedProductionCost, int increaseStep)
    {
        Preconditions.checkArgument(increaseStep >= 0);
        updateRegressorAndUseItToUpdatePredictor();




        return predictor.predictSalePriceAfterIncreasingProduction(dept, expectedProductionCost,increaseStep );

    }


    /**
     * This is called by the firm when it wants to predict the price they can sell to if they increase production
     *
     *
     * @param dept                   the sales department that has to answer this question
     * @param expectedProductionCost the HQ estimate of costs in producing whatever it wants to sell. It isn't necesarilly used.
     * @param decreaseStep  by how much the production will decrease, has to be positive
     * @return the best offer available/predicted or -1 if there are no quotes/good predictions
     */
    @Override
    public long predictSalePriceAfterDecreasingProduction(SalesDepartment dept, long expectedProductionCost, int decreaseStep) {
        Preconditions.checkArgument(decreaseStep >= 0);
        updateRegressorAndUseItToUpdatePredictor();




        return predictor.predictSalePriceAfterDecreasingProduction(dept, expectedProductionCost, decreaseStep);
    }

    protected void updateRegressorAndUseItToUpdatePredictor() {
        //force a regression
        regressor.updateModel();
        //update slope (we need to put the inverse as a sign because the number is subtracted from old price)
        if(!Double.isNaN(regressor.getSlope()))
            predictor.setDecrementDelta((int) Math.round(-regressor.getSlope()));
        else
            predictor.setDecrementDelta(0);
    }

    /**
     * Call this to kill the predictor
     */
    @Override
    public void turnOff() {
        regressor.turnOff();
        predictor.turnOff();
    }

    /**
     * This is a little bit weird to predict, but basically you want to know what will be "tomorrow" price if you don't change production.
     * Most predictors simply return today closing price, because maybe this will be useful in some cases. It's used by Marginal Maximizer Statics
     *
     * @param dept the sales department
     * @return predicted price
     */
    @Override
    public long predictSalePriceWhenNotChangingPoduction(SalesDepartment dept) {
        return predictor.predictSalePriceWhenNotChangingPoduction(dept);
    }
}
