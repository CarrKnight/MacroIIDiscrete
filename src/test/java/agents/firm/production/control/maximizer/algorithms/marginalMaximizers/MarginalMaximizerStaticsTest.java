/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.production.control.maximizer.algorithms.marginalMaximizers;

import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.production.Plant;
import agents.firm.production.control.PlantControl;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.sales.SalesDepartment;
import goods.GoodType;
import model.utilities.DelayException;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;

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
 * @version 2013-04-18
 * @see
 */
public class MarginalMaximizerStaticsTest
{

    //--------------------------------------------------------------------------------
    // marginal revenue
    //--------------------------------------------------------------------------------

    //increase production by 1(workers: 1-->2), price drops from 60 to 59. You were producing 10
    //old revenue: 60*10
    //new revenue 59*11
    //marginal revenue is 49

    @Test
    public void simpleRevenueTest() throws DelayException {

        Plant plant = mock(Plant.class);
        when(plant.hypotheticalThroughput(1, GoodType.GENERIC)).thenReturn(10f);
        when(plant.hypotheticalThroughput(2, GoodType.GENERIC)).thenReturn(11f);
        HashSet<GoodType> outputs = new HashSet<>(); outputs.add(GoodType.GENERIC);
        when(plant.getOutputs()).thenReturn(outputs);

        Firm owner = mock(Firm.class);
        SalesDepartment department = mock(SalesDepartment.class);
        when(owner.getSalesDepartment(GoodType.GENERIC)).thenReturn(department);
        when(department.getLastClosingPrice()).thenReturn(60l);
        when(department.predictSalePrice(anyInt())).thenReturn(59l);
        when(department.getTodayOutflow()).thenReturn(10);// if this is 0 then the drop in demand price is ignored.


        Assert.assertEquals(MarginalMaximizerStatics.computeMarginalRevenue(owner,plant,
                MarginalMaximizer.RandomizationPolicy.MORE_TIME,1,2,0,0),49f,.00001);



    }


    //increase production by 10(workers: 1-->2), price drops from 60 to 59. You were producing 10
    //old revenue: 60*10
    //new revenue 59*20
    //marginal revenue is 580

    @Test
    public void simpleRevenueTest2() throws DelayException {

        Plant plant = mock(Plant.class);
        when(plant.hypotheticalThroughput(1, GoodType.GENERIC)).thenReturn(10f);
        when(plant.hypotheticalThroughput(2, GoodType.GENERIC)).thenReturn(20f);
        HashSet<GoodType> outputs = new HashSet<>(); outputs.add(GoodType.GENERIC);
        when(plant.getOutputs()).thenReturn(outputs);

        Firm owner = mock(Firm.class);
        SalesDepartment department = mock(SalesDepartment.class);
        when(owner.getSalesDepartment(GoodType.GENERIC)).thenReturn(department);
        when(department.getLastClosingPrice()).thenReturn(60l);
        when(department.predictSalePrice(anyInt())).thenReturn(59l);
        when(department.getTodayOutflow()).thenReturn(10);// if this is 0 then the drop in demand price is ignored.


        Assert.assertEquals(MarginalMaximizerStatics.computeMarginalRevenue(owner,plant,
                MarginalMaximizer.RandomizationPolicy.MORE_TIME,1,2,0,0),   580f,.00001);
    }

    //if you predict price going up, ignore the effect on goods you were already selling.
    //increase production by 10(workers: 1-->2), price INCREASES from 60 to 70. You were producing 10
    //old revenue: 60*10
    //new revenue 70*20
    //marginal revenue is 800, but you really only think your profits are going to be 60*10+70*10  - 60*10f-->700
    @Test
    public void simpleRevenueTest3() throws DelayException {

        Plant plant = mock(Plant.class);
        when(plant.hypotheticalThroughput(1, GoodType.GENERIC)).thenReturn(10f);
        when(plant.hypotheticalThroughput(2, GoodType.GENERIC)).thenReturn(20f);
        HashSet<GoodType> outputs = new HashSet<>(); outputs.add(GoodType.GENERIC);
        when(plant.getOutputs()).thenReturn(outputs);

        Firm owner = mock(Firm.class);
        SalesDepartment department = mock(SalesDepartment.class);
        when(owner.getSalesDepartment(GoodType.GENERIC)).thenReturn(department);
        when(department.getLastClosingPrice()).thenReturn(60l);
        when(department.predictSalePrice(anyInt())).thenReturn(70l);
        when(department.getTodayOutflow()).thenReturn(10);// if this is 0 then the drop in demand price is ignored.


        Assert.assertEquals(MarginalMaximizerStatics.computeMarginalRevenue(owner,plant,
                MarginalMaximizer.RandomizationPolicy.MORE_TIME,1,2,0,0),   700f,.00001);
    }

