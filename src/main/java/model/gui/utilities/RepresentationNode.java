/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.gui.utilities;

/**
 * Representation node is a node in a responsibility chain of how to represent a selected object.
 * Created by carrknight on 5/6/14.
 */
public interface RepresentationNode
{


    public void representSelectedObject(SelectionEvent event);

}
