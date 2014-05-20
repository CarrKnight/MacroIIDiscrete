/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.people;

import com.google.common.base.Preconditions;
import goods.GoodType;
import goods.UndifferentiatedGoodType;

/**
 * Created by carrknight on 5/19/14.
 */
public class CobbDouglas2GoodsUtility implements UtilityFunction {

    /**
     * the exponent of alpha good
     */
    private float alpha = 0.5f;

    /**
     * the first good
     */
    private final GoodType alphaGood;

    /**
     * the second good
     */
    private final GoodType betaGood;

    public CobbDouglas2GoodsUtility()
    {
        alpha = 0.5f;
        alphaGood = UndifferentiatedGoodType.GENERIC;
        betaGood = UndifferentiatedGoodType.MONEY;
    }

    public CobbDouglas2GoodsUtility(GoodType alphaGood, GoodType betaGood,
                                    float alpha) {
        Preconditions.checkArgument(alpha > 0);
        Preconditions.checkArgument(alpha < 1);

        this.alphaGood = alphaGood;
        this.betaGood = betaGood;
        this.alpha = alpha;
    }

    @Override
    public float computesUtility(Person p) {
        return (float) (Math.pow(p.hasHowMany(alphaGood)+1,alpha) *
                Math.pow(p.hasHowMany(betaGood)+1,1f-alpha));
    }

    @Override
    public float howMuchOfThisGoodDoYouNeedToCompensateTheLossOfOneUnitOfAnother(GoodType typeLost, GoodType typeGained, Person p) {
        float utility = computesUtility(p);
        float beta = 1- alpha;
        //the minus one cancels the usual plus one in the denominator
        float toSquareRoot = (float) (utility/Math.pow(p.hasHowMany(alphaGood),alpha));
        toSquareRoot = (float) Math.pow(toSquareRoot,1/beta);
        return (float) (toSquareRoot - p.hasHowMany(betaGood)-1);

    }

    @Override
    public float howMuchOfThisGoodWouldYouGiveAwayInExchangeForOneUnitOfAnother(GoodType typeYouGainOneUnitOf, GoodType typeOfGoodToGiveAway, Person p) {
        float utility = computesUtility(p);

        float beta = 1-alpha;
        float toSquareRoot = (float) (utility/Math.pow(p.hasHowMany(alphaGood)+2,alpha)); //the original plus one and then the new one we hypothetically receive
        toSquareRoot = (float) Math.pow(toSquareRoot,1/beta);

        return (float) (-toSquareRoot + p.hasHowMany(betaGood)+1);
    }


    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
        Preconditions.checkArgument(alpha > 0);
        Preconditions.checkArgument(alpha < 1);
    }
}
