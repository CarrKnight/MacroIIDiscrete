/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.logs;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import model.MacroII;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Appends date and phase to each log line. Done poorly. Very slow.
 * Created by carrknight on 5/2/14.
 */
public class LoggerOutputWithTimeStamp implements LogListener {

    /**
     * this is where we put out the output
     */
    private final Logger output;

    /**
     * this is the model we use
     */
    private final MacroII model;


    public LoggerOutputWithTimeStamp(Logger output, MacroII model) {
        this.output = output;
        this.model = model;
    }

    @Override
    public void handleNewEvent(LogEvent logEvent) {

        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        StatusPrinter.print(lc);


        String newMessage = "date:" + model.getMainScheduleTime() + ", phase: " + model.getCurrentPhase() + logEvent.getMessage();
        LogLevel.log(output,logEvent.getLevel(),newMessage,logEvent.getAdditionalParameters());
   /*     if(logEvent.getLevel().compareTo(LogLevel.INFO)>=0)
            System.out.println(
                    "date:" + model.getMainScheduleTime() + ", phase: " + model.getCurrentPhase() + " " +
                    MessageFormatter.arrayFormat(logEvent.getMessage(), logEvent.getAdditionalParameters()).getMessage()
            );
*/
    }
}
