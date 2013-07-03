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
        assert currentWorkers == p.workerSize(); //I can't think of a use where this isn't true

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


    /*    if(targetWorkers > currentWorkers)
        {
            System.out.println("total input: " + inputCosts.getTotalCost() + ", total wage costs: " + wageCosts.getTotalCost());
            System.out.println("marginal input: " + inputCosts.getMarginalCost() + ", marginal wages:" + wageCosts.getMarginalCost() + ", revenue: " +
                    marginalRevenue + " ----- workers: " + currentWorkers);
        }
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

        if(targetWorkers > currentWorkers)  //if we are going to hire somebody
        {
            //wages!
            futureWage = hr.predictPurchasePrice();
            //if there is no prediction react to it
            futureWage = futureWage < 0 ? policy.replaceUnknownPrediction(hr.getMarket(), hr.getRandom()) : futureWage;
            //remember that you'll have to raise everybody's wages if you hire somebody new at a higher cost
            if(futureWage > control.getCurrentWage() && hr.isFixedPayStructure())  //if you have to raise your wages to hire somebody
            {
                //your total costs then are the new wage + the adjustment you'll have to make to pay everybody that wage
                marginalWageCosts = ( (futureWage- control.getCurrentWage()) * currentWorkers) + futureWage * (targetWorkers-currentWorkers);
                assert marginalWageCosts > 0 || futureWage == 0;
            }
            else{
                marginalWageCosts = futureWage * (targetWorkers-currentWorkers); //otherwise you just need to hire the new guy
            }
        }
        else{
            //we are firing people, what wage would ensue then?
            futureWage = hr.hypotheticalWageAtThisLevel(targetWorkers);
            assert futureWage <= control.getCurrentWage() : "firing people will result in higher wage, that's weird";
            //make a list of workers

            marginalWageCosts = futureWage * targetWorkers -  control.getCurrentWage() * currentWorkers; //get marginal cost!
            assert marginalWageCosts <=0;    //cost should be negative! we are saving!
        }
        long totalFutureWageCosts = futureWage * targetWorkers;

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
            //if you are increasing production, predict future price. Otherwise get last price
            long pricePerUnit = targetWorkers > currentWorkers ?
                    owner.getSalesDepartment(output).predictSalePrice(p.hypotheticalUnitOutputCost(output, totalFutureCosts, targetWorkers, totalFutureWageCosts)) :
                    owner.getSalesDepartment(output).getLastClosingPrice();
            //if prediction is not available, react to it!
            pricePerUnit = pricePerUnit < 0 ? policy.replaceUnknownPrediction(owner.getSalesDepartment(output).getMarket(), p.getRandom()) : pricePerUnit;
            //add it to the revenue
            marginalRevenue += pricePerUnit * marginalProduction;
            //but now decrease it if you caused the price to change
            //if you sold anything today (if you haven't and you use very old "closing price" then your estimates are very wrong
            if(owner.getSalesDepartment(output).getTodayOutflow() > 0
                    &&
                    owner.getSalesDepartment(output).getLastClosingPrice() > pricePerUnit )
            {

                marginalRevenue -= (owner.getSalesDepartment(output).getLastClosingPrice()-
                        pricePerUnit) * p.hypotheticalThroughput(currentWorkers, output);
            }

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
            int marginalInputNeeded = totalInputNeeded-  p.hypotheticalWeeklyInputNeeds(input, currentWorkers) ;  //marginal input needs
            assert (marginalInputNeeded>=0 && targetWorkers > currentWorkers) ^ (marginalInputNeeded<=0 && targetWorkers < currentWorkers) ; //

            PurchasesDepartment dept = owner.getPurchaseDepartment(input); //get the purchase department that buys this input
            long costPerInput = targetWorkers > currentWorkers ? dept.predictPurchasePrice() : dept.getLastClosingPrice();
            //if we are increasing production, predict. if we are decreasing production use old prices
            //if there is no prediction, react to it
            costPerInput = costPerInput < 0 ? policy.replaceUnknownPrediction(owner.getPurchaseDepartment(input).getMarket(), p.getRandom()) : costPerInput;

            //count the costs!
            if(costPerInput == 0)
                marginalInputCosts+= 0 ;
            else
            if(costPerInput < dept.getLastClosingPrice()) //if price is decreasing weirdly
            {                                                                           //then assumes only the new stuff you buy will be discounted
                marginalInputCosts+= (costPerInput * marginalInputNeeded);
                totalInputCosts +=  costPerInput*marginalInputNeeded + dept.getLastClosingPrice() * ( totalInputNeeded-marginalInputNeeded) ;

            }
            else
            {
                marginalInputCosts+= (costPerInput * totalInputNeeded) - dept.getLastClosingPrice() *
                        p.hypotheticalWeeklyInputNeeds(input, currentWorkers) ;
                totalInputCosts +=  costPerInput*totalInputNeeded;

            }

            //marginal costs are negative (marginal savings) if we are reducing production
            assert (marginalInputCosts >= 0 && targetWorkers > currentWorkers) ^   (marginalInputCosts <= 0 && targetWorkers < currentWorkers);
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
