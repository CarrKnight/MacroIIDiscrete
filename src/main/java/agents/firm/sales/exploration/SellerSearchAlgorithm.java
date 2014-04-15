/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.exploration;

import agents.EconomicAgent;
import ec.util.MersenneTwisterFast;
import financial.market.Market;
import financial.utilities.PurchaseResult;
import model.utilities.Deactivatable;
import model.utilities.NonDrawable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

/**
 * <h4>Description</h4>
 * <p/>  This is an interface used by the agent/search department to explore registry and find sellers
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-07-20
 * @see
 */
public interface SellerSearchAlgorithm extends Deactivatable {



    /**
     * look into the seller registry and return what the search algorithm deems the best
     * @return the best seller available or null if you can't find any
     */

    public EconomicAgent getBestInSampleSeller();


    /**
     * Go to the market and return a sample of sellers
     * @return an array containing a sample of sellers
     */
    public EconomicAgent[] sampleSellers();


    /**
     * Tell the search algorithm that the last match was a good one
     * @param seller match made
     * @param reason purchase result of the transaction
     */
    public void reactToSuccess(EconomicAgent seller, PurchaseResult reason);


    /**
     * Tell the search algorithm that the last match was a bad one
     * @param seller match made
     * @param reason purchase result of the transaction
     */
    public void reactToFailure(EconomicAgent seller, PurchaseResult reason);

    /**
     * This is the static generator to create random or non-random inventory control. It expects all subclasses to have a constructor with as a single argument
     * a purchasesDepartment.
     */
    /**
     * This is the static generator to create random or non-random buyerSearchAlgorithm. 
     * It expects all subclasses to have a constructor with as two arguments: market and agent
     */
    public static class Factory {

        /**
         * This holds a list of all the subclasses of SellerSearchAlgorithm. It's an arraylist to make randomization easier, but it's generated as a set first so there won't be duplicates
         */
        final private static ArrayList<Class<? extends SellerSearchAlgorithm>> rules;

        //static clause to fill the set names
        static {
            rules = new ArrayList<>(); //read all the rules
            assert rules.size() > 0; // there should be at least one!!

            rules.add(SimpleSellerSearch.class);
            rules.add(SimpleFavoriteSellerSearch.class);
            rules.removeIf(aClass -> aClass.isAnnotationPresent(NonDrawable.class));

        }

        /**
         * Returns a new specific market algorithm for this house population
         *
         * @param rule the simpleName of the class!
         * @param market the market the algorithm will search into
         * @param agent the agent that is doing the search (so we avoid sampling ourselves)
         * @return the new rule to follow
         */
        public static SellerSearchAlgorithm newSellerSearchAlgorithm( String rule, Market market,  EconomicAgent agent) {
            for (Class<? extends SellerSearchAlgorithm> c : rules) {
                if (c.getSimpleName().equals(rule)) //if the name matches
                {
                    try {

                        return c.getConstructor(Market.class,EconomicAgent.class).newInstance(market,agent); //return it!
                    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException |
                            IllegalArgumentException | InvocationTargetException ex) {
                        throw new RuntimeException("failed to instantiate SellerSearchAlgorithm" + ex.getMessage());
                    }


                }
            }

            //if you are here, nothing was found!
            throw new RuntimeException("failed to instantiate an algorithm with name" + rule);


        }


        /**
         * Returns a new random search algorithm
         * @param market the market the algorithm will search into
         * @param agent the agent that is doing the search (so we avoid sampling ourselves)
         * @return the new rule to follow
         */
        public static SellerSearchAlgorithm randomSellerSearchAlgorithm( Market market,  EconomicAgent agent)
        {
            Class<? extends SellerSearchAlgorithm > sellerSearchAlgorithm = null;
            MersenneTwisterFast randomizer = agent.getRandom(); //get the randomizer
            //now you are going to pick at random, but keep doing it as long as you draw abstract classes or interfaces
            while(sellerSearchAlgorithm == null || Modifier.isAbstract(sellerSearchAlgorithm.getModifiers()) || sellerSearchAlgorithm.isInterface())
            {
                //get a new rule
                sellerSearchAlgorithm = rules.get(randomizer.nextInt(rules.size()));
            }

            //now just instantiate it!
            return newSellerSearchAlgorithm(sellerSearchAlgorithm, market, agent);


        }

        /**
         * Returns a new specific algorithm for this house population
         *
         * @param rule the simpleName of the class!
         * @param market the market the algorithm will search into
         * @param agent the agent that is doing the search (so we avoid sampling ourselves)
         * @return the new rule to follow
         */
        public static <SS extends SellerSearchAlgorithm> SS newSellerSearchAlgorithm(  Class<SS> rule, Market market,  EconomicAgent agent )
        {

            if(!rules.contains(rule) || Modifier.isAbstract(rule.getModifiers()) || rule.isInterface() )
                throw new IllegalArgumentException("The rule given is either abstract or just not recognized");


            try {
                return rule.getConstructor(Market.class,EconomicAgent.class).newInstance(market,agent);
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException |
                    IllegalArgumentException | InvocationTargetException ex) {
                throw new RuntimeException("failed to instantiate SellerSearchAlgorithm" + ex.getMessage());
            }



        }

        /**
         * Private constructor to avoid anybody from instatiating this
         */
        private Factory() {
        }

    }







}
