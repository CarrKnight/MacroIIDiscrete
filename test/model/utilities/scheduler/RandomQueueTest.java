package model.utilities.scheduler;

import ec.util.MersenneTwisterFast;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.powermock.api.mockito.PowerMockito.when;

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
 * @version 2013-02-11
 * @see
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(MersenneTwisterFast.class)
public class RandomQueueTest {
    @Test
    public void testRetainOrder() throws Exception {

        MersenneTwisterFast randomizer = PowerMockito.mock(MersenneTwisterFast.class);
        RandomQueue<Object> queue = new RandomQueue<>(randomizer);

        Object first = new Integer(0);
        when(randomizer.nextInt()).thenReturn(0); //this will be the position randomly given to object
        queue.add(first);


        for(int i=1; i < 100; i++)
        {
            when(randomizer.nextInt()).thenReturn(i);
            queue.add(new Integer(i));




        }

        Assert.assertEquals(first,queue.poll());





    }

    @Test
    public void testInverseOrder() throws Exception {

        MersenneTwisterFast randomizer = PowerMockito.mock(MersenneTwisterFast.class);
        RandomQueue<Object> queue = new RandomQueue<>(randomizer);




        for(int i=1; i < 100; i++)
        {
            when(randomizer.nextInt()).thenReturn(i);
            queue.add(new Integer(i));




        }
        Object last = new Integer(999);
        when(randomizer.nextInt()).thenReturn(0); //this will be the position randomly given to object
        queue.add(last);

        Assert.assertEquals(last,queue.poll());


    }
}
