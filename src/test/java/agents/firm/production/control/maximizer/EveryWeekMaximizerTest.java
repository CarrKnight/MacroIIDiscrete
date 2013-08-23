/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.production.control.maximizer;

import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.production.Plant;
import agents.firm.production.control.PlantControl;
import agents.firm.production.control.maximizer.algorithms.marginalMaximizers.MarginalMaximizer;
import model.MacroII;
import model.utilities.ActionOrder;
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
 * @version 2013-08-23
 * @see
 */
public class EveryWeekMaximizerTest {

    //rescheduling test

    @Test
    public void rescheduling()
    {
        MacroII model = mock(MacroII.class);
        MarginalMaximizer algorithm = mock(MarginalMaximizer.class);

        EveryWeekMaximizer <MarginalMaximizer> maximizer = new EveryWeekMaximizer<MarginalMaximizer>(
                model,mock(Firm.class),
                mock(HumanResources.class),mock(Plant.class),
                mock(PlantControl.class),algorithm
        );
        //not scheduled before start!
        verify(model,never()).scheduleAnotherDay(ActionOrder.THINK,maximizer,maximizer.getHowManyDaysBeforeEachCheck());

        maximizer.start();
        verify(model,times(1)).scheduleAnotherDay(ActionOrder.THINK,maximizer,maximizer.getHowManyDaysBeforeEachCheck());
        //start doesn't touch the algorithm
        verify(algorithm,never()).chooseWorkerTarget(anyInt(),anyFloat(),anyFloat(),anyFloat(),anyFloat(),anyFloat(),
                anyInt(),anyFloat());


        //each step reschedules and also calls the algorithm
        maximizer.step(model);
        verify(model,times(2)).scheduleAnotherDay(ActionOrder.THINK, maximizer, maximizer.getHowManyDaysBeforeEachCheck());
        verify(algorithm,times(1)).chooseWorkerTarget(anyInt(), anyFloat(), anyFloat(), anyFloat(), anyFloat(), anyFloat(),
                anyInt(), anyFloat());
        maximizer.step(model);
        verify(model,times(3)).scheduleAnotherDay(ActionOrder.THINK,maximizer,maximizer.getHowManyDaysBeforeEachCheck());
        verify(algorithm,times(2)).chooseWorkerTarget(anyInt(),anyFloat(),anyFloat(),anyFloat(),anyFloat(),anyFloat(),
                anyInt(),anyFloat());

        maximizer.turnOff();
        maximizer.step(model);
        verify(model,times(3)).scheduleAnotherDay(ActionOrder.THINK,maximizer,maximizer.getHowManyDaysBeforeEachCheck());
        verify(algorithm,times(2)).chooseWorkerTarget(anyInt(),anyFloat(),anyFloat(),anyFloat(),anyFloat(),anyFloat(),
                anyInt(),anyFloat());



    }




}
