package agents.firm;

import agents.firm.production.Plant;
import agents.firm.sales.SalesDepartment;
import financial.MarketEvents;
import goods.GoodType;

import java.util.*;

/**
 * <h4>Description</h4>
 * <p/> An object that computes the revenues/profits for accounting sakes
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
public class ProfitReport {


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
    private Map<Plant,Float> profitsMade;


    /**
     *  maps to each plant its fixed costs
     */
    private Map<Plant,Long> ammortizedFixedCosts;

    private  long aggregateProfits;









    public ProfitReport(Firm firm) {
        this.firm = firm;
        //instantiate stuff
        //    producers = HashMultimap.create();
        totalProduction = new int[GoodType.values().length];
        profitsMade = new HashMap<>();
        ammortizedFixedCosts = new HashMap<>();

    }

    public void weekEnd()
    {
        //reset the counters!
        aggregateProfits = 0;
        profitsMade.clear();
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



        //go through the plants one more time
        for(Plant p : firm.getPlants())
        {

            float margin=0; //this is simple REVENUE - COSTS as recorded by the sales department
            float revenue=0; //this is just REVENUE without costs

            //for each output
            Set<GoodType> outputs = p.getBlueprint().getOutputs().keySet();
            for (GoodType output: outputs) {

                //your share of margin is equal to the salesDepartment margin in proportion to your production
                if(totalProduction[output.ordinal()]>0)
                {
                    margin += (float)grossMargins.get(output) *
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
            assert wageCosts > 0 || p.workerSize() == 0 || p.getWorkers().get(0).getMinimumWageRequired() == 0: wageCosts + " --- " + p.workerSize();


            //subtract fixed costs
            profits -= p.weeklyFixedCosts();


            profitsMade.put(p,profits);
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
        profitsMade.clear();
        profitsMade = null;
    }


    public float getPlantProfits(Plant p){
        assert firm.getPlants().contains(p);
        return profitsMade.get(p);
    }


    /**
     * @return  the total profits of the week
     * */
    public long getAggregateProfits() {
        return aggregateProfits;
    }


    /**
     * Get last recorded production for a specific good
     */
    public int getLastWeekProduction(GoodType type)
    {
        return totalProduction[type.ordinal()];
    }
}
