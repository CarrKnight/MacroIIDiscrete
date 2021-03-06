/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.people;

import model.MacroII;

/**
 * The default consumption strategy, simply calls consumeAll() on the person
 * Created by carrknight on 5/16/14.
 */
public class ConsumeAllStrategy implements ConsumptionStrategy {

    @Override
    public void consume(Person person, MacroII model) {
        person.consumeAll();
    }


}
