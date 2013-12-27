/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.inventoryControl;

import agents.HasInventory;
import agents.firm.Firm;
import agents.firm.utilities.NumberOfPlantsListener;
import agents.firm.purchases.PurchasesDepartment;
import goods.GoodType;
import agents.firm.production.Plant;
import agents.firm.production.PlantListener;
import agents.firm.production.technology.Machinery;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

/**
 * <h4>Description</h4>
 * <p/>  This inventory control targets weekly needs of its plants. It consider acceptable holding input inventories equal to weekly input consumption.
 * <p/> It implements both plantListener and NumberOfPlantListener as the weekly input volume changes both with new plants being built and with a change in the number of workers
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-06
 * @see PlantListener
 * @see NumberOfPlantsListener
 */
//todo remember to switch name to DAILY
public class DailyInventoryControl extends AbstractInventoryControl implements PlantListener, NumberOfPlantsListener
{

    /**
     * How much we target to buy
     */
    private float dailyTarget = 0;

    /**
     * How much do you to get all your firms up and running once
     */
    private int singleProductionRunNeed;


    private int howManyDaysOfInventoryToHold = 2;


    /**
     * This list is useful when shutting down to make sure we close all the possible listeners
     */
    private LinkedList<Plant> plantsListened = new LinkedList<>();

    /**
     * Creates the inventory control and sets up its listeners. Also, as inherited, it sets itself to adjust as soon as possible to check whether to buy or not
     * @param purchasesDepartment the purchases department
     */
    public DailyInventoryControl(@Nonnull PurchasesDepartment purchasesDepartment) {
        super(purchasesDepartment);
        purchasesDepartment.getFirm().addPlantCreationListener(this); //addSalesDepartmentListener yourself as a plant creation listener
        //listen to each initial plant
        for(Plant p : purchasesDepartment.getFirm().getListOfPlantsUsingSpecificInput(getGoodTypeToControl()))
        {
            plantsListened.add(p);
            p.addListener(this);
        }


        //compute what are you going to need for first week.
        updateWeeklyNeeds();

    }

    /**
     * Call this to compute how much do we need for a week of run.
     */
    private void updateWeeklyNeeds(){


        int oldNeeds = 0;

        dailyTarget = 0;
        singleProductionRunNeed=0;
        int weeklyNeeds = 0;
        //get the firm
        Firm f = getPurchasesDepartment().getFirm();
        List<Plant> importantPlants = f.getListOfPlantsUsingSpecificInput(getGoodTypeToControl());

        for(Plant p : importantPlants)
        {

            //addSalesDepartmentListener the plant needs to the overall needs
            int oneRunNeeds = p.getBlueprint().getInputs().get(getGoodTypeToControl());

            //weekly need of a plant = needs for a run * runs in a week
            float weeklyNeedsThisPlant = (p.expectedWeeklyProductionRuns() * oneRunNeeds);
            if(weeklyNeedsThisPlant > 0)
                singleProductionRunNeed += oneRunNeeds; //count for a single production as long as there is at least a little bit being done every day
            weeklyNeeds +=  weeklyNeedsThisPlant;

        }

        dailyTarget =howManyDaysOfInventoryToHold * ((float)weeklyNeeds) /7f;

        //if this is an update whereas we moved from targeting 0 to targeting something else, buy
        if(oldNeeds ==0 && dailyTarget > 0 && canBuy())
            getPurchasesDepartment().buy();

    }

    /**
     * React to a new plant having been built
     *
     * @param firm     the firm who built the plant
     * @param newPlant the new plant!
     */
    @Override
    public void plantCreatedEvent(Firm firm, Plant newPlant) {
        plantsListened.add(newPlant);
        newPlant.addListener(this); //start listening to this
        updateWeeklyNeeds();
    }

    /**
     * React to an old plant having been closed/made obsolete
     *
     * @param firm     the firm who built the plant
     * @param oldPlant the old plant
     */
    @Override
    public void plantClosedEvent(Firm firm, Plant oldPlant) {
        plantsListened.remove(oldPlant);
        oldPlant.removeListener(this); //start listening to this
        updateWeeklyNeeds();
    }

    /**
     * This is the method to be overriden by the subclasses so that the public method rateCurrentLevel is meaningful
     *
     * @return the inventory level rating
     */
    @Nonnull
    @Override
    protected Level rateInventory() {
        int inventoryAmount = getPurchasesDepartment().getCurrentInventory(); //get inventory
        return rateInventory(inventoryAmount); //call the method
    }

