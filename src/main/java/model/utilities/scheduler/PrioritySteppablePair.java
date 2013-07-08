/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.scheduler;

import sim.engine.Steppable;

/**
 * <h4>Description</h4>
 * <p/> This is a very simple object holding a reference to a Steppable and a priority. It's used by the scheduler
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-07-08
 * @see
 */
public class PrioritySteppablePair {

    private final Steppable steppable;

    private final Priority priority;

    public PrioritySteppablePair(Steppable steppable, Priority priority) {
        this.steppable = steppable;
        this.priority = priority;
    }


    public Steppable getSteppable() {
        return steppable;
    }

    public Priority getPriority() {
        return priority;
    }
}
