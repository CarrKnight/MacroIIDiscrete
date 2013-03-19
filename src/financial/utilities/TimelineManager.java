/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package financial.utilities;

import financial.MarketEvents;
import lifelines.LifelinesPanel;
import lifelines.data.DataManager;
import lifelines.data.Event;
import lifelines.data.Record;
import model.MacroII;
import model.MacroIIGUI;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * <h4>Description</h4>
 * <p/>
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-09-08
 * @see
 */
public class TimelineManager {


    /**
     * If god is merciful then this will hold the lifelines data. One record for each agent
     */
    protected HashMap<Object,Record> records;


    /**
     * Data Manager holding records and hopefully visualizing them
     */
    private DataManager recordManager;


    private final LifelinesPanel panel;

    /**
     * if the updating should be weekly, we have to wait till one agent shows up before starting updating. This is set to true when the first agent shows up
     */
    private boolean isUpdating = false;

    /**
     * This manager keeps track of events and records, clearing up the clutter from the
     */
    public TimelineManager(LifelinesPanel panel) {
        assert MacroII.hasGUI();

        records = new HashMap<>();
        recordManager = new DataManager(new ArrayList<Record>());
        this.panel = panel;


    }

    public DataManager getRecordManager() {
        return recordManager;
    }

    /**
     * When a (possibly) new agent enters the market call this to create a record for him.
     * It's not acceptable to add an logEvent for an agent that wasn't added
     */
    public void addAgent(Object newAgent){
        {

            assert MacroII.hasGUI();


            //is it REALLY new?
            if(!records.keySet().contains(newAgent)){
                //need to create it
                Record record = new Record(newAgent.toString());
                recordManager.addRecord(record);

                records.put(newAgent,record); //put it in.
            }

        }

    }


    /**
     * If an logEvent worth to be put in the timeline occurred, call this!
     * @param agent the one who performed the logEvent
     * @param action the action that has occurred
     * @param time the "real" time when this occurred
     */
    public void event(Object agent, MarketEvents action, long time )
    {
        assert MacroII.hasGUI();


        //get the right record
        Record record = records.get(agent);
        if(record == null)
            throw new IllegalArgumentException("Agent not recognized by the timeline manager! Is it registered?");

        //add it!
        record.addEvent(new Event(action.ordinal(),time));
        updateTimeLine();


    }


    /**
     * If an logEvent worth to be put in the timeline occurred, call this!
     * @param agent the one who performed the logEvent
     * @param action the action that has occurred
     * @param time the "real" time when this occurred
     * @param annotations additional information to display!
     */
    public void event(Object agent, MarketEvents action, long time, String annotations)
    {

        assert MacroII.hasGUI();

        //get the right record
        Record record = records.get(agent);
        if(record == null)
            throw new IllegalArgumentException("Agent not recognized by the timeline manager! Is it registered?");

        //add it!
        record.addEvent(new Event(action.ordinal(),time,annotations));
        updateTimeLine();


    }



    public void updateTimeLine(){
        assert MacroII.hasGUI();

        if(!MacroIIGUI.TIMELINE_WEEKLY_UPDATE) //if the model is not set up to update weekly, update each movement; that's REALLY slow
        {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    //    if(recordPanel.isShowing()) <---- If I put this in,it doesn't update even when it should
                    panel.myVisualizationPanels[0].myTimeLine.update();
                }
            });
        }
    }


    /**
     * if MacroIIGUI says to update only every week, update only every week.
     */
    public void weekEnd(){

        if(MacroIIGUI.TIMELINE_WEEKLY_UPDATE) //if the model is not set up to update weekly, update each movement; that's REALLY slow
        {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    //    if(recordPanel.isShowing()) <---- If I put this in,it doesn't update even when it should
                    panel.myVisualizationPanels[0].myTimeLine.update();
                }
            });
        }
    }


}
