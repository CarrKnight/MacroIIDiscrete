/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.prediction;

import agents.firm.Firm;
import agents.firm.production.Plant;
import agents.firm.purchases.PurchasesDepartment;
import model.MacroII;
import model.utilities.stats.collectors.enums.PurchasesDataType;
import model.utilities.stats.regression.LinearRegression;

import java.util.List;

/**
 * <h4>Description</h4>
 * <p/> Finds the last time there was a change in workers, run a regression on the observations around that point and
 * from it estimate the slope!
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-08-26
 * @see
 */
public class AroundShockLinearRegressionPurchasePredictor implements PurchasesPredictor
{

    final private FixedIncreasePurchasesPredictor predictor = new FixedIncreasePurchasesPredictor();

    final private LinearRegression regression = new LinearRegression();

    final private PurchasesDepartment department;

    final private Firm owner;

    final private MacroII model;

    private int lastUsedLowerBound=-1;

    private int lastUsedUpperBound=-1;


    private int minimumNumberOfDaysToLookAhead=2;

    /**
     * builds the predictor
     */
    public AroundShockLinearRegressionPurchasePredictor(PurchasesDepartment department) {
        this(department,department.getFirm(),department.getModel());
    }

    /**
     * builds the predictor
     * @param department
     * @param owner
     */
    public AroundShockLinearRegressionPurchasePredictor(PurchasesDepartment department, Firm owner, MacroII model) {
        this.department = department;
        this.owner = owner;
        this.model=model;


    }


    /**
     * how many days before the shock can you look into?
     */
    private int howManyDaysBackShallILook  = 40;

    /**
     * how many days after the shock can you look into?
     */
    private int maximumNumberOfDaysToLookAhead = 60;

    /**
     * Predicts the future price of the next good to buy
     *
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    @Override
    public long predictPurchasePriceWhenIncreasingProduction(PurchasesDepartment dept) {

        updateModelIfPossible(dept);

     //   System.out.println(predictor.getIncrementDelta());
        return predictor.predictPurchasePriceWhenIncreasingProduction(dept);


    }

    private void updateModelIfPossible(PurchasesDepartment dept) {
        int shockDay = findLatestShockDay();
        if(shockDay>=1){
            int lowestBound=Math.max(0,shockDay-howManyDaysBackShallILook);
            assert lowestBound<shockDay;
            int upperBound = Math.min((int)model.getMainScheduleTime()-1,shockDay+maximumNumberOfDaysToLookAhead );
            assert upperBound>=lowestBound;
            if(upperBound >= shockDay + minimumNumberOfDaysToLookAhead
                    &&
                    wereWorkersAlwaysPresent(dept, shockDay) &&
                    lowestBound != lastUsedLowerBound && lastUsedUpperBound != upperBound)
            {
                assert upperBound>lowestBound;

                double[] quantity = dept.getObservationsRecordedTheseDays(PurchasesDataType.WORKERS_CONSUMING_THIS_GOOD, lowestBound, upperBound);
                double[] price = dept.getObservationsRecordedTheseDays(PurchasesDataType.CLOSING_PRICES, lowestBound, upperBound);

                //build weights
                double[] weights = new double[quantity.length];
                double[] gaps =  dept.getObservationsRecordedTheseDays(PurchasesDataType.DEMAND_GAP, lowestBound, upperBound);
                for(int i=0; i<weights.length; i++)
                {
                    double gap = gaps[i];
                    if(quantity[i]==0 || price[i]==-1) //if there was nothing traded that day, ignore the observation entirely
                        weights[i]=0;
                    else {
                        double weight = 2d / (1d + Math.exp(Math.abs(gap)));
                        weights[i] = weight;
                    }
                }



                regression.estimateModel(quantity,
                        price,
                        weights);

                if(lastUsedLowerBound == -1) //if this is the first regression
                {
                    predictor.setIncrementDelta((float) regression.getSlope());
                }
                else
                {
                    //combine old and new slope (minuses abound, but that's because I coded the decrementDelta weirdly)
                    float weightedAverage = (float) (regression.getSlope() * .5f + predictor.getIncrementDelta() * .5f);
              //      System.out.println("slope: " + weightedAverage);
                    predictor.setIncrementDelta(weightedAverage);
                }



                //memorize the new bounds
                lastUsedLowerBound = lowestBound;
                lastUsedUpperBound = upperBound;

            }
        }
    }

    private boolean wereWorkersAlwaysPresent(PurchasesDepartment dept, int shockDay) {

        return dept.getObservationRecordedThisDay(PurchasesDataType.WORKERS_CONSUMING_THIS_GOOD, shockDay - 1) > 0
                &&
                dept.getObservationRecordedThisDay(PurchasesDataType.WORKERS_CONSUMING_THIS_GOOD, shockDay + 1) > 0;

    }


    public int findLatestShockDay()
    {
        List<Plant> plants = owner.getListOfPlantsUsingSpecificInput(department.getGoodType());
        int latestShockDay = -1;
        for(Plant p : plants)
        {
            latestShockDay = Math.max(latestShockDay,p.getLastDayAMeaningfulChangeInWorkforceOccurred());
        }
        return latestShockDay;


    }

    /**
     * Predicts the future price of the next good to buy
     *
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    @Override
    public long predictPurchasePriceWhenDecreasingProduction(PurchasesDepartment dept) {
        updateModelIfPossible(dept);
    //    System.out.println("slope: " + predictor.getIncrementDelta());

        return predictor.predictPurchasePriceWhenDecreasingProduction(dept);

    }

    /**
     * Call this to kill the predictor
     */
    @Override
    public void turnOff() {
        predictor.turnOff();

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
}
