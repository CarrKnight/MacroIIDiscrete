/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.logs;

import model.utilities.Deactivatable;
import org.slf4j.Logger;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This should be a simple object agents/departments can hold on to to delegate logging to. Imagine it being a node in a tree.
 * Except there is no check that this is part of a tree and not part of some crazy circular logging facility.
 * Created by carrknight on 4/30/14.
 */
public class LogNode implements LogListener, Loggable, Deactivatable
{

    /**
     * object that are listening to YOU
     */
    private final Set<LogListener> roots;

    /**
     * objects that YOU are listening to
     */
    private final Set<Loggable> branches;

    /**
     * loggers registered here, you broadcast log events there!
     */
    private final Set<Logger> outputs;

    public LogNode() {
        roots = new LinkedHashSet<>();
        branches = new LinkedHashSet<>();
        outputs = new LinkedHashSet<>();

    }

    /**
     * when it receives a logevent if there are any "outputs" registered then it logs the event there.
     * If there are any roots, it rebroadcast the message there.
     * @param logEvent the event to log
     */
    @Override
    public void handleNewEvent(LogEvent logEvent)
    {
        //tell the outputs to log
        for(Logger sink : outputs)
        {
            LogLevel.log(sink,logEvent.getLevel(),logEvent.getMessage(),logEvent.getAdditionalParameters());
        }
        //rebroadcast to roots
        for(LogListener root : roots)
        {
            root.handleNewEvent(logEvent);
        }
    }

    @Override
    public boolean addLogEventListener(LogListener toAdd) {
        return roots.add(toAdd);
    }

    @Override
    public boolean removeLogEventListener(LogListener toRemove) {
       return roots.add(toRemove);
    }

    /**
     * start monitoring this loggable
     * @return
     */
    public boolean listenTo(Loggable branch){
        branches.add(branch);
        return branch.addLogEventListener(this);
    }

    /**
     * stop listening, stop broadcasting, stop everything
     */
    @Override
    public void turnOff()
    {
        roots.clear();
        outputs.clear();
        for(Loggable branch : branches)
            branch.removeLogEventListener(this);
        branches.clear();
    }

    /**
     * every log event that passes through here will go to this logger
     */
    public boolean attachOutput(Logger logger)
    {
       return outputs.add(logger);
    }
}
