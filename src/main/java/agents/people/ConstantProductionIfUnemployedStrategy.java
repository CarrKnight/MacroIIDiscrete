/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.people;

import goods.UndifferentiatedGoodType;
import model.MacroII;

/**
 * Like Constant Personal Production (which is in fact a delegate) except that it only produce if the worker has no employer
 * Created by carrknight on 5/17/14.
 */
public class ConstantProductionIfUnemployedStrategy implements PersonalProductionStrategy {

    private final ConstantPersonalProductionStrategy delegate;


    public ConstantProductionIfUnemployedStrategy() {
        delegate = new ConstantPersonalProductionStrategy();
    }

    public ConstantProductionIfUnemployedStrategy(int productionRate, UndifferentiatedGoodType type)
    {
        delegate = new ConstantPersonalProductionStrategy(productionRate,type);
    }

    public ConstantProductionIfUnemployedStrategy(ConstantPersonalProductionStrategy delegate) {
        this.delegate = delegate;
    }

    public int getDailyProductionRate() {
        return delegate.getDailyProductionRate();
    }

    public void setDailyProductionRate(int dailyProductionRate) {
        delegate.setDailyProductionRate(dailyProductionRate);
    }

    public void setDailyProductionType(UndifferentiatedGoodType dailyProductionType) {
        delegate.setDailyProductionType(dailyProductionType);
    }

    public UndifferentiatedGoodType getDailyProductionType() {
        return delegate.getDailyProductionType();
    }

    @Override
    public void produce(Person person, MacroII model) {
        if(person.getEmployer()==null)
            delegate.produce(person, model);
    }
}
