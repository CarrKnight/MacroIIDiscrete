/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.collectors;

import agents.firm.production.Plant;
import com.google.common.base.Preconditions;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import sim.engine.SimState;


/**
 * <h4>Description</h4>
 * <p/> This is also a plant data, but it deals exclusively with how much was produced for each goodtype,
 * hence its separatedness
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-11-02
 * @see
 */
public class ProductionData extends DataStorage<GoodType>
{

    /**
     * when it is set to off, it stops rescheduling itself!
     */
    protected boolean active = true;

    /**
     * the firm owning the plant we are documenting
     */
    protected Plant plant = null;


    public ProductionData() {
        super(GoodType.class);
    }


    /**
     * called when the data gathering is supposed to start. It schedules itself to start at next CLEANUP phase. It grabs the Firm
     * reference from getOwner() of the plant
     */
    public void start( MacroII state, Plant plant) {
        this.plant = plant;
        //we are going to set the starting day at -1 and then change it at our first step()
        setStartingDay(-1);

        state.scheduleSoon(ActionOrder.CLEANUP_DATA_GATHERING,this);
    }



    @Override
    public void turnOff() {
        active = false;
    }

    @Override
    public void step(SimState state) {
        Preconditions.checkState(plant != null);
        if(!active)
            return;

        //make sure it's the right time
        assert state instanceof MacroII;
        MacroII model = (MacroII) state;
        assert model.getCurrentPhase().equals(ActionOrder.CLEANUP_DATA_GATHERING);
        //set starting day if needed
        if(getStartingDay()==-1)
            setCorrectStartingDate(model);
        assert getStartingDay() >=0;


        //memorize
        //grab the production vector
        for(GoodType type : GoodType.values())  //record all production
            data.get(type).add((double) plant.getProducedToday(type));

        //reschedule
        model.scheduleTomorrow(ActionOrder.CLEANUP_DATA_GATHERING, this);


    }

    protected void setCorrectStartingDate(MacroII model) {
        setStartingDay((int) Math.round(model.getMainScheduleTime()));

        for(DailyObservations obs : data.values())
            obs.setStartingDay(getStartingDay());
    }


}
