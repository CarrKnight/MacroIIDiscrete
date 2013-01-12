package agents.firm.production.control.maximizer;

import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.purchases.PurchasesDepartment;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import financial.Market;
import goods.GoodType;
import agents.firm.production.Plant;
import agents.firm.production.control.PlantControl;
import model.utilities.DelayException;

import java.util.Set;

/**
 * <h4>Description</h4>
 * <p/>
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-10-04
 * @see
 */
public class MarginalMaximizer extends weeklyWorkforceMaximizer
{
    /**
     * The plant to maximize
     */
    final private Plant p;

    /**
     * The firm owning the plant
     */
    final private Firm owner;

    /**
     * What to do if some predictions don't exist?
     */
    private RandomizationPolicy policy = RandomizationPolicy.MORE_TIME;

    /**
     * Each time the marginal maximizer is called,
     */

    /**
     * Creates a maximizer that acts as hill-climber but rather than "experimenting" it infers the slope by checking marginal costs
     * @param hr Human resources
     * @param control The plant control
     */
    public MarginalMaximizer(HumanResources hr, PlantControl control) {
        super(hr,control);
        p = getHr().getPlant();
        owner = p.getOwner();
    }

    /**
     * Computes the marginal profits of changing worker size
     * @param currentWorkers the workers the plan currently has
     * @param targetWorkers the number of workers we want to have
     * @return the marginal profits of this change in workforce
     * @throws Exception an exception is thrown if a prediction can't be made and we require more time.
     */
    public float computeMarginalProfits(int currentWorkers, int targetWorkers) throws DelayException {
        Preconditions.checkArgument(targetWorkers != currentWorkers, "marginal from here to here is stupid");
        assert currentWorkers == p.workerSize(); //I can't think of a use where this isn't true

        //this function smells a bit because it keeps branching according to whether we are hiring or firing. But I really wanted a generic function.


        /**********************************************
         * WAGES
         *********************************************/
        long marginalWageCosts; long futureWage;

        if(targetWorkers > currentWorkers)  //if we are going to hire somebody
        {
            //wages!
            futureWage = getHr().predictPurchasePrice();
            //if there is no prediction react to it
            futureWage = futureWage < 0 ? policy.replaceUnknownPrediction(getHr().getMarket(),p.getRandom()) : futureWage;
            //remember that you'll have to raise everybody's wages if you hire somebody new at a higher cost
            if(futureWage > getControl().getCurrentWage() && getHr().isFixedPayStructure())  //if you have to raise your wages to hire somebody
            {
                //your total costs then are the new wage + the adjustment you'll have to make to pay everybody that wage
                marginalWageCosts = (futureWage * p.workerSize() - getHr().getWagesPaid()) + futureWage * (targetWorkers-currentWorkers);
                assert marginalWageCosts > 0 || futureWage == 0;
            }
            else{
                marginalWageCosts = futureWage * (targetWorkers-currentWorkers); //otherwise you just need to hire the new guy
            }
        }
        else{
            //we are firing people, what wage would ensue then?
            futureWage = getHr().hypotheticalWageAtThisLevel(targetWorkers);
            //make a list of workers

            marginalWageCosts = futureWage * (targetWorkers-currentWorkers); //get marginal cost!
            assert marginalWageCosts <=0;    //cost should be negative! we are saving!
        }

        /**********************************************
         * INPUTS
         *********************************************/
        Set<GoodType> inputs = p.getBlueprint().getInputs().keySet();
        long marginalInputCosts = 0; //here we will store the costs of increasing production
        long totalInputCosts = 0; //here we will store the TOTAL input costs of increased production
        for(GoodType input : inputs)
        {
            int totalInputNeeded =  p.hypotheticalWeeklyInputNeeds(input,targetWorkers);  //total input needed
            int marginalInputNeeded = totalInputNeeded-  p.hypotheticalWeeklyInputNeeds(input,currentWorkers) ;  //marginal input needs
            assert (marginalInputNeeded>=0 && targetWorkers > currentWorkers) ^ (marginalInputNeeded<=0 && targetWorkers < currentWorkers) ; //

            PurchasesDepartment dept = owner.getPurchaseDepartment(input); //get the purchase department that buys this input
            long costPerInput = targetWorkers > currentWorkers ? dept.predictPurchasePrice() : dept.getLastClosingPrice(); //if we are increasing production, predict. if we are decreasing production use old prices
            //if there is no prediction, react to it
            costPerInput = costPerInput < 0 ? policy.replaceUnknownPrediction(owner.getPurchaseDepartment(input).getMarket(),p.getRandom()) : costPerInput;

            //count the costs!
            marginalInputCosts+= (costPerInput * totalInputNeeded) - dept.getLastClosingPrice() *
                    p.hypotheticalWeeklyInputNeeds(input,currentWorkers) ;
            //marginal costs are negative (marginal savings) if we are reducing production
            assert (marginalInputCosts >= 0 && targetWorkers > currentWorkers) ^   (marginalInputCosts <= 0 && targetWorkers < currentWorkers);
            totalInputCosts +=  costPerInput*totalInputNeeded;
            assert totalInputCosts >= 0;
        }

        //sum up everything
        long totalMarginalCosts = marginalWageCosts + marginalInputCosts;
        assert(totalMarginalCosts >= 0 && targetWorkers > currentWorkers) ^   (totalMarginalCosts <= 0 && targetWorkers < currentWorkers);
        //we are going to need the total costs to predict our future sales price
        long totalFutureCosts = totalInputCosts;
        long totalFutureWageCosts = futureWage * targetWorkers;
        assert totalFutureCosts>=0;

        /*********************************************************
         * Marginal Revenues
         ********************************************************/
        float marginalRevenue = 0;
        Set<GoodType> outputs = p.getBlueprint().getOutputs().keySet();
        for(GoodType output : outputs)
        {
            float marginalProduction = p.hypotheticalThroughput(targetWorkers,output) - p.hypotheticalThroughput(currentWorkers,output);
            assert (marginalProduction >= 0 && targetWorkers >= currentWorkers) ^  (marginalProduction <= 0 && targetWorkers < currentWorkers);
            //if you are increasing production, predict future price. Otherwise get last price
            long pricePerUnit = targetWorkers > currentWorkers ? owner.getSalesDepartment(output).predictSalePrice(p.hypotheticalUnitOutputCost(output,totalFutureCosts,targetWorkers,totalFutureWageCosts)) :
                    owner.getSalesDepartment(output).getLastClosingPrice();
            //if prediction is not available, react to it!
            pricePerUnit = pricePerUnit < 0 ? policy.replaceUnknownPrediction(owner.getSalesDepartment(output).getMarket(),p.getRandom()) : pricePerUnit;
            //add it to the revenue
            marginalRevenue += pricePerUnit * marginalProduction;
            //but now decrease it if you caused the price to change
            marginalRevenue -= (owner.getSalesDepartment(output).getLastClosingPrice()-
                    pricePerUnit) * p.hypotheticalThroughput(currentWorkers,output);


        }
        //if you are increasing production revenue should be positive or zero. If we are decreasing production then revenue will be negative
        assert (marginalRevenue >=0 && targetWorkers > currentWorkers) ^ (marginalRevenue <=0 && targetWorkers < currentWorkers);

        //FINALLY return
        return marginalRevenue - totalMarginalCosts;



    }


