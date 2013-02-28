package agents.firm.production.control.facades;

import agents.firm.personell.HumanResources;
import agents.firm.production.control.PlantControl;
import agents.firm.production.control.TargetAndMaximizePlantControl;
import agents.firm.purchases.inventoryControl.Level;
import goods.Good;
import goods.GoodType;
import agents.firm.production.Plant;
import agents.firm.production.PlantListener;
import agents.firm.production.control.maximizer.FullCapacityMaximizer;
import agents.firm.production.control.maximizer.WorkforceMaximizer;
import agents.firm.production.control.targeter.PIDTargeter;
import agents.firm.production.control.targeter.WorkforceTargeter;
import agents.firm.production.technology.Machinery;

import javax.annotation.Nonnull;

/**
 * <h4>Description</h4>
 * <h4>Description</h4>
 * <p/> This now is just a facade, after I updated the plant control interface. Basically it creates a specific Target And Maximize PlantContol instance
 * and just delegates to it
 * <p/>  The TargetAndMaximizePlantControl it delegates has PIDTargeter and FullCapacityMaximizer
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-22
 * @see
 */
public class MaxCapacityControl implements PlantControl, PlantListener {

    /**
     * The actual control we delegate everything to
     */
    private final TargetAndMaximizePlantControl control;



    /**
     * Creates a TargetAndMaximizePlantControl with PIDTargeter and GradientMaximizer
     * @param hr human resources
     */
    public MaxCapacityControl(@Nonnull HumanResources hr) {
        //instantiate the real control
        control = TargetAndMaximizePlantControl.PlantControlFactory(hr, PIDTargeter.class, FullCapacityMaximizer.class);

    }


    public boolean isActive() {
        return control.isActive();
    }

    /**
     * Returns the human resources object
     */
    public HumanResources getHr() {
        return control.getHr();
    }

    /**
     * Sets the wage, update offers and then updates wages of current workers
     * @param newWage the new wage
     */
    public void setCurrentWage(long newWage) {
        control.setCurrentWage(newWage);
    }

    /**
     * Get the wage offered (for subclasses only)
     * @return the wage offered.
     */
    public long getCurrentWage() {
        return control.getCurrentWage();
    }

    /**
     * Returns the plant monitored by the HR
     */
    public Plant getPlant() {
        return control.getPlant();
    }

    /**
     * Ask the targeter what is the current worker target
     * @return the number of workers the strategy is targeted to find!
     */
    public int getTarget() {
        return control.getTarget();
    }

    /**
     * Set the flag to allow or ban the hr from hiring people
     * @param canBuy true if the hr can hire more people at this wage.
     */
    public void setCanBuy(boolean canBuy) {
        control.setCanBuy(canBuy);
    }

    /**
     * Plant Control factory, instantiates a class of the kind of targeter and maximizer specified
     * @param hr the human resources object
     */
    public static TargetAndMaximizePlantControl PlantControlFactory(@Nonnull HumanResources hr, Class<? extends WorkforceTargeter> targeterClass, Class<? extends WorkforceMaximizer> maximizerClass) {
        return TargetAndMaximizePlantControl.PlantControlFactory(hr, targeterClass, maximizerClass);
    }

    /**
     * The targeter is told that now we need to hire this many workers
     * @param workerSizeTargeted the new number of workers we should target
     */
    public void setTarget(int workerSizeTargeted) {
        control.setTarget(workerSizeTargeted);
    }

    /**
     * the method just calls the start of the Targeter and the Maximizer
     */
    @Override
    public void start() {
        control.start();
    }

    /**
     * pass the message down
     *
     * @param p          the plant that made the change
     * @param workerSize the new number of workers
     */
    @Override
    public void changeInWorkforceEvent(Plant p, int workerSize) {
        control.changeInWorkforceEvent(p, workerSize);
    }

    /**
     * This is called by the plant whenever the machinery used has been changed
     *
     * @param p         The plant p
     * @param machinery the machinery used.
     */
    @Override
    public void changeInMachineryEvent(Plant p, Machinery machinery) {
        control.changeInMachineryEvent(p, machinery);
    }

    /**
     * This method returns the control rating on current stock held <br>
     *
     * @return the rating on the current stock conditions or null if the department is not active.
     */
    @Override
    public Level rateCurrentLevel() {
        return control.rateCurrentLevel();
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
        control.changeInWageEvent(p, workerSize, wage);
    }

    /**
     * Answer the question: how much am I willing to pay for this kind of labor?
     * Notice that NO UPDATING SHOULD TAKE PLACE in calling this method. Human Resources expects maxPrice() to be consistent from one call to the next.
     * To notify hr of inconsistencies call updateEmployeeWages(). <br>
     * In short,for plant control, <b>this should be a simple getter.</b>. If you are a subclass and want to change it, use the current wage setter.
     *
     * @param type the type of good you want to buy
     * @return the maximum price I am willing to pay for this good
     */
    @Override
    public long maxPrice(@Nonnull GoodType type) {
        return control.maxPrice(type);
    }

    /**
     * Answer the question: how much am I willing to pay for this specific kind of labor?
     *
     * @param good the specific good being offered to you
     * @return the maximum price I am willing to pay for this good
     */
    @Override
    public long maxPrice(@Nonnull Good good) {
        return control.maxPrice(good);
    }

    /**
     * Call this if we change/remove the control to stop it from giving more orders.Turn off is irreversible
     */
    @Override
    public void turnOff() {
        control.turnOff();
    }

    /**
     * This is called whenever a plant has been shut down or just went obsolete
     *
     * @param p the plant that made the change
     */
    @Override
    public void plantShutdownEvent(Plant p) {
        control.plantShutdownEvent(p);
    }

    /**
     * This is used by the the user to ask the control whether or not to act.<br>
     *
     * @return
     */
    @Override
    public boolean canBuy() {
        return control.canBuy();
    }
}

