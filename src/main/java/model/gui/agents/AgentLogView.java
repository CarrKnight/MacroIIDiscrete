/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui.agents;

import agents.EconomicAgent;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import model.MacroII;
import model.gui.utilities.InformationTab;
import model.utilities.Deactivatable;

import java.util.LinkedList;

/**
 * A single tab showing the table with the log
 * Created by carrknight on 5/6/14.
 */
public class AgentLogView extends InformationTab implements Deactivatable {


    private final MacroII model;

    private final LogPresentationBuffering presentation;

    private final EconomicAgent agent;




    public AgentLogView(MacroII model, EconomicAgent agent) {
        super(new LinkedList<>(),new Tab());
        //create the presentation
        this.model = model;
        this.agent = agent;
        presentation = new LogPresentationBuffering(model,agent);



        //create the view itself
        BorderPane pane = new BorderPane(presentation.getLogView());
        getView().setContent(pane);
        getView().setText("Log of: " + agent.toString());

        //when the tab closes, turn off the presentation
        getView().onClosedProperty().addListener((observableValue, eventEventHandler, eventEventHandler2) -> turnOff());
    }

    @Override
    public void turnOff() {
        presentation.turnOff();
    }

    public LogPresentationBuffering getPresentation() {
        return presentation;
    }
}