/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.logs;

import org.slf4j.Logger;

/**
 * listen to log events and dump them into a logger
 * Created by carrknight on 5/2/14.
 */
public class LoggerOutput implements LogListener{

    private final Logger output;

    public LoggerOutput(Logger output) {
        this.output = output;
    }

    @Override
    public void handleNewEvent(LogEvent logEvent) {
        LogLevel.log(output,logEvent.getLevel(),logEvent.getMessage(),logEvent.getAdditionalParameters());
    }
}
