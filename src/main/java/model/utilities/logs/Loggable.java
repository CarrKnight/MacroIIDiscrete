/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.logs;

/**
 * An object is loggable if it can be listened to for log-events
 * Created by carrknight on 4/30/14.
 */
public interface Loggable
{

    public boolean addLogEventListener(LogListener toAdd);

    public boolean removeLogEventListener(LogListener toRemove);


}
