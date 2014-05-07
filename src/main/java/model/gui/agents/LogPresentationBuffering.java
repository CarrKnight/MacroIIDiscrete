/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui.agents;

import com.google.common.base.Preconditions;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.Deactivatable;
import model.utilities.logs.*;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.LinkedList;
import java.util.List;

/**
 * This presentation class holds a log listener for gui, it places the log events in a table.
 * It buffers the table, updating it through steppable, to avoid calling the FXPlatform too often.
 * Created by carrknight on 5/5/14.
 */
public class LogPresentationBuffering implements Steppable, Deactivatable, ListChangeListener<LogEventForGUI>
{

    /**
     * the listener/converter object
     */
    private final LogListenerForGUI listener;

    /**
     * the model to schedule yourself to
     */
    private final MacroII model;

    /**
     * whatever we are listening to
     */
    private final Loggable listeningTo;

    /**
     * the list containing log events
     */
    private final ObservableList<LogEventForGUI> log;

    private final List<LogEventForGUI> buffer;

    private final TableView<LogEventForGUI> logView;

    private boolean active = true;

    public LogPresentationBuffering(MacroII model, Loggable agent) {
        this.model = model;


        //create the log
        log = FXCollections.observableArrayList();
        buffer = new LinkedList<>();

        //create the table and columns
        logView = new TableView<>(log);
        TableColumn<LogEventForGUI, Number> day = new TableColumn<>("Day");
        day.setCellValueFactory(cell -> cell.getValue().dayProperty());
        TableColumn<LogEventForGUI, ActionOrder> phase = new TableColumn<>("Phase");
        phase.setCellValueFactory(cell -> cell.getValue().phaseProperty());
        TableColumn<LogEventForGUI, LogLevel> level = new TableColumn<>("Level");
        level.setCellValueFactory(cell -> cell.getValue().logLevelProperty());
        TableColumn<LogEventForGUI, Object> source = new TableColumn<>("Source");
        source.setCellValueFactory(cell -> cell.getValue().sourceProperty());
        TableColumn<LogEventForGUI, String> message = new TableColumn<>("Message");
        message.setCellValueFactory(cell -> cell.getValue().messageProperty());
        //add all the columns
        logView.getColumns().addAll(day, phase, level, source, message);

        //create the listener
        listener = new LogListenerForGUI(model);
        this.listeningTo = agent;
        //we are going to listen to the listener
        listener.getEventList().addListener(this);

        //log the beginning of recording
        listener.handleNewEvent(new LogEvent(this,LogLevel.INFO,"Started Listening"));
        //let the listener tune in the agent
        listeningTo.addLogEventListener(listener);



    }

    @Override
    public void onChanged(Change<? extends LogEventForGUI> change) {

        change.next();
        Preconditions.checkArgument(change.getRemovedSize()==0,"I never expect logs to be deleted");
        Preconditions.checkArgument(change.getAddedSize()==1,"One log at a time!");
        Preconditions.checkArgument(change.wasAdded(),"I never expect logs to be deleted!!!");
        buffer.add(change.getAddedSubList().get(0));
        //if this is the first buffer addition, schedule yourself!
        if(buffer.size()==1)
            model.scheduleSoon(ActionOrder.GUI_PHASE,this);
        Preconditions.checkArgument(!change.next(),"One log at a time!");
    }


    /**
     * our step is when we bother the FXPlatform to update our buffer.
     */
    @Override
    public void step(SimState state)
    {
        if(!active)
            return;
        assert buffer.size() >0;
        final  List<LogEventForGUI> secondBuffer = new LinkedList<>(buffer); //make a copy
        buffer.clear();
        Platform.runLater(() -> log.addAll(secondBuffer)); //put the copy in

    }

    @Override
    public void turnOff() {
        active=false;
        //stop listening
        listener.getEventList().removeListener(this);
        listeningTo.removeLogEventListener(listener);

    }


    public TableView<LogEventForGUI> getLogView() {
        return logView;
    }


    public LogLevel getMinimumLevelToListenTo() {
        return listener.getMinimumLevelToListenTo();
    }

    public Property<LogLevel> minimumLevelToListenToProperty() {
        return listener.minimumLevelToListenToProperty();
    }

    public void setMinimumLevelToListenTo(LogLevel minimumLevelToListenTo) {
        listener.setMinimumLevelToListenTo(minimumLevelToListenTo);
    }
}
