/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.people;

import goods.GoodType;

/**
 * Utility that just counts the total number of goods held. Doesn't care about type or quantity
 * Created by carrknight on 5/19/14.
 */
public class AllTheSameUtilityFunction implements UtilityFunction {
    @Override
    public float computesUtility(Person p) {
        int sum = 0;
        for(GoodType type : p.goodTypesEncountered())
            sum += p.hasHowMany(type);
        return sum;
    }

    @Override
    public float howMuchOfThisGoodDoYouNeedToCompensateTheLossOfOneUnitOfAnother(GoodType typeLost, GoodType typeGained, Person p) {
        return 1;
    }

    @Override
    public float howMuchOfThisGoodWouldYouGiveAwayInExchangeForOneUnitOfAnother(GoodType typeYouGainOneUnitOf, GoodType typeOfGoodToGiveAway, Person p) {
        return 1;
    }
}
