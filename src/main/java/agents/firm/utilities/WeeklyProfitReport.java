/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.utilities;

import agents.firm.Firm;
import agents.firm.production.Plant;
import agents.firm.sales.SalesDepartment;
import financial.MarketEvents;
import goods.GoodType;

import java.util.*;

/**
 * <h4>Description</h4>
 * <p/> An object that computes the revenuesPerPlant/profits for accounting sakes
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-09-04
 * @see
 */
public class WeeklyProfitReport implements ProfitReport {


    Firm firm;

    /**
     * List of plants producing a specific input
     */
    // Multimap<GoodType, Plant> producers;

    /**
     * For each good type what was? the total production last week
     */
    private int[] totalProduction;


    /**
     *  maps to each plant the profits it made
     */
    private Map<Plant,Float> profitsPerPlant;
    /**
     * maps to each plant the revenue associated with it
     */
    private Map<Plant,Float> revenuesPerPlant;

    /**
     * maps to each plant the costs associated with it as they appear in the profitsPerPlant
     */
    private Map<Plant,Float> costsPerPlant;



    /**
     *  maps to each plant its fixed costs
     */
    private Map<Plant,Long> ammortizedFixedCosts;

    private  long aggregateProfits;









    public WeeklyProfitReport(Firm firm) {
        this.firm = firm;
        //instantiate stuff
        //    producers = HashMultimap.create();
        totalProduction = new int[GoodType.values().length];
        profitsPerPlant = new HashMap<>();
        revenuesPerPlant = new HashMap<>();
        costsPerPlant = new HashMap<>();
        ammortizedFixedCosts = new HashMap<>();

    }

    @Override
    public void weekEnd()
    {
        //reset the counters!
        aggregateProfits = 0;
        profitsPerPlant.clear(); revenuesPerPlant.clear(); costsPerPlant.clear();

        ammortizedFixedCosts.clear();
        totalProduction = new int[GoodType.values().length];

        //check total production
        for(Plant p : firm.getPlants())
        {
            //get the outputs
            Set<GoodType> outputs = p.getBlueprint().getOutputs().keySet();
            for (GoodType output: outputs) {

                //count it among the producers
                //          producers.put(output,p);
                assert  p.getBlueprint().getOutputs().get(output) > 0; //you are producing, right?
                //how much did this plant completeProductionRunNow this week
                //add it to total production
                totalProduction[output.ordinal()] +=  p.getLastWeekThroughput()[output.ordinal()];

            }


        }


        //find gross margins for each salesDepartment
        Set<Map.Entry<GoodType,SalesDepartment>> sales = firm.getSalesDepartments().entrySet();
        //here we store gross margins for each goodType
        Map<GoodType, Long> grossMargins = new EnumMap<>(GoodType.class);
        for(Map.Entry<GoodType, SalesDepartment> dept : sales)
            grossMargins.put(dept.getKey(), dept.getValue().getLastWeekMargin());
        //same for revenue
        Map<GoodType, Long> totalSales = new EnumMap<>(GoodType.class);
        for(Map.Entry<GoodType, SalesDepartment> dept : sales)
            totalSales.put(dept.getKey(), dept.getValue().getLastWeekSales());
        //same for costs
        Map<GoodType, Long> cogs = new EnumMap<>(GoodType.class);
        for(Map.Entry<GoodType, SalesDepartment> dept : sales)
            cogs.put(dept.getKey(), dept.getValue().getLastWeekCostOfGoodSold());




        //go through the plants one more time
        for(Plant p : firm.getPlants())
        {

            float margin=0; //this is simple REVENUE - COSTS as recorded by the sales department
            float revenue=0; //this is just REVENUE without costs
            float cost = 0;

            //for each output
            Set<GoodType> outputs = p.getBlueprint().getOutputs().keySet();
            for (GoodType output: outputs) {

                //your share of margin is equal to the salesDepartment margin in proportion to your production
                if(totalProduction[output.ordinal()]>0)
                {
                    margin += (float)grossMargins.get(output) *
                            ((float)p.getLastWeekThroughput()[output.ordinal()]) /
                            ((float)totalProduction[output.ordinal()]);
                    revenue += (float)totalSales.get(output) *
                            ((float)p.getLastWeekThroughput()[output.ordinal()]) /
                            ((float)totalProduction[output.ordinal()]);
                    cost += (float)cogs.get(output) *
                            ((float)p.getLastWeekThroughput()[output.ordinal()]) /
                            ((float)totalProduction[output.ordinal()]);
                }
                else
                {
                    assert  totalProduction[output.ordinal()]==0;
                    margin +=0;
                }


            }

            float profits = margin;
            //add wage costs
            float wageCosts = firm.getHR(p).getWagesPaid();
            assert wageCosts > 0 || p.getNumberOfWorkers() == 0 || p.getWorkers().get(0).getMinimumDailyWagesRequired() == 0: wageCosts + " --- " + p.getNumberOfWorkers();


            //subtract fixed costs
            profits -= p.weeklyFixedCosts();

            revenuesPerPlant.put(p, revenue);
            costsPerPlant.put(p, cost +p.weeklyFixedCosts());
            profitsPerPlant.put(p, profits);
            ammortizedFixedCosts.put(p,p.weeklyFixedCosts());
            aggregateProfits +=profits;

        }


        firm.logEvent(this, MarketEvents.WEEKEND,firm.getModel().getCurrentSimulationTimeInMillis(),
                "production:" + Arrays.toString(totalProduction) + "\n"
                        +
                        "profits" + aggregateProfits
        );

    }




    public void turnOff(){
        totalProduction = null;
        firm = null;
        profitsPerPlant.clear();
        profitsPerPlant = null;
    }


    @Override
    public float getPlantProfits(Plant p){
        if(profitsPerPlant.size() == 0) //haven't actually had time to generate a single profit!
            return -1;
        assert firm.getPlants().contains(p);
        return profitsPerPlant.get(p);
    }


    /**
     * @return  the total profits of the week
     * */
    @Override
    public long getAggregateProfits() {
        return aggregateProfits;
    }

    /**
     * Efficiency ratio is just costs/revenues, the lower the better
     * @return the efficiency ratio
     */
    @Override
    public float getEfficiencyRatio(Plant p)
    {
        return costsPerPlant.get(p)/revenuesPerPlant.get(p);
    }

    /**
     * This is just the ratio of profits to revenues
     */
    @Override
    public float getNetProfitRatio(Plant p)
    {
        return profitsPerPlant.get(p)/revenuesPerPlant.get(p);

    }

    /**
     * returns the total sales attributable to this plant
     * @param p the plant
     * @return the revenues of the plant
     */
    @Override
    public float getPlantRevenues(Plant p)
    {
        if(revenuesPerPlant.size() == 0) //haven't actually had time to generate a single profit!
            return -1;
       return revenuesPerPlant.get(p);
    }

    /**
     * returns the total costs attributable to this plant
     * @param p the plant
     * @return the variable AND amortized fixed costs.
     */
    @Override
    public float getPlantCosts(Plant p){
        if(costsPerPlant.size()==0) //haven't actually had time to generate a single profit!
            return -1;
        return costsPerPlant.get(p);
    }


    /**
     * Get last recorded production for a specific good
     */
    @Override
    public int getLastWeekProduction(GoodType type)
    {
        return totalProduction[type.ordinal()];
    }
}
