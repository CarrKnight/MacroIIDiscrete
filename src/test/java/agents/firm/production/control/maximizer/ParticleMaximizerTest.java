/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.production.control.maximizer;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.production.Blueprint;
import agents.firm.production.Plant;
import agents.firm.production.control.PlantControl;
import agents.firm.production.control.maximizer.algorithms.hillClimbers.ParticleMaximizer;
import ec.util.MersenneTwisterFast;
import goods.GoodType;
import model.MacroII;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
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
 * @author Ernesto
 * @version 2012-10-23
 * @see
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(MersenneTwisterFast.class)
public class ParticleMaximizerTest {


    static private final GoodType BEEF = new GoodType("testBeef","beef");

    @Test
    public void testCurrentOrNeighborTarget(){

        HumanResources hr = mock(HumanResources.class);
        MersenneTwisterFast random = PowerMockito.mock(MersenneTwisterFast.class);
        when(hr.getRandom()).thenReturn(random);
        Plant plant = mock(Plant.class); when(plant.weeklyFixedCosts()).thenReturn(0l); when(hr.getPlant()).thenReturn(plant);
        PlantControl pc = mock(PlantControl.class);
        //hr.getFirm().getModel().getWeekLength();
        Firm firm = mock(Firm.class);
        MacroII model = new MacroII(1l); when(firm.getModel()).thenReturn(model); when(hr.getFirm()).thenReturn(firm);
        when(hr.getTime()).thenReturn(10d);


        Set<EconomicAgent> employers = new LinkedHashSet<>(); employers.add(firm);
        when(firm.getRandomPlantProducingThis(BEEF)).thenReturn(plant);
        when(firm.getPlantProfits(plant)).thenReturn(1f);
        //add 2 competitors
        Firm competitor1 = mock(Firm.class); Plant plant1 = mock(Plant.class); when(plant1.getNumberOfWorkers()).thenReturn(100);
        when(competitor1.getRandomPlantProducingThis(any(GoodType.class))).thenReturn(plant1); //return whatever mock you want
        when(competitor1.getPlantProfits(plant1)).thenReturn(2f); //competitor 1 is better than you!
        Firm competitor2 = mock(Firm.class);  Plant plant2 = mock(Plant.class); when(plant2.getNumberOfWorkers()).thenReturn(100);
        when(competitor2.getRandomPlantProducingThis(any(GoodType.class))).thenReturn(plant2); //return whatever mock you want
        when(competitor2.getPlantProfits(plant2)).thenReturn(-2f); //competitor 1 is better than you!
        employers.add(competitor1); employers.add(competitor2);

        //other mocking initialization, my good look at what I have done!
        when(hr.getAllEmployers()).thenReturn(employers);
        when(plant.getBlueprint()).thenReturn(Blueprint.simpleBlueprint(GoodType.GENERIC,1,BEEF,1));
        when(pc.getHr()).thenReturn(hr); when(plant.maximumWorkersPossible()).thenReturn(100); when(plant.minimumWorkersNeeded()).thenReturn(0);
        when(plant.getNumberOfWorkers()).thenReturn(1);
        when(random.nextGaussian()).thenReturn(0d);
        ParticleMaximizer maximizer = new ParticleMaximizer(plant.weeklyFixedCosts(),plant.minimumWorkersNeeded(),plant.maximumWorkersPossible()
        , random,hr);
        maximizer.setTimeToSpendHillClimbing(0);

        try{
            //use reflection to gain access to that function
            Method method = ParticleMaximizer.class.getDeclaredMethod("currentOrNeighborTarget", int.class, float.class);
            method.setAccessible(true);

            when(random.nextInt(3)).thenReturn(0);
            Integer target = (Integer) method.invoke(maximizer,1,0);  //compares to itself
            assertEquals(target.intValue(), 1);
            //compare to the most succesful one
            when(random.nextInt(3)).thenReturn(1);
            target = (Integer) method.invoke(maximizer,1,0);  //compares to the succesful one
            assertEquals(target.intValue(),100);
            //compare to the least succesful one
            when(random.nextInt(3)).thenReturn(2);
            target = (Integer) method.invoke(maximizer,1,0);  //compares to the succesful one
            assertEquals(target.intValue(),1);

            verify(random, times(3)).nextInt(3);
        }
        catch (Exception e){
            Assert.fail();


        }
    }


