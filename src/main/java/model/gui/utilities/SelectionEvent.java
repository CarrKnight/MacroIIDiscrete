/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.gui.utilities;

/**
 * This event represents an agent/market/object being "clicked/selected". This gets passed up along the responsibility chain until
 * something extinguishes it.
 * Created by carrknight on 5/6/14.
 */
public class SelectionEvent
{
    private final Object objectSelected;


    public SelectionEvent(Object objectSelected) {
        this.objectSelected = objectSelected;
    }

    public Object getObjectSelected() {
        return objectSelected;
    }
}
