/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.sales.SalesDepartment;
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
 * <p/> This is the strategy by which the sales department tell the firm/production what they expect to be the sale price
 * of their good.
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-07-26
 * @see
 */
public interface SalesPredictor {





    /**
     * This is called by the firm when it wants to predict the price they can sell to if they increase production
     *
     * @param dept the sales department that has to answer this question
     * @param expectedProductionCost the HQ estimate of costs in producing whatever it wants to sell. It isn't necesarilly used.
     * @param increaseStep by how much the daily production will increase (has to be a positive number)
     * @return the best offer available/predicted or -1 if there are no quotes/good predictions
     */
    public long predictSalePriceAfterIncreasingProduction(SalesDepartment dept, long expectedProductionCost, int increaseStep);


    /**
     * This is called by the firm when it wants to predict the price they can sell to if they increase production
     *
     * @param dept the sales department that has to answer this question
     * @param expectedProductionCost the HQ estimate of costs in producing whatever it wants to sell. It isn't necesarilly used.
     * @param decreaseStep by how much the daily production will decrease (has to be a positive number)
     * @return the best offer available/predicted or -1 if there are no quotes/good predictions
     */
    public long predictSalePriceAfterDecreasingProduction(SalesDepartment dept, long expectedProductionCost, int decreaseStep);


    /**
     * This is a little bit weird to predict, but basically you want to know what will be "tomorrow" price if you don't change production.
     * Most predictors simply return today closing price, because maybe this will be useful in some cases. It's used by Marginal Maximizer Statics
     * @param dept the sales department
     * @return predicted price
     */
    public long predictSalePriceWhenNotChangingPoduction(SalesDepartment dept);

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
        final private static ArrayList<Class<? extends SalesPredictor>> rules;

        //static clause to fill the set names
        static {
            Reflections strategyReader = new Reflections("agents.firm.sales.prediction");
            rules = new ArrayList<>(strategyReader.getSubTypesOf(SalesPredictor.class)); //read all the rules
            assert rules.size() > 0; // there should be at least one!!
        }

        /**
         * Returns a new price predictor algorithm for this sales departmetn
         *
         * @param rule the simpleName of the class!
         * @return the new rule to follow
         */
        public static SalesPredictor newSalesPredictor(@Nonnull String rule) {
            for (Class<? extends SalesPredictor> c : rules) {
                if (c.getSimpleName().equals(rule)) //if the name matches
                {
                    try {

                        return c.newInstance();
                    } catch (SecurityException | InstantiationException | IllegalAccessException |
                            IllegalArgumentException  ex) {
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
        public static SalesPredictor randomSalesPredictor(MersenneTwisterFast randomizer, SalesDepartment department)
        {
            Class<? extends SalesPredictor > salesPredictor = null;
            //now you are going to pick at random, but keep doing it as long as you draw abstract classes or interfaces
            while(salesPredictor == null || Modifier.isAbstract(salesPredictor.getModifiers()) || salesPredictor.isInterface())
            {
                //get a new rule
                salesPredictor = rules.get(randomizer.nextInt(rules.size()));
            }

            //now just instantiate it!
            return newSalesPredictor(salesPredictor,department);


        }

        /**
         * Returns a new specific algorithm for this house population
         *
         * @param rule the simpleName of the class!
         * @return the new rule to follow
         */
        public static <SP extends SalesPredictor> SP newSalesPredictor(
                @Nonnull Class<SP> rule, SalesDepartment department)
        {

            if(!rules.contains(rule) || Modifier.isAbstract(rule.getModifiers()) || rule.isInterface() )
                throw new IllegalArgumentException("The rule given is either abstract or just not recognized");


            try {
                //we don't need a big switch here, they are mostly without constructor

                if(rule.equals(LinearExtrapolationPredictor.class) || rule.equals(AroundShockLinearRegressionSalesPredictor.class))
                    return rule.getConstructor(SalesDepartment.class).newInstance(department);
                if(rule.equals(SamplingLearningDecreaseSalesPredictor.class))
                    return rule.getConstructor(MacroII.class).newInstance(department.getModel());
                if(rule.equals(RecursiveSalePredictor.class))
                    return rule.getConstructor(MacroII.class,SalesDepartment.class).newInstance(department.getModel(),department);
                if(rule.equals(RegressionSalePredictor.class) || rule.equals(RegressionWeightedSalePredictor.class)
                        || rule.equals(LearningDecreaseSalesPredictor.class) || rule.equals(LearningFixedElasticitySalesPredictor.class))
                    return rule.getConstructor(Market.class, MacroII.class).
                            newInstance(department.getMarket(),department.getModel());
                else
                    return rule.newInstance();


            } catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException |
                    NoSuchMethodException | InvocationTargetException ex) {
                throw new RuntimeException("failed to instantiate BuyerSearchAlgorithm " + ex.getMessage());
            }


        }

        /**
         * Private constructor to avoid anybody from instatiating this
         */
        private Factory() {
        }

    }
}
