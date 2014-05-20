/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.production.control.maximizer.algorithms;

import agents.firm.personell.HumanResources;
import agents.firm.production.control.PlantControl;
import agents.firm.production.control.maximizer.algorithms.hillClimbers.*;
import agents.firm.production.control.maximizer.algorithms.marginalMaximizers.*;
import agents.firm.production.control.maximizer.algorithms.otherMaximizers.FixedTargetMaximizationAlgorithm;


/**
 * <h4>Description</h4>
 * <p/> This creates a MaximizationAlgorithm given a class object. This is interesting in terms of code because
 * I am quite tired of making reflection magic and I am starting to think just a long switch is better.
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-03-12
 * @see
 */
public class WorkerMaximizationAlgorithmFactory
{


    public static
    <T extends WorkerMaximizationAlgorithm> T buildMaximizationAlgorithm( HumanResources hr,
                                                                          PlantControl plantControl,
                                                                          Class<T> tClass)
    {
        //hill-climbers
        if(tClass.equals(AlwaysMovingHillClimber.class))
            return tClass.cast(new AlwaysMovingHillClimber(hr.getPlant().minimumWorkersNeeded(),hr.getPlant().maximumWorkersPossible()));
        if(tClass.equals(AnnealingMaximizer.class))
            return tClass.cast(new AnnealingMaximizer(hr.getPlant().weeklyFixedCosts(),hr.getPlant().minimumWorkersNeeded(),
                    hr.getPlant().maximumWorkersPossible(),hr.getRandom()));
        if(tClass.equals(AnnealingReactingMaximizer.class))
            return tClass.cast(new AnnealingReactingMaximizer(hr.getPlant().weeklyFixedCosts(),hr.getPlant().minimumWorkersNeeded(),
                    hr.getPlant().maximumWorkersPossible(),hr.getRandom()));
        if(tClass.equals(GradientMaximizer.class))
            return tClass.cast(new GradientMaximizer(hr.getPlant().weeklyFixedCosts(),hr.getPlant().minimumWorkersNeeded(),
                    hr.getPlant().maximumWorkersPossible()));
        if(tClass.equals(HillClimberMaximizer.class))
            return tClass.cast(new HillClimberMaximizer(hr.getPlant().weeklyFixedCosts(),hr.getPlant().minimumWorkersNeeded(),
                    hr.getPlant().maximumWorkersPossible()));
        if(tClass.equals(ParticleMaximizer.class))
            return tClass.cast(new ParticleMaximizer(hr.getPlant().weeklyFixedCosts(),hr.getPlant().minimumWorkersNeeded(),
                    hr.getPlant().maximumWorkersPossible(),hr.getRandom(),hr));
        if(tClass.equals(PIDHillClimber.class))
            return tClass.cast(new PIDHillClimber(hr.getPlant().weeklyFixedCosts(),hr.getPlant().minimumWorkersNeeded(),
                    hr.getPlant().maximumWorkersPossible(),hr.getPlant().getNumberOfWorkers(),hr.getRandom(),
                    hr.getPlant().getModel().drawProportionalGain(),
                    hr.getPlant().getModel().drawIntegrativeGain(), hr.getPlant().getModel().drawDerivativeGain()));
        //marginals
        if(tClass.equals(MarginalAndPIDMaximizer.class))
            return tClass.cast(new MarginalAndPIDMaximizer(hr,plantControl,hr.getPlant(),hr.getFirm(),hr.getPlant().getModel()));
        if(tClass.equals(MarginalMaximizer.class))
            return tClass.cast(new MarginalMaximizer(hr,plantControl,hr.getPlant(),hr.getFirm()));
        if(tClass.equals(RobustMarginalMaximizer.class))
            return tClass.cast(new RobustMarginalMaximizer(hr,plantControl,hr.getPlant(),hr.getFirm()));
        if(tClass.equals(MarginalMaximizerWithUnitPID.class))
            return tClass.cast(new MarginalMaximizerWithUnitPID(hr,plantControl,hr.getPlant(),hr.getFirm(),hr.getRandom(),
                    hr.getPlant().getNumberOfWorkers()));
        if(tClass.equals(MarginalMaximizerWithUnitPIDCascadeEfficency.class))
            return tClass.cast(new MarginalMaximizerWithUnitPIDCascadeEfficency(hr,plantControl,hr.getPlant(),hr.getFirm(),hr.getRandom(),
                    hr.getPlant().getNumberOfWorkers()));
        if(tClass.equals(FixedTargetMaximizationAlgorithm.class))
            return tClass.cast(new FixedTargetMaximizationAlgorithm());


        //if we are here we didn't find it!
        throw new IllegalArgumentException("The WorkerMaximizationAlgorithmFactory doesn't know about the class supplied "
                + tClass.getName());




    }

}
