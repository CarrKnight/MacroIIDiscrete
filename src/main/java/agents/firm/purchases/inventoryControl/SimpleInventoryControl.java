/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.inventoryControl;

import agents.HasInventory;
import agents.firm.Firm;
import agents.firm.utilities.NumberOfPlantsListener;
import agents.firm.purchases.PurchasesDepartment;
import goods.GoodType;
import agents.firm.production.Plant;

import java.util.List;

/**
 * <h4>Description</h4>
 * <p/> Simple inventory control checks the minimum amount of goods needed to keep inputs going. Then each Level is just a multiple of it.
 * It keeps buying until it reaches the SAFE level (more than twice the minimum necessary).
 * <p/> It is a number of plants listener as it needs to update the need only when the number of plants differ
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-05
 * @see
 */
public class SimpleInventoryControl extends AbstractInventoryControl implements NumberOfPlantsListener
{

    /**
     * How much do you need to get all your firms up and running once
     */
    private int singleProductionRunNeed;


    public SimpleInventoryControl( PurchasesDepartment purchasesDepartment) {
        super(purchasesDepartment);
        purchasesDepartment.getFirm().addPlantCreationListener(this); //addSalesDepartmentListener yourself as a plant creation listener



        //update the needs
        updateProductionRunNeed();




    }

    /**
     * This is the method to be overriden by the subclasses so that the public method rateCurrentLevel is meaningful
     *
     * @return the inventory level rating
     */

    @Override
    protected Level rateInventory() {
        //how much do we have?
        int currentLevel = getPurchasesDepartment().getCurrentInventory();
        return rateInventory(currentLevel);

    }

    /**
     * Just like rateInventory() but with the inventory already queried
     * @param currentLevel  how much I have right now
     * @return the inventory level given what I have
     */

    private Level rateInventory(int currentLevel){
        if(currentLevel < singleProductionRunNeed)
            return Level.DANGER;
        else if(currentLevel < 2 * singleProductionRunNeed)
            return Level.BARELY;
        else if(currentLevel < 3 * singleProductionRunNeed )
            return Level.ACCEPTABLE;
        else
        {
            assert currentLevel >= 3*singleProductionRunNeed;
            return Level.TOOMUCH;
        }
    }

    /**
     * As long as we judge the inventory less than acceptable we do  buy
     * @param source   The firm
     * @param type     The goodtype associated with this inventory control
     * @param quantity the new inventory level
     * @return true if we need to buy one more good
     */
    @Override
    protected boolean shouldIBuy(HasInventory source, GoodType type, int quantity) {
        return rateInventory(quantity).compareTo(Level.ACCEPTABLE) < 0; //keep buying as long as it is not at acceptable levels
    }

    /**
     * As long as we judge the inventory less than acceptable we do  buy
     * @return true if inventory level is less than acceptable
     */
    @Override
    public boolean canBuy() {
        return getSingleProductionRunNeed() >0 &&rateInventory().compareTo(Level.ACCEPTABLE) <= 0; //keep buying as long as it is not at acceptable levels
    }

    /**
     * call this when the variable "singleProductionRunNeed" needs updating. In this case just when the number of plants change
     */
    private void updateProductionRunNeed(){

        singleProductionRunNeed = 0;
        //get the firm
        Firm f = getPurchasesDepartment().getFirm();
        List<Plant> importantPlants = f.getListOfPlantsUsingSpecificInput(getGoodTypeToControl());

        for(Plant p : importantPlants)
        {
            //addSalesDepartmentListener the plant needs to the overall needs
            singleProductionRunNeed += p.getBlueprint().getInputs().get(getGoodTypeToControl());

        }

    }

    /**
     * React to a new plant having been built
     *
     * @param firm     the firm who built the plant
     * @param newPlant the new plant!
     */
    @Override
    public void plantCreatedEvent(Firm firm, Plant newPlant) {
        updateProductionRunNeed();
    }

    /**
     * React to an old plant having been closed/made obsolete
     *
     * @param firm     the firm who built the plant
     * @param newPlant the old plant
     */
    @Override
    public void plantClosedEvent(Firm firm, Plant newPlant) {
        updateProductionRunNeed();
    }

    /**
     * Turns off the inventory control. Useful if we are going out of business or being replaced.
     * On top of the original shutdown, this class also stops listening to new plants being built
     */
    @Override
    public void turnOff() {
        super.turnOff();
        boolean successfullyRemoved = getPurchasesDepartment().getFirm().removePlantCreationListener(this);
        assert successfullyRemoved;


    }

    /**
     * Returns estimated needs for this inventory control
     */
    public int getSingleProductionRunNeed() {
        return singleProductionRunNeed;
    }


    /**
     * This is somewhat similar to rate current level. It estimates the excess (or shortage)of goods purchased. It is basically
     * getCurrentInventory-AcceptableInventory
     *
     * @return positive if there is an excess of goods bought, negative if there is a shortage, 0 if you are right on target.
     */
    @Override
    public int estimateDemandGap() {
        Level currentLevel = rateCurrentLevel();
        if(currentLevel == null || currentLevel.equals(Level.ACCEPTABLE))
           return 0;
        else
       {
           int currentInventory = getPurchasesDepartment().getCurrentInventory();
           if(currentInventory<3 * singleProductionRunNeed)
           {
               return currentInventory - 2 * singleProductionRunNeed;
           }
           else
               return currentInventory - 3 * singleProductionRunNeed;
       }

    }
}
