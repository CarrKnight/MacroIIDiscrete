/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.scenario.oil;

import agents.EconomicAgent;
import agents.people.Person;
import financial.market.OrderBookMarket;
import goods.UndifferentiatedGoodType;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import model.MacroII;
import model.scenario.MonopolistScenario;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

/**
 * Created by carrknight on 4/22/14.
 */
public class LaborMarketForOilUpdaterTest
{


    @Test
    public void makeSureItUpdates() throws Exception {

        IntegerProperty slope = new SimpleIntegerProperty(1);
        IntegerProperty intercept = new SimpleIntegerProperty(0);
        IntegerProperty workers = new SimpleIntegerProperty(2);
        MacroII model = new MacroII(1);
        model.start();

        OrderBookMarket market = new OrderBookMarket(UndifferentiatedGoodType.LABOR);

        LaborMarketForOilUpdater updater = new LaborMarketForOilUpdater(slope,intercept,workers,model,market);
        //the updater doesn't initialize, only updates
        MonopolistScenario.fillLaborSupply(0,1,true,false,2,market,model);

        //make sure it's initialized correctly
        Assert.assertTrue(market.getSellers().toString(),market.getSellers().size()==2);
        Iterator<EconomicAgent> sellerIterator = market.getSellers().iterator();
        Person worker1 = (Person) sellerIterator.next();
        Person worker2 = (Person) sellerIterator.next();
        Assert.assertNotEquals(worker1.getMinimumDailyWagesRequired(),worker2.getMinimumDailyWagesRequired());
        Assert.assertTrue(worker1.getMinimumDailyWagesRequired()==1 || worker1.getMinimumDailyWagesRequired()==2);
        Assert.assertTrue(worker2.getMinimumDailyWagesRequired()==1 || worker2.getMinimumDailyWagesRequired()==2);

        //now switch stuff around
        slope.setValue(4);
        slope.setValue(2);
        slope.setValue(1);
        intercept.setValue(1);
        model.schedule.step(model);
        Assert.assertTrue(market.getSellers().size()==2);
        sellerIterator = market.getSellers().iterator();
        worker1 = (Person) sellerIterator.next();
        worker2 = (Person) sellerIterator.next();
        Assert.assertNotEquals(worker1.getMinimumDailyWagesRequired(),worker2.getMinimumDailyWagesRequired());
        Assert.assertTrue(worker1.getMinimumDailyWagesRequired()==3 || worker1.getMinimumDailyWagesRequired()==2);
        Assert.assertTrue(worker2.getMinimumDailyWagesRequired()==3 || worker2.getMinimumDailyWagesRequired()==2);


        //again
        slope.setValue(4);
        slope.setValue(1);
        slope.setValue(2);
        intercept.setValue(1);
        model.schedule.step(model);
        Assert.assertTrue(market.getSellers().size()==2);
        sellerIterator = market.getSellers().iterator();
        worker1 = (Person) sellerIterator.next();
        worker2 = (Person) sellerIterator.next();
        Assert.assertNotEquals(worker1.getMinimumDailyWagesRequired(),worker2.getMinimumDailyWagesRequired());
        Assert.assertTrue(worker1.getMinimumDailyWagesRequired()==3 || worker1.getMinimumDailyWagesRequired()==5);
        Assert.assertTrue(worker2.getMinimumDailyWagesRequired()==3 || worker2.getMinimumDailyWagesRequired()==5);

        //if you turn it off, it stops listening
        updater.turnOff();
        slope.setValue(100);
        intercept.setValue(100);
        //the results haven't changed
        model.schedule.step(model);
        Assert.assertTrue(market.getSellers().size()==2);
        sellerIterator = market.getSellers().iterator();
        worker1 = (Person) sellerIterator.next();
        worker2 = (Person) sellerIterator.next();
        Assert.assertNotEquals(worker1.getMinimumDailyWagesRequired(),worker2.getMinimumDailyWagesRequired());
        Assert.assertTrue(worker1.getMinimumDailyWagesRequired()==3 || worker1.getMinimumDailyWagesRequired()==5);
        Assert.assertTrue(worker2.getMinimumDailyWagesRequired()==3 || worker2.getMinimumDailyWagesRequired()==5);


    }
}
