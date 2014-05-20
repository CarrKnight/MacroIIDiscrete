/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.logs;

import model.utilities.ActionOrder;

/**
 * A log-event with a time-stamp of when it happened in the model. It'll be useful for gui
 * Created by carrknight on 5/2/14.
 */
public class TimeStampedLogEvent extends  LogEvent
{
    private final int day;

    private final int phase;


    public TimeStampedLogEvent(int day, ActionOrder phase,Object source, LogLevel level, String message,
                               Object... additionalParameters) {
        super(source, level, message, additionalParameters);
        this.day = day;
        this.phase = phase.ordinal();

    }
}
