/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package financial;

import agents.EconomicAgent;

/**
 * Created with IntelliJ IDEA.
 * User: Ernesto
 * Date: 7/5/12
 * Time: 10:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class Bankruptcy extends RuntimeException {

    private final EconomicAgent cause;

    public Bankruptcy(EconomicAgent cause) {

        this.cause = cause;
    }

    @Override
    public String toString() {
        return "Bankruptcy{" + "cause=" + cause + '}';
    }
}
