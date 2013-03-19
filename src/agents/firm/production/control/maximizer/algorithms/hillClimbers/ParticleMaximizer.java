/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.production.control.maximizer.algorithms.hillClimbers;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.production.Plant;
import ec.util.MersenneTwisterFast;
import goods.GoodType;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;

/**
 * <h4>Description</h4>
 * <p/> This wants to be a very PALE imitation of a particle maximizer. Technically it's just a particle.
 * The main implementation problem is of course that I can't just instantiate the plant at a random worker-target since it would still have to hire those workers.
 * Instead the particle will act as an hill climber for the first x steps (right now x hardcoded to be 50,000)
 * <p/> It is also a good example of how terrible of a programmer I am. This extends hillclimber maximizer because it uses its memory, but it is really a profanity given how much it overrides.
 * Oh well. It's not like anybody is ever going to read this!
 * <p/> Velocity is always ceiled when finding the next target since targets have to be integers!
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-10-06
 * @see
 */
public class ParticleMaximizer extends  HillClimberMaximizer {

    /**
     * the speed the particle is currently following
     */
    private float currentVelocity;

    /**
     * This is the maximum inertia on the speed of  maximizer, the alpha of PSO
     */
    private float velocityInertia = .75f;

    /**
     * This is the maximum attraction of the personal best, the beta of PSO
     */
    private float personalBestAttraction = .15f;

    /**
     * This is the maximum attraction to either the target of a random competitor or our current result, the gamma of PSO
     */
    private float neighborAttraction = .15f;
    /**
     * This is the maximum attraction to either the target of a random competitor or our current result, the gamma of PSO
     */
    private float bestAttraction = .5f;

    /**
     * This is the randomizer used by the maximizer. It is stolen from the plant at initialization
     */
    private MersenneTwisterFast random;

    /**
     * Before this time, the particle will just hillclimb
     * //todo make this random!
     */
    private float timeToSpendHillClimbing;
    /**
     * the human resources object! Needed to check the time
     */
    private final HumanResources hr;


    public ParticleMaximizer(long weeklyFixedCosts, int minimumWorkers, int maximumWorkers,
                             @Nonnull MersenneTwisterFast random,@Nonnull HumanResources hr) {
        super(weeklyFixedCosts, minimumWorkers, maximumWorkers);
        this.random = random;
        this.hr = hr;
    }

    /**
     * Particle optimization works on velocity which is really direction * stepSize.
     * We are going to compute velocity in stepSize so this will always be 1
     * @return 1
     */
    @Override
    protected int direction(int currentWorkerTarget, float newProfits, int oldWorkerTarget, float oldProfits) {
        if(hr.getTime() > timeToSpendHillClimbing)
            return 1;
        else
            return super.direction(currentWorkerTarget,newProfits,oldWorkerTarget,oldProfits);
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

        if(hr.getTime() > timeToSpendHillClimbing)
        {
            //velocity should have moved us here!
//        assert currentWorkerTarget - (int)Math.round(currentVelocity) == oldWorkerTarget;



            //inertial part
            float inertialVelocity =  random.nextFloat() * velocityInertia * currentVelocity;
            float personalBest = random.nextFloat() *  personalBestAttraction * (getBestWorkerTargetInMemory() - currentWorkerTarget);
            float neighborBest = random.nextFloat() * neighborAttraction * (currentOrNeighborTarget(currentWorkerTarget,newProfits) -currentWorkerTarget );
            float best = random.nextFloat() * bestAttraction * (bestNeighborTarget(currentWorkerTarget, newProfits) -currentWorkerTarget );


            //      System.out.println("iV: " + inertialVelocity + ", pB: " + personalBest + ", nB: " + neighborBest);
            //update velocity!
            currentVelocity = inertialVelocity + personalBest + neighborBest + best;

  //          if(Math.round(currentVelocity) == 0) //if you round to 0, only round to 0 if it's really close, otherwise keep exploring
 //               return !random.nextBoolean(Math.abs(currentVelocity)) ? 0 : (int) Math.signum(currentVelocity);

            return (int)Math.round(currentVelocity);

        }
        else
            return super.stepSize(currentWorkerTarget,newProfits,oldWorkerTarget,oldProfits);
    }


