/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.collectors.enums;

import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.base.Preconditions;
import javafx.beans.value.ObservableDoubleValue;
import model.utilities.Deactivatable;
import model.utilities.stats.collectors.DailyObservations;
import sim.engine.Steppable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * <h4>Description</h4>
 * <p> The most generic abstraction of data storage. This one doesn't assume that T is an enum, so we can leverage the infrastructure that keeps track of time and observations for non-enum keyed data
 * <p>
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-04-20
 * @see
 */
public abstract class DataStorageSkeleton<T> implements Steppable, Deactivatable {


    /**
     * where we store all our cozy data
     */
    protected Map<T,DailyObservations>  data;


    /**
     * the day you started recording.
     */
    private int startingDay = -1;

    /**
     * how many days worth of observations are here?
     */
    public int numberOfObservations()
    {
        //always exists because we specified so in constructor

        int numberOfObs = data.isEmpty() ? 0 : data.values().iterator().next().size();

        assert doubleCheckNumberOfObservationsAreTheSameForAllObservations(numberOfObs);

        return numberOfObs;

    }


    public boolean doubleCheckNumberOfObservationsAreTheSameForAllObservations(int numberOfObs)
    {
        //for each type, the number of obs has to be the same!
        for(DailyObservations observations : data.values())
            if(observations.size() != numberOfObs)
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
    public double[] getObservationsRecordedTheseDays(T type, int[] days)
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
            for(T e : data.keySet())
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
                for(T e : data.keySet())
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
