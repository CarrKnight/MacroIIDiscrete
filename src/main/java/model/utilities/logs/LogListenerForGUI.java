/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.logs;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.MacroII;
import org.slf4j.helpers.MessageFormatter;

/**
 * A log listener that reads log events and turn them into GUI-readable log events.
 * Basically an adaptor LogEvent-->LogEventForGUI as well as a listener that stores everything in a
 * easy accessible collection <p>
 * It doesn't register or turn off on its own.
 * Created by carrknight on 5/5/14.
 */
public class LogListenerForGUI implements LogListener{

    /**
     * the model, needed to check the model time of the event AND to schedule an update of the list
     */
    private final MacroII model;

    /**
     * the list of logged events that is visible for the gui and any other outside element
     */
    private final ObservableList<LogEventForGUI> eventList;


    /**
     * logs below this level aren't listened/converted
     */
    private final Property<LogLevel> minimumLevelToListenTo;

    public LogListenerForGUI(MacroII model) {
        this(model,LogLevel.TRACE);
    }

    public LogListenerForGUI(MacroII model, LogLevel minimumLevelToListenTo) {
        this.model = model;
        this.minimumLevelToListenTo = new SimpleObjectProperty<>(minimumLevelToListenTo);
        eventList = FXCollections.observableArrayList();
    }

    @Override
    public void handleNewEvent(LogEvent logEvent)
    {
        LogLevel level = logEvent.getLevel();
        if(level.compareTo(minimumLevelToListenTo.getValue()) < 0) //don't bother if not needed
            return;

        //create a new gui log event
        LogEventForGUI logEventForGUI = new LogEventForGUI((int)model.getMainScheduleTime(),
                model.getCurrentPhase(),logEvent.getSource(),
                MessageFormatter.arrayFormat(logEvent.getMessage(),logEvent.getAdditionalParameters()).getMessage(),level);
        eventList.add(logEventForGUI);


    }



    public LogLevel getMinimumLevelToListenTo() {
        return minimumLevelToListenTo.getValue();
    }

    public Property<LogLevel> minimumLevelToListenToProperty() {
        return minimumLevelToListenTo;
    }

    public void setMinimumLevelToListenTo(LogLevel minimumLevelToListenTo) {
        this.minimumLevelToListenTo.setValue(minimumLevelToListenTo);
    }


    public ObservableList<LogEventForGUI> getEventList() {
        return eventList;
    }


}