    /**
     * Picks a random competitor and studies its profits. If its profits are higher than our current ones, return its workforce, otherwise return ours
     * @return
     */
    private int currentOrNeighborTarget(int currentWorkerTarget, float newProfits){
        //get random neighbor
        ArrayList<EconomicAgent> agents =  new ArrayList<>(hr.getAllEmployers());
        assert !agents.isEmpty(); //it can't be empty, at the very least there us in it!
        //uuuuh, ugly explicit cast! but what can I do? It is surely an employer!
        Firm randomCompetitor = (Firm) agents.get(random.nextInt(agents.size()));
        assert randomCompetitor != null;


        //TODO the big problem here is that we might pick up plants that completeProductionRunNow a different SET of goods, then we are imitating them wrongly. Maybe I need a list of all plants with the same kind of machinery?

        //if we randomly picked ourselves, just return ourselves
        if(randomCompetitor == hr.getFirm())
            return currentWorkerTarget;
        //Demeter is turning in his grave
        GoodType output = hr.getPlant().getBlueprint().getOutputs().keySet().iterator().next();
        Plant competitorPlant = randomCompetitor.getRandomPlantProducingThis(output);
        //are our profits higher?
        if(newProfits > randomCompetitor.getPlantProfits(competitorPlant) )
            return currentWorkerTarget;
        else
            return competitorPlant.workerSize();


    }


    /**
     * Picks a random competitor and studies its profits. If its profits are higher than our current ones, return its workforce, otherwise return ours
     * @return
     */
    private int bestNeighborTarget(int currentWorkerTarget, float newProfits){
        //get random neighbor
        Collection<EconomicAgent> agents =  new ArrayList<>(hr.getAllEmployers());
        assert !agents.isEmpty(); //it can't be empty, at the very least there us in it!
        //this is very stupid: just copy from a plant that produces our first output. Really silly but until I smarten up this is the only thing I can do
        GoodType output = hr.getPlant().getBlueprint().getOutputs().keySet().iterator().next();

        EconomicAgent bestCompetitor = null; float maxProfits = Float.NEGATIVE_INFINITY; Plant bestPlant =null;
        //go through all agents
        for(EconomicAgent a : agents){
            if(a instanceof Firm)
            {
                //find out their profits
                //uuuuh, ugly explicit cast! but what can I do? It is surely an employer!
                Plant p = ((Firm)a).getRandomPlantProducingThis(output);
                float competitorProfits = ((Firm) a).getPlantProfits(p);
                if(competitorProfits > maxProfits)
                {
                    //record it as the best
                    maxProfits = competitorProfits;
                    bestPlant = p;
                    bestCompetitor = a;

                }
            }

        }


        //TODO the big problem here is that we might pick up plants that completeProductionRunNow a different SET of goods, then we are imitating them wrongly. Maybe I need a list of all plants with the same kind of machinery?

        assert bestPlant != null : "max Profits: " + maxProfits +", best competitor: " + bestCompetitor + ", agents: " + agents; //at worst it's us
        assert maxProfits >= newProfits;
        return bestPlant.workerSize();



    }


    /**
     * Memory is not a problem, always returns true
     */
    @Override
    public boolean checkMemory(int futureTarget, float currentProfits) {
        if(hr.getTime() > timeToSpendHillClimbing)
            return true;
        else
            return super.checkMemory(futureTarget,currentProfits);

    }

    public float getVelocityInertia() {
        return velocityInertia;
    }

    public void setVelocityInertia(float velocityInertia) {
        this.velocityInertia = velocityInertia;
    }

    public float getPersonalBestAttraction() {
        return personalBestAttraction;
    }

    public void setPersonalBestAttraction(float personalBestAttraction) {
        this.personalBestAttraction = personalBestAttraction;
    }

    public float getNeighborAttraction() {
        return neighborAttraction;
    }

    public void setNeighborAttraction(float neighborAttraction) {
        this.neighborAttraction = neighborAttraction;
    }

    public MersenneTwisterFast getRandom() {
        return random;
    }

    public void setRandom(MersenneTwisterFast random) {
        this.random = random;
    }

    public float getTimeToSpendHillClimbing() {
        return timeToSpendHillClimbing;
    }

    public void setTimeToSpendHillClimbing(float timeToSpendHillClimbing) {
        this.timeToSpendHillClimbing = timeToSpendHillClimbing;
    }

    public float getBestAttraction() {
        return bestAttraction;
    }

    public void setBestAttraction(float bestAttraction) {
        this.bestAttraction = bestAttraction;
    }

    public float getCurrentVelocity() {
        return currentVelocity;
    }
}
