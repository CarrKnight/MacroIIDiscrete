/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.collectors;

import com.google.common.base.Preconditions;
import model.utilities.stats.collectors.enums.DataStorageSkeleton;

import java.util.EnumMap;

/**
 * <h4>Description</h4>
 * <p/>  given an enum describing the type of data to store, this abstract class provides the facility for outside objects to query it and leaves the data
 * protected so it can be accessed by subclasses to be filled
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-08-19
 * @see
 */
public abstract class DataStorage<T extends  Enum<T>> extends DataStorageSkeleton<T>
{

    /**
     * grabbed at constructor, makes all the enum stuff possible
     */
    final private Class<T> enumType;
    /**
     * when it is set to off, it stops rescheduling itself!
     */
    private boolean active = true;


    protected DataStorage(Class<T> enumType) {
        data = new EnumMap<>(enumType);
        this.enumType = enumType;

        Preconditions.checkState(enumType.getEnumConstants().length > 0, "the enum must have a size!");

        for(T type : enumType.getEnumConstants())
            data.put(type,new DailyObservations());

    }





    //i override because it's more natural to check all enums here
    @Override
    public boolean doubleCheckNumberOfObservationsAreTheSameForAllObservations(int numberOfObs)
    {
        //for each type, the number of obs has to be the same!
        for(T type : enumType.getEnumConstants())
            if(data.get(type).size() != numberOfObs)
                return false;

        return true;



    }


    public boolean isActive() {
        return active;
    }

    @Override
    public void turnOff() {
        active = false;
        data.clear();
    }
}
