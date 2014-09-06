/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.processes;

import model.utilities.pid.PIDController;
import model.utilities.stats.regression.SISORegression;

import static model.utilities.stats.processes.PIGradientDescent.computeITAE;

/**
 * Basically tests whether a PID controller would do better by stepping less often
 * Created by carrknight on 9/2/14.
 */
public class StickinessDescent
{


    private final SISORegression systemRegression;

    private final PIDController originalController;

    private final double desiredTarget;

    private final int howManyStepsToSimulate;


    public StickinessDescent(SISORegression systemRegression, PIDController originalController, double desiredTarget, int howManyStepsToSimulate) {
        this.systemRegression = systemRegression;
        this.originalController = originalController;
        this.desiredTarget = desiredTarget;
        this.howManyStepsToSimulate = howManyStepsToSimulate;
    }


    public int getNewSpeed()
    {

        double originalITAE = computeITAE(new PIDController(originalController),systemRegression.generateDynamicProcessImpliedByRegression(),
                (float) desiredTarget,howManyStepsToSimulate);
        assert !(Double.isNaN(originalITAE ));

        //   System.out.println(originalITAE);

        /***
         *       _       ____
         *     _| |_    |    |
         *    |_   _|    |   |
         *      |_|      |   |
         *               |   |
         *               |   |
         *               |___|
         */
        PIDController stickierPID = new PIDController(originalController);
        stickierPID.setSpeed(originalController.getSpeed()+1);
        double stickyITAE = computeITAE(stickierPID,systemRegression.generateDynamicProcessImpliedByRegression(),
                (float) desiredTarget,howManyStepsToSimulate);
        assert !(Double.isNaN(stickyITAE ));
        /***
         *              ____
         *             |    |
         *     ____     |   |
         *    |____|    |   |
         *              |   |
         *              |   |
         *              |___|
         */
        double flexibleITAE = Double.MAX_VALUE;
        if(originalController.getSpeed() >0)
        {
            PIDController flexible = new PIDController(originalController);
            flexible.setSpeed(originalController.getSpeed()-1);
            flexibleITAE = computeITAE(flexible,systemRegression.generateDynamicProcessImpliedByRegression(),
                    (float) desiredTarget,howManyStepsToSimulate);
        }






        //choose the lowest
        System.out.println(flexibleITAE + " --- " + originalITAE + " -----"  + stickyITAE);
        if(originalITAE <= stickyITAE && originalITAE <= flexibleITAE)
            return originalController.getSpeed();
        if(stickyITAE <= originalITAE && stickyITAE <= flexibleITAE)
            return originalController.getSpeed() + 1;
        else
            return originalController.getSpeed() -1;







    }



}
