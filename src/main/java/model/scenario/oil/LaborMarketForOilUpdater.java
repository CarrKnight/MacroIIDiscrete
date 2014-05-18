/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.scenario.oil;

import agents.EconomicAgent;
import agents.people.Person;
import com.google.common.base.Preconditions;
import financial.market.Market;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ObservableIntegerValue;
import model.MacroII;
import model.scenario.MonopolistScenario;
import model.utilities.ActionOrder;
import model.utilities.Deactivatable;

import java.util.HashSet;
import java.util.Set;

/**
 * This is basically a change listener that, whenever there is any change in a parameter schedules at dawn
 * the destruction of the old workersand recreation of a new labor supply
 * Created by carrknight on 4/21/14.
 */
public class LaborMarketForOilUpdater implements InvalidationListener, Deactivatable
{

    /**
     * What if the user keeps changing the slider BEFORE the change in market happens?
     * it's useless to schedule a million changes, so this forces to make it only once a day
     */
    private boolean locked = false;

    private final ObservableIntegerValue laborSupplySlope;

    private final ObservableIntegerValue laborSupplyIntercept;

    private final ObservableIntegerValue totalNumberOfWorkers;

    private final Market laborMarket;

    private final MacroII model;


    /**
     * creates the listener (and automatically starts listening to them!)
     * @param laborSupplySlope the slope of the curve
     * @param laborSupplyIntercept the intercept of the curve
     * @param totalNumberOfWorkers the total number of workers to build
     * @param model a reference to the model, to register as deactivable and schedule updates
     */
    public LaborMarketForOilUpdater(ObservableIntegerValue laborSupplySlope,
                                    ObservableIntegerValue laborSupplyIntercept,
                                    ObservableIntegerValue totalNumberOfWorkers,
                                    MacroII model, Market laborMarket) {
        this.laborSupplySlope = laborSupplySlope;
        this.laborSupplyIntercept = laborSupplyIntercept;
        this.totalNumberOfWorkers = totalNumberOfWorkers;
        this.laborMarket = laborMarket;
        Preconditions.checkArgument(laborMarket.getGoodType().isLabor());

        //start listening to these
        this.laborSupplySlope.addListener(this);
        this.laborSupplyIntercept.addListener(this);
        this.totalNumberOfWorkers.addListener(this);

        //remember the model reference
        this.model = model;
        model.registerDeactivable(this);


    }


    /**
     * whenever a parameter changes, prepare yourself to update the model
     * @param ignored ignored
     */
    @Override
    public void invalidated(Observable ignored) {

        if(locked == false)
        {
            locked = true;
            model.scheduleSoon(ActionOrder.DAWN, state -> {
                locked = false;
                //turn off all registered sellers that are workers persons
                final Set<EconomicAgent> sellersCopy = new HashSet<>(laborMarket.getSellers()); //i copy the sellers set because it will be accessed by
                //the turn off method of Person
                sellersCopy.stream().filter(a -> a instanceof Person).forEach(a -> a.turnOff());
                //now regenerate!
                MonopolistScenario.fillLaborSupply(laborSupplyIntercept.get(),laborSupplySlope.get(),
                        true,false,totalNumberOfWorkers.get(),laborMarket,model);
            });
        }

    }

    /**
     * simply stops listening
     */
    @Override
    public void turnOff() {
        laborSupplySlope.removeListener(this);
        laborSupplyIntercept.removeListener(this);
        totalNumberOfWorkers.removeListener(this);

    }
}
