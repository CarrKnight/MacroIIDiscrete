/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.Firm;
import agents.firm.production.Plant;
import agents.firm.sales.SalesDepartment;
import com.google.common.primitives.Doubles;
import model.MacroII;
import model.utilities.logs.LogEvent;
import model.utilities.logs.LogLevel;
import model.utilities.stats.collectors.enums.SalesDataType;
import model.utilities.stats.regression.LinearRegression;

import java.util.ArrayList;
import java.util.List;

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
 * @author carrknight
 * @version 2013-08-27
 * @see
 */
public class AroundShockLinearRegressionSalesPredictor extends BaseSalesPredictor {


    final protected FixedDecreaseSalesPredictor predictor = new FixedDecreaseSalesPredictor();

    final protected LinearRegression regression = new LinearRegression();

    final protected SalesDepartment department;

    final protected Firm owner;

    final protected MacroII model;

    /**
     * builds the predictor
     */
    public AroundShockLinearRegressionSalesPredictor(SalesDepartment department) {
        this(department,department.getFirm(),department.getModel());
    }

    /**
     * builds the predictor
     * @param department
     * @param owner
     */
    public AroundShockLinearRegressionSalesPredictor(SalesDepartment department, Firm owner, MacroII model) {
        this.department = department;
        this.owner = owner;
        this.model=model;


    }


    /**
     * how many days before the shock can you look into?
     */
    private int howManyDaysBackShallILook  = 60;

    /**
     * how many days after the shock can you look into?
     */
    private int maximumNumberOfDaysToLookAhead = 50;


    /**
     * how many days MUST have passed before we even try to regress
     */
    private int minimumNumberOfDaysToLookAhead = 5;  //a little bit less than a week.

    /**
     * we memorize the bound so that we don't run the regression multiple times if there is no new data
     */
    private int lastUsedLowerBound = -1;
    /**
     * we memorize the bound so that we don't run the regression multiple times if there is no new data
     */
    private int lastUsedUpperBound = -1;

    /**
     * This is called by the firm when it wants to predict the price they can sell to if they increase production
     *
     * @param dept                   the sales department that has to answer this question
     * @param expectedProductionCost the HQ estimate of costs in producing whatever it wants to sell. It isn't necesarilly used.
     * @param increaseStep           by how much the daily production will increase (has to be a positive number)
     * @return the best offer available/predicted or -1 if there are no quotes/good predictions
     */
    @Override
    public long predictSalePriceAfterIncreasingProduction(SalesDepartment dept, long expectedProductionCost, int increaseStep) {

        updateModelIfPossible(dept);
        return predictor.predictSalePriceAfterIncreasingProduction(dept, expectedProductionCost, increaseStep);


    }

