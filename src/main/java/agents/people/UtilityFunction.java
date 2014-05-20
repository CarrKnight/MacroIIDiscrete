/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.people;

import goods.GoodType;

/**
 * Something I might or might not use, but useful to have for completeness.
 * Basically a function that ranks the enjoyment of the person of its current endowment.
 * Created by carrknight on 5/19/14.
 */
public interface UtilityFunction {

    /**
     * return a floating point representing more or less an ordinal ranking on the material enjoyment of this
     * agent given his consumption. Marvel how cheaply we judge ourselves and our lives
     * @param p the person whose endowment we rank
     * @return a ranking in utils.
     */
    public float computesUtility(Person p);

    /**
     * A simple comparison, how many of typeGained do you need to compensate the loss of one unite of typeLost.
     * This way we can do easy trading/pricing
     * @param typeLost the good we are hypothetically giving one unit away of
     * @param typeGained the good we are hypothetically receiveing
     * @param p the person hypothetically gainin or losing.
     * @return how much of the typeGained we would have to receive in order for our utility to be just the same after
     * losing one unit of typelost
     */
    public float howMuchOfThisGoodDoYouNeedToCompensateTheLossOfOneUnitOfAnother(GoodType typeLost,
                                                                                 GoodType typeGained, Person p);

    /**
     * A simple comparison, how many of typeYouGainOneUnitOf do you need to compensate the loss of one unite of typeOfGoodToGiveAway.
     * This way we can do easy trading/pricing
     * @param typeYouGainOneUnitOf the good we are hypothetically receiveing
     * @param typeOfGoodToGiveAway the good we are hypothetically giving one unit away of
     * @param p the person hypothetically gainin or losing.
     * @return how much of the typeYouGainOneUnitOf we would have to receive in order for our utility to be just the same after
     * losing one unit of typelost
     */
    public float howMuchOfThisGoodWouldYouGiveAwayInExchangeForOneUnitOfAnother(GoodType typeYouGainOneUnitOf,
                                                                                 GoodType typeOfGoodToGiveAway, Person p);


    public static class Factory
    {

        private static NoUtilityFunction alwaysZero = null;
        private static AllTheSameUtilityFunction alwaysOne = null;

        public static UtilityFunction build(Class<? extends UtilityFunction> type)
        {
            if(type.equals(NoUtilityFunction.class))
            {
                if(alwaysZero == null)
                    alwaysZero = new NoUtilityFunction();
                return alwaysZero;
            }
            if(type.equals(AllTheSameUtilityFunction.class))
            {
                if(alwaysOne == null)
                    alwaysOne = new AllTheSameUtilityFunction();
                return alwaysOne;
            }
            if(type.equals(CobbDouglas2GoodsUtility.class))
                return new CobbDouglas2GoodsUtility();
            else
                throw new RuntimeException("Unknown utility function!");

        }

    }

}
