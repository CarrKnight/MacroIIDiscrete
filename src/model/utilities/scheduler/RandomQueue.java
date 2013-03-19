/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.scheduler;

import ec.util.MersenneTwisterFast;

import java.util.PriorityQueue;

/**
 * <h4>Description</h4>
 * <p/> A simple object delegating everything to a priority queue but giving each element a random integer to be sorted on.
 * Strictly speaking then, there is a VERY VERY small advantage in being put in the queue earlier as there is a small chance that
 * the new entry will be given the same integer. But hopefully it is going to be way faster than removing and adding in arraylists
 * <p/> I am not really implementing interfaces because i need a very limited set of operations
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-02-11
 * @see
 */
public class RandomQueue<T>  {


    private final PriorityQueue<Entry<T>> delegate;

    private MersenneTwisterFast randomizer;

    public RandomQueue(MersenneTwisterFast randomizer) {
        this.delegate =  new PriorityQueue<>();
        this.randomizer = randomizer;
    }

    /**
     * Inserts the specified element into this queue if it is possible to do so
     * immediately without violating capacity restrictions, returning
     * <tt>true</tt> upon success and throwing an <tt>IllegalStateException</tt>
     * if no space is currently available.
     *
     * @param t the element to add
     * @return <tt>true</tt> (as specified by {@link java.util.Collection#add})
     * @throws IllegalStateException    if the element cannot be added at this
     *                                  time due to capacity restrictions
     * @throws ClassCastException       if the class of the specified element
     *                                  prevents it from being added to this queue
     * @throws NullPointerException     if the specified element is null and
     *                                  this queue does not permit null elements
     * @throws IllegalArgumentException if some property of this element
     *                                  prevents it from being added to this queue
     */
    public boolean add(T t) {
        Entry<T> e = new Entry<>(t);
        return delegate.add(e);

    }

    /**
     * Inserts the specified element into this queue if it is possible to do
     * so immediately without violating capacity restrictions.
     * When using a capacity-restricted queue, this method is generally
     * preferable to {@link #add}, which can fail to insert an element only
     * by throwing an exception.
     *
     * @param t the element to add
     * @return <tt>true</tt> if the element was added to this queue, else
     *         <tt>false</tt>
     * @throws ClassCastException       if the class of the specified element
     *                                  prevents it from being added to this queue
     * @throws NullPointerException     if the specified element is null and
     *                                  this queue does not permit null elements
     * @throws IllegalArgumentException if some property of this element
     *                                  prevents it from being added to this queue
     */
    public boolean offer(T t) {
        Entry<T> e = new Entry<>(t);
        return delegate.offer(e);
    }

    /**
     * Retrieves and removes the head of this queue.  This method differs
     * from {@link #poll poll} only in that it throws an exception if this
     * queue is empty.
     *
     * @return the head of this queue
     * @throws java.util.NoSuchElementException
     *          if this queue is empty
     */
    public T remove() {
        Entry<T> e = delegate.remove();
        if(e==null)
            return null;
        else
            return e.getElement();
    }

    /**
     * Retrieves and removes the head of this queue,
     * or returns <tt>null</tt> if this queue is empty.
     *
     * @return the head of this queue, or <tt>null</tt> if this queue is empty
     */
    public T poll() {
        Entry<T> e = delegate.poll();
        if(e==null)
            return null;
        else
            return e.getElement();    }

    /**
     * Retrieves, but does not remove, the head of this queue.  This method
     * differs from {@link #peek peek} only in that it throws an exception
     * if this queue is empty.
     *
     * @return the head of this queue
     * @throws java.util.NoSuchElementException
     *          if this queue is empty
     */
    public T element() {
        return delegate.element().getElement(); //throws an exception otherwise
    }


    /**
     * Removes all of the elements from this priority queue.
     * The queue will be empty after this call returns.
     */
    public void clear() {
        delegate.clear();
    }

