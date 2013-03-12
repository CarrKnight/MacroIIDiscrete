package agents.firm.production.control.maximizer.algorithms.hillClimbers;

import agents.firm.personell.HumanResources;
import agents.firm.production.control.PlantControl;
import ec.util.MersenneTwisterFast;
import model.utilities.pid.PIDController;

/**
 * <h4>Description</h4>
 * <p/> This is the same as the old hill-climber except that it uses a PID to set the size of the step
 * <p/> Initially I coded it so as to target DELTA_profits = 0 but that's a terrible idea since absolute values means tuning it each
 * time separetely. Rather I am going to target no change in efficiency ratio: MR/MC = 1
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-02-02
 * @see
 */
public class PIDHillClimber extends HillClimberMaximizer
{

    /**
     * The pid controller. It is always used through adjustOnce so it's this climber responsibility to update it regularly
     */
    PIDController pid;

    /**
     * I store here the last profits we have seen where the worker target was different so this climber doesn't get stuck somewhere
     */
    float lastDifferentProfits = 0;

    /**
     * Here I store the last target we set that wasn't the one we currently are using, to always use for comparison if we stay still
     */
    int lastDifferentTarget = 0;

    //last marginal efficency found
    private float marginalEfficency;

    /**
     * Creates the hill-climber and instantiates the PID controller needed by asking the model for the parameters
     * @param hr the human resources in charge of the hiring for the plan
     * @param control the control using this maximizer.
     */
    public PIDHillClimber(HumanResources hr, PlantControl control) {

        this(hr,hr.getFirm().getModel().drawProportionalGain(),  hr.getFirm().getModel().drawIntegrativeGain(),
                hr.getFirm().getModel().drawDerivativeGain());

    }

    /**
     * Creates the hill-climber and instantiates the PID controller needed by asking the model for the parameters
     * @param hr the human resources in charge of the hiring for the plan
     */
    public PIDHillClimber(HumanResources hr,float proportional, float integrative, float derivative) {

        this(hr.getPlant().weeklyFixedCosts(),hr.getPlant().minimumWorkersNeeded(),hr.getPlant().maximumWorkersPossible(),
                hr.getPlant().workerSize(),hr.getRandom(),proportional,integrative,derivative);



    }

    public PIDHillClimber(long weeklyFixedCosts, int minimumWorkers, int maximumWorkers,
                          int currentWorkers, MersenneTwisterFast random,
                          float proportional, float integrative, float derivative) {
        super(weeklyFixedCosts, minimumWorkers, maximumWorkers);
        pid = new PIDController(proportional,integrative,derivative,random);
        pid.setOffset(currentWorkers+ 1);

    }

    /**
     * Asks the subclass what the next worker target will be!
     *
     * @param currentWorkerTarget what is the current worker target
     * @param newProfits          what are the new profits
     * @param newRevenues
     *@param newCosts
     * @param oldRevenues
     * @param oldCosts
     * @param oldWorkerTarget     what was the target last time we changed them
     * @param oldProfits          what were the profits back then   @return the new worker targets. Any negative number means to check again!
     */
    @Override
    public int chooseWorkerTarget(int currentWorkerTarget, float newProfits, float newRevenues, float newCosts,
                                  float oldRevenues, float oldCosts, int oldWorkerTarget, float oldProfits) {


        if(currentWorkerTarget != oldWorkerTarget)
        {
            float differenceInRevenues = newRevenues - oldRevenues;
            float differenceInCosts = newCosts - oldCosts;

            //marginal efficency (bound to 10)
            marginalEfficency = Math.min(10,differenceInRevenues/differenceInCosts);

        }
        else
        {

        }
        pid.adjustOnce(marginalEfficency-1,true);


        return Math.min(Math.round(pid.getCurrentMV()),getMaximumWorkersPossible());





    }
}
