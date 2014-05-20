/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.production.control.maximizer.algorithms.marginalMaximizers;

import agents.firm.personell.HumanResources;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.*;

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
 * @version 2013-09-06
 * @see
 */
public class RobustMarginalMaximizerTest {

    @Test
    public void robustMaximizerTest()
    {
        HumanResources hr = mock(HumanResources.class);
        MarginalMaximizer maximizer = mock(MarginalMaximizer.class);
        when(maximizer.getHr()).thenReturn(hr);

        //imagine that we have 10 workers, and the maximizer says to hire one more
        when(hr.getNumberOfWorkers()).thenReturn(10);
        when(maximizer.chooseWorkerTarget(10, 0, 0, 0, 0, 0, 0, 0)).thenReturn(11);

        //create the "robust" maximizer
        RobustMarginalMaximizer robust = new RobustMarginalMaximizer(maximizer);
        //now imagine our old target used to be 6, then even though we have 10 workers, we really want our new target to be 7, not 11
        Assert.assertEquals(robust.chooseWorkerTarget(6,0,0,0,0,0,0,0),7);

        //notice instead that when our target was 6 but we find ourselves with 10 workers, the order to reduce the number of workers is useless
        //so we ignore it and stay at 6
        when(maximizer.chooseWorkerTarget(10, 0, 0, 0, 0, 0, 0, 0)).thenReturn(9);
        Assert.assertEquals(robust.chooseWorkerTarget(6, 0, 0, 0, 0, 0, 0, 0), 6);




    }


}