    /**
     * do any regression you see fit in changing the slope of the predictor!
     * @param dept
     */
    protected void updateModelIfPossible(SalesDepartment dept) {
        int shockDay = findLatestShockDay();
        if(shockDay>=1){
            int lowestBound=Math.max(0,shockDay-howManyDaysBackShallILook);
            assert lowestBound<shockDay;
            int now = (int) model.getMainScheduleTime();
            int upperBound = Math.min(now -1,shockDay+maximumNumberOfDaysToLookAhead );
            assert upperBound>=lowestBound;
            if(upperBound < shockDay + minimumNumberOfDaysToLookAhead) //if we don't have enough observation for today's shockday, just look at the most recent data!
            {
                upperBound = now-1;
                lowestBound = Math.max(1,upperBound - maximumNumberOfDaysToLookAhead - howManyDaysBackShallILook);
            }
            //if we have enough observations post shock day AND more than one worker both before and after, then do the regression!
            if(
                    lowestBound != lastUsedLowerBound && lastUsedUpperBound != upperBound)
            {
                assert upperBound>lowestBound;


                List<Double> quantities = new ArrayList<>(Doubles.asList(dept.getObservationsRecordedTheseDays(SalesDataType.OUTFLOW, lowestBound, upperBound)));
                List<Double> prices = new ArrayList<>(Doubles.asList(dept.getObservationsRecordedTheseDays(SalesDataType.AVERAGE_CLOSING_PRICES, lowestBound, upperBound)));
                List<Double> gaps = new ArrayList<>(Doubles.asList(dept.getObservationsRecordedTheseDays(SalesDataType.SUPPLY_GAP, lowestBound, upperBound)));


                while(prices.indexOf(-1d)!=-1)     //remove all days with no price (no sales)
                {
                    int indexToRemove=prices.indexOf(-1d);

                    prices.remove(indexToRemove);
                    quantities.remove(indexToRemove);
                    gaps.remove(indexToRemove);
                }


                double[] quantity = Doubles.toArray(quantities);
                double[] price = Doubles.toArray(prices);
                double[] gap =  Doubles.toArray(gaps);

                if(price.length < minimumNumberOfDaysToLookAhead + 1)
                    return;

                for(double thatDayPrice : price) //we don't want negative prices
                        if(thatDayPrice == -1)
                            return;




                //build weights
                double[] weights = new double[quantity.length];
                for(int i=0; i<weights.length; i++)
                {
                    double thisGap = gap[i];
                    if(quantity[i]==0 || price[i]==-1) //if there was nothing traded that day, ignore the observation entirely
                        weights[i]=0;
                    else {
                        double weight = 1d / (1d + Math.exp(Math.abs(thisGap)));
                        weights[i] = weight;
                    }
                }

                //run the regression
                regression.estimateModel(quantity,price,weights);


                if(lastUsedLowerBound == -1) //if this is the first regression
                {
                    predictor.setDecrementDelta((float) -regression.getSlope());
                    handleNewEvent(new LogEvent(this, LogLevel.TRACE,
                            "new regressed slope:{}",(-regression.getSlope())));
                }
                else
                {
                    //combine old and new slope (minuses abound, but that's because I coded the decrementDelta weirdly)
                    float weightedAverage = (float) -(regression.getSlope() * .5f - predictor.getDecrementDelta() * .5f);
                    predictor.setDecrementDelta(weightedAverage);
                    handleNewEvent(new LogEvent(this, LogLevel.TRACE,
                            "new regressed slope:{}",weightedAverage));
                }


                //memorize the new bounds
                lastUsedLowerBound = lowestBound;
                lastUsedUpperBound = upperBound;


            }
        }
    }




    public int findLatestShockDay()
    {
        List<Plant> plants = owner.getListOfPlantsProducingSpecificOutput(department.getGoodType());
        int latestShockDay = -1;
        for(Plant p : plants)
        {
            latestShockDay = Math.max(latestShockDay,p.getLastDayAMeaningfulChangeInWorkforceOccurred());
        }
        return latestShockDay;


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
    public long predictSalePriceAfterDecreasingProduction(SalesDepartment dept, long expectedProductionCost, int decreaseStep) {
        updateModelIfPossible(dept);

        return predictor.predictSalePriceAfterDecreasingProduction(dept,expectedProductionCost,decreaseStep);

    }


    /**
     * This is a little bit weird to predict, but basically you want to know what will be "tomorrow" price if you don't change production.
     * Most predictors simply return today closing price, because maybe this will be useful in some cases. It's used by Marginal Maximizer Statics
     *
     * @param dept the sales department
     * @return predicted price
     */
    @Override
    public long predictSalePriceWhenNotChangingProduction(SalesDepartment dept) {
        return predictor.predictSalePriceWhenNotChangingProduction(dept);
    }

    public int getHowManyDaysBackShallILook() {
        return howManyDaysBackShallILook;
    }

    public void setHowManyDaysBackShallILook(int howManyDaysBackShallILook) {
        this.howManyDaysBackShallILook = howManyDaysBackShallILook;
    }

    public int getMaximumNumberOfDaysToLookAhead() {
        return maximumNumberOfDaysToLookAhead;
    }

    public void setMaximumNumberOfDaysToLookAhead(int maximumNumberOfDaysToLookAhead) {
        this.maximumNumberOfDaysToLookAhead = maximumNumberOfDaysToLookAhead;
    }




    /**
     * Call this to kill the predictor
     */
    @Override
    public void turnOff() {
        super.turnOff();
        predictor.turnOff();
    }

    /**
     * Sets new how many days MUST have passed before we even try to regress.
     *
     * @param minimumNumberOfDaysToLookAhead New value of how many days MUST have passed before we even try to regress.
     */
    public void setMinimumNumberOfDaysToLookAhead(int minimumNumberOfDaysToLookAhead) {
        this.minimumNumberOfDaysToLookAhead = minimumNumberOfDaysToLookAhead;
    }

    /**
     * Gets how many days MUST have passed before we even try to regress.
     *
     * @return Value of how many days MUST have passed before we even try to regress.
     */
    public int getMinimumNumberOfDaysToLookAhead() {
        return minimumNumberOfDaysToLookAhead;
    }
}
