/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package goods;


import model.utilities.Deactivatable;

import java.util.*;

/**
 * <h4>Description</h4>
 * <p>
 * <p>
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-04-19
 * @see
 */
public class GoodTypeMasterList implements Deactivatable {

    /**
     * I store all the industry codes in a map so that I can always get the
     * goodtype if I have the industry code
     */
    public final Map<String, GoodType> industryCodes =
            new HashMap<>();
    /**
     * Create the list of all sectors
     */
    private final LinkedHashSet<GoodType> listOfAllSectors = new LinkedHashSet<>();

    /**
     * here we store all the observers
     */
    private final LinkedHashSet<MasterListObserver> observers = new LinkedHashSet<>();


    public GoodTypeMasterList() {

        addToListAndMap(UndifferentiatedGoodType.LABOR);
        addToListAndMap(UndifferentiatedGoodType.GENERIC);
        addToListAndMap(DifferentiatedGoodType.CAPITAL);
        addToListAndMap(UndifferentiatedGoodType.MONEY);
    }

    /**
     * You can get a list of all sectors that exist from anywhere with this method. It returns an unmodifiable view.
     */
    public Set<GoodType> getListOfAllSectors() {
        return Collections.unmodifiableSet(listOfAllSectors);
    }

    /**
     * When you create a sector/new good type add it to the list of all sectors. You can't add the same sector twice,
     * so it will return false if you added that sector already
     */
    public boolean addNewSector(GoodType goodType) {

        boolean added = addToListAndMap(goodType);

        if(added) //if the add was valid
            //tell the observers
            notifyObservers();

        return added;
    }

    @Override
    public void turnOff() {
        observers.clear();
        clearMasterList();
    }

    /**
     * Add a bunch of sectors all at once; This is handy because it notifies the observers only once at the end
     */
    public boolean addNewSectors(Collection<GoodType> goodTypes) {
        boolean added = false;

        for(GoodType newGoodType : goodTypes)
        {
            boolean wasThisAdded = addToListAndMap(newGoodType);
            added = added || wasThisAdded; //add to the list and check if it made any difference
        }

        if(added) //if the add was valid
            //tell the observers
            notifyObservers();

        assert listOfAllSectors.size() == industryCodes.size();
        return added;
    }

    public boolean addNewSectors(GoodType... goodTypes){
        return addNewSectors(Arrays.asList(goodTypes));
    }


    private boolean addToListAndMap(GoodType goodType) {
        boolean added = listOfAllSectors.add(goodType);
        if (added)
            industryCodes.put(goodType.getCode(), goodType);
        return added;
    }

    /**
     * Removes all sectors
     */
    public void clearMasterList() {
        listOfAllSectors.clear();
        industryCodes.clear();

        //tell the observers
        notifyObservers();


    }

    /**
     * Given the code, returns the sector object associated with it, if it exists
     *
     * @param code the BLS/NAICS code of the sector
     * @return the sector
     */
    public GoodType getGoodTypeCorrespondingToThisCode(String code) {
        return industryCodes.get(code);
    }

    /**
     * Returns the number of goodtypes in this list
     */
    public int size() {
        return listOfAllSectors.size();
    }

    /**
     * The observer is registered and will be notified when the master list is updated
     * @param o the observer is added
     * @return true if it was added correctly (the underlying container is a set)
     */
    public boolean addObserver(MasterListObserver o)
    {
        return observers.add(o);
    }

    /**
     * deregister the given observer
     * @param o the observer
     * @return true if it was removed correctly (the underlying container is a set)
     */
    public boolean removeObserver(MasterListObserver o)
    {
        return observers.remove(o);
    }

    private void notifyObservers()
    {
        for(MasterListObserver o : observers)
            o.theMasterListHasChanged();
    }

}
