/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.people;

import model.MacroII;

/**
 * easiest strategy: be lazy and produce nothing! And this is default. This doesn't reflect any comment of the author
 * on the sociological nature of poverty.
 * Created by carrknight on 5/16/14.
 */
public class NoPersonalProductionStrategy implements PersonalProductionStrategy {

    /**
     * Produce nothing. Waste everyone's time.
     * @param person the person running this strategy
     * @param model a link to the model
     */
    @Override
    public void produce(Person person, MacroII model) {

    }
}
