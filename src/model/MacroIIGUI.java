/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model;

import financial.Market;
import model.scenario.Scenario;
import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.util.Bag;

import javax.swing.*;

/**
 * <h4>Description</h4>
 * <p/> The GUI of macro II
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-09-06
 * @see
 */
public class MacroIIGUI extends GUIState {


    public Display2D testDisplay;

    public JFrame displayFrame;

    final private MacroII state;

    static public boolean  TIMELINE_WEEKLY_UPDATE = true;


    org.jfree.data.xy.XYSeries series;    // the data series we'll add to
    sim.util.media.chart.TimeSeriesChartGenerator chart;  // the charting facility


    public MacroIIGUI(MacroII state) {
        super(state);
        this.state = state;
        state.registerGUI(this);
    }


    @Override
    public void init(Controller controller) {
        super.init(controller);
        testDisplay = new Display2D(600,600,this);
        testDisplay.setClipping(false);
        displayFrame = testDisplay.createFrame();
        controller.registerFrame(displayFrame);
        displayFrame.setVisible(true);


        //initialize the selector
        Bag inspectors = new Bag(1); inspectors.add(Scenario.scenarioSelector(state));
        Bag names = new Bag(1); names.add("Scenario controller");
        controller.setInspectors(inspectors,names);




    }

    @Override
    public void start() {
        super.start();
        //for each market
        for(Market m : state.getMarkets())
            testDisplay.attach(m.getMarketInspector(),m.getMarketInspector().getName());
        testDisplay.reset();
        testDisplay.repaint();


  //     chart.removeAllSeries();


    }

    @Override
    public boolean step() {
        return super.step();

    }


    public static void main(String[] args){
        MacroIIGUI gui = new MacroIIGUI(new MacroII(System.currentTimeMillis()));
        Console c = new Console(gui);
        c.setVisible(true);
    }


    @Override
    public void finish() {
        super.finish();

        //initialize the selector
        Bag inspectors = new Bag(1); inspectors.add(Scenario.scenarioSelector(state));
        Bag names = new Bag(1); names.add("Scenario controller");
        controller.setInspectors(inspectors, names);
        testDisplay.detatchAll();
    }


}
