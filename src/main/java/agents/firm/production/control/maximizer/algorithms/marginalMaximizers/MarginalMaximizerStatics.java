/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.production.control.maximizer.algorithms.marginalMaximizers;

import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.production.Plant;
import agents.firm.production.control.PlantControl;
import agents.firm.purchases.PurchasesDepartment;
import com.google.common.base.Preconditions;
import goods.GoodType;
import model.utilities.DelayException;

import java.util.Set;

/**
 * <h4>Description</h4>
 * <p/> These are statics used by the marginal maximizations, useful and kept here for everyone to use
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-03-04
 * @see
 */
public final class MarginalMaximizerStatics {

    private MarginalMaximizerStatics(){}

    /**
     * Computes the marginal profits of changing worker size
     * @param owner
     *@param p
     * @param policy @param currentWorkers the workers the plan currently has
     * @param targetWorkers the number of workers we want to have
     * @return the marginal profits of this change in workforce
     * @throws Exception an exception is thrown if a prediction can't be made and we require more time.
     */
    public static float computeMarginalProfits(Firm owner, Plant p, HumanResources hr, PlantControl control,
                                               MarginalMaximizer.RandomizationPolicy policy, int currentWorkers, int targetWorkers) throws DelayException {
        Preconditions.checkArgument(targetWorkers != currentWorkers, "marginal from here to here is stupid");
       // assert currentWorkers == p.getNumberOfWorkers(); //I can't think of a use where this isn't true

        //this function smells a bit because it keeps branching according to whether we are hiring or firing. But I really wanted a generic function.


        /**********************************************
         * WAGES
         *********************************************/
        CostEstimate wageCosts = computeWageCosts(hr, control, currentWorkers, targetWorkers, policy);

        /**********************************************
         * INPUTS
         *********************************************/
        CostEstimate inputCosts = computeInputCosts(owner, p, policy, currentWorkers,targetWorkers);

        /**********************************************
         * TOTAL COSTS
         *********************************************/
        //sum up everything
        long totalMarginalCosts = wageCosts.getMarginalCost()+ inputCosts.getMarginalCost();
        assert(totalMarginalCosts >= 0 && targetWorkers > currentWorkers) ^   (totalMarginalCosts <= 0 && targetWorkers < currentWorkers);
        //we are going to need the total costs to predict our future sales price
        assert  inputCosts.getTotalCost() >=0;

        /*********************************************************
         * Marginal Revenues
         ********************************************************/
        float marginalRevenue = computeMarginalRevenue(owner, p, policy, currentWorkers, targetWorkers, inputCosts.getTotalCost(), wageCosts.getTotalCost());



    /*
        System.out.println("total input: " + inputCosts.getTotalCost() + ", total wage costs: " + wageCosts.getTotalCost() + ", targetWorkers: " + targetWorkers);
        System.out.println("marginal input: " + inputCosts.getMarginalCost() + ", marginal wages:" + wageCosts.getMarginalCost() + ", revenue: " +
                marginalRevenue + " ----- workers: " + currentWorkers);
        System.out.println( "old wage costs: " + p.getLatestObservation(PlantDataType.WAGES_PAID_THAT_WEEK) + ", today wages: " + control.getCurrentWage());

        System.out.println("--------------------------------------------------------------------------------------------------------------------------------------");
      */


        //FINALLY return
        return marginalRevenue - totalMarginalCosts;



    }

    /**
     * This is simply marginal profits divided by the marginal production. Basically it is average marginal profits
     * @param owner
     *@param p
     * @param policy @param currentWorkers the workers the plan currently has
     * @param targetWorkers the number of workers we want to have
     * @return the marginal profits of this change in workforce divided by marginal production (in absolute value)
     * @throws model.utilities.DelayException
     */
    public static float computeUnitMarginalProfits(Firm owner, Plant p, HumanResources hr, PlantControl control,
                                                   MarginalMaximizer.RandomizationPolicy policy,
                                                   int currentWorkers, int targetWorkers) throws DelayException
    {

        float marginalProduction = marginalProduction(p, currentWorkers, targetWorkers);
        marginalProduction = Math.abs(marginalProduction);     //take the absolute value

        return computeMarginalProfits(owner, p, hr, control, policy, currentWorkers, targetWorkers)
                / marginalProduction;


    }

