/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.people;

import com.google.common.base.Preconditions;
import goods.UndifferentiatedGoodType;
import model.MacroII;

/**
 * With this strategy the person will produce only the days when he isn't hired by anyone
 * Created by carrknight on 5/16/14.
 */
public class ConstantPersonalProductionStrategy implements PersonalProductionStrategy {

    private final static int DEFAULT_PRODUCTION_RATE = 1;

    private final static UndifferentiatedGoodType DEFAULT_PRODUCTION_TYPE = UndifferentiatedGoodType.GENERIC;

    private int dailyProductionRate;

    private UndifferentiatedGoodType dailyProductionType;

    public ConstantPersonalProductionStrategy() {
        this(DEFAULT_PRODUCTION_RATE,DEFAULT_PRODUCTION_TYPE);
    }

    public ConstantPersonalProductionStrategy(int dailyProductionRate, UndifferentiatedGoodType dailyProductionType) {
        this.dailyProductionRate = dailyProductionRate;
        this.dailyProductionType = dailyProductionType;
    }

    @Override
    public void produce(Person person, MacroII model) {

       if(dailyProductionRate > 0)
           person.receiveMany(dailyProductionType,dailyProductionRate);

    }


    public int getDailyProductionRate() {
        return dailyProductionRate;
    }

    public void setDailyProductionRate(int dailyProductionRate) {
        Preconditions.checkArgument(dailyProductionRate >=0);
        this.dailyProductionRate = dailyProductionRate;
    }

    public UndifferentiatedGoodType getDailyProductionType() {
        return dailyProductionType;
    }

    public void setDailyProductionType(UndifferentiatedGoodType dailyProductionType) {
        Preconditions.checkArgument(dailyProductionType !=null);
        this.dailyProductionType = dailyProductionType;
    }
}