    public int size() {
        return delegate.size();
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation returns <tt>size() == 0</tt>.
     */
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    /**
     * Returns a string representation of this collection.  The string
     * representation consists of a list of the collection's elements in the
     * order they are returned by its iterator, enclosed in square brackets
     * (<tt>"[]"</tt>).  Adjacent elements are separated by the characters
     * <tt>", "</tt> (comma and space).  Elements are converted to strings as
     * by {@link String#valueOf(Object)}.
     *
     * @return a string representation of this collection
     */
    @Override
    public String toString() {
        return delegate.toString();
    }

    /**
     * Returns the hash code value for this collection.  While the
     * <tt>Collection</tt> interface adds no stipulations to the general
     * contract for the <tt>Object.hashCode</tt> method, programmers should
     * take note that any class that overrides the <tt>Object.equals</tt>
     * method must also override the <tt>Object.hashCode</tt> method in order
     * to satisfy the general contract for the <tt>Object.hashCode</tt> method.
     * In particular, <tt>c1.equals(c2)</tt> implies that
     * <tt>c1.hashCode()==c2.hashCode()</tt>.
     *
     * @return the hash code value for this collection
     *
     * @see Object#hashCode()
     * @see Object#equals(Object)
     */
    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    /**
     * Compares the specified object with this collection for equality. <p>
     *
     * While the <tt>Collection</tt> interface adds no stipulations to the
     * general contract for the <tt>Object.equals</tt>, programmers who
     * implement the <tt>Collection</tt> interface "directly" (in other words,
     * create a class that is a <tt>Collection</tt> but is not a <tt>Set</tt>
     * or a <tt>List</tt>) must exercise care if they choose to override the
     * <tt>Object.equals</tt>.  It is not necessary to do so, and the simplest
     * course of action is to rely on <tt>Object</tt>'s implementation, but
     * the implementor may wish to implement a "value comparison" in place of
     * the default "reference comparison."  (The <tt>List</tt> and
     * <tt>Set</tt> interfaces mandate such value comparisons.)<p>
     *
     * The general contract for the <tt>Object.equals</tt> method states that
     * equals must be symmetric (in other words, <tt>a.equals(b)</tt> if and
     * only if <tt>b.equals(a)</tt>).  The contracts for <tt>List.equals</tt>
     * and <tt>Set.equals</tt> state that lists are only equal to other lists,
     * and sets to other sets.  Thus, a custom <tt>equals</tt> method for a
     * collection class that implements neither the <tt>List</tt> nor
     * <tt>Set</tt> interface must return <tt>false</tt> when this collection
     * is compared to any list or set.  (By the same logic, it is not possible
     * to write a class that correctly implements both the <tt>Set</tt> and
     * <tt>List</tt> interfaces.)
     *
     * @param o object to be compared for equality with this collection
     * @return <tt>true</tt> if the specified object is equal to this
     * collection
     *
     * @see Object#equals(Object)
     * @see java.util.Set#equals(Object)
     * @see java.util.List#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    /**
     * Retrieves, but does not remove, the head of this queue,
     * or returns <tt>null</tt> if this queue is empty.
     *
     * @return the head of this queue, or <tt>null</tt> if this queue is empty
     */
    public T peek() {
        Entry<T> e = delegate.peek();
        if(e==null)
            return null;
        else
            return e.getElement();
    }

    public void addAll(Iterable<T> tomorrowSamePhase) {
        for(T element : tomorrowSamePhase)
            this.add(element);

    }

    private class Entry<T> implements Comparable<Entry<T>>
    {
        /**
         * the element of the queue
         */
        private final T element;

        /**
         * A random number given at construction to make it random
         */
        private final Integer randomPosition;

        private Entry(T element) {
            this.element = element;
            randomPosition = randomizer.nextInt();
        }


        public T getElement() {
            return element;
        }

        /**
         * Compares their random position
         */
        @Override
        public int compareTo(Entry<T> o) {
            return Integer.compare(this.randomPosition,o.randomPosition);
        }
    }

}
