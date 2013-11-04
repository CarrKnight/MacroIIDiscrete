/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.production;

import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.Deactivatable;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.Arrays;

/**
 * <h4>Description</h4>
 * <p/> An object held by the plant, it keeps track of how much is produced every day and every week.
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
public class PlantProductionAndConsumptionCounter implements Steppable, Deactivatable
{
    private final Plant plantToLook;


    private boolean isActive = true;

    private int producedToday[] = new int[GoodType.values().length];

    private int producedYesterday[]=new int[GoodType.values().length];

    private int producedThisWeek[]=new int[GoodType.values().length];

    private int producedLastWeek[]=new int[GoodType.values().length];

    private int consumedToday[] = new int[GoodType.values().length];

    private int consumedYesterday[]=new int[GoodType.values().length];

    private int consumedThisWeek[]=new int[GoodType.values().length];

    private int consumedLastWeek[]=new int[GoodType.values().length];



    public PlantProductionAndConsumptionCounter(Plant plantToLook)
    {
        this.plantToLook = plantToLook;
        isActive = true;

        Arrays.fill(producedToday,0);
        Arrays.fill(producedYesterday,0);
        Arrays.fill(producedThisWeek,0);
        Arrays.fill(producedLastWeek,0);
        Arrays.fill(consumedToday,0);
        Arrays.fill(consumedYesterday,0);
        Arrays.fill(consumedThisWeek,0);
        Arrays.fill(consumedLastWeek,0);

    }

    /**
     * start scheduling counter resets
     */
    public void start(MacroII modelToScheduleOn)
    {
        //start scheduling yourself
        modelToScheduleOn.scheduleSoon(ActionOrder.DAWN,this);
    }


    /**
     * all the step does is to reset the daily counter
     * @param state
     */
    @Override
    public void step(SimState state) {

        if(!isActive)
            return;

        producedYesterday=producedToday;
        producedToday = new int[producedYesterday.length];
        Arrays.fill(producedToday,0);
        consumedYesterday=consumedToday;
        consumedToday = new int[consumedYesterday.length];
        Arrays.fill(consumedToday,0);

        ((MacroII)state).scheduleTomorrow(ActionOrder.DAWN,this);
    }


    /**
     * get notified of new production
     */
    public void newProduction(GoodType goodType)
    {
        producedToday[goodType.ordinal()]++;
        producedThisWeek[goodType.ordinal()]++;
    }

    /**
     * get notified of new consumption
     */
    public void newConsumption(GoodType goodType)
    {
        consumedToday[goodType.ordinal()]++;
        consumedThisWeek[goodType.ordinal()]++;
    }

    /**
     * reset weekly counter
     */
    public void weekEnd()
    {
        producedLastWeek=producedThisWeek;
        producedThisWeek = new int[producedLastWeek.length];
        Arrays.fill(producedThisWeek,0);
        consumedLastWeek=consumedThisWeek;
        consumedThisWeek = new int[consumedLastWeek.length];
        Arrays.fill(consumedThisWeek,0);
    }

    @Override
    public void turnOff() {
        isActive=false;

    }

    public int[] getProducedToday() {
        return producedToday;
    }

    public int[] getProducedYesterday() {
        return producedYesterday;
    }

    public int[] getProducedThisWeek() {
        return producedThisWeek;
    }

    public int[] getProducedLastWeek() {
        return producedLastWeek;
    }

    public int[] getConsumedToday() {
        return consumedToday;
    }

    public int[] getConsumedYesterday() {
        return consumedYesterday;
    }

    public int[] getConsumedThisWeek() {
        return consumedThisWeek;
    }

    public int[] getConsumedLastWeek() {
        return consumedLastWeek;
    }
}
