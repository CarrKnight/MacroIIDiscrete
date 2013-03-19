/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.production.control;

import agents.firm.personell.HumanResources;
import agents.firm.production.Plant;
import agents.firm.production.control.decorators.PlantControlDecorator;
import agents.firm.production.control.maximizer.WorkforceMaximizer;
import agents.firm.production.control.maximizer.algorithms.WorkerMaximizationAlgorithm;
import agents.firm.production.control.targeter.WorkforceTargeter;
import agents.firm.production.technology.Machinery;
import agents.firm.purchases.inventoryControl.Level;
import com.google.common.base.Preconditions;
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
    private WorkforceTargeter targeter;

    /**
     * The strategy running the change in targets.
     */
    private WorkforceMaximizer maximizer;

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
     * Create a plant control with premade targeter and Maximizer
     * @param hr the human resource
     * @return the target and maximize control
     */
    public static TargetAndMaximizePlantControl emptyTargetAndMaximizePlantControl(@Nonnull final HumanResources hr)
    {
        return new  TargetAndMaximizePlantControl(hr);
    }

    /**
     * Plant Control factory, instantiates a class of the kind of targeter and maximizer specified
     * @param hr the human resources object
     */
    public static <WT extends WorkforceTargeter, ALG extends WorkerMaximizationAlgorithm, WM extends WorkforceMaximizer<ALG>>
    FactoryProducedTargetAndMaximizePlantControl<WT,WM> PlantControlFactory(
            @Nonnull final HumanResources hr, Class<WT> targeterClass,Class<WM> maximizerClass,
            Class<ALG> algorithmType) {

        //todo switch to generator so I can do it by string too.
        TargetAndMaximizePlantControl instance = new TargetAndMaximizePlantControl(hr);

        try {
            //create substrategies
            WT targeter = targeterClass.getConstructor(HumanResources.class,PlantControl.class).newInstance(hr,instance);
           WM maximizer =maximizerClass.getConstructor(HumanResources.class,PlantControl.class,Class.class).
                   newInstance(hr,instance,algorithmType);
            //assign them!
            instance.targeter = targeter;
            instance.maximizer = maximizer;

            FactoryProducedTargetAndMaximizePlantControl<WT,WM> container =
                    new FactoryProducedTargetAndMaximizePlantControl<WT, WM>(
                    targeter,maximizer,instance
            );
            assert instance.targeter == container.getWorkforceTargeter();
            assert instance.maximizer == container.getWorkforceMaximizer();
            assert instance == container.getControl();

            return container;

        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new IllegalArgumentException("targeter and maximizer classes failed to instantiate either due to conditions being wrong or me being an idiot  " +'\n'
                    + targeterClass + ", " + algorithmType + "---" + ((InvocationTargetException) e).getCause().toString());
        }

    }


    /**
     * This plant control factory adds decorators to the control. for a good measure. It assumes all decorators need only the control object to instantiate
     * @param hr the human resources object
     */
    @SafeVarargs
    public static <WT extends WorkforceTargeter, ALG extends WorkerMaximizationAlgorithm, WM extends WorkforceMaximizer<ALG> >
    PlantControl
    PlantControlFactory(@Nonnull final HumanResources hr,
                        Class<WT> targeterClass,
                        Class<WM> maximizerClass,
                        Class<ALG> algorithmType,
                        Class<? extends PlantControlDecorator>... decorators) {

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
            WT targeter = targeterClass.getConstructor(HumanResources.class,PlantControl.class).newInstance(hr,instance);
            WM maximizer = maximizerClass.getConstructor(HumanResources.class,PlantControl.class,Class.class).
                    newInstance(hr,instance,algorithmType);

            //assign them!
            originalInstance.targeter = targeter;
            originalInstance.maximizer = maximizer;



        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new IllegalArgumentException("targeter and maximizer classes failed to instantiate either due to conditions being wrong or me being an idiot." + '\n'
                    + algorithmType.getCanonicalName() + " --- " +
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

    /**
     * This is used to set the targeter if you created an empty target and maximize plant control. It throws an exception
     * if it had already been set
     * @param targeter the new targeter to use
     */
    public void setTargeter(WorkforceTargeter targeter) {
        Preconditions.checkState(this.targeter==null);
        Preconditions.checkNotNull(targeter);
        this.targeter = targeter;
    }

    /**
     * This is used to set the maximizer if you created an empty target and maximize plant control. It throws an exception
     * if it had already been set
     * @param maximizer the new maximizer to use
     */
    public void setMaximizer(WorkforceMaximizer maximizer) {
        Preconditions.checkState(this.maximizer==null);
        Preconditions.checkNotNull(maximizer);
        this.maximizer = maximizer;
    }
}
