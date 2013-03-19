/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package financial.utilities;

import com.google.common.collect.ForwardingQueue;
import sim.util.media.chart.HistogramGenerator;

import java.util.Collection;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * <h4>Description</h4>
 * <p/>  This is basically a decorator of the priority queue that also has a link to an histogram generator and updates it regularly.
 * It has to call "toArray" because JFreechart is unwieldy.
 * <p/> I extends ForwardingQueue in the JavaCollection that is nice because that way I don't havet o delegate EVERYTHING
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>  Since I use it only for orderbook I assume that the priority queue given holds queues.
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-09-08
 * @see
 */
public class HistogramDecoratedPriorityBook extends ForwardingQueue<Quote> {


    final private PriorityQueue<Quote> book;

    final private HistogramGenerator histogramGenerator;

    final private int index;

    final private String name;

    /**
     * Create the decorated book
     * @param book the part of the order book we need decorated
     * @param histogramGenerator a link to the histogram generator!
     * @param index the index of the book as a series in the histogram
     */
    public HistogramDecoratedPriorityBook(PriorityQueue<Quote> book, HistogramGenerator histogramGenerator, int index, String name) {
        this.book = book;
        this.histogramGenerator = histogramGenerator;
        this.index = index;
        this.name = name;
    }

    @Override
    protected Queue delegate() {

        return book;
    }

    /**
     * Like the super function, but also updates the histogram before returning
     */
    @Override
    public Quote remove() {
        Quote q = super.remove();
        if(q != null)
            updateHistogram();
        return q;
    }

    /**
     * Like the super function, but also updates the histogram before returning
     */
    @Override
      public Quote poll() {
        Quote q = super.poll();
        if(q != null)
            updateHistogram();
        return q;
    }

    /**
     * Like the super function, but also updates the histogram before returning
     */
    @Override
    public boolean removeAll(Collection<?> collection) {
        boolean removed = super.removeAll(collection);
        if(removed)
            updateHistogram();
        return removed;

    }

    /**
     * Like the super function, but also updates the histogram before returning
     */
    @Override
    public boolean remove(Object object) {
        boolean removed = super.remove(object);
        if(removed)
            updateHistogram();
        return removed;

    }

    /**
     * Like the super function, but also updates the histogram before returning
     */
    @Override
    public boolean offer(Quote o) {
        boolean added = super.offer(o);
        if(added)
            updateHistogram();
        return added;

    }

    /**
     * Like the super function, but also updates the histogram before returning
     */
    @Override
    public boolean add(Quote element) {
        boolean added = super.add(element);
        if(added)
            updateHistogram();
        return added;
    }

    /**
     * Like the super function, but also updates the histogram before returning
     */
    @Override
    public boolean addAll(Collection<? extends Quote> collection) {
        boolean added = super.addAll(collection);
        if(added)
            updateHistogram();
        return added;
    }

    /**
     * Like the super function, but also updates the histogram before returning
     */
    @Override
    public boolean retainAll(Collection<?> collection) {
        boolean added = super.retainAll(collection);
        if(added)
            updateHistogram();
        return added;
    }



    /**
     * This is the worst possible solution to my problems. I am sure somewhere Dijkstra is cursing my name
     */
    public void updateHistogram(){

        //create the array
        double[] array = new double[book.size()]; int i=0;
        for(Quote q : book)
        {
            array[i] = q.getPriceQuoted();
            i++;
        }

        histogramGenerator.updateSeries(index, array);

    }
}
