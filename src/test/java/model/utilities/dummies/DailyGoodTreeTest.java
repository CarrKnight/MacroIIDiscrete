/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.dummies;

import financial.market.ImmediateOrderHandler;
import financial.market.OrderBookMarket;
import financial.utilities.ShopSetPricePolicy;
import goods.UndifferentiatedGoodType;
import model.MacroII;
import org.junit.Assert;
import org.junit.Test;

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
 * @version 2013-12-18
 * @see
 */
public class DailyGoodTreeTest
{

    @Test
    public void sellTwoOutOfThree()
    {

        MacroII model = new MacroII(System.currentTimeMillis());
        model.start();

        OrderBookMarket market = new OrderBookMarket(UndifferentiatedGoodType.GENERIC);
        market.setOrderHandler(new ImmediateOrderHandler(),model);
        market.setPricePolicy(new ShopSetPricePolicy()); //the seller price is the final price

        //create the tree
        DailyGoodTree tree = new DailyGoodTree(model,3,10,market);
        model.addAgent(tree);

        //create 3 customers
        //customer 1 willing to pay 20$
        Customer customer1 = new Customer(model,20,market);
        model.addAgent(customer1);
        //customer 2 willing to pay 10$
        Customer customer2 = new Customer(model,10,market);
        model.addAgent(customer2);
        //customer 3 willing to pay 5$
        Customer customer3 = new Customer(model,5,market);
        model.addAgent(customer3);

        //the tree should sell only to the first 2 customers
        model.schedule.step(model);

        Assert.assertEquals(1,tree.hasHowMany(UndifferentiatedGoodType.GENERIC));
        Assert.assertEquals(1,customer1.hasHowMany(UndifferentiatedGoodType.GENERIC));
        Assert.assertEquals(1,customer2.hasHowMany(UndifferentiatedGoodType.GENERIC));
        Assert.assertEquals(0,customer3.hasHowMany(UndifferentiatedGoodType.GENERIC));
        Assert.assertEquals(1,market.numberOfAsks());
        Assert.assertEquals(1,market.numberOfBids());
        Assert.assertEquals(20,tree.hasHowMany(UndifferentiatedGoodType.MONEY)); //this might be wrong now that we changed "consumeAll"

        //step it again, it repeats itself
        model.schedule.step(model);
        //both customers and tree destroy old inventory
        Assert.assertEquals(1,tree.hasHowMany(UndifferentiatedGoodType.GENERIC));
        Assert.assertEquals(1,customer1.hasHowMany(UndifferentiatedGoodType.GENERIC));
        Assert.assertEquals(1,customer2.hasHowMany(UndifferentiatedGoodType.GENERIC));
        Assert.assertEquals(0,customer3.hasHowMany(UndifferentiatedGoodType.GENERIC));
        Assert.assertEquals(1,market.numberOfAsks());
        Assert.assertEquals(1,market.numberOfBids());
        //but tree doesn't destroy old cash, so this accumulates
        Assert.assertEquals(40,tree.hasHowMany(UndifferentiatedGoodType.MONEY));


    }


