/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.people;

import com.google.common.base.Preconditions;
import goods.GoodType;
import goods.UndifferentiatedGoodType;

/**
 * A simple cobb-douglas utility of two goods only.
 *
 *
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
        this(UndifferentiatedGoodType.GENERIC,UndifferentiatedGoodType.MONEY,0.5f);

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
        float exponentGained;
        float exponentLost;
        if(typeGained.equals(betaGood)){
            exponentGained = 1- alpha;
            exponentLost = alpha;
        }
        else if(typeGained.equals(alphaGood))
        {
            exponentGained = alpha;
            exponentLost = 1-alpha;

        }
        else
        {
            return 0;
        }
        //the minus one cancels the usual plus one in the denominator
        float toSquareRoot = (float) (utility/Math.pow(p.hasHowMany(typeLost),exponentLost));
        toSquareRoot = (float) Math.pow(toSquareRoot,1/exponentGained);
        return toSquareRoot - p.hasHowMany(typeGained)-1;

    }

    // If I gain one unit of a, i have to remove X of b to maintain the utility constant:
    //
    //                 alpha            1 - alpha
    // u  =  (a + 1 + 1)      (b + 1 - x)

    //This means:
    //                                   1
    //                               ---------
    //                 /      u     \1 - alpha
    // x =  b  + 1  -  |------------|
    //                 |       alpha|
     //                \(a + 2)     /


    /**
     * If I gain one unit of a, i have to remove X of b to maintain the utility constant:
     * <pre>                    alpha            1 - alpha
     u  =  (a + 1 + 1)      (b + 1 - x)    </pre>
     *This means:
     * <pre>
                                       1
                                    ---------
                     /      u     \1 - alpha
     x =  b  + 1  -  |------------|
                     |       alpha|
                     \(a + 2)     /

     </pre>
     * @param typeYouGainOneUnitOf the good we are hypothetically receiveing
     * @param typeOfGoodToGiveAway the good we are hypothetically giving one unit away of
     * @param p the person hypothetically gainin or losing.
     * @return
     */
    @Override
    public float howMuchOfThisGoodWouldYouGiveAwayInExchangeForOneUnitOfAnother(GoodType typeYouGainOneUnitOf, GoodType typeOfGoodToGiveAway, Person p) {
        float utility = computesUtility(p);

        float exponentAway;
        float exponentGain;
        if(typeOfGoodToGiveAway.equals(betaGood)){
            exponentAway = 1- alpha;
            exponentGain = alpha;
        }
        else if(typeOfGoodToGiveAway.equals(alphaGood))
        {
            exponentAway = alpha;
            exponentGain = 1-alpha;

        }
        else
        {
            return 0;
        }

        float toSquareRoot = (float) (utility/Math.pow(p.hasHowMany(typeYouGainOneUnitOf)+2,exponentGain)); //the original plus one and then the new one we hypothetically receive
        toSquareRoot = (float) Math.pow(toSquareRoot,1/exponentAway);

        return -toSquareRoot + p.hasHowMany(typeOfGoodToGiveAway)+1;
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