    /**
     * computes the change in production expected by the change in workers
     * @param p
     * @param currentWorkers the new number of workers
     * @param targetWorkers the target workers
     * @return marginal production
     */
    public static int marginalProduction(Plant p, int currentWorkers, int targetWorkers) {
        int marginalProduction = 0;
        Set<GoodType> outputs = p.getBlueprint().getOutputs().keySet();

        //for all the outputs
        for(GoodType output : outputs)
        {
            marginalProduction += Math.round(p.hypotheticalThroughput(targetWorkers, output) - p.hypotheticalThroughput(currentWorkers, output));
            assert (marginalProduction >= 0 && targetWorkers >= currentWorkers) ^  (marginalProduction <= 0 && targetWorkers < currentWorkers);
        }
        return marginalProduction;
    }

    /**
     * Computes the labor expenses associated with the proposed number of workers
     * @param targetWorkers the proposed number of workers
     * @param policy
     * @return the estimate of both the marginal costs and the total costs
     * @throws model.utilities.DelayException
     */
    public static CostEstimate computeWageCosts(HumanResources hr, PlantControl control, int currentWorkers,
                                                int targetWorkers, MarginalMaximizer.RandomizationPolicy policy) throws DelayException {
        long marginalWageCosts; long futureWage;

        if(targetWorkers > currentWorkers)
        {
            futureWage = hr.predictPurchasePriceWhenIncreasingProduction() ;
            //but it should never be below the minimum to keep our currentworkers
            if(currentWorkers>0 && futureWage > 0) //do this only if you aren't asking for a delay and you have at least one worker
                futureWage = Math.max(futureWage,hr.hypotheticalWageAtThisLevel(currentWorkers));
        }
        else
        {
            futureWage=hr.hypotheticalWageAtThisLevel(targetWorkers);
        }
        futureWage = futureWage < 0 ? policy.replaceUnknownPrediction(hr.getMarket(), hr.getRandom()) : futureWage;

        long oldWage = currentWorkers == 0 ? 0 : hr.hypotheticalWageAtThisLevel(currentWorkers);
      //  System.out.println("wage costs: " + futureWage + " , old wages: " + oldWage);


        long totalFutureWageCosts = futureWage * targetWorkers;

        marginalWageCosts = totalFutureWageCosts - currentWorkers * oldWage;



        return new CostEstimate(marginalWageCosts,totalFutureWageCosts);

    }

    /**
     * Computes the marginal revenue of the change in production
     * @param owner
     *@param p
     * @param policy @param currentWorkers the number of current workers
     * @param targetWorkers the number of workers targeted
     * @param totalFutureCosts the total future costs expected AFTER changing production
     * @param totalFutureWageCosts the total future wages expected after changing production
     * @return All the marginal revenues expected from the change in production
     * @throws model.utilities.DelayException
     */
    public static float computeMarginalRevenue(Firm owner, Plant p, MarginalMaximizer.RandomizationPolicy policy, int currentWorkers, int
            targetWorkers, long totalFutureCosts, long totalFutureWageCosts) throws DelayException {

        float marginalRevenue = 0;
        Set<GoodType> outputs = p.getOutputs();
        for(GoodType output : outputs)
        {
            float marginalProduction = p.hypotheticalThroughput(targetWorkers, output) - p.hypotheticalThroughput(currentWorkers, output);
            assert (marginalProduction >= 0 && targetWorkers >= currentWorkers) ^  (marginalProduction <= 0 && targetWorkers < currentWorkers) :
                    "this method was thought for monotonic production functions.";
            long oldPrice = owner.getSalesDepartment(output).predictSalePriceWhenNotChangingPoduction();
            //are we increasing or decreasing production?
            long pricePerUnit =  targetWorkers>currentWorkers ?
                    owner.getSalesDepartment(output).predictSalePriceAfterIncreasingProduction(
                            p.hypotheticalUnitOutputCost(output, totalFutureCosts, targetWorkers, totalFutureWageCosts
                            ), Math.round(marginalProduction/7f)) :
                    owner.getSalesDepartment(output).predictSalePriceAfterDecreasingProduction(
                            p.hypotheticalUnitOutputCost(output, totalFutureCosts, targetWorkers, totalFutureWageCosts),Math.round(-marginalProduction/7f) );


            pricePerUnit = pricePerUnit < 0 ? policy.replaceUnknownPrediction(owner.getSalesDepartment(output).getMarket(), p.getRandom()) : pricePerUnit;
/*            System.out.println("predicted price: " + pricePerUnit + ", averaged price: " + owner.getSalesDepartment(output).getAveragedLastPrice() +
            ",.market average:" + owner.getSalesDepartment(output).getMarket().getTodayAveragePrice() + ", old price:" + oldPrice);
  */

            marginalRevenue += p.hypotheticalThroughput(targetWorkers, output) * pricePerUnit -
                    p.hypotheticalThroughput(currentWorkers, output) * oldPrice;



        }
        return marginalRevenue;
    }

