/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui.utilities;

/**
 * Tab node represents a node in the chain of responsibility to deal with the created tab
 * Created by carrknight on 4/29/14.
 */
public interface TabNode {


    public void handleNewTab(TabEvent event);



}
