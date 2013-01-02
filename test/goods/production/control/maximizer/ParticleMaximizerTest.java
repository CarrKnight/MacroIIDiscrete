package goods.production.control.maximizer;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import ec.util.MersenneTwisterFast;
import goods.GoodType;
import goods.production.Blueprint;
import goods.production.Plant;
import goods.production.control.PlantControl;
import junit.framework.Assert;
import model.MacroII;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
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


    @Test
    public void testFormula()
    {
        HumanResources hr = mock(HumanResources.class);
        MersenneTwisterFast random = PowerMockito.mock(MersenneTwisterFast.class);
        when(hr.getRandom()).thenReturn(random);
        Plant plant = mock(Plant.class); when(plant.weeklyFixedCosts()).thenReturn(0l); when(hr.getPlant()).thenReturn(plant);
        PlantControl pc = mock(PlantControl.class);
        //hr.getFirm().getModel().getWeekLength();
        Firm firm = mock(Firm.class);
        MacroII model = new MacroII(1l); when(firm.getModel()).thenReturn(model); when(hr.getFirm()).thenReturn(firm);
        when(hr.getTime()).thenReturn(10d);
        HashSet<EconomicAgent> employers = new HashSet<>(); employers.add(firm);
        when(hr.getAllEmployers()).thenReturn(employers);
        when(plant.getBlueprint()).thenReturn(Blueprint.simpleBlueprint(GoodType.GENERIC,1,GoodType.BEEF,1));
        when(firm.getRandomPlantProducingThis(GoodType.BEEF)).thenReturn(plant);
        when(firm.getPlantProfits(plant)).thenReturn(1f);
        when(pc.getHr()).thenReturn(hr); when(plant.maximumWorkersPossible()).thenReturn(100); when(plant.minimumWorkersNeeded()).thenReturn(0);
        when(plant.workerSize()).thenReturn(1);




        when(random.nextGaussian()).thenReturn(0d);
        ParticleMaximizer maximizer = new ParticleMaximizer(hr,pc);
        assertEquals(maximizer.getTimeToSpendHillClimbing(),50000,.001f);
        maximizer.setTimeToSpendHillClimbing(0);
        assertTrue(maximizer.getTimeToSpendHillClimbing() < hr.getTime());
        assertEquals(maximizer.direction(1,2,3,4),1);             //should always return 1 in the particle maximization phase

        assertTrue(maximizer.checkMemory(1,2)); //it shouldn't check


        //set the attractions
        maximizer.setVelocityInertia(.75f);assertEquals(.75f,maximizer.getVelocityInertia());
        maximizer.setPersonalBestAttraction(.15f); assertEquals(.15f, maximizer.getPersonalBestAttraction());
        maximizer.setNeighborAttraction(.15f); assertEquals(.15f, maximizer.getNeighborAttraction());
        maximizer.setBestAttraction(.25f);assertEquals(.25f, maximizer.getBestAttraction());

        when(random.nextFloat()).thenReturn(1f);

        int nextStep = maximizer.chooseWorkerTarget(1,profitFunction(1),0,profitFunction(0));
        when(firm.getPlantProfits(plant)).thenReturn((float) profitFunction(nextStep));        when(plant.workerSize()).thenReturn(nextStep);

        assertEquals(nextStep,2);
        assertEquals(maximizer.getCurrentVelocity(),.75f,.001f);

        //once again, this time should move to three
        nextStep = maximizer.chooseWorkerTarget(2,profitFunction(2),1,profitFunction(1));
        when(firm.getPlantProfits(plant)).thenReturn((float)profitFunction(nextStep));        when(plant.workerSize()).thenReturn(nextStep);

        assertEquals(nextStep,3);
        assertEquals(maximizer.getCurrentVelocity(),0.5625f,.001f);

        //it's going to stay at 3 for a while now
        nextStep = maximizer.chooseWorkerTarget(3,profitFunction(3),3,profitFunction(3));
        when(firm.getPlantProfits(plant)).thenReturn((float)profitFunction(nextStep));
        when(plant.workerSize()).thenReturn(nextStep);
        assertEquals(nextStep, 3);
        assertEquals(maximizer.getCurrentVelocity(), 0.271875f, .001f);

        nextStep = maximizer.chooseWorkerTarget(3,profitFunction(3),3,profitFunction(3));
        when(firm.getPlantProfits(plant)).thenReturn((float)profitFunction(nextStep));
        when(plant.workerSize()).thenReturn(nextStep);
        assertEquals(nextStep,3);
        assertEquals(maximizer.getCurrentVelocity(),0.05390625f,.001f);


        nextStep = maximizer.chooseWorkerTarget(3,profitFunction(3),3,profitFunction(3));
        when(firm.getPlantProfits(plant)).thenReturn((float)profitFunction(nextStep));
        when(plant.workerSize()).thenReturn(nextStep);
        assertEquals(nextStep,3);
        assertEquals(maximizer.getCurrentVelocity(),-0.1095703125,.001f);

        nextStep = maximizer.chooseWorkerTarget(3,profitFunction(3),3,profitFunction(3));
        when(firm.getPlantProfits(plant)).thenReturn((float)profitFunction(nextStep));
        when(plant.workerSize()).thenReturn(nextStep);
        assertEquals(nextStep,3);
        assertEquals(maximizer.getCurrentVelocity(),-0.2321777344,.001f);


        nextStep = maximizer.chooseWorkerTarget(3,profitFunction(3),3,profitFunction(3));
        assertEquals(nextStep,3);
        assertEquals(maximizer.getCurrentVelocity(),-0.3241333008f,.001f);

        nextStep = maximizer.chooseWorkerTarget(3,profitFunction(3),3,profitFunction(3));
        assertEquals(nextStep,3);
        assertEquals(maximizer.getCurrentVelocity(),-0.3930999756f,.001f);

        nextStep = maximizer.chooseWorkerTarget(3,profitFunction(3),3,profitFunction(3));
        assertEquals(nextStep,3);
        assertEquals(maximizer.getCurrentVelocity(),-0.4448249817,.001f);

        nextStep = maximizer.chooseWorkerTarget(3,profitFunction(3),3,profitFunction(3));
        assertEquals(nextStep,3);
        assertEquals(maximizer.getCurrentVelocity(),-0.4836187363,.001f);

        nextStep = maximizer.chooseWorkerTarget(3,profitFunction(3),3,profitFunction(3));
        assertEquals(nextStep,2);
        assertEquals(maximizer.getCurrentVelocity(),-0.5127140522,.001f);
        when(firm.getPlantProfits(plant)).thenReturn((float)profitFunction(nextStep));
        when(plant.workerSize()).thenReturn(nextStep);


        nextStep = maximizer.chooseWorkerTarget(2,profitFunction(2),3,profitFunction(3));
        assertEquals(nextStep,2);
        assertEquals(maximizer.getCurrentVelocity(),-0.3845355392,.001f);

    }


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
        when(firm.getRandomPlantProducingThis(GoodType.BEEF)).thenReturn(plant);
        when(firm.getPlantProfits(plant)).thenReturn(1f);
        //add 2 competitors
        Firm competitor1 = mock(Firm.class); Plant plant1 = mock(Plant.class); when(plant1.workerSize()).thenReturn(100);
        when(competitor1.getRandomPlantProducingThis(any(GoodType.class))).thenReturn(plant1); //return whatever mock you want
        when(competitor1.getPlantProfits(plant1)).thenReturn(2f); //competitor 1 is better than you!
        Firm competitor2 = mock(Firm.class);  Plant plant2 = mock(Plant.class); when(plant2.workerSize()).thenReturn(100);
        when(competitor2.getRandomPlantProducingThis(any(GoodType.class))).thenReturn(plant2); //return whatever mock you want
        when(competitor2.getPlantProfits(plant2)).thenReturn(-2f); //competitor 1 is better than you!
        employers.add(competitor1); employers.add(competitor2);

        //other mocking initialization, my good look at what I have done!
        when(hr.getAllEmployers()).thenReturn(employers);
        when(plant.getBlueprint()).thenReturn(Blueprint.simpleBlueprint(GoodType.GENERIC,1,GoodType.BEEF,1));
        when(pc.getHr()).thenReturn(hr); when(plant.maximumWorkersPossible()).thenReturn(100); when(plant.minimumWorkersNeeded()).thenReturn(0);
        when(plant.workerSize()).thenReturn(1);
        when(random.nextGaussian()).thenReturn(0d);
        ParticleMaximizer maximizer = new ParticleMaximizer(hr,pc);
        assertEquals(maximizer.getTimeToSpendHillClimbing(),50000,.001f);
        maximizer.setTimeToSpendHillClimbing(0);

        try{
            //use reflection to gain access to that function
            Method method = ParticleMaximizer.class.getDeclaredMethod("currentOrNeighborTarget", int.class, float.class);
            method.setAccessible(true);

            when(random.nextInt(3)).thenReturn(0);
            Integer target = (Integer) method.invoke(maximizer,1,0);  //compares to itself
            Assert.assertEquals(target.intValue(),1);
            //compare to the most succesful one
            when(random.nextInt(3)).thenReturn(1);
            target = (Integer) method.invoke(maximizer,1,0);  //compares to the succesful one
            Assert.assertEquals(target.intValue(),100);
            //compare to the least succesful one
            when(random.nextInt(3)).thenReturn(2);
            target = (Integer) method.invoke(maximizer,1,0);  //compares to the succesful one
            Assert.assertEquals(target.intValue(),1);

            verify(random, times(3)).nextInt(3);
        }
        catch (Exception e){
            System.out.println(e);
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
        when(firm.getRandomPlantProducingThis(GoodType.BEEF)).thenReturn(plant);
        when(firm.getPlantProfits(plant)).thenReturn(1f);
        //add 2 competitors
        Firm competitor1 = mock(Firm.class); Plant plant1 = mock(Plant.class); when(plant1.workerSize()).thenReturn(100);
        when(competitor1.getRandomPlantProducingThis(any(GoodType.class))).thenReturn(plant1); //return whatever mock you want
        when(competitor1.getPlantProfits(plant1)).thenReturn(2f); //competitor 1 is better than you!
        Firm competitor2 = mock(Firm.class);  Plant plant2 = mock(Plant.class); when(plant2.workerSize()).thenReturn(100);
        when(competitor2.getRandomPlantProducingThis(any(GoodType.class))).thenReturn(plant2); //return whatever mock you want
        when(competitor2.getPlantProfits(plant2)).thenReturn(-2f); //competitor 1 is better than you!
        employers.add(competitor1); employers.add(competitor2);

        //other mocking initialization, my good look at what I have done!
        when(hr.getAllEmployers()).thenReturn(employers);
        when(plant.getBlueprint()).thenReturn(Blueprint.simpleBlueprint(GoodType.GENERIC,1,GoodType.BEEF,1));
        when(pc.getHr()).thenReturn(hr); when(plant.maximumWorkersPossible()).thenReturn(100); when(plant.minimumWorkersNeeded()).thenReturn(0);
        when(plant.workerSize()).thenReturn(1);
        when(random.nextGaussian()).thenReturn(0d);
        ParticleMaximizer maximizer = new ParticleMaximizer(hr,pc);
        assertEquals(maximizer.getTimeToSpendHillClimbing(),50000,.001f);
        maximizer.setTimeToSpendHillClimbing(0);

        try{
            //use reflection to gain access to that function
            Method method = ParticleMaximizer.class.getDeclaredMethod("bestNeighborTarget", int.class, float.class);
            method.setAccessible(true);

            when(random.nextInt(3)).thenReturn(0);
            Integer target = (Integer) method.invoke(maximizer,1,0);  //compares to itself
            Assert.assertEquals(target.intValue(),100);
            //compare to the most succesful one
            when(random.nextInt(3)).thenReturn(1);
            target = (Integer) method.invoke(maximizer,1,0);  //compares to the succesful one
            Assert.assertEquals(target.intValue(),100);
            //compare to the least succesful one
            when(random.nextInt(3)).thenReturn(2);
            target = (Integer) method.invoke(maximizer,1,0);  //compares to the succesful one
            Assert.assertEquals(target.intValue(),100);

        }
        catch (Exception e){
            System.out.println(e);
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
