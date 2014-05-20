/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
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

    final private Priority priority;

    private int daysAway;

    public FutureAction(ActionOrder phase, Steppable action, Priority priority, int daysAway) {
        this.phase = phase;
        this.action = action;
        this.priority = priority;
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

    public Priority getPriority() {
        return priority;
    }
}
