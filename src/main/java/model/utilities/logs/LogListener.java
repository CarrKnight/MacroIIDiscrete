/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.logs;

/**
 * Created by carrknight on 4/30/14.
 */
public interface LogListener
{



    /**
     * get notified of a log event!
     */
    public void handleNewEvent(LogEvent logEvent);





}
