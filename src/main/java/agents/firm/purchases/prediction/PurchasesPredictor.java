/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases.prediction;

import agents.firm.purchases.PurchasesDepartment;
import ec.util.MersenneTwisterFast;
import financial.market.Market;
import model.MacroII;
import org.reflections.Reflections;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

/**
 * <h4>Description</h4>
 * <p/> This is the strategy owned by the purchases department (but more likely used by the production side) to predict what the price of future
 * purchases will be like.
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-10-04
 * @see
 */
public interface PurchasesPredictor
{

    /**
     * Predicts the future price of the next good to buy
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    public long predictPurchasePriceWhenIncreasingProduction(PurchasesDepartment dept);


    /**
     * Predicts the future price of the next good to buy
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    public long predictPurchasePriceWhenDecreasingProduction(PurchasesDepartment dept);

    /**
     * Predicts the future price of the next good to buy
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    public long predictPurchasePriceWhenNoChangeInProduction(PurchasesDepartment dept);


    /**
     * Call this to kill the predictor
     */
    public void turnOff();


    /**
     * This is the static generator to create random or non-random buyerSearchAlgorithm.
     * It expects all subclasses to have a constructor with as two arguments: market and firm
     */
    public static class Factory {

        /**
         * This holds a list of all the subclasses of BuyerSearchAlgorithm. It's an arraylist to make randomization easier, but it's generated as a set first so there won't be duplicates
         */
        final private static ArrayList<Class<? extends PurchasesPredictor>> rules;

        //static clause to fill the set names
        static {
            Reflections strategyReader = new Reflections("agents.firm.purchases.prediction");
            rules = new ArrayList<>(strategyReader.getSubTypesOf(PurchasesPredictor.class)); //read all the rules
            assert rules.size() > 0; // there should be at least one!!
        }

        /**
         * Returns a new price predictor algorithm for this sales departmetn
         *
         * @param rule the simpleName of the class!
         * @return the new rule to follow
         */
        public static PurchasesPredictor newPurchasesPredictor(@Nonnull String rule, PurchasesDepartment purchasesDepartment) {
            for (Class<? extends PurchasesPredictor> c : rules) {
                if (c.getSimpleName().equals(rule)) //if the name matches
                {
                    try {

                        return c.newInstance();

                    } catch (SecurityException | InstantiationException | IllegalAccessException |
                            IllegalArgumentException ex) {
                        throw new RuntimeException("failed to instantiate BuyerSearchAlgorithm" + ex.getMessage());
                    }


                }
            }

            //if you are here, nothing was found!
            throw new RuntimeException("failed to instantiate an algorithm with name" + rule);


        }


        /**
         * Returns a new random search algorithm
         * @return the new rule to follow
         */
        public static PurchasesPredictor randomSalesPredictor(MersenneTwisterFast randomizer, PurchasesDepartment department)
        {
            Class<? extends PurchasesPredictor > purchasesPredictor = null;
            //now you are going to pick at random, but keep doing it as long as you draw abstract classes or interfaces
            while(purchasesPredictor == null || Modifier.isAbstract(purchasesPredictor.getModifiers()) || purchasesPredictor.isInterface())
            {
                //get a new rule
                purchasesPredictor = rules.get(randomizer.nextInt(rules.size()));
            }

            //now just instantiate it!
            return newPurchasesPredictor(purchasesPredictor,department);


        }

        /**
         * Returns a new specific algorithm for this house population
         *
         * @param rule the simpleName of the class!
         * @return the new rule to follow
         */
        public static <PP extends PurchasesPredictor> PP newPurchasesPredictor(
                @Nonnull Class<PP> rule, PurchasesDepartment department)
        {

            if(!rules.contains(rule) || Modifier.isAbstract(rule.getModifiers()) || rule.isInterface() )
                throw new IllegalArgumentException("The rule given is either abstract or just not recognized");


            try {
                //we don't need a big switch here, since they are all without constructor except one
                if(rule.equals(LearningIncreasePurchasesPredictor.class) || rule.equals(LearningIncreaseWithTimeSeriesPurchasePredictor.class))
                    return rule.getConstructor(Market.class, MacroII.class).
                            newInstance(department.getMarket(), department.getModel());
                if(rule.equals(SamplingLearningIncreasePurchasePredictor.class))
                    return rule.getConstructor().newInstance(department.getModel());
                if(rule.equals(RecursivePurchasesPredictor.class) || rule.equals(OpenLoopRecursivePurchasesPredictor.class))
                    return rule.getConstructor(MacroII.class,PurchasesDepartment.class).newInstance(department.getModel(),department);
                if(rule.equals(LinearExtrapolatorPurchasePredictor.class) || rule.equals(AroundShockLinearRegressionPurchasePredictor.class))
                    return rule.getConstructor(PurchasesDepartment.class).newInstance(department);
                else
                    return rule.newInstance();


            } catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException |
                    NoSuchMethodException |InvocationTargetException  ex  ) {
                throw new RuntimeException("failed to instantiate " + ex.getMessage());
            }

        }

        /**
         * Private constructor to avoid anybody from instatiating this
         */
        private Factory() {
        }

    }
}
