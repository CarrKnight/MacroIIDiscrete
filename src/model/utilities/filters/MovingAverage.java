package model.utilities.filters;

import java.util.LinkedList;

/**
 * <h4>Description</h4>
 * <p/> An object to compute the moving average of whatever is put in.
 * <p/> It accepts any number but the computations are all done through float value call
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2012-11-15
 * @see
 */
public class MovingAverage<T extends Number> implements Filter<T>{

    /**
     * Where we keep all the observations
     */
    LinkedList<T> lastElements = new LinkedList<>();

    /**
     * The size of the queue
     */
    final private int movingAverageSize;

    /**
     * the constructor that creates the moving average object
     */
    public MovingAverage(int movingAverageSize) {
        this.movingAverageSize = movingAverageSize;
    }

    /**
     * Add a new observation to the moving average
     * @param observation
     */
    public void addObservation(T observation){

        //add the last observation
        lastElements.addLast(observation);
        //if the queue is full, remove the first guy
        if(lastElements.size()>movingAverageSize)
            lastElements.removeFirst();

        assert lastElements.size() <=movingAverageSize;


    }

    public float getSmoothedObservation(){
        //if the stack is still not ready, just return the last

        assert lastElements.size()<=movingAverageSize;

        float n = lastElements.size();
        float sum = 0f;

        for(Number observation : lastElements)
            sum += observation.floatValue();

        return sum/n;




    }

}


