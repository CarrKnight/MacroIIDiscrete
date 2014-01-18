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
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashSet;

import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

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
@PrepareForTest(MarginalMaximizerStatics.class) //this is needed for the marginal profits calls
@RunWith(PowerMockRunner.class)
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
        when(department.predictSalePriceWhenNotChangingPoduction()).thenReturn(60l);
        when(department.predictSalePriceAfterIncreasingProduction(anyInt(), anyInt())).thenReturn(59l);
        when(department.getTodayOutflow()).thenReturn(10);// if this is 0 then the drop in demand price is ignored.


        Assert.assertEquals(MarginalMaximizerStatics.computeMarginalRevenue(owner,plant,
                MarginalMaximizer.RandomizationPolicy.MORE_TIME,1,2,0,0),49d/7d,.00001);



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
        when(department.predictSalePriceWhenNotChangingPoduction()).thenReturn(60l);
        when(department.predictSalePriceAfterIncreasingProduction(anyInt(), anyInt())).thenReturn(59l);
        when(department.getTodayOutflow()).thenReturn(10);// if this is 0 then the drop in demand price is ignored.


        Assert.assertEquals(MarginalMaximizerStatics.computeMarginalRevenue(owner,plant,
                MarginalMaximizer.RandomizationPolicy.MORE_TIME,1,2,0,0),   580d/7d,.00001);
    }

    //if you predict price going up
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
        when(department.predictSalePriceWhenNotChangingPoduction()).thenReturn(60l);
        when(department.predictSalePriceAfterIncreasingProduction(anyInt(), anyInt())).thenReturn(70l);
        when(department.getTodayOutflow()).thenReturn(10);// if this is 0 then the drop in demand price is ignored.


        Assert.assertEquals(MarginalMaximizerStatics.computeMarginalRevenue(owner,plant,
                MarginalMaximizer.RandomizationPolicy.MORE_TIME,1,2,0,0),   800d/7d,.00001);
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
        when(department.predictSalePriceWhenNotChangingPoduction()).thenReturn(60l);
        when(department.predictSalePriceAfterIncreasingProduction(anyInt(), anyInt())).thenReturn(59l);
        when(department.getTodayOutflow()).thenReturn(10);// if this is 0 then the drop in demand price is ignored.


        Assert.assertEquals(MarginalMaximizerStatics.computeMarginalRevenue(owner,plant,
                MarginalMaximizer.RandomizationPolicy.MORE_TIME,1,3,0,0),49d/7d,.00001);

    }


    //same as simple revenue test, but now you produce two goods (same numbers for each)

    //increase production by 1(workers: 1-->2), price drops from 60 to 59. You were producing 10
    //old revenue: 60*10 *2
    //new revenue 59*11 *2
    //marginal revenue is 98

    @Test
    public void doubleRevenueTest() throws DelayException {

        Plant plant = mock(Plant.class);
        when(plant.hypotheticalThroughput(1, GoodType.GENERIC)).thenReturn(10f);
        when(plant.hypotheticalThroughput(2, GoodType.GENERIC)).thenReturn(11f);
        when(plant.hypotheticalThroughput(1, GoodType.BEEF)).thenReturn(10f);
        when(plant.hypotheticalThroughput(2, GoodType.BEEF)).thenReturn(11f);
        HashSet<GoodType> outputs = new HashSet<>(); outputs.add(GoodType.GENERIC);  outputs.add(GoodType.BEEF);
        when(plant.getOutputs()).thenReturn(outputs);

        Firm owner = mock(Firm.class);
        SalesDepartment department = mock(SalesDepartment.class);
        when(owner.getSalesDepartment(GoodType.GENERIC)).thenReturn(department);
        when(owner.getSalesDepartment(GoodType.BEEF)).thenReturn(department);
        when(department.predictSalePriceWhenNotChangingPoduction()).thenReturn(60l);
        when(department.predictSalePriceAfterIncreasingProduction(anyInt(), anyInt())).thenReturn(59l);
        when(department.getTodayOutflow()).thenReturn(10);// if this is 0 then the drop in demand price is ignored.


        Assert.assertEquals(MarginalMaximizerStatics.computeMarginalRevenue(owner,plant,
                MarginalMaximizer.RandomizationPolicy.MORE_TIME,1,2,0,0),98d/7d,.00001);



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
        when(hr.predictPurchasePriceWhenIncreasingProduction()).thenReturn(10l);
        when(hr.isFixedPayStructure()).thenReturn(true);
        PlantControl control = mock(PlantControl.class);

        when(hr.predictPurchasePriceWhenNoChangeInProduction()).thenReturn(5l);


        CostEstimate estimate = MarginalMaximizerStatics.
                computeWageCosts(hr, control, 1, 2, MarginalMaximizer.RandomizationPolicy.MORE_TIME);

        Assert.assertEquals(estimate.getTotalCost(),20l,.0001);
        Assert.assertEquals(estimate.getMarginalCost(),15l,.0001);


    }

    //same numbers as before, but this time the workers to hire are 2
    @Test
    public void simpleWageCosts2() throws DelayException {

        HumanResources hr = mock(HumanResources.class);
        when(hr.predictPurchasePriceWhenIncreasingProduction()).thenReturn(10l);
        when(hr.isFixedPayStructure()).thenReturn(true);
        PlantControl control = mock(PlantControl.class);
        when(hr.predictPurchasePriceWhenNoChangeInProduction()).thenReturn(5l);

        CostEstimate estimate = MarginalMaximizerStatics.
                computeWageCosts(hr, control, 1, 3, MarginalMaximizer.RandomizationPolicy.MORE_TIME);

        Assert.assertEquals(estimate.getTotalCost(),30l,.0001);
        Assert.assertEquals(estimate.getMarginalCost(),25l,.0001);


    }


    //same numbers as wagecosts1, but this time you are firing
    @Test
    public void simpleWageCosts3() throws DelayException {

        HumanResources hr = mock(HumanResources.class);
        when(hr.isFixedPayStructure()).thenReturn(true);
        when(hr.predictPurchasePriceWhenDecreasingProduction()).thenReturn(5l);
        PlantControl control = mock(PlantControl.class);
        when(hr.predictPurchasePriceWhenNoChangeInProduction()).thenReturn(10l);


        CostEstimate estimate = MarginalMaximizerStatics.
                computeWageCosts(hr, control, 2, 1, MarginalMaximizer.RandomizationPolicy.MORE_TIME);

        Assert.assertEquals(estimate.getTotalCost(),5l,.0001);
        Assert.assertEquals(estimate.getMarginalCost(),-15l,.0001);


    }

    //same as above, but firing 2 people rather than 1
    @Test
    public void simpleWageCosts4() throws DelayException {

        HumanResources hr = mock(HumanResources.class);
        when(hr.isFixedPayStructure()).thenReturn(true);
        when(hr.predictPurchasePriceWhenDecreasingProduction()).thenReturn(5l);
        PlantControl control = mock(PlantControl.class);
        when(hr.predictPurchasePriceWhenNoChangeInProduction()).thenReturn(10l);


        CostEstimate estimate = MarginalMaximizerStatics.
                computeWageCosts(hr, control, 3, 1, MarginalMaximizer.RandomizationPolicy.MORE_TIME);

        Assert.assertEquals(estimate.getTotalCost(),5l,.0001);
        Assert.assertEquals(estimate.getMarginalCost(),-25l,.0001);


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
        when(department.predictPurchasePriceWhenNoChangeInProduction()).thenReturn(5l);
        when(department.predictPurchasePriceWhenIncreasingProduction()).thenReturn(10l);

        CostEstimate estimate = MarginalMaximizerStatics.
                computeInputCosts(owner, plant, MarginalMaximizer.RandomizationPolicy.MORE_TIME, 1, 2);


        Assert.assertEquals(estimate.getMarginalCost(),15d/7d,.0001f);
        Assert.assertEquals(estimate.getTotalCost(),20d/7d,.0001f);



    }

    //1--->2 workers
    //1--->4 weekly inputs
    //price of input will move from 5 to 10
    //total input costs = 40
    //marginal input costs= 35
    @Test
    public void simpleInputCosts2() throws DelayException {

        Plant plant = mock(Plant.class);


        HashSet<GoodType> inputs = new HashSet<>(); inputs.add(GoodType.GENERIC);
        when(plant.getInputs()).thenReturn(inputs);
        when(plant.hypotheticalWeeklyInputNeeds(GoodType.GENERIC,1)).thenReturn(1);
        when(plant.hypotheticalWeeklyInputNeeds(GoodType.GENERIC,2)).thenReturn(4);


        Firm owner = mock(Firm.class);
        PurchasesDepartment department = mock(PurchasesDepartment.class);
        when(owner.getPurchaseDepartment(GoodType.GENERIC)).thenReturn(department);
        when(department.predictPurchasePriceWhenNoChangeInProduction()).thenReturn(5l);
        when(department.predictPurchasePriceWhenIncreasingProduction()).thenReturn(10l);

        CostEstimate estimate = MarginalMaximizerStatics.
                computeInputCosts(owner, plant, MarginalMaximizer.RandomizationPolicy.MORE_TIME, 1, 2);


        Assert.assertEquals(estimate.getMarginalCost(),35d/7d,.0001f);
        Assert.assertEquals(estimate.getTotalCost(),40d/7d,.0001f);



    }


    //price of input decreases weirdly.
    //1--->2 workers
    //1--->2 weekly inputs
    //price of input will move from 5 to 3
    //total input costs = 8
    //marginal input costs= 3
    @Test
    public void simpleInputCosts3() throws DelayException {

        Plant plant = mock(Plant.class);


        HashSet<GoodType> inputs = new HashSet<>(); inputs.add(GoodType.GENERIC);
        when(plant.getInputs()).thenReturn(inputs);
        when(plant.hypotheticalWeeklyInputNeeds(GoodType.GENERIC,1)).thenReturn(1);
        when(plant.hypotheticalWeeklyInputNeeds(GoodType.GENERIC,2)).thenReturn(2);


        Firm owner = mock(Firm.class);
        PurchasesDepartment department = mock(PurchasesDepartment.class);
        when(owner.getPurchaseDepartment(GoodType.GENERIC)).thenReturn(department);
        when(department.predictPurchasePriceWhenNoChangeInProduction()).thenReturn(5l);
        when(department.predictPurchasePriceWhenIncreasingProduction()).thenReturn(3l);

        CostEstimate estimate = MarginalMaximizerStatics.
                computeInputCosts(owner, plant, MarginalMaximizer.RandomizationPolicy.MORE_TIME, 1, 2);


        Assert.assertEquals(estimate.getMarginalCost(),1d/7d,.0001f);
        Assert.assertEquals(estimate.getTotalCost(),6d/7d,.0001f);



    }
    //same numbers as the first test, but now we are decreasing the workers
    //2--->1 workers
    //2--->1 weekly inputs
    //price of input will move from 10 to 5 (but it is ignored)
    //total input costs = 5
    //marginal input costs= -15
    @Test
    public void simpleInputCosts4() throws DelayException {

        Plant plant = mock(Plant.class);


        HashSet<GoodType> inputs = new HashSet<>(); inputs.add(GoodType.GENERIC);
        when(plant.getInputs()).thenReturn(inputs);
        when(plant.hypotheticalWeeklyInputNeeds(GoodType.GENERIC,1)).thenReturn(1);
        when(plant.hypotheticalWeeklyInputNeeds(GoodType.GENERIC,2)).thenReturn(2);


        Firm owner = mock(Firm.class);
        PurchasesDepartment department = mock(PurchasesDepartment.class);
        when(owner.getPurchaseDepartment(GoodType.GENERIC)).thenReturn(department);
        when(department.predictPurchasePriceWhenDecreasingProduction()).thenReturn(5l);
        when(department.predictPurchasePriceWhenNoChangeInProduction()).thenReturn(10l);

        CostEstimate estimate = MarginalMaximizerStatics.
                computeInputCosts(owner, plant, MarginalMaximizer.RandomizationPolicy.MORE_TIME, 2, 1);


        Assert.assertEquals(estimate.getMarginalCost(),-15d/7d,.0001f);
        Assert.assertEquals(estimate.getTotalCost(),5d/7d,.0001f);



    }




    //two kinds of inputs, both exactly same prices like simpleInputCosts()
    //1--->2 workers
    //1--->2 weekly inputs
    //price of input will move from 5 to 10
    //total input costs = 20 *2
    //marginal input costs= 15 *2
    @Test
    public void doubleInputCosts() throws DelayException {

        Plant plant = mock(Plant.class);


        HashSet<GoodType> inputs = new HashSet<>(); inputs.add(GoodType.GENERIC);inputs.add(GoodType.BEEF);
        when(plant.getInputs()).thenReturn(inputs);
        when(plant.hypotheticalWeeklyInputNeeds(GoodType.GENERIC,1)).thenReturn(1);
        when(plant.hypotheticalWeeklyInputNeeds(GoodType.GENERIC,2)).thenReturn(2);
        when(plant.hypotheticalWeeklyInputNeeds(GoodType.BEEF,1)).thenReturn(1);
        when(plant.hypotheticalWeeklyInputNeeds(GoodType.BEEF,2)).thenReturn(2);


        Firm owner = mock(Firm.class);
        PurchasesDepartment department = mock(PurchasesDepartment.class);
        when(owner.getPurchaseDepartment(GoodType.GENERIC)).thenReturn(department);
        when(owner.getPurchaseDepartment(GoodType.BEEF)).thenReturn(department);
        when(department.predictPurchasePriceWhenNoChangeInProduction()).thenReturn(5l);
        when(department.predictPurchasePriceWhenIncreasingProduction()).thenReturn(10l);

        CostEstimate estimate = MarginalMaximizerStatics.
                computeInputCosts(owner, plant, MarginalMaximizer.RandomizationPolicy.MORE_TIME, 1, 2);


        Assert.assertEquals(estimate.getMarginalCost(),30d/7d,.0001f);
        Assert.assertEquals(estimate.getTotalCost(),40d/7d,.0001f);

    }


    //--------------------------------------------------------------------------------
    // marginal profits
    //--------------------------------------------------------------------------------
    //workers 1--->2
    //marginal cost of labor = 100
    //no input
    //marginal revenues = 200
    //marginal profits = 100
    @Test
    public void marginalProfits1() throws DelayException {

        mockStatic(MarginalMaximizerStatics.class);
        Plant p = mock(Plant.class); when(p.getNumberOfWorkers()).thenReturn(1);

        //costs
        CostEstimate wages = new CostEstimate(100,100000);
        Mockito.when(MarginalMaximizerStatics.computeWageCosts(any(HumanResources.class),any(PlantControl.class),
                anyInt(),anyInt(),any(MarginalMaximizer.RandomizationPolicy.class))).thenReturn(wages);
        CostEstimate inputs = new CostEstimate(0,0);
        Mockito.when(MarginalMaximizerStatics.computeInputCosts(any(Firm.class), any(Plant.class),
                any(MarginalMaximizer.RandomizationPolicy.class), anyInt(), anyInt())).thenReturn(inputs);

        //revenues
        Mockito.when(MarginalMaximizerStatics.computeMarginalRevenue(any(Firm.class),any(Plant.class),
                any(MarginalMaximizer.RandomizationPolicy.class),anyInt(),anyInt(),anyLong(),anyLong())).thenReturn(200f);

        Mockito.when(MarginalMaximizerStatics.computeMarginalProfits(any(Firm.class),any(Plant.class), any(HumanResources.class),
                any(PlantControl.class),
                any(MarginalMaximizer.RandomizationPolicy.class),anyInt(),anyInt())).thenCallRealMethod();


        //profits should be 100
        Assert.assertEquals(MarginalMaximizerStatics.computeMarginalProfits(mock(Firm.class),p ,
                mock(HumanResources.class), Mockito.mock(PlantControl.class), MarginalMaximizer.RandomizationPolicy.MORE_TIME,
                1,2), 100,.0001f);

    }



    //bigger step
    //workers 1--->4
    //marginal cost of labor = 100
    //no input
    //marginal revenues = 200
    //marginal profits = 100
    @Test
    public void marginalProfits2() throws DelayException {

        mockStatic(MarginalMaximizerStatics.class);
        Plant p = mock(Plant.class); when(p.getNumberOfWorkers()).thenReturn(1);

        //costs
        CostEstimate wages = new CostEstimate(100,100000);
        Mockito.when(MarginalMaximizerStatics.computeWageCosts(any(HumanResources.class),any(PlantControl.class),
                anyInt(),anyInt(),any(MarginalMaximizer.RandomizationPolicy.class))).thenReturn(wages);
        CostEstimate inputs = new CostEstimate(0,0);
        Mockito.when(MarginalMaximizerStatics.computeInputCosts(any(Firm.class), any(Plant.class),
                any(MarginalMaximizer.RandomizationPolicy.class), anyInt(), anyInt())).thenReturn(inputs);

        //revenues
        Mockito.when(MarginalMaximizerStatics.computeMarginalRevenue(any(Firm.class),any(Plant.class),
                any(MarginalMaximizer.RandomizationPolicy.class),anyInt(),anyInt(),anyLong(),anyLong())).thenReturn(200f);

        Mockito.when(MarginalMaximizerStatics.computeMarginalProfits(any(Firm.class),any(Plant.class), any(HumanResources.class),
                any(PlantControl.class),
                any(MarginalMaximizer.RandomizationPolicy.class),anyInt(),anyInt())).thenCallRealMethod();


        //profits should be 100
        Assert.assertEquals(MarginalMaximizerStatics.computeMarginalProfits(mock(Firm.class),p ,
                mock(HumanResources.class), Mockito.mock(PlantControl.class), MarginalMaximizer.RandomizationPolicy.MORE_TIME,
                1,4), 100,.0001f);

    }

    //add inputs
    //workers 1--->2
    //marginal cost of labor = 100
    //marginal input = 200
    //marginal revenues = 200
    //marginal profits = -100
    @Test
    public void marginalProfits3() throws DelayException {

        mockStatic(MarginalMaximizerStatics.class);
        Plant p = mock(Plant.class); when(p.getNumberOfWorkers()).thenReturn(1);

        //costs
        CostEstimate wages = new CostEstimate(100,100000);
        Mockito.when(MarginalMaximizerStatics.computeWageCosts(any(HumanResources.class),any(PlantControl.class),
                anyInt(),anyInt(),any(MarginalMaximizer.RandomizationPolicy.class))).thenReturn(wages);
        CostEstimate inputs = new CostEstimate(200,101248108);
        Mockito.when(MarginalMaximizerStatics.computeInputCosts(any(Firm.class), any(Plant.class),
                any(MarginalMaximizer.RandomizationPolicy.class), anyInt(), anyInt())).thenReturn(inputs);

        //revenues
        Mockito.when(MarginalMaximizerStatics.computeMarginalRevenue(any(Firm.class),any(Plant.class),
                any(MarginalMaximizer.RandomizationPolicy.class),anyInt(),anyInt(),anyLong(),anyLong())).thenReturn(200f);

        Mockito.when(MarginalMaximizerStatics.computeMarginalProfits(any(Firm.class),any(Plant.class), any(HumanResources.class),
                any(PlantControl.class),
                any(MarginalMaximizer.RandomizationPolicy.class),anyInt(),anyInt())).thenCallRealMethod();


        //profits should be 100
        Assert.assertEquals(MarginalMaximizerStatics.computeMarginalProfits(mock(Firm.class),p ,
                mock(HumanResources.class), Mockito.mock(PlantControl.class), MarginalMaximizer.RandomizationPolicy.MORE_TIME,
                1,4), -100,.0001f);

    }

    //decrease production
    //workers 2--->1
    //marginal cost of labor = -100
    //no input
    //marginal revenues = -200
    //marginal profits = -100
    @Test
    public void marginalProfits4() throws DelayException {

        mockStatic(MarginalMaximizerStatics.class);
        Plant p = mock(Plant.class); when(p.getNumberOfWorkers()).thenReturn(2);

        //costs
        CostEstimate wages = new CostEstimate(-100,100000);
        Mockito.when(MarginalMaximizerStatics.computeWageCosts(any(HumanResources.class),any(PlantControl.class),
                anyInt(),anyInt(),any(MarginalMaximizer.RandomizationPolicy.class))).thenReturn(wages);
        CostEstimate inputs = new CostEstimate(0,0);
        Mockito.when(MarginalMaximizerStatics.computeInputCosts(any(Firm.class), any(Plant.class),
                any(MarginalMaximizer.RandomizationPolicy.class), anyInt(), anyInt())).thenReturn(inputs);

        //revenues
        Mockito.when(MarginalMaximizerStatics.computeMarginalRevenue(any(Firm.class),any(Plant.class),
                any(MarginalMaximizer.RandomizationPolicy.class),anyInt(),anyInt(),anyLong(),anyLong())).thenReturn(-200f);

        Mockito.when(MarginalMaximizerStatics.computeMarginalProfits(any(Firm.class),any(Plant.class), any(HumanResources.class),
                any(PlantControl.class),
                any(MarginalMaximizer.RandomizationPolicy.class),anyInt(),anyInt())).thenCallRealMethod();


        //profits should be 100
        Assert.assertEquals(MarginalMaximizerStatics.computeMarginalProfits(mock(Firm.class),p ,
                mock(HumanResources.class), Mockito.mock(PlantControl.class), MarginalMaximizer.RandomizationPolicy.MORE_TIME,
                2,1), -100,.0001f);

    }

}
