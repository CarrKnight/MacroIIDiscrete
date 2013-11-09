/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.collectors;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Doubles;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableDoubleValue;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * <h4>Description</h4>
 * <p/> A simple decorator to an array list to return some weird arrays when needed
 * <p/> Additionaly it has an observable double which is always the last element added. This should make charting a lot easier
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-08-16
 * @see
 */
public class DailyObservations implements Iterable<Double> {

    /**
     * this is the real deal
     */
    final private List<Double> observations;

    final private SimpleDoubleProperty lastObservation = new SimpleDoubleProperty();

    /**
     * something of an offset in case the observations were collected only after a specific day
     */
    private int startingDay=0;

    /**
     * flag that decides whether new instances use array list or linked list
     */
    public static boolean preferArrayListOverLinkedLists = true;

    public DailyObservations()
    {

        if(preferArrayListOverLinkedLists)
            observations = new ArrayList<>();
        else
            observations = new LinkedList<>();

    }

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     *
     * <p>The returned iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
     *
     * @return an iterator over the elements in this list in proper sequence
     */
    public Iterator<Double> iterator() {
        return observations.iterator();
    }

    /**
     * Returns a list iterator over the elements in this list (in proper
     * sequence).
     *
     * <p>The returned list iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
     *
     * @see #listIterator(int)
     */
    public ListIterator<Double> listIterator() {
        return observations.listIterator();
    }

    /**
     * Returns a list iterator over the elements in this list (in proper
     * sequence), starting at the specified position in the list.
     * The specified index indicates the first element that would be
     * returned by an initial call to {@link java.util.ListIterator#next next}.
     * An initial call to {@link java.util.ListIterator#previous previous} would
     * return the element with the specified index minus one.
     *
     * <p>The returned list iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public ListIterator<Double> listIterator(int index) {
        return observations.listIterator(index);
    }

    /**
     * Appends all of the elements in the specified collection to the end of
     * this list, in the order that they are returned by the
     * specified collection's Iterator.  The behavior of this operation is
     * undefined if the specified collection is modified while the operation
     * is in progress.  (This implies that the behavior of this call is
     * undefined if the specified collection is this list, and this
     * list is nonempty.)
     *
     * @param c collection containing elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     */
    public boolean addAll(Collection<? extends Double> c) {

        boolean toReturn = observations.addAll(c);
        lastObservation.setValue(observations.get(observations.size() - 1));
        return toReturn;

    }

    /**
     * Inserts all of the elements in the specified collection into this
     * list, starting at the specified position.  Shifts the element
     * currently at that position (if any) and any subsequent elements to
     * the right (increases their indices).  The new elements will appear
     * in the list in the order that they are returned by the
     * specified collection's iterator.
     *
     * @param index index at which to insert the first element from the
     *              specified collection
     * @param c collection containing elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws NullPointerException if the specified collection is null
     */
    public boolean addAll(int index, Collection<? extends Double> c) {
        boolean toReturn = observations.addAll(index,c);
        lastObservation.setValue(observations.get(observations.size() - 1));
        return toReturn;
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * @param e element to be appended to this list
     * @return <tt>true</tt> (as specified by {@link java.util.Collection#add})
     */
    public boolean add(Double e) {
        lastObservation.setValue(e);
        return observations.add(e);
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param  index index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public Double get(int index) {
        return observations.get(index);
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list
     */
    public int size() {
        return observations.size();
    }


    /**
     * returns a copy of all the observed last prices so far!
     */
    public double[] getAllRecordedObservations(){
        return Doubles.toArray(observations);

    }


    /**
     * utility method to analyze only specific days
     */
    public double[] getObservationsRecordedTheseDays(@Nonnull int[] days)
    {
        Preconditions.checkArgument(days.length > 0);
        double[] toReturn = new double[days.length];
        //fill in the array
        for(int i=0; i < days.length; i++)
            toReturn[i] = getObservationRecordedThisDay(days[i]);
        //go!
        return toReturn;

    }


    /**
     * utility method to analyze only specific days in an interval( the beginningDay and lastDay are INCLUDED, if possible)
     */
    public double[] getObservationsRecordedTheseDays(@Nonnull int beginningDay, int lastDay)
    {
        Preconditions.checkArgument(beginningDay <= lastDay);


        //go!
        return Doubles.toArray(observations.subList(dayToIndex(beginningDay), dayToIndex(lastDay+1)));

    }



    /**
     * utility method to analyze only  a specific day
     */
    public double getObservationRecordedThisDay(int day)
    {
        return observations.get(dayToIndex(day));
    }

    private int dayToIndex(int day) {
        return day-startingDay;
    }


    /**
     * Sets new something of an offset in case the observations were collected only after a specific day.
     *
     * @param startingDay New value of something of an offset in case the observations were collected only after a specific day.
     */
    public void setStartingDay(int startingDay) {
        this.startingDay = startingDay;
    }

    /**
     * Gets something of an offset in case the observations were collected only after a specific day.
     *
     * @return Value of something of an offset in case the observations were collected only after a specific day.
     */
    public int getStartingDay() {
        return startingDay;
    }


    public Double getLastObservation()
    {
        return observations.get(observations.size()-1);
    }

    /**
     * this returns an observableDoubleValue that updates every time a new observation is received.
     * @return
     */
    public ObservableDoubleValue getObservableLastObservation()
    {
        return lastObservation;
    }


}
