package agents.firm.production.control;

import agents.firm.personell.HumanResources;
import agents.firm.purchases.inventoryControl.Level;
import agents.firm.production.Plant;
import agents.firm.production.control.decorators.PlantControlDecorator;
import agents.firm.production.control.maximizer.WorkforceMaximizer;
import agents.firm.production.control.targeter.WorkforceTargeter;
import agents.firm.production.technology.Machinery;
import model.utilities.NonDrawable;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * <h4>Description</h4>
 * <p/> This is the "composite" plant control that is made up of a Workforce Targeter and a Workforce Maximizer. Workforce targeter is a "high-frequency" decision maker that adjust wages to achieve worker-size targets.
 * The targets are instead set by the workforce maximizer presumably as an attempt to maximize profits.
 * <p/> Targeter and maximizers work on their own and this class responsibility is only to pass down messages and turning on/off gracefully.
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/> It is non-drawable!
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-09-23
 * @see
 */
@NonDrawable
public class TargetAndMaximizePlantControl extends AbstractPlantControl {

    /**
     * the strategy running the wage-setting people hiring.
     */
    WorkforceTargeter targeter;

    /**
     * The strategy running the change in targets.
     */
    WorkforceMaximizer maximizer;

    /**
     * the method just calls the start of the Targeter and the Maximizer
     */
    @Override
    public void start() {
        //start the targeter
        targeter.start();
        //start the maximizer
        maximizer.start();

    }

    /**
     * pass the message down
     *
     * @param p          the plant that made the change
     * @param workerSize the new number of workers
     */
    @Override
    public void changeInWorkforceEvent(Plant p, int workerSize) {
        //pass the message
        targeter.changeInWorkforceEvent(p,workerSize);
        maximizer.changeInWorkforceEvent(p,workerSize);

    }

    /**
     * This is called by the plant whenever the machinery used has been changed
     *
     * @param p         The plant p
     * @param machinery the machinery used.
     */
    @Override
    public void changeInMachineryEvent(Plant p, Machinery machinery) {
        targeter.changeInMachineryEvent(p, machinery);
        maximizer.changeInMachineryEvent(p, machinery);    }

    /**
     * This method returns the control rating on current stock held <br>
     *
     * @return the rating on the current stock conditions or null if the department is not active.
     */
    @Override
    public Level rateCurrentLevel() {
        if(canBuy()) return Level.ACCEPTABLE;
        else
            return Level.DANGER;
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
        targeter.changeInWageEvent(p, workerSize, wage);
        maximizer.changeInWageEvent(p, workerSize, wage);
    }

    /**
     * Completely default target and maximizer, to be filled by the factory.
     * @param hr the human resources object
     */
    private TargetAndMaximizePlantControl(@Nonnull final HumanResources hr) {
        super(hr);
    }

    /**
     * Plant Control factory, instantiates a class of the kind of targeter and maximizer specified
     * @param hr the human resources object
     */
    public static TargetAndMaximizePlantControl PlantControlFactory(@Nonnull final HumanResources hr, Class<? extends WorkforceTargeter> targeterClass,
                                                                    Class<? extends WorkforceMaximizer> maximizerClass) {

        //todo switch to generator so I can do it by string too.
        TargetAndMaximizePlantControl instance = new TargetAndMaximizePlantControl(hr);

        try {
            //create substrategies
            WorkforceTargeter targeter = targeterClass.getConstructor(HumanResources.class,PlantControl.class).newInstance(hr,instance);
            WorkforceMaximizer maximizer = maximizerClass.getConstructor(HumanResources.class,PlantControl.class).newInstance(hr,instance);
            //assign them!
            instance.targeter = targeter;
            instance.maximizer = maximizer;
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new IllegalArgumentException("targeter and maximizer classes failed to instantiate either due to conditions being wrong or me being an idiot  " +'\n'
                    + targeterClass + ", " + maximizerClass + "---" + ((InvocationTargetException) e).getCause().toString());
        }

       return instance;
    }


    /**
     * This plant control factory adds decorators to the control. for a good measure. It assumes all decorators need only the control object to instantiate
     * @param hr the human resources object
     */
    @SafeVarargs
    public static PlantControl PlantControlFactory(@Nonnull final HumanResources hr, Class<? extends WorkforceTargeter> targeterClass,
                                                                    Class<? extends WorkforceMaximizer> maximizerClass, Class<? extends PlantControlDecorator>... decorators) {

        //todo switch to generator so I can do it by string too.
        //create the original instance
        TargetAndMaximizePlantControl originalInstance = new TargetAndMaximizePlantControl(hr);
        //make it more generic so I can add decorators
        PlantControl instance = originalInstance;


        try {
            //add all the decorators, if any
            for(Class<? extends PlantControlDecorator> decorator : decorators)
            {

                //keep decorating, in order
                instance = decorator.getConstructor(PlantControl.class).newInstance(instance);
            }



            //create substrategies
            WorkforceTargeter targeter = targeterClass.getConstructor(HumanResources.class,PlantControl.class).newInstance(hr,instance);
            WorkforceMaximizer maximizer = maximizerClass.getConstructor(HumanResources.class,PlantControl.class).newInstance(hr,instance);
            //assign strategies to the original instance (it's the only one that knows about its fields)
            originalInstance.targeter = targeter;
            originalInstance.maximizer = maximizer;
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new IllegalArgumentException("targeter and maximizer classes failed to instantiate either due to conditions being wrong or me being an idiot." + '\n' + maximizerClass.getCanonicalName() + " --- " +
            targeterClass.getCanonicalName() + " ---- " + Arrays.toString(decorators));
        }

        return instance;
    }



    /**
     * The targeter is told that now we need to hire this many workers
     * @param workerSizeTargeted the new number of workers we should target
     */
    @Override
    public void setTarget(int workerSizeTargeted) {
        targeter.setTarget(workerSizeTargeted);
    }

    /**
     * Ask the targeter what is the current worker target
     * @return the number of workers the strategy is targeted to find!
     */
    @Override
    public int getTarget() {
        return targeter.getTarget();
    }



}
