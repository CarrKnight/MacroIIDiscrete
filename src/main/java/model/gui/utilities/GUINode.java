/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.gui.utilities;

/**
 * Every gui object (whether a view or a presentation or something else) that can generate or has objects that can generate
 * tabs/representation events
 * Created by carrknight on 5/6/14.
 */
public interface GUINode extends RepresentationNode, TabNode{

    /**
     * set the root/parent of this node to propagate all events we don't deal with ourselves.
     * @param parent the parent
     */
    public void setGUINodeParent(GUINode parent);





}
