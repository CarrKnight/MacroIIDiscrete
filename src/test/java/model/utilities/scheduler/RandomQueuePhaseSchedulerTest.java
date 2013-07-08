/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.scheduler;

import ec.util.MersenneTwisterFast;
import model.MacroII;
import model.utilities.ActionOrder;
import org.junit.Assert;
import org.junit.Test;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.LinkedList;

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
 * @version 2013-07-08
 * @see
 */
public class RandomQueuePhaseSchedulerTest {

    //schedule two different actions at the same time and priority. Make sure that if I do it 10 times, their orders change a bit
    @Test
    public void isRandomTest()
    {

        int countHowManyTimes0IsWritten = 0;

        for(int i =0 ; i <100; i++)
        {

            final LinkedList<Integer> listToCheckForOrder = new LinkedList<>();

            RandomQueuePhaseScheduler scheduler = new RandomQueuePhaseScheduler(1,new MersenneTwisterFast(i));

            scheduler.scheduleSoon(ActionOrder.values()[0],new Steppable() {
                @Override
                public void step(SimState state) {
                    listToCheckForOrder.add(1);

                }
            },Priority.AFTER_STANDARD);

            scheduler.scheduleSoon(ActionOrder.values()[0],new Steppable() {
                @Override
                public void step(SimState state) {
                    listToCheckForOrder.add(0);

                }
            },Priority.AFTER_STANDARD);

            MacroII macroII = new MacroII(1l);
            scheduler.step(macroII);
            if(listToCheckForOrder.get(0)==0)
                countHowManyTimes0IsWritten++;

        }

        Assert.assertTrue(countHowManyTimes0IsWritten > 0);
        Assert.assertTrue(countHowManyTimes0IsWritten < 100);

    }

    //make sure the priority is working alright

    @Test
    public void priorityTest()
    {

        final LinkedList<Integer> listToCheckForOrder = new LinkedList<>();


        RandomQueuePhaseScheduler scheduler = new RandomQueuePhaseScheduler(1,new MersenneTwisterFast(System.currentTimeMillis()));

        scheduler.scheduleSoon(ActionOrder.values()[0],new Steppable() {
            @Override
            public void step(SimState state) {
                listToCheckForOrder.add(1);

            }
        });

        scheduler.scheduleSoon(ActionOrder.values()[0],new Steppable() {
            @Override
            public void step(SimState state) {
                listToCheckForOrder.add(0);

            }
        },Priority.BEFORE_STANDARD);

        scheduler.scheduleSoon(ActionOrder.values()[1],new Steppable() {
            @Override
            public void step(SimState state) {
                listToCheckForOrder.add(200);

            }
        });

        scheduler.scheduleSoon(ActionOrder.values()[1],new Steppable() {
            @Override
            public void step(SimState state) {
                listToCheckForOrder.add(100);

            }
        },Priority.BEFORE_STANDARD);

        MacroII macroII = new MacroII(1l);
        scheduler.step(macroII);


        Assert.assertEquals((int)listToCheckForOrder.get(0),0);
        Assert.assertEquals((int)listToCheckForOrder.get(1),1);
        Assert.assertEquals((int)listToCheckForOrder.get(2),100);
        Assert.assertEquals((int)listToCheckForOrder.get(3),200);


    }


    //same test as before but with "tomorrow"  schedules

    //make sure the priority is working alright

    @Test
    public void priorityTomorrowTest()
    {

        final LinkedList<Integer> listToCheckForOrder = new LinkedList<>();


        final RandomQueuePhaseScheduler scheduler = new RandomQueuePhaseScheduler(1,new MersenneTwisterFast(System.currentTimeMillis()));

        scheduler.scheduleSoon(ActionOrder.values()[0],new Steppable() {
            @Override
            public void step(SimState state) {
                listToCheckForOrder.add(1);
                scheduler.scheduleTomorrow(ActionOrder.values()[0],new Steppable() {
                    @Override
                    public void step(SimState state) {
                        listToCheckForOrder.add(200);

                    }
                });
                scheduler.scheduleTomorrow(ActionOrder.values()[0], new Steppable() {
                    @Override
                    public void step(SimState state) {
                        listToCheckForOrder.add(100);

                    }
                }, Priority.BEFORE_STANDARD);
            }
        });

        scheduler.scheduleSoon(ActionOrder.values()[0],new Steppable() {
            @Override
            public void step(SimState state) {
                listToCheckForOrder.add(0);

            }
        },Priority.BEFORE_STANDARD);





        MacroII macroII = new MacroII(1l);
        scheduler.step(macroII);
        scheduler.step(macroII);



        Assert.assertEquals((int)listToCheckForOrder.get(0),0);
        Assert.assertEquals((int)listToCheckForOrder.get(1),1);
        Assert.assertEquals((int)listToCheckForOrder.get(2),100);
        Assert.assertEquals((int)listToCheckForOrder.get(3),200);


    }



    @Test
    public void priorityInFutureTest()
    {

        final LinkedList<Integer> listToCheckForOrder = new LinkedList<>();


        final RandomQueuePhaseScheduler scheduler = new RandomQueuePhaseScheduler(1,new MersenneTwisterFast(System.currentTimeMillis()));

        scheduler.scheduleSoon(ActionOrder.values()[0],new Steppable() {
            @Override
            public void step(SimState state) {
                listToCheckForOrder.add(1);
                scheduler.scheduleAnotherDay(ActionOrder.values()[0],new Steppable() {
                    @Override
                    public void step(SimState state) {
                        listToCheckForOrder.add(200);

                    }
                },2);
                scheduler.scheduleAnotherDay(ActionOrder.values()[0], new Steppable() {
                    @Override
                    public void step(SimState state) {
                        listToCheckForOrder.add(100);

                    }
                }, 2,Priority.BEFORE_STANDARD);
            }
        });

        scheduler.scheduleSoon(ActionOrder.values()[0],new Steppable() {
            @Override
            public void step(SimState state) {
                listToCheckForOrder.add(0);

            }
        },Priority.BEFORE_STANDARD);





        MacroII macroII = new MacroII(1l);
        scheduler.step(macroII);
        scheduler.step(macroII);
        scheduler.step(macroII);




        Assert.assertEquals((int)listToCheckForOrder.get(0),0);
        Assert.assertEquals((int)listToCheckForOrder.get(1),1);
        Assert.assertEquals((int)listToCheckForOrder.get(2),100);
        Assert.assertEquals((int)listToCheckForOrder.get(3),200);


    }
    
    
}
