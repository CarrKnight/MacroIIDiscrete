package goods.production.control.maximizer;

import agents.firm.personell.HumanResources;
import financial.MarketEvents;
import goods.production.Plant;
import goods.production.control.PlantControl;
import goods.production.technology.Machinery;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * <h4>Description</h4>
 * <p/> This is a maximizer that every week checks profits and may decide to change workforce targets. It's abstract because it is really a template class. It takes care of checking profits and waiting a fixed number of weeks
 * before making a choice but the choice itself is made in a method to be overriden so that I can have many maximizers and so on.
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-09-23
 * @see
 */
public abstract class weeklyWorkforceMaximizer implements WorkforceMaximizer, Steppable {

    /**
     * Week length, set at constructor
     */
    private final float weekLength;

    /**
     * the human resources object
     */
    private final HumanResources hr;

    /**
     * the control object
     */
    private final PlantControl control;

    /**
     * keeps stepping as long as this is active!
     */
    boolean isActive = true;

    /**
     * here we memorize the last time we checked for profits
     */
    private float oldProfits = 0;

    /**
     * here we memorize the last worker target of ours
     */
    private int oldWorkerTarget = 0;


    /**
     * Profit check to change target is only activated at the start of the week when we have the right number of workers (or the observation makes no sense). When that week starts we set this flag to true
     */
    private boolean checkWeek = false;
    /**
     * Number of weeks to pass before we can trust the profit report we get.
     */
    private float weeksToMakeObservation = 3;

    public weeklyWorkforceMaximizer(HumanResources hr, PlantControl control) {
        this.hr = hr;
        this.control = control;

        weekLength = hr.getFirm().getModel().getWeekLength();
    }


    /**
     * Method to switch the strategy off. Irreversible
     */
    @Override
    public void turnOff()
    {
        isActive = false;
    }

    /**
     * ignored
     */
    @Override
    public void changeInWorkforceEvent(Plant p, int workerSize) {
    }

    /**
     * ignored
     */
    @Override
    public void changeInWageEvent(Plant p, int workerSize, long wage) {
    }

    /**
     ignored
     */
    @Override
    public void plantShutdownEvent(Plant p) {
    }

    /**
     * ignored
     */
    @Override
    public void changeInMachineryEvent(Plant p, Machinery machinery) {
    }

    /**
     * The profit check steps work as follow
     * <ul>
     *     <li>
     *         Wait until the targeter achieved the number of workers we set for it
     *     </li>
     *     <li>
     *         Once the targeter has achieved its targets we wait  "weeksToMakeObservation" so that all the other departments adapt to the new production (and to avoid initial noise)
     *     </li>
     *     <li>
     *         Checkweek: we waited enough weeks, compare old profits with new profits and select a new worker target
     *     </li>
     * </ul>
     * @param state
     */
    public void step(SimState state){

        if(!isActive) //if you are not active, you are done!
            return;

        //you are going to run this again in a week (with slight noise)
        double nextCheck;
        nextCheck = weekLength + state.random.nextFloat() * 5f;


        //if you haven't achieved your worker objective you can't really make a judgment so just try again in next week
        if(isActive() && hr.getPlant().workerSize() != control.getTarget()){
            state.schedule.scheduleOnceIn(nextCheck,this);
            checkWeek = false; //if it was observation week and you missed on your target, start over :(
            return;
        }
        //if we are at the right target then we move to checkWeek status
        if(hr.getPlant().workerSize() == control.getTarget() && !checkWeek){
            checkWeek = true; //next observation is check week!
       //     System.out.println("next week is checkweek!");
            state.schedule.scheduleOnceIn(nextCheck*weeksToMakeObservation,this);
            return;
        }




        //if we are here, it's observation week!
        assert checkWeek;
        //todo this happens very rarely during messy world, should I be concerned?
        assert hr.getPlant().workerSize() == control.getTarget() :  hr.getPlant().workerSize() + "," +  control.getTarget();

        //get profits
        float newProfits = hr.getFirm().getPlantProfits(hr.getPlant());



        //what's the future target?
        int futureTarget = chooseWorkerTarget(control.getTarget(),newProfits,oldWorkerTarget,oldProfits);


        //System.out.println(" old worker target: " + oldWorkerTarget + " , old profits: " + oldProfits + " current worker target: " + control.getTarget() + " current profits: " + newProfits);
 /*       System.out.println( getHr().getPlant().hashCode() + ", old Profits: " + oldProfits + ", new profits: " + newProfits +
                "; old workerTarget:" + oldWorkerTarget + ", new target:" + futureTarget +", current workers: " + getHr().getPlant().workerSize()
                + ", sale price: " + getHr().getFirm().getSalesDepartment(GoodType.GENERIC).getLastClosingPrice() +
                ",totalwages: " + getHr().getWagesPaid() + ", single wage: " + getControl().getCurrentWage() +
                ", marketVolume: " + getHr().getPlant().getModel().getMarket(GoodType.GENERIC).getLastWeekVolume());

   */

        //if the future target is negative, do it again next week (the subclass wants more info)
        if(futureTarget < 0){
            state.schedule.scheduleOnceIn(nextCheck*weeksToMakeObservation,new Steppable() {    //i make this anonymous only for debug sake (it checks that checkweek is still active)
                @Override
                public void step(SimState simState) {
                    assert checkWeek;
                    weeklyWorkforceMaximizer.this.step(simState);
                }
            });
        }
        else {


            //log it
            hr.getFirm().logEvent(hr,
                    MarketEvents.CHANGE_IN_TARGET,
                    hr.getFirm().getModel().getCurrentSimulationTimeInMillis(),
                    "old Profits: " + oldProfits + ", new profits: " + newProfits +
                            "; old workerTarget:" + oldWorkerTarget + ", new target:" + futureTarget);

            //remember
            oldProfits = newProfits;
            oldWorkerTarget = control.getTarget();


            //tell control/targeter about new target
            control.setTarget(futureTarget);

            //if we did change targets, next week is not observation week
            checkWeek = false; //set it to false


            //try again!
            state.schedule.scheduleOnceIn(nextCheck,this);

        }




    }


    /**
     * Method to start the workforce maximizer
     */
    @Override
    public void start() {
        //set target to 1
        control.setTarget(1);
        oldProfits = -getHr().getPlant().getCostStrategy().weeklyFixedCosts();
        oldWorkerTarget = 0;
        //adjust on it
        step(getHr().getPlant().getModel());

    }

    public boolean isActive() {
        return isActive;
    }


    /**
     * Asks the subclass what the next worker target will be!
     * @param currentWorkerTarget what is the current worker target
     * @param newProfits what are the new profits
     * @param oldWorkerTarget what was the target last time we changed them
     * @param oldProfits what were the profits back then
     * @return the new worker targets. Any negative number means to check again!
     */
    protected abstract int chooseWorkerTarget(int currentWorkerTarget, float newProfits, int oldWorkerTarget, float oldProfits);


    public float getWeekLength() {
        return weekLength;
    }

    public HumanResources getHr() {
        return hr;
    }

    public PlantControl getControl() {
        return control;
    }


    public float getWeeksToMakeObservation() {
        return weeksToMakeObservation;
    }

    public void setWeeksToMakeObservation(float weeksToMakeObservation) {
        this.weeksToMakeObservation = weeksToMakeObservation;
    }


    public boolean isCheckWeek() {
        return checkWeek;
    }

    public int getOldWorkerTarget() {
        return oldWorkerTarget;
    }

    public float getOldProfits() {
        return oldProfits;
    }




}
