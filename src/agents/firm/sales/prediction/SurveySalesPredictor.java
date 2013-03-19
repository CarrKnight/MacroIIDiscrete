/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.exploration.BuyerSearchAlgorithm;
import ec.util.MersenneTwisterFast;
import financial.Market;
import org.reflections.Reflections;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

/**
 * <h4>Description</h4>
 * <p/>
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-07-27
 * @see
 */
public class SurveySalesPredictor implements SalesPredictor {





    /**
     * The prediction of this class involves using the buyer search to
     *
     * @param dept                   the sales department that has to answer this question
     * @param expectedProductionCost the HQ estimate of costs in producing whatever it wants to sell. It isn't necesarilly used.
     * @return the best offer available/predicted or -1 if there are no quotes/good predictions
     */
    @Override
    public long predictSalePrice(SalesDepartment dept, long expectedProductionCost) {
        EconomicAgent bestBuyer = dept.getBuyerSearchAlgorithm().getBestInSampleBuyer();     //sample the best buyer
        if(bestBuyer == null)
            return -1; //none found, return sadly
        else
        {
            long offer = bestBuyer.askedForABuyOffer(dept.getMarket().getGoodType()); //
            assert offer >= 0; //should be a real offer!
            return offer;
        }
    }

    /**
     * Call this to kill the predictor
     */
    @Override
    public void turnOff() {


    }






}
