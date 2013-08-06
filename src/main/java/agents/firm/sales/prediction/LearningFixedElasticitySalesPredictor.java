/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import financial.Market;
import model.MacroII;
import org.apache.commons.collections15.Transformer;

/**
 * <h4>Description</h4>
 * <p/> Similar to LearningDecreaseSalesPredictor but rather than fitting y=a+bx it fits
 * log(y)=a+b*log(x)
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-07-17
 * @see
 */
public class LearningFixedElasticitySalesPredictor extends LearningDecreaseSalesPredictor {
    /**
     * Create a learning decrease sales predictor that uses weighted OLS
     *
     * @param market
     * @param model
     */

    public final static Transformer<Double, Double> logTransformer = new Transformer<Double, Double>() {
        @Override
        public Double transform(Double aDouble) {
            return Math.log(aDouble);

        }
    };

    public final static Transformer<Double, Double> expTransformer = new Transformer<Double, Double>() {
        @Override
        public Double transform(Double aDouble) {
            return Math.exp(aDouble);

        }
    };


    public LearningFixedElasticitySalesPredictor(Market market, MacroII model) {
        super(market, model);
        regressor.setPriceTransformer(logTransformer,expTransformer);
        regressor.setQuantityTransformer(logTransformer);

    }

    public LearningFixedElasticitySalesPredictor(Market market, MacroII model, boolean weighted) {
        super(market, model, weighted);
        regressor.setPriceTransformer(logTransformer,expTransformer);
        regressor.setQuantityTransformer(logTransformer);
    }

    @Override
    protected void updateRegressorAndUseItToUpdatePredictor() {
        //force a regression
        regressor.updateModel();
        //update slope (we need to put the inverse as a sign because the number is subtracted from old price)
        if(!Double.isNaN(regressor.getSlope()))
            predictor.setDecrementDelta((int) Math.round(
                    -regressor.getSlope() * Math.exp(regressor.getLastPriceObserved())/
                            Math.exp(regressor.getLastQuantityConsumedObserved()) ));
        else
            predictor.setDecrementDelta(0);
    }
}
