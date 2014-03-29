/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.collectors;

import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.base.Preconditions;
import javafx.beans.value.ObservableDoubleValue;
import javafx.collections.ListChangeListener;
import model.utilities.Deactivatable;
import sim.engine.Steppable;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
public abstract class DataStorage<T extends  Enum<T>> implements Steppable, Deactivatable
{

    /**
     * where we store all our cozy data
     */
    final protected EnumMap<T,DailyObservations> data;
    /**
     * grabbed at constructor, makes all the enum stuff possible
     */
    final private Class<T> enumType;


    /**
     * the day you started recording.
     */
    private int startingDay = -1;

    protected DataStorage(Class<T> enumType) {
        data = new EnumMap<>(enumType);
        this.enumType = enumType;

        Preconditions.checkState(enumType.getEnumConstants().length > 0, "the enum must have a size!");

        for(T type : enumType.getEnumConstants())
            data.put(type,new DailyObservations());

    }

    /**
     * how many days worth of observations are here?
     */
    public int numberOfObservations()
    {
        //always exists because we specified so in constructor
        int numberOfObs = data.get(enumType.getEnumConstants()[0]).size();

        assert doubleCheckNumberOfObservationsAreTheSameForAllObservations(numberOfObs);

        return numberOfObs;

    }


    public boolean doubleCheckNumberOfObservationsAreTheSameForAllObservations(int numberOfObs)
    {
        //for each type, the number of obs has to be the same!
        for(T type : enumType.getEnumConstants())
            if(data.get(type).size() != numberOfObs)
                return false;

        return true;



    }


    public int getStartingDay() {
        return startingDay;
    }

    public void setStartingDay(int startingDay) {
        this.startingDay = startingDay;
    }



    public int getLastObservedDay()
    {

        return numberOfObservations()-1+getStartingDay();

    }



    protected void checkThatThereIsAtLeastOneObservation() {
        Preconditions.checkState(numberOfObservations() > 0, "no observations recorded yet");
    }


    /**
     * returns a copy of all the observed last prices so far!
     */
    public double[] getAllRecordedObservations(T type){
        checkThatThereIsAtLeastOneObservation();
        return data.get(type).getAllRecordedObservations();

    }




    /**
     * utility method to analyze only specific days
     */
    public double[] getObservationsRecordedTheseDays(T type,@Nonnull int[] days)
    {
        return data.get(type).getObservationsRecordedTheseDays(days);

    }


    /**
     * utility method to analyze only specific days
     */
    public double[] getObservationsRecordedTheseDays(T type,int beginningDay, int lastDay)
    {
        return data.get(type).getObservationsRecordedTheseDays(beginningDay, lastDay);

    }


    /**
     * utility method to analyze only  a specific day
     */
    public double getObservationRecordedThisDay(T type,int day)
    {
        return data.get(type).getObservationRecordedThisDay(day);
    }

    /**
     * return the latest price observed
     */
    @Nonnull
    public Double getLatestObservation(T type)
    {
        checkThatThereIsAtLeastOneObservation();
        return data.get(type).get(numberOfObservations()-1);
    }


    /**
     * return an observable value that keeps updating
     */
    public ObservableDoubleValue getLatestObservationObservable(T type)
    {
        return data.get(type).getObservableLastObservation();
    }



    /**
     * Utility method to write all the data to CSV File
     * @param file link to the file to write to
     */
    public void writeToCSVFile(File file)
    {
        try {

            CSVWriter writer = new CSVWriter(new FileWriter(file));

            //header
            String[]  header = new String[data.keySet().size()];
            int i=0;
            for(Enum e : data.keySet())
            {
                header[i] = e.toString();
                i++;
            }
            writer.writeNext(header);
            //now go through all the observations
            for(i=0; i< numberOfObservations(); i++)
            {
                String[] newline = new String[header.length];
                int j=0;
                for(Enum e : data.keySet())
                {
                    newline[j] = String.valueOf(data.get(e).get(i));
                    j++;
                }
                writer.writeNext(newline);

            }

            writer.flush();
            writer.close();


        } catch (IOException e) {
            System.err.println("File could not be written");
        }

    }




}
