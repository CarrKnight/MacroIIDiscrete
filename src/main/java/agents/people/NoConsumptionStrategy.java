/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.people;

import model.MacroII;

/**
 * As the name implies, no consumption whatsoever.
 * Created by carrknight on 5/17/14.
 */
public class NoConsumptionStrategy implements ConsumptionStrategy {
    @Override
    public void consume(Person ignored1, MacroII ignored2) {

    }
}
