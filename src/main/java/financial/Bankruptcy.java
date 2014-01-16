/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
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
        final StringBuilder sb = new StringBuilder("Bankruptcy{");
        sb.append("cause=").append(cause);
        sb.append('}');
        return sb.toString();
    }
}
