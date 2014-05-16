/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.pricing;

import agents.firm.sales.SalesDepartment;
import agents.firm.sales.pricing.pid.SalesControlFlowPIDWithFixedInventoryButTargetingFlowsOnly;
import agents.firm.sales.pricing.pid.SalesControlWithFixedInventoryAndPID;
import agents.firm.sales.pricing.pid.SimpleFlowSellerPID;
import agents.firm.sales.pricing.pid.salesControlWithSmoothedinventoryAndPID;
import ec.util.MersenneTwisterFast;
import goods.Good;
import model.utilities.Deactivatable;
import model.utilities.NonDrawable;
import model.utilities.logs.LogNode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

/**
 * <h4>Description</h4>
 * <p/> Pricing strategy has only one method telling an agent how to price a specific good.
 * <p/> <b>There must be only one constructor</b>, and it must have SalesDepartment as argument
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-07-22
 * @see
 */
public interface AskPricingStrategy extends Deactivatable, LogNode {

    /**
     * The sales department is asked at what should be the sale price for a specific good; this, I guess, is the fundamental
     * part of the sales department
     *
     *
     * @param g the good to price
     * @return the price given to that good
     */
    public int price(Good g);




    /**
     * When the pricing strategy is changed or the firm is shutdown this is called. It's useful to kill off steppables and so on
     */
    public void turnOff();


    /**
     * After computing all statistics the sales department calls the weekEnd method. This might come in handy
     */
    public void weekEnd();

    /**
     * asks the pricing strategy if the inventory is acceptable
     * @param inventorySize
     * @return
     */
    boolean isInventoryAcceptable(int inventorySize);


    /**
     * This is somewhat similar to rate current level. It estimates the excess (or shortage)of goods sold. It is basically
     * getCurrentInventory-AcceptableInventory
     * @return positive if there is an excess of goods bought, negative if there is a shortage, 0 if you are right on target.
     */
    public float estimateSupplyGap();



    /**
     * This is the static generator to create random or non-random buyerSearchAlgorithm.
     * It expects all subclasses to have a constructor with as two arguments: market and firm
     */
    public static class Factory {

        /**
         * This holds a list of all the subclasses of BuyerSearchAlgorithm. It's an arraylist to make randomization easier, but it's generated as a set first so there won't be duplicates
         */
        final private static ArrayList<Class<? extends AskPricingStrategy>> rules;

        //static clause to fill the set names
        static {
            rules = new ArrayList<>(); //read all the rules
            rules.add(CostAskPricing.class);
            rules.add(EverythingMustGoAdaptive.class);
            rules.add(MarkupFollower.class);
            rules.add(PriceFollower.class);
            rules.add(PriceImitator.class);
            rules.add(UndercuttingAskPricing .class);
            rules.add(SalesControlFlowPIDWithFixedInventoryButTargetingFlowsOnly.class);
            rules.add(SalesControlWithFixedInventoryAndPID.class);
            rules.add(salesControlWithSmoothedinventoryAndPID.class);
            rules.add(SimpleFlowSellerPID.class);


            //remove not drawables
            rules.removeIf(aClass -> aClass.isAnnotationPresent(NonDrawable.class));
            assert rules.size() > 0; // there should be at least one!!
        }

        /**
         * Returns a new specific askprice strategy algorithm for this sales department
         *
         * @param rule the simpleName of the class!
         * @param sales the sales department that will use this pricing strategy
         * @return the new rule to follow
         */
        public static AskPricingStrategy newAskPricingStrategy( String rule, SalesDepartment sales) {
            for (Class<? extends AskPricingStrategy> c : rules) {
                if (c.getSimpleName().equals(rule)) //if the name matches
                {
                    try {

                        return c.getConstructor(SalesDepartment.class).newInstance(sales);
                    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException |
                            IllegalArgumentException | InvocationTargetException ex) {
                        throw new RuntimeException("failed to instantiate AskPricingStrategy" + ex.getMessage());
                    }


                }
            }

            //if you are here, nothing was found!
            throw new RuntimeException("failed to instantiate an algorithm with name" + rule);


        }


        /**
         * Returns a new random askprice strategy algorithm for this sales department
         * @param sales the sales department that will use this pricing strategy
         * @return the new rule to follow
         */
        public static AskPricingStrategy randomAskPricingStrategy( SalesDepartment sales)
        {
            Class<? extends AskPricingStrategy > askPricingStrategy = null;
            MersenneTwisterFast randomizer = sales.getFirm().getRandom(); //get the randomizer
            //now you are going to pick at random, but keep doing it as long as you draw abstract classes or interfaces
            while(askPricingStrategy == null || Modifier.isAbstract(askPricingStrategy.getModifiers())
                    || askPricingStrategy.isInterface())
            {
                //get a new rule
                askPricingStrategy = rules.get(randomizer.nextInt(rules.size()));
            }

            //now just instantiate it!
            return newAskPricingStrategy(askPricingStrategy,sales);


        }

        /**
         * Returns a new specific askprice strategy algorithm for this sales department
         *
         * @param rule the simpleName of the class!
         * @param sales the sales department that will use this pricing strategy
         * @return the new rule to follow
         */
        public static <AP extends AskPricingStrategy> AP newAskPricingStrategy(  Class<AP> rule, SalesDepartment sales )
        {

            if(!rules.contains(rule) || Modifier.isAbstract(rule.getModifiers()) || rule.isInterface() )
                throw new IllegalArgumentException("The rule given is either abstract or just not recognized");


            try {
                return rule.getConstructor(SalesDepartment.class).newInstance(sales);
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException |
                    IllegalArgumentException | InvocationTargetException ex) {
                throw new RuntimeException("failed to instantiate AskPricingStrategy " + ex.getMessage());
            }



        }

        /**
         * Private constructor to avoid anybody from instatiating this
         */
        private Factory() {
        }

    }



}
