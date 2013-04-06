/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents;

import sim.engine.Steppable;

/**
 * This is the interface of every agent. It's basically steppable + start of the day + end of the day <br>
 * User: Ernesto
 * Date: 4/12/12
 * Time: 7:39 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Agent extends Steppable {


    /**
     * Like weekStart, weekEnd should be mostly about accounting.
     * @param time
     */
    public void weekEnd(double time);


}
