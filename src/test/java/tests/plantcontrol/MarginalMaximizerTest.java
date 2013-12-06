package tests.plantcontrol;

import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.production.Blueprint;
import agents.firm.production.Plant;
import agents.firm.production.control.PlantControl;
import agents.firm.production.control.maximizer.algorithms.marginalMaximizers.MarginalMaximizer;
import agents.firm.production.control.maximizer.algorithms.marginalMaximizers.MarginalMaximizerStatics;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentAllAtOnce;
import goods.GoodType;
import model.MacroII;
import model.utilities.DelayException;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
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
 * @version 2012-10-05
 * @see
 */
public class MarginalMaximizerTest {

    //test that the delay exception cascades properly
    @Test
    public void testDelay(){
        //get the constructor stuff
        HumanResources hr = mock(HumanResources.class);
        Plant p = mock(Plant.class);
        Blueprint b = new Blueprint.Builder().output(GoodType.GENERIC,1).build(); //just one output
        Firm owner = mock(Firm.class); when(p.getOwner()).thenReturn(owner); when(hr.getPlant()).thenReturn(p); when(hr.getFirm()).thenReturn(owner);
        when(owner.getModel()).thenReturn(new MacroII(1l));  when(p.getNumberOfWorkers()).thenReturn(10);
        when(hr.predictPurchasePriceWhenIncreasingProduction()).thenReturn(-1l); //tell the hr to fail at predicting
        PlantControl control = mock(PlantControl.class);
        //it should immediately fail
        MarginalMaximizer maximizer = new MarginalMaximizer(hr,control,p,owner);
        boolean exceptionThrown = false;
        try {
            float marginalProfits = MarginalMaximizerStatics.computeMarginalProfits(owner, p, hr, control, maximizer.getPolicy(), 10, 11);
            fail();
        } catch (DelayException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);


    }

    //make it maximizes in a vacuum
    @Test
    public void testMarginalProfits(){
        //get the constructor stuff
        HumanResources hr = mock(HumanResources.class);
        Plant p = mock(Plant.class); when(p.minimumWorkersNeeded()).thenReturn(0); when(p.maximumWorkersPossible()).thenReturn(1000);
        Blueprint b = new Blueprint.Builder().output(GoodType.GENERIC,1).build(); //just one output
        when(p.getBlueprint()).thenReturn(b);
        when(p.getOutputs()).thenReturn(b.getOutputs().keySet());
        Firm owner = mock(Firm.class); when(p.getOwner()).thenReturn(owner); when(hr.getPlant()).thenReturn(p); when(hr.getFirm()).thenReturn(owner);
        when(owner.getModel()).thenReturn(new MacroII(1l));  when(p.getNumberOfWorkers()).thenReturn(10);
        when(hr.predictPurchasePriceWhenIncreasingProduction()).thenReturn(-1l); //tell the hr to fail at predicting
        SalesDepartment sales = mock(SalesDepartmentAllAtOnce.class);
        when(owner.getSalesDepartment(GoodType.GENERIC)).thenReturn(sales);
        when(p.hypotheticalThroughput(anyInt(),any(GoodType.class))).thenAnswer(new Answer<Object>() {     //production is just number of workers
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Integer o = (Integer) invocation.getArguments()[0];
                return o *7;
            }
        });



        //say that wages are always 50, sell prices are always 100
        when(hr.predictPurchasePriceWhenIncreasingProduction()).thenReturn(50l); when(hr.predictPurchasePriceWhenDecreasingProduction()).thenReturn(50l);
        when(hr.getWagesPaid()).thenReturn(10*50l);
        when(sales.predictSalePriceAfterIncreasingProduction(anyLong(), anyInt())).thenReturn(100l);
        when(sales.predictSalePriceWhenNotChangingPoduction()).thenReturn(100l);
        when(sales.predictSalePriceAfterDecreasingProduction(anyLong(), anyInt())).thenReturn(100l);



