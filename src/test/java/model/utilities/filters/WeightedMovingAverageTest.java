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
 * @version 2013-12-19
 * @see
 */
public class WeightedMovingAverageTest
{


    @Test
    public void simpleWeightedAverage() throws Exception {

        WeightedMovingAverage<Integer,Integer> ma = new WeightedMovingAverage<>(3);

        ma.addObservation(1,1);
        Assert.assertEquals(ma.getSmoothedObservation(),1,.001f);
        ma.addObservation(2,2);
        Assert.assertEquals(ma.getSmoothedObservation(),1.66667,.001f);
        ma.addObservation(2,2);
        Assert.assertEquals(ma.getSmoothedObservation(),1.8,.001f);
        ma.addObservation(2,2);
        Assert.assertEquals(ma.getSmoothedObservation(),2,.001f);



    }
}
