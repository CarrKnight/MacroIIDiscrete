/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
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
