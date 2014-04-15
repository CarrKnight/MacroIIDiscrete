/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases;

import agents.firm.purchases.inventoryControl.DailyInventoryControl;
import agents.firm.purchases.inventoryControl.FixedInventoryControl;
import agents.firm.purchases.inventoryControl.InventoryControl;
import agents.firm.purchases.inventoryControl.SimpleInventoryControl;
import agents.firm.purchases.pid.PurchasesDailyPID;
import agents.firm.purchases.pid.PurchasesFixedPID;
import agents.firm.purchases.pid.PurchasesSimplePID;
import agents.firm.purchases.pricing.*;
import ec.util.MersenneTwisterFast;
import model.utilities.NonDrawable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

/**
 * <h4>Description</h4>
 * <p/>  This is a utility class that purchases department can call in order to generate pricing bidPricingRules, inventory controls or both
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-13
 * @see
 */
public class PurchasesRuleFactory {
    //private constructor.
    private PurchasesRuleFactory(){}

    /**
     * This holds a list of all the subclasses of InventoryControl. It's an arraylist to make randomization easier, but it's generated as a set first so there won't be duplicates
     */
    final private static ArrayList<Class<? extends InventoryControl>> inventoryControlRules;
    /**
     * This holds a list of all the subclasses of BidPricingStrategy. It's an arraylist to make randomization easier, but it's generated as a set first so there won't be duplicates
     */
    final private static ArrayList<Class<? extends BidPricingStrategy>> bidPricingRules;
    /**
     * This holds a list of all the subclasses of BOTH BidPricingStrategy and InventoryControl
     */
    final private static ArrayList<Class<? extends BidPricingStrategy>> integratedRules;


    //static clause to fill the set names
    static {
        bidPricingRules = new ArrayList<>(); //read all the bidPricingRules
        bidPricingRules.add(CheaterPricing.class);
        bidPricingRules.add(SurveyMaxPricing .class);
        bidPricingRules.add(UrgentPriceFollowerStrategy .class);
        bidPricingRules.add(ZeroIntelligenceBidPricing .class);
        assert bidPricingRules.size() > 0; // there should be at least one!!

        inventoryControlRules = new ArrayList<>(); //read all the inventoryControlRules
        inventoryControlRules.add(DailyInventoryControl.class);
        inventoryControlRules.add(FixedInventoryControl .class);
        inventoryControlRules.add(SimpleInventoryControl .class);
        assert inventoryControlRules.size() > 0; // there should be at least one!!

        integratedRules = new ArrayList<>();
        integratedRules.add(PurchasesDailyPID.class);
        integratedRules.add(PurchasesFixedPID .class);
        integratedRules.add(PurchasesSimplePID .class);

        assert integratedRules.size() > 0;
        //remove it from the other lists


        //remove not drawables

        bidPricingRules.removeIf(aClass -> aClass.isAnnotationPresent(NonDrawable.class));
        inventoryControlRules.removeIf(aClass -> aClass.isAnnotationPresent(NonDrawable.class));
        integratedRules.removeIf(aClass -> aClass.isAnnotationPresent(NonDrawable.class));



    }

    /**
     * Returns a new specific BidPricingStrategy (but not a rule that is BOTH a pricing and an inventory control rule)
     *
     * @param rule the simpleName of the class!
     * @param department  the department owning the rule
     * @return the new rule to follow
     */
    public static BidPricingStrategy newBidPricingStrategy( String rule, PurchasesDepartment department) {
        for (Class<? extends BidPricingStrategy> c : bidPricingRules) {
            if (c.getSimpleName().equals(rule)) //if the name matches
            {
                try {

                    return  c.getConstructor(PurchasesDepartment.class).newInstance(department); //return it!
                } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException |
                        IllegalArgumentException | InvocationTargetException ex) {
                    throw new RuntimeException("failed to instantiate BidPricingStrategy" + ex.getMessage());
                }


            }
        }

