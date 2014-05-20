/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.pricing;

import model.utilities.logs.LogEvent;
import model.utilities.logs.LogListener;
import model.utilities.logs.LogNodeSimple;
import model.utilities.logs.Loggable;

/**
 * An abstract base class for ask pricing. Only implements the logger
 * Created by carrknight on 5/1/14.
 */
public abstract class BaseAskPricingStrategy implements AskPricingStrategy
{


    @Override
    public void turnOff() {
        logNode.turnOff();
    }



    /***
     *       __
     *      / /  ___   __ _ ___
     *     / /  / _ \ / _` / __|
     *    / /__| (_) | (_| \__ \
     *    \____/\___/ \__, |___/
     *                |___/
     */

    /**
     * simple lognode we delegate all loggings to.
     */
    private final LogNodeSimple logNode = new LogNodeSimple();

    @Override
    public boolean addLogEventListener(LogListener toAdd) {
        return logNode.addLogEventListener(toAdd);
    }

    @Override
    public boolean removeLogEventListener(LogListener toRemove) {
        return logNode.removeLogEventListener(toRemove);
    }

    @Override
    public void handleNewEvent(LogEvent logEvent)
    {
        logNode.handleNewEvent(logEvent);
    }

    @Override
    public boolean stopListeningTo(Loggable branch) {
        return logNode.stopListeningTo(branch);
    }

    @Override
    public boolean listenTo(Loggable branch) {
        return logNode.listenTo(branch);
    }

    

}
