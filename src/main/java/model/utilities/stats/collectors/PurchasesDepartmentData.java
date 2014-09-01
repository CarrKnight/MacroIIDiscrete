/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.collectors;

import agents.firm.personell.HumanResources;
import agents.firm.purchases.PurchasesDepartment;
import com.google.common.base.Preconditions;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.stats.collectors.enums.PurchasesDataType;
import sim.engine.SimState;


/**
 * <h4>Description</h4>
 * <p/> Similar to MarketData, but storing flows in and out of the department + last closing prices
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-08-19
 * @see
 */
public class PurchasesDepartmentData extends DataStorage<PurchasesDataType> {



    /**
     * the department we are documenting
     */
    private PurchasesDepartment departmentToFollow = null;


    /**
     * creates an empty purchase department data gatherer. It starts collecting at start!
     */
    public PurchasesDepartmentData() {
        super(PurchasesDataType.class);
    }



    /**
     * called when the data gathering is supposed to start. It schedules itself to start at next CLEANUP phase
     */
    public void start( MacroII state,  PurchasesDepartment departmentToFollow) {
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



        //memorize
        final double inflow = (double) departmentToFollow.getTodayInflow();
        data.get(PurchasesDataType.INFLOW).add(inflow);
        data.get(PurchasesDataType.OUTFLOW).add((double) departmentToFollow.getTodayOutflow());
        final int lastClosingPrice = departmentToFollow.getLastClosingPrice();
        data.get(PurchasesDataType.CLOSING_PRICES).add((double) lastClosingPrice);
        data.get(PurchasesDataType.INVENTORY).add((double) departmentToFollow.getCurrentInventory());
        data.get(PurchasesDataType.FAILURES_TO_CONSUME).add((double) departmentToFollow.getTodayFailuresToConsume());
        data.get(PurchasesDataType.WORKERS_CONSUMING_THIS_GOOD).add((double) departmentToFollow.getNumberOfWorkersWhoConsumeWhatWePurchase());
        data.get(PurchasesDataType.AVERAGE_CLOSING_PRICES).add((double) departmentToFollow.getAveragedClosingPrice());
        data.get(PurchasesDataType.LAST_OFFERED_PRICE).add((double) departmentToFollow.getLastOfferedPrice());
        data.get(PurchasesDataType.PREDICTED_SUPPLY_SLOPE).add((double) departmentToFollow.predictPurchasePriceWhenIncreasingProduction()-
                departmentToFollow.predictPurchasePriceWhenNoChangeInProduction());

        data.get(PurchasesDataType.DEMAND_GAP).add((double) departmentToFollow.estimateDemandGap());
        int workersTargeted = departmentToFollow instanceof HumanResources ? ((HumanResources) departmentToFollow).getWorkerTarget() : 0;
        data.get(PurchasesDataType.WORKERS_TARGETED).add((double) workersTargeted);


        //reschedule
        model.scheduleTomorrow(ActionOrder.CLEANUP_DATA_GATHERING, this);



    }

    @Override
    public void turnOff() {
        super.turnOff();
        departmentToFollow = null;


    }
}
