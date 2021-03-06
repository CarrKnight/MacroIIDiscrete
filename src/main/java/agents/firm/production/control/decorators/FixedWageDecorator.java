/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.production.control.decorators;

import agents.firm.production.control.PlantControl;
import model.utilities.NonDrawable;

/**
 * <h4>Description</h4>
 * <p/> This decorator intercepts calls to set wages BELOW the current value and ignores them: wages never go down. Instead the control starts firing people if the targets aren't met.
 * <p/> The decorator also stops the plant control from hiring more people until
 * <p/> Fixed wage decorator remembers whether the last wage set was an increase or decrease. If it was a decrease it stops the human resources from hiring any more until a new wage increase is proposed
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-09-26
 * @see
 */
@NonDrawable //decorators aren't drawable per se
public class FixedWageDecorator extends PlantControlDecorator {

    /**
     * Fixed wage decorator remembers whether the last wage set was an increase or decrease. If it was a decrease it stops the human resources from hiring any more
     */
    boolean lastWageWasACut = false;

    /**
     * instantiate the decorator
     *
     * @param toDecorate the plant control to decorate
     */
    public FixedWageDecorator(PlantControl toDecorate) {
        super(toDecorate);
    }

    /**
     * Pass the new wage to the delegate ONLY if the wage is above
     *
     * @param newWage the new wage
     */
    @Override
    public void setCurrentWage(int newWage)
    {
        if(newWage > maxPrice(toDecorate.getHr().getGoodType()))   //if it's an increase...
        {
            lastWageWasACut = false;
            toDecorate.setCurrentWage(newWage); //do it
        }
        else{
            lastWageWasACut = true;

            //ignore the wage cut, rather layoff what's needed
            if(toDecorate.getHr().getPlant().getNumberOfWorkers() >  getTarget())
            {
                //keep removing workers till you are done
                toDecorate.getHr().getPlant().removeLastWorker();
            }




        }
    }


}