    @Test
    public void sellZeroOutOfThree()
    {

        MacroII model = new MacroII(System.currentTimeMillis());
        model.start();

        OrderBookMarket market = new OrderBookMarket(UndifferentiatedGoodType.GENERIC);
        market.setPricePolicy(new ShopSetPricePolicy()); //the seller price is the final price

        //create the tree
        DailyGoodTree tree = new DailyGoodTree(model,3,100,market);
        model.addAgent(tree);

        //create 3 customers
        //customer 1 willing to pay 20$
        Customer customer1 = new Customer(model,20,market);
        model.addAgent(customer1);
        //customer 2 willing to pay 10$
        Customer customer2 = new Customer(model,10,market);
        model.addAgent(customer2);
        //customer 3 willing to pay 5$
        Customer customer3 = new Customer(model,5,market);
        model.addAgent(customer3);

        //the tree can't sell to anyone
        model.schedule.step(model);

        Assert.assertEquals(3,tree.hasHowMany(UndifferentiatedGoodType.GENERIC));
        Assert.assertEquals(0,customer1.hasHowMany(UndifferentiatedGoodType.GENERIC));
        Assert.assertEquals(0,customer2.hasHowMany(UndifferentiatedGoodType.GENERIC));
        Assert.assertEquals(0,customer3.hasHowMany(UndifferentiatedGoodType.GENERIC));
        Assert.assertEquals(1,market.numberOfAsks());
        Assert.assertEquals(3,market.numberOfBids());
        Assert.assertEquals(0,tree.hasHowMany(UndifferentiatedGoodType.MONEY));

        //step it again, it repeats itself
        model.schedule.step(model);
        //both customers and tree destroy old inventory
        Assert.assertEquals(3,tree.hasHowMany(UndifferentiatedGoodType.GENERIC));
        Assert.assertEquals(0,customer1.hasHowMany(UndifferentiatedGoodType.GENERIC));
        Assert.assertEquals(0,customer2.hasHowMany(UndifferentiatedGoodType.GENERIC));
        Assert.assertEquals(0,customer3.hasHowMany(UndifferentiatedGoodType.GENERIC));
        Assert.assertEquals(1,market.numberOfAsks());
        Assert.assertEquals(3,market.numberOfBids());
        //but tree doesn't destroy old cash, so this accumulates
        Assert.assertEquals(0,tree.hasHowMany(UndifferentiatedGoodType.MONEY));


    }


    @Test
    public void sellThreeOutOfThree()
    {

        MacroII model = new MacroII(System.currentTimeMillis());
        model.start();

        OrderBookMarket market = new OrderBookMarket(UndifferentiatedGoodType.GENERIC);
        market.setOrderHandler(new ImmediateOrderHandler(),model);
        market.setPricePolicy(new ShopSetPricePolicy()); //the seller price is the final price

        //create the tree
        DailyGoodTree tree = new DailyGoodTree(model,3,1,market);
        model.addAgent(tree);

        //create 3 customers
        //customer 1 willing to pay 20$
        Customer customer1 = new Customer(model,20,market);
        model.addAgent(customer1);
        //customer 2 willing to pay 10$
        Customer customer2 = new Customer(model,10,market);
        model.addAgent(customer2);
        //customer 3 willing to pay 5$
        Customer customer3 = new Customer(model,5,market);
        model.addAgent(customer3);

        //the tree sell all three
        model.schedule.step(model);

        Assert.assertEquals(0,tree.hasHowMany(UndifferentiatedGoodType.GENERIC));
        Assert.assertEquals(1,customer1.hasHowMany(UndifferentiatedGoodType.GENERIC));
        Assert.assertEquals(1,customer2.hasHowMany(UndifferentiatedGoodType.GENERIC));
        Assert.assertEquals(1,customer3.hasHowMany(UndifferentiatedGoodType.GENERIC));
        Assert.assertEquals(0,market.numberOfAsks());
        Assert.assertEquals(0,market.numberOfBids());
        Assert.assertEquals(3,tree.hasHowMany(UndifferentiatedGoodType.MONEY));

        //step it again, it repeats itself
        model.schedule.step(model);
        //both customers and tree destroy old inventory
        Assert.assertEquals(0,tree.hasHowMany(UndifferentiatedGoodType.GENERIC));
        Assert.assertEquals(1,customer1.hasHowMany(UndifferentiatedGoodType.GENERIC));
        Assert.assertEquals(1,customer2.hasHowMany(UndifferentiatedGoodType.GENERIC));
        Assert.assertEquals(1,customer3.hasHowMany(UndifferentiatedGoodType.GENERIC));
        Assert.assertEquals(0,market.numberOfAsks());
        Assert.assertEquals(0,market.numberOfBids());
        //but tree doesn't destroy old cash, so this accumulates
        Assert.assertEquals(6,tree.hasHowMany(UndifferentiatedGoodType.MONEY));


    }


}
