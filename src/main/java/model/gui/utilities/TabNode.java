/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
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
