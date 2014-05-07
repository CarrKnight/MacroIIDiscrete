/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
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
