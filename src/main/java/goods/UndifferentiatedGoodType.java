/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package goods;

import agents.firm.Firm;
import com.google.common.base.Preconditions;

/**
 * Created by carrknight on 5/12/14.
 */
public class UndifferentiatedGoodType extends GoodType {
    /**
     * a "test"/madeup goodtype, representing generic homogeneous goods. Imagine macro's "c" (consumption good)
     */
    public static final UndifferentiatedGoodType GENERIC = new UndifferentiatedGoodType("testGeneric","Test good",false,false);
    /**
     * a "test"/madeup goodtype, representing generic labor. Imagine micro's "L"
     */
    public static final UndifferentiatedGoodType LABOR = new UndifferentiatedGoodType("testLabor","Test Labor",false,true);

    /**
     * a simple generic kind of money, it's the default "money" to use for all economic agents
     */
    public static final UndifferentiatedGoodType MONEY = new UndifferentiatedGoodType("money","money",false,false);

    public UndifferentiatedGoodType(String code, String name, boolean isMachinery, boolean isLabor) {
        super(code, name, isMachinery, isLabor);
    }

    public UndifferentiatedGoodType(String code, String name) {
        super(code, name);
    }

    @Override
    public boolean isDifferentiated() {
        return false;
    }

    @Override
    public Good[] produceAndDeliver(int amount, Firm receiver, int unitCost) {
        Preconditions.checkArgument(amount>0);
        receiver.receiveMany(this,amount); //create
        receiver.reactToPlantProduction(this,amount);
        return null;
    }
}
