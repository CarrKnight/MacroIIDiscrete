/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.scenario;

import agents.firm.Firm;
import model.MacroII;

import java.util.LinkedList;

/**
 * <h4>Description</h4>
 * <p/> Same situation as Monopoly Scenario, but on top of it I add two more identical firms
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-09-14
 * @see
 */
public class TripolistScenario extends MonopolistScenario{



    private int additionalCompetitors = 2;

    /**
     * A linked list of all competiors, so that we can query them in constant ordering
     */
    private LinkedList<Firm> competitors;

    public TripolistScenario(MacroII macroII) {
        super(macroII);
        //make default maximizer as always climbing so reviewers can replicate the paper!
        super.setControlType(MonopolistScenarioIntegratedControlEnum.HILL_CLIMBER_ALWAYS_MOVING);


        //instantiate the list
        competitors = new LinkedList<>();
    }


    /**
     * Called by MacroII, it creates agents and then schedules them.
     */
    @Override
    public void start() {
        //do all the monopolist stuff
        super.start();
        //add the monopolist to the list of competitors
        competitors.add(monopolist);
        monopolist.setName("competitor0");

        //sanity check
        //  assert !getAgents().isEmpty() : getAgents();
        assert getMarkets().size()==2;

        //now add n more agents
        for(int i=0; i < additionalCompetitors; i++)
        {

            Firm competitor = buildFirm();
            //the monopolist reference now points to the new guy
            competitor.setName("competitor" + i + 1);
            //also add it to the competitors' list
            competitors.add(competitor);

        }

    }





    public int getAdditionalCompetitors() {
        return additionalCompetitors;
    }

    public void setAdditionalCompetitors(int additionalCompetitors) {
        this.additionalCompetitors = additionalCompetitors;
    }





    public LinkedList<Firm> getCompetitors() {
        return competitors;
    }


}
