package agents.firm.production.control.maximizer;

import agents.firm.personell.HumanResources;
import agents.firm.production.Plant;
import agents.firm.production.control.PlantControl;
import agents.firm.production.control.maximizer.algorithms.WorkerMaximizationAlgorithm;
import agents.firm.production.technology.Machinery;

/**
 * <h4>Description</h4>
 * <p/> This maximizer is extremely simple: it sets the target to the maximum number of workers and NEVER changes it (except when machinery is changed)
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-09-24
 * @see
 */
public class FullCapacityMaximizer<ALG extends WorkerMaximizationAlgorithm> implements WorkforceMaximizer<ALG> {

    /**
     * The human resources the control controls
     */
    private HumanResources hr;

    /**
     * the plant control we are part of
     */
    private PlantControl control;

    /**
     * Create a new full capacity
     * @param hr
     * @param control
     * @param ignored not used.
     */
    public  FullCapacityMaximizer(HumanResources hr, PlantControl control,
                                                                           Class<ALG> ignored) {
        this.hr = hr;
        this.control = control;
    }



    /**
     * Method to start the workforce maximizer
     */
    @Override
    public void start() {
        //set target to max
        control.setTarget(control.getHr().getPlant().maximumWorkersPossible());

    }

    /**
     * Method to switch the strategy off. Irreversible
     */
    @Override
    public void turnOff() {
        //nothing really happens
    }

    /**
     * This is called whenever a plant has changed the number of workers
     *
     * @param p          the plant that made the change
     * @param workerSize the new number of workers
     */
    @Override
    public void changeInWorkforceEvent(Plant p, int workerSize) {
        assert control.getHr().getPlant() == p;

        //don't care
    }

    /**
     * This is called whenever a plant has changed the wage it pays to workers
     *
     * @param wage       the new wage
     * @param p          the plant that made the change
     * @param workerSize the new number of workers
     */
    @Override
    public void changeInWageEvent(Plant p, int workerSize, long wage) {
        assert control.getHr().getPlant() == p;

        //don't care
    }

    /**
     * This is called whenever a plant has been shut down or just went obsolete
     *
     * @param p the plant that made the change
     */
    @Override
    public void plantShutdownEvent(Plant p) {
        assert control.getHr().getPlant() == p;
        //don't care
    }

    /**
     * This is called by the plant whenever the machinery used has been changed
     *
     * @param p         The plant p
     * @param machinery the machinery used.
     */
    @Override
    public void changeInMachineryEvent(Plant p, Machinery machinery) {
        //set target to max (as the max may have shifted during takeoff)
        control.setTarget(control.getHr().getPlant().maximumWorkersPossible());

    }
}
