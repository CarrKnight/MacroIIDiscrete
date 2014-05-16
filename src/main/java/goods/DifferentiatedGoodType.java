/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package goods;

import agents.firm.Firm;
import com.google.common.base.Preconditions;

/**
 * a good that is always differentiated
 * Created by carrknight on 5/12/14.
 */
public class DifferentiatedGoodType extends GoodType {
    /**
     * a "test"/madeup goodtype, representing generic capital. Imagine micro's "k"
     */
    public static final DifferentiatedGoodType CAPITAL = new DifferentiatedGoodType("testCapital","Test Capital",true,false);

    public DifferentiatedGoodType(String code, String name, boolean isMachinery, boolean isLabor) {
        super(code, name, isMachinery, isLabor);
    }

    public DifferentiatedGoodType(String code, String name) {
        super(code, name);
    }


    @Override
    public boolean isDifferentiated() {
        return true;
    }

    @Override
    public Good[] produceAndDeliver(int amount, Firm receiver, int unitCost) {
        Preconditions.checkArgument(amount > 0);
        Good[] toReturn = new Good[amount];
        for(int i=0; i<amount; i++) {
            toReturn[i] = Good.getInstanceOfDifferentiatedGood(this, receiver, unitCost);
            receiver.receive(toReturn[i], receiver); //supply it
            receiver.reactToPlantProduction(toReturn[i]); //react to production
        }

        return toReturn;
    }
}
