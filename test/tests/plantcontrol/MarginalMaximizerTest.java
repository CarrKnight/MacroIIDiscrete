package tests.plantcontrol;

import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.production.Blueprint;
import agents.firm.production.Plant;
import agents.firm.production.control.PlantControl;
import agents.firm.production.control.maximizer.marginalMaximizers.MarginalMaximizer;
import agents.firm.sales.SalesDepartment;
import goods.GoodType;
import junit.framework.Assert;
import model.MacroII;
import model.utilities.DelayException;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

    @Test
    public void testDelay(){
        //get the constructor stuff
        HumanResources hr = mock(HumanResources.class);
        Plant p = mock(Plant.class);
        Blueprint b = new Blueprint.Builder().output(GoodType.GENERIC,1).build(); //just one output
        Firm owner = mock(Firm.class); when(p.getOwner()).thenReturn(owner); when(hr.getPlant()).thenReturn(p); when(hr.getFirm()).thenReturn(owner);
        when(owner.getModel()).thenReturn(new MacroII(1l));  when(p.workerSize()).thenReturn(10);
        when(hr.predictPurchasePrice()).thenReturn(-1l); //tell the hr to fail at predicting
        PlantControl control = mock(PlantControl.class);
        //it should immediately fail
        MarginalMaximizer maximizer = new MarginalMaximizer(hr,control);
        boolean exceptionThrown = false;
        try {
            float marginalProfits = maximizer.computeMarginalProfits(10,11);
            Assert.fail();
        } catch (DelayException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);


    }

    @Test
    public void testMarginalProfits(){
        //get the constructor stuff
        HumanResources hr = mock(HumanResources.class);
        Plant p = mock(Plant.class); when(p.minimumWorkersNeeded()).thenReturn(0); when(p.maximumWorkersPossible()).thenReturn(1000);
        Blueprint b = new Blueprint.Builder().output(GoodType.GENERIC,1).build(); //just one output
        when(p.getBlueprint()).thenReturn(b);
        Firm owner = mock(Firm.class); when(p.getOwner()).thenReturn(owner); when(hr.getPlant()).thenReturn(p); when(hr.getFirm()).thenReturn(owner);
        when(owner.getModel()).thenReturn(new MacroII(1l));  when(p.workerSize()).thenReturn(10);
        when(hr.predictPurchasePrice()).thenReturn(-1l); //tell the hr to fail at predicting
        SalesDepartment sales = mock(SalesDepartment.class);
        when(owner.getSalesDepartment(GoodType.GENERIC)).thenReturn(sales);
        when(p.hypotheticalThroughput(anyInt(),any(GoodType.class))).thenAnswer(new Answer<Object>() {     //production is just number of workers
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArguments()[0];
            }
        });



        //say that wages are always 50, sell prices are always 100
        when(hr.predictPurchasePrice()).thenReturn(50l); when(hr.hypotheticalWageAtThisLevel(anyInt())).thenReturn(50l);
        when(hr.getWagesPaid()).thenReturn(10*50l);
        when(sales.predictSalePrice(anyLong())).thenReturn(100l); when(sales.getLastClosingPrice()).thenReturn(100l);



        PlantControl control = mock(PlantControl.class);
        //it should immediately fail
        MarginalMaximizer maximizer = new MarginalMaximizer(hr,control);
        boolean exceptionThrown = false;
        float marginalProfits = -1;
        try {
            marginalProfits = maximizer.computeMarginalProfits(10,11);
            Assert.assertEquals(marginalProfits,50f,.0001f);
            marginalProfits = maximizer.computeMarginalProfits(10,9);
            Assert.assertEquals(marginalProfits,-50f,.0001f);
            marginalProfits = maximizer.computeMarginalProfits(10,8);
            Assert.assertEquals(marginalProfits,-100f,.0001f);
            marginalProfits = maximizer.computeMarginalProfits(10,12);
            Assert.assertEquals(marginalProfits,100f,.0001f);
        } catch (DelayException e) {
            Assert.fail();
        }
        Assert.assertTrue(!exceptionThrown);




        //now make sure it's used properly in the maximizer method.
        //the method is protected so we need to open it from afar
        // chooseWorkerTarget(int currentWorkerTarget, float newProfits, float newRevenues, float newCosts, float oldRevenues, float oldCosts, int oldWorkerTarget, float oldProfits)
        try {
            Method chooseTarget = MarginalMaximizer.class.getDeclaredMethod("chooseWorkerTarget", int.class, float.class,
                    float.class,float.class,float.class, float.class,int.class, float.class);
            chooseTarget.setAccessible(true);
            Integer newtarget = (Integer) chooseTarget.invoke(maximizer,10,10000,-1,-1,-1,-1,11,1000000);
            Assert.assertEquals(11,newtarget.intValue());
            //same should happen at target 100
            when(p.workerSize()).thenReturn(100);  when(hr.getWagesPaid()).thenReturn(50*100l);
            newtarget = (Integer) chooseTarget.invoke(maximizer,100,10000,-1,-1,-1,-1,11,1000000);
            Assert.assertEquals(101,newtarget.intValue());


            //do it again, but this time it pays to cut back
            when(hr.predictPurchasePrice()).thenReturn(100l);
            when(hr.getWagesPaid()).thenReturn(10*50l);
            when(sales.predictSalePrice(anyLong())).thenReturn(100l); when(sales.getLastClosingPrice()).thenReturn(100l);



        } catch (NoSuchMethodException  | InvocationTargetException | IllegalAccessException e) {
            Assert.fail();
        }


    }




   //the monopolist test now is in the scenario section



}
