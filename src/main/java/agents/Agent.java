/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents;

import model.MacroII;
import model.utilities.Deactivatable;
import sim.engine.Steppable;

/**
 * This is the interface of every agent. It's basically steppable + start of the day + end of the day <br>
 * User: Ernesto
 * Date: 4/12/12
 * Time: 7:39 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Agent extends Steppable, Deactivatable{

    /**
     * called when the agent has to start acting!
     */
    public void start(MacroII state);

    /**
     * Like weekStart, weekEnd should be mostly about accounting.
     * @param time
     */
    public void weekEnd(double time);


}
