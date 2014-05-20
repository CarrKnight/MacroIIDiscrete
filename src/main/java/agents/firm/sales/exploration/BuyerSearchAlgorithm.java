/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
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
 * <p/>  This is an interface used by the firm/search department to explore registry and find buyers
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
public interface BuyerSearchAlgorithm extends Deactivatable {


    /**
     * look into the buyer registry and return what the search algorithm deems the best
     * @return the best buyer available or null if there were none
     */
    public EconomicAgent getBestInSampleBuyer();


    /**
     * Go to the market and return a sample of buyers
     * @return an array containing a sample of buyers
     */
    public EconomicAgent[] sampleBuyers();


    /**
     * Tell the search algorithm that the last match was a good one
     * @param buyer match made
     * @param reason purchase result of the transaction
     */
    public void reactToSuccess(EconomicAgent buyer, PurchaseResult reason);


    /**
     * Tell the search algorithm that the last match was a bad one
     * @param buyer match made
     * @param reason purchase result of the transaction
     */
    public void reactToFailure(EconomicAgent buyer, PurchaseResult reason);


    /**
     * This is the static generator to create random or non-random buyerSearchAlgorithm.
     * It expects all subclasses to have a constructor with as two arguments: market and firm
     */
    public static class Factory {

        /**
         * This holds a list of all the subclasses of BuyerSearchAlgorithm. It's an arraylist to make randomization easier, but it's generated as a set first so there won't be duplicates
         */
        final private static ArrayList<Class<? extends BuyerSearchAlgorithm>> rules;

        //static clause to fill the set names
        static {
            rules = new ArrayList<>(); //read all the rules
            rules.add(SimpleBuyerSearch.class);
            rules.add(SimpleFavoriteBuyerSearch.class);
            rules.removeIf(aClass -> aClass.isAnnotationPresent(NonDrawable.class));

            assert rules.size() > 0; // there should be at least one!!
        }

        /**
         * Returns a new specific search algorithm for this firm/department
         *
         * @param rule the simpleName of the class!
         * @param market the market the algorithm will search into
         * @param firm the firm that is doing the search (so we avoid sampling ourselves)
         * @return the new rule to follow
         */
        public static BuyerSearchAlgorithm newBuyerSearchAlgorithm( String rule, Market market,  EconomicAgent firm) {
            for (Class<? extends BuyerSearchAlgorithm> c : rules) {
                if (c.getSimpleName().equals(rule)) //if the name matches
                {
                    try {

                        return c.getConstructor(Market.class,EconomicAgent.class).newInstance(market,firm); //return it!
                    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException |
                            IllegalArgumentException | InvocationTargetException ex) {
                        throw new RuntimeException("failed to instantiate BuyerSearchAlgorithm" + ex.getMessage());
                    }


                }
            }

            //if you are here, nothing was found!
            throw new RuntimeException("failed to instantiate an algorithm with name" + rule);


        }


        /**
         * Returns a new random search algorithm for this firm/department
         * @param market the market the algorithm will search into
         * @param firm the firm that is doing the search (so we avoid sampling ourselves)
         * @return the new rule to follow
         */
        public static  BuyerSearchAlgorithm randomBuyerSearchAlgorithm( Market market,  EconomicAgent firm)
        {
            Class<? extends BuyerSearchAlgorithm > buyerSearchAlgorithm = null;
            MersenneTwisterFast randomizer = firm.getRandom(); //get the randomizer
            //now you are going to pick at random, but keep doing it as long as you draw abstract classes or interfaces
            while(buyerSearchAlgorithm == null || Modifier.isAbstract(buyerSearchAlgorithm.getModifiers()) || buyerSearchAlgorithm.isInterface())
            {
                //get a new rule
                buyerSearchAlgorithm = rules.get(randomizer.nextInt(rules.size()));
            }

            //now just instantiate it!
            return newBuyerSearchAlgorithm(buyerSearchAlgorithm,market,firm);


        }

        /**
         * Returns a new specific search algorithm for this firm/department
         *
         * @param rule the simpleName of the class!
         * @param market the market the algorithm will search into
         * @param firm the firm that is doing the search (so we avoid sampling ourselves)
         * @return the new rule to follow
         */
        public static <BS extends  BuyerSearchAlgorithm> BS newBuyerSearchAlgorithm(
                 Class<BS> rule, Market market,  EconomicAgent firm )
        {

            if(!rules.contains(rule) || Modifier.isAbstract(rule.getModifiers()) || rule.isInterface() )
                throw new IllegalArgumentException("The rule given is either abstract or just not recognized");


            try {
                return rule.getConstructor(Market.class,EconomicAgent.class).newInstance(market,firm);
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException |
                    IllegalArgumentException | InvocationTargetException ex) {
                throw new RuntimeException("failed to instantiate BuyerSearchAlgorithm " + ex.getMessage());
            }



        }

        /**
         * Private constructor to avoid anybody from instatiating this
         */
        private Factory() {
        }

    }

    /**
     * set the market where to search
     */
    public void setMarket(Market market);







}
