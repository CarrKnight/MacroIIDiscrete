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
import agents.firm.sales.SalesDepartment;
import com.google.common.base.Preconditions;
import goods.GoodType;
import model.utilities.DelayException;
import model.utilities.logs.LogEvent;
import model.utilities.logs.LogLevel;
import model.utilities.stats.collectors.enums.PlantDataType;

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

        //     System.out.println(currentWorkers + "---" + targetWorkers);
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
        float totalMarginalCosts = wageCosts.getMarginalCost()+ inputCosts.getMarginalCost();
        //we are going to need the total costs to predict our future sales price
        assert  inputCosts.getTotalCost() >=0;

        /*********************************************************
         * Marginal Revenues
         ********************************************************/
        float marginalRevenue = computeMarginalRevenue(owner, p, policy, currentWorkers, targetWorkers, inputCosts.getTotalCost(), wageCosts.getTotalCost());


        hr.handleNewEvent(new LogEvent(hr, LogLevel.INFO,
                "total input: {}, total wage costs: {}, targetWorkers: {} \n marginal input: {}, marginal wages:{}, revenue: {}\n old wage costs: {}, today wages: {} ",
                inputCosts.getTotalCost() ,wageCosts.getTotalCost(), targetWorkers , inputCosts.getMarginalCost(),wageCosts.getMarginalCost(),
                marginalRevenue , currentWorkers , p.getLatestObservation(PlantDataType.WAGES_PAID_THAT_WEEK) , control.getCurrentWage() ));






        //    assert(totalMarginalCosts >= 0 && targetWorkers > currentWorkers) ^   (totalMarginalCosts <= 0 && targetWorkers < currentWorkers);



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
        float marginalWageCosts; float futureWage;

        if(targetWorkers > currentWorkers)
        {
            futureWage = hr.predictPurchasePriceWhenIncreasingProduction() ;

        }
        else
        {
            futureWage=hr.predictPurchasePriceWhenDecreasingProduction();
        }
        futureWage = futureWage < 0 ? policy.replaceUnknownPrediction(hr.getMarket(), hr.getRandom()) : futureWage;
        //todo I don't know about this
        //long oldWage = currentWorkers == 0 ? 0 : control.getCurrentWage();
        float oldWage = currentWorkers == 0 ? 0 : hr.predictPurchasePriceWhenNoChangeInProduction();

        hr.handleNewEvent(new LogEvent(hr, LogLevel.INFO,
                    "predicte wages: {}, predicted current wage: {}, actual old wage: {}, total labor: {}",
                futureWage,oldWage,control.getCurrentWage(),hr.getMarket().getYesterdayVolume()));


        float totalFutureWageCosts = futureWage * targetWorkers;

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
            targetWorkers, float totalFutureCosts, float totalFutureWageCosts) throws DelayException {

        float marginalRevenue = 0;
        Set<GoodType> outputs = p.getOutputs();
        for(GoodType output : outputs)
        {
            float marginalProduction = (p.hypotheticalThroughput(targetWorkers, output) - p.hypotheticalThroughput(currentWorkers, output));
            assert (marginalProduction >= 0 && targetWorkers >= currentWorkers) ^  (marginalProduction <= 0 && targetWorkers < currentWorkers) :
                    "this method was thought for monotonic production functions.";
            final SalesDepartment department = owner.getSalesDepartment(output);
            float oldPrice = department.predictSalePriceWhenNotChangingPoduction();
            oldPrice = oldPrice < 0 ? policy.replaceUnknownPrediction(department.getMarket(), p.getRandom()) : oldPrice;

            //are we increasing or decreasing production?
            float pricePerUnit =  targetWorkers>currentWorkers ?
                    department.predictSalePriceAfterIncreasingProduction(
                            p.hypotheticalUnitOutputCost(output, Math.round(totalFutureCosts), targetWorkers, Math.round(totalFutureWageCosts)
                            ), Math.round(marginalProduction / 7f)) :
                    department.predictSalePriceAfterDecreasingProduction(
                            p.hypotheticalUnitOutputCost(output, Math.round(totalFutureCosts), targetWorkers, Math.round(totalFutureWageCosts)), Math.round(-marginalProduction / 7f));


            pricePerUnit = pricePerUnit < 0 ? policy.replaceUnknownPrediction(department.getMarket(), p.getRandom()) : pricePerUnit;

            department.handleNewEvent(new LogEvent(department, LogLevel.INFO,
                    "today workers: {}, tomorrow workers:{}, predicted future price: {}, predicted  current price: {}, actual price: {}",
                    currentWorkers,targetWorkers,pricePerUnit,oldPrice,department.getLastClosingPrice()));



            marginalRevenue += (p.hypotheticalThroughput(targetWorkers, output) * pricePerUnit -
                    p.hypotheticalThroughput(currentWorkers, output) * oldPrice)/7f;



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
        float marginalInputCosts = 0; //here we will store the costs of increasing production
        float totalInputCosts = 0; //here we will store the TOTAL input costs of increased production
        for(GoodType input : inputs)
        {
            float totalInputNeeded =  p.hypotheticalWeeklyInputNeeds(input, targetWorkers);  //total input needed
            float oldNeeds = p.hypotheticalWeeklyInputNeeds(input, currentWorkers) ;
            float marginalInputNeeded = totalInputNeeded-oldNeeds;    //marginal input needs
            assert (marginalInputNeeded>=0 && targetWorkers > currentWorkers) ^ (marginalInputNeeded<=0 && targetWorkers < currentWorkers) ; //

            PurchasesDepartment dept = owner.getPurchaseDepartment(input); //get the purchase department that buys this input
            float costPerInput = targetWorkers > currentWorkers ?
                    dept.predictPurchasePriceWhenIncreasingProduction() :
                    dept.predictPurchasePriceWhenDecreasingProduction();
            float oldCosts = dept.predictPurchasePriceWhenNoChangeInProduction();
            //if there is no prediction, react to it
            costPerInput = costPerInput < 0 ? policy.replaceUnknownPrediction(owner.getPurchaseDepartment(input).getMarket(), p.getRandom()) : costPerInput;

            //count the costs!
            totalInputCosts +=  (costPerInput*totalInputNeeded);
            marginalInputCosts += (costPerInput*totalInputNeeded - oldCosts*oldNeeds);

            dept.handleNewEvent(new LogEvent(dept, LogLevel.INFO,
                    "today workers: {}, tomorrow workers:{}, predicte inputCost: {}, predicted current costs: {}, actual old costs: {}, total input: {}",
                    currentWorkers,targetWorkers,costPerInput,oldCosts,dept.getAveragedClosingPrice(),totalInputNeeded));


            //marginal costs are negative (marginal savings) if we are reducing production
            //       assert (marginalInputCosts >= 0 && targetWorkers > currentWorkers) ^   (marginalInputCosts <= 0 && targetWorkers < currentWorkers);
            assert totalInputCosts >= 0;
        }

        return new CostEstimate(marginalInputCosts/7f,totalInputCosts/7f);

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
