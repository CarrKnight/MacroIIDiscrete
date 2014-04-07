/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.gui;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.GeographicalFirm;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import financial.market.Market;
import javafx.collections.*;
import javafx.scene.paint.Color;
import model.utilities.Deactivatable;

import java.util.*;

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
    static {
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
     * returns an unmodifiable view of the default initial colors!
     */
    public static List<Color> getDefaultColors(){
        return Collections.unmodifiableList(defaultInitialColors);
    }

    /**
     * we use the iterator to grab the default colors, if possible
     */
    private final Iterator<Color> defaultColorsIterator = defaultInitialColors.iterator();

    /**
     * the randomizer for colors when we run out of the default ones!
     */
    private final MersenneTwisterFast randomizer;

    /**
     * the map at the core of the class
     */
    private final ObservableMap<GeographicalFirm,Color> colorMap = FXCollections.observableMap(new HashMap<>());

    private final Market market;

    /**
     * Starts to listen to the market straight away
      * @param market
     */
    public SellingFirmToColorMap(Market market, MersenneTwisterFast randomizer)
    {
        this.market = market;
        this.randomizer = randomizer;
        //initialize the colorMap if the seller list is already filled!
        Set<EconomicAgent> sellers = market.getSellers();
        for(EconomicAgent agent : sellers)
        {
            if(agent instanceof GeographicalFirm)
                addNewFirm((GeographicalFirm) agent);
        }

        market.addListenerToSellerSet(this);


    }

    private void addNewFirm(GeographicalFirm agent) {
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
        market.removeListenerFromSellerSet(this);
        colorMap.clear();
    }


    @Override
    public void onChanged(Change<? extends EconomicAgent> change) {
        if (change.wasAdded()) {
            EconomicAgent agent = change.getElementAdded();
            if (agent instanceof GeographicalFirm)
                addNewFirm((GeographicalFirm) agent);
        }
        else
        {
            assert change.wasRemoved();
            EconomicAgent agent = change.getElementRemoved();
            if (agent instanceof GeographicalFirm)
            {
                Color removedColor = colorMap.remove(agent);
                Preconditions.checkNotNull(removedColor);
            }
        }
    }


    /**
     * An unmodifiable copy of the current color map.
     * Useful to know the color associated with each firm, but also just to know who the seller firms are
     * without having to ask the market object itself
     * @return the map
     */
    public ObservableMap<GeographicalFirm, Color> getColorMap() {
        return FXCollections.unmodifiableObservableMap(colorMap);
    }

    /**
     * add a listener to the color map changes.
     * @param mapChangeListener
     */
    public void addListener(MapChangeListener<? super GeographicalFirm, ? super Color> mapChangeListener) {
        colorMap.addListener(mapChangeListener);
    }
/**
 * removes a listener to the color map changes.
 * @param mapChangeListener
 */
    public void removeListener(MapChangeListener<? super GeographicalFirm, ? super Color> mapChangeListener) {
        colorMap.removeListener(mapChangeListener);
    }

    public Color getFirmColor(Firm firm)
    {
        return colorMap.get(firm);
    }
}
