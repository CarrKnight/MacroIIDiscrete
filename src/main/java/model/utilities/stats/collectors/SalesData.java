/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.collectors;

import agents.firm.sales.SalesDepartment;
import com.google.common.base.Preconditions;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.stats.collectors.enums.SalesDataType;
import sim.engine.SimState;


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
 * @author carrknight
 * @version 2013-08-27
 * @see
 */
public class SalesData extends DataStorage<SalesDataType> {


    /**
     * the department we are documenting
     */
    private SalesDepartment departmentToFollow = null;


    /**
     * creates an empty purchase department data gatherer. It starts collecting at start!
     */
    public SalesData() {
        super(SalesDataType.class);
    }



    /**
     * called when the data gathering is supposed to start. It schedules itself to start at next CLEANUP phase
     */
    public void start( MacroII state,  SalesDepartment departmentToFollow) {
        if(!isActive())
            return;

        Preconditions.checkState(this.departmentToFollow == null, " can't start the gatherer twice!");

        //schedule yourself
        this.departmentToFollow = departmentToFollow;
        //we are going to set the starting day at -1 and then change it at our first step()
        setStartingDay(-1);

        state.scheduleSoon(ActionOrder.CLEANUP_DATA_GATHERING,this);
    }


    @Override
    public void step(SimState state) {
        if(!isActive())
            return;

        //make sure it's the right time
        assert state instanceof MacroII;
        MacroII model = (MacroII) state;
        assert model.getCurrentPhase().equals(ActionOrder.CLEANUP_DATA_GATHERING);
        assert  (this.departmentToFollow)!=null;


        if(getStartingDay()==-1)
            setCorrectStartingDate(model);

        assert getStartingDay() >=0;



        //learn
        Double inflow = (double) departmentToFollow.getTodayInflow();
        Double outflow = (double) departmentToFollow.getTodayOutflow();
        Double closingPrices = (double) departmentToFollow.getLastClosingPrice();
        Double howManyToSell = (double) departmentToFollow.getHowManyToSell();
        Double workersProducing = (double) departmentToFollow.getTotalWorkersWhoProduceThisGood();
        Double averageClosingPrice = (double) departmentToFollow.getAverageClosingPrice();
        Double lastAskedPrice = (double) departmentToFollow.getLastAskedPrice();
        Double supplyGap = (double) departmentToFollow.estimateSupplyGap();
        Double predictedSlope = (double) (departmentToFollow.predictSalePriceAfterIncreasingProduction(0, 1) - departmentToFollow.predictSalePriceWhenNotChangingPoduction());

        //memorize
        data.get(SalesDataType.INFLOW).add(inflow);
        data.get(SalesDataType.OUTFLOW).add(outflow);
        data.get(SalesDataType.CLOSING_PRICES).add(closingPrices);
        data.get(SalesDataType.HOW_MANY_TO_SELL).add(howManyToSell);
        data.get(SalesDataType.WORKERS_PRODUCING_THIS_GOOD).add(workersProducing);
        data.get(SalesDataType.AVERAGE_CLOSING_PRICES).add(averageClosingPrice);
        data.get(SalesDataType.LAST_ASKED_PRICE).add(lastAskedPrice);
        data.get(SalesDataType.PREDICTED_DEMAND_SLOPE).add(predictedSlope);

        data.get(SalesDataType.SUPPLY_GAP).add(supplyGap);

        //reschedule
        model.scheduleTomorrow(ActionOrder.CLEANUP_DATA_GATHERING, this);



    }

    private void setCorrectStartingDate(MacroII model) {
        setStartingDay((int) Math.round(model.getMainScheduleTime()));

        for(DailyObservations obs : data.values())
            obs.setStartingDay(getStartingDay());
    }

    @Override
    public void turnOff() {
        super.turnOff();
        departmentToFollow = null;


    }


}
