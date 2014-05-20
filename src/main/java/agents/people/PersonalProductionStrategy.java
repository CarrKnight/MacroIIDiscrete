/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.people;

import model.MacroII;

/**
 * A simple strategy describing what, if anything, the person produces during its production phase.
 * This is called shortly after the consume() of consumption strategy.
 * Created by carrknight on 5/16/14.
 */
public interface PersonalProductionStrategy {

    /**
     * after consumption, during production phase, the person might produce something for himself.
     * This is separate and independent from firm production
     * @param person the person running this strategy
     * @param model a link to the model
     */
    public void produce(Person person, MacroII model);



    public static class Factory
    {
        private static NoPersonalProductionStrategy noProductionInstance;


        public static PersonalProductionStrategy build(Class<? extends PersonalProductionStrategy> type)
        {
            if(type.equals(NoPersonalProductionStrategy.class))
            {
                if(noProductionInstance == null)
                    noProductionInstance = new NoPersonalProductionStrategy();
                return noProductionInstance;
            }
            if(type.equals(ConstantPersonalProductionStrategy.class))
                return new ConstantPersonalProductionStrategy();
            else
                throw new RuntimeException("unrecognized personal production strategy");
        }


    }
}
