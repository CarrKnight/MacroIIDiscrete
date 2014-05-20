/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.logs;

/**
 * Simply an interface for something that can both listen and generate logevents.
 * Created by carrknight on 5/1/14.
 */
public interface LogNode extends LogListener, Loggable {



    /**
     * this isn't really necessary but I personally like it.
     * All it says is basically tell this listener to start listening (hopefully by add themselves as listeners)
     * @param eventSource the source the listener should start listening to
     * @return true if it starts listening correctly
     */
    public boolean listenTo(Loggable eventSource);

    /**
     * this isn't really necessary but I personally like it.
     * Tells the listener to remove itself from the source.
     */
    public boolean stopListeningTo(Loggable branch);

}
