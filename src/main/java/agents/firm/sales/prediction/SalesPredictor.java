/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.prediction;

import agents.firm.sales.SalesDepartment;
import ec.util.MersenneTwisterFast;
import financial.market.Market;
import model.MacroII;
import model.utilities.Deactivatable;
import model.utilities.NonDrawable;
import model.utilities.logs.LogNode;

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
public interface SalesPredictor extends LogNode, Deactivatable {





    /**
     * This is called by the firm when it wants to predict the price they can sell to if they increase production
     *
     * @param dept the sales department that has to answer this question
     * @param expectedProductionCost the HQ estimate of costs in producing whatever it wants to sell. It isn't necesarilly used.
     * @param increaseStep by how much the daily production will increase (has to be a positive number)
     * @return the best offer available/predicted or -1 if there are no quotes/good predictions
     */
    public float predictSalePriceAfterIncreasingProduction(SalesDepartment dept, int expectedProductionCost, int increaseStep);


    /**
     * This is called by the firm when it wants to predict the price they can sell to if they increase production
     *
     * @param dept the sales department that has to answer this question
     * @param expectedProductionCost the HQ estimate of costs in producing whatever it wants to sell. It isn't necesarilly used.
     * @param decreaseStep by how much the daily production will decrease (has to be a positive number)
     * @return the best offer available/predicted or -1 if there are no quotes/good predictions
     */
    public float predictSalePriceAfterDecreasingProduction(SalesDepartment dept, int expectedProductionCost, int decreaseStep);


    /**
     * This is a little bit weird to predict, but basically you want to know what will be "tomorrow" price if you don't change production.
     * Most predictors simply return today closing price, because maybe this will be useful in some cases. It's used by Marginal Maximizer Statics
     * @param dept the sales department
     * @return predicted price
     */
    public float predictSalePriceWhenNotChangingProduction(SalesDepartment dept);


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
            rules = new ArrayList<>(); //read all the rules
            rules.add(AroundShockLinearRegressionSalesPredictor.class);
            rules.add(ErrorCorrectingSalesPredictor.class);
            rules.add(FixedDecreaseSalesPredictor.class);
            rules.add(FixedFormulaFakePredictor.class);
            rules.add(LearningDecreaseSalesPredictor.class);
            rules.add(LearningDecreaseWithTimeSeriesSalesPredictor .class);
            rules.add(LearningFixedElasticitySalesPredictor  .class);
            rules.add(LinearExtrapolationPredictor.class);
            rules.add(LookupSalesPredictor.class);
            rules.add(MarketSalesPredictor.class);
            rules.add(MemorySalesPredictor.class);
            rules.add(OpenLoopRecursiveSalesPredictor .class);
            rules.add(PricingSalesPredictor.class);
            rules.add(RecursiveSalePredictor.class);
            rules.add(RegressionSalePredictor.class);
            rules.add(RegressionWeightedSalePredictor.class);
            rules.add(SamplingLearningDecreaseSalesPredictor.class);
            rules.add(SISOGuessingSalesPredictor.class);
            rules.add(SurveySalesPredictor.class);
            assert rules.size() > 0; // there should be at least one!!
        }

        /**
         * Returns a new price predictor algorithm for this sales departmetn
         *
         * @param rule the simpleName of the class!
         * @return the new rule to follow
         */
        public static SalesPredictor newSalesPredictor( String rule) {
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
            while(salesPredictor == null || Modifier.isAbstract(salesPredictor.getModifiers()) || salesPredictor.isInterface()|| salesPredictor.isAnnotationPresent(NonDrawable.class))
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
                 Class<SP> rule, SalesDepartment department)
        {

            if(Modifier.isAbstract(rule.getModifiers()) || rule.isInterface() )
                throw new IllegalArgumentException("The rule  -->"+  rule.getName() +"given is either abstract or just not recognized");


            try {
                //we don't need a big switch here, they are mostly without constructor
                if(rule.equals(LinearExtrapolationPredictor.class) || rule.equals(AroundShockLinearRegressionSalesPredictor.class))
                    return rule.getConstructor(SalesDepartment.class).newInstance(department);
                if(rule.equals(SamplingLearningDecreaseSalesPredictor.class))
                    return rule.getConstructor().newInstance();
                if(rule.equals(RecursiveSalePredictor.class) || rule.equals(OpenLoopRecursiveSalesPredictor.class) ||
                        rule.equals(SISOGuessingSalesPredictor.class) || rule.equals(ErrorCorrectingSalesPredictor.class))
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
