/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package goods;

import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * <h4>Description</h4>
 * <p>
 * <p>
 * <p>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p>
 * <p>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-04-19
 * @see
 */
public class GoodTypeMasterListTest {

    //test observers
    @Test
    public void observerTest1()
    {
        GoodTypeMasterList list = new GoodTypeMasterList();
        Assert.assertEquals(4,list.size());
        MasterListObserver o = mock(MasterListObserver.class);

        boolean add1 = list.addObserver(o);
        boolean add2 = list.addObserver(o);
        boolean add3 = list.addObserver(o);
        //added only once!
        Assert.assertTrue(add1);
        Assert.assertFalse(add2);
        Assert.assertFalse(add3);

        //add a sector
        GoodType agriculture = new UndifferentiatedGoodType("1","agriculture");
        list.addNewSector(agriculture);
        verify(o,times(1)).theMasterListHasChanged();






    }

    @Test
    public void observerTest2()
    {
        GoodTypeMasterList list = new GoodTypeMasterList();
        Assert.assertEquals(4,list.size());
        MasterListObserver o = mock(MasterListObserver.class);

        //add a sector
        GoodType agriculture = new UndifferentiatedGoodType("1","agriculture");
        list.addNewSector(agriculture);

        //start observing now:
        list.addObserver(o);

        //try to add the same sector twice, it won't notify anybody
        list.addNewSector(agriculture);
        verify(o,times(0)).theMasterListHasChanged();

    }

    @Test
    public void observerTest3(){

        GoodTypeMasterList list = new GoodTypeMasterList();
        Assert.assertEquals(4,list.size());
        MasterListObserver o = mock(MasterListObserver.class);


        //add a sector
        GoodType agriculture = new UndifferentiatedGoodType("1","agriculture");
        list.addNewSector(agriculture);

        //try to add the same sector twice, it won't notify anybody
        list.addNewSector(agriculture);

        //start observing now
        list.addObserver(o);

        //try to create the same sector again and add it, it won't work
        GoodType agriculture2 = new UndifferentiatedGoodType("1","agriculture");
        boolean addedCorrectly = list.addNewSector(agriculture2);
        Assert.assertFalse(addedCorrectly); //it wasn't added!
        Assert.assertEquals(5,list.size());
        verify(o,times(0)).theMasterListHasChanged();

    }



    @Test
    public void observerTest4()
    {

        GoodTypeMasterList list = new GoodTypeMasterList();
        Assert.assertEquals(4,list.size());
        MasterListObserver o = mock(MasterListObserver.class);


        //add a sector
        GoodType agriculture = new UndifferentiatedGoodType("1","agriculture");
        list.addNewSector(agriculture);

        //try to add the same sector twice, it won't notify anybody
        list.addNewSector(agriculture);

        //try to create the same sector again and add it, it won't work
        agriculture = new UndifferentiatedGoodType("1","agriculture");
        boolean addedCorrectly = list.addNewSector(agriculture);
        Assert.assertFalse(addedCorrectly); //it wasn't added!
        Assert.assertEquals(5,list.size());

        //start observing now!!
        list.addObserver(o);

        //now try to add a list of 3: one old and 2 new sector, it will call the masterlist change of the observer but only once
        List<GoodType> newTypes = new LinkedList<GoodType>();
        newTypes.add(agriculture);
        GoodType forestry = new UndifferentiatedGoodType("2","forestry");
        newTypes.add(forestry);
        GoodType fishing = new UndifferentiatedGoodType("3","fishing");
        newTypes.add(fishing);
        list.addNewSectors(newTypes);
        Assert.assertEquals(7,list.size());
        verify(o,times(1)).theMasterListHasChanged();



    }



    @Test
    public void observerTest5()
    {

        GoodTypeMasterList list = new GoodTypeMasterList();
        Assert.assertEquals(4,list.size());
        MasterListObserver o = mock(MasterListObserver.class);


        //add a sector
        GoodType agriculture = new UndifferentiatedGoodType("1","agriculture");
        list.addNewSector(agriculture);

        //try to add the same sector twice, it won't notify anybody
        list.addNewSector(agriculture);

        //try to create the same sector again and add it, it won't work
        agriculture = new UndifferentiatedGoodType("1","agriculture");
        boolean addedCorrectly = list.addNewSector(agriculture);
        Assert.assertFalse(addedCorrectly); //it wasn't added!
        Assert.assertEquals(5,list.size());



        //now try to add a list of 3: one old and 2 new sector, it will call the masterlist change of the observer but only once
        List<GoodType> newTypes = new LinkedList<GoodType>();
        newTypes.add(agriculture);
        GoodType forestry = new UndifferentiatedGoodType("2","forestry");
        newTypes.add(forestry);
        GoodType fishing = new UndifferentiatedGoodType("3","fishing");
        newTypes.add(fishing);
        list.addNewSectors(newTypes);
        Assert.assertEquals(7,list.size());

        //start observing now!!
        list.addObserver(o);

        //and now clear!
        list.clearMasterList();
        verify(o,times(1)).theMasterListHasChanged();


    }


}
