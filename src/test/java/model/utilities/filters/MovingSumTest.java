/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.filters;

import org.junit.Assert;
import org.junit.Test;

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
 * @version 2013-12-20
 * @see
 */
public class MovingSumTest {

    @Test
    public void testMovingSum() throws Exception {


        MovingSum<Long> movingSum = new MovingSum<>(3);
        Assert.assertTrue(Float.isNaN(movingSum.getSmoothedObservation()));
        movingSum.addObservation(10l);
        Assert.assertEquals(10f,movingSum.getSmoothedObservation(),.0001d);
        movingSum.addObservation(20l);
        Assert.assertEquals(30f,movingSum.getSmoothedObservation(),.0001d);
        movingSum.addObservation(30l);
        Assert.assertEquals(60f,movingSum.getSmoothedObservation(),.0001d);
        movingSum.addObservation(40l);
        Assert.assertEquals(90f,movingSum.getSmoothedObservation(),.0001d);

    }
}
