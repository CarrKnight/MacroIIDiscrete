/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.scenario;

import agents.EconomicAgent;
import financial.market.Market;
import goods.GoodType;
import model.MacroII;
import org.reflections.Reflections;
import sim.portrayal.Inspector;
import sim.portrayal.SimpleInspector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * <h4>Description</h4>
 * <p/> A scenario is just a class holding one method: start(). This is called by  MacroII
 * to setup the model. The scenario basically schedules all the agents and stores them in collections
 * that the MacroII object can reference to
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-09-10
 * @see
 */
public abstract class Scenario {

    /**
     * Link to the model
     */
    protected final MacroII model;

    /**
     * A map for each good type to one markets
     */
    private EnumMap<GoodType,Market> markets;

    /**
     * the list of ALL agents. Notice it's an arraylist so it's faster to shuffle
     */
    private ArrayList<EconomicAgent> agents;

    /**
     * Creates the scenario object, so that it links to the model.
=     */
    protected Scenario(MacroII model) {
        this.model = model;
        //instantiate the collections!
        markets = new EnumMap<>(GoodType.class);
        agents = new ArrayList<>();
    }

    /**
     * Called by MacroII, it creates agents and then schedules them.
     */
    public abstract void start();

    /**
     * A map for each good type to one markets
     */
    public EnumMap<GoodType, Market> getMarkets() {
        return markets;
    }

    /**
     * the list of ALL agents. Notice it's an arraylist so it's faster to shuffle
     */
    public ArrayList<EconomicAgent> getAgents() {
        return agents;
    }

    /**
     * a getter for subclasses to do their funny businesses.
     */
    protected MacroII getModel() {
        return model;
    }


    /**********************************************
     * Scenario inspector, to choose which scenario to run
     ***********************************************/

    final static Set<Class<? extends Scenario>> scenarios;

    static{
        //turn on the reader
        Reflections reflections = new Reflections("model.scenario");
        //read all the scenarios
        scenarios = reflections.getSubTypesOf(Scenario.class);


    }

    public static Inspector scenarioSelector(final MacroII model)
    {

        //the gui should be on!
        assert MacroII.hasGUI();

        /**
         * The inspector we'll return
         */
        Inspector selector = new Inspector() {
            @Override
            public void updateInspector() {
                this.repaint();
            }
        };




        //get the scenario
        Class<? extends Scenario> scenario = model.getScenario().getClass();
        //create the combo box.
        final JComboBox<Class<? extends Scenario>> comboBox = new JComboBox<>();
        //better make sure the combo-box finds the original scenario
        boolean found = false;


        ArrayList<Class<? extends Scenario>> sortedScenarios = new ArrayList<>(scenarios);
        System.out.println(sortedScenarios);
        Collections.sort(sortedScenarios,new Comparator<Class<? extends Scenario>>() {
            @Override
            public int compare(Class<? extends Scenario> o1, Class<? extends Scenario> o2) {
                System.out.println(o1);
                System.out.println(o2);
                return o1.getName().compareToIgnoreCase(o2.getName());

            }
        });
        //go through
        for(Class<? extends Scenario> s : sortedScenarios)
        {
            comboBox.addItem(s);
            //if this is the class in use
            if(s.equals(scenario)){
                comboBox.setSelectedItem(s); //set it as selected
                found=true;
            }

        }
        //make sure our strategy is already in the list, somehow
        assert found;


        //create the box containing the combox for scenario and info about the current strategy!
        final Box scenarioBox = new Box(BoxLayout.Y_AXIS);
        scenarioBox.add(comboBox);
        SimpleInspector currentScenario = new SimpleInspector(model.getScenario(),
                MacroII.getGUI());
        for(Component c : currentScenario.getComponents()){
            scenarioBox.add(c);
        }
        //add it to the inspector
        selector.add(scenarioBox);

        //now tell

        //now tell combo-box about what to do when clicked
        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                //the new strategy
                Class<? extends Scenario> newStrategy =
                        (Class<? extends Scenario>) comboBox.getSelectedItem();

                //create the instance of the new strategy
                Scenario newInstance;
                try {
                    newInstance = newStrategy.getConstructor(MacroII.class).newInstance(model);
                } catch (InstantiationException e1) {
                    throw new RuntimeException("The scenario implemented doesn't have a no-args constructor");
                } catch (IllegalAccessException e1) {
                    throw new RuntimeException("The constructor needs to have a public access");
                } catch (NoSuchMethodException | InvocationTargetException e1) {
                    throw new RuntimeException("The no-args constructor needs to have a public access");
                }

                //now we need to call the create() method
                assert newInstance != null;
                //now we give it back to the model

                model.setScenario(newInstance);

                //now we need to remove everything from the box
                scenarioBox.removeAll();
                scenarioBox.add(comboBox); //re-add the combo-box
                //get the new info by creating a new strategy inspector.
                SimpleInspector currentScenario = new SimpleInspector(model.getScenario(),
                        MacroII.getGUI());
                for(Component c : currentScenario.getComponents()){
                    scenarioBox.add(c);
                }
                scenarioBox.repaint();
                //DONE!

            }
        });

        //return selector
        return selector;


    }




}
