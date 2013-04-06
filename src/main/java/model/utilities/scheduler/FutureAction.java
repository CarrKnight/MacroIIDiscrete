/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.scheduler;

import com.google.common.base.Preconditions;
import model.utilities.ActionOrder;
import sim.engine.Steppable;

/**
 * A simple struct storing the phase, the kind of action and how many days from now it is supposed to be
 */
public class FutureAction{

    final private ActionOrder phase;

    final private Steppable action;

    private int daysAway;

    public FutureAction(ActionOrder phase, Steppable action, int daysAway) {
        Preconditions.checkArgument(daysAway > 0); //delay has to be positive
        this.phase = phase;
        this.action = action;
        this.daysAway = daysAway;
    }


    /**
     * Decrease days away by 1
     * @return true if days away are 0
     */
    public boolean spendOneDay()
    {
        daysAway--;
        Preconditions.checkState(daysAway >=0);
        return daysAway == 0;

    }

    public ActionOrder getPhase() {
        return phase;
    }

    public Steppable getAction() {
        return action;
    }
}