    /**
     * Simply checks profit in the 2 directions around the currentWorkerTarget. Choose the highest of the three
     *
     * @param currentWorkerTarget what is the current worker target
     * @param newProfits          what are the new profits
     * @param oldWorkerTarget     what was the target last time we changed them
     * @param oldProfits          what were the profits back then
     * @return the new worker targets. Any negative number means to check again!
     */
    @Override
    protected int chooseWorkerTarget(int currentWorkerTarget, float newProfits, int oldWorkerTarget, float oldProfits)
    {

        //compute profits if we increase

        try{
            float profitsIfWeIncrease = currentWorkerTarget < p.maximumWorkersPossible() ? //if we can increase production
                    computeMarginalProfits(currentWorkerTarget,currentWorkerTarget+1) //check marginal profits
                    :
                    Float.NEGATIVE_INFINITY; //otherwise don't go there!


            //compute profits if we decrease
             float profitsIfWeDecrease = currentWorkerTarget > p.minimumWorkersNeeded() ? //can we decrease production?
                    computeMarginalProfits(currentWorkerTarget, currentWorkerTarget-1) //if so check marginal profits
                    :
                    Float.NEGATIVE_INFINITY; //otherwise ignore this choice!

            if(profitsIfWeDecrease < 0 && profitsIfWeIncrease < 0)

                //if profits decrease in both direction, stay where you are
                return currentWorkerTarget;
            else
            if(profitsIfWeIncrease >= profitsIfWeDecrease){ //if we increase profits going up, let's do that
                assert profitsIfWeIncrease >= 0;
                return currentWorkerTarget+1;
            }
            else
            {
                assert profitsIfWeDecrease >0;
                return currentWorkerTarget-1;
            }

        }catch (DelayException e)
        {
            //if an exception was thrown, it must be because we need more time!
            return -1;
        }


    }

    /**
     * what happens if the predictor finds it impossible to give a price prediction?
     */
    public enum RandomizationPolicy{
        /**
         * Randomly choose a number from 0 to 100
         */
        RANDOM,
        /**
         * Checks the last closing price of the market, if it's -1 randomly choose a number from 0 to 100
         */
        MARKET_THEN_RANDOM,

        /**
         * Ask for more time
         */
        MORE_TIME;

        /**
         *
         * ask the randomization policy what to do when a prediction doesn't exist
         * @param market  the market where prediction could not be found
         * @param random a randomizer
         * @return a replacement for the missing prediction
         * @throws Exception an exception is thrown when the policy is to ask for more time
         */
        public long replaceUnknownPrediction(Market market, MersenneTwisterFast random) throws DelayException {

            switch (this){
                default:
                case RANDOM:
                    return random.nextLong(100); //return a random price
                case MARKET_THEN_RANDOM:
                    long marketLastClosingPrice = market.getLastPrice();
                    if(marketLastClosingPrice > 0 ) //if there is a last closing price return that one
                        return marketLastClosingPrice;
                    else
                        return random.nextLong(100); //otherwise return a random price
                case MORE_TIME:
                    throw new DelayException("We need more time to make predictions!");



            }

        }

    }

    public RandomizationPolicy getPolicy() {
        return policy;
    }

    public void setPolicy(RandomizationPolicy policy) {
        this.policy = policy;
    }


}
