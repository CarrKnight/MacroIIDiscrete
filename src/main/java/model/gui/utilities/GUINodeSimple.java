/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui.utilities;

/**
 * A simple gui node that pushes up all the events. Useful to delegate to.
 * Created by carrknight on 5/7/14.
 */
public class GUINodeSimple implements GUINode {

    private GUINode root;

    @Override
    public void setGUINodeParent(GUINode parent)
    {
        root = parent;
    }

    @Override
    public void representSelectedObject(SelectionEvent event) {
        if(root!=null)
            root.representSelectedObject(event);
    }

    @Override
    public void handleNewTab(TabEvent event) {
        if(root!=null)
            root.handleNewTab(event);
    }

    public GUINode getGUINodeParent() {
        return root;
    }
}
