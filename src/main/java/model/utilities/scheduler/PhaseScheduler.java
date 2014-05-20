/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.scheduler;

import model.utilities.ActionOrder;
import sim.engine.Steppable;

/**
 * <h4>Description</h4>
 * <p/>
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-02-12
 * @see
 */
public interface PhaseScheduler extends Steppable {

    /**
     * Schedule as soon as this phase occurs (at priority STANDARD)
     * @param phase the phase i want the action to occur in
     * @param action the steppable that should be called
     */
    void scheduleSoon(ActionOrder phase, Steppable action);


    /**
     * Schedule as soon as this phase occurs
     * @param phase the phase i want the action to occur in
     * @param action the steppable that should be called
     * @param priority the action priority
     *
     */
    void scheduleSoon(ActionOrder phase, Steppable action, Priority priority);

    /**
     * Schedule tomorrow assuming the phase passed is EXACTLY the current phase (at priority STANDARD)
     * @param phase the phase i want the action to occur in
     * @param action the steppable that should be called
     */
    void scheduleTomorrow(ActionOrder phase, Steppable action);

    /**
     * Schedule tomorrow assuming the phase passed is EXACTLY the current phase
     * @param phase the phase i want the action to occur in
     * @param action the steppable that should be called
     * @param priority the action priority
     */
    void scheduleTomorrow(ActionOrder phase, Steppable action,Priority priority);

    /**
     * Schedule in as many days as passed (at priority standard)
     * @param phase the phase i want the action to occur in
     * @param action the steppable that should be called
     * @param daysAway how many days into the future should this happen
     */
    void scheduleAnotherDay( ActionOrder phase, Steppable action,
                            int daysAway);

    /**
     * Schedule in as many days as passed (at priority standard)
     * @param phase the phase i want the action to occur in
     * @param action the steppable that should be called
     * @param daysAway how many days into the future should this happen
     * @param priority the action priority
     */
    void scheduleAnotherDay( ActionOrder phase, Steppable action,
                            int daysAway,Priority priority);

    /**
     *
     * @param phase the phase i want the action to occur in
     * @param probability each day we check against this fixed probability to know if we will step on this action today
     * @param action the steppable that should be called
     */
    void scheduleAnotherDayWithFixedProbability( ActionOrder phase,  Steppable action,
                                                float probability);

    /**
     * @param probability each day we check against this fixed probability to know if we will step on this action today
     * @param phase the phase i want the action to occur in
     * @param action the steppable that should be called
     * @param
     */
    void scheduleAnotherDayWithFixedProbability( ActionOrder phase,  Steppable action,
                                                float probability, Priority priority);

    /**
     * deletes everything
     */
    void clear();

    ActionOrder getCurrentPhase();
}
