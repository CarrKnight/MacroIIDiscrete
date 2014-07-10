/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities;

import org.junit.Assert;
import org.junit.Test;

public class DelayBinTest {


    //size 5


    @Test
    public void size5() throws Exception {

        DelayBin<Integer> fiver = new DelayBin<>(5, -1);
        for (int i = 0; i < 5; i++) {
            int retrieved = fiver.addAndRetrieve(i);
            Assert.assertEquals(-1, retrieved);
        }

        Integer[] in = fiver.peekAll(Integer.class);
        Assert.assertEquals(5,in.length);
        for(int i=0; i<5;i++)
            Assert.assertEquals(i,(int)in[i]);



        //after you have 5 in the bin, you start retrieving old values
        for (int i = 0; i < 5; i++) {
            int retrieved = fiver.addAndRetrieve(100);
            Assert.assertEquals(i, retrieved);
        }

    }


    @Test
    public void size0() throws Exception {

        DelayBin<Integer> nodelay = new DelayBin<>(0, -1);
        for (int i = 0; i < 5; i++) {
            int retrieved = nodelay.addAndRetrieve(i);
            Assert.assertEquals(i, retrieved);
        }

    }


}