    //marginal revenue with step different from 1 (numbers are the same as simple revenue test 1)
    //increase production by 1(workers: 1-->3), price drops from 60 to 59. You were producing 10
    //old revenue: 60*10
    //new revenue 59*11
    //marginal revenue is 49

    @Test
    public void simpleRevenueTest4() throws DelayException {

        Plant plant = mock(Plant.class);
        when(plant.hypotheticalThroughput(1, GoodType.GENERIC)).thenReturn(10f);
        when(plant.hypotheticalThroughput(3, GoodType.GENERIC)).thenReturn(11f);
        HashSet<GoodType> outputs = new HashSet<>(); outputs.add(GoodType.GENERIC);
        when(plant.getOutputs()).thenReturn(outputs);

        Firm owner = mock(Firm.class);
        SalesDepartment department = mock(SalesDepartment.class);
        when(owner.getSalesDepartment(GoodType.GENERIC)).thenReturn(department);
        when(department.getLastClosingPrice()).thenReturn(60l);
        when(department.predictSalePrice(anyInt())).thenReturn(59l);
        when(department.getTodayOutflow()).thenReturn(10);// if this is 0 then the drop in demand price is ignored.


        Assert.assertEquals(MarginalMaximizerStatics.computeMarginalRevenue(owner,plant,
                MarginalMaximizer.RandomizationPolicy.MORE_TIME,1,3,0,0),49f,.00001);

    }


    //--------------------------------------------------------------------------------
    // marginal wage
    //--------------------------------------------------------------------------------



    //currently 1 worker, want to hire the second
    //current wage 5, next wage is 10
    //marginal cost 15 (10 to hire the new guy, 5 to raise the wage to the old one)
    //total wages would then be 20
    @Test
    public void simpleWageCosts() throws DelayException {

        HumanResources hr = mock(HumanResources.class);
        when(hr.predictPurchasePrice()).thenReturn(10l);
        when(hr.isFixedPayStructure()).thenReturn(true);
        PlantControl control = mock(PlantControl.class);
        when(control.getCurrentWage()).thenReturn(5l);

        CostEstimate estimate = MarginalMaximizerStatics.
                computeWageCosts(hr, control, 1, 2, MarginalMaximizer.RandomizationPolicy.MORE_TIME);

        Assert.assertEquals(estimate.getTotalCost(),20l);
        Assert.assertEquals(estimate.getMarginalCost(),15l);


    }

    //same numbers as before, but this time the workers to hire are 2
    @Test
    public void simpleWageCosts2() throws DelayException {

        HumanResources hr = mock(HumanResources.class);
        when(hr.predictPurchasePrice()).thenReturn(10l);
        when(hr.isFixedPayStructure()).thenReturn(true);
        PlantControl control = mock(PlantControl.class);
        when(control.getCurrentWage()).thenReturn(5l);

        CostEstimate estimate = MarginalMaximizerStatics.
                computeWageCosts(hr, control, 1, 3, MarginalMaximizer.RandomizationPolicy.MORE_TIME);

        Assert.assertEquals(estimate.getTotalCost(),30l);
        Assert.assertEquals(estimate.getMarginalCost(),25l);


    }


