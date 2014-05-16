/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.production.control;

import agents.firm.personell.HumanResources;
import agents.firm.purchases.pricing.BidPricingStrategy;
import goods.GoodType;
import agents.firm.production.PlantListener;
import model.utilities.Control;

/**
 * <h4>Description</h4>
 * <p/> This is the interface is the rule the plant follows to set its production rhythm: basically how many workers it will try to hire.
 * <p/> In principle this problem is not different from inventory control,that's why they share the same interface. The main differences regard pricing. All plant controls
 * are integrated (both Control and Pricing) because the wage is the variable to control.
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-20
 * @see
 */
public interface PlantControl extends Control, BidPricingStrategy, PlantListener {


    /**
     * Answer the question: how much am I willing to pay for this kind of labor?
     * Notice that NO UPDATING SHOULD TAKE PLACE in calling this method. Human Resources expects maxPrice() to be consistent from one call to the next.
     * To notify hr of inconsistencies call updateEmployeeWages(). <br>
     * In short,for plant control, <b>this should be a simple getter.</b>. If you are a subclass and want to change wages, use the current wage setter.
     *
     * @param type the type of good you want to buy
     * @return the maximum price I am willing to pay for this good
     */
    @Override
    public int maxPrice(GoodType type);


    /**
     * The controller sets wages for everybody. Probably only used by subcomponents
     * @param newWage the new wage
     */
    public void setCurrentWage(int newWage);


    /**
     * Generic getter to know the human resources objects associated with the control
     */
    public HumanResources getHr();

    /**
     * get workforce size targeted
     */
    public int getTarget();

    /**
     * set the workforce size target
     */
    public void setTarget(int workSize);


    /**
     * Set whether or not the control can buy
     */
    public void setCanBuy(boolean canBuy);

    /**
     * Get the current wages paid by the control
     */
    public long getCurrentWage();



}
