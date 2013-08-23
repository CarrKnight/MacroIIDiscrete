/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.collectors.enums;

import agents.firm.Firm;
import agents.firm.production.Plant;
import com.google.common.base.Preconditions;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.stats.collectors.DailyObservations;
import model.utilities.stats.collectors.DataStorage;
import sim.engine.SimState;

import javax.annotation.Nonnull;

/**
 * <h4>Description</h4>
 * <p/> An extension to the DataStorage class to store plant specific data
 * <p/> It is only slightly different from the other DataStorage because it takes some information straight from Firm rather than the plant.
 * Also, while the profits are computed weekly they are queried and stored daily.
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-08-22
 * @see
 */
public class PlantData extends DataStorage<PlantDataType> {

    /**
     * when it is set to off, it stops rescheduling itself!
     */
    private boolean active = true;

    /**
     * the firm owning the plant we are documenting
     */
    private Firm plantOwner = null;

    /**
     * this says when was the last meaningful change of workforce (sometimes we hire and fire a guy the same day, but this wouldn't show up in the data here)
     */
    private int lastDayAMeaningfulChangeInWorkforceOccurred = -1;

    /**
     * the firm owning the plant we are documenting
     */
    private Plant plant = null;


    public PlantData() {
        super(PlantDataType.class);
    }

    /**
     * called when the data gathering is supposed to start. It schedules itself to start at next CLEANUP phase
     */
    public void start(@Nonnull MacroII state,@Nonnull Plant plant, @Nonnull Firm plantOwner) {
        if(!active)
            return;

        Preconditions.checkState(this.plantOwner == null, " can't start the gatherer twice!");

        //schedule yourself
        this.plantOwner = plantOwner;
        this.plant = plant;
        //we are going to set the starting day at -1 and then change it at our first step()
        setStartingDay(-1);

        state.scheduleSoon(ActionOrder.CLEANUP_DATA_GATHERING,this);
    }

    /**
     * called when the data gathering is supposed to start. It schedules itself to start at next CLEANUP phase. It grabs the Firm
     * reference from getOwner() of the plant
     */
    public void start(@Nonnull MacroII state,@Nonnull Plant plant) {
        this.start(state,plant,plant.getOwner());

    }



    @Override
    public void step(SimState state) {
        if(!active)
            return;

        //make sure it's the right time
        assert state instanceof MacroII;
        MacroII model = (MacroII) state;
        assert model.getCurrentPhase().equals(ActionOrder.CLEANUP_DATA_GATHERING);
        assert  (this.plantOwner)!=null;


        if(getStartingDay()==-1)
            setCorrectStartingDate(model);

        assert getStartingDay() >=0;


        //memorize
        data.get(PlantDataType.PROFITS_THAT_WEEK).add(Double.valueOf(plantOwner.getPlantProfits(plant)));
        data.get(PlantDataType.REVENUES_THAT_WEEK).add(Double.valueOf(plantOwner.getPlantRevenues(plant)));
        data.get(PlantDataType.COSTS_THAT_WEEK).add(Double.valueOf(plantOwner.getPlantCosts(plant)));
        int numberOfWorkers = plant.getNumberOfWorkers();
        //before adding it, check if it's different!
        if(data.get(PlantDataType.TOTAL_WORKERS).size()>0
                && ((int)Math.round(data.get(PlantDataType.TOTAL_WORKERS).getLastObservation())) != numberOfWorkers)
            lastDayAMeaningfulChangeInWorkforceOccurred = (int)model.getMainScheduleTime();
        data.get(PlantDataType.TOTAL_WORKERS).add(Double.valueOf(numberOfWorkers));


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
        active = false;
    }

    public int getLastDayAMeaningfulChangeInWorkforceOccurred() {
        return lastDayAMeaningfulChangeInWorkforceOccurred;
    }
}
