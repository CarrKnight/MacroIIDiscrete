package agents.firm.production.control;

import agents.firm.personell.HumanResources;
import goods.Good;
import goods.GoodType;
import agents.firm.production.Plant;
import agents.firm.production.technology.Machinery;

import javax.annotation.Nonnull;

/**
 * <h4>Description</h4>
 * <p/> This is the "skeletric" (Java Effective term) abstract class implementing PlantControl. <br>
 * Since this control is integrated (both control and pricing strategy) the functioning is quite different:
 * <ul>
 *     <li> Whenever we change wages, we need to change it to everybody we ALREADY hired; that's unlike goods where only qutoes need to be adjusted</li>
 *     <li> Many subclasses target WAGES rather than number of workers. So all PID magic is quite useless</li>
 * </ul>
 * Because of the above plant control <b> stores the current wage</b>. Whenever that gets updated hr gets notified (so it can raise wages and change offers).
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
public abstract class AbstractPlantControl implements  PlantControl{

    /**
     * The human resources object we control
     */
    private final HumanResources hr;

    /**
     * The plant that is under the HR responsibility
     */
    private  final Plant plant;

    /**
     * whether the control allows the hr to buy more people!
     */
    private boolean canBuy = true;

    /**
     * A flag teling me whether the plant control is active or not.
     */
    private boolean isActive = true;

    /**
     * This is the wage held by the plant control, whenever it is changed important stuff happens.
     */
    private long currentWage;

    protected AbstractPlantControl(@Nonnull final HumanResources hr) {
        this.hr = hr;
        plant = hr.getPlant();

        //now LISTEN to the plant
        plant.addListener(this);



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
    public long maxPrice(@Nonnull final GoodType type){



        return currentWage;


    }

    /**
     * Answer the question: how much am I willing to pay for this specific kind of labor?
     *
     * @param good the specific good being offered to you
     * @return the maximum price I am willing to pay for this good
     */
    @Override
    public long maxPrice(@Nonnull final Good good){
        return maxPrice(good.getType());
    }


    /**
     * Call this if we change/remove the control to stop it from giving more orders.Turn off is irreversible
     */
    @Override
    public void turnOff() {
        if(isActive) //turn off is ignored if we are already off!
        {
        isActive = false; //remember you are off
        boolean stopped = plant.removeListener(this);  //stop listening.
        assert stopped; //make sure!
        }
    }

    /**
     * When instantiated the control doesn't move until it receives a stimulus OR start() is called. With this function you can start it immediately.
     * Notice: THIS DOESN'T ACTIVATE TURNEDOFF controls. Turn off is irreversible
     */
    @Override
    public abstract void start();


    /**
     * Sets the wage, update offers and then updates wages of current workers
     * @param newWage the new wage
     */
    @Override
    public void setCurrentWage(long newWage) {
        if(this.currentWage != newWage) //if the change is meaningful
        {

            //set the new wage
            this.currentWage = newWage;
            //tell new hires
            hr.updateOfferPrices();
            //tell the old employees
            hr.updateEmployeeWages();


            //if you can, buy
            if(canBuy())
                hr.buy();

        }



        //done!

    }

    /**
     * Returns the human resources object
     */
    public HumanResources getHr() {
        return hr;
    }

    /**
     * Returns the plant monitored by the HR
     */
    public Plant getPlant() {
        return plant;
    }

    public boolean isActive() {
        return isActive;
    }


    /**
     * Check if you want to hire more
     *
     * @param p          the plant that made the change
     * @param workerSize the new number of workers
     */
    @Override
    public abstract void changeInWorkforceEvent(Plant p, int workerSize);

    /**
     * This is called whenever a plant has been shut down or just went obsolete
     *
     * @param p the plant that made the change
     */
    @Override
    public void plantShutdownEvent(Plant p) {
        //sounds bad, probably we are going to turn off soon
    }

    /**
     * This is called by the plant whenever the machinery used has been changed
     *
     * @param p         The plant p
     * @param machinery the machinery used.
     */
    @Override
    public abstract void changeInMachineryEvent(Plant p, Machinery machinery);

    /**
     * Get the wage offered (for subclasses only)
     * @return the wage offered.
     */
    public long getCurrentWage() {
        return currentWage;
    }

    /**
     * This is used by the the user to ask the control whether or not to act.<br>
     *
     * @return
     */
    @Override
    public boolean canBuy() {
        return canBuy && plant.workerSize() < plant.maximumWorkersPossible();
    }

    /**
     * Set the flag to allow or ban the hr from hiring people
     * @param canBuy true if the hr can hire more people at this wage.
     */
    public void setCanBuy(boolean canBuy) {
     //   boolean different = canBuy != this.canBuy();
        this.canBuy = canBuy;

        if(!this.canBuy && hr.hasQuoted())
            hr.cancelQuote();


    }



}
