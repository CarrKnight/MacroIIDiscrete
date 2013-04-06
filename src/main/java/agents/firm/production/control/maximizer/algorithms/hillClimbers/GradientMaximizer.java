/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.production.control.maximizer.algorithms.hillClimbers;

/**
 * <h4>Description</h4>
 * <p/> This is like hill-climber but the adjust size is half the derivative (ceiled)
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
public class GradientMaximizer extends HillClimberMaximizer {


    public GradientMaximizer(long weeklyFixedCosts, int minimumWorkers, int maximumWorkers) {
        super(weeklyFixedCosts, minimumWorkers, maximumWorkers);
    }

    /**
     * This should return the absolute value of the difference between futureTarget - currentTarget.
     *
     * @param currentWorkerTarget what is the current worker target
     * @param newProfits          what are the new profits
     * @param oldWorkerTarget     what was the target last time we changed them
     * @param oldProfits          what were the profits back then
     * @return the return can't be negative or an exception is thrown!!!
     */
    @Override
    protected int stepSize(int currentWorkerTarget, float newProfits, int oldWorkerTarget, float oldProfits) {

        //if you are at the same point, don't bother because the derivative would be infinite
        if(currentWorkerTarget == oldWorkerTarget || newProfits == oldProfits)
            return 1;
        else
        {
            float derivative = Math.abs((newProfits - oldProfits)/((float) (currentWorkerTarget - oldWorkerTarget)));
            int toReturn =  (int)Math.ceil(derivative); //return the derivative ceiled!
            assert toReturn > 0 ;
            return toReturn;
        }

    }
}
