/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.sales.SalesDepartment;
import model.MacroII;
import model.utilities.stats.collectors.DataStorage;
import model.utilities.stats.collectors.enums.MarketDataType;
import model.utilities.stats.collectors.enums.SalesDataType;

/**
 * <h4>Description</h4>
 * <p/> A simple recursive, every X days linear regression
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-11-10
 * @see
 */
public class RecursiveSalePredictor extends AbstractRecursivePredictor implements SalesPredictor{


    /**
     * whether our x is workers or outflow
     */
    boolean regressingOnWorkers = false;


    /**
     * the purchase department we are predicting for
     */
    final private SalesDepartment department;

    /**
     * to use if the regression is simply linear
     */
    private final FixedDecreaseSalesPredictor delegate = new FixedDecreaseSalesPredictor();


    public RecursiveSalePredictor(MacroII model, SalesDepartment department) {
        this(model, department,AbstractRecursivePredictor.defaultPriceLags,AbstractRecursivePredictor.defaultIndependentLags,defaultMovingAverageSize);
    }

    public RecursiveSalePredictor(MacroII model, SalesDepartment department,int movingAverageSize) {
        this(model, department,AbstractRecursivePredictor.defaultPriceLags,AbstractRecursivePredictor.defaultIndependentLags,movingAverageSize);
    }

    public RecursiveSalePredictor(final MacroII model, final SalesDepartment department,
                                  int priceLags, int indepedentLags, int movingAverageSize) {
        this(model, department,new double[indepedentLags+priceLags+1], priceLags, indepedentLags,movingAverageSize);

    }



    public RecursiveSalePredictor(final MacroII model, final SalesDepartment department,double[] initialCoefficients,
                                  int priceLags, int indepedentLags, int movingAverageSize) {
        super(model,initialCoefficients,priceLags,indepedentLags,movingAverageSize);
        this.department = department;

    }


    /**
     * This is called by the firm when it wants to predict the price they can sell to if they increase production
     *
     * @param dept                   the sales department that has to answer this question
     * @param expectedProductionCost the HQ estimate of costs in producing whatever it wants to sell. It isn't necesarilly used.
     * @param increaseStep           by how much the daily production will increase (has to be a positive number)
     * @return the best offer available/predicted or -1 if there are no quotes/good predictions
     */
    @Override
    public float predictSalePriceAfterIncreasingProduction(SalesDepartment dept, int expectedProductionCost, int increaseStep) {

        if(getIndependentLags() > 1 || getPriceLags() > 0)
        {
            double toReturn = Math.min(predictPrice(increaseStep), predictPrice(0));
            return (float) toReturn;
        }
        else
        {
            delegate.setDecrementDelta(-(float)((predictPrice(1)-predictPrice(0))));
            return delegate.predictSalePriceAfterIncreasingProduction(dept,expectedProductionCost,increaseStep);
        }

    }

    /**
     * This is called by the firm when it wants to predict the price they can sell to if they increase production
     *
     * @param dept                   the sales department that has to answer this question
     * @param expectedProductionCost the HQ estimate of costs in producing whatever it wants to sell. It isn't necesarilly used.
     * @param decreaseStep           by how much the daily production will decrease (has to be a positive number)
     * @return the best offer available/predicted or -1 if there are no quotes/good predictions
     */
    @Override
    public float predictSalePriceAfterDecreasingProduction(SalesDepartment dept, int expectedProductionCost, int decreaseStep) {



        if(getIndependentLags() > 1 || getPriceLags() > 0)
        {
            double toReturn = Math.max(predictPrice(-decreaseStep), predictPrice(0));
            return (int) Math.round(toReturn);
        }
        else
        {
            delegate.setDecrementDelta(-(float)((predictPrice(0)-predictPrice(-1))));
            return delegate.predictSalePriceAfterDecreasingProduction(dept, expectedProductionCost, decreaseStep);
        }

    }

    /**
     * This is a little bit weird to predict, but basically you want to know what will be "tomorrow" price if you don't change production.
     * Most predictors simply return today closing price, because maybe this will be useful in some cases. It's used by Marginal Maximizer Statics
     *
     * @param dept the sales department
     * @return predicted price
     */
    @Override
    public float predictSalePriceWhenNotChangingProduction(SalesDepartment dept) {

        if(getIndependentLags() > 1 || getPriceLags() > 0)
        {
            return (int) Math.round(predictPrice(0));
        }
        else
        {
            delegate.setDecrementDelta(-(float)((predictPrice(1)-predictPrice(0))));
            return delegate.predictSalePriceWhenNotChangingProduction(dept);
        }
    }


    public void setRegressingOnWorkers(boolean regressingOnWorkers) {
        this.regressingOnWorkers = regressingOnWorkers;
    }

    /**
     * simulates the decrement delta
     * @return
     */
    public double getDecrementDelta()
    {

        return -(predictPrice(1)-predictPrice(0));
    }


    public Enum getXVariableType() {
        if(regressingOnWorkers)
            return SalesDataType.WORKERS_PRODUCING_THIS_GOOD;
        else
            return SalesDataType.OUTFLOW;
    }

    public Enum getYVariableType() {
        return SalesDataType.LAST_ASKED_PRICE;
    }

    public int modifyStepIfNeeded(int step) {
        if(regressingOnWorkers)
            step = Integer.signum(step);
        return step;
    }

    public double defaultPriceWithNoObservations() {
        long lastPrice = Math.round(department.getAveragedPrice());  //get the last closing price
        //do we not have anything in memory or did we screw up so badly
        //in the past term that we didn't sell a single item?
        if(lastPrice == -1)
            if(department.getTotalWorkersWhoProduceThisGood() == 0 && department.getMarket().getNumberOfObservations() > 0) //if you have no price to lookup and no production you are in a vicious circle, just lookup the market then
                return Math.round(department.getMarket().getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE));
            else
                return -1;
        else
        {
            //return your memory.
            assert lastPrice >= 0 : lastPrice;

            return lastPrice;

        }
    }

    @Override
    public DataStorage getData() {
        return department.getData();

    }

    @Override
    public boolean hasDepartmentTradedAtLeastOnce() {
        return department.hasTradedAtLeastOnce();
    }


    public SalesDataType getDisturbanceType() {
        return SalesDataType.SUPPLY_GAP;
    }


}