    //same numbers as wagecosts1, but this time you are firing
    @Test
    public void simpleWageCosts3() throws DelayException {

        HumanResources hr = mock(HumanResources.class);
        when(hr.isFixedPayStructure()).thenReturn(true);
        when(hr.hypotheticalWageAtThisLevel(1)).thenReturn(5l);
        PlantControl control = mock(PlantControl.class);
        when(control.getCurrentWage()).thenReturn(10l);

        CostEstimate estimate = MarginalMaximizerStatics.
                computeWageCosts(hr, control, 2, 1, MarginalMaximizer.RandomizationPolicy.MORE_TIME);

        Assert.assertEquals(estimate.getTotalCost(),5l);
        Assert.assertEquals(estimate.getMarginalCost(),-15l);


    }

    //same as above, but firing 2 people rather than 1
    @Test
    public void simpleWageCosts4() throws DelayException {

        HumanResources hr = mock(HumanResources.class);
        when(hr.isFixedPayStructure()).thenReturn(true);
        when(hr.hypotheticalWageAtThisLevel(1)).thenReturn(5l);
        PlantControl control = mock(PlantControl.class);
        when(control.getCurrentWage()).thenReturn(10l);

        CostEstimate estimate = MarginalMaximizerStatics.
                computeWageCosts(hr, control, 3, 1, MarginalMaximizer.RandomizationPolicy.MORE_TIME);

        Assert.assertEquals(estimate.getTotalCost(),5l);
        Assert.assertEquals(estimate.getMarginalCost(),-25l);


    }


    //--------------------------------------------------------------------------------
    // marginal input
    //--------------------------------------------------------------------------------

    //1--->2 workers
    //1--->2 weekly inputs
    //price of input will move from 5 to 10
    //total input costs = 20
    //marginal input costs= 15
    @Test
    public void simpleInputCosts() throws DelayException {

        Plant plant = mock(Plant.class);


        HashSet<GoodType> inputs = new HashSet<>(); inputs.add(GoodType.GENERIC);
        when(plant.getInputs()).thenReturn(inputs);
        when(plant.hypotheticalWeeklyInputNeeds(GoodType.GENERIC,1)).thenReturn(1);
        when(plant.hypotheticalWeeklyInputNeeds(GoodType.GENERIC,2)).thenReturn(2);


        Firm owner = mock(Firm.class);
        PurchasesDepartment department = mock(PurchasesDepartment.class);
        when(owner.getPurchaseDepartment(GoodType.GENERIC)).thenReturn(department);
        when(department.getLastClosingPrice()).thenReturn(5l);
        when(department.predictPurchasePrice()).thenReturn(10l);

        CostEstimate estimate = MarginalMaximizerStatics.
                computeInputCosts(owner, plant, MarginalMaximizer.RandomizationPolicy.MORE_TIME, 1, 2);


        Assert.assertEquals(estimate.getMarginalCost(),15,.0001f);
        Assert.assertEquals(estimate.getTotalCost(),20,.0001f);



    }

    //1--->2 workers
    //1--->2 weekly inputs
    //price of input will move from 5 to 10
    //total input costs = 20
    //marginal input costs= 15
    @Test
    public void simpleInputCosts2() throws DelayException {

        Plant plant = mock(Plant.class);


        HashSet<GoodType> inputs = new HashSet<>(); inputs.add(GoodType.GENERIC);
        when(plant.getInputs()).thenReturn(inputs);
        when(plant.hypotheticalWeeklyInputNeeds(GoodType.GENERIC,1)).thenReturn(1);
        when(plant.hypotheticalWeeklyInputNeeds(GoodType.GENERIC,2)).thenReturn(2);


        Firm owner = mock(Firm.class);
        PurchasesDepartment department = mock(PurchasesDepartment.class);
        when(owner.getPurchaseDepartment(GoodType.GENERIC)).thenReturn(department);
        when(department.getLastClosingPrice()).thenReturn(5l);
        when(department.predictPurchasePrice()).thenReturn(10l);

        CostEstimate estimate = MarginalMaximizerStatics.
                computeInputCosts(owner, plant, MarginalMaximizer.RandomizationPolicy.MORE_TIME, 1, 2);


        Assert.assertEquals(estimate.getMarginalCost(),15,.0001f);
        Assert.assertEquals(estimate.getTotalCost(),20,.0001f);



    }


    //what happens when price decreases weirdly?


}