        //if you are, nothing was found!
        throw new RuntimeException("failed to instantiate a market algorithm with name" + rule);


    }

    public static BidPricingStrategy randomBidPricingStrategy( PurchasesDepartment department)
    {
        Class<? extends BidPricingStrategy> bidPricingStrategy= null;
        MersenneTwisterFast randomizer = department.getFirm().getRandom(); //get the randomizer
        //now you are going to pick at random, but keep doing it as long as you draw abstract classes or interfaces
        while(bidPricingStrategy== null || Modifier.isAbstract(bidPricingStrategy.getModifiers()) || bidPricingStrategy.isInterface())
        {
            //get a new rule
            bidPricingStrategy= bidPricingRules.get(randomizer.nextInt(bidPricingRules.size()));
        }

        //now just instantiate it!
        return newBidPricingStrategy(bidPricingStrategy,department);


    }

    public static <BP extends  BidPricingStrategy>
    BP newBidPricingStrategy(  Class<BP> rule, PurchasesDepartment department )
    {

        if(!bidPricingRules.contains(rule) || Modifier.isAbstract(rule.getModifiers()) || rule.isInterface() )
            throw new IllegalArgumentException("The rule given is either abstract or just not recognized " + rule.getSimpleName());


        try {
            return rule.getConstructor(PurchasesDepartment.class).newInstance(department);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException |
                IllegalArgumentException | InvocationTargetException ex) {
            ex.printStackTrace();
            throw new RuntimeException("failed to instantiate BidPricingStrategy" + ex.getMessage());
        }



    }


    /**
     * Returns a new specific InventoryControl (but not a rule that is BOTH a pricing and inventory control rule)
     *
     * @param rule the simpleName of the class!
     * @param department  the department owning the rule
     * @return the new rule to follow
     */
    public static InventoryControl newInventoryControl( String rule, PurchasesDepartment department) {
        for (Class<? extends InventoryControl> c : inventoryControlRules) {
            if (c.getSimpleName().equals(rule)) //if the name matches
            {
                try {

                    return c.getConstructor(PurchasesDepartment.class).newInstance(department); //return it!
                } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException |
                        IllegalArgumentException | InvocationTargetException ex) {
                    throw new RuntimeException("failed to instantiate InventoryControl" + ex.getMessage());
                }


            }
        }

        //if you are, nothing was found!
        throw new RuntimeException("failed to instantiate a market algorithm with name" + rule);


    }

    public static InventoryControl randomInventoryControl( PurchasesDepartment department)
    {
        Class<? extends InventoryControl > inventoryControl = null;
        MersenneTwisterFast randomizer = department.getFirm().getRandom(); //get the randomizer
        //now you are going to pick at random, but keep doing it as long as you draw abstract classes or interfaces
        while(inventoryControl == null || Modifier.isAbstract(inventoryControl.getModifiers()) || inventoryControl.isInterface())
        {
            assert randomizer != null;
            assert !inventoryControlRules.isEmpty();
            //get a new rule
            inventoryControl = inventoryControlRules.get(randomizer.nextInt(inventoryControlRules.size()));
        }

        //now just instantiate it!
        return newInventoryControl(inventoryControl,department);


    }

    public static <IC extends InventoryControl>
    IC newInventoryControl(  Class<IC> rule, PurchasesDepartment department )
    {
        assert rule != null;

        if(!inventoryControlRules.contains(rule) || Modifier.isAbstract(rule.getModifiers()) || rule.isInterface() )
            throw new IllegalArgumentException("The rule given is either abstract or just not recognized");


        try {
            return rule.getConstructor(PurchasesDepartment.class).newInstance(department);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException |
                IllegalArgumentException | InvocationTargetException ex)
        {
            ex.printStackTrace();
            throw new RuntimeException(" failed to instantiate InventoryControl ----- " + ex.getMessage());
        }



    }



    /**
     * Returns a new rule tht is BOTH a bidPricingStrategy and an inventory control!
     *
     * @param rule the simpleName of the class!
     * @param department  the department owning the rule
     * @return the new rule to follow
     */
    public static BidPricingStrategy newIntegratedRule( String rule, PurchasesDepartment department) {
        for (Class<? extends BidPricingStrategy> c : integratedRules) {
            if (c.getSimpleName().equals(rule)) //if the name matches
            {
                try {
                    BidPricingStrategy toReturn =  c.getConstructor(PurchasesDepartment.class).newInstance(department);
                    assert toReturn instanceof InventoryControl;
                    return toReturn; //return it!
                } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException |
                        IllegalArgumentException | InvocationTargetException ex) {
                    throw new RuntimeException("failed to instantiate BidPricingStrategy" + ex.getMessage());
                }


            }
        }

        //if you are, nothing was found!
        throw new RuntimeException("failed to instantiate a market algorithm with name" + rule);


    }

    public static <IC extends BidPricingStrategy & InventoryControl>
    BidPricingStrategy randomIntegratedRule( PurchasesDepartment department)
    {
        Class<? extends IC> bidPricingStrategy= null;
        MersenneTwisterFast randomizer = department.getFirm().getRandom(); //get the randomizer
        //now you are going to pick at random, but keep doing it as long as you draw abstract classes or interfaces
        while(bidPricingStrategy== null || Modifier.isAbstract(bidPricingStrategy.getModifiers()) || bidPricingStrategy.isInterface())
        {
            //get a new rule
            //this cast looks a bit iffy, but tested aplenty and it works fine.
            bidPricingStrategy= (Class<? extends IC>) integratedRules.get(randomizer.nextInt(integratedRules.size()));
        }

        //now just instantiate it!
        return newIntegratedRule(bidPricingStrategy,department);


    }

    public static <IC extends InventoryControl & BidPricingStrategy>
    IC newIntegratedRule(  Class<IC> rule, PurchasesDepartment department )
    {

        if(!integratedRules.contains(rule) || Modifier.isAbstract(rule.getModifiers()) || rule.isInterface() )
            throw new IllegalArgumentException("The rule given is either abstract or just not recognized " + rule.getSimpleName());


        try {
            IC toReturn =  rule.getConstructor(PurchasesDepartment.class).newInstance(department);
            assert toReturn!= null && toReturn instanceof InventoryControl && toReturn instanceof BidPricingStrategy;
            return toReturn; //return it!
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException |
                IllegalArgumentException | InvocationTargetException ex) {
            ex.printStackTrace();
            throw new RuntimeException("failed to instantiate BidPricingStrategy" + ex.getMessage());
        }



    }




}