    /**
     * Just lik rateInventory() but with the inventory already queried
     * @param currentLevel  how much I have right now
     * @return the inventory level given what I have
     */
    @Nonnull
    private Level rateInventory(int currentLevel){

        int dangerLevel = (int) Math.min(singleProductionRunNeed, dailyTarget+ 1);

        if(currentLevel < dangerLevel)
            return Level.DANGER;
        else if(currentLevel < dailyTarget)
            return Level.BARELY;
        else if(currentLevel < 1.5f* dailyTarget || dailyTarget == 0)
            return Level.ACCEPTABLE;
        else
        {
            assert currentLevel >= 1.5f* dailyTarget;
            return Level.TOOMUCH;
        }


    }


    /**
     * This is used by the purchases department for either debug or to ask the inventory control if
     * we can accept offer from peddlers.<br>
     * Asserts expect this to be consistent with the usual behavior of inventory control. But if asserts are off, then there is no other check
     *
     * @return
     */
    @Override
    public boolean canBuy() {
        return dailyTarget > 0 && rateInventory().compareTo(Level.ACCEPTABLE) <= 0; //keep buying as long as it is not at acceptable levels

    }

    /**
     * keep buying as long as it is not at acceptable levels
     * @param source   The firm
     * @param type     The goodtype associated with this inventory control
     * @param quantity the new inventory level
     * @return true if we need to buy one more good
     */
    @Override
    protected boolean shouldIBuy(HasInventory source, GoodType type, int quantity) {

        return rateInventory(quantity).compareTo(Level.ACCEPTABLE) <= 0; //keep buying as long as it is not at acceptable levels
    }

    /**
     * This is called whenever a plant has changed the number of workers
     *
     * @param p          the plant that made the change
     * @param workerSizeNow the new number of workers
     * @param workerSizeBefore
     */
    @Override
    public void changeInWorkforceEvent(Plant p, int workerSizeNow, int workerSizeBefore) {
        updateWeeklyNeeds();
    }

    /**
     * This is called whenever a plant has been shut down or just went obsolete
     *
     * @param p the plant that made the change
     */
    @Override
    public void plantShutdownEvent(Plant p) {
        //ignore this, you'll get notified by the NumberOfPlantsListener
    }

    /**
     * This is called by the plant whenever the machinery used has been changed
     *
     * @param p         The plant p
     * @param machinery the machinery used.
     */
    @Override
    public void changeInMachineryEvent(Plant p, Machinery machinery) {
        updateWeeklyNeeds();
    }

    /**
     * Turns off the inventory control. Useful if we are going out of business or being replaced.
     * On top of the original shutdown, this class also stops listening to new plants being built
     */
    @Override
    public void turnOff() {
        super.turnOff();
        //stop listening to plant creation
        boolean successfullyRemoved = getPurchasesDepartment().getFirm().removePlantCreationListener(this);
        assert successfullyRemoved;
        for(Plant p :plantsListened)    //this might be useless if plants are turnedoff before the strategy
            p.removeListener(this); //stop listening!
        plantsListened.clear();


    }

    /**
     * How much control thinks it needs to keep functioning for a week
     * @return weekly needs
     */
    public float getDailyTarget() {
        return dailyTarget;
    }

    /**
     * ignored
     */
    @Override
    public void changeInWageEvent(Plant p, int workerSize, long wage) {
    }


    /**
     * This is somewhat similar to rate current level. It estimates the excess (or shortage)of goods purchased. It is basically
     * getCurrentInventory-AcceptableInventory
     *
     * @return positive if there is an excess of goods bought, negative if there is a shortage, 0 if you are right on target.
     */
    @Override
    public int estimateDemandGap()
    {
        Level currentLevel = rateCurrentLevel();
        if(currentLevel == null || currentLevel.equals(Level.ACCEPTABLE))
            return 0;
        else
        {
            int currentInventory = getPurchasesDepartment().getCurrentInventory();
            if(currentInventory<3 * dailyTarget)
            {
                return Math.round(currentInventory - dailyTarget);
            }
            else
                return Math.round(currentInventory - 1.5f* dailyTarget);
        }

    }

    public int getHowManyDaysOfInventoryToHold() {
        return howManyDaysOfInventoryToHold;
    }

    public void setHowManyDaysOfInventoryToHold(int howManyDaysOfInventoryToHold) {
        this.howManyDaysOfInventoryToHold = howManyDaysOfInventoryToHold;
        updateWeeklyNeeds();
    }
}