    @Test
    public void testBest(){

        HumanResources hr = mock(HumanResources.class);
        MersenneTwisterFast random = PowerMockito.mock(MersenneTwisterFast.class);
        when(hr.getRandom()).thenReturn(random);
        Plant plant = mock(Plant.class); when(plant.weeklyFixedCosts()).thenReturn(0l); when(hr.getPlant()).thenReturn(plant);
        PlantControl pc = mock(PlantControl.class);
        //hr.getFirm().getModel().getWeekLength();
        Firm firm = mock(Firm.class);
        MacroII model = new MacroII(1l); when(firm.getModel()).thenReturn(model); when(hr.getFirm()).thenReturn(firm);
        when(hr.getTime()).thenReturn(10d);


        Set<EconomicAgent> employers = new LinkedHashSet<>(); employers.add(firm);
        when(firm.getRandomPlantProducingThis(BEEF)).thenReturn(plant);
        when(firm.getPlantProfits(plant)).thenReturn(1f);
        //add 2 competitors
        Firm competitor1 = mock(Firm.class); Plant plant1 = mock(Plant.class); when(plant1.getNumberOfWorkers()).thenReturn(100);
        when(competitor1.getRandomPlantProducingThis(any(GoodType.class))).thenReturn(plant1); //return whatever mock you want
        when(competitor1.getPlantProfits(plant1)).thenReturn(2f); //competitor 1 is better than you!
        Firm competitor2 = mock(Firm.class);  Plant plant2 = mock(Plant.class); when(plant2.getNumberOfWorkers()).thenReturn(100);
        when(competitor2.getRandomPlantProducingThis(any(GoodType.class))).thenReturn(plant2); //return whatever mock you want
        when(competitor2.getPlantProfits(plant2)).thenReturn(-2f); //competitor 1 is better than you!
        employers.add(competitor1); employers.add(competitor2);

        //other mocking initialization, my good look at what I have done!
        when(hr.getAllEmployers()).thenReturn(employers);
        when(plant.getBlueprint()).thenReturn(Blueprint.simpleBlueprint(GoodType.GENERIC,1,BEEF,1));
        when(pc.getHr()).thenReturn(hr); when(plant.maximumWorkersPossible()).thenReturn(100); when(plant.minimumWorkersNeeded()).thenReturn(0);
        when(plant.getNumberOfWorkers()).thenReturn(1);
        when(random.nextGaussian()).thenReturn(0d);
        ParticleMaximizer maximizer = new ParticleMaximizer(plant.weeklyFixedCosts(),plant.minimumWorkersNeeded(),plant.maximumWorkersPossible()
                , random,hr);
        maximizer.setTimeToSpendHillClimbing(0);

        try{
            //use reflection to gain access to that function
            Method method = ParticleMaximizer.class.getDeclaredMethod("bestNeighborTarget", int.class, float.class);
            method.setAccessible(true);

            when(random.nextInt(3)).thenReturn(0);
            Integer target = (Integer) method.invoke(maximizer,1,0);  //compares to itself
            assertEquals(target.intValue(),100);
            //compare to the most succesful one
            when(random.nextInt(3)).thenReturn(1);
            target = (Integer) method.invoke(maximizer,1,0);  //compares to the succesful one
            assertEquals(target.intValue(),100);
            //compare to the least succesful one
            when(random.nextInt(3)).thenReturn(2);
            target = (Integer) method.invoke(maximizer,1,0);  //compares to the succesful one
            assertEquals(target.intValue(),100);

        }
        catch (Exception e){
            Assert.fail();


        }
    }

    private long profitFunction(int x){
        if(x==1 || x==3)
            return 1;
        if(x==2)
            return 2;
        else
            return 0;

    }

}
