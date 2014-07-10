/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.regression;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class AutovarianceReweighterDecoratorTest {


    //add observations from a line for the first 20 points but then start feeding it the same number over and over again, the weight should become lower and lower


    @Test
    public void deathToEquilibriumLearning() throws Exception {

        ArgumentCaptor<Double> captor = ArgumentCaptor.forClass(Double.class);
        RecursiveLinearRegression decorated = mock(RecursiveLinearRegression.class);
        //only care about y and the first non-intercept
        AutovarianceReweighterDecorator decorator = new AutovarianceReweighterDecorator(decorated,5,1);

        for(double i=40; i>35; i--)
        {
            final double garbageXToIgnore = Math.random();
            decorator.addObservation(1,i,1,i, garbageXToIgnore);
            verify(decorated).addObservation(captor.capture(),eq(i),eq(1d),eq(i),eq(garbageXToIgnore));
            //first 5 should be 100
            Assert.assertEquals(100,captor.getValue(),.0001f);
        }
        for(double i=35; i>20; i--)
        {
            final double garbageXToIgnore = Math.random();
            decorator.addObservation(1,i,1,i, garbageXToIgnore);
            verify(decorated).addObservation(captor.capture(),eq(i),eq(1d),eq(i),eq(garbageXToIgnore));
            //variance is steady but average decreasing so I expect all the weights to be slightly higher
            Assert.assertTrue(100<captor.getValue());

        }
        //now reach equilibrium!
        for(int i=0; i<10;i++)
        {
            final double garbageXToIgnore = Math.random();
            decorator.addObservation(1d,21d,1d,21d, garbageXToIgnore);
            verify(decorated).addObservation(captor.capture(),eq(21d),eq(1d),eq(21d),eq(garbageXToIgnore));
            //variance is decreasing faster than average, I expect the weights given to each observation to decrease
            Assert.assertTrue(captor.getAllValues().get(captor.getAllValues().size()-2)>captor.getValue() || captor.getValue().equals(0d));
        }
        //in fact in the end it should be 0
        decorator.addObservation(1,21,1,21, 5);
        verify(decorated).addObservation(captor.capture(),eq(21d),eq(1d),eq(21d),eq(5d));
        Assert.assertEquals(0,captor.getValue(),.001f);

        System.out.println(captor.getAllValues());


    }
}