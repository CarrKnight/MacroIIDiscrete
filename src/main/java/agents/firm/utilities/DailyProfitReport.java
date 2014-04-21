/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.utilities;

import agents.firm.Firm;
import agents.firm.production.Plant;
import goods.GoodType;
import model.utilities.stats.collectors.enums.PurchasesDataType;
import model.utilities.stats.collectors.enums.SalesDataType;

/**
 * <h4>Description</h4>
 * <p/> On the fly computes revenues, costs and profits of plants
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-11-03
 * @see
 */
public class DailyProfitReport implements ProfitReport
{


    final private Firm firm;

    @Override
    public float getPlantRevenues(Plant p) {

        double totalRevenue=0;

        //for all stuff that is produced
        for(GoodType type : p.getOutputs())
        {
            if(p.numberOfProductionObservations()==0)
                continue;
            //get how much was produced by the plant
            int producedByPlant = p.getProducedYesterday(type);
            if(producedByPlant == 0)
                continue;

            double price = firm.
                    getSalesDepartment(type).getLatestObservation(SalesDataType.AVERAGE_CLOSING_PRICES);

            totalRevenue += price * producedByPlant;


        }

        return (float) totalRevenue;

    }

    @Override
    public float getPlantProfits(Plant p) {

        return getPlantRevenues(p) - getPlantCosts(p);

    }

    @Override
    public void weekEnd() {
        //no need

    }

    @Override
    public float getEfficiencyRatio(Plant p) {
        return getPlantRevenues(p)/getPlantCosts(p);

    }

    @Override
    public float getNetProfitRatio(Plant p) {
        return getPlantProfits(p)/getPlantRevenues(p);
    }

    @Override
    public void turnOff() {
        //no need

    }

    @Override
    public long getAggregateProfits() {
        float profits = 0;
        for(Plant p : firm.getPlants())
            profits += this.getPlantProfits(p);

        return Math.round(profits);
    }

    @Override
    public float getPlantCosts(Plant p)
    {
        //wage costs
        double wages = p.getHr().getWagesPaidLastProductionPhase();

        //input costs
        double totalCosts=0;

        //for all stuff that is produced
        for(GoodType type : p.getInputs())
        {
            if(type.isLabor() || p.numberOfProductionObservations() == 0)
                continue;
            //get how much was produced by the plant
            int consumedByPlant = p.getConsumedYesterday(type);
            if(consumedByPlant == 0)
                continue;

            double price = firm.
                    getPurchaseDepartment(type).
                    getLatestObservation(PurchasesDataType.AVERAGE_CLOSING_PRICES);

            totalCosts += price * consumedByPlant;


        }

        return (float)(totalCosts + wages );
    }

    @Override
    public int getLastWeekProduction(GoodType type) {
        int dayNow = (int)Math.round(firm.getModel().getMainScheduleTime());
        if(dayNow < 7)
            return 0;
        else
        {
            int productionLastWeek = 0;

            for(Plant p : firm.getPlants())
            {
                if(p.numberOfProductionObservations() >=7)
                    productionLastWeek += p.getProductionObservationRecordedThisDay(type,dayNow-7);

            }
         return productionLastWeek;
        }


    }

    public DailyProfitReport(Firm firm) {
        this.firm = firm;
    }
}
