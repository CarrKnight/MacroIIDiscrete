package model.utilities.scheduler;

import com.sun.javafx.beans.annotations.NonNull;
import model.utilities.ActionOrder;
import sim.engine.Steppable;

import javax.annotation.Nonnull;

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
     * Schedule as soon as this phase occurs
     * @param phase the phase i want the action to occur in
     * @param action the steppable that should be called
     */
    void scheduleSoon(@NonNull ActionOrder phase, @NonNull Steppable action);

    /**
     * Schedule tomorrow assuming the phase passed is EXACTLY the current phase
     * @param phase the phase i want the action to occur in
     * @param action the steppable that should be called
     */
    void scheduleTomorrow(ActionOrder phase, Steppable action);

    /**
     * Schedule in as many days as passed
     * @param phase the phase i want the action to occur in
     * @param action the steppable that should be called
     * @param daysAway how many days into the future should this happen
     */
    void scheduleAnotherDay(@Nonnull ActionOrder phase, @Nonnull Steppable action,
                            int daysAway);

    /**
     *
     * @param phase the phase i want the action to occur in
     * @param action the steppable that should be called
     */
    void scheduleAnotherDayWithFixedProbability(@Nonnull ActionOrder phase, @Nonnull Steppable action,
                                                float probability);

    /**
     * deletes everything
     */
    void clear();

    ActionOrder getCurrentPhase();
}
