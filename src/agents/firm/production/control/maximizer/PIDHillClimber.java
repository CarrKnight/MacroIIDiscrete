package agents.firm.production.control.maximizer;

import agents.firm.personell.HumanResources;
import agents.firm.production.control.PlantControl;
import model.MacroII;
import model.utilities.pid.PIDController;

/**
 * <h4>Description</h4>
 * <p/> This is the same as the old hill-climber except that it uses a PID to set the size of the step
 * <p/>
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

    /**
     * Creates the hill-climber and instantiates the PID controller needed by asking the model for the parameters
     * @param hr the human resources in charge of the hiring for the plan
     * @param control the control using this maximizer.
     */
    public PIDHillClimber(HumanResources hr, PlantControl control) {

        super(hr, control);
        MacroII model = hr.getFirm().getModel();
        //the pid controller
        pid = new PIDController(model.drawProportionalGain(),model.drawIntegrativeGain(),
                model.drawDerivativeGain(),model.getRandom());
        pid.setOffset(hr.getPlant().workerSize());

    }


    /**
     * Asks the subclass what the next worker target will be!
     * @param currentWorkerTarget what is the current worker target
     * @param newProfits          what are the new profits
     * @param oldWorkerTarget     what was the target last time we changed them
     * @param oldProfits          what were the profits back then
     * @return the new worker targets. Any negative number means to check again!
     */
    @Override
    public int chooseWorkerTarget(int currentWorkerTarget, float newProfits, int oldWorkerTarget, float oldProfits) {


        float differenceInProfits;
        if(currentWorkerTarget != oldWorkerTarget)
        {
            differenceInProfits = newProfits - oldProfits;
            //turn it upside down if we were going backwards (decreasing workers)
            differenceInProfits = currentWorkerTarget < oldWorkerTarget ? -differenceInProfits : differenceInProfits;
            //record them
            lastDifferentProfits = oldProfits; lastDifferentTarget = oldWorkerTarget;

        }
        else
        {
            differenceInProfits = newProfits - lastDifferentProfits;
            differenceInProfits = currentWorkerTarget < lastDifferentTarget ? -differenceInProfits : differenceInProfits;

        }
        pid.adjustOnce(differenceInProfits,true);


        return Math.min(Math.round(pid.getCurrentMV()),getHr().maximumWorkersPossible());





    }
}
