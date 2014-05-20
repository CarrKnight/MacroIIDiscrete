/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.people;

import model.MacroII;

/**
 * Called at PRODUCTION time before the Person goes on producing.
 *
 * Created by carrknight on 5/16/14.
 */
public interface ConsumptionStrategy {

    /**
     * called at PRODUCTION, it is a simple way for Person to consume what needs to be consumed.
     * @param person the person making the consumption
     * @param model the model, if needed for consumption
     */
    public void consume(Person person, MacroII model);


    public static class Factory{

        /**
         * while consume all is not a singleton, we let the factory only instantiate always the same since
         * it is stateless.
         */
        private static ConsumeAllStrategy consumeAllInstance;

        //as above
        private static NoConsumptionStrategy consumeNothingInstance;

        public static ConsumptionStrategy build(Class<? extends ConsumptionStrategy> strategyType)
        {
            if(strategyType.equals(ConsumeAllStrategy.class))
            {
                if(consumeAllInstance == null) consumeAllInstance = new ConsumeAllStrategy();
                return consumeAllInstance;
            }
            if(strategyType.equals(NoConsumptionStrategy.class)) {
                if (consumeNothingInstance == null) consumeNothingInstance = new NoConsumptionStrategy();
                return consumeNothingInstance;
            }
            else
            {
                throw new RuntimeException("Unknown consumption strategy!");
            }

        }


    }


}
