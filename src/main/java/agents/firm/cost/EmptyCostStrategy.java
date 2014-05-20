/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.cost;

import goods.GoodType;

/**
 * This is the "empty" class
 * User: carrknight
 * Date: 7/14/12
 * Time: 3:49 PM
 */
public class EmptyCostStrategy implements PlantCostStrategy{

    @Override
    public int unitOutputCost(GoodType t, int totalCostOfInputs) {
        return 0;
    }


    /**
     * The unit costs of goods if we change the worker number is going to be how much?
     *
     * @param t                 the type of good we want to price
     * @param totalCostOfInputs the value of input CONSUMED to perform the production RUN
     * @param workers           the new number of workers
     * @param totalWages        the new wages being paid if we have these workers.
     * @return the cost we assign to this good.
     */
    @Override
    public int hypotheticalUnitOutputCost(GoodType t, int totalCostOfInputs, int workers, int totalWages) {
        return  0;
    }

    /**
     * The fixed costs associated with having the plant. These could be the ammortized costs of building the plant, wages if considered quasi-fixed and so on.
     *
     * @return the costs of running the plant
     */
    @Override
    public int weeklyFixedCosts() {
        return 0;
    }

    /**
     * Turning off the strategy
     */
    @Override
    public void turnOff() {

    }
}
