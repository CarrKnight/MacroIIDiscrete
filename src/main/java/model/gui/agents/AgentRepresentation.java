/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui.agents;

import agents.EconomicAgent;
import model.gui.utilities.RepresentationNode;

/**
 * Whatever you can click that corresponds to an agent
 * Created by carrknight on 5/6/14.
 */
public interface AgentRepresentation extends RepresentationNode
{

    public EconomicAgent getRepresentedAgent();



}
