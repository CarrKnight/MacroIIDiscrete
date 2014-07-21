/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.logs;

import model.MacroII;
import org.slf4j.helpers.MessageFormatter;

/**
 * <h4>Description</h4>
 * <p>
 * <p>
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-07-18
 * @see
 */
public class LogToConsole implements LogListener {

    private final LogLevel minimumLogLevel;


    private final MacroII model;

    public LogToConsole(LogLevel minimumLogLevel, MacroII model) {

        this.minimumLogLevel = minimumLogLevel;
        this.model = model;

    }


    /**
     * get notified of a log event!
     *
     * @param logEvent
     */
    @Override
    public void handleNewEvent(LogEvent logEvent)
    {

        if(logEvent.getLevel().compareTo(minimumLogLevel)>=0)
        {
            String message =  "date: " + model.getMainScheduleTime() + ", phase: " + model.getCurrentPhase() + " -----> " +
                    MessageFormatter.arrayFormat(logEvent.getMessage(), logEvent.getAdditionalParameters()).getMessage() + "\n";

            System.out.println(message);
        }




    }
}
