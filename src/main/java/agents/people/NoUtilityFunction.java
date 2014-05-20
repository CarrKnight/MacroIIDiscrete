/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.people;

import goods.GoodType;

/**
 * Utility is always 0, no matter what
 * Created by carrknight on 5/19/14.
 */
public class NoUtilityFunction implements UtilityFunction {
    @Override
    public float computesUtility(Person p) {
        return 0;
    }

    @Override
    public float howMuchOfThisGoodDoYouNeedToCompensateTheLossOfOneUnitOfAnother(GoodType typeLost, GoodType typeGained, Person p) {
        return 0;
    }

    @Override
    public float howMuchOfThisGoodWouldYouGiveAwayInExchangeForOneUnitOfAnother(GoodType typeYouGainOneUnitOf, GoodType typeOfGoodToGiveAway, Person p) {
        return 0;
    }
}