        PlantControl control = mock(PlantControl.class); when(hr.predictPurchasePriceWhenNoChangeInProduction()).thenReturn(50l);
        //no delay exception thrown this time!!!
        MarginalMaximizer maximizer = new MarginalMaximizer(hr,control,p,owner);
        boolean exceptionThrown = false;
        float marginalProfits = -1;
        //check the the statics are correct
        try {
            marginalProfits = MarginalMaximizerStatics.computeMarginalProfits(maximizer.getOwner(), maximizer.getP(), maximizer.getHr(), maximizer.getControl(), maximizer.getPolicy(), 10, 11);
            assertEquals(marginalProfits, 50f, .0001f);
            marginalProfits = MarginalMaximizerStatics.computeMarginalProfits(maximizer.getOwner(), maximizer.getP(), maximizer.getHr(), maximizer.getControl(), maximizer.getPolicy(), 10, 9);
            assertEquals(marginalProfits, -50f, .0001f);
            marginalProfits = MarginalMaximizerStatics.computeMarginalProfits(maximizer.getOwner(), maximizer.getP(), maximizer.getHr(), maximizer.getControl(), maximizer.getPolicy(), 10, 8);
            assertEquals(marginalProfits, -100f, .0001f);
            marginalProfits = MarginalMaximizerStatics.computeMarginalProfits(maximizer.getOwner(), maximizer.getP(), maximizer.getHr(), maximizer.getControl(), maximizer.getPolicy(), 10, 12);
            assertEquals(marginalProfits, 100f, .0001f);
        } catch (DelayException e) {
            fail();
        }
        assertTrue(!exceptionThrown);




        //now make sure it's used properly in the maximizer method.
        //the method is protected so we need to open it from afar
        // chooseWorkerTarget(int currentWorkerTarget, float newProfits, float newRevenues, float newCosts, float oldRevenues, float oldCosts, int oldWorkerTarget, float oldProfits)
        try {
            Method chooseTarget = MarginalMaximizer.class.getDeclaredMethod("chooseWorkerTarget", int.class, float.class,
                    float.class,float.class,float.class, float.class,int.class, float.class);
            chooseTarget.setAccessible(true);
            Integer newtarget = (Integer) chooseTarget.invoke(maximizer,10,10000,-1,-1,-1,-1,11,1000000);
            assertEquals(11, newtarget.intValue());
            //same should happen at target 100
            when(p.getNumberOfWorkers()).thenReturn(100);  when(hr.getWagesPaid()).thenReturn(50*100l);
            newtarget = (Integer) chooseTarget.invoke(maximizer,100,10000,-1,-1,-1,-1,11,1000000);
            assertEquals(101, newtarget.intValue());


            //do it again, but this time it pays to cut back
            when(hr.predictPurchasePriceWhenIncreasingProduction()).thenReturn(100l);
            when(hr.getWagesPaid()).thenReturn(10*50l);
            when(sales.predictSalePriceAfterIncreasingProduction(anyLong(), anyInt())).thenReturn(100l); when(sales.getLastClosingPrice()).thenReturn(100l);
            when(sales.predictSalePriceAfterDecreasingProduction(anyLong(), anyInt())).thenReturn(100l);


        } catch (NoSuchMethodException  | InvocationTargetException | IllegalAccessException e) {
            fail();
        }


    }


    //just tests whether the sigmoid function spits the number out correctly
    @Test
    public void testSigmoid(){

       assertEquals(MarginalMaximizerStatics.sigmoid(0), 0f, .0001f);
        assertEquals(MarginalMaximizerStatics.sigmoid(1f), 0.5f, .0001f);
        assertEquals(MarginalMaximizerStatics.sigmoid(4f), 0.8f, .0001f);


    }

    @Test
    public void testSigmoid2(){

        assertEquals(MarginalMaximizerStatics.exponentialSigmoid(0,0), 0.5f, .0001f);
        assertEquals(MarginalMaximizerStatics.exponentialSigmoid(1,1), 0.5f, .0001f);
        assertEquals(MarginalMaximizerStatics.exponentialSigmoid(4f,0), 0.9820f, .0001f);
        assertEquals(MarginalMaximizerStatics.exponentialSigmoid(-4f,0), 0.017986f, .0001f);



    }




    //the monopolist test now is in the scenario section



}
