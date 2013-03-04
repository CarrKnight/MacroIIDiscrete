package agents.firm.production.control.maximizer.marginalMaximizers;

import agents.firm.personell.HumanResources;
import agents.firm.production.control.PlantControl;
import model.MacroII;
import model.utilities.DelayException;
import model.utilities.pid.PIDController;

/**
 * <h4>Description</h4>
 * <p/>  very simple marginal maximizer that chooses worker by targeting MB/MC = 1
 * where MB is unit marginal revenue and MC is unit marginal costs of increasing production by 1
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-02-25
 * @see
 */
public class MarginalMaximizerWithUnitPID  extends MarginalMaximizer
{

    private PIDController pid;

    /**
     * One weakness of this PID is that efficency is bound downward at 0 but can go to infinity. Since the target is 1 this means
     * that it's possible for it to move very quickly upward, but not very quickly downward (except for proportional effects).
     * When this is set to true, every efficiency of 0 is fed in the PID controller as an efficiency of -2.  Terrible hack but, will it work?
     */
    private boolean penalize0 = false;

    /**
     * this is the efficency targeted
     */
    private float targetEfficiency = 1;

    /**
     * Very simple marginal maximizer that chooses worker by targeting MB/MC = 1
     * where MB is unit marginal revenue and MC is unit marginal costs of increasing production by 1
     * @param hr the human resources
     * @param control the plant control.
     */
    public MarginalMaximizerWithUnitPID(HumanResources hr, PlantControl control) {
        super(hr, control);
        MacroII model = hr.getFirm().getModel();
        //the pid controller
        pid = new PIDController( 1.31f,0.71f,     //these numbers were tuned in the monopolist scenario
                0.055f,model.getRandom());
        pid.setOffset(hr.getPlant().workerSize());
    }


    /**
     * Very simple marginal maximizer that chooses worker by targeting MB/MC = 1
     * where MB is unit marginal revenue and MC is unit marginal costs of increasing production by 1
     * @param hr the human resources
     * @param control the plant control.
     */
    public MarginalMaximizerWithUnitPID(HumanResources hr, PlantControl control, float proportional,
                                        float integral, float derivative) {
        super(hr, control);
        MacroII model = hr.getFirm().getModel();
        //the pid controller
        pid = new PIDController(proportional,integral,derivative,model.getRandom());
        pid.setOffset(hr.getPlant().workerSize());
    }


    @Override
    protected int chooseWorkerTarget(int currentWorkerTarget, float newProfits, float newRevenues, float newCosts,
                                     float oldRevenues, float oldCosts, int oldWorkerTarget, float oldProfits) {

        //check the marginals always one step forward (not because you are moving one step only,
        // but because it's less biased by mistaken prediction of price changes

        try{


            float marginalProduction = MarginalMaximizerStatics.marginalProduction(getP(), currentWorkerTarget, currentWorkerTarget + 1);
            //cost
            MarginalMaximizerStatics.CostEstimate wageCosts = MarginalMaximizerStatics.computeWageCosts(getPolicy(), getP(), getHr(), getControl(), currentWorkerTarget, currentWorkerTarget + 1);
            MarginalMaximizerStatics.CostEstimate inputCosts = MarginalMaximizerStatics.computeInputCosts(getOwner(), getP(), getPolicy(), currentWorkerTarget, currentWorkerTarget + 1);
            float marginalCosts = wageCosts.getMarginalCost() + inputCosts.getMarginalCost();
            marginalCosts = marginalCosts / marginalProduction;

            //benefits
            float marginalBenefits = MarginalMaximizerStatics.computeMarginalRevenue(getOwner(), getP(), getPolicy(), currentWorkerTarget, currentWorkerTarget + 1,
                    inputCosts.getTotalCost(), wageCosts.getTotalCost());
            marginalBenefits = marginalBenefits / marginalProduction;

            //now that they are properly "averaged" we divide them and target efficency of 1
            float marginalEfficency = Math.min(10,marginalBenefits/marginalCosts);  //bound to 10


            //penalize if needed
            if(penalize0 && marginalEfficency ==0)
                marginalEfficency = -2;

            pid.adjustOnce(marginalEfficency- getTargetEfficiency(),true);
            return Math.min(Math.round(pid.getCurrentMV()), getHr().maximumWorkersPossible());






        }catch (DelayException e)
        {
            //if an exception was thrown, it must be because we need more time!
            return -1;
        }



    }


    public boolean isPenalize0() {
        return penalize0;
    }

    public void setPenalize0(boolean penalize0) {
        this.penalize0 = penalize0;
    }

    public float getTargetEfficiency() {
        return targetEfficiency;
    }

    public void setTargetEfficiency(float targetEfficiency) {
        this.targetEfficiency = targetEfficiency;
    }
}
