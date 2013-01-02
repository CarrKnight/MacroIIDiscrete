package model.utilities.pid;

import model.utilities.filters.MovingAverage;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * <h4>Description</h4>
 * <p/>
 * <p/>
 * <p/>
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
public class MovingAverageTest {


    double expected[] = new double[]{1,
            1.5,2,2.5,3,3.5,4,4.5,5
            ,5.5,6,6.5,7,7.5,8,8.5,9
            ,9.5,10,10.4
            ,11.2,11.9
            ,12.5,13
            ,13.4,13.7
            ,13.9,14
            ,14  ,13.9
            ,13.7,13.4
            ,13  ,12.5
            ,11.9,11.2
            ,10.4,9.6
            ,8.8 ,8.1
            ,7.5,7
            ,6.6,6.3
            ,6.1,6
            ,6,6.1
            ,6.3,6.6
            ,7,7.5
            ,8.1,8.8
            ,9.6,10.4
            ,11.2,11.9
            ,12.5,13
            ,13.4,13.7
            ,13.9,14
            ,14,13.9
            ,13.7,13.4
            ,13,12.5
            ,11.9,11.2
            ,10.4};


    @Test
    public void testGetAverage() throws Exception {

        MovingAverage<Float> ma = new MovingAverage<>(20);

        int index = 0;
        for(int i=0; i < 19; i++){
            ma.addObservation(i+1f);
            System.out.println(i + ", " + ma.getSmoothedObservation());
            assertEquals(expected[index],ma.getSmoothedObservation(),.001f);
            index++;

        }

        for(int i=18; i>0; i--){
            ma.addObservation((float)i);
            assertEquals(expected[index],ma.getSmoothedObservation(),.001f);
            index++;

        }




    }
}
