/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.logs;

import javafx.beans.property.*;
import model.utilities.ActionOrder;

/**
 * A better categorized log event, useful in custom loggers
 * Created by carrknight on 5/5/14.
 */
public class LogEventForGUI
{

    /**
     * At what day did this event occur
     */
    private final ReadOnlyProperty<Number> day;

    /**
     * at what phase of the day did this event show up
     */
    private final ReadOnlyProperty<ActionOrder> phase;

    /**
     * the source of the event
     */
    private final ReadOnlyProperty<Object> source;

    /**
     * the log message, it must be already collated.
     */
    private final ReadOnlyProperty<String> message;

    /**
     * the log level
     */
    private final ReadOnlyProperty<LogLevel> logLevel;

    public LogEventForGUI(Integer day, ActionOrder phase,
                          Object source, String message, LogLevel logLevel) {
        this.day = new SimpleIntegerProperty(day);
        this.phase = new SimpleObjectProperty<>(phase);
        this.source = new SimpleObjectProperty<>(source);
        this.message = new SimpleStringProperty(message);
        this.logLevel = new SimpleObjectProperty<>(logLevel);
    }


    public Number getDay() {
        return day.getValue();
    }

    public ReadOnlyProperty<Number> dayProperty() {
        return day;
    }

    public ActionOrder getPhase() {
        return phase.getValue();
    }

    public ReadOnlyProperty<ActionOrder> phaseProperty() {
        return phase;
    }

    public Object getSource() {
        return source.getValue();
    }

    public ReadOnlyProperty<Object> sourceProperty() {
        return source;
    }

    public String getMessage() {
        return message.getValue();
    }

    public ReadOnlyProperty<String> messageProperty() {
        return message;
    }

    public LogLevel getLogLevel() {
        return logLevel.getValue();
    }

    public ReadOnlyProperty<LogLevel> logLevelProperty() {
        return logLevel;
    }
}