    /**
     * computes the input costs associated with change in production
     * @param owner
     *@param p
     * @param policy @param currentWorkers the current number of workers
     * @param targetWorkers the target number of workers
     * @return an object containing the marginal costs and the total costs of the new production schedule
     * @throws model.utilities.DelayException
     */
    public static CostEstimate computeInputCosts(Firm owner, Plant p, MarginalMaximizer.RandomizationPolicy policy, int currentWorkers, int targetWorkers) throws DelayException {
        Set<GoodType> inputs = p.getInputs();
        long marginalInputCosts = 0; //here we will store the costs of increasing production
        long totalInputCosts = 0; //here we will store the TOTAL input costs of increased production
        for(GoodType input : inputs)
        {
            int totalInputNeeded =  p.hypotheticalWeeklyInputNeeds(input, targetWorkers);  //total input needed
            int oldNeeds = p.hypotheticalWeeklyInputNeeds(input, currentWorkers) ;
            int marginalInputNeeded = totalInputNeeded-oldNeeds;    //marginal input needs
            assert (marginalInputNeeded>=0 && targetWorkers > currentWorkers) ^ (marginalInputNeeded<=0 && targetWorkers < currentWorkers) ; //

            PurchasesDepartment dept = owner.getPurchaseDepartment(input); //get the purchase department that buys this input
            long costPerInput = targetWorkers > currentWorkers ?
                    dept.predictPurchasePriceWhenIncreasingProduction() :
                    dept.predictPurchasePriceWhenDecreasingProduction();
            long oldCosts = dept.getLastClosingPrice();
            //if there is no prediction, react to it
            costPerInput = costPerInput < 0 ? policy.replaceUnknownPrediction(owner.getPurchaseDepartment(input).getMarket(), p.getRandom()) : costPerInput;

            //count the costs!
            totalInputCosts +=  costPerInput*totalInputNeeded;
            marginalInputCosts += costPerInput*totalInputNeeded - oldNeeds *oldCosts;


            //marginal costs are negative (marginal savings) if we are reducing production
            //       assert (marginalInputCosts >= 0 && targetWorkers > currentWorkers) ^   (marginalInputCosts <= 0 && targetWorkers < currentWorkers);
            assert totalInputCosts >= 0;
        }

        return new CostEstimate(marginalInputCosts,totalInputCosts);

    }

    /**
     * simply returns x/(1+x)
     * @param x the x
     * @return  x/(1+x)
     */
    public static float sigmoid(float x)
    {
        return x/(1+x);
    }


    /**
     * simply returns 1/(1+e^-x)-center
     * @param x the x
     * @return  1/(1+e^-(x-center))
     */
    public static float exponentialSigmoid(float x,float center)
    {
        return (float) (1f/(1+Math.exp(-(x- center)))) ;
    }
}
