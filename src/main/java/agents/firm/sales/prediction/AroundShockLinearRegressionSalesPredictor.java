/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.Firm;
import agents.firm.production.Plant;
import agents.firm.sales.SalesDepartment;
import model.MacroII;
import model.utilities.stats.collectors.enums.SalesDataType;
import model.utilities.stats.regression.LinearRegression;

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
public class AroundShockLinearRegressionSalesPredictor implements SalesPredictor {


    final private FixedDecreaseSalesPredictor predictor = new FixedDecreaseSalesPredictor();

    final private LinearRegression regression = new LinearRegression();

    final private SalesDepartment department;

    final private Firm owner;

    final private MacroII model;

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
    private int maximumNumberOfDaysToLookAhead = 60;


    /**
     * how many days MUST have passed before we even try to regress
     */
    private int minimumNumberOfDaysToLookAhead = 5;  //a little bit less than 2 weeks.

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
        System.out.println("slope: " + predictor.getDecrementDelta() + ", day: " + findLatestShockDay());
        return predictor.predictSalePriceAfterIncreasingProduction(dept, expectedProductionCost, increaseStep);


    }

    private void updateModelIfPossible(SalesDepartment dept) {
        int shockDay = findLatestShockDay();
        if(shockDay>=1){
            int lowestBound=Math.max(0,shockDay-howManyDaysBackShallILook);
            assert lowestBound<shockDay;
            int now = (int) model.getMainScheduleTime();
            int upperBound = Math.min(now -1,shockDay+maximumNumberOfDaysToLookAhead );
            assert upperBound>=lowestBound;
            //if we have enough observations post shock day AND more than one worker both before and after, then do the regression!
            if(upperBound >= shockDay + minimumNumberOfDaysToLookAhead
                    &&
                    lowestBound != lastUsedLowerBound && lastUsedUpperBound != upperBound)
            {
                assert upperBound>lowestBound;

                double[] quantity = dept.getObservationsRecordedTheseDays(SalesDataType.OUTFLOW, lowestBound, upperBound);
                double[] price = dept.getObservationsRecordedTheseDays(SalesDataType.AVERAGE_CLOSING_PRICES, lowestBound, upperBound);



                //build weights
                double[] weights = new double[quantity.length];
                double[] gaps =  dept.getObservationsRecordedTheseDays(SalesDataType.SUPPLY_GAP, lowestBound, upperBound);
                for(int i=0; i<weights.length; i++)
                {
                    double gap = gaps[i];
                    if(quantity[i]==0 || price[i]==-1) //if there was nothing traded that day, ignore the observation entirely
                        weights[i]=0;
                    else {
                        double weight = 1d / (1d + Math.exp(Math.abs(gap)));
                        weights[i] = weight;
                    }
                }

                //run the regression
                regression.estimateModel(quantity,price,weights);


                if(lastUsedLowerBound == -1) //if this is the first regression
                {
                    predictor.setDecrementDelta((float) -regression.getSlope());
                }
                else
                {
                    //combine old and new slope (minuses abound, but that's because I coded the decrementDelta weirdly)
                    float weightedAverage = (float) -(regression.getSlope() * .5f - predictor.getDecrementDelta() * .5f);
                    predictor.setDecrementDelta(weightedAverage);
                }

                System.out.println("slope: " + predictor.getDecrementDelta() +", workers: " + department.getLatestObservation(SalesDataType.WORKERS_PRODUCING_THIS_GOOD));

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
    public long predictSalePriceWhenNotChangingPoduction(SalesDepartment dept) {
        return predictor.predictSalePriceWhenNotChangingPoduction(dept);
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
