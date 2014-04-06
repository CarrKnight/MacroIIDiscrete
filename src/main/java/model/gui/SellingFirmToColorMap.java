/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui;

import agents.EconomicAgent;
import agents.firm.Firm;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import financial.market.Market;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.paint.Color;
import model.utilities.Deactivatable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * A very simple object, in essence a map firm---> color. It takes a Market object and observes its seller set to keep
 * the color updated.
 * Because it checks whether a seller is a firm or not, this object is also handy as a container of a "sellers that are firms" set.
 * Created by carrknight on 4/5/14.
 */
public class SellingFirmToColorMap implements Deactivatable, SetChangeListener<EconomicAgent>{


    //taken from: http://jfly.iam.u-tokyo.ac.jp/color/
    //they should be color-blind friendly (and they work on me, so that's nice)
    private static final LinkedList<Color> defaultInitialColors  =
            new LinkedList<>();
    {
        defaultInitialColors.add(new Color(.9,.6,0,1)); //Orange
        defaultInitialColors.add(new Color(.35,.7,.9,1)); //SkyBlue
        defaultInitialColors.add(new Color(0,.6,.5,1)); //bluish green
        defaultInitialColors.add(new Color(.95,.90,.25,1)); //Yellow
        defaultInitialColors.add(new Color(0,.45,.70,1)); //Blue
        defaultInitialColors.add(new Color(.8,.4,0,1)); //Vermillon
        defaultInitialColors.add(new Color(.8,.6,.7,1)); //Reddish-Purple
        defaultInitialColors.add(new Color(0,0,0,1)); //Black

    }

    /**
     * we use the iterator to grab the default colors, if possible
     */
    private final Iterator<Color> defaultColorsIterator = defaultInitialColors.iterator();

    /**
     * the randomizer for colors when we run out of the default ones!
     */
    private final MersenneTwisterFast randomizer = new MersenneTwisterFast();

    /**
     * the map at the core of the class
     */
    private final ObservableMap<Firm,Color> colorMap = FXCollections.observableMap(new HashMap<>());

    /**
     * the set of sellers to observe
     */
    private final ObservableSet<EconomicAgent> sellers;

    /**
     * Starts to listen to the market straight away
      * @param market
     */
    public SellingFirmToColorMap(Market market)
    {
        //initialize the colorMap if the seller list is already filled!
        sellers = market.getSellers();
        for(EconomicAgent agent : sellers)
        {
            if(agent instanceof Firm)
                addNewFirm((Firm) agent);
        }

        sellers.addListener(this);


    }

    private void addNewFirm(Firm agent) {
        Color oldColor = colorMap.put(agent, getNewColor());
        Preconditions.checkState(oldColor == null); //it shouldn't replace an old color, it should just be gone!
    }


    /**
     * very simple generator of colors: go through default, if they are all used up then randomize.
     * It never recycles colors
     * @return a new color
     */
    private Color getNewColor(){
        if(defaultColorsIterator.hasNext())
            return defaultColorsIterator.next();
        else
        {
            return new Color(randomizer.nextDouble(),
                    randomizer.nextDouble(),randomizer.nextDouble(),1);
        }
    }


    @Override
    public void turnOff() {
        sellers.removeListener(this);
        colorMap.clear();
    }


    @Override
    public void onChanged(Change<? extends EconomicAgent> change) {
        if (change.wasAdded()) {
            EconomicAgent agent = change.getElementAdded();
            if (agent instanceof Firm)
                addNewFirm((Firm) agent);
        }
        else
        {
            assert change.wasRemoved();
            EconomicAgent agent = change.getElementRemoved();
            if (agent instanceof Firm)
            {
                Color removedColor = colorMap.remove(agent);
                Preconditions.checkNotNull(removedColor);
            }
        }
    }


    /**
     * an unmodifiable view of the color map. Useful to know the color associated with each firm, but also just to know who the seller firms are
     * without having to ask the market object itself
     * @return the map
     */
    public ObservableMap<Firm, Color> getColorMap() {
        return FXCollections.unmodifiableObservableMap(colorMap);
    }

    public Color getFirmColor(Firm firm)
    {
        return colorMap.get(firm);
    }
}
