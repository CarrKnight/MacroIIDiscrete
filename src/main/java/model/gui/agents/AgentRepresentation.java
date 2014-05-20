/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
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
