package model.utilities.scheduler;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import sim.engine.Steppable;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.mock;
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
        RandomQueue queue = new RandomQueue(randomizer);

        PrioritySteppablePair first = new PrioritySteppablePair(mock(Steppable.class),Priority.STANDARD);

        when(randomizer.nextFloat()).thenReturn(0f); //this will be the position randomly given to object
        queue.add(first);


        for(int i=1; i < 100; i++)
        {
            when(randomizer.nextFloat()).thenReturn((float)i);
            PrioritySteppablePair pair = new PrioritySteppablePair(mock(Steppable.class),Priority.STANDARD);
            queue.add(pair);




        }

        assertEquals(first,queue.poll());





    }

    @Test
    public void testInverseOrder() throws Exception {

        MersenneTwisterFast randomizer = PowerMockito.mock(MersenneTwisterFast.class);
        RandomQueue queue = new RandomQueue(randomizer);




        for(int i=1; i < 100; i++)
        {
            when(randomizer.nextFloat()).thenReturn((float)(i));
            queue.add(new PrioritySteppablePair(mock(Steppable.class),Priority.STANDARD));




        }
        PrioritySteppablePair last = new PrioritySteppablePair(mock(Steppable.class),Priority.STANDARD);
        when(randomizer.nextFloat()).thenReturn((float)0); //this will be the position randomly given to object
        queue.add(last);

        assertEquals(last,queue.poll());


    }
}
